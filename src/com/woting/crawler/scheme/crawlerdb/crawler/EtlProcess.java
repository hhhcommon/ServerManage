package com.woting.crawler.scheme.crawlerdb.crawler;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.spiritdata.framework.util.JsonUtils;
import com.spiritdata.framework.util.SequenceUUID;
import com.woting.cm.core.ResOrgAsset.persis.po.ResOrgAssetPo;
import com.woting.cm.core.ResOrgAsset.service.ResOrgAssetService;
import com.woting.cm.core.channel.persis.po.ChannelAssetPo;
import com.woting.cm.core.channel.persis.po.ChannelMapRefPo;
import com.woting.cm.core.channel.persis.po.ChannelPo;
import com.woting.cm.core.channel.service.ChannelService;
import com.woting.cm.core.dict.persis.po.DictRefResPo;
import com.woting.cm.core.dict.service.DictService;
import com.woting.cm.core.keyword.service.KeyWordService;
import com.woting.cm.core.media.persis.po.MaSourcePo;
import com.woting.cm.core.media.persis.po.MediaAssetPo;
import com.woting.cm.core.media.persis.po.MediaPlayCountPo;
import com.woting.cm.core.media.persis.po.SeqMaRefPo;
import com.woting.cm.core.media.persis.po.SeqMediaAssetPo;
import com.woting.cm.core.media.service.MediaService;
import com.woting.cm.core.perimeter.persis.po.OrganizePo;
import com.woting.cm.core.perimeter.service.OrganizeService;
import com.woting.cm.core.person.persis.po.PersonPo;
import com.woting.cm.core.person.persis.po.PersonRefPo;
import com.woting.cm.core.person.service.PersonService;
import com.woting.crawler.compare.CompareAttribute;
import com.woting.crawler.core.album.persis.po.AlbumPo;
import com.woting.crawler.core.album.service.AlbumService;
import com.woting.crawler.core.albumaudioref.persis.po.AlbumAudioRefPo;
import com.woting.crawler.core.albumaudioref.service.AlbumAudioRefService;
import com.woting.crawler.core.audio.persis.po.AudioPo;
import com.woting.crawler.core.audio.service.AudioService;
import com.woting.crawler.core.dict.persis.po.DictRefPo;
import com.woting.crawler.core.dict.service.CrawlerDictService;
import com.woting.crawler.core.samedb.persis.po.SameDBPo;
import com.woting.crawler.core.samedb.service.SameDBService;
import com.woting.crawler.core.solr.SolrUpdateThread;
import com.woting.crawler.ext.SpringShell;
import com.woting.crawler.scheme.utils.ConvertUtils;

public class EtlProcess {
	private Logger logger = LoggerFactory.getLogger(EtlProcess.class);
	private AlbumService albumService;
	private ChannelService channelService;
	private MediaService mediaService;
	private ResOrgAssetService resAssService;
	private DictService dictService;
	private PersonService personService;
	private OrganizeService organizeService;
	private List<ChannelPo> chlist;
	private List<ChannelMapRefPo> chaMapRefs;
	Map<String, Object> chmap = new HashMap<>();

	public EtlProcess() {
		albumService = (AlbumService) SpringShell.getBean("albumService");
		channelService = (ChannelService) SpringShell.getBean("channelService");
		mediaService = (MediaService) SpringShell.getBean("mediaService");
		resAssService = (ResOrgAssetService) SpringShell.getBean("resOrgAssetService");
		dictService = (DictService) SpringShell.getBean("dictService");
		personService = (PersonService) SpringShell.getBean("personService");
		chaMapRefs = channelService.getChannelMapRefList(null, null, 1);
		chlist = channelService.getChannelList();
		if (chlist != null && chlist.size() > 0) {
			for (ChannelPo chPo : chlist) {
				chmap.put(chPo.getId(), chPo.getChannelName());
			}
		}
		organizeService = (OrganizeService) SpringShell.getBean("organizeService");
	}

	public void convertToWT() {
		new Thread(new Runnable() {
			public void run() {
				while (true) {
					try {
						logger.info("开始中间库转正式库进程");
						List<AlbumPo> aPos = null;
						Map<String, Object> m = new HashMap<>();
						m.put("isValidate", 2);
						// m.put("albumPublisher", "蜻蜓");
						m.put("sortByClause", " cTime DESC");
						m.put("pageByClause", "0,5000");
						aPos = albumService.getAlbumListBy(m);
						ExecutorService fixedThreadPool = Executors.newFixedThreadPool(2);
						if (aPos != null && aPos.size() > 0) {
							for (AlbumPo albumPo : aPos) {
								fixedThreadPool.execute(new Runnable() {
									public void run() {
										removeDB(albumPo.getId());
										albumPo.setIsValidate(1);
										albumService.updateAlbum(albumPo);
									}
								});
							}
						}
						fixedThreadPool.shutdown();
						while (true) {
							Thread.sleep(1000);
							System.out.println("等待清除上次未完成数据");
							if (fixedThreadPool.isTerminated()) {
								break;
							}
						}
						fixedThreadPool = Executors.newFixedThreadPool(5);
						m.clear();
						m.put("isValidate", 1);
//						m.put("albumPublisher", "蜻蜓");
						m.put("sortByClause", " cTime DESC");
						m.put("pageByClause", "0,5000");
						aPos = albumService.getAlbumListBy(m);
						if (aPos != null && aPos.size() > 0) {
							CompareAttribute cAttribute = new CompareAttribute();
							AudioService audioService = (AudioService) SpringShell.getBean("audioService");
							for (AlbumPo albumPo : aPos) {
								fixedThreadPool.execute(new Runnable() {
									@SuppressWarnings("unchecked")
									public void run() {
										System.out.println((new Timestamp(System.currentTimeMillis())).toString() + "      " + albumPo.getId());
										Map<String, Object> map = null; // 是否有相似专辑
										if (!albumPo.getAlbumPublisher().equals("喜马拉雅")) {
											List<AudioPo> audioPos = audioService.getAudioListByAlbumId(albumPo.getId(),albumPo.getAlbumPublisher());
											if (audioPos != null && audioPos.size() > 0) {
												albumPo.setAudioPos(audioPos);
												try {
													map = cAttribute.getSolrListToCompare(albumPo, false);
												} catch (Exception e) {}
											}
										}
										if (map!=null && map.size()>0) {
											AlbumPo _albumPo = (AlbumPo) map.get("albumPo");
											String smaId = map.get("smaId").toString();
											List<Map<String, Object>> simls = (List<Map<String, Object>>) map.get("simls");
											makeSameAlbum(_albumPo, smaId, simls);
										} else makeNewAlbum(albumPo.getId());
									}
								});
							}
						}
						fixedThreadPool.shutdown();
						while (true) {
							Thread.sleep(10000);
							if (fixedThreadPool.isTerminated()) {
								break;
							}
						}
						logger.info("中间库转正式库完成");
						Thread.sleep(20 * 60 * 1000);
					} catch (Exception e) {
						e.printStackTrace();
						continue;
					}
				}
			}
		}).start();
	}

	/**
	 * id:* id_CTIME:* redis对于处理内容进程加标记 1 正在入库 2入库完成 3中间库内容不存在
	 * 
	 * @param id
	 */
	public void makeNewAlbum(String id) {
		AlbumPo al = albumService.getAlbumInfo(id);
		if (al != null) {
			Map<String, Object> map = null;
			try {
				map = ConvertUtils.convert2SeqMedia(al, chaMapRefs, chmap);
			} catch (Exception e) {
				al.setIsValidate(2);
				albumService.updateAlbum(al);
				e.printStackTrace();
			}
			try {
				if (map != null && map.size() > 0) {
					SeqMediaAssetPo seq = (SeqMediaAssetPo) map.get("seq");
					new AddCacheDBInfoThread(seq.getId()).start();
					new SolrUpdateThread(seq).start();
				}
			} catch (Exception e) {
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void makeSameAlbum(AlbumPo albumPo, String smaId, List<Map<String, Object>> ls) {
		Map<String, Object> mas = null;
		mas = ConvertUtils.convert2SeqMediaToSame(albumPo);
		if (mas == null) return;
		SeqMediaAssetPo sma = mediaService.getSeqInfo(smaId);
		if (sma != null && ls != null && ls.size() > 0) {
			SameDBService sameDBService = (SameDBService) SpringShell.getBean("sameDBService");
			List<OrganizePo> oganlist = organizeService.getOrganizeList();
			String sameSmaId = mas.get("smaId").toString();
			List<PersonPo> persons = (List<PersonPo>) mas.get("persons");
			Map<String, Object> auid_maid = (Map<String, Object>) mas.get("auid_maid");
			SameDBPo sameDBPo = new SameDBPo();
			sameDBPo.setId(SequenceUUID.getPureUUID());
			sameDBPo.setResTableName("wt_SeqMediaAsset");
			sameDBPo.setResId(smaId);
			sameDBPo.setSameId(sameSmaId);
			sameDBPo.setIsValidate(0);
			sameDBService.insert(sameDBPo);
			if (persons != null && persons.size() > 0) {
				for (PersonPo personPo : persons) {
					PersonRefPo personRefPo = new PersonRefPo();
					personRefPo.setId(SequenceUUID.getPureUUID());
					personRefPo.setPersonId(personPo.getId());
					personRefPo.setRefName("主播-专辑");
					personRefPo.setResTableName("wt_SeqMediaAsset");
					personRefPo.setResId(smaId);
					personRefPo.setcTime(new Timestamp(System.currentTimeMillis()));
					personService.insertPersonRef(personRefPo);
				}
			}

			Map<String, Object> exmaIdMap = new HashMap<>();
			for (Map<String, Object> m : ls) {
				String maId = m.get("perId").toString();
				String id = m.get("audioId").toString();
				String samemaId = auid_maid.get(id).toString();
				exmaIdMap.put(samemaId, null);
				SameDBPo sameDBma = new SameDBPo();
				sameDBma.setId(SequenceUUID.getPureUUID());
				sameDBma.setResTableName("wt_MediaAsset");
				sameDBma.setResId(maId);
				sameDBma.setSameId(samemaId);
				sameDBma.setIsValidate(0);
				sameDBService.insert(sameDBma);
				List<MaSourcePo> masls = mediaService.getMaSources(samemaId);
				if (masls != null && masls.size() > 0) {
					for (MaSourcePo maSourcePo : masls) {
						MaSourcePo masPo = new MaSourcePo();
						masPo.setId(SequenceUUID.getPureUUID());
						masPo.setMaId(maId);
						masPo.setMaSrcType(1);
						for (OrganizePo organs : oganlist) {
							if (organs.getOrgName().equals(albumPo.getAlbumPublisher())) {
								masPo.setMaSource(organs.getOrgName());
								masPo.setMaSrcId(organs.getId());
								break;
							}
						}
						masPo.setIsMain(0);
						masPo.setPlayURI(maSourcePo.getPlayURI());
						masPo.setDescn(maSourcePo.getDescn());
						mediaService.insertMas(masPo);

						if (persons != null && persons.size() > 0) {
							for (PersonPo personPo : persons) {
								PersonRefPo personRefPo = new PersonRefPo();
								personRefPo.setId(SequenceUUID.getPureUUID());
								personRefPo.setPersonId(personPo.getId());
								personRefPo.setRefName("主播-节目");
								personRefPo.setResTableName("wt_MediaAsset");
								personRefPo.setResId(maId);
								personRefPo.setcTime(new Timestamp(System.currentTimeMillis()));
								personService.insertPersonRef(personRefPo);
							}
						}
						sameDBma.setIsValidate(1);
						sameDBService.update(sameDBma);
					}
				}
			}
			int columnNum = 0;
			KeyWordService keyWordService = (KeyWordService) SpringShell.getBean("keyWordService");
			CrawlerDictService crawlerDictService = (CrawlerDictService) SpringShell.getBean("crawlerDictService");
			List<DictRefPo> cdRefPos = crawlerDictService.getDictRefs(albumPo.getId(), "c_Album", null);
			String chaNameStr = "";
			Map<String, Object> chaMapRefIdMap = new HashMap<>();
			if (cdRefPos != null && cdRefPos.size() > 0 && chaMapRefs != null && chaMapRefs.size() > 0) {
				for (DictRefPo dictRefPo : cdRefPos) {
					for (ChannelMapRefPo chaRef : chaMapRefs) {
						if (chaRef.getSrcDid().equals(dictRefPo.getCdictDid())) {
							if (!chaNameStr.contains(chaRef.getChannelId())) {
								chaNameStr += "," + chaRef.getChannelId();
								chaMapRefIdMap.put(chaRef.getChannelId(), "wt_ChannelMapRef_" + chaRef.getId());
							}
						}
					}
				}
			}

			SeqMaRefPo smaref = mediaService.getOneSmarefOrderByColumnNum(smaId);
			if (smaref != null)
				columnNum = smaref.getColumnNum();
			List<MediaAssetPo> maPos = mediaService.getMasByAlbumId(albumPo.getId());
			if (maPos != null && maPos.size() > 0) {
				for (MediaAssetPo mediaAssetPo : maPos) {
					if (exmaIdMap.containsKey(mediaAssetPo.getId())) continue;
					columnNum++;
					SeqMaRefPo seqMaRefPo = new SeqMaRefPo();
					seqMaRefPo.setId(SequenceUUID.getPureUUID());
					seqMaRefPo.setsId(smaId);
					seqMaRefPo.setmId(mediaAssetPo.getId());
					seqMaRefPo.setDescn(mediaAssetPo.getDescn());
					seqMaRefPo.setIsMain(0);
					seqMaRefPo.setColumnNum(columnNum);
					seqMaRefPo.setcTime(new Timestamp(System.currentTimeMillis()));
					mediaService.insertSeqRef(seqMaRefPo);

					if (persons != null && persons.size() > 0) {
						for (PersonPo personPo : persons) {
							PersonRefPo personRefPo = new PersonRefPo();
							personRefPo.setId(SequenceUUID.getPureUUID());
							personRefPo.setPersonId(personPo.getId());
							personRefPo.setRefName("主播-节目");
							personRefPo.setResTableName("wt_MediaAsset");
							personRefPo.setResId(mediaAssetPo.getId());
							personRefPo.setcTime(new Timestamp(System.currentTimeMillis()));
							personService.insertPersonRef(personRefPo);
						}
					}

					if (chaNameStr.length() > 0) {
						if (chaNameStr.lastIndexOf(",") == 0)
							chaNameStr = chaNameStr.substring(1);
						String[] chasStr = chaNameStr.split(",");
						if (chasStr.length > 0) {
							for (String chaStr : chasStr) {
								DictRefResPo dictRefRes = new DictRefResPo();
								dictRefRes.setId(SequenceUUID.getPureUUID());
								dictRefRes.setRefName("专辑-内容分类");
								dictRefRes.setResTableName("wt_MediaAsset");
								dictRefRes.setResId(mediaAssetPo.getId());
								dictRefRes.setDictMid("3");
								dictRefRes.setDictDid(chaStr);
								dictRefRes.setCTime(new Timestamp(System.currentTimeMillis()));
								if (dictService.getDictRefs(dictRefRes.getDictDid(), dictRefRes.getResId(),
										dictRefRes.getResTableName()) == null) {
									try {
										dictService.insertDictRef(dictRefRes);
									} catch (Exception e) {
									}
								}

								ChannelAssetPo cha = new ChannelAssetPo();
								cha.setId(SequenceUUID.getPureUUID());
								cha.setAssetType("wt_MediaAsset");
								cha.setAssetId(mediaAssetPo.getId());
								cha.setPublisherId(mediaAssetPo.getMaPubId());
								cha.setIsValidate(1); // 设为无效
								cha.setCheckerId("1");
								cha.setPubName(mediaAssetPo.getMaTitle());
								cha.setPubImg(mediaAssetPo.getMaImg());
								cha.setSort(0);
								cha.setFlowFlag(2);
								try {
									cha.setInRuleIds(chaMapRefIdMap.get(chaStr).toString());
								} catch (Exception e) {
									System.err.println(chaStr);
									System.err.println(JsonUtils.objToJson(chaMapRefIdMap));
									continue;
								}
								cha.setCheckRuleIds("etl");
								cha.setCTime(new Timestamp(System.currentTimeMillis()));
								cha.setPubTime(mediaAssetPo.getMaPublishTime());
								cha.setChannelId(chaStr);
								List<ChannelAssetPo> chass = channelService.getChannelAssetListBy(cha.getChannelId(),
										cha.getAssetId(), cha.getAssetType());
								if (chass != null && chass.size() > 0) {
									for (ChannelAssetPo channelAssetPo : chass) {
										String inRuleIds = channelAssetPo.getInRuleIds();
										inRuleIds += "," + cha.getInRuleIds();
										channelAssetPo.setInRuleIds(inRuleIds);
										channelService.updateChannelAsset(channelAssetPo);
									}
								} else {
									try {
										channelService.insertChannelAsset(cha);
									} catch (Exception e) {
									}
								}
								keyWordService.saveKwAndKeRef(mediaAssetPo.getKeyWords(), "wt_Channel",
										cha.getChannelId()); // 标签与栏目绑定
							}
						}
					} else {
						Map<String, Object> m = new HashMap<>();
						m.put("assetId", smaId);
						m.put("assetType", "wt_SeqMediaAsset");
						m.put("isValidate", 1);
						m.put("flowFalg", 2);
						List<ChannelAssetPo> chas = channelService.getChannelAssetListBy(m);
						if (chas != null && chas.size() > 0) {
							for (ChannelAssetPo channelAssetPo : chas) {
								DictRefResPo dictref = new DictRefResPo();
								dictref.setId(SequenceUUID.getPureUUID());
								dictref.setRefName("专辑-内容分类");
								dictref.setResTableName("wt_MediaAsset");
								dictref.setResId(mediaAssetPo.getId());
								dictref.setDictMid("3");
								dictref.setDictDid(channelAssetPo.getChannelId());
								dictref.setCTime(new Timestamp(System.currentTimeMillis()));
								if (dictService.getDictRefs(dictref.getDictDid(), dictref.getResId(),
										dictref.getResTableName()) == null) {
									try {
										dictService.insertDictRef(dictref);
									} catch (Exception e) {
									}
								}

								ChannelAssetPo cha = new ChannelAssetPo();
								cha.setId(SequenceUUID.getPureUUID());
								cha.setAssetType("wt_MediaAsset");
								cha.setAssetId(mediaAssetPo.getId());
								cha.setPublisherId(mediaAssetPo.getMaPubId());
								cha.setIsValidate(1); // 设为无效
								cha.setCheckerId("1");
								cha.setPubName(mediaAssetPo.getMaTitle());
								cha.setPubImg(mediaAssetPo.getMaImg());
								cha.setSort(0);
								cha.setFlowFlag(2);
								cha.setInRuleIds(channelAssetPo.getInRuleIds());
								cha.setCheckRuleIds("etl");
								cha.setCTime(new Timestamp(System.currentTimeMillis()));
								cha.setPubTime(mediaAssetPo.getMaPublishTime());
								cha.setChannelId(channelAssetPo.getChannelId());
								List<ChannelAssetPo> chass = channelService.getChannelAssetListBy(cha.getChannelId(),
										cha.getAssetId(), cha.getAssetType());
								if (chass != null && chass.size() > 0) {
									for (ChannelAssetPo _channelAssetPo : chass) {
										String inRuleIds = _channelAssetPo.getInRuleIds();
										inRuleIds += "," + cha.getInRuleIds();
										_channelAssetPo.setInRuleIds(inRuleIds);
										channelService.updateChannelAsset(_channelAssetPo);
									}
								} else {
									try {
										channelService.insertChannelAsset(cha);
									} catch (Exception e) {
									}
								}
							}
						}
					}
				}
			}

			sameDBPo.setIsValidate(1);
			sameDBService.update(sameDBPo);
			new AddCacheDBInfoThread(smaId).start();
			new SolrUpdateThread(sma).start();
		}
	}

	public void saveContents(List<MediaAssetPo> malist, List<ResOrgAssetPo> resAss, List<MaSourcePo> maslist,
			List<SeqMaRefPo> seqreflist, List<MediaPlayCountPo> mecounts, List<DictRefResPo> dictreflist,
			List<ChannelAssetPo> chalist, List<PersonRefPo> pfs) {
		// 往资源库插入声音数据
		if (malist != null && malist.size() > 0) {
			mediaService.insertMaList(malist);
		}
		// 往资源库插入资源与外部资源对照
		if (resAss != null && resAss.size() > 0) {
			resAssService.insertResOrgAssetList(resAss);
		}
		if (maslist != null && maslist.size() > 0) {
			mediaService.insertMasList(maslist);
		}
		// 往资源库插入专辑声音关系表数据
		if (seqreflist != null && seqreflist.size() > 0) {
			mediaService.insertSeqRefList(seqreflist);
		}
		// 往资源库插入音频播放次数数据
		if (mecounts != null && mecounts.size() > 0) {
			mediaService.insertMediaPlayCountList(mecounts);
		}
		// 往字典关系表里插入内容分类关系数据
		if (dictreflist != null && dictreflist.size() > 0) {
			dictService.insertDictRefList(dictreflist);
		}
		// 往栏目发布表里插入发布信息
		if (chalist != null && chalist.size() > 0) {
			channelService.insertChannelAssetList(chalist);
		}
		// 插入主播关联信息
		if (pfs != null && pfs.size() > 0) {
			personService.insertPersonRef(pfs);
		}
	}

	// public void removeDBAll(Map<String, Object> dbmap, String publisher) {
	// if (dbmap!=null && dbmap.size()>0) {
	// if (dbmap.containsKey("CrawlerNum")) {
	// String crawlerNum = dbmap.get("CrawlerNum").toString();
	// dbmap.remove("CrawlerNum");
	// Set<String> sets = dbmap.keySet();
	// if (sets!=null && sets.size()>0) {
	// for (String albumId : sets) {
	// try {
	// System.out.println("删除临时专辑数据 "+albumId);
	// removeDB(albumId, publisher, crawlerNum);
	// } catch (Exception e) {
	// continue;
	// }
	// }
	// }
	// }
	// }
	// }

	private void removeDB(String albumId) {
		ResOrgAssetService resOrgAssetService = (ResOrgAssetService) SpringShell.getBean("resOrgAssetService");
		AlbumAudioRefService albumAudioRefService = (AlbumAudioRefService) SpringShell.getBean("albumAudioRefService");
		// SolrJService solrJService = (SolrJService)
		// SpringShell.getBean("solrJService");
		List<AlbumAudioRefPo> albumAudioRefPos = albumAudioRefService.getAlbumAudioRefs(albumId);
		String ids = "";
		String maIds = "";
		String unionsql = "select resId from wt_ResOrgAsset_Ref where  ";
		if (albumAudioRefPos != null && albumAudioRefPos.size() > 0) {
			for (AlbumAudioRefPo albumAudioRefPo : albumAudioRefPos) {
				if (albumAudioRefPo != null) {
					ids += " or origId = '" + albumAudioRefPo.getAuId() + "'";
				}
			}
		}

		if (ids.length() > 0) {
			ids = ids.substring(3);
			ids = "(" + ids + ")";

			List<ResOrgAssetPo> res = resOrgAssetService.getResOrgAssetListBySQL(unionsql + ids);
			if (res != null && res.size() > 0) {
				for (ResOrgAssetPo resOrgAssetPo : res) {
					maIds += " or resId = '" + resOrgAssetPo.getResId() + "'";
					// solrJService.deleteById("AUDIO_"+resOrgAssetPo.getResId());
				}
				maIds = maIds.substring(3);
			}
			resOrgAssetService.deleteByOrigIds(ids, "wt_MediaAsset");
		}
		ResOrgAssetPo resOrgAssetPo = resOrgAssetService.getResOrgAssetPo(albumId, null, "wt_SeqMediaAsset");
		if (resOrgAssetPo != null) {
			resOrgAssetService.deleteByOrigId(albumId, null, "wt_SeqMediaAsset");
			// solrJService.deleteById("SEQU_"+resOrgAssetPo.getResId());
			if (maIds != null && maIds.length() > 0) {
				String smaId = resOrgAssetPo.getResId();
				Map<String, Object> m = new HashMap<>();
				m.put("resmaIds", maIds);
				m.put("ressmaIds", smaId);
				m.put("assetIds", maIds.replace("resId", "assetId"));
				mediaService.removeSeqMediaAssetAll(m);
			}
		}
	}
}
