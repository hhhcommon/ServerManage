package com.woting.crawler.core.etl.control;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.spiritdata.framework.core.cache.SystemCache;
import com.spiritdata.framework.ext.spring.redis.RedisOperService;
import com.woting.crawler.CrawlerConstants;
import com.woting.crawler.core.etl.model.Etl1Process;
import com.woting.crawler.core.etl.service.Etl1Service;
import com.woting.crawler.core.scheme.model.Scheme;
import com.woting.crawler.ext.SpringShell;
import com.woting.crawler.scheme.utils.RedisUtils;

public class Etl1Controller {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private Etl1Process etl1Process;
	private Etl1Service etl1Service;
	
	public Etl1Controller(Etl1Process etl1Process) {
		this.etl1Process = etl1Process;
		etl1Service = (Etl1Service) SpringShell.getBean("etl1Service");
	}
	
	public void runningScheme() {
		Scheme scheme = (Scheme) SystemCache.getCache(CrawlerConstants.SCHEME).getContent();
		RedisOperService rs = scheme.getRedisOperService();
		while(etl1Process!=null && !RedisUtils.isOrNoCrawlerFinish(rs, etl1Process.getEtlnum())){
			try {
				logger.info("等待抓取完成");
				RedisUtils.waitCrawlerFinish(rs, etl1Process.getEtlnum());
				Thread.sleep(10000);
			} catch (InterruptedException e) {e.printStackTrace();}
		}
		
//		new QTEtl1Process(etl1Process).makeQTOrigDataList();
//		new XMLYEtl1Process(etl1Process).makeXMLYOrigDataList();
		etl1Service.removeNull();
		logger.info("抓取第一次数据转换完成");
//		logger.info("蜻蜓FM抓取数据第一次转换数据存放中间库中");
//		etl1Service.insertSqlAlbumAndAudio(qtm);
//		logger.info("蜻蜓FM抓取数据第一次转换完成");
//		logger.info("喜马拉雅FM抓取数据第一次转换数据存放中间库中");
//		etl1Service.insertSqlAlbumAndAudio(xmlym);
//		logger.info("喜马拉雅FM抓取数据第一次转换完成");
		RedisUtils.writeEtl1Finish(rs, etl1Process.getEtlnum(), "");
	}
}
