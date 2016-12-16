package com.woting.crawler.core.scheme.control;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.spiritdata.framework.core.cache.SystemCache;
import com.spiritdata.framework.ext.spring.redis.RedisOperService;
import com.woting.crawler.CrawlerConstants;
import com.woting.crawler.core.scheme.model.Scheme;
import com.woting.crawler.scheme.crawlersrc.XMLY.crawler.XMLYCrawlerRedis;
import com.woting.crawler.scheme.crawlersrc.crawler.Crawler;
import com.woting.crawler.scheme.utils.RedisUtils;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

public class SchemeMoniter extends Thread {
	Logger logger = LoggerFactory.getLogger(this.getClass());
	private String crawlernum;
	private Scheme scheme;

	public SchemeMoniter(Scheme scheme) {
		this.crawlernum = SystemCache.getCache(CrawlerConstants.CRAWLERNUM).getContent()+"";
		this.scheme = scheme;
	}
	
	@Override
	public void run() {
		logger.info("开始进行数据抓取");
		logger.info("开始辅助信息抓取");
		new XMLYCrawlerRedis(scheme).start();
		logger.info("开启Crawler4j抓取");
		new Thread(){public void run() {startCrawler4j(); }}.start(); //开启Crawler4j抓取
	}
	
	public void startCrawler4j(){
		try {
			String crawlStorageFolder = "./tmp";
			int numberOfCrawlers = scheme.getNumberOfCrawlers();
			CrawlConfig config = new CrawlConfig();
			// 文明请求web：确保我们不发送超过1每秒请求数（1000毫秒之间的请求）。
//			config.setPolitenessDelay(1000);
			// 深度，即从入口URL开始算，URL是第几层。如入口A是1，从A中找到了B，B中又有C，则B是2，C是3 
			config.setMaxDepthOfCrawling(Integer.valueOf((scheme.getCrawlerExtent()==null?"1":scheme.getCrawlerExtent())));
//			config.setUserAgentString("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.101 Safari/537.36");
			//设置最大的抓取页面数。默认值为1，页面的数量不限
//			config.setMaxPagesToFetch(1);
			// 如果需要代理服务器的话
			//config.setProxyHost("proxyserver.example.com");  //设置代理域名
			//config.setProxyPort(8080);//端口
			// 如果代理服务器需要认证
			//config.setProxyUsername(username); 
			// config.getProxyPassword(password);  //设置代理
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
			controller.waitUntilFinish();
			//写入抓取完成信息
			Scheme scheme = (Scheme) SystemCache.getCache(CrawlerConstants.SCHEME).getContent();
			RedisOperService rs = new RedisOperService(scheme.getJedisConnectionFactory(), scheme.getRedisDB());
			RedisUtils.writeCrawlerFinishInfo(rs, crawlernum);
			rs.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
