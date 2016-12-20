package com.woting.cm.core.searchword.service;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.spiritdata.framework.core.dao.mybatis.MybatisDAO;
import com.woting.cm.core.searchword.persis.po.SearchWordPo;

@Service
public class SearchWordService {
	@Resource(name="defaultDAO")
    private MybatisDAO<SearchWordPo> searchWordDao;
	
	@PostConstruct
    public void initParam() {
		searchWordDao.setNamespace("A_SEARCHWORD");
	}
	
	public void insertSearchWord(SearchWordPo sw) {
		searchWordDao.insert(sw);
	}
	
	public List<SearchWordPo> getSearchWordList() {
		List<SearchWordPo> sws = searchWordDao.queryForList("getList");
		if (sws!=null && sws.size()>0) {
			return sws;
		}
		return null;
	}
	
	public void deleteSearchWord(String id) {
		searchWordDao.delete(id);
	}
}
