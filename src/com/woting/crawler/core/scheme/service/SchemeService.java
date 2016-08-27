package com.woting.crawler.core.scheme.service;

import org.springframework.stereotype.Service;
import com.woting.crawler.core.etl.model.Etl1Process;
import com.woting.crawler.core.scheme.model.Scheme;

@Service
public class SchemeService {
	private Scheme scheme;

	public SchemeService() {
		scheme = new Scheme("");
	}

	public Scheme getScheme(){
		return scheme;
	}
	
	public Etl1Process gEtl1Process() {
		return scheme.getEtl1Process();
	}
}
