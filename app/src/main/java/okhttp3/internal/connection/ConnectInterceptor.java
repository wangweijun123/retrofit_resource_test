/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package okhttp3.internal.connection;

import android.util.Log;

import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.internal.http.HttpCodec;
import okhttp3.internal.http.RealInterceptorChain;
import retrofit2.Retrofit;
/**
 * 访问网络，好像是在访问api接口前，提前发现网络问题
 * 创建三个重要的对象， StreamAllocation(这个已经在Retry..Interceptor类中创建)，
 * RealConnection(从连接池中找到)， HttpCodec(在连接上建立新的流)
 */

/** Opens a connection to the target server and proceeds to the next interceptor. */
public final class ConnectInterceptor implements Interceptor {
  public final OkHttpClient client;

  public ConnectInterceptor(OkHttpClient client) {
    this.client = client;
  }

  @Override public Response intercept(Chain chain) throws IOException {
    Log.i(Retrofit.TAG, this + " intercept start ...");
    RealInterceptorChain realChain = (RealInterceptorChain) chain;
    Request request = realChain.request();
    // streamAllocation 是在RetryAndFollowupInterceptor.java 中生成
    StreamAllocation streamAllocation = realChain.streamAllocation();

    // We need the network to satisfy this request. Possibly for validating a conditional GET.
    boolean doExtensiveHealthChecks = !request.method().equals("GET");
    // 这里为何要去newStream 产生一个新的HttpCodec对象
    HttpCodec httpCodec = streamAllocation.newStream(client, doExtensiveHealthChecks);
    RealConnection connection = streamAllocation.connection();

    Response response = realChain.proceed(request, streamAllocation, httpCodec, connection);
    Log.i(Retrofit.TAG, this + " intercept end");
    return response;
  }
}
