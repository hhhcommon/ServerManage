package com.woting.crawler.scheme.XMLY.crawler;

import java.util.HashMap;
import java.util.Map;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import com.woting.crawler.scheme.util.RedisUtils;

public class XMLYCrawler extends Thread {
	private String num;
	private static String CategoryLink = "http://www.ximalaya.com/dq/all/";

	public XMLYCrawler(String num) {
		this.num = num;
	}

	public void getXMLYCategory() {
		Document doc;
		try {
			Map<String, Object> catemap = new HashMap<String, Object>();
			doc = Jsoup.connect(CategoryLink).timeout(10000).ignoreContentType(true).get();
			Elements els = doc.select("li[cid]");
			if (els != null && !els.isEmpty()) {
				for (Element el : els) {
					catemap.put(el.select("a").get(0).html(), el.attr("cid"));
				}
				if (catemap != null) {
					RedisUtils.addXMLYCategory(num, catemap);
				}
			}
		} catch (Exception e) {e.printStackTrace();}
	}
	
	@Override
	public void run() {
		getXMLYCategory();
	}
}
