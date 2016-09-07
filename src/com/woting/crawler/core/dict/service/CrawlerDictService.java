package com.woting.crawler.core.dict.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.spiritdata.framework.core.dao.mybatis.MybatisDAO;
import com.woting.crawler.core.dict.persis.po.DictDPo;
import com.woting.crawler.core.dict.persis.po.DictMPo;
@Service
public class CrawlerDictService {

	@Resource(name = "defaultDAO_CM")
	private MybatisDAO<DictMPo> dictmDao;
	@Resource(name = "defaultDAO_CM")
	private MybatisDAO<DictDPo> dictDDao;

	@PostConstruct
	public void initParam() {
		dictmDao.setNamespace("A_DICTM");
		dictDDao.setNamespace("A_DICTD");
	}
	
	public void insertDictM(List<DictMPo> dictmlist){
		Map<String, Object> m = new HashMap<String,Object>();
		m.put("list", dictmlist);
		dictmDao.insert("insertList", m);
	}
	
	public void insertDictD(List<DictDPo> dictdlist){
		Map<String, Object> m = new HashMap<String,Object>();
		m.put("list", dictdlist);
		dictDDao.insert("insertList", m);
	}
	
	public DictMPo getDictMList(String id){
		DictMPo dictm = dictmDao.getInfoObject("getListById", id);
		return dictm;
	}
	
	public List<DictDPo> getDictDList(String publisher){
		Map<String, Object> m = new HashMap<String,Object>();
		m.put("publisher", publisher);
		List<DictDPo> ddlist = dictDDao.queryForList("getListByPub", m);
		return ddlist;
	}
	
	public List<DictDPo> getDictDByMid(String mid){
		List<DictDPo> dictdlist = dictDDao.queryForList("getListByMid", mid);
		return dictdlist;
	}
	
	public int getDictdValidNum(String publisher) {
		List<DictDPo> ds = dictDDao.queryForList("getDictdValidNum", publisher);
		if(ds!=null&&ds.size()>0) 
			return ds.size();
		else 
			return 0;
	}
	
	private DictDPo getDictDByNameAndPubIs1(DictDPo ddpo) {
		Map<String, Object> m = new HashMap<String,Object>();
		m.put("ddName", ddpo.getDdName());
		m.put("publisher", ddpo.getPublisher());
		m.put("pId", ddpo.getpId());
		return dictDDao.getInfoObject("getDictDInfo", m);
	}
	
	public boolean compareDictIsOrNoNew(List<DictDPo> listd) {
		for (DictDPo dDPo : listd) {
			if(getDictDByNameAndPubIs1(dDPo)==null) 
				return false;
		}
		return true;
	}
	
	public int getMaxIsValidateNum(String publisher) {
		DictDPo ddp = dictDDao.getInfoObject("getMaxIsValidate" ,publisher);
		if(ddp!=null) 
			return ddp.getIsValidate();
	    return 0;
	}
}
