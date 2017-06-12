package com.example.wangweijun1.retrofit_xxx;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.retrofit.Crawler;
import com.example.retrofit.DynamicBaseUrl;
import com.example.retrofit.ErrorHandlingAdapter;
import com.example.retrofit.HTTPSUtils;
import com.example.retrofit.SimpleMockService;
import com.example.retrofit.SimpleService;
import com.example.retrofit.StoreService;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

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
//                    SimpleService.httpsRequest();
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


    /**
     * 异步请求
     * @param v
     */
    public void doGetAsync(View v) {
        try {
            StoreService.doGetAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 取消请求
     * @param v
     */
    public void cancelCall(View v) {
        try {
            StoreService.cancelCall();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 测试https
     * @param v
     */
    public void getByHttpsSync(View v) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    SimpleService.httpsRequest();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * 测试百度 https
     * @param v
     */
    public void testBaiduHttps(View v) {
        try {
            SimpleService.testBaiduHttps();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 客户端使用证书测试 https
     * @param v
     */
    public void testHttpsUseCerti(View v) {
        HTTPSUtils customTrust = new HTTPSUtils(this);

        try {
            customTrust.run();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 查询参数与headers使用map get请求
     * @param v
     */
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

    /**
     * post 请求
     * @param v
     */
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

    /**
     * post 文件与参数
     * @param v
     */
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

    public void doPostJson2(View v) {
        StoreService.doPostJson2();
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
//        Map<String,Integer> map = getAllLocalSimpleBaseAppsMap(getApplicationContext());
        serviceIntent.putExtra(PACKAGENAME, "com.gameloft.android.HEP.GloftA8HP");

//        int code = map.get("com.gameloft.android.HEP.GloftA8HP");
//        Log.i("wang", "code:"+code);
        serviceIntent.putExtra(VERSIONCODE, 5);
        serviceIntent.putExtra(TYPE_START_UPDATE_SERVICE, 5);
        serviceIntent.setComponent(new ComponentName("com.letv.tvos.appstore", "com.letv.tvos.appstore.service.PackageUpdateInfoService"));
        startService(serviceIntent);
    }


    public void dynamicBaseUrl(View v) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    DynamicBaseUrl.main();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    public void dynamicUrl(View v) {
        StoreService.dynamicUrlSync();
    }


    public void downloadFileWithdynamicUrlSync(View v) {
        StoreService.downloadFileWithdynamicUrlSync();
    }

    public void serviceMethodTest(View v) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    StoreService.serviceMethodTest();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    public void crawlerTest(View v) {
        try {
            Crawler.main();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void handleException(View v) {
        ErrorHandlingAdapter.main();
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
