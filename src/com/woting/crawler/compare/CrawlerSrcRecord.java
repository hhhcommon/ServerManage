package com.woting.crawler.compare;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.woting.crawler.core.album.persis.po.AlbumPo;
import com.woting.crawler.core.album.service.AlbumService;
import com.woting.crawler.core.audio.persis.po.AudioPo;
import com.woting.crawler.core.audio.service.AudioService;
import com.woting.crawler.ext.SpringShell;
import com.woting.crawler.scheme.utils.RedisUtils;

public class CrawlerSrcRecord {
	Logger logger = LoggerFactory.getLogger(CrawlerSrcRecord.class);
	private AlbumService albumService;
	private AudioService audioService;
	private String crawlernum = "";
	private int pagesize = 1000;
	
	public CrawlerSrcRecord(String crawlwenum) {
		this.crawlernum = (Integer.valueOf(crawlwenum)-1)+"";
	}
	
	public void reloadCrawlerInfo(){
		albumService = (AlbumService) SpringShell.getBean("albumService");
		audioService = (AudioService) SpringShell.getBean("audioService");
		int num = Integer.valueOf(crawlernum);
		while (true) {
			for (int i = 1; i <= num; i++) {
				if (RedisUtils.isOrNoCrawlerFinish(i+"")) {
					logger.info("开始第[{}]次抓取Redis快照加载", i);
					List<AlbumPo> als = albumService.getAlbumList(i+"");
					if(als!=null&&als.size()>0) {
						logger.info("第[{}]次加载专辑[{}]", i, als.size());
						for (AlbumPo al : als) { //快照格式 albumName+albumPublisher : albumId
							RedisUtils.addCrawlerSrcRecord(al.getAlbumName()+al.getAlbumPublisher(), al.getId());
						}
					}
					int n = audioService.getAudioNum(crawlernum);
					logger.info("第[{}]次加载声音[{}]", i, n);
					if(n>0) {
						for (int j = 0; j <= n/1000; j++) {
							long begtime = System.currentTimeMillis();
							List<AudioPo> aus = audioService.getAudioList(j*pagesize, pagesize, crawlernum);
							if(aus!=null&&aus.size()>0) {
								for (AudioPo au : aus) { //快照格式 audioURL : audioId
									RedisUtils.addCrawlerSrcRecord(au.getAudioURL(), au.getId());
								}
							}
							logger.info("声音第[{}]次加载完成,耗时[{}]", j+1, System.currentTimeMillis()-begtime);
						}
					}
				}
			}
			break;
		}
	}
}
