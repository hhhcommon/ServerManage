package com.woting.crawler.scheme.crawlerdb.qt;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.spiritdata.framework.util.JsonUtils;
import com.woting.crawler.core.album.service.AlbumService;
import com.woting.crawler.core.record.persis.po.RecordPo;
import com.woting.crawler.core.record.service.RecordService;
import com.woting.crawler.core.scheme.model.Scheme;
import com.woting.crawler.ext.SpringShell;
import com.woting.crawler.scheme.utils.RedisUtils;

public class QTCrawler {
	private Logger logger = LoggerFactory.getLogger(QTCrawler.class);
	private Map<String, Object> map = new HashMap<>();
	int httpclientnums = 0;
	String path = null;
	private Scheme scheme;
	private AlbumService albumService;
	private boolean isOk = false;
	private List<Integer> iList = new ArrayList<>();
	private long begTime;
	
	public QTCrawler() {
		this.begTime = System.currentTimeMillis();
		this.scheme = new Scheme();
		this.isOk = true;
		albumService = (AlbumService) SpringShell.getBean("albumService");
		this.scheme = new Scheme();
		Set<String> sets = RedisUtils.keys("connectionFactory", 1, "LOADCRAWLERDB:QT_ALBUM*");
		if (sets!=null && sets.size()>0) {
			for (String set : sets) {
				String val = RedisUtils.get("connectionFactory", 1, set);
				if (val!=null) {
					try {
						long ctime = Long.valueOf(val);
						String[] vals = set.split(":");
						String id = vals[1];
						if (System.currentTimeMillis()-ctime>(10*60*1000)) {
							albumService.removeAlbumById(id);
							RedisUtils.delete("connectionFactory", 1, set);
						}
					} catch (Exception e) {}
				}
			}
		}
		sets = RedisUtils.keys("connectionFactory", 1, "*QT_ALBUM*");
		if (sets!=null && sets.size()>0) {
			for (String set : sets) {
				String[] vals = set.split("_");
				String id = vals[2];
				map.put(id, null);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public void beginCrawler() {
		if (!isOk) {
			logger.info("蜻蜓抓取启动失败");
			return ;
		}
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
																	String cates = "";
																	if(map.get(albumId)!=null) 
																		cates = map.get(albumId).toString();
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
			System.out.println("待新增专辑"+newls.size());
//			EtlProcess etlProcess = new EtlProcess();
			fixedThreadPool = Executors.newFixedThreadPool(scheme.getQTThread_Limit_Size());
			for (int i = 0; i < newls.size(); i++) {
				int fonum = i;
				fixedThreadPool.execute(new Runnable() {
					public void run() {
						String albumId = newls.get(fonum);
						try {
							Thread.sleep(50);
//							FileUtils.doingDB(file, albumId, map, scheme.getSchemenum());
							String numstr = insertNewZJ(albumId,"1",false);
							System.out.println(albumId+"   "+numstr);
							if (numstr!=null) {
								iList.add(Integer.valueOf(numstr));
								String id = insertNewZJ(albumId, numstr, true);
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
			}
			fixedThreadPool.shutdown();
			int audios = 0;
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
			RecordService recordService = (RecordService) SpringShell.getBean("recordService");
			RecordPo rPo = new RecordPo();
			rPo.setBeginTime(new Timestamp(begTime));
			long endTime = System.currentTimeMillis();
			rPo.setEndTime(new Timestamp(endTime));
			rPo.setDuration((int)(endTime-begTime));
			rPo.setRecordType("QT_ADD");
			rPo.setDescn("定时扫描，蜻蜓新增内容,新增专辑 "+ iList.size() +",  新增节目 "+ audios);
			rPo.setRecordCount(audios);
			recordService.insertRecord(rPo);
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
				int num = size/10+1;
				List<Map<String, Object>> audios = new ArrayList<>();
				for (int i = 1; i <= num; i++) {
					String requrl = "http://api2.qingting.fm/v6/media/channelondemands/"+albumId+"/programs/order/0/curpage/"+i+"/pagesize/10";
					try {
						doc = Jsoup.connect(requrl).ignoreContentType(true).timeout(50000).get();
					} catch (Exception e) {
						System.out.println(requrl);
					}				
					String mediaInfo = doc.body().html();
					Map<String, Object> audiomap = (Map<String, Object>) JsonUtils.jsonToObj(mediaInfo, Map.class);
					List<Map<String, Object>> audiols = (List<Map<String, Object>>) audiomap.get("data");
					if (audiols!=null && audiols.size()>0) {
						audios.addAll(audiols);
					}
				}
				QTEtl1Process qtEtl1Process = new QTEtl1Process();
				String id = qtEtl1Process.insertNewAlbum(map, albummap, usermap, audios);
				iList.add(audios.size());
				return id;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
