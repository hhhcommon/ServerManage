package com.woting.crawler.scheme.XMLY.etl1;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.woting.crawler.core.etl.model.Etl1Process;
import com.woting.crawler.scheme.util.ConvertUtils;
import com.woting.crawler.scheme.util.RedisUtils;

public class XMLYEtl1Process {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private Etl1Process etl1Process;
	private Map<String, Object> catemap;
	private List<Map<String, Object>> albumlist;
	private List<Map<String, Object>> audiolist;
	private boolean loadOk = true;
	private long begintime;

	public XMLYEtl1Process(Etl1Process etl1Process) {
		begintime = System.currentTimeMillis();
		this.etl1Process = etl1Process;
		catemap = RedisUtils.getOrigData("XMLY_FastGetCategoryId_" + etl1Process.getEtlnum());
		albumlist = RedisUtils.getOrigDataList("XMLY_Album_" + etl1Process.getEtlnum());
		audiolist = RedisUtils.getOrigDataList("XMLY_Audio_" + etl1Process.getEtlnum());
		if (catemap == null || albumlist == null || audiolist == null)
			loadOk = false;
		logger.info("喜玛拉雅第一次转换加载Redis数据是否成功[{}]", loadOk);
		logger.info("喜玛拉雅第一次加载Redis数据耗时[{}]秒", (System.currentTimeMillis() - begintime) / 1000);
	}

	public Map<String, Object> makeXMLYOrigDataList() {
		Map<String, Object> map = new HashMap<String, Object>();
		if (loadOk) {
			logger.info("喜玛拉雅开始第一次转换");
			for (Map<String, Object> m : audiolist) {
				String cateid = catemap.get(m.get("categoryName")) + "";
				m.put("categoryId", cateid);
			}
			map.put("audioliat", ConvertUtils.convert2Aludio(audiolist, "喜马拉雅FM"));
			for (Map<String, Object> m : albumlist) {
				String cateid = catemap.get(m.get("categoryName")) + "";
				m.put("categoryId", cateid);
			}
			map.put("albumlist", ConvertUtils.convert2Album(albumlist, "喜马拉雅FM"));
		}
		return map;
	}
}
