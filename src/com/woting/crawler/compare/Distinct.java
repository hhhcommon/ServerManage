package com.woting.crawler.compare;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.spiritdata.framework.core.cache.SystemCache;
import com.spiritdata.framework.ext.spring.redis.RedisOperService;
import com.spiritdata.framework.util.SpiritRandom;
import com.woting.cm.core.media.persis.po.MediaAssetPo;
import com.woting.cm.core.media.persis.po.SeqMediaAssetPo;
import com.woting.cm.core.media.service.MediaService;
import com.woting.crawler.CrawlerConstants;
import com.woting.crawler.core.album.model.Album;
import com.woting.crawler.core.album.persis.po.AlbumPo;
import com.woting.crawler.core.album.service.AlbumService;
import com.woting.crawler.core.audio.persis.po.AudioPo;
import com.woting.crawler.core.audio.service.AudioService;
import com.woting.crawler.core.scheme.model.Scheme;
import com.woting.crawler.ext.SpringShell;
import com.woting.crawler.scheme.utils.RedisUtils;

public class Distinct {
	Logger logger = LoggerFactory.getLogger(Distinct.class);
	private int pagesize;
	private AlbumService albumService;
	private AudioService audioService;
	private MediaService mediaService;
	private RedisOperService rs;

	public Distinct() {
		Scheme scheme = (Scheme) SystemCache.getCache(CrawlerConstants.SCHEME).getContent();
		rs = new RedisOperService(scheme.getJedisConnectionFactory(), 1);
	}

	/**
	 * 删除本次抓取中间库里专辑和单体重复信息
	 * 
	 * @param crawlernum
	 * @return
	 */
	public void removeSameAlbumAndAudio(String crawlernum) {
		albumService = (AlbumService) SpringShell.getBean("albumService");
		audioService = (AudioService) SpringShell.getBean("audioService");
		List<AlbumPo> allist = albumService.getAlbumList(crawlernum);
		if (allist != null && allist.size() > 0) {
			String albumstr = "";
			for (AlbumPo al : allist) {
				if (albumstr.contains(al.getAlbumName() + al.getAlbumPublisher())) {
					List<AudioPo> aus = audioService.getAudioListByAlbumId(al.getAlbumId(), al.getAlbumPublisher(), al.getCrawlerNum());
					if (aus!=null && aus.size()>0) {
						//TODO 待处理
					}
					logger.info("查出抓取到相同专辑[{}]",
							al.getAlbumName() + "_" + al.getAlbumPublisher() + "_" + al.getAlbumId());
					logger.info("进行删除查询到相同专辑下级单体");
					audioService.removeSameAudio(al.getAlbumId(), al.getAlbumPublisher(), crawlernum);
					albumService.removeSameAlbum(al.getAlbumId(), al.getAlbumPublisher(), crawlernum);
				}
				albumstr += al.getAlbumName() + al.getAlbumPublisher();
			}
		}
		int num = audioService.getAudioNum(crawlernum);
		if (num / 1000 > 0) {
			String audiostr = "";
			for (int i = 0; i < num / 1000 + 1; i++) {
				List<AudioPo> aulist = audioService.getAudioList(i * pagesize, pagesize, crawlernum);
				for (AudioPo au : aulist) {
					if (audiostr.contains(au.getAudioName() + au.getAudioPublisher())) {
						logger.info("查出抓取到的相同声音[{}]", au.getAudioName() + "_" + au.getAudioPublisher());
						audioService.removeSameAudio(au.getId());
					}
					audiostr += au.getAudioName() + au.getAudioPublisher();
				}
			}
		}
	}

	/**
	 * 删除中间库无下级的专辑信息
	 * 
	 * @param crawlernum
	 */
	public void removeAlbumNoAudio(String crawlernum) {
		albumService = (AlbumService) SpringShell.getBean("albumService");
		audioService = (AudioService) SpringShell.getBean("audioService");
		List<AlbumPo> allist = albumService.getAlbumList(crawlernum);
		if (allist != null && allist.size() > 0) {
			for (AlbumPo al : allist) {
				if (audioService.countNumByAlbumId(al.getAlbumId(), al.getAlbumPublisher(), crawlernum) == 0) {
					logger.info("查询到无下级声音的专辑[{}]", al.getAlbumName() + "_" + al.getAlbumPublisher());
					albumService.removeSameAlbum(al.getAlbumId(), al.getAlbumPublisher(), crawlernum);
				}
			}
		}
	}

	/**
	 * 专辑与Redis快照对比
	 * 
	 * @param crawlernum
	 * @return
	 */
	public Map<String, Object> compareRedisByAlbum(String crawlernum) {
		List<AlbumPo> oldlist = new ArrayList<AlbumPo>();
		albumService = (AlbumService) SpringShell.getBean("albumService");
		audioService = (AudioService) SpringShell.getBean("audioService");
		List<AlbumPo> allist = albumService.getAlbumList(crawlernum);
		Iterator<AlbumPo> als = allist.iterator();
		while (als.hasNext()) {
			AlbumPo al = (AlbumPo) als.next();
			if (isOrNoCrawlerSrcRecord(rs, al)) {
				oldlist.add(al);
				als.remove();
			}
		}
		logger.info("Redis快照不存在专辑数目[{}]", allist.size());
		logger.info("Redis快照存在专辑数目[{}]", oldlist.size());

		Map<String, Object> m = new HashMap<String, Object>();
		m.put("oldlist", oldlist); // 本次次抓取与Redis快照对比已存在专辑列表
		m.put("newlist", allist); // 本次抓取新增专辑列表
		return m;
	}

	/**
	 * 声音跟中间库对比
	 * 
	 * @return
	 */
	public List<Album> compareRedisByAudio(List<AlbumPo> allist) {
		List<Album> alls = new ArrayList<>();
		audioService = (AudioService) SpringShell.getBean("audioService");
		Iterator<AlbumPo> als = allist.iterator();
		while (als.hasNext()) {
			AlbumPo al = (AlbumPo) als.next();
			List<AudioPo> aulist = audioService.getAudioListByAlbumId(al.getAlbumId(), al.getAlbumPublisher(), al.getCrawlerNum());
			if (aulist != null && aulist.size() > 0) {
				Iterator<AudioPo> aus = aulist.iterator();
				while (aus.hasNext()) {
					AudioPo au = (AudioPo) aus.next();
					if (isOrNoCrawlerSrcRecord(rs, au)) {
						audioService.removeSameAudio(au.getId());
						aus.remove();
					}
				}
				if (aulist.size() > 0) {
					Album album = new Album();
					album.setAlbumPo(al);
					album.setAudiolist(aulist);
					alls.add(album);
				}
			}
		}
		return alls;
	}

	/**
	 * 声音跟资源库对比
	 * 
	 * @param aulist
	 * @return
	 */
	public List<Album> compareCMByAudio(List<Album> allist) {
		mediaService = (MediaService) SpringShell.getBean("mediaService");
		if (allist != null && allist.size() > 0) {
			Iterator<Album> als = allist.iterator();
			while (als.hasNext()) {
				Album al = (Album) als.next();
				List<AudioPo> aulist = al.getAudiolist();
				if (aulist != null && aulist.size() > 0) {
					Iterator<AudioPo> aus = aulist.iterator();
					while (aus.hasNext()) {
						try {
							Thread.sleep(SpiritRandom.getRandom(new Random(), 10, 20));
						} catch (Exception e) {
						}
						AudioPo au = (AudioPo) aus.next();
						List<MediaAssetPo> mes = mediaService.getMaSameList(au.getAudioURL(), au.getAudioName(),
								au.getAudioPublisher());
						if (mes != null && mes.size() > 0)
							aus.remove();
					}
				}
				if (aulist == null || aulist.size() == 0)
					als.remove();
			}
		}
		return allist;
	}

	/**
	 * 专辑与资源库对比
	 * 
	 * @param allist
	 * @return
	 */
	public Map<String, Object> compareCMByAlbum(List<AlbumPo> allist) {
		List<Album> oldlist = new ArrayList<>();
		audioService = (AudioService) SpringShell.getBean("audioService");
		mediaService = (MediaService) SpringShell.getBean("mediaService");
		if (allist != null && allist.size() > 0) {
			Iterator<AlbumPo> als = allist.iterator();
			while (als.hasNext()) {
				AlbumPo al = (AlbumPo) als.next();
				List<SeqMediaAssetPo> smalist = mediaService.getSeqInfo(al.getAlbumName(), al.getAlbumPublisher());
				if (smalist != null && smalist.size() > 0) {
					logger.info("资源库存在专辑[{}]", al.getAlbumName() + "_" + al.getAlbumPublisher());
					Album album = new Album();
					List<AudioPo> aulist = audioService.getAudioListByAlbumId(al.getAlbumId(), al.getAlbumPublisher(),
							al.getCrawlerNum());
					album.setAlbumPo(al);
					album.setAudiolist(aulist);
					oldlist.add(album);
					als.remove();
				}
			}
		}
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("oldlist", oldlist); // 本次次抓取与资源库对比已存在专辑列表
		m.put("newlist", allist); // 本次抓取新增专辑列表
		return m;
	}

	public Map<String, Object> comparePublisherSrc(List<AlbumPo> allist, String crawlernum) {
		audioService = (AudioService) SpringShell.getBean("audioService");
		mediaService = (MediaService) SpringShell.getBean("mediaService");
		List<AlbumPo> newlist = new ArrayList<>();
		List<Map<String, Object>> samelist = new ArrayList<>();
		CompareAttribute compareAttribute = new CompareAttribute(crawlernum);
		if (allist != null && allist.size() > 0) {
			int processnum = 0;
			logger.info("相似专辑抓取进度[{}]%", 0);
			for (int i = 0; i < allist.size(); i++) {
				int procnum = i * 100 / allist.size();
				if (procnum != processnum) {
					logger.info("相似专辑抓取进度[{}]%", procnum);
					processnum = procnum;
				}
				AlbumPo al = allist.get(i);
				SeqMediaAssetPo sma = compareAttribute.getSameSma(al);
				if (sma != null) {
					logger.info("相似专辑抓取库_资源库[{}]", al.getAlbumName() + "_" + sma.getSmaTitle());
					Map<String, Object> m = new HashMap<>();
					m.put("album", al);
					m.put("Sma", sma);
					List<AudioPo> aulist = audioService.getAudioListByAlbumId(al.getAlbumId(), al.getAlbumPublisher(),
							al.getCrawlerNum());
					List<Map<String, Object>> lm = new ArrayList<>();
					List<AudioPo> la = new ArrayList<>();
					if (aulist != null && aulist.size() > 0) {
						for (int j = 0; j < aulist.size(); j++) {
							AudioPo au = aulist.get(j);
							MediaAssetPo ma = compareAttribute.getSameMa(au, sma);
							if (ma != null) {
								Map<String, Object> mm = new HashMap<>();
								mm.put("audio", au);
								mm.put("ma", ma);
								lm.add(mm);
							} else
								la.add(au);
						}
					}
					m.put("sameaudiolist", lm);
					m.put("newaudiolist", la);
					samelist.add(m);
				} else {
					logger.info("非相似专辑抓取库_资源库[{}]", al.getAlbumName() + "_" + al.getAlbumPublisher());
					newlist.add(al);
				}
			}
		}
		Map<String, Object> map = new HashMap<>();
		map.put("samelist", samelist);
		map.put("newlist", newlist);
		return map;
	}

	/**
	 * 判断抓取数据快照是否存在
	 * 
	 * @param o
	 * @return
	 */
	private boolean isOrNoCrawlerSrcRecord(RedisOperService rs, Object o) {
		if (o != null) {
			if (o instanceof AlbumPo) {
				AlbumPo al = (AlbumPo) o;
				return RedisUtils.isOrNoCrawlerSrcRecordExist(rs, al.getAlbumName() + al.getAlbumPublisher());
			}
			if (o instanceof AudioPo) {
				AudioPo au = (AudioPo) o;
				return RedisUtils.isOrNoCrawlerSrcRecordExist(rs, au.getAudioURL());
			}
		}
		return false;
	}
}
