package com.woting.crawler.scheme.QT.crawler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import com.spiritdata.framework.util.JsonUtils;
import com.woting.crawler.scheme.util.HttpUtils;

public class QTParseUtils {

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
					Map<String, Object> au = (Map<String, Object>) JsonUtils.jsonToObj(jsonstr, Map.class);
					audiom.put("audioName", au.get("name"));
					audiom.put("audioId", au.get("id"));
					audiom.put("Duration", au.get("duration"));
					audiom.put("img", au.get("thumb"));
					audiom.put("albumId",parseData.get("albumId"));
					audiom.put("albumName", parseData.get("albumName"));
					List<String> playlist = (List<String>) au.get("urls");
					audiom.put("PlayUrl","http://od.qingting.fm"+playlist.get(0));
					doc = Jsoup.connect("http://i.qingting.fm/wapi/program_playcount?pids="+parseData.get("albumId")+"_"+audiom.get("audioId")).ignoreContentType(true).get();
					doc = Jsoup.connect("http://i.qingting.fm/wapi/channel_playcount?cids="+parseData.get("albumId")).ignoreContentType(true).get();
					String str = doc.select("body").get(0).html();
					str = HttpUtils.getTextByDispose(str);
					Map<String, Object> map = (Map<String, Object>) JsonUtils.jsonToObj(str, Map.class);
					if(map!=null&&map.containsKey("data")){
						List<Map<String, Object>> l = (List<Map<String, Object>>) map.get("data");
						Map<String, Object> m = l.get(0);
						audiom.put("playCount",m.get("playcount"));
					}
					System.out.println(JsonUtils.objToJson(audiom));
				}
			}
		} catch (Exception e) {e.printStackTrace();}
		//专辑
		try {
			doc = Jsoup.connect("http://i.qingting.fm/wapi/channel_playcount?cids="+parseData.get("albumId")).ignoreContentType(true).get();
			String str = doc.select("body").get(0).html();
			str = HttpUtils.getTextByDispose(str);
			Map<String, Object> map = (Map<String, Object>) JsonUtils.jsonToObj(str, Map.class);
			if(map!=null&&map.containsKey("data")){
				List<Map<String, Object>> l = (List<Map<String, Object>>) map.get("data");
				Map<String, Object> m = l.get(0);
				parseData.put("playCount",m.get("playcount"));
			}
		} catch (Exception e) {e.printStackTrace();}
	}
	
	//专辑播放次数http://i.qingting.fm/wapi/channel_playcount?cids=115850
	//声音播放次数http://i.qingting.fm/wapi/program_playcount?pids=115850_5019265
}
