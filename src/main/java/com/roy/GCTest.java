package com.roy;

import java.util.ArrayList;
import java.util.List;

public class GCTest {
    public static void main(String[] args) throws InterruptedException {
        List l = new ArrayList<>();
        for(int i = 0 ; i < 100_0000 ; i ++){
            l.add(new String("dddddd"));
            Thread.sleep(100);
        }
    }
}
