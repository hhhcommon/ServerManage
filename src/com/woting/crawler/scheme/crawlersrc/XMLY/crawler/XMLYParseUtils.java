package com.woting.crawler.scheme.crawlersrc.XMLY.crawler;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;
import org.apache.commons.lang3.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import com.spiritdata.framework.core.cache.SystemCache;
import com.spiritdata.framework.ext.spring.redis.RedisOperService;
import com.spiritdata.framework.util.DateUtils;
import com.woting.crawler.CrawlerConstants;
import com.woting.crawler.core.cperson.persis.po.CPersonPo;
import com.woting.crawler.core.cperson.service.CPersonService;
import com.woting.crawler.core.scheme.model.Scheme;
import com.woting.crawler.ext.SpringShell;
import com.woting.crawler.scheme.crawlerperson.XMLY.XMLYPersonUtils;
import com.woting.crawler.scheme.utils.CleanDataUtils;
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
	public static void parseAlbum(boolean isToRedis, byte[] htmlByteArray, Map<String, Object> parseData) {
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
			eles = doc.select("link[rel=canonical]");
			if (eles != null && !eles.isEmpty()) {
				String zhuboid = eles.get(0).attr("href").trim();
				zhuboid = zhuboid.substring(zhuboid.lastIndexOf("/")+1, zhuboid.length());
				parseData.put("albumId", zhuboid);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		//主播
		try {
			eles = doc.select("div[class=picture]");
			if (eles != null && !eles.isEmpty()) {
				String zhubos = eles.get(0).select("a").get(0).attr("href");
				zhubos = zhubos.replace("/zhubo/", "").replace("/", "");
				saveCPerson(zhubos, "hotspot_Album", parseData.get("albumId")+"");
			}
		} catch (Exception ex) {ex.printStackTrace();}
		if (isToRedis) {
			Scheme scheme = (Scheme) SystemCache.getCache(CrawlerConstants.SCHEME).getContent();
		    RedisOperService rs = new RedisOperService(scheme.getJedisConnectionFactory(), scheme.getRedisDB());
		    RedisUtils.addXMLYOriginalSeq(rs, parseData.get("CrawlerNum") + "", parseData);
		    rs.close();
		}
	}

	/**
	 * 分析声音
	 * 
	 * @param htmlByteArray
	 *            html内容
	 * @param parseData
	 *            返回的数据
	 */
	public static void parseSond(boolean isToRedis, byte[] htmlByteArray, Map<String, Object> parseData) {
		Elements eles = null;
		Element e = null;
		Document doc = Jsoup.parse(new String(htmlByteArray), "UTF-8");
//		Map<String, Object> pData = parseData;
		// 得到名称、ID、img
		try {
			eles = doc.select("img[sound_popsrc]");
			if (eles != null && !eles.isEmpty()) {
				e = eles.get(0);
				parseData.put("audioId", e.attr("sound_popsrc").trim());
				parseData.put("audioName", e.attr("alt").trim());
				parseData.put("audioImg", e.attr("src").trim());
			} else {
				return;
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
					String created_at = m.get("formatted_created_at")+"";
					String time_utils_now = m.get("time_until_now")+"";
					if (time_utils_now.contains("年")) {
						int year = CleanDataUtils.findInt(time_utils_now);
						year = new Date(System.currentTimeMillis()).getYear() - year + 1900;
						created_at = year+"年"+created_at;
						long date = DateUtils.getDateTime("yyyy年MM月dd日 HH:mm", created_at).getTime();
						parseData.put("cTime", date);
					} else {
						int year = new Date(System.currentTimeMillis()).getYear() + 1900;
						created_at = year+"年"+created_at;
						long date = DateUtils.getDateTime("yyyy年MM月dd日 HH:mm", created_at).getTime();
						parseData.put("cTime", date);
					}
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
		//主播
//		try {
//			eles = doc.select("div[class=picture]");
//			if (eles != null && !eles.isEmpty()) {
//				String zhubos = eles.get(0).select("a").get(0).attr("href");
//				zhubos = zhubos.replace("/zhubo/", "").replace("/", "");
//				saveCPerson(zhubos, "hotspot_Audio", parseData.get("audioId")+"");
//			}
//		} catch (Exception ex) {
//			ex.printStackTrace();
//		}
		if (isToRedis) {
			Scheme scheme = (Scheme) SystemCache.getCache(CrawlerConstants.SCHEME).getContent();
		    RedisOperService rs = new RedisOperService(scheme.getJedisConnectionFactory(), scheme.getRedisDB());
		    RedisUtils.addXMLYOriginalMa(rs, parseData.get("CrawlerNum") + "", parseData);
		    rs.close();
		}
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
				if (isDig) firstNumStr.append(c[i]);
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
		if (_firstNumStr.endsWith(".")) _firstNumStr += "0";
		float f = Float.parseFloat(_firstNumStr);
		f = f * gene;
		ret = (new Float(f)).longValue();
		return ret;
	}
	
	private static void saveCPerson(String cpersonId, String resTableName, String resId) {
		CPersonPo po = XMLYPersonUtils.parsePerson(cpersonId);
		if (po!=null) {
			po.setResTableName(resTableName);
			po.setResId(resId);
			po.setcTime(new Timestamp(System.currentTimeMillis()));
			CPersonService cPersonService = (CPersonService) SpringShell.getBean("CPersonService");
			cPersonService.insertPerson(po);
		}
	}
}