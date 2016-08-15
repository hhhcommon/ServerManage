package com.woting.crawler.scheme.util;

import java.util.ArrayList;
import java.util.FormatFlagsConversionMismatchException;
import java.util.HashMap;
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
			jedis.lpush("XMLY_Audio_"+num, JsonUtils.objToJson(str));
		} catch (Exception e) {} finally {
			release(jedis);
		}
	}
	
	public static void addXMLYOriginalSeq(String num, Object str){
		Jedis jedis = jedisPool.getResource();
		try {
			jedis.lpush("XMLY_Album_"+num, JsonUtils.objToJson(str));
		} catch (Exception e) {} finally {
			release(jedis);
		}
	}
	
	public static void addXMLYCategory(String num, Object str){
		Jedis jedis = jedisPool.getResource();
		try {
			jedis.set("XMLY_Category_"+num, JsonUtils.objToJson(str));
		} catch (Exception e) {} finally {
			release(jedis);
		}
	}
	
	public static void addQTAudio(String num,Object str){
		Jedis jedis = jedisPool.getResource();
		try {
//			jedis.select(1); //选择redis db1 库，其余未标明的默认db0库
			jedis.lpush("QT_Audio_"+num, JsonUtils.objToJson(str));
		} catch (Exception e) {} finally {
			release(jedis);
		}
	}
	
	public static void addQTAlbum(String num,Object str){
		Jedis jedis = jedisPool.getResource();
		try {
			jedis.lpush("QT_Album_"+num, JsonUtils.objToJson(str));
		} catch (Exception e) {} finally {
			release(jedis);
		}
	}
	
	public static void addQTCategory(String num,Object cate){
		Jedis jedis = jedisPool.getResource();
		try {
			jedis.set("QT_Category_"+num, JsonUtils.objToJson(cate));
		} catch (Exception e) {} finally {
			release(jedis);
		}
	}
	
	public static void addKLAlbum(String num, Object seq){
		Jedis jedis = jedisPool.getResource();
		try {
			jedis.lpush("KL_Album_"+num, JsonUtils.objToJson(seq));
		} catch (Exception e) {} finally {
			release(jedis);
		}
	}
	
	public static void addKLAudio(String num,Object malist){
		Jedis jedis = jedisPool.getResource();
		try {
			jedis.lpush("KL_Audio_"+num, JsonUtils.objToJson(malist));
		} catch (Exception e) {} finally {
			release(jedis);
		}
	}
	
	public static void addKLCategory(String num,Object catelist){
		Jedis jedis = jedisPool.getResource();
		try {
			jedis.set("KL_Category_"+num, JsonUtils.objToJson(catelist));
		} catch (Exception e) {} finally {
			release(jedis);
		}
	}
	
	@SuppressWarnings("unchecked")
	public static Map<String, Object> getOrigData(String key){
		Jedis jedis = jedisPool.getResource();
		Map<String, Object> m = new HashMap<String,Object>();
		try {
			String str = jedis.get(key);
			m = (Map<String, Object>) JsonUtils.jsonToObj(str, Map.class);
		} catch (Exception e) {e.printStackTrace();} finally {
			release(jedis);
		}
		return m;
	}
	
	@SuppressWarnings("unchecked")
	public static List<Map<String, Object>> getOrigDataList(String key){
		Jedis jedis = jedisPool.getResource();
		List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
		try {
			List<String> l = jedis.lrange(key, 0, -1);
			for (String str : l) {
				list.add((Map<String, Object>) JsonUtils.jsonToObj(str, Map.class));
			}
		} catch (Exception e) {e.printStackTrace();} finally {
			release(jedis);
		}
		return list;
	}
	
	public static long getOrigDataListSize(String key){
		Jedis jedis = jedisPool.getResource();
		long num = 0;
		try {
			num = jedis.llen(key);
		} catch (Exception e) {e.printStackTrace();} finally {
			release(jedis);
		}
		return num;
	}
	
	public static void writeCrawlerFinishInfo(String num){
		Jedis jedis = jedisPool.getResource();
		try {
			Map<String, Object> m = new HashMap<String,Object>();
			m.put("KLAudio.size", getOrigDataListSize("KL_Audio_"+num));
			m.put("KLAlbum.size", getOrigDataListSize("KL_Album_"+num));
			m.put("QTAudio.size", getOrigDataListSize("QT_Audio_"+num));
			m.put("QTAlbum.size", getOrigDataListSize("QT_Album_"+num));
			m.put("XMLYAudio.size", getOrigDataListSize("XMLY_Audio_"+num));
			m.put("XMLYAlbum.size", getOrigDataListSize("XMLY_Album_"+num));
			m.put("cTime", System.currentTimeMillis());
			jedis.set("Scheme_CrawlerInfo_"+num, JsonUtils.objToJson(m));
		} catch (Exception e) {e.printStackTrace();} finally {
			release(jedis);
		}
	}
	
	public static boolean isOrNoCrawlerFinish(String num){
		Jedis jedis = jedisPool.getResource();
		boolean isok = false;
		try {
			isok = jedis.exists("Scheme_CrawlerInfo_"+num);
		} catch (Exception e) {e.printStackTrace();} finally {
			release(jedis);
		}
		return isok;
	}
}
