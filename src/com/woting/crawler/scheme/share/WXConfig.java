package com.woting.crawler.scheme.share;

import java.util.Map;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;

import com.spiritdata.framework.ext.spring.redis.RedisOperService;
import com.spiritdata.framework.util.JsonUtils;
import com.woting.crawler.core.share.model.Share;
import com.woting.crawler.ext.SpringShell;

public class WXConfig {
	
	@SuppressWarnings("unchecked")
	public void updateWXJsapiTicket() {
		Share share = new Share();
		try {
			Document doc = Jsoup.connect("https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid="+share.getWxAppId()+"&secret="+share.getWxAppSecret()).ignoreContentType(true).timeout(10000).get();
			if(doc!=null) {
				String jsonstr = doc.body().html();
				if (jsonstr.length()>10) {
					Map<String, Object> ret1 = (Map<String, Object>) JsonUtils.jsonToObj(jsonstr, Map.class);
					if (ret1.containsKey("access_token") && ret1.containsKey("expires_in")) {
						String access_token = ret1.get("access_token")+"";
						doc = Jsoup.connect("https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token="+access_token+"&type=jsapi").ignoreContentType(true).timeout(10000).get();
						if (doc!=null) {
							jsonstr = doc.body().html();
							if (jsonstr.length()>10) {
								Map<String, Object> ret2 = (Map<String, Object>) JsonUtils.jsonToObj(jsonstr, Map.class);
								if (ret2.containsKey("ticket")) {
									String jsapi_ticket = ret2.get("ticket")+"";
									JedisConnectionFactory conn = (JedisConnectionFactory) SpringShell.getBean("connectionFactorySearch");
									RedisOperService ros = new RedisOperService(conn,share.getRedisDB());
									ros.set("WX_JSAPI_TICKET", jsapi_ticket, 7200*1000);
									ros.set("WX_TICKET_CTIME", System.currentTimeMillis()+"", 7200*1000);
									ros.close();
									conn.destroy();
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
