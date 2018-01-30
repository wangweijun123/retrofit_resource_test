package com.example.retrofit;

import android.content.Context;
import android.util.Log;

import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Converter;
import retrofit2.OkHttpUtils;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.helpers.ToStringConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.HeaderMap;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

/**
 * Created by wangweijun1 on 2017/4/9.
 */

public class StoreService {

//    public static final String URL_BASIC_SERVICE_TEST = "http://10.11.146.202/mstore_api/";
    /** 提供基础服务的测试服务器地址  外网测试地址*/
//    public static final String URL_BASIC_SERVICE_TEST = "http://123.125.91.30/api34/";
    public static final String URL_BASIC_SERVICE_RELEASE = "http://106.38.226.79:8080/";
    public static final String URL_BASIC_SERVICE_TEST = "http://mapi.letvstore.com/";

    class Repo {
        int id;
        String name;

        Owner owner;
    }

    class Owner {
        int id;
        String login;
    }

    public interface StoreApi {

        @GET("mapi/edit/recommend")
        Call<MyResp> doGet(@Query("pagefrom") String pagefrom, @Query("pagesize") String pagesize, @Query("code") String code);

        // // http://123.125.91.30/api34/mapi/coop/business
        @GET("api34/mapi/coop/business")
        Call<MyCache> doTestCacheInterceptor();


        @GET("helloworld.txt")
        Call<String> doTestHttpsCacheInterceptor();



        @GET("mapi/edit/recommend")
        Call<MyResp> doGetByMap(@QueryMap Map<String, String> pagefrom);

        @GET("mapi/edit/recommend")
        Call<MyResp> doGetByMapAndHeaders(@QueryMap Map<String, String> pagefrom, @HeaderMap Map<String, String> headers);

        @GET("mapi/edit/recommend")
        Call<String> customconverterFactory(@QueryMap Map<String, String> pagefrom, @HeaderMap Map<String, String> headers);

        @GET("mapi/edit/recommend")
        Call<Void> returnVoidconverterFactory(@QueryMap Map<String, String> pagefrom, @HeaderMap Map<String, String> headers);


        @GET("mapi/edit/recommend")
        Call<String> toStringConverterFactory(@QueryMap Map<String, String> pagefrom, @HeaderMap Map<String, String> headers);


        //  @FieldMap parameters can only be used with form encoding
        @FormUrlEncoded
        @POST("mapi/edit/postrecommend")
        Call<MyResp> doPost(@FieldMap Map<String, String> map, @HeaderMap Map<String, String> headers);

        @POST
        Call<String> doPostForJson(@Url String url, @Body RequestBody requestBody);

        @Headers("Content-Type: application/json")
        @POST
        Call<String> doPostJson2(@Url String url, @Body String jsonBody);

        @FormUrlEncoded
        @POST("mapi/edit/postrecommend")
        Call<MyResp> doPostAndQueryParams(@QueryMap Map<String, String> queryMaps, @FieldMap Map<String, String> fieldMap, @HeaderMap Map<String, String> headers);

        @Multipart
        @POST("mapi/userfeedback/submit")
        Call<MyResp> testPostFile(
                @Part("mobile") RequestBody mobile,
                @Part("content") RequestBody content,
                @Part MultipartBody.Part file
        );

        /**
         *  适合url是由服务器端传过来的，不是写死的，适合下载，图片什么的
         * @param fileUrl  文件地址全的，与设置baseURL没任何关系
         * @return
         */
        @Streaming
        @GET
        Call<ResponseBody> downloadFileWithDynamicUrlSync(@Url String fileUrl);



        @GET("hyuser/test?sleeptime=1000")
        Call<String> reUseConnection();


        @POST("hyuser/install/list")
        Call<String> reUseConnection2();
    }



    public class MyResp {
        String status;
    }

    public class MyCache {
        JsonObject
                entity;
    }


    // String url = "http://mapi.letvstore.com/mapi/edit/recommend?pagefrom=1&pagesize=1&code=RANK_HOT";

    /**
     * 同步请求(商店服务器设置代理fiddler也是没有任何问题的)
     * @throws IOException
     */
    public static void doGetSync() throws IOException {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(URL_BASIC_SERVICE_TEST)
                .addConverterFactory(GsonConverterFactory.create())
                .client(OkHttpUtils.getInstance().getOkHttpClient())
                .build();
        StoreApi service = retrofit.create(StoreApi.class);
//         pagefrom=1&pagesize=1&code=RANK_HOT";
        Call<MyResp> call = service.doGet("1", "1", "RANK_HOT");
        Response<MyResp> resp = call.execute();
        MyResp list = resp.body();
        Log.i(Retrofit.TAG, "list status:"+list.status);
    }


    public static void serviceMethodTest() throws IOException {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(URL_BASIC_SERVICE_TEST)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        StoreApi service = retrofit.create(StoreApi.class);
        // pagefrom=1&pagesize=1&code=RANK_HOT";
        Call<MyResp> call = service.doGet("1", "1", "RANK_HOT");
        Response<MyResp> resp = call.execute();
        MyResp list = resp.body();
        Log.i(Retrofit.TAG, "list status:"+list.status);
        Log.i(Retrofit.TAG,"##################");
        call = service.doGet("1", "1", "RANK_HOT");
        resp = call.execute();
        list = resp.body();
        Log.i(Retrofit.TAG, "list status:"+list.status);
    }

    /**
     * 异步请求
     * @throws IOException
     */
    public static void doGetAsync() throws IOException {
        int count = 5;
        for (int i=0; i<count; i++) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(URL_BASIC_SERVICE_TEST)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(OkHttpUtils.getInstance().getOkHttpClient())
                    .build();
            StoreApi service = retrofit.create(StoreApi.class);
            // pagefrom=1&pagesize=1&code=RANK_HOT";
            // retrofit (代理对象调用doget方法，返回ExecutorCallbackCall(其实就是OkhttpCall对象)))
            Call<MyResp> call = service.doGet("1", "1", "RANK_HOT");
            Callback<MyResp> callback = new Callback<MyResp>() {
                @Override
                public void onResponse(Call<MyResp> call, Response<MyResp> response) {
                    MyResp list = response.body();
                    Log.i(Retrofit.TAG, "tid:"+Thread.currentThread().getId());
                    Log.i(Retrofit.TAG, response.headers().toString());
                    Log.i(Retrofit.TAG,  response.code()+", "+response.message());
                }

                @Override
                public void onFailure(Call<MyResp> call, Throwable t) {
                    Log.i(Retrofit.TAG, "tid:"+Thread.currentThread().getId());
                    t.printStackTrace();
                    Log.i(Retrofit.TAG, "onFailure status");
                }
            };
            call.enqueue(callback);
        }
    }


   static int cacheSize = 10 * 1024 * 1024; // 10 MiB
// /storage/emulated/0/Android/data/com.example.wangweijun1.retrofit_xxx/cache

    /**
     *  OkhttpClient 必须使用单例，牵涉到的成员变量，比如说缓存Cache
     * @param context
     * @throws IOException
     */
    public static void testCacheInterceptor(Context context) throws IOException {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(URL_BASIC_SERVICE_TEST)
                .addConverterFactory(GsonConverterFactory.create())
                .client(OkHttpUtils.getInstance().getCacheOkHttpClient())
                .build();
        StoreApi service = retrofit.create(StoreApi.class);
        // pagefrom=1&pagesize=1&code=RANK_HOT";
        Call<MyResp> call = service.doGet("1", "1", "RANK_HOT");
        Response<MyResp> resp = call.execute();
        MyResp list = resp.body();
        Log.i(Retrofit.TAG, "list status:"+list.status);
    }


    public static void testCustomCacheInterceptor(Context context) throws IOException {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(URL_BASIC_SERVICE_TEST)
                .addConverterFactory(GsonConverterFactory.create())
                .client(OkHttpUtils.getInstance().getCacheOkHttpClient())
                .build();
        StoreApi service = retrofit.create(StoreApi.class);
        // pagefrom=1&pagesize=1&code=RANK_HOT";
        Call<MyResp> call = service.doGet("1", "1", "RANK_HOT");
        Response<MyResp> resp = call.execute();
        MyResp list = resp.body();
        Log.i(Retrofit.TAG, "list status:"+list.status);
    }


    public static void testHttpsCacheInterceptor(Context context) throws IOException {
        File cacheDir = context.getExternalCacheDir();
        Log.i(Retrofit.TAG, "cacheDir : "+cacheDir.getAbsoluteFile());
        //  https://publicobject.com/helloworld.txt
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://publicobject.com/")
                .addConverterFactory(new ToStringConverterFactory())
                .client(OkHttpUtils.getInstance().getCacheOkHttpClient())
                .build();
        StoreApi service = retrofit.create(StoreApi.class);
        Call<String> call = service.doTestHttpsCacheInterceptor();
        Response<String> resp = call.execute();
        String str = resp.body();
        Log.i(Retrofit.TAG, "result is ok");
    }


    public static void testReDirectUrl() throws IOException {
        //  http://publicobject.com/helloworld.txt
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://publicobject.com/")
                .addConverterFactory(new ToStringConverterFactory())
                .client(OkHttpUtils.getInstance().getOkHttpClient())
                .build();
        StoreApi service = retrofit.create(StoreApi.class);
        Call<String> call = service.doTestHttpsCacheInterceptor();
        Response<String> resp = call.execute();
        String str = resp.body();
        Log.i(Retrofit.TAG, "result is ok");
    }

    /**
     * 取消请求
     * @throws IOException
     */
    public static void cancelCall() throws IOException {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(URL_BASIC_SERVICE_TEST)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        StoreApi service = retrofit.create(StoreApi.class);
        Call<MyResp> call = service.doGet("1", "1", "RANK_HOT");
        call.enqueue(new Callback<MyResp>() {
            @Override
            public void onResponse(Call<MyResp> call, Response<MyResp> response) {
                MyResp list = response.body();
                Log.i(Retrofit.TAG, "call" + call + ", list status:"+list.status+", tid:"+Thread.currentThread().getId());
            }

            @Override
            public void onFailure(Call<MyResp> call, Throwable t) {
                Log.i(Retrofit.TAG, "onFailure status: call:"+call+ "   "+t.toString());
            }
        });
        Log.i(Retrofit.TAG, "call.cancel ");
        call.cancel();
    }

    public static void doGetByQueryMap() throws IOException {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(URL_BASIC_SERVICE_TEST)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        StoreApi service = retrofit.create(StoreApi.class);
        // pagefrom=1&pagesize=1&code=RANK_HOT";
        Map<String, String> map = new HashMap<>();
        map.put("pagefrom", "1");
        map.put("pagesize", "1");
        map.put("code", "RANK_HOT");
        Call<MyResp> call = service.doGetByMap(map);
        Response<MyResp> resp = call.execute();
        MyResp list = resp.body();
        System.out.println("list status:"+list.status);
    }

    public static void doGetByMapAndHeaders() throws IOException {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(URL_BASIC_SERVICE_TEST)
                .addConverterFactory(GsonConverterFactory.create())
                .client(OkHttpUtils.getInstance().getOkHttpClient())
                .build();
        StoreApi service = retrofit.create(StoreApi.class);
        // pagefrom=1&pagesize=1&code=RANK_HOT";
        Map<String, String> map = new HashMap<>();
        map.put("pagefrom", "1");
        map.put("pagesize", "1");
        map.put("code", "RANK_HOT");
        Call<MyResp> call = service.doGetByMapAndHeaders(map, getCommonParamsMap());
        Log.i(Retrofit.TAG, "call.execute() start...");
        Response<MyResp> resp = call.execute();
        Log.i(Retrofit.TAG, "call.execute() end");
        MyResp list = resp.body();
        Log.i(Retrofit.TAG, "list status:"+list.status);
    }





//    POST http://106.38.226.79:8080/mapi/edit/postrecommend

//    isgt=1&pagefrom=0&versioncodes=18%2C51%2C116%2C11%2C10153%2C104%2C18000%2C8703448%2C23%2C13520%2C25%2C1038%2C790%2C23%2C1%2C3350%2C186%2C10000301&packagenames=com.quicksdk.qnyh.leshi%2Ccom.letv.android.letvlive%2Ccom.lesports.glivesports%2Ccom.letv.bbs%2Ccom.letv.letvshop%2Ccom.baidu.input_letv%2Ccom.letv.android.client%2Ccom.google.android.gms%2Ccom.google.android.gsf%2Ccom.wandoujia.phoenix2%2Ccom.letv.games%2Ccom.letv.lesophoneclient%2Ccom.baidu.BaiduMap%2Ccom.google.android.gsf.login%2Ccom.le.www.retrofit_test_by_me_2%2Ccom.sina.weibo%2Ccn.wps.moffice_eng%2Ccom.qqreader.leshi&record=4%2C30&pagesize=30&code=FOCUS_GAME_NEWINDEX%2CREC_CLASSIC_GAME_INDEX_PLUS
    public static void doPost() throws IOException {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(URL_BASIC_SERVICE_TEST)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        StoreApi service = retrofit.create(StoreApi.class);
        Map<String, String> map = new HashMap<>();
        map.put("isgt" , "1");
        map.put("pagefrom" , "0");
        map.put("packagenames", "com.quicksdk.qnyh.leshi");
        map.put("versioncodes", "18");
        map.put("record", "4,30");
        map.put("pagesize", "30");
        map.put("code", "FOCUS_GAME_NEWINDEX,CREC_CLASSIC_GAME_INDEX_PLUS");
        Call<MyResp> repos = service.doPost(map, getCommonParamsMap());


        Response<MyResp> resp = repos.execute();
        MyResp list = resp.body();
        System.out.println("list status:"+list.status);
    }

     static final String post_json_tv_store_url = "http://api.s5.letvstore.com/test2/api/apps/update?device=LETV_Le%2BX625&letvReleaseVersion=&letvSwVersion=&mac=02%253A00%253A00%253A00%253A00%253A00&letvUiVersion=&store=LETV&letvCarrier=-1&timeStamp=1496916296012&appVersion=5805&imei=868896020022309&letvDeviceType=-1&letvPlatform=-1&version=0&osVersion=6.0&letvHwVersion=&deviceInfo=Le%2BX625&letvUiType=&Authorization=";
    /**
     * post json ，但是这个接口随便传都行
     */
    public static void doPostForJson() {
        // http://www.roundsapp.com/post
        // "http://download.log.letvstore.com/record/dl"

        //  http://api.s5.letvstore.com/test2/api/apps/update?device=LETV_Le%2BX625&letvReleaseVersion=&letvSwVersion=&mac=02%253A00%253A00%253A00%253A00%253A00&letvUiVersion=&store=LETV&letvCarrier=-1&timeStamp=1496916296012&appVersion=5805&imei=868896020022309&letvDeviceType=-1&letvPlatform=-1&version=0&osVersion=6.0&letvHwVersion=&deviceInfo=Le%2BX625&letvUiType=&Authorization=
        Retrofit retrofit=new Retrofit.Builder()
                .baseUrl("http://download.log.letvstore.com/")
                .addConverterFactory(new ToStringConverterFactory())
                .build();
        StoreApi postRoute=retrofit.create(StoreApi.class);
        // 自己创建 RequestBody 指定类型
        RequestBody body=RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"),bowlingJson("Jesse", "Jake"));
        Call<String> call=postRoute.doPostForJson(post_json_tv_store_url, body);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                Log.i(Retrofit.TAG,  "onResponse:" +  response.body());
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.i(Retrofit.TAG, "onFailure" + t.getMessage());
            }
        });
    }


    public static void doPostJson2() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(URL_BASIC_SERVICE_TEST) // 由于后面定义了Url，所以这里的base url是没用
                .addConverterFactory(new ToStringConverterFactory())
                .build();
        StoreApi service = retrofit.create(StoreApi.class);
        Call<String> call = service.
                doPostJson2(post_json_tv_store_url,
                        "{\"com.gameloft.android.HEP.GloftA8HP\":\"5\"}");
        Callback<String> callback = new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                Log.i(Retrofit.TAG, response.body());
            }
            @Override
            public void onFailure(Call<String> call, Throwable t) {
                t.printStackTrace();
                Log.i(Retrofit.TAG, "onFailure status");
            }
        };
        call.enqueue(callback);
    }

    static String bowlingJson(String player1, String player2) {
        return "{'winCondition':'HIGH_SCORE',"
                + "'name':'Bowling',"
                + "'round':4,"
                + "'lastSaved':1367702411696,"
                + "'dateStarted':1367702378785,"
                + "'players':["
                + "{'name':'" + player1 + "','history':[10,8,6,7,8],'color':-13388315,'total':39},"
                + "{'name':'" + player2 + "','history':[6,10,5,10,10],'color':-48060,'total':41}"
                + "]}";
    }

    public static void doPostAndQueryParams() throws IOException {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(URL_BASIC_SERVICE_TEST)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        StoreApi service = retrofit.create(StoreApi.class);

        Map<String, String> map = new HashMap<>();
        map.put("isgt" , "1");
        map.put("pagefrom" , "0");
        map.put("packagenames", "com.quicksdk.qnyh.leshi");
        map.put("versioncodes", "18");
        map.put("record", "4,30");
        map.put("pagesize", "30");
        map.put("code", "FOCUS_GAME_NEWINDEX,CREC_CLASSIC_GAME_INDEX_PLUS");


        Map<String, String> queryMap = new HashMap<String, String>();
        queryMap.put("uid", "10049");

        Call<MyResp> repos = service.doPostAndQueryParams(queryMap, map, getCommonParamsMap());


        Response<MyResp> resp = repos.execute();
        MyResp list = resp.body();
        System.out.println("list status:"+list.status);
    }

    /**
     * post 文件与参数
     * @throws IOException
     */
    public static void doPostFileAndParams() throws IOException {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(URL_BASIC_SERVICE_TEST)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        StoreApi service = retrofit.create(StoreApi.class);

        String filename = "/sdcard/222.zip";
        File file = new File(filename);
        if (!file.exists()) {
            boolean flag = file.createNewFile();
        }
        RequestBody requestFile = RequestBody.create(
                MediaType.parse("application/octet-stream"),
                file
        );

        MultipartBody.Part body = MultipartBody.Part.createFormData("imgs", file.getName(), requestFile);

        String mobileStr = "15801097878";
        RequestBody mobile = RequestBody.create(
                MediaType.parse("multipart/form-data"),
                mobileStr);

        String contentStr = "dddddddddddddddddddddddddddddd";
        RequestBody content = RequestBody.create(
                MediaType.parse("multipart/form-data"),
                contentStr);
        Call<MyResp> repos = service.testPostFile(mobile, content, body);
        Response<MyResp> resp = repos.execute();
        MyResp list = resp.body();
        System.out.println("list status:"+list.status);
    }

    /**
     * 返回字符串
     * @throws IOException
     */
    public static void customconverterFactory() throws IOException {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(URL_BASIC_SERVICE_TEST)
                .addConverterFactory(new CustomConvertor())
                .build();
        StoreApi service = retrofit.create(StoreApi.class);
        // pagefrom=1&pagesize=1&code=RANK_HOT";
        Map<String, String> map = new HashMap<>();
        map.put("pagefrom", "1");
        map.put("pagesize", "1");
        map.put("code", "RANK_HOT");
        Call<String> call = service.customconverterFactory(map, getCommonParamsMap());
        Log.i(Retrofit.TAG, "call.execute() start...");
        Response<String> resp = call.execute();
        Log.i(Retrofit.TAG, "call.execute() end");
        String list = resp.body();
        Log.i(Retrofit.TAG, "list status:"+list);
    }

    /**
     * 返回空
     * @throws IOException
     */
    public static void returnVoidconverterFactory() throws IOException {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(URL_BASIC_SERVICE_TEST)
                .build();
        StoreApi service = retrofit.create(StoreApi.class);
        // pagefrom=1&pagesize=1&code=RANK_HOT";
        Map<String, String> map = new HashMap<>();
        map.put("pagefrom", "1");
        map.put("pagesize", "1");
        map.put("code", "RANK_HOT");
        Call<Void> call = service.returnVoidconverterFactory(map, getCommonParamsMap());
        Log.i(Retrofit.TAG, "call.execute() start...");
        Response<Void> resp = call.execute();
        Void v = resp.body();
        Log.i(Retrofit.TAG, "call.execute() end v:"+v);
    }


    public static void toStringConverterFactory() throws IOException {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(URL_BASIC_SERVICE_TEST)
                .addConverterFactory(new ToStringConverterFactory())
                .build();
        StoreApi service = retrofit.create(StoreApi.class);
        // pagefrom=1&pagesize=1&code=RANK_HOT";
        Map<String, String> map = new HashMap<>();
        map.put("pagefrom", "1");
        map.put("pagesize", "1");
        map.put("code", "RANK_HOT");
        Call<String> call = service.toStringConverterFactory(map, getCommonParamsMap());
        Log.i(Retrofit.TAG, "call.execute() start...");
        Response<String> resp = call.execute();
        String v = resp.body();
        Log.i(Retrofit.TAG, "call.execute() end v:"+v);
    }

   static class CustomConvertor extends Converter.Factory {
        @Override
        public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
            return new ToStringConverter();
        }
    }

   public static class ToStringConverter implements Converter<ResponseBody, String> {
        @Override
        public String convert(ResponseBody value) throws IOException {
            String result = value.string();
            return result;
        }
    }


    /**
     * 测试okhttpclient 单例 (Dispatcher单例，ConnectionPool 单例)
     * @throws IOException
     */
    public static void testOkhttpClientSingleInstance() {
        try {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(URL_BASIC_SERVICE_TEST)
                    .client(OkHttpUtils.getInstance().getOkHttpClient())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            Log.i(Retrofit.TAG, "StoreService create service");
            // 使用retrofit创建一个api接口对象(retrofit newProxyInstance)
            StoreApi service = retrofit.create(StoreApi.class);
            Log.i(Retrofit.TAG, "StoreService service doGet");
            // pagefrom=1&pagesize=1&code=RANK_HOT";
            // retrofit (代理对象调用doget方法，返回ExecutorCallbackCall(其实就是OkhttpCall对象)))
            Call<MyResp> call = service.doGet("1", "1", "RANK_HOT");
            Log.i(Retrofit.TAG, "StoreService call:"+call);

            Callback<MyResp> callback = new Callback<MyResp>() {
                @Override
                public void onResponse(Call<MyResp> call, Response<MyResp> response) {
                    MyResp list = response.body();
                    Log.i(Retrofit.TAG,  " list status:"+list.status+", tid:"+Thread.currentThread().getId());
                }

                @Override
                public void onFailure(Call<MyResp> call, Throwable t) {
                    t.printStackTrace();
                    Log.i(Retrofit.TAG, "onFailure status");
                }
            };

            Log.i(Retrofit.TAG, "id callback:"+callback);
            call.enqueue(callback);
        } catch (Exception e){

        }

    }

    /**
     * okhttpclient 多实例
     */
    public static void testOkhttpClientMutipart() {
        try {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(URL_BASIC_SERVICE_TEST)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            Log.i(Retrofit.TAG, "StoreService create service");
            // 使用retrofit创建一个api接口对象(retrofit newProxyInstance)
            StoreApi service = retrofit.create(StoreApi.class);
            Log.i(Retrofit.TAG, "StoreService service doGet");
            // pagefrom=1&pagesize=1&code=RANK_HOT";
            // retrofit (代理对象调用doget方法，返回ExecutorCallbackCall(其实就是OkhttpCall对象)))
            Call<MyResp> call = service.doGet("1", "1", "RANK_HOT");
            Log.i(Retrofit.TAG, "StoreService call:"+call);

            Callback<MyResp> callback = new Callback<MyResp>() {
                @Override
                public void onResponse(Call<MyResp> call, Response<MyResp> response) {
                    MyResp list = response.body();
                    Log.i(Retrofit.TAG,  " list status:"+list.status+", tid:"+Thread.currentThread().getId());
                }

                @Override
                public void onFailure(Call<MyResp> call, Throwable t) {
                    t.printStackTrace();
                    Log.i(Retrofit.TAG, "onFailure status");
                }
            };

            Log.i(Retrofit.TAG, "id callback:"+callback);
            call.enqueue(callback);
        } catch (Exception e){

        }

    }


    public static void testHttpDns() {
        try {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(URL_BASIC_SERVICE_TEST)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(OkHttpUtils.getInstance().getHTTPDnsClient())
                    .build();
            Log.i(Retrofit.TAG, "StoreService create service");
            // 使用retrofit创建一个api接口对象(retrofit newProxyInstance)
            StoreApi service = retrofit.create(StoreApi.class);
            Log.i(Retrofit.TAG, "StoreService service doGet");
            // pagefrom=1&pagesize=1&code=RANK_HOT";
            // retrofit (代理对象调用doget方法，返回ExecutorCallbackCall(其实就是OkhttpCall对象)))
            Call<MyResp> call = service.doGet("1", "1", "RANK_HOT");
            Log.i(Retrofit.TAG, "StoreService call:"+call);

            Callback<MyResp> callback = new Callback<MyResp>() {
                @Override
                public void onResponse(Call<MyResp> call, Response<MyResp> response) {
                    MyResp list = response.body();
                    Log.i(Retrofit.TAG,  " list status:"+list.status+", tid:"+Thread.currentThread().getId());
                }

                @Override
                public void onFailure(Call<MyResp> call, Throwable t) {
                    t.printStackTrace();
                    Log.i(Retrofit.TAG, "onFailure status");
                }
            };

            Log.i(Retrofit.TAG, "id callback:"+callback);
            call.enqueue(callback);
        } catch (Exception e){

        }

    }

    /**
     * 下载大文件还是出现oom
     */
    public static void dynamicUrlSync() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(URL_BASIC_SERVICE_TEST) // 由于后面定义了Url，所以这里的base url是没用
                .build();
        StoreApi service = retrofit.create(StoreApi.class);
        Call<ResponseBody> call = service.downloadFileWithDynamicUrlSync("http://www.coca-cola.com/robots.txt");
        Callback<ResponseBody> callback = new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                ResponseBody responseBody = response.body();
                Log.i(Retrofit.TAG, "responseBody:"+responseBody);

            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
                Log.i(Retrofit.TAG, "onFailure status");
            }
        };
        call.enqueue(callback);
    }



    public static void downloadFileWithdynamicUrlSync() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(URL_BASIC_SERVICE_TEST) // 由于后面定义了Url，所以这里的base url是没用
                .build();
        StoreApi service = retrofit.create(StoreApi.class);
        Call<ResponseBody> call = service.downloadFileWithDynamicUrlSync("http://g3.letv.cn/265/27/73/mstore/0/apkupload/jdrapp_1515814087151.apk");
        Callback<ResponseBody> callback = new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                final ResponseBody responseBody = response.body();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Log.i(Retrofit.TAG, "responseBody:"+responseBody);
                        boolean flag = writeResponseBodyToDisk(responseBody);
                        Log.i(Retrofit.TAG,"flag:"+flag);
                    }
                }).start();

            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
                Log.i(Retrofit.TAG, "onFailure status");
            }
        };
        call.enqueue(callback);
    }


    public static void testConnection() throws IOException {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(URL_BASIC_SERVICE_TEST)
                .addConverterFactory(GsonConverterFactory.create())
                .client(OkHttpUtils.getInstance().getOkHttpClient())
                .build();
        StoreApi service = retrofit.create(StoreApi.class);
        // pagefrom=1&pagesize=1&code=RANK_HOT";
        Call<MyResp> call = service.doGet("1", "1", "RANK_HOT");
        Response<MyResp> resp = call.execute();
        MyResp list = resp.body();
        Log.i(Retrofit.TAG, "list status:"+list.status);
    }

    public static void testConnection2() throws IOException {
        //  https://publicobject.com/helloworld.txt
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://publicobject.com/")
                .addConverterFactory(new ToStringConverterFactory())
                .client(OkHttpUtils.getInstance().getOkHttpClient())
                .build();
        StoreApi service = retrofit.create(StoreApi.class);
        Call<String> call = service.doTestHttpsCacheInterceptor();
        Response<String> resp = call.execute();
        String str = resp.body();
        Log.i(Retrofit.TAG, "result is ok");
    }

    /**
     * 连接重用测试，当是http1.x协议时，一个连接上只能有一个stream，也就是如果
     * 同一太服务器，当上一个api还没返回，下一个会重新建立一个连接，发送数据，当然
     * 如果前一个请求回来了，重用连接,我担心的问题没了
     */
    public static void testReUseConnection() {
        // http://10.127.92.182:8888/hyuser/test?sleeptime=10000"
        // http://10.127.92.182:8888/hyuser/install/list
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.127.92.182:8888/")
                .addConverterFactory(new ToStringConverterFactory())
                .client(OkHttpUtils.getInstance().getOkHttpClient())
                .build();
        StoreApi service = retrofit.create(StoreApi.class);
        Call<String> call = service.reUseConnection();
         call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                Log.i(Retrofit.TAG, "first request onResponse:"+response.body());
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.i(Retrofit.TAG, "first request onFailure");
            }
        });


        try {
            Log.i(Retrofit.TAG, "sleep 2000 ms");
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.i(Retrofit.TAG, "sleep finished");

        Retrofit retrofit2 = new Retrofit.Builder()
                .baseUrl("http://10.127.92.182:8888/")
                .addConverterFactory(new ToStringConverterFactory())
                .client(OkHttpUtils.getInstance().getOkHttpClient())
                .build();
        StoreApi service2 = retrofit2.create(StoreApi.class);
        Call<String> call2 = service2.reUseConnection2();
        call2.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                Log.i(Retrofit.TAG, "second request onResponse:"+response.body());
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.i(Retrofit.TAG, "second request onFailure");
            }
        });


    }





    public static Map<String, String> getCommonParamsMap() {
        Map<String, String> commonParamsMap = new HashMap<String, String>();
        commonParamsMap.put("mac", "");
        commonParamsMap.put("imei", "");
        commonParamsMap.put("storeflag", "ebfzYZIyzcQnvLxVAppEog==");
        // 用户信息
        commonParamsMap.put("productno", "60");
        commonParamsMap.put("productpackageno", "");
        commonParamsMap.put("unitno", "");
        commonParamsMap.put("appversion", "1080");
                commonParamsMap.put("osversion", "");
        commonParamsMap.put("net", "mobile");
        commonParamsMap.put("screensize", "1920*1080");
        commonParamsMap.put("platform", "aphone");
        commonParamsMap.put("osversioncode", "16");
        commonParamsMap.put("channelno", "20"); // 渠道号
        commonParamsMap.put("channelpackageno", "602001"); // 二级渠道号
        commonParamsMap.put("devicemodel", "Le X625");
        commonParamsMap.put("devicebrand", "letv"); // 设备品牌
        commonParamsMap.put("appversioncode", "1080");
        commonParamsMap.put("appversion", "1080");
        commonParamsMap.put("osversion", "6.0");
        commonParamsMap.put("timestamp", "1491669045636");
        commonParamsMap.put("language", "zh");
        return commonParamsMap;
    }

    private static boolean writeResponseBodyToDisk(ResponseBody body) {
        try {
            // todo change the file location/name according to your needs


            File futureStudioIconFile = new File("sdcard/xxxxx.apk");

            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {
                byte[] fileReader = new byte[4096];

                long fileSize = body.contentLength();
                long fileSizeDownloaded = 0;

                inputStream = body.byteStream();// 输入流,从服务端过来的stream
                outputStream = new FileOutputStream(futureStudioIconFile);//输出流到本地文件

                while (true) {
                    int read = inputStream.read(fileReader);

                    if (read == -1) {
                        break;
                    }

                    outputStream.write(fileReader, 0, read);

                    fileSizeDownloaded += read;

                    Log.i(Retrofit.TAG, "file download: " + fileSizeDownloaded + " of " + fileSize);
                }

                outputStream.flush();

                return true;
            } catch (IOException e) {
                return false;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }

                if (outputStream != null) {
                    outputStream.close();
                }
            }
        } catch (IOException e) {
            return false;
        }
    }
}
