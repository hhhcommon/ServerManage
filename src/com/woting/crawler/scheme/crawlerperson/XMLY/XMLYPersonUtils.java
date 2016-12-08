package com.woting.crawler.scheme.crawlerperson.XMLY;

import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.spiritdata.framework.util.JsonUtils;
import com.spiritdata.framework.util.SequenceUUID;
import com.woting.crawler.core.cperson.persis.po.CPersonPo;


public class XMLYPersonUtils {
	
	@SuppressWarnings("unchecked")
	public static CPersonPo parsePerson(String personId) {
		Document doc = null;
		try {
			doc = Jsoup.connect("http://www.ximalaya.com/mobile/v1/artist/intro?device=android&statEvent=pageview%2Fuser%40"+personId+"&statPage=tab%40%E5%8F%91%E7%8E%B0_%E4%B8%BB%E6%92%AD&statPosition=2&toUid="+personId).ignoreContentType(true).get();
			String jsonstr = doc.body().html();
			Map<String, Object> pm = (Map<String, Object>) JsonUtils.jsonToObj(jsonstr, Map.class);
			CPersonPo po = new CPersonPo();
			po.setId(SequenceUUID.getPureUUID());
			po.setpName(pm.get("nickname")+"");
			po.setpSource("喜马拉雅");
			po.setpSrcId(pm.get("uid")+"");
			if (pm.containsKey("personDescribe")) {
				if (!pm.get("personDescribe").equals("") || !pm.get("personDescribe").equals("null")) {
					po.setDescn(pm.get("personDescribe")+"");
				}
			}
			po.setIsVerified(pm.get("isVerified").equals(true)?1:2);
			String location = "";
			if (pm.containsKey("province")) {
				location += pm.get("province");
			}
			if (pm.containsKey("city")) {
				location += pm.get("city");
			}
			if (!location.equals("")) {
				po.setLocation(location);
			}
			po.setPortrait(pm.get("mobileLargeLogo")+"");
			if (pm.containsKey("constellation")) {
				po.setConstellation(pm.get("constellation")+"");
			}
			if (pm.containsKey("gender")) {
				if (pm.get("gender").equals("1")) {
					po.setSex(1);
				} else {
					if (pm.get("gender").equals("2")) {
						po.setSex(2);
					} else {
						po.setSex(0);
					}
				}
			} else {
				po.setSex(0);
			}
			po.setpSrcHomePage("http://www.ximalaya.com/zhubo/"+personId);
			return po;
//			PersonService personService = (PersonService) SpringShell.getBean("personService");
//			personService.insertPerson(po);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
