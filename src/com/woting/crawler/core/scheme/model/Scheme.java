package com.woting.crawler.core.scheme.model;

import java.sql.Timestamp;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import com.spiritdata.framework.ext.spring.redis.RedisOperService;
import com.spiritdata.framework.util.StringUtils;
import com.woting.crawler.core.etl.model.Etl1Process;
import com.woting.crawler.core.etl.model.Etl2Process;
import com.woting.crawler.core.scheme.persis.po.SchemePo;
import com.woting.crawler.ext.SpringShell;

public class Scheme {

	private String schemenum;
	private String crawlerExtent;
	private int numberOfCrawlers;
	private Timestamp cTimestamp;
	private Etl1Process etl1Process;
	private Etl2Process etl2Process;
	private RedisOperService redisOperService;
	private JedisConnectionFactory jedisConnectionFactory;
	
	public Scheme() {
		SchemePo schemePo = (SchemePo) SpringShell.getBean("scheme");
		this.setSchemenum(schemePo.getSchemenum());
		this.setCrawlerExtent(schemePo.getCrawlerExtent());
		this.setNumberOfCrawlers(StringUtils.isNullOrEmptyOrSpace(schemePo.getNumberOfCrawlers())?10:Integer.valueOf(schemePo.getNumberOfCrawlers()));
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
	public int getNumberOfCrawlers() {
		return numberOfCrawlers;
	}

	public void setNumberOfCrawlers(int numberOfCrawlers) {
		this.numberOfCrawlers = numberOfCrawlers;
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
