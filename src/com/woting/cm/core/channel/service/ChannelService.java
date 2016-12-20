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
import com.woting.cm.core.channel.persis.po.ChannelPo;

@Service
public class ChannelService {
	@Resource(name = "defaultDAO")
	private MybatisDAO<ChannelPo> channelDao;
	@Resource(name = "defaultDAO")
	private MybatisDAO<ChannelAssetPo> ChannelAssetDao;

	@PostConstruct
	public void initParam() {
		channelDao.setNamespace("A_CHANNEL");
		ChannelAssetDao.setNamespace("A_CHANNELASSET");
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
}
