CREATE SEQUENCE lead_pass_id_seq   START 1;

CREATE TABLE PASS (
 id INT default nextval('lead_pass_id_seq'),
 pass character varying(1000) not NULL ,
 regid character varying(10000) NOT NULL,
 dateTime timestamp default current_timestamp,
 CONSTRAINT id_pass_pkey PRIMARY KEY (id)
);