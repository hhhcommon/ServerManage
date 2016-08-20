package com.woting.crawler.scheme.utils;

import java.util.Map;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import com.spiritdata.framework.util.JsonUtils;
import com.spiritdata.framework.util.StringUtils;

public abstract class HttpUtils {
	
	public static String getTextByDispose(String str){
		str = StringEscapeUtils.unescapeHtml4(str);
		str = ascii2native(str);
		return str;
	}

	public static Map<String, Object> getJsonMapFromURL(String url) {
		HttpClientBuilder clientBuilder = HttpClientBuilder.create();
		CloseableHttpClient httpClient = clientBuilder.build();
		HttpGet httpget = new HttpGet(url);
		try {
			HttpResponse res = httpClient.execute(httpget);
			if (res.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				HttpEntity entity = res.getEntity();
				if (entity != null) {
					String jsonStr = EntityUtils.toString(entity, "UTF-8");
					return (Map<String, Object>) JsonUtils.jsonToObj(jsonStr, Map.class);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			httpget.abort();
		}
		return null;
	}

	/**
	 * 清除搜索到页面文本里标签
	 * 
	 * @param str
	 * @return
	 */
	public static String cleanTag(String str) {
		if (StringUtils.isNullOrEmptyOrSpace(str))
			return "";
		while (true) {
			int tagbegin = str.indexOf("<");
			int tagend = str.indexOf(">", tagbegin);
			if (tagbegin != -1 && tagend != -1) {
				String constr = str.substring(tagbegin, tagend + 1);
				str = str.replace(constr, "");
			} else {
				return StringEscapeUtils.unescapeHtml4(str.replace("\n", "").replace("\r", "").trim());
			}
		}
	}

	public static String ascii2native ( String asciicode )
    {
        String[] asciis = asciicode.split ("\\\\u");
        String nativeValue = asciis[0];
        try{
            for (int i=1;i<asciis.length;i++)
            {
                String code = asciis[i];
                nativeValue += (char)Integer.parseInt(code.substring(0, 4), 16);
                if (code.length() > 4){
                	nativeValue += code.substring(4,code.length());
                }
            }
        }
        catch(NumberFormatException e){return asciicode;}
        return nativeValue;
    }
}