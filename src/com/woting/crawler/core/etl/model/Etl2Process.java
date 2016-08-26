package com.woting.crawler.core.etl.model;

import java.sql.Timestamp;
import java.util.Map;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import com.spiritdata.framework.util.JsonUtils;
import com.woting.crawler.scheme.utils.HttpUtils;

public class Etl2Process {

	private String etlnum;
	private Timestamp cTimestamp;
	private Map<String, Object> categorys;
	private Map<String, Object> channels;
	
	@SuppressWarnings("unchecked")
	public Etl2Process() {
		Document doc ;
		try {
			doc = Jsoup.connect("http://123.56.254.75:908/CM/common/getCataTreeWithSelf.do").ignoreContentType(true).data("cataId", "3").post();
			String catejson = doc.select("body").get(0).html();
			catejson = HttpUtils.getTextByDispose(catejson);
			setCategorys((Map<String, Object>) JsonUtils.jsonToObj(catejson, Map.class));
			doc = Jsoup.connect("http://123.56.254.75:908/CM/content/getConditions.do").timeout(10000).ignoreContentType(true).get();
			String chjson = doc.select("body").get(0).html();
			chjson = HttpUtils.getTextByDispose(chjson);
			setChannels((Map<String, Object>) JsonUtils.jsonToObj(chjson, Map.class));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public String getEtlnum() {
		return etlnum;
	}
	public void setEtlnum(String etlnum) {
		this.etlnum = etlnum;
	}
	public Timestamp getcTimestamp() {
		return cTimestamp;
	}
	public void setcTimestamp(Timestamp cTimestamp) {
		this.cTimestamp = cTimestamp;
	}
	public Map<String, Object> getCategorys() {
		return categorys;
	}
	public Map<String, Object> getChannels() {
		return channels;
	}
	public void setCategorys(Map<String, Object> categorys) {
		this.categorys = categorys;
	}

	public void setChannels(Map<String, Object> channels) {
		this.channels = channels;
	}
}
