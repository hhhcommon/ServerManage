package com.woting.crawler.scheme.crawlerdb.xmly;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.spiritdata.framework.util.JsonUtils;
import com.spiritdata.framework.util.SequenceUUID;
import com.woting.crawler.core.album.persis.po.AlbumPo;
import com.woting.crawler.core.album.service.AlbumService;
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
import com.woting.crawler.core.csubscribe.persis.po.CSubscribePo;
import com.woting.crawler.core.csubscribe.service.CSubscribeService;
import com.woting.crawler.core.scheme.model.Scheme;
import com.woting.crawler.ext.SpringShell;
import com.woting.crawler.scheme.utils.HttpUtils;

public class XMLYEtl1Process {
	
	@SuppressWarnings("unchecked")
	public String insertNewAlbum(Map<String, Object> alm, Map<String, Object> map, Scheme scheme) {
		alm = (Map<String, Object>) alm.get("data");
		Map<String, Object> albummap = (Map<String, Object>) alm.get("album");
		Map<String, Object> usermap = (Map<String, Object>) alm.get("user");
		Map<String, Object> tracks = (Map<String, Object>) alm.get("tracks");
		String userId = usermap.get("uid").toString();
		AlbumPo albumPo = new AlbumPo();
		albumPo.setId(SequenceUUID.getPureUUID());
		albumPo.setAlbumId(albummap.get("albumId")+"");
		if (map.containsKey(albumPo.getAlbumId())) {
			String img = albummap.get("coverLargePop")+"";
			if (img.length()<10) {
				img = albummap.get("coverWebLarge")+"";
			}
			if (img.length()<10) {
				img = albummap.get("coverLarge")+"";
			}
			albumPo.setAlbumImg(img);
			albumPo.setAlbumName(albummap.get("title")+"");
			albumPo.setAlbumPublisher("喜马拉雅");
			albumPo.setPlayCount(albummap.get("playTimes")+"");
			if (albummap.get("tags")!=null && albummap.get("tags").toString().length()>0) {
				albumPo.setAlbumTags(albummap.get("tags")+"");
			}
			if (albummap.get("intro")!=null && albummap.get("intro").toString().length()>0) {
				albumPo.setDescn(albummap.get("intro")+"");
			}
			albumPo.setCategoryId(albummap.get("categoryId")+"");
			albumPo.setCategoryName(map.get(albumPo.getAlbumId())+"");
			albumPo.setVisitUrl("http://www.ximalaya.com/"+userId+"/album/"+albumPo.getAlbumId());
			long data = System.currentTimeMillis();
			try {data = Long.valueOf(albummap.get("lastUptrackAt")+"");} catch (Exception e) {}
			try {data = Long.valueOf(albummap.get("updatedAt")+"");} catch (Exception e) {}
			try {data = Long.valueOf(albummap.get("createdAt")+"");} catch (Exception e) {}
			albumPo.setcTime(new Timestamp(data));
			albumPo.setCrawlerNum(scheme.getSchemenum());
			AlbumService albumService = (AlbumService) SpringShell.getBean("albumService");
			List<AlbumPo> albumPos = new ArrayList<>();
			albumPos.add(albumPo);
			albumService.insertAlbumList(albumPos);
			CPlayCountService cPlayCountService = (CPlayCountService) SpringShell.getBean("CPlayCountService");
			CPlayCountPo cPlayCountPo = new CPlayCountPo();
			cPlayCountPo.setId(SequenceUUID.getPureUUID());
			cPlayCountPo.setPublisher("喜马拉雅");
			cPlayCountPo.setResTableName("c_Album");
			cPlayCountPo.setResId(albumPo.getAlbumId());
			cPlayCountPo.setPlayCount(Long.valueOf(albumPo.getPlayCount()));
			cPlayCountService.insertCPlayCount(cPlayCountPo);
			//专辑订阅数据入库
			try {
				CSubscribePo cSubscribePo = new CSubscribePo();
				cSubscribePo.setId(SequenceUUID.getPureUUID());
				cSubscribePo.setPublisher("喜马拉雅");
				cSubscribePo.setResTableName("c_Album");
				cSubscribePo.setResId(albumPo.getAlbumId());
				cSubscribePo.setSubscribeCount(Long.valueOf(albummap.get("subscribeCount").toString()));
				CSubscribeService cSubscribeService = (CSubscribeService) SpringShell.getBean("CSubscribeService");
				cSubscribeService.insertCSubscribe(cSubscribePo);
			} catch (Exception e) {
				e.printStackTrace();
			}
//			FileUtils.writeFile(JsonUtils.objToJson(albumPo), "E:\\XMLY\\"+albumPo.getAlbumId()+".txt");
			
			try {
				Document docpers = null;
				Map<String, Object> persm = null;
				try {
					docpers = HttpUtils.makeXMLYJsoup("http://www.ximalaya.com/mobile/v1/artist/intro?device=android&statEvent=pageview%2Fuser%40"+userId+"&statPage=tab%40%E5%8F%91%E7%8E%B0_%E4%B8%BB%E6%92%AD&statPosition=2&toUid="+userId);
					String vals = docpers.body().html();
					persm = (Map<String, Object>) JsonUtils.jsonToObj(vals, Map.class);
				} catch (Exception e) {
					String vals = HttpUtils.HttpClient("http://www.ximalaya.com/mobile/v1/artist/intro?device=android&statEvent=pageview%2Fuser%40"+userId+"&statPage=tab%40%E5%8F%91%E7%8E%B0_%E4%B8%BB%E6%92%AD&statPosition=2&toUid="+userId);
					persm = (Map<String, Object>) JsonUtils.jsonToObj(vals, Map.class);
				}
				CPersonPo cPo = new CPersonPo();
				cPo.setId(SequenceUUID.getPureUUID());
				cPo.setpName(persm.get("nickname").toString());
				cPo.setpSource("喜马拉雅");
				try {cPo.setpSrcId(persm.get("uid").toString());} catch (Exception e) {}
				try {cPo.setpTitle(persm.get("ptitle").toString());} catch (Exception e) {}
				try {cPo.setSignature(persm.get("personDescribe").toString());} catch (Exception e) {}
				try {cPo.setpTitle(persm.get("ptitle").toString());} catch (Exception e) {}
				try {cPo.setDescn(persm.get("personDescribe").toString());} catch (Exception e) {}
				try {cPo.setIsVerified(persm.get("isVerified").equals("True")?1:2);} catch (Exception e) {}
				String address = "";
				try {if (persm.get("province")!=null) {address += persm.get("province").toString()+"/";}} catch (Exception e) {}
				try {if (persm.get("city")!=null) {address += persm.get("city").toString();}} catch (Exception e) {}
				if (address.length()>1) {
					cPo.setLocation(address);
				}
				try {cPo.setPortrait(persm.get("mobileLargeLogo").toString());} catch (Exception e) {}
				// 0保密 1 男性 2女性
				try {cPo.setSex(Integer.valueOf(persm.get("gender").toString()));} catch (Exception e) {}
				CPersonService cPersonService = (CPersonService) SpringShell.getBean("CPersonService");
				try {
					CPersonPo exsitcPo = cPersonService.getCPersonByPersonId(cPo.getpSource(), cPo.getpSrcId());
					if (exsitcPo==null) {
						cPersonService.insertPerson(cPo);
						exsitcPo = cPo;
					}
				} catch (Exception e) {}
				
				CPersonRefPo cPersonrefPo = new CPersonRefPo();
				cPersonrefPo.setId(SequenceUUID.getPureUUID());
				cPersonrefPo.setRefName("主播-专辑");
				cPersonrefPo.setPersonId(cPo.getpSrcId());
				cPersonrefPo.setResTableName("c_Album");
				cPersonrefPo.setResId(albumPo.getAlbumId());
				cPersonService.insertPersonRef(cPersonrefPo);
			} catch (Exception e) {
				e.printStackTrace();
			}
//			List<AudioPo> aus = new ArrayList<>();
			List<Map<String, Object>> ls = (List<Map<String, Object>>) tracks.get("list");
			if (ls!=null && ls.size()>0) {
				for (int i = 0; i < ls.size(); i++) {
					Map<String, Object> audiomap = ls.get(i);
					AudioPo audioPo = new AudioPo();
					audioPo.setId(SequenceUUID.getPureUUID());
					audioPo.setAlbumId(albumPo.getAlbumId());
					audioPo.setAlbumName(albumPo.getAlbumName());
					audioPo.setAudioId(audiomap.get("trackId")+"");
					audioPo.setAudioName(audiomap.get("title")+"");
					audioPo.setAudioImg(audiomap.get("coverLarge")+"");
					audioPo.setPlayCount(audiomap.get("playtimes")+"");
					audioPo.setAudioPublisher("喜马拉雅");
					audioPo.setCategoryId(albumPo.getCategoryId());
					audioPo.setCategoryName(albumPo.getCategoryName());
					audioPo.setAudioURL(audiomap.get("playUrl64")+"");
					audioPo.setDuration(audiomap.get("duration")+"000"); //接口得到播放时长单位为秒
					audioPo.setVisitUrl("http://www.ximalaya.com/"+userId+"/sound/"+audioPo.getAudioId());
					audioPo.setColumnNum(ls.size()-i);
					audioPo.setCrawlerNum("1");
					
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
						
						//加载转发数
						if (doc!=null) {
							try {
								String forwards = doc.select("a[class=forwardBtn link1]").get(0).select("span").get(0).html();
								CForwardPo cForwardPo = new CForwardPo();
								cForwardPo.setId(SequenceUUID.getPureUUID());
								cForwardPo.setPublisher("喜马拉雅");
								cForwardPo.setResTableName("c_Audio");
								cForwardPo.setResId(audioPo.getAudioId());
								cForwardPo.setForwardCount(Long.valueOf(forwards));
								CForwardService cForwardService = (CForwardService) SpringShell.getBean("CForwardService");
								cForwardService.insertCForward(cForwardPo);
							} catch (Exception e) {}
						}
					} catch (Exception e) {}
					
					try {audioPo.setcTime(new Timestamp(Long.valueOf(audiomap.get("createdAt")+"")));} catch (Exception e) {}
					AudioService audioService = (AudioService) SpringShell.getBean("audioService");
					audioService.insertAudio(audioPo);
//					aus.add(audioPo);
					//节目喜欢数入库
					try {
						CFavoritePo cFavoritePo = new CFavoritePo();
						cFavoritePo.setId(SequenceUUID.getPureUUID());
						cFavoritePo.setPublisher("喜马拉雅");
						cFavoritePo.setResTableName("c_Audio");
						cFavoritePo.setResId(audioPo.getAudioId());
						cFavoritePo.setFavoriteCount(Long.valueOf(audiomap.get("likes").toString()));
						CFavoriteService cFavoriteService = (CFavoriteService) SpringShell.getBean("CFavoriteService");
						cFavoriteService.insertCFavorite(cFavoritePo);
					} catch (Exception e) {
						e.printStackTrace();
					}
					//播放次数入库
					try {
						cPlayCountPo = new CPlayCountPo();
						cPlayCountPo.setId(SequenceUUID.getPureUUID());
						cPlayCountPo.setPublisher("喜马拉雅");
						cPlayCountPo.setResTableName("c_Audio");
						cPlayCountPo.setResId(audioPo.getAudioId());
						cPlayCountPo.setPlayCount(Long.valueOf(audioPo.getPlayCount()));
						cPlayCountService.insertCPlayCount(cPlayCountPo);
					} catch (Exception e) {
						e.printStackTrace();
					}
					try {
						CCommentPo cCommentPo = new CCommentPo();
						cCommentPo.setId(SequenceUUID.getPureUUID());
						cCommentPo.setPublisher("喜马拉雅");
						cCommentPo.setResTableName("c_Audio");
						cCommentPo.setResId(audioPo.getAudioId());
						cCommentPo.setCommentCount(Long.valueOf(audiomap.get("comments").toString()));
						CCommentService commentService = (CCommentService) SpringShell.getBean("CCommentService");
						commentService.insertCComment(cCommentPo);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
//				if (aus!=null && aus.size()>0) {
//					AudioService audioService = (AudioService) SpringShell.getBean("audioService");
//					audioService.insertAudioList(aus);
//				}
			}
		}
		return albumPo.getId();
	}
}
