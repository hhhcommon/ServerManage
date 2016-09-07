package com.woting.crawler.core.album.model;

import java.io.Serializable;
import java.util.List;

import com.spiritdata.framework.core.model.ModelSwapPo;
import com.woting.crawler.core.album.persis.po.AlbumPo;
import com.woting.crawler.core.audio.persis.po.AudioPo;

public class Album implements Serializable, ModelSwapPo {

	private static final long serialVersionUID = -2274656839379986208L;
	private AlbumPo albumPo;
	private List<AudioPo> audiolist;
	
	public AlbumPo getAlbumPo() {
		return albumPo;
	}
	public void setAlbumPo(AlbumPo albumPo) {
		this.albumPo = albumPo;
	}
	public List<AudioPo> getAudiolist() {
		return audiolist;
	}
	public void setAudiolist(List<AudioPo> audiolist) {
		this.audiolist = audiolist;
	}
	@Override
	public void buildFromPo(Object po) {
		
	}
	@Override
	public Object convert2Po() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
