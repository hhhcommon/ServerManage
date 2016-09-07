package com.woting.crawler.core.dict.persis.po;

import java.sql.Timestamp;
import com.spiritdata.framework.core.model.BaseObject;
import com.spiritdata.framework.util.ChineseCharactersUtils;

public class DictDPo extends BaseObject {

	private static final long serialVersionUID = 6856288723652596765L;
	private String id;
	private String sourceId;
	private String mId;
	private String pId; //父节点ID
	private String publisher;
	private String ddName; //字典项名称
	private String nPy; //名称拼音
	private String aliasName; //字典项别名
	private String anPy; //别名拼音
	private int isValidate;
	private String schemeId; //抓取方案Id
	private String schemeName; //抓取方案名称
	private String visitUrl;
	private String descn;
	private Timestamp cTime;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getSourceId() {
		return sourceId;
	}
	public void setSourceId(String sourceId) {
		this.sourceId = sourceId;
	}
	public String getmId() {
		return mId;
	}
	public void setmId(String mId) {
		this.mId = mId;
	}
	public String getpId() {
		return pId;
	}
	public void setpId(String pId) {
		this.pId = pId;
	}
	public String getPublisher() {
		return publisher;
	}
	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}
	public int getIsValidate() {
		return isValidate;
	}
	public void setIsValidate(int isValidate) {
		this.isValidate = isValidate;
	}
	public String getDdName() {
		return ddName;
	}
	public void setDdName(String ddName) {
		this.ddName = ddName;
		setnPy(ChineseCharactersUtils.getFullSpellFirstUp(ddName));
		setAliasName(ddName);
	}
	public String getnPy() {
		return nPy;
	}
	public void setnPy(String nPy) {
		this.nPy = nPy;
	}
	public String getAliasName() {
		return aliasName;
	}
	public void setAliasName(String aliasName) {
		this.aliasName = aliasName;
		setAnPy(ChineseCharactersUtils.getFullSpellFirstUp(aliasName));
	}
	public String getAnPy() {
		return anPy;
	}
	public void setAnPy(String anPy) {
		this.anPy = anPy;
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
	public String getVisitUrl() {
		return visitUrl;
	}
	public void setVisitUrl(String visitUrl) {
		this.visitUrl = visitUrl;
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
