package com.woting.crawler.scheme.crawlersrc.XMLY.crawler;

import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.woting.crawler.core.scheme.model.Scheme;
import com.woting.crawler.scheme.utils.RedisUtils;

public class XMLYCrawlerRedis extends Thread {
	private static String CategoryLink = "http://www.ximalaya.com/dq/all/";
	private Scheme scheme;
	
	public XMLYCrawlerRedis(Scheme scheme) {
		this.scheme = scheme;
	}
	
	public void getRedisCategory() {
		Elements eles = null;
		Document doc;
		try {
			Map<String, Object> catemap = new HashMap<String, Object>();
			doc = Jsoup.connect(CategoryLink).timeout(10000).ignoreContentType(true).get();
			eles = doc.select("li[cid]");
			if (eles!=null&&!eles.isEmpty()) {
				for (Element ele : eles) {
					String catename = ele.select("a").get(0).html();
					String cateid = ele.attr("cid");
					if(catename.equals("外语"))
						catename = "英语";
					catemap.put(catename, cateid);
				}
				if (catemap != null) {
					RedisUtils.addXMLYCategory(scheme.getSchemenum(), catemap);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		getRedisCategory();
		super.run();
	}
}
