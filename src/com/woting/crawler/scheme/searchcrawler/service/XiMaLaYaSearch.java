package com.woting.crawler.scheme.searchcrawler.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import com.spiritdata.framework.ext.spring.redis.RedisOperService;
import com.spiritdata.framework.util.SequenceUUID;
import com.woting.crawler.core.album.persis.po.AlbumPo;
import com.woting.crawler.core.audio.persis.po.AudioPo;
import com.woting.crawler.ext.SpringShell;
import com.woting.crawler.scheme.crawlersrc.XMLY.crawler.XMLYParseUtils;
import com.woting.crawler.scheme.searchcrawler.utils.SearchUtils;
import com.woting.crawler.scheme.utils.HttpUtils;

public class XiMaLaYaSearch extends Thread {

	private static int S_S_NUM = 5; // 搜索频道的数目
	private static int S_F_NUM = 5; // 搜索频道内节目的数目
	private static int F_NUM = 5; // 搜索节目的数目 以上排列顺序按照搜索到的排列顺序
	private static int T = 5000;
	private String constr;
	private Map<String, Object> result = new HashMap<>();
	private int okNum = 0;

	public XiMaLaYaSearch(String constr) {
		this.constr = constr;
	}
	
	public XiMaLaYaSearch() {
		
	}

	private void ximalayaService(String content) {
		new Thread(new Runnable() {
			public void run() {
				stationS(content);
			}
		}).start();
		new Thread(new Runnable() {
			public void run() {
				audiosS(content);
			}
		}).start();
	}

	// 专辑搜索
	private void stationS(String content) {
		String url = "http://www.ximalaya.com/search/" + SearchUtils.utf8TOurl(content) + "/t3";
		Document doc = null;
		try {
			doc = HttpUtils.getJsonStrForUrl(url);
			Elements elements = doc.select("div[class=content_wrap2]");
			for (int i = 0; i < (elements.size() > S_S_NUM ? S_S_NUM : elements.size()); i++) {
				AlbumPo albumPo = new AlbumPo();
				albumPo.setId(SequenceUUID.getPureUUID());
				albumPo.setAlbumPublisher("喜马拉雅");
				Element element1 = elements.get(i).select("a[class=albumface100]").get(0);
				String hrefstation = element1.attr("href");
				albumPo = albumS("http://www.ximalaya.com"+hrefstation);
				albumPo.setVisitUrl("http://www.ximalaya.com"+hrefstation);
				String stationpic = element1.select("span").select("img").attr("src");
				albumPo.setAlbumId(hrefstation.substring(hrefstation.indexOf("/album/")+7, hrefstation.length()));
				albumPo.setAlbumImg(stationpic);
				Elements eles = doc.select("div.detailContent_intro");
				if (eles != null && !eles.isEmpty()) {
					albumPo.setDescn(StringEscapeUtils.unescapeHtml4(eles.select("div.rich_intro").select("article").get(0).html().trim()));
				}
				List<AudioPo> aus = albumAudioS(hrefstation);
				albumPo.setcTime(new Timestamp(System.currentTimeMillis()));
				if (aus != null && aus.size() > 0) {
					albumPo.setAudioPos(aus);
					result.put(albumPo.getAlbumName() + "::" + albumPo.getAlbumId(), albumPo);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			okNum++;
		}
	}
	
	public AlbumPo albumS(String url) {
		Document doc = HttpUtils.getJsonStrForUrl(url);
		if (doc!=null) {
			Map<String, Object> parseData = new HashMap<>();
			XMLYParseUtils.parseAlbum(false, doc.toString().getBytes(), parseData);
			AlbumPo albumPo = new AlbumPo();
			albumPo.setId(SequenceUUID.getPureUUID());
			albumPo.setAlbumId(parseData.get("albumId")+"");
			albumPo.setAlbumName(parseData.get("albumName")+"");
			albumPo.setAlbumImg(parseData.get("albumImg")+"");
			albumPo.setCategoryName(parseData.get("categoryName")+"");
			albumPo.setAlbumPublisher("喜马拉雅");
			albumPo.setPlayCount(parseData.get("playCount")+"");
			albumPo.setAlbumTags(parseData.get("tags")+"");
			albumPo.setDescn(parseData.get("descript")+"");
			albumPo.setVisitUrl(parseData.get("visitUrl")+"");
			return albumPo;
		}
		return null;
	}

	/**
	 * 专辑下的节目搜索
	 * 
	 * @param contentid
	 * @return
	 */
	public List<AudioPo> albumAudioS(String contentid) {
		String url = "http://www.ximalaya.com" + contentid;
		List<AudioPo> aus = new ArrayList<>();
		Document doc = null;
		doc = HttpUtils.getJsonStrForUrl(url);
		Elements elements = doc.select("li[sound_id]");
		for (int i = 0; i < (elements.size() > S_F_NUM ? S_F_NUM : elements.size()); i++) {
			url = "http://www.ximalaya.com" + elements.get(i).select("a[class=title]").attr("href");
			AudioPo audioPo = audioS(url);
			if (audioPo!=null) {
				aus.add(audioPo);
			}
		}
		return aus;
	}

	/**
	 * 根据节目id进行节目信息获取
	 * 
	 * @param contendid
	 * @param festival
	 * @return
	 */
	public AudioPo audioS(String url) {
		Document docc = HttpUtils.getJsonStrForUrl(url);
		Map<String, Object> parseData = new HashMap<>();
		XMLYParseUtils.parseSond(false, docc.toString().getBytes(), parseData);
		if (parseData != null && parseData.containsKey("audioId") && parseData.get("audioId")!=null && !parseData.get("audioId").equals("null")) {
			AudioPo audioPo = new AudioPo();
			audioPo.setId(SequenceUUID.getPureUUID());
			audioPo.setAudioPublisher("喜马拉雅");
			audioPo.setAudioId(parseData.get("audioId") + "");
			audioPo.setAudioName(parseData.get("audioName") + "");
			audioPo.setAudioImg(parseData.get("audioImg") + "");
			audioPo.setAudioURL(parseData.get("playUrl") + ""); //
			audioPo.setDuration(parseData.get("duration") + "");
			audioPo.setcTime(new Timestamp(Long.valueOf((parseData.get("cTime") + "").equals("null")?System.currentTimeMillis()+"":(parseData.get("cTime") + ""))));
			audioPo.setCategoryName(parseData.get("categoryName") + "");
			audioPo.setAudioTags(parseData.get("tags") + "");
			audioPo.setPlayCount(parseData.get("playCount") + "");
			audioPo.setDescn(parseData.containsKey("descript") ? parseData.get("descript") + "" : null);
			audioPo.setAlbumId(parseData.containsKey("albumId") ? parseData.get("albumId") + "" : null);
			audioPo.setAlbumName(parseData.containsKey("albumName") ? parseData.get("albumName") + "" : null);
			audioPo.setVisitUrl(parseData.get("visitUrl")+"");
			return audioPo;
		}
		return null;
	}

	/**
	 * 单体节目搜索
	 * 
	 * @param content
	 */
	private void audiosS(String content) {
		String url = "http://www.ximalaya.com/search/" + SearchUtils.utf8TOurl(content) + "/t2";
		Document doc = null;
		try {
			Thread.sleep(100);
			doc = Jsoup.connect(url).timeout(T).ignoreContentType(true)
					.header("User-Agent","Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.102 Safari/537.36")
					.header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
					.header("Accept-Encoding", "gzip, deflate, sdch")
					.header("Cookie","Hm_lvt_4a7d8ec50cfd6af753c4f8aee3425070=1481942163; Hm_lpvt_4a7d8ec50cfd6af753c4f8aee3425070=1481942193; _ga=GA1.2.2074075166.1481942163")
					.header("Host", "www.ximalaya.com").header("Connection", "keep-alive")
					.header("X-Requested-With", "XMLHttpRequest").header("Referer", "http://www.ximalaya.com/explore/")
					.get();
			Elements elements = doc.select("div[class=row soundReport]");
			if (elements.size() > 0) {
				for (int i = 0; i < (elements.size() > F_NUM ? F_NUM : elements.size()); i++) {
					try {
						Element elf = elements.get(i);
						if (elf.select("a[class=soundReport_soundname]") == null || elf.select("a[class=soundReport_soundname]").size() == 0) {
							continue;
						}
						url = "http://www.ximalaya.com" + elf.select("a[class=soundReport_soundname]").get(0).attr("href");
						Document docc = HttpUtils.getJsonStrForUrl(url);
						Map<String, Object> parseData = new HashMap<>();
						XMLYParseUtils.parseSond(false, docc.toString().getBytes(), parseData);
						if (parseData != null && parseData.containsKey("audioId") && parseData.get("audioId")!=null && !parseData.get("audioId").equals("null")) {
							AudioPo audioPo = new AudioPo();
							audioPo.setId(SequenceUUID.getPureUUID());
							audioPo.setAudioPublisher("喜马拉雅");
							audioPo.setAudioId(parseData.get("audioId")==null?null:parseData.get("audioId").toString());
							audioPo.setAudioName(parseData.get("audioName")==null?null:parseData.get("audioName").toString());
							audioPo.setAudioImg(parseData.get("audioImg")==null?null:parseData.get("audioImg").toString());
							audioPo.setAudioURL(parseData.get("playUrl")==null?null:parseData.get("playUrl").toString()); //
							audioPo.setDuration(parseData.get("duration")==null?null:parseData.get("duration").toString());
							audioPo.setcTime(new Timestamp(Long.valueOf(parseData.get("cTime") + "")));
							audioPo.setCategoryName(parseData.get("categoryName")==null?null:parseData.get("categoryName").toString());
							audioPo.setAudioTags(parseData.get("tags")==null?null:parseData.get("tags").toString());
							audioPo.setPlayCount(parseData.get("playCount")==null?null:parseData.get("playCount").toString());
							audioPo.setDescn(parseData.containsKey("descript") ? parseData.get("descript") + "" : null);
							audioPo.setAlbumId(parseData.containsKey("albumId") ? parseData.get("albumId") + "" : null);
							audioPo.setAlbumName(parseData.containsKey("albumName") ? parseData.get("albumName") + "" : null);
							audioPo.setVisitUrl(parseData.get("visitUrl")+"");
							result.put(audioPo.getAudioName() + "::" + audioPo.getAudioId(), audioPo);
						}
					} catch (Exception e) {
						e.printStackTrace();
						continue;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			audioS(content);
		} finally {
			okNum++;
		}
	}

	@Override
	public void run() {
		System.out.println("喜马拉雅搜索开始");
		try {
			ximalayaService(constr);
			Thread.sleep(200);
			while (true) {
				Thread.sleep(20);
				if (okNum == 2) {
					Map<String, Object> albummap = new HashMap<>();
					Map<String, Object> audiomap = new HashMap<>();
					for (String key : result.keySet()) {
						if (result.get(key)!=null) {
							try {
								AlbumPo albumPo = (AlbumPo) result.get(key);
								albummap.put(albumPo.getAlbumId(), albumPo);
							} catch (Exception e) {
								AudioPo audioPo = (AudioPo) result.get(key);
								audiomap.put(audioPo.getAudioId(), audioPo);
								if (!albummap.containsKey(audioPo.getAlbumId())) {
									albummap.put(audioPo.getAlbumId(), null);
								}
								continue;
							}
						}
					}
					JedisConnectionFactory conn = (JedisConnectionFactory) SpringShell.getBean("connectionFactorySearch");
					RedisOperService roService = new RedisOperService(conn);
					for (String key : albummap.keySet()) {
						try {
							if (albummap.get(key)!=null) {
							    SearchUtils.addListInfo(constr, (AlbumPo)albummap.get(key), roService);
						    }
						} catch (Exception e) {
							e.printStackTrace();
							continue;
						}
						
					}
					for (String key : audiomap.keySet()) {
						try {
							if (audiomap.get(key)!=null) {
							    SearchUtils.addListInfo(constr, (AudioPo)audiomap.get(key), roService);
						    }
						} catch (Exception e) {
							e.printStackTrace();
							continue;
						}
					}
					roService.close();
					conn.destroy();
//					System.out.println(JsonUtils.objToJson(albummap));
//					System.out.println(JsonUtils.objToJson(audiomap));
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("喜马拉雅搜索异常");
		} finally {
			JedisConnectionFactory conn = (JedisConnectionFactory) SpringShell.getBean("connectionFactorySearch");
			RedisOperService roService = new RedisOperService(conn);
			SearchUtils.updateSearchFinish(constr, roService);
			System.out.println("喜马拉雅搜索结束");
			roService.close();
			conn.destroy();
		}
	}
}
