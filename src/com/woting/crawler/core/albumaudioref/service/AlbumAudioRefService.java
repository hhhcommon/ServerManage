package com.woting.crawler.core.albumaudioref.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import com.spiritdata.framework.core.dao.mybatis.MybatisDAO;
import com.woting.crawler.core.albumaudioref.persis.po.AlbumAudioRefPo;

public class AlbumAudioRefService {

	@Resource(name = "defaultDAO_CM")
	private MybatisDAO<AlbumAudioRefPo> albumAudioRefDao;

	@PostConstruct
	public void initParam() {
		albumAudioRefDao.setNamespace("A_ALBUMAUDIOREF");
	}
	
	public void insertAlbumAudioRef(AlbumAudioRefPo aRefPo) {
		if (aRefPo!=null) albumAudioRefDao.insert(aRefPo);
	}
	
	public List<AlbumAudioRefPo> getAlbumAudioRefs(String alId) {
		Map<String, Object> m = new HashMap<>();
		m.put("alId", alId);
		return albumAudioRefDao.queryForList("getList", m);
	}
}
