package com.woting.crawler.core.solr;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.woting.cm.core.channel.persis.po.ChannelAssetPo;
import com.woting.cm.core.channel.persis.po.ChannelPo;
import com.woting.cm.core.channel.service.ChannelService;
import com.woting.cm.core.media.persis.po.MediaAssetPo;
import com.woting.cm.core.media.persis.po.MediaPlayCountPo;
import com.woting.cm.core.media.persis.po.SeqMediaAssetPo;
import com.woting.cm.core.media.service.MediaService;
import com.woting.cm.core.person.persis.po.PersonPo;
import com.woting.cm.core.person.service.PersonService;
import com.woting.crawler.core.solr.service.SolrJService;
import com.woting.crawler.ext.SpringShell;

public class SolrUpdateThread extends Thread {

	private SeqMediaAssetPo sma;
	private String person;
	private String chstr;

	public SolrUpdateThread(SeqMediaAssetPo sma) {
		this.sma = sma;
	}

	public void addSolr() {
		if (sma != null) {
			SolrJService solrJService = (SolrJService) SpringShell.getBean("solrJService");
			MediaService mediaService = (MediaService) SpringShell.getBean("mediaService");
			PersonService personService = (PersonService) SpringShell.getBean("personService");
			ChannelService channelService = (ChannelService) SpringShell.getBean("channelService");
			List<ChannelPo> chs = channelService.getChannelList();
			Map<String, Object> chMap = new HashMap<>();
			if (chs!=null && chs.size()>0) {
				for (ChannelPo channelPo : chs) {
					chMap.put(channelPo.getId(), channelPo.getChannelName());
				}
			}
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
				List<PersonPo> personPos = personService.getPersonsByResIdAndResTableName(sma.getId(), "wt_SeqMediaAsset");
				String persons = null;
				if (personPos!=null && personPos.size()>0) {
					for (PersonPo personPo : personPos) {
						if (persons==null) persons = "";
						persons += ","+personPo.getpName();
					}
				}
				String chaStr = null;
				List<ChannelAssetPo> chas = channelService.getChannelAssetListBy(null, sma.getId(), "wt_SeqMediaAsset");
				if (chas!=null && chas.size()>0) {
					for (ChannelAssetPo channelAssetPo : chas) {
						if (chaStr==null) chaStr = "";
						try {
							chaStr += ","+chMap.get(channelAssetPo.getChannelId()).toString();
						} catch (Exception e) {
							if (chaStr==null || chaStr.length()==0) chaStr = null;
						}
					}
				}
				if (persons != null) persons = persons.substring(1);
				if (chaStr!=null) chaStr = chaStr.substring(1);
				if (mp != null)
					solrJService.addSolrIndex(sma, null, persons, chaStr, mp.getPlayCount());
				else
					solrJService.addSolrIndex(sma, null, persons, chaStr, playcount);
				for (MediaAssetPo ma : mas) {
					try {
						Thread.sleep(200);
						playcount = 0L;
						m.clear();
						m.put("resId", ma.getId());
						m.put("resTableName", "wt_MediaAsset");
						mp = mediaService.getMediaPlayCount(m);
						persons = null;
						personPos = personService.getPersonsByResIdAndResTableName(ma.getId(), "wt_MediaAsset");
						if (personPos!=null && personPos.size()>0) {
							for (PersonPo personPo : personPos) {
								if (persons==null) persons = "";
								persons += ","+personPo.getpName();
							}
						}
						if (persons != null) persons = persons.substring(1);
						chaStr = null;
						chas = channelService.getChannelAssetListBy(null, sma.getId(), "wt_SeqMediaAsset");
						if (chas!=null && chas.size()>0) {
							for (ChannelAssetPo channelAssetPo : chas) {
								if (chaStr==null) chaStr = "";
								try {
									chaStr += ","+chMap.get(channelAssetPo.getChannelId()).toString();
								} catch (Exception e) {
									if (chaStr==null || chaStr.length()==0) chaStr = null;
								}
							}
						}
						if (chaStr!=null) chaStr = chaStr.substring(1);
						if (mp != null)
							solrJService.addSolrIndex(ma, sma.getId(), persons, chaStr, mp.getPlayCount());
						else
							solrJService.addSolrIndex(ma, sma.getId(), persons, chaStr, playcount);
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
