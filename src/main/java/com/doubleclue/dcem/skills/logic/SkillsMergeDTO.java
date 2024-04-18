package com.doubleclue.dcem.skills.logic;

import java.util.Objects;

import com.doubleclue.dcem.skills.entities.SkillsEntity;

public class SkillsMergeDTO {

	private SkillsEntity mainSkill;
	private SkillsEntity mergingSkills;

	public SkillsMergeDTO() {

	}

	public SkillsMergeDTO(SkillsEntity mainSkill, SkillsEntity mergingSkill) {
		this.mainSkill = mainSkill;
		this.mergingSkills = mergingSkill;
	}

	public SkillsEntity getMainSkill() {
		return mainSkill;
	}

	public void setMainSkill(SkillsEntity mainSkill) {
		this.mainSkill = mainSkill;
	}

	public SkillsEntity getMergingSkills() {
		return mergingSkills;
	}

	public void setMergingSkills(SkillsEntity mergingSkills) {
		this.mergingSkills = mergingSkills;
	}

	@Override
	public String toString() {
		return "SkillsMergeDTO [mainSkill=" + mainSkill + ", mergingSkills=" + mergingSkills + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(mainSkill, mergingSkills);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SkillsMergeDTO other = (SkillsMergeDTO) obj;
		return Objects.equals(mainSkill, other.mainSkill) && Objects.equals(mergingSkills, other.mergingSkills);
	}

}
