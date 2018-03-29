package retrofit2;

import com.example.wangweijun1.retrofit_xxx.MyApplication;

import java.io.IOException;

import okhttp3.*;
import okhttp3.Response;

/**
 * Created by wangweijun on 2018/1/30.
 * 1 有网络是，根据客户端设置的时间来判断是否使用缓存，还是网络请求
 * 2 没有网络时，直接使用缓存(注意这里还是要判断是否缓存)，提升用户体验
 */

public class CacheControlInterceptor implements Interceptor{
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        if (!NetworkUtil.isNetworkAvailable(MyApplication.myApplication)) {
            request = request.newBuilder()
                    .cacheControl(CacheControl.FORCE_CACHE)
                    .build();
        }

        Response originalResponse = chain.proceed(request);

        if (NetworkUtil.isNetworkAvailable(MyApplication.myApplication)) {

            String cacheControl = request.cacheControl().toString();

            return originalResponse.newBuilder()
                    .header("Cache-Control", cacheControl)
                    .build();
        } else {
            return originalResponse.newBuilder()
                    .header("Cache-Control", CacheControl.FORCE_CACHE.toString())
                    .build();
        }
    }
}
