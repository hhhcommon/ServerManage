package com.woting.crawler.core.timer;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.woting.crawler.scheme.redisrefresh.RedisRefresh;

public class RedisRefreshTimerJob implements Job  {
	Logger logger = LoggerFactory.getLogger(ShareTimerJob.class);

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		logger.info("开始进行Redis和Solr数据同步");
		new RedisRefresh().beginRediaRefresh();
		logger.info("Redis和Solr数据同步成功!");
	}
}
