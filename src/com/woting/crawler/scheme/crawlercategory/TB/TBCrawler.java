package com.woting.crawler.scheme.crawlercategory.TB;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.spiritdata.framework.util.JsonUtils;
import com.woting.crawler.core.dict.persis.po.DictDPo;
import com.woting.crawler.core.dict.service.CrawlerDictService;
import com.woting.crawler.core.record.persis.po.RecordPo;
import com.woting.crawler.core.record.service.RecordService;
import com.woting.crawler.ext.SpringShell;
import com.woting.crawler.scheme.utils.ConvertUtils;
import com.woting.crawler.scheme.utils.HttpUtils;

public class TBCrawler extends Thread {
	private Logger logger = LoggerFactory.getLogger(TBCrawler.class);
	private static String CategoryLink = "http://www.tingban.cn/webapi/category/list?fid=0";
	private static String Category2Link = "http://www.tingban.cn/webapi/category/list?fid=";
	private CrawlerDictService crawlerDictService;
	private int insertNum_1 = 0;
	private int insertNum_2 = 0;
	
	@SuppressWarnings("unchecked")
	public void getTBCategory() {
		logger.info("听伴分类抓取开始 ");
		long begTime = System.currentTimeMillis();
		List<DictDPo> listd = new ArrayList<DictDPo>();
		Document doc;
		
		crawlerDictService = (CrawlerDictService) SpringShell.getBean("crawlerDictService");
		List<DictDPo> dictDs = crawlerDictService.getDictDs("0", null);
		String tbPid = null;
		if (dictDs!=null && dictDs.size()>0) {
			for (DictDPo dictDPo : dictDs) {
				if (dictDPo.getPublisher().equals("听伴")) {
					tbPid = dictDPo.getId();
					listd.add(dictDPo);
				}
			}
			if (tbPid!=null) {
				List<DictDPo> dictDPos_1 = crawlerDictService.getDictDs(tbPid, "听伴");
				Map<String, Object> tbMap = new HashMap<>();
				if (dictDPos_1!=null && dictDPos_1.size()>0) {
					for (DictDPo dictDPo : dictDPos_1) {
						tbMap.put("1_"+dictDPo.getDdName(), dictDPo.getId());
					}
				}
				tbMap.put("1_听伴", tbPid);
				doc = HttpUtils.getJsonStrForUrl(CategoryLink);
				// 加载分类信息
				List<Map<String, Object>> catelist = new ArrayList<Map<String, Object>>();
				String jsonStr = doc.body().html();
				Map<String, Object> cateMap = (Map<String, Object>) JsonUtils.jsonToObj(jsonStr, Map.class);
				if (cateMap!=null && cateMap.size()>0) {
					cateMap = (Map<String, Object>) cateMap.get("result");
					List<Map<String, Object>> cates = (List<Map<String, Object>>) cateMap.get("dataList");
					if (cates!=null && cates.size()>0) {
						for (Map<String, Object> map : cates) {
							String id = map.get("categoryId").toString();
							String name = map.get("categoryName").toString();
							if (name.equals("智能电台") || name.equals("传统电台") || name.equals("热门榜单") || name.equals("直播 ")) continue;
							if (tbMap.containsKey("1_"+name)) continue;
							Map<String, Object> m = new HashMap<>();
							m.put("id", id);
							m.put("name", name);
							m.put("visitUrl", "http://www.tingban.cn/category/" + id);
							m.put("pid", "tingban");
							catelist.add(m);
						}
					}
				}
				if (catelist!=null && catelist.size()>0) {
					listd.addAll(ConvertUtils.convert2DictD(catelist, listd, "听伴", "3", 1, 1));
				}
				if (listd!=null && listd.size()>0) listd.remove(0);
				insertDicts(listd, 1);
				listd.clear();
				//加载二级分类信息
				dictDPos_1 = crawlerDictService.getDictDs(tbPid, "听伴");
				if (dictDPos_1!=null && dictDPos_1.size()>0) {
					for (DictDPo dictDPo : dictDPos_1) {
						try {
							listd.clear();
							catelist.clear();
							List<DictDPo> dds2 = crawlerDictService.getDictDs(dictDPo.getId(), "听伴");
							Map<String, Object> dds2Map = new HashMap<>();
							if (dds2!=null && dds2.size()>0) {
								for (DictDPo dictDPo2 : dds2) {
									dds2Map.put("2_"+dictDPo2.getDdName(), null);
								}
							}
							listd.add(dictDPo);
							String sourceId = dictDPo.getSourceId();
							System.out.println(sourceId);
							doc = HttpUtils.getJsonStrForUrl(Category2Link+sourceId);
							if (doc==null) continue;
							jsonStr = doc.body().html();
							if (jsonStr==null || jsonStr.length()==0) continue;
							Map<String, Object> jsonMap = (Map<String, Object>) JsonUtils.jsonToObj(jsonStr, Map.class);
							if (jsonMap==null || jsonMap.size()==0) continue;
							jsonMap = (Map<String, Object>) jsonMap.get("result");
							List<Map<String, Object>> cates2 = (List<Map<String, Object>>) jsonMap.get("dataList");
							if (cates2==null || cates2.size()==0) continue;
							for (Map<String, Object> map : cates2) {
								String id = map.get("categoryId").toString();
								String name = map.get("categoryName").toString();
								if (dds2Map.containsKey("2_"+name)) continue;
								Map<String, Object> m = new HashMap<>();
								m.put("id", id);
								m.put("name", name);
								m.put("visitUrl", "http://www.tingban.cn/category/" + id);
								m.put("pid", sourceId);
								catelist.add(m);
							}
							if (catelist!=null && catelist.size()>0) {
								listd.addAll(ConvertUtils.convert2DictD(catelist, listd, "听伴", "3", 1, 1));
							}
							if (listd!=null && listd.size()>0) listd.remove(0);
							insertDicts(listd, 2);
						} catch (Exception e) {
							continue;
						}
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
			rPo.setRecordType("TINGBAN_CATEGORY_ADD");
			rPo.setDescn("定时扫描，听伴新增1级栏目  " + insertNum_1 + " 听伴新增2级栏目 " + insertNum_2);
			rPo.setRecordCount(insertNum_1+insertNum_2);
			recordService.insertRecord(rPo);
		}
	}

	@Override
	public void run() {
		getTBCategory();
	}
	
	public void insertDicts(List<DictDPo> listd, int num) {
		if (listd != null && listd.size() > 0) {
			crawlerDictService.insertDictD(listd);
			if (num==1) insertNum_1 += listd.size();
			if (num==2) insertNum_2 += listd.size();
		}
	}
}
