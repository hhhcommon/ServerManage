package com.woting.crawler.scheme.crawlersrc.XMLY.crawler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.spiritdata.framework.util.ChineseCharactersUtils;
import com.woting.crawler.core.dict.service.CrawlerDictService;
import com.woting.crawler.core.scheme.model.Scheme;
import com.woting.crawler.ext.SpringShell;
import com.woting.crawler.scheme.utils.ConvertUtils;
import com.woting.crawler.scheme.utils.RedisUtils;

public class XMLYCrawler extends Thread {
	private static String CategoryLink = "http://www.ximalaya.com/dq/all/";
	private Scheme scheme;
	public XMLYCrawler(Scheme scheme) {
		this.scheme = scheme;
	}

	public void getXMLYCategory() {
		CrawlerDictService crawlerDictService = (CrawlerDictService) SpringShell.getBean("crawlerDictService");
		Elements eles = null;
		Element el = null;
		Document doc;
		try {
			Map<String, Object> catemap = new HashMap<String, Object>();
			doc = Jsoup.connect(CategoryLink).timeout(10000).ignoreContentType(true).get();
			eles = doc.select("li[cid]");
			if (eles!=null&&!eles.isEmpty()) {
				for (Element ele : eles) {
					String catename = ele.select("a").get(0).html();
					String cateid = ele.attr("cid");
					if(catename.equals("外语"))
						catename = "英语";
					catemap.put(catename, cateid);
				}
				if (catemap != null) {
					RedisUtils.addXMLYCategory(scheme.getSchemenum(), catemap);
				}
			}
			
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
				crawlerDictService.insertDictD(ConvertUtils.convert2DictD(scheme, catelist, "喜马拉雅", "3"));
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
				crawlerDictService.insertDictD(ConvertUtils.convert2DictD(scheme, catelist, "喜马拉雅", "3"));
			}
		} catch (Exception e) {e.printStackTrace();}
	}
	
	@Override
	public void run() {
		getXMLYCategory();
	}
}
