package com.woting.crawler.scheme.utils;

public class CleanDataUtils {

	public static String cleanString(String str) {
		int begnum, begin = 0;
		while (true) {
			begnum = str.indexOf("\"", begin + 1);
			if (begnum != -1) {
				if (str.substring(begnum - 1, begnum).equals("{") || str.substring(begnum - 1, begnum).equals(",") || str.substring(begnum - 1, begnum).equals(":")) {
					begin = begnum;
					begnum = str.indexOf("\"", begin + 1);
				} else {
					if (str.substring(begnum + 1, begnum + 2).equals("}") || str.substring(begnum + 1, begnum + 2).equals(",") || str.substring(begnum + 1, begnum + 2).equals(":")) {
						begin = str.indexOf("\"", begin + 1);
					} else {
						begin = begnum;
						str = str.substring(0, begnum) + "“" + str.substring(begnum + 1, str.indexOf("\"", begin + 1)) + "”" + str.substring(str.indexOf("\"", begin + 1) + 1, str.length());
						begin = str.indexOf("\"", begin + 1);
					}
				}
			} else {
				return str;
			}
		}
	}
}
