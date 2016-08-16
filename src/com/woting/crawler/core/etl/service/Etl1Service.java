package com.woting.crawler.core.etl.service;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Insert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.hp.hpl.sparta.xpath.ThisNodeTest;
import com.woting.crawler.core.album.persis.po.AlbumPo;
import com.woting.crawler.core.album.service.AlbumService;
import com.woting.crawler.core.audio.persis.po.AudioPo;
import com.woting.crawler.core.audio.service.AudioService;
import com.woting.crawler.ext.SpringShell;

@Service
public class Etl1Service {
	private Logger logger = LoggerFactory.getLogger(ThisNodeTest.class);
	private List<AudioPo> audiolist;
	private List<AlbumPo> albumlist;
	
	public Etl1Service() {
		logger.info("第一次数据转换存放到中间数据库中");
	}
	
	@SuppressWarnings("unchecked")
	public void insertSqlAlbumAndAudio(Map<String, Object> m){
		AlbumService albumService = (AlbumService) SpringShell.getBean("albumService");
		AudioService audioService = (AudioService) SpringShell.getBean("audioService");
		if(m!=null){
			audiolist = (List<AudioPo>) m.get("audiolist");
			albumlist = (List<AlbumPo>) m.get("albumlist");
			if(audiolist!=null&&audiolist.size()>0){
				audioService.insertAudioList(audiolist);
			}
			if(albumlist!=null&&albumlist.size()>0){
				albumService.insertAlbumList(albumlist);
			}
		}
	}
}
