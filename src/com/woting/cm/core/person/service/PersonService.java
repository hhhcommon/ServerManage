package com.woting.cm.core.person.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import com.spiritdata.framework.core.dao.mybatis.MybatisDAO;
import com.woting.cm.core.person.persis.po.PersonPo;
import com.woting.cm.core.person.persis.po.PersonRefPo;

public class PersonService {
	@Resource(name = "defaultDAO")
	private MybatisDAO<PersonPo> personDao;
	@Resource(name = "defaultDAO")
	private MybatisDAO<PersonRefPo> personRefDao;
	
	@PostConstruct
	public void initParam() {
	    personDao.setNamespace("A_PERSON");
	    personRefDao.setNamespace("A_PERSONREF");
	}
	
	public List<PersonPo> getPersons() {
		List<PersonPo> pers = personDao.queryForList("getList");
		return pers;
	}
	
	public List<PersonRefPo> getPersonRefs(String personId) {
		Map<String, Object> m = new HashMap<>();
		m.put("personId", personId);
		return personRefDao.queryForList("getListBy", m);
	}

	public PersonRefPo getPersonRefBy(String resTableName, String resId) {
		Map<String, Object> m = new HashMap<>();
		m.put("resTableName", resTableName);
		m.put("resId", resId);
		List<PersonRefPo> pfs = personRefDao.queryForList("getListBy", m);
		if (pfs!=null && pfs.size()>0) {
			return pfs.get(0);
		}
		return null;
	}
	
	public PersonRefPo getPersonRefBy(String personId, String resTableName, String resId) {
		Map<String, Object> m = new HashMap<>();
		m.put("resTableName", resTableName);
		m.put("resId", resId);
		m.put("personId", personId);
		List<PersonRefPo> pfs = personRefDao.queryForList("getListBy", m);
		if (pfs!=null && pfs.size()>0) {
			return pfs.get(0);
		}
		return null;
	}
	
	public void insertPerson(List<PersonPo> ps) {
		Map<String, Object> m = new HashMap<>();
		m.put("list", ps);
		personDao.insert("insertList", m);
	}
	
	public void insertPerson(PersonPo po) {
		List<PersonPo> pos = new ArrayList<>();
		pos.add(po);
		Map<String, Object> m = new HashMap<>();
		m.put("list", pos);
		personDao.insert("insertList", m);
	}
	
	public void insertPersonRef(List<PersonRefPo> pfs) {
		Map<String, Object> m = new HashMap<>();
		m.put("list", pfs);
		personRefDao.insert("insertList", m);
	}
	
	public void insertPersonRef(PersonRefPo pf) {
		List<PersonRefPo> pfs = new ArrayList<>();
		pfs.add(pf);
		Map<String, Object> m = new HashMap<>();
		m.put("list", pfs);
		personRefDao.insert("insertList", m);
	}
	
	public PersonPo getPersonByPersonId(String personId) {
		return personDao.getInfoObject("getListById", personId);
	}
	
	public List<PersonPo> getPersonsByResIdAndResTableName(String resId, String resTableName) {
		String whereSql = "";
		if (resId!=null) {
			whereSql += " and resId = '" + resId +"'";
		}
		if (resTableName!=null) {
			whereSql += " and resTableName = '"+ resTableName +"'";
		}
		if (whereSql.length()>0) {
			whereSql = whereSql.substring(4);
			whereSql = " where " + whereSql;
		}
		Map<String, Object> m = new HashMap<>();
		m.put("whereSql", whereSql);
		List<PersonPo> pers = personDao.queryForList("getListByResIdAndResTableName", m);
		if (pers!=null && pers.size()>0) {
			return pers;
		}
		return null;
	}
}
