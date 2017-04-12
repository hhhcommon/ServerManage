package com.woting.crawler.scheme.cacherefresh;

import java.util.List;
import java.util.Map;

import com.woting.cm.core.channel.persis.po.ChannelPo;
import com.woting.cm.core.channel.service.ChannelService;
import com.woting.crawler.ext.SpringShell;

public class CacheRefresh {
	private ChannelService channelService;
	
	public CacheRefresh() {
		channelService  = (ChannelService) SpringShell.getBean("channelService");
	}

	public Map<String, Object> begCacheRefresh() {
		if (channelService!=null) {
			List<ChannelPo> chs = channelService.getChannelListNoRef();
			if (chs!=null) {
				for (ChannelPo channelPo : chs) {
					ChannelPo upch = new ChannelPo();
					upch.setId(channelPo.getId());
					upch.setIsValidate(2);
					channelService.updateChannel(upch);
				}
			}
		}
		return null;
	}
}
