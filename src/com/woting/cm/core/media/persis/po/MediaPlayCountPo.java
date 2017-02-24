package com.woting.cm.core.media.persis.po;

import java.sql.Timestamp;

import com.spiritdata.framework.core.model.BaseObject;

public class MediaPlayCountPo extends BaseObject {
	
	private static final long serialVersionUID = 494694551585536226L;
	private String id;
	private String resTableName;
	private String resId;
	private long playCount;
	private String publisher;
	private Timestamp cTime;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getResTableName() {
		return resTableName;
	}
	public void setResTableName(String resTableName) {
		this.resTableName = resTableName;
	}
	public String getResId() {
		return resId;
	}
	public void setResId(String resId) {
		this.resId = resId;
	}
	public long getPlayCount() {
		return playCount;
	}
	public void setPlayCount(long playCount) {
		this.playCount = playCount;
	}
	public String getPublisher() {
		return publisher;
	}
	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}
	public Timestamp getcTime() {
		return cTime;
	}
	public void setcTime(Timestamp cTime) {
		this.cTime = cTime;
	}
}
