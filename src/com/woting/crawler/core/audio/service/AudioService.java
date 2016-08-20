package com.woting.crawler.core.audio.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import com.spiritdata.framework.core.dao.mybatis.MybatisDAO;
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
	
	public List<AudioPo> getAudioListByAlbumId(String albumId,String publisher){
		Map<String, Object> m = new HashMap<String,Object>();
		m.put("albumId", albumId);
		m.put("publisher", publisher);
		List<AudioPo> list = audioDao.queryForList("getAudioByAlbumIdAndPublisher", m);
		return list;
	}
	
	public void removeSameAudio(String albumId, String publisher){
		Map<String, Object> m = new HashMap<String,Object>();
		m.put("albumId", albumId);
		m.put("audioPublisher", publisher);
		audioDao.delete("deleteByAlbumIdAndPublisher", m);
	}
	
	public void removeNull(){
		Map<String, Object> m = new HashMap<String,Object>();
		m.put("schemeId", "null");
		audioDao.update("removeNull", m);
		m.clear();
		m.put("schemeName", "null");
		audioDao.update("removeNull", m);
		m.clear();
		m.put("audioTags", "null");
		audioDao.update("removeNull", m);
		m.clear();
		m.put("descn", "null");
		audioDao.update("removeNull", m);
		m.clear();
		m.put("descn", "");
		audioDao.update("removeNull", m);
	}
}
