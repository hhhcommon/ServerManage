package com.woting.crawler.core.cplaycount.service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import com.spiritdata.framework.core.dao.mybatis.MybatisDAO;
import com.woting.crawler.core.cplaycount.persis.po.CPlayCountPo;

public class CPlayCountService {
	@Resource(name = "defaultDAO_CM")
	private MybatisDAO<CPlayCountPo> cplayCountDao;
	
	@PostConstruct
	public void initParam() {
		cplayCountDao.setNamespace("A_CPLAYCOUNT");
	}
	
	public void insertCPlayCount(CPlayCountPo cPo) {
		cplayCountDao.insert(cPo);
	}
}
