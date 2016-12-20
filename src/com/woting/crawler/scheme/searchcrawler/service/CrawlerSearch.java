package com.woting.crawler.scheme.searchcrawler.service;

import java.util.List;

import com.woting.cm.core.searchword.persis.po.SearchWordPo;
import com.woting.cm.core.searchword.service.SearchWordService;
import com.woting.crawler.ext.SpringShell;

public class CrawlerSearch extends Thread {
	
	private void checkWord() {
		SearchWordService searchWordService = (SearchWordService) SpringShell.getBean("searchWordService");
		try {
			List<SearchWordPo> sws = searchWordService.getSearchWordList();
		    if (sws!=null && sws.size()>0) {
			    for (SearchWordPo searchWordPo : sws) {
				    new XiMaLaYaSearch(searchWordPo.getWord()).start();
				    new QingTingSearch(searchWordPo.getWord()).start();
//				    new KaoLaSearch(searchWordPo.getWord()).start();
//				    new BaiDuNewsSearch(searchWordPo.getWord()).start();
				    System.out.println(searchWordPo.getWord());
				    searchWordService.deleteSearchWord(searchWordPo.getId());
			    }
		    }
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	@Override
	public void run() {
		while (true) {
			try {
				Thread.sleep(20);
				checkWord();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
