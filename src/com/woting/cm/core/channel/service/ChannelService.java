package com.woting.cm.core.channel.service;

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
	
	public void insertChannelAssetList(List<ChannelAssetPo> chalist){
		Map<String, Object> m = new HashMap<>();
		m.put("list", chalist);
		ChannelAssetDao.insert("insertList", m);
	}
	
	public List<ChannelPo> getChannelList(){
		List<ChannelPo> chlist = channelDao.queryForList("getList");
		return chlist;
	}
	
	public void insertChannelAsset(ChannelAssetPo cha){
		ChannelAssetDao.insert("insert", cha.toHashMap());
	}
}
