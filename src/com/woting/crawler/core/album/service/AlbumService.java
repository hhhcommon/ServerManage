package com.woting.crawler.core.album.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.spiritdata.framework.core.dao.mybatis.MybatisDAO;
import com.woting.crawler.core.album.persis.po.AlbumPo;

@Service
public class AlbumService {

	@Resource(name = "defaultDAO_CM")
	private MybatisDAO<AlbumPo> albumDao;

	@PostConstruct
	public void initParam() {
		albumDao.setNamespace("A_ALBUM");
	}
	
	public void insertAlbumList(List<AlbumPo> albumlist){
		Map<String, Object> m = new HashMap<>();
		m.put("list", albumlist);
		albumDao.insert("insertList", m);
	}
}
