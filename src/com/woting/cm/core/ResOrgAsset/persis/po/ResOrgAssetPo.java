package com.woting.cm.core.ResOrgAsset.persis.po;

import java.sql.Timestamp;

import com.spiritdata.framework.core.model.BaseObject;

public class ResOrgAssetPo extends BaseObject {

	private static final long serialVersionUID = -378400403786676574L;
	
	private String id; //主键Id
	private String resTableName; //资源库表名
	private String resId; //资源库Id
	private String orgName; //组织名称
	private String origTableName; //中间库表名
	private String origId; //中间库Id
	private Timestamp cTime; //创建时间
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
	public String getOrgName() {
		return orgName;
	}
	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}
	public String getOrigTableName() {
		return origTableName;
	}
	public void setOrigTableName(String origTableName) {
		this.origTableName = origTableName;
	}
	public String getOrigId() {
		return origId;
	}
	public void setOrigId(String origId) {
		this.origId = origId;
	}
	public Timestamp getcTime() {
		return cTime;
	}
	public void setcTime(Timestamp cTime) {
		this.cTime = cTime;
	}
}
