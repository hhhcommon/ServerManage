package com.woting.crawler.scheme.crawlercategory.KL;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.woting.crawler.core.dict.persis.po.DictDPo;
import com.woting.crawler.core.dict.service.CrawlerDictService;
import com.woting.crawler.ext.SpringShell;
import com.woting.crawler.scheme.utils.ConvertUtils;
import com.woting.crawler.scheme.utils.HttpUtils;

public class KLCrawler extends Thread {
	private Logger logger = LoggerFactory.getLogger(KLCrawler.class);
	private static String CategoryLink = "http://www.kaolafm.com/webapi/category/list?fid=";

	@SuppressWarnings("unchecked")
	public void getKLCategory() {
		List<DictDPo> list = new ArrayList<DictDPo>();
		List<Map<String, Object>> catelist = new ArrayList<Map<String, Object>>();
		int isValidate = 2;
		CrawlerDictService crawlerDictService = (CrawlerDictService) SpringShell.getBean("crawlerDictService");
		if(crawlerDictService.getDictdValidNum("考拉")==0) isValidate = 1;
		else isValidate = crawlerDictService.getMaxIsValidateNum("考拉")+1;
		try {
			Map<String, Object> map = HttpUtils.getJsonMapFromURL(CategoryLink + "0");
			Map<String, Object> result = (Map<String, Object>) map.get("result");
			if (result != null) {
				List<Map<String, Object>> mcatelist = new ArrayList<Map<String, Object>>();
				List<Map<String, Object>> datalist = (List<Map<String, Object>>) result.get("dataList");
				for (Map<String, Object> m : datalist) {
					Map<String, Object> catem = new HashMap<String, Object>();
					catem.put("id", m.get("categoryId") + "");
					catem.put("name", m.get("categoryName") + "");
					catem.put("visitUrl", "http://www.kaolafm.com/category/"+m.get("categoryId") + "");
					mcatelist.add(catem);
				}
				list.addAll(ConvertUtils.convert2DictD(mcatelist, null, "考拉", "3", isValidate));
				for (Map<String, Object> m : mcatelist) {
					try {
						map = HttpUtils.getJsonMapFromURL(CategoryLink + m.get("cateId"));
						result = (Map<String, Object>) map.get("result");
						datalist = (List<Map<String, Object>>) result.get("dataList");
						if (datalist != null) {
							for (Map<String, Object> m2 : datalist) {
								Map<String, Object> catem = new HashMap<String, Object>();
								catem.put("id", m2.get("categoryId") + "");
								catem.put("name", m2.get("categoryName") + "");
								catem.put("pid", m.containsKey("cateId") ? m.get("cateId") : "0");
								catem.put("visitUrl", "http://www.kaolafm.com/category/"+catem.get("pid")+"/"+catem.get("id"));
								catelist.add(catem);
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
						continue;
					}
				}
				list.addAll(ConvertUtils.convert2DictD(catelist, list, "考拉", "3", isValidate));
				logger.info("考拉分类抓取数目[{}]", list.size());
				if(list!=null&&list.size()>0) {
					if(isValidate==1) {
						crawlerDictService.insertDictD(list);
						logger.info("首次考拉分类抓取，数据全部入库，并生效");
					}
					else {
						if (!crawlerDictService.compareDictIsOrNoNew(list)) {
							logger.info("发现考拉有新的分类产生,入库");
							crawlerDictService.insertDictD(list);
						} else {
							logger.info("未发现考拉新分类,抓取数据作废");
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		getKLCategory();
	}
}
