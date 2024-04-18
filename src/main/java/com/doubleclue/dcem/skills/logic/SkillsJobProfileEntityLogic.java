package com.doubleclue.dcem.skills.logic;

import java.util.List;
import java.util.SortedSet;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.entities.DcemAction;
import com.doubleclue.dcem.core.jpa.DcemTransactional;
import com.doubleclue.dcem.core.logic.AuditingLogic;
import com.doubleclue.dcem.skills.entities.SkillsEntity;
import com.doubleclue.dcem.skills.entities.SkillsJobProfileEntity;
import com.doubleclue.dcem.skills.entities.SkillsLevelEntity;
import com.doubleclue.dcem.skills.entities.SkillsUserEntity;
import com.doubleclue.dcem.skills.entities.SkillsUserJobProfileEntity;

@ApplicationScoped
public class SkillsJobProfileEntityLogic {

	@SuppressWarnings("unused")
	private Logger logger = LogManager.getLogger(SkillsJobProfileEntityLogic.class);

	@Inject
	EntityManager em;

	@Inject
	AuditingLogic auditingLogic;

	@Inject
	SkillsLevelLogic skillsLevelLogic;

	@Inject
	SkillsUserLogic skillsUserLogic;

	public SkillsJobProfileEntity findJobProfileById(int id) {
		return em.find(SkillsJobProfileEntity.class, id);
	}

	@DcemTransactional
	public void addOrUpdate(SkillsJobProfileEntity skillsJobProfileEntity, DcemAction dcemAction) throws Exception {
		SortedSet<SkillsLevelEntity> sortedSet = skillsLevelLogic.updateSkillsLevelEntities(skillsJobProfileEntity.getSkillLevels());
		skillsJobProfileEntity.setSkillLevels(sortedSet);
		if (dcemAction.getAction().equals(DcemConstants.ACTION_ADD)) {
			em.persist(skillsJobProfileEntity);
		} else {
			em.merge(skillsJobProfileEntity);
			updateUserMatch(skillsJobProfileEntity);
		}
		auditingLogic.addAudit(dcemAction, skillsJobProfileEntity.toString());
	}

	@DcemTransactional
	private void updateUserMatch(SkillsJobProfileEntity skillsJobProfileEntity) throws Exception {
		TypedQuery<SkillsUserJobProfileEntity> query = em.createNamedQuery(SkillsUserJobProfileEntity.GET_ALL_USER_JOBPROFILES_BY_JOBPROFILE,
				SkillsUserJobProfileEntity.class);
		List<SkillsUserJobProfileEntity> userJobProfiles = query.setParameter(1, skillsJobProfileEntity).getResultList();
		for (SkillsUserJobProfileEntity userJobProfile : userJobProfiles) {
			userJobProfile.setMatch(SkillsUserLogic.computeMatch(userJobProfile.getSkillsUserEntity(), skillsJobProfileEntity));
		}
	}

	public List<SkillsUserJobProfileEntity> getJobProfilesOfUser(Integer userId) throws Exception {
		TypedQuery<SkillsUserJobProfileEntity> query = em.createNamedQuery(SkillsUserJobProfileEntity.GET_ALL_PROFILES_OF_USER,
				SkillsUserJobProfileEntity.class);
		List<SkillsUserJobProfileEntity> result = query.setParameter(1, userId).getResultList();
		return result;
	}

	public List<SkillsJobProfileEntity> getAutoCompleteJobProfileList(String name, int max) throws Exception {
		TypedQuery<SkillsJobProfileEntity> query = em.createNamedQuery(SkillsJobProfileEntity.GET_BY_NAME_LIKE, SkillsJobProfileEntity.class);
		query.setParameter(1, "%" + name + "%");
		query.setMaxResults(max);
		return query.getResultList();
	}

	public SkillsJobProfileEntity getJobProfileByName(String jobProfileName) throws Exception {
		TypedQuery<SkillsJobProfileEntity> query = em.createNamedQuery(SkillsJobProfileEntity.GET_BY_NAME, SkillsJobProfileEntity.class);
		query.setParameter(1, jobProfileName);
		try {
			return query.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	public List<SkillsJobProfileEntity> getJobProfilesBySkillsLevels(List<SkillsLevelEntity> skillsLevelEntities) throws Exception {
		TypedQuery<SkillsJobProfileEntity> query = em.createNamedQuery(SkillsJobProfileEntity.GET_BY_SKILLS_LEVELS, SkillsJobProfileEntity.class);
		query.setParameter(1, skillsLevelEntities);
		return query.getResultList();
	}

	@DcemTransactional
	public void removeSkillsLevelsFromJobProfile(List<SkillsLevelEntity> skillsLevelEntities) throws Exception {
		List<SkillsJobProfileEntity> jobProfiles = getJobProfilesBySkillsLevels(skillsLevelEntities);
		for (SkillsJobProfileEntity jobProfile : jobProfiles) {
			jobProfile.getSkillLevels().removeAll(skillsLevelEntities);
		}
		for (SkillsUserEntity skillsUserEntity : skillsUserLogic.getUserByJobprofiles(jobProfiles)) {
			SkillsUserLogic.updateAllMatches(skillsUserEntity);
		}
	}

	public List<SkillsJobProfileEntity> getJobProfilesBySkill(SkillsEntity skill) {
		TypedQuery<SkillsJobProfileEntity> query = em.createNamedQuery(SkillsJobProfileEntity.GET_BY_SKILL, SkillsJobProfileEntity.class);
		query.setParameter(1, skill);
		return query.getResultList();
	}

}
