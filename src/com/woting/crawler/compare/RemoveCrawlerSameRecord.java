package com.woting.crawler.compare;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.spiritdata.framework.core.cache.SystemCache;
import com.woting.crawler.CrawlerConstants;
import com.woting.crawler.core.album.persis.po.AlbumPo;
import com.woting.crawler.core.album.service.AlbumService;
import com.woting.crawler.core.audio.persis.po.AudioPo;
import com.woting.crawler.core.audio.service.AudioService;
import com.woting.crawler.core.scheme.model.Scheme;
import com.woting.crawler.ext.SpringShell;

public class RemoveCrawlerSameRecord {
	private String crawlerNum;
	private AlbumService albumService;
	private AudioService audioService;

	public RemoveCrawlerSameRecord() {
		Scheme scheme = (Scheme) SystemCache.getCache(CrawlerConstants.SCHEME).getContent();
		this.crawlerNum = scheme.getSchemenum();
	}

	public void startCleanData() {
		albumService = (AlbumService) SpringShell.getBean("albumService");
		audioService = (AudioService) SpringShell.getBean("audioService");
		int num = Integer.valueOf(crawlerNum);
		int pageSize = 1000;
		if (num >= 2) {
			// 专辑清理
			for (int i = 1; i < num; i++) {
				try {
					List<AlbumPo> als = albumService.getAlbumList(i + "");
					if (als != null && als.size() > 0) {
						for (int j = 0; j < als.size(); j++) {
							AlbumPo oldal = als.get(j);
							for (int k = i + 1; k <= num; k++) {
								try {
									Map<String, Object> m = new HashMap<>();
									m.put("albumId", oldal.getAlbumId());
									m.put("albumName", oldal.getAlbumName());
									m.put("albumPublisher", oldal.getAlbumPublisher());
									m.put("crawlerNum", k);
									List<AlbumPo> albs = albumService.getAlbumListBy(m);
									if (albs != null) {
										for (AlbumPo albumPo : albs) {
											try {
												if (oldal.getAlbumImg().equals(albumPo.getAlbumImg())
														&& oldal.getAlbumTags().equals(albumPo.getAlbumTags())
														&& oldal.getDescn().equals(albumPo.getDescn())
														&& oldal.getCategoryName().equals(albumPo.getCategoryName())) {
													albumService.removeAlbumById(albumPo.getId());
												}
											} catch (Exception e) {
												e.printStackTrace();
												continue;
											}
										}
									}
								} catch (Exception e) {
									e.printStackTrace();
									continue;
								}
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					continue;
				}
			}
			// 节目清理
			for (int i = 1; i < num; i++) {
				try {
					int count = audioService.getAudioNum(i + "");
					for (int j = 0; j < count / 1000 + 1; j++) {
						List<AudioPo> aus = audioService.getAudioList(j * pageSize, pageSize, i + "");
						if (aus != null) {
							for (int k = 0; k < aus.size(); k++) {
								try {
									AudioPo oldau = aus.get(k);
									for (int l = i + 1; l <= num; l++) {
										try {
											Map<String, Object> m = new HashMap<>();
											m.put("audioId", oldau.getAudioId());
											m.put("audioPublisher", oldau.getAudioPublisher());
											m.put("crawlerNum", l);
											List<AudioPo> auds = audioService.getAudios(m);
											if (auds != null) {
												for (AudioPo audioPo : auds) {
													String oldstr = oldau.getAudioImg()==null?"":oldau.getAudioImg()+oldau.getAudioTags()==null?"":oldau.getAudioTags()+oldau.getCategoryName()==null?"":oldau.getCategoryName()+oldau.getAudioURL()+oldau.getDescn()==null?"":oldau.getDescn();
													String newstr = audioPo.getAudioImg()==null?"":audioPo.getAudioImg()+audioPo.getAudioTags()==null?"":audioPo.getAudioTags()+audioPo.getCategoryName()==null?"":audioPo.getCategoryName()+audioPo.getAudioURL()+audioPo.getDescn()==null?"":audioPo.getDescn();
													System.out.println(oldstr);
													System.out.println(newstr);
													if (oldstr.equals(newstr)) {
														audioService.removeSameAudio(audioPo.getId());
													}
												}
											}
										} catch (Exception e) {
											e.printStackTrace();
											continue;
										}
									}
								} catch (Exception e) {
									e.printStackTrace();
									continue;
								}
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					continue;
				}
			}
		}
	}
}
