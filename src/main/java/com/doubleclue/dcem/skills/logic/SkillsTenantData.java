package com.doubleclue.dcem.skills.logic;

import com.doubleclue.dcem.core.logic.module.ModuleTenantData;
import com.doubleclue.dcem.skills.entities.SkillsEntity;

public class SkillsTenantData extends ModuleTenantData {
	
	private SkillsEntity skillsRoot = null;

	public SkillsEntity getSkillsRoot() {
		return skillsRoot;
	}

	public void setSkillsRoot(SkillsEntity skillsRoot) {
		this.skillsRoot = skillsRoot;
	}
	
	

	
}
