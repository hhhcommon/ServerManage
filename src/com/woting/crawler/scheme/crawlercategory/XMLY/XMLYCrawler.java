package com.woting.crawler.scheme.crawlercategory.XMLY;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.spiritdata.framework.util.ChineseCharactersUtils;
import com.woting.crawler.core.dict.persis.po.DictDPo;
import com.woting.crawler.core.dict.service.CrawlerDictService;
import com.woting.crawler.core.record.persis.po.RecordPo;
import com.woting.crawler.core.record.service.RecordService;
import com.woting.crawler.ext.SpringShell;
import com.woting.crawler.scheme.utils.ConvertUtils;
import com.woting.crawler.scheme.utils.HttpUtils;

public class XMLYCrawler extends Thread {
	private Logger logger = LoggerFactory.getLogger(XMLYCrawler.class);
	private static String CategoryLink = "http://www.ximalaya.com/dq/all/";
	private CrawlerDictService crawlerDictService;
	private int insertNum_1 = 0;
	private int insertNum_2 = 0;
	
	public void getXMLYCategory() {
		logger.info("喜马拉雅分类抓取开始 ");
		long begTime = System.currentTimeMillis();
		List<DictDPo> listd = new ArrayList<DictDPo>();
		Elements eles = null;
		Element el = null;
		Document doc;
		
		crawlerDictService = (CrawlerDictService) SpringShell.getBean("crawlerDictService");
		List<DictDPo> dictDs = crawlerDictService.getDictDs("0", null);
		String xmlyPid = null;
		if (dictDs!=null && dictDs.size()>0) {
			for (DictDPo dictDPo : dictDs) {
				if (dictDPo.getPublisher().equals("喜马拉雅")) {
					xmlyPid = dictDPo.getId();
					listd.add(dictDPo);
				}
			}
			if (xmlyPid!=null) {
				List<DictDPo> dictDPos_1 = crawlerDictService.getDictDs(xmlyPid, "喜马拉雅");
				Map<String, Object> xmlyMap = new HashMap<>();
				if (dictDPos_1!=null && dictDPos_1.size()>0) {
					for (DictDPo dictDPo : dictDPos_1) {
						xmlyMap.put("1_"+dictDPo.getDdName(), dictDPo.getId());
					}
				}
				xmlyMap.put("1_喜马拉雅", xmlyPid);
				doc = HttpUtils.getJsonStrForUrl(CategoryLink);
				// 加载分类信息
				List<Map<String, Object>> catelist = new ArrayList<Map<String, Object>>();
				// 加载一级分类信息
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
							String name = els.html();
							if (xmlyMap.containsKey("1_"+name)) continue;
							else {
								m.put("visitUrl", "http://www.ximalaya.com" + ele.attr("href"));
								m.put("name", els.html());
								m.put("pid", "ximalaya");
								catelist.add(m);
							}
						}
					}
				}
				if (catelist!=null && catelist.size()>0) {
					listd.addAll(ConvertUtils.convert2DictD(catelist, listd, "喜马拉雅", "3", 1, 1));
				}
				if (listd!=null && listd.size()>0) listd.remove(0);
				insertDicts(listd, 1);
				listd.clear();
				//加载二级分类信息
				eles = doc.select("div[data-cache]");
				if (eles != null && !eles.isEmpty()) {
					for (Element ele : eles) {
						catelist.clear();
						Elements els = ele.select("a[hashlink]");
						String pid = ele.attr("data-cache");
						DictDPo dictDPo = crawlerDictService.getDictDInfoBySourceIdAndPublisher(pid, "喜马拉雅");
						if (dictDPo==null) continue;
						String cpid = dictDPo.getId();
						List<DictDPo> dDs = crawlerDictService.getDictDs(cpid, "喜马拉雅");
						Map<String, Object> dDsMap = new HashMap<>();
						if (dDs!=null && dDs.size()>0) {
							for (DictDPo dictDPo2 : dDs) {
								dDsMap.put(dictDPo2.getSourceId(), null);
							}
						}
						listd.add(dictDPo);
						for (Element element : els) {
							Map<String, Object> m = new HashMap<String, Object>();
							m.put("visitUrl", "http://www.ximalaya.com" + element.attr("href"));
							m.put("name", element.attr("tid")); // 喜马拉雅二级分类只给出名称信息
							m.put("pid", pid);
							String sourceId = ele.attr("data-cache") + "_" + ChineseCharactersUtils.getFullSpellFirstUp(element.attr("tid"));
							m.put("id", sourceId);
							if (!dDsMap.containsKey(sourceId)) catelist.add(m);
						}
						listd.addAll(ConvertUtils.convert2DictD(catelist, listd, "喜马拉雅", "3", 1, 1));
						if (listd!=null && listd.size()>0) listd.remove(0);
						insertDicts(listd, 2);
						listd.clear();
					}
				}
			}
		}
		if (insertNum_1 > 0 || insertNum_2 > 0) {
			RecordService recordService = (RecordService) SpringShell.getBean("recordService");
			RecordPo rPo = new RecordPo();
			rPo.setBeginTime(new Timestamp(begTime));
			long endTime = System.currentTimeMillis();
			rPo.setEndTime(new Timestamp(endTime));
			rPo.setDuration((int)(endTime-begTime));
			rPo.setRecordType("XMLY_CATEGORY_ADD");
			rPo.setDescn("定时扫描，喜马拉雅新增1级栏目  " + insertNum_1 + " 喜马拉雅新增2级栏目 " + insertNum_2);
			rPo.setRecordCount(insertNum_1+insertNum_2);
			recordService.insertRecord(rPo);
		}
	}

	@Override
	public void run() {
		getXMLYCategory();
	}
	
	public void insertDicts(List<DictDPo> listd, int num) {
		if (listd != null && listd.size() > 0) {
			crawlerDictService.insertDictD(listd);
			if (num==1) insertNum_1++;
			if (num==2) insertNum_2++;
		}
	}
}
