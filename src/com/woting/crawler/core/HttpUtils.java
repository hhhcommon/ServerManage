package com.woting.crawler.core;

import java.util.Map;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import com.spiritdata.framework.util.JsonUtils;

public abstract class HttpUtils {

    public static Map<String, Object> getJsonMapFromURL(String url) {
        HttpClientBuilder clientBuilder=HttpClientBuilder.create();
        CloseableHttpClient httpClient=clientBuilder.build();
        HttpGet httpget=new HttpGet(url);
        try {
            HttpResponse res=httpClient.execute(httpget);
            if (res.getStatusLine().getStatusCode()==HttpStatus.SC_OK) {
                HttpEntity entity=res.getEntity();
                if (entity!=null) {
                    String jsonStr=EntityUtils.toString(entity, "UTF-8");
                    System.out.println("####"+jsonStr);
                    return (Map<String, Object>)JsonUtils.jsonToObj(jsonStr, Map.class);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            httpget.abort();
        }
        return null;
    }
}