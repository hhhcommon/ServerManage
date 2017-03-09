package com.woting.crawler.scheme.utils;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.spiritdata.framework.util.JsonUtils;
import com.spiritdata.framework.util.StringUtils;

public abstract class HttpUtils {
	
	public static String getTextByDispose(String str){
		str = StringEscapeUtils.unescapeHtml4(str);
		str = ascii2native(str);
		return str;
	}
	
	public static Document makeXMLYJsoup(String url) {
		String data = System.currentTimeMillis()/1000+"";
		Document doc = null;
		try {
			doc = Jsoup.connect(url).header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.102 Safari/537.36")
		.header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
		.header("Accept-Encoding", "gzip, deflate, sdch")
		.header("Cookie", "Hm_lvt_4a7d8ec50cfd6af753c4f8aee3425070="+data+"; Hm_lpvt_4a7d8ec50cfd6af753c4f8aee3425070="+data+"; _ga=GA1.2.2074075166."+data)
		.header("Host", "www.ximalaya.com")
		.header("Connection", "keep-alive")
		.header("X-Requested-With", "XMLHttpRequest")
		.header("Referer", "http://www.ximalaya.com/explore/").ignoreContentType(true).timeout(10000).get();
		} catch (Exception e) {
			e.getMessage();
		}
		return doc;
	}

	@SuppressWarnings("unchecked")
	public static Map<String, Object> getJsonMapFromURL(String url) {
		try {
			Document doc = Jsoup.connect(url).timeout(10000)
					.header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.102 Safari/537.36")
					.header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
					.header("Accept-Encoding", "gzip, deflate, sdch")
					.header("Cookie", "Hm_lvt_4a7d8ec50cfd6af753c4f8aee3425070=1481942163; Hm_lpvt_4a7d8ec50cfd6af753c4f8aee3425070=1481942193; _ga=GA1.2.2074075166.1481942163")
					.header("Host", "www.ximalaya.com")
					.header("Connection", "keep-alive")
					.header("X-Requested-With", "XMLHttpRequest")
					.header("Referer", "http://www.ximalaya.com/explore/")
					.ignoreContentType(true).get();
			String jsonstr = doc.body().html();
			if (!StringUtils.isNullOrEmptyOrSpace(jsonstr) && jsonstr.length()>10) {
				return (Map<String, Object>) JsonUtils.jsonToObj(jsonstr, Map.class);
			} else {
				return getJsonMapFromURL(url);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
//		HttpClientBuilder clientBuilder = HttpClientBuilder.create();
//		CloseableHttpClient httpClient = clientBuilder.build();
//		HttpGet httpget = new HttpGet(url);
//		System.out.println(url);
//		try {
//			HttpResponse res = httpClient.execute(httpget);
//			if (res.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
//				HttpEntity entity = res.getEntity();
//				if (entity != null) {
//					String jsonStr = EntityUtils.toString(entity, "UTF-8");
//					System.out.println(jsonStr);
//					return (Map<String, Object>) JsonUtils.jsonToObj(jsonStr, Map.class);
//				}
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		} finally {
//			httpget.abort();
//		}
//		return null;
	}
	
//	public static String HttpClient(String url) {
//		HttpClientBuilder clientBuilder = HttpClientBuilder.create();
//		CloseableHttpClient httpClient = clientBuilder.build();
//		HttpGet httpget = new HttpGet(url);
//		String jsonStr = null;
//		try {
//			HttpResponse res = httpClient.execute(httpget);
//			if (res.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
//				HttpEntity entity = res.getEntity();
//				if (entity != null) {
//					jsonStr = EntityUtils.toString(entity, "UTF-8");
//					return jsonStr;
//				}
//			}
//		} catch (Exception e) {
//			e.getMessage();
//		} finally {
//			httpget.abort();
//		}
//		return null;
//	}
	
	/** 
     * 发送 get请求 
     */  
    public static String HttpClient(String url) {  
    	String data = System.currentTimeMillis()/1000+"";
        CloseableHttpClient httpclient = HttpClients.createDefault();  
        try {  
            // 创建httpget.
            HttpGet httpget = new HttpGet(url);  
            httpget.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.102 Safari/537.36");
            httpget.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
            httpget.addHeader("Accept-Encoding", "gzip, deflate, sdch");
            httpget.addHeader("Cookie", "Hm_lvt_4a7d8ec50cfd6af753c4f8aee3425070="+data+"; Hm_lpvt_4a7d8ec50cfd6af753c4f8aee3425070="+data+"; _ga=GA1.2.2074075166."+data);
            httpget.addHeader("Host", "www.ximalaya.com");
            httpget.addHeader("Connection", "keep-alive");
            httpget.addHeader("X-Requested-With", "XMLHttpRequest");
            httpget.addHeader("Referer", "http://www.ximalaya.com/explore/");
            // 执行get请求
            CloseableHttpResponse response = httpclient.execute(httpget);  
            try {
                // 获取响应实体    
                HttpEntity entity = response.getEntity();  
                if (entity != null) {  
                    String jsonStr =  EntityUtils.toString(entity);
                    return jsonStr;
                }
            } finally {
                response.close();  
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 关闭连接,释放资源    
            try {
                httpclient.close();  
            } catch (IOException e) {  
                e.printStackTrace();  
            }  
        }  
        return null;
    }  
	
	public static Document getJsonStrForUrl(String url) {
		try {
			Document doc = Jsoup.connect(url)
					.header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.102 Safari/537.36")
					.header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
					.header("Accept-Encoding", "gzip, deflate, sdch")
					.header("Cookie", "Hm_lvt_4a7d8ec50cfd6af753c4f8aee3425070=1481942163; Hm_lpvt_4a7d8ec50cfd6af753c4f8aee3425070=1481942193; _ga=GA1.2.2074075166.1481942163")
					.header("Host", "www.ximalaya.com")
					.header("Connection", "keep-alive")
					.header("X-Requested-With", "XMLHttpRequest")
					.header("Referer", "http://www.ximalaya.com/explore/")
					.ignoreContentType(true).get();
			return doc;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
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