package com.woting.crawler.scheme.crawlerplaynum.XMLY;

import java.util.Map;

import org.apache.commons.lang3.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.spiritdata.framework.util.JsonUtils;
import com.woting.cm.core.ResOrgAsset.persis.po.ResOrgAssetPo;
import com.woting.crawler.core.album.persis.po.AlbumPo;
import com.woting.crawler.core.album.service.AlbumService;
import com.woting.crawler.core.audio.service.AudioService;
import com.woting.crawler.ext.SpringShell;

public class XMLYPlayNumCrawler {
	private AlbumService albumService;
	private AudioService audioService;
	private static String xmlyAlbumPlayUrl = "http://mobile.ximalaya.com/mobile/v1/album?albumId=#albumId#&pageId=1&pageSize=5&pre_page=0";
	
	@SuppressWarnings("unchecked")
	public void parsePlayNum(ResOrgAssetPo resass) {
		if(resass!=null) {
			if (resass.getOrigTableName().equals("hotspot_Album")) {
				albumService = (AlbumService) SpringShell.getBean("albumService");
				AlbumPo al = albumService.getAlbumInfo(resass.getOrigId());
				String albumId = al.getAlbumId();
				String url = xmlyAlbumPlayUrl.replace("#albumId#", albumId);
				Document doc;
				try {
					doc = Jsoup.connect(url).ignoreContentType(true).timeout(10000).get();
					if (doc!=null) {
						String alstr = doc.body().html();
						alstr = StringEscapeUtils.unescapeHtml4(alstr);
						Map<String, Object> m = (Map<String, Object>) JsonUtils.jsonToObj(alstr, Map.class);
						Map<String, Object> album = (Map<String, Object>) ((Map<String, Object>) m.get("data")).get("album");
						if(album!=null) {
							
						}
					}
				} catch (Exception e) {
					
				}
			}
		}
	}
}
