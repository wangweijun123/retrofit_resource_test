/*
 * Copyright (C) 2012 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.retrofit;

import android.util.Log;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;

public final class SimpleService {
  public static final String API_URL = "https://api.github.com";

  public static class Contributor {
    public final String login;
    public final int contributions;

    public Contributor(String login, int contributions) {
      this.login = login;
      this.contributions = contributions;
    }
  }

  public interface GitHub {
    @GET("/repos/{owner}/{repo}/contributors")
    Call<List<Contributor>> contributors(
            @Path("owner") String owner,
            @Path("repo") String repo);


    @GET("/")
    Call<String> testHttpsBaidu();
  }

  /**
   * @throws IOException
   */
  public static void syncRequest() throws IOException {
    // Create a very simple REST adapter which points the GitHub API.
    // Https://www.baidu.com/   从CA申请的证书, okhttp可以直接访问,我没改动代码，
    // 也没写与证书相关的代码，是底层处理了吗, 但是设置代理后出现SSLHandshakeException:

    // https://kyfw.12306.cn/otn/   自签名的证书,握手失败
    // javax.net.ssl.SSLHandshakeException:
    // java.security.cert.CertPathValidatorException:
    // Trust anchor for certification path not found.
    Retrofit retrofit = new Retrofit.Builder()
        .baseUrl(API_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build();

    // Create an instance of our GitHub API interface.
    GitHub github = retrofit.create(GitHub.class);

    // Create a call instance for looking up Retrofit contributors.
    Call<List<Contributor>> call = github.contributors("square", "retrofit");

    // Fetch and print a list of the contributors to the library.
    List<Contributor> contributors = call.execute().body();
    for (Contributor contributor : contributors) {
      Log.i("wang", "thread id:" + Thread.currentThread().getId() + " , " + contributor.login + " (" + contributor.contributions + ")");
    }
  }


  public static void testBaiduHttps() throws IOException {
    // Create a very simple REST adapter which points the GitHub API.

    String url = "Https://www.baidu.com/";
    //   https://kyfw.12306.cn/otn/
    //  https://kyfw.12306.cn/otn/
    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(new StoreService.CustomConvertor())
            .build();

    // Create an instance of our GitHub API interface.
    GitHub github = retrofit.create(GitHub.class);

    // Create a call instance for looking up Retrofit contributors.
    Call<String> call = github.testHttpsBaidu();

    call.enqueue(new Callback<String>() {
      @Override
      public void onResponse(Call<String> call, Response<String> response) {
        String contributors = response.body();
        Log.i("wang", "thread id:" + Thread.currentThread().getId()
                + " , " + contributors);
      }

      @Override
      public void onFailure(Call<String> call, Throwable t) {
        t.printStackTrace();
        Log.i("wang", "onFailure ");
      }
    });
  }


  /**
   * 异步请求
   * @throws IOException
   */
  public static void asyncRequest() throws IOException {
    // Create a very simple REST adapter which points the GitHub API.
    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(API_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    // Create an instance of our GitHub API interface.
    GitHub github = retrofit.create(GitHub.class);

    // Create a call instance for looking up Retrofit contributors.
    Call<List<Contributor>> call = github.contributors("square", "retrofit");

    call.enqueue(new Callback<List<Contributor>>() {
      @Override
      public void onResponse(Call<List<Contributor>> call, Response<List<Contributor>> response) {
        List<Contributor> contributors = response.body();
        for (Contributor contributor : contributors) {
          Log.i("wang", "thread id:" + Thread.currentThread().getId() + " , " + contributor.login + " (" + contributor.contributions + ")");
        }
        for (Contributor contributor : contributors) {
          System.out.println("thread id:" + Thread.currentThread().getId() + " , " + contributor.login + " (" + contributor.contributions + ")");
        }
      }

      @Override
      public void onFailure(Call<List<Contributor>> call, Throwable t) {
        t.printStackTrace();
        Log.i("wang", "onFailure ");
      }
    });
  }


}
