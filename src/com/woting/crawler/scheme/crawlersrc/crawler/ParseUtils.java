package com.woting.crawler.scheme.crawlersrc.crawler;

public abstract class ParseUtils {

	public static int getType(String href){
		if(href.startsWith("http://www.kan8kan.com/zj/")) return 1;
		if(href.startsWith("http://www.kan8kan.com/jm/")) return 2;
		if(href.startsWith("http://www.qingting.fm/s/home")) return 3;
		if(href.startsWith("http://www.qingting.fm/s/vchannels")) return 4;
		if(href.startsWith("http://www.ximalaya.com") && href.contains("/album/") && (href.length()-href.lastIndexOf("/")>3)) return 5;
		if(href.startsWith("http://www.ximalaya.com") && href.contains("/sound/")) return 6;
		if(href.equals("http://www.duotin.com/webfm")) return 7;
		if(href.startsWith("http://www.duotin.com/webfm/album")) return 8;
		return 0;
	}
}
