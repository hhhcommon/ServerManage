package com.woting.crawler.core.share.persis.po;

import com.spiritdata.framework.core.model.BaseObject;

public class SharePo extends BaseObject {
	private static final long serialVersionUID = -9222802097134547459L;
	private String wxAppId;
	private String wxAppSecret;
	private String RedisDB;
	
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
	public String getRedisDB() {
		return RedisDB;
	}
	public void setRedisDB(String redisDB) {
		RedisDB = redisDB;
	}
}
