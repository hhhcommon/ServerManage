package com.woting.crawler.core.share.model;

import com.woting.crawler.core.share.persis.po.SharePo;
import com.woting.crawler.ext.SpringShell;

public class Share {
	
	private String wxAppId;
	private String wxAppSecret;
	private int RedisDB;
	
	public Share() {
		SharePo sharePo = (SharePo) SpringShell.getBean("share");
		this.wxAppId = sharePo.getWxAppId();
		this.wxAppSecret = sharePo.getWxAppSecret();
		this.RedisDB = Integer.valueOf(sharePo.getRedisDB());
	}
	
	public String getWxAppId() {
		return wxAppId;
	}
	public void setWxAppId(String wxAppId) {
		this.wxAppId = wxAppId;
	}
	public String getWxAppSecret() {
		return wxAppSecret;
	}
	public void setWxAppSecret(String wxAppSecret) {
		this.wxAppSecret = wxAppSecret;
	}
	public int getRedisDB() {
		return RedisDB;
	}
	public void setRedisDB(int redisDB) {
		RedisDB = redisDB;
	}
}
