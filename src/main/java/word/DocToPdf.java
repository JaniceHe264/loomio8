package word;


import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.converter.PicturesManager;
import org.apache.poi.hwpf.converter.WordToFoConverter;
import org.apache.poi.hwpf.usermodel.Picture;
import org.apache.poi.hwpf.usermodel.PictureType;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.xmlgraphics.io.URIResolverAdapter;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.swing.filechooser.FileSystemView;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;


/**
 * @ProjectName: word-pdf-image
 * @Package: word
 * @ClassName: DocToPdf
 * @Author: guohailong
 * @Description: doc转换pdf
 * @Date: 2021/4/22 0:39
 * @Version: 1.0
 */

public class DocToPdf {

    /**
     * 获取classPath
     */
    private static final String classPath = DocToPdf.class.getResource("/").getPath();

    /**
     * fop配置文件位置
     */
    private static final String CONFIG = classPath + File.separator + "fop" + File.separator + "fop.xml";


    /**
     * doc转xml，即fop可以识别的xml。专用名字为XSL-FO
     */
    public String toXML(String docFilePath, String xmlPath) {
        try {
            POIFSFileSystem nPOIFSFileSystem = new POIFSFileSystem(new File(docFilePath));
            HWPFDocument nHWPFDocument = new HWPFDocument(nPOIFSFileSystem);
            WordToFoConverter nWordToHtmlConverter = new WordToFoConverter(
                    DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument());
            // 临时路径，保存doc文档中的图片
            PicturesManager nPicturesManager = new PicturesManager() {
                public String savePicture(byte[] arg0, PictureType arg1, String arg2, float arg3, float arg4) {
                    return classPath + File.separator + "temp" + File.separator + "images" + File.separator + arg2;
                }
            };
            nWordToHtmlConverter.setPicturesManager(nPicturesManager);
            nWordToHtmlConverter.processDocument(nHWPFDocument);
            String nTempPath = classPath + File.separator + "temp" + File.separator + "images" + File.separator;
            File nFile = new File(nTempPath);
            if (!nFile.exists()) {
                nFile.mkdirs();
            }
            for (Picture nPicture : nHWPFDocument.getPicturesTable().getAllPictures()) {
                nPicture.writeImageContent(new FileOutputStream(nTempPath + nPicture.suggestFullFileName()));
            }
            Document nHtmlDocument = nWordToHtmlConverter.getDocument();
            OutputStream nByteArrayOutputStream = new FileOutputStream(xmlPath);
            DOMSource nDOMSource = new DOMSource(nHtmlDocument);
            StreamResult nStreamResult = new StreamResult(nByteArrayOutputStream);

            TransformerFactory nTransformerFactory = TransformerFactory.newInstance();
            Transformer nTransformer = nTransformerFactory.newTransformer();

            nTransformer.setOutputProperty(OutputKeys.ENCODING, "GBK");
            nTransformer.setOutputProperty(OutputKeys.INDENT, "YES");
            nTransformer.setOutputProperty(OutputKeys.METHOD, "xml");

            nTransformer.transform(nDOMSource, nStreamResult);
            nByteArrayOutputStream.close();

            return "";

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }


    /**
     * 得到桌面路径
     *
     * @return
     */
    public static String getDesktopPath() {
        return FileSystemView.getFileSystemView().getHomeDirectory().getPath();
    }

    /**
     * @param xmlPath 输入，fop可以识别的xml文件
     * @param pdfPath 输出，生成的pdf路径
     * @throws SAXException
     * @throws TransformerException
     */
    public void xmlToPDF(String xmlPath, String pdfPath) throws SAXException, TransformerException {
        // Step 1: Construct a FopFactory by specifying a reference to the configuration file
        // (reuse if you plan to render multiple documents!)
        FopFactory fopFactory = null;
        new URIResolverAdapter(new URIResolver() {
            public Source resolve(String href, String base) throws TransformerException {
                try {
                    URL url = new URL(href);
                    URLConnection connection = url.openConnection();
                    connection.setRequestProperty("User-Agent", "whatever");
                    return new StreamSource(connection.getInputStream());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        );
        OutputStream out = null;
        try {

            fopFactory = FopFactory.newInstance(new File(CONFIG));

            // Step 2: Set up output stream.
            // Note: Using BufferedOutputStream for performance reasons (helpful with FileOutputStreams).

            out = new BufferedOutputStream(new FileOutputStream(pdfPath));

            // Step 3: Construct fop with desired output format
            Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, out);

            // Step 4: Setup JAXP using identity transformer
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer(); // identity transformer

            // Step 5: Setup input and output for XSLT transformation
            // Setup input stream
            Source src = new StreamSource(xmlPath);

            // Resulting SAX events (the generated FO) must be piped through to FOP
            Result res = new SAXResult(fop.getDefaultHandler());
            // Step 6: Start XSLT transformation and FOP processing
            transformer.transform(src, res);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            //Clean-up
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws SAXException, TransformerException {
        //doc源文件路径
        // String docFilePath = PathUtil.getClassPath() + File.separator + "file" + File.separator + "test.doc";
        String docFilePath = getDesktopPath() + File.separator + "github简介.doc";
        //生成的XSL-FO文件路径
        String fileXmlPath = classPath + File.separator + "temp" + File.separator + "temp.xml";
        //生成的pdf文件路径
        String filePdfPath = getDesktopPath() + File.separator + "github简介.pdf";

        new DocToPdf().toXML(docFilePath, fileXmlPath);
        new DocToPdf().xmlToPDF(fileXmlPath, filePdfPath);
    }
}
