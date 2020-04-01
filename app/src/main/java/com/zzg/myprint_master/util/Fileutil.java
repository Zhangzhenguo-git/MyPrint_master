package com.zzg.myprint_master.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author Zhangzhenguo
 * @create 2019/10/12
 * @Email 18311371235@163.com
 * @Describe
 */
public class Fileutil {

    public static void makeDirs(String file) {
        try {
            FileOutputStream createStream=new FileOutputStream(file);
            File file1=new File(file);
            if (!file1.exists()){
                file1.createNewFile();
            }



        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
