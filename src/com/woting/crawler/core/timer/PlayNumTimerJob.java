package com.woting.crawler.core.timer;

import java.util.Map;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.woting.crawler.scheme.crawlerplaynum.crawler.Crawler;

public class PlayNumTimerJob implements Job {
	Logger logger = LoggerFactory.getLogger(PlayNumTimerJob.class);

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		logger.info("开始点击量抓取");
		Crawler crawler = new Crawler();
        Map<String, Object> m = crawler.beginCrawlerPlayCount();
        logger.info("点击量抓取总数 [{}],耗费时间[{}],创建日期[{}]", m.get("num"),m.get("duration"),m.get("cTime"));
	}

}
