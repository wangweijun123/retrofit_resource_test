package com.example.wangweijun1.retrofit_xxx;

import android.app.Application;

/**
 * Created by wangweijun on 2018/1/30.
 */

public class MyApplication extends Application {

    public static MyApplication myApplication;
    @Override
    public void onCreate() {
        super.onCreate();
        myApplication = this;
    }
}
