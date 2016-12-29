package com.woting.crawler.scheme.searchcrawler.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import com.spiritdata.framework.ext.spring.redis.RedisOperService;
import com.spiritdata.framework.util.SequenceUUID;
import com.spiritdata.framework.util.StringUtils;
import com.woting.crawler.core.album.model.Album;
import com.woting.crawler.core.album.persis.po.AlbumPo;
import com.woting.crawler.core.audio.persis.po.AudioPo;
import com.woting.crawler.ext.SpringShell;
import com.woting.crawler.scheme.searchcrawler.utils.SearchUtils;
import com.woting.crawler.scheme.utils.ConvertUtils;
import com.woting.crawler.scheme.utils.HttpUtils;

public class QTSearch extends Thread {
	private static int S_S_NUM = 5; // 搜索频道的数目
	private static int F_NUM = 5; // 搜索节目的数目 以上排列顺序按照搜索到的排列顺序
	private String content;
	private Map<String, Object> resultmap = new HashMap<>();
	private int okNum = 0;

	public QTSearch(String content) {
		this.content = content;
	}
	
	public QTSearch() {
		
	}

	@SuppressWarnings("unchecked")
	public void qtSeqSearch() {
		String url = "http://i.qingting.fm/wapi/search?k=" + content + "&groups=channel_ondemand&type=newcms&page=1&pagesize=" + S_S_NUM;
		try {
			Map<String, Object> seqm = HttpUtils.getJsonMapFromURL(url);
			if (seqm != null && seqm.size() > 0) {
				JedisConnectionFactory conn = (JedisConnectionFactory) SpringShell.getBean("connectionFactorySearch");
				RedisOperService roService = new RedisOperService(conn);
				Map<String, Object> datam = (Map<String, Object>) seqm.get("data");
				List<Map<String, Object>> datal = (List<Map<String, Object>>) datam.get("data");
				if (datal != null && datal.size() > 0) {
					datam = datal.get(0);
					datam = (Map<String, Object>) datam.get("doclist");
					datal = (List<Map<String, Object>>) datam.get("docs");
					if (datal != null && datal.size() > 0) {
						for (Map<String, Object> m : datal) {
							AlbumPo albumPo = new AlbumPo();
							albumPo.setId(SequenceUUID.getPureUUID());
							albumPo.setAlbumId(m.get("id") + "");
							albumPo.setAlbumName(m.get("title") + "");
							albumPo.setAlbumImg(m.get("cover") + "");
							albumPo.setAlbumPublisher("蜻蜓");
							String keywords = m.get("keywords") + "";
							if (!StringUtils.isNullOrEmptyOrSpace(keywords) && !keywords.equals("null")) {
								String[] kws = keywords.split(" ");
								String kks = "";
								if (kws.length > 0) {
									for (int i = 0; i < kws.length; i++) {
										kks += "," + kws[i];
									}
									kks = kks.substring(1);
									albumPo.setAlbumTags(kks);
								}
							}
							albumPo.setCategoryId(m.get("category_id") + "");
							albumPo.setCategoryName(m.get("category_name") + "");
							albumPo.setcTime(new Timestamp(Long.valueOf(m.get("updatetime") + "000")));
							albumPo.setDescn(m.get("description") + "");
							albumPo.setPlayCount(ConvertUtils.convertPlayNum2Long(m.get("playcount") + ""));
							url = "http://api2.qingting.fm/v6/media/channelondemands/" + albumPo.getAlbumId() + "/programs/order/0/curpage/1/pagesize/5";
							Map<String, Object> aums = HttpUtils.getJsonMapFromURL(url);
							List<Map<String, Object>> auls = (List<Map<String, Object>>) aums.get("data");
							if (auls != null && auls.size() > 0) {
								List<AudioPo> audioPos = new ArrayList<>();
								Album album = new Album();
								for (Map<String, Object> am : auls) {
									AudioPo aPo = new AudioPo();
									aPo.setId(SequenceUUID.getPureUUID());
									aPo.setAudioId(am.get("id") + "");
									aPo.setAudioName(am.get("title") + "");
									aPo.setAudioImg(albumPo.getAlbumImg());
									aPo.setCategoryId(albumPo.getCategoryId());
									aPo.setCategoryName(albumPo.getCategoryName());
									aPo.setAlbumId(albumPo.getAlbumId());
									aPo.setAlbumName(albumPo.getAlbumName());
									aPo.setAudioPublisher("蜻蜓");
									aPo.setDuration(am.get("duration")==null?null:am.get("duration")+"");
									aPo.setcTime(new Timestamp(ConvertUtils.makeLongTime(am.get("update_time") + "")));
									Map<String, Object> ms = (Map<String, Object>) am.get("mediainfo");
									if (ms!=null && ms.size()>0) {
										List<Map<String, Object>> pls = (List<Map<String, Object>>) ms.get("bitrates_url");
									    if (pls != null && pls.size() > 0) {
										    aPo.setAudioURL("http://od.qingting.fm/" + pls.get(0).get("file_path"));
										    audioPos.add(aPo);
									    }
									}
								}
								if (audioPos!=null && audioPos.size()>0) {
									album.setAlbumPo(albumPo);
								    album.setAudiolist(audioPos);
								    resultmap.put(albumPo.getAlbumId(), album);
								}
							}
						}
					}
				}
				roService.close();
				conn.destroy();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			okNum++;
		}
	}
	
	public Album albumS(String albumId) {
		String url = "http://i.qingting.fm/wapi/channels/"+albumId;
		Map<String, Object> m = HttpUtils.getJsonMapFromURL(url);
		if (m!=null && m.size()>0) {
			m = (Map<String, Object>) m.get("data");
			AlbumPo albumPo = new AlbumPo();
			albumPo.setId(SequenceUUID.getPureUUID());
			albumPo.setAlbumId(m.get("id")+"");
			albumPo.setAlbumName(m.get("name")+"");
			albumPo.setAlbumImg(m.get("img_url")+"");
			albumPo.setAlbumPublisher("蜻蜓");
			albumPo.setCategoryId(m.get("category_id")+"");
			albumPo.setPlayCount(m.get("playcount")==null?"14562":m.get("playcount")+"");
			albumPo.setDescn(m.get("desc")==null?null:m.get("desc")+"");
			albumPo.setcTime(new Timestamp(ConvertUtils.makeLongTime(m.get("update_time")+"")));
			url = "http://api2.qingting.fm/v6/media/channelondemands/" + albumPo.getAlbumId() + "/programs/order/0/curpage/1/pagesize/10";
			Map<String, Object> aums = HttpUtils.getJsonMapFromURL(url);
			List<Map<String, Object>> auls = (List<Map<String, Object>>) aums.get("data");
			if (auls != null && auls.size() > 0) {
				List<AudioPo> audioPos = new ArrayList<>();
				Album album = new Album();
				for (Map<String, Object> am : auls) {
					AudioPo aPo = new AudioPo();
					aPo.setId(SequenceUUID.getPureUUID());
					aPo.setAudioId(am.get("id") + "");
					aPo.setAudioName(am.get("title") + "");
					aPo.setAudioImg(albumPo.getAlbumImg());
					aPo.setCategoryId(albumPo.getCategoryId());
					aPo.setCategoryName(albumPo.getCategoryName());
					aPo.setAlbumId(albumPo.getAlbumId());
					aPo.setAlbumName(albumPo.getAlbumName());
					aPo.setAudioPublisher("蜻蜓");
					aPo.setDuration(am.get("duration")==null?null:am.get("duration")+"");
					aPo.setcTime(new Timestamp(ConvertUtils.makeLongTime(am.get("update_time") + "")));
					Map<String, Object> ms = (Map<String, Object>) am.get("mediainfo");
					if (ms!=null && ms.size()>0) {
						List<Map<String, Object>> pls = (List<Map<String, Object>>) ms.get("bitrates_url");
					    if (pls != null && pls.size() > 0) {
						    aPo.setAudioURL("http://od.qingting.fm/" + pls.get(0).get("file_path"));
						    audioPos.add(aPo);
					    }
					}
				}
				if (audioPos!=null && audioPos.size()>0) {
					album.setAlbumPo(albumPo);
				    album.setAudiolist(audioPos);
				    return album;
				}
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public void qtMaSearch() {
		String url = "http://i.qingting.fm/wapi/search?k=" + content + "&groups=program_ondemand&type=newcms&page=1&pagesize=" + F_NUM;
		try {
			Map<String, Object> mam = HttpUtils.getJsonMapFromURL(url);
			if (mam != null && mam.size() > 0) {
				JedisConnectionFactory conn = (JedisConnectionFactory) SpringShell.getBean("connectionFactorySearch");
				RedisOperService roService = new RedisOperService(conn);
				Map<String, Object> datam = (Map<String, Object>) mam.get("data");
				List<Map<String, Object>> datal = (List<Map<String, Object>>) datam.get("data");
				if (datal != null && datal.size() > 0) {
					datam = datal.get(0);
					datam = (Map<String, Object>) datam.get("doclist");
					datal = (List<Map<String, Object>>) datam.get("docs");
					if (datal != null && datal.size() > 0) {
						for (Map<String, Object> m : datal) {
							AudioPo audioPo = new AudioPo();
							audioPo.setId(SequenceUUID.getPureUUID());
							audioPo.setAudioId(m.get("id") + "");
							audioPo.setAudioName(m.get("title") + "");
							audioPo.setAudioImg(m.get("cover") + "");
							audioPo.setCategoryId(m.get("category_id") + "");
							if (m.get("parent_id")==null) {
								continue;
							}
							audioPo.setCategoryName(m.get("category_name") + "");
							if (m.get("parent_id")==null) {
								continue;
							}
							audioPo.setAlbumId(m.get("parent_id") + "");
							audioPo.setAlbumName(m.get("parent_name") + "");
							audioPo.setPlayCount(m.get("playcount") + "");
							audioPo.setcTime(new Timestamp(Long.valueOf(m.get("updatetime") + "000")));
							audioPo.setAudioPublisher("蜻蜓");
							audioPo.setVisitUrl("http://neo.qingting.fm/channels/" + audioPo.getAlbumId() + "/programs/" + audioPo.getAudioId());
							Map<String, Object>map = HttpUtils.getJsonMapFromURL("http://i.qingting.fm/wapi/channels/"+audioPo.getAlbumId()+"/programs/"+audioPo.getAudioId());
							if (map!=null) {
								map = (Map<String, Object>) map.get("data");
								if (!map.containsKey("file_path")) {
									continue;
								}
								audioPo.setAudioURL("http://od.qingting.fm/"+map.get("file_path")+"");
								String durstr = map.get("duration")+"";
								if (durstr.equals("null")) {
									audioPo.setDuration("1000");
								} else {
									if (durstr.contains(".")) {
										long d1 = (long) (Double.valueOf(durstr)/1);
										long d2 = (long) (Double.valueOf(durstr)%1*1000);
										audioPo.setDuration(d1*1000+d2+"");
									} else {
										audioPo.setDuration(Long.valueOf(durstr)+"");
									}
								}
								resultmap.put(audioPo.getAudioId(), audioPo);
							}
						}
					}
				}
				roService.close();
				conn.destroy();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			okNum++;
		}
	}

	@Override
	public void run() {
		try {
			new Thread(new Runnable() {
				public void run() {
					qtSeqSearch();
				}
			}).start();
			new Thread(new Runnable() {
				public void run() {
					qtMaSearch();
				}
			}).start();
			
			while (true) {
				Thread.sleep(20);
				if (okNum==2) {
					Map<String, Object> albummap = new HashMap<>();
					Map<String, Object> audiomap = new HashMap<>();
					for (String key : resultmap.keySet()) {
						if (resultmap.get(key)!=null) {
							try {
								Album album = (Album) resultmap.get(key);
								albummap.put(album.getAlbumPo().getAlbumId(), album);
							} catch (Exception e) {
								AudioPo audioPo = (AudioPo) resultmap.get(key);
								audiomap.put(audioPo.getAudioId(), audioPo);
								if (!albummap.containsKey(audioPo.getAlbumId())) {
									albummap.put(audioPo.getAlbumId(), null);
								}
								continue;
							}
						}
					}
					JedisConnectionFactory conn = (JedisConnectionFactory) SpringShell.getBean("connectionFactorySearch");
					RedisOperService roService = new RedisOperService(conn);
					for (String key : albummap.keySet()) {
						try {
							if (albummap.get(key)!=null) {
							    SearchUtils.addListInfo(content, (Album)albummap.get(key), roService);
						    }
						} catch (Exception e) {
							e.printStackTrace();
							continue;
						}
						
					}
					for (String key : audiomap.keySet()) {
						try {
							if (audiomap.get(key)!=null) {
							    SearchUtils.addListInfo(content, (AudioPo)audiomap.get(key), roService);
						    }
						} catch (Exception e) {
							e.printStackTrace();
							continue;
						}
					}
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			JedisConnectionFactory conn = (JedisConnectionFactory) SpringShell.getBean("connectionFactorySearch");
			RedisOperService roService = new RedisOperService(conn);
			SearchUtils.updateSearchFinish(content, roService);
			System.out.println("蜻蜓结束搜索");
		}

	}
}
