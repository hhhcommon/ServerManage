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
	private String duration;
	private String descn;
	private String visitUrl;
	private Timestamp pubTime;
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
	public Timestamp getPubTime() {
		return pubTime;
	}
	public void setPubTime(Timestamp pubTime) {
		this.pubTime = pubTime;
	}
}
