package com.woting.crawler.core.cperson.service;

import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.spiritdata.framework.core.dao.mybatis.MybatisDAO;
import com.woting.crawler.core.cperson.persis.po.CPersonPo;
import com.woting.crawler.core.cperson.persis.po.CPersonRefPo;
@Service
public class CPersonService {

	@Resource(name = "defaultDAO_CM")
	private MybatisDAO<CPersonPo> personDao;
	@Resource(name = "defaultDAO_CM")
	private MybatisDAO<CPersonRefPo> personRefDao;

	@PostConstruct
	public void initParam() {
		personDao.setNamespace("A_CPERSON");
		personRefDao.setNamespace("A_CPERSONREF");
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
	
	public void insertPersonRef(CPersonRefPo cPersonRefPo) {
		List<CPersonRefPo> ps = new ArrayList<>();
		ps.add(cPersonRefPo);
		Map<String, Object> m = new HashMap<>();
		m.put("list", ps);
		personRefDao.insert("insertList", m);
	}
	
	public CPersonPo getCPersonByPersonId(String pSource, String personId) {
		Map<String, Object> m = new HashMap<>();
		m.put("pSource", pSource);
		m.put("pSrcId", personId);
		CPersonPo cPo = personDao.getInfoObject("getList", m);
		return cPo;
	}
	
	public CPersonPo getCPerson(String pSource, String resId, String resTableName) {
		Map<String, Object> m = new HashMap<>();
		m.put("pSource", pSource);
		m.put("resTableName", resTableName);
		m.put("resId", resId);
		List<CPersonPo> ps = personDao.queryForList("getListBy", m);
		if (ps!=null && ps.size()>0) {
			return ps.get(0);
		}
		return null;
	}
	
	public CPersonRefPo getCPersonRef(String personId, String resTableName, String resId) {
		Map<String, Object> m = new HashMap<>();
		m.put("personId", personId);
		m.put("resTableName", resTableName);
		m.put("resId", resId);
		CPersonRefPo cRefPo = personRefDao.getInfoObject("getList", m);
		return cRefPo;
	}
	
	//删除旧的历史数据，保留最新主播信息
	public void removeSame() {
		personDao.delete("delete");
	}
}
