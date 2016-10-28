package com.woting.crawler.compare;

import java.util.List;

import com.spiritdata.framework.core.cache.SystemCache;
import com.spiritdata.framework.ext.spring.redis.RedisOperService;
import com.woting.cm.core.media.persis.po.MediaAssetPo;
import com.woting.cm.core.media.persis.po.SeqMediaAssetPo;
import com.woting.cm.core.media.service.MediaService;
import com.woting.crawler.CrawlerConstants;
import com.woting.crawler.core.album.persis.po.AlbumPo;
import com.woting.crawler.core.audio.persis.po.AudioPo;
import com.woting.crawler.core.scheme.model.Scheme;
import com.woting.crawler.ext.SpringShell;
import com.woting.crawler.scheme.utils.RedisUtils;

class CompareAttribute {
	private MediaService mediaService;
	private String crawlernum;
	private float sameproportion = 0.8f;
	private RedisOperService rs;

	public CompareAttribute(String crawlernum) {
		this.crawlernum = crawlernum;
		Scheme scheme = (Scheme) SystemCache.getCache(CrawlerConstants.SCHEME).getContent();
		rs = scheme.getRedisOperService();
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
}
