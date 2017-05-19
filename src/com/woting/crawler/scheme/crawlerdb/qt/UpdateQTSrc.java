package com.woting.crawler.scheme.crawlerdb.qt;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.spiritdata.framework.util.JsonUtils;
import com.spiritdata.framework.util.SequenceUUID;
import com.woting.cm.core.ResOrgAsset.persis.po.ResOrgAssetPo;
import com.woting.cm.core.ResOrgAsset.service.ResOrgAssetService;
import com.woting.crawler.core.album.persis.po.AlbumPo;
import com.woting.crawler.core.album.service.AlbumService;
import com.woting.crawler.core.albumaudioref.persis.po.AlbumAudioRefPo;
import com.woting.crawler.core.albumaudioref.service.AlbumAudioRefService;
import com.woting.crawler.core.audio.persis.po.AudioPo;
import com.woting.crawler.core.audio.service.AudioService;
import com.woting.crawler.core.cperson.persis.po.CPersonPo;
import com.woting.crawler.core.cperson.persis.po.CPersonRefPo;
import com.woting.crawler.core.cperson.service.CPersonService;
import com.woting.crawler.core.cplaycount.persis.po.CPlayCountPo;
import com.woting.crawler.core.cplaycount.service.CPlayCountService;
import com.woting.crawler.core.dict.persis.po.DictRefPo;
import com.woting.crawler.core.dict.service.CrawlerDictService;
import com.woting.crawler.core.samedb.persis.po.SameDBPo;
import com.woting.crawler.core.samedb.service.SameDBService;
import com.woting.crawler.ext.SpringShell;
import com.woting.crawler.scheme.utils.ConvertUtils;

public class UpdateQTSrc {
	private static AlbumService albumService;
	private static AudioService audioService;
	private static AlbumAudioRefService albumAudioRefService;
	private static CPersonService cPersonService;
	private static CPlayCountService cPlayCountService;
	private static CrawlerDictService crawlerDictService;
	private static ResOrgAssetService resAssService;
	private static SameDBService sameDBService;
	
	public void updateSrc() {
		loadService();
		while (true) {
//			try {Thread.sleep(4*60*60*1000);} catch (Exception e) {};
			long beg = System.currentTimeMillis();
			Map<String, Object> m = new HashMap<>();
			m.put("albumPublisher", "蜻蜓");
			m.put("isValidate", 3);
			List<String> numLs = new ArrayList<>();
			List<AlbumPo> albumPos = albumService.getAlbumListBy(m);
			if (albumPos!=null && albumPos.size()>0) {
				QTCrawler qtCrawler = new QTCrawler(false);
				ExecutorService fixedThreadPool = Executors.newFixedThreadPool(10);
				for (AlbumPo albumPo : albumPos) {
					fixedThreadPool.execute(new Runnable() {
						@SuppressWarnings("unchecked")
						public void run() {
							String auSizeStr = qtCrawler.insertNewZJ(albumPo.getAlbumId(), "1", false);
							int auSize = 0;
							try {auSize = Integer.valueOf(auSizeStr);} catch (Exception e) {}
							int _auSize = albumAudioRefService.getAlbumAudioRefNum(albumPo.getId());
							numLs.add(albumPo.getId());
							if (auSize > _auSize) {
								List<AlbumAudioRefPo> audioRefPos = albumAudioRefService.getAlbumAudioRefs(albumPo.getId());
								if (audioRefPos!=null && audioRefPos.size()>0) {
									System.out.println(auSize + "     " + _auSize  + "    " + albumPo.getVisitUrl());
									Map<String, Object> map = new HashMap<>();
									List<Map<String, Object>> aus = new ArrayList<>();
									try {
										map = getZJData(albumPo.getAlbumId(), auSizeStr);
										if (map!=null && map.size()>0) {
											aus = (List<Map<String, Object>>) map.get("audios");
										} else return;
									} catch (Exception e) {}
									if (aus!=null && aus.size()>0 ) {
										for (int i = 0; i < aus.size(); i++) {
											Map<String, Object> audiomap = aus.get(i);
											String audioId = audiomap.get("id").toString();
											String id = "QT_AUDIO_" + audioId;
											if (audioService.getAudioInfo(id)==null) {
												AudioPo audioPo = new AudioPo();
												audioPo.setId(id);
												audioPo.setAudioId(audiomap.get("id").toString());
												audioPo.setAudioName(audiomap.get("title").toString());
												audioPo.setAudioImg(albumPo.getAlbumImg());
												audioPo.setAudioPublisher("蜻蜓");
												double duration = Double.valueOf(audiomap.get("duration").toString());
												audioPo.setDuration((int)duration*1000+"");
												Map<String, Object> urlmap = (Map<String, Object>) audiomap.get("mediainfo");
												if (urlmap==null || urlmap.size()==0) {continue;}
												List<Map<String, Object>> urllist = (List<Map<String, Object>>) urlmap.get("bitrates_url");
												if (urllist !=null && urllist.size()>0) {
													urlmap = urllist.get(urllist.size()-1);
												} else continue;
												String uri = urlmap.get("file_path").toString();
												uri = "http://od.qingting.fm/"+uri;
												audioPo.setAudioURL(uri);
												audioPo.setVisitUrl("http://neo.qingting.fm/channels/"+albumPo.getAlbumId()+"/programs/"+audioPo.getAudioId());
												String updatetime = audiomap.get("update_time").toString();
												if (updatetime.equals("0000-00-00 00:00:00")) audioPo.setPubTime(new Timestamp(System.currentTimeMillis()));
												else {audioPo.setPubTime(new Timestamp(ConvertUtils.makeLongTime(updatetime)));}
												audioPo.setcTime(new Timestamp(System.currentTimeMillis()));
												String playcount = "0";
												try {
													Document doc = Jsoup.connect("http://i.qingting.fm/wapi/program_playcount?pids="+albumPo.getAlbumId()+"_"+audioPo.getAudioId()).ignoreContentType(true).timeout(10000).get();
													playcount =  doc.body().html();
													Map<String, Object> playcountmap = (Map<String, Object>) JsonUtils.jsonToObj(playcount, Map.class);
													List<Map<String, Object>> playcounts =  (List<Map<String, Object>>) playcountmap.get("data");
													if (playcounts!=null && playcounts.size()>0) {
														for (Map<String, Object> map2 : playcounts) {
															if (map2.get("id").equals(albumPo.getAlbumId()+"_"+audioPo.getAudioId())) {
																playcount = QTEtl1Process.makePlayCount(map2.get("playcount").toString())+"";
															}
														}
													}
												} catch (Exception e) {System.out.println("播放次数出错  "+audioPo.getAudioId());}
												try {
													audioService.insertAudio(audioPo);
												} catch (Exception e) {}
												
												//获得专辑的分类信息
												List<DictRefPo> dictRefLs = crawlerDictService.getDictRefs(albumPo.getId(), "c_Album", null);
												
												if (dictRefLs!=null && dictRefLs.size()>0) {
													for (DictRefPo dictRef : dictRefLs) {
														DictRefPo dictRefPo = new DictRefPo();
														dictRefPo.setId(SequenceUUID.getPureUUID());
														dictRefPo.setCdictMid(dictRef.getCdictMid());
														dictRefPo.setCdictDid(dictRef.getCdictDid());
														dictRefPo.setResId(audioPo.getId());
														dictRefPo.setResTableName("c_Audio");
														try {crawlerDictService.insertDictRef(dictRefPo);} catch (Exception e) {continue;}
													}
												}
												
												try {
													AlbumAudioRefPo aRefPo = new AlbumAudioRefPo();
													aRefPo.setId(SequenceUUID.getPureUUID());
													aRefPo.setAlId(albumPo.getId());
													aRefPo.setAuId(audioPo.getId());
													aRefPo.setColumnNum(aus.size()-i);
													aRefPo.setIsMain(1);
//												    else aRefPo.setIsMain(0);
													albumAudioRefService.insertAlbumAudioRef(aRefPo);
												} catch (Exception e) {}
												
												List<CPersonPo> cPersonPos = cPersonService.getCPersons(null, albumPo.getId(), "c_Album");
												if (cPersonPos!=null && cPersonPos.size()>0) {
													for (CPersonPo cPo : cPersonPos) {
														CPersonRefPo cPersonrefPo = new CPersonRefPo();
														cPersonrefPo.setId(SequenceUUID.getPureUUID());
														cPersonrefPo.setRefName("主播-节目");
														cPersonrefPo.setPersonId(cPo.getId());
														cPersonrefPo.setResTableName("c_Audio");
														cPersonrefPo.setResId(audioPo.getId());
														try {
															cPersonService.insertPersonRef(cPersonrefPo);
														} catch (Exception e) {}
													}
												}
												
												//播放次数入库
												try {
													CPlayCountPo cPlayCountPo = new CPlayCountPo();
													cPlayCountPo.setId(SequenceUUID.getPureUUID());
													cPlayCountPo.setPublisher("蜻蜓");
													cPlayCountPo.setResTableName("c_Audio");
													cPlayCountPo.setResId(audioPo.getId());
													cPlayCountPo.setPlayCount(Long.valueOf(playcount));
													cPlayCountService.insertCPlayCount(cPlayCountPo);
												} catch (Exception e) {}
											} else {
												try {
													AlbumAudioRefPo _aAlbumAudioRefPo = albumAudioRefService.getAlbumAudioRefBy(albumPo.getId(), id);
													if (_aAlbumAudioRefPo==null) {
														AlbumAudioRefPo aRefPo = new AlbumAudioRefPo();
														aRefPo.setId(SequenceUUID.getPureUUID());
														aRefPo.setAlId(albumPo.getId());
														aRefPo.setAuId(id);
														aRefPo.setColumnNum(aus.size()-i);
														aRefPo.setIsMain(1);
														albumAudioRefService.insertAlbumAudioRef(aRefPo);
													} else {
														if (_aAlbumAudioRefPo.getColumnNum()!=(aus.size()-i)) {
															_aAlbumAudioRefPo.setColumnNum(aus.size()-i);
															albumAudioRefService.updateAlbumAudioRef(_aAlbumAudioRefPo);
														}
													}
												} catch (Exception e) {}
											}
										}
									}
									ResOrgAssetPo rOrgAssetPo = resAssService.getResOrgAssetPo(albumPo.getId(), albumPo.getAlbumPublisher(), "wt_SeqMediaAsset");
									if (rOrgAssetPo==null) return;
									// 已入库的重复专辑处理
									List<SameDBPo> sDbPos = sameDBService.getSameDBs(null, "wt_SeqMediaAsset", rOrgAssetPo.getResId());
									if (sDbPos != null && sDbPos.size()>0) {
										for (SameDBPo sameDBPo : sDbPos) {
											ConvertUtils.makeSameAlbumToAddAudio(albumPo, sameDBPo.getResId());
										}
									} else ConvertUtils.convert2SeqMediaToAddMedia(albumPo);
								}
							}
						}
					});
				}
				fixedThreadPool.shutdown();
				while (true) {
					try {
						Thread.sleep(3000);
						System.out.println(numLs.size());
					} catch (Exception e) {}
					if (fixedThreadPool.isTerminated()) {
						break;
					}
				}
				System.out.println(System.currentTimeMillis()-beg);
			}
			try {Thread.sleep(20*60*60*1000);} catch (Exception e) {};
		}
	}
	
	@SuppressWarnings("unchecked")
	public Map<String, Object> getZJData(String albumId, String auSizeStr) {
		try {
			Document doc = Jsoup.connect("http://api2.qingting.fm/v6/media/channelondemands/"+albumId).ignoreContentType(true).timeout(100000).get();
			String albuminfo = doc.body().html();
			Map<String, Object> albummap = (Map<String, Object>) JsonUtils.jsonToObj(albuminfo, Map.class);
			albummap = (Map<String, Object>) albummap.get("data");
			Map<String, Object> usermap = (Map<String, Object>) albummap.get("detail");
			int size = 1000;
			try {size = Integer.valueOf(auSizeStr);} catch (Exception e) {}
			int num = size/10+1;
			List<Map<String, Object>> audios = new ArrayList<>();
			for (int i = 1; i <= num; i++) {
				String requrl = "http://api2.qingting.fm/v6/media/channelondemands/"+albumId+"/programs/order/0/curpage/"+i+"/pagesize/10";
				try {
					doc = Jsoup.connect(requrl).ignoreContentType(true).timeout(50000).get();
				} catch (Exception e) {
					System.out.println(requrl);
				}
				String mediaInfo = doc.body().html();
				Map<String, Object> audiomap = (Map<String, Object>) JsonUtils.jsonToObj(mediaInfo, Map.class);
				List<Map<String, Object>> audiols = (List<Map<String, Object>>) audiomap.get("data");
				if (audiols!=null && audiols.size()>0) {
					audios.removeAll(audiols);
					audios.addAll(audiols);
				}
			}
			Map<String, Object> map = new HashMap<>();
			map.put("usermap", usermap);
			map.put("audios", audios);
			return map;
		} catch (Exception e) {}
		return null;
	}

	private static void loadService() {
		albumService = (AlbumService) SpringShell.getBean("albumService");
		audioService = (AudioService) SpringShell.getBean("audioService");
		albumAudioRefService = (AlbumAudioRefService) SpringShell.getBean("albumAudioRefService");
		cPersonService = (CPersonService) SpringShell.getBean("CPersonService");
		cPlayCountService = (CPlayCountService) SpringShell.getBean("CPlayCountService");
		crawlerDictService = (CrawlerDictService) SpringShell.getBean("crawlerDictService");
		resAssService = (ResOrgAssetService) SpringShell.getBean("resOrgAssetService");
		sameDBService = (SameDBService) SpringShell.getBean("sameDBService");
	}
}
