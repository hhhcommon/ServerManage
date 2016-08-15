package com.woting.crawler.scheme.QT.crawler;

import java.util.HashMap;
import java.util.Map;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import com.spiritdata.framework.util.JsonUtils;
import com.woting.crawler.scheme.util.HttpUtils;
import com.woting.crawler.scheme.util.RedisUtils;

public class QTCrawler extends Thread {
	private String num;
	private static String CategoryLink = "http://www.qingting.fm/s/home";
	
	public QTCrawler(String num) {
		this.num = num;
	}
	
	public void getQTCrawler() {
		Elements els = null;
		Document doc;
		Map<String, Object> map = new HashMap<String,Object>();
		try {
			doc = Jsoup.connect(CategoryLink).ignoreContentType(true).timeout(10000).get();
			els = doc.select("div[class=category]");
			if(els!=null&&!els.isEmpty()){
				for (Element el : els) {
					String str = el.select("div[class=title pull-left]").get(0).html();
					if (!str.equals("正在直播")&&!str.equals("主播")) {
						String cateName = str;
						String cateId = el.select("a[class=more pull-left]").get(0).attr("href").replace("/supervcategories/", "");
						Elements es = el.select("li[class=playable]");
						for (Element e : es) {
							Map<String, Object> m = (Map<String, Object>) JsonUtils.jsonToObj(HttpUtils.getTextByDispose(e.attr("data-play-info")), Map.class);
							String albumId = m.get("parentid")+"";
							if (!albumId.equals("null")) {
								map.put(albumId, cateId+"::"+cateName);
							}
						}
					}
				}
			}
		} catch (Exception e) {e.printStackTrace();}
		RedisUtils.addQTCategory(num, map);
	}
	
	@Override
	public void run() {
		getQTCrawler();
	}
}
