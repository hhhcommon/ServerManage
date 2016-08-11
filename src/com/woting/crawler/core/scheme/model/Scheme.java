package com.woting.crawler.core.scheme.model;

import java.sql.Timestamp;

public class Scheme {

	private String schemenum;
	private Timestamp cTimestamp;
	
	public Scheme(String jsonpath) {
		setSchemenum("1");
		setcTimestamp(new Timestamp(System.currentTimeMillis()));
	}
	
	public String getSchemenum() {
		return schemenum;
	}
	public void setSchemenum(String schemenum) {
		this.schemenum = schemenum;
	}
	public Timestamp getcTimestamp() {
		return cTimestamp;
	}
	public void setcTimestamp(Timestamp cTimestamp) {
		this.cTimestamp = cTimestamp;
	}
	
}
