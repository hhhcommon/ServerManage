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
import com.woting.crawler.core.albumaudioref.persis.po.AlbumAudioRefPo;
import com.woting.crawler.core.albumaudioref.service.AlbumAudioRefService;
import com.woting.crawler.core.audio.persis.po.AudioPo;
import com.woting.crawler.core.audio.service.AudioService;
import com.woting.crawler.core.cplaycount.persis.po.CPlayCountPo;
import com.woting.crawler.core.cplaycount.service.CPlayCountService;
import com.woting.crawler.ext.SpringShell;
import com.woting.crawler.scheme.utils.CleanDataUtils;
import com.woting.crawler.scheme.utils.FileUtils;
import com.woting.crawler.scheme.utils.HttpUtils;
import com.woting.crawler.scheme.utils.TimeUtils;

public class XMLYPlayNumCrawler {
	private AlbumService albumService;
	private AudioService audioService;
	private MediaService mediaService;
	private CPlayCountService cPlayCountService;
	private AlbumAudioRefService albumAudioRefService;

	private static String xmlyAlbumPlayCountUrl = "http://mobile.ximalaya.com/mobile/v1/album?albumId=#albumId#&pageId=1&pageSize=1&pre_page=0";
	private static String xmlyAudioPlayCountUrl = "http://mobile.ximalaya.com/v1/track/baseInfo?device=android&trackId=#trackId#&trackUid=#trackUid#";

	@SuppressWarnings("unchecked")
	public void parseSmaPlayNum(String  albumId) {
		if (albumId==null || albumId.length()==0) return;
		String url = xmlyAlbumPlayCountUrl.replace("#albumId#", albumId);
		Document doc;
		cPlayCountService = (CPlayCountService) SpringShell.getBean("CPlayCountService");
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
				System.out.println(alstr);
				Map<String, Object> m = null;
				try {
					m = (Map<String, Object>) JsonUtils.jsonToObj(alstr, Map.class);
				} catch (Exception e) {
					m = (Map<String, Object>) JsonUtils.jsonToObj(CleanDataUtils.cleanString(alstr), Map.class);
				}
				Map<String, Object> album = (Map<String, Object>) ((Map<String, Object>) m.get("data")).get("album");
				String tracks = "";
				long playCount = 0;
				try {playCount = Long.valueOf(album.get("playTimes").toString());} catch (Exception e) {}
				CPlayCountPo countPo = cPlayCountService.getCPlayCountPo(""+albumId, "c_Album");
				if (countPo==null) {
					countPo = new CPlayCountPo();
					countPo.setId(SequenceUUID.getPureUUID());
					countPo.setPublisher("喜马拉雅");
					countPo.setResId(albumId);
					countPo.setResTableName("c_Album");
					cPlayCountService.insertCPlayCount(countPo);
				} else {
					if (playCount!=0 && countPo.getPlayCount()!=playCount) {
						countPo.setPlayCount(playCount);
						cPlayCountService.updateCPlayCountPo(countPo);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		List<Map<String, Object>> audiopls = albumAudioRefService.getAudioRefAndPlayCount(albumId);
		if (audiopls==null || audiopls.size()==0) return;
		for (Map<String, Object> map : audiopls) {
			String audioId = map.get("auId").toString();
			long playCounts = 0;
			if (map.get("playCount")!=null) playCounts = Long.valueOf(map.get("playCount").toString());
			Map<String, Object> resMap = HttpUtils.getJsonMapFromURL("http://www.ximalaya.com/tracks/"+audioId.replace("XMLY_AUDIO_", "")+".json");
			if (resMap==null || resMap.size()==0) continue;
			long _playCounts = 0;
			try {
				_playCounts = Long.valueOf(resMap.get("8905").toString());
			} catch (Exception e) {};
			if (_playCounts > playCounts) {
				CPlayCountPo countPo = cPlayCountService.getCPlayCountPo(audioId, "c_Audio");
				if (countPo==null) {
					countPo = new CPlayCountPo();
					countPo.setId(SequenceUUID.getPureUUID());
					countPo.setPublisher("喜马拉雅");
					countPo.setResId(audioId);
					countPo.setResTableName("c_Audio");
					cPlayCountService.insertCPlayCount(countPo);
				} else {
					countPo.setPlayCount(_playCounts);
					cPlayCountService.updateCPlayCountPo(countPo);
				}
			}
		}
	}
}
