package com.woting.cm.core.channel.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import com.spiritdata.framework.core.dao.mybatis.MybatisDAO;
import com.woting.cm.core.channel.persis.po.ChannelAssetPo;
import com.woting.cm.core.channel.persis.po.ChannelMapRefPo;
import com.woting.cm.core.channel.persis.po.ChannelPo;

@Service
public class ChannelService {
	@Resource(name = "defaultDAO")
	private MybatisDAO<ChannelPo> channelDao;
	@Resource(name = "defaultDAO")
	private MybatisDAO<ChannelAssetPo> ChannelAssetDao;
	@Resource(name = "defaultDAO")
	private MybatisDAO<ChannelMapRefPo> ChannelMapRefDao;

	@PostConstruct
	public void initParam() {
		channelDao.setNamespace("A_CHANNEL");
		ChannelAssetDao.setNamespace("A_CHANNELASSET");
		ChannelMapRefDao.setNamespace("A_CHANNELMAPREF");
	}

	public void insertChannelAssetList(List<ChannelAssetPo> chalist) {
		List<ChannelAssetPo> chas = new ArrayList<>();
		int num = 0;
		for (ChannelAssetPo channelAssetPo : chalist) {
			chas.add(channelAssetPo);
			if (num == 1000) {
				Map<String, Object> m = new HashMap<>();
				m.put("list", chas);
				ChannelAssetDao.insert("insertList", m);
				chas.clear();
				num=0;
			}
			num++;
		}
		Map<String, Object> m = new HashMap<>();
		m.put("list", chas);
		ChannelAssetDao.insert("insertList", m);
	}
	
	public List<ChannelAssetPo> getChannelAssetListBy(String channelId, String assetId, String assetType) {
		Map<String, Object> m = new HashMap<>();
		if (channelId!=null) m.put("channelId", channelId);
		if (assetId!=null) m.put("assetId", assetId);
		if (assetType!=null) m.put("assetType", assetType);
		return getChannelAssetListBy(m);
		
	}
	
	public List<ChannelAssetPo> getChannelAssetListBy(Map<String, Object> m) {
		List<ChannelAssetPo> chas = ChannelAssetDao.queryForList("getList", m);
		if (chas!=null && chas.size()>0) {
			return chas;
		}
		return null;
	}

	public List<ChannelPo> getChannelList() {
		List<ChannelPo> chlist = channelDao.queryForList("getList");
		return chlist;
	}

	public void insertChannelAsset(ChannelAssetPo cha) {
		ChannelAssetDao.insert("insert", cha.toHashMap());
	}
	
	public void updateChannelAsset(List<ChannelAssetPo> cha) {
		if (cha!=null && cha.size()>0) {
			for (ChannelAssetPo channelAssetPo : cha) {
				ChannelAssetDao.update(channelAssetPo);
			}
		}
	}
	
	public void updateChannelAsset(ChannelAssetPo cha) {
		ChannelAssetDao.update(cha);
	}
	
	public List<ChannelMapRefPo> getChannelMapRefList(String channelId, String srcDid, int isValidate) {
		Map<String, Object> m = new HashMap<>();
		if (channelId!=null) m.put("channelId", channelId);
		if (srcDid!=null) m.put("srcDid", srcDid);
		if (isValidate!=0) m.put("isValidate", isValidate);
		return ChannelMapRefDao.queryForList("getList", m);
	}
	
	public List<ChannelPo> getChannelListNoRef() {
		String whereSql = "id not IN (SELECT channelId FROM wt_ChannelAsset) and isValidate = 1";
		Map<String, Object> m = new HashMap<>();
		m.put("whereSql", whereSql);
		List<ChannelPo> chs = channelDao.queryForList("getList", m);
		if (chs!=null && chs.size()>0) {
			return chs;
		}
		return null;
	}
	
	public List<ChannelPo> getChannelListRef() {
		String whereSql = "id not IN (SELECT channelId FROM wt_ChannelAsset)";
		Map<String, Object> m = new HashMap<>();
		m.put("whereSql", whereSql);
		List<ChannelPo> chs = channelDao.queryForList("getList", m);
		if (chs!=null && chs.size()>0) {
			return chs;
		}
		return null;
	}
	
	public ChannelMapRefPo getChannelMapRef(String id) {
		Map<String, Object> m = new HashMap<>();
		m.put("id", id);
		return ChannelMapRefDao.getInfoObject("getList", m);
	}
	
	public void updateChannelList(List<ChannelPo> chs) {
		if (chs!=null && chs.size()>0) {
			for (ChannelPo channelPo : chs) {
				channelDao.update(channelPo);
			}
		}
	}
	
	public void updateChannel(ChannelPo ch) {
		channelDao.update(ch);
	}
}
