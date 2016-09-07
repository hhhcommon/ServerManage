package com.woting.crawler.scheme.crawlercategory.XMLY;

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

import com.spiritdata.framework.util.ChineseCharactersUtils;
import com.woting.crawler.core.dict.persis.po.DictDPo;
import com.woting.crawler.core.dict.service.CrawlerDictService;
import com.woting.crawler.ext.SpringShell;
import com.woting.crawler.scheme.utils.ConvertUtils;

public class XMLYCrawler extends Thread {
	private Logger logger = LoggerFactory.getLogger(XMLYCrawler.class);
	private static String CategoryLink = "http://www.ximalaya.com/dq/all/";

	public void getXMLYCategory() {
		logger.info("喜马拉雅分类抓取开始 ");
		List<DictDPo> listd = new ArrayList<DictDPo>();
		Elements eles = null;
		Element el = null;
		Document doc;
		int isValidate = 0;
		CrawlerDictService crawlerDictService = (CrawlerDictService) SpringShell.getBean("crawlerDictService");
		if(crawlerDictService.getDictdValidNum("喜马拉雅")==0) isValidate = 1;
		else isValidate = crawlerDictService.getMaxIsValidateNum("喜马拉雅")+1;
		try {
			doc = Jsoup.connect(CategoryLink).timeout(10000).ignoreContentType(true).get();
			//加载分类信息 
			List<Map<String, Object>> catelist = new ArrayList<Map<String, Object>>();
			//加载一级分类信息
			eles = doc.select("ul[class=sort_list]");
			if (eles != null && !eles.isEmpty()) {
				el = eles.get(0);
				eles = el.select("li[cid]");
				for (Element ele : eles) {
					Map<String, Object> m = new HashMap<String, Object>();
					m.put("id", ele.attr("cid"));
					m.put("nPy", ele.attr("cname"));
					Elements els = ele.select("a");
					if (els != null && !els.isEmpty()) {
						ele = els.get(0);
						m.put("visitUrl", "http://www.ximalaya.com" + ele.attr("href"));
						m.put("name", els.html());
						catelist.add(m);
					}
				}
				listd.addAll(ConvertUtils.convert2DictD(catelist,null ,"喜马拉雅", "3", isValidate));
				catelist.clear();
				//加载二级分类信息
				eles = doc.select("div[data-cache]");
				if (eles != null && !eles.isEmpty()) {
					for (Element ele : eles) {
						Elements els = ele.select("a[hashlink]");
						for (Element element : els) {
							Map<String, Object> m = new HashMap<String, Object>();
							m.put("visitUrl", "http://www.ximalaya.com" + element.attr("href"));
							m.put("name", element.attr("tid")); //喜马拉雅二级分类只给出名称信息
							m.put("pid", ele.attr("data-cache"));
							m.put("id", ele.attr("data-cache")+"_"+ChineseCharactersUtils.getFullSpellFirstUp(element.attr("tid")));
							catelist.add(m);
						}
					}
				}
				listd.addAll(ConvertUtils.convert2DictD(catelist, listd, "喜马拉雅", "3", isValidate));
				logger.info("喜马拉雅分类抓取数目[{}]", listd.size());
				if(listd!=null&&listd.size()>0) {
					if(isValidate==1) {
						crawlerDictService.insertDictD(listd);
						logger.info("首次喜马拉雅分类抓取，数据全部入库，并生效");
					}
					else {
						if (!crawlerDictService.compareDictIsOrNoNew(listd)) {
							logger.info("发现喜马拉雅有新的分类产生,入库");
							crawlerDictService.insertDictD(listd);
						} else {
							logger.info("未发现喜马拉雅新分类,抓取数据作废");
						}
					}
				}
			}
		} catch (Exception e) {e.printStackTrace();}
	}
	
	@Override
	public void run() {
		getXMLYCategory();
	}
}
