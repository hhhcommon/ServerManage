package com.woting.crawler.core.album.persis.po;

import java.sql.Timestamp;
import java.util.List;

import com.spiritdata.framework.core.model.BaseObject;
import com.woting.crawler.core.audio.persis.po.AudioPo;

public class AlbumPo extends BaseObject {
	
	private static final long serialVersionUID = 1677996483407334558L;
	private String id;
	private String albumId;
	private String albumName;
	private String albumPublisher;
	private String albumImg;
	private String albumTags;
	private String descn;
	private String visitUrl;
	private Timestamp pubTime;
	private Timestamp cTime;
	private List<AudioPo> audioPos;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getAlbumId() {
		return albumId;
	}
	public void setAlbumId(String albumId) {
		this.albumId = albumId;
	}
	public String getAlbumName() {
		return albumName;
	}
	public void setAlbumName(String albumName) {
		this.albumName = albumName;
	}
	public String getAlbumPublisher() {
		return albumPublisher;
	}
	public void setAlbumPublisher(String albumPublisher) {
		this.albumPublisher = albumPublisher;
	}
	public String getAlbumImg() {
		return albumImg;
	}
	public void setAlbumImg(String albumImg) {
		this.albumImg = albumImg;
	}
	public String getAlbumTags() {
		return albumTags;
	}
	public void setAlbumTags(String albumTags) {
		this.albumTags = albumTags;
	}
	public String getDescn() {
		return descn;
	}
	public void setDescn(String descn) {
		this.descn = descn;
	}
	public String getVisitUrl() {
		return visitUrl;
	}
	public void setVisitUrl(String visitUrl) {
		this.visitUrl = visitUrl;
	}
	public Timestamp getcTime() {
		return cTime;
	}
	public void setcTime(Timestamp cTime) {
		this.cTime = cTime;
	}
	public List<AudioPo> getAudioPos() {
		return audioPos;
	}
	public void setAudioPos(List<AudioPo> audioPos) {
		this.audioPos = audioPos;
	}
	public Timestamp getPubTime() {
		return pubTime;
	}
	public void setPubTime(Timestamp pubTime) {
		this.pubTime = pubTime;
	}
}
