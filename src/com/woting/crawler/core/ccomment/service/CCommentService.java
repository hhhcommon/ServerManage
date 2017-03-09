package com.woting.crawler.core.ccomment.service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import com.spiritdata.framework.core.dao.mybatis.MybatisDAO;
import com.woting.crawler.core.ccomment.persis.po.CCommentPo;

public class CCommentService {
	@Resource(name = "defaultDAO_CM")
	private MybatisDAO<CCommentPo> ccommentDao;
	
	@PostConstruct
	public void initParam() {
		ccommentDao.setNamespace("A_CCOMMENT");
	}
	
	public void insertCComment(CCommentPo cPo) {
		ccommentDao.insert(cPo);
	}
}
