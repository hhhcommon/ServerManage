package com.woting.crawler.core.solr;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.woting.cm.core.media.persis.po.MediaAssetPo;
import com.woting.cm.core.media.persis.po.MediaPlayCountPo;
import com.woting.cm.core.media.persis.po.SeqMediaAssetPo;
import com.woting.cm.core.media.service.MediaService;
import com.woting.crawler.core.solr.service.SolrJService;
import com.woting.crawler.ext.SpringShell;

public class SolrUpdateThread extends Thread {

	private SeqMediaAssetPo sma;
	private String person;
	private String chstr;

	public SolrUpdateThread(SeqMediaAssetPo sma, String person, String chstr) {
		this.sma = sma;
		this.person = person;
		this.chstr = chstr;
	}

	public void addSolr() {
		if (sma != null) {
			SolrJService solrJService = (SolrJService) SpringShell.getBean("solrJService");
			MediaService mediaService = (MediaService) SpringShell.getBean("mediaService");
			Map<String, Object> m = new HashMap<String, Object>();
			m.put("maPublisher", sma.getSmaPublisher());
			m.put("whereSql", " id in (select mId from wt_SeqMA_Ref where sId = '" + sma.getId() + "')");
			List<MediaAssetPo> mas = mediaService.getMaList(m);
			if ((mas != null) && (mas.size() > 0)) {
				sma.setSmaAllCount(mas.size());
				long playcount = 0L;
				m.clear();
				m.put("resId", sma.getId());
				m.put("resTableName", "wt_SeqMediaAsset");
				MediaPlayCountPo mp = mediaService.getMediaPlayCount(m);
				String persons = null;
				if (person != null) {
					persons = person;
				}
				if (mp != null)
					solrJService.addSolrIndex(sma, null, persons, chstr, mp.getPlayCount());
				else
					solrJService.addSolrIndex(sma, null, persons, chstr, playcount);
				for (MediaAssetPo ma : mas) {
					try {
						Thread.sleep(200);
						playcount = 0L;
						m.clear();
						m.put("resId", ma.getId());
						m.put("resTableName", "wt_MediaAsset");
						mp = mediaService.getMediaPlayCount(m);
						if (mp != null)
							solrJService.addSolrIndex(ma, sma.getId(), persons, chstr, mp.getPlayCount());
						else
							solrJService.addSolrIndex(ma, sma.getId(), persons, chstr, playcount);
					} catch (Exception localException1) {
						continue;
					}
				}
			}
		}
	}

	@Override
	public void run() {
		addSolr();
	}
}
