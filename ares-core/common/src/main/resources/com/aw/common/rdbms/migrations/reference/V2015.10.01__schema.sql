create table document(
	tenant_id varchar(200) NOT NULL,
	id  varchar(200),
	version  BIGINT NOT NULL DEFAULT 1,
	 is_current  SMALLINT NOT NULL default 1,
	author varchar(200) NOT NULL,
	type varchar(200) NOT NULL,
	body_class varchar(200) ,
	description varchar(1000),
	name varchar(200) NOT NULL ,
	display_name varchar(200),
	body CLOB,
	 version_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	version_author varchar(200)  NOT NULL ,
	deleted smallint NOT NULL DEFAULT 0,   -- no bit data type in derby
	grouping varchar(1000) ,
	perm_read varchar(200) NOT NULL DEFAULT 'author',
	perm_write varchar(200) NOT NULL DEFAULT 'author',
	dependencies_guid varchar(1000) ,
	dependencies_name varchar(1000),
	PRIMARY KEY (id)
);

CREATE UNIQUE INDEX DOCUMENT_U1 on document (TENANT_ID, TYPE, NAME, VERSION);

CREATE TABLE PATCH_LEVEL (patch_level BIGINT);