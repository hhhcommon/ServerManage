package com.woting.crawler.scheme.QT.crawler;

import java.util.HashMap;
import java.util.Map;

import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.spiritdata.framework.util.StringUtils;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.url.WebURL;

public class QTCrawler extends WebCrawler {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private String tempStorePath="";
    private boolean needStoreWeb=false;

    /**
     * 启动时执行
     */
    @Override
    public void onStart() {
//        tempStorePath=(String)((Map<String, Object>)(this.getMyController().getCustomData())).get("storeUrl");
        needStoreWeb=!StringUtils.isNullOrEmptyOrSpace(tempStorePath);
    }
    
    /**
     * 网页是否需要被访问
     */
    @Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
        String href = url.getURL().toLowerCase();
        href=href.trim().toLowerCase();
        //url判断
        if (href.startsWith("http://www.qingting.fm/vchannels")){
        	url.setURL(href.replace("http://www.qingting.fm", "http://www.qingting.fm/s"));
        	return true;
        } 
        return false;
    }
    
    @Override
    public void visit(Page page) {
    	try {
			Thread.sleep(1000);
		} catch (Exception e) {}
    	String url = page.getWebURL().getURL().trim();
    	Document doc;
    	Map<String, Object> parseData = new HashMap<String,Object>();
    	try {
    		QTParseUtils.parseAlbum(page.getContentData(), parseData);
//    		url = url.replace("http://www.qingting.fm", "http://www.qingting.fm/s");
//    		doc = Jsoup.connect(url).timeout(10000).ignoreContentType(false).get();
//    		String seqid = doc.select("a[data-switch-url]").get(0).attr("href");
//			seqid = seqid.replace("/vchannels/", "");
//			String seqname = doc.select("a").get(0).html();
//			String seqimg = doc.select("img ").get(0).attr("src");
//			String seqdescn = doc.select("div[class=abstract clearfix]").get(0).select("div[class=content]").get(0).html();
//			doc = Jsoup.connect(url+"/ajax").timeout(10000).ignoreContentType(false).get();
//			String jsonstr = doc.select("body").get(0).html();
//			logger.info("专辑名称：[{}],专辑id：[{}],专辑封面：[{}],专辑简介：[{}]", seqname, seqid, seqimg, seqdescn);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
}
