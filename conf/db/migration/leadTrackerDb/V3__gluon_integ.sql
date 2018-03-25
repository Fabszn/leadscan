
ALTER TABLE ADMIN_ACCOUNT ADD PASS character varying(50) not NULL;

ALTER TABLE SPONSOR ADD SLUG character varying(50) not NULL;


insert  into admin_account  (id, email_adress,pass) values (1, 'admin@devoxx.fr', '1234');

