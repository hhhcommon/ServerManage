package com.woting.crawler.scheme.searchcrawler.model;

import java.io.Serializable;
import java.util.Arrays;

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
	private String CTime;
	private Festival[] festival; // 专辑节目信息

	@Override
	public String toString() {
		return "Station [MediaType=" + MediaType + ", contentPub=" + contentPub + ", id=" + id + ", name=" + name
				+ ", desc=" + desc + ", pic=" + pic + ", host=" + host + ", createTime=" + CTime + ", festival="
				+ Arrays.toString(festival) + "]";
	}

	public String getCTime() {
		return CTime;
	}

	public void setCTime(String cTime) {
		CTime = cTime;
	}

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

	public Festival[] getFestival() {
		return festival;
	}

	public void setFestival(Festival[] festival) {
		this.festival = festival;
	}
}
