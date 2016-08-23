package com.woting.crawler.scheme.XMLY.crawler;

import java.util.Map;
import org.apache.commons.lang3.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.woting.crawler.scheme.utils.HttpUtils;
import com.woting.crawler.scheme.utils.RedisUtils;

public abstract class XMLYParseUtils {
	
	/**
	 * 分析专辑
	 * 
	 * @param htmlByteArray
	 *            html内容
	 * @param parseData
	 *            返回的数据
	 */
	public static void parseAlbum(byte[] htmlByteArray, Map<String, Object> parseData) {
		Elements eles = null;
		Element e = null;
		Document doc = Jsoup.parse(new String(htmlByteArray), "UTF-8");
		// 得到名称、ID、img
		try {
			eles = doc.select("div.personal_body").select("div.left").select("img");
			if (eles != null && !eles.isEmpty()) {
				e = eles.get(0);
				parseData.put("albumName", e.attr("alt").trim());
				parseData.put("albumImg", e.attr("src").trim());
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		// 类别
		try {
			eles = doc.select("div.detailContent_category");
			if (eles != null && !eles.isEmpty()) {
				e = eles.get(0);
				parseData.put("categoryName", e.select("a").get(0).html().trim().replace("【", "").replace("】", ""));
				parseData.put("playUrl", e.select("a").get(0).attr("href").trim());
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		// 标签
		try {
			String tags = "";
			eles = doc.select("div.tagBtnList");
			if (eles != null && !eles.isEmpty()) {
				eles = eles.select("span");
				for (int i = 0; i < eles.size(); i++) {
					e = eles.get(i);
					tags += "," + e.select("span").html();
				}
			}
			if (tags.length() > 0)
				parseData.put("tags", tags.substring(1).trim());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		// 播放数
		try {
			eles = doc.select("div.detailContent_playcountDetail");
			if (eles != null && !eles.isEmpty()) {
				parseData.put("playCount", XMLYParseUtils.getFirstNum(eles.select("span").html()));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		// 描述
		try {
			eles = doc.select("div.detailContent_intro");
			if (eles != null && !eles.isEmpty()) {
				parseData.put("descript", StringEscapeUtils.unescapeHtml4(eles.select("div.mid_intro").select("article").get(0).html().trim()));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		// 专辑
		try {
			eles = doc.select("a.shareLink");
			if (eles != null && !eles.isEmpty()) {
				parseData.put("albumId", eles.get(0).attr("album_id").trim());
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		RedisUtils.addXMLYOriginalSeq(parseData.get("CrawlerNum") + "", parseData);
	}

	/**
	 * 分析声音
	 * 
	 * @param htmlByteArray
	 *            html内容
	 * @param parseData
	 *            返回的数据
	 */
	public static void parseSond(byte[] htmlByteArray, Map<String, Object> parseData) {
		Elements eles = null;
		Element e = null;
		Document doc = Jsoup.parse(new String(htmlByteArray), "UTF-8");
		Map<String, Object> pData = parseData;
		// 得到名称、ID、img
		try {
			eles = doc.select("img[sound_popsrc]");
			if (eles != null && !eles.isEmpty()) {
				e = eles.get(0);
				parseData.put("audioId", e.attr("sound_popsrc").trim());
				parseData.put("audioName", e.attr("alt").trim());
				parseData.put("audioImg", e.attr("src").trim());
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		// 声音
		try {
			eles = doc.select("div.detail_soundBox2");
			// 播放URL
			Map<String, Object> m = HttpUtils.getJsonMapFromURL("http://www.ximalaya.com/tracks/" + parseData.get("audioId") + ".json");
			if (m != null) {
				if ((parseData.get("audioName") + "").equals(m.get("title") + "")) {
					parseData.put("playUrl", m.get("play_path"));
					parseData.put("duration", m.get("duration"));
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		if (parseData.get("playUrl") == null)
			return;// 若获得不到声音，则不用进行后续处理了，没有声音的也入原始库
		// 类别
		try {
			eles = doc.select("div.detailContent_category");
			if (eles != null && !eles.isEmpty()) {
				e = eles.get(0);
				String catename = e.select("a").get(0).html().trim().replace("【", "").replace("】", "");
				if(catename.equals("外语"))
					catename = "英语";
				parseData.put("categoryName", catename);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		// 标签
		try {
			String tags = "";
			eles = doc.select("div.tagBtnList");
			if (eles != null && !eles.isEmpty()) {
				eles = eles.select("span");
				for (int i = 0; i < eles.size(); i++) {
					e = eles.get(i);
					tags += "," + e.select("span").html();
				}
			}
			if (tags.length() > 0)
				parseData.put("tags", tags.substring(1));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		// 播放数
		try {
			eles = doc.select("div.soundContent_playcount");
			if (eles != null && !eles.isEmpty()) {
				parseData.put("playCount", XMLYParseUtils.getFirstNum(eles.get(0).html()));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		// 描述
		try {
			eles = doc.select("div.detailContent_intro");
			if (eles != null && !eles.isEmpty()) {
				parseData.put("descript", HttpUtils.cleanTag(eles.get(0).select("article").get(0).html().trim()));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		// 专辑
		try {
			eles = doc.select("div.albumBar");
			if (eles != null && !eles.isEmpty()) {
				eles = eles.get(0).select("li");
				if (eles != null && !eles.isEmpty()) {
					e = eles.first();
					if (e != null) {
						e = e.select("div.right").get(0);
						parseData.put("albumName", e.select("a").get(0).attr("title").trim());
						String s = e.select("a").get(0).attr("href");
						if (s.startsWith("/"))
							s = s.substring(1);
						String[] _s = s.split("/");
						if (_s.length == 3) {
							parseData.put("albumId", _s[2].trim());
						}
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		RedisUtils.addXMLYOriginalMa(parseData.get("CrawlerNum") + "", parseData);
	}

	/**
	 * 分析主播
	 * 
	 * @param htmlByteArray
	 *            html内容
	 * @param parseData
	 *            返回的数据 public static void parseZhubo(byte[] htmlByteArray,
	 *            Map<String, Object> parseData) { }
	 * 
	 *            /** 分析标签
	 * @param htmlByteArray
	 *            html内容
	 * @param parseData
	 *            返回的数据 public static void parseTags(byte[] htmlByteArray,
	 *            Map<String, Object> parseData) { }
	 */

	public static long getFirstNum(String str) {
		long ret = 0l;
		StringBuffer firstNumStr = new StringBuffer();
		char[] c = str.toCharArray();
		boolean begin = false, end = false, isDig = false, isFirstDot = true;
		int i = 0;
		for (; i < c.length; i++) {
			isDig = Character.isDigit(c[i]);
			if (isDig && !begin)
				begin = true;
			if (begin) {
				if (isDig)
					firstNumStr.append(c[i]);
				if (!isDig) {
					// 是.
					if (c[i] == '.') {
						if (isFirstDot) {
							isFirstDot = false;
							firstNumStr.append(c[i]);
							continue;
						}
					}
					if (c[i] == '万')
						firstNumStr.append(c[i]);
					end = true;
				}
			}
			if (end)
				break;
		}
		String _firstNumStr = firstNumStr.toString();
		int gene = 1;
		if (_firstNumStr.endsWith("万")) {
			gene = 10000;
			_firstNumStr = _firstNumStr.substring(0, _firstNumStr.length() - 1);
		}
		if (_firstNumStr.endsWith("."))
			_firstNumStr += "0";
		float f = Float.parseFloat(_firstNumStr);
		f = f * gene;
		ret = (new Float(f)).longValue();
		return ret;
	}
}