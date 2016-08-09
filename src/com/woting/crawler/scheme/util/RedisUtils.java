package com.woting.crawler.scheme.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.spiritdata.framework.util.JsonUtils;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisUtils {
	private static JedisPool jedisPool = getPool();

	public static JedisPool getPool() {
		JedisPool pool = null;
		if (pool == null) {
			JedisPoolConfig config = new JedisPoolConfig();
			// 控制一个pool可分配多少个jedis实例，通过pool.getResource()来获取；
			// 如果赋值为-1，则表示不限制；如果pool已经分配了maxActive个jedis实例，则此时pool的状态为exhausted(耗尽)。
			config.setMaxTotal(1000);
			// 控制一个pool最多有多少个状态为idle(空闲的)的jedis实例。
			config.setMaxIdle(5);
			// 表示当borrow(引入)一个jedis实例时，最大的等待时间，如果超过等待时间，则直接抛出JedisConnectionException；
			config.setMaxWaitMillis(1000 * 100);
			// 在borrow一个jedis实例时，是否提前进行validate操作；如果为true，则得到的jedis实例均是可用的；
			config.setTestOnBorrow(true);
			config.setTestOnReturn(true);
			pool = new JedisPool(config, "localhost", 6379);
		}
		return pool;
	}

	/**
	 * 释放jedis客户端
	 * 
	 * @param jedis
	 */
	@SuppressWarnings("deprecation")
	private static void release(Jedis jedis) {
		if(jedis!=null)
			jedisPool.returnResource(jedis);
	}
	
	public static void addXMLYOriginalMa(String num,Object str){
		Jedis jedis = jedisPool.getResource();
		try {
			jedis.lpush("XMLYOriginalMaList_"+num, JsonUtils.objToJson(str));
		} catch (Exception e) {} finally {
			release(jedis);
		}
	}
	
	public static void addXMLYOriginalSeq(String num, String seqnum,Object str){
		Jedis jedis = jedisPool.getResource();
		try {
			jedis.set("XMLY_Seq_"+seqnum+"_"+num, JsonUtils.objToJson(str));
		} catch (Exception e) {} finally {
			release(jedis);
		}
	}
	
	public static List<Map<String, Object>> getMaList(String listkey){
		Jedis jedis = jedisPool.getResource();
		List<Map<String, Object>> malist = new ArrayList<Map<String,Object>>();
		try {
			List<String> l = jedis.lrange(listkey, 0, -1);
			for (String str : l) {
				malist.add((Map<String, Object>) JsonUtils.jsonToObj(str, Map.class));
			}
		} catch (Exception e) {e.printStackTrace();}finally {release(jedis);}
		return malist;
	}
	
	public static void multiPhoneCheckInfo(String phonenum) {
		Jedis jedis = jedisPool.getResource();
		jedis.del("phoneCheckInfo_"+phonenum);
		release(jedis);
	}
}
