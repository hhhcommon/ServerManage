package com.woting.crawler.scheme.crawlersrc.KL.crawler;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.woting.crawler.core.cperson.persis.po.CPersonPo;
import com.woting.crawler.core.cperson.service.CPersonService;
import com.woting.crawler.ext.SpringShell;
import com.woting.crawler.scheme.crawlerperson.KL.KLPersonUtils;
import com.woting.crawler.scheme.crawlersrc.KL.etl.KLEtl1Process;
import com.woting.crawler.scheme.crawlersrc.KL.etl.KLHashCode;
import com.woting.crawler.scheme.utils.HttpUtils;


public class KLParseUtils {

	@SuppressWarnings("unchecked")
	public static void parseAlbum(boolean isToRedis, byte[] htmlByteArray, Map<String, Object> parseData){
		Document doc = Jsoup.parse(new String(htmlByteArray),"UTF-8");
		Elements eles = doc.select("input[id=albumID]");
		try {
			String visitUrl = doc.select("link[rel=canonical]").get(0).attr("href");
			parseData.put("visitUrl", visitUrl);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			if (eles!=null && !eles.isEmpty()) {
				String albumId = eles.get(0).attr("value");
				parseData.put("albumId", albumId);
				Map<String, Object> m = HttpUtils.getJsonMapFromURL("http://www.kan8kan.com/webapi/albumdetail/get?albumid="+albumId);
				if (m!=null && m.size()>0) {
					m = (Map<String, Object>) m.get("result");
					parseData.put("albumName",m.get("name"));
					parseData.put("albumImg", m.get("pic"));
					parseData.put("descript", m.get("radioDesc"));
					parseData.put("playCount", m.get("countNum"));
					List<String> kwlist = (List<String>) m.get("keyWords");
					String kws = "";
					if (kwlist!=null && kwlist.size()>0) {
						for (String str : kwlist) {
							kws += ","+str;
						}
						kws = kws.substring(1);
						parseData.put("tags", kws);
					}
					m = HttpUtils.getJsonMapFromURL("http://www.kan8kan.com/webapi/audios/list?id="+albumId+"&pagesize=1&pagenum=1&sorttype=1");
					if (m!=null && m.size()>0) {
						m = (Map<String, Object>) m.get("result");
						int countNum = Integer.valueOf(m.get("count")+"");
						int page = countNum/20+1;
						for (int i = 1; i <= page; i++) {
							try {
								m = HttpUtils.getJsonMapFromURL("http://www.kan8kan.com/webapi/audios/list?id="+albumId+"&pagesize=20&pagenum="+i+"&sorttype=1");
								if (m!=null && m.size()>0) {
									m = (Map<String, Object>) m.get("result");
									List<Map<String, Object>> dataList = (List<Map<String, Object>>) m.get("dataList");
									if (dataList!=null && dataList.size()>0) {
										for (Map<String, Object> mm : dataList) {
											Map<String, Object> audioMap = new HashMap<>();
									        audioMap.put("audioId", mm.get("audioId"));
									        audioMap.put("audioName", mm.get("audioName"));
									        audioMap.put("audioImg", mm.get("audioPic"));
									        audioMap.put("descript", mm.get("audioDes"));
									        audioMap.put("albumId", mm.get("albumId"));
									        audioMap.put("albumName", mm.get("albumName"));
									        audioMap.put("playUrl", mm.get("mp3PlayUrl"));
									        audioMap.put("categoryId", mm.get("categoryId"));
									        audioMap.put("duration", mm.get("mp3Duration"));
									        audioMap.put("cTime", mm.get("createTime"));
									        audioMap.put("playCount", mm.get("listenNum"));
									        String url = "http://www.kan8kan.com/jm/"+KLHashCode.decodeKL(mm.get("audioId")+"")+".html";
									        audioMap.put("visitUrl", url);
									        doc = HttpUtils.getJsonStrForUrl(url);
									        //标签
											try {
												eles = doc.select("a[class=bd_d1]");
												if (eles!=null && eles.size()>0) {
													String tags = "";
													for (Element el : eles) {
														tags += ","+el.html();
													}
													tags = tags.substring(1);
													audioMap.put("tags", tags);
												}
											} catch (Exception e) {
												e.printStackTrace();
											} finally {
												if (isToRedis) {
									                KLEtl1Process.makeKLOrigDataList(2, audioMap);
									            }
											}
										}
									}
								}
							} catch (Exception e) {
								e.printStackTrace();
								continue;
							}
						}
					}
				}
				try {
					saveCPerson(albumId, "hotspot_Album");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (isToRedis) {
			KLEtl1Process.makeKLOrigDataList(1, parseData);
		}
	}
	
	
	@SuppressWarnings("unchecked")
	public static void parseSond(boolean isToRedis, byte[] htmlByteArray, Map<String, Object> parseData) {
		Elements eles = null;
		Document doc = Jsoup.parse(new String(htmlByteArray), "UTF-8");
		try {
			String visitUrl = doc.select("link[rel=canonical]").get(0).attr("href");
			parseData.put("visitUrl", visitUrl);
		} catch (Exception e) {
			e.printStackTrace();
		}
		//标签
		try {
			eles = doc.select("a[class=bd_d1]");
			if (eles!=null && eles.size()>0) {
				String tags = "";
				for (Element el : eles) {
					tags += ","+el.html();
				}
				tags = tags.substring(1);
				parseData.put("tags", tags);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			eles = doc.select("input[id=audioID]");
			if (eles!=null && !eles.isEmpty()) {
				String audioId = eles.get(0).attr("value");
				parseData.put("audioId", audioId);
				Map<String, Object> m = HttpUtils.getJsonMapFromURL("http://www.kan8kan.com/webapi/audiodetail/get?id="+audioId);
				if (m!=null && m.size()>0) {
					m = (Map<String, Object>) m.get("result");
					parseData.put("audioName", m.get("audioName"));
					parseData.put("audioImg", m.get("audioPic"));
					parseData.put("descript", m.get("audioDes"));
					parseData.put("albumId", m.get("albumId"));
					parseData.put("albumName", m.get("albumName"));
					parseData.put("playUrl", m.get("mp3PlayUrl"));
					parseData.put("categoryId",m.get("categoryId"));
					parseData.put("duration", m.get("mp3Duration"));
					parseData.put("cTime", m.get("createTime"));
					parseData.put("playCount", m.get("listenNum"));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (isToRedis) {
			KLEtl1Process.makeKLOrigDataList(2, parseData);
		}
	}
	
	
	private static void saveCPerson(String albumId, String resTableName) {
		CPersonPo po = KLPersonUtils.parsePerson(albumId);
		if (po!=null) {
			po.setcTime(new Timestamp(System.currentTimeMillis()));
			CPersonService cPersonService = (CPersonService) SpringShell.getBean("CPersonService");
			cPersonService.insertPerson(po);
		}
	}
}
