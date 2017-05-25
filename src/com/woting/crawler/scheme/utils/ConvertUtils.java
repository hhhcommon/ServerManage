package com.woting.crawler.scheme.utils;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.spiritdata.framework.util.DateUtils;
import com.spiritdata.framework.util.SequenceUUID;
import com.spiritdata.framework.util.StringUtils;
import com.woting.cm.core.person.persis.po.PersonPo;
import com.woting.crawler.core.cperson.persis.po.CPersonPo;
import com.woting.crawler.core.dict.persis.po.DictDPo;

public abstract class ConvertUtils {
	
	public static List<DictDPo> convert2DictD(List<Map<String, Object>> list, List<DictDPo> ddlist, String publisher, String dmid ,int isValidate, int crawlerNum) {
		List<DictDPo> dictdlist = new ArrayList<DictDPo>();
		if (list != null && list.size() > 0) {
			for (Map<String, Object> m : list) {
				DictDPo dd = new DictDPo();
				dd.setId(SequenceUUID.getPureUUID());
				dd.setSourceId(m.get("id") + "");
				dd.setDdName(m.get("name") + "");
				dd.setmId(dmid);
				if (!m.containsKey("pid") || ddlist == null) {
					dd.setpId("0");
				} else {
					for (DictDPo dictDPo : ddlist) {
						if (dictDPo.getSourceId().equals(m.get("pid") + ""))
							dd.setpId(dictDPo.getId());
					}
				}
				dd.setPublisher(publisher);
				if (m.containsKey("nPy"))
					dd.setnPy(m.get("nPy") + "");
				dd.setVisitUrl(m.get("visitUrl") + "");
				dd.setIsValidate(isValidate);
				dd.setCrawlerNum(crawlerNum);
				dd.setcTime(new Timestamp(System.currentTimeMillis()));
				dictdlist.add(dd);
			}
		}
		return dictdlist;
	}
	
	public static PersonPo convert2Person(CPersonPo cpo){
		PersonPo po = new PersonPo();
		po.setId(SequenceUUID.getPureUUID());
		po.setpName(cpo.getpName());
		po.setpSource(cpo.getpSource());
		if (po.getpSource().equals("喜马拉雅")) {
			po.setpSrcId("2");
		} else {
			if (po.getpSource().equals("蜻蜓")) {
				po.setpSrcId("3");
			} else {
				if (po.getpSource().equals("考拉")) {
					po.setpSrcId("4");
				} else {
					if (po.getpSource().equals("多听")) {
						po.setpSrcId("5");
					}
				}
			}
		}
		String imgp = cpo.getPortrait();
//		po.setPortrait(imgp);
		if (!StringUtils.isNullOrEmptyOrSpace(imgp) && imgp.length()>5) {
			String imgpath = null;
			try {
				imgpath = FileUtils.makeImgFile("1", imgp);
			} catch (Exception e) {}
			if (!StringUtils.isNullOrEmptyOrSpace(imgpath)) {
				po.setPortrait(imgpath);
			}
		}
		if (cpo.getAge()!=null) {
			po.setAge(cpo.getAge());
		}
		if (cpo.getBirthday()!=null) {
			po.setBirthday(cpo.getBirthday());
		}
		if (cpo.getConstellation()!=null) {
			po.setConstellation(cpo.getConstellation());
		}
		if(cpo.getEmail()!=null) {
			po.setEmail(cpo.getEmail());
		}
		if (cpo.getDescn()!=null) {
			po.setDescn(cpo.getDescn());
		}
		if (cpo.getPhoneNum()!=null) {
			po.setPhoneNum(cpo.getPhoneNum());
		}
		if (cpo.getpSrcHomePage()!=null) {
			po.setpSrcHomePage(cpo.getpSrcHomePage());
		}
		po.setIsVerified(cpo.getIsVerified());
		po.setcTime(new Timestamp(System.currentTimeMillis()));
		po.setLmTime(new Timestamp(System.currentTimeMillis()));
		return po;
	}

	public static String convertPlayNum2Long(String playnum) {
		int lastnum = -1;
		int begnum = -1;
		if (!playnum.contains("."))
			return playnum;
		begnum = playnum.indexOf(".");
		if (playnum.contains("万")) {
			lastnum = playnum.indexOf("万");
			if (lastnum - begnum == 2) {
				playnum = playnum.substring(0, lastnum);
				playnum = playnum.replace(".", "") + "000";
			}
			if (lastnum - begnum == 1) {
				playnum = playnum.substring(0, lastnum);
				playnum = playnum.replace(".", "")+"0000";
			}
		}
		if (playnum.contains("亿")) {
			lastnum = playnum.indexOf("亿");
			if (lastnum - begnum == 2) {
				playnum = playnum.substring(0, lastnum);
				playnum = playnum.replace(".", "") + "0000000";
			}
			if (lastnum - begnum == 1) {
				playnum = playnum.substring(0, lastnum);
				playnum = playnum.replace(".", "")+"00000000";
			}
		}
		return playnum;
	}
	
	public static long makeLongTime(String time) {
		try {
			Date date = DateUtils.getDateTime("yyyy-MM-dd HH:mm:ss", time);
			return date.getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return 0;
	}
	
}
