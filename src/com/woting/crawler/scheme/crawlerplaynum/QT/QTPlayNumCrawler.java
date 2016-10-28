package com.woting.crawler.scheme.crawlerplaynum.QT;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import com.spiritdata.framework.util.JsonUtils;
import com.spiritdata.framework.util.SequenceUUID;
import com.woting.cm.core.ResOrgAsset.persis.po.ResOrgAssetPo;
import com.woting.cm.core.media.persis.po.MediaPlayCountPo;
import com.woting.cm.core.media.service.MediaService;
import com.woting.crawler.core.album.persis.po.AlbumPo;
import com.woting.crawler.core.album.service.AlbumService;
import com.woting.crawler.core.audio.persis.po.AudioPo;
import com.woting.crawler.core.audio.service.AudioService;
import com.woting.crawler.ext.SpringShell;
import com.woting.crawler.scheme.utils.ConvertUtils;

public class QTPlayNumCrawler {
	//专辑播放次数http://i.qingting.fm/wapi/channel_playcount?cids=115850
	//声音播放次数http://i.qingting.fm/wapi/program_playcount?pids=115850_5019265
	private static String qtAlbumPlayCountUrl = "http://i.qingting.fm/wapi/channel_playcount?cids=#cids#";
	private static String qtAudioPlayCountUrl = "http://i.qingting.fm/wapi/program_playcount?pids=#pids#";
	private AlbumService albumService;
	private AudioService audioService;
	private MediaService mediaService;
	
	@SuppressWarnings("unchecked")
	public void parsePlayNum(ResOrgAssetPo resass) {
		if (resass!=null) {
			if(resass.getOrigTableName().equals("hotspot_Album")) {
				albumService = (AlbumService) SpringShell.getBean("albumService");
				mediaService = (MediaService) SpringShell.getBean("mediaService");
				List<AlbumPo> als = albumService.getAlbumListById(resass.getOrigId());
				if(als!=null&&als.size()>0) {
					AlbumPo al = als.get(0);
					String url = qtAlbumPlayCountUrl.replace("#cids#", al.getAlbumId());
					Document doc;
					try {
						doc = Jsoup.connect(url).ignoreContentType(true).timeout(10000).get();
						String austr = doc.body().html();
						austr = StringEscapeUtils.unescapeHtml4(austr);
						Map<String, Object> m = (Map<String, Object>) JsonUtils.jsonToObj(austr, Map.class);
						List<Map<String, Object>> data = (List<Map<String, Object>>) m.get("data");
						if(data!=null&&data.size()>0) {
							Map<String, Object> dm = data.get(0);
							String playnum = dm.get("playcount")+"";
							playnum = ConvertUtils.convertPlayNum2Long(playnum);
							MediaPlayCountPo mplay = mediaService.getMediaPlayCount(resass.getResId(), resass.getResTableName());
							if(mplay!=null && mplay.getPlayCount().equals(playnum))
								return;
							MediaPlayCountPo mpc = new MediaPlayCountPo();
							mpc.setId(SequenceUUID.getPureUUID());
							mpc.setPublisher(resass.getOrgName());
							mpc.setResId(resass.getResId());
							mpc.setResTableName("wt_SeqMediaAsset");
							mpc.setPlayCount(playnum);
							mpc.setcTime(new Timestamp(System.currentTimeMillis()));
							mediaService.insertMediaPlayCount(mpc);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			if(resass.getOrigTableName().equals("hotspot_Audio")) {
				audioService = (AudioService) SpringShell.getBean("audioService");
				mediaService = (MediaService) SpringShell.getBean("mediaService");
				List<AudioPo> aus = audioService.getAudioListById(resass.getOrigId());
				if(aus!=null&&aus.size()>0) {
					AudioPo au = aus.get(0);
					String url = qtAudioPlayCountUrl.replace("#pids#", au.getAlbumId()+"_"+au.getAudioId());
					Document doc;
					try {
						doc = Jsoup.connect(url).ignoreContentType(true).timeout(10000).get();
						String austr = doc.body().html();
						austr = StringEscapeUtils.unescapeHtml4(austr);
						Map<String, Object> m = (Map<String, Object>) JsonUtils.jsonToObj(austr, Map.class);
						List<Map<String, Object>> data = (List<Map<String, Object>>) m.get("data");
						Map<String, Object> m0 = data.get(0);
						String playnum = m0.get("playcount")+"";
						playnum = ConvertUtils.convertPlayNum2Long(playnum);
						MediaPlayCountPo mplay = mediaService.getMediaPlayCount(resass.getResId(), resass.getResTableName());
						if(mplay!=null && mplay.getPlayCount().equals(playnum))
							return;
						MediaPlayCountPo mpc = new MediaPlayCountPo();
						mpc.setId(SequenceUUID.getPureUUID());
						mpc.setPublisher(resass.getOrgName());
						mpc.setResId(resass.getResId());
						mpc.setResTableName(resass.getResTableName());
						mpc.setPlayCount(playnum);
						mpc.setcTime(new Timestamp(System.currentTimeMillis()));
						mediaService.insertMediaPlayCount(mpc);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
}
