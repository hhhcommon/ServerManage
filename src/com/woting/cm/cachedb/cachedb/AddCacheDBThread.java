package com.woting.cm.cachedb.cachedb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import com.spiritdata.framework.util.FileNameUtils;
import com.spiritdata.framework.util.JsonUtils;
import com.woting.cm.cachedb.cachedb.persis.po.CacheDBPo;
import com.woting.cm.cachedb.cachedb.service.CacheDBService;
import com.woting.cm.cachedb.playcountdb.persis.po.PlayCountDBPo;
import com.woting.cm.cachedb.playcountdb.service.PlayCountDBService;
import com.woting.crawler.ext.SpringShell;

public class AddCacheDBThread extends Thread {
	private String id;
	private String mediaType;
	private DataSource DataSource = null;
	private CacheDBService cacheDBService;
	private PlayCountDBService playCountDBService;
	
	public AddCacheDBThread(String id, String mediaType) {
		this.id = id;
		this.mediaType = mediaType;
        this.DataSource = (DataSource) SpringShell.getBean("dataSource");
        this.cacheDBService = (CacheDBService) SpringShell.getBean("cacheDBService");
        this.playCountDBService = (PlayCountDBService) SpringShell.getBean("playCountDBService");
	}
	
	@Override
	public void run() {
		addCacheDB();
	}
	
	public void addCacheDB() {
		try {
			if (id==null && mediaType==null) return ;
			if (mediaType.equals("SEQU")) addSeqInfoToCacheDB();
			else if (mediaType.equals("AUDIO")) addMaInfoToCacheDB();
			else if (mediaType.equals("RADIO")) addBroadcastCacheDB();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void addBroadcastCacheDB() {
		Map<String, Object> oneDate = new HashMap<String, Object>();
    	Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = DataSource.getConnection();
			String sql = "select a.*, b.bcSource, b.flowURI from wt_Broadcast a left join wt_BCLiveFlow b on a.id=b.bcId and b.isMain=1 where a.id= '"+id+"'";
			try {
				ps = conn.prepareStatement(sql);
				rs = ps.executeQuery();
				while (rs != null && rs.next()) {
					oneDate.put("ContentName", rs.getString("bcTitle"));
					oneDate.put("ContentId", rs.getString("id"));
					oneDate.put("ContentPub", rs.getString("bcPublisher"));
					oneDate.put("MediaType", "RADIO");
					oneDate.put("ContentDescn", rs.getString("descn"));
					oneDate.put("ContentImg", rs.getString("bcImg"));
					oneDate.put("CTime", rs.getTimestamp("cTime").getTime());
					oneDate.put("ContentPlay", rs.getString("flowURI"));
					oneDate.put("ContentSource", rs.getString("bcSource"));
					oneDate.put("ContentURIS", null);
					oneDate.put("ContentPubTime", rs.getTimestamp("cTime").getTime());
					oneDate.put("ContentFreq", null);
					oneDate.put("ContentFreqs", null);
					oneDate.put("IsPlaying", null);
					oneDate.put("PlayCount", 0);
					oneDate.put("ContentList", null);
					oneDate.put("ContentFavorite", 0);
					oneDate.put("ContentShareURL", "http://www.wotingfm.com/share/mweb/bc/"+oneDate.get("ContentId").toString()+"/content.html");
					try {
						String ext = FileNameUtils.getExt(oneDate.containsKey("ContentPlay")?(oneDate.get("ContentPlay").toString()):null);
				        if (ext!=null) {
				        	if (ext.contains("?")) ext = ext.replace(ext.substring(ext.indexOf("?"), ext.length()), ""); 
				        	oneDate.put("ContentPlayType", ext.contains("/flv")?"flv":ext.replace(".", ""));
						} else oneDate.put("ContentPlayType", null);
					} catch (Exception e) {
						oneDate.put("ContentPlayType", null);
					}
					
				}
				if (rs!=null) try {rs.close();rs=null;} catch(Exception e) {rs=null;} finally {rs=null;};
	            if (ps!=null) try {ps.close();ps=null;} catch(Exception e) {ps=null;} finally {ps=null;};
			} catch (Exception e) {e.printStackTrace();}
			
			sql = "SELECT cha.channelId,ch.channelName,cha.publisherId,org.oName,cha.pubTime,cha.flowFlag FROM wt_ChannelAsset cha"
					+ " LEFT JOIN wt_Channel ch ON ch.id = cha.channelId LEFT JOIN wt_Organize org ON org.id = cha.publisherId"
					+ " where cha.assetId = '"+id+"' and cha.assetType = 'wt_Broadcast' and cha.flowFlag = 2";
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
				if (chals==null || chals.size()==0) return;
				oneDate.put("ContentPubChannels", chals);
				if (rs!=null) try {rs.close();rs=null;} catch(Exception e) {rs=null;} finally {rs=null;};
	            if (ps!=null) try {ps.close();ps=null;} catch(Exception e) {ps=null;} finally {ps=null;};
			} catch (Exception e) {e.printStackTrace();}
			
			sql = "SELECT resf.dictMid mid,dd.id did,dd.ddName,resf.refName FROM wt_ResDict_Ref resf"
					+ " LEFT JOIN plat_DictD dd ON dd.id = resf.dictDid"
					+ " where resf.resId = '"+id+"' and resf.resTableName = 'wt_Broadcast'";
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
			} catch (Exception e) {e.printStackTrace();}
			
			sql = "SELECT per.id,per.pName,pef.refName FROM wt_Person_Ref pef"
					+ " LEFT JOIN wt_Person per ON per.id = pef.personId"
					+ " where pef.resId = '"+id+"' and pef.resTableName = 'wt_Broadcast'";
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
			} catch (Exception e) {e.printStackTrace();}
			CacheDBPo cacheDBPo = new CacheDBPo();
			cacheDBPo.setId("RADIO_"+id+"_INFO");
			cacheDBPo.setResTableName("wt_Broadcast");
			cacheDBPo.setResId(id);
			cacheDBPo.setValue(JsonUtils.objToJson(oneDate));
			cacheDBService.insertCacheDBPo(cacheDBPo);
			System.out.println(JsonUtils.objToJson(cacheDBPo));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
            if (rs!=null) try {rs.close();rs=null;} catch(Exception e) {rs=null;} finally {rs=null;};
            if (ps!=null) try {ps.close();ps=null;} catch(Exception e) {ps=null;} finally {ps=null;};
            if (conn!=null) try {conn.close();conn=null;} catch(Exception e) {conn=null;} finally {conn=null;};
        }
		
	}

	private void addSeqInfoToCacheDB() {
		Map<String, Object> oneDate = makeSeqMediaAssetInfo(id); // 获得专辑相关静态信息
		List<String> maIds = getSeqMARef(id); // 获得专辑下级节目id列表
		if (oneDate!=null && oneDate.size()>0) {
			if (maIds!=null && maIds.size()>0) {
				oneDate.put("ContentSubCount", maIds.size());
			}
			CacheDBPo cacheDBPo = new CacheDBPo();
			cacheDBPo.setId("SEQU_"+id+"_INFO");
			cacheDBPo.setResTableName("wt_SeqMediaAsset");
			cacheDBPo.setResId(id);
			cacheDBPo.setValue(JsonUtils.objToJson(oneDate));
			cacheDBService.insertCacheDBPo(cacheDBPo);
		}
		
		List<Map<String, Object>> reply = new ArrayList<>();
		if (maIds!=null && maIds.size()>0) {
			List<String> retMaIds = new ArrayList<>();
			for (String str : maIds) {
				Map<String, Object> m = new HashMap<>();
				m.put("id", str);
				m.put("type", "wt_MediaAsset");
				reply.add(m);
				retMaIds.add("AUDIO_"+str);
			}
			CacheDBPo cacheDBPo = new CacheDBPo();
			cacheDBPo.setId("SEQU_"+id+"_SUBLIST");
			cacheDBPo.setResTableName("wt_SeqMediaAsset");
			cacheDBPo.setResId(id);
			cacheDBPo.setValue(JsonUtils.objToJson(retMaIds));
			cacheDBService.insertCacheDBPo(cacheDBPo);
		}
		List<Map<String, Object>> maLs = getMediaAssetInfos(maIds);
		if (maLs!=null && maLs.size()>0) {
			for (Map<String, Object> map : maLs) {
				Map<String, Object> smam = new HashMap<>();
				smam.put("ContentId", id);
				map.put("SeqInfo", smam);
				CacheDBPo cacheDBPo = new CacheDBPo();
				cacheDBPo.setId("AUDIO_"+map.get("ContentId")+"_INFO");
				cacheDBPo.setResTableName("wt_MediaAsset");
				cacheDBPo.setResId(map.get("ContentId")+"");
				cacheDBPo.setValue(JsonUtils.objToJson(map));
				cacheDBService.insertCacheDBPo(cacheDBPo);
			}
		}
		Map<String, Object> m = new HashMap<>();
		m.put("id", id);
		m.put("type", "wt_SeqMediaAsset");
		reply.add(m);
		List<Map<String, Object>> playLs = getPlayCountInfo(reply);
		if (playLs!=null && playLs.size()>0) {
			for (Map<String, Object> map : playLs) {
				PlayCountDBPo playCountDBPo = new PlayCountDBPo();
				playCountDBPo.setId(map.get("type")+"_"+map.get("id")+"_PLAYCOUNT");
				playCountDBPo.setResTableName(map.get("type")+"");
				playCountDBPo.setResId(map.get("id").toString());
				playCountDBPo.setPlayCount(Long.valueOf(map.get("playcount").toString()));
				playCountDBService.insertPlayCountDBPo(playCountDBPo);
			}
		}
	}
	
    @SuppressWarnings("unchecked")
	private void addMaInfoToCacheDB() {
    	Map<String, Object> oneDate = new HashMap<String, Object>();
    	Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = DataSource.getConnection();
			String sql = "SELECT * FROM wt_MediaAsset where id = '" + id + "'";
			try {
				ps = conn.prepareStatement(sql);
				rs = ps.executeQuery();
				while (rs != null && rs.next()) {
					oneDate.put("ContentName", rs.getString("maTitle"));
					oneDate.put("ContentId", rs.getString("id"));
					oneDate.put("ContentPub", rs.getString("maPublisher"));
					oneDate.put("MediaType", "AUDIO");
					oneDate.put("ContentDescn", rs.getString("descn"));
					oneDate.put("ContentImg", rs.getString("maImg"));
					oneDate.put("CTime", rs.getTimestamp("cTime").getTime());
					oneDate.put("ContentPlay", rs.getString("playURI"));
					oneDate.put("ContentKeyWord", rs.getString("keyWords"));
					oneDate.put("ContentTimes", rs.getLong("timeLong"));
					oneDate.put("ContentPubTime", rs.getTimestamp("cTime").getTime());
					oneDate.put("ContentShareURL", "http://www.wotingfm.com/share/mweb/jm/"+oneDate.get("ContentId").toString()+"/content.html");
					try {
						String ext = FileNameUtils.getExt(oneDate.containsKey("ContentPlay")?(oneDate.get("ContentPlay").toString()):null);
				        if (ext!=null) {
				        	if (ext.contains("?")) ext = ext.replace(ext.substring(ext.indexOf("?"), ext.length()), ""); 
				        	oneDate.put("ContentPlayType", ext.contains("/flv")?"flv":ext.replace(".", ""));
						} else oneDate.put("ContentPlayType", null);
					} catch (Exception e) {
						oneDate.put("ContentPlayType", null);
					}
				}
				if (rs!=null) try {rs.close();rs=null;} catch(Exception e) {rs=null;} finally {rs=null;};
	            if (ps!=null) try {ps.close();ps=null;} catch(Exception e) {ps=null;} finally {ps=null;};
			} catch (Exception e) {e.printStackTrace();}
			List<String> smaIds = new ArrayList<>();
			if (oneDate==null || oneDate.size()==0) return ;
			sql = "SELECT * FROM wt_SeqMA_Ref where mId = '" + id + "'";
			try {
				conn = DataSource.getConnection();
				ps = conn.prepareStatement(sql);
				rs = ps.executeQuery();
				while (rs != null && rs.next()) {
					smaIds.add(rs.getString("sId"));
				}
				if (rs!=null) try {rs.close();rs=null;} catch(Exception e) {rs=null;} finally {rs=null;};
	            if (ps!=null) try {ps.close();ps=null;} catch(Exception e) {ps=null;} finally {ps=null;};
			} catch (Exception e) {e.printStackTrace();}
			if (smaIds==null || smaIds.size()==0) return;
			for (String smaId : smaIds) {
				try {
					CacheDBPo seqSubList = cacheDBService.getCacheDBPoById("SEQU_" + smaId + "_SUBLIST");
					if (seqSubList==null) continue;
					String subValue = seqSubList.getValue();
					if (subValue.contains(id)) continue;
					CacheDBPo cacheDBPo = cacheDBService.getCacheDBPoById("SEQU_" + smaId + "_INFO");
					if (cacheDBPo==null) continue;
					String jsonStr = cacheDBPo.getValue();
					Map<String, Object> seqMap = (Map<String, Object>) JsonUtils.jsonToObj(jsonStr, Map.class);
					if (seqMap==null) continue;
					String subCount = seqMap.get("ContentSubCount").toString();
					int subCountNum = Integer.valueOf(subCount);
					subCountNum ++;
					seqMap.put("ContentSubCount", subCountNum);
					cacheDBPo.setValue(JsonUtils.objToJson(seqMap));
					cacheDBService.insertCacheDBPo(cacheDBPo);
					List<String> maIds = new ArrayList<>();
					sql = "SELECT mId,columnNum FROM wt_SeqMA_Ref"
							+ " where sId = '"+smaId+"'"
							+ " ORDER BY columnNum DESC";
					try {
						ps = conn.prepareStatement(sql);
						rs = ps.executeQuery();
						while (rs != null && rs.next()) {
							maIds.add(rs.getString("mId"));
						}
						if (rs!=null) try {rs.close();rs=null;} catch(Exception e) {rs=null;} finally {rs=null;};
			            if (ps!=null) try {ps.close();ps=null;} catch(Exception e) {ps=null;} finally {ps=null;};
					} catch (Exception e) {e.printStackTrace();}
					seqSubList.setValue(JsonUtils.objToJson(maIds));
					cacheDBService.insertCacheDBPo(seqSubList);
					maIds.clear(); //id清理
					maIds.add(id);
					List<Map<String, Object>> maLs = getMediaAssetInfos(maIds);
					if (maLs!=null && maLs.size()>0) {
						for (Map<String, Object> map : maLs) {
							Map<String, Object> smam = new HashMap<>();
							smam.put("ContentId", smaId);
							map.put("SeqInfo", smam);
							CacheDBPo maInfo = new CacheDBPo();
							maInfo.setId("AUDIO_"+map.get("ContentId")+"_INFO");
							maInfo.setResTableName("wt_MediaAsset");
							maInfo.setResId(map.get("ContentId")+"");
							maInfo.setValue(JsonUtils.objToJson(map));
							cacheDBService.insertCacheDBPo(cacheDBPo);
						}
					}
				} catch (Exception e) {continue;}
			}
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
					oneDate.put("ContentDescn", rs.getString("descn"));
					oneDate.put("ContentImg", rs.getString("smaImg"));
					oneDate.put("ContentKeyWord", rs.getString("keyWords"));
					oneDate.put("CTime", rs.getTimestamp("cTime").getTime());
					oneDate.put("ContentPubTime", rs.getTimestamp("cTime").getTime());
					oneDate.put("ContentSubscribe", 0);
					oneDate.put("ContentFavorite", 0);
					oneDate.put("ContentShareURL", "http://www.wotingfm.com/share/mweb/zj/"+oneDate.get("ContentId").toString()+"/content.html");
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
						oneDate.put("ContentDescn", rs.getString("descn"));
						oneDate.put("ContentImg", rs.getString("maImg"));
						oneDate.put("CTime", rs.getTimestamp("cTime").getTime());
						oneDate.put("ContentPlay", rs.getString("playURI"));
						oneDate.put("ContentKeyWord", rs.getString("keyWords"));
						oneDate.put("ContentTimes", rs.getLong("timeLong"));
						oneDate.put("ContentPubTime", rs.getTimestamp("cTime").getTime());
						oneDate.put("ContentShareURL", "http://www.wotingfm.com/share/mweb/jm/"+oneDate.get("ContentId").toString()+"/content.html");
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
}
