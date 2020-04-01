//package com.zzg.myprint_master.util;
//
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.InputStream;
//
//import com.aspose.words.Document;
//import com.aspose.words.License;
//import com.aspose.words.SaveFormat;
///**
// * @author Zhangzhenguo
// * @create 2019/10/11
// * @Email 18311371235@163.com
// * @Describe
// */
//public class WordTwoPdf {
//
//    public static void doc2pdf(String inPath, String outPath) {
////        try {
////            long old = System.currentTimeMillis();
////            File file = new File(outPath); // 新建一个空白pdf文档
////            FileOutputStream os = new FileOutputStream(file);
////            Document doc = new Document(inPath); // Address是将要被转化的word文档
////            doc.save(os, SaveFormat.PDF);// 全面支持DOC, DOCX, OOXML, RTF HTML, OpenDocument, PDF,
////            // EPUB, XPS, SWF 相互转换
////            long now = System.currentTimeMillis();
////            System.out.println("共耗时：" + ((now - old) / 1000.0) + "秒"); // 转化用时
////        } catch (Exception e) {
////            e.printStackTrace();
////        }
//
//
//        try {
//            //doc路径
//            Document document = new Document(inPath);
//            //pdf路径
//            File outputFile = new File(outPath);
//            //操作文档保存
//            document.save(outputFile.getAbsolutePath(), com.aspose.words.SaveFormat.PDF);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//
//}
