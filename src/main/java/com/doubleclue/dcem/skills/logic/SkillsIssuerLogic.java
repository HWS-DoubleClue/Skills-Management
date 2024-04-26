package com.doubleclue.dcem.skills.logic;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.entities.DcemAction;
import com.doubleclue.dcem.core.jpa.DcemTransactional;
import com.doubleclue.dcem.core.logic.AuditingLogic;
import com.doubleclue.dcem.skills.entities.SkillsIssuerEntity;

@ApplicationScoped
@Named("skillsIssuerLogic")
public class SkillsIssuerLogic {

	@Inject
	EntityManager em;
	
	@Inject
	AuditingLogic auditingLogic;

	@DcemTransactional
	public void addOrUpdateSkillsIssuer(SkillsIssuerEntity skillsIssuerEntity, DcemAction dcemAction) {
		if (dcemAction.getAction().equals(DcemConstants.ACTION_ADD)) {
			auditingLogic.addAudit(dcemAction, skillsIssuerEntity);
			em.persist(skillsIssuerEntity);
		} else {
			auditingLogic.addAudit(dcemAction, skillsIssuerEntity);
			em.merge(skillsIssuerEntity);
		}
	}

	public SkillsIssuerEntity getIssuerByName(String name) throws Exception {
		TypedQuery<SkillsIssuerEntity> query = em.createNamedQuery(SkillsIssuerEntity.GET_BY_NAME, SkillsIssuerEntity.class);
		query.setParameter(1, name);
		try {
			return query.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	public List<String> getCompleteIssuer(String name, int max) throws Exception {
		TypedQuery<String> query = em.createNamedQuery(SkillsIssuerEntity.GET_ISSUER_NAME_BY_NAME, String.class);
		query.setParameter(1, "%" + name.toLowerCase() + "%");
		query.setMaxResults(max);
		return query.getResultList();
	}
}
