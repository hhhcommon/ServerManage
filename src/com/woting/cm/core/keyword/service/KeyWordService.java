package com.woting.cm.core.keyword.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import com.spiritdata.framework.core.dao.mybatis.MybatisDAO;
import com.woting.cm.core.keyword.persis.po.KeyWordPo;
import com.woting.cm.core.keyword.persis.po.KwResPo;


public class KeyWordService {
	
	@Resource(name = "defaultDAO")
	private MybatisDAO<KeyWordPo> keyWordDao;
	@Resource(name = "defaultDAO")
	private MybatisDAO<KwResPo> kwReSDao;
	
	@PostConstruct
	public void initParam() {
		keyWordDao.setNamespace("A_KEYWORD");
		kwReSDao.setNamespace("A_KWRES");
	}
	
	public boolean KeyWordIsNull(String kwName) {
		if (kwName!=null) {
			List<KeyWordPo> kws = keyWordDao.queryForList("getKeyWord", kwName);
			if (kws!=null && kws.size()>0) return false;
			else return true;
		}
		return false;
	}
	
	public void insertKeyWords(List<KeyWordPo> kws) {
		if (kws!=null && kws.size()>0) {
			Map<String, Object> m = new HashMap<>();
			m.put("list", kws);
			keyWordDao.insert("insertKeyWordList", m);
		}
	}
	
	public void insertKwRefs(List<KwResPo> krs) {
		if (krs!=null && krs.size()>0) {
			Map<String, Object> m = new HashMap<>();
			m.put("list", krs);
			kwReSDao.insert("insertKwResList", m);
		}
	}
}
