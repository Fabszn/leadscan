create table ADMIN_ACCOUNT (

 id BIGSERIAL not NULL,
 email_adress  character varying(255) NOT NULL,
 CONSTRAINT id_person_admin_pkey PRIMARY KEY (id)

)