package com.woting.crawler.scheme.crawlersrc.DT.etl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.spiritdata.framework.core.cache.SystemCache;
import com.spiritdata.framework.ext.spring.redis.RedisOperService;
import com.spiritdata.framework.util.DateUtils;
import com.spiritdata.framework.util.SequenceUUID;
import com.woting.crawler.CrawlerConstants;
import com.woting.crawler.core.album.persis.po.AlbumPo;
import com.woting.crawler.core.audio.persis.po.AudioPo;
import com.woting.crawler.core.etl.service.Etl1Service;
import com.woting.crawler.core.scheme.model.Scheme;
import com.woting.crawler.ext.SpringShell;
import com.woting.crawler.scheme.utils.RedisUtils;

public class DTEtl1Process {

	public static void makeKLOrigDataList(int mediaType, Map<String, Object> parseData) {
		try {
			Thread.sleep(20);
			Scheme scheme = (Scheme) SystemCache.getCache(CrawlerConstants.SCHEME).getContent();
			RedisOperService rs = new RedisOperService(scheme.getJedisConnectionFactory(),scheme.getRedisSnapShootDB());
			String crawlerNum = scheme.getSchemenum();
			if (mediaType == 1) { // 专辑处理
				AlbumPo albumPo = new AlbumPo();
				albumPo.setId(SequenceUUID.getPureUUID());
				if (parseData.containsKey("albumId")) {
					albumPo.setAlbumId(parseData.get("albumId") + "");
				} else
					return;
				if (parseData.containsKey("albumName")) {
					albumPo.setAlbumName(parseData.get("albumName") + "");
				} else
					return;
				if (parseData.containsKey("albumImg")) {
					albumPo.setAlbumImg(parseData.get("albumImg") + "");
				} else
					return;
				if (RedisUtils.exeitsSnapShootInfo(rs, "DT::" + albumPo.getAlbumId())) {
					String cates = RedisUtils.getSnapShootInfo(rs, "DT::" + albumPo.getAlbumId());
					String[] cats = cates.split(",");
					if (cats != null && cats.length > 0) {
						String categoryId = "";
						String categoryName = "";
						for (String cat : cats) {
							String[] cas = cat.split("::");
							categoryId += "," + cas[0];
							categoryName += "," + cas[1];
						}
						categoryId = categoryId.substring(1);
						categoryName = categoryName.substring(1);
						albumPo.setCategoryId(categoryId);
						albumPo.setCategoryName(categoryName);
					} else
						return;
				} else
					return;
				if (parseData.containsKey("descript")) {
					albumPo.setDescn(parseData.get("descript").equals("null") ? null : parseData.get("descript") + "");
				}
				if (parseData.containsKey("playCount")) {
					albumPo.setPlayCount(
							parseData.get("playCount").equals("null") ? "1234" : parseData.get("playCount") + "");
				}
				if (parseData.containsKey("tags")) {
					albumPo.setAlbumTags(parseData.get("tags") + "");
				}
				if (parseData.containsKey("visitUrl")) {
					albumPo.setVisitUrl(parseData.get("visitUrl") + "");
				}
				albumPo.setAlbumPublisher("多听");
				if (parseData.containsKey("cTime")) {
					try {
						String ct = parseData.get("cTime") + "";
						Date date = DateUtils.getDateTime("yyyy-MM-dd", ct);
						albumPo.setcTime(new Timestamp(date.getTime()));
					} catch (Exception e) {
						e.printStackTrace();
						albumPo.setcTime(new Timestamp(System.currentTimeMillis()));
						albumPo.setCrawlerNum(crawlerNum);
						List<AlbumPo> als = new ArrayList<>();
						als.add(albumPo);
						Map<String, Object> m = new HashMap<>();
						m.put("albumlist", als);
						Etl1Service etl1Service = (Etl1Service) SpringShell.getBean("etl1Service");
						etl1Service.insertSqlAlbumAndAudio(m);
						rs.close();
					}
				} else {
					albumPo.setcTime(new Timestamp(System.currentTimeMillis()));
				}
				albumPo.setCrawlerNum(crawlerNum);
				List<AlbumPo> als = new ArrayList<>();
				als.add(albumPo);
				Map<String, Object> m = new HashMap<>();
				m.put("albumlist", als);
				Etl1Service etl1Service = (Etl1Service) SpringShell.getBean("etl1Service");
				etl1Service.insertSqlAlbumAndAudio(m);
				rs.close();
			} else {
				if (mediaType == 2) {
					AudioPo audioPo = new AudioPo();
					audioPo.setId(SequenceUUID.getPureUUID());
					if (parseData.containsKey("audioId")) {
						audioPo.setAudioId(parseData.get("audioId") + "");
					} else
						return;
					if (parseData.containsKey("audioName")) {
						audioPo.setAudioName(parseData.get("audioName") + "");
					} else
						return;
					if (parseData.containsKey("audioImg")) {
						audioPo.setAudioImg(parseData.get("audioImg") + "");
					} else
						return;
					if (parseData.containsKey("albumId") && !parseData.get("albumId").equals("null")
							&& RedisUtils.exeitsSnapShootInfo(rs, "DT::" + parseData.get("albumId"))) {
						String cates = RedisUtils.getSnapShootInfo(rs, "DT::" + parseData.get("albumId"));
						String[] cats = cates.split(",");
						if (cats != null && cats.length > 0) {
							String categoryId = "";
							String categoryName = "";
							for (String cat : cats) {
								String[] cas = cat.split("::");
								categoryId += "," + cas[0];
								categoryName += "," + cas[1];
							}
							categoryId = categoryId.substring(1);
							categoryName = categoryName.substring(1);
							audioPo.setCategoryId(categoryId);
							audioPo.setCategoryName(categoryName);
							audioPo.setAlbumId(parseData.get("albumId") + "");
							audioPo.setAlbumName(parseData.get("albumName") + "");
						} else
							return;
					} else
						return;
					if (parseData.containsKey("descript")) {
						audioPo.setDescn(parseData.get("descript").equals("null") ? null : parseData.get("descript") + "");
					}
					if (parseData.containsKey("playCount")) {
						audioPo.setPlayCount(parseData.get("playCount").equals("null") ? "1234" : parseData.get("playCount") + "");
					}
					if (parseData.containsKey("tags")) {
						audioPo.setAudioTags(parseData.get("tags") + "");
					}
					if (parseData.containsKey("visitUrl")) {
						audioPo.setVisitUrl(parseData.get("visitUrl") + "");
					}
					if (parseData.containsKey("duration")) {
						String dr = parseData.get("duration") + "";
						String[] ts = dr.split(":");
						if (ts != null) {
							if (ts.length == 3) {
								long time = 0;
								time += Integer.valueOf(ts[0]) * 3600;
								time += Integer.valueOf(ts[1]) * 60;
								time += Integer.valueOf(ts[2]);
								time = time * 1000;
								audioPo.setDuration(time + "");
							}
							if (ts.length == 2) {
								long time = 0;
								time += Integer.valueOf(ts[0]) * 60;
								time += Integer.valueOf(ts[1]);
								time = time * 1000;
								audioPo.setDuration(time + "");
							}
						}
					} else {
						audioPo.setDuration("1234");
					}
					if (parseData.containsKey("playUrl") && !parseData.get("playUrl").equals("null")) {
						audioPo.setAudioURL(parseData.get("playUrl") + "");
					} else
						return;
					if (parseData.containsKey("cTime")) {
						try {
							String ct = parseData.get("cTime") + "";
							Date date = DateUtils.getDateTime("yyyy-MM-dd", ct);
							audioPo.setcTime(new Timestamp(date.getTime()));
						} catch (Exception e) {
							e.printStackTrace();
							audioPo.setcTime(new Timestamp(System.currentTimeMillis()));
							audioPo.setCrawlerNum(crawlerNum);
							audioPo.setAudioPublisher("多听");
							audioPo.setCrawlerNum(crawlerNum);
							List<AudioPo> audioPos = new ArrayList<>();
							audioPos.add(audioPo);
							Map<String, Object> m = new HashMap<>();
							m.put("audiolist", audioPos);
							Etl1Service etl1Service = (Etl1Service) SpringShell.getBean("etl1Service");
							etl1Service.insertSqlAlbumAndAudio(m);
							rs.close();
						}
					} else {
						audioPo.setcTime(new Timestamp(System.currentTimeMillis()));
					}
					audioPo.setAudioPublisher("多听");
					audioPo.setCrawlerNum(crawlerNum);
					List<AudioPo> audioPos = new ArrayList<>();
					audioPos.add(audioPo);
					Map<String, Object> m = new HashMap<>();
					m.put("audiolist", audioPos);
					Etl1Service etl1Service = (Etl1Service) SpringShell.getBean("etl1Service");
					etl1Service.insertSqlAlbumAndAudio(m);
					rs.close();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
