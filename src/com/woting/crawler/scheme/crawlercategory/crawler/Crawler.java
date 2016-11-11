package com.woting.crawler.scheme.crawlercategory.crawler;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.woting.crawler.scheme.crawlercategory.QT.QTCrawler;
import com.woting.crawler.scheme.crawlercategory.XMLY.XMLYCrawler;

public class Crawler {
	Logger logger = LoggerFactory.getLogger(Crawler.class);
	
	public Map<String, Object> beginCrawlerCagtegory() {
		logger.info("开始分类抓取");
		long begtime = System.currentTimeMillis();
		logger.info("分类抓取启动");
		startCrawlerCategory();
		Map<String, Object> map = new HashMap<String,Object>();
		map.put("duration", System.currentTimeMillis()-begtime);
		logger.info("分类抓取完成");
		return map;
	}
	
	private void startCrawlerCategory() {
		new QTCrawler().start();
		new XMLYCrawler().start();
//		new KLCrawler().start();
	}
}
