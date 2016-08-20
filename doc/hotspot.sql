/**001 ץȡר����Ϣ*/
DROP TABLE IF EXISTS hotspot_Album;
CREATE TABLE hotspot_Album (
  id              varchar(32)      NOT NULL             COMMENT 'uuid(����)',
  albumId         varchar(32)      NOT NULL             COMMENT 'ר��Id',
  albumName       varchar(200)     NOT NULL             COMMENT 'ר����Դ����',
  albumPublisher  varchar(32)      NOT NULL             COMMENT '����������֯',
  albumImg        varchar(500)                          COMMENT 'ý��ͼ',
  albumTags       varchar(400)                          COMMENT '��ǩ',
  categoryId      varchar(32)      NOT NULL             COMMENT '����Id',
  categoryName    varchar(20)      NOT NULL             COMMENT '��������',
  descn           varchar(4000)                         COMMENT '˵��',
  visitUrl        varchar(800)                          COMMENT '������ַ',
  playCount       varchar(20)      NOT NULL             COMMENT '���Ŵ���',
  crawlerNum      varchar(32)                           COMMENT 'ץȡ���',
  schemeId        varchar(32)                           COMMENT '����Id',
  schemeName      varchar(32)                           COMMENT '��������',
  cTime           timestamp        NOT NULL  DEFAULT CURRENT_TIMESTAMP  COMMENT '����ʱ��',
  PRIMARY KEY(id)
)
ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='001ר����Դ';

/**002 ý����Դ*/
DROP TABLE IF EXISTS hotspot_Audio;
CREATE TABLE hotspot_Audio (
  id              varchar(32)      NOT NULL             COMMENT 'uuid(����)',
  audioId         varchar(32)      NOT NULL             COMMENT '��ԴId',
  audioName       varchar(200)     NOT NULL             COMMENT 'ý����Դ����',
  audioPublisher  varchar(100)     NOT NULL             COMMENT '������',
  audioImg        varchar(500)                          COMMENT 'ý��ͼ',
  audioURL        varchar(500)     NOT NULL             COMMENT 'ý������ַ�������Ǿۺϵ�Դ��Ҳ������Wtƽ̨�е��ļ�URL',
  audioTags       varchar(400)                          COMMENT '��ǩ',
  albumId         varchar(32)      NOT NULL             COMMENT '�ϼ�ר��Id',
  albumName       varchar(100)     NOT NULL             COMMENT '�ϼ�ר������',
  categoryId      varchar(32)      NOT NULL             COMMENT '����Id',
  categoryName    varchar(20)      NOT NULL             COMMENT '��������',
  duration        varchar(32)      NOT NULL             COMMENT '����ʱ��',
  visitUrl        varchar(800)                          COMMENT '������ַ',
  descn           varchar(4000)                         COMMENT '˵��',
  playCount       varchar(20)      NOT NULL             COMMENT '���Ŵ���',
  crawlerNum      varchar(32)      NOT NULL             COMMENT 'ץȡ���',
  schemeId        varchar(32)                           COMMENT '����Id',
  schemeName      varchar(32)                           COMMENT '��������',
  cTime           timestamp        NOT NULL  DEFAULT CURRENT_TIMESTAMP  COMMENT '����ʱ��',
  PRIMARY KEY(id)
)
ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='002ý����Դ';

/**C003 ץȡ�ֵ���[HOTSPOT_DICDM]*/
DROP TABLE IF EXISTS hotspot_DictM;
CREATE TABLE hotspot_DictM (
  id               varchar(32)      NOT NULL             COMMENT '�ֵ����ID(UUID)',
  dmName           varchar(200)     NOT NULL             COMMENT '�ֵ�������',
  nPy              varchar(800)     NOT NULL             COMMENT '����ƴ��',
  descn            varchar(500)                          COMMENT '˵��',
  cTime            timestamp        NOT NULL  DEFAULT CURRENT_TIMESTAMP  COMMENT '����ʱ��',
  PRIMARY KEY (id)
)
ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='C003ץȡ�ֵ���';

/**C004 ץȡ�ֵ���[HOTSPOT_DICTD]*/
DROP TABLE IF EXISTS hotspot_DictD;
CREATE TABLE hotspot_DictD (
  id               varchar(32)      NOT NULL             COMMENT '�ֵ����ID(UUID)',
  sourceId         varchar(32)      NOT NULL             COMMENT 'ԴId',
  mId              varchar(32)      NOT NULL             COMMENT '�ֵ������(UUID)',
  pId              varchar(32)      NOT NULL             COMMENT '�����ID(UUID)',
  publisher        varchar(20)      NOT NULL             COMMENT '����ƽ̨',
  ddName           varchar(200)     NOT NULL             COMMENT '�ֵ�������',
  nPy              varchar(800)     NOT NULL             COMMENT '����ƴ��',
  aliasName        varchar(200)                          COMMENT '�ֵ������',
  anPy             varchar(800)                          COMMENT '����ƴ��',
  visitUrl         varchar(800)                          COMMENT '������ַ',
  crawlerNum       varchar(32)      NOT NULL             COMMENT 'ץȡ���',
  schemeId         varchar(32)                           COMMENT '����Id',
  schemeName       varchar(32)                           COMMENT '��������',
  descn            varchar(500)                          COMMENT '˵��',
  cTime            timestamp        NOT NULL  DEFAULT CURRENT_TIMESTAMP  COMMENT '����ʱ��',
  PRIMARY KEY (id)
)
ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='C004ץȡ�ֵ���';

insert into hotspot_DictM(id,dmName,nPy,descn,cTime) values('3','���ݷ���','NeiRongFenLei','ץȡ���ݷ���','2016-08-16 17:23:13');

























