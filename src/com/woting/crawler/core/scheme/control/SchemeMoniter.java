package com.woting.crawler.core.scheme.control;

import com.woting.crawler.core.scheme.model.Scheme;
import com.woting.crawler.scheme.KL.crawler.KLCrawler;
import com.woting.crawler.scheme.crawler.Crawler;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

public class SchemeMoniter extends Thread {

	private Scheme scheme;
	
	public SchemeMoniter(Scheme scheme) {
		this.scheme = scheme;
	}
	
	@Override
	public void run() {
		startCrawler4j(); //开启Crawler4j抓取 
		startCustomCrawler(); //开启辅助信息抓取
	}
	
	private void startCustomCrawler() {
		new KLCrawler(scheme.getSchemenum()).start(); //开启考拉分类信息加载线程
	}

	private void startCrawler4j(){
		try {
			String crawlStorageFolder = "./tmp";
			int numberOfCrawlers = 50;
			CrawlConfig config = new CrawlConfig();
			// 文明请求web：确保我们不发送超过1每秒请求数（1000毫秒之间的请求）。
//			config.setPolitenessDelay(1000);	
			// 深度，即从入口URL开始算，URL是第几层。如入口A是1，从A中找到了B，B中又有C，则B是2，C是3 
			config.setMaxDepthOfCrawling(1);
//			config.setUserAgentString("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.101 Safari/537.36");
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
			controller.addSeed("http://www.qingting.fm/s/home");
			controller.addSeed("http://www.kaolafm.com");
			controller.start(Crawler.class, numberOfCrawlers);
//			controller.waitUntilFinish();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
