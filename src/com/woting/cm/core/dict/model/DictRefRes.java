package com.woting.cm.core.dict.model;

import java.io.Serializable;
import java.sql.Timestamp;

import com.spiritdata.framework.core.model.ModelSwapPo;

public class DictRefRes implements Serializable, ModelSwapPo {
    private static final long serialVersionUID=5201517946401777207L;

    private String id; //uuid(主键)
    private String refName; //关系名称：resType+dictMId=唯一关系名称，既相当于某类资源的一个字段
    private String resTableName; //资源类型Id：1电台；2单体媒体资源；3专辑资源
    private String resId; //资源Id
    private DictModel dm;
    private DictDetail dd;
    private Timestamp CTime; //创建时间

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id=id;
    }
    public String getRefName() {
        return refName;
    }
    public void setRefName(String refName) {
        this.refName=refName;
    }
    public String getResTableName() {
        return resTableName;
    }
    public void setResTableName(String resTableName) {
        this.resTableName=resTableName;
    }
    public String getResId() {
        return resId;
    }
    public void setResId(String resId) {
        this.resId=resId;
    }
    public DictMaster getDm() {
        return dm;
    }
    public void setDm(DictModel dm) {
        if (dd!=null&&!dd.getMId().equals(dm.getId())) return;
        this.dm=dm;
    }
    public DictDetail getDd() {
        return dd;
    }
    public void setDd(DictDetail dd) {
        if (dm!=null&&!dm.getId().equals(dd.getMId())) return;
        this.dd=dd;
    }
    public Timestamp getCTime() {
        return CTime;
    }
    public void setCTime(Timestamp cTime) {
        CTime=cTime;
    }
	@Override
	public void buildFromPo(Object arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public Object convert2Po() {
		// TODO Auto-generated method stub
		return null;
	}
}