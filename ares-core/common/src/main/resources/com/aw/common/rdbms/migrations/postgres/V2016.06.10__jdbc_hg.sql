
CREATE TABLE GAMEEVENT (
	eventid varchar(200),
	alarmActive varchar(200),
	alarmCode varchar(200),
	alarmCodeDesc varchar(200),
	PRIMARY KEY (eventid)

);

CREATE TABLE alarmcodedescriptions (
  ref_key varchar(200),
  ref_value varchar (200)
);

insert into alarmcodedescriptions values ('12','foober');