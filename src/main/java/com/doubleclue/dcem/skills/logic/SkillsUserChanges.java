package com.doubleclue.dcem.skills.logic;

import java.util.List;

import com.doubleclue.dcem.skills.entities.SkillsUserCertificateEntity;
import com.doubleclue.dcem.skills.entities.SkillsUserEntity;
import com.doubleclue.dcem.skills.entities.SkillsUserJobProfileEntity;
import com.doubleclue.dcem.skills.entities.SkillsUserSkillEntity;

public class SkillsUserChanges {

	private SkillsUserEntity skillsUser;
	private List<SkillsUserSkillEntity> oldSkills;
	private List<SkillsUserSkillEntity> newSkills;
	private List<SkillsUserCertificateEntity> oldCertificateEntities;
	private List<SkillsUserCertificateEntity> newCertificateEntities;
	private List<SkillsUserJobProfileEntity> oldJobProfiles;
	private List<SkillsUserJobProfileEntity> newJobProfiles;

	public SkillsUserChanges() {
	}

	public SkillsUserChanges(SkillsUserEntity skillsUser) {
		this.skillsUser = skillsUser;
	}

	public List<SkillsUserSkillEntity> getOldSkills() {
		return oldSkills;
	}

	public void setOldSkills(List<SkillsUserSkillEntity> oldSkills) {
		this.oldSkills = oldSkills;
	}

	public List<SkillsUserSkillEntity> getNewSkills() {
		return newSkills;
	}

	public void setNewSkills(List<SkillsUserSkillEntity> newSkills) {
		this.newSkills = newSkills;
	}

	public List<SkillsUserCertificateEntity> getOldCertificateEntities() {
		return oldCertificateEntities;
	}

	public void setOldCertificateEntities(List<SkillsUserCertificateEntity> oldCertificateEntities) {
		this.oldCertificateEntities = oldCertificateEntities;
	}

	public List<SkillsUserCertificateEntity> getNewCertificateEntities() {
		return newCertificateEntities;
	}

	public void setNewCertificateEntities(List<SkillsUserCertificateEntity> newCertificateEntities) {
		this.newCertificateEntities = newCertificateEntities;
	}

	public List<SkillsUserJobProfileEntity> getOldJobProfiles() {
		return oldJobProfiles;
	}

	public void setOldJobProfiles(List<SkillsUserJobProfileEntity> oldJobProfiles) {
		this.oldJobProfiles = oldJobProfiles;
	}

	public List<SkillsUserJobProfileEntity> getNewJobProfiles() {
		return newJobProfiles;
	}

	public void setNewJobProfiles(List<SkillsUserJobProfileEntity> newJobProfiles) {
		this.newJobProfiles = newJobProfiles;
	}

	public SkillsUserEntity getSkillsUser() {
		return skillsUser;
	}

	public void setSkillsUser(SkillsUserEntity skillsUser) {
		this.skillsUser = skillsUser;
	}

}
