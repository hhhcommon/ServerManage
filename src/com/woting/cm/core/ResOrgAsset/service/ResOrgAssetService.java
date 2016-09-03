package com.woting.cm.core.ResOrgAsset.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import com.spiritdata.framework.core.dao.mybatis.MybatisDAO;
import com.woting.cm.core.ResOrgAsset.persis.po.ResOrgAssetPo;

@Service
public class ResOrgAssetService{

	@Resource(name="defaultDAO")
    private MybatisDAO<ResOrgAssetPo> resOrgAssetDao;

    @PostConstruct
    public void initParam() {
    	resOrgAssetDao.setNamespace("A_RESORGASSET");
    }
    
    public void insertResOrgAssetList(List<ResOrgAssetPo> resAss){
    	Map<String, Object> m = new HashMap<>();
    	m.put("list", resAss);
    	resOrgAssetDao.insert("insertList", m);
    }
    
    public int getResOrgAssetNum() {
		return resOrgAssetDao.getCount("count","");
    }
    
    public List<ResOrgAssetPo> getResOrgAssetList(int page ,int pagesize){
    	Map<String, Object> m = new HashMap<>();
    	m.put("page", page);
    	m.put("pagesize", pagesize);
		return resOrgAssetDao.queryForList("getResOrgAssetList", m);
    }
}
