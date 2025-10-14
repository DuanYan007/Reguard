package com.roy;

/**
 * 测试解释器模式和 JIT编译模式
 * -Xint 解释器模式 3682
 * -Xcomp 编译模式 190
 *  * -XX:TieredStopAtLevel=0  强制使用C1编译 3938
 *  * -XX:TieredStopAtLevel=5  强制使用C1+C2编译 208
 * -Xmixed 混合模式(默认) 192
 */
public class EngineTest {
    public static void main(String[] args) {

        long start = System.currentTimeMillis();

        testPrimeNumber(1000000);

        long end = System.currentTimeMillis();

        System.out.println("花费的时间为：" + (end - start));

    }

    public static void testPrimeNumber(int count){
        for (int i = 0; i < count; i++) {
            //计算100以内的质数
            label:for(int j = 2;j <= 100;j++){
                for(int k = 2;k <= Math.sqrt(j);k++){
                    if(j % k == 0){
                        continue label;
                    }
                }
                //System.out.println(j);
            }

        }
    }
}
