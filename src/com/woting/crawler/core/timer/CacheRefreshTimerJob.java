package com.woting.crawler.core.timer;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.woting.crawler.scheme.cacherefresh.CacheRefresh;

public class CacheRefreshTimerJob implements Job  {
	Logger logger = LoggerFactory.getLogger(BCPlayIsValidateTimerJob.class);
	
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		long begTime = System.currentTimeMillis();
		logger.info("开始电台播放地址是否有效检测");
		CacheRefresh cacheRefresh = new CacheRefresh();
		cacheRefresh.begCacheRefresh();
		logger.info("电台播放地址是否有效检测完成,耗时[{}]",System.currentTimeMillis()-begTime);
	}

}
