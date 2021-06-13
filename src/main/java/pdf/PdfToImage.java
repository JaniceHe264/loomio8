package pdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.imageio.ImageIO;
import javax.swing.filechooser.FileSystemView;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @ProjectName: word-pdf-image
 * @Package: pdf
 * @ClassName: PdfToImage
 * @Author: guohailong
 * @Description: pdf转换成图片
 * @Date: 2021/4/22 0:29
 * @Version: 1.0
 */


/**
 * @ProjectName: ruoyi
 * @Package: com.ruoyi.web.utils
 * @ClassName: PdfToImage
 * @Author: guohailong
 * @Description:
 * @Date: 2021/4/21 0:29
 * @Version: 1.0
 */
public class PdfToImage {


    /**
     * 得到桌面路径
     *
     * @return
     */
    private static String getDesktopPath() {
        return FileSystemView.getFileSystemView().getHomeDirectory().getPath();
    }

    //输出调用
    public static void main(String[] args) {
        //PDF转图片 （单张）
        //pdfToImagePath(filePath);
        //PDF转长图(目前30张以内无压力)
        String filePath = getDesktopPath() + File.separator + "github简介.pdf";
        String outPath = getDesktopPath() + File.separator + "github简介.jpg";
        pdfToOneImage(filePath, outPath);
        System.out.println("pdf转换图片完成");
    }

    /**
     * 将PDF按页数每页转换成一个jpg图片
     *
     * @param filePath
     * @return
     */
    public static List<String> pdfToImagePath(String filePath) {
        List<String> list = new ArrayList<String>();
        String fileDirectory = filePath.substring(0, filePath.lastIndexOf("."));//获取去除后缀的文件路径

        String imagePath;
        File file = new File(filePath);
        try {
            File f = new File(fileDirectory);
            if (!f.exists()) {
                f.mkdir();
            }
            PDDocument doc = PDDocument.load(file);
            PDFRenderer renderer = new PDFRenderer(doc);
            int pageCount = doc.getNumberOfPages();
            for (int i = 0; i < pageCount; i++) {
                // 方式1,第二个参数是设置缩放比(即像素)
                // BufferedImage image = renderer.renderImageWithDPI(i, 296);
                // 方式2,第二个参数是设置缩放比(即像素)
                BufferedImage image = renderer.renderImage(i, 1.25f);  //第二个参数越大生成图片分辨率越高，转换时间也就越长
                imagePath = fileDirectory + File.separator + i + ".jpg";
                ImageIO.write(image, "jpg", new File(imagePath));
                list.add(imagePath);
            }
            doc.close();//关闭文件,不然该pdf文件会一直被占用。
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * @param filePath
     * @param outPath
     * @Description pdf转成一张图片
     */
    public static void pdfToOneImage(String filePath, String outPath) {
        try {
            InputStream is = new FileInputStream(filePath);
            PDDocument pdf = PDDocument.load(is);
            int actSize = pdf.getNumberOfPages();
            if (actSize > 30) {
                System.err.println("pdf图片太多,可能导致Java heap space");
                return;
            }
            List<BufferedImage> picList = new ArrayList<BufferedImage>();
            for (int i = 0; i < actSize; i++) {
                BufferedImage image = new PDFRenderer(pdf).renderImageWithDPI(i, 200, ImageType.RGB);
                picList.add(image);
            }
            picFixY(picList, outPath);
            pdf.close();
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 将宽度相同的图片，竖向追加在一起 ##注意：宽度必须相同
     *
     * @param picList 文件流数组
     * @param outPath 输出路径
     */
    private static void picFixY(List<BufferedImage> picList, String outPath) {// 纵向处理图片
        if (picList == null || picList.size() == 0) {
            return;
        }
        try {
            int height = 0, // 总高度
                    width = 0, // 总宽度
                    _height = 0, // 临时的高度 , 或保存偏移高度
                    __height = 0, // 临时的高度，主要保存每个高度
                    picNum = picList.size();// 图片的数量
            int[] heightArray = new int[picNum]; // 保存每个文件的高度
            BufferedImage buffer = null; // 保存图片流
            List<int[]> imgRGB = new ArrayList<int[]>(); // 保存所有的图片的RGB
            int[] _imgRGB; // 保存一张图片中的RGB数据
            for (int i = 0; i < picNum; i++) {
                buffer = picList.get(i);
                heightArray[i] = _height = buffer.getHeight();// 图片高度
                if (i == 0) {
                    width = buffer.getWidth();// 图片宽度
                }
                height += _height; // 获取总高度
                _imgRGB = new int[width * _height];// 从图片中读取RGB
                _imgRGB = buffer.getRGB(0, 0, width, _height, _imgRGB, 0, width);
                imgRGB.add(_imgRGB);
            }
            _height = 0; // 设置偏移高度为0
            // 生成新图片
            BufferedImage imageResult = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            for (int i = 0; i < picNum; i++) {
                __height = heightArray[i];
                if (i != 0) _height += __height; // 计算偏移高度
                imageResult.setRGB(0, _height, width, __height, imgRGB.get(i), 0, width); // 写入流中
            }
            File outFile = new File(outPath);
            ImageIO.write(imageResult, "jpg", outFile);// 写图片
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

