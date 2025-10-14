package com.roy.jit;

/**
 * 逃逸分析-》栈上分配 -》 标量替换
 * -XX:-DoEscapeAnalysis 关闭逃逸分析
 * -XX:-EliminateAllocations 关闭标量替换
 * -XX:+PrintGC 打印 GC 日志
 */
public class EscapeAnalysisTest {
    public static void main(String[] args) throws InterruptedException {
        long start = System.currentTimeMillis();
        for (int i = 0; i < 10000000; i++) {
            allocate();
        }
        System.out.println("运行耗时："+(System.currentTimeMillis()-start));
        Thread.sleep(6000000);
    }

    static void allocate(){
        MyObject myObject = new MyObject(2024,2024.6);
    }

    static class MyObject {
        int a;
        double b;

        MyObject(int a,double b){
            this.a = a;
            this.b = b;
        }
    }
}
