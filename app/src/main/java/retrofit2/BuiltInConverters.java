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
package retrofit2;

import android.util.Log;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.http.Streaming;

public final  class BuiltInConverters extends Converter.Factory {
  @Override
  public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations,
      Retrofit retrofit) {
    Log.i(Retrofit.TAG,"responseBodyConverter type == ResponseBody.class?"+(type == ResponseBody.class));
    if (type == ResponseBody.class) {
      boolean isStreaming = Utils.isAnnotationPresent(annotations, Streaming.class);
      Log.i(Retrofit.TAG,"isStreaming :"+isStreaming);
      if (isStreaming) {
        return StreamingResponseBodyConverter.INSTANCE;
      } else {
        return  BufferingResponseBodyConverter.INSTANCE;
      }
    }
    if (type == Void.class) {
      return VoidResponseBodyConverter.INSTANCE;
    }
    Log.i(Retrofit.TAG,"return null");
    return null;
  }

  @Override
  public Converter<?, RequestBody> requestBodyConverter(Type type,
      Annotation[] parameterAnnotations, Annotation[] methodAnnotations, Retrofit retrofit) {
    if (RequestBody.class.isAssignableFrom(Utils.getRawType(type))) {
      Log.i(Retrofit.TAG, "return RequestBodyConverter.INSTANCE ");
      return RequestBodyConverter.INSTANCE;
    }
    return null;
  }

  static final class VoidResponseBodyConverter implements Converter<ResponseBody, Void> {
    static final VoidResponseBodyConverter INSTANCE = new VoidResponseBodyConverter();

    @Override public Void convert(ResponseBody value) throws IOException {
      value.close();
      return null;
    }
  }

  static final class RequestBodyConverter implements Converter<RequestBody, RequestBody> {
    static final RequestBodyConverter INSTANCE = new RequestBodyConverter();

    @Override public RequestBody convert(RequestBody value) throws IOException {
      Log.i(Retrofit.TAG,  "BuiltInConverters.this convert value  ");
      return value;
    }
  }

  static final class StreamingResponseBodyConverter
      implements Converter<ResponseBody, ResponseBody> {
    static final StreamingResponseBodyConverter INSTANCE = new StreamingResponseBodyConverter();

    @Override public ResponseBody convert(ResponseBody value) throws IOException {
      return value;
    }
  }

  static final class BufferingResponseBodyConverter
      implements Converter<ResponseBody, ResponseBody> {
    static final BufferingResponseBodyConverter INSTANCE = new BufferingResponseBodyConverter();

    @Override public ResponseBody convert(ResponseBody value) throws IOException {
      try {
        // Buffer the entire body to avoid future I/O.
        return Utils.buffer(value);
      } finally {
        value.close();
      }
    }
  }

  public static final class ToStringConverter implements Converter<Object, String> {
    public static final ToStringConverter INSTANCE = new ToStringConverter();

    @Override public String convert(Object value) {
      return value.toString();
    }
  }
}
