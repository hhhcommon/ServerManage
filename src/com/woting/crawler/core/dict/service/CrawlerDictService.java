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
	
	public List<DictDPo> getDictDList(String publisher, String crawlernum){
		Map<String, Object> m = new HashMap<String,Object>();
		m.put("publisher", publisher);
		m.put("crawlerNum", crawlernum);
		List<DictDPo> ddlist = dictDDao.queryForList("getListByPubAndCrawlerNum", m);
		return ddlist;
	}
	
	public List<DictDPo> getDictDByMid(String mid){
		List<DictDPo> dictdlist = dictDDao.queryForList("getListByMid", mid);
		return dictdlist;
	}
}
