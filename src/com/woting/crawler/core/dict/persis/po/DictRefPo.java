package com.woting.crawler.core.dict.persis.po;

import java.sql.Timestamp;

import com.spiritdata.framework.core.model.BaseObject;

public class DictRefPo extends BaseObject {
	private static final long serialVersionUID = 3582693488634378984L;

	private String id;
	private String resTableName;
	private String resId;
	private String cdictMid;
	private String cdictDid;
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
	public String getCdictMid() {
		return cdictMid;
	}
	public void setCdictMid(String cdictMid) {
		this.cdictMid = cdictMid;
	}
	public String getCdictDid() {
		return cdictDid;
	}
	public void setCdictDid(String cdictDid) {
		this.cdictDid = cdictDid;
	}
	public Timestamp getcTime() {
		return cTime;
	}
	public void setcTime(Timestamp cTime) {
		this.cTime = cTime;
	}
}
