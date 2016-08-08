package com.woting.crawler.core.scheme.control;

public class SchemeController {

	public void runningScheme(){
		SchemeMoniter sm = new SchemeMoniter();
		sm.start();
	}
}
