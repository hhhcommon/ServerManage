package com.woting.cm.core.perimeter.service;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import com.spiritdata.framework.core.dao.mybatis.MybatisDAO;
import com.woting.cm.core.perimeter.persis.po.OrganizePo;

public class OrganizeService {
	@Resource(name="defaultDAO")
    private MybatisDAO<OrganizePo> oganDao;

    @PostConstruct
    public void initParam() {
        oganDao.setNamespace("A_ORGANIZE");
    }
	
    public List<OrganizePo> getOrganizeList() {
    	return oganDao.queryForList("getOrganizeList");
    }
}
