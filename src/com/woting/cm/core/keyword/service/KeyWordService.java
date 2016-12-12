package com.woting.cm.core.keyword.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import com.spiritdata.framework.core.dao.mybatis.MybatisDAO;
import com.spiritdata.framework.util.ChineseCharactersUtils;
import com.spiritdata.framework.util.SequenceUUID;
import com.spiritdata.framework.util.StringUtils;
import com.woting.cm.core.keyword.persis.po.KeyWordPo;
import com.woting.cm.core.keyword.persis.po.KwResPo;


public class KeyWordService {
	
	@Resource(name = "defaultDAO")
	private MybatisDAO<KeyWordPo> keyWordDao;
	@Resource(name = "defaultDAO")
	private MybatisDAO<KwResPo> kwReSDao;
	
	@PostConstruct
	public void initParam() {
		keyWordDao.setNamespace("A_KEYWORD");
		kwReSDao.setNamespace("A_KWRES");
	}
	
	public boolean KeyWordIsNull(String kwName) {
		if (kwName!=null) {
			List<KeyWordPo> kws = keyWordDao.queryForList("getKeyWord", kwName);
			if (kws!=null && kws.size()>0) return false;
			else return true;
		}
		return false;
	}
	
	public void insertKeyWords(KeyWordPo kw) {
		List<KeyWordPo> kws = new ArrayList<>();
		kws.add(kw);
		insertKeyWords(kws);
	}
	
	public void insertKeyWords(List<KeyWordPo> kws) {
		if (kws!=null && kws.size()>0) {
			Map<String, Object> m = new HashMap<>();
			m.put("list", kws);
			keyWordDao.insert("insertKeyWordList", m);
		}
	}
	
	public void insertKwRefs(KwResPo kr) {
		List<KwResPo> krs = new ArrayList<>();
		krs.add(kr);
		insertKwRefs(krs);
	}
	
	public void insertKwRefs(List<KwResPo> krs) {
		if (krs!=null && krs.size()>0) {
			Map<String, Object> m = new HashMap<>();
			m.put("list", krs);
			kwReSDao.insert("insertKwResList", m);
		}
	}
	
	public void saveKwAndKeRef(String kws, String resTableName, String resId) {
		if (!StringUtils.isNullOrEmptyOrSpace(kws)) {
			String[] kw = kws.split(",");
			if (kw.length>0) {
				for (String str : kw) {
					Map<String, Object> m = new HashMap<>();
					m.put("kwName", str);
					m.put("isValidate", 1);
					List<KeyWordPo> kwpos = keyWordDao.queryForList("getList", m);
					if (kwpos!=null && kwpos.size()>0) {
						KeyWordPo kwpo = kwpos.get(0);
						KwResPo kwr = new KwResPo();
						kwr.setId(SequenceUUID.getPureUUID());
						kwr.setKwId(kwpo.getId());
						if (resTableName.equals("wt_SeqMediaAsset")) {
							kwr.setRefName("标签-专辑");
						} else {
							if (resTableName.equals("wt_MediaAsset")) {
								kwr.setRefName("标签-节目");
							} else if (resTableName.equals("wt_Channel")) {
								kwr.setRefName("标签-栏目");
							}
						}
						kwr.setResTableName(resTableName);
						kwr.setResId(resId);
						kwr.setcTime(new Timestamp(System.currentTimeMillis()));
						insertKwRefs(kwr);
					} else {
						KeyWordPo kwpo = new KeyWordPo();
						kwpo.setId(SequenceUUID.getPureUUID());
						kwpo.setIsValidate(1);
						kwpo.setKwName(str);
						kwpo.setnPy(ChineseCharactersUtils.getFullSpellFirstUp(str));
						kwpo.setSort(0);
						kwpo.setOwnerId("cm");
						kwpo.setOwnerType(0);
						kwpo.setcTime(new Timestamp(System.currentTimeMillis()));
						insertKeyWords(kwpo);
						
						KwResPo kwr = new KwResPo();
						kwr.setId(SequenceUUID.getPureUUID());
						kwr.setKwId(kwpo.getId());
						if (resTableName.equals("wt_SeqMediaAsset")) {
							kwr.setRefName("标签-专辑");
						} else {
							if (resTableName.equals("wt_MediaAsset")) {
								kwr.setRefName("标签-节目");
							}
						}
						kwr.setResTableName(resTableName);
						kwr.setResId(resId);
						kwr.setcTime(new Timestamp(System.currentTimeMillis()));
						insertKwRefs(kwr);
					}
				}
			}
		}
	}
}
