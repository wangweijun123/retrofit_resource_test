package com.example.wangweijun1.retrofit_xxx;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.example.retrofit.SimpleMockService;
import com.example.retrofit.SimpleService;
import com.example.retrofit.StoreService;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.net.ssl.HttpsURLConnection;

import retrofit2.http.Url;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (addPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }
        URL url = null;
        try {
            url = new URL("xxxx");
            HttpsURLConnection urlConnection =  (HttpsURLConnection)url.openConnection();
            urlConnection.disconnect();;
        } catch (Exception e) {
            e.printStackTrace();
        }

        Handler handler;

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


    public void doGetAsync(View v) {
        try {
            StoreService.doGetAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void cancelCall(View v) {
        try {
            StoreService.cancelCall();
        } catch (IOException e) {
            e.printStackTrace();
        }
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


    public void SimpleMockService(View v) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    SimpleMockService.main();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    public void customconverterFactory(View v) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    StoreService.customconverterFactory();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    public void returnVoidconverterFactory(View v) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    StoreService.returnVoidconverterFactory();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    public void toStringConverterFactory(View v) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    StoreService.toStringConverterFactory();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

//    public void JsonAndXmlConverters(View v) {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    JsonAndXmlConverters.main();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();
//    }


    public void doPostForJson(View v) {
        StoreService.doPostForJson();
    }

    public void volatileTest(View v) {
        Counter.main();
    }


    private boolean addPermission(Context context, String permission) {
        if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }
}
