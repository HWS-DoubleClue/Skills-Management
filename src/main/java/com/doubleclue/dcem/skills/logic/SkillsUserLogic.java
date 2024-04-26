package com.doubleclue.dcem.skills.logic;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.as.entities.CloudSafeEntity;
import com.doubleclue.dcem.as.logic.CloudSafeDto;
import com.doubleclue.dcem.as.logic.CloudSafeLogic;
import com.doubleclue.dcem.as.logic.CloudSafeUploadFile;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.entities.DcemAction;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.entities.DcemUser_;
import com.doubleclue.dcem.core.jpa.DcemTransactional;
import com.doubleclue.dcem.core.logic.AuditingLogic;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.doubleclue.dcem.skills.entities.SkillsCertificateEntity;
import com.doubleclue.dcem.skills.entities.SkillsCertificatePriorityEntity;
import com.doubleclue.dcem.skills.entities.SkillsEntity;
import com.doubleclue.dcem.skills.entities.SkillsEntity_;
import com.doubleclue.dcem.skills.entities.SkillsJobProfileEntity;
import com.doubleclue.dcem.skills.entities.SkillsLevelEntity;
import com.doubleclue.dcem.skills.entities.SkillsUserCertificateEntity;
import com.doubleclue.dcem.skills.entities.SkillsUserEntity;
import com.doubleclue.dcem.skills.entities.SkillsUserEntity_;
import com.doubleclue.dcem.skills.entities.SkillsUserJobProfileEntity;
import com.doubleclue.dcem.skills.entities.SkillsUserSkillEntity;
import com.doubleclue.dcem.skills.entities.SkillsUserSkillEntity_;
import com.doubleclue.dcem.skills.entities.enums.LogicalConjunction;
import com.doubleclue.dcem.skills.entities.enums.SkillsLevel;
import com.doubleclue.dcem.skills.entities.enums.SkillsStatus;

@ApplicationScoped
@Named("skillsUserLogic")
public class SkillsUserLogic {

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(SkillsUserLogic.class);

	@Inject
	EntityManager em;

	@Inject
	SkillsJobProfileEntityLogic jobProfileEntityLogic;

	@Inject
	CloudSafeLogic cloudSafeLogic;

	@Inject
	OperatorSessionBean operatorSessionBean;
	
	@Inject
	AuditingLogic auditingLogic;

	@DcemTransactional
	public void updateUserSkillEntity(SkillsUserEntity skillsUserEntity) throws Exception {
		Query query = em.createNamedQuery(SkillsUserEntity.REMOVE_ALL_SKILLS_FROM_USER);
		query.setParameter(1, skillsUserEntity.getId());
		query.executeUpdate();
		skillsUserEntity = em.merge(skillsUserEntity);
		updateAllMatches(skillsUserEntity);
	}

	@DcemTransactional
	public void addOrUpdateUserEntityWithCertificates(SkillsUserEntity skillsUserWithoutNewUploadedCertificates, DcemAction dcemAction,
			HashMap<SkillsCertificateEntity, List<CloudSafeUploadFile>> mapCertificateToUploadedFiles, List<CloudSafeEntity> deletedFiles) throws Exception {
		// Auditing

		for (SkillsUserJobProfileEntity userJobProfile : skillsUserWithoutNewUploadedCertificates.getUserJobProfiles()) {
			userJobProfile.setMatch(
					computeMatch(skillsUserWithoutNewUploadedCertificates, jobProfileEntityLogic.findJobProfileById(userJobProfile.getJobProfile().getId())));
		}

		SkillsUserEntity skillsUserWithCertificates;
		if (dcemAction.getAction().equals(DcemConstants.ACTION_ADD)) {
			skillsUserWithCertificates = updateCertificateFiles(skillsUserWithoutNewUploadedCertificates, mapCertificateToUploadedFiles, dcemAction);
			em.persist(skillsUserWithCertificates);
		} else {
			skillsUserWithCertificates = updateCertificateFiles(skillsUserWithoutNewUploadedCertificates, mapCertificateToUploadedFiles, dcemAction);
			auditingLogic.addAudit(dcemAction, skillsUserWithCertificates);
			clearUserDataInDB(skillsUserWithoutNewUploadedCertificates);
			if (deletedFiles != null) {
				List<CloudSafeDto> deletedDbFiles = cloudSafeLogic.deleteCloudSafeFiles(deletedFiles, null, false);
				cloudSafeLogic.deleteCloudSafeFilesContent(deletedDbFiles);
			}
			em.merge(skillsUserWithCertificates);
		}
	}

	@DcemTransactional
	public void addOrUpdateSkillsUserEntity(SkillsUserEntity skillsUserEntity, DcemAction dcemAction) throws Exception {
		Query querySkills = em.createNamedQuery(SkillsUserEntity.REMOVE_ALL_SKILLS_FROM_USER);
		querySkills.setParameter(1, skillsUserEntity.getId());
		querySkills.executeUpdate();
		Query queryCertificates = em.createNamedQuery(SkillsUserCertificateEntity.REMOVE_ALL_CERTIFICATES_FROM_USER);
		queryCertificates.setParameter(1, skillsUserEntity.getId());
		queryCertificates.executeUpdate();
		for (SkillsUserSkillEntity skillsUserSkillEntity : skillsUserEntity.getSkills()) { // avoid deletion conflicts
			skillsUserSkillEntity.setId(null);
		}
		for (SkillsUserCertificateEntity skillsUserCertificateEntity : skillsUserEntity.getCertificates()) {
			skillsUserCertificateEntity.setId(null);
		}
		if (dcemAction.getAction().equals(DcemConstants.ACTION_ADD)) {
			em.persist(skillsUserEntity);
		} else {
			skillsUserEntity = em.merge(skillsUserEntity);
		}
		for (SkillsUserJobProfileEntity skillsUserJobProfileEntity : skillsUserEntity.getUserJobProfiles()) {
			skillsUserJobProfileEntity.setMatch(SkillsUserLogic.computeMatch(skillsUserEntity, skillsUserJobProfileEntity.getJobProfile()));
		}
	}

	private SkillsUserEntity updateCertificateFiles(SkillsUserEntity skillsUserWithoutNewUploadedCertificates,
			HashMap<SkillsCertificateEntity, List<CloudSafeUploadFile>> mapCertificateToUploadedFiles, DcemAction dcemAction) throws Exception {
		for (SkillsUserCertificateEntity skillsUserCertificateEntity : skillsUserWithoutNewUploadedCertificates.getCertificates()) {
			List<CloudSafeEntity> savedFiles = new LinkedList<CloudSafeEntity>();
			List<CloudSafeUploadFile> newUploadedFiles = mapCertificateToUploadedFiles
					.getOrDefault(skillsUserCertificateEntity.getSkillsCertificateEntity(), new ArrayList<CloudSafeUploadFile>()).stream()
					.filter(uploadedFile -> uploadedFile.file != null).collect(Collectors.toList());
			if (newUploadedFiles != null && newUploadedFiles.isEmpty() == false) {
				savedFiles = cloudSafeLogic.saveMultipleFiles(newUploadedFiles, operatorSessionBean.getDcemUser());
			}
			skillsUserCertificateEntity.getFiles().addAll(savedFiles);
		}
		return skillsUserWithoutNewUploadedCertificates;
	}

	private void clearUserDataInDB(SkillsUserEntity skillsUserEntity) throws Exception {
		Query query = em.createNamedQuery(SkillsUserEntity.REMOVE_ALL_SKILLS_FROM_USER);
		query.setParameter(1, skillsUserEntity.getId());
		query.executeUpdate();

		query = em.createNamedQuery(SkillsUserEntity.REMOVE_ALL_CERTIFICATES_FROM_USER);
		query.setParameter(1, skillsUserEntity.getId());
		query.executeUpdate();

		query = em.createNamedQuery(SkillsUserEntity.REMOVE_ALL_JOBPROFILES_FROM_USER);
		query.setParameter(1, skillsUserEntity.getId());
		query.executeUpdate();
	}

	@DcemTransactional
	public void deleteSkillsUser(DcemUser dcemUser) {
		try {
			SkillsUserEntity skillsUserEntity = getSkillsUserById(dcemUser.getId());
			if (skillsUserEntity != null) {
				em.remove(skillsUserEntity);
			}
			em.createNamedQuery(SkillsJobProfileEntity.REMOVE_USER_FROM_MANAGED_BY_FROM_SKILLSPROFILE).setParameter(1, dcemUser).executeUpdate();
			em.createNamedQuery(SkillsUserEntity.REMOVE_USER_FROM_REPORTS_TO_FROM_SKILLSUSERS).setParameter(1, dcemUser).executeUpdate();

		} catch (Exception e) {
			logger.error("Could not delete user from skills module: " + dcemUser.getLoginId(), e);
		}
	}
	

	public SkillsUserEntity getSkillsUserById(int id) {
		return em.find(SkillsUserEntity.class, id);
	}
	
	@DcemTransactional
	public SkillsUserEntity retrieveSkillsUserByDcemUser (DcemUser dcemUser) {
		SkillsUserEntity skillsUserEntity = em.find(SkillsUserEntity.class, dcemUser.getId());
		if (skillsUserEntity == null) {
			skillsUserEntity = new SkillsUserEntity(dcemUser);
			em.persist(skillsUserEntity);
		}
		return skillsUserEntity;
	}

	public List<String> getCompleteSkillsUser(String name, int max) throws Exception {
		TypedQuery<String> query = em.createNamedQuery(SkillsUserEntity.GET_COMPLETE_SKILLSUSER, String.class);
		query.setParameter(1, "%" + name.toLowerCase() + "%");
		query.setMaxResults(max);
		return query.getResultList();
	}

	public List<Tuple> getUserAndCertificatesForSkillSearch(String searchName) throws Exception {
		TypedQuery<Tuple> query = em.createNamedQuery(SkillsUserEntity.GET_USER_CERTIFICATES_BY_NAME_FILTERED, Tuple.class);
		query.setParameter(1, "%" + searchName + "%");
		return query.getResultList();
	}

	public List<Tuple> getUserAndSkillsForSkillSearch(List<String> skillsNames, int ordinalLevel, LogicalConjunction logicalConjunction,
			boolean onlyOwnedSkills) throws Exception {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Tuple> criteriaQuery = cb.createTupleQuery();
		Root<SkillsUserSkillEntity> userSkillRoot = criteriaQuery.from(SkillsUserSkillEntity.class);
		Join<SkillsUserSkillEntity, SkillsEntity> skillsJoin = userSkillRoot.join(SkillsUserSkillEntity_.skill, JoinType.INNER);
		Join<SkillsUserSkillEntity, SkillsUserEntity> skillsUserJoin = userSkillRoot.join(SkillsUserSkillEntity_.skillsUserEntity, JoinType.INNER);
		Join<SkillsUserEntity, DcemUser> dcemUserJoin = skillsUserJoin.join(SkillsUserEntity_.dcemUser, JoinType.INNER);
		criteriaQuery.multiselect(skillsUserJoin.alias("user"), userSkillRoot.alias("userSkill"));

		List<Predicate> skillNamePredicates = new ArrayList<Predicate>();
		List<Predicate> userHasSkillPredicate = new ArrayList<Predicate>();
		for (String skillsName : skillsNames) {
			Subquery<Integer> userIdSubquery = getSubqueryForUserIdsOwningSkillByNameAndLevel(cb, skillsName, ordinalLevel, onlyOwnedSkills);
			Predicate foundUser = cb.in(skillsUserJoin.get(SkillsUserEntity_.id)).value(userIdSubquery);
			userHasSkillPredicate.add(foundUser);

			Predicate skillName = cb.like(cb.lower(skillsJoin.get(SkillsEntity_.name)), cb.lower(cb.literal("%" + skillsName + "%")));
			skillNamePredicates.add(skillName);
		}

		List<Predicate> wherePredicates = new ArrayList<Predicate>();

		Predicate userSkillPredicate = logicalConjunction == LogicalConjunction.AND
				? cb.and(userHasSkillPredicate.toArray(new Predicate[skillNamePredicates.size()]))
				: cb.or(userHasSkillPredicate.toArray(new Predicate[skillNamePredicates.size()]));
		wherePredicates.add(userSkillPredicate);

		Predicate skillName = cb.or(skillNamePredicates.toArray(new Predicate[skillNamePredicates.size()]));
		wherePredicates.add(skillName);

		Predicate predicateSkillLevel = filterSkillLevelPredicate(cb, userSkillRoot, ordinalLevel);
		wherePredicates.add(predicateSkillLevel);

		if (onlyOwnedSkills) {
			Predicate predicateOwnsSkill = cb.equal(userSkillRoot.get(SkillsUserSkillEntity_.status), SkillsStatus.OWNS);
			wherePredicates.add(predicateOwnsSkill);
		}

		criteriaQuery.where(cb.and(wherePredicates.toArray(new Predicate[wherePredicates.size()])));

		List<Order> orderList = new ArrayList<Order>();
		orderList.add(cb.asc(dcemUserJoin.get(DcemUser_.displayName)));
		orderList.add(cb.asc(skillsJoin.get(SkillsEntity_.name)));
		orderList.add(cb.asc(userSkillRoot.get(SkillsUserSkillEntity_.level)));
		criteriaQuery.orderBy(orderList);

		List<Tuple> resultList = em.createQuery(criteriaQuery).getResultList();
		return resultList;
	}

	private Subquery<Integer> getSubqueryForUserIdsOwningSkillByNameAndLevel(CriteriaBuilder cb, String skillsName, int ordinalLevel, boolean onlyOwnedFilter)
			throws Exception {
		CriteriaQuery<Integer> criteriaSubquery = cb.createQuery(Integer.class);
		Subquery<Integer> userIdSubquery = criteriaSubquery.subquery(Integer.class);
		Root<SkillsUserSkillEntity> userSkillRootSubquery = userIdSubquery.from(SkillsUserSkillEntity.class);
		Join<SkillsUserSkillEntity, SkillsUserEntity> skillsUserJoinSubquery = userSkillRootSubquery.join(SkillsUserSkillEntity_.skillsUserEntity,
				JoinType.INNER);
		Join<SkillsUserSkillEntity, SkillsEntity> skillsJoinSubquery = userSkillRootSubquery.join(SkillsUserSkillEntity_.skill, JoinType.INNER);
		userIdSubquery.select(skillsUserJoinSubquery.get(SkillsUserEntity_.id));

		List<Predicate> skillPredicate = new ArrayList<Predicate>();
		Predicate predicateSkillName = cb.like(cb.lower(skillsJoinSubquery.get(SkillsEntity_.name)), cb.lower(cb.literal("%" + skillsName + "%")));
		skillPredicate.add(predicateSkillName);

		if (onlyOwnedFilter) {
			Predicate predicateOwnsSkill = cb.equal(userSkillRootSubquery.get(SkillsUserSkillEntity_.status), SkillsStatus.OWNS);
			skillPredicate.add(predicateOwnsSkill);
		}

		Predicate predicateSkillLevel = filterSkillLevelPredicate(cb, userSkillRootSubquery, ordinalLevel);
		skillPredicate.add(predicateSkillLevel);

		userIdSubquery.where(cb.and(skillPredicate.toArray(new Predicate[skillPredicate.size()])));
		return userIdSubquery;
	}

	private Predicate filterSkillLevelPredicate(CriteriaBuilder cb, Root<SkillsUserSkillEntity> userSkillRoot, int ordinalLevel) {
		List<Predicate> orSkillLevelPredicates = new ArrayList<Predicate>();
		for (SkillsLevel level : SkillsLevel.values()) {
			if (level.ordinal() >= ordinalLevel) {
				Predicate skillLevelPredicate = cb.equal(userSkillRoot.get(SkillsUserSkillEntity_.level), level);
				orSkillLevelPredicates.add(skillLevelPredicate);
			}
		}
		Predicate predicateSkillLevel = cb.or(orSkillLevelPredicates.toArray(new Predicate[orSkillLevelPredicates.size()]));
		return predicateSkillLevel;
	}

	public List<Tuple> getJobProfilesForSkillSearch(String searchName, int match) throws Exception {
		TypedQuery<Tuple> query = em.createNamedQuery(SkillsUserEntity.GET_USER_JOBPROFILES_BY_NAME_FILTERED, Tuple.class);
		query.setParameter(1, "%" + searchName + "%");
		query.setParameter(2, match);
		return query.getResultList();
	}

	public List<SkillsUserSkillEntity> getSkillsOfUser(int userId) throws Exception {
		TypedQuery<SkillsUserSkillEntity> query = em.createNamedQuery(SkillsUserSkillEntity.GET_ALL_SKILLS_OF_USER, SkillsUserSkillEntity.class);
		return query.setParameter(1, userId).getResultList();
	}

	public void detachObjectsFromSkillsUser(SkillsUserEntity skillsUser) throws Exception {
		for (SkillsUserSkillEntity skillsUserSkillEntity : skillsUser.getSkills()) {
			em.detach(skillsUserSkillEntity);
		}
		for (SkillsUserCertificateEntity skillsUserCertificateEntity : skillsUser.getCertificates()) {
			em.detach(skillsUserCertificateEntity);
		}
		for (SkillsUserJobProfileEntity skillsUserJobProfileEntity : skillsUser.getUserJobProfiles()) {
			em.detach(skillsUserJobProfileEntity);
		}
	}

	public static int computeMatch(SkillsUserEntity skillsUserEntity, SkillsJobProfileEntity skillsJobProfileEntity) throws Exception {
		if (skillsJobProfileEntity == null || skillsUserEntity == null) {
			throw new IllegalArgumentException("Arguments can not be null");
		}
		int totalWeight = 0;
		double totalMatchedWeight = 0;

		HashMap<Integer, SkillsUserSkillEntity> skillMap = new HashMap<Integer, SkillsUserSkillEntity>();
		for (SkillsUserSkillEntity userSkill : skillsUserEntity.getSkills()) {
			if (userSkill.isSkillNotOwned() == false) {
				skillMap.put(userSkill.getSkill().getId(), userSkill);
			}
		}
		HashMap<Integer, SkillsUserCertificateEntity> certificateMap = new HashMap<Integer, SkillsUserCertificateEntity>();
		for (SkillsUserCertificateEntity userCertificate : skillsUserEntity.getCertificates()) {
			if (userCertificate.isCertificateNotOwned() == false) {
				certificateMap.put(userCertificate.getSkillsCertificateEntity().getId(), userCertificate);
			}
		}
		for (SkillsLevelEntity requiredSkillLevel : skillsJobProfileEntity.getSkillLevels()) {
			SkillsLevel requiredLevel = requiredSkillLevel.getLevel();
			SkillsUserSkillEntity userSkill = skillMap.get(requiredSkillLevel.getSkill().getId());
			totalWeight += requiredSkillLevel.getPriority();
			if (userSkill != null && requiredLevel.ordinal() > 0) {
				totalMatchedWeight += (double) requiredSkillLevel.getPriority() * Math.min(userSkill.getLevel().ordinal(), requiredLevel.ordinal())
						/ requiredLevel.ordinal();
			}
		}
		for (SkillsCertificatePriorityEntity requiredCertificate : skillsJobProfileEntity.getCertificatesPriorities()) {
			SkillsUserCertificateEntity userCertificate = certificateMap.get(requiredCertificate.getCertificateEntity().getId());
			totalWeight += requiredCertificate.getPriority();
			if (userCertificate != null && userCertificate.getExpirationDate() != null && userCertificate.getExpirationDate().isBefore(LocalDate.now())) {
				totalMatchedWeight += requiredCertificate.getPriority() * 0.75;
			} else if (userCertificate != null) {
				totalMatchedWeight += requiredCertificate.getPriority();
			}
		}
		if (totalWeight == 0) {
			return 100;
		} else {
			return (int) (totalMatchedWeight * 100 / totalWeight);
		}
	}

	public static void updateAllMatches(SkillsUserEntity skillsUserEntity) throws Exception {
		for (SkillsUserJobProfileEntity skillsUserJobProfileEntity : skillsUserEntity.getUserJobProfiles()) {
			skillsUserJobProfileEntity.setMatch(computeMatch(skillsUserEntity, skillsUserJobProfileEntity.getJobProfile()));
		}
	}

	public List<SkillsUserEntity> getUserByJobprofiles(List<SkillsJobProfileEntity> jobProfiles) throws Exception {
		TypedQuery<SkillsUserEntity> query = em.createNamedQuery(SkillsUserEntity.GET_ALL_USERS_BY_JOBPROFILES, SkillsUserEntity.class);
		query.setParameter(1, jobProfiles);
		return query.getResultList();
	}

	public List<SkillsUserSkillEntity> getTargetSkillsFromUser(SkillsUserEntity skillsUserEntity) throws Exception {
		EntityGraph<?> entityGraph = em.getEntityGraph(SkillsUserSkillEntity.GRAPH_SKILL);
		TypedQuery<SkillsUserSkillEntity> query = em.createNamedQuery(SkillsUserSkillEntity.GET_TARGET_SKILLS_OF_USER, SkillsUserSkillEntity.class);
		query.setParameter(1, skillsUserEntity);
		query.setHint("javax.persistence.fetchgraph", entityGraph);
		return query.getResultList();
	}

	@DcemTransactional
	public void deleteUserskillsBySkills(List<SkillsEntity> skillsEntities) throws Exception {
		Query query = em.createNamedQuery(SkillsUserSkillEntity.DELETE_USER_SKILLS_BY_SKILLS);
		query.setParameter(1, skillsEntities);
		query.executeUpdate();
	}

	public List<SkillsUserEntity> getSkillsUserBySkill(SkillsEntity skillEntity) {
		List<SkillsEntity> skillAsList = new ArrayList<SkillsEntity>();
		skillAsList.add(skillEntity);
		return getSkillsUserBySkills(skillAsList);
	}

	public List<SkillsUserEntity> getSkillsUserBySkills(List<SkillsEntity> skillEntities) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<SkillsUserEntity> criteriaQuery = cb.createQuery(SkillsUserEntity.class);
		Root<SkillsUserEntity> userRoot = criteriaQuery.from(SkillsUserEntity.class);

		Join<SkillsUserEntity, SkillsUserSkillEntity> userSkillJoin = userRoot.join(SkillsUserEntity_.skills, JoinType.INNER);
		Join<SkillsUserSkillEntity, SkillsEntity> skillsJoin = userSkillJoin.join(SkillsUserSkillEntity_.skill, JoinType.INNER);

		Predicate wherePredicate = skillsJoin.in(skillEntities);
		criteriaQuery.select(userRoot).distinct(true).where(wherePredicate);

		userRoot.fetch(SkillsUserEntity_.skills, JoinType.INNER).fetch(SkillsUserSkillEntity_.skill, JoinType.INNER);

		List<SkillsUserEntity> resultList = em.createQuery(criteriaQuery).getResultList();
		return resultList;
	}
	
	public List<DcemUser> getReciepientsForRequests() throws Exception {
		TypedQuery<DcemUser> query = em.createNamedQuery(SkillsUserEntity.GET_RECIPIENTS_FOR_REQUESTS, DcemUser.class);
		return query.getResultList();
	}

}
