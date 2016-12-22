package com.woting.crawler.scheme.searchcrawler.service;

import java.sql.Timestamp;
import java.util.ArrayList;
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
	private static int S_S_NUM = 10; // 搜索频道的数目
	private static int F_NUM = 10; // 搜索节目的数目 以上排列顺序按照搜索到的排列顺序
	private String content;
	
	public QTSearch(String content) {
		this.content = content;
	}
	
	@SuppressWarnings("unchecked")
	public void qtSeqSearch() {
		String url = "http://i.qingting.fm/wapi/search?k="+content+"&groups=channel_ondemand&type=newcms&page=1&pagesize="+S_S_NUM;
		Map<String, Object> seqm = HttpUtils.getJsonMapFromURL(url);
		if (seqm!=null && seqm.size()>0) {
			JedisConnectionFactory conn = (JedisConnectionFactory) SpringShell.getBean("connectionFactorySearch");
			RedisOperService roService = new RedisOperService(conn);
			Map<String, Object> datam = (Map<String, Object>) seqm.get("data");
			List<Map<String, Object>> datal = (List<Map<String, Object>>) datam.get("data");
			if (datal!=null && datal.size()>0) {
				datam = datal.get(0);
				datam = (Map<String, Object>)datam.get("doclist");
				datal = (List<Map<String, Object>>) datam.get("docs");
				if (datal!=null && datal.size()>0) {
					for (Map<String, Object> m : datal) {
						AlbumPo albumPo = new AlbumPo();
						albumPo.setId(SequenceUUID.getPureUUID());
						albumPo.setAlbumId(m.get("id")+"");
						albumPo.setAlbumName(m.get("title")+"");
						albumPo.setAlbumImg(m.get("cover")+"");
						albumPo.setAlbumPublisher("蜻蜓");
						String keywords = m.get("keywords")+"";
						if (!StringUtils.isNullOrEmptyOrSpace(keywords) && !keywords.equals("null")) {
							String[] kws = keywords.split(" ");
							String kks = "";
							if (kws.length>0) {
								for (int i = 0; i < kws.length; i++) {
									kks += ","+kws[i];
								}
								kks = kks.substring(1);
								albumPo.setAlbumTags(kks);
							}
						}
						albumPo.setCategoryId(m.get("category_id")+"");
						albumPo.setCategoryName(m.get("category_name")+"");
						albumPo.setcTime(new Timestamp(Long.valueOf(m.get("updatetime")+"000")));
						albumPo.setDescn(m.get("description")+"");
						albumPo.setPlayCount(ConvertUtils.convertPlayNum2Long(m.get("playcount")+""));
						url = "http://api2.qingting.fm/v6/media/channelondemands/"+albumPo.getAlbumId()+"/programs/order/0/curpage/1/pagesize/10";
						Map<String, Object> aums = HttpUtils.getJsonMapFromURL(url);
						List<Map<String, Object>> auls = (List<Map<String, Object>>) aums.get("data");
						if (auls!=null && auls.size()>0) {
							List<AudioPo> audioPos = new ArrayList<>();
							Album album = new Album();
							for (Map<String, Object> am : auls) {
								AudioPo aPo = new AudioPo();
								aPo.setId(SequenceUUID.getPureUUID());
								aPo.setAudioId(am.get("id")+"");
								aPo.setAudioName(am.get("title")+"");
								aPo.setAudioImg(albumPo.getAlbumImg());
								aPo.setCategoryId(albumPo.getCategoryId());
								aPo.setCategoryName(albumPo.getCategoryName());
								aPo.setAlbumId(albumPo.getAlbumId());
								aPo.setAlbumName(albumPo.getAlbumName());
								aPo.setAudioPublisher("蜻蜓");
								aPo.setDuration(am.get("duration")+"");
								aPo.setcTime(new Timestamp(ConvertUtils.makeLongTime(am.get("update_time")+"")));
								Map<String, Object> ms = (Map<String, Object>) am.get("mediainfo");
								List<Map<String, Object>> pls = (List<Map<String, Object>>) ms.get("bitrates_url");
								if (pls!=null && pls.size()>0) {
									aPo.setAudioURL("http://od.qingting.fm/"+pls.get(0).get("file_path"));
								}
								audioPos.add(aPo);
							}
							album.setAlbumPo(albumPo);
							album.setAudiolist(audioPos);
							SearchUtils.addListInfo(content, album, roService);
						}
					}
				}
			}
			roService.close();
			conn.destroy();
		}
	}
	
	@SuppressWarnings("unchecked")
	public void qtMaSearch() {
		String url = "http://i.qingting.fm/wapi/search?k="+content+"&groups=program_ondemand&type=newcms&page=1&pagesize="+F_NUM;
		Map<String, Object> mam = HttpUtils.getJsonMapFromURL(url);
		if (mam!=null && mam.size()>0) {
			JedisConnectionFactory conn = (JedisConnectionFactory) SpringShell.getBean("connectionFactorySearch");
			RedisOperService roService = new RedisOperService(conn);
			Map<String, Object> datam = (Map<String, Object>) mam.get("data");
			List<Map<String, Object>> datal = (List<Map<String, Object>>) datam.get("data");
			if (datal!=null && datal.size()>0) {
				datam = datal.get(0);
				datam = (Map<String, Object>)datam.get("doclist");
				datal = (List<Map<String, Object>>) datam.get("docs");
				if (datal!=null && datal.size()>0) {
					for (Map<String, Object> m : datal) {
						AudioPo audioPo = new AudioPo();
						audioPo.setId(SequenceUUID.getPureUUID());
						audioPo.setAudioId(m.get("id")+"");
						audioPo.setAudioName(m.get("title")+"");
						audioPo.setAudioImg(m.get("cover")+"");
						audioPo.setCategoryId(m.get("category_id")+"");
						audioPo.setCategoryName(m.get("category_name")+"");
						audioPo.setAlbumId(m.get("parent_id")+"");
						audioPo.setAlbumName(m.get("parent_name")+"");
						audioPo.setPlayCount(m.get("playcount")+"");
						audioPo.setcTime(new Timestamp(Long.valueOf(m.get("updatetime")+"000")));
						audioPo.setAudioPublisher("蜻蜓");
						SearchUtils.addListInfo(content, audioPo, roService);
					}
				}
			}
			roService.close();
			conn.destroy();
		}
	}
	
	@Override
	public void run() {
		try {
			new Thread(new  Runnable() {
				public void run() {
					qtSeqSearch();
				}
			}).start();
			new Thread(new  Runnable() {
				public void run() {
					qtMaSearch();
				}
			}).start();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			JedisConnectionFactory conn = (JedisConnectionFactory) SpringShell.getBean("connectionFactorySearch");
			RedisOperService roService = new RedisOperService(conn);
			SearchUtils.updateSearchFinish(content, roService);
			roService.close();
			conn.destroy();
			System.out.println("蜻蜓结束搜索");
		}
		
	}
}
