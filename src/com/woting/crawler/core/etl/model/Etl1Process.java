package com.woting.crawler.core.etl.model;

import java.sql.Timestamp;

public class Etl1Process {

	private String etlnum;
	private Timestamp cTime;
	
	public String getEtlnum() {
		return etlnum;
	}
	public void setEtlnum(String etlnum) {
		this.etlnum = etlnum;
	}
	public Timestamp getcTime() {
		return cTime;
	}
	public void setcTime(Timestamp cTime) {
		this.cTime = cTime;
	}
	
}
