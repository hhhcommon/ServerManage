package com.woting.crawler.core.etl.control;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.woting.crawler.core.etl.model.Etl1Process;
import com.woting.crawler.core.etl.service.Etl1Service;
import com.woting.crawler.ext.SpringShell;
import com.woting.crawler.scheme.crawlersrc.QT.etl.QTEtl1Process;
import com.woting.crawler.scheme.crawlersrc.XMLY.etl.XMLYEtl1Process;
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
		while(etl1Process!=null && !RedisUtils.isOrNoCrawlerFinish(etl1Process.getEtlnum())){
			try {
				logger.info("等待抓取完成");
				RedisUtils.waitCrawlerFinish(etl1Process.getEtlnum());
				Thread.sleep(5000);
			} catch (InterruptedException e) {e.printStackTrace();}
		}
		Map<String, Object> qtm =  new QTEtl1Process(etl1Process).makeQTOrigDataList();
		Map<String, Object> xmlym = new XMLYEtl1Process(etl1Process).makeXMLYOrigDataList();
		logger.info("蜻蜓FM抓取数据第一次转换数据存放中间库中");
		etl1Service.insertSqlAlbumAndAudio(qtm);
		logger.info("蜻蜓FM抓取数据第一次转换完成");
		logger.info("喜马拉雅FM抓取数据第一次转换数据存放中间库中");
		etl1Service.insertSqlAlbumAndAudio(xmlym);
		logger.info("喜马拉雅FM抓取数据第一次转换完成");
		etl1Service.removeNull();
		RedisUtils.writeEtl1Finish(etl1Process.getEtlnum(), "");
	}
}
