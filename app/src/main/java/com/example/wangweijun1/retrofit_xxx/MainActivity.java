package com.example.wangweijun1.retrofit_xxx;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.example.retrofit.SimpleService;
import com.example.retrofit.StoreService;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (addPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }
    }


    public void getByHttpsSync(View v) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    SimpleService.syncRequest();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * 同步请求
     * @param v
     */
    public void syncRequest(View v) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    StoreService.doGetSync();
//                    SimpleService.syncRequest();
//                    SimpleService2.syncRequestString();
//                    MyService.test();
//                    MyService.doGetSync();
//                    MyService.testByQueryMap();
//                    MyService.doPost();
//                    MyService.testPostFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void doGetByMapAndHeaders(View v) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    StoreService.doGetByMapAndHeaders();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void doPost(View v) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    StoreService.doPost();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    public void doPostFileAndParams(View v) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    StoreService.doPostFileAndParams();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private boolean addPermission(Context context, String permission) {
        if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }
}
