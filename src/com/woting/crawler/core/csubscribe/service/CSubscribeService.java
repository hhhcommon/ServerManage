package com.woting.crawler.core.csubscribe.service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import com.spiritdata.framework.core.dao.mybatis.MybatisDAO;
import com.woting.crawler.core.csubscribe.persis.po.CSubscribePo;

public class CSubscribeService {
	@Resource(name = "defaultDAO_CM")
	private MybatisDAO<CSubscribePo> csubcribeDao;
	
	@PostConstruct
	public void initParam() {
		csubcribeDao.setNamespace("A_CSUBSCRIBE");
	}
	
	public void insertCSubscribe(CSubscribePo cPo) {
		csubcribeDao.insert(cPo);
	}
}
