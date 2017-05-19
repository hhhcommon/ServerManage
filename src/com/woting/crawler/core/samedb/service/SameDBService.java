package com.woting.crawler.core.samedb.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import com.spiritdata.framework.core.dao.mybatis.MybatisDAO;
import com.woting.crawler.core.samedb.persis.po.SameDBPo;

public class SameDBService {
	@Resource(name = "defaultDAO_CM")
	private MybatisDAO<SameDBPo> sameDBDao;
	
	@PostConstruct
	public void initParam() {
		sameDBDao.setNamespace("A_SAMEDB");
	}
	
	public List<SameDBPo> getSameDBs(String resId, String resTableName, String sameId) {
		Map<String, Object> m = new HashMap<>();
		if (resId!=null) m.put("resId", resId);
		if (resTableName!=null) m.put("resTableName", resTableName);
		if (sameId!=null) m.put("sameId", sameId);
		return sameDBDao.queryForList(m);
	}
	
	public void insert(SameDBPo sameDBPo) {
		sameDBDao.insert(sameDBPo);
	}
	
	public void update(SameDBPo sameDBPo) {
		sameDBDao.update(sameDBPo);
	}
}
