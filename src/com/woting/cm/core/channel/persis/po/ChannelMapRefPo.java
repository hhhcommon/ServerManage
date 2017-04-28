package com.woting.cm.core.channel.persis.po;

import java.sql.Timestamp;

import com.spiritdata.framework.core.model.BaseObject;

public class ChannelMapRefPo extends BaseObject {
	private static final long serialVersionUID = 3622387706572734950L;

	private String id;
	private String channelId;
	private String srcMid;
	private String srcDid;
	private String srcName;
	private int isValidate;
	private Timestamp cTime;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getChannelId() {
		return channelId;
	}
	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}
	public String getSrcMid() {
		return srcMid;
	}
	public void setSrcMid(String srcMid) {
		this.srcMid = srcMid;
	}
	public String getSrcDid() {
		return srcDid;
	}
	public void setSrcDid(String srcDid) {
		this.srcDid = srcDid;
	}
	public String getSrcName() {
		return srcName;
	}
	public void setSrcName(String srcName) {
		this.srcName = srcName;
	}
	public Timestamp getcTime() {
		return cTime;
	}
	public void setcTime(Timestamp cTime) {
		this.cTime = cTime;
	}
	public int getIsValidate() {
		return isValidate;
	}
	public void setIsValidate(int isValidate) {
		this.isValidate = isValidate;
	}
}
