package com.woting.crawler.core.scheme.model;

import java.sql.Timestamp;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import com.spiritdata.framework.ext.spring.redis.RedisOperService;
import com.woting.crawler.core.etl.model.Etl1Process;
import com.woting.crawler.core.etl.model.Etl2Process;
import com.woting.crawler.core.scheme.persis.po.SchemePo;
import com.woting.crawler.ext.SpringShell;

public class Scheme {

	private String schemenum;
	private String crawlerExtent;
	private int redisDB;
	private int RedisSnapShootDB;
	private Timestamp cTimestamp;
	private Etl1Process etl1Process;
	private Etl2Process etl2Process;
	private RedisOperService redisOperService;
	private JedisConnectionFactory jedisConnectionFactory;
	private int XMLYThread_Limit_Size;
	private int QTThread_Limit_Size;
	
	public Scheme() {
		SchemePo schemePo = (SchemePo) SpringShell.getBean("scheme");
		this.setRedisSnapShootDB(Integer.valueOf(schemePo.getRedisSnapShootDB()));
		this.setSchemenum(schemePo.getSchemenum());
		this.setCrawlerExtent(schemePo.getCrawlerExtent());
		this.setRedisDB(Integer.valueOf(schemePo.getRedisDB()));
		this.setcTimestamp(new Timestamp(System.currentTimeMillis()));
		this.setXMLYThread_Limit_Size(schemePo.getXMLYThread_Limit_Size());
		this.setQTThread_Limit_Size(schemePo.getQTThread_Limit_Size());
		this.etl1Process = new Etl1Process();
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
	public int getRedisDB() {
		return redisDB;
	}
	public void setRedisDB(int redisDB) {
		this.redisDB = redisDB;
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
	public int getRedisSnapShootDB() {
		return RedisSnapShootDB;
	}
	public void setRedisSnapShootDB(int redisSnapShootDB) {
		RedisSnapShootDB = redisSnapShootDB;
	}
	public int getXMLYThread_Limit_Size() {
		return XMLYThread_Limit_Size;
	}
	public void setXMLYThread_Limit_Size(int xMLYThread_Limit_Size) {
		XMLYThread_Limit_Size = xMLYThread_Limit_Size;
	}
	public int getQTThread_Limit_Size() {
		return QTThread_Limit_Size;
	}
	public void setQTThread_Limit_Size(int qTThread_Limit_Size) {
		QTThread_Limit_Size = qTThread_Limit_Size;
	}
}
