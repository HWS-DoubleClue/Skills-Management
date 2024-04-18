package com.doubleclue.dcem.skills.gui;

import com.doubleclue.dcem.skills.entities.SkillsEntity;
import com.doubleclue.dcem.skills.entities.SkillsLevelEntity;
import com.doubleclue.dcem.skills.entities.enums.SkillsLevel;

public class SkillTreeNode {
	
	SkillsEntity skillsEntity;
	SkillsLevel skillsLevel;
	int priority = 1;
	Integer id = null;

	public SkillTreeNode(Integer id, SkillsEntity skillsEntity, SkillsLevel skillsLevel, int priority) {
		this.id = id;
		this.skillsEntity = skillsEntity;
		this.skillsLevel = skillsLevel;
		this.priority = priority;
	}

	public SkillTreeNode(SkillsLevelEntity skillsLevelEntity) {
		this.id = skillsLevelEntity.getId();
		this.skillsEntity = skillsLevelEntity.getSkill();
		this.skillsLevel = skillsLevelEntity.getLevel();
		this.priority = skillsLevelEntity.getPriority();
	}

	public SkillsLevel getSkillsLevel() {
		return skillsLevel;
	}

	public void setSkillsLevel(SkillsLevel skillsLevel) {
		this.skillsLevel = skillsLevel;
	}

	public String getName() {
		return skillsEntity.getName();
	}

	public SkillsEntity getSkillsEntity() {
		return skillsEntity;
	}

	public void setSkillsEntity(SkillsEntity skillsEntity) {
		this.skillsEntity = skillsEntity;
	}

	public String getIncludeIcon() {
		return "fa fa-xmark";
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getId() {
		return id;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	@Override
	public String toString() {
		return "SkillTreeNode [skillsEntity=" + skillsEntity + ", skillsLevel=" + skillsLevel + ", priority=" + priority + ", id=" + id + "]";
	}

}
