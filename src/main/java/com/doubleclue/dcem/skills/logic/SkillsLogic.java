package com.doubleclue.dcem.skills.logic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.entities.DcemAction;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.jpa.DcemTransactional;
import com.doubleclue.dcem.skills.entities.SkillsCertificateEntity;
import com.doubleclue.dcem.skills.entities.SkillsEntity;
import com.doubleclue.dcem.skills.entities.SkillsEntity_;
import com.doubleclue.dcem.skills.entities.SkillsJobProfileEntity;
import com.doubleclue.dcem.skills.entities.SkillsLevelEntity;
import com.doubleclue.dcem.skills.entities.SkillsUserEntity;
import com.doubleclue.dcem.skills.entities.SkillsUserSkillEntity;
import com.doubleclue.dcem.skills.entities.enums.ApprovalStatus;

@ApplicationScoped
@Named("skillsLogic")
public class SkillsLogic {

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(SkillsLogic.class);

	@Inject
	SkillsLevelLogic skillsLevelLogic;

	@Inject
	SkillsUserLogic skillsUserLogic;

	@Inject
	SkillsCertificateLogic skillsCertificateLogic;

	@Inject
	SkillsModule skillsModule;

	@Inject
	SkillsJobProfileEntityLogic skillsJobProfileEntityLogic;

	@Inject
	EntityManager em;

	@Inject
	Event<List<SkillsEntity>> eventSkill;

	@Inject
	Event<SkillsMergeDTO> eventMerge;

	@DcemTransactional
	public void addOrUpdateSkill(SkillsEntity skillEntity, DcemAction dcemAction) throws Exception {
		if (skillEntity.getParent() == null) {
			skillEntity.setParent(getSkillRoot());
		}

		if (dcemAction.getAction().equals(DcemConstants.ACTION_ADD) || dcemAction.getAction().equals(DcemConstants.ACTION_COPY)
				|| dcemAction.getAction().equals(SkillsConstants.REQUEST_SKILLS)) {
			skillEntity.setId(null);
			if (skillEntity.getParent() == null) { // this is needed for db which handle the case of a null-unique key in an unexpected way
				if (getSkillByNameAndParent(skillEntity.getName(), null) != null) {
					throw new DcemException(DcemErrorCodes.CONSTRAIN_VIOLATION_DB, "Skillname: " + skillEntity.toString(), null);
				}
			}
			em.persist(skillEntity);
		} else {
			em.merge(skillEntity);
		}
	}

	public SkillsEntity getSkillById(int id) throws Exception {
		return em.find(SkillsEntity.class, id);
	}

	@DcemTransactional
	public SkillsEntity getSkillRoot() throws Exception {
		SkillsEntity skillRoot = skillsModule.getSkillsTenantData().getSkillsRoot();
		if (skillRoot == null) {
			skillRoot = getSkillByNameAndParent(SkillsConstants.SKILLS_ROOT, SkillsConstants.SKILLS_ROOT);
			if (skillRoot == null) {
				skillRoot = new SkillsEntity();
				skillRoot.setName(SkillsConstants.SKILLS_ROOT);
				skillRoot.setAbbreviation(SkillsConstants.SKILLS_ROOT);
				skillRoot.setObtainable(false);
				skillRoot.setParent(skillRoot);
				skillRoot.setStatus(ApprovalStatus.APPROVED);
				em.persist(skillRoot);
				addSkillRootToSkill(skillRoot);
			}
			skillsModule.getSkillsTenantData().setSkillsRoot(skillRoot);
		}
		return skillRoot;
	}

	private void addSkillRootToSkill(SkillsEntity skillRoot) {
		TypedQuery<SkillsEntity> query = em.createNamedQuery(SkillsEntity.GET_PARENTLESS_SKILLS, SkillsEntity.class);
		List<SkillsEntity> skillsWithoutParents = query.getResultList();
		for (SkillsEntity skill : skillsWithoutParents) {
			skill.setParent(skillRoot);
		}
	}

	public List<SkillsEntity> getAllSkills() throws Exception {
		TypedQuery<SkillsEntity> query = em.createNamedQuery(SkillsEntity.GET_ALL, SkillsEntity.class);
		return query.getResultList();
	}

	public List<SkillsEntity> getAllApprovedSkills() throws Exception {
		TypedQuery<SkillsEntity> query = em.createNamedQuery(SkillsEntity.GET_APPROVED_SKILLS, SkillsEntity.class);
		return query.getResultList();
	}

	public List<SkillsEntity> getAllSkillsWithFilteringNotApproved() throws Exception {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<SkillsEntity> criteriaQuery = cb.createQuery(SkillsEntity.class);
		Root<SkillsEntity> skillRoot = criteriaQuery.from(SkillsEntity.class);
		criteriaQuery.select(skillRoot);
		List<Predicate> predicates = new ArrayList<Predicate>();
		if (skillsModule.getModulePreferences().isRequestedSkillAndCertificateImmediateAvailable() == false) {
			predicates.add(cb.equal(skillRoot.get(SkillsEntity_.status), ApprovalStatus.APPROVED));
		}
		Predicate whereCondition = cb.and(predicates.toArray(new Predicate[predicates.size()]));
		criteriaQuery.where(whereCondition);
		criteriaQuery.orderBy(cb.asc(skillRoot.get(SkillsEntity_.name)));
		List<SkillsEntity> resultList = em.createQuery(criteriaQuery).getResultList();
		return resultList;
	}

	public List<SkillsEntity> getAllObtainableSkillsWithFilteringNotApproved() throws Exception {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<SkillsEntity> criteriaQuery = cb.createQuery(SkillsEntity.class);
		Root<SkillsEntity> skillRoot = criteriaQuery.from(SkillsEntity.class);
		criteriaQuery.select(skillRoot);
		List<Predicate> predicates = new ArrayList<Predicate>();
		predicates.add(cb.isTrue(skillRoot.get(SkillsEntity_.obtainable)));
		if (skillsModule.getModulePreferences().isRequestedSkillAndCertificateImmediateAvailable() == false) {
			predicates.add(cb.equal(skillRoot.get(SkillsEntity_.status), ApprovalStatus.APPROVED));
		}
		Predicate whereCondition = cb.and(predicates.toArray(new Predicate[predicates.size()]));
		criteriaQuery.where(whereCondition);
		criteriaQuery.orderBy(cb.asc(skillRoot.get(SkillsEntity_.name)));
		List<SkillsEntity> resultList = em.createQuery(criteriaQuery).getResultList();
		return resultList;
	}

	public List<SkillsEntity> getAutoCompleteSkillsListWithFilteringNotApproved(String name, int max) throws Exception {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<SkillsEntity> criteriaQuery = cb.createQuery(SkillsEntity.class);
		Root<SkillsEntity> skillRoot = criteriaQuery.from(SkillsEntity.class);
		criteriaQuery.select(skillRoot);
		List<Predicate> predicates = new ArrayList<Predicate>();
		predicates.add(cb.like(cb.lower(skillRoot.get(SkillsEntity_.name)), cb.lower(cb.literal("%" + name + "%"))));
		predicates.add(cb.isTrue(skillRoot.get(SkillsEntity_.obtainable)));
		if (skillsModule.getModulePreferences().isRequestedSkillAndCertificateImmediateAvailable() == false) {
			predicates.add(cb.equal(skillRoot.get(SkillsEntity_.status), ApprovalStatus.APPROVED));
		}
		Predicate whereCondition = cb.and(predicates.toArray(new Predicate[predicates.size()]));
		criteriaQuery.where(whereCondition);
		criteriaQuery.orderBy(cb.asc(skillRoot.get(SkillsEntity_.name)));
		List<SkillsEntity> resultList = em.createQuery(criteriaQuery).setMaxResults(max).getResultList();
		return resultList;
	}

	public List<SkillsEntity> getAutoCompleteSkillsList(String name, int max) {
		TypedQuery<SkillsEntity> query = em.createNamedQuery(SkillsEntity.GET_BY_NAME, SkillsEntity.class);
		query.setParameter(1, "%" + name + "%");
		query.setMaxResults(max);
		return query.getResultList();
	}

	public SkillsEntity getSkillByNameAndParent(String skillNameWithParent) throws Exception {
		String[] skillNameAndParent = skillNameWithParent.split(SkillsConstants.PARENT_SEPERATOR);
		if (skillNameAndParent.length == 1) {
			return getSkillByNameAndParent(skillNameAndParent[0], SkillsConstants.SKILLS_ROOT);
		} else {
			return getSkillByNameAndParent(skillNameAndParent[0], skillNameAndParent[1]);
		}
	}

	public SkillsEntity getSkillByNameAndParent(String skillName, String parentName) throws Exception {
		TypedQuery<SkillsEntity> query = em.createNamedQuery(SkillsEntity.GET_BY_NAME_AND_PARENT, SkillsEntity.class);
		query.setParameter(1, skillName);
		query.setParameter(2, parentName);
		try {
			return query.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	public SkillsUserSkillEntity getSkillFromUserById(SkillsUserEntity skillsUserEntity, int id) throws Exception {
		EntityGraph<?> entityGraph = em.getEntityGraph(SkillsUserSkillEntity.GRAPH_SKILL);
		TypedQuery<SkillsUserSkillEntity> query = em.createNamedQuery(SkillsUserSkillEntity.GET_BY_SKILLS_ID_AND_SKILLS_USER, SkillsUserSkillEntity.class);
		query.setParameter(1, skillsUserEntity);
		query.setParameter(2, id);
		query.setHint("javax.persistence.fetchgraph", entityGraph);
		return query.getSingleResult();
	}

	@DcemTransactional
	public void deleteSkill(SkillsEntity skillsEntity) throws Exception {
		List<SkillsEntity> skillAsList = new ArrayList<SkillsEntity>();
		skillAsList.add(skillsEntity);
		deleteSkills(skillAsList);
	}

	@DcemTransactional
	public void deleteSkills(List<SkillsEntity> skillsEntities) throws Exception {
		skillsLevelLogic.deleteSkillsLevel(skillsLevelLogic.getSkillsLevelEntities(skillsEntities));
		skillsUserLogic.deleteUserskillsBySkills(skillsEntities);
		skillsCertificateLogic.removeSkillsFromCertificates(skillsEntities);
		eventSkill.fire(skillsEntities);
		for (SkillsEntity skillsEntity : skillsEntities) {
			skillsEntity = em.merge(skillsEntity);
			em.remove(skillsEntity);
		}
	}

	@DcemTransactional
	public void approveSkills(List<SkillsEntity> skillsEntities) throws Exception {
		for (SkillsEntity skill : skillsEntities) {
			skill = getSkillById(skill.getId());
			skill.setStatus(ApprovalStatus.APPROVED);
			skill.setRequestedFrom(null);
			em.merge(skill);
		}
	}

	public HashMap<Integer, Long> getUserCountForSkills(List<SkillsEntity> skillsEntities) throws Exception {
		TypedQuery<Tuple> query = em.createNamedQuery(SkillsUserSkillEntity.GET_SKILL_AND_USER_COUNT_BY_SKILLS, Tuple.class);
		query.setParameter(1, skillsEntities);
		List<Tuple> count = query.getResultList();
		HashMap<Integer, Long> map = convertTupleListToMap(count);
		return map;
	}

	public HashMap<Integer, Long> getCertificateCountForSkills(List<SkillsEntity> skillsEntities) throws Exception {
		TypedQuery<Tuple> query = em.createNamedQuery(SkillsCertificateEntity.GET_SKILL_AND_CERTIFICATE_COUNT_BY_SKILLS, Tuple.class);
		query.setParameter(1, skillsEntities);
		List<Tuple> count = query.getResultList();
		HashMap<Integer, Long> map = convertTupleListToMap(count);
		return map;
	}

	public HashMap<Integer, Long> getJobprofileCountForSkills(List<SkillsEntity> skillsEntities) throws Exception {
		TypedQuery<Tuple> query = em.createNamedQuery(SkillsJobProfileEntity.GET_SKILL_ID_AND_JOBPROFILE_COUNT_BY_SKILLS, Tuple.class);
		query.setParameter(1, skillsEntities);
		List<Tuple> count = query.getResultList();
		HashMap<Integer, Long> map = convertTupleListToMap(count);
		return map;
	}

	private HashMap<Integer, Long> convertTupleListToMap(List<Tuple> list) {
		HashMap<Integer, Long> map = new HashMap<Integer, Long>();
		for (Tuple tuple : list) {
			map.put((Integer) tuple.get("id"), (Long) tuple.get("count"));
		}
		return map;
	}

	@DcemTransactional
	public void mergeSkills(SkillsEntity mainSkill, SkillsEntity mergingSkill) throws Exception {
		replaceSkillInCertificate(mainSkill, mergingSkill);
		replaceSkillInUser(mainSkill, mergingSkill);
		replaceSkillInJobProfile(mainSkill, mergingSkill);
		replaceChildrenReference(mainSkill, mergingSkill);

		eventMerge.fire(new SkillsMergeDTO(mainSkill, mergingSkill));
		deleteSkill(mergingSkill);
	}

	private void replaceSkillInCertificate(SkillsEntity mainSkill, SkillsEntity mergingSkill) throws Exception {
		List<SkillsCertificateEntity> certificates = skillsCertificateLogic.getCertificatesBySkill(mergingSkill);
		for (SkillsCertificateEntity certificate : certificates) {
			certificate.getAppliesForSkills().remove(mergingSkill);
			if (certificate.getAppliesForSkills().contains(mainSkill) == false) {
				certificate.getAppliesForSkills().add(mainSkill);
			}
		}
	}

	private void replaceSkillInUser(SkillsEntity mainSkill, SkillsEntity mergingSkill) {
		List<SkillsUserEntity> skillsUsers = skillsUserLogic.getSkillsUserBySkill(mergingSkill);
		for (SkillsUserEntity skillsUser : skillsUsers) {
			boolean isMainSkillUsed = false;
			List<SkillsUserSkillEntity> userMergingSkills = new ArrayList<SkillsUserSkillEntity>();

			Iterator<SkillsUserSkillEntity> itr = skillsUser.getSkills().iterator();
			while (itr.hasNext()) {
				SkillsUserSkillEntity userSkill = itr.next();
				if (userSkill.getSkill().equals(mainSkill)) {
					isMainSkillUsed = true;
				} else if (userSkill.getSkill().equals(mergingSkill)) {
					userMergingSkills.add(userSkill);
					itr.remove();
				}
			}

			if (isMainSkillUsed) {
				for (SkillsUserSkillEntity userSkill : userMergingSkills) {
					em.remove(userSkill);
				}
			} else {
				for (SkillsUserSkillEntity userSkill : userMergingSkills) {
					userSkill.setSkill(mainSkill);
					skillsUser.getSkills().add(userSkill);
					em.merge(userSkill);
				}
			}
		}
	}

	private void replaceSkillInJobProfile(SkillsEntity mainSkill, SkillsEntity mergingSkill) throws Exception {
		List<SkillsJobProfileEntity> jobProfiles = skillsJobProfileEntityLogic.getJobProfilesBySkill(mergingSkill);
		for (SkillsJobProfileEntity jobProfile : jobProfiles) {
			boolean isMainSkillUsed = false;
			SkillsLevelEntity mergingSkillWithLevel = null;
			Iterator<SkillsLevelEntity> itr = jobProfile.getSkillLevels().iterator();
			while (itr.hasNext()) {
				SkillsLevelEntity skillsLevel = itr.next();
				if (skillsLevel.getSkill().equals(mainSkill)) {
					isMainSkillUsed = true;
				} else if (skillsLevel.getSkill().equals(mergingSkill)) {
					mergingSkillWithLevel = skillsLevel;
					itr.remove();
				}
				if (isMainSkillUsed && mergingSkillWithLevel != null) {
					break;
				}
			}
			if (isMainSkillUsed == false) {
				SkillsLevelEntity mergedSkillLevel = skillsLevelLogic
						.getOrCreateSkillLevel(new SkillsLevelEntity(mainSkill, mergingSkillWithLevel.getLevel(), mergingSkillWithLevel.getPriority()));
				jobProfile.getSkillLevels().add(mergedSkillLevel);
			}
			em.merge(jobProfile);
		}
		for (SkillsUserEntity skillsUserEntity : skillsUserLogic.getUserByJobprofiles(jobProfiles)) {
			SkillsUserLogic.updateAllMatches(skillsUserEntity);
		}
	}

	private void replaceChildrenReference(SkillsEntity mainSkill, SkillsEntity mergingSkill) throws Exception {
		List<SkillsEntity> childrenOfMergingSkill = mergingSkill.getChildren();
		for (SkillsEntity child : childrenOfMergingSkill) {
			child.setParent(mainSkill);
			em.merge(child);
		}
	}
}
