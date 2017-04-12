package com.woting.crawler.scheme.utils;

import java.util.HashMap;
import java.util.Map;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShareHtml extends Thread {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private String contentId;
	private String mediaType;
	
	public ShareHtml(String contentId, String mediaType) {
		this.contentId = contentId;
		this.mediaType = mediaType;
	}

	public void getShareHtml(String contentId, String mediaType) {
		try {
			logger.info("开启线程进行专辑分享    [{}]", contentId);
			Map<String, String> data = new HashMap<>();
			data.put("ContentId", contentId);
			data.put("MediaType", mediaType);
			Jsoup.connect("http://www.wotingfm.com:908/CM/share/makeContentShareHtml.do").ignoreContentType(true).data(data).post();
		} catch (Exception e) {}
	}

	@Override
	public void run() {
	    getShareHtml(contentId, mediaType);
	}
}
