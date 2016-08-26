package com.woting.crawler.scheme.QT.crawler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import com.spiritdata.framework.util.JsonUtils;
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
			if(els!=null && !els.isEmpty()){
				el = els.get(0).select("img").get(0);
				parseData.put("albumImg", el.attr("src"));
			}
		} catch (Exception e) {e.printStackTrace();}
		//专辑简介
		try {
			els = doc.select("div[class=abstract clearfix]").select("div[class=content]");
			if(els!=null && !els.isEmpty()){
				el = els.get(0);
				parseData.put("descript", HttpUtils.getTextByDispose(el.html()));
			}
		} catch (Exception e) {e.printStackTrace();}
		try {
			els = doc.select("li[class=playable clearfix]");
			if(els!=null&&!els.isEmpty()){
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
					RedisUtils.addQTAudio(parseData.get("CrawlerNum")+"", pDate);
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
			RedisUtils.addQTAlbum(parseData.get("CrawlerNum")+"", parseData);
		} catch (Exception e) {e.printStackTrace();}
	}
	
	//专辑播放次数http://i.qingting.fm/wapi/channel_playcount?cids=115850
	//声音播放次数http://i.qingting.fm/wapi/program_playcount?pids=115850_5019265
	
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
		RedisUtils.addQTCategory(parseData.get("CrawlerNum")+"", map);
	}
}
