package com.woting.crawler.core.audio.persis.po;

import java.sql.Timestamp;

import com.spiritdata.framework.core.model.BaseObject;

public class AudioPo extends BaseObject {

	private static final long serialVersionUID = 8560519254749780765L;
	private String id;
	private String audioId;
	private String audioName;
	private String audioPublisher;
	private String audioImg;
	private String audioURL;
	private String audioTags;
	private String albumId;
	private String albumName;
	private String categoryId;
	private String categoryName;
	private String duration;
	private String descn;
	private String visitUrl;
	private String playCount;
	private String crawlerNum;
	private String schemeId;
	private String schemeName;
	private Timestamp cTime;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getAudioId() {
		return audioId;
	}
	public void setAudioId(String audioId) {
		this.audioId = audioId;
	}
	public String getAudioName() {
		return audioName;
	}
	public void setAudioName(String audioName) {
		this.audioName = audioName;
	}
	public String getAudioPublisher() {
		return audioPublisher;
	}
	public void setAudioPublisher(String audioPublisher) {
		this.audioPublisher = audioPublisher;
	}
	public String getAudioImg() {
		return audioImg;
	}
	public void setAudioImg(String audioImg) {
		this.audioImg = audioImg;
	}
	public String getAudioURL() {
		return audioURL;
	}
	public void setAudioURL(String audioURL) {
		this.audioURL = audioURL;
	}
	public String getAudioTags() {
		return audioTags;
	}
	public void setAudioTags(String audioTags) {
		this.audioTags = audioTags;
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
	public String getCategoryId() {
		return categoryId;
	}
	public void setCategoryId(String categoryId) {
		this.categoryId = categoryId;
	}
	public String getCategoryName() {
		return categoryName;
	}
	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}
	public String getDuration() {
		return duration;
	}
	public void setDuration(String duration) {
		this.duration = duration;
	}
	public String getDescn() {
		return descn;
	}
	public void setDescn(String descn) {
		this.descn = descn;
	}
	public String getPlayCount() {
		return playCount;
	}
	public void setPlayCount(String playCount) {
		this.playCount = playCount;
	}
	public String getVisitUrl() {
		return visitUrl;
	}
	public void setVisitUrl(String visitUrl) {
		this.visitUrl = visitUrl;
	}
	public String getCrawlerNum() {
		return crawlerNum;
	}
	public void setCrawlerNum(String crawlerNum) {
		this.crawlerNum = crawlerNum;
	}
	public String getSchemeId() {
		return schemeId;
	}
	public void setSchemeId(String schemeId) {
		this.schemeId = schemeId;
	}
	public String getSchemeName() {
		return schemeName;
	}
	public void setSchemeName(String schemeName) {
		this.schemeName = schemeName;
	}
	public Timestamp getcTime() {
		return cTime;
	}
	public void setcTime(Timestamp cTime) {
		this.cTime = cTime;
	}
	
	
}
