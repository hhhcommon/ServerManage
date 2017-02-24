package com.woting.crawler.core.cforward.persis.po;

import com.spiritdata.framework.core.model.BaseObject;

public class CForwardPo extends BaseObject {
	private static final long serialVersionUID = -467361985557022241L;
	private String id;
	private String resId;
	private String resTableName;
	private String publisher;
	private long forwardCount;
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
	public long getForwardCount() {
		return forwardCount;
	}
	public void setForwardCount(long forwardCount) {
		this.forwardCount = forwardCount;
	}
}
