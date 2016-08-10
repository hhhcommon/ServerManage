package com.woting.crawler.scheme.KL.crawler;

import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.spiritdata.framework.util.JsonUtils;
import com.woting.crawler.scheme.util.HttpUtils;

public class KLParseUtils {

	/**
	 * 
	 * @param href
	 * @return 返回0为不符合类型，返回1为专辑，返回2为节目
	 */
	public static int getType(String href){
		if(href.length()<26) return 0;
		if(href.startsWith("http://www.kaolafm.com/zj/")) return 1;
		if (href.startsWith("http://www.kaolafm.com/jm/")) return 2;
		return 0;
	}
	
	public static void parseAlbum(byte[] htmlByteArray, Map<String, Object> parseData){
		Elements els = null;
		Element el = null;
		Document doc = Jsoup.parse(new String(htmlByteArray),"UTF-8");
		//专辑描述
		try {
			els = doc.select("meta[name=description]");
			if(els!=null && !els.isEmpty()){
				el = els.get(0);
				parseData.put("descript",el.attr("content"));
			}
		} catch (Exception e) {e.printStackTrace();}
		//专辑名称
		try {
			els = doc.select("title");
			if(els!=null && !els.isEmpty()){
				el = els.get(0);
				parseData.put("albumName",el.html());
			}
		} catch (Exception e) {e.printStackTrace();}
		//专辑封面
		try {
			els = doc.select("fl img pos_re");
			if(els!=null && !els.isEmpty()){
				el = els.get(0).select("img").get(0);
				parseData.put("imgUrl",el.attr("src"));
			}
		} catch (Exception e) {e.printStackTrace();}
		//专辑id
		try {
			els = doc.select("input[id=albumID]");
			if(els!=null && !els.isEmpty()){
				el = els.get(0);
				parseData.put("albumId", el.attr("value"));
			}
		} catch (Exception e) {e.printStackTrace();}
		//专辑标签
		try {
			els = doc.select("a[class=bd_d1]");
			if(els!=null && !els.isEmpty()){
				String tags = "";
				for (Element ele : els) {
					tags+=","+ele.html();
				}
				parseData.put("tags", tags.substring(1).trim());
			}
		} catch (Exception e) {e.printStackTrace();}
		//专辑播放数,分类名称,分类id,订阅数 
		try {
      	    Map<String, Object> m = HttpUtils.getJsonMapFromURL("http://www.kaolafm.com/webapi/albumdetail/get?albumid="+parseData.get("albumId"));
      	    Map<String, Object> result = (Map<String, Object>) m.get("result");
      	    parseData.put("playCount",result.get("listenNum")+"");
      	    parseData.put("categoryName", result.get("categoryName"));
    	    parseData.put("categoryId", result.get("categoryId"));
    	    parseData.put("subscribeNum", result.get("followedNum"));
    	    parseData.put("countNum", result.get("countNum"));
      	} catch (Exception e) {e.printStackTrace();}
		System.out.println("专辑信息###"+JsonUtils.objToJson(parseData));
	}
	
	public static void parseSond(byte[] htmlByteArray, Map<String, Object> parseData){
		Elements els=null;
        Element el=null;
        Document doc=Jsoup.parse(new String(htmlByteArray), "UTF-8");
        //声音id
        try {
			els = doc.select("input[id=audioID]");
			if(els!=null && !els.isEmpty()){
				el = els.get(0);
				parseData.put("audioId",el.attr("value"));
			}
		} catch (Exception e) {e.printStackTrace();}
        //声音描述
        try {
			els = doc.select("meta[name=description]");
			if(els!=null && !els.isEmpty()){
				el = els.get(0);
				parseData.put("descript",el.attr("content"));
			}
		} catch (Exception e) {e.printStackTrace();}
        //声音标签
      	try {
      	    els = doc.select("a[class=bd_d1]");
      		if(els!=null && !els.isEmpty()){
      			String tags = "";
      			for (Element ele : els) {
      				tags+=","+ele.html();
      			}
      			parseData.put("tags", tags.substring(1).trim());
      		}
      	} catch (Exception e) {e.printStackTrace();}
      	//声音内容
      	try {
      	    els = doc.select("p[class=alltext]");
      		if(els!=null && !els.isEmpty()){
      			el = els.get(0);
      			parseData.put("content", el.html());
      		}
      	} catch (Exception e) {e.printStackTrace();}
      	//声音播放次数,分类名,分类id,专辑id,播放地址,播放时长,创建时间,收藏数
      	try {
      	    Map<String, Object> m = HttpUtils.getJsonMapFromURL("http://www.kaolafm.com/webapi/audiodetail/get?id="+parseData.get("audioId"));
      	    Map<String, Object> result = (Map<String, Object>) m.get("result");
      	    parseData.put("playCount",result.get("listenNum"));
      	    parseData.put("categoryName", result.get("categoryName"));
      	    parseData.put("categoryId", result.get("categoryId"));
      	    parseData.put("likedNum", result.get("likedNum"));
      	    parseData.put("mp3PlayUrl", result.get("mp3PlayUrl"));
      	    parseData.put("mp3Duration",result.get("mp3Duration"));
      	    parseData.put("cTime",result.get("createTime"));
      	    parseData.put("albumId",result.get("albumId"));
      	    parseData.put("albumName",result.get("albumName"));
      	    parseData.put("audioPic",result.get("audioPic"));
      	} catch (Exception e) {e.printStackTrace();}
      	System.out.println("单体信息###"+JsonUtils.objToJson(parseData));
	}
}
