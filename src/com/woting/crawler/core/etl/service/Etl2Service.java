package com.woting.crawler.core.etl.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.spiritdata.framework.core.cache.SystemCache;
import com.spiritdata.framework.util.JsonUtils;
import com.woting.cm.core.channel.persis.po.ChannelAssetPo;
import com.woting.cm.core.channel.persis.po.ChannelPo;
import com.woting.cm.core.channel.service.ChannelService;
import com.woting.cm.core.dict.persis.po.DictRefResPo;
import com.woting.cm.core.dict.service.DictService;
import com.woting.cm.core.media.persis.po.MaSourcePo;
import com.woting.cm.core.media.persis.po.MediaAssetPo;
import com.woting.cm.core.media.persis.po.MediaPlayCountPo;
import com.woting.cm.core.media.persis.po.SeqMaRefPo;
import com.woting.cm.core.media.persis.po.SeqMediaAssetPo;
import com.woting.cm.core.media.service.MediaService;
import com.woting.crawler.CrawlerConstants;
import com.woting.crawler.core.album.persis.po.AlbumPo;
import com.woting.crawler.core.album.service.AlbumService;
import com.woting.crawler.core.audio.persis.po.AudioPo;
import com.woting.crawler.core.audio.service.AudioService;
import com.woting.crawler.core.etl.model.Etl2Process;
import com.woting.crawler.ext.SpringShell;
import com.woting.crawler.scheme.utils.ConvertUtils;
import com.woting.crawler.scheme.utils.FileUtils;

@Service
public class Etl2Service {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
//	private CrawlerDictService crawlerDictService;
	private AlbumService albumService;
	private AudioService audioService;
	private MediaService mediaService;
	private ChannelService channelService;
	private DictService dictService;
	private List<Map<String, Object>> cate2dictdlist = new ArrayList<Map<String, Object>>();

	public void getDictAndCrawlerDict(Etl2Process etl2Process) {
		cate2dictdlist = FileUtils.readFileByJson(SystemCache.getCache(CrawlerConstants.APP_PATH).getContent() + "craw.txt");
//		crawlerDictService = (CrawlerDictService) SpringShell.getBean("crawlerDictService");
		albumService = (AlbumService) SpringShell.getBean("albumService");
		audioService = (AudioService) SpringShell.getBean("audioService");
		mediaService = (MediaService) SpringShell.getBean("mediaService");
//		Map<String, Object> catem = etl2Process.getCategorys();
//		Map<String, Object> chm = etl2Process.getChannels();
//		DictMPo dm = crawlerDictService.getDictMList("3");
		int alnum = albumService.countNum(etl2Process.getEtlnum());
		logger.info("抓取专辑数目[{}]", alnum);
		alnum = alnum / 1000 + 1;
		long begintime = System.currentTimeMillis();
		List<String> alnames = new ArrayList<String>();
		List<AlbumPo> allist = new ArrayList<AlbumPo>();
		List<AlbumPo> existals = new ArrayList<AlbumPo>();
		for (int i = 0; i < alnum; i++) {
			allist = albumService.getAlbumList(i * 1000, 1000, etl2Process.getEtlnum());
			List<AudioPo> aulist = new ArrayList<AudioPo>();
			for (AlbumPo albumPo : allist) {
				alnames.add(albumPo.getAlbumName());
			}
			logger.info("开始相似专辑匹配");
			List<SeqMediaAssetPo> smalist = mediaService.getSeqSameList(alnames);
			if (smalist != null && smalist.size() > 0) {
				logger.info("与资源库对比相同专辑数量[{}]", smalist.size());
				Map<String, Object> m = new HashMap<String, Object>();
				for (SeqMediaAssetPo seq : smalist) {
					m.put(seq.getSmaTitle(), seq.getSmaPublisher());
				}
				Iterator<AlbumPo> als = allist.iterator();

				String albu = "";
				while (als.hasNext()) {
					AlbumPo al = (AlbumPo) als.next();
					if (albu.contains(al.getAlbumName() + al.getAlbumPublisher())) {
						logger.info("查出抓取到相同专辑[{}]",al.getAlbumName() + "_" + al.getAlbumPublisher() + "_" + al.getAlbumId());
						logger.info("进行删除查询到相同专辑下级单体");
						audioService.removeSameAudio(al.getAlbumId(), al.getAlbumPublisher(), etl2Process.getEtlnum());
						albumService.removeSameAlbum(al.getAlbumId(), al.getAlbumPublisher(), etl2Process.getEtlnum());
						als.remove();
						continue;
					}
					albu += al.getAlbumName() + al.getAlbumPublisher();
				}
				logger.info("非重复专辑数量[{}]", allist.size());
				logger.info("开始进行查询专辑与声音的绑定信息");

				als = allist.iterator();
				while (als.hasNext()) {
					AlbumPo al = (AlbumPo) als.next();
					if (m.containsKey(al.getAlbumName()) && m.get(al.getAlbumName()).equals(al.getAlbumPublisher())) {
						existals.add(al);
						als.remove();
					}
				} // 资源库已存在专辑列表existals,新添专辑列表 allist

				als = allist.iterator();
				while (als.hasNext()) {
					AlbumPo al = (AlbumPo) als.next();
					try {
						Thread.sleep(10);
					} catch (Exception e) {}
					List<AudioPo> aus = audioService.getAudioListByAlbumId(al.getAlbumId(), al.getAlbumPublisher(),
							etl2Process.getEtlnum());
					if (aus.size() == 0 || aus.isEmpty() || aus == null) {
						logger.info("删除无下级声音的专辑[{}]", al.getAlbumName() + "_" + al.getAlbumPublisher());
						albumService.removeSameAlbum(al.getAlbumId(), al.getAlbumPublisher(), etl2Process.getEtlnum());
						als.remove();
						continue;
					}
					aulist.addAll(aus);
				}
				logger.info("与专辑有绑定关系的声音数量为[{}]", aulist.size());
			}
		}
		System.out.println("时长=" + (System.currentTimeMillis() - begintime));
		logger.info("开始往资源库进行数据转换");
		makeExistAlbums(existals);
		makeNewAlbums(allist);
	}

	/**
	 * 资源库已存在专辑的整理
	 * 
	 * @param allist
	 */
	@SuppressWarnings("unchecked")
	private void makeExistAlbums(List<AlbumPo> allist) {
		channelService = (ChannelService) SpringShell.getBean("channelService");
		dictService = (DictService) SpringShell.getBean("dictService");
		List<MediaAssetPo> malist = new ArrayList<MediaAssetPo>();
		List<MaSourcePo> maslist = new ArrayList<MaSourcePo>();
		List<DictRefResPo> dictreflist = new ArrayList<DictRefResPo>();
		List<ChannelAssetPo> chalist = new ArrayList<ChannelAssetPo>();
		List<SeqMaRefPo> seqreflist = new ArrayList<SeqMaRefPo>();
		List<MediaPlayCountPo> mecounts = new ArrayList<MediaPlayCountPo>();
		List<ChannelPo> chlist = channelService.getChannelList();
		if (allist != null && allist.size() > 0) {
			for (AlbumPo al : allist) {
				List<AudioPo> aulist = audioService.getAudioListByAlbumId(al.getAlbumId(), al.getAlbumPublisher(),al.getCrawlerNum());
				if (aulist.size() > 0) {
					Iterator<AudioPo> aus = aulist.iterator();
					while (aus.hasNext()) {
						AudioPo au = (AudioPo) aus.next();
						int num = mediaService.getMaSame(au.getAudioURL());
						if (num > 0)
							aus.remove();
					}
					if (aulist.size() > 0) {
						List<SeqMediaAssetPo> seqlist = mediaService.getSeqInfo(al.getAlbumName(), al.getAlbumPublisher());
						if (seqlist != null && seqlist.size() > 0) {
							SeqMediaAssetPo seq = seqlist.get(0);
							Map<String, Object> mall = ConvertUtils.convert2MediaAsset(aulist, seq, cate2dictdlist, chlist);
							if (mall != null) {
								if (mall.containsKey("malist")) {
									malist.addAll((List<MediaAssetPo>) mall.get("malist"));
									maslist.addAll((List<MaSourcePo>) mall.get("maslist"));
									dictreflist.addAll((List<DictRefResPo>) mall.get("dictreflist"));
									chalist.addAll((List<ChannelAssetPo>) mall.get("chalist"));
									seqreflist.addAll((List<SeqMaRefPo>) mall.get("seqmareflist"));
									mecounts.addAll((List<MediaPlayCountPo>)mall.get("mediaplaycount"));
								}
							}
						}
					}
				}
			}
		}
		logger.info("转换声音的数据[{}],转换播放资源表的数据[{}],转换分类数据[{}],转换栏目发布表数据[{}],专辑声音关系数量[{}]", malist.size(),maslist.size(), dictreflist.size(), chalist.size(), seqreflist.size());
		if (malist.size()>0) {
			//往资源库插入声音数据
		    mediaService.insertMaList(malist);
	        //往资源库插入播放流数据
	        mediaService.insertMasList(maslist);
	        //往资源库插入专辑声音关系表数据
		    mediaService.insertSeqRefList(seqreflist);
		    //往资源库插入音频播放次数数据
		    mediaService.insertMediaPlayCountList(mecounts);
		    //往字典关系表里插入内容分类关系数据
		    dictService.insertDictRefList(dictreflist);
		    //往栏目发布表里插入发布信息
		    channelService.insertChannelAssetList(chalist);
		}else{
			logger.info("已存在的专辑无最新下级声音资源");
		}
		
	}
	
	@SuppressWarnings("unchecked")
	private void makeNewAlbums(List<AlbumPo> allist){
		channelService = (ChannelService) SpringShell.getBean("channelService");
		dictService = (DictService) SpringShell.getBean("dictService");
		List<MediaAssetPo> malist = new ArrayList<MediaAssetPo>();
		List<MaSourcePo> maslist = new ArrayList<MaSourcePo>();
		List<DictRefResPo> dictreflist = new ArrayList<DictRefResPo>();
		List<ChannelAssetPo> chalist = new ArrayList<ChannelAssetPo>();
		List<SeqMediaAssetPo> seqlist = new ArrayList<SeqMediaAssetPo>();
		List<SeqMaRefPo> seqreflist = new ArrayList<SeqMaRefPo>();
		List<MediaPlayCountPo> mecounts = new ArrayList<MediaPlayCountPo>();
		List<ChannelPo> chlist = channelService.getChannelList();
		if (allist != null && allist.size() > 0) {
			for (AlbumPo al : allist) { 
				Map<String, Object> map = ConvertUtils.convert2SeqMediaAsset(al,cate2dictdlist,chlist);
				if (map==null) {
					continue;
				}
				SeqMediaAssetPo se = (SeqMediaAssetPo) map.get("seq");
				seqlist.add(se);
				dictreflist.add((DictRefResPo)map.get("dictref"));
				chalist.add((ChannelAssetPo)map.get("cha"));
				mecounts.add((MediaPlayCountPo)map.get("playnum"));
				//获取抓取到的专辑下级节目信息
				List<AudioPo> aulist = audioService.getAudioListByAlbumId(al.getAlbumId(), al.getAlbumPublisher(),al.getCrawlerNum());
				if (aulist.size() > 0) {
					Map<String, Object> mall = ConvertUtils.convert2MediaAsset(aulist, se, cate2dictdlist, chlist);
					if (mall != null &&mall.containsKey("malist")) {
						malist.addAll((List<MediaAssetPo>) mall.get("malist"));
						maslist.addAll((List<MaSourcePo>) mall.get("maslist"));
						dictreflist.addAll((List<DictRefResPo>) mall.get("dictreflist"));
						chalist.addAll((List<ChannelAssetPo>) mall.get("chalist"));
						seqreflist.addAll((List<SeqMaRefPo>) mall.get("seqmareflist"));
						mecounts.addAll((List<MediaPlayCountPo>)mall.get("mediaplaycount"));
					}
				}
			}
		}
		logger.info("转换声音的数据[{}],转换播放资源表的数据[{}],转换分类数据[{}],转换栏目发布表数据[{}],专辑声音关系数量[{}]", malist.size(),maslist.size(), dictreflist.size(), chalist.size(), seqreflist.size());
		if (malist.size()>0) {
			mediaService.insertSeqList(seqlist);
			//往资源库插入声音数据
		    mediaService.insertMaList(malist);
	        //往资源库插入播放流数据
	        mediaService.insertMasList(maslist);
	        //往资源库插入专辑声音关系表数据
		    mediaService.insertSeqRefList(seqreflist);
		    //往资源库插入音频播放次数数据
		    mediaService.insertMediaPlayCountList(mecounts);
		    //往字典关系表里插入内容分类关系数据
		    dictService.insertDictRefList(dictreflist);
		    //往栏目发布表里插入发布信息
		    channelService.insertChannelAssetList(chalist);
		}else{
			logger.info("新专辑无下级声音资源");
		}
	}
}
