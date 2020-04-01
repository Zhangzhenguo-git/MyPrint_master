//package com.zzg.myprint_master.util;
//
//
//import android.os.FileUtils;
//
//import org.apache.poi.hwpf.HWPFDocument;
//import org.apache.poi.hwpf.converter.PicturesManager;
//import org.apache.poi.hwpf.converter.WordToHtmlConverter;
//import org.apache.poi.hwpf.usermodel.Picture;
//import org.apache.poi.hwpf.usermodel.PictureType;
//import org.w3c.dom.Document;
//
//import java.io.BufferedWriter;
//import java.io.ByteArrayOutputStream;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.OutputStreamWriter;
//import java.util.List;
//
//import javax.xml.parsers.DocumentBuilderFactory;
//import javax.xml.transform.OutputKeys;
//import javax.xml.transform.Transformer;
//import javax.xml.transform.TransformerFactory;
//import javax.xml.transform.dom.DOMSource;
//import javax.xml.transform.stream.StreamResult;
//
//
///**
// * 将Office文档转换为PDF文档
// *
// * @author 王文路
// * @date 2015-7-22
// */
//public class Office2PDFConverter {
//
//    /**
//     * 开始
//     * @param inFilePath
//     * @param outFilePath
//     */
//	public static void doWordPdf(String inFilePath,String outFilePath){
//		convert2Html(inFilePath , outFilePath);
//    }
//		/**
//		 * word文档转成html格式
//		 * */
//		public static void convert2Html(String fileName, String outPutFile){
//			HWPFDocument wordDocument = null;
//			try {
//				wordDocument = new HWPFDocument(new FileInputStream(fileName));
//				WordToHtmlConverter wordToHtmlConverter = new WordToHtmlConverter(
//						DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument());
//				//设置图片路径
//				wordToHtmlConverter.setPicturesManager(new PicturesManager() {
//					public String savePicture(byte[] content,
//											  PictureType pictureType, String suggestedName,
//											  float widthInches, float heightInches) {
//						String name = docName.substring(0, docName.indexOf("."));
//						return name + "/" + suggestedName;
//					}
//				});
//				//保存图片
//				List<Picture> pics = wordDocument.getPicturesTable().getAllPictures();
//				if (pics != null) {
//					for (int i = 0; i < pics.size(); i++) {
//						Picture pic = (Picture) pics.get(i);
//						System.out.println(pic.suggestFullFileName());
//						try {
//							String name = docName.substring(0, docName.indexOf("."));
//							String file = savePath + name + "/"
//									+ pic.suggestFullFileName();
//							Fileutil.makeDirs(file);
//							pic.writeImageContent(new FileOutputStream(file));
//						} catch (FileNotFoundException e) {
//							e.printStackTrace();
//						}
//					}
//				}
//				wordToHtmlConverter.processDocument(wordDocument);
//				Document htmlDocument = wordToHtmlConverter.getDocument();
//				ByteArrayOutputStream out = new ByteArrayOutputStream();
//				DOMSource domSource = new DOMSource(htmlDocument);
//				StreamResult streamResult = new StreamResult(out);
//				TransformerFactory tf = TransformerFactory.newInstance();
//				Transformer serializer = tf.newTransformer();
//				serializer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
//				serializer.setOutputProperty(OutputKeys.INDENT, "yes");
//				serializer.setOutputProperty(OutputKeys.METHOD, "html");
//				serializer.transform(domSource, streamResult);
//				out.close();
//				//保存html文件
//				writeFile(new String(out.toByteArray()), outPutFile);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
//		/**
//		 * 将html文件保存到sd卡
//		 * */
//		public static void writeFile(String content,String path){
//			FileOutputStream fos = null;
//			BufferedWriter bw = null;
//			try {
//				File file = new File(path);
//				if (!file.exists()) {
//					file.createNewFile();
//				}
//				fos = new FileOutputStream(file);
//				bw = new BufferedWriter(new OutputStreamWriter(fos, "utf-8"));
//				bw.write(content);
//			} catch (FileNotFoundException fnfe) {
//				fnfe.printStackTrace();
//			} catch (IOException ioe) {
//				ioe.printStackTrace();
//			} finally {
//				try {
//					if (bw != null)
//						bw.close();
//					if (fos != null)
//						fos.close();
//				} catch (IOException ie) {
//				}
//			}
//		}
//}
