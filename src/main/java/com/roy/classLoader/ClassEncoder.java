package com.roy.classLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

//Class文件加密
public class ClassEncoder {

    public static void main(String[] args) throws Exception {
        FileInputStream fis = new FileInputStream("/Users/roykingw/DevCode/JVMDemo/target/classes/com/roy/classLoader/SecretUtil.class");

        File targetFile = new File("/Users/roykingw/DevCode/JVMDemo/target/classes/com/roy/classLoader/SecretUtil.myclass");
        if(targetFile.exists()) {
            targetFile.delete();
        }
        FileOutputStream fos = new FileOutputStream(targetFile);

        int code = 0;
        fos.write(1);
        while((code = fis.read())!= -1 ) {
            fos.write(code);
        }
        fis.close();
        fos.close();
        System.out.println("文件转换完成");
    }
}
