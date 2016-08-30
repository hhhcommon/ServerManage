package com.woting.crawler.scheme.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.spiritdata.framework.util.JsonUtils;
import com.woting.cm.core.media.persis.po.MediaAssetPo;
import com.woting.cm.core.media.persis.po.SeqMediaAssetPo;
import com.woting.crawler.compare.SolrParticiple;
import com.woting.crawler.core.album.persis.po.AlbumPo;
import com.woting.crawler.core.audio.persis.po.AudioPo;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisUtils {
	public static Logger logger = LoggerFactory.getLogger(RedisUtils.class);
	private static JedisPool jedisPool = getPool();
	private static int crawlInfo = 1;
	private static int crawlrecord = 2;

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

	private static Jedis getResource(int dbnum) {
		Jedis jedis = jedisPool.getResource();
		jedis.select(dbnum); // 存放redis第几个db库
		return jedis;
	}

	/**
	 * 释放jedis客户端
	 * 
	 * @param jedis
	 */
	@SuppressWarnings("deprecation")
	private static void release(Jedis jedis) {
		if (jedis != null)
			jedisPool.returnResource(jedis);
	}

	public static void addXMLYOriginalMa(String num, Object str) {
		Jedis jedis = getResource(crawlInfo);
		try {
			jedis.lpush("XMLY_Audio_" + num, JsonUtils.objToJson(str));
		} catch (Exception e) {
		} finally {
			release(jedis);
		}
	}

	public static void addXMLYOriginalSeq(String num, Object str) {
		Jedis jedis = getResource(crawlInfo);
		try {
			jedis.lpush("XMLY_Album_" + num, JsonUtils.objToJson(str));
		} catch (Exception e) {
		} finally {
			release(jedis);
		}
	}

	public static void addXMLYCategory(String num, Object str) {
		Jedis jedis = getResource(crawlInfo);
		try {
			jedis.set("XMLY_FastGetCategoryId_" + num, JsonUtils.objToJson(str));
		} catch (Exception e) {
		} finally {
			release(jedis);
		}
	}

	public static void addXMLYCategorys(String num, Object str) {
		Jedis jedis = getResource(crawlInfo);
		try {
			jedis.lpush("XMLY_Categorys_" + num, JsonUtils.objToJson(str));
		} catch (Exception e) {
			release(jedis);
		}
	}

	public static void addQTAudio(String num, Object str) {
		Jedis jedis = getResource(crawlInfo);
		try {
			// jedis.select(1); //选择redis db1 库，其余未标明的默认db0库
			jedis.lpush("QT_Audio_" + num, JsonUtils.objToJson(str));
		} catch (Exception e) {
		} finally {
			release(jedis);
		}
	}

	public static void addQTAlbum(String num, Object str) {
		Jedis jedis = getResource(crawlInfo);
		try {
			jedis.lpush("QT_Album_" + num, JsonUtils.objToJson(str));
		} catch (Exception e) {
		} finally {
			release(jedis);
		}
	}

	public static void addQTCategory(String num, Object cate) {
		Jedis jedis = getResource(crawlInfo);
		try {
			jedis.set("QT_ResourceIdAndCategoryId_" + num, JsonUtils.objToJson(cate));
		} catch (Exception e) {
		} finally {
			release(jedis);
		}
	}

	public static void addQTCategorys(String num, Object cate) {
		Jedis jedis = getResource(crawlInfo);
		try {
			jedis.lpush("QT_Categorys_" + num, JsonUtils.objToJson(cate));
		} catch (Exception e) {
		} finally {
			release(jedis);
		}
	}

	public static void addKLAlbum(String num, Object seq) {
		Jedis jedis = getResource(crawlInfo);
		try {
			jedis.lpush("KL_Album_" + num, JsonUtils.objToJson(seq));
		} catch (Exception e) {
		} finally {
			release(jedis);
		}
	}

	public static void addKLAudio(String num, Object malist) {
		Jedis jedis = getResource(crawlInfo);
		try {
			jedis.lpush("KL_Audio_" + num, JsonUtils.objToJson(malist));
		} catch (Exception e) {
		} finally {
			release(jedis);
		}
	}

	public static void addKLCategory(String num, Object catelist) {
		Jedis jedis = getResource(crawlInfo);
		try {
			jedis.set("KL_CategroyId_" + num, JsonUtils.objToJson(catelist));
		} catch (Exception e) {
		} finally {
			release(jedis);
		}
	}

	@SuppressWarnings("unchecked")
	public static Map<String, Object> getOrigData(String key) {
		Jedis jedis = getResource(crawlInfo);
		Map<String, Object> m = new HashMap<String, Object>();
		try {
			String str = jedis.get(key);
			m = (Map<String, Object>) JsonUtils.jsonToObj(str, Map.class);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			release(jedis);
		}
		return m;
	}

	@SuppressWarnings("unchecked")
	public static List<Map<String, Object>> getOrigDataList(String key) {
		Jedis jedis = getResource(crawlInfo);
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		try {
			List<String> l = jedis.lrange(key, 0, -1);
			for (String str : l) {
				list.add((Map<String, Object>) JsonUtils.jsonToObj(str, Map.class));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			release(jedis);
		}
		return list;
	}

	public static void waitCrawlerFinish(String crawlernum) {
		Jedis jedis = getResource(crawlInfo);
		try {
			logger.info("正在抓取喜马拉雅专辑[{}],单体[{}],正在抓取蜻蜓专辑[{}],单体[{}]", getOrigDataListSize("XMLY_Album_" + crawlernum),
					getOrigDataListSize("XMLY_Audio_" + crawlernum), getOrigDataListSize("QT_Album_" + crawlernum),
					getOrigDataListSize("QT_Audio_" + crawlernum));
		} finally {
			release(jedis);
		}
	}

	public static long getOrigDataListSize(String key) {
		Jedis jedis = getResource(crawlInfo);
		long num = 0;
		try {
			num = jedis.llen(key);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			release(jedis);
		}
		return num;
	}

	public static void writeCrawlerFinishInfo(String num) {
		Jedis jedis = getResource(crawlInfo);
		try {
			Map<String, Object> m = new HashMap<String, Object>();
			m.put("KLAudio.size", getOrigDataListSize("KL_Audio_" + num));
			m.put("KLAlbum.size", getOrigDataListSize("KL_Album_" + num));
			m.put("QTAudio.size", getOrigDataListSize("QT_Audio_" + num));
			m.put("QTAlbum.size", getOrigDataListSize("QT_Album_" + num));
			m.put("XMLYAudio.size", getOrigDataListSize("XMLY_Audio_" + num));
			m.put("XMLYAlbum.size", getOrigDataListSize("XMLY_Album_" + num));
			m.put("cTime", System.currentTimeMillis());
			jedis.set("Scheme_CrawlerInfo_" + num, JsonUtils.objToJson(m));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			release(jedis);
		}
	}

	public static boolean isOrNoCrawlerFinish(String num) {
		Jedis jedis = getResource(crawlInfo);
		boolean isok = false;
		try {
			isok = jedis.exists("Scheme_CrawlerInfo_" + num);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			release(jedis);
		}
		return isok;
	}

	public static void writeEtl1Finish(String num, String jsonstr) {
		Jedis jedis = getResource(crawlInfo);
		try {
			jedis.set("Scheme_Etl1Info_" + num, jsonstr);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			release(jedis);
		}
	}

	public static boolean isOrNoEtl1Finish(String num) {
		Jedis jedis = getResource(crawlInfo);
		boolean isok = false;
		try {
			isok = jedis.exists("Scheme_Etl1Info_" + num);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			release(jedis);
		}
		return isok;
	}

	public static void addUrl(String url) {
		Jedis jedis = getResource(crawlInfo);
		try {
			jedis.lpush("XMLYURL", url);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			release(jedis);
		}
	}

	public static void writeCrawlerSrcRecordFinish(String crawlernum) {
		Jedis jedis = getResource(crawlrecord);
		try {
			jedis.set("CrawlerSrcRecord_Finish_" + crawlernum, System.currentTimeMillis() + "");
		} finally {
			release(jedis);
		}
	}

	public static boolean isOrNoCrawlerSrcRecord(String crawlernum) {
		Jedis jedis = getResource(crawlrecord);
		boolean isok = false;
		try {
			isok = jedis.exists("CrawlerSrcRecord_Finish_" + crawlernum);
		} finally {
			release(jedis);
		}
		return isok;
	}

	public static void addCrawlerSrcRecord(String src, String srcinfo) {
		Jedis jedis = getResource(crawlrecord);
		try {
			if (!jedis.exists(src)) {
				jedis.set(src, srcinfo);
			}
		} finally {
			release(jedis);
		}
	}

	public static boolean isOrNoCrawlerSrcRecordExist(String src) {
		Jedis jedis = getResource(crawlrecord);
		boolean isok = false;
		try {
			isok = jedis.exists(src);
		} finally {
			release(jedis);
		}
		return isok;
	}

	public static void writeCompareInfo(Object o, float f, String srcid, String crawlernum) {
		Jedis jedis = getResource(crawlInfo);
		try {
			if (o instanceof AlbumPo) {
				AlbumPo al = (AlbumPo) o;
				if(f!=0&&srcid!=null&&!srcid.equals("null")) {
					jedis.set(al.getAlbumPublisher()+"_Album_"+al.getId()+"_SameName_MaxProportion_"+crawlernum, f+"");
					jedis.set(al.getAlbumPublisher()+"_Album_"+al.getId()+"_SameName_SmaId_"+crawlernum, srcid);
				}
			} else {
				if(o instanceof AudioPo) {
					AudioPo au = (AudioPo) o;
					if(f!=0&&srcid!=null&&!srcid.equals("null")) {
						jedis.set(au.getAudioPublisher()+"_Audio_"+au.getId()+"_SameName_MaxProportion_"+crawlernum, f+"");
						jedis.set(au.getAudioPublisher()+"_Audio_"+au.getId()+"_SameName_MaId_"+crawlernum, srcid);
					}
				}
			}
		} finally {
			release(jedis);
		}
	}
	
	public static String getCompareSameSrcId(Object o, String crawlernum) {
		Jedis jedis = getResource(crawlInfo);
		try {
			if(o instanceof AlbumPo) {
				AlbumPo al = (AlbumPo) o;
				return jedis.get(al.getAlbumPublisher()+"_Album_"+al.getId()+"_SameName_SmaId_"+crawlernum);
			}
			if(o instanceof AudioPo) {
				AudioPo au = (AudioPo) o;
				return jedis.get(au.getAudioPublisher()+"_Audio_"+au.getId()+"_SameName_MaId_"+crawlernum);
			}
		} finally {
			release(jedis);
		}
		return null;
	}
	
	public static String getCompareMaxProportion(Object o, String crawlernum){
		Jedis jedis = getResource(crawlInfo);
		try {
			if(o instanceof AlbumPo) {
				AlbumPo al = (AlbumPo) o;
				return jedis.get(al.getAlbumPublisher()+"_Album_"+al.getId()+"_SameName_MaxProportion_"+crawlernum);
			}
			if(o instanceof AudioPo) {
				AudioPo au = (AudioPo) o;
				return jedis.get(au.getAudioPublisher()+"_Audio_"+au.getId()+"_SameName_MaxProportion_"+crawlernum);
			}
		} finally {
			release(jedis);
		}
		return null;
	}
	
	public static void writeSrcParticiple(Object o, String str) {
		Jedis jedis = getResource(crawlInfo);
		try {
			if(o instanceof SeqMediaAssetPo) {
				SeqMediaAssetPo sma = (SeqMediaAssetPo) o;
				if (!jedis.exists(sma.getSmaPublisher()+"_Sma_"+sma.getId()+"_Participle")) 
					jedis.set(sma.getSmaPublisher()+"_Sma_"+sma.getId()+"_Participle", str);
			}
			if(o instanceof AudioPo) {
				MediaAssetPo ma = new MediaAssetPo();
				if(!jedis.exists(ma.getMaPublisher()+"_Ma_"+ma.getId()+"_Participle"))
				jedis.set(ma.getMaPublisher()+"_Ma_"+ma.getId()+"_Participle", str);
			}
		} finally {
			release(jedis);
		}
	}
	
	@SuppressWarnings("unchecked")
	public static List<String> getSrcParticiple(Object o) {
		Jedis jedis = getResource(crawlInfo);
		try {
			if(o instanceof SeqMediaAssetPo) {
				SeqMediaAssetPo sma = (SeqMediaAssetPo) o;
				if (jedis.exists(sma.getSmaPublisher()+"_Sma_"+sma.getId()+"_Participle")) {
					String str = jedis.get(sma.getSmaPublisher()+"_Sma_"+sma.getId()+"_Participle");
					return (List<String>) JsonUtils.jsonToObj(str, List.class);
				} else {
					List<String> list = SolrParticiple.getAnalysis(sma.getSmaTitle());
					writeSrcParticiple(sma, JsonUtils.objToJson(list));
					return list;
				}
			}
			if (o instanceof MediaAssetPo) {
				MediaAssetPo ma = (MediaAssetPo) o;
				if(jedis.exists(ma.getMaPublisher()+"_Ma_"+ma.getId()+"_Participle")) {
					String str = jedis.get(ma.getMaPublisher()+"_Ma_"+ma.getId()+"_Participle");
					return (List<String>) JsonUtils.jsonToObj(str, List.class);
				} else {
					List<String> list = SolrParticiple.getAnalysis(ma.getMaTitle());
					writeSrcParticiple(ma, JsonUtils.objToJson(list));
					return list;
				}
			}
		} catch (Exception e){
			return null;
		} finally {
			release(jedis);
		}
		return null;
	}
}
