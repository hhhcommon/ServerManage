package com.woting.crawler.core.albumaudioref.persis.po;

import java.sql.Timestamp;

import com.spiritdata.framework.core.model.BaseObject;

public class AlbumAudioRefPo extends BaseObject {
	private static final long serialVersionUID = 3444958099759483741L;
	
	private String id;
	private String alId;
	private String auId;
	private int columnNum;
	private int isMain;
	private Timestamp cTime;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getAlId() {
		return alId;
	}
	public void setAlId(String alId) {
		this.alId = alId;
	}
	public String getAuId() {
		return auId;
	}
	public void setAuId(String auId) {
		this.auId = auId;
	}
	public int getColumnNum() {
		return columnNum;
	}
	public void setColumnNum(int columnNum) {
		this.columnNum = columnNum;
	}
	public int getIsMain() {
		return isMain;
	}
	public void setIsMain(int isMain) {
		this.isMain = isMain;
	}
	public Timestamp getcTime() {
		return cTime;
	}
	public void setcTime(Timestamp cTime) {
		this.cTime = cTime;
	}
}
