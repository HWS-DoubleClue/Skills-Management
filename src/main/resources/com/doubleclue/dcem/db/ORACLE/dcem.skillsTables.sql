
create table skills_cert_Priority (
dc_id number(10,0) generated as identity,
dc_priority number(10,0),
dc_cert number(10,0) not null,
primary key (dc_id)
);

create table skills_certificate (
skills_certificate_id number(10,0) generated as identity,
dc_status number(10,0) not null,
description varchar2(255 char),
certificate_name varchar2(255 char) not null,
issuer_id number(10,0),
requested_from_id number(10,0),
primary key (skills_certificate_id)
);

create table skills_certificate_files (
certificate_id number(10,0) not null,
file_id number(10,0) not null
);

create table skills_certificate_skills (
certificate_id number(10,0) not null,
skills_id number(10,0) not null
);

create table skills_issuer (
skills_issuer_id number(10,0) generated as identity,
accreditation varchar2(255 char),
address varchar2(255 char),
comment varchar2(255 char),
contact_email varchar2(255 char),
contact_person varchar2(255 char),
country varchar2(255 char) not null,
homepage varchar2(255 char),
issuer_name varchar2(255 char) not null,
primary key (skills_issuer_id)
);

create table skills_jobprofile (
dc_id number(10,0) generated as identity,
description varchar2(255 char),
dc_name varchar2(255 char) not null,
managedBy_dc_id number(10,0),
primary key (dc_id)
);

create table skills_ref_certificate_priority_jobProfile (
dc_id number(10,0) not null,
certificate_priority_id number(10,0) not null,
primary key (dc_id, certificate_priority_id)
);

create table skills_ref_skills_jobProfile (
dc_id number(10,0) not null,
skills_level_id number(10,0) not null,
primary key (dc_id, skills_level_id)
);

create table skills_skills (
skills_id number(10,0) generated as identity,
abbreviation varchar2(255 char),
description varchar2(255 char),
skills_name varchar2(255 char),
obtainable number(1,0) not null,
dc_status number(10,0) not null,
parent_id number(10,0),
requested_from_id number(10,0),
primary key (skills_id)
);

create table skills_skills_level (
skills_level_id number(10,0) generated as identity,
level number(10,0) not null,
dc_priority number(10,0),
skills_id number(10,0) not null,
primary key (skills_level_id)
);

create table skills_skills_user (
skills_user_id number(10,0) not null,
dc_availability number(10,0) not null,
availableFrom date,
description varchar2(255 char),
skills_user_report_id number(10,0),
primary key (skills_user_id)
);

create table skills_user_certificates (
skills_user_certificate_id number(10,0) generated as identity,
comment varchar2(255 char),
certificate_date date,
expiration_date date,
sendNotification number(10,0),
certificate_status number(10,0) not null,
skills_certificate_id number(10,0) not null,
skills_user_id number(10,0) not null,
primary key (skills_user_certificate_id)
);

create table skills_user_skill (
user_skill_id number(10,0) generated as identity,
skills_date date,
level number(10,0) not null,
skill_status number(10,0) not null,
skills_id number(10,0) not null,
skills_user_id number(10,0) not null,
primary key (user_skill_id)
);

create table skills_userjobprofile (
dc_id number(10,0) generated as identity,
dc_date date,
dc_match number(10,0),
dc_status number(10,0) not null,
jobprofile_id number(10,0) not null,
user_id number(10,0) not null,
primary key (dc_id)
);

alter table skills_cert_Priority
add constraint UK_SKILLS_CERTIFICATE_PRIORITY unique (dc_cert, dc_priority);

alter table skills_certificate
add constraint UK_CERTIFICATE_NAME unique (certificate_name);

alter table skills_issuer
add constraint UK_ISSUER_NAME unique (issuer_name);

alter table skills_jobprofile
add constraint UK_JOBPROFILE_NAME unique (dc_name);

alter table skills_skills
add constraint UK_SKILLS_NAME unique (skills_name, parent_id);

alter table skills_skills
add constraint UK_SKILLS_ABBR unique (abbreviation);

alter table skills_skills_level
add constraint UK_SKILLS_SKILLSLEVEL unique (skills_id, level, dc_priority);

alter table skills_user_certificates
add constraint UK_SKILLS_USERCERTIFICATES unique (skills_user_id, skills_certificate_id);

alter table skills_user_skill
add constraint UK_SKILLS_USER_SKILL unique (skills_user_id, skills_id, level);

alter table skills_userjobprofile
add constraint UK_SKILLS_USERJOBPROFILES unique (user_id, jobprofile_id);

alter table skills_cert_Priority
add constraint FK_SKILLS_CERTIFICATE_PRIO
foreign key (dc_cert)
references skills_certificate;

alter table skills_certificate
add constraint FK_CERTIFICATE_ISSUER
foreign key (issuer_id)
references skills_issuer;

alter table skills_certificate
add constraint FK_CERTIFICATES_REQUESTEDFROM
foreign key (requested_from_id)
references core_user;

alter table skills_certificate_files
add constraint FK_CERTIFICATE_FILE
foreign key (file_id)
references as_cloudsafe;

alter table skills_certificate_files
add constraint FK_FILES_CERTIFICATE
foreign key (certificate_id)
references skills_user_certificates;

alter table skills_certificate_skills
add constraint FK_CERTIFICATE_SKILLS
foreign key (skills_id)
references skills_skills;

alter table skills_certificate_skills
add constraint FK_SKILLS_CERTIFICATE
foreign key (certificate_id)
references skills_certificate;

alter table skills_jobprofile
add constraint FK_JOBPROFILE_USER
foreign key (managedBy_dc_id)
references core_user;

alter table skills_ref_certificate_priority_jobProfile
add constraint FK_JOBPROFILE_CERTIFICATE
foreign key (certificate_priority_id)
references skills_cert_Priority;

alter table skills_ref_certificate_priority_jobProfile
add constraint FK_CERTIFICATE__PRIORITY_JOBPROFILE
foreign key (dc_id)
references skills_jobprofile;

alter table skills_ref_skills_jobProfile
add constraint FK_JOBPROFILE_SKILLS
foreign key (skills_level_id)
references skills_skills_level;

alter table skills_ref_skills_jobProfile
add constraint FK_SKILLS_JOBPROFILE
foreign key (dc_id)
references skills_jobprofile;

alter table skills_skills
add constraint FK_SKILLS_PARENT
foreign key (parent_id)
references skills_skills;

alter table skills_skills
add constraint FK_SKILLS_REQUESTEDFROM
foreign key (requested_from_id)
references core_user;

alter table skills_skills_level
add constraint FK_SKILLS
foreign key (skills_id)
references skills_skills;

alter table skills_skills_user
add constraint FK_SKILLS_REPORT_USER
foreign key (skills_user_report_id)
references core_user;

alter table skills_skills_user
add constraint FK_SKILLS_USER
foreign key (skills_user_id)
references core_user;

alter table skills_user_certificates
add constraint FK_CERTIFICATE_USERCERTIFICATE
foreign key (skills_certificate_id)
references skills_certificate;

alter table skills_user_certificates
add constraint FK_CERTIFICATE_SKILLSUSER
foreign key (skills_user_id)
references skills_skills_user;

alter table skills_user_skill
add constraint FK_SKILLS_SKILL_OF_USERSKILL
foreign key (skills_id)
references skills_skills;

alter table skills_user_skill
add constraint FK_SKILLS_USER_OF_USERSKILL
foreign key (skills_user_id)
references skills_skills_user;

alter table skills_userjobprofile
add constraint FK_SKILLS_JOBPROFILE_USER
foreign key (jobprofile_id)
references skills_jobprofile;

alter table skills_userjobprofile
add constraint FK_SKILLS_USERJOBPROFILE
foreign key (user_id)
references skills_skills_user;
