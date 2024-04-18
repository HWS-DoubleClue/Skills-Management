package com.doubleclue.dcem.skills.logic;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.CacheRetrieveMode;
import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.as.logic.CloudSafeLogic;
import com.doubleclue.dcem.core.entities.DcemAction;
import com.doubleclue.dcem.core.jpa.DcemTransactional;
import com.doubleclue.dcem.core.logic.AuditingLogic;
import com.doubleclue.dcem.skills.entities.SkillsCertificateEntity;
import com.doubleclue.dcem.skills.entities.SkillsCertificateEntity_;
import com.doubleclue.dcem.skills.entities.SkillsCertificatePriorityEntity;
import com.doubleclue.dcem.skills.entities.SkillsEntity;
import com.doubleclue.dcem.skills.entities.SkillsUserCertificateEntity;
import com.doubleclue.dcem.skills.entities.SkillsUserEntity;
import com.doubleclue.dcem.skills.entities.enums.ApprovalStatus;

@ApplicationScoped
@Named("skillsCertificateLogic")
public class SkillsCertificateLogic {

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(SkillsCertificateLogic.class);

	@Inject
	EntityManager em;

	@Inject
	AuditingLogic auditingLogic;

	@Inject
	CloudSafeLogic cloudSafeLogic;

	@Inject
	SkillsModule skillsModule;

	@DcemTransactional
	public void addOrUpdateSkillsCertificate(SkillsCertificateEntity skillsCertificateEntity, DcemAction dcemAction) {
		if (dcemAction.getAction().equals(SkillsConstants.ADD_CERTIFICATE)) {
			em.persist(skillsCertificateEntity);
		} else {
			em.merge(skillsCertificateEntity);
		}
	}
	
	public SkillsCertificateEntity getCertificateById(int id) throws Exception {
		return em.find(SkillsCertificateEntity.class, id);
	}

	@DcemTransactional
	public void updateSkillsUserCertificate(SkillsUserCertificateEntity skillsUserCertificateEntity) {
		em.merge(skillsUserCertificateEntity);
	}

	public SkillsCertificateEntity getCertificateByName(String name) throws Exception {
		try {
			TypedQuery<SkillsCertificateEntity> query = em.createNamedQuery(SkillsCertificateEntity.GET_BY_NAME, SkillsCertificateEntity.class);
			query.setParameter(1, name);
			return query.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	public SkillsUserCertificateEntity getCertificateFromUserById(int userId, int certificateId) throws Exception {
		try {
			TypedQuery<SkillsUserCertificateEntity> query = em.createNamedQuery(SkillsUserCertificateEntity.GET_BY_CERTIFICATE_ID_AND_USER,
					SkillsUserCertificateEntity.class);
			query.setParameter(1, userId);
			query.setParameter(2, certificateId);
			return query.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	public List<SkillsUserCertificateEntity> getAllCertificatesFromUser(SkillsUserEntity skillsUserEntity) throws Exception {
		TypedQuery<SkillsUserCertificateEntity> query = em.createNamedQuery(SkillsUserCertificateEntity.GET_BY_USER, SkillsUserCertificateEntity.class);
		query.setParameter(1, skillsUserEntity);
		return query.getResultList();
	}

	public List<SkillsUserCertificateEntity> getAllCertificatesFromUserWithFiles(SkillsUserEntity skillsUserEntity) throws Exception {
		EntityGraph<?> entityGraph = em.getEntityGraph(SkillsUserCertificateEntity.GRAPH_FILES);
		TypedQuery<SkillsUserCertificateEntity> query = em.createNamedQuery(SkillsUserCertificateEntity.GET_BY_USER, SkillsUserCertificateEntity.class);
		query.setHint("javax.persistence.fetchgraph", entityGraph);
		query.setHint("org.hibernate.cacheable", false);
		query.setHint("javax.persistence.cache.retrieveMode", CacheRetrieveMode.BYPASS);
		query.setParameter(1, skillsUserEntity);
		return query.getResultList();
	}

	public List<String> getAutoCompleteCertificateNameListWithFilteringNotApproved(String name, int max) throws Exception {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<String> criteriaQuery = cb.createQuery(String.class);
		Root<SkillsCertificateEntity> certificateRoot = criteriaQuery.from(SkillsCertificateEntity.class);
		criteriaQuery.select(certificateRoot.get(SkillsCertificateEntity_.name));
		List<Predicate> predicates = new ArrayList<Predicate>();
		predicates.add(cb.like(cb.lower(certificateRoot.get(SkillsCertificateEntity_.name)), cb.lower(cb.literal("%" + name + "%"))));
		if (skillsModule.getModulePreferences().isRequestedSkillAndCertificateImmediateAvailable() == false) {
			predicates.add(cb.equal(certificateRoot.get(SkillsCertificateEntity_.approvalStatus), ApprovalStatus.APPROVED));
		}
		Predicate whereCondition = cb.and(predicates.toArray(new Predicate[predicates.size()]));
		criteriaQuery.where(whereCondition);
		criteriaQuery.orderBy(cb.asc(certificateRoot.get(SkillsCertificateEntity_.name)));
		List<String> resultList = em.createQuery(criteriaQuery).setMaxResults(max).getResultList();
		return resultList;
	}

	public List<SkillsUserCertificateEntity> getSoonExpiredCertificateUser(LocalDate date, int notification, int notificationCount) throws Exception {
		TypedQuery<SkillsUserCertificateEntity> query = em.createNamedQuery(SkillsUserCertificateEntity.GET_ALL_SOON_EXPIRING_CERTIFICATES,
				SkillsUserCertificateEntity.class);
		query.setParameter(1, date.plusDays(notification));
		query.setParameter(2, notificationCount);
		return query.getResultList();
	}

	@DcemTransactional
	public SkillsCertificatePriorityEntity getOrCreateCertificatePriority(SkillsCertificateEntity certificateEntity, int priority) {
		TypedQuery<SkillsCertificatePriorityEntity> query = em.createNamedQuery(SkillsCertificatePriorityEntity.GET_CERTIFICATE_PRIORITY,
				SkillsCertificatePriorityEntity.class);
		query.setParameter(1, certificateEntity);
		query.setParameter(2, priority);
		try {
			return query.getSingleResult();
		} catch (NoResultException e) {
			SkillsCertificatePriorityEntity certificatePriorityEntity = new SkillsCertificatePriorityEntity();
			certificatePriorityEntity.setCertificateEntity(certificateEntity);
			certificatePriorityEntity.setPriority(priority);
			em.persist(certificatePriorityEntity);
			return certificatePriorityEntity;
		}
	}

	
	
	public List<SkillsCertificateEntity> getCertificatesBySkill(SkillsEntity skillsEntity) throws Exception {
		List<SkillsEntity> skillAsList = new ArrayList<SkillsEntity>();
		skillAsList.add(skillsEntity);
		return getCertificatesBySkills(skillAsList);
	}
	
	public List<SkillsCertificateEntity> getCertificatesBySkills(List<SkillsEntity> skillsEntities) throws Exception {
		TypedQuery<SkillsCertificateEntity> query = em.createNamedQuery(SkillsCertificateEntity.GET_ALL_CERTIFICATES_BY_SKILLS, SkillsCertificateEntity.class);
		query.setParameter(1, skillsEntities);
		return query.getResultList();
	}

	public List<SkillsUserCertificateEntity> getTargetCertificatesFromUser(SkillsUserEntity skillsUserEntity) throws Exception {
		TypedQuery<SkillsUserCertificateEntity> query = em.createNamedQuery(SkillsUserCertificateEntity.GET_TARGET_CERTIFICATES_OF_USER,
				SkillsUserCertificateEntity.class);
		query.setParameter(1, skillsUserEntity.getId());
		return query.getResultList();
	}

	@DcemTransactional
	public void removeSkillsFromCertificates(List<SkillsEntity> skillsEntities) throws Exception {
		for (SkillsCertificateEntity skillsCertificateEntity : getCertificatesBySkills(skillsEntities)) {
			skillsCertificateEntity.getAppliesForSkills().removeAll(skillsEntities);
		}
	}

	
	@DcemTransactional
	public void approveCertificates(List<SkillsCertificateEntity> certificates) throws Exception {
		for (SkillsCertificateEntity certificate : certificates) {
			certificate = getCertificateById(certificate.getId());
			certificate.setApprovalStatus(ApprovalStatus.APPROVED);
			em.merge(certificate);
		}
	}
}
