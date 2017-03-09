package com.woting.crawler.scheme.crawlerdb.crawler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.spiritdata.framework.core.cache.SystemCache;
import com.spiritdata.framework.util.SequenceUUID;
import com.woting.cm.core.ResOrgAsset.persis.po.ResOrgAssetPo;
import com.woting.cm.core.ResOrgAsset.service.ResOrgAssetService;
import com.woting.cm.core.channel.persis.po.ChannelAssetPo;
import com.woting.cm.core.channel.service.ChannelService;
import com.woting.cm.core.dict.persis.po.DictRefResPo;
import com.woting.cm.core.dict.service.DictService;
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
import com.woting.crawler.CrawlerConstants;
import com.woting.crawler.compare.CompareAttribute;
import com.woting.crawler.core.album.persis.po.AlbumPo;
import com.woting.crawler.core.album.service.AlbumService;
import com.woting.crawler.core.audio.persis.po.AudioPo;
import com.woting.crawler.core.audio.service.AudioService;
import com.woting.crawler.core.cperson.persis.po.CPersonPo;
import com.woting.crawler.core.cperson.service.CPersonService;
import com.woting.crawler.ext.SpringShell;
import com.woting.crawler.scheme.crawlerdb.qt.QTCrawler;
import com.woting.crawler.scheme.utils.ConvertUtils;
import com.woting.crawler.scheme.utils.FileUtils;
import com.woting.crawler.scheme.utils.ShareHtml;


public class EtlProcess {
	private AlbumService albumService;
	private AudioService audioService;
	private ChannelService channelService;
	private MediaService mediaService;
	private ResOrgAssetService resAssService;
	private DictService dictService;
	private PersonService personService;
	private CPersonService cPersonService;
	private OrganizeService organizeService;
	
	public EtlProcess() {
		FileUtils.readFileByJson(SystemCache.getCache(CrawlerConstants.APP_PATH).getContent() + "conf/craw.txt");
		albumService = (AlbumService) SpringShell.getBean("albumService");
		audioService = (AudioService) SpringShell.getBean("audioService");
		channelService = (ChannelService) SpringShell.getBean("channelService");
		audioService = (AudioService) SpringShell.getBean("audioService");
		mediaService = (MediaService) SpringShell.getBean("mediaService");
		resAssService = (ResOrgAssetService) SpringShell.getBean("resOrgAssetService");
		dictService = (DictService) SpringShell.getBean("dictService");
		personService = (PersonService) SpringShell.getBean("personService");
		cPersonService = (CPersonService) SpringShell.getBean("CPersonService");
		channelService.getChannelList();
		organizeService = (OrganizeService) SpringShell.getBean("organizeService");
	}
	
	public void makeDatas() {
//		XMLYCrawler xmlyCrawler = new XMLYCrawler();
//		xmlyCrawler.beginCrawler();
//		try {
//			ExecutorService fixedThreadPool = Executors.newFixedThreadPool(2);
//			fixedThreadPool.execute(new Runnable() {
//				public void run() {
					QTCrawler qtCrawler = new QTCrawler();
					qtCrawler.beginCrawler();
//				}
//			});
//			fixedThreadPool.execute(new Runnable() {
//				public void run() {
//					XMLYCrawler xmlyCrawler = new XMLYCrawler();
//					xmlyCrawler.beginCrawler();
//				}
//			});
//			fixedThreadPool.shutdown();
//			while (true) {
//				Thread.sleep(10000);
//				if (fixedThreadPool.isTerminated()) {
//					break;
//				}
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		
	}
	
	public void makeNewAlbums(Map<String, Object> newmap) {
		if (newmap!=null && newmap.size()>0) {
			Set<String> sets = newmap.keySet();
			for (String albumId : sets) {
				try {
					String id = (String) newmap.get(albumId);
					makeNewAlbum(id);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public void makeNewAlbum(String id) {
		List<AudioPo> aulist = null;
		AlbumPo al = albumService.getAlbumInfo(id);
		if (al!=null) {
			aulist = audioService.getAudioListByAlbumId(al.getAlbumId(), al.getAlbumPublisher(), al.getCrawlerNum());
			al.setAudioPos(aulist);
			CompareAttribute cAttribute = new CompareAttribute(al.getCrawlerNum());
			cAttribute.getSolrListToCompare(al);
//			CompareAttribute com = new 
//			Map<String, Object> smamap = ConvertUtils.convert2SeqMediaAssetNew(al, cate2dictdlist, chlist);
//			if (smamap==null) {
//				return;
//			}
//			List<SeqMediaAssetPo> seqlist = new ArrayList<SeqMediaAssetPo>();
//			List<MediaAssetPo> malist = new ArrayList<MediaAssetPo>();
//			List<ResOrgAssetPo> resAss = new ArrayList<ResOrgAssetPo>();
//			List<MaSourcePo> maslist = new ArrayList<MaSourcePo>();
//			List<DictRefResPo> dictreflist = new ArrayList<DictRefResPo>();
//			List<ChannelAssetPo> chalist = new ArrayList<ChannelAssetPo>();
//			List<SeqMaRefPo> seqreflist = new ArrayList<SeqMaRefPo>();
//			List<MediaPlayCountPo> mecounts = new ArrayList<MediaPlayCountPo>();
//			List<PersonRefPo> pfs = new ArrayList<>();
//			
//			SeqMediaAssetPo se = (SeqMediaAssetPo) smamap.get("seq");
//			seqlist.add(se);
//			mediaService.insertSeqList(seqlist);
//			// 保存主播信息
//			CPersonPo cps = cPersonService.getCPerson(se.getSmaPublisher(), al.getAlbumId(), "c_Album");
//			if (cps != null) {
//				PersonPo po = ConvertUtils.convert2Person(cps);
//				personService.insertPerson(po);
//				PersonRefPo pf = new PersonRefPo();
//				pf.setId(SequenceUUID.getPureUUID());
//				pf.setPersonId(po.getId());
//				pf.setRefName("主播");
//				pf.setResId(se.getId());
//				pf.setResTableName("wt_SeqMediaAsset");
//				pf.setcTime(new Timestamp(System.currentTimeMillis()));
//				personService.insertPersonRef(pf);
//				DictRefResPo dictRefResPo = new DictRefResPo();
//				dictRefResPo.setId(SequenceUUID.getPureUUID());
//				dictRefResPo.setRefName("主播-性别");
//				dictRefResPo.setDictMid("8");
//				if (cps.getSex() == 0) {
//					dictRefResPo.setDictDid("xb003");
//				} else if (cps.getSex() == 1) {
//					dictRefResPo.setDictDid("xb001");
//				} else if (cps.getSex() == 2) {
//					dictRefResPo.setDictDid("xb002");
//				}
//				dictRefResPo.setResTableName("wt_Person");
//				dictRefResPo.setResId(po.getId());
//				dictRefResPo.setCTime(new Timestamp(System.currentTimeMillis()));
//				dictService.insertDictRef(dictRefResPo);
//				if (!StringUtils.isNullOrEmptyOrSpace(cps.getLocation())) {
//					String[] lco = cps.getLocation().split("_");
//					DictDetailPo ddpo = dictService.getDictDetail("2", "0", lco[0]);
//					if (ddpo != null) {
//						if (lco.length >= 2) {
//							DictDetailPo ddpo2 = dictService.getDictDetail("2", ddpo.getId(), lco[1]);
//							if (ddpo2 != null) {
//								dictRefResPo.setId(SequenceUUID.getPureUUID());
//								dictRefResPo.setRefName("主播-地区");
//								dictRefResPo.setDictMid("2");
//								dictRefResPo.setDictDid(ddpo2.getId());
//								dictRefResPo.setResTableName("wt_Person");
//								dictRefResPo.setResId(po.getId());
//								dictRefResPo.setCTime(new Timestamp(System.currentTimeMillis()));
//								dictService.insertDictRef(dictRefResPo);
//							}
//						} else {
//							dictRefResPo.setId(SequenceUUID.getPureUUID());
//							dictRefResPo.setRefName("主播-地区");
//							dictRefResPo.setDictMid("2");
//							dictRefResPo.setDictDid(ddpo.getId());
//							dictRefResPo.setResTableName("wt_Person");
//							dictRefResPo.setResId(po.getId());
//							dictRefResPo.setCTime(new Timestamp(System.currentTimeMillis()));
//							dictService.insertDictRef(dictRefResPo);
//						}
//					}
//				}
//			}
//			dictreflist.addAll((List<DictRefResPo>) smamap.get("dictref"));
//			chalist.addAll((List<ChannelAssetPo>) smamap.get("cha"));
//			if (smamap.containsKey("playnum")) {
//				mecounts.add((MediaPlayCountPo) smamap.get("playnum"));
//			}
//			ResOrgAssetPo resass = new ResOrgAssetPo();
//			resass.setId(SequenceUUID.getPureUUID());
//			resass.setResId(se.getId());
//			resass.setResTableName("wt_SeqMediaAsset");
//			resass.setOrgName(se.getSmaPublisher());
//			resass.setOrigId(al.getId());
//			resass.setOrigTableName("c_Album");
//			resass.setOrigSrcId(al.getAlbumId());
//			resass.setcTime(new Timestamp(System.currentTimeMillis()));
//			resAss.add(resass);
//			saveContents(malist, resAss, maslist, seqreflist, mecounts, dictreflist, chalist, pfs);
//
//			// 获取抓取到的专辑下级节目信息
//			if (aulist == null) {
//				aulist = audioService.getAudioListByAlbumId(al.getAlbumId(), al.getAlbumPublisher(), al.getCrawlerNum());
//			}
//			if (aulist!=null && aulist.size() > 0) {
//				Map<String, Object> mall = ConvertUtils.convert2MediaAsset(aulist, se, (List<DictRefResPo>) smamap.get("dictref"), (List<ChannelAssetPo>) smamap.get("cha"));
//				if (mall != null && mall.containsKey("malist")) {
//					malist = (List<MediaAssetPo>) mall.get("malist");
//					resAss = (List<ResOrgAssetPo>) mall.get("resAss");
//					maslist = (List<MaSourcePo>) mall.get("maslist");
//					dictreflist = (List<DictRefResPo>) mall.get("dictreflist");
//					chalist = (List<ChannelAssetPo>) mall.get("chalist");
//					seqreflist = (List<SeqMaRefPo>) mall.get("seqmareflist");
//					if (mall.containsKey("mediaplaycount")) {
//						mecounts = (List<MediaPlayCountPo>) mall.get("mediaplaycount");
//					}
//					pfs = (List<PersonRefPo>) mall.get("personRef");
//				}
//				if (malist.size() > 0) {
//					saveContents(malist, resAss, maslist, seqreflist, mecounts, dictreflist, chalist, pfs);
//				} 
////				new ShareHtml(se.getId(), "SEQU").start();
//			}
//			aulist = null;
		}
	}
	
	@SuppressWarnings("unchecked")
	public void makeSameAlbum(AlbumPo albumPo, String smaId, List<Map<String, Object>> ls) {
		SeqMediaAssetPo sma = mediaService.getSeqInfo(smaId);
		if (sma!=null && ls!=null && ls.size()>0) {
			List<OrganizePo> oganlist = organizeService.getOrganizeList();
			ResOrgAssetPo resOrgAssetsma = new ResOrgAssetPo();
			resOrgAssetsma.setId(SequenceUUID.getPureUUID());
			resOrgAssetsma.setResTableName("wt_SeqMediaAsset");
			resOrgAssetsma.setResId(smaId);
			resOrgAssetsma.setOrgName(albumPo.getAlbumPublisher());
			resOrgAssetsma.setOrigId(albumPo.getId());
			resOrgAssetsma.setOrigTableName("c_Album");
			resOrgAssetsma.setOrigSrcId(albumPo.getAlbumId());
			resAssService.insertResOrgAsset(resOrgAssetsma);
			CPersonPo cPersonPo = cPersonService.getCPerson(albumPo.getAlbumPublisher(), albumPo.getAlbumId(), "c_Album");
			PersonPo personPo = null;
			String orgId = null;
			if (cPersonPo!=null) {
				List<PersonPo> pers = personService.getPersonsByResIdAndResTableName(albumPo.getId(), "wt_SeqMediaAsset");
				boolean isExist = true;
				if (pers!=null && pers.size()>0) {
					for (PersonPo per : pers) {
						if (cPersonPo.getpName().equals(per.getpName()) && cPersonPo.getpSource().equals(per.getpSource())) {
							isExist = false;
						}
					}
				}
				if (isExist) {
					Map<String, Object> pm = new HashMap<>();
					pm.put("origId",cPersonPo.getId());
					pm.put("origTableName", "c_Person");
					List<ResOrgAssetPo> resOrgAssetPos = resAssService.getResOrgAssetPo(pm);
					if (resOrgAssetPos!=null && resOrgAssetPos.size()>0) {
						ResOrgAssetPo resOrgAssetper = resOrgAssetPos.get(0);
						personPo = personService.getPersonByPersonId(resOrgAssetper.getResId());
						if (personPo!=null) {
							PersonRefPo personRefPo = new PersonRefPo();
							personRefPo.setId(SequenceUUID.getPureUUID());
							personRefPo.setPersonId(personPo.getId());
							personRefPo.setRefName("主播-专辑");
							personRefPo.setResId(sma.getId());
							personRefPo.setResTableName("wt_SeqMediaAsset");
							personService.insertPersonRef(personRefPo);
						}
					}
				} else {
					personPo = ConvertUtils.convert2Person(cPersonPo);
					if (personPo!=null) {
						personService.insertPerson(personPo);
						ResOrgAssetPo resOrgAssetper = new ResOrgAssetPo();
						resOrgAssetper.setId(SequenceUUID.getPureUUID());
						resOrgAssetper.setOrgName(personPo.getpSource());
						resOrgAssetper.setOrigId(cPersonPo.getId());
						resOrgAssetper.setOrigTableName("cPerson");
						resOrgAssetper.setOrigSrcId(cPersonPo.getId());
						resOrgAssetper.setResId(personPo.getId());
						resOrgAssetper.setResTableName("wt_Person");
						resAssService.insertResOrgAsset(resOrgAssetper);
						
						PersonRefPo personRefPo = new PersonRefPo();
						personRefPo.setId(SequenceUUID.getPureUUID());
						personRefPo.setPersonId(personPo.getId());
						personRefPo.setRefName("主播-专辑");
						personRefPo.setResId(sma.getId());
						personRefPo.setResTableName("wt_SeqMediaAsset");
						personService.insertPersonRef(personRefPo);
					}
				}
			}
			
			List<AudioPo> aus = albumPo.getAudioPos();
			Iterator<AudioPo> its = aus.iterator();
			for (OrganizePo organs : oganlist) {
				if (organs.getOrgName().equals(albumPo.getAlbumPublisher())) {
					orgId = organs.getId();
					break;
				}
			}
			for (Map<String, Object> m : ls) {
				String maId = m.get("perId").toString();
				String id = m.get("audioId").toString();
				while (its.hasNext()) {
					AudioPo audioPo = (AudioPo) its.next();
					if (audioPo.getId().equals(id)) {
						List<MaSourcePo> masls = mediaService.getMaSources(maId);
						if (masls!=null && masls.size()>0) {
							MaSourcePo mas = new MaSourcePo();
							mas.setId(SequenceUUID.getPureUUID());
							mas.setMaId(maId);
							mas.setMaSrcType(1);
							for (OrganizePo organs : oganlist) {
								if (organs.getOrgName().equals(audioPo.getAudioPublisher())) {
									mas.setMaSource(organs.getOrgName());
									mas.setMaSrcId(organs.getId());
									
									break;
								}
							}
							mas.setIsMain(0);
							mas.setPlayURI(audioPo.getAudioURL());
							mas.setMaSrcType(0);
							mas.setDescn(audioPo.getDescn());
							mediaService.insertMas(mas);
							ResOrgAssetPo resOrgAsset = new ResOrgAssetPo();
							resOrgAsset.setId(SequenceUUID.getPureUUID());
							resOrgAsset.setOrgName(audioPo.getAudioPublisher());
							resOrgAsset.setResTableName("wt_MediaAsset");
							resOrgAsset.setResId(maId);
							resOrgAsset.setOrigId(audioPo.getId());
							resOrgAsset.setOrigTableName("c_Audio");
							resOrgAsset.setOrigSrcId(audioPo.getAudioId());
							resAssService.insertResOrgAsset(resOrgAsset);
							if (personPo!=null) {
								PersonRefPo personRefPo = new PersonRefPo();
								personRefPo.setId(SequenceUUID.getPureUUID());
								personRefPo.setPersonId(personPo.getId());
								personRefPo.setRefName("主播-节目");
								personRefPo.setResId(maId);
								personRefPo.setResTableName("wt_MediaAsset");
								personService.insertPersonRef(personRefPo);
							}
						}
						its.remove();
					}
				}
			}
			
			if (aus!=null && aus.size()>0) {
				//TODO
				SeqMaRefPo smaref = mediaService.getOneSmarefOrderByColumnNum(smaId);
				if (smaref!=null) {
					int maxnum = smaref.getColumnNum();
					List<MediaAssetPo> malist = new ArrayList<MediaAssetPo>();
					List<ResOrgAssetPo> resAss = new ArrayList<ResOrgAssetPo>();
					List<MaSourcePo> maslist = new ArrayList<MaSourcePo>();
					List<DictRefResPo> dictreflist = new ArrayList<DictRefResPo>();
					List<ChannelAssetPo> chalist = new ArrayList<ChannelAssetPo>();
					List<SeqMaRefPo> seqreflist = new ArrayList<SeqMaRefPo>();
					List<MediaPlayCountPo> mecounts = new ArrayList<MediaPlayCountPo>();
					List<PersonRefPo> pfs = new ArrayList<>();
					Map<String, Object> param = new HashMap<>();
					param.put("assetId", sma.getId());
					param.put("assetType", "wt_SeqMediaAsset");
					List<ChannelAssetPo> chas = channelService.getChannelAssetListBy(param);
					List<DictRefResPo> dictrefs = dictService.getDictRefs(smaId, "wt_SeqMediaAsset");
					Map<String, Object> mall = ConvertUtils.convert2MediaAsset(aus, sma, orgId, maxnum, chas, dictrefs);
					if (mall != null && mall.containsKey("malist")) {
					    malist = (List<MediaAssetPo>) mall.get("malist");
					    resAss = (List<ResOrgAssetPo>) mall.get("resAss");
					    maslist = (List<MaSourcePo>) mall.get("maslist");
					    dictreflist = (List<DictRefResPo>) mall.get("dictreflist");
					    chalist = (List<ChannelAssetPo>) mall.get("chalist");
					    seqreflist = (List<SeqMaRefPo>) mall.get("seqmareflist");
					    if (mall.containsKey("mediaplaycount")) {
						    mecounts = (List<MediaPlayCountPo>) mall.get("mediaplaycount");
					    }
					    pfs = (List<PersonRefPo>) mall.get("personRef");
				    }
				    if (malist.size() > 0) {
					    try {
						    saveContents(malist, resAss, maslist, seqreflist, mecounts, dictreflist, chalist, pfs);
					    } catch (Exception e) {
						    e.printStackTrace();
					    }
				    }
				    new ShareHtml(sma.getId(), "SEQU").start();
				}
			}
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
}
