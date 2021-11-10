package com.example.wangweijun1.retrofit_xxx;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.webkit.WebView;

/**
 * 加载本地html, zip过来解压, 然后再加载
 *
 */
public class WebActivity extends Activity {
//    public static final String htmlPath = "file:///android_asset/aidl.html";
    public static final String htmlPath = "file:///mnt/sdcard/test/aidl.html";
    public static final String baseUrl = "file:///mnt/sdcard/test/";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        WebView webView = findViewById(R.id.webview);
        webView.loadUrl(htmlPath);

//        webView.loadDataWithBaseURL(baseUrl, data, "text/html", "utf-8", null);
    }
}
