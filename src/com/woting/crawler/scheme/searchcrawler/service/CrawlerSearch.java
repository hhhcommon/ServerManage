package com.woting.crawler.scheme.searchcrawler.service;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;

import com.spiritdata.framework.ext.spring.redis.RedisOperService;
import com.spiritdata.framework.util.JsonUtils;
import com.spiritdata.framework.util.StringUtils;
import com.woting.cm.core.searchword.persis.po.SearchWordPo;
import com.woting.cm.core.searchword.service.SearchWordService;
import com.woting.crawler.core.album.persis.po.AlbumPo;
import com.woting.crawler.core.audio.persis.po.AudioPo;
import com.woting.crawler.ext.SpringShell;
import com.woting.crawler.scheme.searchcrawler.utils.SearchUtils;

public class CrawlerSearch extends Thread {
	
	private void checkWord() {
		SearchWordService searchWordService = (SearchWordService) SpringShell.getBean("searchWordService");
		try {
			List<SearchWordPo> sws = searchWordService.getSearchWordList();
		    if (sws!=null && sws.size()>0) {
			    for (SearchWordPo searchWordPo : sws) {
				    new XiMaLaYaSearch(searchWordPo.getWord()).start();
			    	new QTSearch(searchWordPo.getWord()).start();
				    System.out.println(searchWordPo.getWord());
				    searchWordService.deleteSearchWord(searchWordPo.getId());
			    }
		    }
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		new Thread(new Runnable() {
			public void run() {
				JedisConnectionFactory conn = (JedisConnectionFactory) SpringShell.getBean("connectionFactorySearch");
				RedisOperService ros = new RedisOperService(conn,3);
				while (true) {
					try {
						Thread.sleep(20);
						Set<String> set = ros.keys("CONTENT_SEARCH*");
						if (set!=null && set.size()>0) {
							Iterator<String> it = set.iterator();
							while (it.hasNext()) {
								String str = it.next();
								String value = ros.get(str);
								if (!StringUtils.isNullOrEmptyOrSpace(value)) {
									if (str.contains("AUDIO")) {
										SearchUtils.addListInfo(JsonUtils.jsonToObj(value, AudioPo.class));
									} else if (str.contains("SEQU"))
										SearchUtils.addListInfo(JsonUtils.jsonToObj(value, AlbumPo.class));
								}
								ros.del(str);
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
						continue;
					}
				}
			}
		}).start();
		while (true) {
			try {
				Thread.sleep(20);
				checkWord();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
