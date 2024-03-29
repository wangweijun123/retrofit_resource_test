package com.example.wangweijun1.retrofit_xxx;

import android.Manifest;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.FileObserver;
import android.os.PowerManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.example.retrofit.BaseModel;
import com.example.retrofit.Crawler;
import com.example.retrofit.DynamicBaseUrl;
import com.example.retrofit.ErrorHandlingAdapter;
import com.example.retrofit.HTTPSUtils;
import com.example.retrofit.IResponse;
import com.example.retrofit.RankListMiddle;
import com.example.retrofit.RankListModel;
import com.example.retrofit.SimpleMockService;
import com.example.retrofit.SimpleService;
import com.example.retrofit.StoreService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.CharBuffer;
import java.nio.IntBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.internal.io.FileSystem;
import retrofit2.Retrofit;

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
//                    StoreService.doGetSync();
//                    SimpleService.httpsRequest();
//                    SimpleService2.syncRequestString();
//                    MyService.test();
//                    MyService.doGetSync();
//                    MyService.testByQueryMap();
//                    MyService.doPost();
//                    MyService.testPostFile();
                    StoreService.doGetSyncReturnObj();
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
     * 缓存测试
     * @param v
     */
    public void testCacheInterceptor(View v) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    StoreService.testCacheInterceptor(getApplicationContext());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void testCustomCacheInterceptor(View v) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    StoreService.testCustomCacheInterceptor(getApplicationContext());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    public void testHttpsCacheInterceptor(View v) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    StoreService.testHttpsCacheInterceptor(getApplicationContext());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }



    public void getCacheBody(View v) {
        String headerFileName = "/storage/emulated/0/Android/data/com.example.wangweijun1.retrofit_xxx/cache/7cfaf75e77a65080b5c7fae99df1a1e0.0";
        String bodyFileName = "/storage/emulated/0/Android/data/com.example.wangweijun1.retrofit_xxx/cache/7cfaf75e77a65080b5c7fae99df1a1e0.1";

        readFile(headerFileName);
        readFile(bodyFileName);
    }

    public static void readFile(String fileName) {
        try {
            FileInputStream in = new FileInputStream(fileName);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = -1;
            while ((len=in.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            byte[] body = baos.toByteArray();
            String result = new String(body, "gb2312");
            Log.i(Retrofit.TAG, "result:"+result);
            in.close();
            baos.close();
        } catch (Exception e) {
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


    public void testBaiduHttpsByHttpUrlconnection(View v) {
        try {
            SimpleService.testBaiduHttpsByHttpUrlconnection();
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

    public void JsonAndXmlConverters(View v) {
        new Thread(new Runnable() {
            @Override
            public void run() {
//                try {
//                    JsonAndXmlConverters.main();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
            }
        }).start();
    }


    public void doPostForJson(View v) {
        StoreService.doPostForJson();
    }

    public void doPostJson2(View v) {
        StoreService.doPostJson2();
    }


    public void sanjiaoshou(View v) {
        StoreService.sanjiaoshou();
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

    public void http200Sync(View v) {

//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    CallTest.http200Sync();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();

        ArrayList<User> list1 = new ArrayList<User>();
        ArrayList<User> list2 = list1;
        Log.i("wang", "list1.equals(list2) :" +list1.equals(list2));//true 先比较list是否为同一个对象
        User u = new User();
        list1.add(u);
        ArrayList<User> list3 = new ArrayList<User>();//再比较里面的元素的是否一致
        list3.add(u);
        Log.i("wang", "list1.equals(list3):" + list1.equals(list3));// true
    }

    public void webviewTest(View view) {
        startActivity(new Intent(getApplicationContext(), WebActivity.class));
    }


    class User{
        int id;
        String name;
    }

    /**
     * break 就是跳出当前循环而已,如果是双层循环，那肯定只跳出里层循环，
     * continue 进入当前循环的下一个继续，也就是说此循环contiue 后面语句不执行
     * @param v
     */
    public void breakContinue(View v) {
        for (int i=0; i<5; i++) {
            Log.i("wang", "i="+i);
            for (int j=0;j<4; j++) {
                Log.i("wang", "j="+j);
                if (j == 1){
                    continue;
                }
                Log.i("wang", "---");
            }
            Log.i("wang", "##########");
        }
    }


    public void listTest(View v) {
        List<User> list1 = new ArrayList<>();
        User u = new User();
        u.id = 1;
        u.name = "属于list 1";

        User u2 = new User();
        u2.id = 2;
        u2.name = "属于list 1";
        User u3 = new User();
        u3.id = 3;
        u3.name = "属于list 1";
        list1.add(u);
        list1.add(u2);
        list1.add(u3);



        List<User> list2 = new ArrayList<>();
        User ux2 = new User();
        ux2.id = 2;
        ux2.name = "属于list 2";
        User u22 = new User();
        u22.id = 3;
        u22.name = "属于list 2";
        User u32 = new User();
        u32.id = 4;
        u32.name = "属于list 2";
        list2.add(ux2);
        list2.add(u22);
        list2.add(u32);

        test(list1, list2);
    }

    private void test(List<User> oldList, List<User> newList){
        List<User> temp = new ArrayList<>();
        for (int i=0; i<newList.size();i++) {
            boolean exsit = false;
            User newUser = newList.get(i);
            for (int j=0;j<oldList.size();j++) {
                User oldUser = oldList.get(j);
                if (oldUser.id == newUser.id) {
                    exsit = true;
                    temp.add(oldUser);
                    break;
                }
            }
            if (!exsit) {
                temp.add(newUser);
            }
        }

        for(int i=0;i<temp.size();i++){
            Log.i("wang", temp.get(i).id + ", "+temp.get(i).name);
        }
    }

    public void testConnection(View v) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    StoreService.testConnection();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    public void testConnection2(View v) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    StoreService.testConnection2();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    public void testReUseConnection(View v) {
        StoreService.testReUseConnection();

    }


    public void testReDirectUrl(View v) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    StoreService.testReDirectUrl();

                    PowerManager p;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    DiskLruCacheClenup diskLruCacheClenup = new DiskLruCacheClenup();
    public void testCleanup(View v) {
        diskLruCacheClenup.run();
    }

    public void testRename(View v) {// /storage/emulated/0/Android/data/com.example.wangweijun1.retrofit_xxx/cache/test.txt.temp
        File from = new File(getExternalCacheDir(), "test.txt.temp");
        Log.i(Retrofit.TAG,"from.exists() : " +from.exists());
        if (!from.exists()) {
            try {
                from.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        File to = new File(getExternalCacheDir(), "test.txt");
        if (!to.exists()) {
            try {
                to.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Log.i(Retrofit.TAG,"to.exists() : " +to.exists());
        try {
            FileSystem.SYSTEM.rename(from, to);
        }catch (IOException io){
            io.printStackTrace();
        }

    }

    File logFile = null;
    File monitorSelfWrited = null;
    FileObserver fileObserver;
    FileObserver uploadfileObserver;
    File uploadDir;
    public void initMonitorFile(View view) {
        logFile = new File(getExternalCacheDir(), "log.txt");
        monitorSelfWrited = getExternalCacheDir();

        uploadDir = new File(getExternalCacheDir(), "upload");
        if (!uploadDir.exists()) {
            uploadDir.mkdir();
        }
    }
    /**
     * 多进程写文件(stream map)（写完转移到单进程上报目录），单进程上报(监听上报目录)
     *
     *
     * @param view
     */
    public void monitorSP(View view) {
        // 监听本进程自己log文件的目录, 如果是监听文件，文件必须存在哈
        fileObserver = new FileObserver(monitorSelfWrited.getAbsolutePath()) {
            @Override
            public void onEvent(int event, @android.support.annotation.Nullable String path) {
                Log.i(Retrofit.TAG,"event="+event+" " + path); // 2, 32, 2, 8
                if (event == FileObserver.CLOSE_WRITE) {
                    Log.i(Retrofit.TAG, "写完了哈, 开始移动");
                    //
                    try {
                        FileSystem.SYSTEM.rename(logFile, new File(uploadDir, "upload.txt"));
                        Log.i(Retrofit.TAG, "移动成功");
                    }catch (IOException io){
                        io.printStackTrace();
                    }
                }
            }
        };

        uploadfileObserver = new FileObserver(uploadDir.getAbsolutePath()) {
            @Override
            public void onEvent(int event, @android.support.annotation.Nullable String path) {
                // upload event=128 upload.txt
                Log.i(Retrofit.TAG,"upload event="+event+" " + path); // 2, 32, 2, 8
            }
        };

        fileObserver.startWatching();
        uploadfileObserver.startWatching();

        Log.i(Retrofit.TAG,"startWatching success ");
    }
    public void writeFileEvent(View view) {

        String content = "Hello World !!";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                Files.write(Paths.get(logFile.getAbsolutePath()), content.getBytes());
                Log.i(Retrofit.TAG,"write success ");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
//            testStreamWrite();
            testMappedWrite();
        }
    }

    public void testStreamWrite() {
        try {
            long starttime = System.currentTimeMillis();
            Log.i(Retrofit.TAG,"file :" +logFile.getAbsolutePath());
            DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(logFile)));
            for(int i = 0; i < 20; i++){
                dos.writeInt(i);
            }
            if(dos != null){
                dos.flush();// 一定要flush一下哈，当你立马就要读取的时候
                dos.close();
            }
            long endtime = System.currentTimeMillis();
            Log.i(Retrofit.TAG,"testStreamWrite:"+(endtime-starttime)+"ms");
        }catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 使用map写文件，监听的文件夹始终都没有event 8的信号，也就是没有关闭stream
     * XLOG。写log，内存
     */
    public void testMappedWrite() {
        long starttime = System.currentTimeMillis();
        RandomAccessFile randomAccessFile = null;
        try {
            randomAccessFile = new RandomAccessFile(logFile, "rw");
            FileChannel fc = randomAccessFile.getChannel();
            IntBuffer ib = fc.map(FileChannel.MapMode.READ_WRITE, 0, 10*(4))
                    .asIntBuffer();
            for (int i = 0; i < 10; i++) {
                ib.put(i);
            }
            randomAccessFile.close();
            if (fc != null) {
                fc.close();
                fc = null;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        long endtime = System.currentTimeMillis();
        System.out.println("testMappedWrite:"+(endtime-starttime)+"ms");
    }


    public void avaibleMemory(View v) {
        // Before doing something that requires a lot of memory,
        // check to see whether the device is in a low memory state.
        ActivityManager.MemoryInfo memoryInfo = getAvailableMemory();
        System.out.println("availMem:"+memoryInfo.availMem +
                ", totalMem:"+memoryInfo.totalMem + ", lowMemory:" + memoryInfo.lowMemory);
        if (!memoryInfo.lowMemory) {
            // Do memory intensive work ...
            // availMem=2 147 405 824  totalMem=7 670 865 920   lowMemory=false
        }
        getAppMemory();
    }
    // Get a MemoryInfo object for the device's current memory status.
    private ActivityManager.MemoryInfo getAvailableMemory() {
        ActivityManager activityManager = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);
        return memoryInfo;
    }

    private void getAppMemory() {

        ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        //最大分配内存
        int memory = activityManager.getMemoryClass();
        System.out.println("memory: "+memory);
        //最大分配内存获取方法2
        float maxMemory = (float) (Runtime.getRuntime().maxMemory() * 1.0/ (1024 * 1024));
        //当前分配的总内存
        float totalMemory = (float) (Runtime.getRuntime().totalMemory() * 1.0/ (1024 * 1024));
        //剩余内存
        float freeMemory = (float) (Runtime.getRuntime().freeMemory() * 1.0/ (1024 * 1024));
        System.out.println("maxMemory: "+maxMemory);
        System.out.println("totalMemory: "+totalMemory);
        System.out.println("freeMemory: "+freeMemory);
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

    public void parseReturnJson(View v) {
        try {
            InputStream inputStream = getAssets().open("json_return.txt");
            byte buffer[] = new byte[8*1024];
            int numb = inputStream.read(buffer);
            String result = new String(buffer, 0, numb, "utf-8");
            Log.i("wang","result:"+result);
            // good
            IResponse<RankListModel> iResponse =  new Gson().fromJson(result, new TypeToken<IResponse<RankListModel>>() {
            }.getType());
            RankListModel rankListModel = iResponse.getEntity();
            Log.i(Retrofit.TAG, "mseid:"+rankListModel.mseid);
            List<RankListMiddle> ranklist = rankListModel.ranklist;
            if (ranklist != null && ranklist.size() > 0) {
                RankListMiddle rankListMiddle = ranklist.get(0);
                Log.i(Retrofit.TAG, "pagesize:"
                        +rankListMiddle.pagesize + ", "+rankListMiddle.total);
                List<BaseModel> items = rankListMiddle.items;
                for (int i = 0; i < items.size(); i++) {
                    Log.i(Retrofit.TAG, items.get(i).toString());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}

