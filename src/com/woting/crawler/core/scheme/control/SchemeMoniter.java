package com.woting.crawler.core.scheme.control;

import com.woting.crawler.scheme.XMLY.crawler.Crawler;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

public class SchemeMoniter extends Thread {

	@Override
	public void run() {
		try {
			String crawlStorageFolder = "./tmp";
			int numberOfCrawlers = 10;
			CrawlConfig config = new CrawlConfig();
			// 文明请求web：确保我们不发送超过1每秒请求数（1000毫秒之间的请求）。
//			config.setPolitenessDelay(1000);	
			// 深度，即从入口URL开始算，URL是第几层。如入口A是1，从A中找到了B，B中又有C，则B是2，C是3 
			config.setMaxDepthOfCrawling(1);
			
	        //设置最大的抓取页面数。默认值为1，页面的数量不限
//			config.setMaxPagesToFetch(1);

			 // 如果需要代理服务器的话
			 //config.setProxyHost("proxyserver.example.com");  //设置代理域名
			 //config.setProxyPort(8080);//端口

			 // 如果代理服务器需要认证
			 //config.setProxyUsername(username); config.getProxyPassword(password);  //设置代理
			/*
			 * 此配置参数可以用来设置你的爬行是可恢复的（这意味着可以从先前中断/恢复爬行）
			 * 注意：如果启用恢复特征，想开始一个新的抓取，你需要删除的内容手动rootfolder。
			 */
			config.setResumableCrawling(false);
			
			config.setCrawlStorageFolder(crawlStorageFolder);
			PageFetcher pageFetcher = new PageFetcher(config);
			RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
			RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
			CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);
			controller.getCrawlersLocalData();
			controller.addSeed("http://www.ximalaya.com/explore/");	
			controller.start(Crawler.class, numberOfCrawlers);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
