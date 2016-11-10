package com.woting.cm.core.keyword.persis.po;

import java.sql.Timestamp;

import com.spiritdata.framework.core.model.BaseObject;

public class KwResPo extends BaseObject {

	private static final long serialVersionUID = 2869646486062644605L;
	
	private String id;
	private String refName;
	private String kwId;
	private String resTableName;
	private String resId;
	private Timestamp cTime;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getRefName() {
		return refName;
	}
	public void setRefName(String refName) {
		this.refName = refName;
	}
	public String getKwId() {
		return kwId;
	}
	public void setKwId(String kwId) {
		this.kwId = kwId;
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
	public Timestamp getcTime() {
		return cTime;
	}
	public void setcTime(Timestamp cTime) {
		this.cTime = cTime;
	}
}
