package com.woting.crawler.scheme.isvalidate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import com.woting.cm.core.broadcast.persis.po.BCLiveFlowPo;
import com.woting.cm.core.broadcast.persis.po.BroadcastPo;
import com.woting.cm.core.broadcast.service.BcLiveFlowService;
import com.woting.cm.core.broadcast.service.BroadcastService;
import com.woting.crawler.core.m3u8.persis.po.M3U8;
import com.woting.crawler.ext.SpringShell;
import com.woting.crawler.scheme.utils.M3U8Utils;

public class PlayUrlValid {
	
	public void verifyUrlValid() {
		BroadcastService broadcastService = (BroadcastService) SpringShell.getBean("broadcastService");
		BcLiveFlowService bcLiveFlowService = (BcLiveFlowService) SpringShell.getBean("bcLiveFlowService");
		List<BroadcastPo> bcs = broadcastService.getBroadcastList();
		if (bcs!=null && bcs.size()>0) {
			for (BroadcastPo bc : bcs) {
				List<BCLiveFlowPo> bcls = bcLiveFlowService.getBCLiveFlowListByBcId(bc.getId());
				if (bcls!=null) {
					Map<Long, Object> m = new HashMap<>();
					long[] times = new long[bcls.size()];
					for (int i=0;i<bcls.size();i++) {
						long time = isValid(bcls.get(i).getFlowURI());
						m.put(time, bcls.get(i));
						times[i] = time;
					}
					M3U8Utils.bubble_sort(times);
					boolean isok = false;
					String ismainId = null;
					for (int i=0;i<times.length;i++) {
						if (times[i]==0) {
							continue;
						} else {
							if (!isok) {
								BCLiveFlowPo bclf = (BCLiveFlowPo) m.get(times[i]);
							    ismainId = bclf.getId();
							    isok = true;
							}
						}
					}
					for (BCLiveFlowPo bcpo : bcls) {
						if (ismainId!=null && ismainId.equals(bcpo.getId())) {
							if (bcpo.getIsMain()!=1) {
								bcpo.setIsMain(1);
								bcLiveFlowService.updateBCLiveFlow(bcpo);
							}
						} else {
							if (bcpo.getIsMain()!=0) {
								bcpo.setIsMain(0);
								bcLiveFlowService.updateBCLiveFlow(bcpo);
							}
						}
					}
				}
			}
		}
	}
	
	private long isValid(String url) {
		String postfix = url.substring(url.lastIndexOf(".")+1, url.indexOf("?", url.lastIndexOf("."))==-1?url.length():url.indexOf("?", url.lastIndexOf(".")));
		if (postfix.equals("m3u8")) {
			try {
				long num1 = System.currentTimeMillis();
				Document doc= Jsoup.connect(url).ignoreContentType(true).timeout(10000).get();
				long time = System.currentTimeMillis()-num1;
				String m3u8 = doc.body().html();
				M3U8 m3 = M3U8Utils.makeM3U8(m3u8);
				if (m3.isOrNoEffective()) {
					return time;
				} else {
					return 0;
				}
			} catch (Exception e) {
				System.out.println("package com.woting.crawler.scheme.isvalidate");
				System.out.println(e.toString());
				return 0;
			}
		}
		return 0;
	}
}
