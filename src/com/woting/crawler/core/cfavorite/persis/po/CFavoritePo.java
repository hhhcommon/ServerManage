package com.woting.crawler.core.cfavorite.persis.po;

import com.spiritdata.framework.core.model.BaseObject;

public class CFavoritePo extends BaseObject {
	private static final long serialVersionUID = -6193019803587888934L;
	private String id;
	private String resId;
	private String resTableName;
	private String publisher;
	private long favoriteCount;
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
	public long getFavoriteCount() {
		return favoriteCount;
	}
	public void setFavoriteCount(long favoriteCount) {
		this.favoriteCount = favoriteCount;
	}
}
