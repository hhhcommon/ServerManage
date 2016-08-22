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
import com.woting.cm.core.media.persis.po.SeqMediaAssetPo;
import com.woting.cm.core.media.service.MediaService;
import com.woting.crawler.CrawlerConstants;
import com.woting.crawler.core.album.persis.po.AlbumPo;
import com.woting.crawler.core.album.service.AlbumService;
import com.woting.crawler.core.audio.persis.po.AudioPo;
import com.woting.crawler.core.audio.service.AudioService;
import com.woting.crawler.core.dict.persis.po.DictDPo;
import com.woting.crawler.core.dict.persis.po.DictMPo;
import com.woting.crawler.core.dict.service.CrawlerDictService;
import com.woting.crawler.core.etl.model.Etl2Process;
import com.woting.crawler.ext.SpringShell;
import com.woting.crawler.scheme.utils.FileUtils;

@Service
public class Etl2Service {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private CrawlerDictService crawlerDictService;
	private AlbumService albumService;
	private AudioService audioService;
	private MediaService mediaService;
	private List<Map<String, Object>> cate2dictdlist = new ArrayList<Map<String,Object>>();
	
	public void getDictAndCrawlerDict(Etl2Process etl2Process){
//		cate2dictdlist = FileUtils.readFileByJson(SystemCache.getCache(CrawlerConstants.APP_PATH).getContent()+"craw.txt");
		crawlerDictService = (CrawlerDictService) SpringShell.getBean("crawlerDictService");
		albumService = (AlbumService) SpringShell.getBean("albumService");
		audioService = (AudioService) SpringShell.getBean("audioService");
		mediaService = (MediaService) SpringShell.getBean("mediaService");
		Map<String, Object> catem = etl2Process.getCategorys();
		Map<String, Object> chm = etl2Process.getChannels();
		DictMPo dm = crawlerDictService.getDictMList("3");
		int alnum = albumService.countNum(etl2Process.getEtlnum());
		logger.info("抓取专辑数目[{}]",alnum);
		alnum = alnum/1000+1;
		long begintime = System.currentTimeMillis();
		List<String> alnames = new ArrayList<String>();
		for (int i = 0; i < alnum; i++) {
			List<AlbumPo> allist = albumService.getAlbumList(i, 1000, etl2Process.getEtlnum());
			List<AudioPo> aulist = new ArrayList<AudioPo>();
			for (AlbumPo albumPo : allist) {
				alnames.add(albumPo.getAlbumName());
			}
			logger.info("开始相似专辑匹配");
			List<SeqMediaAssetPo> smalist = mediaService.getSeqSameList(alnames);
			if(smalist!=null&&smalist.size()>0){
				logger.info("与资源库对比相同专辑数量[{}]",smalist.size());
				Map<String, Object> m = new HashMap<String,Object>();
				for (SeqMediaAssetPo seq : smalist) {
					m.put(seq.getSmaTitle(), seq.getSmaPublisher());
				}
				Iterator<AlbumPo> als = allist.iterator();
				while (als.hasNext()) {
					AlbumPo al = (AlbumPo) als.next();
					if(m.containsKey(al.getAlbumName()) && m.get(al.getAlbumName()).equals(al.getAlbumPublisher())){
						als.remove();
					}
				}
				logger.info("非重复专辑数量[{}]", allist.size());
				logger.info("开始进行查询专辑与声音的绑定信息");
				als = allist.iterator();
				String albu = "";
				while (als.hasNext()) {
					AlbumPo al = (AlbumPo) als.next();
					if(albu.contains(al.getAlbumName()+al.getAlbumPublisher())){
						logger.info("查出抓取到相同专辑[{}]", al.getAlbumName()+"_"+al.getAlbumPublisher()+"_"+al.getAlbumId());
						logger.info("进行删除查询到相同专辑下级单体");
						audioService.removeSameAudio(al.getAlbumId(), al.getAlbumPublisher(), etl2Process.getEtlnum());
						albumService.removeSameAlbum(al.getAlbumId(), al.getAlbumPublisher(), etl2Process.getEtlnum());
						continue;
					}
					albu+=al.getAlbumName()+al.getAlbumPublisher();
				}
				als = allist.iterator();
				while (als.hasNext()) {
					AlbumPo al = (AlbumPo) als.next();
					try {
						Thread.sleep(10);
					} catch (Exception e) {}
					List<AudioPo> aus = audioService.getAudioListByAlbumId(al.getAlbumId(), al.getAlbumPublisher(), etl2Process.getEtlnum());
					if(al.getAlbumId().equals("208933"))
						System.out.println(aus.size()+"_"+aus.isEmpty()+"_"+aus==null);
					if(aus.size()==0 || aus.isEmpty() || aus==null){
						logger.info("删除无下级声音的专辑[{}]", al.getAlbumName()+"_"+al.getAlbumPublisher());
						albumService.removeSameAlbum(al.getAlbumId(), al.getAlbumPublisher(), etl2Process.getEtlnum());
						als.remove();
						continue;
					}
					aulist.addAll(aus);
				}
				logger.info("与专辑有绑定关系的声音数量为[{}]", aulist.size());
			}
		}
		System.out.println("时长="+(System.currentTimeMillis()-begintime));
	}
}
