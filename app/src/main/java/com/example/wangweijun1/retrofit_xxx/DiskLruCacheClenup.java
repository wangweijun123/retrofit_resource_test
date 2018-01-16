package com.example.wangweijun1.retrofit_xxx;

import android.util.Log;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import retrofit2.Retrofit;

/**
 * Created by wangweijun on 2018/1/13.
 */

public class DiskLruCacheClenup {

    private final Executor executor = new ThreadPoolExecutor(0, 1, 10L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>());


    private final Runnable cleanupRunnable = new Runnable() {
        public void run() {
            synchronized (DiskLruCacheClenup.this) {
                Log.i(Retrofit.TAG," clean start ...."+Thread.currentThread().getId());
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Log.i(Retrofit.TAG," clean end");
            }
        }
    };

    public void run() {
        Log.i(Retrofit.TAG,"run "+Thread.currentThread().getId());
        // 同一个runnable，execute 两遍，run 方法回执行两遍，并且在runnable在队列中排队
        executor.execute(cleanupRunnable);
    }
}
