package com.woting.crawler.scheme.searchcrawler.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jsoup.nodes.Document;
import com.spiritdata.framework.core.cache.SystemCache;
import com.woting.cm.core.channel.persis.po.ChannelAssetPo;
import com.woting.cm.core.channel.persis.po.ChannelPo;
import com.woting.cm.core.channel.service.ChannelService;
import com.woting.cm.core.media.persis.po.MediaAssetPo;
import com.woting.cm.core.media.persis.po.SeqMediaAssetPo;
import com.woting.cm.core.media.service.MediaService;
import com.woting.crawler.CrawlerConstants;
import com.woting.crawler.core.album.persis.po.AlbumPo;
import com.woting.crawler.core.audio.persis.po.AudioPo;
import com.woting.crawler.core.etl.service.Etl2Service;
import com.woting.crawler.ext.SpringShell;
import com.woting.crawler.scheme.crawlersrc.QT.crawler.QTParseUtils;
import com.woting.crawler.scheme.crawlersrc.XMLY.crawler.XMLYParseUtils;
import com.woting.crawler.scheme.searchcrawler.model.Festival;
import com.woting.crawler.scheme.searchcrawler.model.Station;
import com.woting.crawler.scheme.utils.ConvertUtils;
import com.woting.crawler.scheme.utils.FileUtils;
import com.woting.crawler.scheme.utils.HttpUtils;

/**
 * 数据转换
 * 
 * @author wbq
 *
 */
public abstract class DataTransform {
	/**
	 * 提出专辑里的第一个节目
	 * 
	 * @param list_stations
	 * @return
	 */
	public static Map<String, Object> datas2Sequ_Audio(Station station) {
		if (station.getFestival() == null)
			return null;
		Map<String, Object> map = new HashMap<String, Object>();
		if (station.getFestival()[0] != null) {
			Festival festival = station.getFestival()[0];
			map = festival2Audio(true, festival);
			for (int i = 1; i < station.getFestival().length; i++) {
				Festival festiva = station.getFestival()[i];
				new Thread(new Runnable() {
					public void run() {
						festival2Audio(false, festiva);
					}
				}).start();;
			}
			if (map != null) {
				return map;
			}
		}
		return null;
	}

	/**
	 * 单festival转audio
	 * 
	 * @param festival
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, Object> festival2Audio(boolean makeSma , Festival festival) {
		// ContentCatalogs内容分类、ContentKeyWord关键词、ContentSubjectWord主题词和PlayCount播放次数未定义参数
		if (festival == null)
			return null;
		MediaService mediaService = (MediaService) SpringShell.getBean("mediaService");
		Map<String, Object> m = new HashMap<>();
		if (festival.getAudioName().contains(" - ")) {
			festival.setAudioName(festival.getAudioName().substring(festival.getAudioName().indexOf(" - ") + 3, festival.getAudioName().length()));
		}
		m.put("maTitle", festival.getAudioName());
		m.put("maPublisher", festival.getContentPub());
		List<MediaAssetPo> mas = mediaService.getMaList(m);
		if (mas != null && mas.size() > 0) {
			MediaAssetPo ma = mas.get(0);
			ChannelService channelService = (ChannelService) SpringShell.getBean("channelService");
			m.clear();
			m.put("assetType", "wt_MediaAsset");
			m.put("assetId", ma.getId());
			m.put("flowFlag", 2);
			List<ChannelAssetPo> chas = channelService.getChannelAssetListBy(m);
			if (chas == null) {
				return null;
			}
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("ContentId", ma.getId());
			map.put("ContentName", ma.getMaTitle());
			map.put("ContentImg", ma.getMaImg());
			map.put("ContentPlay", ma.getMaURL());
			map.put("ContentPub", ma.getMaPublisher());
			map.put("MediaType", "AUDIO");
			map.put("PlayCount", "1234");
			if (makeSma) {
				List<SeqMediaAssetPo> smas = mediaService.getSmaByMaId(ma.getId(), ma.getMaPublisher());
				if (smas!=null && smas.size()>0) {
					SeqMediaAssetPo sma = smas.get(0);
					Map<String, Object> smam = new HashMap<>();
					smam.put("ContentName", sma.getSmaTitle());
					smam.put("ContentId", sma.getId());
					smam.put("ContentImg", sma.getSmaImg());
					smam.put("ContentPub", sma.getSmaPublisher());
					smam.put("MediaType", "SEQU");
					smam.put("PlayCount", "123");
					map.put("SeqInfo",smam);
					return map;
				}
			}
			return map;
		} else {
			List<SeqMediaAssetPo> smas = mediaService.getSmaList(festival.getAlbumName(), festival.getContentPub());
			if (smas != null) {
				List<Map<String, Object>> cate2dictdlist = FileUtils.readFileByJson(SystemCache.getCache(CrawlerConstants.APP_PATH).getContent() + "conf/craw.txt");
				ChannelService channelService = (ChannelService) SpringShell.getBean("channelService");
				List<ChannelPo> chlist = channelService.getChannelList();
				if (smas!=null && smas.size()>0) {
					SeqMediaAssetPo sma = smas.get(0);
				    Map<String, Object> map = ConvertUtils.convert2MediaAsset(festival, sma, cate2dictdlist, chlist);
				    if (makeSma) {
						Map<String, Object> smam = new HashMap<>();
					    smam.put("ContentName", sma.getSmaTitle());
					    smam.put("ContentId", sma.getId());
					    smam.put("ContentImg", sma.getSmaImg());
					    smam.put("ContentPub", sma.getSmaPublisher());
					    smam.put("MediaType", "SEQU");
					    smam.put("PlayCount", "123");
					    map.put("SeqInfo",smam);
					}
				    return map;
				}
			} else {
				Etl2Service etl2Service = (Etl2Service) SpringShell.getBean("etl2Service");
				if (festival.getContentPub().equals("喜马拉雅")) {
					try {
						String url = "http://www.ximalaya.com/" + festival.getPersonId() + "/album/"+ festival.getAlbumId();
						Document doc = HttpUtils.getJsonStrForUrl(url);
						String docstr = doc.toString();
						Map<String, Object> al = new HashMap<>();
						XMLYParseUtils.parseAlbum(false, docstr.getBytes(), al);
						List<Map<String, Object>> als = new ArrayList<>();
						als.add(al);
						List<AlbumPo> albs = ConvertUtils.convert2Album(als, "喜马拉雅");
						url = "http://www.ximalaya.com/" + festival.getPersonId() + "/sound/" + festival.getAudioId();
						doc = HttpUtils.getJsonStrForUrl(url);
						docstr = doc.toString();
						Map<String, Object> ad = new HashMap<>();
						XMLYParseUtils.parseSond(false, docstr.getBytes(), ad);
						List<Map<String, Object>> ads = new ArrayList<>();
						ads.add(ad);
						List<AudioPo> adus = ConvertUtils.convert2Aludio(ads, "喜马拉雅");
						if (albs != null && albs.size() > 0 && adus != null && adus.size() > 0) {
							etl2Service.makeNewAlbums(albs, adus);
							AudioPo retAu = adus.get(0);
							MediaAssetPo ma = mediaService.getMaInfoById(retAu.getId());
							if (ma!=null) {
								Map<String, Object> retM = new HashMap<>();
							    retM.put("ContentId", ma.getId());
							    retM.put("ContentName", ma.getMaTitle());
							    retM.put("ContentImg", ma.getMaImg());
							    retM.put("ContentPlay", ma.getMaURL());
							    retM.put("ContentPub", ma.getMaPublisher());
							    retM.put("MediaType", "AUDIO");
							    retM.put("PlayCount", "1234");
							    if (makeSma) {
									List<SeqMediaAssetPo> sms = mediaService.getSmaByMaId(ma.getId(), ma.getMaPublisher());
									if (sms!=null && sms.size()>0) {
										SeqMediaAssetPo sma = sms.get(0);
										Map<String, Object> smam = new HashMap<>();
										smam.put("ContentName", sma.getSmaTitle());
										smam.put("ContentId", sma.getId());
										smam.put("ContentImg", sma.getSmaImg());
										smam.put("ContentPub", sma.getSmaPublisher());
										smam.put("MediaType", "SEQU");
										smam.put("PlayCount", "123");
										retM.put("SeqInfo",smam);
									}
								}
							    return retM;
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					if (festival.getContentPub().equals("蜻蜓")) {
						String url = "http://www.qingting.fm/#/vchannels/"+festival.getAlbumId();
						Document doc = HttpUtils.getJsonStrForUrl(url);
						String jsonstr = doc.toString();
						Map<String, Object> alm = new HashMap<>();
						QTParseUtils.parseAlbum(false, jsonstr.getBytes(), alm);
						alm.put("categoryId", "cn36");
						alm.put("categoryName", "其他");
						List<Map<String, Object>> als = new ArrayList<>();
						als.add(alm);
						List<AlbumPo> albs = ConvertUtils.convert2Album(als, "蜻蜓");
						List<Map<String, Object>> aums = (List<Map<String, Object>>) alm.get("audioList");
						List<AudioPo> aus = ConvertUtils.convert2Aludio(aums, "蜻蜓");
						if (albs!=null && albs.size()>0 && aus!=null && aus.size()>0) {
							Map<String, Object> retM = new HashMap<>();
							for (AudioPo audioPo : aus) {
								if (audioPo.getAudioId().equals(festival.getAudioId())) {
									retM.put("ContentId", audioPo.getId());
									break;
								}
							}
							etl2Service.makeNewAlbums(albs, aus);
							MediaAssetPo ma = mediaService.getMaInfoById(retM.get("ContentId")+"");
							if (ma!=null) {
								retM.put("ContentId", ma.getId());
								retM.put("ContentName", ma.getMaTitle());
								retM.put("ContentImg", ma.getMaImg());
								retM.put("ContentPlay", ma.getMaURL());
								retM.put("ContentPub", ma.getMaPublisher());
								retM.put("MediaType", "AUDIO");
								retM.put("PlayCount", "1234");
								if (makeSma) {
									List<SeqMediaAssetPo> sms = mediaService.getSmaByMaId(ma.getId(), ma.getMaPublisher());
									if (sms!=null && sms.size()>0) {
										SeqMediaAssetPo sma = sms.get(0);
										Map<String, Object> smam = new HashMap<>();
										smam.put("ContentName", sma.getSmaTitle());
										smam.put("ContentId", sma.getId());
										smam.put("ContentImg", sma.getSmaImg());
										smam.put("ContentPub", sma.getSmaPublisher());
										smam.put("MediaType", "SEQU");
										smam.put("PlayCount", "123");
										retM.put("SeqInfo",smam);
									}
								}
								return retM;
							}
						}
					}
				}
			}
		}
		return null;
		// map.put("ContentId", festival.getAudioId());
		// map.put("ContentName", festival.getAudioName());
		// map.put("ContentURI",
		// "content/getContentInfo.do?MediaType=AUDIO&ContentId=" +
		// festival.getAudioId());
		// map.put("ContentImg", festival.getAudioPic());
		// map.put("ContentPlay", festival.getPlayUrl());
		// map.put("ContentImg", festival.getAudioPic());
		// map.put("ContentPersons", festival.getPersonName());
		// map.put("ContentTimes", festival.getDuration());// 以ms为计量单位
		// map.put("ContentPubTime", festival.getUpdateTime());
		// map.put("ContentPub", festival.getContentPub());
		// map.put("ContentDescn", festival.getAudioDes());
		// map.put("CTime", festival.getUpdateTime());
		// map.put("MediaType", festival.getMediaType());
		// map.put("ContentCatalogs", null);
		// map.put("ContentKeyWord", null);
		// map.put("ContentSubjectWord", null);
	}

	/**
	 * 单station转sequ
	 * 
	 * @param station
	 * @return
	 */
	public static Map<String, Object> station2Sequ(Station station) {
		// ContentCatalogs内容分类、ContentKeyWord关键词、ContentSubjectWord主题词和PlayCount播放次数
		if (station == null)
			return null;
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("ContentSubCount", (station.getFestival().length) + "");
		map.put("ContentURI", "content/getContentInfo.do?MediaType=SEQU&ContentId=" + station.getId());
		map.put("ContentPersons", station.getHost());
		map.put("CTime", station.getCTime());
		map.put("ContentName", station.getName());
		map.put("ContentPub", station.getContentPub());
		map.put("MediaType", station.getMediaType());
		map.put("ContentId", station.getId());
		map.put("ContentDescn", station.getDesc());
		map.put("ContentImg", station.getPic());
		map.put("ContentCatalogs", null);
		map.put("ContentKeyWord", null);
		map.put("ContentSubjectWord", null);
		map.put("PlayCount", "1234");
		return map;
	}

	public static int findInt(String str) {
		char[] s = str.toCharArray();
		String d = "";
		for (int i = 0; i < s.length; i++) {
			if (Character.isDigit(s[i])) {
				d += s[i];
			}
		}
		return Integer.valueOf(d);
	}
}
