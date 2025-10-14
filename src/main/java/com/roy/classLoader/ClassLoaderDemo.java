package com.roy.classLoader;

public class ClassLoaderDemo {

    public static void main(String[] args) throws Exception{
        //普通调用方式，class文件是可以被反编译的，不安全。
//        SecretUtil secretUtil = new SecretUtil();
//        System.out.println(secretUtil.hello());
        //从加密后的myclass文件中加载测试类。
        MyClassLoader myClassLoader = new MyClassLoader("/Users/roykingw/DevCode/JVMDemo/target/classes");
        Class<?> clazz = myClassLoader.loadClass("com.roy.classLoader.SecretUtil");
        Object obj = clazz.newInstance();
        Object result = clazz.getMethod("hello", null).invoke(obj);
        System.out.println(result);
        //沙箱保护机制，不允许加载java.开头的类
//        Class<?> clazz2 = myClassLoader.loadClass("java.Test");
//        System.out.println(clazz2);

    }
}
