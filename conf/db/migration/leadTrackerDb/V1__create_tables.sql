CREATE TABLE PROFIL_TYPE (
  id   BIGSERIAL not NULL,
  label character varying(255) NOT NULL,
  CONSTRAINT id_PROFIL_TYPE_pkey PRIMARY KEY (id)

);

CREATE TABLE PERSON_SENSITIVE (
    id   BIGSERIAL not NULL,
    email character varying(100) NOT NULL,
    phoneNumber character varying(255) NOT NULL,
    company character varying(255) NOT NULL,
    workLocation character varying(255) NOT NULL,
    lookingForAJob boolean NOT NULL,
     CONSTRAINT id_PERSON_SENSITIVE_pkey PRIMARY KEY (id)
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
    profilid bigint NOT NULL REFERENCES PROFIL_TYPE(id),
    sensitiveId bigint NOT NULL REFERENCES PERSON_SENSITIVE(id),
     CONSTRAINT id_PERSON_pkey PRIMARY KEY (id)
);


CREATE TABLE LEAD (
 idApplicant BIGSERIAL not NULL REFERENCES PERSON(id),
 idTarget BIGSERIAL not NULL REFERENCES PERSON(id),
 CONSTRAINT id_LEAD_pkey PRIMARY KEY (idApplicant,idTarget)
);


