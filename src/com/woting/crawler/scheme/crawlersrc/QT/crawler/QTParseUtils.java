package com.woting.crawler.scheme.crawlersrc.QT.crawler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.spiritdata.framework.core.cache.SystemCache;
import com.spiritdata.framework.ext.spring.redis.RedisOperService;
import com.spiritdata.framework.util.DateUtils;
import com.spiritdata.framework.util.JsonUtils;
import com.woting.crawler.CrawlerConstants;
import com.woting.crawler.core.cperson.persis.po.CPersonPo;
import com.woting.crawler.core.cperson.service.CPersonService;
import com.woting.crawler.core.scheme.model.Scheme;
import com.woting.crawler.ext.SpringShell;
import com.woting.crawler.scheme.crawlerperson.QT.QTPersonUtils;
import com.woting.crawler.scheme.utils.HttpUtils;
import com.woting.crawler.scheme.utils.RedisUtils;

public class QTParseUtils {
	
	@SuppressWarnings("unchecked")
	public static void parseAlbum(byte[] htmlByteArray, Map<String, Object> parseData){
		Elements els = null;
		Element el = null;
		Document doc = Jsoup.parse(new String(htmlByteArray),"UTF-8");
		Map<String, Object> pDate = new HashMap<String,Object>();
		pDate.putAll(parseData);
		//专辑名称,专辑id
		try {
			els = doc.select("a[data-switch-url]");
			if(els!=null && !els.isEmpty()){
				el = els.get(0);
				parseData.put("albumName", HttpUtils.getTextByDispose(el.html()));
				parseData.put("albumId",el.attr("href").replace("/vchannels/", ""));
			}
		} catch (Exception e) {e.printStackTrace();}
		//专辑封面
		try {
			els = doc.select("div[class=channel-info clearfix]");
			if(els!=null && !els.isEmpty()) {
				el = els.get(0).select("img").get(0);
				parseData.put("albumImg", el.attr("src"));
			}
		} catch (Exception e) {e.printStackTrace();}
		//专辑简介
		try {
			els = doc.select("div[class=abstract clearfix]").select("div[class=content]");
			if(els!=null && !els.isEmpty()) {
				el = els.get(0);
				parseData.put("descript", HttpUtils.getTextByDispose(el.html()));
			}
		} catch (Exception e) {e.printStackTrace();}
		//主播
		try {
			CPersonPo po = QTPersonUtils.parsePerson(parseData.get("albumId")+"");
			saveCPerson(po, "hotspot_Album", parseData.get("albumId")+"");
		} catch (Exception e) {e.printStackTrace();}
//		int num = 0;
		Scheme scheme = (Scheme) SystemCache.getCache(CrawlerConstants.SCHEME).getContent();
		RedisOperService rs = new RedisOperService(scheme.getJedisConnectionFactory(), 1);
		try {
			els = doc.select("li[class=playable clearfix]");
			if(els!=null&&!els.isEmpty()) {
				for (Element e : els) {
					String jsonstr = e.attr("data-play-info");
					jsonstr = HttpUtils.getTextByDispose(jsonstr);
					if (jsonstr.contains("<a href=")) {
						int begnum = jsonstr.indexOf("thumb")-3;
						int lbegnum = jsonstr.indexOf("{", begnum);
						if (lbegnum!=-1) {
							int lendnum = jsonstr.indexOf("}", lbegnum)+1;
							jsonstr = jsonstr.replace(jsonstr.substring(begnum, lendnum), "");
						}else {
							int endnum = jsonstr.indexOf("}", lbegnum)-1;
							jsonstr = jsonstr.replace(jsonstr.substring(begnum,endnum), "");
						}
					}
					Map<String, Object> au = (Map<String, Object>) JsonUtils.jsonToObj(jsonstr, Map.class);
					pDate.put("audioName", au.get("name"));
					pDate.put("audioId", au.get("id"));
					pDate.put("duration", au.get("duration"));
					pDate.put("audioImg", parseData.get("albumImg"));
					pDate.put("albumId",parseData.get("albumId"));
					pDate.put("albumName", parseData.get("albumName"));
					List<String> playlist = (List<String>) au.get("urls");
					pDate.put("playUrl","http://od.qingting.fm"+playlist.get(0));
					doc = Jsoup.connect("http://i.qingting.fm/wapi/program_playcount?pids="+parseData.get("albumId")+"_"+pDate.get("audioId")).timeout(10000).ignoreContentType(true).get();
					String str = doc.select("body").get(0).html();
					str = HttpUtils.getTextByDispose(str);
					Map<String, Object> map = (Map<String, Object>) JsonUtils.jsonToObj(str, Map.class);
					if(map!=null&&map.containsKey("data")){
						List<Map<String, Object>> l = (List<Map<String, Object>>) map.get("data");
						Map<String, Object> m = l.get(0);
						pDate.put("playCount",m.get("playcount"));
					}
					doc = Jsoup.connect("http://api2.qingting.fm/v6/media/programs/"+au.get("id")).ignoreContentType(true).get();
					str = doc.select("body").get(0).html();
					str = HttpUtils.getTextByDispose(str);
					map = (Map<String, Object>) JsonUtils.jsonToObj(str, Map.class);
					if(map!=null&&map.containsKey("data")){
						Map<String, Object> m = (Map<String, Object>) map.get("data");
						String datestr = m.get("update_time") + "";
						long date =  DateUtils.getDateTime("yyyy-MM-dd HH:mm:ss", datestr).getTime();
						pDate.put("cTime", date);
					}
					RedisUtils.addQTAudio(rs, parseData.get("CrawlerNum")+"", pDate);
//					num++;
//					if(num==2){
//						num=0;
//						break;
//					}
				}
			}
		} catch (Exception e) {e.printStackTrace();}
		//专辑播放次数
		try {
			doc = Jsoup.connect("http://i.qingting.fm/wapi/channel_playcount?cids="+parseData.get("albumId")).timeout(10000).ignoreContentType(true).get();
			String str = doc.select("body").get(0).html();
			str = HttpUtils.getTextByDispose(str);
			Map<String, Object> map = (Map<String, Object>) JsonUtils.jsonToObj(str, Map.class);
			if(map!=null&&map.containsKey("data")){
				List<Map<String, Object>> l = (List<Map<String, Object>>) map.get("data");
				Map<String, Object> m = l.get(0);
				parseData.put("playCount",m.get("playcount"));
			}
			RedisUtils.addQTAlbum(rs, parseData.get("CrawlerNum")+"", parseData);
		} catch (Exception e) {e.printStackTrace();}
		finally {
			rs.close();
		}
	}
	
	@SuppressWarnings("unchecked")
	public static void parseQTResourceIdAndCategoryId(byte[] htmlByteArray, Map<String, Object> parseData) {
		Elements els = null;
		Map<String, Object> map = new HashMap<String,Object>();
		try {
			Document doc = Jsoup.parse(new String(htmlByteArray, "UTF-8"));
			els = doc.select("div[class=category]");
			if(els!=null&&!els.isEmpty()){
				for (Element el : els) {
					String str = el.select("div[class=title pull-left]").get(0).html();
					if (!str.equals("正在直播")&&!str.equals("主播")) {
						String cateName = str;
						String cateId = el.select("a[class=more pull-left]").get(0).attr("href").replace("/supervcategories/", "");
						Elements es = el.select("li[class=playable]");
						for (Element e : es) {
							Map<String, Object> m = (Map<String, Object>) JsonUtils.jsonToObj(HttpUtils.getTextByDispose(e.attr("data-play-info")), Map.class);
							String albumId = m.get("parentid")+"";
							if (!albumId.equals("null")) {
								map.put(albumId, cateId+"::"+cateName);
							}
						}
					}
				}
			}
		} catch (Exception e) {e.printStackTrace();}
		Scheme scheme = (Scheme) SystemCache.getCache(CrawlerConstants.SCHEME).getContent();
		RedisOperService rs = new RedisOperService(scheme.getJedisConnectionFactory(), 1);
		RedisUtils.addQTCategory(rs, parseData.get("CrawlerNum")+"", map);
		rs.close();
	}
	
	private static void saveCPerson(CPersonPo po, String resTableName, String resId) {
		if (po!=null) {
			po.setResTableName(resTableName);
			po.setResId(resId);
			CPersonService cPersonService = (CPersonService) SpringShell.getBean("CPersonService");
			cPersonService.insertPerson(po);
		}
	}
}
