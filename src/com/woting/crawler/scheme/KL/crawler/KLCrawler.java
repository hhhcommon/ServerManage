package com.woting.crawler.scheme.KL.crawler;

import java.util.HashMap;
import java.util.Map;

import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.spiritdata.framework.util.StringUtils;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.url.WebURL;

public class KLCrawler extends WebCrawler {

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
        if (href.startsWith("http://www.kaolafm.com/zj/") || href.startsWith("http://www.kaolafm.com/jm/")) return true;
        return false;
    }
    
    @Override
    public void visit(Page page) {
    	try {
			Thread.sleep(100);
		} catch (Exception e) {}
    	Map<String, Object> parseData = new HashMap<String,Object>();
    	String url = page.getWebURL().getURL().trim();
    	Document doc;
    	try {
    		int pageType = KLParseUtils.getType(url);
    		switch(pageType){
    		case 1: KLParseUtils.parseAlbum(page.getContentData(), parseData);break;
    		case 2: KLParseUtils.parseSond(page.getContentData(), parseData);break;
    		}
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
}
