package com.woting.crawler.scheme.crawlersrc.QT.etl;

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

public class QTEtl1Process {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private Etl1Service etl1Service;
	// private Etl1Process etl1Process;
	private Map<String, Object> catemap;
	private List<Map<String, Object>> albumlist;
	private List<Map<String, Object>> audiolist;
	private RedisOperService rs;
	private boolean loadOk = true;
	private String crawlerNum;

	public QTEtl1Process(Etl1Process etl1Process) {
		etl1Service = (Etl1Service) SpringShell.getBean("etl1Service");
		Scheme scheme = (Scheme) SystemCache.getCache(CrawlerConstants.SCHEME).getContent();
		this.rs = new RedisOperService(scheme.getJedisConnectionFactory(), 1);
		this.crawlerNum = scheme.getSchemenum();
		catemap = RedisUtils.getOrigData(rs, "QT_ResourceIdAndCategoryId_" + etl1Process.getEtlnum());
	}

	public void makeQTOrigDataList() {
		long qtalbumSize = RedisUtils.getOrigDataListSize(rs, "QT_Album_" + crawlerNum);
		long qtaudioSize = RedisUtils.getOrigDataListSize(rs, "QT_Audio_" + crawlerNum);
		Map<String, Object> map = new HashMap<String, Object>();
		logger.info("开始蜻蜓FM第一次转换");
		if (qtalbumSize > 0) {
			int num = Integer.valueOf(qtalbumSize / 1000 + "") + 1;
			for (int i = 0; i < num; i++) {
				albumlist = RedisUtils.getOrigDataList(rs, "QT_Album_" + crawlerNum, i * 1000, (i + 1) * 1000);
				if (loadOk) {
					for (Map<String, Object> m : albumlist) {
						String cate = catemap.get(m.get("albumId")) + "";
						if (cate.split("::").length > 1) {
							String cateid = cate.split("::")[0];
							String catename = cate.split("::")[1];
							m.put("categoryName", catename);
							m.put("categoryId", cateid);
						}
					}
					map = new HashMap<>();
					map.put("albumlist", ConvertUtils.convert2Album(albumlist, "蜻蜓"));
					etl1Service.insertSqlAlbumAndAudio(map);
				}
			}
		}
		if (qtaudioSize > 0) {
			int num = Integer.valueOf(qtaudioSize / 1000 + "") + 1;
			for (int i = 0; i < num; i++) {
				audiolist = RedisUtils.getOrigDataList(rs, "QT_Audio_" + crawlerNum, i * 1000, (i + 1) * 1000);
				for (Map<String, Object> m : audiolist) {
					String cate = catemap.get(m.get("albumId")) + "";
					if (cate.split("::").length > 1) {
						String cateid = cate.split("::")[0];
						String catename = cate.split("::")[1];
						m.put("categoryName", catename);
						m.put("categoryId", cateid);
					}
				}
				map = new HashMap<>();
				map.put("audiolist", ConvertUtils.convert2Aludio(audiolist, "蜻蜓"));
				etl1Service.insertSqlAlbumAndAudio(map);
			}
		}
		rs.close();
	}
}
