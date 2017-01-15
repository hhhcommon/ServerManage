package com.woting.crawler.core.timer;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.woting.crawler.scheme.share.WXConfig;

public class ShareTimerJob implements Job {
	Logger logger = LoggerFactory.getLogger(ShareTimerJob.class);

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		logger.info("开始进行分享临时票据数据更新");
		new WXConfig().updateWXJsapiTicket();
		logger.info("分享临时票据数据更新成功!");
	}

}
