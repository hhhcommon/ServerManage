package com.woting.crawler.scheme.util;

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

	public static String decodeUnicode(String theString) {
		char aChar;
		int len = theString.length();
		StringBuffer outBuffer = new StringBuffer(len);
		for (int x = 0; x < len;) {
			aChar = theString.charAt(x++);
			if (aChar == '\\') {
				aChar = theString.charAt(x++);
				if (aChar == 'u') {
					int value = 0;
					for (int i = 0; i < 4; i++) {
						aChar = theString.charAt(x++);
						switch (aChar) {
						case '0':
						case '1':
						case '2':
						case '3':
						case '4':
						case '5':
						case '6':
						case '7':
						case '8':
						case '9':
							value = (value << 4) + aChar - '0';
							break;
						case 'a':
						case 'b':
						case 'c':
						case 'd':
						case 'e':
						case 'f':
							value = (value << 4) + 10 + aChar - 'a';
							break;
						case 'A':
						case 'B':
						case 'C':
						case 'D':
						case 'E':
						case 'F':
							value = (value << 4) + 10 + aChar - 'A';
							break;
						default:
							throw new IllegalArgumentException("Malformed   \\uxxxx   encoding.");
						}
					}
					outBuffer.append((char) value);
				} else {
					if (aChar == 't')
						aChar = '\t';
					else if (aChar == 'r')
						aChar = '\r';
					else if (aChar == 'n')
						aChar = '\n';
					else if (aChar == 'f')
						aChar = '\f';
					outBuffer.append(aChar);
				}
			} else
				outBuffer.append(aChar);
		}
		return outBuffer.toString();
	}
}