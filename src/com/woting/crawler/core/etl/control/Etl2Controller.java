package com.woting.crawler.core.etl.control;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.woting.crawler.core.etl.model.Etl2Process;
import com.woting.crawler.core.etl.service.Etl2Service;
import com.woting.crawler.ext.SpringShell;
import com.woting.crawler.scheme.utils.RedisUtils;

public class Etl2Controller {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private Etl2Process etl2Process;
	private Etl2Service etl2Service;

	public Etl2Controller(Etl2Process etl2Process) {
		this.etl2Process = etl2Process;
		etl2Service = (Etl2Service) SpringShell.getBean("etl2Service");
	}

	public void runningScheme() {
		while (etl2Process != null && !RedisUtils.isOrNoEtl1Finish(etl2Process.getEtlnum())) {
			try {
				logger.info("等待抓取完成");
				Thread.sleep(5000);
			} catch (InterruptedException e) {e.printStackTrace();}
		}
		logger.info("开始第二次数据转换");
		etl2Service.getDictAndCrawlerDict(etl2Process);
	}
}
