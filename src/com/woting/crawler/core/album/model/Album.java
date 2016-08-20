package com.woting.crawler.core.album.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

import com.spiritdata.framework.core.model.ModelSwapPo;
import com.spiritdata.framework.exceptionC.Plat0006CException;
import com.woting.cm.core.dict.persis.po.DictMasterPo;
import com.woting.crawler.core.album.persis.po.AlbumPo;
import com.woting.crawler.core.audio.persis.po.AudioPo;

public class Album implements Serializable, ModelSwapPo {

	private static final long serialVersionUID = -2274656839379986208L;
	private String id;
	private String albumId;
	private String albumName;
	private String albumPublisher;
	private String albumImg;
	private String albumTags;
	private String categoryId;
	private String categoryName;
	private String descn;
	private String visitUrl;
	private String playCount;
	private String crawlerNum;
	private String schemeId;
	private String schemeName;
	private Timestamp cTime;
	private List<AudioPo> audiolist;
	
	public List<AudioPo> getAudiolist() {
		return audiolist;
	}
	public void setAudiolist(List<AudioPo> audiolist) {
		this.audiolist = audiolist;
	}
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
	public String getPlayCount() {
		return playCount;
	}
	public void setPlayCount(String playCount) {
		this.playCount = playCount;
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
	@Override
	public void buildFromPo(Object po) {
		if (po==null) throw new Plat0006CException("Po对象为空，无法从空对象得到概念/逻辑对象！");
        if (!(po instanceof AlbumPo)) throw new Plat0006CException("Po对象不是DictMasterPo的实例，无法从此对象构建字典组对象！");
		Album album = (Album) po;
	}
	@Override
	public Object convert2Po() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
