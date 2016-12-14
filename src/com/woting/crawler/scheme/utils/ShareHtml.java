package com.woting.crawler.scheme.utils;

import java.util.HashMap;
import java.util.Map;
import org.jsoup.Jsoup;

public class ShareHtml extends Thread {
	private String contentId;
	private String mediaType;
	
	public ShareHtml(String contentId, String mediaType) {
		this.contentId = contentId;
		this.mediaType = mediaType;
	}

	public boolean getShareHtml(String contentId, String mediaType) {
		try {
			Map<String, String> data = new HashMap<>();
			data.put("ContentId", contentId);
			data.put("MediaType", mediaType);
			Jsoup.connect("http://10.172.161.67:908/CM/content/getShareHtml.do").ignoreContentType(true).data(data).post();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	public void run() {
		boolean isok = false;
		int num = 0;
		while(isok) {
			num++;
			isok = getShareHtml(contentId, mediaType);
			if (num==10) {
				break;
			}
		}
	}
}
