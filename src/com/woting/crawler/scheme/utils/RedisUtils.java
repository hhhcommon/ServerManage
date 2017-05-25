package com.woting.crawler.scheme.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;

import com.spiritdata.framework.ext.spring.redis.RedisOperService;
import com.spiritdata.framework.util.JsonUtils;
import com.woting.cm.core.media.persis.po.MediaAssetPo;
import com.woting.cm.core.media.persis.po.SeqMediaAssetPo;
import com.woting.crawler.core.album.persis.po.AlbumPo;
import com.woting.crawler.core.audio.persis.po.AudioPo;
import com.woting.crawler.ext.SpringShell;

public class RedisUtils {
	
	public static Logger logger = LoggerFactory.getLogger(RedisUtils.class);

	public static void addSnapShootInfo(RedisOperService rs, String key,String value) {
		try {
			rs.set(key, value);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static String getSnapShootInfo(RedisOperService rs, String key) {
		try {
			return rs.get(key);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static boolean exeitsSnapShootInfo(RedisOperService rs, String key) {
		try {
			return rs.exist(key);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public static void removeSnapShoots(RedisOperService rs, String keys) {
		try {
			Set<String> strs =  rs.keys(keys);
			if (strs!=null && strs.size()>0) {
				for (String str : strs) {
				    rs.del(str);
			    }
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void removeOldCrawler(RedisOperService rs, String num) {
		try {
			rs.del("XMLY_Audio_"+num,"XMLY_Album_"+num,"QT_Audio_"+num,"QT_Album_"+num
					,"KL_Audio_"+num,"KL_Album_"+num);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void addXMLYOriginalMa(RedisOperService rs, String num, Object str) {
		try {
			rs.rPush("XMLY_Audio_" + num, JsonUtils.objToJson(str));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void addXMLYOriginalSeq(RedisOperService rs, String num, Object str) {
		try {
			rs.rPush("XMLY_Album_" + num, JsonUtils.objToJson(str));
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}

	public static void addXMLYCategory(RedisOperService rs, String num, Object str) {
		try {
			rs.set("XMLY_FastGetCategoryId_" + num, JsonUtils.objToJson(str));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void addXMLYCategorys(RedisOperService rs, String num, Object str) {
		try {
			rs.rPush("XMLY_Categorys_" + num, JsonUtils.objToJson(str));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void addQTAudio(RedisOperService rs, String num, Object str) {
		try {
			rs.rPush("QT_Audio_" + num, JsonUtils.objToJson(str));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void addQTAlbum(RedisOperService rs, String num, Object str) {
		try {
			rs.rPush("QT_Album_" + num, JsonUtils.objToJson(str));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void addQTCategory(RedisOperService rs, String num, Object cate) {
		try {
			rs.set("QT_ResourceIdAndCategoryId_" + num, JsonUtils.objToJson(cate));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void addKLAlbum(RedisOperService rs, String num, Object seq) {
		try {
			rs.rPush("KL_Album_" + num, JsonUtils.objToJson(seq));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void addKLAudio(RedisOperService rs, String num, Object malist) {
		try {
			rs.rPush("KL_Audio_" + num, JsonUtils.objToJson(malist));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	public static Map<String, Object> getOrigData(RedisOperService rs, String key) {
		Map<String, Object> m = new HashMap<String, Object>();
		try {
			String str = rs.get(key);
			m = (Map<String, Object>) JsonUtils.jsonToObj(str, Map.class);
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return m;
	}

	@SuppressWarnings("unchecked")
	public static List<Map<String, Object>> getOrigDataList(RedisOperService rs, String key, long begin, long end) {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		try {
			List<String> l = rs.lRange(key, begin, end);
			for (String str : l) {
				list.add((Map<String, Object>) JsonUtils.jsonToObj(str, Map.class));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	public static void waitCrawlerFinish(RedisOperService rs, String crawlernum) {
		try {
			long xmalbum = getOrigDataListSize(rs, "XMLY_Album_" + crawlernum);
			long xmaudio = getOrigDataListSize(rs, "XMLY_Audio_" + crawlernum);
			long qtalbum = getOrigDataListSize(rs, "QT_Album_" + crawlernum);
			long qtaudio = getOrigDataListSize(rs, "QT_Audio_" + crawlernum);
			logger.info("正在抓取喜马拉雅专辑[{}],单体[{}],正在抓取蜻蜓专辑[{}],单体[{}]", xmalbum, xmaudio, qtalbum, qtaudio);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static long getOrigDataListSize(RedisOperService rs, String key) {
		long num = -1;
		try {
			num = rs.lLen(key);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return num;
	}

	public static void writeCrawlerFinishInfo(RedisOperService rs, String num) {
		try {
			Map<String, Object> m = new HashMap<String, Object>();
			m.put("KLAudio.size", getOrigDataListSize(rs, "KL_Audio_" + num));
			m.put("KLAlbum.size", getOrigDataListSize(rs, "KL_Album_" + num));
			m.put("QTAudio.size", getOrigDataListSize(rs, "QT_Audio_" + num));
			m.put("QTAlbum.size", getOrigDataListSize(rs, "QT_Album_" + num));
			m.put("XMLYAudio.size", getOrigDataListSize(rs, "XMLY_Audio_" + num));
			m.put("XMLYAlbum.size", getOrigDataListSize(rs, "XMLY_Album_" + num));
			m.put("cTime", System.currentTimeMillis());
			rs.set("Scheme_CrawlerInfo_" + num, JsonUtils.objToJson(m));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static boolean isOrNoCrawlerFinish(RedisOperService rs,String num) {
		boolean isok = false;
		try {
			isok = rs.exist("Scheme_CrawlerInfo_" + num);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return isok;
	}

	public static void writeEtl1Finish(RedisOperService rs, String num, String jsonstr) {
		try {
			rs.set("Scheme_Etl1Info_" + num, jsonstr);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static boolean isOrNoEtl1Finish(RedisOperService rs, String num) {
		boolean isok = false;
		try {
			isok = rs.exist("Scheme_Etl1Info_" + num);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return isok;
	}

	public static void addCrawlerSrcRecord(RedisOperService rs, String src, String srcinfo) {
		if (!rs.exist(src)) {
			rs.set(src, srcinfo, 24*60*60*1000);
		}
	}

	public static boolean isOrNoCrawlerSrcRecordExist(RedisOperService rs, String src) {
		boolean isok = false;
		isok = rs.exist(src);
		return isok;
	}

	public static void writeCompareInfo(RedisOperService rs, Object o, float f, String srcid, String crawlernum) {
		if (o instanceof AlbumPo) {
			AlbumPo al = (AlbumPo) o;
			if(f!=0&&srcid!=null&&!srcid.equals("null")) {
				rs.set(al.getAlbumPublisher()+"_Album_"+al.getId()+"_SameName_MaxProportion_"+crawlernum, f+"", 24*60*60*1000);
				rs.set(al.getAlbumPublisher()+"_Album_"+al.getId()+"_SameName_SmaId_"+crawlernum, srcid, 24*60*60*1000);
			}
		} else {
			if(o instanceof AudioPo) {
			    AudioPo au = (AudioPo) o;
			    if(f!=0&&srcid!=null&&!srcid.equals("null")) {
				    rs.set(au.getAudioPublisher()+"_Audio_"+au.getId()+"_SameName_MaxProportion_"+crawlernum, f+"", 24*60*60*1000);
				    rs.set(au.getAudioPublisher()+"_Audio_"+au.getId()+"_SameName_MaId_"+crawlernum, srcid, 24*60*60*1000);
				}
			}
		}
	}
	
	public static String getCompareSameSrcId(RedisOperService rs, Object o, String crawlernum) {
		if(o instanceof AlbumPo) {
			AlbumPo al = (AlbumPo) o;
			return rs.get(al.getAlbumPublisher()+"_Album_"+al.getId()+"_SameName_SmaId_"+crawlernum);
		}
		if(o instanceof AudioPo) {
			AudioPo au = (AudioPo) o;
			return rs.get(au.getAudioPublisher()+"_Audio_"+au.getId()+"_SameName_MaId_"+crawlernum);
		}
		return null;
	}
	
	public static String getCompareMaxProportion(RedisOperService rs,Object o, String crawlernum){
		if(o instanceof AlbumPo) {
			AlbumPo al = (AlbumPo) o;
			return rs.get(al.getAlbumPublisher()+"_Album_"+al.getId()+"_SameName_MaxProportion_"+crawlernum);
		}
		if(o instanceof AudioPo) {
			AudioPo au = (AudioPo) o;
			return rs.get(au.getAudioPublisher()+"_Audio_"+au.getId()+"_SameName_MaxProportion_"+crawlernum);
		}
		return null;
	}
	
	public static void writeSrcParticiple(RedisOperService rs, Object o, String str) {
		if(o instanceof SeqMediaAssetPo) {
			SeqMediaAssetPo sma = (SeqMediaAssetPo) o;
			if (!rs.exist(sma.getSmaPublisher()+"_Sma_"+sma.getId()+"_Participle")) 
				rs.set(sma.getSmaPublisher()+"_Sma_"+sma.getId()+"_Participle", str, 24*60*60*1000);
		}
		if(o instanceof MediaAssetPo) {
			MediaAssetPo ma = new MediaAssetPo();
			if(!rs.exist(ma.getMaPublisher()+"_Ma_"+ma.getId()+"_Participle"))
			rs.set(ma.getMaPublisher()+"_Ma_"+ma.getId()+"_Participle", str, 24*60*60*1000);
		}
	}

	public static void set(String JedisConnectionFactory,int redisDB, String key, String value) {
		RedisOperService redis = null;
		try {
			JedisConnectionFactory jedisConnectionFactory = (JedisConnectionFactory) SpringShell.getBean(JedisConnectionFactory);
			try {
				redis = new RedisOperService(jedisConnectionFactory, redisDB);
			} catch (Exception e) {
				int excepNum = 10;
				boolean isok = false;
				while (excepNum-->0) {
					try {
						redis = new RedisOperService(jedisConnectionFactory, redisDB);
					} catch (Exception e2) {continue;}
					if (redis!=null) {
						isok = true;
						break;
					}
				}
				if (isok) redis.set(key, value);
				else throw e;
			}
			redis.set(key, value);
		} catch (Exception e) {} finally {
			if (redis!=null) redis.close();
		}
	}
	
	public static void set(String JedisConnectionFactory,int redisDB, String key, String value, long expireTime) {
		RedisOperService redis = null;
		try {
			JedisConnectionFactory jedisConnectionFactory = (JedisConnectionFactory) SpringShell.getBean(JedisConnectionFactory);
			try {
				redis = new RedisOperService(jedisConnectionFactory, redisDB);
			} catch (Exception e) {
				int excepNum = 10;
				boolean isok = false;
				while (excepNum-->0) {
					try {
						redis = new RedisOperService(jedisConnectionFactory, redisDB);
					} catch (Exception e2) {continue;}
					if (redis!=null) {
						isok = true;
						break;
					}
				}
				if (isok) redis.set(key, value, expireTime);
				else throw e;
			}
			redis.set(key, value, expireTime);
		} catch (Exception e) {} finally {
			if (redis!=null) redis.close();
		}
	}
	
	public static String get(String JedisConnectionFactory,int redisDB, String key) {
		RedisOperService redis = null;
		try {
			JedisConnectionFactory jedisConnectionFactory = (JedisConnectionFactory) SpringShell.getBean(JedisConnectionFactory);
			try {
				redis = new RedisOperService(jedisConnectionFactory, redisDB);
			} catch (Exception e) {
				int excepNum = 10;
				boolean isok = false;
				while (excepNum-->0) {
					try {
						redis = new RedisOperService(jedisConnectionFactory, redisDB);
					} catch (Exception e2) {continue;}
					if (redis!=null) {
						isok = true;
						break;
					}
				}
				if (isok) return redis.get(key);
				else throw e;
			}
			return redis.get(key);
		} catch (Exception e) {} finally {
			if (redis!=null) redis.close();
		}
		return null;
	}
	
	public static void delete(String JedisConnectionFactory,int redisDB, String key) {
		RedisOperService redis = null;
		try {
			JedisConnectionFactory jedisConnectionFactory = (JedisConnectionFactory) SpringShell.getBean(JedisConnectionFactory);
			try {
				redis = new RedisOperService(jedisConnectionFactory, redisDB);
			} catch (Exception e) {
				int excepNum = 10;
				boolean isok = false;
				while (excepNum-->0) {
					try {
						redis = new RedisOperService(jedisConnectionFactory, redisDB);
					} catch (Exception e2) {continue;}
					if (redis!=null) {
						isok = true;
						break;
					}
				}
				if (isok) redis.del(key);
				else throw e;
			}
			redis.del(key);
		} catch (Exception e) {} finally {
			if (redis!=null) redis.close();
		}
	}
	
	public static Set<String> keys(String JedisConnectionFactory,int redisDB, String keys) {
		RedisOperService redis = null;
		try {
			JedisConnectionFactory jedisConnectionFactory = (JedisConnectionFactory) SpringShell.getBean(JedisConnectionFactory);
			try {
				redis = new RedisOperService(jedisConnectionFactory, redisDB);
			} catch (Exception e) {
				int excepNum = 10;
				boolean isok = false;
				while (excepNum-->0) {
					try {
						redis = new RedisOperService(jedisConnectionFactory, redisDB);
					} catch (Exception e2) {continue;}
					if (redis!=null) {
						isok = true;
						break;
					}
				}
				if (isok) return redis.keys(keys);
				else throw e;
			}
			return redis.keys(keys);
		} catch (Exception e) {} finally {
			if (redis!=null) redis.close();
		}
		return null;
	}
	
	/**
	 * 
	 * @param JedisConnectionFactory
	 * @param redisDB
	 * @param Id
	 * @param purpose 目的
	 * 				=0 保证正在入中间库唯一性
	 * 				=1保证待抓取唯一性
	 * 				=2保证正在入正式库唯一性
	 * 				=3保证已入正式库唯一性
	 * 				=4清除所有
	 * @return
	 */
	public static void keepKeyToOnly(String JedisConnectionFactory,int redisDB, String id, int purpose) {
		Set<String> sets = keys(JedisConnectionFactory, redisDB, "*" + id + "*");
		String[] tempStrs = {"LOADCRAWLERDB:","CRAWLERDB:","LOADWT:","WT:"};
		boolean isok = false;
		if (sets!=null && sets.size()>0) {
			for (String set : sets) {
				if (set!=null) {
					if (purpose!=4 && set.equals(tempStrs[purpose]+id)) isok = true;
					else delete(JedisConnectionFactory, redisDB, set);
				}
			}
		}
		if (purpose!=4 && !isok) set(JedisConnectionFactory, redisDB, tempStrs[purpose]+id, System.currentTimeMillis()+"");
	}
}
