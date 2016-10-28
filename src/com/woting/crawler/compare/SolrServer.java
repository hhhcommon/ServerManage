package com.woting.crawler.compare;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import com.spiritdata.framework.ext.spring.redis.RedisOperService;
import com.spiritdata.framework.util.JsonUtils;
import com.woting.crawler.scheme.utils.CleanDataUtils;
import com.woting.crawler.scheme.utils.RedisUtils;

public class SolrServer {

	@SuppressWarnings("unchecked")
	public static List<String> getAnalysis(String sentence) {
		Map<String, Object> map = new HashMap<>();
		sentence = CleanDataUtils.utf8TOurl(sentence);
		String url = "http://123.56.254.75:1008/solr/collection1/analysis/field?wt=json&analysis.showmatch=true&analysis.fieldvalue=#fieldvalue#&analysis.fieldtype=text_ik";
		Document doc;
		List<String> lstr = new ArrayList<String>();
		try {
			url = url.replace("#fieldvalue#", sentence);
			doc = Jsoup.connect(url).ignoreContentType(true).get();
			String str = doc.body().html();
			if (str != null && !str.equals("null")) {
				map = (Map<String, Object>) JsonUtils.jsonToObj(str, Map.class);
				if (((Map<String, Object>) map.get("responseHeader")).get("status").equals(0)) {
					map = (Map<String, Object>) map.get("analysis");
					map = (Map<String, Object>) map.get("field_types");
					map = (Map<String, Object>) map.get("text_ik");
					List<Object> ls = (List<Object>) map.get("index");
					if(ls!=null&&ls.size()>0) {
						List<Map<String, Object>> lm = (List<Map<String, Object>>) ls.get(1);
						for (Map<String, Object> m : lm) {
							String text = (String) m.get("text");
							lstr.add(text);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lstr;
	}
	
	public static float getSameProportion(RedisOperService rs, String srcname, Object o) {
		List<String> n1 = getAnalysis(srcname);
		List<String> n2 = RedisUtils.getSrcParticiple(rs, o);
		int num = 0;
		for (String str1 : n1) {
			for (String str2 : n2) {
				if(str1.equals(str2)) num++;
			}
		}
		try {
			return num*2/(n1.size()+n2.size());
		} catch (Exception e) {
			return 0;
		}
	}
}
