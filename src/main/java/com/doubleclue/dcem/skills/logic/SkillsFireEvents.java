package com.doubleclue.dcem.skills.logic;

import java.util.List;

import javax.enterprise.event.Observes;

import com.doubleclue.dcem.skills.entities.SkillsEntity;
import com.doubleclue.dcem.skills.entities.SkillsLevelEntity;

public interface SkillsFireEvents {
	
	public void deleteSkillsListener(@Observes List<SkillsEntity> skillsEntities) throws Exception;
	
	public void deleteSkillsLevelListener(@Observes List<SkillsLevelEntity> skillsLevelEntities) throws Exception;
	
}
