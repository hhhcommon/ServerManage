package com.woting.crawler.scheme.crawlerperson.XMLY.crawler;

import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.spiritdata.framework.util.JsonUtils;
import com.spiritdata.framework.util.SequenceUUID;
import com.woting.crawler.core.person.persis.po.PersonPo;
import com.woting.crawler.core.person.service.PersonService;
import com.woting.crawler.ext.SpringShell;

public class XMLYParseUtils {
	
	@SuppressWarnings("unchecked")
	public void parsePerson(String personId) {
		Document doc = null;
		try {
			doc = Jsoup.connect("http://www.ximalaya.com/mobile/v1/artist/intro?device=android&statEvent=pageview%2Fuser%40"+personId+"&statPage=tab%40%E5%8F%91%E7%8E%B0_%E4%B8%BB%E6%92%AD&statPosition=2&toUid="+personId).ignoreContentType(true).get();
			String jsonstr = doc.body().html();
			Map<String, Object> pm = (Map<String, Object>) JsonUtils.jsonToObj(jsonstr, Map.class);
			PersonPo po = new PersonPo();
			po.setId(SequenceUUID.getPureUUID());
			po.setpName(pm.get("nickname")+"");
			po.setpSource("喜马拉雅FM");
			po.setpSrcId(pm.get("uid")+"");
			if (pm.containsKey("personDescribe")) {
				if (!pm.get("personDescribe").equals("") || !pm.get("personDescribe").equals("null")) {
					po.setDescn(pm.get("personDescribe")+"");
				}
			}
			po.setIsVerified(pm.get("isVerified").equals("True")?1:2);
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
			if (pm.containsKey("albums")) {
				po.setAlbums(Integer.valueOf(pm.get("albums")+""));
			}
			po.setpSrcHomePage("http://www.ximalaya.com/zhubo/"+personId);
			po.setJsonstr(jsonstr);
//			PersonService personService = (PersonService) SpringShell.getBean("personService");
//			personService.insertPerson(po);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
