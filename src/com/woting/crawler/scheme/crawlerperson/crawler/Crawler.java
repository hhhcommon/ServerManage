package com.woting.crawler.scheme.crawlerperson.crawler;

import com.woting.crawler.scheme.crawlerperson.thread.PersonThread;

public class Crawler {

	public void beginCrawlerPerson() {
		new PersonThread("1", "10000000").start();
		new PersonThread("10000001", "20000000").start();
		new PersonThread("20000001", "30000000").start();
		new PersonThread("30000001", "40000000").start();
		new PersonThread("40000001", "50000000").start();
		new PersonThread("50000001", "60000000").start();
		new PersonThread("60000001", "70000000").start();
		new PersonThread("70000001", "80000000").start();
	}
}
