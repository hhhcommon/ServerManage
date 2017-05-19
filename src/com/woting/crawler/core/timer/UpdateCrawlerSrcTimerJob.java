package com.woting.crawler.core.timer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.woting.crawler.scheme.crawlerdb.qt.QTCrawler;
import com.woting.crawler.scheme.crawlerdb.xmly.XMLYCrawler;

public class UpdateCrawlerSrcTimerJob implements Job {
	Logger logger = LoggerFactory.getLogger(UpdateCrawlerSrcTimerJob.class);

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		// 加载抓取方案
		logger.info("开始新内容数据抓取");
		
		beginCrawler();
		
		logger.info("新内容数据抓取完成");
	}
	
	public boolean beginCrawler() {
		ExecutorService fixedThreadPool = Executors.newFixedThreadPool(2);
		fixedThreadPool.execute(new Runnable() {
			public void run() {
				XMLYCrawler xCrawler = new XMLYCrawler(true);
				xCrawler.beginCrawler();
			}
		});
		fixedThreadPool.execute(new Runnable() {
			public void run() {
				QTCrawler qtCrawler = new QTCrawler(true);
				qtCrawler.beginCrawler();
			}
		});
		fixedThreadPool.shutdown();
		while (true) {
			try {
				Thread.sleep(1000);
			} catch (Exception e) {}
			if (fixedThreadPool.isTerminated()) {
				break;
			}
		}
		return true;
	}

}
