package com.woting.crawler.ext;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.spiritdata.framework.core.cache.CacheEle;
import com.spiritdata.framework.core.cache.SystemCache;
import com.woting.crawler.CrawlerConstants;

/**
 * 为Spring所做的壳程序
 * @author wanghui
 */
public class SpringShell {
    public static void init() {
        //得到系统路径
        String contextConfFilePath=(String)SystemCache.getCache(CrawlerConstants.APP_PATH).getContent();
        String os = System.getProperty("os.name");
        if (os.toLowerCase().startsWith("linux")||os.toLowerCase().startsWith("unix")||os.toLowerCase().startsWith("aix")) contextConfFilePath="conf/appContext.xml";
        else if (os.toLowerCase().startsWith("window")) contextConfFilePath+="conf/appContext.xml";

        SystemCache.setCache(new CacheEle<ApplicationContext>(CrawlerConstants.CONTEXT_SPRINGCTX, "Spring ICO上下文",
                new FileSystemXmlApplicationContext(new String[] {contextConfFilePath})));
    }

    public static Object getBean(String beanName) {
        return ((ApplicationContext)(SystemCache.getCache(CrawlerConstants.CONTEXT_SPRINGCTX).getContent())).getBean(beanName);
    }
}