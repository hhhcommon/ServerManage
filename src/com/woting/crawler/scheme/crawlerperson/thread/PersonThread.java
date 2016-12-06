package com.woting.crawler.scheme.crawlerperson.thread;

import com.woting.crawler.scheme.crawlerperson.XMLY.crawler.XMLYParseUtils;

public class PersonThread extends Thread {

	private String begionId;
	private String endId;
	
	public PersonThread(String begionId, String endId) {
		this.begionId = begionId;
		this.endId = endId;
	}
	
	@Override
	public void run() {
		long beg = Long.valueOf(begionId);
		long end = Long.valueOf(endId);
		XMLYParseUtils xUtils = new XMLYParseUtils();
		for (long i = beg; i <= end; i++) {
			System.out.println("起始Id="+begionId+" 结束Id="+endId+"  当前查询Id"+i);
			xUtils.parsePerson(i+"");
		}
	}
}
