package com.woting.crawler.scheme.utils;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.spiritdata.framework.util.SequenceUUID;
import com.spiritdata.framework.util.StringUtils;
import com.woting.cm.core.ResOrgAsset.persis.po.ResOrgAssetPo;
import com.woting.cm.core.channel.persis.po.ChannelAssetPo;
import com.woting.cm.core.channel.persis.po.ChannelPo;
import com.woting.cm.core.dict.persis.po.DictRefResPo;
import com.woting.cm.core.keyword.service.KeyWordService;
import com.woting.cm.core.media.persis.po.MaSourcePo;
import com.woting.cm.core.media.persis.po.MediaAssetPo;
import com.woting.cm.core.media.persis.po.MediaPlayCountPo;
import com.woting.cm.core.media.persis.po.SeqMaRefPo;
import com.woting.cm.core.media.persis.po.SeqMediaAssetPo;
import com.woting.cm.core.perimeter.persis.po.OrganizePo;
import com.woting.cm.core.person.persis.po.PersonPo;
import com.woting.cm.core.person.persis.po.PersonRefPo;
import com.woting.cm.core.person.service.PersonService;
import com.woting.crawler.core.album.persis.po.AlbumPo;
import com.woting.crawler.core.audio.persis.po.AudioPo;
import com.woting.crawler.core.cperson.persis.po.CPersonPo;
import com.woting.crawler.core.dict.persis.po.DictDPo;
import com.woting.crawler.ext.SpringShell;

public abstract class ConvertUtils {
	private static Logger logger = LoggerFactory.getLogger(ConvertUtils.class);

	public static List<AlbumPo> convert2Album(List<Map<String, Object>> list, String publisher) {
		List<AlbumPo> albums = new ArrayList<AlbumPo>();
		if (list != null && !list.isEmpty()) {
			for (Map<String, Object> m : list) {
				AlbumPo al = new AlbumPo();
				al.setId(SequenceUUID.getPureUUID());
				al.setAlbumId(m.get("albumId") + "");
				al.setAlbumName(m.get("albumName") + "");
				al.setAlbumImg(m.get("albumImg") + "");
				al.setAlbumPublisher(publisher);
				al.setAlbumTags(m.get("tags") + "");
				al.setCategoryId(m.get("categoryId") + "");
				al.setCategoryName(m.get("categoryName") + "");
				al.setPlayCount(m.get("playCount") + "");
				al.setVisitUrl(m.get("visitUrl") + "");
				al.setDescn(m.get("descript") + "");
				al.setCrawlerNum(m.get("CrawlerNum") + "");
				al.setSchemeId(m.get("schemeId") + "");
				al.setSchemeName(m.get("schemeName") + "");
				al.setcTime(new Timestamp(System.currentTimeMillis()));
				albums.add(al);
			}
		}
		return albums;
	}

	public static List<AudioPo> convert2Aludio(List<Map<String, Object>> list, String publisher) {
		List<AudioPo> audios = new ArrayList<AudioPo>();
		if (list != null && list.size() > 0) {
			for (Map<String, Object> m : list) {
				AudioPo audio = new AudioPo();
				audio.setId(SequenceUUID.getPureUUID());
				audio.setAudioId(m.get("audioId") + "");
				audio.setAudioName(m.get("audioName") + "");
				audio.setAlbumId(m.get("albumId") + "");
				audio.setAlbumName(m.get("albumName") + "");
				audio.setAudioImg(m.get("audioImg") + "");
				audio.setCategoryId(m.get("categoryId") + "");
				audio.setCategoryName(m.get("categoryName") + "");
				audio.setAudioURL(m.get("playUrl") + "");
				audio.setAudioTags(m.get("tags") + "");
				audio.setDuration(m.get("duration") + "");
				audio.setPlayCount(m.get("playCount") + "");
				audio.setDescn(m.get("descript") + "");
				audio.setAudioPublisher(publisher);
				audio.setVisitUrl(m.get("visitUrl") + "");
				audio.setCrawlerNum(m.get("CrawlerNum") + "");
				audio.setSchemeId(m.get("schemeId") + "");
				audio.setSchemeName(m.get("schemeName") + "");
				audio.setcTime(new Timestamp(Long.valueOf(m.get("cTime")+"")));
				audios.add(audio);
			}
		}
		return audios;
	}

	public static List<DictDPo> convert2DictD(List<Map<String, Object>> list, List<DictDPo> ddlist, String publisher, String dmid ,int isValidate) {
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
				dd.setcTime(new Timestamp(System.currentTimeMillis()));
				dictdlist.add(dd);
			}
		}
		return dictdlist;
	}

	public static Map<String, Object> convert2MediaAsset(List<AudioPo> aulist, SeqMediaAssetPo seq, List<Map<String, Object>> dicts, List<ChannelPo> chlist) {
		List<MediaAssetPo> malist = new ArrayList<MediaAssetPo>();
		List<ResOrgAssetPo> resAsslist = new ArrayList<ResOrgAssetPo>();
		List<MaSourcePo> maslist = new ArrayList<MaSourcePo>();
		List<DictRefResPo> dictreflist = new ArrayList<DictRefResPo>();
		List<ChannelAssetPo> chalist = new ArrayList<ChannelAssetPo>();
		List<SeqMaRefPo> seqreflist = new ArrayList<SeqMaRefPo>();
		List<MediaPlayCountPo> mecounts = new ArrayList<MediaPlayCountPo>();
		List<PersonRefPo> pfs = new ArrayList<>();
		if (aulist != null && aulist.size() > 0) {
			for (AudioPo au : aulist) {
				// 声音数据转换
				MediaAssetPo ma = new MediaAssetPo();
				ma.setId(au.getId());
				ma.setMaTitle(au.getAudioName());
				if (au.getAudioPublisher().equals("蜻蜓")) {
					ma.setMaImg(seq.getSmaImg());
				} else {
					String imgp = au.getAudioImg();
					if (!StringUtils.isNullOrEmptyOrSpace(imgp) && imgp.length()>5) {
						String imgpath = FileUtils.makeImgFile("2", imgp);
						if (!StringUtils.isNullOrEmptyOrSpace(imgpath)) {
							ma.setMaImg(imgpath);
						}
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
				ma.setKeyWords(au.getAudioTags());
				ma.setPubCount(1);
				ma.setTimeLong(new Long(au.getDuration() + "000"));
				ma.setMaStatus(1);
				ma.setCTime(au.getcTime());
				
				ResOrgAssetPo roa = new ResOrgAssetPo();
				roa.setId(SequenceUUID.getPureUUID());
				roa.setResId(ma.getId());
				roa.setResTableName("wt_MediaAsset");
				roa.setOrgName(ma.getMaPublisher());
				roa.setOrigId(au.getId());
				roa.setOrigTableName("hotspot_Audio");
				roa.setcTime(new Timestamp(System.currentTimeMillis()));

				MaSourcePo maS = new MaSourcePo();
				maS.setId(SequenceUUID.getPureUUID());
				maS.setMaId(au.getId());
				maS.setIsMain(1);
				maS.setMaSrcType(seq.getSmaPubType());
				maS.setMaSrcId(seq.getSmaPubId());
				maS.setMaSource(seq.getSmaPublisher());
				maS.setPlayURI(au.getAudioURL());
				maS.setDescn(au.getDescn());
				maS.setCTime(new Timestamp(System.currentTimeMillis()));

				SeqMaRefPo seqMaRef = new SeqMaRefPo();
				seqMaRef.setId(SequenceUUID.getPureUUID());
				seqMaRef.setsId(seq.getId());
				seqMaRef.setmId(ma.getId());
				seqMaRef.setColumnNum(0);
				seqMaRef.setDescn(ma.getDescn());
				seqMaRef.setcTime(new Timestamp(System.currentTimeMillis()));
				
				PersonService personService = (PersonService) SpringShell.getBean("personService");
				PersonRefPo pf = personService.getPersonRefBy("wt_SeqMediaAsset", seq.getId());
				if (pf!=null) {
					pf.setRefName("主播-节目");
				    pf.setResTableName("wt_MediaAsset");
				    pf.setResId(ma.getId());
				    pf.setId(SequenceUUID.getPureUUID());
				    pf.setcTime(new Timestamp(System.currentTimeMillis()));
				    pfs.add(pf);
				}

				DictRefResPo dictRefRes = new DictRefResPo();
				dictRefRes.setId(SequenceUUID.getPureUUID());
				dictRefRes.setRefName("单体-内容分类");
				dictRefRes.setResTableName("wt_MediaAsset");
				dictRefRes.setResId(au.getId());
				for (Map<String, Object> ms : dicts) {
					if (au.getAudioPublisher().equals(ms.get("publisher")) && au.getCategoryName().equals(ms.get("crawlerDictdName"))) {
						dictRefRes.setDictMid(ms.get("dictmId") + "");
						dictRefRes.setDictDid(ms.get("dictdId") + "");
						dictRefRes.setCTime(new Timestamp(System.currentTimeMillis()));
					}
				}
				if (dictRefRes.getDictDid() != null && !dictRefRes.getDictDid().equals("null")) {
					ChannelAssetPo cha = new ChannelAssetPo();
					cha.setId(SequenceUUID.getPureUUID());
					cha.setAssetType("wt_MediaAsset");
					cha.setAssetId(ma.getId());
					cha.setPublisherId(ma.getMaPubId());
					cha.setIsValidate(1);
					cha.setCheckerId("1");
					cha.setPubName(ma.getMaTitle());
					cha.setPubImg(ma.getMaImg());
					cha.setSort(0);
					cha.setFlowFlag(2);
					cha.setInRuleIds("etl");
					cha.setCheckRuleIds("etl");
					cha.setCTime(au.getcTime());
					cha.setPubTime(cha.getCTime());
					if (chlist != null && chlist.size() > 0) {
						for (ChannelPo ch : chlist) {
							if (dictRefRes.getDictDid().equals(ch.getId())) {
								cha.setChannelId(ch.getId());
								break;
							}
						}
					}
					if (cha.getChannelId() == null || cha.getChannelId().equals("null"))
						continue;
					//存储标签关系
					if (!StringUtils.isNullOrEmptyOrSpace(au.getAudioTags())) {
						KeyWordService keyWordService = (KeyWordService) SpringShell.getBean("keyWordService");
						keyWordService.saveKwAndKeRef(au.getAudioTags(), "wt_MediaAsset", ma.getId());
						keyWordService.saveKwAndKeRef(au.getAudioTags(), "wt_Channel", cha.getChannelId());
					}
					chalist.add(cha);
					malist.add(ma);
					resAsslist.add(roa);
					maslist.add(maS);
					dictreflist.add(dictRefRes);
					seqreflist.add(seqMaRef);

					MediaPlayCountPo mecount = new MediaPlayCountPo();
					mecount.setId(SequenceUUID.getPureUUID());
					mecount.setResTableName("wt_MediaAsset");
					mecount.setResId(ma.getId());
					mecount.setPlayCount(convertPlayNum2Long(au.getPlayCount()));
					mecount.setPublisher(au.getAudioPublisher());
					mecount.setcTime(new Timestamp(System.currentTimeMillis()));
					mecounts.add(mecount);
				} else {
					continue;
				}
			}
		}
		logger.info("转换声音的数据[{}],转换播放资源表的数据[{}],转换分类数据[{}],转换栏目发布表数据[{}]", malist.size(), maslist.size(),
				dictreflist.size(), chalist.size());
		if (malist != null && malist.size() > 0) {
			Map<String, Object> m = new HashMap<String, Object>();
			m.put("malist", malist);
			m.put("resAss", resAsslist);
			m.put("maslist", maslist);
			m.put("dictreflist", dictreflist);
			m.put("chalist", chalist);
			m.put("seqmareflist", seqreflist);
			m.put("mediaplaycount", mecounts);
			m.put("personRef", pfs);
			return m;
		}
		return null;
	}

	public static Map<String, Object> convert2SeqMediaAsset(AlbumPo al, List<Map<String, Object>> dicts,
			List<ChannelPo> chlist) {
		Map<String, Object> map = new HashMap<>();
		SeqMediaAssetPo seq = new SeqMediaAssetPo();
		seq.setId(SequenceUUID.getPureUUID());
		seq.setSmaTitle(al.getAlbumName());
		String imgp = al.getAlbumImg();
		if (!StringUtils.isNullOrEmptyOrSpace(imgp) && imgp.length()>5) {
			String imgpath = FileUtils.makeImgFile("2", imgp);
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
				}
			}
		}
		seq.setSmaAllCount(0);
		if (al.getAlbumTags() != null && !al.getAlbumTags().equals("null"))
			seq.setKeyWords(al.getAlbumTags());
		seq.setLangDid("zho");
		seq.setLanguage("中文");
		if (al.getDescn() != null && !al.getDescn().equals("null")) seq.setDescn(al.getDescn());
		else seq.setDescn("欢迎大家收听"+seq.getSmaTitle());
		seq.setCTime(new Timestamp(System.currentTimeMillis()));
		seq.setPubCount(1);
		seq.setSmaStatus(1);
		map.put("seq", seq);

		MediaPlayCountPo mecount = new MediaPlayCountPo();
		mecount.setId(SequenceUUID.getPureUUID());
		mecount.setResTableName("wt_SeqMediaAsset");
		mecount.setResId(seq.getId());
		mecount.setPlayCount(convertPlayNum2Long(al.getPlayCount()));
		mecount.setPublisher(al.getAlbumPublisher());
		mecount.setcTime(new Timestamp(System.currentTimeMillis()));
		map.put("playnum", mecount);

		DictRefResPo dictRefRes = new DictRefResPo();
		dictRefRes.setId(SequenceUUID.getPureUUID());
		dictRefRes.setRefName("专辑-内容分类");
		dictRefRes.setResTableName("wt_SeqMediaAsset");
		dictRefRes.setResId(seq.getId());
		for (Map<String, Object> ms : dicts) {
			if (al.getAlbumPublisher().equals(ms.get("publisher"))
					&& al.getCategoryName().equals(ms.get("crawlerDictdName"))) {
				dictRefRes.setDictMid(ms.get("dictmId") + "");
				dictRefRes.setDictDid(ms.get("dictdId") + "");
				dictRefRes.setCTime(new Timestamp(System.currentTimeMillis()));
				break;
			}
		}

		if (dictRefRes.getDictDid() != null && !dictRefRes.getDictDid().equals("null")) {
			map.put("dictref", dictRefRes);
			ChannelAssetPo cha = new ChannelAssetPo();
			cha.setId(SequenceUUID.getPureUUID());
			cha.setAssetType("wt_SeqMediaAsset");
			cha.setAssetId(seq.getId());
			cha.setPublisherId(seq.getSmaPubId());
			cha.setIsValidate(1);
			cha.setCheckerId("1");
			cha.setPubName(seq.getSmaTitle());
			cha.setPubImg(seq.getSmaImg());
			cha.setSort(0);
			cha.setFlowFlag(2);
			cha.setInRuleIds("etl");
			cha.setCheckRuleIds("etl");
			cha.setCTime(new Timestamp(System.currentTimeMillis()));
			cha.setPubTime(cha.getCTime());
			if (chlist != null && chlist.size() > 0) {
				for (ChannelPo ch : chlist) {
					if (dictRefRes.getDictDid().equals(ch.getId())) {
						cha.setChannelId(ch.getId());
						map.put("cha", cha);
						//存储标签关系
						if (!StringUtils.isNullOrEmptyOrSpace(al.getAlbumTags())) {
							KeyWordService keyWordService = (KeyWordService) SpringShell.getBean("keyWordService");
							keyWordService.saveKwAndKeRef(al.getAlbumTags(), "wt_SeqMediaAsset", seq.getId());
							keyWordService.saveKwAndKeRef(al.getAlbumTags(), "wt_Channel", cha.getChannelId());
						}
						break;
					}
				}
			}
		}
		if (map.containsKey("cha")) {
			return map;
		}
		else
			return null;
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
				}
			}
		}
		String imgp = cpo.getPortrait();
		if (!StringUtils.isNullOrEmptyOrSpace(imgp) && imgp.length()>5) {
			String imgpath = FileUtils.makeImgFile("1", imgp);
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

	public static Map<String, Object> convert2Masource(AudioPo audio, MediaAssetPo media ,List<OrganizePo> oganlist) {
		if (audio == null || media == null)
			return null;
//		List<ResOrgAssetPo> resAss = new ArrayList<ResOrgAssetPo>();
		MaSourcePo mas = new MaSourcePo();
		mas.setId(SequenceUUID.getPureUUID());
		mas.setMaId(media.getId());
		mas.setIsMain(0);
		mas.setMaSrcType(media.getMaPubType());
		if (media.getMaPubType() == 1 && oganlist != null && oganlist.size() > 0) {
			for (OrganizePo ogan : oganlist) {
				if (audio.getAudioPublisher().equals(ogan.getOrgName())) {
					mas.setMaSrcId(ogan.getId());
					mas.setMaSource(audio.getAudioPublisher());
					mas.setPlayURI(audio.getAudioURL());
					mas.setDescn(audio.getDescn());
					mas.setCTime(new Timestamp(System.currentTimeMillis()));
				}
			}
		}
		
		ResOrgAssetPo resass = new ResOrgAssetPo();
		resass.setId(SequenceUUID.getPureUUID());
		resass.setResId(mas.getId());
		resass.setResTableName("wt_Masource");
		resass.setOrgName(mas.getMaSource());
		resass.setOrigId(audio.getId());
		resass.setOrigTableName("hotspot_Audio");
//		resAss.add(resass);
		if(mas.getMaSrcId()!=null) {
			Map<String, Object> m = new HashMap<String,Object>();
			m.put("mas", mas);
			m.put("resAss", resass);
			return m;
		}
		return null;
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
}
