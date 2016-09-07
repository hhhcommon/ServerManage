package com.woting.crawler.scheme.crawlerplaynum.XMLY;

import java.sql.Timestamp;
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
						doc = Jsoup.connect(url).ignoreContentType(true).timeout(10000).get();
						if (doc != null) {
							String alstr = doc.body().html();
//							alstr = StringEscapeUtils.unescapeHtml4(alstr);
							Map<String, Object> m = null;
							try {
								m = (Map<String, Object>) JsonUtils.jsonToObj(alstr, Map.class);
							} catch (Exception e) {
								m = (Map<String, Object>) JsonUtils.jsonToObj(CleanDataUtils.cleanString(alstr), Map.class);
							}
							Map<String, Object> album = (Map<String, Object>) ((Map<String, Object>) m.get("data")).get("album");
							String tracks = "";
							if (album != null) {
								MediaPlayCountPo mpc = new MediaPlayCountPo();
								mpc.setId(SequenceUUID.getPureUUID());
								mpc.setResTableName(resass.getResTableName());
								mpc.setResId(resass.getResId());
								mpc.setPublisher(resass.getOrgName());
								mpc.setPlayCount(album.get("playTimes") + "");
								mpc.setcTime(new Timestamp(System.currentTimeMillis()));
								mediaService.insertMediaPlayCount(mpc);
								tracks = album.get("tracks") + "";
							}
							url = url.replace("pageSize=1", tracks);
							doc = Jsoup.connect(url).ignoreContentType(true).timeout(10000).get();
							if (doc != null) {
								alstr = doc.body().html();
//								alstr = StringEscapeUtils.unescapeHtml4(alstr);
								FileUtils.writeFile(alstr, SystemCache.getCache(CrawlerConstants.APP_PATH).getContent() + "XMLYCrawlerInfo/" + albumId + "/" + TimeUtils.getNowTime() + ".txt");
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
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
						doc = Jsoup.connect(url).ignoreContentType(true).timeout(10000).get();
						String austr = doc.body().html();
						Map<String, Object> m = null;
						try {
							m = (Map<String, Object>) JsonUtils.jsonToObj(austr, Map.class);
						} catch (Exception e) {
							m = (Map<String, Object>) JsonUtils.jsonToObj(CleanDataUtils.cleanString(austr), Map.class);
						}
						if(m!=null) {
							String playnum = m.get("playtimes")+"";
							MediaPlayCountPo mpc = new MediaPlayCountPo();
							mpc.setId(SequenceUUID.getPureUUID());
							mpc.setPublisher(resass.getOrgName());
							mpc.setResId(resass.getResId());
							mpc.setResTableName(resass.getResTableName());
							mpc.setPlayCount(playnum);
							mpc.setcTime(new Timestamp(System.currentTimeMillis()));
							mediaService.insertMediaPlayCount(mpc);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
}
