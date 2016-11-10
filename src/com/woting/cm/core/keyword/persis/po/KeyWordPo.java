package com.woting.cm.core.keyword.persis.po;

import java.sql.Timestamp;

import com.spiritdata.framework.core.model.BaseObject;

public class KeyWordPo extends BaseObject {

	private static final long serialVersionUID = 4566889626843154074L;

	private String id;
	private String ownerId;
	private int ownerType;
	private String kwName;
	private String nPy;
	private int sort;
	private int isValidate;
	private String descn;
	private Timestamp cTime;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getOwnerId() {
		return ownerId;
	}
	public void setOwnerId(String ownerId) {
		this.ownerId = ownerId;
	}
	public int getOwnerType() {
		return ownerType;
	}
	public void setOwnerType(int ownerType) {
		this.ownerType = ownerType;
	}
	public String getKwName() {
		return kwName;
	}
	public void setKwName(String kwName) {
		this.kwName = kwName;
	}
	public String getnPy() {
		return nPy;
	}
	public void setnPy(String nPy) {
		this.nPy = nPy;
	}
	public int getSort() {
		return sort;
	}
	public void setSort(int sort) {
		this.sort = sort;
	}
	public int getIsValidate() {
		return isValidate;
	}
	public void setIsValidate(int isValidate) {
		this.isValidate = isValidate;
	}
	public String getDescn() {
		return descn;
	}
	public void setDescn(String descn) {
		this.descn = descn;
	}
	public Timestamp getcTime() {
		return cTime;
	}
	public void setcTime(Timestamp cTime) {
		this.cTime = cTime;
	}
}
