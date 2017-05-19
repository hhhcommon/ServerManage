package com.woting.crawler.scheme.utils;

import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.encoding.conversion.CJKConverter;
import org.apache.solr.client.solrj.SolrQuery.SortClause;
import org.apache.solr.common.SolrInputDocument;

import com.woting.cm.core.media.persis.po.MediaAssetPo;
import com.woting.cm.core.media.persis.po.SeqMediaAssetPo;
import com.woting.crawler.core.solr.persis.po.SolrInputPo;

/**
 * Solr工具
 * @author wbq
 *
 */
public abstract class SolrUtils {

	public static SolrInputPo convert2SolrInput(Object obj, String pid, String persons, String chstr, long playcount) {
		if (obj instanceof SeqMediaAssetPo) {
			SeqMediaAssetPo sma = (SeqMediaAssetPo) obj;
			SolrInputPo sPo = new SolrInputPo();
			sPo.setId("SEQU_"+sma.getId());
			sPo.setItem_id(sma.getId());
			sPo.setItem_title(sma.getSmaTitle());
			sPo.setItem_type("SEQU");
			sPo.setItem_mediasize(sma.getSmaAllCount());
			sPo.setItem_publisher(sma.getSmaPublisher());
			if (chstr!=null) {
				sPo.setItem_channel(chstr);
			}
			sPo.setItem_descn(sma.getDescn());
			if (persons!=null) {
				sPo.setItem_persons(persons);
			}
			sPo.setItem_playcount(playcount);
			return sPo;
		} else {
			if (obj instanceof MediaAssetPo) {
				MediaAssetPo ma = (MediaAssetPo) obj;
				SolrInputPo sPo = new SolrInputPo();
				sPo.setId("AUDIO_"+ma.getId());
				sPo.setItem_id(ma.getId());
				if (pid!=null) {
					sPo.setItem_pid(pid);
				}
				if (persons!=null) {
					sPo.setItem_persons(persons);
				}
				sPo.setItem_title(ma.getMaTitle());
				sPo.setItem_publisher(ma.getMaPublisher());
				sPo.setItem_descn(ma.getDescn());
				sPo.setItem_timelong(ma.getTimeLong());
				if (chstr!=null) {
					sPo.setItem_channel(chstr);
				}
				sPo.setItem_playcount(playcount);
				sPo.setItem_type("AUDIO");
				return sPo;
			}
		}
		return null;
	}
	
	
	public static SolrInputDocument convert2SolrDocument(SolrInputPo solrInputPo) {
		if (solrInputPo!=null) {
			SolrInputDocument document = new SolrInputDocument();
			if (solrInputPo.getId()!=null) document.addField("id", solrInputPo.getId());
			if (solrInputPo.getItem_id()!=null) document.addField("item_id", solrInputPo.getItem_id());
			if (solrInputPo.getItem_title()!=null) document.addField("item_title", solrInputPo.getItem_title()); 
			if (solrInputPo.getItem_imghash()!=null) document.addField("item_imghash", solrInputPo.getItem_imghash());
			if (solrInputPo.getItem_publisher()!=null) document.addField("item_publisher", solrInputPo.getItem_publisher());
			if (solrInputPo.getItem_timelong()!=0) document.addField("item_timelong", solrInputPo.getItem_timelong());
			if (solrInputPo.getItem_mediasize()!=0) document.addField("item_meidasize", solrInputPo.getItem_mediasize());
			if (solrInputPo.getItem_type()!=null) document.addField("item_type", solrInputPo.getItem_type());
			if (solrInputPo.getItem_pid()!=null) document.addField("item_pid", solrInputPo.getItem_pid());
		    document.addField("item_playcount", solrInputPo.getItem_playcount());
			if (solrInputPo.getItem_descn()!=null) document.addField("item_descn", solrInputPo.getItem_descn());
			if (solrInputPo.getItem_persons()!=null) document.addField("item_persons", solrInputPo.getItem_persons());
			if (solrInputPo.getItem_channel()!=null) document.addField("item_channel", solrInputPo.getItem_channel());
			return document;
		}
		return null;
	}
	
	/**
	 * 去除solr查询过敏字段
	 * @param str
	 * @param searorans true:查询        false:分词
	 * @return
	 */
	public static String makeQueryStr(String str, boolean searorans) {
		String[] allergicField   = {"[","]",":","（","）","\"","“","”","/","{","}","-"," ","《","》","~"};
		String[] reallergicField = {"" ,"" ,"" ,"(",")" ,""  ,"" ,"" ,"" ,"" ,"" ,"" ,"" ,"" , "", "" };
		String[] analysis = {"第","集"};
		String[] reanalysis = {"",""};
		if (searorans) { // 整理待查询字段
			if (str!=null && str.length()>0) {
				for (int i = 0; i < allergicField.length; i++) {
					str = str.replace(allergicField[i], reallergicField[i]);
				}
				if (str.contains("(") && !str.contains(")")) str = str.replace("(", "");
				if (str.contains(")") && !str.contains("(")) str = str.replace(")", "");
				return cleanStrNum(str);
			}
		} else { // 整理待搜索字段
			if (str!=null && str.length()>0) {
				for (int i = 0; i < allergicField.length; i++) {
					str = str.replace(allergicField[i], reallergicField[i]);
				}
				if (str.contains("(") && !str.contains(")")) str = str.replace("(", "");
				if (str.contains(")") && !str.contains("(")) str = str.replace(")", "");
				for (int i = 0; i < analysis.length; i++) {
					str = str.replace(analysis[i], reanalysis[i]);
				}
				return cleanStrNum(str);
			}
		}
		return null;
	}
	
	public static List<SortClause> makeSolrSort(String... flstr) {
		if (flstr!=null && flstr.length>0) {
			List<SortClause> sorts = new ArrayList<>();
			for (String sort : flstr) {
				String[] sortts = sort.split(" ");
				sorts.add(new SortClause(sortts[0], sortts[1]));
			}
			return sorts;
		}
		return null;
	}
	
	public static String cleanStrNum(String title) {
		String queryStr = "item_title:"+title;
    	String _title = title;
    	String strNum = getStrNum(title);
    	if (strNum!=null && strNum.length()>0) {
			queryStr += " item_title:"+strNum;
			while(true) {
        		String _title_ = makeStrNum(_title);
        		if (_title_.length()==_title.length()) break;
        		else {
        			queryStr += " item_title:"+_title_;
        			_title = _title_;
        		}
        	}
		}
		return queryStr;
	}
	
	public static String makeStrNum(String title) {
		title = title.trim();
		int begNum = 0;
		int begStr = -1;
		int endStr = title.length();
		boolean isCon_1 = false;
		boolean isCon = false;
		String numStr = "";
		for (int i = 0; i < title.length(); i++) {
			if(title.charAt(i)>=48 && title.charAt(i)<=57){
				if (title.charAt(i)==48 && begNum==0 && isCon_1==false && isCon == false) {
					begStr = i;
					isCon = true;
				}
				if (isCon_1==false) isCon_1 = true;
			} else {
				isCon_1 = false;
				if (begNum>=0 && isCon==true) {
					endStr = i;
					isCon = false;
				}
			}
		}
		if (begStr>=0 && endStr>=0 && begStr < endStr) {
			numStr = title.substring(begStr, endStr);
			String _numStr = numStr.substring(1);
			title = title.replace(numStr, _numStr);
		}
		return title;
	}
	
	public static String getStrNum(String title) {
		title = title.trim();
		int begNum = 0;
		int begStr = -1;
		int endStr = title.length();
		boolean isCon_1 = false;
		boolean isCon = false;
		String numStr = "";
		for (int i = 0; i < title.length(); i++) {
			if(title.charAt(i)>=48 && title.charAt(i)<=57){
				if (title.charAt(i)==48 && begNum==0 && isCon_1==false && isCon == false) {
					begStr = i;
					isCon = true;
					endStr = title.length();
				}
				if (isCon_1==false) isCon_1 = true;
			} else {
				isCon_1 = false;
				if (begNum>=0 && isCon==true) {
					endStr = i;
					isCon = false;
				}
			}
		}
		if (begStr>=0 && endStr>=0 && begStr < endStr) {
			numStr = title.substring(begStr, endStr);
			return numStr;
		}
		return null;
	}
}
