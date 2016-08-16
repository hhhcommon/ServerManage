package com.woting.crawler.core.scheme.control;

import com.woting.crawler.core.scheme.model.Scheme;

public class SchemeController {
	private Scheme scheme;
	
	public SchemeController(Scheme scheme) {
		this.scheme = scheme;
	}
	
	public void runningScheme(){
		SchemeMoniter sm = new SchemeMoniter(scheme);
		sm.start();
	}
}
