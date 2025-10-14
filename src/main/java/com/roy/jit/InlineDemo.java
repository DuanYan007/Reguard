package com.roy.jit;

/**
 * 设置 VM 参数：
 * -XX:+PrintCompilation -XX:+UnlockDiagnosticVMOptions -XX:+PrintInlining -XX:+PrintCompilation -XX:+UnlockDiagnosticVMOptions
 * -XX:+PrintCompilation //在控制台打印编译过程信息
 * -XX:+UnlockDiagnosticVMOptions //解锁对JVM进行诊断的选项参数。默认是关闭的，开启
 * 后支持一些特定参数对JVM进行诊断
 * -XX:+PrintInlining //将内联方法打印出来
 *
 * -Xint 解释执行
 * -Xcomp 编译执行
 * -XX:TieredStopAtLevel=0  强制使用C1编译
 * -XX:TieredStopAtLevel=5  强制使用C1+C2编译
 */
public class InlineDemo {
    public static void foo(Object obj){
        if(obj !=null){
            System.out.println("do something");
        }
    }

    //方法内联之后会继续进行无用代码消除
    public static void testInline(){
        Object obj= null;
        foo(obj);

//        if(obj !=null){
//            System.out.println("do something");
//        }
    }

    public static void main(String[] args) {
        long l = System.currentTimeMillis();
        for (int i = 0; i < 10000000; i++) {
            testInline();
        }
        System.out.println(">>>>>>>>"+(System.currentTimeMillis()-l));
    }
}
