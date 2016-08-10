package com.woting.crawler.scheme.QT.crawler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import com.spiritdata.framework.util.JsonUtils;
import com.woting.crawler.scheme.util.HttpUtils;
import com.woting.crawler.scheme.util.RedisUtils;

public class QTParseUtils {
	
	@SuppressWarnings("unchecked")
	public static void parseCategory(byte[] htmlByteArray, Map<String, Object> parseData){
		Elements els = null;
		Document doc = Jsoup.parse(new String(htmlByteArray),"UTF-8");
		List<Map<String, Object>> catelist = new ArrayList<Map<String,Object>>();
		try {
			els = doc.select("div[class=category]");
			if(els!=null&&!els.isEmpty()){
				for (Element el : els) {
					String str = el.select("div[class=title pull-left]").get(0).html();
					if (!str.equals("正在直播")&&!str.equals("主播")) {
						Map<String, Object> map = new HashMap<String,Object>();
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
						catelist.add(map);
					}
				}
			}
		} catch (Exception e) {e.printStackTrace();}
		RedisUtils.addQTCategory(parseData.get("CrawlerNum")+"", catelist);
	}

	@SuppressWarnings("unchecked")
	public static void parseAlbum(byte[] htmlByteArray, Map<String, Object> parseData){
		Elements els = null;
		Element el = null;
		Document doc = Jsoup.parse(new String(htmlByteArray),"UTF-8");
		//专辑名称,专辑id
		try {
			els = doc.select("a[data-switch-url]");
			if(els!=null && !els.isEmpty()){
				el = els.get(0);
				parseData.put("albumName", el.html());
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
				parseData.put("descript", el.html());
			}
		} catch (Exception e) {e.printStackTrace();}
		try {
			els = doc.select("li[class=playable clearfix]");
			if(els!=null&&!els.isEmpty()){
				for (Element e : els) {
					Map<String, Object> audiom = new HashMap<String,Object>();
					String jsonstr = e.attr("data-play-info");
					jsonstr = HttpUtils.getTextByDispose(jsonstr);
					if (jsonstr.contains("<a href=")) {
						int begnum = jsonstr.indexOf("thumb")-2;
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
					audiom.put("audioName", au.get("name"));
					audiom.put("audioId", au.get("id"));
					audiom.put("Duration", au.get("duration"));
					audiom.put("audioImg", au.get("thumb"));
					audiom.put("albumId",parseData.get("albumId"));
					audiom.put("albumName", parseData.get("albumName"));
					List<String> playlist = (List<String>) au.get("urls");
					audiom.put("PlayUrl","http://od.qingting.fm"+playlist.get(0));
					doc = Jsoup.connect("http://i.qingting.fm/wapi/program_playcount?pids="+parseData.get("albumId")+"_"+audiom.get("audioId")).timeout(10000).ignoreContentType(true).get();
					String str = doc.select("body").get(0).html();
					str = HttpUtils.getTextByDispose(str);
					Map<String, Object> map = (Map<String, Object>) JsonUtils.jsonToObj(str, Map.class);
					if(map!=null&&map.containsKey("data")){
						List<Map<String, Object>> l = (List<Map<String, Object>>) map.get("data");
						Map<String, Object> m = l.get(0);
						audiom.put("playCount",m.get("playcount"));
					}
					RedisUtils.addQTMa(parseData.get("CrawlerNum")+"", audiom);
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
			RedisUtils.addQTSeq(parseData.get("CrawlerNum")+"", parseData);
		} catch (Exception e) {e.printStackTrace();}
	}
	
	//专辑播放次数http://i.qingting.fm/wapi/channel_playcount?cids=115850
	//声音播放次数http://i.qingting.fm/wapi/program_playcount?pids=115850_5019265
}
