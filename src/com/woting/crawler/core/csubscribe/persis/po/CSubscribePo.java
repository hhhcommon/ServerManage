package com.woting.crawler.core.csubscribe.persis.po;

import com.spiritdata.framework.core.model.BaseObject;

public class CSubscribePo extends BaseObject {
	private static final long serialVersionUID = 7531547447988307550L;
	private String id;
	private String resId;
	private String resTableName;
	private String publisher;
	private long subscribeCount;
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
	public long getSubscribeCount() {
		return subscribeCount;
	}
	public void setSubscribeCount(long subscribeCount) {
		this.subscribeCount = subscribeCount;
	}
}
