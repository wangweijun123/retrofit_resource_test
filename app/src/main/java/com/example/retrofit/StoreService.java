package com.example.retrofit;

import android.util.Log;

import com.google.gson.annotations.JsonAdapter;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.BuiltInConverters;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Converter;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.helpers.ToStringConverterFactory;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.HeaderMap;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

/**
 * Created by wangweijun1 on 2017/4/9.
 */

public class StoreService {

    public static final String URL_BASIC_SERVICE_TEST = "http://10.11.146.202/mstore_api/";
    /** 提供基础服务的测试服务器地址  外网测试地址*/
//    public static final String URL_BASIC_SERVICE_TEST = "http://123.125.91.30/api34/";
    public static final String URL_BASIC_SERVICE_RELEASE = "http://106.38.226.79:8080/";
    public static final String URL_BASIC_SERVICE = "http://mapi.letvstore.com/";

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


        @FormUrlEncoded
        @POST("mapi/edit/postrecommend")
        Call<MyResp> doPost(@FieldMap Map<String, String> map, @HeaderMap Map<String, String> headers);

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
    }



    class MyResp {
        String status;
    }


    // String url = "http://mapi.letvstore.com/mapi/edit/recommend?pagefrom=1&pagesize=1&code=RANK_HOT";

    /**
     * 同步请求
     * @throws IOException
     */
    public static void doGetSync() throws IOException {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(URL_BASIC_SERVICE_TEST)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        Log.i(Retrofit.TAG, "StoreService create service");
        StoreApi service = retrofit.create(StoreApi.class);
        Log.i(Retrofit.TAG, "StoreService service doGet");
        // pagefrom=1&pagesize=1&code=RANK_HOT";
        Call<MyResp> call = service.doGet("1", "1", "RANK_HOT");
        Log.i(Retrofit.TAG, "StoreService call.execute()");
        Response<MyResp> resp = call.execute();
        MyResp list = resp.body();
        Log.i(Retrofit.TAG, "list status:"+list.status);
    }

    /**
     * 异步请求
     * @throws IOException
     */
    public static void doGetAsync() throws IOException {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(URL_BASIC_SERVICE_TEST)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        Log.i(Retrofit.TAG, "StoreService create service");
        StoreApi service = retrofit.create(StoreApi.class);
        Log.i(Retrofit.TAG, "StoreService service doGet");
        // pagefrom=1&pagesize=1&code=RANK_HOT";
        Call<MyResp> call = service.doGet("1", "1", "RANK_HOT");
        Log.i(Retrofit.TAG, "StoreService call.execute()");
        call.enqueue(new Callback<MyResp>() {
            @Override
            public void onResponse(Call<MyResp> call, Response<MyResp> response) {
                MyResp list = response.body();
                Log.i(Retrofit.TAG, "list status:"+list.status+", tid:"+Thread.currentThread().getId());
            }

            @Override
            public void onFailure(Call<MyResp> call, Throwable t) {
                Log.i(Retrofit.TAG, "onFailure status:");
            }
        });
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
        Log.i(Retrofit.TAG, "StoreService create service");
        StoreApi service = retrofit.create(StoreApi.class);
        Log.i(Retrofit.TAG, "StoreService service doGet");
        // pagefrom=1&pagesize=1&code=RANK_HOT";
        Call<MyResp> call = service.doGet("1", "1", "RANK_HOT");
        Log.i(Retrofit.TAG, "StoreService call.execute()");
        call.enqueue(new Callback<MyResp>() {
            @Override
            public void onResponse(Call<MyResp> call, Response<MyResp> response) {
                MyResp list = response.body();
                Log.i(Retrofit.TAG, "list status:"+list.status+", tid:"+Thread.currentThread().getId());
            }

            @Override
            public void onFailure(Call<MyResp> call, Throwable t) {
                Log.i(Retrofit.TAG, "onFailure status:");
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

    public static void customconverterFactory() throws IOException {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(URL_BASIC_SERVICE_TEST)
                .addConverterFactory(new CustomConvertor())
                .addConverterFactory(new BuiltInConverters())
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

    public static void returnVoidconverterFactory() throws IOException {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(URL_BASIC_SERVICE_TEST)
                .addConverterFactory(new BuiltInConverters())
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

   static class ToStringConverter implements Converter<ResponseBody, String> {
        @Override
        public String convert(ResponseBody value) throws IOException {
            String result = value.string();
            return result;
        }
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
}
