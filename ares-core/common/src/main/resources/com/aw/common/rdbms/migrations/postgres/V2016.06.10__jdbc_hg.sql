
CREATE TABLE GAMEEVENT (
	eventid varchar(200),
	alarmActive int,
	alarmCode int,
	alarmCodeDesc varchar(200),
	someDouble DECIMAL,
	someBigInt DECIMAL,
	someTimestamp TIMESTAMP,
	defaulted_ts TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	PRIMARY KEY (eventid)

);

CREATE TABLE alarmcodedescriptions (
  ref_key varchar(200),
  ref_value varchar (200)
);

insert into alarmcodedescriptions values ('12','foober');