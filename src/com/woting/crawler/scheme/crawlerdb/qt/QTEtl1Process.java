package com.woting.crawler.scheme.crawlerdb.qt;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.spiritdata.framework.util.JsonUtils;
import com.spiritdata.framework.util.SequenceUUID;
import com.woting.crawler.core.album.persis.po.AlbumPo;
import com.woting.crawler.core.album.service.AlbumService;
import com.woting.crawler.core.albumaudioref.persis.po.AlbumAudioRefPo;
import com.woting.crawler.core.albumaudioref.service.AlbumAudioRefService;
import com.woting.crawler.core.audio.persis.po.AudioPo;
import com.woting.crawler.core.audio.service.AudioService;
import com.woting.crawler.core.ccomment.persis.po.CCommentPo;
import com.woting.crawler.core.ccomment.service.CCommentService;
import com.woting.crawler.core.cperson.persis.po.CPersonPo;
import com.woting.crawler.core.cperson.persis.po.CPersonRefPo;
import com.woting.crawler.core.cperson.service.CPersonService;
import com.woting.crawler.core.cplaycount.persis.po.CPlayCountPo;
import com.woting.crawler.core.cplaycount.service.CPlayCountService;
import com.woting.crawler.core.dict.persis.po.DictDPo;
import com.woting.crawler.core.dict.persis.po.DictRefPo;
import com.woting.crawler.core.dict.service.CrawlerDictService;
import com.woting.crawler.ext.SpringShell;
import com.woting.crawler.scheme.utils.ConvertUtils;

public class QTEtl1Process {

	@SuppressWarnings("unchecked")
	public String insertNewAlbum(Map<String, Object> map, Map<String, Object> albummap, Map<String, Object> usermap, List<Map<String, Object>> audios) {
		if (albummap!=null && albummap.size()>0 && audios!=null && audios.size()>0) {
			String albumId = albummap.get("id").toString();
			AlbumService albumService = (AlbumService) SpringShell.getBean("albumService");
			String id = "QT_ALBUM_"+albumId;
			AlbumPo albumPo = albumService.getAlbumInfo(id);
			if (albumPo!=null) return null;
			
			albumPo = new AlbumPo();
			albumPo.setId(id);
			albumPo.setAlbumId(albummap.get("id").toString());
			Map<String, Object> imgmap = (Map<String, Object>) albummap.get("thumbs");
			String img = "";
			try {img = imgmap.get("800_thumb").toString();} catch (Exception e) {}
			if (img.length()==0)
				try {img = imgmap.get("400_thumb").toString();} catch (Exception e) {}
			else if (img.length()==0)
				try {img = imgmap.get("200_thumb").toString();} catch (Exception e) {}
			else if (img.length()==0)
				try {img = imgmap.get("large_thumb").toString();} catch (Exception e) {}
			else if (img.length()==0)
				try {img = imgmap.get("medium_thumb").toString();} catch (Exception e) {}
			else if (img.length()==0)
				try {img = imgmap.get("small_thumb").toString();} catch (Exception e) {}
			if (img.length()>0) albumPo.setAlbumImg(img);
			albumPo.setAlbumPublisher("蜻蜓");
			albumPo.setAlbumName(albummap.get("title").toString());
			albumPo.setVisitUrl("http://neo.qingting.fm/channels/"+albumPo.getAlbumId());
			Map<String, Object> detailmap = (Map<String, Object>) albummap.get("detail");
			String descn = albummap.get("description").toString();
			if (descn.length()>0) albumPo.setDescn(descn);
			try {albumPo.setPubTime(new Timestamp(ConvertUtils.makeLongTime(albummap.get("update_time").toString())));} catch (Exception e) {}
			albumPo.setcTime(new Timestamp(System.currentTimeMillis()));
			List<AlbumPo> albums = new ArrayList<>();
			albums.add(albumPo);
			try {
				albumService.insertAlbumList(albums);
			} catch (Exception e) {}
			
			//a282e20b7f98479aa3c354539f4715e3
			
			List<Map<String, Object>> dictRefLs = new ArrayList<>();
			CrawlerDictService dictService = (CrawlerDictService) SpringShell.getBean("crawlerDictService");
			DictDPo dictDPo = dictService.getDictDInfo("0", "蜻蜓");
			String dpId = dictDPo.getId();
			String categoryName = map.get(albumPo.getAlbumId())+"";
			if (categoryName!=null && !categoryName.equals("null")) {
				String[] cates = categoryName.split(",");
				for (String st : cates) {
					String[] cts = st.split("/");
					String cate = cts[0];
					DictDPo ddPo = dictService.getDictDInfo(dpId, cate);
					if (ddPo!=null) {
						DictRefPo dictRefPo = new DictRefPo();
						dictRefPo.setId(SequenceUUID.getPureUUID());
						dictRefPo.setCdictMid(ddPo.getmId());
						dictRefPo.setCdictDid(ddPo.getId());
						dictRefPo.setResId(id);
						dictRefPo.setResTableName("c_Album");
						try {dictService.insertDictRef(dictRefPo);} catch (Exception e) {continue;}
						Map<String, Object> catem = new HashMap<>();
						catem.put("dictMid", ddPo.getmId());
						catem.put("dictDid", ddPo.getId());
						dictRefLs.add(catem);
					}
				}
			}
			
			long playCount = 0;
			try {
				String pstr = detailmap.get("playcount").toString();
				playCount = Long.valueOf(makePlayCount(pstr));
			} catch (Exception e) {}
			
			CPlayCountService cPlayCountService = (CPlayCountService) SpringShell.getBean("CPlayCountService");
			CPlayCountPo cPlayCountPo = null;
			try {
				cPlayCountPo = new CPlayCountPo();
				cPlayCountPo.setId(SequenceUUID.getPureUUID());
				cPlayCountPo.setPublisher("蜻蜓");
				cPlayCountPo.setResTableName("c_Album");
				cPlayCountPo.setResId(id);
				cPlayCountPo.setPlayCount(playCount);
				cPlayCountService.insertCPlayCount(cPlayCountPo);
			} catch (Exception e) {}
			
			String userId = null;
			List<String> pIds = new ArrayList<>();
			List<Map<String, Object>> uList = (List<Map<String, Object>>) usermap.get("podcasters");
			CPersonService cPersonService = (CPersonService) SpringShell.getBean("CPersonService");
			if (uList!=null && uList.size()>0) {
				for (Map<String, Object> um : uList) {
					userId = um.get("user_system_id").toString();
					try {
						Document doc = Jsoup.connect("http://u2.qingting.fm/u2/api/v3/podcaster/"+userId).ignoreContentType(true).timeout(100000).get();
						String userstr = doc.body().html();
						usermap = (Map<String, Object>) JsonUtils.jsonToObj(userstr, Map.class);
						usermap = (Map<String, Object>) usermap.get("data");
						CPersonPo cPo = null;
						String pId = usermap.get("qingting_id").toString();
						pId = "QT_PERSON_"+pId;
						pIds.add(pId);
						cPo = cPersonService.getCPersonById(pId);
						if (cPo==null) {
							cPo = new CPersonPo();
							cPo.setId(pId);
							cPo.setpSrcId(usermap.get("qingting_id").toString());
							cPo.setpName(usermap.get("nick_name").toString());
							cPo.setpSource("蜻蜓");
							try {cPo.setPortrait(usermap.get("avatar").toString());} catch (Exception e) {}
							if (usermap.get("phone_number")!=null) {
								String phonenum = usermap.get("phone_number").toString();
								if (phonenum!=null && phonenum.length()>0) {
									cPo.setPhoneNum(phonenum);
								}
							}
							if (usermap.get("propertyType")!=null) {
								String ptitle = usermap.get("propertyType").toString();
								if (ptitle!=null && ptitle.length()>0) {
									cPo.setpTitle(ptitle);
								}
							}
							if (usermap.get("award_description")!=null) {
								String cpodescn = usermap.get("award_description").toString();
								if (cpodescn!=null && cpodescn.length()>0) {
									cPo.setDescn(cpodescn);
								}
							}
							if (usermap.get("gender")!=null) {
								String gender = usermap.get("gender").toString();
								if (gender!=null) {
									if (gender.equals("f")) cPo.setSex(1);
									else if (gender.equals("m")) cPo.setSex(2);
									else cPo.setSex(0);
								} else cPo.setSex(0);
							}
							if (usermap.get("update_time")!=null) {
								String updatetime = usermap.get("update_time").toString();
								if (updatetime!=null && updatetime.length()>0) {
									if (updatetime.contains("T")) {
										updatetime = updatetime.replace("T", " ");
									}
									cPo.setcTime(new Timestamp(ConvertUtils.makeLongTime(updatetime)));
								}
							}
							try {cPersonService.insertPerson(cPo);} catch (Exception e) {}
						}
						
						try {
							if (pId!=null) {
								CPersonRefPo cPersonrefPo = new CPersonRefPo();
								cPersonrefPo.setId(SequenceUUID.getPureUUID());
								cPersonrefPo.setRefName("主播-专辑");
								cPersonrefPo.setPersonId(pId);
								cPersonrefPo.setResTableName("c_Album");
								cPersonrefPo.setResId(id);
								cPersonService.insertPersonRef(cPersonrefPo);
							}
						} catch (Exception e) {}
					} catch (Exception e) {}
				}
			}
			AudioService audioService = (AudioService) SpringShell.getBean("audioService");
			CCommentService commentService = (CCommentService) SpringShell.getBean("CCommentService");
			AlbumAudioRefService aRefService = (AlbumAudioRefService) SpringShell.getBean("albumAudioRefService");
			for (int i = 0; i < audios.size(); i++) {
				try {
					Map<String, Object> audiomap = audios.get(i);
					AudioPo audioPo = new AudioPo();
					String audioId = audiomap.get("id").toString();
					audioId = "QT_AUDIO_"+audioId;
					audioPo.setId(audioId);
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
					} else {continue;}
					String uri = urlmap.get("file_path").toString();
					uri = "http://od.qingting.fm/"+uri;
					audioPo.setAudioURL(uri);
	//				audioPo.setColumnNum(audios.size()-i);
	//				audioPo.setCrawlerNum("1");
					audioPo.setVisitUrl("http://neo.qingting.fm/channels/"+albumPo.getAlbumId()+"/programs/"+audioPo.getAudioId());
					String updatetime = audiomap.get("update_time").toString();
					if (updatetime.equals("0000-00-00 00:00:00")) audioPo.setcTime(new Timestamp(System.currentTimeMillis()));
					else {audioPo.setcTime(new Timestamp(ConvertUtils.makeLongTime(updatetime)));}
					String playcount = "0";
					try {
						Document doc = Jsoup.connect("http://i.qingting.fm/wapi/program_playcount?pids="+albumPo.getAlbumId()+"_"+audioPo.getAudioId()).ignoreContentType(true).timeout(10000).get();
						playcount =  doc.body().html();
						Map<String, Object> playcountmap = (Map<String, Object>) JsonUtils.jsonToObj(playcount, Map.class);
						List<Map<String, Object>> playcounts =  (List<Map<String, Object>>) playcountmap.get("data");
						if (playcounts!=null && playcounts.size()>0) {
							for (Map<String, Object> map2 : playcounts) {
								if (map2.get("id").equals(albumPo.getAlbumId()+"_"+audioPo.getAudioId())) {
									playcount = makePlayCount(map2.get("playcount").toString())+"";
								}
							}
						}
					} catch (Exception e) {System.out.println("播放次数出错  "+audioPo.getAudioId());}
					try {
						audioService.insertAudio(audioPo);
					} catch (Exception e) {}
					
					// 栏目关系绑定
					if (dictRefLs!=null && dictRefLs.size()>0) {
						for (Map<String, Object> map2 : dictRefLs) {
							DictRefPo dictRefPo = new DictRefPo();
							dictRefPo.setId(SequenceUUID.getPureUUID());
							dictRefPo.setCdictMid(map2.get("dictMid").toString());
							dictRefPo.setCdictDid(map2.get("dictDid").toString());
							dictRefPo.setResId(audioPo.getId());
							dictRefPo.setResTableName("c_Audio");
							try {dictService.insertDictRef(dictRefPo);} catch (Exception e) {continue;}
						}
					}
					
					try {
						AlbumAudioRefPo aRefPo = new AlbumAudioRefPo();
						aRefPo.setId(SequenceUUID.getPureUUID());
						aRefPo.setAlId(id);
						aRefPo.setAuId(audioPo.getId());
						aRefPo.setColumnNum(audios.size()-i);
						aRefPo.setIsMain(1);
//					    else aRefPo.setIsMain(0);
						aRefService.insertAlbumAudioRef(aRefPo);
					} catch (Exception e) {}
					
					try {
						if (pIds!=null && pIds.size()>0) {
							for (String pId : pIds) {
								CPersonRefPo cPersonrefPo = new CPersonRefPo();
								cPersonrefPo.setId(SequenceUUID.getPureUUID());
								cPersonrefPo.setRefName("主播-节目");
								cPersonrefPo.setPersonId(pId);
								cPersonrefPo.setResTableName("c_Audio");
								cPersonrefPo.setResId(audioId);
								cPersonService.insertPersonRef(cPersonrefPo);
							}
						}
					} catch (Exception e) {}
					
					//播放次数入库
					try {
						cPlayCountPo = new CPlayCountPo();
						cPlayCountPo.setId(SequenceUUID.getPureUUID());
						cPlayCountPo.setPublisher("蜻蜓");
						cPlayCountPo.setResTableName("c_Audio");
						cPlayCountPo.setResId(audioPo.getAudioId());
						cPlayCountPo.setPlayCount(Long.valueOf(playcount));
						cPlayCountService.insertCPlayCount(cPlayCountPo);
					} catch (Exception e) {}
					
					try {
						if (userId==null) userId = "4e44a2268f9901d970d49f6206f20f7a";
						Document doc = Jsoup.connect("http://qtime.qingting.fm/api/v1/wsq/album/"+albumPo.getAlbumId()+"/program/"+audioPo.getAudioId()+"/comments?podcast_id="+userId).ignoreContentType(true).timeout(10000).get();
						String commentstr = doc.body().html();
						Map<String, Object> commentmap = (Map<String, Object>) JsonUtils.jsonToObj(commentstr, Map.class);
						if (commentmap!=null && commentmap.size()>0) {
							CCommentPo cCommentPo = new CCommentPo();
							cCommentPo.setId(SequenceUUID.getPureUUID());
							cCommentPo.setPublisher("蜻蜓");
							cCommentPo.setResTableName("c_Audio");
							cCommentPo.setResId(audioId);
							cCommentPo.setCommentCount(Long.valueOf(commentmap.get("total").toString()));
							if (cCommentPo.getCommentCount()>0) {
								commentService.insertCComment(cCommentPo);
							}
						}
					} catch (Exception e) {}
				} catch (Exception e) {
					continue;
				}
			}
			return albumPo.getId();
		}
		return null;
	}

	private String makePlayCount(String playnum) {
		int lastnum = -1;
		int begnum = -1;
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
