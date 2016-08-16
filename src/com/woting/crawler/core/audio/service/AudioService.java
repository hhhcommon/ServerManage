package com.woting.crawler.core.audio.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.spiritdata.framework.core.dao.mybatis.MybatisDAO;
import com.spiritdata.framework.util.JsonUtils;
import com.woting.crawler.core.audio.persis.po.AudioPo;

@Service
public class AudioService {

	@Resource(name = "defaultDAO_CM")
	private MybatisDAO<AudioPo> audioDao;

	@PostConstruct
	public void initParam() {
		audioDao.setNamespace("A_AUDIO");
	}

	public void insertAudioList(List<AudioPo> audiolist) {
		int num = 0;
		List<AudioPo> aulist = new ArrayList<AudioPo>();
		Map<String, Object> m = new HashMap<>();
		for (int i = 0; i < audiolist.size(); i++) {
			aulist.add(audiolist.get(i));
			num++;
			if (num == 1000) {
				m.put("list", aulist);
				audioDao.insert("insertList", m);
				num=0;
				m.clear();
				aulist.clear();
			}
		}
		m.put("list", aulist);
		audioDao.insert("insertList", m);
	}
}
