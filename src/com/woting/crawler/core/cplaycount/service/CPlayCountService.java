package com.woting.crawler.core.cplaycount.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import com.spiritdata.framework.core.dao.mybatis.MybatisDAO;
import com.woting.crawler.core.cperson.persis.po.CPersonPo;
import com.woting.crawler.core.cplaycount.persis.po.CPlayCountPo;

public class CPlayCountService {
	@Resource(name = "defaultDAO_CM")
	private MybatisDAO<CPlayCountPo> cplayCountDao;
	
	@PostConstruct
	public void initParam() {
		cplayCountDao.setNamespace("A_CPLAYCOUNT");
	}
	
	/**
	 * 
	 * @param resIds 多个id之间逗号隔开
	 * @param resTableName
	 * @return
	 */
	public List<CPlayCountPo> getCPlayCountPos(String resIds, String resTableName) {
		Map<String, Object> m = new HashMap<>();
		m.put("whereSql", " resId in ("+resIds+")");
		m.put("resTableName", resTableName);
		List<CPlayCountPo> cps = cplayCountDao.queryForList("getList", m);
		if (cps!=null && cps.size()>0) {
			return cps;
		}
		return null;
	}
	
	public CPlayCountPo getCPlayCountPo(String resId, String resTableName) {
		Map<String, Object> m = new HashMap<>();
		m.put("resId", resId);
		m.put("resTableName", resTableName);
		List<CPlayCountPo> cps = cplayCountDao.queryForList("getList", m);
		if (cps!=null && cps.size()>0) {
			return cps.get(0);
		}
		return null;
	}
	
	public void insertCPlayCount(CPlayCountPo cPo) {
		cplayCountDao.insert(cPo);
	}
}
