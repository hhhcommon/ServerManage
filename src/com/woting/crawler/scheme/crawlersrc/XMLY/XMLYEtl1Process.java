package com.woting.crawler.scheme.crawlersrc.XMLY;

import java.sql.Timestamp;
import java.util.Map;

import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;

import com.spiritdata.framework.ext.spring.redis.RedisOperService;
import com.spiritdata.framework.util.SequenceUUID;
import com.woting.crawler.core.album.persis.po.AlbumPo;
import com.woting.crawler.ext.SpringShell;

public class XMLYEtl1Process {
	
	@SuppressWarnings("unchecked")
	public void insertNewAlbum(Map<String, Object> alm) {
		JedisConnectionFactory jedisConnectionFactory = (JedisConnectionFactory) SpringShell.getBean("connectionFactory");
		RedisOperService rs = new RedisOperService(jedisConnectionFactory, 6);
		alm = (Map<String, Object>) alm.get("data");
		Map<String, Object> albummap = (Map<String, Object>) alm.get("album");
		Map<String, Object> usermap = (Map<String, Object>) alm.get("user");
		Map<String, Object> tracks = (Map<String, Object>) alm.get("tracks");
		AlbumPo albumPo = new AlbumPo();
		albumPo.setId(SequenceUUID.getPureUUID());
		albumPo.setAlbumId(albummap.get("albumId")+"");
		String img = albummap.get("coverLargePop")+"";
		if (img.length()<10) {
			img = albummap.get("coverWebLarge")+"";
		}
		if (img.length()<10) {
			img = albummap.get("coverLarge")+"";
		}
		albumPo.setAlbumImg(img);
		albumPo.setAlbumName(albummap.get("nickname")+"");
		albumPo.setAlbumPublisher("喜马拉雅");
		albumPo.setPlayCount(albummap.get("playTimes")+"");
		if (albummap.get("KeyWords")!=null) {
			albumPo.setAlbumTags(albummap.get("KeyWords")+"");
		}
		albumPo.setDescn(albummap.get("intro")+"");
		albumPo.setCategoryId(albummap.get("categoryId")+"");
		albumPo.setCategoryName(rs.get("XMLY:ZJ:")+albumPo.getAlbumId());
		albumPo.setcTime(new Timestamp(Long.valueOf(albummap.get("createdAt")+"")));
		
	}
}
