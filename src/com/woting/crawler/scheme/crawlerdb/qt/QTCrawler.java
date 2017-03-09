package com.woting.crawler.scheme.crawlerdb.qt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import com.spiritdata.framework.util.JsonUtils;
import com.woting.crawler.scheme.crawlerdb.crawler.EtlProcess;
import com.woting.crawler.scheme.utils.FileUtils;

public class QTCrawler {
	private Map<String, Object> map = new HashMap<>();
	private Map<String, Object> newmap = new HashMap<>();
	int httpclientnums = 0;
	String path = "/opt/CrawlerCS/QTREF.txt";
	
	@SuppressWarnings("unchecked")
	public QTCrawler() {
		String str = FileUtils.readFile(path);
		if (str!=null && str.length()>0) {
			map = (Map<String, Object>) JsonUtils.jsonToObj(str, Map.class);
		}
	}
	
	@SuppressWarnings("unchecked")
	public void beginCrawler() {
		List<String> newls = new ArrayList<>();
		Map<String, Object> catemap = new HashMap<>();
		try {
			Document doc = Jsoup.connect("http://i.qingting.fm/wapi/categories").ignoreContentType(true).timeout(10000).get();
			String catestr = doc.body().html();
			Map<String, Object> qtcatemap = (Map<String, Object>) JsonUtils.jsonToObj(catestr, Map.class);
			List<Map<String, Object>> cateLs = (List<Map<String, Object>>) qtcatemap.get("data");
			ExecutorService fixedThreadPool = Executors.newFixedThreadPool(cateLs.size());
			if (cateLs!=null && cateLs.size()>0) {
				for (Map<String, Object> cam : cateLs) {
					if (cam!=null && cam.size()>0) {
						try {
							if (cam.get("type").equals("ondemand")) {
								fixedThreadPool.execute(new Runnable() {
									public void run() {
										String cateId = cam.get("id").toString();
										String cateName = cam.get("name").toString();
										catemap.put(cateId, cateName);
										try {
											Document doc = Jsoup.connect("http://i.qingting.fm/wapi/flip/categories/"+cateId+"/channels/attrs/0/page/1/pagesize/1").ignoreContentType(true).timeout(100000).get();
											String str = doc.body().html();
											Map<String, Object> catepage = (Map<String, Object>) JsonUtils.jsonToObj(str, Map.class);
											catepage = (Map<String, Object>) catepage.get("data");
											int allcount = Integer.valueOf(catepage.get("total").toString());
											int num = allcount/50+1;
											for (int i = 1; i <= num; i++) {
												try {
													doc = Jsoup.connect("http://i.qingting.fm/wapi/flip/categories/"+cateId+"/channels/attrs/0/page/"+i+"/pagesize/50").ignoreContentType(true).timeout(100000).get();
													String pagestr = doc.body().html();
													Map<String, Object> pagemap = (Map<String, Object>) JsonUtils.jsonToObj(pagestr, Map.class);
													pagemap = (Map<String, Object>) pagemap.get("data");
													List<Map<String, Object>> pagelist = (List<Map<String, Object>>) pagemap.get("channels");
													if (pagelist!=null && pagelist.size()>0) {
														for (Map<String, Object> m : pagelist) {
															String albumId = m.get("id").toString();
															synchronized (map) {
																if (map.containsKey(albumId)) {
																	String cates = map.get(albumId).toString();
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
													e.printStackTrace();
													continue;
												}
											}
										} catch (Exception e) {
											e.printStackTrace();
										}
									}
								});
							}
							
						} catch (Exception e) {
							e.printStackTrace();
							continue;
						}
					}
				}
			}
			fixedThreadPool.shutdown();
			while (true) {
				Thread.sleep(10000);
				System.out.println("本次抓取遍历到专辑数:"+map.size()+"   新增专辑数:"+newls.size());
				if (fixedThreadPool.isTerminated()) {
					break;
				}
			}
			FileUtils.writeFile(JsonUtils.objToJson(map), path);
			System.out.println(newls.size());
			EtlProcess etlProcess = new EtlProcess();
			List<Integer> iList = new ArrayList<>();
			int audiothreads = newls.size()/3000+1;
			fixedThreadPool = Executors.newFixedThreadPool(audiothreads);
			for (int i = 1; i <= audiothreads; i++) {
				int fonum = i;
				fixedThreadPool.execute(new Runnable() {
					public void run() {
						List<String> ls2 = newls.subList((fonum-1)*3000, fonum*3000>newls.size()?newls.size():fonum*3000);
						for (String albumId : ls2) {
							try {
								Thread.sleep(50);
								String numstr = insertNewZJ(albumId,"1",false);
								System.out.println(albumId+"   "+numstr);
								if (numstr!=null) {
									iList.add(Integer.valueOf(numstr));
									String id = insertNewZJ(albumId, numstr, true);
									newmap.put(albumId, id);
									etlProcess.makeNewAlbum(id);
								}
							} catch (Exception e) {
								System.out.println("http://mobile.ximalaya.com/mobile/v1/album?albumId="+albumId+"&device=android&isAsc=true&pageId=1&pageSize=1&pre_page=0&source=5");
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
				System.out.println(iList.size());
				if (fixedThreadPool.isTerminated()) {
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	protected String insertNewZJ(String albumId, String pageSize, boolean toInsert) {
		try {
			if (!toInsert) {
				Document doc = Jsoup.connect("http://i.qingting.fm/wapi/channels/"+albumId).ignoreContentType(true).timeout(100000).get();
				String albuminfo = doc.body().html();
				Map<String, Object> albummap = (Map<String, Object>) JsonUtils.jsonToObj(albuminfo, Map.class);
				if (albummap!=null && albummap.size()>0) {
					albummap = (Map<String, Object>) albummap.get("data");
					int mediasize = Integer.valueOf(albummap.get("program_count").toString());
					if (mediasize>0) return mediasize+"";
					else return null;
				}
			} else {
				Document doc = Jsoup.connect("http://api2.qingting.fm/v6/media/channelondemands/"+albumId).ignoreContentType(true).timeout(100000).get();
				String albuminfo = doc.body().html();
				Map<String, Object> albummap = (Map<String, Object>) JsonUtils.jsonToObj(albuminfo, Map.class);
				albummap = (Map<String, Object>) albummap.get("data");
				Map<String, Object> usermap = (Map<String, Object>) albummap.get("detail");
				int size = 1000;
				try {size = Integer.valueOf(pageSize);} catch (Exception e) {}
				int num = size/50+1;
				List<Map<String, Object>> audios = new ArrayList<>();
				for (int i = 1; i <= num; i++) {
					doc = Jsoup.connect("http://api2.qingting.fm/v6/media/channelondemands/"+albumId+"/programs/order/0/curpage/"+i+"/pagesize/50").ignoreContentType(true).timeout(50000).get();
					String mediaInfo = doc.body().html();
					Map<String, Object> audiomap = (Map<String, Object>) JsonUtils.jsonToObj(mediaInfo, Map.class);
					List<Map<String, Object>> audiols = (List<Map<String, Object>>) audiomap.get("data");
					if (audiols!=null && audiols.size()>0) {
						audios.addAll(audiols);
					}
				}
				QTEtl1Process qtEtl1Process = new QTEtl1Process();
				String id = qtEtl1Process.insertNewAlbum(map, albummap, usermap, audios);
				return id;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
