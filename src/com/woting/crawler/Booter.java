package com.woting.crawler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.quartz.Scheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.spiritdata.framework.core.cache.CacheEle;
import com.spiritdata.framework.core.cache.SystemCache;
import com.spiritdata.framework.util.JsonUtils;
import com.woting.cm.core.media.persis.po.MediaAssetPo;
import com.woting.cm.core.media.persis.po.MediaPlayCountPo;
import com.woting.cm.core.media.persis.po.SeqMediaAssetPo;
import com.woting.cm.core.media.service.MediaService;
import com.woting.crawler.compare.CompareAttribute;
import com.woting.crawler.core.solr.service.SolrJService;
import com.woting.crawler.core.timer.model.Timer;
import com.woting.crawler.ext.SpringShell;
import com.woting.crawler.scheme.crawlerdb.crawler.EtlProcess;
import com.woting.crawler.scheme.crawlerdb.xmly.XMLYEtl1Process;
import com.woting.crawler.scheme.crawlersrc.XMLY.XMLYCrawler;
import com.woting.crawler.scheme.searchcrawler.service.CrawlerSearch;
import com.woting.crawler.scheme.utils.FileUtils;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;

public class Booter {
	public static void main(String[] args) {
		long beginTime=System.currentTimeMillis();
        //获取运行路径
        String rootPath=Booter.class.getResource("").getPath();
        if (rootPath.indexOf("!")!=-1) {
            rootPath=rootPath.substring(0, rootPath.indexOf("!"));
            String[] _s=rootPath.split("/");
            if (_s.length>1) {
                rootPath="/";
                for (int i=0; i<_s.length-1; i++) {
                    if (_s[i].equals("file:")) continue;
                    if (_s[i].length()>0) rootPath+=_s[i]+"/";
                }
            }
        } else {
            rootPath=rootPath.substring(0, rootPath.length()-"com.woting.crawler".length()-1);
            String[] _s=rootPath.split("/");
            if (_s.length>1) {
                rootPath="/";
                for (int i=0; i<_s.length-1; i++) if (_s[i].length()>0) rootPath+=_s[i]+"/";
            }
        }
        String os = System.getProperty("os.name");
        if (os.toLowerCase().startsWith("linux")||os.toLowerCase().startsWith("unix")||os.toLowerCase().startsWith("aix")) rootPath+="/";
        else if (os.toLowerCase().startsWith("window")&&rootPath.startsWith("/")) rootPath=rootPath.substring(1);
        SystemCache.setCache(new CacheEle<String>(CrawlerConstants.APP_PATH, "系统运行的路径", rootPath));
        
        //logback加载xml内容
        LoggerContext lc = (LoggerContext)LoggerFactory.getILoggerFactory();
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(lc);
        lc.reset();
        try {
            String logConfFileName="logback-log";
            if (os.toLowerCase().startsWith("linux")||os.toLowerCase().startsWith("unix")||os.toLowerCase().startsWith("aix")) logConfFileName="conf/"+logConfFileName+"-linux.xml";
            else if (os.toLowerCase().startsWith("window")) logConfFileName=rootPath+"conf/"+logConfFileName+"-window.xml";
            configurator.doConfigure(logConfFileName);
        } catch (JoranException e) {
            e.printStackTrace();
        }
        Logger logger = LoggerFactory.getLogger(Booter.class);
        logger.info("内容抓取，环境初始化开始");
        logger.info("系统运行路径 [{}]", (SystemCache.getCache(CrawlerConstants.APP_PATH)).getContent());
        logger.info("计算并加载运行目录，用时[{}]毫秒", System.currentTimeMillis()-beginTime);
        
        //Spring环境加载
        long _begin=System.currentTimeMillis();
        SpringShell.init();
        logger.info("加载Spring配置，用时[{}]毫秒", System.currentTimeMillis()-_begin);
        
        //定时器加载
//        Timer timer = new Timer(SystemCache.getCache(CrawlerConstants.APP_PATH).getContent()+"conf/timer.txt");
//        Scheduler scheduler = timer.getScheduler();
//        if(scheduler==null) {
//        	logger.info("定时加载出错，结束抓取服务");
//        	return;
//        }
//        try {
//			scheduler.start();
//			logger.info("首页资源抓取定时功能已加载[{}]", timer.getSrcCronExpression());
//			logger.info("点击量抓取定时功能已加载[{}]", timer.getPlayCountCronExpression());
//			logger.info("分类抓取定时功能已加载[{}]", timer.getCategoryCronExpression());
//			logger.info("电台播放地址检测[{}]", timer.getBCPlayIsValidateCronExpression());
//			logger.info("更新分享临时票据已加载[{}]", timer.getShareCronExpression());
//			new CrawlerSearch().start();
//			while(true) {
//				Thread.sleep(60*60*1000);
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
        
        long beg = System.currentTimeMillis();
        EtlProcess etlProcess = new EtlProcess();
        etlProcess.makeDatas();
        System.out.println(System.currentTimeMillis()-beg);
        
//        SolrJService solrJService = (SolrJService) SpringShell.getBean("solrJService");
//        CompareAttribute cAttribute = new CompareAttribute("1");
//        float f = cAttribute.compareTitle(solrJService, "20120204你要出轨_郭德纲,于谦,三里屯专场", "郭德纲 于谦 - 论50年之现状");
//        System.out.println(f);
	}
}