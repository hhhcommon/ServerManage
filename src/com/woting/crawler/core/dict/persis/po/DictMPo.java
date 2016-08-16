package com.woting.crawler.core.dict.persis.po;

import java.sql.Timestamp;

import com.spiritdata.framework.core.model.BaseObject;
import com.spiritdata.framework.util.ChineseCharactersUtils;

public class DictMPo extends BaseObject {

	private static final long serialVersionUID = 435254792160966465L;
	private String id;
	private String dmName;
	private String nPy;
	private String publisher;
	private String crawlerNum; //抓取序号
	private String schemeId; //抓取方案Id
	private String schemeName; //抓取方案名称
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
		setnPy(ChineseCharactersUtils.getFullSpell(dmName));
	}
	public String getnPy() {
		return nPy;
	}
	public void setnPy(String nPy) {
		this.nPy = nPy;
	}
	public String getPublisher() {
		return publisher;
	}
	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}
	public String getCrawlerNum() {
		return crawlerNum;
	}
	public void setCrawlerNum(String crawlerNum) {
		this.crawlerNum = crawlerNum;
	}
	public String getSchemeId() {
		return schemeId;
	}
	public void setSchemeId(String schemeId) {
		this.schemeId = schemeId;
	}
	public String getSchemeName() {
		return schemeName;
	}
	public void setSchemeName(String schemeName) {
		this.schemeName = schemeName;
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
