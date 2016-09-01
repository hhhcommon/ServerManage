package com.woting.crawler.scheme.crawlersrc.KL.etl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.woting.crawler.core.etl.model.Etl1Process;
import com.woting.crawler.scheme.utils.RedisUtils;

public class KLEtl1Process {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private Etl1Process etl1Process;
	private List<Map<String, Object>> categorylist;
	private List<Map<String, Object>> albumlist;
	private List<Map<String, Object>> audiolist;
	private boolean loadOk = true;
	private long begintime;
	
	public KLEtl1Process(Etl1Process etl1Process){
		begintime = System.currentTimeMillis();
		this.etl1Process = etl1Process;
//		categorylist = RedisUtils.getOrigData("QT_Category_"+etl1Process.getEtlnum());
		albumlist = RedisUtils.getOrigDataList("QT_Album_"+etl1Process.getEtlnum());
		audiolist = RedisUtils.getOrigDataList("QT_Audio_"+etl1Process.getEtlnum());
		if(categorylist==null || albumlist==null || audiolist ==null)
			loadOk = false;
		logger.info("酷狗第一次转换加载Redis数据是否成功[{}]", loadOk);
		logger.info("酷狗第一次加载Redis数据耗时[{}]秒",(System.currentTimeMillis()-begintime)/1000);
	}

	public List<Map<String, Object>> makeKLOrigDataList(){
		if (loadOk) {
			logger.info("酷狗开始第一次转换");
			Map<String, Object> dictmap = new HashMap<String,Object>();
			for (Map<String, Object> m : categorylist) {
				dictmap.put(m.get("cateId")+"", m.get("cateName"));
			}
		}
		return null;
	}
}
