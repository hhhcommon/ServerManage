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
		if(aulist!=null&&aulist.size()>0){
			m.put("list", aulist);
		    audioDao.insert("insertList", m);
		}
	}
	
	public int getAudioNum(String crawlerNum){
		return audioDao.getCount("count", crawlerNum);
	}
	
	public List<AudioPo> getAudioList(int page, int pagesize, String crawlernum){
		Map<String, Object> m = new HashMap<String,Object>();
		m.put("page", page);
		m.put("pagesize", pagesize);
		m.put("crawlerNum", crawlernum);
		List<AudioPo> l = audioDao.queryForList("getAudioList", m);
		return l;
	}
	
	public List<AudioPo> getAudios(String audioId, String albumId, String publisher, String crawlernum) {
		Map<String, Object> m = new HashMap<>();
		m.put("albumId", albumId);
		m.put("publisher", publisher);
		m.put("crawlerNum", crawlernum);
		m.put("audioId", audioId);
		List<AudioPo> l = audioDao.queryForList("getAudioList", m);
		if (l!=null && l.size()>0) {
			return l;
		}
		return null;
	}
	
	public List<AudioPo> getAudios(Map<String, Object> m) {
		List<AudioPo> l = audioDao.queryForList("getList", m);
		if (l!=null && l.size()>0) {
			return l;
		}
		return null;
	}
	
	public int countNumByAlbumId(String albumId, String publisher, String crawlernum) {
		Map<String, Object> m = new HashMap<String,Object>();
		m.put("albumId", albumId);
		m.put("publisher", publisher);
		m.put("crawlerNum", crawlernum);
		return audioDao.getCount("getAudioNumByAlbumIdAndPublisher", m);
	}
	
	public List<AudioPo> getAudioListByAlbumId(String albumId,String publisher, String num){
		Map<String, Object> m = new HashMap<String,Object>();
		m.put("albumId", albumId);
		m.put("publisher", publisher);
		m.put("crawlerNum", num);
		List<AudioPo> list = audioDao.queryForList("getAudioByAlbumIdAndPublisher", m);
		if (list!=null && list.size()>0) {
			return list;
		}
		return null;
	}
	
	public List<AudioPo> getAudioListById(String id) {
		return audioDao.queryForList("getAudioInfo", id);
	}
	
	public AudioPo getAudioInfo(String id) {
		return audioDao.getInfoObject("getAudioInfo", id);
	}
	
	public void updateAudio(AudioPo audioPo) {
		audioDao.update(audioPo);
	}
	
	public void removeSameAudio(String id){
		audioDao.delete("deleteAudioById", id);
	}
	
	public void removeSameAudio(String albumId, String publisher, String num){
		Map<String, Object> m = new HashMap<String,Object>();
		m.put("albumId", albumId);
		m.put("audioPublisher", publisher);
		m.put("crawlerNum", num);
		audioDao.delete("deleteByAlbumIdAndPublisher", m);
	}
	
	public void removeNull(String num){
		Map<String, Object> m = new HashMap<String,Object>();
		m.put("schemeId", "null");
		m.put("crawlerNum", num);
		audioDao.update("removeNull", m);
		m.clear();
		m.put("schemeName", "null");
		m.put("crawlerNum", num);
		audioDao.update("removeNull", m);
		m.clear();
		m.put("audioTags", "null");
		m.put("crawlerNum", num);
		audioDao.update("removeNull", m);
		m.clear();
		m.put("descn", "null");
		m.put("crawlerNum", num);
		audioDao.update("removeNull", m);
		m.clear();
		m.put("descn", "");
		m.put("crawlerNum", num);
		audioDao.update("removeNull", m);
	}
}
