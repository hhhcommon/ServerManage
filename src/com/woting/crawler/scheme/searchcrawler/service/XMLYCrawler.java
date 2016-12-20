package com.woting.crawler.scheme.searchcrawler.service;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.spiritdata.framework.core.cache.SystemCache;
import com.spiritdata.framework.util.JsonUtils;
import com.spiritdata.framework.util.SequenceUUID;
import com.woting.cm.core.channel.persis.po.ChannelPo;
import com.woting.cm.core.channel.service.ChannelService;
import com.woting.cm.core.media.persis.po.MediaAssetPo;
import com.woting.cm.core.media.persis.po.SeqMediaAssetPo;
import com.woting.cm.core.media.service.MediaService;
import com.woting.crawler.CrawlerConstants;
import com.woting.crawler.ext.SpringShell;
import com.woting.crawler.scheme.searchcrawler.model.Festival;
import com.woting.crawler.scheme.searchcrawler.utils.SearchUtils;
import com.woting.crawler.scheme.utils.ConvertUtils;
import com.woting.crawler.scheme.utils.FileUtils;

public class XMLYCrawler {

	@SuppressWarnings("unchecked")
	public Map<String, Object> ParseAlbum(Festival festival) {
		try {
			//加载专辑信息
			Document doc = Jsoup.connect("http://www.ximalaya.com/"+festival.getPersonId()+"/album/"+festival.getAlbumId()).ignoreContentType(true).timeout(5000).get();
			if (doc!=null) {
				Elements eles = null;
				Element e = null;
				SeqMediaAssetPo sma = new SeqMediaAssetPo();
				// 得到名称、ID、img
				try {
					eles = doc.select("div.personal_body").select("div.left").select("img");
					if (eles != null && !eles.isEmpty()) {
						e = eles.get(0);
						sma.setSmaTitle(e.attr("alt").trim());
						sma.setSmaImg(e.attr("src").trim());
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				// 类别
				try {
					eles = doc.select("div.detailContent_category");
					if (eles != null && !eles.isEmpty()) {
						e = eles.get(0);
//						parseData.put("categoryName", e.select("a").get(0).html().trim().replace("【", "").replace("】", ""));
//						parseData.put("playUrl", e.select("a").get(0).attr("href").trim());
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				// 标签
				try {
					String tags = "";
					eles = doc.select("div.tagBtnList");
					if (eles != null && !eles.isEmpty()) {
						eles = eles.select("span");
						for (int i = 0; i < eles.size(); i++) {
							e = eles.get(i);
							tags += "," + e.select("span").html();
						}
					}
					if (tags.length() > 0)
//						parseData.put("tags", tags.substring(1).trim());
						sma.setKeyWords(tags.substring(1).trim());
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				// 播放数
				try {
					eles = doc.select("div.detailContent_playcountDetail");
					if (eles != null && !eles.isEmpty()) {
//						parseData.put("playCount", XMLYParseUtils.getFirstNum(eles.select("span").html()));
						
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				// 描述
				try {
					eles = doc.select("div.detailContent_intro");
					if (eles != null && !eles.isEmpty()) {
//						parseData.put("descript", StringEscapeUtils.unescapeHtml4(eles.select("div.mid_intro").select("article").get(0).html().trim()));
						sma.setDescn(StringEscapeUtils.unescapeHtml4(eles.select("div.mid_intro").select("article").get(0).html().trim()));
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				// 专辑
//				try {
//					eles = doc.select("link[rel=canonical]");
//					if (eles != null && !eles.isEmpty()) {
//						String zhuboid = eles.get(0).attr("href").trim();
//						zhuboid = zhuboid.substring(zhuboid.lastIndexOf("/")+1, zhuboid.length());
//						parseData.put("albumId", zhuboid);
//					}
//				} catch (Exception ex) {
//					ex.printStackTrace();
//				}
				sma.setId(SequenceUUID.getPureUUID());
				sma.setSmaPubType(1);
				sma.setSmaPubId("2");
				sma.setSmaPublisher("喜马拉雅");
				sma.setSmaPublishTime(new Timestamp(System.currentTimeMillis()));
				MediaService mediaService = (MediaService) SpringShell.getBean("mediaService");
				List<SeqMediaAssetPo> smas = mediaService.getSeqInfo(sma.getSmaTitle(), sma.getSmaPublisher());
				ChannelService channelService = (ChannelService) SpringShell.getBean("channelService");
				if (smas!=null && smas.size()>0) {
					sma = smas.get(0);
					Map<String, Object> m = new HashMap<>();
					m.put("maPublisher", sma.getSmaPublisher());
					m.put("maTitle", festival.getAudioName());
					m.put("whereSql", " id in (select mId from wt_SeqMa_Ref where sId = '"+sma.getId()+"')");
					List<MediaAssetPo> mas = mediaService.getMaList(m);
					if (mas!=null && mas.size()>0) {
						MediaAssetPo ma = mas.get(0);
						Map<String, Object> retM = new HashMap<>();
						retM.put("ContentId", ma.getId());
						retM.put("ContentImg", ma.getMaImg());
						retM.put("ContentName", ma.getMaTitle());
						retM.put("ContentPlay", ma.getMaURL());
						retM.put("ContentPub", ma.getMaPublisher());
						retM.put("PlayCount", "1234");
						return retM;
					} else {
						List<Map<String, Object>> cate2dictdlist = FileUtils.readFileByJson(SystemCache.getCache(CrawlerConstants.APP_PATH).getContent() + "conf/craw.txt");
						List<ChannelPo> chlist = channelService.getChannelList();
						Map<String, Object> retM = ConvertUtils.convert2MediaAsset(festival, sma, cate2dictdlist, chlist);
						return retM;
					}
				} else {
					
				}
			}
			//加载节目信息
			String jsonstr = SearchUtils.jsoupTOstr("http://m.ximalaya.com/album/more_tracks?url=%2Falbum%2Fmore_tracks&aid="+festival.getAlbumId());
			Map<String, Object> m = (Map<String, Object>) JsonUtils.jsonToObj(jsonstr, Map.class);
			if (m!=null && m.size()>0) {
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
