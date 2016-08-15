package com.woting.crawler.core.etl.service;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Insert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.sparta.xpath.ThisNodeTest;
import com.woting.crawler.core.album.persis.po.Album;
import com.woting.crawler.core.album.service.AlbumService;
import com.woting.crawler.core.audio.persis.po.Audio;
import com.woting.crawler.core.audio.service.AudioService;
import com.woting.crawler.ext.SpringShell;

public class Etl1Service {
	Logger logger = LoggerFactory.getLogger(ThisNodeTest.class);
	AlbumService albumService;
	AudioService audioService;
	
	public Etl1Service() {
		logger.info("第一次数据转换存放到中间数据库中");
		albumService = (AlbumService) SpringShell.getBean("albumService");
		audioService = (AudioService) SpringShell.getBean("audioService");
	}
	
	@SuppressWarnings("unchecked")
	public void insertSqlAlbumAndAudio(Map<String, Object> m){
		List<Audio> audiolist;
		List<Album> albumlist;
		if(m!=null){
			audiolist = (List<Audio>) m.get("audiolist");
			albumlist = (List<Album>) m.get("albumlist");
			if(audiolist!=null&&audiolist.size()>0){
				audioService.insertAudioList(audiolist);
			}
			if(albumlist!=null&&albumlist.size()>0){
				albumService.insertAlbumList(albumlist);
			}
		}
	}
}
