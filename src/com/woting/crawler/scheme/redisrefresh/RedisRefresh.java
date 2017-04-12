package com.woting.crawler.scheme.redisrefresh;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;

import com.spiritdata.framework.ext.spring.redis.RedisOperService;
import com.woting.crawler.core.redis.AddContentRedisThread;
import com.woting.crawler.core.solr.persis.po.SolrInputPo;
import com.woting.crawler.core.solr.persis.po.SolrSearchResult;
import com.woting.crawler.core.solr.service.SolrJService;
import com.woting.crawler.ext.SpringShell;

public class RedisRefresh {
	Logger logger = LoggerFactory.getLogger(RedisRefresh.class);
	
	public void beginRediaRefresh() {
      JedisConnectionFactory jedisConnectionFactory = (JedisConnectionFactory) SpringShell.getBean("connectionFactoryContent");
      RedisOperService redis = new RedisOperService(jedisConnectionFactory, 11);
      SolrJService solrJService = (SolrJService) SpringShell.getBean("solrJService");
      try {
      	long all = 0;
			SolrSearchResult ssr = solrJService.solrSearch(null, null, null, 1, 1, "item_type:SEQU");
			if (ssr!=null) {
				all = ssr.getRecordCount();
				logger.info("查询到Solr里存在专辑数目为[{}]", all);
				if (all>0) {
					int page = (int) (all/100+1);
					for (int i = 1; i <= page; i++) {
						ssr = solrJService.solrSearch(null, null, null, i, 100, "item_type:SEQU");
						List<SolrInputPo> sPos = ssr.getSolrInputPos();
						if (sPos!=null && sPos.size()>0) {
					        for (SolrInputPo solrInputPo : sPos) {
					        	try {
									String contentid = solrInputPo.getItem_id();
							        String values = redis.get("Content::MediaType_CID::[SEQU_"+contentid+"]::INFO");
							        if (values==null) {
							        	logger.info("开始往redis添加专辑【 "+solrInputPo.getItem_title()+" 】信息");
								        new AddContentRedisThread(contentid).addRedia();
							        }
								} catch (Exception e) {
									continue;
								}
					        }
				        }
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
