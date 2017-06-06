package com.example.wangweijun1.retrofit_xxx;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.retrofit.HTTPSUtils;
import com.example.retrofit.SimpleMockService;
import com.example.retrofit.SimpleService;
import com.example.retrofit.StoreService;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.OkHttpUtils;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Url;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PreloadAppPresenter.getInstance(getApplicationContext()).registerObserverForStore();
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

    public void testBaiduHttps(View v) {
        try {
            SimpleService.testBaiduHttps();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void testHttpsUseCerti(View v) {
        HTTPSUtils customTrust = new HTTPSUtils(this);

        try {
            customTrust.run();

        } catch (Exception e) {
            e.printStackTrace();
        }
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

    public void testOkhttpClientSingleInstance(View v) {
        StoreService.testOkhttpClientSingleInstance();

//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        int count = 49;
//        for (int i=0; i<count; i++) {
//            StoreService.testOkhttpClientSingleInstance();
//        }


    }


    public void testOkhttpClientMutipart(View v) {
        StoreService.testOkhttpClientMutipart();

//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        int count = 49;
//        for (int i=0; i<count; i++) {
//            StoreService.testOkhttpClientMutipart();
//        }


    }

    public void testHttpDns(View v) {
        StoreService.testHttpDns();
    }
    public static final String PACKAGENAME = "packageName";
    public static final String VERSIONCODE = "versionCode";
    public static final String TYPE_START_UPDATE_SERVICE = "type_start_update_service";
    public void startTvService(View v) {
        // package: name='com.gameloft.android.HEP.GloftA8HP' versionCode='21000'
        Intent serviceIntent = new Intent();
        Map<String,Integer> map = getAllLocalSimpleBaseAppsMap(getApplicationContext());
        serviceIntent.putExtra(PACKAGENAME, "com.gameloft.android.HEP.GloftA8HP");

        int code = map.get("com.gameloft.android.HEP.GloftA8HP");
        Log.i("wang", "code:"+code);
        serviceIntent.putExtra(VERSIONCODE, code);
        serviceIntent.putExtra(TYPE_START_UPDATE_SERVICE, 5);
        serviceIntent.setComponent(new ComponentName("com.letv.tvos.appstore", "com.letv.tvos.appstore.service.PackageUpdateInfoService"));
        startService(serviceIntent);
    }

    public static Map<String,Integer> getAllLocalSimpleBaseAppsMap(Context context) {
        String selfPackageName = context.getPackageName();
        Map<String,Integer> params=null;
        PackageManager pm = context.getPackageManager();
        synchronized (context) {
            List<PackageInfo> packages = pm.getInstalledPackages(0);
            if (null != packages) {
                params = new HashMap<String, Integer>();
                for (PackageInfo packageInfo : packages) {
                    if (packageInfo.packageName.equals("com.gameloft.android.HEP.GloftA8HP")) {
                        params.put(packageInfo.packageName, packageInfo.versionCode);
                        Log.i("wang", packageInfo.packageName + ", " + packageInfo.versionCode + "");
                    }
                }
            }
        }
        return params;
    }





    private boolean addPermission(Context context, String permission) {
        if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }
}
