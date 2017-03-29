


CREATE TABLE PERSON (
    id   character varying(50) not NULL,
    json character varying(10000),
     CONSTRAINT id_PERSON_pkey PRIMARY KEY (id)
);

create table ADMIN_ACCOUNT (

 id BIGSERIAL not NULL,
 email_adress  character varying(255) NOT NULL,
 CONSTRAINT id_person_admin_pkey PRIMARY KEY (id)

);


CREATE TABLE LEAD (
 idApplicant character varying not NULL REFERENCES PERSON(id),
 idTarget character varying not NULL REFERENCES PERSON(id),
 dateTime timestamp NOT NULL,
 CONSTRAINT id_LEAD_pkey PRIMARY KEY (idApplicant,idTarget)
);


CREATE SEQUENCE lead_note_id_seq START 1;

CREATE TABLE LEAD_NOTE (
 id INT default nextval('lead_note_id_seq'),
 idApplicant character varying not NULL REFERENCES PERSON(id),
 idTarget character varying not NULL REFERENCES PERSON(id),
 note character varying(6000),
 dateTime timestamp NOT NULL,
 CONSTRAINT id_LEAD_NOTE_pkey PRIMARY KEY (id)
);


CREATE table NOTIFICATION_STATUS(
id   BIGSERIAL not NULL,
label character varying(255) NOT NULL,
CONSTRAINT id_NOTIFICATION_STATUS_pkey PRIMARY KEY (id)
);

CREATE table NOTIFICATION_TYPE(
id   BIGSERIAL not NULL,
label character varying(255) NOT NULL,
description character varying(255) NOT NULL,
CONSTRAINT id_NOTIFICATION_TYPE_pkey PRIMARY KEY (id)
);

CREATE SEQUENCE notification_id_seq START 1;

CREATE TABLE NOTIFICATION (
id INT  default nextval('notification_id_seq'),
idRecipient  character varying not NULL REFERENCES PERSON(id),
IdRequester character varying not NULL REFERENCES PERSON(id),
idType bigint NOT NULL REFERENCES NOTIFICATION_TYPE(id),
idStatus bigint NOT NULL REFERENCES NOTIFICATION_STATUS(id),
dateTime timestamp NOT NULL,
CONSTRAINT id_NOTIFICATION_pkey PRIMARY KEY (id)
);

CREATE SEQUENCE sponsor_id_seq START 1;

CREATE TABLE SPONSOR (
 id Int default nextval('sponsor_id_seq') ,
 name character varying(255) NOT NULL,
 level character varying(255) NOT NULL,
 CONSTRAINT id_sponsor_pkey PRIMARY KEY (id)
);

CREATE TABLE PERSON_SPONSOR (
 idPerson character varying not NULL REFERENCES PERSON(id),
 idSponsor Int not NULL REFERENCES SPONSOR(id),
 CONSTRAINT id_person_sponsor_pkey PRIMARY KEY (idPerson)
);

CREATE SEQUENCE events_id_seq START 1;

CREATE TABLE EVENTS (
 id INT default nextval('lead_note_id_seq'),
 type character varying(1000) not NULL ,
 message character varying(10000) NOT NULL,
 dateTime timestamp NOT NULL,
 CONSTRAINT id_event_pkey PRIMARY KEY (id)
);

CREATE SEQUENCE lead_pass_id_seq   START 1;

CREATE TABLE PASS (
 id INT default nextval('lead_pass_id_seq'),
 pass character varying(1000) not NULL ,
 regid character varying(10000) NOT NULL,
 dateTime timestamp default current_timestamp,
 CONSTRAINT id_pass_pkey PRIMARY KEY (id)
);