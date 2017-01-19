insert into profil_type (id, label) values (1,'attendee');
insert into profil_type (id, label) values (2,'organisateur');
insert into profil_type (id, label) values (3,'sponsor');


INSERT INTO public.person
(id, firstname, lastname, gender, "position", status, experience, istraining, showsensitive, profil)
VALUES(nextval('person_id_seq'::regclass), 'Fabrice', 'Sznajderman', 'M', 'developer', 'contractor', 16, false, true, 1);

INSERT INTO public.person
(id, firstname, lastname, gender, "position", status, experience, istraining, showsensitive, profil)
VALUES(nextval('person_id_seq'::regclass), 'Antonio', 'Goncalvez', 'M', 'CTO', 'contractor', 15, false, true, 2);