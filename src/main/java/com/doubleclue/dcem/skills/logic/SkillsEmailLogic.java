package com.doubleclue.dcem.skills.logic;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.gui.DcemApplicationBean;
import com.doubleclue.dcem.core.jpa.DcemTransactional;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.doubleclue.dcem.core.logic.UserLogic;
import com.doubleclue.dcem.core.tasks.EmailTask;
import com.doubleclue.dcem.core.tasks.TaskExecutor;
import com.doubleclue.dcem.skills.entities.SkillsCertificateEntity;
import com.doubleclue.dcem.skills.entities.SkillsComparableInterface;
import com.doubleclue.dcem.skills.entities.SkillsEntity;
import com.doubleclue.dcem.skills.entities.SkillsUserCertificateEntity;
import com.doubleclue.dcem.skills.entities.SkillsUserEntity;
import com.doubleclue.dcem.skills.entities.SkillsUserJobProfileEntity;
import com.doubleclue.dcem.skills.entities.SkillsUserSkillEntity;
import com.doubleclue.dcem.skills.model.SkillsCompareResult;

@ApplicationScoped
@Named("skillsEmailLogic")
public class SkillsEmailLogic {

	private Logger logger = LogManager.getLogger(SkillsEmailLogic.class);

	@Inject
	OperatorSessionBean operatorSessionBean;

	@Inject
	DcemApplicationBean dcemApplicationBean;

	@Inject
	EntityManager em;

	@Inject
	SkillsModule skillsModule;

	@Inject
	UserLogic userLogic;

	@Inject
	SkillsUserLogic skillsUserLogic;

	@Inject
	SkillsCertificateLogic skillsCertificateLogic;

	@Inject
	SkillsJobProfileEntityLogic skillsJobProfileEntityLogic;

	@Inject
	TaskExecutor taskExecutor;

	public void notifySkillsRequest(SkillsEntity skillsEntity, String comment) {
		if (skillsModule.getModulePreferences().isDisableEmailNotifications()) {
			return;
		}
		try {
			List<DcemUser> recipients = getRecipientsForRequests();
			if (recipients.isEmpty() == false) {
				Map<String, Object> data = new HashMap<String, Object>();
				data.put("Requester", operatorSessionBean.getDcemUser().getDisplayName());
				data.put("Parent", skillsEntity.getParent() == null ? "" : skillsEntity.getParent().getName());
				data.put("Skill", skillsEntity.getName());
				data.put("Description", skillsEntity.getDescription());
				data.put("TenantManagementUrl", dcemApplicationBean.getDcemManagementUrl(null));
				comment = comment == null ? "" : comment.trim();
				data.put("Comment", comment);
				taskExecutor.execute(new EmailTask(recipients, data, SkillsConstants.SKILLS_NEWSKILL_REQUEST_TEMPLATE,
						SkillsConstants.SKILLS_NEWSKILL_REQUEST_SUBJECT, null));
			}
		} catch (Exception e) {
			logger.error("Could not send request email for skill: " + skillsEntity.getNameWithParent(), e);
		}
	}

	public void notifyCertificateRequest(SkillsCertificateEntity skillsCertificateEntity) {
		if (skillsModule.getModulePreferences().isDisableEmailNotifications()) {
			return;
		}
		try {
			List<DcemUser> recipients = getRecipientsForRequests();
			if (recipients.isEmpty() == false) {
				Map<String, Object> data = new HashMap<String, Object>();
				data.put("Requester", operatorSessionBean.getDcemUser().getLoginId());
				data.put("Name", skillsCertificateEntity.getName());
				data.put("Description", skillsCertificateEntity.getDescription());
				data.put("TenantManagementUrl", dcemApplicationBean.getDcemManagementUrl(null));
				if (skillsCertificateEntity.getIssuer() != null) {
					data.put("Issuer", skillsCertificateEntity.getIssuer().getName());
				} else {
					data.put("Issuer", null);
				}
				data.put("appliedForSkills", skillsCertificateEntity.getAppliesForSkills());
				taskExecutor.execute(new EmailTask(recipients, data, SkillsConstants.SKILLS_NEWCERTIFICATE_REQUEST_TEMPLATE,
						SkillsConstants.SKILLS_NEWCERTIFICATE_REQUEST_SUBJECT, null));
			}
		} catch (Exception e) {
			logger.error("Could not send request email for certificate: " + skillsCertificateEntity.getName(), e);
		}
	}

	private List<DcemUser> getRecipientsForRequests() throws Exception {
		List<DcemUser> recipients = skillsUserLogic.getReciepientsForRequests();
		if (skillsModule.getModulePreferences().isNotificationForHod() == true && hasHeadOf(operatorSessionBean.getDcemUser())) {
			recipients.add(operatorSessionBean.getDcemUser().getDepartment().getHeadOf());
		}
		return recipients;
	}

	public void notifySkillsChange(SkillsUserEntity oldSkillsUserEntity, SkillsUserEntity skillsUserEntity) {
		if (skillsModule.getModulePreferences().isDisableEmailNotifications()) {
			return;
		}
		try {
			skillsUserEntity = skillsUserLogic.getSkillsUserById(skillsUserEntity.getId());
			Set<DcemUser> allRecipients = new HashSet<>();
			if (skillsModule.getModulePreferences().isNotificationForHod() == true && hasHeadOf(skillsUserEntity.getDcemUser()) 
					&& operatorSessionBean.getDcemUser().equals(skillsUserEntity.getDcemUser().getDepartment().getHeadOf()) == false) {
				allRecipients.add(skillsUserEntity.getDcemUser().getDepartment().getHeadOf());
			}
			if (skillsUserEntity.getReportsTo() != null && skillsUserEntity.getReportsTo().equals(operatorSessionBean.getDcemUser()) == false) {
				allRecipients.add(skillsUserEntity.getReportsTo());
			}
			if (skillsUserEntity.getDcemUser().equals(operatorSessionBean.getDcemUser()) == false) {
				allRecipients.add(skillsUserEntity.getDcemUser());
			}
			
			List<DcemUser> recipients = new ArrayList<DcemUser>();
			for (DcemUser recipient : allRecipients) {
				SkillsUserEntity skillsRecipient = skillsUserLogic.getSkillsUserById(recipient.getId());
				if (skillsRecipient == null || skillsRecipient.isDisableNotifications() == false) {
					recipients.add(recipient);
				}
			}

			SkillsCompareResult skillResult = compareSkillObjectLists(oldSkillsUserEntity.getSkills(), skillsUserEntity.getSkills());
			SkillsCompareResult certificateResult = compareSkillObjectLists(oldSkillsUserEntity.getCertificates(), skillsUserEntity.getCertificates());
			SkillsCompareResult jobprofileResult = compareSkillObjectLists(oldSkillsUserEntity.getUserJobProfiles(), skillsUserEntity.getUserJobProfiles());

			Set<String> changedSkills = Stream.concat(skillResult.getOldList().stream(), skillResult.getNewList().stream())
					.map(userSkill -> ((SkillsUserSkillEntity) userSkill).getSkill().getNameWithParent()).collect(Collectors.toSet());
			Set<String> changedCertificates = Stream.concat(certificateResult.getOldList().stream(), certificateResult.getNewList().stream())
					.map(userCert -> ((SkillsUserCertificateEntity) userCert).getSkillsCertificateEntity().getName()).collect(Collectors.toSet());
			Set<String> changedJobProfiles = Stream.concat(jobprofileResult.getOldList().stream(), jobprofileResult.getNewList().stream())
					.map(userJobprofile -> ((SkillsUserJobProfileEntity) userJobprofile).getJobProfile().getName()).collect(Collectors.toSet());

			for (DcemUser recipient : recipients) {
				Map<String, Object> data = new HashMap<String, Object>();
				data.put("changedSkills", changedSkills);
				data.put("changedCertificates", changedCertificates);
				data.put("changedJobProfiles", changedJobProfiles);
//				data.put("oldSkills", oldSkillsUserEntity.getSkills());
//				data.put("newSkills", skillsUserLogic.getSkillsOfUser(skillsUserEntity.getId()));
//				data.put("oldCertificates", oldSkillsUserEntity.getCertificates());
//				data.put("newCertificates", skillsCertificateLogic.getAllCertificatesFromUserWithFiles(skillsUserEntity));
//				data.put("oldJobProfiles", oldSkillsUserEntity.getUserJobProfiles());
//				data.put("newJobProfiles", skillsJobProfileEntityLogic.getJobProfilesOfUser(skillsUserEntity.getId()));
				data.put("SkillsUser", skillsUserEntity.getDcemUser().getDisplayName());
				data.put("Operator", operatorSessionBean.getDcemUser().getDisplayName());
				data.put("TenantManagementUrl", dcemApplicationBean.getDcemManagementUrl(null));
				data.put("Recipient", recipient.getDisplayName());
				taskExecutor.execute(new EmailTask(Arrays.asList(recipient), data, SkillsConstants.SKILLS_NOTIFICATION_TEMPLATE,
						SkillsConstants.SKILLS_NOTIFICATION_SUBJECT, null));
			}
		} catch (Exception e) {
			logger.error("Could not send email for changes of skilluser: " + skillsUserEntity.getDcemUser().getLoginId(), e);
		}
	}

	private <T extends SkillsComparableInterface> SkillsCompareResult compareSkillObjectLists(List<T> oldList, List<T> newList) {
		List<T> oldChangedObjs = new ArrayList<T>();
		List<T> newChangedObjs = new ArrayList<T>();
		List<T> unchangedObjs = new ArrayList<T>();

		for (T oldObj : oldList) {
			boolean hasChanges = true;
			for (T newObj : newList) {
				if (newObj.compareObject(oldObj)) {
					hasChanges = false;
					break;
				}
			}
			if (hasChanges) {
				oldChangedObjs.add(oldObj);
			} else {
				unchangedObjs.add(oldObj);
			}
		}

		for (T newObj : newList) {
			boolean hasChanges = true;
			for (T unchangedObj : unchangedObjs) {
				if (newObj.compareObject(unchangedObj)) {
					hasChanges = false;
					break;
				}
			}
			if (hasChanges) {
				newChangedObjs.add(newObj);
			}
		}
		SkillsCompareResult result = new SkillsCompareResult(oldChangedObjs, newChangedObjs);
		return result;
	}
	
	private boolean hasHeadOf(DcemUser user) {
		if (user.getDepartment() == null || user.getDepartment().getHeadOf() == null) {
			return false;
		}
		return true;
	}

	@DcemTransactional
	public void sendExpirationEmail() {
		if (skillsModule.getModulePreferences().isDisableEmailNotifications()) {
			return;
		}
		if (skillsModule.getModulePreferences().getFirstNotificationForExpiringCertificate() == 0) {
			return;
		}
		try {
			List<SkillsUserCertificateEntity> secondNotificationCertificates = skillsCertificateLogic.getSoonExpiredCertificateUser(LocalDate.now(),
					skillsModule.getModulePreferences().getSecondNotificationForExpiringCertificate(), 1);
			for (SkillsUserCertificateEntity skillsUserCertificateEntity : secondNotificationCertificates) {
				skillsUserCertificateEntity.setSendNotification(2);
				skillsCertificateLogic.updateSkillsUserCertificate(skillsUserCertificateEntity);
			}
			List<SkillsUserCertificateEntity> firstNotificationCertificates = skillsCertificateLogic.getSoonExpiredCertificateUser(LocalDate.now(),
					skillsModule.getModulePreferences().getFirstNotificationForExpiringCertificate(), 0);
			for (SkillsUserCertificateEntity skillsUserCertificateEntity : firstNotificationCertificates) {
				skillsUserCertificateEntity.setSendNotification(1);
				skillsCertificateLogic.updateSkillsUserCertificate(skillsUserCertificateEntity);
			}
			List<SkillsUserCertificateEntity> allCertificates = new ArrayList<SkillsUserCertificateEntity>();
			allCertificates.addAll(firstNotificationCertificates);
			allCertificates.addAll(secondNotificationCertificates);
			for (SkillsUserCertificateEntity skillsUserCertificateEntity : allCertificates) {
				if (skillsUserCertificateEntity.getSkillsUserEntity().isDisableNotifications()) {
					continue;
				}
				Map<String, Object> data = new HashMap<String, Object>();
				data.put("User", skillsUserCertificateEntity.getSkillsUserEntity().getDcemUser().getDisplayName());
				data.put("Certificate", skillsUserCertificateEntity.getSkillsCertificateEntity().getName());
				data.put("Date", skillsUserCertificateEntity);
				taskExecutor.execute(new EmailTask(Arrays.asList(skillsUserCertificateEntity.getSkillsUserEntity().getDcemUser()), data,
						SkillsConstants.SKILLS_EXPIRING_CERTIFICATE_TEMPLATE, SkillsConstants.SKILLS_EXPIRING_CERTIFICATE_SUBJECT, null));
			}
		} catch (Exception e) {
			logger.error("Could not send email for expiring certificates", e);
		}
	}
}
