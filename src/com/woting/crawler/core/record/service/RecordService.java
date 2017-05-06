package com.woting.crawler.core.record.service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import com.spiritdata.framework.core.dao.mybatis.MybatisDAO;
import com.woting.crawler.core.record.persis.po.RecordPo;

public class RecordService {
	@Resource(name = "defaultDAO_CM")
	private MybatisDAO<RecordPo> recordDao;
	
	@PostConstruct
	public void initParam() {
		recordDao.setNamespace("A_RECORD");
	}
	
	public void insertRecord(RecordPo rPo) {
		recordDao.insert(rPo);
	}
}
