package com.woting.crawler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.spiritdata.framework.core.cache.CacheEle;
import com.spiritdata.framework.core.cache.SystemCache;
import com.woting.crawler.compare.CrawlerSrcRecord;
import com.woting.crawler.core.etl.control.Etl1Controller;
import com.woting.crawler.core.etl.control.Etl2Controller;
import com.woting.crawler.core.etl.model.Etl1Process;
import com.woting.crawler.core.etl.model.Etl2Process;
import com.woting.crawler.core.scheme.control.SchemeController;
import com.woting.crawler.core.scheme.model.Scheme;
import com.woting.crawler.ext.SpringShell;
import com.woting.crawler.scheme.utils.RedisUtils;

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
        
        //加载抓取方案
        Scheme scheme = new Scheme("");
        String crawlernum = scheme.getSchemenum();
		while (RedisUtils.isOrNoCrawlerFinish(crawlernum)) {
			logger.info("开始判断redis里是否存在当前抓取序号[{}]是否已存在", crawlernum);
			logger.info("抓取序号[{}]已存在", crawlernum);
			int num = Integer.valueOf(crawlernum) + 1;
			crawlernum = num + "";
			logger.info("验证抓取序号[{}]是否存在", crawlernum);
		}
		logger.info("抓取序号[{}]不存在", crawlernum);
		logger.info("开始进行序号为[{}]抓取", crawlernum);
		scheme.setSchemenum(crawlernum);
		SystemCache.setCache(new CacheEle<String>(CrawlerConstants.CRAWLERNUM, "抓取序号", crawlernum));
		
		CrawlerSrcRecord srcRecord = new CrawlerSrcRecord(crawlernum);
		srcRecord.reloadCrawlerInfo();
		
        //开始抓取数据
        SchemeController sc = new SchemeController(scheme);
        sc.runningScheme();
		
        //第一次数据转换
        Etl1Process etl1Process = new Etl1Process();
        etl1Process.setEtlnum(scheme.getSchemenum());
        Etl1Controller etl1 = new Etl1Controller(etl1Process);
        etl1.runningScheme();
//		scheme.setSchemenum("1");
        
        //第二次数据转换
        Etl2Process etl2Process = new Etl2Process();
        etl2Process.setEtlnum(scheme.getSchemenum());
        Etl2Controller etl2 = new Etl2Controller(etl2Process);
        etl2.runningScheme();
        
	}
}
