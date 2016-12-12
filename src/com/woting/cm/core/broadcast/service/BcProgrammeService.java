package com.woting.cm.core.broadcast.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import com.spiritdata.framework.core.dao.mybatis.MybatisDAO;
import com.woting.cm.core.broadcast.persis.po.BCProgrammePo;

@Service
public class BcProgrammeService {

	@Resource(name="defaultDAO_CM")
	private MybatisDAO<BCProgrammePo> bcProDao;
	
	@PostConstruct
    public void initParam() {
        bcProDao.setNamespace("A_BCPROGRAMME");
    }
	
	public void insertBCProgrammeList(List<BCProgrammePo> bcProlist) {
		int num=0;
		List<BCProgrammePo> pplist = new ArrayList<BCProgrammePo>();
		for (int i = 0; i < bcProlist.size(); i++) {
			pplist.add(bcProlist.get(i));
			num++;
			if(num==1000) {
				bcProDao.insert("insertList", pplist);
				pplist.clear();
				num=0;
			}
		}
		bcProDao.insert("insertList", pplist);
	}
	
	public void deleteById(String id) {
		bcProDao.delete("deleteById", id);
	}
	
	public void delete() {
		bcProDao.delete("delete");
	}
	
	public Map<String, Object> getBCProgrammeStr(){
		Map<String, Object> bcprostr = new HashMap<String,Object>();
		List<BCProgrammePo> bcprolist = bcProDao.queryForList("getBCProgrammeList");
		for (BCProgrammePo bcPro : bcprolist) {
			bcprostr.put(bcPro.getBcId()+bcPro.getWeekDay(), bcPro.getTitle()+bcPro.getBeginTime()+bcPro.getEndTime());
		}
		return bcprostr;
	}
}
