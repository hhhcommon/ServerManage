package com.woting.crawler.scheme.crawlerdb.xmly;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.zookeeper.server.quorum.ObserverZooKeeperServer;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.spiritdata.framework.util.JsonUtils;
import com.spiritdata.framework.util.SequenceUUID;
import com.woting.cm.core.ResOrgAsset.service.ResOrgAssetService;
import com.woting.cm.core.channel.service.ChannelService;
import com.woting.cm.core.dict.persis.po.DictRefResPo;
import com.woting.cm.core.dict.service.DictService;
import com.woting.cm.core.keyword.service.KeyWordService;
import com.woting.cm.core.media.persis.po.SeqMediaAssetPo;
import com.woting.cm.core.media.service.MediaService;
import com.woting.cm.core.person.service.PersonService;
import com.woting.crawler.core.album.persis.po.AlbumPo;
import com.woting.crawler.core.album.service.AlbumService;
import com.woting.crawler.core.albumaudioref.persis.po.AlbumAudioRefPo;
import com.woting.crawler.core.albumaudioref.service.AlbumAudioRefService;
import com.woting.crawler.core.audio.persis.po.AudioPo;
import com.woting.crawler.core.audio.service.AudioService;
import com.woting.crawler.core.ccomment.persis.po.CCommentPo;
import com.woting.crawler.core.ccomment.service.CCommentService;
import com.woting.crawler.core.cfavorite.persis.po.CFavoritePo;
import com.woting.crawler.core.cfavorite.service.CFavoriteService;
import com.woting.crawler.core.cforward.persis.po.CForwardPo;
import com.woting.crawler.core.cforward.service.CForwardService;
import com.woting.crawler.core.cperson.persis.po.CPersonPo;
import com.woting.crawler.core.cperson.persis.po.CPersonRefPo;
import com.woting.crawler.core.cperson.service.CPersonService;
import com.woting.crawler.core.cplaycount.persis.po.CPlayCountPo;
import com.woting.crawler.core.cplaycount.service.CPlayCountService;
import com.woting.crawler.core.dict.persis.po.DictRefPo;
import com.woting.crawler.core.dict.service.CrawlerDictService;
import com.woting.crawler.core.httpclient.service.HttpClientService;
import com.woting.crawler.core.solr.SolrUpdateThread;
import com.woting.crawler.ext.SpringShell;
import com.woting.crawler.scheme.crawlerdb.crawler.AddCacheDBInfoThread;
import com.woting.crawler.scheme.utils.CleanDataUtils;
import com.woting.crawler.scheme.utils.ConvertUtils;
import com.woting.crawler.scheme.utils.HttpUtils;

public class UpdateXMLYSrc {
	private static AlbumService albumService;
	private static AudioService audioService;
	private static AlbumAudioRefService albumAudioRefService;
	private static CPersonService cPersonService;
	private static CPlayCountService cPlayCountService;
	private static CrawlerDictService crawlerDictService;
	private static MediaService mediaService;
	private static PersonService personService;
	private static KeyWordService keyWordService;
	private static ChannelService channelService;
	private static DictService dictService;
	private static ResOrgAssetService resAssService;
	private static HttpClientService httpClientService;

	public void updateSrc() {
		loadService();
		while (true) {
			try {Thread.sleep(4*60*60*1000);} catch (Exception e) {}
			long beg = System.currentTimeMillis();
			Map<String, Object> m = new HashMap<>();
			m.put("albumPublisher", "喜马拉雅");
			m.put("isValidate", 3);
			int num = 0;
			List<String> numLs = new ArrayList<>();
			List<AlbumPo> albumPos = albumService.getAlbumListBy(m);
			if (albumPos!=null && albumPos.size()>0) {
				XMLYCrawler xCrawler = new XMLYCrawler(false);
				ExecutorService fixedThreadPool = Executors.newFixedThreadPool(20);
				for (AlbumPo albumPo : albumPos) {
					fixedThreadPool.execute(new Runnable() {
						public void run() {
							String auSizeStr = xCrawler.insertNewZJ(albumPo.getAlbumId(), "1", false);
							int auSize = 0;
							try {auSize = Integer.valueOf(auSizeStr);} catch (Exception e) {}
							int _auSize = albumAudioRefService.getAlbumAudioRefNum(albumPo.getId());
							numLs.add(albumPo.getId());
							if (auSize > _auSize) {
								System.out.println(albumPo.getId()+ "    " + albumPo.getVisitUrl());
								List<AlbumAudioRefPo> audioRefPos = albumAudioRefService.getAlbumAudioRefs(albumPo.getId());
								if (audioRefPos!=null && audioRefPos.size()>0) {
									Map<String, Object> alMap = null;
									try {
										alMap = getZJData(albumPo.getAlbumId(), auSizeStr);
									} catch (Exception e) {}
									if (alMap!=null && alMap.size()>0) {
										alMap = (Map<String, Object>) alMap.get("data");
										Map<String, Object> usermap = (Map<String, Object>) alMap.get("user");
										Map<String, Object> tracks = (Map<String, Object>) alMap.get("tracks");
										String userId = usermap.get("uid").toString();
										List<Map<String, Object>> ls = null;
										try {
											ls = (List<Map<String, Object>>) tracks.get("list");
										} catch (Exception e) {}
										if (ls!=null && ls.size()>0) {
											for (int i = 0; i < ls.size(); i++) {
												Map<String, Object> audiomap = ls.get(i);
												String audioId = audiomap.get("trackId")+"";
												if (audioId.equals("null")) continue;
												String id = "XMLY_AUDIO_"+audioId;
												if (audioService.getAudioInfo(id)==null) {
													AudioPo audioPo = new AudioPo();
													audioPo.setAudioId(audiomap.get("trackId")+"");
													audioPo.setAudioName(audiomap.get("title")+"");
													audioPo.setAudioImg(audiomap.get("coverLarge")+"");
													audioPo.setAudioPublisher("喜马拉雅");
													audioPo.setAudioURL(audiomap.get("playUrl64")+"");
													audioPo.setDuration(audiomap.get("duration")+"000"); //接口得到播放时长单位为秒
													audioPo.setVisitUrl("http://www.ximalaya.com/"+userId+"/sound/"+audioPo.getAudioId());
													audioPo.setId("XMLY_AUDIO_"+audioPo.getAudioId());
													try {
														Document doc = null;
														try {
															doc = HttpUtils.makeXMLYJsoup(audioPo.getVisitUrl());
															Elements eles = doc.select("div[class=tagBtnList]");
															if (eles!=null && eles.size()>0) {
																Element ele = eles.get(0);
																eles = ele.select("span");
																if (eles!=null && eles.size()>0) {
																	String tags = "";
																	for (Element element : eles) {
																		tags += ","+element.html();
																	}
																	audioPo.setAudioTags(tags.substring(1));
																}
															}
															eles = doc.select("article");
															if (eles!=null && eles.size()>0) {
																Element ele = eles.get(eles.size()-1);
																if (ele!=null) {
																	String descn = ele.html();
																	if (descn.length()>0) {
																		descn = descn.replace("\"", "'");
																		audioPo.setDescn(descn);
																	}
																}
															}
														} catch (Exception e) {}
													    try {audioPo.setPubTime(new Timestamp(Long.valueOf(audiomap.get("createdAt")+"")));} catch (Exception e) {}
													    audioPo.setcTime(new Timestamp(System.currentTimeMillis()));
													    try {
															audioService.insertAudio(audioPo);
														} catch (Exception e) {}
													    
													    try {
													    	AlbumAudioRefPo _aAlbumAudioRefPo = albumAudioRefService.getAlbumAudioRefBy(albumPo.getId(), audioPo.getId());
													    	if (_aAlbumAudioRefPo==null) {
																AlbumAudioRefPo aRefPo = new AlbumAudioRefPo();
																aRefPo.setId(SequenceUUID.getPureUUID());
																aRefPo.setAlId(albumPo.getId());
																aRefPo.setAuId(audioPo.getId());
																aRefPo.setColumnNum(ls.size()-i);
																if (!audiomap.containsKey("refUid")) {
																	aRefPo.setIsMain(1);
															    } else aRefPo.setIsMain(0);
																albumAudioRefService.insertAlbumAudioRef(aRefPo);
															} else {
																if (_aAlbumAudioRefPo.getColumnNum()!=(ls.size()-i)) {
																	_aAlbumAudioRefPo.setColumnNum(ls.size()-i);
																	albumAudioRefService.updateAlbumAudioRef(_aAlbumAudioRefPo);
																}
															}
														} catch (Exception e) {}
													    
														//加载转发数
														if (doc!=null) {
															try {
																String forwards = doc.select("a[class=forwardBtn link1]").get(0).select("span").get(0).html();
																CForwardPo cForwardPo = new CForwardPo();
																cForwardPo.setId(SequenceUUID.getPureUUID());
																cForwardPo.setPublisher("喜马拉雅");
																cForwardPo.setResTableName("c_Audio");
																cForwardPo.setResId(audioPo.getId());
																cForwardPo.setForwardCount(Long.valueOf(forwards));
																CForwardService cForwardService = (CForwardService) SpringShell.getBean("CForwardService");
																cForwardService.insertCForward(cForwardPo);
															} catch (Exception e) {}
														}
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
													
													//节目喜欢数入库
													try {
														CFavoritePo cFavoritePo = new CFavoritePo();
														cFavoritePo.setId(SequenceUUID.getPureUUID());
														cFavoritePo.setPublisher("喜马拉雅");
														cFavoritePo.setResTableName("c_Audio");
														cFavoritePo.setResId(audioPo.getId());
														cFavoritePo.setFavoriteCount(Long.valueOf(audiomap.get("likes").toString()));
														CFavoriteService cFavoriteService = (CFavoriteService) SpringShell.getBean("CFavoriteService");
														cFavoriteService.insertCFavorite(cFavoritePo);
													} catch (Exception e) {}
													
													//播放次数入库
													try {
														CPlayCountPo cPlayCountPo = new CPlayCountPo();
														cPlayCountPo.setId(SequenceUUID.getPureUUID());
														cPlayCountPo.setPublisher("喜马拉雅");
														cPlayCountPo.setResTableName("c_Audio");
														cPlayCountPo.setResId(audioPo.getId());
														cPlayCountPo.setPlayCount(Long.valueOf(audiomap.get("playtimes")+""));
														cPlayCountService.insertCPlayCount(cPlayCountPo);
													} catch (Exception e) {}
													
													try {
														CCommentPo cCommentPo = new CCommentPo();
														cCommentPo.setId(SequenceUUID.getPureUUID());
														cCommentPo.setPublisher("喜马拉雅");
														cCommentPo.setResTableName("c_Audio");
														cCommentPo.setResId(audioPo.getId());
														cCommentPo.setCommentCount(Long.valueOf(audiomap.get("comments").toString()));
														CCommentService commentService = (CCommentService) SpringShell.getBean("CCommentService");
														commentService.insertCComment(cCommentPo);
													} catch (Exception e) {}
												} else {
													try {
														AlbumAudioRefPo _aAlbumAudioRefPo = albumAudioRefService.getAlbumAudioRefBy(albumPo.getId(), id);
														if (_aAlbumAudioRefPo==null) {
															AlbumAudioRefPo aRefPo = new AlbumAudioRefPo();
															aRefPo.setId(SequenceUUID.getPureUUID());
															aRefPo.setAlId(albumPo.getId());
															aRefPo.setAuId(id);
															aRefPo.setColumnNum(ls.size()-i);
															if (!audiomap.containsKey("refUid")) aRefPo.setIsMain(1);
															else aRefPo.setIsMain(0);
															albumAudioRefService.insertAlbumAudioRef(aRefPo);
														} else {
															if (_aAlbumAudioRefPo.getColumnNum()!=(ls.size()-i)) {
																_aAlbumAudioRefPo.setColumnNum(ls.size()-i);
																albumAudioRefService.updateAlbumAudioRef(_aAlbumAudioRefPo);
															}
														}
													} catch (Exception e) {}
												}
											}
										}
									}
								}
								Map<String, Object> map = ConvertUtils.convert2SeqMediaToAddMedia(albumPo);
								if (map!=null && map.size()>0) {
									SeqMediaAssetPo seq = (SeqMediaAssetPo) map.get("sma");
									new AddCacheDBInfoThread(seq.getId()).start();
									new SolrUpdateThread(seq).start();
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
			try {Thread.sleep(20*60*60*1000);} catch (Exception e) {}
		}
	}
	
	private static void loadService() {
		albumService = (AlbumService) SpringShell.getBean("albumService");
		audioService = (AudioService) SpringShell.getBean("audioService");
		albumAudioRefService = (AlbumAudioRefService) SpringShell.getBean("albumAudioRefService");
		cPersonService = (CPersonService) SpringShell.getBean("CPersonService");
		cPlayCountService = (CPlayCountService) SpringShell.getBean("CPlayCountService");
		crawlerDictService = (CrawlerDictService) SpringShell.getBean("crawlerDictService");
		mediaService = (MediaService) SpringShell.getBean("mediaService");
		personService = (PersonService) SpringShell.getBean("personService");
		channelService = (ChannelService) SpringShell.getBean("channelService");
		keyWordService = (KeyWordService) SpringShell.getBean("keyWordService");
		dictService = (DictService) SpringShell.getBean("dictService");
		resAssService = (ResOrgAssetService) SpringShell.getBean("resOrgAssetService");
		httpClientService = (HttpClientService) SpringShell.getBean("httpClientService");
	}
	
	@SuppressWarnings("unchecked")
	protected String getZJAudioSzie(String albumId) {
		try {
			Document doc = null;
			Map<String, Object> alm = null;
			try {
				doc = HttpUtils.makeXMLYJsoup("http://mobile.ximalaya.com/mobile/v1/album?albumId="+albumId+"&device=android&isAsc=true&pageId=1&pageSize=1&pre_page=0&source=5");
			    String albumstr = doc.body().html();
			    albumstr = CleanDataUtils.CleanDescnStr(albumstr, "\"intro\":\"", "\",\"shortIntro\":\"", "\",\"introRich\":\"", "\",\"shortIntroRich\":\"", "\",\"tags\":\"", "\",\"tracks\":");
			    alm = (Map<String, Object>) JsonUtils.jsonToObj(albumstr, Map.class);
			} catch (Exception e) {
				String albumstr = httpClientService.doGet("http://mobile.ximalaya.com/mobile/v1/album?albumId="+albumId+"&device=android&isAsc=true&pageId=1&pageSize=1");
				albumstr = CleanDataUtils.CleanDescnStr(albumstr, "\"intro\":\"", "\",\"shortIntro\":\"", "\",\"introRich\":\"", "\",\"shortIntroRich\":\"", "\",\"tags\":\"", "\",\"tracks\":");
				alm = (Map<String, Object>) JsonUtils.jsonToObj(albumstr, Map.class);
			}
			if (alm!=null) {
				Map<String, Object> almm = (Map<String, Object>) alm.get("data");
				Map<String, Object> tracks = (Map<String, Object>) almm.get("tracks");
				return tracks.get("totalCount").toString();
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("出错专辑Id    "+albumId);
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	protected Map<String, Object> getZJData(String albumId, String pageSize) {
		try {
			Document doc = null;
			Map<String, Object> alm = null;
			try {
				doc = HttpUtils.makeXMLYJsoup("http://mobile.ximalaya.com/mobile/v1/album?albumId="+albumId+"&device=android&isAsc=true&pageId=1&pageSize="+pageSize+"&pre_page=0&source=5");
			    String albumstr = doc.body().html();
			    albumstr = CleanDataUtils.CleanDescnStr(albumstr, "\"intro\":\"", "\",\"shortIntro\":\"", "\",\"introRich\":\"", "\",\"shortIntroRich\":\"", "\",\"tags\":\"", "\",\"tracks\":");
			    alm = (Map<String, Object>) JsonUtils.jsonToObj(albumstr, Map.class);
			} catch (Exception e) {
				String albumstr = httpClientService.doGet("http://mobile.ximalaya.com/mobile/v1/album?albumId="+albumId+"&device=android&isAsc=true&pageId=1&pageSize="+pageSize);
				albumstr = CleanDataUtils.CleanDescnStr(albumstr, "\"intro\":\"", "\",\"shortIntro\":\"", "\",\"introRich\":\"", "\",\"shortIntroRich\":\"", "\",\"tags\":\"", "\",\"tracks\":");
				alm = (Map<String, Object>) JsonUtils.jsonToObj(albumstr, Map.class);
			}
			if (alm!=null) {
				Map<String, Object> almm = (Map<String, Object>) alm.get("data");
				Map<String, Object> tracks = (Map<String, Object>) almm.get("tracks");
				List<Map<String, Object>> aus = (List<Map<String, Object>>) tracks.get("list");
				int num = 0;
				try {num = Integer.valueOf(pageSize);} catch (Exception e) {}
				if (num > 1000) {
					int page = num/1000+1;
					for (int i = 2; i <= page; i++) {
						Map<String, Object> _alm = new HashMap<>();
						try {
							doc = HttpUtils.makeXMLYJsoup("http://mobile.ximalaya.com/mobile/v1/album/track?albumId="+albumId+"&device=android&isAsc=true&pageId="+i+"&pageSize=1000&pre_page=0");
						    String albumstr = doc.body().html();
						    _alm = (Map<String, Object>) JsonUtils.jsonToObj(albumstr, Map.class);
						} catch (Exception e) {
							String albumstr = httpClientService.doGet("http://mobile.ximalaya.com/mobile/v1/album/track?albumId="+albumId+"&device=android&isAsc=true&pageId="+i+"&pageSize=1000");
							_alm = (Map<String, Object>) JsonUtils.jsonToObj(albumstr, Map.class);
						}
						if (_alm!=null) {
							_alm = (Map<String, Object>) _alm.get("data");
							if (_alm!=null && _alm.size()>0) {
								List<Map<String, Object>> _aus = (List<Map<String, Object>>) _alm.get("list");
								if (_aus!=null && _aus.size()>0) {
									aus.addAll(_aus);
								}
							}
						}
					}
				}
				return alm;
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("出错专辑Id    "+albumId);
		}
		return null;
	}
}
