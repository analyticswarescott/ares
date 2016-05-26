
CREATE TABLE op_sequence (
	doc_id varchar(200) NOT NULL REFERENCES document(id),
	op_sequence_key varchar(200) NOT NULL,
	op_sequence numeric NOT NULL,
	PRIMARY KEY (op_sequence_key, op_sequence)
);
