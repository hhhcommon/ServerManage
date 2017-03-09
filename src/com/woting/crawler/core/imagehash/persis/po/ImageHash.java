package com.woting.crawler.core.imagehash.persis.po;

import java.sql.Timestamp;

import com.spiritdata.framework.core.model.BaseObject;

public class ImageHash extends BaseObject {
	private static final long serialVersionUID = -4769016187952685959L;

	private String id;
	private String imageSrcPath;
	private String imagePath;
	private String purpose;
	private Timestamp cTime;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getImageSrcPath() {
		return imageSrcPath;
	}
	public void setImageSrcPath(String imageSrcPath) {
		this.imageSrcPath = imageSrcPath;
	}
	public String getImagePath() {
		return imagePath;
	}
	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}
	public String getPurpose() {
		return purpose;
	}
	public void setPurpose(String purpose) {
		this.purpose = purpose;
	}
	public Timestamp getcTime() {
		return cTime;
	}
	public void setcTime(Timestamp cTime) {
		this.cTime = cTime;
	}
}
