
CREATE TABLE document_tag (
	doc_id varchar(200) NOT NULL REFERENCES document(id),
	tag varchar(200) NOT NULL,
	PRIMARY KEY (doc_id, tag)
);
