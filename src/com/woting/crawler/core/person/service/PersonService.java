package com.woting.crawler.core.person.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import com.spiritdata.framework.core.dao.mybatis.MybatisDAO;
import com.woting.crawler.core.person.persis.po.PersonPo;

public class PersonService {

	@Resource(name = "defaultDAO_CM")
	private MybatisDAO<PersonPo> personDao;

	@PostConstruct
	public void initParam() {
		personDao.setNamespace("A_PERSON");
	}
	
	public void insertPerson(PersonPo personPo) {
		List<PersonPo> ps = new ArrayList<>();
		ps.add(personPo);
		Map<String, Object> m = new HashMap<>();
		m.put("list", ps);
		personDao.insert("insertList", m);
	}
	
	public void insertPerson(List<PersonPo> ps) {
		if (ps!=null && ps.size()>0) {
			personDao.insert("insertList", ps);
		}
	}
}
