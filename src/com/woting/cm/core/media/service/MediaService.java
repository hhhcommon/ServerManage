package com.woting.cm.core.media.service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import com.spiritdata.framework.core.dao.mybatis.MybatisDAO;
import com.woting.cm.core.media.model.MaSource;
import com.woting.cm.core.media.model.MediaAsset;
import com.woting.cm.core.media.persis.po.MaSourcePo;
import com.woting.cm.core.media.persis.po.MediaAssetPo;

public class MediaService {
    @Resource(name="defaultDAO_CM")
    private MybatisDAO<MediaAssetPo> mediaAssetDao;
    @Resource(name="defaultDAO_CM")
    private MybatisDAO<MaSourcePo> maSourceDao;

    @PostConstruct
    public void initParam() {
        mediaAssetDao.setNamespace("A_MEDIA");
        maSourceDao.setNamespace("A_MEDIA");
    }

    public MediaAsset getMaInfoById(String id) {
        MediaAsset ma=new MediaAsset();
        ma.buildFromPo(mediaAssetDao.getInfoObject("getInfoById", id));
        return ma;
    }

    public void saveMa(MediaAsset ma) {
        mediaAssetDao.insert("insertMa", ma.convert2Po());
    }

    public void saveMas(MaSource mas) {
        maSourceDao.insert("insertMas", mas.convert2Po());
    }
}