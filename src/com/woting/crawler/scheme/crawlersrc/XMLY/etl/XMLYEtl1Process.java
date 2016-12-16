package com.woting.crawler.scheme.crawlersrc.XMLY.etl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.spiritdata.framework.core.cache.SystemCache;
import com.spiritdata.framework.ext.spring.redis.RedisOperService;
import com.woting.crawler.CrawlerConstants;
import com.woting.crawler.core.etl.model.Etl1Process;
import com.woting.crawler.core.etl.service.Etl1Service;
import com.woting.crawler.core.scheme.model.Scheme;
import com.woting.crawler.ext.SpringShell;
import com.woting.crawler.scheme.utils.ConvertUtils;
import com.woting.crawler.scheme.utils.RedisUtils;

public class XMLYEtl1Process {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private Map<String, Object> catemap;
	private List<Map<String, Object>> albumlist;
	private List<Map<String, Object>> audiolist;
	private RedisOperService rs;
	private String crawlerNum;
	private Etl1Service etl1Service;

	public XMLYEtl1Process(Etl1Process etl1Process) {
		Scheme scheme = (Scheme) SystemCache.getCache(CrawlerConstants.SCHEME).getContent();
		rs = new RedisOperService(scheme.getJedisConnectionFactory(), scheme.getRedisDB());
		this.crawlerNum = scheme.getSchemenum();
		this.etl1Service = (Etl1Service) SpringShell.getBean("etl1Service");
		catemap = RedisUtils.getOrigData(rs, "XMLY_FastGetCategoryId_" + this.crawlerNum);
	}

	public void makeXMLYOrigDataList() {
		logger.info("开始第一次喜马拉雅数据抓取转换");
		long xmlyalbumSize = RedisUtils.getOrigDataListSize(rs, "XMLY_Album_" + crawlerNum);
		long xmlyaudioSize = RedisUtils.getOrigDataListSize(rs, "XMLY_Audio_" + crawlerNum);
		if (xmlyalbumSize > 0) {
			Map<String, Object> map = new HashMap<String, Object>();
			int num = Integer.valueOf(xmlyalbumSize / 1000 + "") + 1;
			for (int i = 0; i < num; i++) {
				albumlist = RedisUtils.getOrigDataList(rs, "XMLY_Album_" + crawlerNum, i * 1000, (i + 1) * 1000);
				for (Map<String, Object> m : albumlist) {
					String cateid = catemap.get(m.get("categoryName")) + "";
					m.put("categoryId", cateid);
				}
				map = new HashMap<>();
				map.put("albumlist", ConvertUtils.convert2Album(albumlist, "喜马拉雅"));
				etl1Service.insertSqlAlbumAndAudio(map);
			}
		}
		if (xmlyaudioSize > 0) {
			Map<String, Object> map = new HashMap<String, Object>();
			int num = Integer.valueOf(xmlyaudioSize / 1000 + "") + 1;
			for (int i = 0; i < num; i++) {
				audiolist = RedisUtils.getOrigDataList(rs, "XMLY_Audio_" + crawlerNum, i * 1000, (i + 1) * 1000);
				for (Map<String, Object> m : audiolist) {
					String cateid = catemap.get(m.get("categoryName")) + "";
					m.put("categoryId", cateid);
				}
				map = new HashMap<>();
				map.put("audiolist", ConvertUtils.convert2Aludio(audiolist, "喜马拉雅"));
				etl1Service.insertSqlAlbumAndAudio(map);
			}
		}
		rs.close();
	}
}
