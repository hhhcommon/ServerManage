package com.woting.crawler.scheme.utils;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;

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
	
	/**
	 * 搜索内容中文转url编码
	 * 
	 * @param s 搜索的中文内容
	 * @return 返回转成的url编码
	 */
	public static String utf8TOurl(String s) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c >= 0 && c <= 255) {
				if (c >= 48 && c<= 57) { //数字
					sb.append(c);
				}
				if (c >= 65 && c<= 90) {
					sb.append(c);
				}
				if (c >= 97 && c<= 122) {
					sb.append(c);
				}
			} else {
				byte[] b;
				try {
					b = String.valueOf(c).getBytes("utf-8");
				} catch (Exception ex) {
					b = new byte[0];
				}
				for (int j = 0; j < b.length; j++) {
					int k = b[j];
					if (k < 0)
						k += 256;
					sb.append("%" + Integer.toHexString(k).toUpperCase());
				}
			}
		}
		return sb.toString();
	}
	
	public static int findInt(String str) {
		char[] s = str.toCharArray();
		String d = "";
		for (int i = 0; i < s.length; i++) {
			if (Character.isDigit(s[i])) {
				d += s[i];
			}
		}
		return Integer.valueOf(d);
	}
	
	public static String CleanDescnStr(String valuestr, String... tag) {
		if (tag!=null && tag.length>0) {
			int[] instrnum = new int[tag.length];
			List<String> introtag = new ArrayList<>();
			for (int i = 0; i < tag.length; i++) {
				introtag.add(tag[i]);
				instrnum[i] = tag[i].length();
			}
			valuestr = StringEscapeUtils.unescapeHtml4(valuestr);
			String begstr = valuestr.substring(0, valuestr.indexOf(introtag.get(0))+instrnum[0]);
			String introstr = "";
			for (int i = 0; i < introtag.size()-1; i++) {
				for (int j = i+1; j < introtag.size(); j++) {
					if (valuestr.contains(introtag.get(i))) {
						if (valuestr.contains(introtag.get(j))) {
							String instr = valuestr.substring(valuestr.indexOf(introtag.get(i))+instrnum[i], valuestr.indexOf(introtag.get(j)));
							instr = instr.replace("\"", "'").replace("\\", "");
							instr += introtag.get(j);
							introstr += instr;
							break;
						}
					} else {
						break;
					}
				}
			}
			String endstr = valuestr.substring(valuestr.indexOf(introtag.get(tag.length-1))+instrnum[tag.length-1], valuestr.length());
			return begstr+introstr+endstr;
		}
		return null;
	}
}
