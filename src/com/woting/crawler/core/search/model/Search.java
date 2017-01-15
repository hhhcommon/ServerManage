package com.woting.crawler.core.search.model;

import com.woting.crawler.core.search.persis.po.SearchPo;
import com.woting.crawler.ext.SpringShell;

public class Search {
	
	private int RedisSearchContents;
	
	public Search() {
		SearchPo searchPo = (SearchPo) SpringShell.getBean("search");
		this.RedisSearchContents = searchPo.getRedisSearchContents();
	}

	public int getRedisSearchContents() {
		return RedisSearchContents;
	}
	public void setRedisSearchContents(int redisSearchContents) {
		RedisSearchContents = redisSearchContents;
	}
}
