package com.woting.crawler.scheme.util;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.spiritdata.framework.util.SequenceUUID;
import com.woting.crawler.core.album.persis.po.AlbumPo;
import com.woting.crawler.core.audio.persis.po.AudioPo;

public abstract class ConvertUtils {

	public static List<AlbumPo> convert2Album(List<Map<String, Object>> list,String publisher){
		List<AlbumPo> albums = new ArrayList<AlbumPo>();
		if (list!=null&&!list.isEmpty()) {
			for (Map<String, Object> m : list) {
				AlbumPo al = new AlbumPo();
				al.setId(SequenceUUID.getPureUUID());
				al.setAlbumId(m.get("albumId")+"");
				al.setAlbumName(m.get("albumName")+"");
				al.setAlbumImg(m.get("albumImg")+"");
				al.setAlbumPublisher(publisher);
				al.setAlbumTags(m.get("tags")+"");
				al.setCategoryId(m.get("categoryId")+"");
				al.setCategoryName(m.get("categoryName")+"");
				al.setPlayCount(m.get("playCount")+"");
				al.setVisitUrl(m.get("visitUrl")+"");
				al.setDescn(m.get("descript")+"");
				al.setCrawlerNum(m.get("CrawlerNum")+"");
				al.setSchemeId(m.get("schemeId")+"");
				al.setSchemeName(m.get("schemeName")+"");
				al.setcTime(new Timestamp(System.currentTimeMillis()));
				albums.add(al);
			}
		}
		return albums;
	}
	
	public static List<AudioPo> convert2Aludio(List<Map<String, Object>> list, String publisher){
		List<AudioPo> audios = new ArrayList<AudioPo>();
		if(list!=null&&list.size()>0){
			for (Map<String, Object> m : list) {
				AudioPo audio = new AudioPo();
				audio.setId(SequenceUUID.getPureUUID());
				audio.setAudioId(m.get("audioId")+"");
				audio.setAudioName(m.get("audioName")+"");
				audio.setAlbumId(m.get("albumId")+"");
				audio.setAlbumName(m.get("albumName")+"");
				audio.setAudioImg(m.get("audioImg")+"");
				audio.setCategoryId(m.get("categoryId")+"");
				audio.setCategoryName(m.get("categoryName")+"");
				audio.setAudioURL(m.get("playUrl")+"");
				audio.setAudioTags(m.get("tags")+"");
				audio.setDuration(m.get("duration")+"");
				audio.setPlayCount(m.get("playCount")+"");
				audio.setDescn(m.get("descript")+"");
				audio.setAudioPublisher(publisher);
				audio.setVisitUrl(m.get("visitUrl")+"");
				audio.setCrawlerNum(m.get("CrawlerNum")+"");
				audio.setSchemeId(m.get("schemeId")+"");
				audio.setSchemeName(m.get("schemeName")+"");
				audio.setcTime(new Timestamp(System.currentTimeMillis()));
				audios.add(audio);
			}
		}
		return audios;
	}
}
