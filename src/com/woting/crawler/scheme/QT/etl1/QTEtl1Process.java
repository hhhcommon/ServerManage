package com.woting.crawler.scheme.QT.etl1;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.woting.crawler.core.etl.model.Etl1Process;
import com.woting.crawler.scheme.util.ConvertUtils;
import com.woting.crawler.scheme.util.RedisUtils;

public class QTEtl1Process {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private Etl1Process etl1Process;
	private Map<String, Object> catemap;
	private List<Map<String, Object>> albumlist;
	private List<Map<String, Object>> audiolist;
	private boolean loadOk = true;
	private long begintime;

	public QTEtl1Process(Etl1Process etl1Process) {
		begintime = System.currentTimeMillis();
		this.etl1Process = etl1Process;
		catemap = RedisUtils.getOrigData("QT_ResourceIsAndCategoryId_" + etl1Process.getEtlnum());
		albumlist = RedisUtils.getOrigDataList("QT_Album_" + etl1Process.getEtlnum());
		audiolist = RedisUtils.getOrigDataList("QT_Audio_" + etl1Process.getEtlnum());
		if (catemap == null || albumlist == null || audiolist == null)
			loadOk = false;
		logger.info("蜻蜓第一次转换加载Redis数据是否成功[{}]", loadOk);
		logger.info("蜻蜓第一次加载Redis数据耗时[{}]秒", (System.currentTimeMillis() - begintime) / 1000);
	}

	public Map<String, Object> makeQTOrigDataList() {
		Map<String, Object> map = new HashMap<String, Object>();
		if (loadOk) {
			logger.info("开始蜻蜓FM第一次转换");
			for (Map<String, Object> m : audiolist) {
				String cate = catemap.get(m.get("albumId")) + "";
				if (cate.split("::").length > 1) {
					String cateid = cate.split("::")[0];
					String catename = cate.split("::")[1];
					m.put("categoryName", catename);
					m.put("categoryId", cateid);
				}
			}
			map.put("audiolist", ConvertUtils.convert2Aludio(audiolist, "蜻蜓FM"));
			for (Map<String, Object> m : albumlist) {
				String cate = catemap.get(m.get("albumId")) + "";
				if (cate.split("::").length > 1) {
					String cateid = cate.split("::")[0];
					String catename = cate.split("::")[1];
					m.put("categoryName", catename);
					m.put("categoryId", cateid);
				}
			}
			map.put("albumlist", ConvertUtils.convert2Album(albumlist, "蜻蜓FM"));
			logger.info("蜻蜓FM第一次转换结束");
		}
		return map;
	}
}
