package com.woting.crawler.core.cplaycount.persis.po;

import com.spiritdata.framework.core.model.BaseObject;

public class CPlayCountPo extends BaseObject {
	private static final long serialVersionUID = -8753760124238590774L;
	private String id;
	private String resId;
	private String resTableName;
	private String publisher;
	private long playCount;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getResId() {
		return resId;
	}
	public void setResId(String resId) {
		this.resId = resId;
	}
	public String getResTableName() {
		return resTableName;
	}
	public void setResTableName(String resTableName) {
		this.resTableName = resTableName;
	}
	public String getPublisher() {
		return publisher;
	}
	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}
	public long getPlayCount() {
		return playCount;
	}
	public void setPlayCount(long playCount) {
		this.playCount = playCount;
	}
}
