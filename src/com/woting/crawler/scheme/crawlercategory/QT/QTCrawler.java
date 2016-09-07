package com.woting.crawler.scheme.crawlercategory.QT;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.woting.crawler.core.dict.persis.po.DictDPo;
import com.woting.crawler.core.dict.service.CrawlerDictService;
import com.woting.crawler.ext.SpringShell;
import com.woting.crawler.scheme.utils.ConvertUtils;

public class QTCrawler extends Thread {
	private Logger logger = LoggerFactory.getLogger(QTCrawler.class);
	private static String CategoryLink = "http://www.qingting.fm/#/home";
	
	public void parseCategory(){
		logger.info("蜻蜓分类抓取开始 ");
		Elements els = null;
		Element el = null;
		List<DictDPo> listd = new ArrayList<DictDPo>();
		int isValidate = 2;
		List<Map<String, Object>> catelist = new ArrayList<Map<String,Object>>();
		List<Map<String, Object>> qtcatelist = new ArrayList<Map<String,Object>>();
		CrawlerDictService crawlerDictService = (CrawlerDictService) SpringShell.getBean("crawlerDictService");
		if(crawlerDictService.getDictdValidNum("蜻蜓")==0) isValidate = 1;
		else isValidate = crawlerDictService.getMaxIsValidateNum("蜻蜓")+1;
		try {
			Document doc = Jsoup.connect(CategoryLink).ignoreContentType(true).get();
			els = doc.select("div[data-category=507]");
			if(els!=null&&!els.isEmpty()){
				el = els.get(0);
				els = el.select("a[data-switch-url]");
				for (Element element : els) {
					Map<String, Object> m = new HashMap<String,Object>();
					m.put("visitUrl", "http://www.qingting.fm/s"+element.attr("href"));
					m.put("id", element.attr("href").replace("/supervcategories/", ""));
					m.put("name", element.select("h5").get(0).html());
					catelist.add(m);
				}
				listd.addAll(ConvertUtils.convert2DictD(catelist, null, "蜻蜓", "3", isValidate));
				for (Map<String, Object> m1 : catelist) {
					try {
						doc = Jsoup.connect(m1.get("visitUrl")+"").ignoreContentType(true).get();
						els = doc.select("div[class=right-bar clearfix]");
						if(els!=null&&!els.isEmpty()){
							for (Element element : els) {
								Map<String, Object> m = new HashMap<String,Object>();
								m.put("name", element.select("div[class=title pull-left]").get(0).html());
								m.put("id", element.select("a[data-switch-url]").get(0).attr("href").replace("/vcategories/", "").replace("/", "_"));
								m.put("pid", m1.get("id"));
								m.put("visitUrl", "http://www.qingting.fm/s"+element.select("a[data-switch-url]").get(0).attr("href"));
								qtcatelist.add(m);
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
						continue;
					}
				}
			}
			listd.addAll(ConvertUtils.convert2DictD(qtcatelist, listd, "蜻蜓", "3", isValidate));
			logger.info("蜻蜓分类抓取数目[{}]", listd.size());
			if(listd!=null&&listd.size()>0) {
				if(isValidate==1) {
					crawlerDictService.insertDictD(listd);
					logger.info("首次蜻蜓分类抓取，数据全部入库，并生效");
				}
				else {
					if (!crawlerDictService.compareDictIsOrNoNew(listd)) {
						logger.info("发现蜻蜓有新的分类产生,入库");
						crawlerDictService.insertDictD(listd);
					} else {
						logger.info("未发现蜻蜓新分类,抓取数据作废");
					}
				}
			}
		} catch (Exception e) {e.printStackTrace();}
	}
	
	@Override
	public void run() {
		parseCategory();
	}
}
