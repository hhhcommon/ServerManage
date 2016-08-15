package com.woting.crawler.core.scheme.model;

import java.sql.Timestamp;

import com.woting.crawler.core.etl.model.Etl1Process;
import com.woting.crawler.core.etl.model.Etl2Process;

public class Scheme {

	private String schemenum;
	private Timestamp cTimestamp;
	private Etl1Process etl1Process;
	private Etl2Process etl2Process;
	
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
	public Etl1Process getEtl1Process() {
		return etl1Process;
	}
	public void setEtl1Process(Etl1Process etl1Process) {
		this.etl1Process = etl1Process;
	}
	public Etl2Process getEtl2Process() {
		return etl2Process;
	}
	public void setEtl2Process(Etl2Process etl2Process) {
		this.etl2Process = etl2Process;
	}
}
