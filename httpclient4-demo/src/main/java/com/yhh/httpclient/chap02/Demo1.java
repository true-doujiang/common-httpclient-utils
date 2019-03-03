package com.yhh.httpclient.chap02;

import org.apache.http.*;
import org.apache.http.conn.BasicManagedEntity;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ClientConnectionRequest;
import org.apache.http.conn.ManagedClientConnection;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.ProxySelectorRoutePlanner;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.junit.Test;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.ProxySelector;
import java.net.Socket;
import java.net.URISyntaxException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

public class Demo1 {

    public static void main(String[] args) {

    }


    /**
     * 2.5 套接字工厂
     * HTTP连接内部使用java.net.Socket对象来处理数据在线路上的传输。它们依赖SocketFactory接口来创建，初始化和连接套接字。
     * 这会使得HttpClient的用户可以提供在运行时指定套接字初始化代码的应用程序。
     * PlainSocketFactory是创建和初始化普通的（不加密的）套接字的默认工厂。
     * 创建套接字的过程和连接到主机的过程是不成对的，所以套接字在连接操作封锁时可以被关闭。
     */
    @Test
    public void test1() throws URISyntaxException, IOException {
        PlainSocketFactory plainSocketFactory = PlainSocketFactory.getSocketFactory();
        Socket socket = plainSocketFactory.createSocket();

        HttpParams params = new BasicHttpParams();
        params.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 1000L);

        plainSocketFactory.connectSocket(socket, "locahost", 8080, null, -1, params);
    }

    /**
     * 2.5.1 安全套接字分层
     * LayeredSocketFactory是SocketFactory接口的扩展。分层的套接字工厂可以创建在已经存在的普通套接字之上的分层套接字。
     * 套接字分层主要通过代理来创建安全的套接字。HttpClient附带实现了SSL/TLS分层的SSLSocketFactory。
     * 请注意HttpClient不使用任何自定义加密功能。它完全依赖于标准的Java密码学（JCE）和安全套接字（JSEE）扩展。
     * <p>
     * 2.5.2 SSL/TLS的定制
     * HttpClient使用SSLSocketFactory来创建SSL连接。SSLSocketFactory允许高度定制。
     * 它可以使用javax.net.ssl.SSLContext的实例作为参数，并使用它来创建定制SSL连接。
     * <p>
     * SSLSocketFactory的定制暗示出一定程度SSL/TLS协议概念的熟悉，这个详细的解释超出了本文档的范围。
     * 请参考Java的安全套接字扩展[http://java.sun.com/j2se/1.5.0/docs/guide/security/jsse/JSSERefGuide.html]，
     * 这是javax.net.ssl.SSLContext和相关工具的详细描述。
     */
    @Test
    public void test2() throws Exception {

        TrustManager easyTrustManager = new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                // 哦，这很简单！
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                //哦，这很简单！
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        };

//        SSLContext sslcontext = SSLContext.getInstance("TLS");
//        sslcontext.init(null, new TrustManager[] { easyTrustManager }, null);
//
//        SSLSocketFactory sf = new SSLSocketFactory(sslcontext);
//        SSLSocket socket = (SSLSocket) sf.createSocket();
//        socket.setEnabledCipherSuites(new String[] { "SSL_RSA_WITH_RC4_128_MD5" });
//        HttpParams params = new BasicHttpParams();
//        params.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 1000L);

//        sf.connectSocket(socket, "locahost", 443, null, -1, params);


//        SSLContext tls = SSLContext.getInstance("TLS");
//        SSLSocketFactory sf = new SSLSocketFactory();
//        sf.setHostnameVerifier(SSLSocketFactory.STRICT_HOSTNAME_VERIFIER);
    }

    /**
     * 2.6 协议模式
     * scheme类代表了一个协议模式，比如“http”或“https”同时包含一些协议属性，
     * 比如默认端口，用来为给定协议创建java.net.Socket实例的套接字工厂。
     * SchemeRegistry类用来维持一组Scheme，当去通过请求URI建立连接时，HttpClient可以从中选择：
     */
    @Test
    public void test3() {
//
//        Scheme http = new Scheme("http", PlainSocketFactory.getSocketFactory(), 80);
//        SSLSocketFactory sf = new SSLSocketFactory(SSLContext.getInstance("TLS"));
//
//        sf.setHostnameVerifier(SSLSocketFactory.STRICT_HOSTNAME_VERIFIER);
//        Scheme https = new Scheme("https", sf, 443);
//
//        SchemeRegistry sr = new SchemeRegistry();
//        sr.register(http);
//        sr.register(https);
    }

    /**
     * 2.7 HttpClient代理配置
     * 尽管HttpClient了解复杂的路由模式和代理链，它仅支持简单直接的或开箱的跳式代理连接。
     * 告诉HttpClient通过代理去连接到目标主机的最简单方式是通过设置默认的代理参数：
     */
    @Test
    public void test4() {
        DefaultHttpClient httpclient = new DefaultHttpClient();
        HttpHost proxy = new HttpHost("someproxy", 8080);
        httpclient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);

    }

    // 也可以构建HttpClient使用标准的JRE代理选择器来获得代理信息：
    @Test
    public void test5() throws IOException {
        DefaultHttpClient httpclient = new DefaultHttpClient();

        ProxySelectorRoutePlanner routePlanner = new ProxySelectorRoutePlanner(
                httpclient.getConnectionManager().getSchemeRegistry(),
                ProxySelector.getDefault());

        httpclient.setRoutePlanner(routePlanner);
    }

    //另外一种选择，可以提供一个定制的RoutePlanner实现来获得HTTP路由计算处理上的复杂的控制：
    @Test
    public void test6() throws IOException {
        DefaultHttpClient httpclient = new DefaultHttpClient();

        httpclient.setRoutePlanner(new HttpRoutePlanner() {

            public HttpRoute determineRoute(HttpHost target, HttpRequest request, HttpContext context) throws HttpException {
                return new HttpRoute(target, null, new HttpHost("someproxy", 8080), "https".equalsIgnoreCase(target.getSchemeName()));
            }
        });


    }

    /**
     * 2.8.2 管理连接和连接管理器
     */
    @Test
    public void test7() throws IOException, InterruptedException {
        HttpParams params = new BasicHttpParams();
        Scheme http = new Scheme("http", PlainSocketFactory.getSocketFactory(), 80);

        SchemeRegistry sr = new SchemeRegistry();
        sr.register(http);

        ClientConnectionManager connMrg = new SingleClientConnManager(params, sr);
        // 请求新连接。这可能是一个很长的过程。
        ClientConnectionRequest connRequest = connMrg.requestConnection(new HttpRoute(new HttpHost("localhost", 80)), null);
        // 等待连接10秒
        ManagedClientConnection conn = connRequest.getConnection(10, TimeUnit.SECONDS);

        try {
            // 用连接在做有用的事情。当完成时释放连接。
            conn.releaseConnection();
        } catch (IOException ex) {
            // 在I/O error之上终止连接。
            conn.abortConnection();
            throw ex;
        }
    }

    /**

     */
    @Test
    public void test8() throws IOException, InterruptedException {
        HttpParams params = new BasicHttpParams();
        Scheme http = new Scheme("http", PlainSocketFactory.getSocketFactory(), 80);

        SchemeRegistry sr = new SchemeRegistry();
        sr.register(http);

        ClientConnectionManager connMrg = new SingleClientConnManager(params, sr);
        ClientConnectionRequest connRequest = connMrg.requestConnection(new HttpRoute(new HttpHost("localhost", 80)), null);
        ManagedClientConnection conn = connRequest.getConnection(10, TimeUnit.SECONDS);
        try {
            BasicHttpRequest request = new BasicHttpRequest("GET", "/");
            conn.sendRequestHeader(request);
            HttpResponse response = conn.receiveResponseHeader();
            conn.receiveResponseEntity(response);

            HttpEntity entity = response.getEntity();
            if (entity != null) {
                BasicManagedEntity managedEntity = new BasicManagedEntity(entity, conn, true);
                // 替换实体
                response.setEntity(managedEntity);
            }
            // 使用响应对象做有用的事情。当响应内容被消耗后这个连接将会自动释放。
        } catch (IOException ex) {
            //在I/O error之上终止连接。
            conn.abortConnection();
            throw ex;
        } catch (HttpException e) {
            e.printStackTrace();
        }
    }



}
