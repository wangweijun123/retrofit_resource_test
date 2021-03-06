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
import java.lang.ref.Reference;
import java.net.ConnectException;
import java.net.ProtocolException;
import java.net.Proxy;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownServiceException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import okhttp3.Address;
import okhttp3.CertificatePinner;
import okhttp3.Connection;
import okhttp3.ConnectionPool;
import okhttp3.ConnectionSpec;
import okhttp3.Handshake;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import okhttp3.internal.Internal;
import okhttp3.internal.Util;
import okhttp3.internal.Version;
import okhttp3.internal.http.HttpCodec;
import okhttp3.internal.http.HttpHeaders;
import okhttp3.internal.http1.Http1Codec;
import okhttp3.internal.http2.ErrorCode;
import okhttp3.internal.http2.Http2Codec;
import okhttp3.internal.http2.Http2Connection;
import okhttp3.internal.http2.Http2Stream;
import okhttp3.internal.platform.Platform;
import okhttp3.internal.tls.OkHostnameVerifier;
import okhttp3.internal.ws.RealWebSocket;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;
import okio.Source;
import retrofit2.Retrofit;

import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_PROXY_AUTH;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static okhttp3.internal.Util.closeQuietly;

public final class RealConnection extends Http2Connection.Listener implements Connection {
  private final ConnectionPool connectionPool;
  private final Route route;

  // The fields below are initialized by connect() and never reassigned.
//下面这些字段，通过connect()方法开始初始化，并且绝对不会再次赋值
  /** The low-level TCP socket. */
  private Socket rawSocket;

  /**
   * The application layer socket. Either an {@link SSLSocket} layered over {@link #rawSocket}, or
   * {@link #rawSocket} itself if this connection does not use SSL.
   * TCP连接或者说socket连接，或者我们常说三次握手,
   * 释放realse连接，断开握手
   * 应用层sockt，所以有可能是SslSocket
   */
  private Socket socket;// 应用层的socket, 协议是http的话就是rawSocket, 协议是https的话就是sslSocket
  private Handshake handshake;
  private Protocol protocol;
  private Http2Connection http2Connection;
  private BufferedSource source;// 相当于输入流
  private BufferedSink sink; // 相当于输出流

  // The fields below track connection state and are guarded by connectionPool.
//下面这个字段是 属于表示链接状态的字段，并且有connectPool统一管理
  /** If true, no new streams can be created on this connection. Once true this is always true. */

  /**  noNewStream可以简单理解为它表示该连接不可用。这个值一旦被设为true,则这个conncetion则不会再创建stream。*/
  public boolean noNewStreams;

  public int successCount;

  /**
   * The maximum number of concurrent streams that can be carried by this connection. If {@code
   * allocations.size() < allocationLimit} then new streams can be created on this connection.
   * 连接之上分配流个上线,在http1.x中，在一个连接之上只能最多建立一个stream,如果并发请求
   * 必须建立多个socket, 在http2.0可以同时并发N个stream
   */
  //此链接可以承载最大并发流的限制，如果不超过限制，可以随意增加
  public int allocationLimit = 1;

  /** Current streams carried by this connection.
   * 当前连接上引用多少个StreamAllocation， http1.x只容许一个
   * 为何连接上的流管理不在realconnection 中执行(add , remove)
   * 引用计数的来实现
   * */
  public final List<Reference<StreamAllocation>> allocations = new ArrayList<>();

  /** Nanotime timestamp when {@code allocations.size()} reached zero. */
  /** 记录该连接处于空闲状态的时间 */
  public long idleAtNanos = Long.MAX_VALUE;

  public RealConnection(ConnectionPool connectionPool, Route route) {
    this.connectionPool = connectionPool;
    this.route = route;
  }

  public static RealConnection testConnection(
      ConnectionPool connectionPool, Route route, Socket socket, long idleAtNanos) {
    RealConnection result = new RealConnection(connectionPool, route);
    result.socket = socket;
    result.idleAtNanos = idleAtNanos;
    return result;
  }

  public void connect(
      int connectTimeout, int readTimeout, int writeTimeout, boolean connectionRetryEnabled) {
    // protocol 它表示在整个连接建立
    if (protocol != null) throw new IllegalStateException("already connected");

    RouteException routeException = null;
    List<ConnectionSpec> connectionSpecs = route.address().connectionSpecs();
    ConnectionSpecSelector connectionSpecSelector = new ConnectionSpecSelector(connectionSpecs);

    if (route.address().sslSocketFactory() == null) {
      if (!connectionSpecs.contains(ConnectionSpec.CLEARTEXT)) {
        throw new RouteException(new UnknownServiceException(
            "CLEARTEXT communication not enabled for client"));
      }
      String host = route.address().url().host();
      if (!Platform.get().isCleartextTrafficPermitted(host)) {
        throw new RouteException(new UnknownServiceException(
            "CLEARTEXT communication to " + host + " not permitted by network security policy"));
      }
    }

    while (true) {
      try {
        boolean istunnel = route.requiresTunnel();
        Log.i(Retrofit.TAG, "是隧道吗 istunnel : "+ istunnel);
        // 如果要求隧道模式，建立通道连接，通常不是这种
        if (istunnel) {
          connectTunnel(connectTimeout, readTimeout, writeTimeout);
        } else {
          // 一般都走这条逻辑了，实际上很简单就是socket的连接
          connectSocket(connectTimeout, readTimeout);
        }
        Log.i(Retrofit.TAG,"socket 连接建立后，确定传输协议");
        establishProtocol(connectionSpecSelector);
        break;// 不抛异常就终止循环了
      } catch (IOException e) {
        closeQuietly(socket);
        closeQuietly(rawSocket);
        socket = null;
        rawSocket = null;
        source = null;
        sink = null;
        handshake = null;
        protocol = null;
        http2Connection = null;

        if (routeException == null) {
          routeException = new RouteException(e);
        } else {
          routeException.addConnectException(e);
        }

        if (!connectionRetryEnabled || !connectionSpecSelector.connectionFailed(e)) {
          throw routeException;
        }
      }
    }

    if (http2Connection != null) {
      synchronized (connectionPool) {
        allocationLimit = http2Connection.maxConcurrentStreams();
      }
    }
  }

  /**
   * Does all the work to build an HTTPS connection over a proxy tunnel. The catch here is that a
   * proxy server can issue an auth challenge and then close the connection.
   */
  private void connectTunnel(int connectTimeout, int readTimeout, int writeTimeout)
      throws IOException {
    Request tunnelRequest = createTunnelRequest();
    HttpUrl url = tunnelRequest.url();
    int attemptedConnections = 0;
    int maxAttempts = 21;
    while (true) {
      if (++attemptedConnections > maxAttempts) {
        throw new ProtocolException("Too many tunnel connections attempted: " + maxAttempts);
      }

      connectSocket(connectTimeout, readTimeout);
      tunnelRequest = createTunnel(readTimeout, writeTimeout, tunnelRequest, url);

      if (tunnelRequest == null) break; // Tunnel successfully created.

      // The proxy decided to close the connection after an auth challenge. We need to create a new
      // connection, but this time with the auth credentials.
      closeQuietly(rawSocket);
      rawSocket = null;
      sink = null;
      source = null;
    }
  }

  /** Does all the work necessary to build a full HTTP or HTTPS connection on a raw socket. */
  private void connectSocket(int connectTimeout, int readTimeout) throws IOException {
    Proxy proxy = route.proxy();
    Address address = route.address();
    boolean proxytype = (proxy.type() == Proxy.Type.DIRECT || proxy.type() == Proxy.Type.HTTP);
    Log.i(Retrofit.TAG,"proxytype is direct or http ? : "+proxytype + " address.socketFactory() : "+address.socketFactory());
    rawSocket = proxytype
        ? address.socketFactory().createSocket()
        : new Socket(proxy);
    Log.i(Retrofit.TAG, "rawSocket:"+rawSocket.hashCode());
    rawSocket.setSoTimeout(readTimeout);
    try {
      //内部就是调用AndroidPlatform--->socket.connect(InetAddress, timeout)方法建立连接
      Platform.get().connectSocket(rawSocket, route.socketAddress(), connectTimeout);
    } catch (ConnectException e) {
      ConnectException ce = new ConnectException("Failed to connect to " + route.socketAddress());
      ce.initCause(e);
      throw ce;
    }
    Log.i(Retrofit.TAG, "建立连接后，通过socket获取输入输出流，实例化source and sink,注意source 与sink是Realconnection 属性，其他都是传过去的");
    source = Okio.buffer(Okio.source(rawSocket));
    sink = Okio.buffer(Okio.sink(rawSocket));
  }

  private void establishProtocol(ConnectionSpecSelector connectionSpecSelector) throws IOException {
    Log.i(Retrofit.TAG,"establishProtocol socketFactory : "+
            (route.address().sslSocketFactory() == null));
    if (route.address().sslSocketFactory() == null) {
      // 明文传输1.1
      protocol = Protocol.HTTP_1_1;
      Log.i(Retrofit.TAG, "protocol http1.1 socket is rawSocket");
      socket = rawSocket;
      return;
    }
    Log.i(Retrofit.TAG,"connectTls ...");
    connectTls(connectionSpecSelector);

    if (protocol == Protocol.HTTP_2) {
      socket.setSoTimeout(0); // HTTP/2 connection timeouts are set per-stream.
      http2Connection = new Http2Connection.Builder(true)
          .socket(socket, route.address().url().host(), source, sink)
          .listener(this)
          .build();
      http2Connection.start();
    }
  }

  private void connectTls(ConnectionSpecSelector connectionSpecSelector) throws IOException {
    Address address = route.address();
    SSLSocketFactory sslSocketFactory = address.sslSocketFactory();
    boolean success = false;
    SSLSocket sslSocket = null;
    try {
      // Create the wrapper over the connected socket.
      // 基于原始的socket,创建一个 sslSocket
      sslSocket = (SSLSocket) sslSocketFactory.createSocket(
          rawSocket, address.url().host(), address.url().port(), true /* autoClose */);

      // Configure the socket's ciphers, TLS versions, and extensions.
      ConnectionSpec connectionSpec = connectionSpecSelector.configureSecureSocket(sslSocket);
      if (connectionSpec.supportsTlsExtensions()) {
        Platform.get().configureTlsExtensions(
            sslSocket, address.url().host(), address.protocols());
      }

      // Force handshake. This can throw!
        // 如果没有CA认证过的https请求，这里会报错， (设置代理CA认证的证书也报错)
      // Trust anchor for certification path not found.
      Log.i(Retrofit.TAG,"startHandshake ...只有再https协议下才嗲用sslSocket.startHandshake,如果设置了代理，这里回报错");
      sslSocket.startHandshake();
      Log.i(Retrofit.TAG,"startHandshake finised");
      Handshake unverifiedHandshake = Handshake.get(sslSocket.getSession());

      // Verify that the socket's certificates are acceptable for the target host.
      if (!address.hostnameVerifier().verify(address.url().host(), sslSocket.getSession())) {
        X509Certificate cert = (X509Certificate) unverifiedHandshake.peerCertificates().get(0);
        throw new SSLPeerUnverifiedException("Hostname " + address.url().host() + " not verified:"
            + "\n    certificate: " + CertificatePinner.pin(cert)
            + "\n    DN: " + cert.getSubjectDN().getName()
            + "\n    subjectAltNames: " + OkHostnameVerifier.allSubjectAltNames(cert));
      }

      // Check that the certificate pinner is satisfied by the certificates presented.
      address.certificatePinner().check(address.url().host(),
          unverifiedHandshake.peerCertificates());

      // Success! Save the handshake and the ALPN protocol.
      String maybeProtocol = connectionSpec.supportsTlsExtensions()
          ? Platform.get().getSelectedProtocol(sslSocket)
          : null;
      Log.i(Retrofit.TAG,"socket is sslSocket");
      socket = sslSocket;
      source = Okio.buffer(Okio.source(socket));
      sink = Okio.buffer(Okio.sink(socket));
      handshake = unverifiedHandshake;
      protocol = maybeProtocol != null
          ? Protocol.get(maybeProtocol)
          : Protocol.HTTP_1_1;
      success = true;
    } catch (AssertionError e) {
      if (Util.isAndroidGetsocknameError(e)) throw new IOException(e);
      throw e;
    } finally {
      if (sslSocket != null) {
        Platform.get().afterHandshake(sslSocket);
      }
      if (!success) {
        closeQuietly(sslSocket);
      }
    }
  }

  /**
   * To make an HTTPS connection over an HTTP proxy, send an unencrypted CONNECT request to create
   * the proxy connection. This may need to be retried if the proxy requires authorization.
   */
  private Request createTunnel(int readTimeout, int writeTimeout, Request tunnelRequest,
      HttpUrl url) throws IOException {
    // Make an SSL Tunnel on the first message pair of each SSL + proxy connection.
    String requestLine = "CONNECT " + Util.hostHeader(url, true) + " HTTP/1.1";
    while (true) {
      Http1Codec tunnelConnection = new Http1Codec(null, null, source, sink);
      source.timeout().timeout(readTimeout, MILLISECONDS);
      sink.timeout().timeout(writeTimeout, MILLISECONDS);
      tunnelConnection.writeRequest(tunnelRequest.headers(), requestLine);
      tunnelConnection.finishRequest();
      Response response = tunnelConnection.readResponseHeaders(false)
          .request(tunnelRequest)
          .build();
      // The response body from a CONNECT should be empty, but if it is not then we should consume
      // it before proceeding.
      long contentLength = HttpHeaders.contentLength(response);
      if (contentLength == -1L) {
        contentLength = 0L;
      }
      Source body = tunnelConnection.newFixedLengthSource(contentLength);
      Util.skipAll(body, Integer.MAX_VALUE, TimeUnit.MILLISECONDS);
      body.close();

      switch (response.code()) {
        case HTTP_OK:
          // Assume the server won't send a TLS ServerHello until we send a TLS ClientHello. If
          // that happens, then we will have buffered bytes that are needed by the SSLSocket!
          // This check is imperfect: it doesn't tell us whether a handshake will succeed, just
          // that it will almost certainly fail because the proxy has sent unexpected data.
          if (!source.buffer().exhausted() || !sink.buffer().exhausted()) {
            throw new IOException("TLS tunnel buffered too many bytes!");
          }
          return null;

        case HTTP_PROXY_AUTH:
          tunnelRequest = route.address().proxyAuthenticator().authenticate(route, response);
          if (tunnelRequest == null) throw new IOException("Failed to authenticate with proxy");

          if ("close".equalsIgnoreCase(response.header("Connection"))) {
            return tunnelRequest;
          }
          break;

        default:
          throw new IOException(
              "Unexpected response code for CONNECT: " + response.code());
      }
    }
  }

  /**
   * Returns a request that creates a TLS tunnel via an HTTP proxy. Everything in the tunnel request
   * is sent unencrypted to the proxy server, so tunnels include only the minimum set of headers.
   * This avoids sending potentially sensitive data like HTTP cookies to the proxy unencrypted.
   */
  private Request createTunnelRequest() {
    return new Request.Builder()
        .url(route.address().url())
        .header("Host", Util.hostHeader(route.address().url(), true))
        .header("Proxy-Connection", "Keep-Alive") // For HTTP/1.0 proxies like Squid.
        .header("User-Agent", Version.userAgent())
        .build();
  }

  /**
   * Returns true if this connection can carry a stream allocation to {@code address}. If non-null
   * {@code route} is the resolved route for a connection.
   * 这个方法主要是判断面对给出的addres和route，这个realConnetion是否可以重用
   */

  public boolean isEligible(Address address, Route route) {
    // If this connection is not accepting new streams, we're done.
    Log.i(Retrofit.TAG, "address:"+address+", route:"+route);
    Log.i(Retrofit.TAG, "allocations.size():"+allocations.size()+", allocationLimit:"+allocationLimit+", noNewStreams:"+noNewStreams);
    // 如果连接正在使用或者链接已经不能使用，不能重用
    if (allocations.size() >= allocationLimit || noNewStreams) return false;

    // If the non-host fields of the address don't overlap, we're done.
    // 判断是否是同一个地址，要求很多，不只是host与端口号，proxy啥得
    if (!Internal.instance.equalsNonHost(this.route.address(), address)) return false;

    // If the host exactly matches, we're done: this connection can carry the address.
    String newAddressUrl = address.url().host();
    String inConnectionPoolAddressUrl = this.route().address().url().host();
    Log.i(Retrofit.TAG, "newAddressUrl:"+newAddressUrl+", inConnectionPoolAddressUrl:"+inConnectionPoolAddressUrl + ", is same ? "+ (newAddressUrl.equals(inConnectionPoolAddressUrl)));

    if (address.url().host().equals(this.route().address().url().host())) {// 对比的是host
      return true; // This connection is a perfect match.
    }

    // At this point we don't have a hostname match. But we still be able to carry the request if
    // our connection coalescing requirements are met. See also:
    // https://hpbn.co/optimizing-application-delivery/#eliminate-domain-sharding
    // https://daniel.haxx.se/blog/2016/08/18/http2-connection-coalescing/

    // 1. This connection must be HTTP/2.
    Log.i(Retrofit.TAG, "http2Connection:"+http2Connection);
    if (http2Connection == null) return false;

    // 2. The routes must share an IP address. This requires us to have a DNS address for both
    // hosts, which only happens after route planning. We can't coalesce connections that use a
    // proxy, since proxies don't tell us the origin server's IP address.
    Log.i(Retrofit.TAG, "route:"+route);
    if (route == null) return false;
    Log.i(Retrofit.TAG, "rroute.proxy().type() != Proxy.Type.DIRECT :"+(route.proxy().type() != Proxy.Type.DIRECT));
    if (route.proxy().type() != Proxy.Type.DIRECT) return false;
    if (this.route.proxy().type() != Proxy.Type.DIRECT) return false;
    if (!this.route.socketAddress().equals(route.socketAddress())) return false;

    // 3. This connection's server certificate's must cover the new host.
    Log.i(Retrofit.TAG, "route.address().hostnameVerifier():"+route.address().hostnameVerifier());
    Log.i(Retrofit.TAG, "OkHostnameVerifier.INSTANCE:"+OkHostnameVerifier.INSTANCE);
    if (route.address().hostnameVerifier() != OkHostnameVerifier.INSTANCE) return false;
    if (!supportsUrl(address.url())) return false;

    // 4. Certificate pinning must match the host.
    try {
      address.certificatePinner().check(address.url().host(), handshake().peerCertificates());
    } catch (SSLPeerUnverifiedException e) {
      return false;
    }

    return true; // The caller's address can be carried by this connection.
  }

  public boolean supportsUrl(HttpUrl url) {
    if (url.port() != route.address().url().port()) {
      return false; // Port mismatch.
    }

    if (!url.host().equals(route.address().url().host())) {
      // We have a host mismatch. But if the certificate matches, we're still good.
      return handshake != null && OkHostnameVerifier.INSTANCE.verify(
          url.host(), (X509Certificate) handshake.peerCertificates().get(0));
    }

    return true; // Success. The URL is supported.
  }

  public HttpCodec  newCodec(
      OkHttpClient client, StreamAllocation streamAllocation) throws SocketException {
    if (http2Connection != null) {
      return new Http2Codec(client, streamAllocation, http2Connection);
    } else {
      socket.setSoTimeout(client.readTimeoutMillis());
      source.timeout().timeout(client.readTimeoutMillis(), MILLISECONDS);
      sink.timeout().timeout(client.writeTimeoutMillis(), MILLISECONDS);
      Log.i(Retrofit.TAG, "创建Http1Codec对象使用 Realconnection中属性 socket:"+socket+", source:"+source+", sink:"+sink);
      return new Http1Codec(client, streamAllocation, source, sink);
    }
  }

  public RealWebSocket.Streams newWebSocketStreams(final StreamAllocation streamAllocation) {
    return new RealWebSocket.Streams(true, source, sink) {
      @Override public void close() throws IOException {
        Log.i(Retrofit.TAG, "RealWebSocket close ...");
        streamAllocation.streamFinished(true, streamAllocation.codec());
      }
    };
  }

  @Override public Route route() {
    return route;
  }

  public void cancel() {
    // Close the raw socket so we don't end up doing synchronous I/O.
    closeQuietly(rawSocket);
  }

  @Override public Socket socket() {
    return socket;
  }

  /** Returns true if this connection is ready to host new streams. */
  public boolean isHealthy(boolean doExtensiveChecks) {
    if (socket.isClosed() || socket.isInputShutdown() || socket.isOutputShutdown()) {
      return false;
    }

    if (http2Connection != null) {
      return !http2Connection.isShutdown();
    }

    if (doExtensiveChecks) {
      try {
        int readTimeout = socket.getSoTimeout();
        try {
          socket.setSoTimeout(1);
          if (source.exhausted()) {
            return false; // Stream is exhausted; socket is closed.
          }
          return true;
        } finally {
          socket.setSoTimeout(readTimeout);
        }
      } catch (SocketTimeoutException ignored) {
        // Read timed out; socket is good.
      } catch (IOException e) {
        return false; // Couldn't read; socket is closed.
      }
    }

    return true;
  }

  /** Refuse incoming streams. */
  @Override public void onStream(Http2Stream stream) throws IOException {
    stream.close(ErrorCode.REFUSED_STREAM);
  }

  /** When settings are received, adjust the allocation limit. */
  @Override public void onSettings(Http2Connection connection) {
    synchronized (connectionPool) {
      allocationLimit = connection.maxConcurrentStreams();
    }
  }

  @Override public Handshake handshake() {
    return handshake;
  }

  /**
   * Returns true if this is an HTTP/2 connection. Such connections can be used in multiple HTTP
   * requests simultaneously.
   */
  public boolean isMultiplexed() {
    return http2Connection != null;
  }

  @Override public Protocol protocol() {
    return protocol;
  }

  @Override public String toString() {
    return hashCode() + "  Connection{"
        + route.address().url().host() + ":" + route.address().url().port()
        + ", proxy="
        + route.proxy()
        + " hostAddress="
        + route.socketAddress()
        + " cipherSuite="
        + (handshake != null ? handshake.cipherSuite() : "none")
        + " protocol="
        + protocol
        + '}';
  }


}
