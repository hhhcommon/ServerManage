/**001 抓取专辑信息*/
DROP TABLE IF EXISTS hotspot_Album;
CREATE TABLE hotspot_Album (
  id              varchar(32)      NOT NULL             COMMENT 'uuid(主键)',
  albumId         varchar(32)      NOT NULL             COMMENT '专辑Id',
  albumName       varchar(200)     NOT NULL             COMMENT '专辑资源名称',
  albumPublisher  varchar(32)      NOT NULL             COMMENT '发布所属组织',
  albumImg        varchar(500)                          COMMENT '媒体图',
  albumTags       varchar(400)                          COMMENT '标签',
  categoryId      varchar(32)      NOT NULL             COMMENT '分类Id',
  categoryName    varchar(20)      NOT NULL             COMMENT '分类名称',
  descn           varchar(4000)                         COMMENT '说明',
  visitUrl        varchar(800)                          COMMENT '访问网址',
  playCount       varchar(20)      NOT NULL             COMMENT '播放次数',
  crawlerNum      varchar(32)                           COMMENT '抓取序号',
  schemeId        varchar(32)                           COMMENT '方案Id',
  schemeName      varchar(32)                           COMMENT '方案名称',
  cTime           timestamp        NOT NULL  DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
  PRIMARY KEY(id)
)
ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='001专辑资源';

/**002 媒体资源*/
DROP TABLE IF EXISTS hotspot_Audio;
CREATE TABLE hotspot_Audio (
  id              varchar(32)      NOT NULL             COMMENT 'uuid(主键)',
  audioId         varchar(32)      NOT NULL             COMMENT '资源Id',
  audioName       varchar(200)     NOT NULL             COMMENT '媒体资源名称',
  audioPublisher  varchar(100)     NOT NULL             COMMENT '发布者',
  audioImg        varchar(500)                          COMMENT '媒体图',
  audioURL        varchar(500)     NOT NULL             COMMENT '媒体主地址，可以是聚合的源，也可以是Wt平台中的文件URL',
  audioTags       varchar(400)                          COMMENT '标签',
  albumId         varchar(32)      NOT NULL             COMMENT '上级专辑Id',
  albumName       varchar(100)     NOT NULL             COMMENT '上级专辑名称',
  categoryId      varchar(32)      NOT NULL             COMMENT '分类Id',
  categoryName    varchar(20)      NOT NULL             COMMENT '分类名称',
  duration        varchar(32)      NOT NULL             COMMENT '播放时长',
  visitUrl        varchar(800)                          COMMENT '访问网址',
  descn           varchar(4000)                         COMMENT '说明',
  playCount       varchar(20)      NOT NULL             COMMENT '播放次数',
  crawlerNum      varchar(32)      NOT NULL             COMMENT '抓取序号',
  schemeId        varchar(32)                           COMMENT '方案Id',
  schemeName      varchar(32)                           COMMENT '方案名称',
  cTime           timestamp        NOT NULL  DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
  PRIMARY KEY(id)
)
ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='002媒体资源';

/**C003 抓取字典组[HOTSPOT_DICDM]*/
DROP TABLE IF EXISTS hotspot_DictM;
CREATE TABLE hotspot_DictM (
  id               varchar(32)      NOT NULL             COMMENT '字典组表ID(UUID)',
  dmName           varchar(200)     NOT NULL             COMMENT '字典组名称',
  nPy              varchar(800)     NOT NULL             COMMENT '名称拼音',
  descn            varchar(500)                          COMMENT '说明',
  cTime            timestamp        NOT NULL  DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
  PRIMARY KEY (id)
)
ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='C003抓取字典组';

/**C004 抓取字典项[HOTSPOT_DICTD]*/
DROP TABLE IF EXISTS hotspot_DictD;
CREATE TABLE hotspot_DictD (
  id               varchar(32)      NOT NULL             COMMENT '字典项表ID(UUID)',
  sourceId         varchar(32)      NOT NULL             COMMENT '源Id',
  mId              varchar(32)      NOT NULL             COMMENT '字典组外键(UUID)',
  pId              varchar(32)      NOT NULL             COMMENT '父结点ID(UUID)',
  publisher        varchar(20)      NOT NULL             COMMENT '发布平台',
  ddName           varchar(200)     NOT NULL             COMMENT '字典项名称',
  nPy              varchar(800)     NOT NULL             COMMENT '名称拼音',
  aliasName        varchar(200)                          COMMENT '字典项别名',
  anPy             varchar(800)                          COMMENT '别名拼音',
  visitUrl         varchar(800)                          COMMENT '访问网址',
  crawlerNum       varchar(32)      NOT NULL             COMMENT '抓取序号',
  schemeId         varchar(32)                           COMMENT '方案Id',
  schemeName       varchar(32)                           COMMENT '方案名称',
  descn            varchar(500)                          COMMENT '说明',
  cTime            timestamp        NOT NULL  DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
  PRIMARY KEY (id)
)
ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='C004抓取字典项';

insert into hotspot_DictM(id,dmName,nPy,descn,cTime) values('3','内容分类','NeiRongFenLei','抓取内容分类','2016-08-16 17:23:13');

























