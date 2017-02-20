insert into profil_type (id, label) values (1,'attendee');
insert into profil_type (id, label) values (2,'organisateur');
insert into profil_type (id, label) values (3,'sponsor');

insert into notification_status (id, label) values (1,'unread');
insert into notification_status (id, label) values (2,'read');

insert into notification_type (id, label, description) values (1,'Connected', 'You are connected with new contact');
insert into notification_type (id, label, description) values (2,'request_for_update', 'One person ask you to update your data');




/*INSERT INTO public.person
(id, firstname, lastname, gender, "position", status, experience, istraining, showsensitive, profilId)
VALUES(nextval('person_id_seq'::regclass), 'Fabrice', 'Sznajderman', 'M', 'developer', 'contractor', 16, false, true, 1);

INSERT INTO public.person
(id, firstname, lastname, gender, "position", status, experience, istraining, showsensitive, profilId)
VALUES(nextval('person_id_seq'::regclass), 'Antonio', 'Goncalvez', 'M', 'CTO', 'contractor', 15, false, true, 2);

INSERT INTO public.person
(id, firstname, lastname, gender, "position", status, experience, istraining, showsensitive, profilId)
VALUES(nextval('person_id_seq'::regclass), 'Maxime', 'Sznajderman', 'M', 'CTO', 'contractor', 2, false, true, 2);



INSERT INTO public.person_sensitive
(id, email, phonenumber, company, worklocation, lookingforajob)
VALUES(1, 'fabrice@gmail.com', '0102030405', 'zenika', 'Paris', true);

INSERT INTO public.person_sensitive
(id, email, phonenumber, company, worklocation, lookingforajob)
VALUES(2, 'antonio@gmail.com', '0504030201', 'allcraft', 'Paris', false);

INSERT INTO public.person_sensitive
(id, email, phonenumber, company, worklocation, lookingforajob)
VALUES(3, 'maxime@gmail.com', '1010101010', 'notAtAll', 'Strasbourg', false);
*/