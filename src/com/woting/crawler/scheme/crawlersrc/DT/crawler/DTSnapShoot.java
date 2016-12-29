package com.woting.crawler.scheme.crawlersrc.DT.crawler;

import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.spiritdata.framework.core.cache.SystemCache;
import com.spiritdata.framework.ext.spring.redis.RedisOperService;
import com.spiritdata.framework.util.JsonUtils;
import com.woting.crawler.CrawlerConstants;
import com.woting.crawler.core.scheme.model.Scheme;
import com.woting.crawler.scheme.utils.RedisUtils;

public class DTSnapShoot {
	private int num;

	public void beginSearch() {
		try {
			Document doc = Jsoup.connect("http://www.duotin.com/webfm").ignoreContentType(true).timeout(10000).get();
			if (doc != null) {
				Elements eles = doc.select("ul[class=list category-list dt-box]");
				if (eles != null && eles.size() > 0) {
					Element ele = eles.get(0);
					eles = ele.select("a[class=pjax-url hash-url]");
					if (eles != null && eles.size() > 0) {
						for (Element el : eles) {
							String cateId = el.attr("data-hash");
							Map<String, Object> cateM = (Map<String, Object>) JsonUtils.jsonToObj(cateId, Map.class);
							String id = cateM.get("category_id") + "";
							String cateName = el.html();
//							Thread.sleep(30);
//							new Thread(new Runnable() {
//							    public void run() {
//							    	System.out.println("开启线程"+id+"::"+cateName);
							        getCate(id, cateName);
//							    }
//							}).start();
						}
					}
				}
			}
//			int numm = 0;
//			while (true) {
//				Thread.sleep(10000);
				System.out.println("查询的专辑数目" + num);
//				if (num == numm) {
//					break;
//				} else {
//					numm = num;
//				}
//			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	private void getCate(String id, String name) {
		try {
			Document doc = Jsoup.connect("http://www.duotin.com/webfm/list-f" + id + "-p1.html?pjax=true").ignoreContentType(true).timeout(10000).get();
			if (doc != null) {
				Scheme scheme = (Scheme) SystemCache.getCache(CrawlerConstants.SCHEME).getContent();
				RedisOperService rs = new RedisOperService(scheme.getJedisConnectionFactory(), scheme.getRedisSnapShootDB());
				Elements eles = doc.select("li[class=total_page]");
				if (eles != null && eles.size() > 0) {
					String page = eles.get(0).select("label").get(0).html();
					page = page.substring(page.indexOf("/") + 1, page.length());
					int pageNum = Integer.valueOf(page);
					System.out.println(name+"::"+pageNum);
					for (int i = 1; i <= pageNum; i++) {
						try {
							doc = Jsoup.connect("http://www.duotin.com/webfm/list-f" + id + "-p" + i + ".html?pjax=true").ignoreContentType(true).timeout(10000).get();
							if (doc != null) {
								Elements albums = doc.select("a[class=pjax-url content-title]");
								if (albums != null && albums.size() > 0) {
									for (Element el : albums) {
										try {
											String cats = el.attr("data-hash");
											Map<String, Object> catM = (Map<String, Object>) JsonUtils.jsonToObj(cats,Map.class);
											String albumId = catM.get("album_id") + "";
											Thread.sleep(5);
											if (RedisUtils.exeitsSnapShootInfo(rs, "DT::"+albumId)) {
												String str = RedisUtils.getSnapShootInfo(rs, "DT::"+albumId);
												if (!str.contains(id + "::" + name)) {
													RedisUtils.addSnapShootInfo(rs, "DT::"+albumId, str + "," + id + "::" + name);
												}
											} else {
												RedisUtils.addSnapShootInfo(rs, "DT::"+albumId, id + "::" + name);
											}
										} catch (Exception e) {
											e.printStackTrace();
											continue;
										} finally {
											num++;
										}
									}
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
							continue;
						}
					}
				} else {
					try {
						doc = Jsoup.connect("http://www.duotin.com/webfm/list-f" + id + "-p1.html?pjax=true").ignoreContentType(true).timeout(10000).get();
						if (doc != null) {
							Elements albums = doc.select("a[class=pjax-url content-title]");
							if (albums != null && albums.size() > 0) {
								for (Element el : albums) {
									try {
										String cats = el.attr("data-hash");
										Map<String, Object> catM = (Map<String, Object>) JsonUtils.jsonToObj(cats,Map.class);
										String albumId = catM.get("album_id") + "";
										if (RedisUtils.exeitsSnapShootInfo(rs, "DT::"+albumId)) {
											String str = RedisUtils.getSnapShootInfo(rs, "DT::"+albumId);
											if (!str.contains(id + "::" + name)) {
												RedisUtils.addSnapShootInfo(rs, "DT::"+albumId, str + "," + id + "::" + name);
											}
										} else {
											RedisUtils.addSnapShootInfo(rs, "DT::"+albumId, id + "::" + name);
										}
									} catch (Exception e) {
										e.printStackTrace();
										continue;
									} finally {
										num++;
									}
								}
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					} 
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
