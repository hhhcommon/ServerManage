package com.woting.crawler.scheme.crawlerplaynum.XMLY;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import com.spiritdata.framework.core.cache.SystemCache;
import com.spiritdata.framework.util.JsonUtils;
import com.spiritdata.framework.util.SequenceUUID;
import com.woting.cm.core.ResOrgAsset.persis.po.ResOrgAssetPo;
import com.woting.cm.core.media.persis.po.MediaPlayCountPo;
import com.woting.cm.core.media.service.MediaService;
import com.woting.crawler.CrawlerConstants;
import com.woting.crawler.core.album.persis.po.AlbumPo;
import com.woting.crawler.core.album.service.AlbumService;
import com.woting.crawler.core.audio.persis.po.AudioPo;
import com.woting.crawler.core.audio.service.AudioService;
import com.woting.crawler.ext.SpringShell;
import com.woting.crawler.scheme.utils.CleanDataUtils;
import com.woting.crawler.scheme.utils.FileUtils;
import com.woting.crawler.scheme.utils.TimeUtils;

public class XMLYPlayNumCrawler {
	private AlbumService albumService;
	private AudioService audioService;
	private MediaService mediaService;

	private static String xmlyAlbumPlayCountUrl = "http://mobile.ximalaya.com/mobile/v1/album?albumId=#albumId#&pageId=1&pageSize=1&pre_page=0";
	private static String xmlyAudioPlayCountUrl = "http://mobile.ximalaya.com/v1/track/baseInfo?device=android&trackId=#trackId#&trackUid=#trackUid#";

	@SuppressWarnings("unchecked")
	public void parseSmaPlayNum(ResOrgAssetPo resass) {
		if (resass != null) {
			if (resass.getOrigTableName().equals("hotspot_Album")) {
				albumService = (AlbumService) SpringShell.getBean("albumService");
				mediaService = (MediaService) SpringShell.getBean("mediaService");
				List<AlbumPo> als = albumService.getAlbumList(resass.getOrigId());
				if (als != null && als.size() > 0) {
					AlbumPo al = als.get(0);
					String albumId = al.getAlbumId();
					String url = xmlyAlbumPlayCountUrl.replace("#albumId#", albumId);
					Document doc;
					try {
						doc = Jsoup.connect(url).ignoreContentType(true).header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.102 Safari/537.36")
								.header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
								.header("Accept-Encoding", "gzip, deflate, sdch")
								.header("Cookie", "Hm_lvt_4a7d8ec50cfd6af753c4f8aee3425070=1481942163; Hm_lpvt_4a7d8ec50cfd6af753c4f8aee3425070=1481942193; _ga=GA1.2.2074075166.1481942163")
								.header("Host", "www.ximalaya.com")
								.header("Connection", "keep-alive")
								.header("X-Requested-With", "XMLHttpRequest")
								.header("Referer", "http://www.ximalaya.com/explore/").timeout(10000).get();
						if (doc != null) {
							String alstr = doc.body().html();
							Map<String, Object> m = null;
							try {
								m = (Map<String, Object>) JsonUtils.jsonToObj(alstr, Map.class);
							} catch (Exception e) {
								m = (Map<String, Object>) JsonUtils.jsonToObj(CleanDataUtils.cleanString(alstr), Map.class);
							}
							Map<String, Object> album = (Map<String, Object>) ((Map<String, Object>) m.get("data")).get("album");
							String tracks = "";
							Map<String, Object> mp = new HashMap<>();
							mp.put("resId", resass.getResId());
							mp.put("resTableName",resass.getResTableName());
							MediaPlayCountPo mplay = mediaService.getMediaPlayCount(mp);
							if (album != null) {
								if (mplay!=null) {
									mplay.setPlayCount((long)album.get("playTimes"));
									mplay.setcTime(new Timestamp(System.currentTimeMillis()));
									mediaService.updateMediaPlayCount(mplay);
								} else {
									MediaPlayCountPo mpc = new MediaPlayCountPo();
									mpc.setId(SequenceUUID.getPureUUID());
									mpc.setResTableName(resass.getResTableName());
									mpc.setResId(resass.getResId());
									mpc.setPublisher(resass.getOrgName());
									mpc.setPlayCount((long)album.get("playTimes"));
									mpc.setcTime(new Timestamp(System.currentTimeMillis()));
									mediaService.insertMediaPlayCount(mpc);
								}
								tracks = album.get("tracks") + "";
							}
							url = url.replace("pageSize=1", tracks);
							doc = Jsoup.connect(url).ignoreContentType(true).timeout(10000).get();
							if (doc != null) {
								alstr = doc.body().html();
								FileUtils.writeFile(alstr, SystemCache.getCache(CrawlerConstants.APP_PATH).getContent() + "XMLYCrawlerInfo/" + albumId + "/" + TimeUtils.getNowTime() + ".txt");
							}
						}
					} catch (Exception e) {
						e.getMessage();
					}
				}
			}
			if (resass.getOrigTableName().equals("hotspot_Audio")) {
				audioService = (AudioService) SpringShell.getBean("audioService");
				mediaService = (MediaService) SpringShell.getBean("mediaService");
				List<AudioPo> aus = audioService.getAudioListById(resass.getOrigId());
				if(aus!=null&&aus.size()>0) {
					AudioPo au = aus.get(0);
					String url = xmlyAudioPlayCountUrl.replace("#trackId#", au.getAudioId()).replace("#trackUid#", au.getAlbumId());
					Document doc;
					try {
						doc = Jsoup.connect(url).ignoreContentType(true).header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.102 Safari/537.36")
								.header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
								.header("Accept-Encoding", "gzip, deflate, sdch")
								.header("Cookie", "Hm_lvt_4a7d8ec50cfd6af753c4f8aee3425070=1481942163; Hm_lpvt_4a7d8ec50cfd6af753c4f8aee3425070=1481942193; _ga=GA1.2.2074075166.1481942163")
								.header("Host", "www.ximalaya.com")
								.header("Connection", "keep-alive")
								.header("X-Requested-With", "XMLHttpRequest")
								.header("Referer", "http://www.ximalaya.com/explore/").timeout(10000).get();
						String austr = doc.body().html();
						Map<String, Object> m = null;
						try {
							m = (Map<String, Object>) JsonUtils.jsonToObj(austr, Map.class);
						} catch (Exception e) {
							m = (Map<String, Object>) JsonUtils.jsonToObj(CleanDataUtils.cleanString(austr), Map.class);
						}
						if(m!=null) {
							String playnum = m.get("playtimes")+"";
							Map<String, Object> mp = new HashMap<>();
							mp.put("resId", resass.getResId());
							mp.put("resTableName",resass.getResTableName());
							MediaPlayCountPo mplay = mediaService.getMediaPlayCount(mp);
							if (mplay!=null) {
								mplay.setPlayCount(Long.valueOf(playnum));
								mplay.setcTime(new Timestamp(System.currentTimeMillis()));
								mediaService.updateMediaPlayCount(mplay);
							} else {
								MediaPlayCountPo mpc = new MediaPlayCountPo();
								mpc.setId(SequenceUUID.getPureUUID());
								mpc.setPublisher(resass.getOrgName());
								mpc.setResId(resass.getResId());
								mpc.setResTableName(resass.getResTableName());
								mpc.setPlayCount(Long.valueOf(playnum));
								mpc.setcTime(new Timestamp(System.currentTimeMillis()));
								mediaService.insertMediaPlayCount(mpc);
							}
						}
					} catch (Exception e) {
						e.getMessage();
					}
				}
			}
		}
	}
}
