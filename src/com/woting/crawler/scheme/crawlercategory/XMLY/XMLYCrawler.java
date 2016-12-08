package com.woting.crawler.scheme.crawlercategory.XMLY;

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

import com.spiritdata.framework.core.cache.SystemCache;
import com.spiritdata.framework.util.ChineseCharactersUtils;
import com.spiritdata.framework.util.JsonUtils;
import com.spiritdata.framework.util.SequenceUUID;
import com.woting.cm.core.keyword.persis.po.KeyWordPo;
import com.woting.cm.core.keyword.persis.po.KwResPo;
import com.woting.cm.core.keyword.service.KeyWordService;
import com.woting.crawler.CrawlerConstants;
import com.woting.crawler.core.dict.persis.po.DictDPo;
import com.woting.crawler.core.dict.service.CrawlerDictService;
import com.woting.crawler.ext.SpringShell;
import com.woting.crawler.scheme.utils.ConvertUtils;
import com.woting.crawler.scheme.utils.FileUtils;

public class XMLYCrawler extends Thread {
	private Logger logger = LoggerFactory.getLogger(XMLYCrawler.class);
	private static String CategoryLink = "http://www.ximalaya.com/dq/all/";

	public void getXMLYCategory() {
		List<Map<String, Object>> cate2dictdlist = FileUtils
				.readFileByJson(SystemCache.getCache(CrawlerConstants.APP_PATH).getContent() + "conf/craw.txt");
		logger.info("喜马拉雅分类抓取开始 ");
		List<DictDPo> listd = new ArrayList<DictDPo>();
		Elements eles = null;
		Element el = null;
		Document doc;
		int isValidate = 0;
		CrawlerDictService crawlerDictService = (CrawlerDictService) SpringShell.getBean("crawlerDictService");
		KeyWordService kwService = (KeyWordService) SpringShell.getBean("keyWordService");
		if (crawlerDictService.getDictdValidNum("喜马拉雅") == 0)
			isValidate = 1;
		else
			isValidate = crawlerDictService.getMaxIsValidateNum("喜马拉雅") + 1;
		try {
			doc = Jsoup.connect(CategoryLink).timeout(10000).ignoreContentType(true).get();
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
						m.put("visitUrl", "http://www.ximalaya.com" + ele.attr("href"));
						m.put("name", els.html());
						catelist.add(m);
					}
				}
				listd.addAll(ConvertUtils.convert2DictD(catelist, null, "喜马拉雅", "3", isValidate));
				catelist.clear();
				// 加载二级分类信息
				eles = doc.select("div[data-cache]");
				if (eles != null && !eles.isEmpty()) {
					for (Element ele : eles) {
						Elements els = ele.select("a[hashlink]");
						for (Element element : els) {
							Map<String, Object> m = new HashMap<String, Object>();
							m.put("visitUrl", "http://www.ximalaya.com" + element.attr("href"));
							m.put("name", element.attr("tid")); // 喜马拉雅二级分类只给出名称信息
							m.put("pid", ele.attr("data-cache"));
							m.put("id", ele.attr("data-cache") + "_"
									+ ChineseCharactersUtils.getFullSpellFirstUp(element.attr("tid")));
							catelist.add(m);
						}
					}
				}
				listd.addAll(ConvertUtils.convert2DictD(catelist, listd, "喜马拉雅", "3", isValidate));
				logger.info("喜马拉雅分类抓取数目[{}]", listd.size());
				System.out.println(JsonUtils.objToJson(listd));
				if (listd != null && listd.size() > 0) {
					if (isValidate == 1) {
						crawlerDictService.insertDictD(listd);
						logger.info("首次喜马拉雅分类抓取，数据全部入库，并生效");
					} else {
						if (!crawlerDictService.compareDictIsOrNoNew(listd)) {
							logger.info("发现喜马拉雅有新的分类产生,入库");
							crawlerDictService.insertDictD(listd);
						} else {
							logger.info("未发现喜马拉雅新分类,抓取数据作废");
						}
					}
					List<KeyWordPo> kws = new ArrayList<>();
					List<KwResPo> krs = new ArrayList<>();
					for (DictDPo m : listd) {
						if (!m.getpId().equals("0")) {
							if (kwService.KeyWordIsNull(m.getDdName())) {
								KeyWordPo kw = new KeyWordPo();
								kw.setId(SequenceUUID.getPureUUID());
								kw.setOwnerId("cm");
								kw.setOwnerType(0);
								kw.setKwName(m.getDdName());
								kw.setIsValidate(1);
								kw.setnPy(ChineseCharactersUtils.getFullSpellFirstUp(m.getDdName()));
								kw.setSort(0);
								kw.setDescn("喜马拉雅");
								kw.setcTime(new Timestamp(System.currentTimeMillis()));
								kws.add(kw);
								for (DictDPo mm : listd) {
									if (m.getpId().equals(mm.getId())) {
										for (Map<String, Object> cate : cate2dictdlist) {
											if (cate.get("publisher").equals("喜马拉雅")
													&& mm.getDdName().equals(cate.get("crawlerDictdName"))) {
												KwResPo kr = new KwResPo();
												kr.setId(SequenceUUID.getPureUUID());
												kr.setRefName("标签-栏目");
												kr.setKwId(kw.getId());
												kr.setResTableName("wt_ChannelAsset");
												if (!cate.get("dictdId").equals("")) {
													kr.setResId(cate.get("dictdId") + "");
													kr.setcTime(new Timestamp(System.currentTimeMillis()));
													krs.add(kr);
												}
											}
										}
									}
								}
							}
						}
					}
					if (kws.size() > 0 && krs.size() > 0) {
						kwService.insertKeyWords(kws);
						kwService.insertKwRefs(krs);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		getXMLYCategory();
	}
}
