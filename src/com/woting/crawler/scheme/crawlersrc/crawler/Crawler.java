package com.woting.crawler.scheme.crawlersrc.crawler;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.spiritdata.framework.core.cache.SystemCache;
import com.spiritdata.framework.util.SpiritRandom;
import com.woting.crawler.CrawlerConstants;
import com.woting.crawler.scheme.crawlersrc.QT.crawler.QTParseUtils;
import com.woting.crawler.scheme.crawlersrc.XMLY.crawler.XMLYParseUtils;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.url.WebURL;

public class Crawler extends WebCrawler {

	private String crawlernum;
	
	@Override
	public void onStart() {
		this.crawlernum = SystemCache.getCache(CrawlerConstants.CRAWLERNUM).getContent()+"";
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
        if(href.startsWith("http://www.qingting.fm/#/home")) return true;
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
    	String url = page.getWebURL().getURL().trim();
    	reloadUrl(url, page.getContentData(), crawlernum);
	}
	
	public void reloadUrl(String url, byte[] htmlByteArray ,String crawlernum){
		Map<String, Object> parseData = new HashMap<String,Object>();
		int pageType = ParseUtils.getType(url);
    	parseData.put("CrawlerNum", crawlernum);
    	parseData.put("visitUrl", url);
    	switch(pageType){
    	case 0: break;
//    	case 1: KLParseUtils.parseAlbum(htmlByteArray, parseData);break;
//    	case 2: KLParseUtils.parseSond(htmlByteArray, parseData);break;
    	case 3: QTParseUtils.parseQTResourceIdAndCategoryId(htmlByteArray, parseData);break;
    	case 4: QTParseUtils.parseAlbum(true, htmlByteArray, parseData);break;
    	case 5: XMLYParseUtils.parseAlbum(true, htmlByteArray, parseData);break;
    	case 6: XMLYParseUtils.parseSond(true, htmlByteArray, parseData);break;
    	}
	}
}
