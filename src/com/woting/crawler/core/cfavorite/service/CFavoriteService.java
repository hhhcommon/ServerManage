package com.woting.crawler.core.cfavorite.service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import com.spiritdata.framework.core.dao.mybatis.MybatisDAO;
import com.woting.crawler.core.cfavorite.persis.po.CFavoritePo;

public class CFavoriteService {
	@Resource(name = "defaultDAO_CM")
	private MybatisDAO<CFavoritePo> cFavoriteDao;
	
	@PostConstruct
	public void initParam() {
		cFavoriteDao.setNamespace("A_CFAVORITE");
	}
	
	public void insertCFavorite(CFavoritePo cPo) {
		cFavoriteDao.insert(cPo);
	}
}
