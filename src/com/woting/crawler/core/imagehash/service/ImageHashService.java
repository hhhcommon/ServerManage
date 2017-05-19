package com.woting.crawler.core.imagehash.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	
	public List<ImageHash> getImageHashByImageSrcPath(String imageSrcPath, String purpose) {
		Map<String, Object> m = new HashMap<>();
		m.put("imageSrcPath", imageSrcPath);
		m.put("purpose", purpose);
		m.put("orderSql", " cTime desc");
		m.put("limitSql", " 1");
		return imageHashDao.queryForList(m);
	}
	
	public ImageHash getImageHash(String hashCode) {
		return imageHashDao.getInfoObject("getInfo", hashCode);
	}
	
	public void updateImageHash(ImageHash imageHash) {
		imageHashDao.update(imageHash);
	}
}
