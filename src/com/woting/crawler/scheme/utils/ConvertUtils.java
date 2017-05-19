package com.woting.crawler.scheme.utils;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.spiritdata.framework.util.DateUtils;
import com.spiritdata.framework.util.JsonUtils;
import com.spiritdata.framework.util.SequenceUUID;
import com.spiritdata.framework.util.StringUtils;
import com.woting.cm.core.ResOrgAsset.persis.po.ResOrgAssetPo;
import com.woting.cm.core.ResOrgAsset.service.ResOrgAssetService;
import com.woting.cm.core.channel.persis.po.ChannelAssetPo;
import com.woting.cm.core.channel.persis.po.ChannelMapRefPo;
import com.woting.cm.core.channel.service.ChannelService;
import com.woting.cm.core.dict.persis.po.DictDetailPo;
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
import com.woting.crawler.core.cperson.persis.po.CPersonPo;
import com.woting.crawler.core.cperson.service.CPersonService;
import com.woting.crawler.core.cplaycount.persis.po.CPlayCountPo;
import com.woting.crawler.core.cplaycount.service.CPlayCountService;
import com.woting.crawler.core.dict.persis.po.DictDPo;
import com.woting.crawler.core.dict.persis.po.DictRefPo;
import com.woting.crawler.core.dict.service.CrawlerDictService;
import com.woting.crawler.core.imagehash.persis.po.ImageHash;
import com.woting.crawler.core.imagehash.service.ImageHashService;
import com.woting.crawler.core.samedb.persis.po.SameDBPo;
import com.woting.crawler.core.samedb.service.SameDBService;
import com.woting.crawler.core.solr.SolrUpdateThread;
import com.woting.crawler.ext.SpringShell;
import com.woting.crawler.scheme.crawlerdb.crawler.AddCacheDBInfoThread;

public abstract class ConvertUtils {
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
	private static SameDBService sameDBService;
	private static OrganizeService organizeService;
	private static String[] filtStr = {"测试"};
	
	public static List<DictDPo> convert2DictD(List<Map<String, Object>> list, List<DictDPo> ddlist, String publisher, String dmid ,int isValidate, int crawlerNum) {
		List<DictDPo> dictdlist = new ArrayList<DictDPo>();
		if (list != null && list.size() > 0) {
			for (Map<String, Object> m : list) {
				DictDPo dd = new DictDPo();
				dd.setId(SequenceUUID.getPureUUID());
				dd.setSourceId(m.get("id") + "");
				dd.setDdName(m.get("name") + "");
				dd.setmId(dmid);
				if (!m.containsKey("pid") || ddlist == null) {
					dd.setpId("0");
				} else {
					for (DictDPo dictDPo : ddlist) {
						if (dictDPo.getSourceId().equals(m.get("pid") + ""))
							dd.setpId(dictDPo.getId());
					}
				}
				dd.setPublisher(publisher);
				if (m.containsKey("nPy"))
					dd.setnPy(m.get("nPy") + "");
				dd.setVisitUrl(m.get("visitUrl") + "");
				dd.setIsValidate(isValidate);
				dd.setCrawlerNum(crawlerNum);
				dd.setcTime(new Timestamp(System.currentTimeMillis()));
				dictdlist.add(dd);
			}
		}
		return dictdlist;
	}
	
	public static PersonPo convert2Person(CPersonPo cpo){
		PersonPo po = new PersonPo();
		po.setId(SequenceUUID.getPureUUID());
		po.setpName(cpo.getpName());
		po.setpSource(cpo.getpSource());
		if (po.getpSource().equals("喜马拉雅")) {
			po.setpSrcId("2");
		} else {
			if (po.getpSource().equals("蜻蜓")) {
				po.setpSrcId("3");
			} else {
				if (po.getpSource().equals("考拉")) {
					po.setpSrcId("4");
				} else {
					if (po.getpSource().equals("多听")) {
						po.setpSrcId("5");
					}
				}
			}
		}
		String imgp = cpo.getPortrait();
//		po.setPortrait(imgp);
		if (!StringUtils.isNullOrEmptyOrSpace(imgp) && imgp.length()>5) {
			String imgpath = null;
			try {
				imgpath = FileUtils.makeImgFile("1", imgp);
			} catch (Exception e) {}
			if (!StringUtils.isNullOrEmptyOrSpace(imgpath)) {
				po.setPortrait(imgpath);
			}
		}
		if (cpo.getAge()!=null) {
			po.setAge(cpo.getAge());
		}
		if (cpo.getBirthday()!=null) {
			po.setBirthday(cpo.getBirthday());
		}
		if (cpo.getConstellation()!=null) {
			po.setConstellation(cpo.getConstellation());
		}
		if(cpo.getEmail()!=null) {
			po.setEmail(cpo.getEmail());
		}
		if (cpo.getDescn()!=null) {
			po.setDescn(cpo.getDescn());
		}
		if (cpo.getPhoneNum()!=null) {
			po.setPhoneNum(cpo.getPhoneNum());
		}
		if (cpo.getpSrcHomePage()!=null) {
			po.setpSrcHomePage(cpo.getpSrcHomePage());
		}
		po.setIsVerified(cpo.getIsVerified());
		po.setcTime(new Timestamp(System.currentTimeMillis()));
		po.setLmTime(new Timestamp(System.currentTimeMillis()));
		return po;
	}

	public static String convertPlayNum2Long(String playnum) {
		int lastnum = -1;
		int begnum = -1;
		if (!playnum.contains("."))
			return playnum;
		begnum = playnum.indexOf(".");
		if (playnum.contains("万")) {
			lastnum = playnum.indexOf("万");
			if (lastnum - begnum == 2) {
				playnum = playnum.substring(0, lastnum);
				playnum = playnum.replace(".", "") + "000";
			}
			if (lastnum - begnum == 1) {
				playnum = playnum.substring(0, lastnum);
				playnum = playnum.replace(".", "")+"0000";
			}
		}
		if (playnum.contains("亿")) {
			lastnum = playnum.indexOf("亿");
			if (lastnum - begnum == 2) {
				playnum = playnum.substring(0, lastnum);
				playnum = playnum.replace(".", "") + "0000000";
			}
			if (lastnum - begnum == 1) {
				playnum = playnum.substring(0, lastnum);
				playnum = playnum.replace(".", "")+"00000000";
			}
		}
		return playnum;
	}
	
	public static long makeLongTime(String time) {
		try {
			Date date = DateUtils.getDateTime("yyyy-MM-dd HH:mm:ss", time);
			return date.getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	public static Map<String, Object> convert2SeqMedia(AlbumPo al, List<ChannelMapRefPo> chaMapRefs, Map<String, Object> chmap) {
		loadService();
		Map<String, Object> map = new HashMap<>();
		if (!matchFiltDB(al)) return null;
		if (resAssService.getResOrgAssetPo(al.getId(), al.getAlbumPublisher(), "wt_SeqMediaAsset")==null ) {
			ImageHashService imageHashService = (ImageHashService) SpringShell.getBean("imageHashService");
			al.setIsValidate(2);
			albumService.updateAlbum(al);
			List<AlbumAudioRefPo> alaurefs = albumAudioRefService.getAlbumAudioRefs(al.getId());
			if (alaurefs==null || alaurefs.size()==0) return null;
			SeqMediaAssetPo seq = new SeqMediaAssetPo();
			seq.setId(SequenceUUID.getPureUUID());
			seq.setSmaTitle(al.getAlbumName());
			String imgp = al.getAlbumImg();
			if (!StringUtils.isNullOrEmptyOrSpace(imgp) && imgp.length()>5) {
				String imgpath = null;
				List<ImageHash> imageHashs = imageHashService.getImageHashByImageSrcPath(imgp, "2");
				if (imageHashs!=null && imageHashs.size()>0) {
					ImageHash imageHash = imageHashs.get(0);
					imgpath = imageHash.getImagePath();
				} else {
					try {
						imgpath = FileUtils.makeImgFile("2", imgp);
					} catch (Exception e) {}
				}
				if (!StringUtils.isNullOrEmptyOrSpace(imgpath)) {
					seq.setSmaImg(imgpath);
				}
			}
			seq.setSmaPubType(1);
			if (al.getAlbumPublisher().equals("喜马拉雅")) {
				seq.setSmaPubId("2");
				seq.setSmaPublisher("喜马拉雅");
			} else {
				if (al.getAlbumPublisher().equals("蜻蜓")) {
					seq.setSmaPubId("3");
					seq.setSmaPublisher("蜻蜓");
				} else {
					if (al.getAlbumPublisher().equals("考拉")) {
						seq.setSmaPubId("4");
						seq.setSmaPublisher("考拉");
					} else {
						if (al.getAlbumPublisher().equals("多听")) {
							seq.setSmaPubId("5");
							seq.setSmaPublisher("多听");
						}
					}
				}
			}
			seq.setSmaAllCount(0);
			if (al.getAlbumTags() != null && !al.getAlbumTags().equals("null")) seq.setKeyWords(al.getAlbumTags());
			seq.setLangDid("zho");
			seq.setLanguage("中文");
			if (al.getDescn() != null && !al.getDescn().equals("null")) seq.setDescn(al.getDescn());
			else seq.setDescn("欢迎大家收听"+seq.getSmaTitle());
			seq.setCTime(al.getcTime());
			seq.setPubCount(1);
			seq.setSmaStatus(1);
			
			ResOrgAssetPo resOrgper = new ResOrgAssetPo();
			resOrgper.setId(SequenceUUID.getPureUUID());
			resOrgper.setResId(seq.getId());
			resOrgper.setResTableName("wt_SeqMediaAsset");
			resOrgper.setOrigSrcId(al.getAlbumId());
			resOrgper.setOrigId(al.getId());
			resOrgper.setOrgName(al.getAlbumPublisher());
			resOrgper.setOrigTableName("c_Album");
			resOrgper.setcTime(new Timestamp(System.currentTimeMillis()));
			resAssService.insertResOrgAsset(resOrgper);
			
			mediaService.insertSeq(seq);
			map.put("seq", seq);

			CPlayCountPo cp = cPlayCountService.getCPlayCountPo(al.getId(), "c_Album");
			MediaPlayCountPo mecount = new MediaPlayCountPo();
			mecount.setId(SequenceUUID.getPureUUID());
			mecount.setResTableName("wt_SeqMediaAsset");
			mecount.setResId(seq.getId());
			if (cp!=null) mecount.setPlayCount(cp.getPlayCount());
			else mecount.setPlayCount(0);
			mecount.setPublisher(al.getAlbumPublisher());
			mecount.setcTime(new Timestamp(System.currentTimeMillis()));
			mediaService.insertMediaPlayCount(mecount);
			
			List<CPersonPo> cPos = cPersonService.getCPersons(al.getAlbumPublisher(), al.getId(), "c_Album");
			List<PersonPo> pos = new ArrayList<>();
			synchronized (cPersonService) {
				if (cPos!=null && cPos.size()>0) {
					for (CPersonPo cPersonPo : cPos) {
						if (cPersonPo!=null) {
							ResOrgAssetPo resOrgPerson = resAssService.getResOrgAssetPo(cPersonPo.getId(), cPersonPo.getpSource(), "wt_Person");
							boolean isok = false;
							PersonPo po = null;
							if (resOrgPerson!=null) {
								po = personService.getPersonByPersonId(resOrgPerson.getResId());
								if (po!=null) {
									isok = true;
									PersonRefPo pf = new PersonRefPo();
									pf.setId(SequenceUUID.getPureUUID());
									pf.setPersonId(po.getId());
									pf.setRefName("主播");
									pf.setResId(seq.getId());
									pf.setResTableName("wt_SeqMediaAsset");
									pf.setcTime(new Timestamp(System.currentTimeMillis()));
									personService.insertPersonRef(pf);
								}
							}
							if (!isok) {
								po = ConvertUtils.convert2Person(cPersonPo);
								personService.insertPerson(po);
								
								resOrgper = new ResOrgAssetPo();
								resOrgper.setId(SequenceUUID.getPureUUID());
								resOrgper.setResId(po.getId());
								resOrgper.setResTableName("wt_Person");
								resOrgper.setOrigSrcId(cPersonPo.getpSrcId());
								resOrgper.setOrigId(cPersonPo.getId());
								resOrgper.setOrgName(cPersonPo.getpSource());
								resOrgper.setOrigTableName("c_Person");
								resOrgper.setcTime(new Timestamp(System.currentTimeMillis()));
								resAssService.insertResOrgAsset(resOrgper);
								
								PersonRefPo pf = new PersonRefPo();
								pf.setId(SequenceUUID.getPureUUID());
								pf.setPersonId(po.getId());
								pf.setRefName("主播");
								pf.setResId(seq.getId());
								pf.setResTableName("wt_SeqMediaAsset");
								pf.setcTime(new Timestamp(System.currentTimeMillis()));
								personService.insertPersonRef(pf);
								
								DictRefResPo dictRefResPo = new DictRefResPo();
								dictRefResPo.setId(SequenceUUID.getPureUUID());
								dictRefResPo.setRefName("主播-性别");
								dictRefResPo.setDictMid("8");
								if (cPersonPo.getSex() == 0) {
									dictRefResPo.setDictDid("xb003");
								} else if (cPersonPo.getSex() == 1) {
									dictRefResPo.setDictDid("xb001");
								} else if (cPersonPo.getSex() == 2) {
									dictRefResPo.setDictDid("xb002");
								}
								dictRefResPo.setResTableName("wt_Person");
								dictRefResPo.setResId(po.getId());
								dictRefResPo.setCTime(new Timestamp(System.currentTimeMillis()));
								dictService.insertDictRef(dictRefResPo);
								
								dictRefResPo = new DictRefResPo();
								dictRefResPo.setId(SequenceUUID.getPureUUID());
								dictRefResPo.setRefName("主播-状态");
								dictRefResPo.setDictMid("10");
								dictRefResPo.setDictDid("zbzt01");
								dictRefResPo.setResTableName("wt_Person");
								dictRefResPo.setResId(po.getId());
								dictRefResPo.setCTime(new Timestamp(System.currentTimeMillis()));
								dictService.insertDictRef(dictRefResPo);
								
								if (!StringUtils.isNullOrEmptyOrSpace(cPersonPo.getLocation())) {
									String[] lco = cPersonPo.getLocation().split("_");
									DictDetailPo ddpo = dictService.getDictDetail("2", "0", lco[0]);
									if (ddpo != null) {
										if (lco.length >= 2) {
											DictDetailPo ddpo2 = dictService.getDictDetail("2", ddpo.getId(), lco[1]);
											if (ddpo2 != null) {
												dictRefResPo.setId(SequenceUUID.getPureUUID());
												dictRefResPo.setRefName("主播-地区");
												dictRefResPo.setDictMid("2");
												dictRefResPo.setDictDid(ddpo2.getId());
												dictRefResPo.setResTableName("wt_Person");
												dictRefResPo.setResId(po.getId());
												dictRefResPo.setCTime(new Timestamp(System.currentTimeMillis()));
												dictService.insertDictRef(dictRefResPo);
											}
										} else {
											dictRefResPo.setId(SequenceUUID.getPureUUID());
											dictRefResPo.setRefName("主播-地区");
											dictRefResPo.setDictMid("2");
											dictRefResPo.setDictDid(ddpo.getId());
											dictRefResPo.setResTableName("wt_Person");
											dictRefResPo.setResId(po.getId());
											dictRefResPo.setCTime(new Timestamp(System.currentTimeMillis()));
											dictService.insertDictRef(dictRefResPo);
										}
									}
								}
							}
							if (po!=null) {
								pos.add(po);
							}
						}
					}
				}
			}
			
			String poStr = null;
			if (pos!=null && pos.size()>0) {
				poStr = "";
				for (PersonPo personPo : pos) {
					poStr += ","+personPo.getpName();
				}
				poStr = poStr.substring(1);
			}
			map.put("pName", poStr);
			
			List<DictRefPo> cdRefPos = crawlerDictService.getDictRefs(al.getId(), "c_Album", null);
			String chaNameStr = "";
			Map<String, Object> chaMapRefIdMap = new HashMap<>();
			if (cdRefPos!=null && cdRefPos.size()>0 && chaMapRefs !=null && chaMapRefs.size()>0) {
				for (DictRefPo dictRefPo : cdRefPos) {
					for (ChannelMapRefPo chaRef : chaMapRefs) {
						if (chaRef.getSrcDid().equals(dictRefPo.getCdictDid())) {
							if (!chaNameStr.contains(chaRef.getChannelId())) {
								chaNameStr +=","+chaRef.getChannelId();
								chaMapRefIdMap.put(chaRef.getChannelId(), "wt_ChannelMapRef_"+chaRef.getId());
							}
						}
					}
				}
			}
			String chstr = "";
			if (chaNameStr!=null && chaNameStr.length()>0) {
				chaNameStr = chaNameStr.substring(1);
				String[] chIds = chaNameStr.split(",");
				for (String chId : chIds) {
					try {chstr += ","+chmap.get(chId).toString();} catch (Exception e) {}
				}
			}
			if (chstr.length()>0) map.put("chaStr", chstr.substring(1));
			else map.put("chaStr", null);
			
			List<DictRefResPo> dictres = new ArrayList<>();
			List<ChannelAssetPo> chas = new ArrayList<>();
			if (chaNameStr.length()>0) {
				String[] chasStr = chaNameStr.split(",");
				if (chasStr.length>0) {
					for (String chaStr : chasStr) {
						DictRefResPo dictRefRes = new DictRefResPo();
			            dictRefRes.setId(SequenceUUID.getPureUUID());
			            dictRefRes.setRefName("专辑-内容分类");
			            dictRefRes.setResTableName("wt_SeqMediaAsset");
			            dictRefRes.setResId(seq.getId());
			            dictRefRes.setDictMid("3");
		                dictRefRes.setDictDid(chaStr);
		                dictRefRes.setCTime(new Timestamp(System.currentTimeMillis()));
		                dictres.add(dictRefRes);
		                
		                ChannelAssetPo cha = new ChannelAssetPo();
						cha.setId(SequenceUUID.getPureUUID());
						cha.setAssetType("wt_SeqMediaAsset");
						cha.setAssetId(seq.getId());
						cha.setPublisherId(seq.getSmaPubId());
						cha.setIsValidate(2);  // 设为无效
						cha.setCheckerId("1");
						cha.setPubName(seq.getSmaTitle());
						cha.setPubImg(seq.getSmaImg());
						cha.setSort(0);
						cha.setFlowFlag(2);
						cha.setInRuleIds(chaMapRefIdMap.get(chaStr).toString());
						cha.setCheckRuleIds("etl");
						cha.setCTime(new Timestamp(System.currentTimeMillis()));
						cha.setPubTime(al.getcTime());
						cha.setChannelId(chaStr);
						chas.add(cha);
					    keyWordService.saveKwAndKeRef(al.getAlbumTags(), "wt_Channel", cha.getChannelId()); //标签与栏目绑定
					}
				}
			} else {
				DictRefResPo dictref = new DictRefResPo();
				dictref.setId(SequenceUUID.getPureUUID());
				dictref.setRefName("专辑-内容分类");
				dictref.setResTableName("wt_SeqMediaAsset");
				dictref.setResId(seq.getId());
	            dictref.setDictMid("3");
	            dictref.setDictDid("cn36");
	            dictref.setCTime(new Timestamp(System.currentTimeMillis()));
	            dictres.add(dictref);
	            
	            ChannelAssetPo cha = new ChannelAssetPo();
				cha.setId(SequenceUUID.getPureUUID());
				cha.setAssetType("wt_SeqMediaAsset");
				cha.setAssetId(seq.getId());
				cha.setPublisherId(seq.getSmaPubId());
				cha.setIsValidate(2);  // 设为无效
				cha.setCheckerId("1");
				cha.setPubName(seq.getSmaTitle());
				cha.setPubImg(seq.getSmaImg());
				cha.setSort(0);
				cha.setFlowFlag(2);
				cha.setInRuleIds("wt_ChannelMapRef_null");
				cha.setCheckRuleIds("etl");
				cha.setCTime(new Timestamp(System.currentTimeMillis()));
				cha.setPubTime(al.getcTime());
				cha.setChannelId("cn36");
				chas.add(cha);
			}
			
			dictService.insertDictRefList(dictres);
			channelService.insertChannelAssetList(chas);
			if (!StringUtils.isNullOrEmptyOrSpace(al.getAlbumTags()) && !al.getAlbumTags().equals("null")) {
			    KeyWordService keyWordService = (KeyWordService) SpringShell.getBean("keyWordService");
			    keyWordService.saveKwAndKeRef(al.getAlbumTags(), "wt_SeqMediaAsset", seq.getId()); // 标签与专辑绑定
		    }
			
			//TODO
			List<AlbumAudioRefPo> erralaurefs = new ArrayList<>();
			ExecutorService fixedThreadPool = Executors.newFixedThreadPool(20);
			for (AlbumAudioRefPo alauref : alaurefs) {
				if (alauref!=null) {
					String chStr = chaNameStr;
					fixedThreadPool.execute(new Runnable() {
						public void run() {
							AudioPo au = null;
							au = audioService.getAudioInfo(alauref.getAuId());
							if (au!=null) {
//								if(matchFiltDB(au.getAudioName())) return;
								Map<String, Object> m = new HashMap<>();
								m.put("origId", au.getId());
						    	m.put("orgName", au.getAudioPublisher());
						    	m.put("resTableName", "wt_MediaAsset");
								List<ResOrgAssetPo> ress = resAssService.getResOrgAssetPo(m);
								if (ress==null || ress.size()==0) {
									// 声音数据转换
									MediaAssetPo ma = new MediaAssetPo();
									ma.setId(SequenceUUID.getPureUUID());
									ma.setMaTitle(au.getAudioName());
									if (au.getAudioPublisher().equals("蜻蜓") || au.getAudioPublisher().equals("多听")) {
										ma.setMaImg(seq.getSmaImg());
									} else {
										String auimgp = au.getAudioImg();
										if (!StringUtils.isNullOrEmptyOrSpace(auimgp) && auimgp.length()>5) {
											String imgpath =null;
											List<ImageHash> imageHashs = imageHashService.getImageHashByImageSrcPath(auimgp, "2");
											if (imageHashs!=null && imageHashs.size()>0) {
												ImageHash imageHash = imageHashs.get(0);
												imgpath = imageHash.getImagePath();
											} else try {imgpath = FileUtils.makeImgFile("2", auimgp);} catch (Exception e) {}
											if (!StringUtils.isNullOrEmptyOrSpace(imgpath)) ma.setMaImg(imgpath);
										}
									}
									ma.setMaURL(au.getAudioURL());
									ma.setMaPubId(seq.getSmaPubId());
									ma.setMaPublisher(seq.getSmaPublisher());
									ma.setMaPubType(seq.getSmaPubType());
									ma.setLangDid(seq.getLangDid());
									ma.setLanguage(seq.getLanguage());
									if (au.getDescn()!=null && !au.getDescn().equals("null")) {
										ma.setDescn(au.getDescn());
									} else {
										ma.setDescn("欢迎大家收听"+au.getAudioName());
									}
									if (au.getAudioTags()!=null && !au.getAudioTags().equals("null") && au.getAudioTags().length()>1) {
										ma.setKeyWords(au.getAudioTags());
									}
									ma.setPubCount(1);
									if (au.getDuration()==null || au.getDuration().equals("null")) {
										ma.setTimeLong(10000);
									} else {
										if (au.getDuration().contains(".")) {
											long d1 = (long) (Double.valueOf(au.getDuration())/1);
											long d2 = (long) (Double.valueOf(au.getDuration())%1*1000);
											ma.setTimeLong(d1*1000+d2);
										} else {
											ma.setTimeLong(Long.valueOf(au.getDuration()));
										}
									}
									ma.setMaStatus(1);
									if (au.getcTime()!=null) {
										ma.setCTime(au.getcTime());
									} else {
										ma.setCTime(new Timestamp(System.currentTimeMillis()));
									}
									
									//先进性资源与外部系统对照表数据插入
									ResOrgAssetPo roa = new ResOrgAssetPo();
									roa.setId(SequenceUUID.getPureUUID());
									roa.setResId(ma.getId());
									roa.setResTableName("wt_MediaAsset");
									roa.setOrgName(ma.getMaPublisher());
									roa.setOrigId(au.getId());
									roa.setOrigTableName("c_Audio");
									roa.setOrigSrcId(au.getAudioId());
									roa.setcTime(new Timestamp(System.currentTimeMillis()));
									try {
										resAssService.insertResOrgAsset(roa);
									} catch (Exception e) {
										erralaurefs.add(alauref);
										return ;
									}

									mediaService.insertMa(ma);
									keyWordService.saveKwAndKeRef(ma.getKeyWords(), "wt_MediaAsset", ma.getId()); //标签与栏目绑定
									
									MaSourcePo maS = new MaSourcePo();
									maS.setId(SequenceUUID.getPureUUID());
									maS.setMaId(ma.getId());
									maS.setIsMain(1);
									maS.setMaSrcType(seq.getSmaPubType());
									maS.setMaSrcId(seq.getSmaPubId());
									maS.setMaSource(seq.getSmaPublisher());
									maS.setPlayURI(au.getAudioURL());
									maS.setDescn(ma.getDescn());
									maS.setCTime(new Timestamp(System.currentTimeMillis()));
									mediaService.insertMas(maS);
									
									try {
										CPlayCountPo countPo = cPlayCountService.getCPlayCountPo(au.getId(), "c_Audio");
										MediaPlayCountPo mecount = new MediaPlayCountPo();
										mecount.setId(SequenceUUID.getPureUUID());
										mecount.setResTableName("wt_MediaAsset");
										mecount.setResId(ma.getId());
										if (countPo!=null) mecount.setPlayCount(countPo.getPlayCount());
										else mecount.setPlayCount(0);
										mecount.setPublisher(al.getAlbumPublisher());
										mecount.setcTime(new Timestamp(System.currentTimeMillis()));
										mediaService.insertMediaPlayCount(mecount);
									} catch (Exception e) {e.printStackTrace();}
									
									SeqMaRefPo seqMaRef = new SeqMaRefPo();
									seqMaRef.setId(SequenceUUID.getPureUUID());
									seqMaRef.setsId(seq.getId());
									seqMaRef.setmId(ma.getId());
									seqMaRef.setColumnNum(alauref.getColumnNum());
									seqMaRef.setDescn(ma.getDescn());
									seqMaRef.setIsMain(alauref.getIsMain());
									seqMaRef.setcTime(new Timestamp(System.currentTimeMillis()));
									mediaService.insertSeqRef(seqMaRef);
									
									if (alauref.getIsMain()==1 && pos!=null && pos.size()>0) {
										for (PersonPo po : pos) {
											PersonRefPo pf = personService.getPersonRefBy(po.getId(), "wt_MediaAsset", ma.getId());
											if (pf==null) {
												pf = new PersonRefPo();
												pf.setId(SequenceUUID.getPureUUID());
												pf.setRefName("主播");
											    pf.setResTableName("wt_MediaAsset");
											    pf.setResId(ma.getId());
											    pf.setPersonId(po.getId());
											    pf.setcTime(new Timestamp(System.currentTimeMillis()));
											    personService.insertPersonRef(pf);
											}
										}
									}
									
									if (chStr.length()>0) {
										String[] chasStr = chStr.split(",");
										if (chasStr.length>0) {
											for (String chaStr : chasStr) {
												DictRefResPo dictRefRes = new DictRefResPo();
									            dictRefRes.setId(SequenceUUID.getPureUUID());
									            dictRefRes.setRefName("专辑-内容分类");
									            dictRefRes.setResTableName("wt_MediaAsset");
									            dictRefRes.setResId(ma.getId());
									            dictRefRes.setDictMid("3");
								                dictRefRes.setDictDid(chaStr);
								                dictRefRes.setCTime(new Timestamp(System.currentTimeMillis()));
								                dictService.insertDictRef(dictRefRes);
								                
								                ChannelAssetPo cha = new ChannelAssetPo();
												cha.setId(SequenceUUID.getPureUUID());
												cha.setAssetType("wt_MediaAsset");
												cha.setAssetId(ma.getId());
												cha.setPublisherId(seq.getSmaPubId());
												cha.setIsValidate(1);  // 设为无效
												cha.setCheckerId("1");
												cha.setPubName(ma.getMaTitle());
												cha.setPubImg(ma.getMaImg());
												cha.setSort(0);
												cha.setFlowFlag(2);
												cha.setInRuleIds(chaMapRefIdMap.get(chaStr).toString());
												cha.setCheckRuleIds("etl");
												cha.setCTime(new Timestamp(System.currentTimeMillis()));
												cha.setPubTime(au.getPubTime());
												cha.setChannelId(chaStr);
												channelService.insertChannelAsset(cha);
											    keyWordService.saveKwAndKeRef(ma.getKeyWords(), "wt_Channel", cha.getChannelId()); //标签与栏目绑定
											}
										}
									} else {
										DictRefResPo dictref = new DictRefResPo();
										dictref.setId(SequenceUUID.getPureUUID());
										dictref.setRefName("专辑-内容分类");
										dictref.setResTableName("wt_MediaAsset");
										dictref.setResId(ma.getId());
							            dictref.setDictMid("3");
							            dictref.setDictDid("cn36");
							            dictref.setCTime(new Timestamp(System.currentTimeMillis()));
							            dictService.insertDictRef(dictref);
							            
							            ChannelAssetPo cha = new ChannelAssetPo();
										cha.setId(SequenceUUID.getPureUUID());
										cha.setAssetType("wt_MediaAsset");
										cha.setAssetId(ma.getId());
										cha.setPublisherId(seq.getSmaPubId());
										cha.setIsValidate(1);  // 设为无效
										cha.setCheckerId("1");
										cha.setPubName(ma.getMaTitle());
										cha.setPubImg(ma.getMaImg());
										cha.setSort(0);
										cha.setFlowFlag(2);
										cha.setInRuleIds("wt_ChannelMapRef_null");
										cha.setCheckRuleIds("etl");
										cha.setCTime(new Timestamp(System.currentTimeMillis()));
										cha.setPubTime(au.getPubTime());
										cha.setChannelId("cn36");
										channelService.insertChannelAsset(cha);
									}	
								} else {
									SeqMaRefPo seqMaRef = new SeqMaRefPo();
									seqMaRef.setId(SequenceUUID.getPureUUID());
									seqMaRef.setsId(seq.getId());
									String sql = "SELECT res.* FROM wt_SeqMA_Ref sf,"
											+ " (SELECT * FROM wt_ResOrgAsset_Ref where resTableName = 'wt_MediaAsset' and origId = '"+au.getId()+"') res"
											+ " where sf.mId = res.resId and sf.isMain = 1 ";
									List<ResOrgAssetPo> resss = resAssService.getResOrgAssetListBySQL(sql);
									if (resss!=null && resss.size()>0) {
										seqMaRef.setmId(resss.get(0).getResId());
									} else {
										seqMaRef.setmId(ress.get(0).getResId());
									}
									seqMaRef.setColumnNum(alauref.getColumnNum());
									seqMaRef.setDescn(au.getDescn());
									seqMaRef.setIsMain(alauref.getIsMain());
									seqMaRef.setcTime(new Timestamp(System.currentTimeMillis()));
									mediaService.insertSeqRef(seqMaRef);
								}
							}
						}
					});
				}
			}
			fixedThreadPool.shutdown();
			while (true) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if (fixedThreadPool.isTerminated()) {
					break;
				}
			}
			if (erralaurefs!=null && erralaurefs.size()>0) {
				for (AlbumAudioRefPo alauref : erralaurefs) {
					AudioPo au = audioService.getAudioInfo(alauref.getAuId());
					if (au!=null) {
						Map<String, Object> m = new HashMap<>();
						m.put("origId", au.getId());
					    m.put("orgName", au.getAudioPublisher());
					    m.put("resTableName", "wt_MediaAsset");
						List<ResOrgAssetPo> ress = resAssService.getResOrgAssetPo(m);
						if (ress!=null && ress.size()>0) {
							SeqMaRefPo seqMaRef = new SeqMaRefPo();
							seqMaRef.setId(SequenceUUID.getPureUUID());
							seqMaRef.setsId(seq.getId());
							String sql = "SELECT res.* FROM wt_SeqMA_Ref sf,"
									+ " (SELECT * FROM wt_ResOrgAsset_Ref where resTableName = 'wt_MediaAsset' and origId = '"+au.getId()+"') res"
									+ " where sf.mId = res.resId and sf.isMain = 1 ";
							List<ResOrgAssetPo> resss = resAssService.getResOrgAssetListBySQL(sql);
							if (resss!=null && resss.size()>0) seqMaRef.setmId(resss.get(0).getResId());
							else seqMaRef.setmId(ress.get(0).getResId());
							seqMaRef.setColumnNum(alauref.getColumnNum());
							seqMaRef.setDescn(au.getDescn());
							seqMaRef.setIsMain(alauref.getIsMain());
							seqMaRef.setcTime(new Timestamp(System.currentTimeMillis()));
							mediaService.insertSeqRef(seqMaRef);
						}
					}
				}
			}
			if (erralaurefs!=null && erralaurefs.size()==0) {
				al.setIsValidate(3);
				albumService.updateAlbum(al);
				return map;
			} else {
				System.out.println("出错专辑    "+al.getId());
				return null;
			}
		}
		return null;
	}
	
	// TODO
	public static Map<String, Object> convert2SeqMediaToSame (AlbumPo al) {
		loadService();
		Map<String, Object> map = new HashMap<>();
		if (!matchFiltDB(al)) return null;
		ResOrgAssetPo rAssetPo = resAssService.getResOrgAssetPo(al.getId(), al.getAlbumName(), "wt_SeqMediaAsset");
		if (rAssetPo==null ) {
			ImageHashService imageHashService = (ImageHashService) SpringShell.getBean("imageHashService");
			al.setIsValidate(2);
			albumService.updateAlbum(al);
			List<AlbumAudioRefPo> alaurefs = albumAudioRefService.getAlbumAudioRefs(al.getId());
			if (alaurefs==null || alaurefs.size()==0) return null;
			SeqMediaAssetPo seq = new SeqMediaAssetPo();
			seq.setId(SequenceUUID.getPureUUID());
			seq.setSmaTitle(al.getAlbumName());
			String imgp = al.getAlbumImg();
			if (!StringUtils.isNullOrEmptyOrSpace(imgp) && imgp.length()>5) {
				String imgpath = null;
				List<ImageHash> imageHashs = imageHashService.getImageHashByImageSrcPath(imgp, "2");
				if (imageHashs!=null && imageHashs.size()>0) {
					ImageHash imageHash = imageHashs.get(0);
					imgpath = imageHash.getImagePath();
				} else {
					try {
						imgpath = FileUtils.makeImgFile("2", imgp);
					} catch (Exception e) {}
				}
				if (!StringUtils.isNullOrEmptyOrSpace(imgpath)) {
					seq.setSmaImg(imgpath);
				}
			}
			seq.setSmaPubType(1);
			if (al.getAlbumPublisher().equals("喜马拉雅")) {
				seq.setSmaPubId("2");
				seq.setSmaPublisher("喜马拉雅");
			} else {
				if (al.getAlbumPublisher().equals("蜻蜓")) {
					seq.setSmaPubId("3");
					seq.setSmaPublisher("蜻蜓");
				} else {
					if (al.getAlbumPublisher().equals("考拉")) {
						seq.setSmaPubId("4");
						seq.setSmaPublisher("考拉");
					} else {
						if (al.getAlbumPublisher().equals("多听")) {
							seq.setSmaPubId("5");
							seq.setSmaPublisher("多听");
						}
					}
				}
			}
			seq.setSmaAllCount(0);
			if (al.getAlbumTags() != null && !al.getAlbumTags().equals("null")) seq.setKeyWords(al.getAlbumTags());
			seq.setLangDid("zho");
			seq.setLanguage("中文");
			if (al.getDescn() != null && !al.getDescn().equals("null")) seq.setDescn(al.getDescn());
			else seq.setDescn("欢迎大家收听"+seq.getSmaTitle());
			seq.setCTime(al.getcTime());
			seq.setPubCount(1);
			seq.setSmaStatus(1);
			mediaService.insertSeq(seq);
			map.put("smaId", seq.getId());
			
			ResOrgAssetPo resOrgper = new ResOrgAssetPo();
			resOrgper.setId(SequenceUUID.getPureUUID());
			resOrgper.setResId(seq.getId());
			resOrgper.setResTableName("wt_SeqMediaAsset");
			resOrgper.setOrigSrcId(al.getAlbumId());
			resOrgper.setOrigId(al.getId());
			resOrgper.setOrgName(al.getAlbumPublisher());
			resOrgper.setOrigTableName("c_Album");
			resOrgper.setcTime(new Timestamp(System.currentTimeMillis()));
			resAssService.insertResOrgAsset(resOrgper);
			
			CPlayCountPo cp = cPlayCountService.getCPlayCountPo(al.getId(), "c_Album");
			MediaPlayCountPo mecount = new MediaPlayCountPo();
			mecount.setId(SequenceUUID.getPureUUID());
			mecount.setResTableName("wt_SeqMediaAsset");
			mecount.setResId(seq.getId());
			if (cp!=null) mecount.setPlayCount(cp.getPlayCount());
			else mecount.setPlayCount(0);
			mecount.setPublisher(al.getAlbumPublisher());
			mecount.setcTime(new Timestamp(System.currentTimeMillis()));
			mediaService.insertMediaPlayCount(mecount);
			
			List<CPersonPo> cPos = cPersonService.getCPersons(al.getAlbumPublisher(), al.getId(), "c_Album");
			List<PersonPo> pos = new ArrayList<>();
			synchronized (cPersonService) {
				if (cPos!=null && cPos.size()>0) {
					for (CPersonPo cPersonPo : cPos) {
						if (cPersonPo!=null) {
							ResOrgAssetPo resOrgPerson = resAssService.getResOrgAssetPo(cPersonPo.getId(), cPersonPo.getpSource(), "wt_Person");
							boolean isok = false;
							PersonPo po = null;
							if (resOrgPerson!=null) {
								po = personService.getPersonByPersonId(resOrgPerson.getResId());
								if (po!=null) {
									isok = true;
								}
							}
							if (!isok) {
								po = ConvertUtils.convert2Person(cPersonPo);
								personService.insertPerson(po);
								
								resOrgper = new ResOrgAssetPo();
								resOrgper.setId(SequenceUUID.getPureUUID());
								resOrgper.setResId(po.getId());
								resOrgper.setResTableName("wt_Person");
								resOrgper.setOrigSrcId(cPersonPo.getpSrcId());
								resOrgper.setOrigId(cPersonPo.getId());
								resOrgper.setOrgName(cPersonPo.getpSource());
								resOrgper.setOrigTableName("c_Person");
								resOrgper.setcTime(new Timestamp(System.currentTimeMillis()));
								resAssService.insertResOrgAsset(resOrgper);
								
								DictRefResPo dictRefResPo = new DictRefResPo();
								dictRefResPo.setId(SequenceUUID.getPureUUID());
								dictRefResPo.setRefName("主播-性别");
								dictRefResPo.setDictMid("8");
								if (cPersonPo.getSex() == 0) {
									dictRefResPo.setDictDid("xb003");
								} else if (cPersonPo.getSex() == 1) {
									dictRefResPo.setDictDid("xb001");
								} else if (cPersonPo.getSex() == 2) {
									dictRefResPo.setDictDid("xb002");
								}
								dictRefResPo.setResTableName("wt_Person");
								dictRefResPo.setResId(po.getId());
								dictRefResPo.setCTime(new Timestamp(System.currentTimeMillis()));
								dictService.insertDictRef(dictRefResPo);
								
								dictRefResPo = new DictRefResPo();
								dictRefResPo.setId(SequenceUUID.getPureUUID());
								dictRefResPo.setRefName("主播-状态");
								dictRefResPo.setDictMid("10");
								dictRefResPo.setDictDid("zbzt01");
								dictRefResPo.setResTableName("wt_Person");
								dictRefResPo.setResId(po.getId());
								dictRefResPo.setCTime(new Timestamp(System.currentTimeMillis()));
								dictService.insertDictRef(dictRefResPo);
								
								if (!StringUtils.isNullOrEmptyOrSpace(cPersonPo.getLocation())) {
									String[] lco = cPersonPo.getLocation().split("_");
									DictDetailPo ddpo = dictService.getDictDetail("2", "0", lco[0]);
									if (ddpo != null) {
										if (lco.length >= 2) {
											DictDetailPo ddpo2 = dictService.getDictDetail("2", ddpo.getId(), lco[1]);
											if (ddpo2 != null) {
												dictRefResPo.setId(SequenceUUID.getPureUUID());
												dictRefResPo.setRefName("主播-地区");
												dictRefResPo.setDictMid("2");
												dictRefResPo.setDictDid(ddpo2.getId());
												dictRefResPo.setResTableName("wt_Person");
												dictRefResPo.setResId(po.getId());
												dictRefResPo.setCTime(new Timestamp(System.currentTimeMillis()));
												dictService.insertDictRef(dictRefResPo);
											}
										} else {
											dictRefResPo.setId(SequenceUUID.getPureUUID());
											dictRefResPo.setRefName("主播-地区");
											dictRefResPo.setDictMid("2");
											dictRefResPo.setDictDid(ddpo.getId());
											dictRefResPo.setResTableName("wt_Person");
				     						dictRefResPo.setResId(po.getId());
											dictRefResPo.setCTime(new Timestamp(System.currentTimeMillis()));
											dictService.insertDictRef(dictRefResPo);
										}
									}
								}
							}
							if (po!=null) {
								pos.add(po);
							}
						}
					}
				}
			}
			map.put("persons", pos);
			
			if (!StringUtils.isNullOrEmptyOrSpace(al.getAlbumTags()) && !al.getAlbumTags().equals("null")) {
			    KeyWordService keyWordService = (KeyWordService) SpringShell.getBean("keyWordService");
			    keyWordService.saveKwAndKeRef(al.getAlbumTags(), "wt_SeqMediaAsset", seq.getId()); // 标签与专辑绑定
		    }
			
			//TODO
			List<AlbumAudioRefPo> erralaurefs = new ArrayList<>();
			Map<String, Object> mas = new HashMap<>();
			ExecutorService fixedThreadPool = Executors.newFixedThreadPool(20);
			for (AlbumAudioRefPo alauref : alaurefs) {
				if (alauref!=null) {
					fixedThreadPool.execute(new Runnable() {
						public void run() {
							AudioPo au = null;
							au = audioService.getAudioInfo(alauref.getAuId());
							if (au!=null) {
								Map<String, Object> m = new HashMap<>();
								m.put("origId", au.getId());
						    	m.put("orgName", au.getAudioPublisher());
						    	m.put("resTableName", "wt_MediaAsset");
								List<ResOrgAssetPo> ress = resAssService.getResOrgAssetPo(m);
								if (ress==null || ress.size()==0) {
									// 声音数据转换
	    					    	MediaAssetPo ma = new MediaAssetPo();
							    	ma.setId(SequenceUUID.getPureUUID());
									ma.setMaTitle(au.getAudioName());
									if (au.getAudioPublisher().equals("蜻蜓") || au.getAudioPublisher().equals("多听")) {
										ma.setMaImg(seq.getSmaImg());
									} else {
										String auimgp = au.getAudioImg();
										if (!StringUtils.isNullOrEmptyOrSpace(auimgp) && auimgp.length()>5) {
											String imgpath =null;
											List<ImageHash> imageHashs = imageHashService.getImageHashByImageSrcPath(auimgp, "2");
											if (imageHashs!=null && imageHashs.size()>0) {
												ImageHash imageHash = imageHashs.get(0);
												imgpath = imageHash.getImagePath();
											} else try {imgpath = FileUtils.makeImgFile("2", auimgp);} catch (Exception e) {}
											if (!StringUtils.isNullOrEmptyOrSpace(imgpath)) ma.setMaImg(imgpath);
											}
									}
									ma.setMaURL(au.getAudioURL());
									ma.setMaPubId(seq.getSmaPubId());
									ma.setMaPublisher(seq.getSmaPublisher());
									ma.setMaPubType(seq.getSmaPubType());
									ma.setLangDid(seq.getLangDid());
									ma.setLanguage(seq.getLanguage());
									if (au.getDescn()!=null && !au.getDescn().equals("null")) {
										ma.setDescn(au.getDescn());
									} else {
										ma.setDescn("欢迎大家收听"+au.getAudioName());
									}
									if (au.getAudioTags()!=null && !au.getAudioTags().equals("null") && au.getAudioTags().length()>1) {
										ma.setKeyWords(au.getAudioTags());
									}
									ma.setPubCount(1);
									if (au.getDuration()==null || au.getDuration().equals("null")) {
										ma.setTimeLong(10000);
									} else {
										if (au.getDuration().contains(".")) {
											long d1 = (long) (Double.valueOf(au.getDuration())/1);
											long d2 = (long) (Double.valueOf(au.getDuration())%1*1000);
											ma.setTimeLong(d1*1000+d2);
										} else {
											ma.setTimeLong(Long.valueOf(au.getDuration()));
										}
									}
									ma.setMaStatus(1);
									if (au.getcTime()!=null) {
										ma.setCTime(au.getcTime());
									} else {
										ma.setCTime(new Timestamp(System.currentTimeMillis()));
									}
									mediaService.insertMa(ma);
									keyWordService.saveKwAndKeRef(ma.getKeyWords(), "wt_MediaAsset", ma.getId()); //标签与栏目绑定
									mas.put(au.getId(), ma.getId());
									
									//先进行资源与外部系统对照表数据插入
									ResOrgAssetPo roa = new ResOrgAssetPo();
									roa.setId(SequenceUUID.getPureUUID());
									roa.setResId(ma.getId());
									roa.setResTableName("wt_MediaAsset");
									roa.setOrgName(ma.getMaPublisher());
									roa.setOrigId(au.getId());
									roa.setOrigTableName("c_Audio");
									roa.setOrigSrcId(au.getAudioId());
									roa.setcTime(new Timestamp(System.currentTimeMillis()));
									resAssService.insertResOrgAsset(roa);
									
									MaSourcePo maS = new MaSourcePo();
									maS.setId(SequenceUUID.getPureUUID());
									maS.setMaId(ma.getId());
									maS.setIsMain(1);
									maS.setMaSrcType(seq.getSmaPubType());
									maS.setMaSrcId(seq.getSmaPubId());
									maS.setMaSource(seq.getSmaPublisher());
									maS.setPlayURI(au.getAudioURL());
									maS.setDescn(ma.getDescn());
									maS.setCTime(new Timestamp(System.currentTimeMillis()));
									mediaService.insertMas(maS);
									
									try {
										CPlayCountPo countPo = cPlayCountService.getCPlayCountPo(au.getId(), "c_Audio");
										MediaPlayCountPo mecount = new MediaPlayCountPo();
										mecount.setId(SequenceUUID.getPureUUID());
										mecount.setResTableName("wt_MediaAsset");
										mecount.setResId(ma.getId());
										if (countPo!=null) mecount.setPlayCount(countPo.getPlayCount());
										else mecount.setPlayCount(0);
										mecount.setPublisher(al.getAlbumPublisher());
										mecount.setcTime(new Timestamp(System.currentTimeMillis()));
										mediaService.insertMediaPlayCount(mecount);
									} catch (Exception e) {e.printStackTrace();}
								}
							}
						}
					});
				}
			}
			fixedThreadPool.shutdown();
			while (true) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if (fixedThreadPool.isTerminated()) {
					break;
				}
			}
			map.put("auid_maid", mas);
			if (erralaurefs!=null && erralaurefs.size()==0) {
				al.setIsValidate(3);
				albumService.updateAlbum(al);
				return map;
			} else {
				System.out.println("出错专辑    "+al.getId());
				return null;
			}
		} else {
			String smaId = rAssetPo.getResId();
			map.put("smaId", smaId);
			List<AlbumAudioRefPo> audioRefPos = albumAudioRefService.getAlbumAudioRefs(al.getId());
			if (audioRefPos!=null && audioRefPos.size()>0) {
				List<ResOrgAssetPo> rList = resAssService.getResOrgAssetListByAlbumId(al.getId());
				if (rList!=null && rList.size()>0) {
					Map<String, Object> auid_maid = new HashMap<>();
					for (ResOrgAssetPo resOrgAssetPo : rList) {
						auid_maid.put(resOrgAssetPo.getOrigId(), resOrgAssetPo.getResId());
					}
					map.put("auid_maid", auid_maid);
				}
			}
			List<CPersonPo> cPos = cPersonService.getCPersons(al.getAlbumPublisher(), al.getId(), "c_Album");
			List<PersonPo> pos = new ArrayList<>();
			if (cPos!=null && cPos.size()>0) {
				for (CPersonPo cPersonPo : cPos) {
					if (cPersonPo!=null) {
						ResOrgAssetPo resOrgPerson = resAssService.getResOrgAssetPo(cPersonPo.getId(), cPersonPo.getpSource(), "wt_Person");
						if (resOrgPerson!=null) {
							PersonPo po  = personService.getPersonByPersonId(resOrgPerson.getResId());
							if (po!=null) {
								pos.add(po);
							}
						}
					}
				}
			}
			map.put("persons", pos);
			return map;
		}
	}
	
	// TODO
	public static Map<String, Object> convert2SeqMediaToSameAdd (AlbumPo al) {
		loadService();
		if (!matchFiltDB(al)) return null;
		ResOrgAssetPo rAssetPo = resAssService.getResOrgAssetPo(al.getId(), al.getAlbumPublisher(), "wt_SeqMediaAsset");
		if (rAssetPo!=null ) {
			Map<String, Object> map = new HashMap<>();
			ImageHashService imageHashService = (ImageHashService) SpringShell.getBean("imageHashService");
			List<AlbumAudioRefPo> alaurefs = albumAudioRefService.getAlbumAudioRefs(al.getId());
			if (alaurefs==null || alaurefs.size()==0) return null;
			SeqMediaAssetPo seq = mediaService.getSeqInfo(rAssetPo.getResId());
			if (seq==null) return null;
			map.put("smaId", seq.getId());
			
			List<CPersonPo> cPos = cPersonService.getCPersons(al.getAlbumPublisher(), al.getId(), "c_Album");
			List<PersonPo> pos = new ArrayList<>();
			if (cPos!=null && cPos.size()>0) {
				for (CPersonPo cPersonPo : cPos) {
					if (cPersonPo!=null) {
						ResOrgAssetPo resOrgPerson = resAssService.getResOrgAssetPo(cPersonPo.getId(), cPersonPo.getpSource(), "wt_Person");
						if (resOrgPerson!=null) {
							PersonPo po  = personService.getPersonByPersonId(resOrgPerson.getResId());
							if (po!=null) {
								pos.add(po);
							}
						}
					}
				}
			}
			map.put("persons", pos);
			
			//TODO
			Map<String, Object> mas = new HashMap<>();
			ExecutorService fixedThreadPool = Executors.newFixedThreadPool(20);
			for (AlbumAudioRefPo alauref : alaurefs) {
				if (alauref!=null) {
					fixedThreadPool.execute(new Runnable() {
						public void run() {
							AudioPo au = null;
							au = audioService.getAudioInfo(alauref.getAuId());
							if (au!=null) {
								Map<String, Object> m = new HashMap<>();
								m.put("origId", au.getId());
							    m.put("orgName", au.getAudioPublisher());
							    m.put("resTableName", "wt_MediaAsset");
								List<ResOrgAssetPo> ress = resAssService.getResOrgAssetPo(m);
								if (ress==null || ress.size()==0) {
									// 声音数据转换
		    					    MediaAssetPo ma = new MediaAssetPo();
								    ma.setId(SequenceUUID.getPureUUID());
									ma.setMaTitle(au.getAudioName());
									if (au.getAudioPublisher().equals("蜻蜓") || au.getAudioPublisher().equals("多听")) {
										ma.setMaImg(seq.getSmaImg());
									} else {
										String auimgp = au.getAudioImg();
										if (!StringUtils.isNullOrEmptyOrSpace(auimgp) && auimgp.length()>5) {
											String imgpath =null;
											List<ImageHash> imageHashs = imageHashService.getImageHashByImageSrcPath(auimgp, "2");
											if (imageHashs!=null && imageHashs.size()>0) {
												ImageHash imageHash = imageHashs.get(0);
												imgpath = imageHash.getImagePath();
											} else try {imgpath = FileUtils.makeImgFile("2", auimgp);} catch (Exception e) {}
											if (!StringUtils.isNullOrEmptyOrSpace(imgpath)) ma.setMaImg(imgpath);
										}
									}
									ma.setMaURL(au.getAudioURL());
									ma.setMaPubId(seq.getSmaPubId());
									ma.setMaPublisher(seq.getSmaPublisher());
									ma.setMaPubType(seq.getSmaPubType());
									ma.setLangDid(seq.getLangDid());
									ma.setLanguage(seq.getLanguage());
									if (au.getDescn()!=null && !au.getDescn().equals("null")) {
										ma.setDescn(au.getDescn());
									} else {
										ma.setDescn("欢迎大家收听"+au.getAudioName());
									}
									if (au.getAudioTags()!=null && !au.getAudioTags().equals("null") && au.getAudioTags().length()>1) {
										ma.setKeyWords(au.getAudioTags());
									}
									ma.setPubCount(1);
									if (au.getDuration()==null || au.getDuration().equals("null")) {
										ma.setTimeLong(10000);
									} else {
										if (au.getDuration().contains(".")) {
											long d1 = (long) (Double.valueOf(au.getDuration())/1);
											long d2 = (long) (Double.valueOf(au.getDuration())%1*1000);
											ma.setTimeLong(d1*1000+d2);
										} else {
											ma.setTimeLong(Long.valueOf(au.getDuration()));
										}
									}
									ma.setMaStatus(1);
									if (au.getcTime()!=null) {
										ma.setCTime(au.getcTime());
									} else {
										ma.setCTime(new Timestamp(System.currentTimeMillis()));
									}
									
									//先进行资源与外部系统对照表数据插入
									ResOrgAssetPo roa = new ResOrgAssetPo();
									roa.setId(SequenceUUID.getPureUUID());
									roa.setResId(ma.getId());
									roa.setResTableName("wt_MediaAsset");
									roa.setOrgName(ma.getMaPublisher());
									roa.setOrigId(au.getId());
									roa.setOrigTableName("c_Audio");
									roa.setOrigSrcId(au.getAudioId());
									roa.setcTime(new Timestamp(System.currentTimeMillis()));
									resAssService.insertResOrgAsset(roa);
									
									mediaService.insertMa(ma);
									keyWordService.saveKwAndKeRef(ma.getKeyWords(), "wt_MediaAsset", ma.getId()); //标签与栏目绑定
									mas.put(au.getId(), ma.getId());
									
									MaSourcePo maS = new MaSourcePo();
									maS.setId(SequenceUUID.getPureUUID());
									maS.setMaId(ma.getId());
									maS.setIsMain(1);
									maS.setMaSrcType(seq.getSmaPubType());
									maS.setMaSrcId(seq.getSmaPubId());
									maS.setMaSource(seq.getSmaPublisher());
									maS.setPlayURI(au.getAudioURL());
									maS.setDescn(ma.getDescn());
									maS.setCTime(new Timestamp(System.currentTimeMillis()));
									mediaService.insertMas(maS);
									
									try {
										CPlayCountPo countPo = cPlayCountService.getCPlayCountPo(au.getId(), "c_Audio");
										MediaPlayCountPo mecount = new MediaPlayCountPo();
										mecount.setId(SequenceUUID.getPureUUID());
										mecount.setResTableName("wt_MediaAsset");
										mecount.setResId(ma.getId());
										if (countPo!=null) mecount.setPlayCount(countPo.getPlayCount());
										else mecount.setPlayCount(0);
										mecount.setPublisher(al.getAlbumPublisher());
										mecount.setcTime(new Timestamp(System.currentTimeMillis()));
										mediaService.insertMediaPlayCount(mecount);
									} catch (Exception e) {e.printStackTrace();}
								}
							}
						}
					});
				}
			}
			fixedThreadPool.shutdown();
			while (true) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if (fixedThreadPool.isTerminated()) {
					break;
				}
			}
			map.put("auid_maid", mas);
			return map;
		}
		return null;
	}
	
	// TODO
	public static Map<String, Object> convert2SeqMediaToAddMedia (AlbumPo al) {
		loadService();
		Map<String, Object> map = new HashMap<>();
		if (!matchFiltDB(al)) return null;
		ResOrgAssetPo rAssetPo = resAssService.getResOrgAssetPo(al.getId(), al.getAlbumPublisher(), "wt_SeqMediaAsset");
		if (rAssetPo!=null ) {
			ImageHashService imageHashService = (ImageHashService) SpringShell.getBean("imageHashService");
			String smaId = rAssetPo.getResId();
			SeqMediaAssetPo seq = mediaService.getSeqInfo(smaId);
			List<AlbumAudioRefPo> alaurefs = albumAudioRefService.getAlbumAudioRefs(al.getId());
			if (alaurefs==null || alaurefs.size()==0) return null;
			map.put("sma", seq);
			List<CPersonPo> cPos = cPersonService.getCPersons(al.getAlbumPublisher(), al.getId(), "c_Album");
			List<PersonPo> pos = new ArrayList<>();
			if (cPos!=null && cPos.size()>0) {
				for (CPersonPo cPersonPo : cPos) {
					if (cPersonPo!=null) {
						ResOrgAssetPo resOrgPerson = resAssService.getResOrgAssetPo(cPersonPo.getId(), cPersonPo.getpSource(), "wt_Person");
						if (resOrgPerson!=null) {
							PersonPo po = personService.getPersonByPersonId(resOrgPerson.getResId());
							if (po!=null) pos.add(po);
						}
					}
				}
			}
			map.put("persons", pos);
			String chStr = "";
			Map<String, Object> chaMapRefIdMap = new HashMap<>();
			List<ChannelAssetPo> chas = channelService.getChannelAssetListBy(null, seq.getId(), "wt_SeqMediaAsset");
			if (chas!=null && chas.size()>0) {
				for (ChannelAssetPo channelAssetPo : chas) {
					String mref = channelAssetPo.getInRuleIds();
					String[] inRuleIds = mref.split(",");
					if (inRuleIds!=null && inRuleIds.length>0) {
						for (String inStr : inRuleIds) {
							ChannelMapRefPo cMapRefPo = channelService.getChannelMapRef(inStr.replace("wt_ChannelMapRef_", ""));
							if (cMapRefPo!=null && cMapRefPo.getSrcName().equals(seq.getSmaPublisher())) {
								chaMapRefIdMap.put(channelAssetPo.getChannelId(), inStr);
								if (!chStr.contains(channelAssetPo.getChannelId())) {
									chStr += ","+channelAssetPo.getChannelId();
								}
							}
						}
						
					}
				}
			}
			if (chStr.length()>0) {
				chStr = chStr.substring(1);
			}

			Map<String, Object> mas = new HashMap<>();
			ExecutorService fixedThreadPool = Executors.newFixedThreadPool(20);
			for (AlbumAudioRefPo alauref : alaurefs) {
				if (alauref!=null) {
					String chaNameStr = chStr;
					fixedThreadPool.execute(new Runnable() {
						public void run() {
							AudioPo au = null;
							au = audioService.getAudioInfo(alauref.getAuId());
							if (au!=null) {
								Map<String, Object> m = new HashMap<>();
								m.put("origId", au.getId());
							   	m.put("orgName", au.getAudioPublisher());
							   	m.put("resTableName", "wt_MediaAsset");
								List<ResOrgAssetPo> ress = resAssService.getResOrgAssetPo(m);
								if (ress==null || ress.size()==0) {
									// 声音数据转换
		    					   	MediaAssetPo ma = new MediaAssetPo();
								   	ma.setId(SequenceUUID.getPureUUID());
									ma.setMaTitle(au.getAudioName());
									if (au.getAudioPublisher().equals("蜻蜓") || au.getAudioPublisher().equals("多听")) {
										ma.setMaImg(seq.getSmaImg());
									} else {
										String auimgp = au.getAudioImg();
										if (!StringUtils.isNullOrEmptyOrSpace(auimgp) && auimgp.length()>5) {
											String imgpath =null;
											List<ImageHash> imageHashs = imageHashService.getImageHashByImageSrcPath(auimgp, "2");
											if (imageHashs!=null && imageHashs.size()>0) {
												ImageHash imageHash = imageHashs.get(0);
												imgpath = imageHash.getImagePath();
											} else try {imgpath = FileUtils.makeImgFile("2", auimgp);} catch (Exception e) {}
											if (!StringUtils.isNullOrEmptyOrSpace(imgpath)) ma.setMaImg(imgpath);
										}
									}
									ma.setMaURL(au.getAudioURL());
									ma.setMaPubId(seq.getSmaPubId());
									ma.setMaPublisher(seq.getSmaPublisher());
									ma.setMaPubType(seq.getSmaPubType());
									ma.setLangDid(seq.getLangDid());
									ma.setLanguage(seq.getLanguage());
									if (au.getDescn()!=null && !au.getDescn().equals("null")) ma.setDescn(au.getDescn()); 
									else ma.setDescn("欢迎大家收听"+au.getAudioName());
									
									if (au.getAudioTags()!=null && !au.getAudioTags().equals("null") && au.getAudioTags().length()>1) {
										ma.setKeyWords(au.getAudioTags());
									}
									ma.setPubCount(1);
									if (au.getDuration()==null || au.getDuration().equals("null")) {
										ma.setTimeLong(10000);
									} else {
										if (au.getDuration().contains(".")) {
											long d1 = (long) (Double.valueOf(au.getDuration())/1);
											long d2 = (long) (Double.valueOf(au.getDuration())%1*1000);
											ma.setTimeLong(d1*1000+d2);
										} else {
											ma.setTimeLong(Long.valueOf(au.getDuration()));
										}
									}
									ma.setMaStatus(1);
									try {
										ma.setMaPublishTime(au.getPubTime());
									} catch (Exception e) {}
									if (au.getcTime()!=null) {
										ma.setCTime(au.getcTime());
									} else ma.setCTime(new Timestamp(System.currentTimeMillis()));
									
									//先进行资源与外部系统对照表数据插入
									ResOrgAssetPo roa = new ResOrgAssetPo();
									roa.setId(SequenceUUID.getPureUUID());
									roa.setResId(ma.getId());
									roa.setResTableName("wt_MediaAsset");
									roa.setOrgName(ma.getMaPublisher());
									roa.setOrigId(au.getId());
									roa.setOrigTableName("c_Audio");
									roa.setOrigSrcId(au.getAudioId());
									roa.setcTime(new Timestamp(System.currentTimeMillis()));
									
									boolean isCon = true;
									try {
										resAssService.insertResOrgAsset(roa);
									} catch (Exception e) {
										ress = resAssService.getResOrgAssetPo(m);
										isCon = false;
									}
									
									if (isCon) {
										mediaService.insertMa(ma);
										keyWordService.saveKwAndKeRef(ma.getKeyWords(), "wt_MediaAsset", ma.getId()); //标签与栏目绑定
										mas.put(au.getId(), ma.getId());
											
										MaSourcePo maS = new MaSourcePo();
										maS.setId(SequenceUUID.getPureUUID());
										maS.setMaId(ma.getId());
										maS.setIsMain(1);
										maS.setMaSrcType(seq.getSmaPubType());
										maS.setMaSrcId(seq.getSmaPubId());
										maS.setMaSource(seq.getSmaPublisher());
										maS.setPlayURI(au.getAudioURL());
										maS.setDescn(ma.getDescn());
										maS.setCTime(new Timestamp(System.currentTimeMillis()));
										mediaService.insertMas(maS);
										
										try {
											CPlayCountPo countPo = cPlayCountService.getCPlayCountPo(au.getId(), "c_Audio");
											MediaPlayCountPo mecount = new MediaPlayCountPo();
											mecount.setId(SequenceUUID.getPureUUID());
											mecount.setResTableName("wt_MediaAsset");
											mecount.setResId(ma.getId());
											if (countPo!=null) mecount.setPlayCount(countPo.getPlayCount());
											else mecount.setPlayCount(0);
											mecount.setPublisher(al.getAlbumPublisher());
											mecount.setcTime(new Timestamp(System.currentTimeMillis()));
											mediaService.insertMediaPlayCount(mecount);
										} catch (Exception e) {e.printStackTrace();}
										
										SeqMaRefPo seqMaRef = new SeqMaRefPo();
										seqMaRef.setId(SequenceUUID.getPureUUID());
										seqMaRef.setsId(seq.getId());
										seqMaRef.setmId(ma.getId());
										seqMaRef.setColumnNum(alauref.getColumnNum());
										seqMaRef.setDescn(ma.getDescn());
										seqMaRef.setIsMain(alauref.getIsMain());
										seqMaRef.setcTime(new Timestamp(System.currentTimeMillis()));
										mediaService.insertSeqRef(seqMaRef);
										
										if (alauref.getIsMain()==1 && pos!=null && pos.size()>0) {
											for (PersonPo po : pos) {
												PersonRefPo pf = personService.getPersonRefBy(po.getId(), "wt_MediaAsset", ma.getId());
												if (pf==null) {
													pf = new PersonRefPo();
													pf.setId(SequenceUUID.getPureUUID());
													pf.setRefName("主播");
													pf.setResTableName("wt_MediaAsset");
													pf.setResId(ma.getId());
													pf.setPersonId(po.getId());
													pf.setcTime(new Timestamp(System.currentTimeMillis()));
													personService.insertPersonRef(pf);
												}
											}
										}
										
										if (chaNameStr.length()>0) {
											String[] chasStr = chaNameStr.split(",");
											if (chasStr.length>0) {
												for (String chaStr : chasStr) {
													System.out.println(chaStr);
													DictRefResPo dictRefRes = new DictRefResPo();
											        dictRefRes.setId(SequenceUUID.getPureUUID());
											        dictRefRes.setRefName("专辑-内容分类");
											        dictRefRes.setResTableName("wt_MediaAsset");
											        dictRefRes.setResId(ma.getId());
											        dictRefRes.setDictMid("3");
										            dictRefRes.setDictDid(chaStr);
										            dictRefRes.setCTime(new Timestamp(System.currentTimeMillis()));
										            dictService.insertDictRef(dictRefRes);
										                
										            ChannelAssetPo cha = new ChannelAssetPo();
												    cha.setId(SequenceUUID.getPureUUID());
													cha.setAssetType("wt_MediaAsset");
													cha.setAssetId(ma.getId());
													cha.setPublisherId(seq.getSmaPubId());
													cha.setIsValidate(1);  // 设为无效
													cha.setCheckerId("1");
													cha.setPubName(ma.getMaTitle());
													cha.setPubImg(ma.getMaImg());
													cha.setSort(0);
													cha.setFlowFlag(2);
													cha.setInRuleIds(chaMapRefIdMap.get(chaStr).toString());
													cha.setCheckRuleIds("etl");
													cha.setCTime(new Timestamp(System.currentTimeMillis()));
													cha.setPubTime(au.getPubTime());
													cha.setChannelId(chaStr);
													channelService.insertChannelAsset(cha);
													keyWordService.saveKwAndKeRef(ma.getKeyWords(), "wt_Channel", cha.getChannelId()); //标签与栏目绑定
												}
											}
										} else {
											DictRefResPo dictref = new DictRefResPo();
											dictref.setId(SequenceUUID.getPureUUID());
											dictref.setRefName("专辑-内容分类");
											dictref.setResTableName("wt_MediaAsset");
											dictref.setResId(ma.getId());
									        dictref.setDictMid("3");
									        dictref.setDictDid("cn36");
									        dictref.setCTime(new Timestamp(System.currentTimeMillis()));
									        dictService.insertDictRef(dictref);
							            
							                ChannelAssetPo cha = new ChannelAssetPo();
											cha.setId(SequenceUUID.getPureUUID());
											cha.setAssetType("wt_MediaAsset");
											cha.setAssetId(ma.getId());
											cha.setPublisherId(seq.getSmaPubId());
											cha.setIsValidate(1);  // 设为无效
											cha.setCheckerId("1");
											cha.setPubName(ma.getMaTitle());
											cha.setPubImg(ma.getMaImg());
											cha.setSort(0);
											cha.setFlowFlag(2);
											cha.setInRuleIds("wt_ChannelMapRef_null");
											cha.setCheckRuleIds("etl");
											cha.setCTime(new Timestamp(System.currentTimeMillis()));
											cha.setPubTime(au.getPubTime());
											cha.setChannelId("cn36");
											channelService.insertChannelAsset(cha);
										}
									}
								}
								
								if (ress!=null && ress.size()>0) {
									String maId = ress.get(0).getResId();
									MediaAssetPo ma = mediaService.getMaInfoById(maId);
									if (ma!=null) {
										SeqMaRefPo seqMaRefPo = mediaService.getSeqMaRefBy(seq.getId(), maId);
										if (seqMaRefPo!=null) {
											if (seqMaRefPo.getColumnNum()!=alauref.getColumnNum()) {
												seqMaRefPo.setColumnNum(alauref.getColumnNum());
												mediaService.updateSeqMaRef(seqMaRefPo);
											}
										}
									}
								}
							}
						}
					});
				}
			}
			fixedThreadPool.shutdown();
			while (true) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if (fixedThreadPool.isTerminated()) {
					break;
				}
			}
		}
		return map;
	}
	
	@SuppressWarnings("unchecked")
	public static void makeSameAlbumToAddAudio(AlbumPo albumPo, String smaId) {
		loadService();
		Map<String, Object> mas = null;
		mas = ConvertUtils.convert2SeqMediaToSameAdd(albumPo);
		if (mas == null) return;
		SeqMediaAssetPo sma = mediaService.getSeqInfo(smaId);
		if (sma != null) {
			List<OrganizePo> oganlist = organizeService.getOrganizeList();
			String sameSmaId = mas.get("smaId").toString();
			List<PersonPo> persons = (List<PersonPo>) mas.get("persons");
			Map<String, Object> auid_maid = (Map<String, Object>) mas.get("auid_maid");
			String orgId = null;
			for (OrganizePo organs : oganlist) {
				if (organs.getOrgName().equals(albumPo.getAlbumPublisher())) {
					orgId = organs.getId();
					break;
				}
			}
			CompareAttribute cAttribute = new CompareAttribute();
			Map<String, Object> exmaIdMap = new HashMap<>();
			if (auid_maid!=null && auid_maid.size()>0) {
				Set<String> sets = auid_maid.keySet();
				for (String set : sets) {
					Map<String, Object> similarMap = cAttribute.getSolrListToCompareMedia(smaId, auid_maid.get(set).toString());
					if (similarMap!=null) {
						String maId = similarMap.get("perId").toString();
						String samemaId = similarMap.get("maId").toString();
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
				}
			}

			int columnNum = 0;
			List<DictRefPo> cdRefPos = crawlerDictService.getDictRefs(albumPo.getId(), "c_Album", null);
			String chaNameStr = "";
			List<ChannelMapRefPo> chaMapRefs = channelService.getChannelMapRefList(null, null, 1);
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
			if (smaref != null) columnNum = smaref.getColumnNum();
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
					
					String playUri = null;
					List<MaSourcePo> maSourcePos = mediaService.getMaSources(mediaAssetPo.getId());
					if (maSourcePos!=null && maSourcePos.size()>0) {
						for (MaSourcePo maSourcePo : maSourcePos) {
							if (maSourcePo.getIsMain()==1) {
								playUri = maSourcePo.getPlayURI();
							}
						}
					}
					if (playUri==null) continue;
					if(mediaService.getSeqMaRefBySIdAndMIdOrPlayUrl(seqMaRefPo.getsId(), seqMaRefPo.getmId(), playUri)!=null) continue;
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
						if (chaNameStr.lastIndexOf(",") == 0) chaNameStr = chaNameStr.substring(1);
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
								if (dictService.getDictRefs(dictRefRes.getDictDid(), dictRefRes.getResId(), dictRefRes.getResTableName()) == null) {
									try {
										dictService.insertDictRef(dictRefRes);
									} catch (Exception e) {}
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
								List<ChannelAssetPo> chass = channelService.getChannelAssetListBy(cha.getChannelId(), cha.getAssetId(), cha.getAssetType());
								if (chass != null && chass.size() > 0) {
									for (ChannelAssetPo channelAssetPo : chass) {
										String inRuleIds = channelAssetPo.getInRuleIds();
										inRuleIds += "," + cha.getInRuleIds();
										channelAssetPo.setInRuleIds(inRuleIds);
										channelService.updateChannelAsset(channelAssetPo);
									}
								} else try {channelService.insertChannelAsset(cha);} catch (Exception e) {}
								keyWordService.saveKwAndKeRef(mediaAssetPo.getKeyWords(), "wt_Channel", cha.getChannelId()); // 标签与栏目绑定
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
								if (dictService.getDictRefs(dictref.getDictDid(), dictref.getResId(), dictref.getResTableName()) == null) {
									try {
										dictService.insertDictRef(dictref);
									} catch (Exception e) {}
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
								List<ChannelAssetPo> chass = channelService.getChannelAssetListBy(cha.getChannelId(), cha.getAssetId(), cha.getAssetType());
								if (chass != null && chass.size() > 0) {
									for (ChannelAssetPo _channelAssetPo : chass) {
										String inRuleIds = _channelAssetPo.getInRuleIds();
										inRuleIds += "," + cha.getInRuleIds();
										_channelAssetPo.setInRuleIds(inRuleIds);
										channelService.updateChannelAsset(_channelAssetPo);
									}
								} else try {channelService.insertChannelAsset(cha);} catch (Exception e) {}
							}
						}
					}
				}
			}
			new AddCacheDBInfoThread(smaId).start();
			new SolrUpdateThread(sma).start();
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
		sameDBService = (SameDBService) SpringShell.getBean("sameDBService");
		organizeService = (OrganizeService) SpringShell.getBean("organizeService");
	}
	
	private static boolean matchFiltDB (AlbumPo albumPo) {
		if (albumPo==null) return false;
		if (albumPo!=null) {
			int num = albumAudioRefService.getAlbumAudioRefNum(albumPo.getId());
			if (num == 0) return false;
			String albumTitle = albumPo.getAlbumName();
			if (filtStr!=null && filtStr.length>0) {
				for (String filt : filtStr) {
					if (albumTitle.contains(filt)) {
						String replacName = albumTitle.replace(filt, "");
						if (replacName.length()==0) return false;
						if (replacName.length()/filt.length() < 2) return false;
						if (num < 5) return false;
					}
				}
			}
		}
		return true;
	}
	
//	private static boolean matchFiltDB (String maTitle) {
//		if (maTitle!=null) {
//			if (filtStr!=null && filtStr.length>0) {
//				for (String filt : filtStr) {
//					if (maTitle.contains(filt)) {
//						String replacName = maTitle.replace(filt, "");
//						if (replacName.length()==0) return false;
//						if (replacName.length()/filt.length() < 3) return false;
//					}
//				}
//			}
//		}
//		return true;
//	}
}
