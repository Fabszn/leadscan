CREATE TABLE PROFIL_TYPE (
  id   BIGSERIAL not NULL,
  label character varying(255) NOT NULL,
  CONSTRAINT id_PROFIL_TYPE_pkey PRIMARY KEY (id)

);

CREATE TABLE PERSON (
    id   BIGSERIAL not NULL,
    firstname character varying(255) NOT NULL,
    lastname character varying(255) NOT NULL,
    gender character varying(1) NOT NULL,
    position character varying(255) NOT NULL,
    status character varying(255) NOT NULL,
    experience INTEGER,
    isTraining boolean NOT NULL,
    showSensitive boolean NOT NULL,
    profil bigint NOT NULL REFERENCES PROFIL_TYPE(id),
     CONSTRAINT id_PERSON_pkey PRIMARY KEY (id)
);

