package com.roy;

//JVM指定栈空间大小： -Xss128K(默认 1M)
public class StackOverFlowTest {
    static int count = 0;

    static void redo(){
        count ++;
        redo();
    }

    public static void main(String[] args) {
        try{
            redo();
        }catch(Throwable t){
            t.printStackTrace();
            System.out.println(count);
        }
    }
}
