package com.woting.crawler.compare;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.request.FieldAnalysisRequest;
import org.apache.solr.client.solrj.response.FieldAnalysisResponse;
import org.apache.solr.client.solrj.response.AnalysisResponseBase.AnalysisPhase;
import org.apache.solr.client.solrj.response.AnalysisResponseBase.TokenInfo;

import com.woting.cm.core.media.persis.po.SeqMediaAssetPo;
import com.woting.crawler.core.album.persis.po.AlbumPo;
import com.woting.crawler.scheme.utils.RedisUtils;

public class SolrParticiple {

	private static HttpSolrServer solrServer;

	static {
		solrServer = new HttpSolrServer("http://localhost:8080/solr/collection1");
		solrServer.setConnectionTimeout(5000);
		solrServer.setMaxTotalConnections(10);
	}
	
	public static List<String> getAnalysis(String sentence) {
		FieldAnalysisRequest request = new FieldAnalysisRequest("/analysis/field");
		// request.addFieldName("text_ik");// 字段名，随便指定一个支持中文分词的字段
		request.addFieldType("text_ik");
		request.setFieldValue("");// 字段值，可以为空字符串，但是需要显式指定此参数
		request.setQuery(sentence);
		List<String> results = null;
		FieldAnalysisResponse response = null;
		try {
			response = request.process(solrServer);
			results = new ArrayList<String>();
			Iterator<AnalysisPhase> it = response.getFieldTypeAnalysis("text_ik")// .getFieldNameAnalysis("text_ik")
					.getQueryPhases().iterator();
			while (it.hasNext()) {
				AnalysisPhase pharse = (AnalysisPhase) it.next();
				List<TokenInfo> list = pharse.getTokens();
				for (TokenInfo info : list) {
					results.add(info.getText());
				}
			}
		} catch (Exception e) {e.printStackTrace();}
		return results;
	}
	
	public static float getSameProportion(String srcname, Object o) {
		List<String> n1 = getAnalysis(srcname);
		List<String> n2 = RedisUtils.getSrcParticiple(o);
		int num = 0;
		for (String str1 : n1) {
			for (String str2 : n2) {
				if(str1.equals(str2)) num++;
			}
		}
		return num*2/(n1.size()+n2.size());
	}
}
