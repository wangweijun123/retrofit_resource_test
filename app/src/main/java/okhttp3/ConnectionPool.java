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
package okhttp3;

import android.util.Log;

import java.lang.ref.Reference;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import okhttp3.internal.Util;
import okhttp3.internal.connection.RealConnection;
import okhttp3.internal.connection.RouteDatabase;
import okhttp3.internal.connection.StreamAllocation;
import okhttp3.internal.platform.Platform;
import retrofit2.Retrofit;

import static okhttp3.internal.Util.closeQuietly;

/**
 * Manages reuse of HTTP and HTTP/2 connections for reduced network latency. HTTP requests that
 * share the same {@link Address} may share a {@link Connection}. This class implements the policy
 * of which connections to keep open for future use.
 */
public final class ConnectionPool {
  /**
   * Background threads are used to cleanup expired connections. There will be at most a single
   * thread running per connection pool. The thread pool executor permits the pool itself to be
   * garbage collected.
   */
  private static final Executor executor = new ThreadPoolExecutor(0 /* corePoolSize */,
      Integer.MAX_VALUE /* maximumPoolSize */, 60L /* keepAliveTime */, TimeUnit.SECONDS,
      new SynchronousQueue<Runnable>(), Util.threadFactory("OkHttp ConnectionPool", true));

  /** The maximum number of idle connections for each address. */
  /** 初始化5个连接 */
  private final int maxIdleConnections;
  /** 初始化每个连接活着的时间5分钟 */
  private final long keepAliveDurationNs;
  private final Runnable cleanupRunnable = new Runnable() {
    @Override public void run() {
      while (true) {
        long waitNanos = cleanup(System.nanoTime());
        if (waitNanos == -1) return;
        if (waitNanos > 0) {
          long waitMillis = waitNanos / 1000000L;
          waitNanos -= (waitMillis * 1000000L);
          synchronized (ConnectionPool.this) {
            try {
              ConnectionPool.this.wait(waitMillis, (int) waitNanos);
            } catch (InterruptedException ignored) {
            }
          }
        }
      }
    }
  };
  /**
   * 数组存储连接
   */
  private final Deque<RealConnection> connections = new ArrayDeque<>();
  // 是一个黑名单，用来记录不可用的route
  final RouteDatabase routeDatabase = new RouteDatabase();
  boolean cleanupRunning;

  /**
   * Create a new connection pool with tuning parameters appropriate for a single-user application.
   * The tuning parameters in this pool are subject to change in future OkHttp releases. Currently
   * this pool holds up to 5 idle connections which will be evicted after 5 minutes of inactivity.
   *
   */
  public ConnectionPool() {
    this(5, 5, TimeUnit.MINUTES);
  }

  public ConnectionPool(int maxIdleConnections, long keepAliveDuration, TimeUnit timeUnit) {
    this.maxIdleConnections = maxIdleConnections;
    this.keepAliveDurationNs = timeUnit.toNanos(keepAliveDuration);

    // Put a floor on the keep alive duration, otherwise cleanup will spin loop.
    if (keepAliveDuration <= 0) {
      throw new IllegalArgumentException("keepAliveDuration <= 0: " + keepAliveDuration);
    }
  }

  /** Returns the number of idle connections in the pool. */
  public synchronized int idleConnectionCount() {
    int total = 0;
    for (RealConnection connection : connections) {
      if (connection.allocations.isEmpty()) total++;
    }
    return total;
  }

  /**
   * Returns total number of connections in the pool. Note that prior to OkHttp 2.7 this included
   * only idle connections and HTTP/2 connections. Since OkHttp 2.7 this includes all connections,
   * both active and inactive. Use {@link #idleConnectionCount()} to count connections not currently
   * in use.
   */
  public synchronized int connectionCount() {
    return connections.size();
  }

  /**
   * Returns a recycled connection to {@code address}, or null if no such connection exists. The
   * route is null if the address has not yet been routed.
   */
  RealConnection get(Address address, StreamAllocation streamAllocation, Route route) {
    assert (Thread.holdsLock(this));
    for (RealConnection connection : connections) {
      if (connection.isEligible(address, route)) {
        streamAllocation.acquire(connection);
        return connection;
      }
    }
    return null;
  }

  /**
   * Replaces the connection held by {@code streamAllocation} with a shared connection if possible.
   * This recovers when multiple multiplexed connections are created concurrently.
   */
  Socket deduplicate(Address address, StreamAllocation streamAllocation) {
    assert (Thread.holdsLock(this));
    for (RealConnection connection : connections) {
      if (connection.isEligible(address, null)
          && connection.isMultiplexed()
          && connection != streamAllocation.connection()) {
        return streamAllocation.releaseAndAcquire(connection);
      }
    }
    return null;
  }

  void put(RealConnection connection) {
    assert (Thread.holdsLock(this));
    if (!cleanupRunning) {
      cleanupRunning = true;
      executor.execute(cleanupRunnable);
    }
    connections.add(connection);
    Log.i(Retrofit.TAG,"connections size:"+connections.size());
  }

  /**
   * Notify this pool that {@code connection} has become idle. Returns true if the connection has
   * been removed from the pool and should be closed.
   * 标识一个连接处于了空闲状态，即没有流任务
   */
  boolean connectionBecameIdle(RealConnection connection) {
    assert (Thread.holdsLock(this));
    Log.i(Retrofit.TAG,"connection.noNewStreams:"+connection.noNewStreams+", maxIdleConnections:"+maxIdleConnections);
    //这里noNewStream标志位之前说过，它可以理解为该连接已经不可用，所以可以直接清理，
    // 而maxIdleConnections==0则标识不允许有空闲连接，也是可以直接清理的
    if (connection.noNewStreams || maxIdleConnections == 0) {
      connections.remove(connection);
      Log.i(Retrofit.TAG,"connectionBecameIdle connections.remove(connection)  size"+connections.size());
      return true;
    } else {
      notifyAll(); // Awake the cleanup thread: we may have exceeded the idle connection limit.
      return false;
    }
  }

  /** Close and remove all idle connections in the pool. */
  public void evictAll() {
    List<RealConnection> evictedConnections = new ArrayList<>();
    synchronized (this) {
      for (Iterator<RealConnection> i = connections.iterator(); i.hasNext(); ) {
        RealConnection connection = i.next();
        if (connection.allocations.isEmpty()) {
          connection.noNewStreams = true;
          evictedConnections.add(connection);
          i.remove();
          Log.i(Retrofit.TAG,"evictAll connections remove size:"
                  +connections.size() + ", evictedConnections size:"+evictedConnections.size()+", noNewStreams is true");
        }
      }
    }

    for (RealConnection connection : evictedConnections) {
      closeQuietly(connection.socket());
    }
  }

  /**
   * Performs maintenance on this pool, evicting the connection that has been idle the longest if
   * either it has exceeded the keep alive limit or the idle connections limit.
   *
   * <p>Returns the duration in nanos to sleep until the next scheduled call to this method. Returns
   * -1 if no further cleanups are required.
   */
  long cleanup(long now) {
    int inUseConnectionCount = 0;// 正在使用的连接
    int idleConnectionCount = 0; // 空闲的连接
    RealConnection longestIdleConnection = null;
    long longestIdleDurationNs = Long.MIN_VALUE;

    // Find either a connection to evict, or the time that the next eviction is due.
    synchronized (this) {
      for (Iterator<RealConnection> i = connections.iterator(); i.hasNext(); ) {
        RealConnection connection = i.next();

        // If the connection is in use, keep searching.
        if (pruneAndGetAllocationCount(connection, now) > 0) {
          inUseConnectionCount++;
          Log.i(Retrofit.TAG, "正在使用的连接数量 inUseConnectionCount:"+inUseConnectionCount);
          continue;
        }

        idleConnectionCount++;
        Log.i(Retrofit.TAG, "空闲连接数量 idleConnectionCount:"+idleConnectionCount);
                // If the connection is ready to be evicted, we're done.
        long idleDurationNs = now - connection.idleAtNanos;
        // 找出空闲时间最长的连接以及对应的空闲时间
        Log.i(Retrofit.TAG, "idleDurationNs:"+idleDurationNs + ", longestIdleDurationNs:"+longestIdleDurationNs);
        if (idleDurationNs > longestIdleDurationNs) {
          longestIdleDurationNs = idleDurationNs;
          longestIdleConnection = connection;
        }
      }

      Log.i(Retrofit.TAG,"longestIdleDurationNs:"+longestIdleDurationNs+
      ", this.keepAliveDurationNs:"+this.keepAliveDurationNs +
      ", idleConnectionCount:"+idleConnectionCount +
      ", this.maxIdleConnections:"+this.maxIdleConnections);

      // 遵循两个指标,最大空闲连接数量和最大空闲时长的判断,满足其一，执行清理,然后循环执行
      if (longestIdleDurationNs >= this.keepAliveDurationNs
          || idleConnectionCount > this.maxIdleConnections) {
        // We've found a connection to evict. Remove it from the list, then close it below (outside
        // of the synchronized block).
        //在符合清理条件下，清理空闲时间最长的连接
        connections.remove(longestIdleConnection);
        Log.i(Retrofit.TAG,"connections.remove(longestIdleConnection connections size :"+connections.size());
      } else if (idleConnectionCount > 0) {
        // A connection will be ready to evict soon.
        Log.i(Retrofit.TAG, "不符合清理条件，则返回下次需要执行清理的等待时间");
        return keepAliveDurationNs - longestIdleDurationNs;
      } else if (inUseConnectionCount > 0) {
        // All connections are in use. It'll be at least the keep alive duration 'til we run again.
        Log.i(Retrofit.TAG,"没有空闲的连接，则隔keepAliveDuration之后再次执行");
        return keepAliveDurationNs;
      } else {
        // No connections, idle or in use.
        Log.i(Retrofit.TAG, "清理结束");
        cleanupRunning = false;
        return -1;
      }
    }

    closeQuietly(longestIdleConnection.socket());

    // Cleanup again immediately.
    //这里是在清理一个空闲时间最长的连接以后会执行到这里，需要立即再次执行清理
    return 0;
  }

  /**
   * Prunes any leaked allocations and then returns the number of remaining live allocations on
   * {@code connection}. Allocations are leaked if the connection is tracking them but the
   * application code has abandoned them. Leak detection is imprecise and relies on garbage
   * collection.
   *
   */
  private int pruneAndGetAllocationCount(RealConnection connection, long now) {
    List<Reference<StreamAllocation>> references = connection.allocations;
    int size = references.size();
    Log.i(Retrofit.TAG, "该连接上流的size ："+ size);
    for (int i = 0; i < size; ) {
      Reference<StreamAllocation> reference = references.get(i);
      StreamAllocation streamAllocation = reference.get();
      Log.i(Retrofit.TAG, "streamAllocation : " +streamAllocation);
      if (streamAllocation != null) {
        i++;
        continue;
      }
      Log.i(Retrofit.TAG, "该连接上没流的分配");
      // We've discovered a leaked allocation. This is an application bug.
      StreamAllocation.StreamAllocationReference streamAllocRef =
          (StreamAllocation.StreamAllocationReference) reference;
      String message = "A connection to " + connection.route().address().url()
          + " was leaked. Did you forget to close a response body?";
      Platform.get().logCloseableLeak(message, streamAllocRef.callStackTrace);

      references.remove(i);
      connection.noNewStreams = true;

      // If this was the last allocation, the connection is eligible for immediate eviction.
      if (references.isEmpty()) {
        connection.idleAtNanos = now - keepAliveDurationNs;
        return 0;
      }
    }

    return references.size();
  }
}
