package com.woting.crawler.scheme.searchcrawler.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;


/**
 * 
 * 考拉FM搜索专辑信息
 * 
 * @author wbq
 *
 */
public class Station implements Serializable {
	private static final long serialVersionUID = 8893255489064157586L;
	private String MediaType = "SEQU";// 类型
	private String contentPub; // 发布者
	private String id; // 专辑ID
	private String name; // 专辑名称
	private String desc; // 专辑内容描述
	private String pic; // 专辑图片链接
	private String host; // 主播人
	private Timestamp CTime;
	private List<Festival> festivals; // 专辑节目信息

	public String getMediaType() {
		return MediaType;
	}

	public String getContentPub() {
		return contentPub;
	}

	public void setContentPub(String contentPub) {
		this.contentPub = contentPub;
	}

	public String getId() {
		return id;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = (host == null ? null : host.replaceAll("<em>|</em>", ""));
	}

	public String getPic() {
		return pic;
	}

	public void setPic(String pic) {
		this.pic = pic;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name.replaceAll("<em>|</em>", "");
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDesc() {
		return desc == null ? null : desc.replaceAll("\n", "").replaceAll("\r", "");
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public List<Festival> getFestivals() {
		return festivals;
	}

	public void setFestivals(List<Festival> festivals) {
		this.festivals = festivals;
	}

	public Timestamp getCTime() {
		return CTime;
	}

	public void setCTime(Timestamp cTime) {
		CTime = cTime;
	}
	
}
