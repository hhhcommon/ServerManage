package com.woting.crawler.scheme.crawlerperson.XMLY;

import java.util.Map;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import com.spiritdata.framework.util.JsonUtils;
import com.spiritdata.framework.util.SequenceUUID;
import com.woting.crawler.core.cperson.persis.po.CPersonPo;
import com.woting.crawler.scheme.utils.CleanDataUtils;


public class XMLYPersonUtils {
	
	@SuppressWarnings("unchecked")
	public static CPersonPo parsePerson(String personId) {
		Document doc = null;
		try {
			doc = Jsoup.connect("http://www.ximalaya.com/mobile/v1/artist/intro?device=android&statEvent=pageview%2Fuser%40"+personId+"&statPage=tab%40%E5%8F%91%E7%8E%B0_%E4%B8%BB%E6%92%AD&statPosition=2&toUid="+personId).timeout(10000)
					.header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.102 Safari/537.36")
					.header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
					.header("Accept-Encoding", "gzip, deflate, sdch")
					.header("Cookie", "Hm_lvt_4a7d8ec50cfd6af753c4f8aee3425070=1481942163; Hm_lpvt_4a7d8ec50cfd6af753c4f8aee3425070=1481942193; _ga=GA1.2.2074075166.1481942163")
					.header("Host", "www.ximalaya.com")
					.header("Connection", "keep-alive")
					.header("X-Requested-With", "XMLHttpRequest")
					.header("Referer", "http://www.ximalaya.com/explore/")
					.ignoreContentType(true).get();
			String jsonstr = doc.body().html();
			jsonstr = CleanDataUtils.cleanString(jsonstr);
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
				location += "_"+pm.get("province");
			}
			if (pm.containsKey("city")) {
				location += "_"+pm.get("city");
			}
			if (!location.equals("")) {
				location = location.substring(1);
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
