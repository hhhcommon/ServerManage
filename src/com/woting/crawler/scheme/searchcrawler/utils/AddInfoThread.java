package com.woting.crawler.scheme.searchcrawler.utils;

import com.spiritdata.framework.ext.spring.redis.RedisOperService;
import com.spiritdata.framework.util.JsonUtils;
import com.spiritdata.framework.util.StringUtils;
import com.woting.crawler.scheme.searchcrawler.model.Festival;
import com.woting.crawler.scheme.searchcrawler.model.Station;

public class AddInfoThread<T> extends Thread {

	private String key;
	private T T;
	private RedisOperService ros;
	
	public AddInfoThread (String key, T T, RedisOperService ros) {
		this.key = key;
		this.ros = ros;
		this.T = T;
	}
	
	public void addInfo() {
		String value = "";
        String classname = T.getClass().getSimpleName();
        if (classname.equals("Festival"))
            value = JsonUtils.objToJson(DataTransform.festival2Audio(false, (Festival) T));
        else if (classname.equals("Station"))
            value = JsonUtils.objToJson(DataTransform.datas2Sequ_Audio((Station) T));
        else if (classname.equals("HashMap"))
            value = JsonUtils.objToJson(T);
        if (!StringUtils.isNullOrEmptyOrSpace(value)&&!value.toLowerCase().equals("null")) {
        	ros.rPush("Search_" + key + "_Data", value);
        }
	}
	
	@Override
	public void run() {
		addInfo();
	}
}
