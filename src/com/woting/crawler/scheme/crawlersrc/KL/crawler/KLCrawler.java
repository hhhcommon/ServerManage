package com.woting.crawler.scheme.crawlersrc.KL.crawler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.woting.crawler.core.scheme.model.Scheme;
import com.woting.crawler.scheme.utils.HttpUtils;
import com.woting.crawler.scheme.utils.RedisUtils;

public class KLCrawler extends Thread {
	private static String CategoryLink = "http://www.kaolafm.com/webapi/category/list?fid=";
	private Scheme scheme;

	public KLCrawler(Scheme scheme) {
		this.scheme = scheme;
	}

	@SuppressWarnings("unchecked")
	public void getKLCategory() {
		List<Map<String, Object>> catelist = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> errlist = new ArrayList<Map<String, Object>>();
		try {
			Map<String, Object> map = HttpUtils.getJsonMapFromURL(CategoryLink + "0");
			Map<String, Object> result = (Map<String, Object>) map.get("result");
			if (result != null) {
				List<Map<String, Object>> mcatelist = new ArrayList<Map<String, Object>>();
				List<Map<String, Object>> datalist = (List<Map<String, Object>>) result.get("dataList");
				for (Map<String, Object> m : datalist) {
					Map<String, Object> catem = new HashMap<String, Object>();
					catem.put("cateId", m.get("categoryId") + "");
					catem.put("cateName", m.get("categoryName") + "");
					catem.put("parentId", m.containsKey("cateId") ? m.get("cateId") : "0");
					catem.put("parentName", m.containsKey("cateName") ? m.get("cateName") : "分类");
					catem.put("isMainCategory", catem.get("parentId").equals("0") ? "1" : "0");
					mcatelist.add(catem);
					catelist.add(catem);
				}
				for (Map<String, Object> m : mcatelist) {
					try {
						map = HttpUtils.getJsonMapFromURL(CategoryLink + m.get("cateId"));
						result = (Map<String, Object>) map.get("result");
						datalist = (List<Map<String, Object>>) result.get("dataList");
						if (datalist != null) {
							for (Map<String, Object> m2 : datalist) {
								Map<String, Object> catem = new HashMap<String, Object>();
								catem.put("cateId", m2.get("categoryId") + "");
								catem.put("cateName", m2.get("categoryName") + "");
								catem.put("parentId", m.containsKey("cateId") ? m.get("cateId") : "0");
								catem.put("parentName", m.containsKey("cateName") ? m.get("cateName") : "分类");
								catem.put("isMainCategory", catem.get("parentId").equals("0") ? "1" : "0");
								catelist.add(catem);
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
						errlist.add(m);
						continue;
					}
				}
				//开始对为抓到的分类信息再次抓取
				for (Map<String, Object> m : errlist) {
					try {
						map = HttpUtils.getJsonMapFromURL(CategoryLink + m.get("cateId"));
						result = (Map<String, Object>) map.get("result");
						datalist = (List<Map<String, Object>>) result.get("dataList");
						if (datalist != null) {
							for (Map<String, Object> m2 : datalist) {
								Map<String, Object> catem = new HashMap<String, Object>();
								catem.put("cateId", m2.get("categoryId") + "");
								catem.put("cateName", m2.get("categoryName") + "");
								catem.put("parentId", m.containsKey("cateId") ? m.get("cateId") : "0");
								catem.put("parentName", m.containsKey("cateName") ? m.get("cateName") : "分类");
								catem.put("isMainCategory", catem.get("parentId").equals("0") ? "1" : "0");
								catelist.add(catem);
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
						continue;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		RedisUtils.addKLCategory(scheme.getSchemenum(), catelist);
	}

	@Override
	public void run() {
		getKLCategory();
	}
}
