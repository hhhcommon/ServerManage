package com.woting.crawler.core.scheme.persis.po;

public class SchemePo {
	private String Schemenum;
	private String CrawlerExtent;
	private String NumberOfCrawlers;
	private String RedisDB;
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
}
