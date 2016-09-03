package com.woting.crawler.scheme.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeUtils {

	public static String getNowTime(){
		Date d = new Date();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return df.format(d);
	}
}
