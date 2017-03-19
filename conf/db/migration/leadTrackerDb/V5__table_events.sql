CREATE SEQUENCE events_id_seq START 1;

CREATE TABLE EVENTS (
 id INT default nextval('lead_note_id_seq'),
 type character varying(1000) not NULL ,
 message character varying(10000) NOT NULL,
 dateTime timestamp NOT NULL,
 CONSTRAINT id_event_pkey PRIMARY KEY (id)
);