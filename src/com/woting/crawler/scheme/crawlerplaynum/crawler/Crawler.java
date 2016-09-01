package com.woting.crawler.scheme.crawlerplaynum.crawler;

import java.util.List;

import com.woting.cm.core.ResOrgAsset.persis.po.ResOrgAssetPo;
import com.woting.cm.core.ResOrgAsset.service.ResOrgAssetService;
import com.woting.crawler.ext.SpringShell;

public class Crawler {

	private ResOrgAssetService resAssService;
	
	private void begionCrawlerPlayCount(){
		resAssService = (ResOrgAssetService) SpringShell.getBean("resOrgAssetService");
		List<ResOrgAssetPo> resAss = resAssService.getResOrgAssetList();
		if(resAss!=null&&resAss.size()>0) {
			for (ResOrgAssetPo resass : resAss) {
				if(resass.getOrgName().equals("喜马拉雅")) {
					
				}
				if(resass.getOrgName().equals("蜻蜓")) {
					
				}
				if(resass.getOrgName().equals("考拉")) {
					
				}
			}
		}
	}
	
}
