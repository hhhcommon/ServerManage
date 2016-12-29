package com.woting.crawler.scheme.crawlersrc.KL.crawler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.spiritdata.framework.core.cache.SystemCache;
import com.spiritdata.framework.ext.spring.redis.RedisOperService;
import com.spiritdata.framework.util.JsonUtils;
import com.woting.crawler.CrawlerConstants;
import com.woting.crawler.core.scheme.model.Scheme;
import com.woting.crawler.scheme.utils.RedisUtils;

public class KLSnapShoot {
	private int num = 0;

	@SuppressWarnings("unchecked")
	public void beginSearch() {
		try {
			Document doc = Jsoup.connect("http://www.kan8kan.com/webapi/category/list?fid=0").timeout(10000).ignoreContentType(true).get();
			Map<String, Object> cataall = (Map<String, Object>) JsonUtils.jsonToObj(doc.body().html(), Map.class);
			cataall = (Map<String, Object>) cataall.get("result");
			List<Map<String, Object>> dataall = (List<Map<String, Object>>) cataall.get("dataList");
			if (dataall != null && dataall.size() > 0) {
				for (Map<String, Object> ms : dataall) {
					new Thread(new Runnable() {
						public void run() {
							getCate(ms.get("categoryId") + "", ms.get("categoryName") + "");
						}
					}).start();
				}
			}
			int numm = 0;
			while (true) {
				Thread.sleep(10000);
				System.out.println("查询的专辑数目" + num);
				if (num == numm) {
					break;
				} else {
					numm = num;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	public void getCate(String id, String name) {
		try {
			int pageNum = 1;
			for (int i = 1; i <= pageNum; i++) {
				Document doc = Jsoup.connect("http://www.kan8kan.com/webapi/resource/search?cid=" + id+ "&rtype=20000&sorttype=HOT_RANK_DESC&pagesize=500&pagenum=" + i).timeout(10000).ignoreContentType(true).post();
				if (doc != null) {
					Map<String, Object> catamap = new HashMap<>();
					String jsonstr = doc.body().html();
					jsonstr = jsonstr.replace("\"", "\"");
					Map<String, Object> m = (Map<String, Object>) JsonUtils.jsonToObj(jsonstr, Map.class);
					if (m != null) {
						Scheme scheme = (Scheme) SystemCache.getCache(CrawlerConstants.SCHEME).getContent();
						RedisOperService rs = new RedisOperService(scheme.getJedisConnectionFactory(), scheme.getRedisSnapShootDB());
						m = (Map<String, Object>) m.get("result");
						pageNum = (int) m.get("totalPages");
						List<Map<String, Object>> dataList = (List<Map<String, Object>>) m.get("dataList");
						if (dataList != null && dataList.size() > 0) {
							for (Map<String, Object> map : dataList) {
								String albumId = map.get("id") + "";
								try {
									doc = Jsoup.connect("http://www.kan8kan.com/webapi/albumdetail/get?albumid=" + albumId).timeout(10000).ignoreContentType(true).get();
									String albumstr = doc.body().html();
									Map<String, Object> mm = (Map<String, Object>) JsonUtils.jsonToObj(albumstr, Map.class);
									mm = (Map<String, Object>) mm.get("result");
									catamap.put(mm.get("categoryId") + "", mm.get("categoryName"));
									if (RedisUtils.exeitsSnapShootInfo(rs, "KL::"+albumId)) {
										String str = RedisUtils.getSnapShootInfo(rs, "KL::"+albumId);
										if (!str.contains(id + "::" + name)) {
											RedisUtils.addSnapShootInfo(rs, "KL::"+albumId, str + "," + id + "::" + name);
										}
									} else {
										RedisUtils.addSnapShootInfo(rs, "KL::"+albumId, id + "::" + name);
									}
									num++;
								} catch (Exception e) {
									e.printStackTrace();
									continue;
								}
							}
						}
						rs.close();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
