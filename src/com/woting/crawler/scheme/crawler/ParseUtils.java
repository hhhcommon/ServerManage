package com.woting.crawler.scheme.crawler;

public abstract class ParseUtils {

	public static int getType(String href){
		if(href.startsWith("http://www.kaolafm.com/zj/")) return 1;
		if(href.startsWith("http://www.kaolafm.com/jm/")) return 2;
		if(href.equals("http://www.qingting.fm/s/home")) return 3;
		if(href.startsWith("http://www.qingting.fm/s/vchannels")) return 4;
		if(href.startsWith("http://www.ximalaya.com") && href.contains("album")) return 5;
		if(href.startsWith("http://www.ximalaya.com") && href.contains("sound")) return 6;
		return 0;
	}
}
