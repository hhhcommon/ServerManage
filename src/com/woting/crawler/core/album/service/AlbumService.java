package com.woting.crawler.core.album.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import com.spiritdata.framework.core.dao.mybatis.MybatisDAO;
import com.woting.crawler.core.album.persis.po.AlbumPo;
import com.woting.crawler.core.audio.persis.po.AudioPo;

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
	
	public List<AlbumPo> getAlbumList(int page, int pagesize ,String num){
		Map<String, Object> m = new HashMap<String,Object>();
		m.put("page", page);
		m.put("pagesize", pagesize);
		m.put("crawlerNum", num);
		List<AlbumPo> aus = albumDao.queryForList("getAlbumList", m);
		return aus;
	}
	
	public List<AlbumPo> getAlbumList(String num){
		Map<String, Object> m = new HashMap<String,Object>();
		m.put("crawlerNum", num);
		List<AlbumPo> aus = albumDao.queryForList("getAlbumListByCrawlerNum", m);
		return aus;
	}
	
	public int countNum(String crawlerNum){
		int num = albumDao.getCount("count",crawlerNum);
		return num;
	}
	
	public void removeSameAlbum(String albumId, String publisher ,String num){
		Map<String, Object> m = new HashMap<String,Object>();
		m.put("albumId", albumId);
		m.put("albumPublisher", publisher);
		m.put("crawlerNum", num);
		albumDao.delete("deleteByAlbumIdAndPublisher", m);
	}
	
	public void removeNull(String num){
		Map<String, Object> m = new HashMap<String,Object>();
		m.put("schemeId", "null");
		m.put("crawlerNum", num);
		albumDao.update("removeNull", m);
		m.clear();
		m.put("schemeName", "null");
		m.put("crawlerNum", num);
		albumDao.update("removeNull", m);
		m.clear();
		m.put("albumTags", "null");
		m.put("crawlerNum", num);
		albumDao.update("removeNull", m);
		m.clear();
		m.put("descn", "null");
		m.put("crawlerNum", num);
		albumDao.update("removeNull", m);
		m.clear();
		m.put("descn", "");
		m.put("crawlerNum", num);
		albumDao.update("removeNull", m);
	}
}