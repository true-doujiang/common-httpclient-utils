package com.yhh.httpclient.util;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * 这里的API 只用到了 commons-httpclient   legacy版本
 */
public class HttpClient3Util {

    private final static Logger logger = LoggerFactory.getLogger(HttpClient3Util.class);

    private static MultiThreadedHttpConnectionManager httpConnectionManager = new MultiThreadedHttpConnectionManager();
    private static HttpClient client;

    static {
        client = new HttpClient(httpConnectionManager);
        client.getHttpConnectionManager().getParams().setDefaultMaxConnectionsPerHost(32);
        //总最大连接数
        client.getHttpConnectionManager().getParams().setMaxTotalConnections(256);
        //超时时间
        client.getHttpConnectionManager().getParams().setConnectionTimeout(3000);
        client.getHttpConnectionManager().getParams().setSoTimeout(3000);
    }


    public static HttpClient getHttpClient() {
        return client;
    }

    /**
     * 用法：
     * HttpClient3Util hrp = new HttpClient3Util();
     * hrp.doRequest("http://www.163.com",null,null,"utf-8");
     *
     * @param url 请求的资源ＵＲＬ
     * @param postData POST请求时form表单封装的数据 没有时传null
     * @param header request请求时附带的头信息(header) 没有时传null
     * @param encoding response返回的信息编码格式 没有时传null
     * @return response返回的文本数据
     * @throws Exception
     */
    public static String doRequest(String url, Map postData, Map header, String encoding) throws Exception {
        String response = null;
        Header[] headers = initHeader(header);
        if (postData != null) {
            //post
            response = executePost(url, postData, encoding, headers);
        } else {
            //get
            response = executeGet(url, encoding, headers);
        }
        return response;
    }

    /**
     *
     * @param url
     * @param encoding
     * @param headers
     * @return
     * @throws Exception
     */
    private static String executeGet(String url, String encoding, Header[] headers) throws Exception{
        String response = "";
        GetMethod getRequest = new GetMethod(url.trim());
        if (headers != null) {
            for (int i=0; i<headers.length; i++) {
                getRequest.setRequestHeader(headers[i]);
            }
        }
        logger.debug("ExecuteGet url = {} , headers = {}", url, headers);
        try {
            response = executeMethod(getRequest, encoding);
        } catch (Exception e) {
            logger.warn("ExecuteGet Error url = {} ", url, e);
            throw e;
        } finally {
            getRequest.releaseConnection();
        }
        return response;
    }

    /**
     *
     * @param url
     * @param postData
     * @param encoding
     * @param headers
     * @return
     */
    private static String executePost(String url, Map postData, String encoding, Header[] headers) throws Exception {
        String response = null;
        PostMethod postRequest = new PostMethod(url.trim());
        if (headers != null) {
            for (int i = 0; i < headers.length; i++) {
                postRequest.setRequestHeader(headers[i]);
            }
        }

        Set entrySet = postData.entrySet();
        int len = entrySet.size();
        NameValuePair[] params = new NameValuePair[len];
        int i = 0;
        for (Iterator it = entrySet.iterator(); it.hasNext(); ) {
            Map.Entry entry = (Map.Entry) it.next();
            params[i++] = new NameValuePair(entry.getKey().toString(), entry.getValue().toString());
        }
        postRequest.setRequestBody(params);
        try {
            logger.debug("ExecutePost url = {} , headers = {}", url, headers);
            response = executeMethod(postRequest, encoding);
        } catch (Exception e) {
            logger.warn("ExecutePost Error url = {} ", url, e);
            throw e;
        } finally {
            postRequest.releaseConnection();
        }

        return response;
    }


    /**
     *
     * @param header
     * @return
     */
    private static Header[] initHeader(Map header) {
        Header[] headers = null;
        if (header != null) {
            Set entrySet = header.entrySet();
            int len = entrySet.size();
            headers = new Header[len];
            int i = 0;
            for (Iterator it = entrySet.iterator(); it.hasNext(); ) {
                Map.Entry entry = (Map.Entry) it.next();
                headers[i++] = new Header(entry.getKey().toString(), entry.getValue().toString());
            }
        }
        return headers;
    }


    /**
     *
     * @param request
     * @param encoding
     * @return
     * @throws Exception
     */
    private static String executeMethod(HttpMethod request, String encoding) throws Exception {
        String response = null;
        InputStream inputStream = null;
        BufferedReader reader = null;

        try {
            Long s = System.currentTimeMillis();
            request.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, encoding);
            //发请求
            getHttpClient().executeMethod(request);
            if (encoding != null) {
                inputStream = request.getResponseBodyAsStream();
                reader = new BufferedReader(new InputStreamReader(inputStream, encoding));
                String tempLine = reader.readLine();
                StringBuilder sb = new StringBuilder();
                String crlf = System.getProperty("line.separator");
                while (tempLine != null) {
                    sb.append(tempLine);
                    sb.append(crlf);
                    tempLine = reader.readLine();
                }
                response = sb.toString();
            } else {
                response = request.getResponseBodyAsString();
            }

            //
            Header locationHeader = request.getResponseHeader("location");
            if (locationHeader != null) {
                //doRequest()
            }
        } catch (HttpException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    logger.error("reader 关闭失败 ", e);
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    logger.error("inputStream 关闭失败 ", e);
                }
            }
        }
        return response;
    }

    public static void main(String[] args) throws Exception {
        String url = "https://www.baidu.com/";
        Map<String, String> param = new HashMap<String, String>();
        param.put("signValue", "12");

        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/json");
        String s = doRequest(url, null, headers, "utf-8");
        System.out.println("结果: " + s);
    }



}
