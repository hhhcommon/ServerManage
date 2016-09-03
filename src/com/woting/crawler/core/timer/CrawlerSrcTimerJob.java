package com.woting.crawler.core.timer;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.spiritdata.framework.core.cache.CacheEle;
import com.spiritdata.framework.core.cache.SystemCache;
import com.woting.crawler.CrawlerConstants;
import com.woting.crawler.compare.CrawlerSrcRecord;
import com.woting.crawler.core.etl.control.Etl1Controller;
import com.woting.crawler.core.etl.control.Etl2Controller;
import com.woting.crawler.core.etl.model.Etl1Process;
import com.woting.crawler.core.etl.model.Etl2Process;
import com.woting.crawler.core.scheme.control.SchemeController;
import com.woting.crawler.core.scheme.model.Scheme;
import com.woting.crawler.scheme.utils.RedisUtils;

public class CrawlerSrcTimerJob implements Job {
	Logger logger = LoggerFactory.getLogger(CrawlerSrcTimerJob.class);

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		// 加载抓取方案
		Scheme scheme = new Scheme("");
		String crawlernum = scheme.getSchemenum();
		while (RedisUtils.isOrNoCrawlerFinish(crawlernum)) {
			logger.info("开始判断redis里是否存在当前抓取序号[{}]是否已存在", crawlernum);
			logger.info("抓取序号[{}]已存在", crawlernum);
			int num = Integer.valueOf(crawlernum) + 1;
			crawlernum = num + "";
			logger.info("验证抓取序号[{}]是否存在", crawlernum);
		}
		logger.info("抓取序号[{}]不存在", crawlernum);
		logger.info("开始进行序号为[{}]抓取", crawlernum);
		scheme.setSchemenum(crawlernum);
		SystemCache.setCache(new CacheEle<String>(CrawlerConstants.CRAWLERNUM, "抓取序号", crawlernum));

		CrawlerSrcRecord srcRecord = new CrawlerSrcRecord(crawlernum);
		srcRecord.reloadCrawlerInfo();

		// 开始抓取数据
		SchemeController sc = new SchemeController(scheme);
		sc.runningScheme();

		// 第一次数据转换
		Etl1Process etl1Process = new Etl1Process();
		etl1Process.setEtlnum(scheme.getSchemenum());
		Etl1Controller etl1 = new Etl1Controller(etl1Process);
		etl1.runningScheme();

		// 第二次数据转换
		Etl2Process etl2Process = new Etl2Process();
		etl2Process.setEtlnum(scheme.getSchemenum());
		Etl2Controller etl2 = new Etl2Controller(etl2Process);
		etl2.runningScheme();
	}

}
