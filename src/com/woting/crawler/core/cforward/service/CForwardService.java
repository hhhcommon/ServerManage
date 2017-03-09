package com.woting.crawler.core.cforward.service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import com.spiritdata.framework.core.dao.mybatis.MybatisDAO;
import com.woting.crawler.core.cforward.persis.po.CForwardPo;

public class CForwardService {
	@Resource(name = "defaultDAO_CM")
	private MybatisDAO<CForwardPo> cforwardDao;
	
	@PostConstruct
	public void initParam() {
		cforwardDao.setNamespace("A_CFORWARD");
	}
	
	public void insertCForward(CForwardPo cPo) {
		cforwardDao.insert(cPo);
	}
}
