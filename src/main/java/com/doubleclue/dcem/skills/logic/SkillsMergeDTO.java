package com.doubleclue.dcem.skills.logic;

import java.util.Objects;

import com.doubleclue.dcem.skills.entities.SkillsEntity;

public class SkillsMergeDTO {

	private SkillsEntity mainSkill;
	private SkillsEntity mergingSkill;

	public SkillsMergeDTO() {

	}

	public SkillsMergeDTO(SkillsEntity mainSkill, SkillsEntity mergingSkill) {
		this.mainSkill = mainSkill;
		this.mergingSkill = mergingSkill;
	}

	public SkillsEntity getMainSkill() {
		return mainSkill;
	}

	public void setMainSkill(SkillsEntity mainSkill) {
		this.mainSkill = mainSkill;
	}

	public SkillsEntity getMergingSkill() {
		return mergingSkill;
	}

	public void setMergingSkill(SkillsEntity mergingSkill) {
		this.mergingSkill = mergingSkill;
	}

	@Override
	public String toString() {
		return "SkillsMergeDTO [mainSkill=" + mainSkill + ", mergingSkill=" + mergingSkill + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(mainSkill, mergingSkill);
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
		return Objects.equals(mainSkill, other.mainSkill) && Objects.equals(mergingSkill, other.mergingSkill);
	}

}
