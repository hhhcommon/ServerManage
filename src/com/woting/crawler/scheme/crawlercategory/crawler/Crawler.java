package com.woting.crawler.scheme.crawlercategory.crawler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.woting.crawler.scheme.crawlercategory.QT.QTCrawler;
import com.woting.crawler.scheme.crawlercategory.TB.TBCrawler;
import com.woting.crawler.scheme.crawlercategory.XMLY.XMLYCrawler;

public class Crawler {
	Logger logger = LoggerFactory.getLogger(Crawler.class);
	
	public void beginCrawlerCagtegory() {
		logger.info("开始分类抓取");
		logger.info("分类抓取启动");
		startCrawlerCategory();
		logger.info("分类抓取完成");
	}
	
	private void startCrawlerCategory() {
		ExecutorService fixedThreadPool = Executors.newFixedThreadPool(3);
		fixedThreadPool.execute(new Runnable() {
			public void run() {
				new QTCrawler().run();
			}
		});
		fixedThreadPool.execute(new Runnable() {
			public void run() {
				new XMLYCrawler().run();
			}
		});
		fixedThreadPool.execute(new Runnable() {
			public void run() {
				new TBCrawler().run();
			}
		});
		fixedThreadPool.shutdown();
		while (true) {
			try {Thread.sleep(1000);} catch (Exception e) {}
			if (fixedThreadPool.isTerminated()) {
				break;
			}
		}
	}
}
