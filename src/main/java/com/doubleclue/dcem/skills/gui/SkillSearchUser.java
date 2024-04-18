package com.doubleclue.dcem.skills.gui;

import java.util.List;

import com.doubleclue.dcem.skills.entities.SkillsUserEntity;
import com.doubleclue.dcem.skills.entities.SkillsUserSkillEntity;

public class SkillSearchUser {

	private SkillsUserEntity skillsUserEntity;
	private List<SkillsUserSkillEntity> skills;
	private String certificates;
	private String jobProfiles;

	public SkillSearchUser() {

	}

	public SkillSearchUser(SkillsUserEntity skillsUserEntity, List<SkillsUserSkillEntity> skills, String certificates, String jobProfiles) {
		this.skillsUserEntity = skillsUserEntity;
		this.skills = skills;
		this.certificates = certificates;
		this.jobProfiles = jobProfiles;
	}

	public SkillsUserEntity getSkillsUserEntity() {
		return skillsUserEntity;
	}

	public void setSkillsUserEntity(SkillsUserEntity skillsUserEntity) {
		this.skillsUserEntity = skillsUserEntity;
	}

	public String getCertificates() {
		certificates = certificates.replace("[", "").replace("]", "");
		return certificates;
	}

	public void setCertificates(String certificates) {
		this.certificates = certificates;
	}

	public String getJobProfiles() {
		jobProfiles = jobProfiles.replace("[", "").replace("]", "");

		return jobProfiles;
	}

	public void setJobProfiles(String jobProfiles) {
		this.jobProfiles = jobProfiles;
	}

	public List<SkillsUserSkillEntity> getSkills() {
		return skills;
	}

	public void setSkills(List<SkillsUserSkillEntity> skills) {
		this.skills = skills;
	}

}
