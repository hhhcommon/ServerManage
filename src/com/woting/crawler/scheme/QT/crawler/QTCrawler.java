package com.woting.crawler.scheme.QT.crawler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.woting.crawler.core.scheme.model.Scheme;
import com.woting.crawler.scheme.util.RedisUtils;

public class QTCrawler extends Thread {
	private static String CategoryLink = "http://www.qingting.fm/#/home";
	private Scheme scheme;
	
	public QTCrawler(Scheme scheme) {
		this.scheme = scheme;
	}
	
	public void parseCategory(){
		Elements els = null;
		Element el = null;
		List<Map<String, Object>> catelist = new ArrayList<Map<String,Object>>();
		List<Map<String, Object>> qtcatelist = new ArrayList<Map<String,Object>>();
		try {
			Document doc = Jsoup.connect(CategoryLink).ignoreContentType(true).get();
			els = doc.select("div[data-category=507]");
			if(els!=null&&!els.isEmpty()){
				el = els.get(0);
				els = el.select("a[data-switch-url]");
				for (Element element : els) {
					Map<String, Object> m = new HashMap<String,Object>();
					m.put("visitUrl", "http://www.qingting.fm/s"+element.attr("href"));
					m.put("cid", element.attr("href").replace("/supervcategories/", ""));
					m.put("cname", element.select("h5").get(0).html());
					catelist.add(m);
				}
				qtcatelist.addAll(catelist);
				for (Map<String, Object> m1 : catelist) {
					try {
						doc = Jsoup.connect(m1.get("visitUrl")+"").ignoreContentType(true).get();
						els = doc.select("div[class=right-bar clearfix]");
						if(els!=null&&!els.isEmpty()){
							for (Element element : els) {
								Map<String, Object> m = new HashMap<String,Object>();
								m.put("tname", element.select("div[class=title pull-left]").get(0).html());
								m.put("tid", element.select("a[data-switch-url]").get(0).attr("href").replace("/vcategories/", "").replace("/", "_"));
								m.put("pid", m1.get("cid"));
								m.put("pname", m1.get("cname"));
								m.put("visitUrl", "http://www.qingting.fm/s"+element.select("a[data-switch-url]").get(0).attr("href"));
								qtcatelist.add(m);
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
						continue;
					}
				}
			}
			
			RedisUtils.addQTCategorys(scheme.getSchemenum(), qtcatelist);
		} catch (Exception e) {e.printStackTrace();}
	}
	
	@Override
	public void run() {
		parseCategory();
	}
}
