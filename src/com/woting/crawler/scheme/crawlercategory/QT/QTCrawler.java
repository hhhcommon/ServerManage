package com.woting.crawler.scheme.crawlercategory.QT;

import java.io.IOException;
import java.sql.Timestamp;
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
import com.woting.crawler.core.record.persis.po.RecordPo;
import com.woting.crawler.core.record.service.RecordService;
import com.woting.crawler.ext.SpringShell;
import com.woting.crawler.scheme.utils.ConvertUtils;

public class QTCrawler extends Thread {
	private Logger logger = LoggerFactory.getLogger(QTCrawler.class);
	private static String CategoryLink = "http://www.qingting.fm/categories";
	private CrawlerDictService crawlerDictService;
	private int insertNum = 0;
	
	public void parseCategory(){
		logger.info("蜻蜓分类抓取开始 ");
		long begTime = System.currentTimeMillis();
		Elements els = null;
		List<DictDPo> listd = new ArrayList<DictDPo>();
		List<Map<String, Object>> catelist = new ArrayList<Map<String,Object>>();
		crawlerDictService = (CrawlerDictService) SpringShell.getBean("crawlerDictService");
		List<DictDPo> dictDs = crawlerDictService.getDictDs("0", null);
		String qtPid = null;
		if (dictDs!=null && dictDs.size()>0) {
			for (DictDPo dictDPo : dictDs) {
				if (dictDPo.getPublisher().equals("蜻蜓")) {
					qtPid = dictDPo.getId();
					listd.add(dictDPo);
				}
			}
		}
		if (qtPid!=null) {
			List<DictDPo> dictDPos_1 = crawlerDictService.getDictDs(qtPid, "蜻蜓");
			Map<String, Object> qtMap = new HashMap<>();
			if (dictDPos_1!=null && dictDPos_1.size()>0) {
				for (DictDPo dictDPo : dictDPos_1) {
					qtMap.put("1_"+dictDPo.getDdName(), dictDPo.getId());
				}
			}
			qtMap.put("1_蜻蜓", qtPid);
			Document doc = null;
			try {
				doc = Jsoup.connect(CategoryLink).ignoreContentType(true).get();
			} catch (IOException e) {
			}
			if (doc==null) return;
			els = doc.select("a[class=_3fnw]");
			if(els!=null&&!els.isEmpty()){
				for (Element element : els) {
					Map<String, Object> m = new HashMap<String,Object>();
					m.put("visitUrl", "http://www.qingting.fm/"+element.attr("href"));
					String sourceId = element.attr("href").replace("/categories/", "").replace("/0/1", "");
					m.put("id", sourceId);
					String name = element.html();
					m.put("name", name);
					m.put("pid", "qingting");
					if (!qtMap.containsKey("1_"+name)) catelist.add(m);
				}
			}
			if (catelist!=null && catelist.size()>0) {
				listd.addAll(ConvertUtils.convert2DictD(catelist, listd, "蜻蜓", "3", 1, 1));
			}
			if (listd!=null && listd.size()>0) listd.remove(0);
			insertDicts(listd);
			System.out.println(insertNum);
			if (insertNum > 0) {
				RecordService recordService = (RecordService) SpringShell.getBean("recordService");
				RecordPo rPo = new RecordPo();
				rPo.setBeginTime(new Timestamp(begTime));
				long endTime = System.currentTimeMillis();
				rPo.setEndTime(new Timestamp(endTime));
				rPo.setDuration((int)(endTime-begTime));
				rPo.setRecordType("QT_CATEGORY_ADD");
				rPo.setDescn("定时扫描，蜻蜓新增1级栏目  "+ insertNum);
				rPo.setRecordCount(insertNum);
				recordService.insertRecord(rPo);
			}
		}
	}
	
	@Override
	public void run() {
		parseCategory();
	}
	
	public void insertDicts(List<DictDPo> listd) {
		if (listd != null && listd.size() > 0) {
			crawlerDictService.insertDictD(listd);
			insertNum += listd.size();
		}
	}
}
