package com.woting.crawler.scheme.crawlersrc.XMLY;

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
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import com.spiritdata.framework.ext.spring.redis.RedisOperService;
import com.spiritdata.framework.util.JsonUtils;
import com.woting.crawler.core.album.persis.po.AlbumPo;
import com.woting.crawler.ext.SpringShell;

public class XMLYCrawler {

	public void beginCrawler() {
		Map<String, Object> map = new HashMap<>();
		List<Map<String, Object>> ls = new ArrayList<>();
		Map<String, Object> catemap = new HashMap<>();
		Map<String, Object> catemap2 = new HashMap<>();
//		Scheme scheme = (Scheme) SystemCache.getCache(CrawlerConstants.SCHEME).getContent();
		JedisConnectionFactory jedisConnectionFactory = (JedisConnectionFactory) SpringShell.getBean("connectionFactory");
		RedisOperService rs = new RedisOperService(jedisConnectionFactory, 6);
		try {
			Document doc = makeXMLYJsoup("http://www.ximalaya.com/dq/all/");
			Elements eles = doc.select("li[cname]");
			if (eles!=null && eles.size()>0) {
				for (Element element : eles) {
					String cid = element.attr("cid");
					String cateName = element.select("a").get(0).html();
					String cateValue = rs.get("XMLY:CateId"+cid);
					if (cateValue != null) {
						cateValue = rs.get("XMLY:CateId"+cid);
						if(!cateValue.contains(cateName)) rs.set("XMLY:CateId"+cid, cateName);
					} else rs.set("XMLY:CateId"+cid, cateName);
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
						String cid = key;
						Elements els = element.select("a[class=tagBtn]");
						if (els!=null && els.size()>0) {
							for (Element element2 : els) {
								catemap2.put(catemap.get(key)+"/"+element2.select("span").get(0).html()+"cid", cid);
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
			catemap.putAll(catemap2);
			int crawlerSize = ls.size()/5+1;
			ExecutorService fixedThreadPool = Executors.newFixedThreadPool(crawlerSize);
			for (int i = 1; i <= crawlerSize; i++) {
				int fonum = i;
				fixedThreadPool.execute(new Runnable() {
					public void run() {
						List<Map<String, Object>> ls2 = ls.subList((fonum-1)*5, fonum*5>ls.size()?ls.size():fonum*5);
//						Scheme scheme = (Scheme) SystemCache.getCache(CrawlerConstants.SCHEME).getContent();
						JedisConnectionFactory jedisConnectionFactory = (JedisConnectionFactory) SpringShell.getBean("connectionFactory");
						RedisOperService rs = new RedisOperService(jedisConnectionFactory, 6);
						
						for (Map<String, Object> map2 : ls2) {
							String url = map2.get("CateUrl").toString();
							String cateName = map2.get("CateName").toString();
							try {
								for (int j = 1; j < 85; j++) {
									try {
										Thread.sleep(50);
										Document doc = makeXMLYJsoup(url+j+".ajax?_toSub_=true&toSub=true");
										String mapstr = doc.body().html();
										mapstr = mapstr.substring(0, mapstr.lastIndexOf("\",\"moduleName\""));
										mapstr = mapstr.replace("\\&quot;", "").replace("{\"html\":\"", "");
										doc = Jsoup.parse(mapstr);
										Elements eles = doc.select("div[class=discoverAlbum_item]");
										if (eles!=null && eles.size()>0) {
											for (Element element : eles) {
												String albumId = element.attr("album_id");
												String cates = rs.get("XMLY:ZJ:"+albumId);
												if (cates!=null) {
													if (!cates.contains(cateName)) {
														map.put(albumId, cates+","+cateName);
														rs.set("XMLY:ZJ:"+albumId, cates+","+cateName);
													}
												} else {
													map.put(albumId, cateName);
													rs.set("XMLY:ZJ:"+albumId, cateName);
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
								continue;
							}
						}
					}
				});
			}
			fixedThreadPool.shutdown();
			while (true) {
				Thread.sleep(10000);
				System.out.println(map.size());
				if (fixedThreadPool.isTerminated()) {
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	private void insertNewZJ(String albumId) {
		try {
			Document doc = makeXMLYJsoup("http://mobile.ximalaya.com/mobile/v1/album?albumId="+albumId+"&device=android&isAsc=true&pageId=1&pageSize=1&pre_page=0&source=5");
			String albumstr = doc.body().html();
			Map<String, Object> alm = (Map<String, Object>) JsonUtils.jsonToObj(albumstr, Map.class);
			alm = (Map<String, Object>) alm.get("data");
			alm = (Map<String, Object>) alm.get("tracks");
			String ausize = alm.get("totalCount").toString();
			doc = makeXMLYJsoup("http://mobile.ximalaya.com/mobile/v1/album?albumId="+albumId+"&device=android&isAsc=true&pageId=1&pageSize="+ausize+"&pre_page=0&source=5");
			alm = (Map<String, Object>) JsonUtils.jsonToObj(doc.body().html(), Map.class);
			alm = (Map<String, Object>) alm.get("data");
			Map<String, Object> albummap = (Map<String, Object>) alm.get("album");
			Map<String, Object> usermap = (Map<String, Object>) alm.get("user");
			Map<String, Object> tracks = (Map<String, Object>) alm.get("tracks");
			doc = makeXMLYJsoup("http://www.ximalaya.com/"+usermap.get("uid")+"/sound/"+albummap.get("albumId"));
			Elements eles = doc.select("a[class=tagBtn2]");
			String keywords = "";
			if (eles!=null && eles.size()>0) {
				for (Element ele : eles) {
					keywords += ","+ele.select("span").get(0).html();
				}
				keywords = keywords.substring(1);
			}
			if (keywords.length()<1) albummap.put("KeyWords", null);
			else albummap.put("KeyWords", keywords);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private Document makeXMLYJsoup(String url) {
		String data = System.currentTimeMillis()/1000+"";
		Document doc = null;
		try {
			doc = Jsoup.connect(url).header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.102 Safari/537.36")
		.header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
		.header("Accept-Encoding", "gzip, deflate, sdch")
		.header("Cookie", "Hm_lvt_4a7d8ec50cfd6af753c4f8aee3425070="+data+"; Hm_lpvt_4a7d8ec50cfd6af753c4f8aee3425070="+data+"; _ga=GA1.2.2074075166."+data)
		.header("Host", "www.ximalaya.com")
		.header("Connection", "keep-alive")
		.header("X-Requested-With", "XMLHttpRequest")
		.header("Referer", "http://www.ximalaya.com/explore/").ignoreContentType(true).timeout(10000).get();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return doc;
	}
}
