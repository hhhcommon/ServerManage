package com.woting.crawler.core.httpclient.service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HttpClientService {
	@Autowired
    private CloseableHttpClient httpClient;
	
	/**
	 * 执行GET请求
	 * 
	 * @param url
	 * @return
	 * @throws IOException
	 * @throws ClientProtocolException
	 */
	public String doGet(String url) throws ClientProtocolException, IOException {
		// 创建http GET请求
		HttpGet httpGet = new HttpGet(url);
		CloseableHttpResponse response = null;
		try {
			// 执行请求
			response = httpClient.execute(httpGet);
			// 判断返回状态是否为200
			if (response.getStatusLine().getStatusCode() == 200) {
				return EntityUtils.toString(response.getEntity(), "UTF-8");
			}
		} finally {
			if (response != null) {
				response.close();
			}
		}
		return null;
	}

	/**
	 * 带有参数的GET请求
	 * 
	 * @param url
	 * @param params
	 * @return
	 * @throws URISyntaxException
	 * @throws IOException
	 * @throws ClientProtocolException
	 */
	public String doGet(String url, Map<String, String> params)
			throws ClientProtocolException, IOException, URISyntaxException {
		URIBuilder uriBuilder = new URIBuilder(url);
		for (String key : params.keySet()) {
			uriBuilder.addParameter(key, params.get(key));
		}
		return this.doGet(uriBuilder.build().toString());
	}
}
