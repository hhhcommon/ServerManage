package com.woting.crawler.core.search.persis.po;

import com.spiritdata.framework.core.model.BaseObject;

public class SearchPo extends BaseObject {
	private static final long serialVersionUID = -4756906396005372566L;
	
	private int RedisSearchContents;

	public int getRedisSearchContents() {
		return RedisSearchContents;
	}
	public void setRedisSearchContents(int redisSearchContents) {
		RedisSearchContents = redisSearchContents;
	}
}
