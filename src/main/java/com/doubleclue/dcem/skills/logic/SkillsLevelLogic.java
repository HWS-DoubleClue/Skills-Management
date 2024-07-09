package com.doubleclue.dcem.skills.logic;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.core.jpa.DcemTransactional;
import com.doubleclue.dcem.skills.entities.SkillsEntity;
import com.doubleclue.dcem.skills.entities.SkillsLevelEntity;
import com.doubleclue.dcem.skills.entities.enums.SkillsLevel;

@ApplicationScoped
@Named("skillsLevelLogic")
public class SkillsLevelLogic {

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(SkillsLevelLogic.class);

	@Inject
	EntityManager em;
	
	@Inject
	SkillsLogic skillsLogic;
	
	@Inject
	SkillsJobProfileEntityLogic skillsJobProfileLogic;

	@Inject
	Event<List<SkillsLevelEntity>> eventSkillLevel;

	@DcemTransactional
	public void addOrUpdateSkillLevel(SkillsLevelEntity skill) {
		if (skill.getId() == null) {
			em.persist(skill);
			return;
		} else {
			SkillsLevelEntity entity = em.find(SkillsLevelEntity.class, skill.getId());
			entity.setLevel(skill.getLevel());
			em.merge(entity);
		}
	}
	
	@DcemTransactional
	private SkillsLevelEntity getOrCreateSkillLevel(SkillsEntity skill, SkillsLevel skillLevel, int priority) throws Exception {
		TypedQuery<SkillsLevelEntity> query = em.createNamedQuery(SkillsLevelEntity.GET_SKILLLEVEL, SkillsLevelEntity.class);
		query.setParameter(1, skill);
		query.setParameter(2, skillLevel);
		query.setParameter(3, priority);
		try {
			return query.getSingleResult();
		} catch (NoResultException e) {
			SkillsLevelEntity skillsLevelEntity = new SkillsLevelEntity();
			skillsLevelEntity.setSkill(skillsLogic.getSkillById(skill.getId()));
			skillsLevelEntity.setLevel(skillLevel);
			skillsLevelEntity.setPriority(priority);
			em.persist(skillsLevelEntity);
			return skillsLevelEntity;
		}
	}
	
	@DcemTransactional
	public SkillsLevelEntity getOrCreateSkillLevel(SkillsLevelEntity skillLevel) throws Exception {
		return getOrCreateSkillLevel(skillLevel.getSkill(), skillLevel.getLevel(), skillLevel.getPriority());
	}

	
	public List<SkillsLevelEntity> getSkillsLevelEntities(List<SkillsEntity> skillsEntities) throws Exception {
		TypedQuery<SkillsLevelEntity> query = em.createNamedQuery(SkillsLevelEntity.GET_ALL_SKILL_LEVELS_BY_SKILLS, SkillsLevelEntity.class);
		query.setParameter(1, skillsEntities);
		return query.getResultList();
	}
	
	@DcemTransactional
	public SortedSet<SkillsLevelEntity> updateSkillsLevelEntities(SortedSet<SkillsLevelEntity> skillsLevelEntities) throws Exception {
		SortedSet<SkillsLevelEntity> list = new TreeSet<SkillsLevelEntity>();
		for (SkillsLevelEntity skillsLevelEntity : skillsLevelEntities) {
			if (skillsLevelEntity.getId() != null) {
				list.add(skillsLevelEntity);
			} else {
				list.add(getOrCreateSkillLevel(skillsLevelEntity));
			}			
		}
		return list;
	}
	
	@DcemTransactional
	public void deleteSkillsLevel(List<SkillsLevelEntity> skillsLevelEntities) throws Exception {
		skillsJobProfileLogic.removeSkillsLevelsFromJobProfile(skillsLevelEntities);
		eventSkillLevel.fire(skillsLevelEntities);
		for (SkillsLevelEntity skillsLevel : skillsLevelEntities) {
			em.remove(skillsLevel);
		}
	}
	
	@DcemTransactional
	public void mergeSkillInSkillLevelEntities(SkillsEntity mergingSkill, SkillsEntity targetSkill, Collection<SkillsLevelEntity> skillsLevelEntities)
			throws Exception {
		if (skillsLevelEntities.isEmpty()) {
			return;
		}
		SkillsLevelEntity mergingSkillWithLevel = null;
		SkillsLevelEntity mainSkillWithLevel = null;
		Iterator<SkillsLevelEntity> itr = skillsLevelEntities.iterator();
		while (itr.hasNext()) {
			SkillsLevelEntity skillsLevel = itr.next();
			if (skillsLevel.getSkill().equals(targetSkill)) {
				mainSkillWithLevel = skillsLevel;
			} else if (skillsLevel.getSkill().equals(mergingSkill)) {
				mergingSkillWithLevel = skillsLevel;
				itr.remove();
			}
			if (mainSkillWithLevel != null && mergingSkillWithLevel != null) {
				break;
			}
		}
		if (mainSkillWithLevel == null && mergingSkillWithLevel != null) {
			SkillsLevelEntity newMergedSkillLevel = getOrCreateSkillLevel(new SkillsLevelEntity(targetSkill, mergingSkillWithLevel.getLevel(), mergingSkillWithLevel.getPriority()));
			skillsLevelEntities.add(newMergedSkillLevel);
		}
	}
}
