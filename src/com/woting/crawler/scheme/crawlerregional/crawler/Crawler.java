package com.woting.crawler.scheme.crawlerregional.crawler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.spiritdata.framework.util.ChineseCharactersUtils;
import com.spiritdata.framework.util.JsonUtils;
import com.woting.cm.core.dict.persis.po.DictDetailPo;
import com.woting.cm.core.dict.service.DictService;
import com.woting.crawler.core.dict.persis.po.DictDPo;
import com.woting.crawler.core.dict.service.CrawlerDictService;
import com.woting.crawler.ext.SpringShell;

public class Crawler {

	public void startCrawlerCategory() {
		DictService dictService = (DictService) SpringShell.getBean("dictService");
		List<DictDetailPo> dds = new ArrayList<>();
		try {
			InputStreamReader read = new InputStreamReader(new FileInputStream(new File("E:\\行政区划.txt")), "gbk");
			BufferedReader reader = new BufferedReader(read);
			String line;
			while ((line = reader.readLine()) != null) {
				String[] res = line.split(" ");
				System.out.println("##" + res[0] + "##" + res[res.length - 1].replace("　", ""));
				String id = res[0].trim();
				String ddName = res[res.length - 1].replace("　", "");
				System.out.println(isNo(id));
				String pId = isNo(id);
				DictDetailPo dd = dictService.getDictDetailInfo(id, "2", pId);
				if (dd != null) {
					dd.setDdName(ddName);
					dictService.updateDictD(dd);
				} else {
					dd = new DictDetailPo();
					dd.setId(id);
					dd.setMId("2");
					dd.setParentId(pId);
					dd.setSort(0);
					dd.setIsValidate(1);
					dd.setDdName(ddName);
					dd.setNPy(ChineseCharactersUtils.getFullSpellFirstUp(ddName));
					dd.setBCode(id);
					dd.setDType(2);
					dd.setDesc("测试录入行政区划");
					dd.setCTime(new Timestamp(System.currentTimeMillis()));
					dd.setLmTime(new Timestamp(System.currentTimeMillis()));
					dds.add(dd);
				}
			}
			read.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (dds != null && dds.size() > 0) {
			dictService.insertDictDList(dds);
		}
	}

	private String isNo(String num) {
		int ns = Integer.parseInt(num);
		if (ns % 100 == 0 && ns % 10000 == 0) {
			return "0";
		} else {
			if (ns % 100 == 0 && ns % 10000 != 0)
				return ns / 10000 + "0000";
			else {
				if (ns % 100 != 0 && ns % 10000 != 0)
					return ns / 100 + "00";
			}
		}
		return "数据异常";
	}
}
