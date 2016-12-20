package com.woting.crawler.scheme.searchcrawler.service;

import java.util.Date;
import java.util.Map;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import com.spiritdata.framework.ext.spring.redis.RedisOperService;
import com.spiritdata.framework.util.DateUtils;
import com.woting.crawler.ext.SpringShell;
import com.woting.crawler.scheme.searchcrawler.model.Festival;
import com.woting.crawler.scheme.searchcrawler.model.Station;
import com.woting.crawler.scheme.searchcrawler.utils.DataTransform;
import com.woting.crawler.scheme.searchcrawler.utils.SearchUtils;
import com.woting.crawler.scheme.utils.HttpUtils;

public class XiMaLaYaSearch extends Thread {

	private static int S_S_NUM = 10; // 搜索频道的数目
	private static int S_F_NUM = 2; // 搜索频道内节目的数目
	private static int F_NUM = 10; // 搜索节目的数目 以上排列顺序按照搜索到的排列顺序
	private static int T = 5000;
	private String constr;

	public XiMaLaYaSearch(String constr) {
		this.constr = constr;
	}

	private void ximalayaService(String content) {
//		festivalsS(content);
		new Thread(new  Runnable() {
			public void run() {
				stationS(content);
			}
		}).start();
		new Thread(new Runnable() {
			public void run() {
				festivalsS(content);
			}
		}).start();//{@Override public void run() {festivalsS(content);}};
	}

	// 专辑搜索
	private void stationS(String content) {
		String url = "http://www.ximalaya.com/search/" + SearchUtils.utf8TOurl(content) + "/t3";
		Document doc = null;
		doc = HttpUtils.getJsonStrForUrl(url);
		Elements elements = doc.select("div[class=content_wrap2]");
		for (int i = 0; i < (elements.size() > S_S_NUM ? S_S_NUM : elements.size()); i++) {
			Station station = new Station();
			station.setContentPub("喜马拉雅");
			Element element1 = elements.get(i).select("a[class=albumface100]").get(0);
			String hrefstation = element1.attr("href");
			String stationpic = element1.select("span").select("img").attr("src");
			String[] strs = hrefstation.split("/");
			station.setId(strs[3]); // 专辑ID
			station.setPic(stationpic); // 专辑图片
			Element element2 = elements.get(i).select("div[class=info title]").select("a[href]").get(0);
			String stationname = element2.html();
			station.setName(stationname); // 专辑名称
			station.setFestival(stationfestiavlS(hrefstation));
			if (station != null) {
				JedisConnectionFactory conn = (JedisConnectionFactory) SpringShell.getBean("connectionFactorySearch");
				RedisOperService roService = new RedisOperService(conn);
				SearchUtils.addListInfo(content, station, roService); // 保存到在redis里key为constr的list里
			}
		}
	}

	/**
	 * 专辑下的节目搜索
	 * 
	 * @param contentid
	 * @return
	 */
	private Festival[] stationfestiavlS(String contentid) {
		String url = "http://www.ximalaya.com" + contentid;
		Festival[] festivals = new Festival[S_F_NUM];
		Document doc = null;
		doc = HttpUtils.getJsonStrForUrl(url);
		Elements elements = doc.select("li[sound_id]");
		for (int i = 0; i < (elements.size() > S_F_NUM ? S_F_NUM : elements.size()); i++) {
			Festival festival = new Festival();
			Element element = elements.get(i).select("a[class=forwardBtn]").get(0);
			festival.setAudioId(element.attr("track_id")); // 节目id
			Elements elementsspan = elements.select("span");
			festival.setUpdateTime(elementsspan.get(0).html()); // 节目创建时间
			elements = doc.select("a[class=shareLink shareLink2]");//shareLink shareLink2
			if (elements!=null && elements.size()>0) {
				festival.setAlbumId(elements.get(0).attr("album_id"));
			}
			festivals[i] = festivalS(festival.getAudioId(), festival);
		}
		return festivals;
	}

	/**
	 * 根据节目id进行节目信息获取
	 * 
	 * @param contendid
	 * @param festival
	 * @return
	 */
	private Festival festivalS(String contendid, Festival festival) {
		String url = "http://www.ximalaya.com/tracks/" + contendid + ".json";
		String jsonstr = SearchUtils.jsoupTOstr(url);
		Map<String, Object> map = SearchUtils.jsonTOmap(jsonstr);
		if (map == null)
			return null;
		else {
			festival.setAudioId(contendid); // 节目id
			festival.setAudioName(map.get("title") + "");
			festival.setPlayUrl(map.get("play_path") + ""); // 节目音频地址
			festival.setDuration(map.get("duration") + "000"); // 音频时长 ms
			festival.setAudioPic(map.get("cover_url_142") + ""); // 节目图片
			festival.setCategory(map.get("category_name") + ""); // 节目分类
			festival.setPlaynum(map.get("play_count") + ""); // 节目播放次数
			festival.setContentPub("喜马拉雅");
			festival.setPersonName(map.get("nickname") + "");
			festival.setPersonId(map.get("uid") + "");
			String created_at = map.get("formatted_created_at") + "";
			String time_utils_now = map.get("time_until_now") + "";
			try {
				if (time_utils_now.contains("年")) {
					int year = DataTransform.findInt(time_utils_now);
					year = new Date(System.currentTimeMillis()).getYear() - year + 1900;
					created_at = year + "年" + created_at;
					long date = DateUtils.getDateTime("yyyy年MM月dd日 HH:mm", created_at).getTime();
					festival.setUpdateTime(date + "");
				} else {
					int year = new Date(System.currentTimeMillis()).getYear() + 1900;
					created_at = year + "年" + created_at;
					long date = DateUtils.getDateTime("yyyy年MM月dd日 HH:mm", created_at).getTime();
					festival.setUpdateTime(date + "");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return festival;
		}
	}

	/**
	 * 单体节目搜索
	 * 
	 * @param content
	 */
	private void festivalsS(String content) {
		String url = "http://www.ximalaya.com/search/" + SearchUtils.utf8TOurl(content) + "/t2";
		Document doc = null;
		try {
			Thread.sleep(100);
			doc = Jsoup.connect(url).timeout(T).ignoreContentType(true)
					.header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.102 Safari/537.36")
					.header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
					.header("Accept-Encoding", "gzip, deflate, sdch")
					.header("Cookie", "Hm_lvt_4a7d8ec50cfd6af753c4f8aee3425070=1481942163; Hm_lpvt_4a7d8ec50cfd6af753c4f8aee3425070=1481942193; _ga=GA1.2.2074075166.1481942163")
					.header("Host", "www.ximalaya.com")
					.header("Connection", "keep-alive")
					.header("X-Requested-With", "XMLHttpRequest")
					.header("Referer", "http://www.ximalaya.com/explore/").get();
			Elements elements = doc.select("div[class=row soundReport]");
			if (elements.size() > 0) {
//				elements.remove(0);
				for (int i = 0; i < (elements.size() > F_NUM ? F_NUM : elements.size()); i++) {
					Festival festival = new Festival();
					Element elf = elements.get(i);
					if (elf.select("a[class=soundReport_soundname]")==null || elf.select("a[class=soundReport_soundname]").size()==0 ) {
						continue;
					}
					String id = elf.select("a[class=soundReport_soundname]").get(0).attr("href").split("/")[3];
					String desc = elf.select("a[class=soundReport_tag]").size() == 0 ? null : elf.select("a[class=soundReport_tag]").get(0).html();
					String host = elf.select("div[class=col soundReport_author]").size() == 0 ? null : elf.select("div[class=col soundReport_author]").get(0).select("a").get(0).html();
					String playnum = elf.select("div[class=col soundReport_playCount]").get(0).select("span").get(0).html();
					String albumName = elf.select("div[class=col soundReport_album]").get(0).select("a").get(0).html();
					String albumId = elf.select("div[class=col soundReport_album]").get(0).select("a").get(0).attr("href");
					albumId = albumId.substring(albumId.indexOf("/album/")+7, albumId.length());
					albumName = albumName.replace("《", "").replace("》", "");
					festival.setAudioId(id);
					festival.setAudioDes(desc);
					festival.setPersonName(host == null ? null : host.split(" ")[0]);
					festival.setPlaynum(playnum);
					festival.setAlbumName(albumName);
					festival.setAlbumId(albumId);
					festival = festivalS(festival.getAudioId(), festival);
					if (festival != null) {
						JedisConnectionFactory conn = (JedisConnectionFactory) SpringShell.getBean("connectionFactorySearch");
						RedisOperService roService = new RedisOperService(conn);
						SearchUtils.addListInfo(content, festival, roService); // 保存到在redis里key为constr的list里
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			festivalsS(content);
		}
	}

	@Override
	public void run() {
		System.out.println("喜马拉雅搜索开始");
		try {
			ximalayaService(constr);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("喜马拉雅搜索异常");
		} finally {
			JedisConnectionFactory conn = (JedisConnectionFactory) SpringShell.getBean("connectionFactorySearch");
			RedisOperService roService = new RedisOperService(conn);
			SearchUtils.updateSearchFinish(constr, roService);
			System.out.println("喜马拉雅搜索结束");
		}
	}
}
