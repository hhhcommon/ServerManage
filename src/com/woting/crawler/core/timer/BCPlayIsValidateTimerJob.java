package com.woting.crawler.core.timer;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.woting.crawler.scheme.isvalidate.PlayUrlValid;

public class BCPlayIsValidateTimerJob implements Job {
	Logger logger = LoggerFactory.getLogger(BCPlayIsValidateTimerJob.class);

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		long begTime = System.currentTimeMillis();
		logger.info("开始电台播放地址是否有效检测");
		PlayUrlValid playUrlValid = new PlayUrlValid();
		playUrlValid.verifyUrlValid();
        logger.info("电台播放地址是否有效检测完成,耗时[{}]",System.currentTimeMillis()-begTime);
	}
}
