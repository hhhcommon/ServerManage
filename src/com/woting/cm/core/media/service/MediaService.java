package com.woting.cm.core.media.service;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import com.spiritdata.framework.core.dao.mybatis.MybatisDAO;
import com.woting.cm.core.media.model.MaSource;
import com.woting.cm.core.media.model.MediaAsset;
import com.woting.cm.core.media.persis.po.MaSourcePo;
import com.woting.cm.core.media.persis.po.MediaAssetPo;
import com.woting.cm.core.media.persis.po.MediaPlayCountPo;
import com.woting.cm.core.media.persis.po.SeqMaRefPo;
import com.woting.cm.core.media.persis.po.SeqMediaAssetPo;

public class MediaService {
	@Resource(name = "defaultDAO")
	private MybatisDAO<MediaAssetPo> mediaAssetDao;
	@Resource(name = "defaultDAO")
	private MybatisDAO<MaSourcePo> maSourceDao;
	@Resource(name = "defaultDAO")
	private MybatisDAO<SeqMediaAssetPo> seqDao;
	@Resource(name = "defaultDAO")
	private MybatisDAO<SeqMaRefPo> seqrefDao;
	@Resource(name = "defaultDAO")
	private MybatisDAO<MediaPlayCountPo> mediaplaycountDao;

	@PostConstruct
	public void initParam() {
		mediaAssetDao.setNamespace("A_MEDIA");
		maSourceDao.setNamespace("A_MEDIA");
		seqDao.setNamespace("A_MEDIA");
		seqrefDao.setNamespace("A_MEDIA");
		mediaplaycountDao.setNamespace("A_MEDIA");
	}

	public void insertMa(MediaAssetPo ma) {
		mediaAssetDao.insert("insertMa", ma);
	}

	public void insertMaList(List<MediaAssetPo> malist) {
		List<MediaAssetPo> mas = new ArrayList<>();
		int num = 0;
		for (int i = 0; i < malist.size(); i++) {
			if(malist.get(i).getMaTitle()!=null) {
				if (malist.get(i).getDescn().length()>=4000) {
					malist.get(i).setDescn(malist.get(i).getDescn().substring(0, 3990)+"...");
				}
				mas.add(malist.get(i));
			}
			num++;
			if(num==1000){
			    mediaAssetDao.insert("insertMaList", mas);
				mas.clear();
				num=0;
			}
		}
		if(mas!=null && mas.size()>0){
		    mediaAssetDao.insert("insertMaList", mas);
		}
	}

	public void insertMasList(List<MaSourcePo> maslist) {
		List<MaSourcePo> ms = new ArrayList<>();
		int num = 0;
		for (MaSourcePo maSourcePo : maslist) {
			ms.add(maSourcePo);
			if (num == 1000) {
				Map<String, Object> m = new HashMap<>();
				m.put("list", ms);
				mediaAssetDao.insert("insertMasList", m);
				ms.clear();
				num = 0;
			}
			num++;
		}
		if(ms!=null&&ms.size()>0){
			Map<String, Object> m = new HashMap<>();
		    m.put("list", ms);
		    mediaAssetDao.insert("insertMasList", m);
		}
	}

	public void insertSeqList(List<SeqMediaAssetPo> seqlist) {
		List<SeqMediaAssetPo> smas = new ArrayList<>();
		int num = 0;
		for (SeqMediaAssetPo seqMediaAssetPo : seqlist) {
			smas.add(seqMediaAssetPo);
			if (num == 1000) {
				Map<String, Object> m = new HashMap<>();
				m.put("list", smas);
				mediaAssetDao.insert("insertSeqList", m);
				smas.clear();
				num=0;
			}
			num++;
		}
		if(smas!=null&&smas.size()>0){
			Map<String, Object> m = new HashMap<>();
		    m.put("list", smas);
		    mediaAssetDao.insert("insertSeqList", m);
		}
	}

	public void insertSeqRefList(List<SeqMaRefPo> seqreflist) {
		List<SeqMaRefPo> smarefs = new ArrayList<>();
		int num = 0;
		for (SeqMaRefPo seqMaRefPo : seqreflist) {
			smarefs.add(seqMaRefPo);
			if (num == 1000) {
				Map<String, Object> m = new HashMap<>();
				m.put("list", smarefs);
				mediaAssetDao.insert("insertSeqRefList", m);
				smarefs.clear();
				num=0;
			}
			num++;
		}if(smarefs!=null&&smarefs.size()>0){
			Map<String, Object> m = new HashMap<>();
		    m.put("list", smarefs);
		    mediaAssetDao.insert("insertSeqRefList", m);
		}
	}
	
	public SeqMaRefPo getOneSmarefOrderByColumnNum(String sId) {
		Map<String, Object> m = new HashMap<>();
		m.put("sId", sId);
		return seqrefDao.getInfoObject("getSmafOrderByColumnNum", m);
	}

	public void insertSeq(SeqMediaAssetPo seq) {
		seqDao.insert("insertSma", seq.toHashMap());
	}

	public void insertMas(MaSourcePo mas) {
		maSourceDao.insert("insertMas", mas.toHashMap());
	}

	public void insertSeqRef(SeqMaRefPo seqref) {
		seqrefDao.insert("bindMa2Sma", seqref.toHashMap());
	}
	
	public List<MediaAssetPo> getMaList(Map<String, Object> m) {
		if (m!=null && m.size()>0) {
			List<MediaAssetPo> mas = mediaAssetDao.queryForList("getMaList", m);
			if (mas!=null && mas.size()>0) {
				return mas;
			}
		}
		return null;
	}
	
	public List<SeqMediaAssetPo> getSeqMediaList(String smaPublisher, int page, int pageSize) {
		Map<String, Object> m = new HashMap<>();
		m.put("smaPublisher", smaPublisher);
		if (page>0 && pageSize>0) {
			m.put("limitByClause", (page-1)*pageSize+","+pageSize);
		}
		List<SeqMediaAssetPo> smas = seqDao.queryForList("getSeqList", m);
		if (smas!=null && smas.size()>0) {
			return smas;
		}
		return null;
	}

	public List<SeqMediaAssetPo> getSeqSameList(List<String> ns) {
		Map<String, Object> m = new HashMap<>();
		m.put("list", ns);
		List<SeqMediaAssetPo> smalist = seqDao.queryForList("getSeqSameList", m);
		return smalist;
	}
	
	public SeqMediaAssetPo getSeqInfo(String id) {
		Map<String, Object> m = new HashMap<>();
		m.put("id", id);
		SeqMediaAssetPo seqMediaAssetPo = seqDao.getInfoObject("getSMaList", m);
		return seqMediaAssetPo;
	}

	public List<SeqMediaAssetPo> getSeqInfo(String albumName, String publisher) {
		Map<String, Object> m = new HashMap<>();
		m.put("smaTitle", albumName);
		m.put("smaPublisher", publisher);
		List<SeqMediaAssetPo> seqlist = seqDao.queryForList("getSeqSame", m);
		return seqlist;
	}

	public MediaAssetPo getMaInfoById(String id) {
		return mediaAssetDao.getInfoObject("getInfoById", id);
	}
	
	public MediaPlayCountPo getMediaPlayCount(Map<String, Object> m) {
//		m.put("resId", resId);
//		m.put("resTableName", resTableName);
		List<MediaPlayCountPo> mplays = mediaplaycountDao.queryForList("getMediaPlayCountLatest", m);
		if(mplays!=null && mplays.size()>0) 
			return mplays.get(0);
		return null;
	}

	public void saveMa(MediaAsset ma) {
		mediaAssetDao.insert("insertMa", ma.convert2Po());
	}

	public void saveMas(MaSource mas) {
		maSourceDao.insert("insertMas", mas.convert2Po());
	}

	public void insertMediaPlayCountList(List<MediaPlayCountPo> mediaCounts) {
		List<MediaPlayCountPo> mepls = new ArrayList<MediaPlayCountPo>();
		int num = 0;
		for (MediaPlayCountPo mePlPo : mediaCounts) {
			mepls.add(mePlPo);
			num++;
			if (num==1000) {
				Map<String, Object> m = new HashMap<>();
		        m.put("list", mepls);
		        mediaplaycountDao.insert("insertMediaPlayCountList", m);
		        mepls.clear();
		        num=0;
			}
		}
		if (mepls!=null&&mepls.size()>0) {
			Map<String, Object> m = new HashMap<>();
	        m.put("list", mepls);
	        mediaplaycountDao.insert("insertMediaPlayCountList", m);
		}
	}
	
	public void insertMediaPlayCount(MediaPlayCountPo mpc) {
		mediaplaycountDao.insert("insertMediaPlayCount", mpc);
	}
	
	public List<MediaAssetPo> getMaSameList(String maURL, String maTitle, String maPublisher) {
		Map<String, Object> m = new HashMap<>();
		m.put("maURL", maURL);
		m.put("maTitle", maTitle);
		m.put("maPublisher", maPublisher);
		List<MediaAssetPo> malist = mediaAssetDao.queryForList("getMaSameList", m);
		return malist;
	}
	
	public List<MaSourcePo> getMaSources(String maId) {
		Map<String, Object> m = new HashMap<>();
		m.put("maId", maId);
		List<MaSourcePo> mas = maSourceDao.queryForList("getMaSources", m);
		if (mas!=null && mas.size()>0) {
			return mas;
		}
		return null;
	}

	public int getMaSame(String url) {
		return mediaAssetDao.getCount("getMaSame", url);
	}
	
	public int getSeqMaNumBySid(String sId) {
		return seqrefDao.getCount("getSeqMaNumBySid", sId);
	}
	
	public List<MediaAssetPo> getMaBySmaId(String sId, List<String> names) {
		Map<String, Object> m = new HashMap<>();
		List<String> ns = new ArrayList<>();
		if (names!=null && names.size()>0) {
			for (String str : names) {
				str = "%"+str+"%";
				ns.add(str);
			}
		}
		m.put("list", ns);
		m.put("sId", sId);
		return mediaAssetDao.queryForList("getMaBySmaId", m);
	}
	
	public List<MediaAssetPo> getMaByPublisher(String publisher, int page, int pagesize) {
		Map<String, Object> m = new HashMap<>();
		m.put("maPublisher", publisher);
		m.put("page", page);
		m.put("pagesize", pagesize);
		return mediaAssetDao.queryForList("getMaListByPublisher", m);
	}
	
	public int getSmaNum(String smaPublisher) {
		return seqDao.getCount("getSmaNum", smaPublisher);
	}
	
	public SeqMediaAssetPo getSmaById(String id) {
		return seqDao.getInfoObject("getSmaById", id);
	}
	
	public List<SeqMediaAssetPo> getSmaByPublisher(String publisher, int page, int pagesize) {
		Map<String, Object> m = new HashMap<>();
		m.put("smaPublisher", publisher);
		m.put("page", page);
		m.put("pagesize", pagesize);
		return seqDao.queryForList("getSmaListByPublisher", m);
	}
	
	public List<SeqMediaAssetPo> getSmaByMaId(String maId, String publisher) {
		Map<String, Object> m = new HashMap<>();
		m.put("smaPublisher", publisher);
		m.put("whereSql", " id in (select sId from wt_SeqMA_Ref where mId = '"+maId+"')");
		return seqDao.queryForList("getSMaList", m);
	}
	
	public List<SeqMediaAssetPo> getSmaList(String smaTitle, String smaPublisher) {
		Map<String, Object> m = new HashMap<>();
		m.put("smaTitle", smaTitle);
		m.put("smaPublisher", smaPublisher);
		List<SeqMediaAssetPo> smas = seqDao.queryForList("getSMaList", m);
		if (smas!=null && smas.size()>0) {
			return smas;
		}
		return null;
	}
	
	public List<SeqMediaAssetPo> getSmaByNames(List<String> names,String publisher) {
		Map<String, Object> m = new HashMap<>();
		if (names!=null && names.size()>0) {
			List<String> ns = new ArrayList<>();
			for (String str : names) {
				str = "%"+str+"%";
				ns.add(str);
			}
			m.put("list", ns);
			m.put("publisher", publisher);
			return seqDao.queryForList("getSeqSameListByNames", m);
		}
		return null;
	}
	public void updateMediaPlayCount(MediaPlayCountPo mply) {
		mediaplaycountDao.update("updateMediaPlayCount", mply);
	}
}