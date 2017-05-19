package com.woting.crawler.compare;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.SolrQuery.SortClause;
import org.bouncycastle.jce.provider.BrokenJCEBlockCipher.BrokePBEWithMD5AndDES;

import com.spiritdata.framework.ext.spring.redis.RedisOperService;
import com.spiritdata.framework.util.JsonUtils;
import com.woting.cm.core.media.persis.po.MediaAssetPo;
import com.woting.cm.core.media.persis.po.SeqMediaAssetPo;
import com.woting.cm.core.media.service.MediaService;
import com.woting.crawler.core.album.persis.po.AlbumPo;
import com.woting.crawler.core.audio.persis.po.AudioPo;
import com.woting.crawler.core.solr.persis.po.SolrInputPo;
import com.woting.crawler.core.solr.persis.po.SolrSearchResult;
import com.woting.crawler.core.solr.service.SolrJService;
import com.woting.crawler.ext.SpringShell;
import com.woting.crawler.scheme.crawlerdb.crawler.EtlProcess;
import com.woting.crawler.scheme.utils.FileUtils;
import com.woting.crawler.scheme.utils.RedisUtils;
import com.woting.crawler.scheme.utils.SolrUtils;

public class CompareAttribute {
	private MediaService mediaService;
	private String crawlernum;
	private float sameproportion = 0.8f;
	private RedisOperService rs;

	public CompareAttribute() {
//		this.crawlernum = crawlernum;
//		Scheme scheme = (Scheme) SystemCache.getCache(CrawlerConstants.SCHEME).getContent();
//		rs = scheme.getRedisOperService();
	}

	public SeqMediaAssetPo getSameSma(AlbumPo albumPo) {
		mediaService = (MediaService) SpringShell.getBean("mediaService");
		int smanum = mediaService.getSmaNum(albumPo.getAlbumPublisher());
		if (smanum > 0) {
			List<String> names = SolrServer.getAnalysis(albumPo.getAlbumName());
			List<SeqMediaAssetPo> smalist = mediaService.getSmaByNames(names, albumPo.getAlbumPublisher());
			if (smalist != null && smalist.size() > 0) {
				compareSmaTitle(albumPo, smalist, crawlernum);
			}
			String fstr = RedisUtils.getCompareMaxProportion(rs,albumPo, crawlernum);
			if (fstr == null || fstr.equals("null")) {
				return null;
			} else {
				float maxf = Float.valueOf(fstr);
				if (maxf >= sameproportion) {
					String seqid = RedisUtils.getCompareSameSrcId(rs, albumPo, crawlernum);
					if (seqid == null || seqid.equals("null")) {
						return null;
					} else {
						SeqMediaAssetPo sma = mediaService.getSmaById(seqid);
						if (sma != null)
							return sma;
					}
				}
			}
		}
		return null;
	}
	
	public MediaAssetPo getSameMa(AudioPo audioPo, SeqMediaAssetPo sma) {
		mediaService = (MediaService) SpringShell.getBean("mediaService");
		int manum = mediaService.getSeqMaNumBySid(sma.getId());
		List<String> names = SolrServer.getAnalysis(audioPo.getAudioName());
		if(manum>0) {
			List<MediaAssetPo> malist = mediaService.getMaBySmaId(sma.getId(),names);
			if(malist!=null&&malist.size()>0) {
				compareMaTitle(audioPo, malist, crawlernum);
			}
		}
		String fstr = RedisUtils.getCompareMaxProportion(rs,audioPo, crawlernum);
		if (fstr == null || fstr.equals("null")) {
			return null;
		} else {
			float maxf = Float.valueOf(fstr);
			if(maxf >= sameproportion) {
				String maid = RedisUtils.getCompareSameSrcId(rs, audioPo, crawlernum);
				if (maid == null || maid.equals("null")) {
					return null;
				} else {
					MediaAssetPo ma = mediaService.getMaInfoById(maid);
					if (ma != null)
						return ma;
				}
			}
		}
		return null;
	}

	/**
	 * 
	 * @param album
	 * @param smalist
	 * @param crawlernum
	 */
	public void compareSmaTitle(AlbumPo album, List<SeqMediaAssetPo> smalist, String crawlernum) {
		String maxf = RedisUtils.getCompareMaxProportion(rs, album, crawlernum);
		if (maxf != null && maxf.equals("1.0")) {
			return;
		}
		if (album != null || (smalist != null && smalist.size() > 0)) {
			for (SeqMediaAssetPo sma : smalist) {
				float f = 0;
				if (album.getAlbumName().equals(sma.getSmaTitle())) { // 名称相同,置f=1,最高相似比
					f = 1.0f;
					RedisUtils.writeCompareInfo(rs, album, f, sma.getId(), crawlernum);
					break;
				} else {
					f = SolrServer.getSameProportion(rs, album.getAlbumName(), sma);
					if (maxf == null || maxf.equals("null"))
						RedisUtils.writeCompareInfo(rs, album, f, sma.getId(), crawlernum);
					else {
						if (f > Float.valueOf(maxf)) {
							RedisUtils.writeCompareInfo(rs, album, f, sma.getId(), crawlernum);
						}
					}
				}
			}
		}
	}
	
	
	public void compareMaTitle(AudioPo audio, List<MediaAssetPo> malist, String crawlernum) {
		String maxf = RedisUtils.getCompareMaxProportion(rs, audio, crawlernum);
		if (maxf != null && maxf.equals("1.0")) {
			return;
		}
		if (audio !=null || (malist != null && malist.size()>0)) {
			for (MediaAssetPo ma : malist) {
				float f = 0;
				if(audio.getAudioName().equals(ma.getMaTitle())) {
					f = 1.0f;
					RedisUtils.writeCompareInfo(rs, audio, f, ma.getId(), crawlernum);
					break;
				} else {
					f = SolrServer.getSameProportion(rs, audio.getAudioName(), ma);
					if (maxf == null || maxf.equals("null"))
						RedisUtils.writeCompareInfo(rs, audio, f, ma.getId(), crawlernum);
					else {
						if (f > Float.valueOf(maxf)) {
							RedisUtils.writeCompareInfo(rs, audio, f, ma.getId(), crawlernum);
						}
					}
				}
			}
		}
	}
	
	public float compareTitle(SolrJService solrJService, String autitle, String solrtitle) {
		if (solrJService!=null) {
			List<String> austr = solrJService.getAnalysis(autitle);
			List<String> solrstr = solrJService.getAnalysis(solrtitle);
			int num = 0;
			for (String str1 : austr) {
				for (String str2 : solrstr) {
					if(str1.equals(str2)) num++;
				}
			}
			try {
				return (float) ((num+0.0)*2/(austr.size()+solrstr.size()));
			} catch (Exception e) {
				return 0;
			}
		}
		return 0;
	}
	
	@SuppressWarnings("unchecked")
	public Map<String, Object> getSolrListToCompare(AlbumPo albumPo, boolean addMedia) {
		if (albumPo!=null) {
			SolrJService solrJService = (SolrJService) SpringShell.getBean("solrJService");
			try {
				List<SortClause> solrsorts = SolrUtils.makeSolrSort("score desc","item_meidasize desc");
				SolrSearchResult sResult = solrJService.solrSearch(albumPo.getAlbumName(), solrsorts, "*,score", 1, 5, "item_type:SEQU","-item_publisher:"+albumPo.getAlbumPublisher());
				if (sResult!=null) {
					List<SolrInputPo> solrips = sResult.getSolrInputPos();
					if (solrips!=null && solrips.size()>0) {
						for (SolrInputPo solrInputPo : solrips) {
							int mediasize =new Long(solrInputPo.getItem_mediasize()).intValue();
							List<AudioPo> aus = albumPo.getAudioPos(); // 获得待入库节目列表
							if (aus!=null && aus.size()>0) {
								if (mediasize>1) {
									long difnum = Math.abs(mediasize-aus.size());
									if ((difnum+0.0)/solrInputPo.getItem_mediasize()<0.2) {
										List<SolrInputPo> audios = solrJService.getAudioListByAlbumId(solrInputPo.getItem_id()); //获得已入库节目列表
										if (audios!=null && audios.size()>0) {
											List<Map<String, Object>> simls = new ArrayList<>(); //相似节目列表
											String smaId = solrInputPo.getItem_id();
											for (AudioPo au : aus) {
												if (au!=null) {
													List<SortClause> solrsort2s = SolrUtils.makeSolrSort("score desc","item_meidasize desc");
													SolrSearchResult sResult2 = solrJService.solrSearch(au.getAudioName(), solrsort2s, "*,score", 1, mediasize, "item_type:AUDIO", "item_pid:"+solrInputPo.getItem_id());
													List<SolrInputPo> ausolrs = sResult2.getSolrInputPos();
													long autimelong = Long.valueOf(au.getDuration());
													if (ausolrs!=null && ausolrs.size()>0) {
														float maxpernum = 0;
														String perId = "";
														String pertitle = "";
														for (SolrInputPo solrInputPo2 : ausolrs) {
															long timelong = solrInputPo2.getItem_timelong();
															long differlong = Math.abs(timelong-autimelong);
															long permitlong = Math.round(timelong*0.3);
															if (differlong < permitlong) {
																if (differlong <= 1000) {
																	System.out.println(au.getAudioName() + "    " + solrInputPo2.getItem_title());
																	if (au.getAudioName().equals(solrInputPo2.getItem_title())) {
																		maxpernum = 2;
																		perId = solrInputPo2.getItem_id();
																		pertitle = solrInputPo2.getItem_title();
																	} else {
																		float pernum = compareTitle(solrJService, au.getAudioName(), solrInputPo2.getItem_title());
																		if (pernum==1) {
																			maxpernum = 2;
																			perId = solrInputPo2.getItem_id();
																			pertitle = solrInputPo2.getItem_title();
																		} else {
																			if (pernum>0.365 && pernum > maxpernum) {
																				maxpernum = (float) ((pernum+1)/2);
																				perId = solrInputPo2.getItem_id();
																				pertitle = solrInputPo2.getItem_title();
																			}
																		}
																	}
																} else {
																	System.out.println(au.getAudioName() + "    " + solrInputPo2.getItem_title());
																	float pernum = compareTitle(solrJService, au.getAudioName(), solrInputPo2.getItem_title());
																	pernum = (float) ((pernum*1.3+(float)((permitlong - differlong+0.0)/permitlong)*0.7+0.0)/2);
																	if (pernum>=0.8 && pernum > maxpernum) {
																		maxpernum = pernum;
																		perId = solrInputPo2.getItem_id();
																		pertitle = solrInputPo2.getItem_title();
																	}
																}
															}
														}
														if (maxpernum>=0.8 && perId!=null) {
															Map<String, Object> m = new HashMap<>();
															m.put("titles", au.getAudioName() + "    " + pertitle);
//															m.put("zjtitle", au.getAlbumName() + "    " + solrInputPo.getItem_title());
															m.put("perId", perId);
															m.put("audioId", au.getId());
															m.put("pernum", maxpernum);
															simls.add(m);
														}
													}
												}
											}
											int differnum = (int) Math.round((aus.size()*0.5));
											if (differnum>=1) {
												if (simls!=null && simls.size()>=differnum) {
													String oldtitlestr = "";
													boolean isok = true;
													for (Map<String, Object> map : simls) {
														String[] titles = map.get("titles").toString().split("    ");
														if (!oldtitlestr.contains(titles[1])) oldtitlestr += titles[1];
														else isok = false;
													}
													if (isok) {
														File file = FileUtils.createFile("/opt/wtcs/sim.txt");
														String lsstr = FileUtils.readFile(file);
														List<Object> ls = new ArrayList<>();
														if (lsstr!=null && lsstr.length()>0) {
															try {
																ls = (List<Object>) JsonUtils.jsonToObj(lsstr, List.class);												
															    ls.add(simls);
															} catch (Exception e) {}
														} else {
															ls.add(simls);
														}
														try {
															FileUtils.writeFile(JsonUtils.objToJson(ls), file);
														} catch (Exception e) {e.printStackTrace();}
														Map<String, Object> param = new HashMap<>();
														param.put("albumPo", albumPo);
														param.put("smaId", smaId);
														param.put("simls", simls);
														return param;
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		return null;
	}
	
	public Map<String, Object> getSolrListToCompareMedia(String smaId, String maId) {
		try {
			SolrJService solrJService = (SolrJService) SpringShell.getBean("solrJService");
			mediaService = (MediaService) SpringShell.getBean("mediaService");
			MediaAssetPo ma = mediaService.getMaInfoById(maId);
			if (ma==null) return null;
			SolrSearchResult sResult = solrJService.solrSearch(null, null, "*,score", 1, 5, "item_type:SEQU","item_id:"+smaId);
			if (sResult==null) return null;
			SolrInputPo solrInputPo = sResult.getSolrInputPos().get(0);
			if (solrInputPo==null) return null;
			int mediasize =new Long(solrInputPo.getItem_mediasize()).intValue();
			List<SortClause> solrsort2s = SolrUtils.makeSolrSort("score desc");
			SolrSearchResult sResult2 = solrJService.solrSearch(ma.getMaTitle(), solrsort2s, "*,score", 1, mediasize, "item_type:AUDIO", "item_pid:"+solrInputPo.getItem_id());
			if (sResult2==null) return null;
			List<SolrInputPo> ausolrs = sResult2.getSolrInputPos();
			if (ausolrs==null || ausolrs.size()==0) return null;
			long autimelong = Long.valueOf(ma.getTimeLong());
			List<Map<String, Object>> simls = new ArrayList<>(); //相似节目列表
			float maxpernum = 0;
			String perId = "";
			String pertitle = "";
			for (SolrInputPo solrInputPo2 : ausolrs) {
				long timelong = solrInputPo2.getItem_timelong();
				long differlong = Math.abs(timelong-autimelong);
				long permitlong = Math.round(timelong*0.3);
				if (differlong > permitlong) continue;
				if (differlong <= 1000) {
					System.out.println(ma.getMaTitle() + "    " + solrInputPo2.getItem_title());
					if (ma.getMaTitle().equals(solrInputPo2.getItem_title())) {
						maxpernum = 2;
						perId = solrInputPo2.getItem_id();
						pertitle = solrInputPo2.getItem_title();
					} else {
						float pernum = compareTitle(solrJService, ma.getMaTitle(), solrInputPo2.getItem_title());
						if (pernum==1) {
							maxpernum = 2;
							perId = solrInputPo2.getItem_id();
							pertitle = solrInputPo2.getItem_title();
						} else {
							if (pernum>0.365 && pernum > maxpernum) {
								maxpernum = (float) ((pernum+1)/2);
								perId = solrInputPo2.getItem_id();
								pertitle = solrInputPo2.getItem_title();
							}
						}
					}
				} else {
					System.out.println(ma.getMaTitle() + "    " + solrInputPo2.getItem_title());
					float pernum = compareTitle(solrJService, ma.getMaTitle(), solrInputPo2.getItem_title());
					pernum = (float) ((pernum*1.3+(float)((permitlong - differlong+0.0)/permitlong)*0.7+0.0)/2);
					if (pernum>=0.8 && pernum > maxpernum) {
						maxpernum = pernum;
						perId = solrInputPo2.getItem_id();
						pertitle = solrInputPo2.getItem_title();
					}
				}
			}
			if (maxpernum>=0.8 && perId!=null) {
				Map<String, Object> m = new HashMap<>();
				m.put("titles", ma.getMaTitle() + "    " + pertitle);
				m.put("perId", perId);
				m.put("maId", ma.getId());
				m.put("pernum", maxpernum);
				return m;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
