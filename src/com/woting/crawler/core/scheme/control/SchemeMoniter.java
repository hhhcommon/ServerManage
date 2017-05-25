package com.woting.crawler.core.scheme.control;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.spiritdata.framework.core.cache.SystemCache;
import com.spiritdata.framework.ext.spring.redis.RedisOperService;
import com.woting.crawler.CrawlerConstants;
import com.woting.crawler.core.scheme.model.Scheme;
import com.woting.crawler.scheme.utils.RedisUtils;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

public class SchemeMoniter extends Thread {
	Logger logger = LoggerFactory.getLogger(this.getClass());
	private String crawlernum;
	private Scheme scheme;

	public SchemeMoniter(Scheme scheme) {
		this.crawlernum = SystemCache.getCache(CrawlerConstants.CRAWLERNUM).getContent()+"";
		this.scheme = scheme;
	}
	
	@Override
	public void run() {
		logger.info("开始进行数据抓取");
		logger.info("开始辅助信息抓取");
//		new XMLYCrawlerRedis(scheme).start();
		logger.info("开启Crawler4j抓取");
		ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(2);
		scheduledThreadPool.execute(new Runnable() {
			public void run() {
//				new KLSnapShoot().beginSearch();
			}
		});
		scheduledThreadPool.execute(new Runnable() {
			public void run() {
//				new DTSnapShoot().beginSearch();
			}
		});
		scheduledThreadPool.shutdown();
		while (true) {
			try {
				Thread.sleep(10000);
			} catch (Exception e) {}
			if (scheduledThreadPool.isTerminated()) {
				break;
			}
		}
		new Thread(){public void run() {startCrawler4j(); }}.start(); //开启Crawler4j抓取
	}
	
	public void startCrawler4j(){
		try {
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
