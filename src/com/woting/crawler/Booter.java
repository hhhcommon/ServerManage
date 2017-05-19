package com.woting.crawler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.spiritdata.framework.core.cache.CacheEle;
import com.spiritdata.framework.core.cache.SystemCache;
import com.woting.crawler.core.timer.UpdateCrawlerSrcTimerJob;
import com.woting.crawler.ext.SpringShell;
import com.woting.crawler.scheme.crawlerdb.crawler.EtlProcess;
import com.woting.crawler.scheme.crawlerdb.qt.UpdateQTSrc;

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
        try {
//			scheduler.start();
//			logger.info("资源抓取定时功能已加载[{}]", timer.getUpdateCrawlerSrcExpression());
//			logger.info("首页资源抓取定时功能已加载[{}]", timer.getSrcCronExpression());
//			logger.info("点击量抓取定时功能已加载[{}]", timer.getPlayCountCronExpression());
//			logger.info("分类抓取定时功能已加载[{}]", timer.getCategoryCronExpression());
//			logger.info("电台播放地址检测[{}]", timer.getBCPlayIsValidateCronExpression());
//			logger.info("更新分享临时票据已加载[{}]", timer.getShareCronExpression());
//			logger.info("更新栏目状态[{}]", timer.getCacheRefreshExpression());
//			logger.info("同步Solr和Redis数据[{}]", timer.getRedisRefreshExpression());
//			new CrawlerSearch().start();
//			long beg = System.currentTimeMillis();
        	
//        	UpdateXMLYSrc updateXMLYSrc = new UpdateXMLYSrc();
//        	updateXMLYSrc.updateSrc();
        	
//        	UpdateQTSrc updateQTSrc = new UpdateQTSrc();
//        	updateQTSrc.updateSrc();
//        	Map<String, Object> map = updateQTSrc.getZJData("100498", "30");
//        	System.out.println(JsonUtils.objToJson(map));
        	
            EtlProcess etlProcess = new EtlProcess();
            etlProcess.convertToWT();
			while(true) {
				Thread.sleep(10*60*60*1000);
				UpdateCrawlerSrcTimerJob uJob = new UpdateCrawlerSrcTimerJob();
				boolean isok = uJob.beginCrawler();
				if (isok) {
					logger.info("各平台栏目下新增内容入中间库完成");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}