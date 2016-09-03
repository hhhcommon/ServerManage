package com.woting.crawler.scheme.crawlerplaynum.crawler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sleepycat.je.utilint.Timestamp;
import com.spiritdata.framework.util.SpiritRandom;
import com.woting.cm.core.ResOrgAsset.persis.po.ResOrgAssetPo;
import com.woting.cm.core.ResOrgAsset.service.ResOrgAssetService;
import com.woting.crawler.ext.SpringShell;
import com.woting.crawler.scheme.crawlerplaynum.QT.QTPlayNumCrawler;
import com.woting.crawler.scheme.crawlerplaynum.XMLY.XMLYPlayNumCrawler;

public class Crawler {
	Logger logger = LoggerFactory.getLogger(Crawler.class);
	private int pagesize = 10000;
	private ResOrgAssetService resAssService;
	private QTPlayNumCrawler qtPlayNumCrawler;
	private XMLYPlayNumCrawler xmlyPlayNumCrawler;

	public Map<String, Object> begionCrawlerPlayCount() {
		long begtime = System.currentTimeMillis();
		resAssService = (ResOrgAssetService) SpringShell.getBean("resOrgAssetService");
		qtPlayNumCrawler = new QTPlayNumCrawler();
		xmlyPlayNumCrawler = new XMLYPlayNumCrawler();
		Map<String, Object> m = new HashMap<>();
		int num = resAssService.getResOrgAssetNum();
		m.put("num", num);
		if (num > 0) {
			for (int i = 0; i <= num / pagesize; i++) {
				try {
					Thread.sleep(SpiritRandom.getRandom(new Random(), 10, 20));
				} catch (Exception e) {}
				List<ResOrgAssetPo> resAss = resAssService.getResOrgAssetList(i * pagesize, pagesize);
				logger.info("第[{}]次获取资源与外不资源对照列表数[{}]", i+1, resAss.size());
				if (resAss != null && resAss.size() > 0) {
					for (ResOrgAssetPo resass : resAss) {
						if (resass.getOrgName().equals("喜马拉雅")) {
							xmlyPlayNumCrawler.parseSmaPlayNum(resass);
						}
						if (resass.getOrgName().equals("蜻蜓")) {
							qtPlayNumCrawler.parsePlayNum(resass);
						}
						if (resass.getOrgName().equals("考拉")) {

						}
					}
				}
			}
		}
		m.put("cTime", new Timestamp(System.currentTimeMillis()));
		m.put("duration", System.currentTimeMillis()-begtime);
		return m;
	}

}
