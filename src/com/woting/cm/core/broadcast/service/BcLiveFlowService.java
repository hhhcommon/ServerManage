package com.woting.cm.core.broadcast.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import com.spiritdata.framework.core.dao.mybatis.MybatisDAO;
import com.woting.cm.core.broadcast.persis.po.BCLiveFlowPo;

@Service
public class BcLiveFlowService {
	
	@Resource(name="defaultDAO_CM")
	private MybatisDAO<BCLiveFlowPo> bclfDao;
	
	@PostConstruct
    public void initParam() {
        bclfDao.setNamespace("A_BCLIVEFLOW");
    }
	
	public Map<String, Object> getBCLiveFlowMap(){
		Map<String, Object> m1 = new HashMap<String,Object>();
		Map<String, Object> m2 = new HashMap<String,Object>();
		List<BCLiveFlowPo> bclflist = bclfDao.queryForList("getBCLiveFlowList");
		if(bclflist==null || bclflist.size()==0) 
			return null;
		for (BCLiveFlowPo bcLiveFlowPo : bclflist) {
			m1.put(bcLiveFlowPo.getBcSource()+"::"+bcLiveFlowPo.getBcSrcChannelId(), bcLiveFlowPo.getFlowURI());
			m2.put(bcLiveFlowPo.getBcSource()+"::"+bcLiveFlowPo.getBcSrcChannelId(), bcLiveFlowPo.getBcId());
		}
		Map<String, Object> map = new HashMap<String,Object>();
		map.put("chid_flowuri", m1);
		map.put("chid_bcid", m2);
		return map;
	}
	
	public List<BCLiveFlowPo> getBCLiveFlowList(String bcSource){
		List<BCLiveFlowPo> bclflist = bclfDao.queryForList("getBCLiveFolwByBcSource", bcSource);
		return bclflist;
	}
	
	public List<BCLiveFlowPo> getBCLiveFlowListByBcId(String bcId) {
		Map<String, Object> m = new HashMap<>();
		m.put("bcId", bcId);
		List<BCLiveFlowPo> bcls = bclfDao.queryForList("getBCLiveFlowList", m);
		if (bcls!=null && bcls.size()>0) {
			return bcls;
		}
		return null;
	}
	
	public Map<String, Object> getBCLFMap(String bcSource) {
		Map<String, Object> m = new HashMap<String,Object>();
		List<BCLiveFlowPo> bclflist = getBCLiveFlowList(bcSource);
		for (BCLiveFlowPo bcLiveFlowPo : bclflist) {
			m.put(bcLiveFlowPo.getBcSrcChannelId(), bcLiveFlowPo.getBcId());
		}
		return m;
	}
	
	public void insertBCLiveFlow(BCLiveFlowPo bclf) {
		bclfDao.insert("insert", bclf);
	}
	
	public List<BCLiveFlowPo> getBCLiveFlowBySrcChannelId(String srcChannelId) {
		List<BCLiveFlowPo> bclflist = bclfDao.queryForList("getBCLiveFolwBySrcChannelId", srcChannelId);
		return bclflist;
	}
	
	public void insertBCLiveFlowList(List<BCLiveFlowPo> bclflist) {
		bclfDao.insert("insertList", bclflist);
	}
	
	public void updateBCLiveFlow(BCLiveFlowPo bcLiveFlowPo){
		bclfDao.update(bcLiveFlowPo);
	}
	
	public void updateBCLiveFlowList(List<BCLiveFlowPo> bclflist) {
		bclfDao.update("updateList", bclflist);
//		String url = "jdbc:mysql://123.56.254.75/woting";
//		String name = "com.mysql.jdbc.Driver";
//		String user = "root";
//		String password = "mysql";
//		Connection conn = null;
//		try {
//			Class.forName(name);// 指定连接类型
//			conn = DriverManager.getConnection(url, user, password);
//			conn.setAutoCommit(false);
//			String sql = "update wt_BCLiveFlow set flowURI=?,cTime=? where bcSource=? and bcSrcChannelId=?";
//			PreparedStatement ps = conn.prepareStatement(sql);
//			for (BCLiveFlowPo bcLiveFlowPo : bclflist) {
//				ps.setString(1,bcLiveFlowPo.getFlowURI());
//				ps.setTimestamp(2, bcLiveFlowPo.getcTime());
//				ps.setString(3, bcLiveFlowPo.getBcSource());
//				ps.setString(4, bcLiveFlowPo.getBcSrcChannelId());
//				ps.addBatch();
//			}
//			ps.executeBatch();
//			conn.commit();
//		} catch (Exception e) {e.printStackTrace();}
//		System.out.println(JsonUtils.objToJson(bclflist));
//		Map<String, Object> m = new HashMap<String,Object>();
//		m.put("list", bclflist);
//		bclfDao.update("updateList", m);
	}
	
	public void deleteBCLiveFlowBySrcChannelId(String bcSrcChannelId) {
		bclfDao.delete("deleteBybcSrcChannelId", bcSrcChannelId);
	}

	public List<BCLiveFlowPo> getBroadcastNotInResDictIsMain(String mid) {
		return bclfDao.queryForList("getBCLiveFlowNotInResDict", mid);
	}
}
