package com.roy.jit;

/**
 * 设置 VM 参数：-XX:+PrintCompilation -XX:+UnlockDiagnosticVMOptions -XX:+PrintInlining -XX:+PrintCompilation -XX:+UnlockDiagnosticVMOptions
 * -XX:+PrintCompilation //在控制台打印编译过程信息
 * -XX:+UnlockDiagnosticVMOptions //解锁对JVM进行诊断的选项参数。默认是关闭的，开启
 * 后支持一些特定参数对JVM进行诊断
 * -XX:+PrintInlining //将内联方法打印出来
 */
public class CompDemo {
    private int add1(int x1,int x2,int x3,int x4){
        return add2(x1,x2)+ add2(x3,x4);
    }
    private int add2(int x1, int x2){
        return x1+x2;
    }
    private int add(int x1,int x2,int x3,int x4){
        return x1+x2+x3+x4;
    }

    public static void main(String[] args) {
        CompDemo compDemo = new CompDemo();
        //超过方法调用计数器的阈值 100000 次，才会进入 JIT 实时编译，进行内联优化。
        for (int i = 0; i < 1000000; i++) {
//            int i1 = compDemo.add2(1, 2);
//            int i2 = compDemo.add2(3, 4);
//            int i3 = i1+i2;
            compDemo.add1(1,2,3,4);
        }
    }
}