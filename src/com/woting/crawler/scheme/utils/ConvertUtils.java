package com.woting.crawler.scheme.utils;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.spiritdata.framework.util.SequenceUUID;
import com.woting.cm.core.media.persis.po.MediaAssetPo;
import com.woting.crawler.core.album.persis.po.AlbumPo;
import com.woting.crawler.core.audio.persis.po.AudioPo;
import com.woting.crawler.core.dict.persis.po.DictDPo;
import com.woting.crawler.core.dict.service.CrawlerDictService;
import com.woting.crawler.core.scheme.model.Scheme;
import com.woting.crawler.ext.SpringShell;

public abstract class ConvertUtils {

	public static List<AlbumPo> convert2Album(List<Map<String, Object>> list, String publisher) {
		List<AlbumPo> albums = new ArrayList<AlbumPo>();
		if (list != null && !list.isEmpty()) {
			for (Map<String, Object> m : list) {
				AlbumPo al = new AlbumPo();
				al.setId(SequenceUUID.getPureUUID());
				al.setAlbumId(m.get("albumId") + "");
				al.setAlbumName(m.get("albumName") + "");
				al.setAlbumImg(m.get("albumImg") + "");
				al.setAlbumPublisher(publisher);
				al.setAlbumTags(m.get("tags") + "");
				al.setCategoryId(m.get("categoryId") + "");
				al.setCategoryName(m.get("categoryName") + "");
				al.setPlayCount(m.get("playCount") + "");
				al.setVisitUrl(m.get("visitUrl") + "");
				al.setDescn(m.get("descript") + "");
				al.setCrawlerNum(m.get("CrawlerNum") + "");
				al.setSchemeId(m.get("schemeId") + "");
				al.setSchemeName(m.get("schemeName") + "");
				al.setcTime(new Timestamp(System.currentTimeMillis()));
				albums.add(al);
			}
		}
		return albums;
	}

	public static List<AudioPo> convert2Aludio(List<Map<String, Object>> list, String publisher) {
		List<AudioPo> audios = new ArrayList<AudioPo>();
		if (list != null && list.size() > 0) {
			for (Map<String, Object> m : list) {
				AudioPo audio = new AudioPo();
				audio.setId(SequenceUUID.getPureUUID());
				audio.setAudioId(m.get("audioId") + "");
				audio.setAudioName(m.get("audioName") + "");
				audio.setAlbumId(m.get("albumId") + "");
				audio.setAlbumName(m.get("albumName") + "");
				audio.setAudioImg(m.get("audioImg") + "");
				audio.setCategoryId(m.get("categoryId") + "");
				audio.setCategoryName(m.get("categoryName") + "");
				audio.setAudioURL(m.get("playUrl") + "");
				audio.setAudioTags(m.get("tags") + "");
				audio.setDuration(m.get("duration") + "");
				audio.setPlayCount(m.get("playCount") + "");
				audio.setDescn(m.get("descript") + "");
				audio.setAudioPublisher(publisher);
				audio.setVisitUrl(m.get("visitUrl") + "");
				audio.setCrawlerNum(m.get("CrawlerNum") + "");
				audio.setSchemeId(m.get("schemeId") + "");
				audio.setSchemeName(m.get("schemeName") + "");
				audio.setcTime(new Timestamp(System.currentTimeMillis()));
				audios.add(audio);
			}
		}
		return audios;
	}

	public static List<DictDPo> convert2DictD(Scheme scheme, List<Map<String, Object>> list, String publisher, String dmid) {
		CrawlerDictService crawlerDictService = (CrawlerDictService) SpringShell.getBean("crawlerDictService");
		List<DictDPo> ddlist = crawlerDictService.getDictDList(publisher, scheme.getSchemenum());
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
				dd.setCrawlerNum(scheme.getSchemenum());
				dd.setVisitUrl(m.get("visitUrl") + "");
				dd.setcTime(new Timestamp(System.currentTimeMillis()));
				dictdlist.add(dd);
			}
		}
		return dictdlist;
	}
	
	public static List<MediaAssetPo> convert2MediaAsset(List<AudioPo> aulist){
		if(aulist!=null&&aulist.size()>0){
			for (AudioPo au : aulist) {
				MediaAssetPo ma = new MediaAssetPo();
			}
		}
		return null;
	}
}
