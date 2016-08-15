package com.woting.crawler.core.audio.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import com.spiritdata.framework.core.dao.mybatis.MybatisDAO;
import com.woting.crawler.core.audio.persis.po.Audio;

public class AudioService {

	@Resource(name = "defaultDAO_CM")
	private MybatisDAO<Audio> audioDao;

	@PostConstruct
	public void initParam() {
		audioDao.setNamespace("A_AUDIO");
	}
	
	public void insertAudioList(List<Audio> audiolist){
		Map<String, Object> m = new HashMap<>();
		m.put("list", audiolist);
		audioDao.insert("insertList", m);
	}
}
