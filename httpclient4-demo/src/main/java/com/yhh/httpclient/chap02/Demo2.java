package com.yhh.httpclient.chap02;

import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class Demo2 {


    public static void main(String[] args) {

    }


    /**
     * 2.8.4 连接池管理器
     * ThreadSafeClientConnManager是一个复杂的实现来管理客户端连接池，它也
     * 可以从多个执行线程中服务连接请求。对每个基本的路由，连接都是池管理的。
     * 对于路由的请求，管理器在池中有可用的持久性连接，将被从池中租赁连接服务，而不是创建一个新的连接。
     * ThreadSafeClientConnManager维护每个基本路由的最大连接限制。每个默认的实现对每个给定路由将会创建不超过两个的并发连接，
     * 而总共也不会超过20个连接。对于很多真实的应用程序，这个限制也证明很大的制约，特别是他们在服务中使用HTTP作为传输协议。
     * 连接限制，也可以使用HTTP参数来进行调整。
     * <p>
     * 这个示例展示了连接池参数是如何来调整的：
     */
    @Test
    public void test1() {
        HttpParams params = new BasicHttpParams();
        // 增加最大连接到200
        ConnManagerParams.setMaxTotalConnections(params, 200);

        // 增加每个路由的默认最大连接到20
        ConnPerRouteBean connPerRoute = new ConnPerRouteBean(20);

        // 对localhost:80增加最大连接到50
        HttpHost localhost = new HttpHost("locahost", 80);
        connPerRoute.setMaxForRoute(new HttpRoute(localhost), 50);
        ConnManagerParams.setMaxConnectionsPerRoute(params, connPerRoute);

        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
        ClientConnectionManager cm = new ThreadSafeClientConnManager(params, schemeRegistry);
        HttpClient httpClient = new DefaultHttpClient(cm, params);
    }

    /**
     * 2.8.5 连接管理器关闭
     * 当一个HttpClient实例不再需要时，而且即将走出使用范围，那么关闭连接管理器来保证由管理器保持活动的所有连接被关闭，
     * 由连接分配的系统资源被释放是很重要的。
     */
    @Test
    public void test2() throws IOException {
        DefaultHttpClient httpclient = new DefaultHttpClient();
        HttpGet httpget = new HttpGet("http://www.baidu.com/");
        HttpResponse response = httpclient.execute(httpget);
        HttpEntity entity = response.getEntity();
        System.out.println(response.getStatusLine());
        if (entity != null) {
            entity.consumeContent();
        }
        httpclient.getConnectionManager().shutdown();
    }


    /**
     * 2.10 多线程执行请求
     * <p>
     * 当配备连接池管理器时，比如ThreadSafeClientConnManager，HttpClient可以同时被用来执行多个请求，使用多线程执行。
     * ThreadSafeClientConnManager将会分配基于它的配置的连接。如果对于给定路由的所有连接都被租出了，那么连接的请求将会阻塞，直到一个连接被释放回连接池。
     * 它可以通过设置'http.conn-manager.timeout'为一个正数来保证连接管理器不会在连接请求执行时无限期的被阻塞。
     * 如果连接请求不能在给定的时间周期内被响应，将会抛出ConnectionPoolTimeoutException异常。
     */
    @Test
    public void test3() throws InterruptedException {
        HttpParams params = new BasicHttpParams();
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        ClientConnectionManager cm = new ThreadSafeClientConnManager(params, schemeRegistry);

        HttpClient httpClient = new DefaultHttpClient(cm, params);
        // 执行GET方法的URI
        String[] urisToGet = {
                "http://www.domain1.com/",
                "http://www.domain2.com/",
                "http://www.domain3.com/",
                "http://www.domain4.com/"
        };

        // 为每个URI创建一个线程
        GetThread[] threads = new GetThread[urisToGet.length];
        for (int i = 0; i < threads.length; i++) {
            HttpGet httpget = new HttpGet(urisToGet[i]);
            threads[i] = new GetThread(httpClient, httpget);
        }
        // 开始执行线程
        for (int j = 0; j < threads.length; j++) {
            threads[j].start();
        }
        // 合并线程
        for (int j = 0; j < threads.length; j++) {
            threads[j].join();
        }
    }


    /**
     * 2.12 连接保持活动的策略
     */
    @Test
    public void test4() {
        DefaultHttpClient httpclient = new DefaultHttpClient();

        httpclient.setKeepAliveStrategy(new ConnectionKeepAliveStrategy() {

            public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
                // 兑现'keep-alive'头部信息
                HeaderElementIterator it = new BasicHeaderElementIterator(response.headerIterator(HTTP.CONN_KEEP_ALIVE));
                while (it.hasNext()) {
                    HeaderElement he = it.nextElement();
                    String param = he.getName();
                    String value = he.getValue();
                    if (value != null && param.equalsIgnoreCase("timeout")) {
                        try {
                            return Long.parseLong(value) * 1000;
                        } catch (NumberFormatException ignore) {
                            ignore.printStackTrace();
                        }
                    }
                }

                HttpHost target = (HttpHost) context.getAttribute(ExecutionContext.HTTP_TARGET_HOST);
                if ("www.naughty-server.com".equalsIgnoreCase(target.getHostName())) {
                    // 只保持活动5秒
                    return 5 * 1000;
                } else {
                    // 否则保持活动30秒
                    return 30 * 1000;
                }
            }
        });
    }


    @Test
    public void test5() {

    }


    @Test
    public void test6() {

    }


    /**
     *
     */
    static class GetThread extends Thread {

        private final HttpClient httpClient;
        private final HttpContext context;
        private final HttpGet httpget;

        public GetThread(HttpClient httpClient, HttpGet httpget) {
            this.httpClient = httpClient;
            this.context = new BasicHttpContext();
            this.httpget = httpget;
        }

        @Override
        public void run() {
            try {
                HttpResponse response = this.httpClient.execute(this.httpget, this.context);
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    // 对实体做些有用的事情...
                    // 保证连接能释放回管理器
                    entity.consumeContent();
                }
            } catch (Exception ex) {
                this.httpget.abort();
            }
        }
    }

    /**
     *
     */
    public static class IdleConnectionMonitorThread extends Thread {

        private final ClientConnectionManager connMgr;
        private volatile boolean shutdown;

        public IdleConnectionMonitorThread(ClientConnectionManager connMgr) {
            super();
            this.connMgr = connMgr;
        }

        @Override
        public void run() {
            try {
                while (!shutdown) {
                    synchronized (this) {
                        wait(5000);
                        // 关闭过期连接
                        connMgr.closeExpiredConnections();
                        // 可选地，关闭空闲超过30秒的连接
                        connMgr.closeIdleConnections(30, TimeUnit.SECONDS);
                    }
                }
            } catch (InterruptedException ex) {
                ex.printStackTrace();
                // 终止
            }
        }

        public void shutdown() {
            shutdown = true;
            synchronized (this) {
                notifyAll();
            }
        }
    }

}
