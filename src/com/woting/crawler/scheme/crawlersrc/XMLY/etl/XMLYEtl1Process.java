package com.woting.crawler.scheme.crawlersrc.XMLY.etl;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.spiritdata.framework.core.cache.SystemCache;
import com.spiritdata.framework.ext.spring.redis.RedisOperService;
import com.woting.crawler.CrawlerConstants;
import com.woting.crawler.core.etl.model.Etl1Process;
import com.woting.crawler.core.scheme.model.Scheme;
import com.woting.crawler.scheme.crawlersrc.crawler.Crawler;
import com.woting.crawler.scheme.utils.ConvertUtils;
import com.woting.crawler.scheme.utils.RedisUtils;

public class XMLYEtl1Process {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private Etl1Process etl1Process;
	private Map<String, Object> catemap;
	private List<Map<String, Object>> albumlist;
	private List<Map<String, Object>> audiolist;
	private boolean loadOk = true;
	private long begintime;
	private RedisOperService rs;
	
	public XMLYEtl1Process(Etl1Process etl1Process) {
		Scheme scheme = (Scheme) SystemCache.getCache(CrawlerConstants.SCHEME).getContent();
		rs = new RedisOperService(scheme.getJedisConnectionFactory(),1);
		begintime = System.currentTimeMillis();
		this.etl1Process = etl1Process;
		catemap = RedisUtils.getOrigData(rs, "XMLY_FastGetCategoryId_" + etl1Process.getEtlnum());
		albumlist = RedisUtils.getOrigDataList(rs, "XMLY_Album_" + etl1Process.getEtlnum());
		audiolist = RedisUtils.getOrigDataList(rs, "XMLY_Audio_" + etl1Process.getEtlnum());
		if (catemap == null || albumlist == null || audiolist == null)
			loadOk = false;
		logger.info("喜玛拉雅第一次转换加载Redis数据是否成功[{}]", loadOk);
		logger.info("喜玛拉雅第一次加载Redis数据耗时[{}]秒", (System.currentTimeMillis() - begintime) / 1000);
	}

	public Map<String, Object> makeXMLYOrigDataList() {
		Map<String, Object> map = new HashMap<String, Object>();
		Map<String, Object> lackm = new HashMap<String, Object>();
		for (Map<String, Object> m1 : audiolist) {
			boolean isok = false;
			String alid = m1.get("albumId") + "";
			for (Map<String, Object> m2 : albumlist) {
				if (alid.equals(m2.get("albumId")))
					isok = true;
			}
			if (!isok) {
				String url = m1.get("visitUrl") + "";
				url = url.replace(url.substring(url.indexOf("sound"), url.length()), "album/" + alid);
				if (!lackm.containsKey(alid)) {
					lackm.put(alid, url);
					try {
						new Crawler().reloadUrl(url, Jsoup.connect(url).ignoreContentType(true).get().toString().getBytes(), etl1Process.getEtlnum());
					} catch (IOException e) {
						e.printStackTrace();
						continue;
					}
				}
			}
		}
		albumlist = RedisUtils.getOrigDataList(rs, "XMLY_Album_" + etl1Process.getEtlnum());
		audiolist = RedisUtils.getOrigDataList(rs, "XMLY_Audio_" + etl1Process.getEtlnum());
		if (loadOk) {
			logger.info("喜玛拉雅开始第一次转换");
			for (Map<String, Object> m : audiolist) {
				String cateid = catemap.get(m.get("categoryName")) + "";
				m.put("categoryId", cateid);
			}
			map.put("audiolist", ConvertUtils.convert2Aludio(audiolist, "喜马拉雅"));
			for (Map<String, Object> m : albumlist) {
				String cateid = catemap.get(m.get("categoryName")) + "";
				m.put("categoryId", cateid);
			}
			map.put("albumlist", ConvertUtils.convert2Album(albumlist, "喜马拉雅"));
		}
		return map;
	}
}
