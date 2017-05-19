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
	private MybatisDAO<CPersonPo> cpersonDao;
	@Resource(name = "defaultDAO_CM")
	private MybatisDAO<CPersonRefPo> cpersonRefDao;

	@PostConstruct
	public void initParam() {
		cpersonDao.setNamespace("A_CPERSON");
		cpersonRefDao.setNamespace("A_CPERSONREF");
	}
	
	public void insertPerson(CPersonPo personPo) {
		List<CPersonPo> ps = new ArrayList<>();
		ps.add(personPo);
		Map<String, Object> m = new HashMap<>();
		m.put("list", ps);
		cpersonDao.insert("insertList", m);
	}
	
	public void insertPerson(List<CPersonPo> ps) {
		if (ps!=null && ps.size()>0) {
			cpersonDao.insert("insertList", ps);
		}
	}
	
	public void insertPersonRef(CPersonRefPo cPersonRefPo) {
		List<CPersonRefPo> ps = new ArrayList<>();
		ps.add(cPersonRefPo);
		Map<String, Object> m = new HashMap<>();
		m.put("list", ps);
		cpersonRefDao.insert("insertList", m);
	}
	
	public CPersonPo getCPersonById(String id) {
		Map<String, Object> m = new HashMap<>();
		m.put("id", id);
		CPersonPo cPo = cpersonDao.getInfoObject("getList", m);
		return cPo;
	}
	
	public CPersonPo getCPersonByPersonId(String pSource, String personId) {
		Map<String, Object> m = new HashMap<>();
		m.put("pSource", pSource);
		m.put("pSrcId", personId);
		CPersonPo cPo = cpersonDao.getInfoObject("getList", m);
		return cPo;
	}
	
	public CPersonPo getCPerson(String pSource, String resId, String resTableName) {
		Map<String, Object> m = new HashMap<>();
		m.put("pSource", pSource);
		m.put("resTableName", resTableName);
		m.put("resId", resId);
		List<CPersonPo> ps = cpersonDao.queryForList("getListBy", m);
		if (ps!=null && ps.size()>0) {
			return ps.get(0);
		}
		return null;
	}
	
	public List<CPersonPo> getCPersons(String pSource, String resId, String resTableName) {
		Map<String, Object> m = new HashMap<>();
		if (pSource!=null) m.put("pSource", pSource);
		if (resId!=null) m.put("resTableName", resTableName);
		if (resTableName!=null) m.put("resId", resId);
		List<CPersonPo> ps = cpersonDao.queryForList("getListBy", m);
		if (ps!=null && ps.size()>0) return ps;
		return null;
	}
	
	public CPersonRefPo getCPersonRef(String personId, String resTableName, String resId) {
		Map<String, Object> m = new HashMap<>();
		m.put("personId", personId);
		m.put("resTableName", resTableName);
		m.put("resId", resId);
		CPersonRefPo cRefPo = cpersonRefDao.getInfoObject("getList", m);
		return cRefPo;
	}
	
	//删除旧的历史数据，保留最新主播信息
	public void removeSame() {
		cpersonDao.delete("delete");
	}
	
	public void removePersonRef(String resId, String resTableName) {
		Map<String, Object> m = new HashMap<>();
		m.put("resId", resId);
		m.put("resTableName", resTableName);
		cpersonRefDao.delete("deleteBy", m);
	}
}
