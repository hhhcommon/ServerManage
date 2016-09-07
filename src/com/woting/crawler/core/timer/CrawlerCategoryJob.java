package com.woting.crawler.core.timer;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.woting.crawler.scheme.crawlercategory.crawler.Crawler;

public class CrawlerCategoryJob implements Job {
	
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		Crawler crawler = new Crawler();
		crawler.beginCrawlerCagtegory();
	}
}
