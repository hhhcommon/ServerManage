package com.woting.crawler.core.scheme.model;

import java.sql.Timestamp;
import java.util.Map;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import com.spiritdata.framework.ext.spring.redis.RedisOperService;
import com.spiritdata.framework.util.JsonUtils;
import com.woting.crawler.core.etl.model.Etl1Process;
import com.woting.crawler.core.etl.model.Etl2Process;
import com.woting.crawler.ext.SpringShell;
import com.woting.crawler.scheme.utils.FileUtils;

public class Scheme {

	private String schemenum;
	private String crawlerExtent;
	private Timestamp cTimestamp;
	private Etl1Process etl1Process;
	private Etl2Process etl2Process;
	private RedisOperService redisOperService;
	private JedisConnectionFactory jedisConnectionFactory;
	
	@SuppressWarnings("unchecked")
	public Scheme(String jsonpath) {
		jsonpath = FileUtils.readFile(jsonpath);
		Map<String, Object> m = (Map<String, Object>) JsonUtils.jsonToObj(jsonpath, Map.class);
		this.setSchemenum(m.get("Schemenum")+"");
		this.setCrawlerExtent(m.get("CrawlerExtent")+"");
		System.out.println(m.get("Schemenum")+""+m.get("CrawlerExtent"));
		this.setcTimestamp(new Timestamp(System.currentTimeMillis()));
		this.etl1Process = new Etl1Process();
		this.jedisConnectionFactory = (JedisConnectionFactory) SpringShell.getBean("connectionFactory");
		this.redisOperService = new RedisOperService(jedisConnectionFactory, 1);
	}
	
	public String getCrawlerExtent() {
		return crawlerExtent;
	}
	public void setCrawlerExtent(String crawlerExtent) {
		this.crawlerExtent = crawlerExtent;
	}
	public String getSchemenum() {
		return schemenum;
	}
	public void setSchemenum(String schemenum) {
		this.schemenum = schemenum;
	}
	public JedisConnectionFactory getJedisConnectionFactory() {
		return jedisConnectionFactory;
	}
	public Timestamp getcTimestamp() {
		return cTimestamp;
	}
	public void setcTimestamp(Timestamp cTimestamp) {
		this.cTimestamp = cTimestamp;
	}
	public RedisOperService getRedisOperService() {
		return redisOperService;
	}
	public void setRedisOperService(RedisOperService redisOperService) {
		this.redisOperService = redisOperService;
	}
	public Etl1Process getEtl1Process() {
		etl1Process.setEtlnum(schemenum);
		return etl1Process;
	}
	public void setEtl1Process(Etl1Process etl1Process) {
		this.etl1Process = etl1Process;
	}
	public Etl2Process getEtl2Process() {
		return etl2Process;
	}
	public void setEtl2Process(Etl2Process etl2Process) {
		this.etl2Process = etl2Process;
	}
}
