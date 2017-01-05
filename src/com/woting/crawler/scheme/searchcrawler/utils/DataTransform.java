package com.woting.crawler.scheme.searchcrawler.utils;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.jsoup.nodes.Document;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;

import com.spiritdata.framework.core.cache.SystemCache;
import com.spiritdata.framework.ext.spring.redis.RedisOperService;
import com.spiritdata.framework.util.FileNameUtils;
import com.spiritdata.framework.util.JsonUtils;
import com.spiritdata.framework.util.SequenceUUID;
import com.woting.cm.core.ResOrgAsset.persis.po.ResOrgAssetPo;
import com.woting.cm.core.ResOrgAsset.service.ResOrgAssetService;
import com.woting.cm.core.channel.persis.po.ChannelAssetPo;
import com.woting.cm.core.channel.persis.po.ChannelPo;
import com.woting.cm.core.channel.service.ChannelService;
import com.woting.cm.core.media.persis.po.MediaAssetPo;
import com.woting.cm.core.media.persis.po.SeqMediaAssetPo;
import com.woting.cm.core.media.service.MediaService;
import com.woting.crawler.CrawlerConstants;
import com.woting.crawler.core.album.model.Album;
import com.woting.crawler.core.album.persis.po.AlbumPo;
import com.woting.crawler.core.audio.persis.po.AudioPo;
import com.woting.crawler.core.etl.service.Etl2Service;
import com.woting.crawler.ext.SpringShell;
import com.woting.crawler.scheme.crawlersrc.QT.crawler.QTParseUtils;
import com.woting.crawler.scheme.crawlersrc.XMLY.crawler.XMLYParseUtils;
import com.woting.crawler.scheme.searchcrawler.model.Festival;
import com.woting.crawler.scheme.searchcrawler.model.Station;
import com.woting.crawler.scheme.searchcrawler.service.QTSearch;
import com.woting.crawler.scheme.searchcrawler.service.XiMaLaYaSearch;
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
		if (station.getFestivals() == null)
			return null;
		Map<String, Object> map = new HashMap<String, Object>();
		if (station.getFestivals().get(0) != null) {
			Festival festival = station.getFestivals().get(0);
			map = festival2Audio(true, festival);
			for (int i = 1; i < station.getFestivals().size(); i++) {
				Festival festiva = station.getFestivals().get(i);
				new Thread(new Runnable() {
					public void run() {festival2Audio(false, festiva);}}).start();
			}
			if (map != null) {
				return map;
			}
		}
		return null;
	}

	public static Map<String, Object> datas2Album_Audio(Album album) {
		if (album == null || album.getAudiolist() == null || album.getAudiolist().size() == 0)
			return null;
		Map<String, Object> map = new HashMap<String, Object>();
		map = album2Audio(true, album);
		if (map != null) {
			return map;
		}
		return null;
	}

	public static Map<String, Object> album2Audio(boolean makeSma, Album album) {
		JedisConnectionFactory conn = (JedisConnectionFactory) SpringShell.getBean("connectionFactorySearch");
		RedisOperService ros = new RedisOperService(conn);
		try {
			
		} finally {
			// TODO: handle finally clause
		}
		MediaService mediaService = (MediaService) SpringShell.getBean("mediaService");
		AudioPo au = album.getAudiolist().get(0);
		Map<String, Object> m = new HashMap<>();
		m.put("maTitle", au.getAudioName());
		m.put("maPublisher", au.getAudioPublisher());
		List<MediaAssetPo> mas = mediaService.getMaList(m);
		Etl2Service etl2Service = (Etl2Service) SpringShell.getBean("etl2Service");
		if (mas != null && mas.size() > 0) {
			MediaAssetPo ma = mas.get(0);
			ChannelService channelService = (ChannelService) SpringShell.getBean("channelService");
			m.clear();
			m.put("assetType", "wt_MediaAsset");
			m.put("assetId", ma.getId());
			m.put("flowFlag", 2);
			List<ChannelAssetPo> chas = channelService.getChannelAssetListBy(m);
			if (chas == null || chas.size() > 0) {
				return null;
			}
			SearchUtils.addRedisValue("CONTENT_AUDIO_SAVED_"+au.getId(), ma.getId(), ros);
//			Map<String, Object> retM = new HashMap<String, Object>();
//			retM.put("ContentId", ma.getId());
//			retM.put("ContentName", ma.getMaTitle());
//			retM.put("ContentImg", ma.getMaImg());
//			retM.put("ContentPlay", ma.getMaURL());
//			retM.put("ContentPub", ma.getMaPublisher());
//			retM.put("ContentTime", ma.getTimeLong());
//			retM.put("MediaType", "AUDIO");
//			retM.put("PlayCount", "1234");
			if (makeSma) {
				List<SeqMediaAssetPo> smas = mediaService.getSmaByMaId(ma.getId(), ma.getMaPublisher());
				if (smas != null && smas.size() > 0) {
					SeqMediaAssetPo sma = smas.get(0);
//					Map<String, Object> smam = new HashMap<>();
//					smam.put("ContentName", sma.getSmaTitle());
//					smam.put("ContentId", sma.getId());
//					smam.put("ContentImg", sma.getSmaImg());
//					smam.put("ContentPub", sma.getSmaPublisher());
//					smam.put("MediaType", "SEQU");
//					smam.put("PlayCount", "123");
//					retM.put("SeqInfo", smam);
				}
			}
//			return retM; TODO
		} else {
			List<SeqMediaAssetPo> smas = mediaService.getSmaList(album.getAlbumPo().getAlbumName(),album.getAlbumPo().getAlbumPublisher());
			if (smas != null && smas.size() > 0) {
				SeqMediaAssetPo sma = smas.get(0);
				Map<String, Object> retM = new HashMap<>();
				m.clear();
				m.put("maPublisher", sma.getSmaPublisher());
				m.put("whereSql", " id in (select mId from wt_SeqMA_Ref where sId = '" + sma.getId() + "')");
				List<MediaAssetPo> oldmas = mediaService.getMaList(m);
				Iterator<AudioPo> itau = album.getAudiolist().iterator();
				if (oldmas != null && oldmas.size() > 0) {
					while (itau.hasNext()) {
						AudioPo audioPo = (AudioPo) itau.next();
						for (MediaAssetPo mediaAssetPo : oldmas) {
							if (audioPo.getAudioName().equals(mediaAssetPo.getMaTitle())) {
								itau.remove();
							}
						}
					}
				}
				List<Album> als = new ArrayList<>();
				als.add(album);
				etl2Service.makeExistAlbums(als);
				MediaAssetPo ma = mediaService.getMaInfoById(au.getId());
				retM.put("ContentName", au.getAudioName());
				retM.put("ContentId", au.getId());
				retM.put("ContentPub", au.getAudioPublisher());
				retM.put("MediaType", "AUDIO");
				retM.put("ContentImg", ma.getMaImg());
				retM.put("PlayCount", "1234");
				if (makeSma) {
					Map<String, Object> smam = new HashMap<>();
					smam.put("ContentName", sma.getSmaTitle());
					smam.put("ContentId", sma.getId());
					smam.put("ContentImg", sma.getSmaImg());
					smam.put("ContentPub", sma.getSmaPublisher());
					smam.put("MediaType", "SEQU");
					smam.put("PlayCount", "1234");
					retM.put("SeqInfo", smam);
				}
//				return retM; TODO
			} else {
				if (album.getAlbumPo().getAlbumPublisher().equals("蜻蜓")) {
					ChannelService channelService = (ChannelService) SpringShell.getBean("channelService");
					List<ChannelPo> chlist = channelService.getChannelList();
					if (chlist == null || chlist.size() == 0) {
						return null;
					}
					List<AlbumPo> als = new ArrayList<>();
					als.add(album.getAlbumPo());
					etl2Service.makeNewAlbums(als, album.getAudiolist(), chlist);
					MediaAssetPo ma = mediaService.getMaInfoById(au.getId());
					Map<String, Object> retM = new HashMap<>();
					if (ma != null) {
						retM.put("ContentId", au.getId());
						retM.put("ContentName", au.getAudioName());
						retM.put("ContentImg", ma.getMaImg());
						retM.put("ContentPlay", au.getAudioURL());
						retM.put("ContentPub", ma.getMaPublisher());
						retM.put("ContentTime", ma.getTimeLong());
						retM.put("MediaType", "AUDIO");
						retM.put("PlayCount", "1234");
					} else
						return null;
					if (makeSma) {
						List<SeqMediaAssetPo> sms = mediaService.getSmaByMaId(ma.getId(), ma.getMaPublisher());
						if (sms != null && sms.size() > 0) {
							SeqMediaAssetPo sma = sms.get(0);
							Map<String, Object> smam = new HashMap<>();
							smam.put("ContentName", sma.getSmaTitle());
							smam.put("ContentId", sma.getId());
							smam.put("ContentImg", sma.getSmaImg());
							smam.put("ContentPub", sma.getSmaPublisher());
							smam.put("MediaType", "SEQU");
							smam.put("PlayCount", "123");
							retM.put("SeqInfo", smam);
						}
					}
//					return retM;TODO
				}
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
	public static Map<String, Object> festival2Audio(boolean makeSma, Festival festival) {
		// ContentCatalogs内容分类、ContentKeyWord关键词、ContentSubjectWord主题词和PlayCount播放次数未定义参数
		if (festival == null)
			return null;
		MediaService mediaService = (MediaService) SpringShell.getBean("mediaService");
		Map<String, Object> m = new HashMap<>();
		if (festival.getAudioName().contains(" - ")) {
			festival.setAudioName(festival.getAudioName().substring(festival.getAudioName().indexOf(" - ") + 3,
					festival.getAudioName().length()));
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
			Map<String, Object> retM = new HashMap<String, Object>();
			retM.put("ContentId", ma.getId());
			retM.put("ContentName", ma.getMaTitle());
			retM.put("ContentImg", ma.getMaImg());
			retM.put("ContentPlay", ma.getMaURL());
			retM.put("ContentPub", ma.getMaPublisher());
			retM.put("ContentTime", ma.getTimeLong());
			retM.put("MediaType", "AUDIO");
			retM.put("PlayCount", "1234");
			if (makeSma) {
				List<SeqMediaAssetPo> smas = mediaService.getSmaByMaId(ma.getId(), ma.getMaPublisher());
				if (smas != null && smas.size() > 0) {
					SeqMediaAssetPo sma = smas.get(0);
					Map<String, Object> smam = new HashMap<>();
					smam.put("ContentName", sma.getSmaTitle());
					smam.put("ContentId", sma.getId());
					smam.put("ContentImg", sma.getSmaImg());
					smam.put("ContentPub", sma.getSmaPublisher());
					smam.put("MediaType", "SEQU");
					smam.put("PlayCount", "123");
					retM.put("SeqInfo", smam);
				}
			}
			return retM;
		} else {
			List<SeqMediaAssetPo> smas = mediaService.getSmaList(festival.getAlbumName(), festival.getContentPub());
			if (smas != null) {
				List<Map<String, Object>> cate2dictdlist = FileUtils
						.readFileByJson(SystemCache.getCache(CrawlerConstants.APP_PATH).getContent() + "conf/craw.txt");
				ChannelService channelService = (ChannelService) SpringShell.getBean("channelService");
				List<ChannelPo> chlist = channelService.getChannelList();
				if (chlist == null || chlist.size() == 0) {
					return null;
				}
				if (smas != null && smas.size() > 0) {
					SeqMediaAssetPo sma = smas.get(0);
					Map<String, Object> retM = ConvertUtils.convert2MediaAsset(festival, sma, cate2dictdlist, chlist);
					if (makeSma) {
						Map<String, Object> smam = new HashMap<>();
						smam.put("ContentName", sma.getSmaTitle());
						smam.put("ContentId", sma.getId());
						smam.put("ContentImg", sma.getSmaImg());
						smam.put("ContentPub", sma.getSmaPublisher());
						smam.put("MediaType", "SEQU");
						smam.put("PlayCount", "1234");
						retM.put("SeqInfo", smam);
					}
					return retM;
				}
			} else {
				Etl2Service etl2Service = (Etl2Service) SpringShell.getBean("etl2Service");
				if (festival.getContentPub().equals("喜马拉雅")) {
					try {
						String url = "http://www.ximalaya.com/" + festival.getPersonId() + "/album/"
								+ festival.getAlbumId();
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
							ChannelService channelService = (ChannelService) SpringShell.getBean("channelService");
							List<ChannelPo> chlist = channelService.getChannelList();
							if (chlist == null || chlist.size() == 0) {
								return null;
							}
							etl2Service.makeNewAlbums(albs, adus, chlist);
							AudioPo retAu = adus.get(0);
							MediaAssetPo ma = mediaService.getMaInfoById(retAu.getId());
							if (ma != null) {
								Map<String, Object> retM = new HashMap<>();
								retM.put("ContentId", ma.getId());
								retM.put("ContentName", ma.getMaTitle());
								retM.put("ContentImg", ma.getMaImg());
								retM.put("ContentPlay", ma.getMaURL());
								retM.put("ContentPub", ma.getMaPublisher());
								retM.put("ContentTime", ma.getTimeLong());
								retM.put("MediaType", "AUDIO");
								retM.put("PlayCount", "1234");
								if (makeSma) {
									List<SeqMediaAssetPo> sms = mediaService.getSmaByMaId(ma.getId(),
											ma.getMaPublisher());
									if (sms != null && sms.size() > 0) {
										SeqMediaAssetPo sma = sms.get(0);
										Map<String, Object> smam = new HashMap<>();
										smam.put("ContentName", sma.getSmaTitle());
										smam.put("ContentId", sma.getId());
										smam.put("ContentImg", sma.getSmaImg());
										smam.put("ContentPub", sma.getSmaPublisher());
										smam.put("MediaType", "SEQU");
										smam.put("PlayCount", "1234");
										retM.put("SeqInfo", smam);
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
						String url = "http://www.qingting.fm/s/vchannels/" + festival.getAlbumId();
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
						if (albs != null && albs.size() > 0 && aus != null && aus.size() > 0) {
							Map<String, Object> retM = new HashMap<>();
							for (AudioPo audioPo : aus) {
								if (audioPo.getAudioId().equals(festival.getAudioId())) {
									retM.put("ContentId", audioPo.getId());
									break;
								}
							}
							ChannelService channelService = (ChannelService) SpringShell.getBean("channelService");
							List<ChannelPo> chlist = channelService.getChannelList();
							if (chlist == null || chlist.size() == 0) {
								return null;
							}
							etl2Service.makeNewAlbums(albs, aus, chlist);
							MediaAssetPo ma = mediaService.getMaInfoById(retM.get("ContentId") + "");
							if (ma != null) {
								retM.put("ContentId", ma.getId());
								retM.put("ContentName", ma.getMaTitle());
								retM.put("ContentImg", ma.getMaImg());
								retM.put("ContentPlay", ma.getMaURL());
								retM.put("ContentPub", ma.getMaPublisher());
								retM.put("ContentTime", ma.getTimeLong());
								retM.put("MediaType", "AUDIO");
								retM.put("PlayCount", "1234");
								if (makeSma) {
									List<SeqMediaAssetPo> sms = mediaService.getSmaByMaId(ma.getId(),
											ma.getMaPublisher());
									if (sms != null && sms.size() > 0) {
										SeqMediaAssetPo sma = sms.get(0);
										Map<String, Object> smam = new HashMap<>();
										smam.put("ContentName", sma.getSmaTitle());
										smam.put("ContentId", sma.getId());
										smam.put("ContentImg", sma.getSmaImg());
										smam.put("ContentPub", sma.getSmaPublisher());
										smam.put("MediaType", "SEQU");
										smam.put("PlayCount", "123");
										retM.put("SeqInfo", smam);
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
		map.put("ContentSubCount", (station.getFestivals().size()) + "");
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

	@SuppressWarnings("unchecked")
	public static Map<String, Object> Audio(boolean makeSma, AudioPo au) {
		MediaService mediaService = (MediaService) SpringShell.getBean("mediaService");
		Map<String, Object> m = new HashMap<>();
		m.put("maTitle", au.getAudioName());
		m.put("maPublisher", au.getAudioPublisher());
		List<MediaAssetPo> mas = mediaService.getMaList(m);
		Etl2Service etl2Service = (Etl2Service) SpringShell.getBean("etl2Service");
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
			Map<String, Object> retM = new HashMap<String, Object>();
			retM.put("ContentId", ma.getId());
			retM.put("ContentName", ma.getMaTitle());
			retM.put("ContentImg", ma.getMaImg());
			retM.put("ContentPlay", ma.getMaURL());
			retM.put("ContentPub", ma.getMaPublisher());
			retM.put("ContentTime", ma.getTimeLong());
			retM.put("MediaType", "AUDIO");
			retM.put("PlayCount", "1234");
			if (makeSma) {
				List<SeqMediaAssetPo> smas = mediaService.getSmaByMaId(ma.getId(), ma.getMaPublisher());
				if (smas != null && smas.size() > 0) {
					SeqMediaAssetPo sma = smas.get(0);
					Map<String, Object> smam = new HashMap<>();
					smam.put("ContentName", sma.getSmaTitle());
					smam.put("ContentId", sma.getId());
					smam.put("ContentImg", sma.getSmaImg());
					smam.put("ContentPub", sma.getSmaPublisher());
					smam.put("MediaType", "SEQU");
					smam.put("PlayCount", "123");
					retM.put("SeqInfo", smam);
				}
			}
			return retM;
		}
		List<SeqMediaAssetPo> smas = mediaService.getSmaList(au.getAlbumName(), au.getAudioPublisher());
		if (smas != null) {
			if (smas != null && smas.size() > 0) {
				SeqMediaAssetPo sma = smas.get(0);
				AlbumPo alPo = new AlbumPo();
				alPo.setAlbumName(au.getAlbumName());
				alPo.setAlbumPublisher(au.getAudioPublisher());
				List<Album> als = new ArrayList<>();
				Album al = new Album();
				al.setAlbumPo(alPo);
				List<AudioPo> aus = new ArrayList<>();
				aus.add(au);
				al.setAudiolist(aus);
				etl2Service.makeExistAlbums(als);
				Map<String, Object> retM = new HashMap<>();
				MediaAssetPo ma = mediaService.getMaInfoById(au.getId());
				retM.put("ContentName", au.getAudioName());
				retM.put("ContentId", au.getId());
				retM.put("ContentPub", au.getAudioPublisher());
				retM.put("MediaType", "AUDIO");
				retM.put("ContentImg", ma.getMaImg());
				retM.put("PlayCount", "1234");
				if (makeSma) {
					Map<String, Object> smam = new HashMap<>();
					smam.put("ContentName", sma.getSmaTitle());
					smam.put("ContentId", sma.getId());
					smam.put("ContentImg", sma.getSmaImg());
					smam.put("ContentPub", sma.getSmaPublisher());
					smam.put("MediaType", "SEQU");
					smam.put("PlayCount", "1234");
					retM.put("SeqInfo", smam);
				}
				return retM;
			}
		} else {
			if (au.getAudioPublisher().equals("蜻蜓")) {
				ChannelService channelService = (ChannelService) SpringShell.getBean("channelService");
				List<ChannelPo> chlist = channelService.getChannelList();
				String url = "http://i.qingting.fm/wapi/channels/" + au.getAlbumId();
				Map<String, Object> smam = HttpUtils.getJsonMapFromURL(url);
				if (smam != null) {
					smam = (Map<String, Object>) smam.get("data");
					AlbumPo albumPo = new AlbumPo();
					albumPo.setId(SequenceUUID.getPureUUID());
					albumPo.setAlbumId(smam.get("id") + "");
					albumPo.setAlbumName(smam.get("name") + "");
					albumPo.setAlbumImg(smam.get("img_url") + "");
					albumPo.setCategoryId(au.getCategoryId());
					albumPo.setCategoryName(au.getCategoryName());
					albumPo.setAlbumPublisher("蜻蜓");
					albumPo.setDescn(smam.get("desc") + "");
					albumPo.setPlayCount(ConvertUtils.convertPlayNum2Long(smam.get("playcount") + ""));
					if (!smam.get("update_time").equals("null")) {
						albumPo.setcTime(new Timestamp(ConvertUtils.makeLongTime(smam.get("update_time") + "")));
					} else {
						albumPo.setcTime(new Timestamp(System.currentTimeMillis()));
					}

					url = "http://api2.qingting.fm/v6/media/channelondemands/" + albumPo.getAlbumId()
							+ "/programs/order/0/curpage/1/pagesize/50";
					Map<String, Object> aums = HttpUtils.getJsonMapFromURL(url);
					List<Map<String, Object>> auls = (List<Map<String, Object>>) aums.get("data");
					if (auls != null && auls.size() > 0) {
						List<AudioPo> audioPos = new ArrayList<>();
						for (Map<String, Object> am : auls) {
							AudioPo aPo = new AudioPo();
							aPo.setId(SequenceUUID.getPureUUID());
							aPo.setAudioId(am.get("id") + "");
							aPo.setAudioName(am.get("title") + "");
							aPo.setAudioImg(albumPo.getAlbumImg());
							aPo.setCategoryId(albumPo.getCategoryId());
							aPo.setCategoryName(albumPo.getCategoryName());
							aPo.setAlbumId(albumPo.getAlbumId());
							aPo.setAlbumName(albumPo.getAlbumName());
							aPo.setAudioPublisher("蜻蜓");
							aPo.setDuration(am.get("duration") + "");
							aPo.setcTime(new Timestamp(ConvertUtils.makeLongTime(am.get("update_time") + "")));
							Map<String, Object> ms = (Map<String, Object>) am.get("mediainfo");
							if (ms != null) {
								List<Map<String, Object>> pls = (List<Map<String, Object>>) ms.get("bitrates_url");
								if (pls != null && pls.size() > 0) {
									aPo.setAudioURL("http://od.qingting.fm/" + pls.get(0).get("file_path"));
									audioPos.add(aPo);
								}
							} else {
								return null;
							}
						}
						List<AlbumPo> als = new ArrayList<>();
						als.add(albumPo);
						etl2Service.makeNewAlbums(als, audioPos, chlist);
						Map<String, Object> retM = new HashMap<>();
						MediaAssetPo ma = mediaService.getMaInfoById(au.getId());
						if (ma == null)
							return null;
						retM.put("ContentName", au.getAudioName());
						retM.put("ContentId", au.getId());
						retM.put("ContentPub", au.getAudioPublisher());
						retM.put("MediaType", "AUDIO");
						retM.put("ContentImg", ma.getMaImg());
						retM.put("PlayCount", "1234");
						if (makeSma) {
							smas = mediaService.getSmaByMaId(ma.getId(), ma.getMaPublisher());
							if (smas != null && smas.size() > 0) {
								SeqMediaAssetPo sma = smas.get(0);
								smam = new HashMap<>();
								smam.put("ContentName", sma.getSmaTitle());
								smam.put("ContentId", sma.getId());
								smam.put("ContentImg", sma.getSmaImg());
								smam.put("ContentPub", sma.getSmaPublisher());
								smam.put("MediaType", "SEQU");
								smam.put("PlayCount", "123");
								retM.put("SeqInfo", smam);
							}
						}
						return retM;
					}
				}
			}
		}
		return null;
	}

	public static Map<String, Object> albumPo(boolean makeSma, AlbumPo albumPo) {
		MediaService mediaService = (MediaService) SpringShell.getBean("mediaService");
		ResOrgAssetService resOrgAssetService = (ResOrgAssetService) SpringShell.getBean("resOrgAssetService");
		Etl2Service etl2Service = (Etl2Service) SpringShell.getBean("etl2Service");
		if (albumPo.getAudioPos() != null && albumPo.getAudioPos().size() > 0) {
			AudioPo au = albumPo.getAudioPos().get(0);
			Map<String, Object> m = new HashMap<>();
			m.put("resTableName", "wt_MediaAsset");
			m.put("orgName", au.getAudioPublisher());
			m.put("origSrcId", au.getAudioId());
			List<ResOrgAssetPo> resList = resOrgAssetService.getResOrgAssetPo(m);
			if (resList != null && resList.size()>0) {
				ResOrgAssetPo resOrgAssetPo = resList.get(0);
				m.clear();
				m.put("maTitle", au.getAudioName());
				m.put("maPublisher", au.getAudioPublisher());
				m.put("id", resOrgAssetPo.getResId());
				List<MediaAssetPo> mas = mediaService.getMaList(m);
				if (mas != null && mas.size() > 0) {
					MediaAssetPo ma = mas.get(0);
					ChannelService channelService = (ChannelService) SpringShell.getBean("channelService");
					m.clear();
					m.put("assetType", "wt_MediaAsset");
					m.put("assetId", ma.getId());
					m.put("flowFlag", 2);
					List<ChannelAssetPo> chas = channelService.getChannelAssetListBy(m);
					if (chas == null || chas.size() > 0) {
						return null;
					}
					Map<String, Object> retM = new HashMap<String, Object>();
					retM.put("ContentId", ma.getId());
					retM.put("ContentName", ma.getMaTitle());
					retM.put("ContentImg", ma.getMaImg());
					retM.put("ContentPlay", ma.getMaURL());
					retM.put("ContentPub", ma.getMaPublisher());
					retM.put("ContentTime", ma.getTimeLong());
					retM.put("MediaType", "AUDIO");
					retM.put("PlayCount", "1234");
					if (makeSma) {
						List<SeqMediaAssetPo> smas = mediaService.getSmaByMaId(ma.getId(), ma.getMaPublisher());
						if (smas != null && smas.size() > 0) {
							SeqMediaAssetPo sma = smas.get(0);
							Map<String, Object> smam = new HashMap<>();
							smam.put("ContentName", sma.getSmaTitle());
							smam.put("ContentId", sma.getId());
							smam.put("ContentImg", sma.getSmaImg());
							smam.put("ContentPub", sma.getSmaPublisher());
							smam.put("MediaType", "SEQU");
							smam.put("PlayCount", "123");
							retM.put("SeqInfo", smam);
						}
					}
//					return retM;TODO
				}
			} else {
				m.clear();
				m.put("resTableName", "wt_SeqMediaAsset");
				m.put("orgName", albumPo.getAlbumPublisher());
				m.put("origSrcId", albumPo.getAlbumId());
				resList = resOrgAssetService.getResOrgAssetPo(m);
				if (resList != null && resList.size()>0) {
					SeqMediaAssetPo sma = mediaService.getSeqInfo(resList.get(0).getResId());
					if (sma != null) {
						m.clear();
						m.put("maPublisher", sma.getSmaPublisher());
						m.put("whereSql", " id in (select mId from wt_SeqMA_Ref where sId = '" + sma.getId() + "')");
						List<MediaAssetPo> oldmas = mediaService.getMaList(m);
						Iterator<AudioPo> itau = albumPo.getAudioPos().iterator();
						if (oldmas != null && oldmas.size() > 0) {
							while (itau.hasNext()) {
								AudioPo audioPo = (AudioPo) itau.next();
								for (MediaAssetPo mediaAssetPo : oldmas) {
									if (audioPo.getAudioName().equals(mediaAssetPo.getMaTitle())) {
										itau.remove();
									}
								}
							}
						}
						Album album = new Album();
						album.setAlbumPo(albumPo);
						album.setAudiolist(albumPo.getAudioPos());
						List<Album> als = new ArrayList<>();
						etl2Service.makeExistAlbums(als);
						MediaAssetPo ma = mediaService.getMaInfoById(au.getId());
						Map<String, Object> retM = new HashMap<>();
						retM.put("ContentName", au.getAudioName());
						retM.put("ContentId", au.getId());
						retM.put("ContentPub", au.getAudioPublisher());
						retM.put("MediaType", "AUDIO");
						retM.put("ContentImg", ma.getMaImg());
						retM.put("PlayCount", "1234");
						if (makeSma) {
							Map<String, Object> smam = new HashMap<>();
							smam.put("ContentName", sma.getSmaTitle());
							smam.put("ContentId", sma.getId());
							smam.put("ContentImg", sma.getSmaImg());
							smam.put("ContentPub", sma.getSmaPublisher());
							smam.put("MediaType", "SEQU");
							smam.put("PlayCount", "1234");
							retM.put("SeqInfo", smam);
						}
//						return retM;TODO
					}
				} else {
					if (albumPo.getAlbumPublisher().equals("喜马拉雅")) {
						ChannelService channelService = (ChannelService) SpringShell.getBean("channelService");
						List<ChannelPo> chlist = channelService.getChannelList();
						if (chlist == null || chlist.size() == 0) {
							return null;
						}
						List<AlbumPo> als = new ArrayList<>();
						als.add(albumPo);
						etl2Service.makeNewAlbums(als, albumPo.getAudioPos(), chlist);
						MediaAssetPo ma = mediaService.getMaInfoById(au.getId());
						Map<String, Object> retM = new HashMap<>();
						if (ma != null) {
							retM.put("ContentId", au.getId());
							retM.put("ContentName", au.getAudioName());
							retM.put("ContentImg", ma.getMaImg());
							retM.put("ContentPlay", au.getAudioURL());
							retM.put("ContentPub", ma.getMaPublisher());
							retM.put("ContentTime", ma.getTimeLong());
							retM.put("MediaType", "AUDIO");
							retM.put("PlayCount", "1234");
						} else
							return null;
						if (makeSma) {
							List<SeqMediaAssetPo> sms = mediaService.getSmaByMaId(ma.getId(), ma.getMaPublisher());
							if (sms != null && sms.size() > 0) {
								SeqMediaAssetPo sma = sms.get(0);
								Map<String, Object> smam = new HashMap<>();
								smam.put("ContentName", sma.getSmaTitle());
								smam.put("ContentId", sma.getId());
								smam.put("ContentImg", sma.getSmaImg());
								smam.put("ContentPub", sma.getSmaPublisher());
								smam.put("MediaType", "SEQU");
								smam.put("PlayCount", "123");
								retM.put("SeqInfo", smam);
							}
						}
//						return retM;TODO
					}
				}
			}
		}
		return null;
	}

	public static Map<String, Object> AudioPo(boolean makeSma, AudioPo au) {
		MediaService mediaService = (MediaService) SpringShell.getBean("mediaService");
		ResOrgAssetService resOrgAssetService = (ResOrgAssetService) SpringShell.getBean("resOrgAssetService");
		Etl2Service etl2Service = (Etl2Service) SpringShell.getBean("etl2Service");
		Map<String, Object> m = new HashMap<>();
		m.put("resTableName", "wt_MediaAsset");
		m.put("orgName", au.getAudioPublisher());
		m.put("origSrcId", au.getAudioId());
		List<ResOrgAssetPo> resList = resOrgAssetService.getResOrgAssetPo(m);
		if (resList != null && resList.size()>0) {
			m.clear();
			m.put("maTitle", au.getAudioName());
			m.put("maPublisher", au.getAudioPublisher());
			m.put("id", resList.get(0).getResId());
			List<MediaAssetPo> mas = mediaService.getMaList(m);
			if (mas != null && mas.size() > 0) {
				MediaAssetPo ma = mas.get(0);
				ChannelService channelService = (ChannelService) SpringShell.getBean("channelService");
				m.clear();
				m.put("assetType", "wt_MediaAsset");
				m.put("assetId", ma.getId());
				m.put("flowFlag", 2);
				List<ChannelAssetPo> chas = channelService.getChannelAssetListBy(m);
				if (chas == null || chas.size() > 0) {
					return null;
				}
				Map<String, Object> retM = new HashMap<String, Object>();
				retM.put("ContentId", ma.getId());
				retM.put("ContentName", ma.getMaTitle());
				retM.put("ContentImg", ma.getMaImg());
				retM.put("ContentPlay", ma.getMaURL());
				retM.put("ContentPub", ma.getMaPublisher());
				retM.put("ContentTime", ma.getTimeLong());
				retM.put("MediaType", "AUDIO");
				retM.put("PlayCount", "1234");
				if (makeSma) {
					List<SeqMediaAssetPo> smas = mediaService.getSmaByMaId(ma.getId(), ma.getMaPublisher());
					if (smas != null && smas.size() > 0) {
						SeqMediaAssetPo sma = smas.get(0);
						Map<String, Object> smam = new HashMap<>();
						smam.put("ContentName", sma.getSmaTitle());
						smam.put("ContentId", sma.getId());
						smam.put("ContentImg", sma.getSmaImg());
						smam.put("ContentPub", sma.getSmaPublisher());
						smam.put("MediaType", "SEQU");
						smam.put("PlayCount", "123");
						retM.put("SeqInfo", smam);
					}
				}
//				return retM; TODO
			}
		} else {
			m.clear();
			m.put("resTableName", "wt_SeqMediaAsset");
			m.put("orgName", au.getAudioPublisher());
			m.put("origSrcId", au.getAlbumId());
			resList = resOrgAssetService.getResOrgAssetPo(m);
			if (resList != null && resList.size()>0) {
				SeqMediaAssetPo sma = mediaService.getSeqInfo(resList.get(0).getResId());
				if (sma != null) {
					Album album = new Album();
					AlbumPo albumPo = new AlbumPo();
					albumPo.setAlbumId(au.getAlbumId());
					albumPo.setAlbumName(au.getAlbumName());
					albumPo.setAlbumPublisher(au.getAudioPublisher());
					List<AudioPo> aus = new ArrayList<>();
					aus.add(au);
					album.setAlbumPo(albumPo);
					album.setAudiolist(aus);
					List<Album> als = new ArrayList<>();
					als.add(album);
					etl2Service.makeExistAlbums(als);
					MediaAssetPo ma = mediaService.getMaInfoById(au.getId());
					if (ma != null) {
						Map<String, Object> retM = new HashMap<>();
						retM.put("ContentName", au.getAudioName());
						retM.put("ContentId", au.getId());
						retM.put("ContentPub", au.getAudioPublisher());
						retM.put("MediaType", "AUDIO");
						retM.put("ContentImg", ma.getMaImg());
						retM.put("PlayCount", "1234");
						if (makeSma) {
							Map<String, Object> smam = new HashMap<>();
							smam.put("ContentName", sma.getSmaTitle());
							smam.put("ContentId", sma.getId());
							smam.put("ContentImg", sma.getSmaImg());
							smam.put("ContentPub", sma.getSmaPublisher());
							smam.put("MediaType", "SEQU");
							smam.put("PlayCount", "1234");
							retM.put("SeqInfo", smam);
						}
//						return retM;TODO
					}
				}
			} else {
				if (au.getAudioPublisher().equals("喜马拉雅")) {
					ChannelService channelService = (ChannelService) SpringShell.getBean("channelService");
					List<ChannelPo> chlist = channelService.getChannelList();
					AlbumPo albumPo = new XiMaLaYaSearch().albumS(au.getVisitUrl().substring(0, au.getVisitUrl().indexOf("/sound/")) + "/album/" + au.getAlbumId());
					List<AudioPo> aus = new XiMaLaYaSearch().albumAudioS(albumPo.getVisitUrl().replace("http://www.ximalaya.com", ""));
					boolean isok = false;
					if (aus != null && aus.size() > 0) {
						for (AudioPo audioPo : aus) {
							if (audioPo.getAudioId().equals(au.getAudioId())) {
								isok = true;
								break;
							}
						}
					}
					if (!isok) {
						aus.add(au);
					}
					List<AlbumPo> als = new ArrayList<>();
					als.add(albumPo);
					etl2Service.makeNewAlbums(als, aus, chlist);
					m.clear();
					m.put("resTableName", "wt_SeqMediaAsset");
					m.put("orgName", au.getAudioPublisher());
					m.put("origSrcId", au.getAlbumId());
					resList = resOrgAssetService.getResOrgAssetPo(m);
					if (resList != null && resList.size()>0) {
						MediaAssetPo ma = mediaService.getMaInfoById(resList.get(0).getResId());
						if (ma != null) {
							Map<String, Object> retM = new HashMap<>();
							retM.put("ContentName", au.getAudioName());
							retM.put("ContentId", au.getId());
							retM.put("ContentPub", au.getAudioPublisher());
							retM.put("MediaType", "AUDIO");
							retM.put("ContentImg", ma.getMaImg());
							retM.put("PlayCount", "1234");
							if (makeSma) {
								SeqMediaAssetPo sma = mediaService.getSeqInfo(resList.get(0).getResId());
								if (sma != null) {
									Map<String, Object> smam = new HashMap<>();
									smam.put("ContentName", sma.getSmaTitle());
									smam.put("ContentId", sma.getId());
									smam.put("ContentImg", sma.getSmaImg());
									smam.put("ContentPub", sma.getSmaPublisher());
									smam.put("MediaType", "SEQU");
									smam.put("PlayCount", "1234");
									retM.put("SeqInfo", smam);
//									return retM;TODO
								}
							}
//							return retM;TODO
						}
					}
				} else {
					if (au.getAudioPublisher().equals("蜻蜓")) {
						ChannelService channelService = (ChannelService) SpringShell.getBean("channelService");
						List<ChannelPo> chlist = channelService.getChannelList();
						Album album = new QTSearch().albumS(au.getAlbumId());
						if (album!=null && album.getAudiolist().size()>0) {
							boolean isok = false;
							for (AudioPo audioPo : album.getAudiolist()) {
								audioPo.setCategoryName(au.getCategoryName());
								if (audioPo.getAudioId().equals(au.getAudioId())) {
									isok = true;
								}
							}
							if (!isok) {
								album.getAudiolist().add(au);
							}
						}
						List<AlbumPo> als = new ArrayList<>();
						album.getAlbumPo().setCategoryName(au.getCategoryName());
						als.add(album.getAlbumPo());
						List<AudioPo> aus = album.getAudiolist();
						etl2Service.makeNewAlbums(als, aus, chlist);
						m.clear();
						m.put("resTableName", "wt_SeqMediaAsset");
						m.put("orgName", au.getAudioPublisher());
						m.put("origSrcId", au.getAlbumId());
						resList = resOrgAssetService.getResOrgAssetPo(m);
						if (resList != null && resList.size()>0) {
							MediaAssetPo ma = mediaService.getMaInfoById(au.getId());
							if (ma != null) {
								Map<String, Object> retM = new HashMap<>();
								retM.put("ContentName", au.getAudioName());
								retM.put("ContentId", au.getId());
								retM.put("ContentPub", au.getAudioPublisher());
								retM.put("MediaType", "AUDIO");
								retM.put("ContentImg", ma.getMaImg());
								retM.put("PlayCount", "1234");
								if (makeSma) {
									SeqMediaAssetPo sma = mediaService.getSeqInfo(resList.get(0).getResId());
									if (sma != null) {
										Map<String, Object> smam = new HashMap<>();
										smam.put("ContentName", sma.getSmaTitle());
										smam.put("ContentId", sma.getId());
										smam.put("ContentImg", sma.getSmaImg());
										smam.put("ContentPub", sma.getSmaPublisher());
										smam.put("MediaType", "SEQU");
										smam.put("PlayCount", "1234");
										retM.put("SeqInfo", smam);
//										return retM;TODO
									}
								}
//								return retM;TODO
							}
						}
					}
				}
			}
		}
		return null;
	}
	
	public static void AlbumToReturn(String key, Album album) {
		AlbumPo albumPo = album.getAlbumPo();
		albumPo.setAudioPos(album.getAudiolist());
		AlbumPoToReturn(key, albumPo);
	}
	
	public static void AlbumPoToReturn(String key,AlbumPo albumPo) {
		if (albumPo!=null) {
			JedisConnectionFactory conn = (JedisConnectionFactory) SpringShell.getBean("connectionFactorySearch");
			RedisOperService ros = new RedisOperService(conn);
			
			Map<String, Object> retM = new HashMap<>();
			Map<String, Object> smamr = new HashMap<>();
			smamr.put("ContentName", albumPo.getAlbumName());
			smamr.put("ContentId", albumPo.getId());
			smamr.put("ContentImg", albumPo.getAlbumImg());
			smamr.put("ContentPub", albumPo.getAlbumPublisher());
			smamr.put("MediaType", "SEQU");
			smamr.put("PlayCount", "1234");
			retM.put("SeqInfo", smamr);
			
			Map<String, Object> smam = new HashMap<>();
			smam.put("ContentId", albumPo.getId());
			smam.put("ContentName", albumPo.getAlbumName());
			smam.put("ContentPubTime", albumPo.getcTime().getTime());
			smam.put("CTime", albumPo.getcTime().getTime());
			smam.put("ContentPub", albumPo.getAlbumPublisher());
			smam.put("ContentDescn", albumPo.getDescn());
			smam.put("MediaType", "SEQU");
			smam.put("ContentShareURL", "http://www.wotingfm.com:908/CM");
			smam.put("ContentImg", albumPo.getAlbumImg());
			smam.put("ConTentStatus", 1);
			smam.put("ContentSubCount", albumPo.getAudioPos().size());
			smam.put("ContentURI", "content/getContentInfo.do?");
			smam.put("ContentKeyWord", albumPo.getAlbumTags());
			smam.put("ContentSubjectWord", null);
			smam.put("ContentPubChannels", null);
			smam.put("ContentCatalogs", null);
			List<Map<String, Object>> subList = new ArrayList<>();
			List<AudioPo> aus = albumPo.getAudioPos();
			if (aus!=null && aus.size()>0) {
				boolean isok = true;
				for (AudioPo audioPo : aus) {
					try {
						if (isok) {
							retM.put("ContentName", audioPo.getAudioName());
							retM.put("ContentId", audioPo.getId());
							retM.put("ContentPub", audioPo.getAudioPublisher());
							retM.put("MediaType", "AUDIO");
							retM.put("ContentImg", audioPo.getAudioImg());
							retM.put("PlayCount", "1234");
						    ros.rPush("Search_" + key + "_Data", JsonUtils.objToJson(retM));
						    isok = false;
						}
						Map<String, Object> mam = new HashMap<>();
					    mam.put("ContentId", audioPo.getId());
					    mam.put("ContentName", audioPo.getAudioName());
					    mam.put("ContentImg", audioPo.getAudioImg());
					    mam.put("ContentPubTime", audioPo.getcTime().getTime());
					    mam.put("CTime", audioPo.getcTime().getTime());
					    mam.put("ContentPub", audioPo.getAudioPublisher());
					    mam.put("ContentPlay", audioPo.getAudioURL());
					    mam.put("ContentDescn", audioPo.getDescn());
					    mam.put("MediaType", "AUDIO");
					    mam.put("ContentStatus", 1);
					    mam.put("ContentTimes", audioPo.getDuration());
					    mam.put("ContentKeyWord", audioPo.getAudioTags());
					    mam.put("ContentSubjectWord", null);
					    mam.put("ContentPubChannels", null);
					    mam.put("ContentCatalogs", null);
					    String ext = FileNameUtils.getExt(audioPo.getAudioURL());
			            if (ext.contains("?")) {
						    ext = ext.replace(ext.substring(ext.indexOf("?"), ext.length()), ""); 
					    }
			            if (ext!=null) {
			        	    mam.put("ContentPlayType", ext.contains("/flv")?"flv":ext.replace(".", ""));
					    } else {
						    mam.put("ContentPlayType", null);
					    }
			            //内容固定信息
			            SearchUtils.addRedisValue("CONTENT_AUDIO_INFO_"+audioPo.getId(), JsonUtils.objToJson(mam), ros);
			            System.out.println("CONTENT_SEQU_INFO_"+albumPo.getId());
			            String playCount = "";
			            if (audioPo.getPlayCount()==null) {
						    playCount = "1234";
					    } else {
						    playCount = audioPo.getPlayCount();
					    }
			            SearchUtils.addRedisValue("CONTENT_AUDIO_PLAYCOUNT_"+audioPo.getId(), playCount, ros);
			            SearchUtils.addRedisValue("CONTENT_AUDIO_SAVED_"+audioPo.getId(), "false", ros);
			            subList.add(mam);
					} catch (Exception e) {
						e.printStackTrace();
						continue;
					}
				}
				if (subList!=null && subList.size()>0) {
					smam.put("SubList", subList);
					SearchUtils.addRedisValue("CONTENT_SEQU_INFO_"+albumPo.getId(), JsonUtils.objToJson(smam), ros);
					String playCount = "";
					if (albumPo.getPlayCount()==null) {
						playCount = "1234";
					} else {
						playCount = albumPo.getPlayCount();
					}
					SearchUtils.addRedisValue("CONTENT_SEQU_PLAYCOUNT_"+albumPo.getId(), playCount, ros);
					SearchUtils.addRedisValue("CONTENT_SEQU_SAVED_"+albumPo.getId(), "false", ros);
				}
			}
			ros.close();
	        conn.destroy();
		}
	}
	
	public static void AudioPoToReturn(String key, AudioPo audioPo) {
		JedisConnectionFactory conn = (JedisConnectionFactory) SpringShell.getBean("connectionFactorySearch");
		RedisOperService ros = new RedisOperService(conn);
		
		Map<String, Object> retM = new HashMap<>();
		retM.put("ContentName", audioPo.getAudioName());
		retM.put("ContentId", audioPo.getId());
		retM.put("ContentPub", audioPo.getAudioPublisher());
		retM.put("MediaType", "AUDIO");
		retM.put("ContentImg", audioPo.getAudioImg());
		retM.put("PlayCount", "1234");
	    ros.rPush("Search_" + key + "_Data", JsonUtils.objToJson(retM));
		
		Map<String, Object> mam = new HashMap<>();
	    mam.put("ContentId", audioPo.getId());
	    mam.put("ContentName", audioPo.getAudioName());
	    mam.put("ContentImg", audioPo.getAudioImg());
	    mam.put("ContentPubTime", audioPo.getcTime().getTime());
	    mam.put("CTime", audioPo.getcTime().getTime());
	    mam.put("ContentPub", audioPo.getAudioPublisher());
	    mam.put("ContentPlay", audioPo.getAudioURL());
	    mam.put("ContentDescn", audioPo.getDescn());
	    mam.put("MediaType", "AUDIO");
	    mam.put("ContentStatus", 1);
	    mam.put("ContentTimes", audioPo.getDuration());
	    mam.put("ContentKeyWord", audioPo.getAudioTags());
	    mam.put("ContentSubjectWord", null);
	    mam.put("ContentPubChannels", null);
	    mam.put("ContentCatalogs", null);
	    String ext = FileNameUtils.getExt(audioPo.getAudioURL());
        if (ext.contains("?")) {
		    ext = ext.replace(ext.substring(ext.indexOf("?"), ext.length()), ""); 
	    }
        if (ext!=null) {
    	    mam.put("ContentPlayType", ext.contains("/flv")?"flv":ext.replace(".", ""));
	    } else {
		    mam.put("ContentPlayType", null);
	    }
        //内容固定信息
        SearchUtils.addRedisValue("CONTENT_AUDIO_INFO_"+audioPo.getId(), JsonUtils.objToJson(mam), ros);
        System.out.println("CONTENT_AUDIO_INFO_"+audioPo.getId());
        String playCount = "";
        if (audioPo.getPlayCount()==null) {
		    playCount = "1234";
	    } else {
		    playCount = audioPo.getPlayCount();
	    }
        SearchUtils.addRedisValue("CONTENT_AUDIO_PLAYCOUNT_"+audioPo.getId(), playCount, ros);
        SearchUtils.addRedisValue("CONTENT_AUDIO_SAVED_"+audioPo.getId(), "false", ros);
        ros.close();
        conn.destroy();
	}
}
