package com.woting.crawler.core.cperson.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import com.spiritdata.framework.core.dao.mybatis.MybatisDAO;
import com.woting.crawler.core.cperson.persis.po.CPersonPo;

public class CPersonService {

	@Resource(name = "defaultDAO_CM")
	private MybatisDAO<CPersonPo> personDao;

	@PostConstruct
	public void initParam() {
		personDao.setNamespace("A_CPERSON");
	}
	
	public void insertPerson(CPersonPo personPo) {
		List<CPersonPo> ps = new ArrayList<>();
		ps.add(personPo);
		Map<String, Object> m = new HashMap<>();
		m.put("list", ps);
		personDao.insert("insertList", m);
	}
	
	public void insertPerson(List<CPersonPo> ps) {
		if (ps!=null && ps.size()>0) {
			personDao.insert("insertList", ps);
		}
	}
	
	public CPersonPo getCPerson(String pSource, String resId, String resTableName) {
		Map<String, Object> m = new HashMap<>();
		m.put("pSource", pSource);
		m.put("resTableName", resTableName);
		m.put("resId", resId);
		m.put("orderSql", "cTime Desc");
		List<CPersonPo> ps = personDao.queryForList("getList", m);
		if (ps!=null && ps.size()>0) {
			return ps.get(0);
		}
		return null;
	}
	
	//删除旧的历史数据，保留最新主播信息
	public void removeSame() {
		personDao.delete("delete");
	}
}
