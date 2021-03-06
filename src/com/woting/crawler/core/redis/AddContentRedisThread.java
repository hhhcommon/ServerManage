package com.woting.crawler.core.redis;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;

import com.spiritdata.framework.ext.spring.redis.RedisOperService;
import com.spiritdata.framework.util.FileNameUtils;
import com.spiritdata.framework.util.JsonUtils;
import com.woting.crawler.ext.SpringShell;

public class AddContentRedisThread extends Thread {

	private String smaId;
	private RedisOperService redis;
	private DataSource DataSource = null;
	
	public AddContentRedisThread(String smaId) {
		this.smaId = smaId;
		JedisConnectionFactory jedisConnectionFactory = (JedisConnectionFactory) SpringShell.getBean("connectionFactoryContent");
		this.redis = new RedisOperService(jedisConnectionFactory, 11);
		this.DataSource = (DataSource) SpringShell.getBean("dataSource");
	}
	
	@Override
	public void run() {
		addRedia();
	}
	
	public void addRedia() {
		try {
			Map<String, Object> oneDate = makeSeqMediaAssetInfo(smaId); // 获得专辑相关静态信息
			List<String> maIds = getSeqMARef(smaId); // 获得专辑下级节目id列表
			if (oneDate!=null && oneDate.size()>0) {
				if (maIds!=null && maIds.size()>0) {
					oneDate.put("ContentSubCount", maIds.size());
				}
				addRedisInfo("Content::MediaType_CID::[SEQU_"+smaId+"]::INFO", JsonUtils.objToJson(oneDate), 30*24*60*60*1000l);
			}
			
			List<Map<String, Object>> reply = new ArrayList<>();
			if (maIds!=null && maIds.size()>0) {
				List<String> retMaIds = new ArrayList<>();
				for (String str : maIds) {
					Map<String, Object> m = new HashMap<>();
					m.put("id", str);
					m.put("type", "wt_MediaAsset");
					reply.add(m);
					retMaIds.add("Content::MediaType_CID::[AUDIO_"+str+"]");
				}
				addRedisInfo("Content::MediaType_CID::[SEQU_"+smaId+"]::SUBLIST", JsonUtils.objToJson(retMaIds), 30*24*60*60*1000l);
			}
			List<Map<String, Object>> maLs = getMediaAssetInfos(maIds);
			if (maLs!=null && maLs.size()>0) {
				for (Map<String, Object> map : maLs) {
					Map<String, Object> smam = new HashMap<>();
					smam.put("ContentId", smaId);
					map.put("SeqInfo", smam);
					addRedisInfo("Content::MediaType_CID::[AUDIO_"+map.get("ContentId")+"]::INFO", JsonUtils.objToJson(map), 30*24*60*60*1000l);
				}
			}
			Map<String, Object> m = new HashMap<>();
			m.put("id", smaId);
			m.put("type", "wt_SeqMediaAsset");
			reply.add(m);
			List<Map<String, Object>> playLs = getPlayCountInfo(reply);
			if (playLs!=null && playLs.size()>0) {
				for (Map<String, Object> map : playLs) {
					addRedisInfo("Content::MediaType_CID::["+map.get("type")+"_"+map.get("id")+"]::PLAYCOUNT", map.get("playcount")+"", 30*24*60*60*1000l);
				}
			}
			redis.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private Map<String, Object> makeSeqMediaAssetInfo(String smaId) {
		Map<String, Object> oneDate = new HashMap<String, Object>();
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = DataSource.getConnection();
			String sql = "SELECT id,smaTitle,smaPublisher,keyWords,descn,smaImg,cTime FROM wt_SeqMediaAsset where id = '"+smaId+"'";
			try {
				ps = conn.prepareStatement(sql);
				rs = ps.executeQuery();
				while (rs != null && rs.next()) {
					oneDate.put("ContentName", rs.getString("smaTitle"));
					oneDate.put("ContentId", rs.getString("id"));
					oneDate.put("ContentPub", rs.getString("smaPublisher"));
					oneDate.put("MediaType", "SEQU");
					oneDate.put("ContentDesc", rs.getString("descn"));
					oneDate.put("ContentImg", rs.getString("smaImg"));
					oneDate.put("ContentKeyWord", rs.getString("keyWords"));
					oneDate.put("CTime", rs.getTimestamp("cTime").getTime());
					oneDate.put("ContentPubTime", rs.getTimestamp("cTime").getTime());
					oneDate.put("ContentSubscribe", 0);
					oneDate.put("ContentFavorite", 0);
					oneDate.put("ContentShareURL", "http://www.wotingfm.com/dataCenter/zj/"+oneDate.get("ContentId").toString()+"/content.html");
				}
				if (rs!=null) try {rs.close();rs=null;} catch(Exception e) {rs=null;} finally {rs=null;};
	            if (ps!=null) try {ps.close();ps=null;} catch(Exception e) {ps=null;} finally {ps=null;};
			} catch (Exception e) {e.printStackTrace();}
			sql = "SELECT cha.channelId,ch.channelName,cha.publisherId,org.oName,cha.pubTime,cha.flowFlag FROM wt_ChannelAsset cha"
					+ " LEFT JOIN wt_Channel ch ON ch.id = cha.channelId LEFT JOIN wt_Organize org ON org.id = cha.publisherId"
					+ " where cha.assetId = '"+smaId+"' and cha.assetType = 'wt_SeqMediaAsset' and cha.flowFlag = 2";
			try {
				List<Map<String, Object>> chals = new ArrayList<>();
				ps = conn.prepareStatement(sql);
				rs = ps.executeQuery();
				while (rs != null && rs.next()) {
					Map<String, Object> chamap = new HashMap<>();
					chamap.put("FlowFlag", rs.getInt("flowFlag"));
					chamap.put("ChannelName", rs.getString("channelName"));
					chamap.put("ChannelId", rs.getString("channelId"));
					chamap.put("PubTime", rs.getTimestamp("pubTime"));
					chals.add(chamap);
				}
				oneDate.put("ContentPubChannels", chals);
				if (rs!=null) try {rs.close();rs=null;} catch(Exception e) {rs=null;} finally {rs=null;};
	            if (ps!=null) try {ps.close();ps=null;} catch(Exception e) {ps=null;} finally {ps=null;};
			} catch (Exception e) {
				e.printStackTrace();
			}
			sql = "SELECT resf.dictMid mid,dd.id did,dd.ddName,resf.refName FROM wt_ResDict_Ref resf"
					+ " LEFT JOIN plat_DictD dd ON dd.id = resf.dictDid"
					+ " where resf.resId = '"+smaId+"' and resf.resTableName = 'wt_SeqMediaAsset'";
			try {
				List<Map<String, Object>> dictls = new ArrayList<>();
				ps = conn.prepareStatement(sql);
				rs = ps.executeQuery();
				while (rs != null && rs.next()) {
					Map<String, Object> catamap = new HashMap<>();
					catamap.put("CataDid", rs.getString("did"));
					catamap.put("CataMName", rs.getString("refName"));
					catamap.put("CataTitle", rs.getString("ddName"));
					catamap.put("CataMid", rs.getString("mid"));
					dictls.add(catamap);
				}
				oneDate.put("ContentCatalogs", dictls);
				if (rs!=null) try {rs.close();rs=null;} catch(Exception e) {rs=null;} finally {rs=null;};
	            if (ps!=null) try {ps.close();ps=null;} catch(Exception e) {ps=null;} finally {ps=null;};
			} catch (Exception e) {
				e.printStackTrace();
			}
			sql = "SELECT per.id,per.pName,pef.refName FROM wt_Person_Ref pef"
					+ " LEFT JOIN wt_Person per ON per.id = pef.personId"
					+ " where pef.resId = '"+smaId+"' and pef.resTableName = 'wt_SeqMediaAsset'";
			try {
				List<Map<String, Object>> perls = new ArrayList<>();
				ps = conn.prepareStatement(sql);
				rs = ps.executeQuery();
				while (rs != null && rs.next()) {
					Map<String, Object> permap = new HashMap<>();
					permap.put("PerId", rs.getString("id"));
					permap.put("PerName", rs.getString("pName"));
					permap.put("RefName", rs.getString("refName"));
					perls.add(permap);
				}
				oneDate.put("ContentPersons", perls);
				if (rs!=null) try {rs.close();rs=null;} catch(Exception e) {rs=null;} finally {rs=null;};
	            if (ps!=null) try {ps.close();ps=null;} catch(Exception e) {ps=null;} finally {ps=null;};
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (oneDate!=null && oneDate.size()>0) {
				return oneDate;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
            if (rs!=null) try {rs.close();rs=null;} catch(Exception e) {rs=null;} finally {rs=null;};
            if (ps!=null) try {ps.close();ps=null;} catch(Exception e) {ps=null;} finally {ps=null;};
            if (conn!=null) try {conn.close();conn=null;} catch(Exception e) {conn=null;} finally {conn=null;};
        }
		return null;
	}
	
	private List<String> getSeqMARef(String smaId) {
		if (smaId!=null) {
			List<String> maIds = new ArrayList<>();
			Connection conn = null;
			PreparedStatement ps = null;
			ResultSet rs = null;
			String sql = "SELECT mId,columnNum FROM wt_SeqMA_Ref"
					+ " where sId = '"+smaId+"'"
					+ " ORDER BY columnNum DESC";
			try {
				conn = DataSource.getConnection();
				ps = conn.prepareStatement(sql);
				rs = ps.executeQuery();
				while (rs != null && rs.next()) {
					maIds.add(rs.getString("mId"));
				}
				if (maIds!=null && maIds.size()>0) {
					return maIds;
				}
				if (rs!=null) try {rs.close();rs=null;} catch(Exception e) {rs=null;} finally {rs=null;};
	            if (ps!=null) try {ps.close();ps=null;} catch(Exception e) {ps=null;} finally {ps=null;};
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
	            if (rs!=null) try {rs.close();rs=null;} catch(Exception e) {rs=null;} finally {rs=null;};
	            if (ps!=null) try {ps.close();ps=null;} catch(Exception e) {ps=null;} finally {ps=null;};
	            if (conn!=null) try {conn.close();conn=null;} catch(Exception e) {conn=null;} finally {conn=null;};
	        }
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	private List<Map<String, Object>> getMediaAssetInfos(List<String> maIds) {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			if (maIds!=null && maIds.size()>0) {
				List<Map<String, Object>> retLs = new ArrayList<>();
				String ids = "";
				for (String str : maIds) {
					ids += " or id = '"+str+"'";
				}
				ids = ids.substring(3);
				ids = "("+ids+")";
				String sql = "SELECT ma.id,ma.maTitle,ma.maPublisher,ma.keyWords,ma.descn,ma.maImg,ma.timeLong,ma.cTime,mas.playURI FROM wt_MediaAsset ma"
						+ " LEFT JOIN wt_MaSource mas ON mas.maId = ma.id and mas.isMain = 1"
						+ " where "+ids.replace("id", "ma.id");
				conn = DataSource.getConnection();
				try {
					ps = conn.prepareStatement(sql);
					rs = ps.executeQuery();
					while (rs != null && rs.next()) {
						Map<String, Object> oneDate = new HashMap<>();
						oneDate.put("ContentName", rs.getString("maTitle"));
						oneDate.put("ContentId", rs.getString("id"));
						oneDate.put("ContentPub", rs.getString("maPublisher"));
						oneDate.put("MediaType", "AUDIO");
						oneDate.put("ContentDesc", rs.getString("descn"));
						oneDate.put("ContentImg", rs.getString("maImg"));
						oneDate.put("CTime", rs.getTimestamp("cTime").getTime());
						oneDate.put("ContentPlay", rs.getString("playURI"));
						oneDate.put("ContentKeyWord", rs.getString("keyWords"));
						oneDate.put("ContentTimes", rs.getLong("timeLong"));
						oneDate.put("ContentPubTime", rs.getTimestamp("cTime").getTime());
						oneDate.put("ContentShareURL", "http://www.wotingfm.com/dataCenter/jm/"+oneDate.get("ContentId").toString()+"/content.html");
						try {
							String ext = FileNameUtils.getExt(oneDate.containsKey("ContentPlay")?(oneDate.get("ContentPlay").toString()):null);
					        
					        if (ext!=null) {
					        	if (ext.contains("?")) {
								    ext = ext.replace(ext.substring(ext.indexOf("?"), ext.length()), ""); 
							    }
					        	oneDate.put("ContentPlayType", ext.contains("/flv")?"flv":ext.replace(".", ""));
							} else {
								oneDate.put("ContentPlayType", null);
							}
						} catch (Exception e) {
							oneDate.put("ContentPlayType", null);
						}
						retLs.add(oneDate);
					}
					if (rs!=null) try {rs.close();rs=null;} catch(Exception e) {rs=null;} finally {rs=null;};
		            if (ps!=null) try {ps.close();ps=null;} catch(Exception e) {ps=null;} finally {ps=null;};
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (retLs!=null && retLs.size()==0) {
					return null;
				}
				sql = "SELECT cha.assetId,cha.channelId,ch.channelName,cha.publisherId,org.oName,cha.pubTime,cha.flowFlag FROM wt_ChannelAsset cha"
						+ " LEFT JOIN wt_Channel ch ON ch.id = cha.channelId LEFT JOIN wt_Organize org ON org.id = cha.publisherId"
						+ " where "+ids.replace("id", "cha.assetId")+" and cha.assetType = 'wt_MediaAsset' and cha.flowFlag = 2";
				try {
					ps = conn.prepareStatement(sql);
					rs = ps.executeQuery();
					while (rs != null && rs.next()) {
						Map<String, Object> chamap = new HashMap<>();
						chamap.put("FlowFlag", rs.getInt("flowFlag"));
						chamap.put("ChannelName", rs.getString("channelName"));
						chamap.put("ChannelId", rs.getString("channelId"));
						chamap.put("PubTime", rs.getTimestamp("pubTime").getTime());
						for (Map<String, Object> map : retLs) {
							if (map.get("ContentId").equals(rs.getString("assetId"))) {
								if (map.containsKey("ContentPubChannels")) {
									List<Map<String, Object>> chamapls = (List<Map<String, Object>>) map.get("ContentPubChannels");
									if (chamapls!=null) {
										chamapls.add(chamap);
									} else {
										chamapls = new ArrayList<>();
										chamapls.add(chamap);
										map.put("ContentPubChannels", chamapls);
									}
								} else {
									List<Map<String, Object>> chamapls = new ArrayList<>();
									chamapls.add(chamap);
									map.put("ContentPubChannels", chamapls);
								}
							}
						}
					}
					if (rs!=null) try {rs.close();rs=null;} catch(Exception e) {rs=null;} finally {rs=null;};
		            if (ps!=null) try {ps.close();ps=null;} catch(Exception e) {ps=null;} finally {ps=null;};
				} catch (Exception e) {
					e.printStackTrace();
				}
				sql = "SELECT resf.dictMid mid,dd.id did,dd.ddName,resf.refName,resf.resId FROM wt_ResDict_Ref resf"
						+ " LEFT JOIN plat_DictD dd ON dd.id = resf.dictDid"
						+ " where "+ids.replace("id", "resf.resId")+" and resf.resTableName = 'wt_MediaAsset'";
				try {
					ps = conn.prepareStatement(sql);
					rs = ps.executeQuery();
					while (rs != null && rs.next()) {
						Map<String, Object> catamap = new HashMap<>();
						catamap.put("CataDid", rs.getString("did"));
						catamap.put("CataMName", rs.getString("refName"));
						catamap.put("CataTitle", rs.getString("ddName"));
						catamap.put("CataMid", rs.getString("mid"));
						for (Map<String, Object> map : retLs) {
							if (map.get("ContentId").equals(rs.getString("resId"))) {
								if (map.containsKey("ContentCatalogs")) {
									List<Map<String, Object>> catamapls = (List<Map<String, Object>>) map.get("ContentCatalogs");
									if (catamapls!=null) {
										catamapls.add(catamap);
									} else {
										catamapls = new ArrayList<>();
										catamapls.add(catamap);
										map.put("ContentCatalogs", catamapls);
									}
								} else {
									List<Map<String, Object>> catamapls = new ArrayList<>();
									catamapls.add(catamap);
									map.put("ContentCatalogs", catamapls);
								}
							}
						}
					}
					if (rs!=null) try {rs.close();rs=null;} catch(Exception e) {rs=null;} finally {rs=null;};
		            if (ps!=null) try {ps.close();ps=null;} catch(Exception e) {ps=null;} finally {ps=null;};
				} catch (Exception e) {
					e.printStackTrace();
				}
				sql = "SELECT per.id,per.pName,pef.refName,pef.resId FROM wt_Person_Ref pef"
						+ " LEFT JOIN wt_Person per ON per.id = pef.personId"
						+ " where "+ids.replace("id", "pef.resId")+" and pef.resTableName = 'wt_MediaAsset'";
				try {
					ps = conn.prepareStatement(sql);
					rs = ps.executeQuery();
					while (rs != null && rs.next()) {
						Map<String, Object> permap = new HashMap<>();
						permap.put("PerId", rs.getString("id"));
						permap.put("PerName", rs.getString("pName"));
						permap.put("RefName", rs.getString("refName"));
						for (Map<String, Object> map : retLs) {
							if (map.get("ContentId").equals(rs.getString("resId"))) {
								if (map.containsKey("ContentPersons")) {
									List<Map<String, Object>> pomapls = (List<Map<String, Object>>) map.get("ContentPersons");
									if (pomapls!=null) {
										pomapls.add(permap);
									} else {
										pomapls = new ArrayList<>();
										pomapls.add(permap);
										map.put("ContentPersons", pomapls);
									}
								} else {
									List<Map<String, Object>> pomapls = new ArrayList<>();
									pomapls.add(permap);
									map.put("ContentPersons", pomapls);
								}
							}
						}
					}
					if (rs!=null) try {rs.close();rs=null;} catch(Exception e) {rs=null;} finally {rs=null;};
		            if (ps!=null) try {ps.close();ps=null;} catch(Exception e) {ps=null;} finally {ps=null;};
				} catch (Exception e) {
					e.printStackTrace();
				}
				return retLs;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
            if (rs!=null) try {rs.close();rs=null;} catch(Exception e) {rs=null;} finally {rs=null;};
            if (ps!=null) try {ps.close();ps=null;} catch(Exception e) {ps=null;} finally {ps=null;};
            if (conn!=null) try {conn.close();conn=null;} catch(Exception e) {conn=null;} finally {conn=null;};
        }
		return null;
	}
	
	private List<Map<String, Object>> getPlayCountInfo(List<Map<String, Object>> ls) {
		String sql = "";
		if (ls!=null && ls.size()>0) {
			for (Map<String, Object> map : ls) {
				sql += " UNION SELECT resId,resTableName,playCount FROM wt_MediaPlayCount"
						+ " where resId = '"+map.get("id")+"' AND resTableName = '"+map.get("type")+"'";
			}
			sql = sql.substring(6);
			List<Map<String, Object>> retLs = new ArrayList<>();
			Connection conn = null;
			PreparedStatement ps = null;
			ResultSet rs = null;
			try {
				conn = DataSource.getConnection();
				ps = conn.prepareStatement(sql);
				rs = ps.executeQuery();
				while (rs != null && rs.next()) {
					Map<String, Object> m = new HashMap<>();
					m.put("id", rs.getString("resId"));
					m.put("playcount", rs.getLong("playCount"));
					m.put("type", rs.getString("resTableName").equals("wt_SeqMediaAsset")?"SEQU":"AUDIO");
					retLs.add(m);
				}
				if (retLs!=null && retLs.size()>0) {
					return retLs;
				}
				if (rs!=null) try {rs.close();rs=null;} catch(Exception e) {rs=null;} finally {rs=null;};
	            if (ps!=null) try {ps.close();ps=null;} catch(Exception e) {ps=null;} finally {ps=null;};
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
	            if (rs!=null) try {rs.close();rs=null;} catch(Exception e) {rs=null;} finally {rs=null;};
	            if (ps!=null) try {ps.close();ps=null;} catch(Exception e) {ps=null;} finally {ps=null;};
	            if (conn!=null) try {conn.close();conn=null;} catch(Exception e) {conn=null;} finally {conn=null;};
	        }
		}
		return null;
	}
	
	private void addRedisInfo(String key, String info, long timeout) {
		if (redis!=null && key!=null) {
			try {
				redis.set(key, info, timeout);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
