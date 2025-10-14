package com.roy.jit;

/**
 * -XX:+EliminateLocks 开启锁消除（jdk1.8默认开启）
 * -XX:-EliminateLocks 关闭锁消除
 */
public class LockElisionDemo {
    public static String BufferString(String s1,String s2){
        StringBuffer sb = new StringBuffer();
        sb.append(s1);
        sb.append(s2);
        return sb.toString();
    }

    public static String BuilderString(String s1, String s2){
        StringBuilder sb = new StringBuilder();
        sb.append(s1);
        sb.append(s2);
        return sb.toString();
    }

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 100000000; i++) {
            BufferString("aaaa","bbbbbb");
        }
        System.out.println("StringBuffer耗时："+(System.currentTimeMillis()-startTime));

        long startTime2 = System.currentTimeMillis();
        for (int i = 0; i < 100000000; i++) {
            BuilderString("aaaaa","bbbbbb");
        }
        System.out.println("StringBuilder耗时："+(System.currentTimeMillis()-startTime2));
    }
}
