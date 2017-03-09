package com.woting.crawler.core.imagehash.service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import com.spiritdata.framework.core.dao.mybatis.MybatisDAO;
import com.woting.crawler.core.imagehash.persis.po.ImageHash;

public class ImageHashService {
	@Resource(name = "defaultDAO_CM")
	private MybatisDAO<ImageHash> imageHashDao;
	
	@PostConstruct
	public void initParam() {
		imageHashDao.setNamespace("A_IMAGEHASH");
	}
	
	public void insertImageHash(ImageHash imageHash) {
		imageHashDao.insert(imageHash);
	}
	
	public ImageHash getImageHash(String hashCode) {
		return imageHashDao.getInfoObject("getInfo", hashCode);
	}
}
