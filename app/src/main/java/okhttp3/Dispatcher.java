/*
 * Copyright (C) 2013 Square, Inc.
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
package okhttp3;

import android.util.Log;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import okhttp3.RealCall.AsyncCall;
import okhttp3.internal.Util;
import retrofit2.Retrofit;

/**
 * Policy on when async requests are executed.
 *
 * <p>Each dispatcher uses an {@link ExecutorService} to run calls internally. If you supply your
 * own executor, it should be able to run {@linkplain #getMaxRequests the configured maximum} number
 * of calls concurrently.
 *
 * 请求的分发
 */
public final class Dispatcher {
  private int maxRequests = 64;// 并发最大请求数量64个请求
  private int maxRequestsPerHost = 5; // 而且同时并发一个host只能5个，否则排队等待
  private Runnable idleCallback;

  /** Executes calls. Created lazily. */
  // 消费者线程,完成请求的处理
  private ExecutorService executorService;

  /** 异步请求的缓存队列，当正在运行的请求数量大于最大请求数量，先把它放到准备请求队列中暂存，
   * 当某一个请求完成后，从准备请求队列中取出添加到runningsAysnCalls，立马执行请求 */
  /** Ready async calls in the order they'll be run. */

  /** 这三个队列只是对call的一个引用，new 的线程的workQueue才是真正的任务队列，这个为了每台主机同时运行5个任务来计算的*/
  // 缓存队列，并发超过64个或者同一个域名超过5个，请求call进入缓存队列
  private final Deque<AsyncCall> readyAsyncCalls = new ArrayDeque<>();

  /** Running asynchronous calls. Includes canceled calls that haven't finished yet. */
  // 正在运行的队列(异步),在请求之前加入队列，请求完成从队列中移除,
          // 只是一个引用，来判断请求的并发量，注意它并不是消费者缓存
  private final Deque<AsyncCall> runningAsyncCalls = new ArrayDeque<>();

  /** Running synchronous calls. Includes canceled calls that haven't finished yet. */
  // 这个队列也是一样，只是它是一个同步的队列
  private final Deque<RealCall> runningSyncCalls = new ArrayDeque<>();

  public Dispatcher(ExecutorService executorService) {
    this.executorService = executorService;
  }

  public Dispatcher() {
  }

  public ExecutorService executorService() {
    if (executorService == null) {
      synchronized (Dispatcher.this) {
        if (executorService == null) {
          // 执行任务的线程数量无限大，当然，空闲时间只有一分中
          // 线程池，最少0个，最大就是max，所以有上面deque来控制线程数量，不可能无限增大，
          // 线程生命周期为一分钟，无边界限制的线程池
          // SynchronousQueue 任务队列size=0 任务队列，也就是最快消费
          //SynchronousQueue每个插入操作必须等待另一个线程的移除操作，
          // 同样任何一个移除操作都等待另一个线程的插入操作。
          // 因此队列内部其实没有任何一个元素，或者说容量为0，
          // 严格说并不是一种容器，由于队列没有容量
          // 高频请求场景，无疑是最合适的
          executorService = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60, TimeUnit.SECONDS,
                  new SynchronousQueue<Runnable>(), Util.threadFactory("OkHttp Dispatcher", false));
        }
      }
    }
    Log.i("wang", this + " executorService:" + executorService);
    return executorService;
  }

  /**
   * Set the maximum number of requests to execute concurrently. Above this requests queue in
   * memory, waiting for the running calls to complete.
   *
   * <p>If more than {@code maxRequests} requests are in flight when this is invoked, those requests
   * will remain in flight.
   */
  public synchronized void setMaxRequests(int maxRequests) {
    if (maxRequests < 1) {
      throw new IllegalArgumentException("max < 1: " + maxRequests);
    }
    this.maxRequests = maxRequests;
    promoteCalls();
  }

  public synchronized int getMaxRequests() {
    return maxRequests;
  }

  /**
   * Set the maximum number of requests for each host to execute concurrently. This limits requests
   * by the URL's host name. Note that concurrent requests to a single IP address may still exceed
   * this limit: multiple hostnames may share an IP address or be routed through the same HTTP
   * proxy.
   *
   * <p>If more than {@code maxRequestsPerHost} requests are in flight when this is invoked, those
   * requests will remain in flight.
   */
  public synchronized void setMaxRequestsPerHost(int maxRequestsPerHost) {
    if (maxRequestsPerHost < 1) {
      throw new IllegalArgumentException("max < 1: " + maxRequestsPerHost);
    }
    this.maxRequestsPerHost = maxRequestsPerHost;
    promoteCalls();
  }

  public synchronized int getMaxRequestsPerHost() {
    return maxRequestsPerHost;
  }

  /**
   * Set a callback to be invoked each time the dispatcher becomes idle (when the number of running
   * calls returns to zero).
   *
   * <p>Note: The time at which a {@linkplain Call call} is considered idle is different depending
   * on whether it was run {@linkplain Call#enqueue(Callback) asynchronously} or
   * {@linkplain Call#execute() synchronously}. Asynchronous calls become idle after the
   * {@link Callback#onResponse onResponse} or {@link Callback#onFailure onFailure} callback has
   * returned. Synchronous calls become idle once {@link Call#execute() execute()} returns. This
   * means that if you are doing synchronous calls the network layer will not truly be idle until
   * every returned {@link Response} has been closed.
   */
  public synchronized void setIdleCallback(Runnable idleCallback) {
    this.idleCallback = idleCallback;
  }

  synchronized void enqueue(AsyncCall call) {
    Log.i(Retrofit.TAG, "runningAsyncCalls.size()=="+(runningAsyncCalls.size())+", maxRequests:"+maxRequests +
            ", runningCallsForHost(call)="+(runningCallsForHost(call))+", maxRequestsPerHost:"+maxRequestsPerHost);
    // 如果正在执行的请求小于设定值即64，并且请求同一个主机的request小于设定值即5
    if (runningAsyncCalls.size() < maxRequests && runningCallsForHost(call) < maxRequestsPerHost) {
      Log.i(Retrofit.TAG, "异步的call满足条件(正在执行的request不超过64的默认值，同一个主机的request不能超过5个)，直接添加到runningAsyncCalls双端队列，线程池立马执行它");
      runningAsyncCalls.add(call);
      executorService().execute(call);
    } else {
      Log.i(Retrofit.TAG, "不满足条件，进入缓存异步调用队列readyAsyncCalls");
      readyAsyncCalls.add(call);
    }
  }

  /**
   * Cancel all calls currently enqueued or executing. Includes calls executed both {@linkplain
   * Call#execute() synchronously} and {@linkplain Call#enqueue asynchronously}.
   */
  public synchronized void cancelAll() {
    for (AsyncCall call : readyAsyncCalls) {
      call.get().cancel();
    }

    for (AsyncCall call : runningAsyncCalls) {
      call.get().cancel();
    }

    for (RealCall call : runningSyncCalls) {
      call.cancel();
    }
  }

  private void promoteCalls() {
    // 从缓存队列移除call，添加进正在运行队列,这里没看到同步，是因为调用它的方法做了同步
    if (runningAsyncCalls.size() >= maxRequests) return; // Already running max capacity.
    if (readyAsyncCalls.isEmpty()) return; // No ready calls to promote.

    for (Iterator<AsyncCall> i = readyAsyncCalls.iterator(); i.hasNext(); ) {
      AsyncCall call = i.next();

      if (runningCallsForHost(call) < maxRequestsPerHost) {
        i.remove();
        runningAsyncCalls.add(call);
        executorService().execute(call);
      }

      if (runningAsyncCalls.size() >= maxRequests) return; // Reached max capacity.
    }
  }

  /** Returns the number of running calls that share a host with {@code call}. */
  private int runningCallsForHost(AsyncCall call) {
    int result = 0;
    for (AsyncCall c : runningAsyncCalls) {
      if (c.host().equals(call.host())) result++;
    }
    return result;
  }

  /** Used by {@code Call#execute} to signal it is in-flight. */
  synchronized void executed(RealCall call) {
    runningSyncCalls.add(call);
    Log.i(Retrofit.TAG, "Dispatcher executed ..runningSyncCalls.add(call) size:" + runningSyncCalls.size());
  }

  /** Used by {@code AsyncCall#run} to signal completion. */
  void finished(AsyncCall call) {
    finished(runningAsyncCalls, call, true);
  }

  /** Used by {@code Call#execute} to signal completion. */
  void finished(RealCall call) {
    finished(runningSyncCalls, call, false);
  }

  private <T> void finished(Deque<T> calls, T call, boolean promoteCalls) {
    int runningCallsCount;
    Runnable idleCallback;
    synchronized (this) {
      if (!calls.remove(call)) throw new AssertionError("Call wasn't in-flight!");
      Log.i(Retrofit.TAG,"promoteCalls:"+promoteCalls+ "  从Dispatcher双端队列中remove call from calls and calls size:"+calls.size()+", 所以realcall其实只是一个应用而已");
      if (promoteCalls) promoteCalls();
      runningCallsCount = runningCallsCount();
      idleCallback = this.idleCallback;
    }

    if (runningCallsCount == 0 && idleCallback != null) {
      idleCallback.run();
    }
  }

  /** Returns a snapshot of the calls currently awaiting execution. */
  public synchronized List<Call> queuedCalls() {
    List<Call> result = new ArrayList<>();
    for (AsyncCall asyncCall : readyAsyncCalls) {
      result.add(asyncCall.get());
    }
    return Collections.unmodifiableList(result);
  }

  /** Returns a snapshot of the calls currently being executed. */
  public synchronized List<Call> runningCalls() {
    List<Call> result = new ArrayList<>();
    result.addAll(runningSyncCalls);
    for (AsyncCall asyncCall : runningAsyncCalls) {
      result.add(asyncCall.get());
    }
    return Collections.unmodifiableList(result);
  }

  public synchronized int queuedCallsCount() {
    return readyAsyncCalls.size();
  }

  public synchronized int runningCallsCount() {
    return runningAsyncCalls.size() + runningSyncCalls.size();
  }
}
