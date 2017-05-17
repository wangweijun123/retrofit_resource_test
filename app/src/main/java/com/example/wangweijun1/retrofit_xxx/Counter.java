package com.example.wangweijun1.retrofit_xxx;

import java.util.Random;

/**
 * Created by wangweijun1 on 2017/5/15.
 */

public class Counter {

    public static  int count = 0;

    public static void inc() {

        //这里延迟1毫秒，使得结果明显
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
        }
        synchronized(Counter.class) {
            count++;
        }
    }

    public static void main() {
        int rr = 120*60*120;
        System.out.println("rr:"+rr);
        int deallmin = new Random().nextInt(rr);
//        int result = minute * 60 * 1000;
        long ll = deallmin;
        System.out.println("deallmin:"+deallmin + ", ll:"+ll);
//同时启动1000个线程，去进行i++计算，看看实际结果

        for (int i = 0; i < 1000; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Counter.inc();
                }
            }).start();
        }

        //这里每次运行的值都有可能不同,可能为1000
        try {
            Thread.sleep(3000);
            System.out.println("运行结果:Counter.count=" + Counter.count);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
