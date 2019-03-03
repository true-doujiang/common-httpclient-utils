package com.yhh.httpclient.chap01;

import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentProducer;
import org.apache.http.entity.EntityTemplate;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParamBean;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

import javax.net.ssl.SSLHandshakeException;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Demo3 {


    public static void main(String[] args) {

    }

    /**
     * HttpClient提供一些类，它们可以用于生成通过HTTP连接获得内容的有效输出流。
     * 为了封闭实体从HTTP请求中获得的输出内容，那些类的实例可以和封闭如POST和PUT请求的实体相关联。
     * HttpClient为很多公用的数据容器，比如字符串，字节数组，输入流和文件提供了一些类：
     * StringEntity，ByteArrayEntity，InputStreamEntity和FileEntity。
     * <p>
     * 请注意InputStreamEntity是不可重复的，因为它仅仅能从低层数据流中读取一次内容。通常来说，
     * 我们推荐实现一个定制的HttpEntity类，这是自我包含式的，用来代替使用通用的InputStreamEntity。FileEntity也是一个很好的起点。
     */
    @Test
    public void test1() throws IOException {
        File file = new File("E://somefile.txt");
        FileEntity fileEntity = new FileEntity(file, "text/plain; charset=\"UTF-8\"");

        HttpPost httppost = new HttpPost("https://www.baidu.com");
        HttpClient httpclient = new DefaultHttpClient();
        HttpResponse response = httpclient.execute(httppost);

        httppost.setEntity(fileEntity);

        System.out.println(response.getStatusLine());

    }


    @Test
    public void test2() throws IOException {

        ContentProducer cp = new ContentProducer() {

            public void writeTo(OutputStream outstream) throws IOException {
                Writer writer = new OutputStreamWriter(outstream, "UTF-8");
                writer.write("<response>");
                writer.write(" <content>");
                writer.write(" important stuff");
                writer.write(" </content>");
                writer.write("</response>");
                writer.flush();
            }
        };

        HttpEntity entity = new EntityTemplate(cp);

        HttpPost httppost = new HttpPost("http://localhost/handler.do");
        httppost.setEntity(entity);

    }

    /**
     * 许多应用程序需要频繁模拟提交一个HTML表单的过程，比如，为了来记录一个Web应用程序或提交输出数据。
     * HttpClient提供了特殊的实体类UrlEncodedFormEntity来这个满足过程。
     */
    @Test
    public void test3() throws IOException {
        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
        formparams.add(new BasicNameValuePair("param1", "value1"));
        formparams.add(new BasicNameValuePair("param2", "value2"));

        //UrlEncodedFormEntity实例将会使用URL编码来编码参数，生成如下的内容：
        //param1=value1&param2=value2
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
        System.out.println(entity);

        HttpPost httppost = new HttpPost("http://localhost/handler.do");
        httppost.setEntity(entity);

    }


    @Test
    public void test4() throws IOException {
        StringEntity entity = new StringEntity("important message", "text/plain; charset=\"UTF-8\"");
        //1.1.7.3 内容分块
        entity.setChunked(true);
        HttpPost httppost = new HttpPost("http://localhost/acrtion.do");
        httppost.setEntity(entity);
    }

    /**
     * 控制响应的最简便和最方便的方式是使用ResponseHandler接口。这个放完完全减轻了用户关于连接管理的担心。
     * 当使用ResponseHandler时，HttpClient将会自动关注
     * 并保证释放连接到连接管理器中去，而不管请求执行是否成功或引发了异常。
     */
    @Test
    public void test5() throws IOException {
        HttpClient httpclient = new DefaultHttpClient();
        HttpGet httpget = new HttpGet("https://www.baidu.com");

        //
        ResponseHandler<byte[]> handler = new ResponseHandler<byte[]>() {

            public byte[] handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    return EntityUtils.toByteArray(entity);
                } else {
                    return null;
                }
            }
        };

        byte[] response = httpclient.execute(httpget, handler);
        System.out.println(new String(response));
    }

    /**
     * 1.2 HTTP执行的环境
     * 最初，HTTP是被设计成无状态的，面向请求-响应的协议。
     * 然而，真实的应用程序经常需要通过一些逻辑相关的请求-响应交换来持久状态信息。
     * 为了开启应用程序来维持一个过程状态，HttpClient允许HTTP请求在一个特定的执行环境中来执行，简称为HTTP上下文。
     * <p>
     * <p>
     * 比如，为了决定最终的重定向目标，在请求执行之后，可以检查http.target_host属性的值
     */
    @Test
    public void test6() throws IOException {

        HttpContext localContext = new BasicHttpContext();

        HttpGet httpget = new HttpGet("http://www.google.com/");

        DefaultHttpClient httpclient = new DefaultHttpClient();
        HttpResponse response = httpclient.execute(httpget, localContext);

        HttpHost target = (HttpHost) localContext.getAttribute(ExecutionContext.HTTP_TARGET_HOST);
        System.out.println("Final target: " + target);

        HttpEntity entity = response.getEntity();
        if (entity != null) {
            entity.consumeContent();
        }
    }

    /**
     * 1.3.4 请求重试处理
     */
    @Test
    public void test7() throws IOException {
        //为了开启自定义异常恢复机制，应该提供一个HttpRequestRetryHandler接口的实现。

        HttpRequestRetryHandler myRetryHandler = new HttpRequestRetryHandler() {

            public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
                if (executionCount >= 5) {
                    // 如果超过最大重试次数，那么就不要继续了
                    return false;
                }
                if (exception instanceof NoHttpResponseException) {
                    // 如果服务器丢掉了连接，那么就重试
                    return true;
                }
                if (exception instanceof SSLHandshakeException) {
                    // 不要重试SSL握手异常
                    return false;
                }

                HttpRequest request = (HttpRequest) context.getAttribute(ExecutionContext.HTTP_REQUEST);
                boolean idempotent = !(request instanceof HttpEntityEnclosingRequest);
                if (idempotent) {
                    // 如果请求被认为是幂等的，那么就重试
                    return true;
                }

                return false;
            }
        };

        DefaultHttpClient httpclient = new DefaultHttpClient();
        httpclient.setHttpRequestRetryHandler(myRetryHandler);


    }

    /**
     * 1.5 HTTP协议拦截器
     */
    @Test
    public void test8() throws IOException {
        // 这个示例给出了本地内容在连续的请求中怎么被用于持久一个处理状态的：
        DefaultHttpClient httpclient = new DefaultHttpClient();

        HttpContext localContext = new BasicHttpContext();
        AtomicInteger count = new AtomicInteger(1);
        localContext.setAttribute("count", count);

        httpclient.addRequestInterceptor(new HttpRequestInterceptor() {
            public void process(final HttpRequest request, final HttpContext context) throws HttpException, IOException {
                AtomicInteger count = (AtomicInteger) context.getAttribute("count");
                System.out.println(count);
                request.addHeader("Count", Integer.toString(count.getAndIncrement()));
            }
        });

        HttpGet httpget = new HttpGet("https://www.baidu.com");

        for (int i = 0; i < 10; i++) {
            HttpResponse response = httpclient.execute(httpget, localContext);

            HttpEntity entity = response.getEntity();
            if (entity != null) {
                entity.consumeContent();
            }
        }
    }

    /**
     * 1.6 HTTP参数
     * HttpParams接口代表了定义组件运行时行为的一个不变的值的集合。
     * 很多情况下，HttpParams和HttpContext相似。二者之间的主要区别是它们在运行时使用的不同。
     * 这两个接口表示了对象的集合，它们被视作为访问对象值的键的Map，但是服务于不同的目的：
     *  HttpParams旨在包含简单对象：整型，浮点型，字符串，集合，还有运行时不变的对象。
     *  HttpParams希望被用在“一次写入-多处准备”模式下。
     * HttpContext旨在包含很可能在HTTP报文处理这一过程中发生改变的复杂对象
     *  HttpParams的目标是定义其它组件的行为。通常每一个复杂的组件都有它自己的HttpParams对象。
     * HttpContext的目标是来表示一个HTTP处理的执行状态。通常相同的执行上下文在很多合作的对象中共享。
     *
     * 参数层次:
     * 在HTTP请求执行过程中，HttpRequest对象的HttpParams是和用于执行请求的HttpClient实例的HttpParams联系在一起的。
     * 这使得设置在HTTP请求级别的参数优先于设置在HTTP客户端级别的HttpParams。
     * 推荐的做法是设置普通参数对所有的在HTTP客户端级别的HTTP请求共享，而且可以选择性重写具体在HTTP请求级别的参数。
     */
    @Test
    public void test9() throws IOException {
        DefaultHttpClient httpclient = new DefaultHttpClient();
        httpclient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_0);
        httpclient.getParams().setParameter(CoreProtocolPNames.HTTP_CONTENT_CHARSET, "UTF-8");

        HttpGet httpget = new HttpGet("http://www.baidu.com/");
        httpget.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
        httpget.getParams().setParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, Boolean.FALSE);

        httpclient.addRequestInterceptor(new HttpRequestInterceptor() {
            public void process(final HttpRequest request, final HttpContext context) throws HttpException, IOException {
                System.out.println(request.getParams().getParameter(CoreProtocolPNames.PROTOCOL_VERSION));
                System.out.println(request.getParams().getParameter(CoreProtocolPNames.HTTP_CONTENT_CHARSET));
                System.out.println(request.getParams().getParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE));
                System.out.println(request.getParams().getParameter(CoreProtocolPNames.STRICT_TRANSFER_ENCODING));
            }
        });

        CloseableHttpResponse response = httpclient.execute(httpget);
        System.out.println(response.getStatusLine());
    }


    /**
     1.6.2 HTTP参数bean
     HttpParams接口允许在处理组件的配置上很大的灵活性。很重要的是，新的参数可以被引入而不会影响老版本的二进制兼容性。
     然而，和常规的Java bean相比，HttpParams也有一个缺点：HttpParams不能使用DI框架来组合。
     为了缓解这个限制，HttpClient包含了一些bean类，它们可以用来按顺序使用标准的Java eban惯例初始化HttpParams对象。

     */
    @Test
    public void test10() throws IOException {
        HttpParams params = new BasicHttpParams();
        HttpProtocolParamBean paramsBean = new HttpProtocolParamBean(params);
        paramsBean.setVersion(HttpVersion.HTTP_1_1);
        paramsBean.setContentCharset("UTF-8");
        paramsBean.setUseExpectContinue(true);

        System.out.println(params.getParameter(CoreProtocolPNames.PROTOCOL_VERSION));
        System.out.println(params.getParameter(CoreProtocolPNames.HTTP_CONTENT_CHARSET));
        System.out.println(params.getParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE));
        System.out.println(params.getParameter(CoreProtocolPNames.USER_AGENT));
    }
}
