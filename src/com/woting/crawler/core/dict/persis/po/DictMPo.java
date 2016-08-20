package com.woting.crawler.core.dict.persis.po;

import java.sql.Timestamp;

import com.spiritdata.framework.core.model.BaseObject;
import com.spiritdata.framework.util.ChineseCharactersUtils;

public class DictMPo extends BaseObject {

	private static final long serialVersionUID = 435254792160966465L;
	private String id;
	private String dmName;
	private String nPy;
	private String descn;
	private Timestamp cTime;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getDmName() {
		return dmName;
	}
	public void setDmName(String dmName) {
		this.dmName = dmName;
		setnPy(ChineseCharactersUtils.getFullSpellFirstUp(dmName));
	}
	public String getnPy() {
		return nPy;
	}
	public void setnPy(String nPy) {
		this.nPy = nPy;
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
