package com.woting.crawler.scheme.searchcrawler.utils;

import com.spiritdata.framework.util.JsonUtils;
import com.woting.crawler.core.album.persis.po.AlbumPo;
import com.woting.crawler.core.audio.persis.po.AudioPo;
import com.woting.crawler.scheme.searchcrawler.model.Festival;
import com.woting.crawler.scheme.searchcrawler.model.Station;

public class AddInfoThread<T> extends Thread {

	private T T;
	
	public AddInfoThread (T T) {
		this.T = T;
	}
	
	public void addInfo() {
        String classname = T.getClass().getSimpleName();
        System.out.println(classname);
        if (classname.equals("Festival"))
            JsonUtils.objToJson(DataTransform.festival2Audio(false, (Festival) T));
        else if (classname.equals("Station"))
            JsonUtils.objToJson(DataTransform.datas2Sequ_Audio((Station) T));
        else if (classname.equals("HashMap"))
            JsonUtils.objToJson(T);
        else if (classname.equals("AudioPo"))
        	JsonUtils.objToJson(DataTransform.AudioPo(false,(AudioPo) T));
        else if (classname.equals("AlbumPo"))
        	JsonUtils.objToJson(DataTransform.albumPo(true,(AlbumPo) T));
//        if (!StringUtils.isNullOrEmptyOrSpace(value)&&!value.toLowerCase().equals("null")) {
//        	ros.rPush("Search_" + key + "_Data", value);
//        }
	}
	
	@Override
	public void run() {
		addInfo();
	}
}
