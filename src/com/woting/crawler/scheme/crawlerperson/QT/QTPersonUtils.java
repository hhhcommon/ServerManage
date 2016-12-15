package com.woting.crawler.scheme.crawlerperson.QT;

import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.spiritdata.framework.util.JsonUtils;
import com.spiritdata.framework.util.SequenceUUID;
import com.woting.crawler.core.cperson.persis.po.CPersonPo;

public class QTPersonUtils {

	@SuppressWarnings("unchecked")
	public static CPersonPo parsePerson(String albumsId) {
		Document doc = null;
		try {
			doc = Jsoup.connect("http://api2.qingting.fm/v6/media/channelondemands/"+albumsId).timeout(10000).ignoreContentType(true).get();
			String aljson = doc.body().html();
			Map<String, Object> alm = (Map<String, Object>) JsonUtils.jsonToObj(aljson, Map.class);
			alm = (Map<String, Object>) alm.get("data");
			alm = (Map<String, Object>) alm.get("detail");
			List<Map<String, Object>> ps = (List<Map<String, Object>>) alm.get("podcasters");
			if (ps!=null && ps.size()>0) {
				alm = ps.get(0);
				CPersonPo po = new CPersonPo();
				po.setId(SequenceUUID.getPureUUID());
				po.setIsVerified(1);
				po.setpName(alm.get("nickname")+"");
				po.setpSource("蜻蜓");
				po.setpSrcId(alm.get("user_system_id")+"");
				po.setPortrait(alm.get("avatar")+"");
				if (!alm.get("location").equals("")) {
					po.setLocation(alm.get("location")+"");
				}
				if (!alm.get("birthday").equals("")) {
					po.setBirthday(alm.get("birthday").equals("")+"");
				}
				if (!alm.get("description").equals("")) {
					po.setDescn(alm.get("description")+"");
				}
				if (alm.get("sex").equals(0)) {
					po.setSex(0);
				} else {
					if (alm.get("sex").equals(1)) {
						po.setSex(1);
					} else {
						if (alm.get("sex").equals(2)) {
							po.setSex(2);
						}
					}
				}
				return po;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
