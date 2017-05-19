package com.woting.crawler.core.timer;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CrawlerSrcTimerJob implements Job {
	Logger logger = LoggerFactory.getLogger(CrawlerSrcTimerJob.class);

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		
	}

}
