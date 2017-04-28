/**抓取专辑信息*/
DROP TABLE IF EXISTS c_Album;
CREATE TABLE c_Album (
  id              varchar(50)      NOT NULL             COMMENT 'uuid(主键)',
  albumId         varchar(32)      NOT NULL             COMMENT '专辑Id',
  albumName       varchar(200)     NOT NULL             COMMENT '专辑资源名称',
  albumPublisher  varchar(32)      NOT NULL             COMMENT '发布所属组织',
  albumImg        varchar(500)                          COMMENT '媒体图',
  albumTags       varchar(400)                          COMMENT '标签',
  descn           MEDIUMTEXT                            COMMENT '说明',
  visitUrl        varchar(800)                          COMMENT '访问网址',
  isValidate      int unsigned  NOT NULL  DEFAULT 1     COMMENT '',
  pubTime         timestamp        NOT NULL  DEFAULT CURRENT_TIMESTAMP  COMMENT '发布时间',
  cTime           timestamp        NOT NULL  DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
  UNIQUE INDEX dataIdx (albumId, albumPublisher) USING HASH,
  PRIMARY KEY(id)
)
ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='抓取专辑资源';

/**媒体资源*/
DROP TABLE IF EXISTS c_Audio;
CREATE TABLE c_Audio (
  id              varchar(50)      NOT NULL             COMMENT 'uuid(主键)',
  audioId         varchar(32)      NOT NULL             COMMENT '资源Id',
  audioName       varchar(500)     NOT NULL             COMMENT '媒体资源名称',
  audioPublisher  varchar(100)     NOT NULL             COMMENT '发布所属组织',
  audioImg        varchar(500)                          COMMENT '媒体图',
  audioURL        varchar(500)     NOT NULL             COMMENT '媒体主地址',
  audioTags       varchar(400)                          COMMENT '标签',
  duration        varchar(32)      NOT NULL             COMMENT '播放时长',
  visitUrl        varchar(800)                          COMMENT '访问网址',
  descn           MEDIUMTEXT                            COMMENT '说明',
  pubTime         timestamp        NOT NULL  DEFAULT CURRENT_TIMESTAMP  COMMENT '发布时间',
  cTime           timestamp        NOT NULL  DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
  UNIQUE INDEX dataIdx (audioId, audioPublisher) USING HASH,
  PRIMARY KEY(id)
)
ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='抓取媒体资源';

/** C_ALBUMAUDIO_REF(专辑节目关系表)*/
DROP TABLE IF EXISTS c_AlbumAudio_Ref;
CREATE TABLE c_AlbumAudio_Ref (
  id            varchar(32)    NOT NULL               COMMENT 'uuid',
  alId          varchar(50)    NOT NULL               COMMENT '专辑Id',
  auId          varchar(50)    NOT NULL               COMMENT '节目Id',
  columnNum     int  unsigned                         COMMENT '卷集号，也是排序号',
  isMain        int(1) unsigned  NOT NULL  DEFAULT 0  COMMENT '是否主专辑(1-是,0-转采)',
  cTime         timestamp      NOT NULL  DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间:创建时的系统时间',
  UNIQUE INDEX dataIdx (alId, auId) USING HASH,
  PRIMARY KEY(id)
)
ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='专辑节目关系表';

/**抓取字典组[C_DICDM]*/
DROP TABLE IF EXISTS c_DictM;
CREATE TABLE c_DictM (
  id               varchar(32)      NOT NULL             COMMENT '字典组表ID(UUID)',
  dmName           varchar(200)     NOT NULL             COMMENT '字典组名称',
  nPy              varchar(800)     NOT NULL             COMMENT '名称拼音',
  descn            varchar(500)                          COMMENT '说明',
  cTime            timestamp        NOT NULL  DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
  PRIMARY KEY (id)
)
ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='抓取字典组';

/**抓取字典项[C_DICTD]*/
DROP TABLE IF EXISTS c_DictD;
CREATE TABLE c_DictD (
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
  crawlerNum       int              NOT NULL  DEFAULT 1  COMMENT '抓取序号',
  schemeId         varchar(32)                           COMMENT '方案Id',
  schemeName       varchar(32)                           COMMENT '方案名称',
  isValidate       int(1) unsigned  NOT NULL  DEFAULT 1  COMMENT '是否生效(1-生效,2-无效)',
  descn            varchar(500)                          COMMENT '说明',
  cTime            timestamp        NOT NULL  DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
  PRIMARY KEY (id)
)
ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='抓取字典项';

insert into c_DictM(id,dmName,nPy,descn) values('3','内容分类','NeiRongFenLei','抓取内容分类');


/**== 抓取主播信息表 =============================================*/
/** C_PERSON(主播信息表)*/
DROP TABLE IF EXISTS c_Person;
CREATE TABLE c_Person (
  id            varchar(50)      NOT NULL                COMMENT 'uuid(用户id)',
  pName         varchar(100)     NOT NULL                COMMENT '名称',
  pSource       varchar(30)      NOT NULL                COMMENT '来源名称',
  pSrcId        varchar(50)      NOT NULL                COMMENT '所属来源的主播id',
  age           varchar(15)                              COMMENT '年龄',
  birthday      varchar(30)                              COMMENT '生日',
  sex           int unsigned     NOT NULL  DEFAULT 0     COMMENT '主播性别，0-保密，1-男性，2-女性',
  constellation varchar(10)                              COMMENT '星座',
  location      varchar(30)                              COMMENT '主播所属地区',
  descn         varchar(1000)              DEFAULT NULL  COMMENT '主播描述',
  signature     varchar(1000)              DEFAULT NULL  COMMENT '主播签名',
  pTitle        varchar(1000)              DEFAULT NULL  COMMENT '主播头衔',
  phoneNum      varchar(100)               DEFAULT NULL  COMMENT '主播手机',
  email         varchar(100)               DEFAULT NULL  COMMENT 'eMail',
  pSrcHomePage  varchar(100)                             COMMENT '来源平台里个人主页',
  portrait      varchar(300)                             COMMENT '主播头像',
  isVerified    int unsigned     NOT NULL  DEFAULT 2     COMMENT '主播认证 1-已认证，2-未认证',
  cTime         timestamp        NOT NULL  DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间:创建时的系统时间',
  UNIQUE INDEX dataIdx (pSrcId, pSource) USING HASH,
  PRIMARY KEY(id)
)
ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='主播信息表';


/** C_PERSON_REF(主播对应关系)*/
DROP TABLE IF EXISTS c_Person_Ref;
CREATE TABLE c_Person_Ref (
  id            varchar(32)    NOT NULL                COMMENT 'uuid',
  refName       varchar(30)    NOT NULL                COMMENT '关系名称，例如主播-节目，主播-专辑',
  personId      varchar(50)    NOT NULL                COMMENT '用户Id',
  resTableName  varchar(50)    NOT NULL                COMMENT '资源类型表名',
  resId         varchar(50)    NOT NULL                COMMENT '资源Id',
  cTime         timestamp      NOT NULL  DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间:创建时的系统时间',
  UNIQUE INDEX dataIdx (personId, resTableName,resId) USING HASH,
  PRIMARY KEY(id)
)
ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='主播对应关系';


/** 播放次数记录表[C_PLAYCOUNT]*/
DROP TABLE IF EXISTS c_PlayCount;
CREATE TABLE c_PlayCount (
  id             varchar(32)      NOT NULL             COMMENT 'UUID',
  resId          varchar(50)      NOT NULL             COMMENT '外部平台对应内容Id',
  resTableName   varchar(20)      NOT NULL             COMMENT '内容类型表名',
  publisher      varchar(32)      NOT NULL             COMMENT '发布所属组织',
  playCount      bigint unsigned  NOT NULL  DEFAULT 0  COMMENT '播放次数',
  cTime          timestamp        NOT NULL  DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
  PRIMARY KEY (id)
)
ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='播放次数记录表';

/** 订阅数记录表[C_SUBSCRIBE]*/
DROP TABLE IF EXISTS c_Subscribe;
CREATE TABLE c_Subscribe (
  id             varchar(32)      NOT NULL             COMMENT 'UUID',
  resId          varchar(50)      NOT NULL             COMMENT '外部平台对应内容Id',
  resTableName   varchar(20)      NOT NULL             COMMENT '内容类型表名',
  publisher      varchar(32)      NOT NULL             COMMENT '发布所属组织',
  subscribeCount bigint unsigned  NOT NULL  DEFAULT 0  COMMENT '订阅次数',
  cTime          timestamp        NOT NULL  DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
  PRIMARY KEY (id)
)
ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='订阅数记录表';

/** 喜欢数记录表[C_FAVORITE]*/
DROP TABLE IF EXISTS c_Favorite;
CREATE TABLE c_Favorite (
  id             varchar(32)      NOT NULL             COMMENT 'UUID',
  resId          varchar(50)      NOT NULL             COMMENT '外部平台对应内容Id',
  resTableName   varchar(20)      NOT NULL             COMMENT '内容类型表名',
  Publisher      varchar(32)      NOT NULL             COMMENT '发布所属组织',
  favoriteCount  bigint unsigned  NOT NULL  DEFAULT 0  COMMENT '喜欢次数',
  cTime          timestamp        NOT NULL  DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
  PRIMARY KEY (id)
)
ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='喜欢数记录表';

/** 评论数记录表[C_COMMENT]*/
DROP TABLE IF EXISTS c_Comment;
CREATE TABLE c_Comment (
  id             varchar(32)      NOT NULL             COMMENT 'UUID',
  resId          varchar(50)      NOT NULL             COMMENT '外部平台对应内容Id',
  resTableName   varchar(20)      NOT NULL             COMMENT '内容类型表名',
  publisher      varchar(32)      NOT NULL             COMMENT '发布所属组织',
  commentCount   bigint unsigned  NOT NULL  DEFAULT 0  COMMENT '评论次数',
  cTime          timestamp        NOT NULL  DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
  PRIMARY KEY (id)
)
ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='评论数记录表';

/** 转采数记录表[C_FORWARD]*/
DROP TABLE IF EXISTS c_Forward;
CREATE TABLE c_Forward (
  id             varchar(32)      NOT NULL             COMMENT 'UUID',
  resId          varchar(50)      NOT NULL             COMMENT '外部平台对应内容Id',
  resTableName   varchar(20)      NOT NULL             COMMENT '内容类型表名',
  publisher      varchar(32)      NOT NULL             COMMENT '发布所属组织',
  forwardCount   bigint unsigned  NOT NULL  DEFAULT 0  COMMENT '转采次数',
  cTime          timestamp        NOT NULL  DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
  PRIMARY KEY (id)
)
ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='转采数记录表';

/** 抓取方案[C_SCHEME]*/
DROP TABLE IF EXISTS c_Scheme;
CREATE TABLE c_Scheme (
  id             varchar(32)   NOT NULL                             COMMENT '表ID(UUID)',
  schemeName     varchar(32)   NOT NULL                             COMMENT '方案名称',
  schemeDescn    varchar(100)  NOT NULL                             COMMENT '方案描述',
  crawlType      int unsigned  NOT NULL  DEFAULT 0                  COMMENT '抓取循环类型；1=只抓取1次；0=定时抓取',
  crawlDescn     varchar(100)  NOT NULL                             COMMENT '抓取循环描述',
  processNum     int unsigned  NOT NULL  DEFAULT 0                  COMMENT '抓取批次',
  cTime          timestamp     NOT NULL  DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
  endTime        timestamp                                          COMMENT '结束时间',
  PRIMARY KEY (id)
)
ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='抓取方案';

/** 图片Hash表[C_IMAGEHASHCODE]*/
DROP TABLE IF EXISTS c_ImageHashCode;
CREATE TABLE c_ImageHashCode (
  id             varchar(32)   NOT NULL                             COMMENT 'HashCode值',
  imageSrcPath   varchar(500)  NOT NULL                             COMMENT '原始图片路径',
  imagePath      varchar(500)  NOT NULL                             COMMENT '图片存储路径',
  purpose        varchar(10)   NOT NULL                             COMMENT '图片用途',
  cTime          timestamp     NOT NULL  DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
  PRIMARY KEY (id)
)
ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='图片Hash表';

/** C_RESDICT_REF(分类对应关系)*/
/**这里的关系时值res到dict之间的关联关系，是有向的*/
DROP TABLE IF EXISTS c_ResDict_Ref;
CREATE TABLE c_ResDict_Ref (
  id            varchar(32)    NOT NULL  COMMENT 'uuid(主键)',
  resTableName  varchar(200)   NOT NULL  COMMENT '资源类型Id：1电台；2单体媒体资源；3专辑资源',
  resId         varchar(50)    NOT NULL  COMMENT '资源Id',
  cdictMid      varchar(32)    NOT NULL  COMMENT '字典组Id',
  cdictDid      varchar(32)    NOT NULL  COMMENT '字典项Id',
  cTime         timestamp      NOT NULL  DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
  UNIQUE INDEX dataIdx (resTableName, resId, cdictMid, cdictDid) USING HASH,
  PRIMARY KEY(id)
)
ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='抓取字典项对应关系';

INSERT INTO c_DictD SELECT * FROM crawlerDB.c_DictD;