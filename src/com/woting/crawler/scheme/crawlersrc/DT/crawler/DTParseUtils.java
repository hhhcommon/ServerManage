package com.woting.crawler.scheme.crawlersrc.DT.crawler;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import com.spiritdata.framework.util.SequenceUUID;
import com.woting.crawler.core.cperson.persis.po.CPersonPo;
import com.woting.crawler.core.cperson.service.CPersonService;
import com.woting.crawler.ext.SpringShell;
import com.woting.crawler.scheme.crawlersrc.DT.etl.DTEtl1Process;
import com.woting.crawler.scheme.utils.HttpUtils;

public class DTParseUtils {
	public static void parseAlbum(boolean isToRedis, byte[] htmlByteArray, Map<String, Object> parseData){
		Elements eles = null;
		Document doc = Jsoup.parse(new String(htmlByteArray), "UTF-8");
		try {
			if (doc!=null) {
				eles = doc.select("tr");
				if (eles!=null && eles.size()>0) {
					Element ele = eles.get(0);
					String audioId = ele.attr("data-content-id");
					getAlbum(isToRedis, audioId, parseData);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void parseContent(boolean isToRedis, byte[] htmlByteArray, Map<String, Object> parseData) {
		Elements eles = null;
		Document doc = Jsoup.parse(new String(htmlByteArray), "UTF-8");
		try {
			if (doc!=null) {
				eles = doc.select("a[class=content-link]");
				if (eles!=null && eles.size()>0) {
					for (Element ele : eles) {
						String audioId = ele.attr("data-content-id");
						getAlbum(isToRedis, audioId, parseData);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	private static void getAlbum(boolean isToRedis,String audioId, Map<String, Object> parseData) {
		Map<String, Object> dateList = HttpUtils.getJsonMapFromURL("http://api.duotin.com/album/page?source=android&content_id="+audioId+"&sort_type=0&page_size=1");
		if (dateList!=null) {
			dateList = (Map<String, Object>) dateList.get("data");
			if (dateList!=null) {
				Map<String, Object> m = (Map<String, Object>) dateList.get("page");
				String count = m.get("total_count")+"";
				m = (Map<String, Object>) dateList.get("album");
				parseData.put("albumId", m.get("id"));
				parseData.put("albumName",m.get("title"));
				parseData.put("albumImg", m.get("image_url"));
				parseData.put("descript", m.get("describe"));
				parseData.put("playCount", m.get("play_num"));
				List<String> tagList = (List<String>) m.get("tag_list");
				if (tagList!=null && tagList.size()>0) {
					String tagstr = "";
					for (String tagM : tagList) {
						tagstr += ","+tagM;
					}
					tagstr = tagstr.substring(1);
					parseData.put("tags", tagstr);
				}
				parseData.put("cTime", m.get("updated"));
				if (isToRedis) {
					DTEtl1Process.makeKLOrigDataList(1, parseData);
				}
				
				dateList = (Map<String, Object>) dateList.get("podcast");
				if (dateList!=null && dateList.size()>0) {
					CPersonPo cPersonPo = new CPersonPo();
				    cPersonPo.setId(SequenceUUID.getPureUUID());
				    cPersonPo.setpName(dateList.get("real_name")+"");
				    cPersonPo.setPortrait(dateList.get("image_url")+"");
				    cPersonPo.setDescn(dateList.get("describe")+"");
				    cPersonPo.setpSource("多听");
				    cPersonPo.setpSrcId(dateList.get("id")+"");
				    cPersonPo.setcTime(new Timestamp(System.currentTimeMillis()));
				    CPersonService cPersonService = (CPersonService) SpringShell.getBean("CPersonService");
//				    CPersonPo cPo = cPersonService.getCPerson("多听", cPersonPo.getResId(), cPersonPo.getResTableName());
//				    if (cPo==null) {
//					    cPersonService.insertPerson(cPersonPo);
//				    }
				}
				
				//节目信息采集
				dateList = HttpUtils.getJsonMapFromURL("http://api.duotin.com/album/page?source=android&content_id="+audioId+"&sort_type=0&page_size="+count);
				if (dateList!=null) {
					dateList = (Map<String, Object>) dateList.get("data");
					List<Map<String, Object>> datas = (List<Map<String, Object>>) dateList.get("data_list");
					if (datas!=null && datas.size()>0) {
						for (Map<String, Object> mm : datas) {
							Map<String, Object> audioMap = new HashMap<>();
					        audioMap.put("audioId", mm.get("id"));
					        audioMap.put("audioName", mm.get("title"));
					        audioMap.put("audioImg", parseData.get("albumImg"));
					        audioMap.put("albumId", parseData.get("albumId"));
					        audioMap.put("albumName", parseData.get("albumName"));
					        audioMap.put("playUrl", mm.get("audio_32_url"));
					        audioMap.put("duration", mm.get("duration"));
					        audioMap.put("cTime", mm.get("updated"));
					        audioMap.put("playCount", mm.get("play_num"));
					        if (isToRedis) {
								DTEtl1Process.makeKLOrigDataList(2, audioMap);
							}
						}
					}
				}
			}
		}
	}
}
