
create table skills_cert_Priority (
dc_id integer not null auto_increment,
dc_priority integer,
dc_cert integer not null,
primary key (dc_id)
) engine=InnoDB;

create table skills_certificate (
skills_certificate_id integer not null auto_increment,
dc_status integer not null,
description varchar(255),
certificate_name varchar(255) not null,
issuer_id integer,
requested_from_id integer,
primary key (skills_certificate_id)
) engine=InnoDB;

create table skills_certificate_files (
certificate_id integer not null,
file_id integer not null
) engine=InnoDB;

create table skills_certificate_skills (
certificate_id integer not null,
skills_id integer not null
) engine=InnoDB;

create table skills_issuer (
skills_issuer_id integer not null auto_increment,
accreditation varchar(255),
address varchar(255),
comment varchar(255),
contact_email varchar(255),
contact_person varchar(255),
country varchar(255) not null,
homepage varchar(255),
issuer_name varchar(255) not null,
primary key (skills_issuer_id)
) engine=InnoDB;

create table skills_jobprofile (
dc_id integer not null auto_increment,
description varchar(255),
dc_name varchar(255) not null,
managedBy_dc_id integer,
primary key (dc_id)
) engine=InnoDB;

create table skills_ref_certificate_priority_jobProfile (
dc_id integer not null,
certificate_priority_id integer not null,
primary key (dc_id, certificate_priority_id)
) engine=InnoDB;

create table skills_ref_skills_jobProfile (
dc_id integer not null,
skills_level_id integer not null,
primary key (dc_id, skills_level_id)
) engine=InnoDB;

create table skills_skills (
skills_id integer not null auto_increment,
abbreviation varchar(255),
dc_status integer not null,
description varchar(255),
skills_name varchar(255),
obtainable bit not null,
parent_id integer,
requested_from_id integer,
primary key (skills_id)
) engine=InnoDB;

create table skills_skills_level (
skills_level_id integer not null auto_increment,
level integer not null,
dc_priority integer,
skills_id integer not null,
primary key (skills_level_id)
) engine=InnoDB;

create table skills_skills_user (
skills_user_id integer not null,
dc_availability integer not null,
availableFrom date,
description varchar(255),
disableNotifications bit not null,
receiveRequests bit not null,
skills_user_report_id integer,
primary key (skills_user_id)
) engine=InnoDB;

create table skills_user_certificates (
skills_user_certificate_id integer not null auto_increment,
comment varchar(255),
certificate_date date,
expiration_date date,
sendNotification integer,
certificate_status integer not null,
skills_certificate_id integer not null,
skills_user_id integer not null,
primary key (skills_user_certificate_id)
) engine=InnoDB;

create table skills_user_skill (
user_skill_id integer not null auto_increment,
skills_date date,
level integer not null,
skill_status integer not null,
skills_id integer not null,
skills_user_id integer not null,
primary key (user_skill_id)
) engine=InnoDB;

create table skills_userjobprofile (
dc_id integer not null auto_increment,
dc_date date,
dc_match integer,
dc_status integer not null,
jobprofile_id integer not null,
user_id integer not null,
primary key (dc_id)
) engine=InnoDB;

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
references skills_certificate (skills_certificate_id);

alter table skills_certificate
add constraint FK_CERTIFICATE_ISSUER
foreign key (issuer_id)
references skills_issuer (skills_issuer_id);

alter table skills_certificate
add constraint FK_CERTIFICATES_REQUESTEDFROM
foreign key (requested_from_id)
references core_user (dc_id);

alter table skills_certificate_files
add constraint FK_CERTIFICATE_FILE
foreign key (file_id)
references as_cloudsafe (dc_id);

alter table skills_certificate_files
add constraint FK_FILES_CERTIFICATE
foreign key (certificate_id)
references skills_user_certificates (skills_user_certificate_id);

alter table skills_certificate_skills
add constraint FK_CERTIFICATE_SKILLS
foreign key (skills_id)
references skills_skills (skills_id);

alter table skills_certificate_skills
add constraint FK_SKILLS_CERTIFICATE
foreign key (certificate_id)
references skills_certificate (skills_certificate_id);

alter table skills_jobprofile
add constraint FK_JOBPROFILE_USER
foreign key (managedBy_dc_id)
references core_user (dc_id);

alter table skills_ref_certificate_priority_jobProfile
add constraint FK_JOBPROFILE_CERTIFICATE
foreign key (certificate_priority_id)
references skills_cert_Priority (dc_id);

alter table skills_ref_certificate_priority_jobProfile
add constraint FK_CERTIFICATE__PRIORITY_JOBPROFILE
foreign key (dc_id)
references skills_jobprofile (dc_id);

alter table skills_ref_skills_jobProfile
add constraint FK_JOBPROFILE_SKILLS
foreign key (skills_level_id)
references skills_skills_level (skills_level_id);

alter table skills_ref_skills_jobProfile
add constraint FK_SKILLS_JOBPROFILE
foreign key (dc_id)
references skills_jobprofile (dc_id);

alter table skills_skills
add constraint FK_SKILLS_PARENT
foreign key (parent_id)
references skills_skills (skills_id);

alter table skills_skills
add constraint FK_SKILLS_REQUESTEDFROM
foreign key (requested_from_id)
references core_user (dc_id);

alter table skills_skills_level
add constraint FK_SKILLS
foreign key (skills_id)
references skills_skills (skills_id);

alter table skills_skills_user
add constraint FK_SKILLS_REPORT_USER
foreign key (skills_user_report_id)
references core_user (dc_id);

alter table skills_skills_user
add constraint FK_SKILLS_USER
foreign key (skills_user_id)
references core_user (dc_id);

alter table skills_user_certificates
add constraint FK_CERTIFICATE_USERCERTIFICATE
foreign key (skills_certificate_id)
references skills_certificate (skills_certificate_id);

alter table skills_user_certificates
add constraint FK_CERTIFICATE_SKILLSUSER
foreign key (skills_user_id)
references skills_skills_user (skills_user_id);

alter table skills_user_skill
add constraint FK_SKILLS_SKILL_OF_USERSKILL
foreign key (skills_id)
references skills_skills (skills_id);

alter table skills_user_skill
add constraint FK_SKILLS_USER_OF_USERSKILL
foreign key (skills_user_id)
references skills_skills_user (skills_user_id);

alter table skills_userjobprofile
add constraint FK_SKILLS_JOBPROFILE_USER
foreign key (jobprofile_id)
references skills_jobprofile (dc_id);

alter table skills_userjobprofile
add constraint FK_SKILLS_USERJOBPROFILE
foreign key (user_id)
references skills_skills_user (skills_user_id);
