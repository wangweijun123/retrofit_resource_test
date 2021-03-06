/*
 * Copyright (C) 2015 Square, Inc.
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
package retrofit2.helpers;

import android.util.Log;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

public class ToStringConverterFactory extends Converter.Factory {
  static final MediaType MEDIA_TYPE = MediaType.parse("text/plain");

  /**
   * 把ResponseBody 转化为自己想要的对象
   * @param type
   * @param annotations
   * @param retrofit
   * @return
   */
  @Override
  public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations,
      Retrofit retrofit) {
    if (String.class.equals(type)) {
      return new Converter<ResponseBody, String>() {
        @Override public String convert(ResponseBody value) throws IOException {
          String str = value.string();
          Log.i(Retrofit.TAG, "responseBodyConverter value.string():"+str);
          return str;
        }
      };
    }
    return null;
  }

  /**
   *
   * @param type
   * @param parameterAnnotations
   * @param methodAnnotations
   * @param retrofit
   * @return
   */
  @Override public Converter<?, RequestBody> requestBodyConverter(Type type,
      Annotation[] parameterAnnotations, Annotation[] methodAnnotations, Retrofit retrofit) {
    Log.i(Retrofit.TAG, "ToStringConverterFactory requestBodyConverter value:");
    if (String.class.equals(type)) {
      return new Converter<String, RequestBody>() {
        @Override public RequestBody convert(String value) throws IOException {
          Log.i(Retrofit.TAG, "requestBodyConverter value:"+value);
          return RequestBody.create(MEDIA_TYPE, value);
        }
      };
    }
    return null;
  }
}
