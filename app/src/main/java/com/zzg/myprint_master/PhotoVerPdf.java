package com.zzg.myprint_master;

import com.itextpdf.text.Document;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.annotation.SuppressLint;
import android.os.Environment;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * @author Zhangzhenguo
 * @create 2020/10/9
 * @Email 18311371235@163.com
 * @Describe
 */

public class PhotoVerPdf {

    /**
     * 图片转换pdf
     * @return
     */
    public boolean ready() {
        //创建一个文档对象
        Document doc = new Document();
        try {
            File fileName=new File(Environment.getExternalStorageDirectory()+"/测试图片打印.pdf");
            //定义输出文件的位置
            PdfWriter.getInstance(doc, new FileOutputStream(fileName.getAbsoluteFile()));
            //开启文档
            doc.open();
            //设定字体 为的是支持中文
            //BaseFont bfChinese = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
            // Font FontChinese = new Font(bfChinese, 12, Font.NORMAL);
            //向文档中加入图片
//            for (int i = 1; i < 32; i++) {
                //取得图片~~~图片格式：
//                Image jpg1 = Image.getInstance("/sdcard/myImage/" + i + ".png");
                Image jpg1 = Image.getInstance(Environment.getExternalStorageDirectory()+ "/测试图片打印.png");
                //原来的图片的路径
                //获得图片的高度
                float heigth = jpg1.getHeight();
                float width = jpg1.getWidth();
//                System.out.println("heigth" + i + "----" + heigth);
//                System.out.println("width" + i + "-----" + width);
                //合理压缩，h>w，按w压缩，否则按w压缩
                //int percent=getPercent(heigth, width);
                //统一按照宽度压缩
                int percent = getPercent2(heigth, width);
                //设置图片居中显示
                jpg1.setAlignment(Image.MIDDLE);
                //直接设置图片的大小~~~~~~~第三种解决方案，按固定比例压缩
                //jpg1.scaleAbsolute(210.0f, 297.0f);
                //按百分比显示图片的比例
                jpg1.scalePercent(percent);//表示是原来图像的比例;
                //可设置图像高和宽的比例
                //jpg1.scalePercent(50, 100);
                doc.add(jpg1);
//            }
            //关闭文档并释放资源
            doc.close();
            if (fileName.exists()){
                return true;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
//    /**
//     * 第一种解决方案
//     * 在不改变图片形状的同时，判断，如果h>w，则按h压缩，否则在w>h或w=h的情况下，按宽度压缩
//     * @param h
//     * @param w
//     * @return
//     */
//
//    public int getPercent(float h,float w)
//    {
//        int p=0;
//        float p2=0.0f;
//        if(h>w)
//        {
//            p2=297/h*100;
//        }
//        else
//        {
//            p2=210/w*100;
//        }
//        p=Math.round(p2);
//        return p;
//    }

    /**
     * 第二种解决方案，统一按照宽度压缩
     * 这样来的效果是，所有图片的宽度是相等的，自我认为给客户的效果是最好的
     *
     * @param h
     * @param w
     */
    private int getPercent2(float h, float w) {
        int p = 0;
        float p2 = 0.0f;
        p2 = 530 / w * 100;
        p = Math.round(p2);
        return p;
    }
    /**
     * 第三种解决方案，就是直接压缩，不安像素比例，全部压缩到固定值，如210*297
     *
     * @param args
     */
    public void main3(String[] args) {
        PhotoVerPdf pt=new PhotoVerPdf();
        pt.ready();
    }
}

