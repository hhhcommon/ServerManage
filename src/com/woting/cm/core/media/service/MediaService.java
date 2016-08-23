package com.woting.cm.core.media.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import com.spiritdata.framework.core.dao.mybatis.MybatisDAO;
import com.spiritdata.framework.util.JsonUtils;
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
		mediaAssetDao.insert("insertMa", ma.toHashMap());
	}

	public void insertMaList(List<MediaAssetPo> malist) {
		List<MediaAssetPo> mas = new ArrayList<>();
		int num = 0;
		for (MediaAssetPo mediaAssetPo : malist) {
			mas.add(mediaAssetPo);
			if (num == 50) {
				System.out.println(JsonUtils.objToJson(mas));
				Map<String, Object> m = new HashMap<>();
				m.put("list", mas);
				mediaAssetDao.insert("insertMaList", m);
				mas.clear();
				num = 0;
			}
			num++;
		}
		Map<String, Object> m = new HashMap<>();
		m.put("list", mas);
		mediaAssetDao.insert("insertMaList", m);
	}

	public void insertMasList(List<MaSourcePo> maslist) {
		List<MaSourcePo> ms = new ArrayList<>();
		int num = 0;
		for (MaSourcePo maSourcePo : maslist) {
			ms.add(maSourcePo);
			if (num == 1000) {
				Map<String, Object> m = new HashMap<>();
				m.put("list", ms);
				mediaAssetDao.insert("insertMaList", m);
				ms.clear();
				num = 0;
			}
			num++;
		}
		Map<String, Object> m = new HashMap<>();
		m.put("list", ms);
		mediaAssetDao.insert("insertMasList", m);
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
		Map<String, Object> m = new HashMap<>();
		m.put("list", smas);
		mediaAssetDao.insert("insertSeqList", m);
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
		}
		Map<String, Object> m = new HashMap<>();
		m.put("list", smarefs);
		mediaAssetDao.insert("insertSeqRefList", m);
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

	public List<SeqMediaAssetPo> getSeqSameList(List<String> ns) {
		Map<String, Object> m = new HashMap<>();
		m.put("list", ns);
		List<SeqMediaAssetPo> smalist = seqDao.queryForList("getSeqSameList", m);
		return smalist;
	}

	public List<SeqMediaAssetPo> getSeqInfo(String albumName, String publisher) {
		Map<String, Object> m = new HashMap<>();
		m.put("smaTitle", albumName);
		m.put("smaPublisher", publisher);
		List<SeqMediaAssetPo> seqlist = seqDao.queryForList("getSeqSame", m);
		return seqlist;
	}

	public MediaAsset getMaInfoById(String id) {
		MediaAsset ma = new MediaAsset();
		ma.buildFromPo(mediaAssetDao.getInfoObject("getInfoById", id));
		return ma;
	}

	public void saveMa(MediaAsset ma) {
		mediaAssetDao.insert("insertMa", ma.convert2Po());
	}

	public void saveMas(MaSource mas) {
		maSourceDao.insert("insertMas", mas.convert2Po());
	}

	public void insertMediaPlayCountList(List<MediaPlayCountPo> mediaCounts) {
		Map<String, Object> m = new HashMap<>();
		m.put("list", mediaCounts);
		mediaplaycountDao.insert("insertMediaPlayCountList", m);
	}

	public List<MediaAssetPo> getMaSameList(List<String> urls) {
		Map<String, Object> m = new HashMap<>();
		m.put("list", urls);
		List<MediaAssetPo> malist = mediaAssetDao.queryForList("getMaSameList", m);
		return malist;
	}

	public int getMaSame(String url) {
		return mediaAssetDao.getCount("getMaSame", url);
	}
}