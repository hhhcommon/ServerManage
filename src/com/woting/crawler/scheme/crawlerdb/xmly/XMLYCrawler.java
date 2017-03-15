package com.woting.crawler.scheme.crawlerdb.xmly;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import com.spiritdata.framework.util.JsonUtils;
import com.woting.crawler.core.httpclient.service.HttpClientService;
import com.woting.crawler.core.scheme.model.Scheme;
import com.woting.crawler.ext.SpringShell;
import com.woting.crawler.scheme.crawlerdb.crawler.EtlProcess;
import com.woting.crawler.scheme.utils.CleanDataUtils;
import com.woting.crawler.scheme.utils.FileUtils;
import com.woting.crawler.scheme.utils.HttpUtils;

public class XMLYCrawler {
	private Map<String, Object> map = new HashMap<>();
	private Map<String, Object> newmap = new HashMap<>();
	private HttpClientService httpClientService;
	int httpclientnums = 0;
	String path = "/opt/CrawlerCS/XMLYREF.txt";
	private Scheme scheme;
	private EtlProcess etlProcess;
	private File file = null;
	
	
	@SuppressWarnings("unchecked")
	public XMLYCrawler(Scheme scheme) {
		file = new File(path);
		String str = FileUtils.readFile(file);
		if (str!=null && str.length()>0) {
			map = (Map<String, Object>) JsonUtils.jsonToObj(str, Map.class);
		}
		httpClientService = (HttpClientService) SpringShell.getBean("httpClientService");
		this.scheme = scheme;
		etlProcess = new EtlProcess();
		if (map.containsKey("doingDB")) {
			Map<String, Object> dbmap = (Map<String, Object>) map.get("doingDB");
			etlProcess.removeDBAll(dbmap);
			map.remove("doingDB");
			FileUtils.writeFile(JsonUtils.objToJson(map), file);
		}
	}
	
	public void beginCrawler() {
		List<Map<String, Object>> ls = new ArrayList<>();
		List<String> newls = new ArrayList<>();
		Map<String, Object> catemap = new HashMap<>();
		try {
			Document doc = HttpUtils.makeXMLYJsoup("http://www.ximalaya.com/dq/all/");
			Elements eles = doc.select("li[cname]");
			if (eles!=null && eles.size()>0) {
				for (Element element : eles) {
					String cid = element.attr("cid");
					catemap.put(cid, element.select("a").get(0).html());
					Map<String, Object> urlmap = new HashMap<>();
					urlmap.put("CateName", element.select("a").get(0).html());
					urlmap.put("CateUrl", "http://www.ximalaya.com"+element.select("a").get(0).attr("href"));
					ls.add(urlmap);
				}
			}
			for (String key : catemap.keySet()) {
				eles = doc.select("div[data-cache="+key+"]");
				if (eles!=null && eles.size()>0) {
					for (Element element : eles) {
//						String cid = key;
						Elements els = element.select("a[class=tagBtn]");
						if (els!=null && els.size()>0) {
							for (Element element2 : els) {
//								catemap2.put(catemap.get(key)+"/"+element2.select("span").get(0).html()+"cid", cid);
								Map<String, Object> urlmap = new HashMap<>();
								urlmap.put("CateName", catemap.get(key)+"/"+element2.select("span").get(0).html());
								urlmap.put("CateUrl", "http://www.ximalaya.com"+element2.attr("href"));
								ls.add(urlmap);
							}
						}
					}
				}
			}
			Map<String, Object> addcatemap = new HashMap<>();
			addcatemap.put("CateName", "其他");
			addcatemap.put("CateUrl", "http://www.ximalaya.com/dq/other/");
			ls.add(addcatemap);
//			catemap.putAll(catemap2);
			int crawlerSize = ls.size()/2+1;
			ExecutorService fixedThreadPool = Executors.newFixedThreadPool(crawlerSize);
			for (int i = 1; i <= crawlerSize; i++) {
				int fonum = i;
				fixedThreadPool.execute(new Runnable() {
					public void run() {
						List<Map<String, Object>> ls2 = ls.subList((fonum-1)*2, fonum*2>ls.size()?ls.size():fonum*2);
						for (Map<String, Object> map2 : ls2) {
							String url = map2.get("CateUrl").toString();
							String cateName = map2.get("CateName").toString();
							try {
								for (int j = 1; j < 85; j++) {
									try {
										Thread.sleep(50);
										Document doc = HttpUtils.makeXMLYJsoup(url+j+".ajax?_toSub_=true&toSub=true");
										String mapstr = doc.body().html();
										mapstr = mapstr.substring(0, mapstr.lastIndexOf("\",\"moduleName\""));
										mapstr = mapstr.replace("\\&quot;", "").replace("{\"html\":\"", "");
										doc = Jsoup.parse(mapstr);
										Elements eles = doc.select("div[class=discoverAlbum_item]");
										if (eles!=null && eles.size()>0) {
											for (Element element : eles) {
												String albumId = element.attr("album_id");
												synchronized (map) {
													if (map.containsKey(albumId)) {
														String cates = map.get(albumId)+"";
														if (!cates.contains(cateName)) {
															map.put(albumId, cates+","+cateName);
														}
													} else {
														map.put(albumId, cateName);
														newls.add(albumId);
													}
												}
											}
										}
									} catch (Exception e) {
										continue;
									}
								}
							} catch (Exception e) {
								e.printStackTrace();
								continue;
							}
						}
					}
				});
			}
			fixedThreadPool.shutdown();
			while (true) {
				Thread.sleep(10000);
				System.out.println("本次抓取遍历到专辑数:"+map.size()+"   新增专辑数:"+newls.size());
				if (fixedThreadPool.isTerminated()) {
					break;
				}
			}
//			FileUtils.writeFile(JsonUtils.objToJson(map), path);
			System.out.println(newls.size());
			int audios = 0;
			List<Integer> iList = new ArrayList<>();
			fixedThreadPool = Executors.newFixedThreadPool(scheme.getXMLYThread_Limit_Size());
			for (int i = 0; i < newls.size(); i++) {
				int fonum = i;
				fixedThreadPool.execute(new Runnable() {
					public void run() {
						String albumId = newls.get(fonum);
						try {
							FileUtils.doingDB(file, albumId, map, scheme.getSchemenum());
							Thread.sleep(50);
							String numstr = insertNewZJ(albumId,"1",false);
							System.out.println(albumId+"   "+numstr);
							if (numstr!=null) {
								iList.add(Integer.valueOf(numstr));
								String id = insertNewZJ(albumId, numstr, true);
								newmap.put(albumId, id);
								etlProcess.makeNewAlbum(id);
								FileUtils.didDB(file, albumId);
							}
						} catch (Exception e) {
							System.out.println("http://mobile.ximalaya.com/mobile/v1/album?albumId="+albumId+"&device=android&isAsc=true&pageId=1&pageSize=1&pre_page=0&source=5");
							e.printStackTrace();
						}
					}
				});
			}
			fixedThreadPool.shutdown();
			while (true) {
				Thread.sleep(10000);
				System.out.println(iList.size());
				if (fixedThreadPool.isTerminated()) {
					break;
				}
			}
			if (iList!=null && iList.size()>0) {
				for (Integer integer : iList) {
					if (integer!=null) {
						audios += integer;
					}
				}
			}
			System.out.println(audios);
		} catch (Exception e) {
			e.printStackTrace();
		}
//		map.clear();
//		map.put("newmap", newmap);
//		map.put("updatamap", updatemap);
//		return map;
	}
	
	@SuppressWarnings("unchecked")
	private String insertNewZJ(String albumId, String pageSize, boolean toInsert) {
		try {
			Document doc = null;
			Map<String, Object> alm = null;
			try {
				doc = HttpUtils.makeXMLYJsoup("http://mobile.ximalaya.com/mobile/v1/album?albumId="+albumId+"&device=android&isAsc=true&pageId=1&pageSize="+pageSize+"&pre_page=0&source=5");
			    String albumstr = doc.body().html();
			    albumstr = CleanDataUtils.CleanDescnStr(albumstr, "\"intro\":\"", "\",\"shortIntro\":\"", "\",\"introRich\":\"", "\",\"shortIntroRich\":\"", "\",\"tags\":\"", "\",\"tracks\":");
			    alm = (Map<String, Object>) JsonUtils.jsonToObj(albumstr, Map.class);
			} catch (Exception e) {
				String albumstr = httpClientService.doGet("http://mobile.ximalaya.com/mobile/v1/album?albumId="+albumId+"&device=android&isAsc=true&pageId=1&pageSize="+pageSize);
				albumstr = CleanDataUtils.CleanDescnStr(albumstr, "\"intro\":\"", "\",\"shortIntro\":\"", "\",\"introRich\":\"", "\",\"shortIntroRich\":\"", "\",\"tags\":\"", "\",\"tracks\":");
				alm = (Map<String, Object>) JsonUtils.jsonToObj(albumstr, Map.class);
			}
			if (alm!=null) {
				Map<String, Object> almm = (Map<String, Object>) alm.get("data");
				Map<String, Object> tracks = (Map<String, Object>) almm.get("tracks");
				if (toInsert) {
					XMLYEtl1Process xmlyEtl1Process = new XMLYEtl1Process();
				    String id = xmlyEtl1Process.insertNewAlbum(alm,map,scheme);
				    return id;
				}
				return tracks.get("totalCount").toString();
			}
		} catch (Exception e) {
			e.getMessage();
			System.out.println("出错专辑Id    "+albumId);
		}
		return null;
	}
}
