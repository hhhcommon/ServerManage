package com.woting.crawler.core.scheme.persis.po;

public class SchemePo {
	private String Schemenum;
	private String CrawlerExtent;
	private String NumberOfCrawlers;
	private String RedisSnapShootDB;
	private String RedisDB;
	private int XMLYThread_Limit_Size;
	private int QTThread_Limit_Size;
	public String getSchemenum() {
		return Schemenum;
	}
	public void setSchemenum(String schemenum) {
		Schemenum = schemenum;
	}
	public String getCrawlerExtent() {
		return CrawlerExtent;
	}
	public void setCrawlerExtent(String crawlerExtent) {
		CrawlerExtent = crawlerExtent;
	}
	public String getNumberOfCrawlers() {
		return NumberOfCrawlers;
	}
	public void setNumberOfCrawlers(String numberOfCrawlers) {
		this.NumberOfCrawlers = numberOfCrawlers;
	}
	public String getRedisDB() {
		return RedisDB;
	}
	public void setRedisDB(String redisDB) {
		RedisDB = redisDB;
	}
	public String getRedisSnapShootDB() {
		return RedisSnapShootDB;
	}
	public void setRedisSnapShootDB(String redisSnapShootDB) {
		RedisSnapShootDB = redisSnapShootDB;
	}
	public int getXMLYThread_Limit_Size() {
		return XMLYThread_Limit_Size;
	}
	public void setXMLYThread_Limit_Size(int xMLYThread_Limit_Size) {
		XMLYThread_Limit_Size = xMLYThread_Limit_Size;
	}
	public int getQTThread_Limit_Size() {
		return QTThread_Limit_Size;
	}
	public void setQTThread_Limit_Size(int qTThread_Limit_Size) {
		QTThread_Limit_Size = qTThread_Limit_Size;
	}
}
