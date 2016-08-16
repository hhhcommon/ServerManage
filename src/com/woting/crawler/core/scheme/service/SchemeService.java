package com.woting.crawler.core.scheme.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.hp.hpl.sparta.xpath.ThisNodeTest;
import com.woting.crawler.core.etl.model.Etl1Process;
import com.woting.crawler.core.scheme.model.Scheme;

@Service
public class SchemeService {
	private Logger logger = LoggerFactory.getLogger(ThisNodeTest.class);
	private Scheme scheme;

	public SchemeService() {
		scheme = new Scheme("");
	}

	public Scheme getScheme(){
		return scheme;
	}
	
//	public String getSchemeNum() {
//		String crawlernum = scheme.getSchemenum();
//		while (RedisUtils.isOrNoCrawlerFinish(crawlernum)) {
//			logger.info("开始判断redis里是否存在当前抓取序号[{}]是否已存在", crawlernum);
//			logger.info("抓取序号[{}]已存在", crawlernum);
//			int num = Integer.valueOf(crawlernum) + 1;
//			crawlernum = num + "";
//			logger.info("验证抓取序号[{}]是否存在", crawlernum);
//		}
//		logger.info("抓取序号[{}]不存在", crawlernum);
//		logger.info("开始进行序号为[{}]抓取", crawlernum);
//		scheme.setSchemenum(crawlernum);
//		return crawlernum;
//	}
	
	public Etl1Process gEtl1Process() {
		return scheme.getEtl1Process();
	}
}
