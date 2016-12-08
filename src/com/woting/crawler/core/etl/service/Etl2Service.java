package com.woting.crawler.core.etl.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.spiritdata.framework.core.cache.SystemCache;
import com.spiritdata.framework.util.SequenceUUID;
import com.woting.cm.core.ResOrgAsset.persis.po.ResOrgAssetPo;
import com.woting.cm.core.ResOrgAsset.service.ResOrgAssetService;
import com.woting.cm.core.channel.persis.po.ChannelAssetPo;
import com.woting.cm.core.channel.persis.po.ChannelPo;
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
import com.woting.crawler.compare.Distinct;
import com.woting.crawler.core.album.model.Album;
import com.woting.crawler.core.album.persis.po.AlbumPo;
import com.woting.crawler.core.audio.persis.po.AudioPo;
import com.woting.crawler.core.audio.service.AudioService;
import com.woting.crawler.core.cperson.persis.po.CPersonPo;
import com.woting.crawler.core.cperson.service.CPersonService;
import com.woting.crawler.core.etl.model.Etl2Process;
import com.woting.crawler.ext.SpringShell;
import com.woting.crawler.scheme.utils.ConvertUtils;
import com.woting.crawler.scheme.utils.FileUtils;

@Service
public class Etl2Service {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private Distinct distinct;
	private AudioService audioService;
	private MediaService mediaService;
	private ChannelService channelService;
	private ResOrgAssetService resAssService;
	private DictService dictService;
	private List<ChannelPo> chlist;
	private OrganizeService oganService;
	private PersonService personService;
	private CPersonService cPersonService;
	private List<Map<String, Object>> cate2dictdlist = new ArrayList<Map<String, Object>>();

	@SuppressWarnings("unchecked")
	public void getDictAndCrawlerDict(Etl2Process etl2Process) {
		cate2dictdlist = FileUtils
				.readFileByJson(SystemCache.getCache(CrawlerConstants.APP_PATH).getContent() + "conf/craw.txt");
		audioService = (AudioService) SpringShell.getBean("audioService");
		mediaService = (MediaService) SpringShell.getBean("mediaService");
		resAssService = (ResOrgAssetService) SpringShell.getBean("resOrgAssetService");
		channelService = (ChannelService) SpringShell.getBean("channelService");
		dictService = (DictService) SpringShell.getBean("dictService");
		personService = (PersonService) SpringShell.getBean("personService");
		cPersonService = (CPersonService) SpringShell.getBean("CPersonService");
		chlist = channelService.getChannelList();
		distinct = new Distinct();

		Map<String, Object> m = new HashMap<>();
		// 删除本次抓取中间库里专辑和单体重复信息
		distinct.removeSameAlbumAndAudio(etl2Process.getEtlnum());
		// 删除中间库无下级的专辑信息
		distinct.removeAlbumNoAudio(etl2Process.getEtlnum());
		// 专辑与Redis快照对比
		m = distinct.compareRedisByAlbum(etl2Process.getEtlnum());
		List<AlbumPo> oldlist = (List<AlbumPo>) m.get("oldlist"); // Redis里存在的专辑列表
		List<AlbumPo> newlist = (List<AlbumPo>) m.get("newlist"); // Redis不存在的专辑列表
		List<Album> oldals = new ArrayList<>();
		// 声音跟中间库对比
		oldals = distinct.compareRedisByAudio(oldlist); // 覆盖oldlist
		// 专辑与资源库对比
		m = distinct.compareCMByAlbum(newlist);
		oldals.addAll((List<Album>) m.get("oldlist"));
		newlist = (List<AlbumPo>) m.get("newlist"); // 待入库专辑列表
		// 声音跟资源库对比
		oldals = distinct.compareCMByAudio(oldals); // 专辑帮顶下的声音待入库
		// 新增资源库已存在的专辑下级声音
		makeExistAlbums(oldals);
		// 进行多平台资源消重
		m = distinct.comparePublisherSrc(newlist, etl2Process.getEtlnum());
		List<Map<String, Object>> samelist = (List<Map<String, Object>>) m.get("samelist");
		// 处理相似专辑数据
		makeSameAlbums(samelist);
		newlist = (List<AlbumPo>) m.get("newlist");
		// 新增资源库
		makeNewAlbums(newlist);
	}

	/**
	 * 资源库已存在专辑的整理
	 * 
	 * @param allist
	 */
	@SuppressWarnings("unchecked")
	private void makeExistAlbums(List<Album> allist) {
		List<MediaAssetPo> malist = new ArrayList<MediaAssetPo>();
		List<ResOrgAssetPo> resAss = new ArrayList<ResOrgAssetPo>();
		List<MaSourcePo> maslist = new ArrayList<MaSourcePo>();
		List<DictRefResPo> dictreflist = new ArrayList<DictRefResPo>();
		List<ChannelAssetPo> chalist = new ArrayList<ChannelAssetPo>();
		List<SeqMaRefPo> seqreflist = new ArrayList<SeqMaRefPo>();
		List<MediaPlayCountPo> mecounts = new ArrayList<MediaPlayCountPo>();
		List<PersonRefPo> pfs = new ArrayList<>();
		if (allist != null && allist.size() > 0) {
			for (Album al : allist) {
				List<AudioPo> aulist = al.getAudiolist();
				if (aulist.size() > 0) {
					List<SeqMediaAssetPo> seqlist = mediaService.getSeqInfo(al.getAlbumPo().getAlbumName(),
							al.getAlbumPo().getAlbumPublisher());
					if (seqlist != null && seqlist.size() > 0) {
						SeqMediaAssetPo seq = seqlist.get(0);
						Map<String, Object> mall = ConvertUtils.convert2MediaAsset(aulist, seq, cate2dictdlist, chlist);
						if (mall != null) {
							if (mall.containsKey("malist")) {
								malist = (List<MediaAssetPo>) mall.get("malist");
								resAss = (List<ResOrgAssetPo>) mall.get("resAss");
								maslist = (List<MaSourcePo>) mall.get("maslist");
								dictreflist = (List<DictRefResPo>) mall.get("dictreflist");
								chalist = (List<ChannelAssetPo>) mall.get("chalist");
								seqreflist = (List<SeqMaRefPo>) mall.get("seqmareflist");
								mecounts = (List<MediaPlayCountPo>) mall.get("mediaplaycount");
								pfs = (List<PersonRefPo>) mall.get("personRef");
							}
							logger.info("增添资源库已存在的专辑新下级声音");
							logger.info("转换声音的数据[{}],转换播放资源表的数据[{}],转换分类数据[{}],转换栏目发布表数据[{}],专辑声音关系数量[{}]",
									malist.size(), maslist.size(), dictreflist.size(), chalist.size(),
									seqreflist.size());
							if (malist.size() > 0) {
								// 往资源库插入声音数据
								mediaService.insertMaList(malist);
								// 往资源库插入资源与外部资源对照
								resAssService.insertResOrgAssetList(resAss);
								// 往资源库插入播放流数据
								mediaService.insertMasList(maslist);
								// 往资源库插入专辑声音关系表数据
								mediaService.insertSeqRefList(seqreflist);
								// 往资源库插入音频播放次数数据
								mediaService.insertMediaPlayCountList(mecounts);
								// 往字典关系表里插入内容分类关系数据
								dictService.insertDictRefList(dictreflist);
								// 往栏目发布表里插入发布信息
								channelService.insertChannelAssetList(chalist);
								//插入主播信息
								if (pfs!=null && pfs.size()>0) {
									personService.insertPersonRef(pfs);
								}
							} else {
								logger.info("已存在的专辑无最新下级声音资源");
							}
						}
					}
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void makeNewAlbums(List<AlbumPo> allist) {

		if (allist != null && allist.size() > 0) {
			for (AlbumPo al : allist) {
				Map<String, Object> map = ConvertUtils.convert2SeqMediaAsset(al, cate2dictdlist, chlist);
				if (map == null) {
					continue;
				}
				
				List<SeqMediaAssetPo> seqlist = new ArrayList<SeqMediaAssetPo>();
				List<MediaAssetPo> malist = new ArrayList<MediaAssetPo>();
				List<ResOrgAssetPo> resAss = new ArrayList<ResOrgAssetPo>();
				List<MaSourcePo> maslist = new ArrayList<MaSourcePo>();
				List<DictRefResPo> dictreflist = new ArrayList<DictRefResPo>();
				List<ChannelAssetPo> chalist = new ArrayList<ChannelAssetPo>();
				List<SeqMaRefPo> seqreflist = new ArrayList<SeqMaRefPo>();
				List<MediaPlayCountPo> mecounts = new ArrayList<MediaPlayCountPo>();
				List<PersonRefPo> pfs = new ArrayList<>();
				
				SeqMediaAssetPo se = (SeqMediaAssetPo) map.get("seq");
				seqlist.add(se);
				mediaService.insertSeqList(seqlist);
				CPersonPo cps = cPersonService.getCPerson(al.getAlbumId(), "hotspot_Album");
				if (cps!=null) {
					PersonPo po = ConvertUtils.convert2Person(cps);
					personService.insertPerson(po);
					PersonRefPo pf = new PersonRefPo();
					pf.setId(SequenceUUID.getPureUUID());
					pf.setPersonId(po.getId());
					pf.setRefName("主播-专辑");
					pf.setResId(se.getId());
					pf.setResTableName("wt_SeqMediaAsset");
					pf.setcTime(new Timestamp(System.currentTimeMillis()));
					personService.insertPersonRef(pf);
				}
				dictreflist.add((DictRefResPo) map.get("dictref"));
				chalist.add((ChannelAssetPo) map.get("cha"));
				mecounts.add((MediaPlayCountPo) map.get("playnum"));

				ResOrgAssetPo resass = new ResOrgAssetPo();
				resass.setId(SequenceUUID.getPureUUID());
				resass.setResId(se.getId());
				resass.setResTableName("wt_SeqMediaAsset");
				resass.setOrgName(se.getSmaPublisher());
				resass.setOrigId(al.getId());
				resass.setOrigTableName("hotspot_Album");
				resass.setcTime(new Timestamp(System.currentTimeMillis()));
				resAss.add(resass);

				// 获取抓取到的专辑下级节目信息
				List<AudioPo> aulist = audioService.getAudioListByAlbumId(al.getAlbumId(), al.getAlbumPublisher(),
						al.getCrawlerNum());
				if (aulist.size() > 0) {
					Map<String, Object> mall = ConvertUtils.convert2MediaAsset(aulist, se, cate2dictdlist, chlist);
					if (mall != null && mall.containsKey("malist")) {
						malist = (List<MediaAssetPo>) mall.get("malist");
						resAss = (List<ResOrgAssetPo>) mall.get("resAss");
						maslist = (List<MaSourcePo>) mall.get("maslist");
						dictreflist = (List<DictRefResPo>) mall.get("dictreflist");
						chalist = (List<ChannelAssetPo>) mall.get("chalist");
						seqreflist = (List<SeqMaRefPo>) mall.get("seqmareflist");
						mecounts = (List<MediaPlayCountPo>) mall.get("mediaplaycount");
						pfs = (List<PersonRefPo>) mall.get("personRef");
					}
					logger.info("新增资源库");
					logger.info("转换声音的数据[{}],转换播放资源表的数据[{}],转换分类数据[{}],转换栏目发布表数据[{}],专辑声音关系数量[{}]", malist.size(),
							maslist.size(), dictreflist.size(), chalist.size(), seqreflist.size());
					if (malist.size() > 0) {
						// 往资源库插入播放流数据
						mediaService.insertMasList(maslist);
						// 往资源库插入专辑声音关系表数据
						mediaService.insertSeqRefList(seqreflist);
						// 往资源库插入音频播放次数数据
						mediaService.insertMediaPlayCountList(mecounts);
						// 往字典关系表里插入内容分类关系数据
						dictService.insertDictRefList(dictreflist);
						// 往栏目发布表里插入发布信息
						channelService.insertChannelAssetList(chalist);
						// 往资源库插入声音数据
						mediaService.insertMaList(malist);
						// 往资源库插入资源与外部资源对照
						resAssService.insertResOrgAssetList(resAss);
						if (pfs!=null && pfs.size()>0) {
							personService.insertPersonRef(pfs);
						}
					} else {
						logger.info("新专辑无下级声音资源");
					}
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void makeSameAlbums(List<Map<String, Object>> samelist) {
		oganService = (OrganizeService) SpringShell.getBean("organizeService");
		List<MediaAssetPo> malist = new ArrayList<MediaAssetPo>();
		List<ResOrgAssetPo> resAss = new ArrayList<ResOrgAssetPo>();
		List<MaSourcePo> maslist = new ArrayList<MaSourcePo>();
		List<DictRefResPo> dictreflist = new ArrayList<DictRefResPo>();
		List<ChannelAssetPo> chalist = new ArrayList<ChannelAssetPo>();
		List<SeqMaRefPo> seqreflist = new ArrayList<SeqMaRefPo>();
		List<MediaPlayCountPo> mecounts = new ArrayList<MediaPlayCountPo>();
		List<PersonRefPo> pfs = new ArrayList<>();
		if (samelist != null && samelist.size() > 0) {
			List<OrganizePo> oganlist = oganService.getOrganizeList();
			for (Map<String, Object> m : samelist) {
				List<Map<String, Object>> sameaus = (List<Map<String, Object>>) m.get("sameaudiolist"); // 相似专辑里相似节目
				List<AudioPo> newaus = (List<AudioPo>) m.get("newaudiolist"); // 相似专辑里不相似的节目
				if (newaus != null && newaus.size() > 0) {
					SeqMediaAssetPo sma = (SeqMediaAssetPo) m.get("Sma");
					Map<String, Object> mall = ConvertUtils.convert2MediaAsset(newaus, sma, cate2dictdlist, chlist);
					if (mall != null && mall.containsKey("malist")) {
						malist=(List<MediaAssetPo>) mall.get("malist");
						resAss=(List<ResOrgAssetPo>) mall.get("resAss");
						maslist=(List<MaSourcePo>) mall.get("maslist");
						dictreflist=(List<DictRefResPo>) mall.get("dictreflist");
						chalist=(List<ChannelAssetPo>) mall.get("chalist");
						seqreflist=(List<SeqMaRefPo>) mall.get("seqmareflist");
						mecounts=(List<MediaPlayCountPo>) mall.get("mediaplaycount");
						pfs=(List<PersonRefPo>) mall.get("personRef");
					}
					logger.info("处理相似专辑数据");
					logger.info("转换声音的数据[{}],转换播放资源表的数据[{}],转换分类数据[{}],转换栏目发布表数据[{}],专辑声音关系数量[{}]", malist.size(),
							maslist.size(), dictreflist.size(), chalist.size(), seqreflist.size());
					if (malist.size() > 0) {
						try {
							// 往资源库插入声音数据
							mediaService.insertMaList(malist);
							// 往资源库插入资源与外部资源对照
							resAssService.insertResOrgAssetList(resAss);
							// 往资源库插入播放流数据
							mediaService.insertMasList(maslist);
							// 往资源库插入专辑声音关系表数据
							mediaService.insertSeqRefList(seqreflist);
							// 往资源库插入音频播放次数数据
							mediaService.insertMediaPlayCountList(mecounts);
							// 往字典关系表里插入内容分类关系数据
							dictService.insertDictRefList(dictreflist);
							// 往栏目发布表里插入发布信息
							channelService.insertChannelAssetList(chalist);
							//插入主播关联信息.
							if (pfs!=null && pfs.size()>0) {
								personService.insertPersonRef(pfs);
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					} else {
						logger.info("已存在的相似专辑无最新下级声音资源");
					}
				}
				maslist.clear();
				resAss.clear();
				if (sameaus != null && sameaus.size() > 0) {
					for (Map<String, Object> mm : sameaus) {
						AudioPo au = (AudioPo) mm.get("audio");
						MediaAssetPo ma = (MediaAssetPo) mm.get("ma");
						Map<String, Object> mss = ConvertUtils.convert2Masource(au, ma, oganlist);
						if (mss != null) {
							maslist.add((MaSourcePo) mss.get("mas"));
							resAss.add((ResOrgAssetPo) mss.get("resAss"));
						}
					}
					logger.info("新增相似声音数[{}]", maslist.size());
					if (maslist != null && maslist.size() > 0) {
						mediaService.insertMasList(maslist);
						resAssService.insertResOrgAssetList(resAss);
					}
				}
			}
		}
	}
}