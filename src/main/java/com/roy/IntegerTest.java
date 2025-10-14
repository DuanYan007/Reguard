package com.roy;

import java.lang.reflect.Field;

public class IntegerTest {

    public static void main(String[] args) throws Exception {
        Class<?> integerCacheClazz = Integer.class.getDeclaredClasses()[0];
        Field cacheField = integerCacheClazz.getDeclaredField("cache");
        cacheField.setAccessible(true);

        Integer[] newCache = (Integer[]) cacheField.get(integerCacheClazz);
        newCache[130] = newCache[131]; // 2 替换成 3
        System.out.println(newCache[130]+";"+newCache[131]);



        Integer a = 1;
        Integer b = a + a;
        System.out.println(a +"+"+a +"="+b);
    }
}
