package com.woting.cm.core.ResOrgAsset.service;

import java.util.ArrayList;
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
    
    public void insertResOrgAsset(ResOrgAssetPo resAss){
    	if (resAss!=null) {
			List<ResOrgAssetPo> ress = new ArrayList<>();
    	    ress.add(resAss);
    	    Map<String, Object> m = new HashMap<>();
    	    m.put("list", ress);
    	    resOrgAssetDao.insert("insertList", m);
		}
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
    
    public List<ResOrgAssetPo> getResOrgAssetPo(Map<String, Object> m) {
    	List<ResOrgAssetPo> resList = resOrgAssetDao.queryForList("getList", m);
    	if (resList!=null) {
			return resList;
		}
		return null;
    }
    
    public List<ResOrgAssetPo> getResOrgAssetListBySQL(String SQL) {
    	Map<String, Object> m = new HashMap<>();
    	m.put("Sql", SQL);
    	List<ResOrgAssetPo> ress = resOrgAssetDao.queryForList("getResOrgAssetListBySQL", m);
    	if (ress!=null && ress.size()>0) {
			return ress;
		}
		return null;
    }
    
    public ResOrgAssetPo getResOrgAssetPo(String origSrcId, String orgName, String resTableName) {
    	Map<String, Object> m = new HashMap<>();
    	m.put("origSrcId", origSrcId);
    	m.put("orgName", orgName);
    	m.put("resTableName", resTableName);
    	return resOrgAssetDao.getInfoObject("getList", m);
    }
    
    public void deleteByOrigSrcIds(String origSrcIds, String orgName, String resTableName) {
    	Map<String, Object> m = new HashMap<>();
    	m.put("whereSql", origSrcIds);
    	m.put("orgName", orgName);
    	m.put("resTableName", resTableName);
    	resOrgAssetDao.delete("deleteByEntity", m);
    }
}
