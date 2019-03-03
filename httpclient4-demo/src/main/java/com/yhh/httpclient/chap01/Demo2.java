package com.yhh.httpclient.chap01;

import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class Demo2 {

    public static void main(String[] args) throws URISyntaxException {
        HttpGet httpget = new HttpGet("http://www.google.com/search?hl=en&q=httpclient&btnG=Google+Search&aq=f&oq=");
        System.out.println(httpget);

        // URI也可以编程来拼装：
        URI uri = URIUtils.createURI("http", "www.google.com", -1, "/search", "q=httpclient&btnG=Google+Search&aq=f&oq=", null);
        HttpGet httpget2 = new HttpGet(uri);
        System.out.println(httpget2.getURI());

    }

    @Test
    public void test1() throws URISyntaxException {
        //查询字符串也可以从独立的参数中来生成：
        List<NameValuePair> qparams = new ArrayList<NameValuePair>();
        qparams.add(new BasicNameValuePair("q", "httpclient"));
        qparams.add(new BasicNameValuePair("btnG", "Google Search"));
        qparams.add(new BasicNameValuePair("aq", "f"));
        qparams.add(new BasicNameValuePair("oq", null));

        URI uri = URIUtils.createURI("http", "www.google.com", -1, "/search", URLEncodedUtils.format(qparams, "UTF-8"), null);
        HttpGet httpget = new HttpGet(uri);
        System.out.println(httpget.getURI());
    }

    @Test
    public void test2() {
        //响应报文的第一行包含了协议版本，之后是数字状态码和相关联的文本段。
        HttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "OK");

        System.out.println(response.getProtocolVersion());
        System.out.println(response.getStatusLine().getStatusCode());
        System.out.println(response.getStatusLine().getReasonPhrase());

        System.out.println(response.getStatusLine().toString());
    }


    @Test
    public void test3() {
        HttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "OK");
        response.addHeader("Set-Cookie", "c1=a; path=/; domain=localhost");
        response.addHeader("Set-Cookie", "c2=b; path=\"/\", c3=c; domain=\"localhost\"");

        Header h1 = response.getFirstHeader("Set-Cookie");
        System.out.println(h1);
        Header h2 = response.getLastHeader("Set-Cookie");
        System.out.println(h2);

        Header[] hs = response.getHeaders("Set-Cookie");
        System.out.println(hs.length);

        Header[] allHeaders = response.getAllHeaders();

        HeaderIterator it = response.headerIterator("Set-Cookie");
        while (it.hasNext()) {
            //it.next().;
            System.out.println(it.next());
        }

    }


    @Test
    public void test4() {
        //它也提供解析HTTP报文到独立头部信息元素的方法方法。
        HttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "OK");
        response.addHeader("Set-Cookie", "c1=a; path=/; domain=localhost");
        response.addHeader("Set-Cookie", "c2=b; path=\"/\", c3=c; domain=\"localhost\"");

        HeaderElementIterator it = new BasicHeaderElementIterator(response.headerIterator("Set-Cookie"));
        while (it.hasNext()) {
            HeaderElement elem = it.nextElement();
            System.out.println(elem.getName() + " = " + elem.getValue());

            NameValuePair[] params = elem.getParameters();
            for (int i = 0; i < params.length; i++) {
                System.out.println(" " + params[i]);
            }
        }
    }


    @Test
    public void test5() throws IOException {
        StringEntity myEntity = new StringEntity("important message", "UTF-8");

        InputStream in = myEntity.getContent();

        ByteArrayOutputStream buff = new ByteArrayOutputStream(32);
        myEntity.writeTo(buff);

        byte[] bytes = buff.toByteArray();
        System.out.println(new String(bytes));
    }

    @Test
    public void test6() throws IOException {
        StringEntity myEntity = new StringEntity("important message", "UTF-8");

        Header contentType = myEntity.getContentType();
        System.out.println(contentType.getName() + " = " + contentType.getValue());
        System.out.println(myEntity.getContentLength());

        System.out.println(EntityUtils.getContentCharSet(myEntity));
        System.out.println(EntityUtils.toString(myEntity));

        byte[] bytes = EntityUtils.toByteArray(myEntity);
        System.out.println(bytes.length);
        System.out.println(new String(bytes));
    }

    @Test
    public void test7() throws IOException {
        HttpGet httpget = new HttpGet("http://www.java1234.com/vipzy.html");
        HttpClient httpclient = new DefaultHttpClient();
        HttpResponse response = httpclient.execute(httpget);
        HttpEntity entity = response.getEntity();

        if (entity != null) {
            InputStream instream = entity.getContent();
            int byteOne = instream.read();
            int byteTwo = instream.read();

            System.out.println((char) byteOne);
            System.out.println((char) byteTwo);
            // Do not need the rest
            httpget.abort();

            int byteThree = instream.read();
            System.out.println((char) byteThree);
        }
    }

    /**
     1.1.6 消耗实体内容
     推荐消耗实体内容的方式是使用它的HttpEntity#getContent()或HttpEntity#writeTo(OutputStream)方法。
     HttpClient也自带EntityUtils类，这会暴露出一些静态方法，这些方法可以更加容易地从实体中读取内容或信息。
     代替直接读取java.io.InputStream，也可以使用这个类中的方法以字符串/字节数组的形式获取整个内容体。
     然而，EntityUtils的使用是强烈不鼓励的，除非响应实体源自可靠的HTTP服务器和已知的长度限制。
     */
    @Test
    public void test8() throws IOException {
        HttpGet httpget = new HttpGet("http://www.java1234.com/vipzy.html");
        HttpClient httpclient = new DefaultHttpClient();

        HttpResponse response = httpclient.execute(httpget);
        HttpEntity entity = response.getEntity();

        if (entity != null) {
            long len = entity.getContentLength();
            if (len != -1 && len < 2048) {
                System.out.println(EntityUtils.toString(entity));
            } else {
                // Stream content out
                System.out.println(len);
            }
        }

        if (entity != null) {
            entity = new BufferedHttpEntity(entity);
        }

    }

}
