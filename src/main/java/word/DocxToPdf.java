package word;

import fr.opensagres.xdocreport.utils.StringUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.poi.xwpf.converter.pdf.PdfConverter;
import org.apache.poi.xwpf.converter.pdf.PdfOptions;
import org.apache.poi.xwpf.usermodel.*;

import javax.swing.filechooser.FileSystemView;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ProjectName: word-pdf-image
 * @Package: word
 * @ClassName: DocxToPdf
 * @Author: guohailong
 * @Description: docx转换成pdf
 * @Date: 2021/4/22 0:29
 * @Version: 1.0
 */
public class DocxToPdf {

    /**
     * 将word文档， 转换成pdf, 中间替换掉变量
     *
     * @param source  源为word文档， 必须为docx文档
     * @param target  目标输出
     * @param params  需要替换的变量
     * @param options PdfOptions.create().fontEncoding( "windows-1250" ) 或者其他
     * @throws Exception
     */
    public static void wordConverterToPdf(InputStream source, OutputStream target, PdfOptions options, Map<String, String> params) throws Exception {
        XWPFDocument doc = new XWPFDocument(source);
        paragraphReplace(doc.getParagraphs(), params);
        for (XWPFTable table : doc.getTables()) {
            for (XWPFTableRow row : table.getRows()) {
                for (XWPFTableCell cell : row.getTableCells()) {
                    paragraphReplace(cell.getParagraphs(), params);
                }
            }
        }
        PdfConverter.getInstance().convert(doc, target, options);
    }

    /**
     * 替换段落中内容
     */
    private static void paragraphReplace(List<XWPFParagraph> paragraphs, Map<String, String> params) {
        if (MapUtils.isNotEmpty(params)) {
            for (XWPFParagraph p : paragraphs) {
                for (XWPFRun r : p.getRuns()) {
                    String content = r.getText(r.getTextPosition());
                    if (StringUtils.isNotEmpty(content) && params.containsKey(content)) {
                        r.setText(params.get(content), 0);
                    }
                }
            }
        }
    }

    /**
     * 得到桌面路径
     *
     * @return
     */
    public static String getDesktopPath() {
        return FileSystemView.getFileSystemView().getHomeDirectory().getPath();
    }


    public static void main(String[] args) {
        //从桌面读取docx文档，输出到桌面pdf文件
        String filepath = getDesktopPath() + File.separator + "github简介.docx";
        String outpath = getDesktopPath() + File.separator + "github简介.pdf";

        InputStream source;
        OutputStream target;
        try {
            source = new FileInputStream(filepath);
            target = new FileOutputStream(outpath);
            Map<String, String> params = new HashMap<String, String>();
            PdfOptions options = PdfOptions.create();
            wordConverterToPdf(source, target, options, params);
            System.out.println("docx转换pdf完成");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
