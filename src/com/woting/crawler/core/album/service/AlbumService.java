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
	
	public List<AlbumPo> getAlbumList(int page, int pagesize){
		Map<String, Object> m = new HashMap<String,Object>();
		m.put("page", page);
		m.put("pagesize", pagesize);
		List<AlbumPo> aus = albumDao.queryForList("getAlbumList", m);
		return aus;
	}
	
	public int countNum(String crawlernum){
		int num = albumDao.getCount("count",crawlernum);
		return num;
	}
	
	public void removeSameAlbum(String albumId, String publisher){
		Map<String, Object> m = new HashMap<String,Object>();
		m.put("albumId", albumId);
		m.put("albumPublisher", publisher);
		albumDao.delete("deleteByAlbumIdAndPublisher", m);
	}
	
	public void removeNull(){
		Map<String, Object> m = new HashMap<String,Object>();
		m.put("schemeId", "null");
		albumDao.update("removeNull", m);
		m.clear();
		m.put("schemeName", "null");
		albumDao.update("removeNull", m);
		m.clear();
		m.put("albumTags", "null");
		albumDao.update("removeNull", m);
		m.clear();
		m.put("descn", "null");
		albumDao.update("removeNull", m);
		m.clear();
		m.put("descn", "");
		albumDao.update("removeNull", m);
	}
}
