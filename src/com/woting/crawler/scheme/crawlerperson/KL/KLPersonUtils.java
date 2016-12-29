package com.woting.crawler.scheme.crawlerperson.KL;
import java.util.Map;

import com.spiritdata.framework.util.SequenceUUID;
import com.woting.crawler.core.cperson.persis.po.CPersonPo;
import com.woting.crawler.scheme.utils.HttpUtils;

public class KLPersonUtils {

	@SuppressWarnings("unchecked")
	public static CPersonPo parsePerson(String albumsId) {
		try {
			String url = "http://www.kan8kan.com/webapi/albumdetail/get?albumid="+albumsId;
			Map<String, Object> m = HttpUtils.getJsonMapFromURL(url);
			if (m!=null) {
				m = (Map<String, Object>) m.get("result");
				String upLoaderId = m.get("uploaderId")+"";
				if (upLoaderId.equals("0")) {
					return null;
				}
				url = "http://passport.kaolafm.com/v4/userv4/others/get?othersuid="+upLoaderId;
				m = HttpUtils.getJsonMapFromURL(url);
				if (m!=null) {
					m = (Map<String, Object>) m.get("result");
					CPersonPo po = new CPersonPo();
					po.setId(SequenceUUID.getPureUUID());
					po.setIsVerified(1);
					po.setpName(m.get("nickName")+"");
					po.setpSource("考拉");
					po.setpSrcId(m.get("uid")+"");
					po.setPortrait(m.get("avatar")+"");
					if (m.containsKey("address") && !m.get("address").equals("")) {
						po.setLocation(m.get("address")+"");
					}
					if (!m.get("intro").equals("")) {
						po.setDescn(m.get("intro")+"");
					}
					if (m.get("gender").equals(0)) {
						po.setSex(0);
					} else {
						if (m.get("gender").equals(1)) {
							po.setSex(1);
						} else {
							if (m.get("gender").equals(2)) {
								po.setSex(2);
							}
						}
					}
					if (!m.get("mobileNumber").equals("null")) {
						po.setPhoneNum(m.get("mobileNumber")+"");
					}
					if (!m.get("emailAccount").equals("null")) {
						po.setEmail(m.get("emailAccount")+"");
					}
					return po;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
