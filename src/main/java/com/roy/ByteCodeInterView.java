package com.roy;

import org.junit.Test;

public class ByteCodeInterView {

    //包装类对象的缓存问题
    @Test
    public void typeTest(){
        Integer i1 = 10;
        Integer i2 = 10;
        System.out.println(i1 == i2);//true

        Integer i3 = 128;
        Integer i4 = 128;
        System.out.println(i3 == i4);//false

        Boolean b1 = true;
        Boolean b2 = true;
        System.out.println(b1 == b2);//true

        Double d1=1.00;
        Double d2=1.00;
        System.out.println(d1==d2);//false
    }

    public int mathTest(){
        int k = 1 ;
        k = k++;
        return k;
    }

    public int inc(){
        int x;
        try{
            x=1;
        }catch (Exception e){
            x = 2;
        }finally {
            x = 3;
        }
        return x;
    }

}










