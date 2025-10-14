package com.roy;

import org.openjdk.jol.info.ClassLayout;

/**
 * 对象的内存布局。
 * 这个布局就跟操作系统有关系了。
 */
public class JOLDemo {
    private String id;
    private String name;
    public static void main(String[] args) {
        JOLDemo o = new JOLDemo();
        System.out.println(ClassLayout.parseInstance(o).toPrintable());

        synchronized (o){
            System.out.println(ClassLayout.parseInstance(o).toPrintable());
        }
    }
}
