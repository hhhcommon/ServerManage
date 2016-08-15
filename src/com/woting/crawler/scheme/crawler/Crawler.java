package com.woting.crawler.scheme.crawler;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.spiritdata.framework.util.SpiritRandom;
import com.woting.crawler.scheme.KL.crawler.KLParseUtils;
import com.woting.crawler.scheme.QT.crawler.QTParseUtils;
import com.woting.crawler.scheme.XMLY.crawler.XMLYParseUtils;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.url.WebURL;

public class Crawler extends WebCrawler {

	@Override
	public void onStart() {
	}
	
	@Override
	public boolean shouldVisit(Page referringPage, WebURL url) {
		String href = url.getURL().toLowerCase();
        href=href.trim().toLowerCase();
        //url判断
        //考拉url判断   只抓取专辑和单体
        if (href.startsWith("http://www.kaolafm.com/zj/") || href.startsWith("http://www.kaolafm.com/jm/")) return true;
        //蜻蜓url判断 只抓去专辑及专辑下单体
        if (href.startsWith("http://www.qingting.fm/vchannels")){
        	url.setURL(href.replace("http://www.qingting.fm", "http://www.qingting.fm/s"));
        	return true;
        }
        //喜马拉雅url判断 抓取专辑和单体
        if (href.startsWith("http://www.ximalaya.com")) return true;
        return false;
	}
	
	@Override
	public void visit(Page page) {
		try {
            Thread.sleep(SpiritRandom.getRandom(new Random(), 10, 100));
        } catch (Exception e) {
            e.printStackTrace();
        }
		Map<String, Object> parseData = new HashMap<String,Object>();
    	String url = page.getWebURL().getURL().trim();
    	int pageType = ParseUtils.getType(url);
    	parseData.put("CrawlerNum", "1");
    	parseData.put("visitUrl", url);
    	switch(pageType){
    	case 0: break;
    	case 1: KLParseUtils.parseAlbum(page.getContentData(), parseData);break;
    	case 2: KLParseUtils.parseSond(page.getContentData(), parseData);break;
    	case 3: QTParseUtils.parseAlbum(page.getContentData(), parseData);break;
    	case 4: XMLYParseUtils.parseAlbum(page.getContentData(), parseData);break;
    	case 5: XMLYParseUtils.parseSond(page.getContentData(), parseData);break;
    	}
	}
	
}
