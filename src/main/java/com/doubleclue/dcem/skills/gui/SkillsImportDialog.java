package com.doubleclue.dcem.skills.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.NoResultException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.event.TabChangeEvent;

import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.gui.AutoViewAction;
import com.doubleclue.dcem.core.gui.DcemDialog;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.doubleclue.dcem.core.logic.UserLogic;
import com.doubleclue.dcem.skills.entities.SkillsCertificatePriorityEntity;
import com.doubleclue.dcem.skills.entities.SkillsEntity;
import com.doubleclue.dcem.skills.entities.SkillsJobProfileEntity;
import com.doubleclue.dcem.skills.entities.SkillsLevelEntity;
import com.doubleclue.dcem.skills.entities.SkillsUserCertificateEntity;
import com.doubleclue.dcem.skills.entities.SkillsUserEntity;
import com.doubleclue.dcem.skills.entities.SkillsUserSkillEntity;
import com.doubleclue.dcem.skills.entities.enums.SkillsLevel;
import com.doubleclue.dcem.skills.entities.enums.SkillsStatus;
import com.doubleclue.dcem.skills.logic.SkillsCertificateLogic;
import com.doubleclue.dcem.skills.logic.SkillsLogic;
import com.doubleclue.dcem.skills.logic.SkillsModule;
import com.doubleclue.dcem.skills.logic.SkillsUserLogic;
import com.doubleclue.dcem.skills.utils.SkillsUtils;

@SessionScoped
@Named("skillsImportDialog")
public class SkillsImportDialog extends DcemDialog {

	private Logger logger = LogManager.getLogger(SkillsImportDialog.class);
	private static final long serialVersionUID = 1L;

	@Inject
	OperatorSessionBean operatorSessionBean;

	@Inject
	SkillsUserLogic skillsUserLogic;

	@Inject
	SkillsLogic skillsLogic;

	@Inject
	UserLogic userLogic;

	@Inject
	SkillsCertificateLogic skillsCertificateLogic;

	@Inject
	SkillsDashboardView skillsDashboardView;

	@Inject
	SkillsJobProfileEntityView skillsJobProfileEntityView;

	private ResourceBundle resourceBundle;

	SkillsJobProfileEntity skillsJobProfileEntity;
	SkillsUserEntity skillsUserEntity;
	boolean skillsTab;
	boolean certificateTab;

	String userLoginId;

	HashMap<Integer, SkillsUserSkillEntity> userSkills;
	List<SkillsUserSkillEntity> skillsNotInJobProfile;
	Set<SkillsLevelEntity> skillsInJobProfile;

	boolean certificateStatus;
	HashMap<Integer, SkillsUserCertificateEntity> userCertificates;
	List<SkillsUserCertificateEntity> certificatesNotInJobProfile;
	Set<SkillsCertificatePriorityEntity> certificatesInJobProfile;

	boolean dashboardView;

	@PostConstruct
	private void init() {
		resourceBundle = JsfUtils.getBundle(SkillsModule.RESOURCE_NAME, operatorSessionBean.getLocale());
	}

	@Override
	public void show(DcemView dcemView, AutoViewAction autoViewAction) {
		if (dcemView.getDisplayName().equals(skillsDashboardView.getDisplayName())) {
			dashboardView = true;
		} else {
			dashboardView = false;
		}
		userLoginId = null;
		skillsTab = true;
		certificateTab = false;
		skillsJobProfileEntity = (SkillsJobProfileEntity) dcemView.getActionObject();
		skillsInJobProfile = skillsJobProfileEntity.getSkillLevels();
		certificatesInJobProfile = skillsJobProfileEntity.getCertificatesPriorities();
		skillsJobProfileEntity.getCertificatesPriorities().size(); // fix lazy initialisation
		initializeHashMap();
	}

	public void initializeHashMap() {
		try {
			List<SkillsUserSkillEntity> allUserSkills = new ArrayList<SkillsUserSkillEntity>();
			List<SkillsUserCertificateEntity> allUserCertificates = new ArrayList<SkillsUserCertificateEntity>();
			userSkills = new HashMap<Integer, SkillsUserSkillEntity>();
			userCertificates = new HashMap<Integer, SkillsUserCertificateEntity>();
			skillsNotInJobProfile = new ArrayList<SkillsUserSkillEntity>();
			certificatesNotInJobProfile = new ArrayList<SkillsUserCertificateEntity>();

			if (dashboardView == true) {
				skillsUserEntity = skillsUserLogic.retrieveSkillsUserByDcemUser(skillsUserEntity.getDcemUser());
			} else {
				if (userLoginId == null) {
					return;
				}
				DcemUser dcemUser = userLogic.getUser(userLoginId);
				skillsUserEntity = skillsUserLogic.retrieveSkillsUserByDcemUser(dcemUser);
			}

			for (SkillsLevelEntity skillsLevelEntity : skillsInJobProfile) {
				try {
					userSkills.put(skillsLevelEntity.getSkill().getId(),
							skillsLogic.getSkillFromUserById(skillsUserEntity, skillsLevelEntity.getSkill().getId()));
				} catch (NoResultException e) {
					userSkills.put(skillsLevelEntity.getSkill().getId(),
							new SkillsUserSkillEntity(skillsUserEntity, skillsLevelEntity.getSkill(), SkillsLevel.No, SkillsStatus.OWNS));
				}
			}
			for (SkillsCertificatePriorityEntity skillsCertificatePriorityEntity : certificatesInJobProfile) {
				try {
					userCertificates.put(skillsCertificatePriorityEntity.getCertificateEntity().getId(), skillsCertificateLogic
							.getCertificateFromUserById(skillsUserEntity.getId(), skillsCertificatePriorityEntity.getCertificateEntity().getId()));
				} catch (NoResultException e) {
					userCertificates.put(skillsCertificatePriorityEntity.getCertificateEntity().getId(), null);
				}
			}
			allUserSkills = skillsUserLogic.getSkillsOfUser(skillsUserEntity.getDcemUser().getId());
			allUserCertificates = skillsCertificateLogic.getAllCertificatesFromUser(skillsUserEntity);

			for (SkillsUserSkillEntity skillsUserSkillEntity : allUserSkills) {
				if (userSkills.containsValue(skillsUserSkillEntity)) {
					continue;
				} else {
					skillsNotInJobProfile.add(skillsUserSkillEntity);
				}
			}
			for (SkillsUserCertificateEntity skillsUserCertificateEntity : allUserCertificates) {
				if (userCertificates.containsValue(skillsUserCertificateEntity)) {
					continue;
				} else {
					certificatesNotInJobProfile.add(skillsUserCertificateEntity);
				}

			}
		} catch (Exception e) {
			JsfUtils.addErrorMessage(resourceBundle, "error.global");
		}
	}

	@Override
	public boolean actionOk() throws Exception {
		List<SkillsUserSkillEntity> newSkills = convertSkillsHashmapToList();
		List<SkillsUserCertificateEntity> newCertificates = convertCertificatesHashmapToList();
		newSkills.addAll(skillsNotInJobProfile);
		newSkills.removeAll(getToDeleteTargetSkills(newSkills));
		newCertificates.addAll(certificatesNotInJobProfile);
		newCertificates.removeAll(getToDeleteTargetCertificates(newCertificates));

		Iterator<SkillsUserSkillEntity> itr = newSkills.iterator();
		while (itr.hasNext()) {
			SkillsUserSkillEntity skillsUserSkillEntity = itr.next();
			if (skillsUserSkillEntity.getLevel().equals(SkillsLevel.No)) {
				itr.remove();
			}
		}
		skillsUserEntity.setSkills(newSkills);
		skillsUserEntity.setCertificates(newCertificates);
		skillsUserLogic.addOrUpdateSkillsUserEntity(skillsUserEntity, this.getAutoViewAction().getDcemAction());
		return true;
	}

	@Override
	public void leavingDialog() {
		skillsUserEntity = null;
		userSkills = null;
		skillsInJobProfile = null;
		skillsNotInJobProfile = null;
		certificatesInJobProfile = null;
		certificatesNotInJobProfile = null;
		userLoginId = null;
	}

	public List<String> completeSkillsUser(String name) {
		try {
			List<String> userNames;
			userNames = skillsUserLogic.getCompleteSkillsUser(name, 20);
			if (userNames == null) {
				JsfUtils.addErrorMessage(resourceBundle, "skillsUserDialog.errorDisplayUser");
			}
			return userNames;
		} catch (Exception e) {
			logger.error("", e);
			JsfUtils.addErrorMessage(e.getMessage());
		}
		return null;
	}

	public List<SkillsUserSkillEntity> convertSkillsHashmapToList() {
		List<SkillsUserSkillEntity> skills = new ArrayList<SkillsUserSkillEntity>();
		for (SkillsUserSkillEntity skillsUserSkillEntity : userSkills.values()) {
			skills.add(skillsUserSkillEntity);
		}
		return skills;
	}

	public List<SkillsUserCertificateEntity> convertCertificatesHashmapToList() {
		List<SkillsUserCertificateEntity> certificates = new ArrayList<SkillsUserCertificateEntity>();
		for (SkillsUserCertificateEntity skillsUserCertificateEntity : userCertificates.values()) {
			if (skillsUserCertificateEntity == null) {
				continue;
			}
			certificates.add(skillsUserCertificateEntity);
		}
		return certificates;
	}

	public String getLevelInStars(SkillsLevel skillsLevel) {
		if (skillsLevel == null) {
			return "";
		}
		return SkillsUtils.convertLevelToStars(skillsLevel);
	}

	public List<SkillsUserSkillEntity> getToDeleteTargetSkills(List<SkillsUserSkillEntity> newSkills) throws Exception {
		List<SkillsUserSkillEntity> toDeleteTargetSkills = new ArrayList<SkillsUserSkillEntity>();
		List<SkillsUserSkillEntity> targetSkillsFromUser = skillsUserLogic.getTargetSkillsFromUser(skillsUserEntity);
		for (SkillsUserSkillEntity newSkill : newSkills) {
			if (newSkill.getStatus() != SkillsStatus.OWNS) {
				continue;
			}
			for (SkillsUserSkillEntity targetSkill : targetSkillsFromUser) {
				if (newSkill.getSkill().getId().equals(targetSkill.getSkill().getId())) {
					if (newSkill.getLevel().ordinal() >= targetSkill.getLevel().ordinal()) {
						toDeleteTargetSkills.add(targetSkill);
					}
				}
			}
		}
		return toDeleteTargetSkills;
	}

	public List<SkillsUserCertificateEntity> getToDeleteTargetCertificates(List<SkillsUserCertificateEntity> newCertificates) throws Exception {
		List<SkillsUserCertificateEntity> toDeleteTargetCertificates = new ArrayList<SkillsUserCertificateEntity>();
		List<SkillsUserCertificateEntity> targetCertificatesFromUser = skillsCertificateLogic.getTargetCertificatesFromUser(skillsUserEntity);
		for (SkillsUserCertificateEntity newCertificate : newCertificates) {
			if (newCertificate.getStatus() != SkillsStatus.OWNS) {
				continue;
			}
			for (SkillsUserCertificateEntity targetSkill : targetCertificatesFromUser) {
				if (newCertificate.getSkillsCertificateEntity().getId().equals(targetSkill.getSkillsCertificateEntity().getId())) {
					toDeleteTargetCertificates.add(targetSkill);
				}
			}
		}
		return toDeleteTargetCertificates;
	}

	public void importAll() {
		for (SkillsLevelEntity skillsLevelEntity : skillsInJobProfile) {
			SkillsUserSkillEntity skillsUserSkillEntity = userSkills.getOrDefault(skillsLevelEntity.getSkill().getId(),
					new SkillsUserSkillEntity(skillsUserEntity, skillsLevelEntity.getSkill(), null, SkillsStatus.OWNS));
			if (skillsUserSkillEntity.getLevel().ordinal() >= skillsLevelEntity.getLevel().ordinal()) {
				continue;
			}
			skillsUserSkillEntity.setLevel(skillsLevelEntity.getLevel());
			userSkills.put(skillsLevelEntity.getSkill().getId(), skillsUserSkillEntity);
		}
		for (SkillsCertificatePriorityEntity skillsCertificatePriorityEntity : certificatesInJobProfile) {
			SkillsUserCertificateEntity skillsUserCertificateEntity = userCertificates.get(skillsCertificatePriorityEntity.getCertificateEntity().getId());
			if (skillsUserCertificateEntity == null) {
				skillsUserCertificateEntity = new SkillsUserCertificateEntity(skillsUserEntity, skillsCertificatePriorityEntity.getCertificateEntity(),
						SkillsStatus.OWNS);
			}
			userCertificates.put(skillsCertificatePriorityEntity.getCertificateEntity().getId(), skillsUserCertificateEntity);
		}
	}

	public void importToUser(Object importObject) {
		if (skillsTab) {
			SkillsLevelEntity skillsLevelEntity = (SkillsLevelEntity) importObject;
			SkillsUserSkillEntity skillsUserSkillEntity = userSkills.getOrDefault(skillsLevelEntity.getSkill().getId(),
					new SkillsUserSkillEntity(skillsUserEntity, skillsLevelEntity.getSkill(), null, SkillsStatus.OWNS));
			skillsUserSkillEntity.setLevel(skillsLevelEntity.getLevel());
			userSkills.put(skillsLevelEntity.getSkill().getId(), skillsUserSkillEntity);
		}
		if (certificateTab) {
			SkillsCertificatePriorityEntity skillsCertificatePriorityEntity = (SkillsCertificatePriorityEntity) importObject;
			SkillsUserCertificateEntity skillsUserCertificateEntity = userCertificates.get(skillsCertificatePriorityEntity.getCertificateEntity().getId());
			if (skillsUserCertificateEntity == null) {
				skillsUserCertificateEntity = new SkillsUserCertificateEntity(skillsUserEntity, skillsCertificatePriorityEntity.getCertificateEntity(),
						SkillsStatus.OWNS);
			}
			userCertificates.put(skillsCertificatePriorityEntity.getCertificateEntity().getId(), skillsUserCertificateEntity);
		}
	}

	public boolean showImportArrowSkills(SkillsLevelEntity skillsLevelEntity) {
		SkillsUserSkillEntity skillsUserSkillEntity = userSkills.get((skillsLevelEntity.getSkill().getId()));
		if (skillsUserSkillEntity == null) {
			return true;
		}
		if (skillsUserSkillEntity.getLevel().ordinal() >= skillsLevelEntity.getLevel().ordinal()) {
			return false;
		}
		return true;
	}

	public boolean showImportArrowCertificates(SkillsCertificatePriorityEntity skillsCertificatePriorityEntity) {
		SkillsUserCertificateEntity skillsUserCertificateEntity = userCertificates.get(skillsCertificatePriorityEntity.getCertificateEntity().getId());
		if (skillsUserCertificateEntity == null) {
			return true;
		}
		return false;
	}

	public String showCertificateStatus(SkillsCertificatePriorityEntity skillsCertificatePriorityEntity) {
		SkillsUserCertificateEntity skillsUserCertificateEntity = userCertificates.get(skillsCertificatePriorityEntity.getCertificateEntity().getId());
		if (skillsUserCertificateEntity == null) {
			return "\u274c";
		}
		return "\u2705";
	}

	public int getMatchFromUser() {
		try {
			if (skillsUserEntity == null) {
				return 0;
			}
			SkillsUserEntity skillsUserMatch = new SkillsUserEntity();
			skillsUserMatch.setSkills(new ArrayList<SkillsUserSkillEntity>(userSkills.values()));
			List<SkillsUserCertificateEntity> matchCertificates = new ArrayList<SkillsUserCertificateEntity>();
			for (SkillsUserCertificateEntity skillsUserCertificateEntity : userCertificates.values()) {
				if (skillsUserCertificateEntity == null) {
					continue;
				}
				matchCertificates.add(skillsUserCertificateEntity);
			}
			skillsUserMatch.setCertificates(matchCertificates);
			return SkillsUserLogic.computeMatch(skillsUserMatch, skillsJobProfileEntity);
		} catch (Exception e) {
			logger.error("", e);
			JsfUtils.addErrorMessage(e.getMessage());
		}
		return 0;
	}

	public void onTabChange(TabChangeEvent<?> event) {
		if (event.getTab().getId().equals("skillsTab")) {
			skillsTab = true;
			certificateTab = false;
		} else if (event.getTab().getId().equals("certificatesTab")) {
			certificateTab = true;
			skillsTab = false;
		}
	}

	public String getJobProfileSkillName(SkillsLevelEntity skillsLevelEntity) {
		try {
			SkillsEntity skillsEntity = skillsLogic.getSkillById(skillsLevelEntity.getSkill().getId());
			String skillName = skillsEntity.getName();
			if (skillName.length() >= 22) {
				skillName = skillName.substring(0, 22) + "[...]";
			}
			return skillName;
		} catch (Exception e) {
			logger.error("", e);
			JsfUtils.addErrorMessage(resourceBundle, "error.global");
			return "";
		}
	}

	public SkillsJobProfileEntity getSkillsJobProfileEntity() {
		return skillsJobProfileEntity;
	}

	public void setSkillsJobProfileEntity(SkillsJobProfileEntity skillsJobProfileEntity) {
		this.skillsJobProfileEntity = skillsJobProfileEntity;
	}

	public HashMap<Integer, SkillsUserSkillEntity> getUserSkills() {
		return userSkills;
	}

	public void setUserSkills(HashMap<Integer, SkillsUserSkillEntity> userSkills) {
		this.userSkills = userSkills;
	}

	public String getUserLoginId() {
		return userLoginId;
	}

	public void setUserLoginId(String userLoginId) {
		this.userLoginId = userLoginId;
	}

	public boolean isCertificateStatus() {
		return certificateStatus;
	}

	public void setCertificateStatus(boolean certificateStatus) {
		this.certificateStatus = certificateStatus;
	}

	@Override
	public String getHeight() {
		return "35em";
	}

	@Override
	public String getWidth() {
		return "70em";
	}

	public boolean isDashboardView() {
		return dashboardView;
	}

	public void setDashboardView(boolean dashboardView) {
		this.dashboardView = dashboardView;
	}

	public SkillsUserEntity getSkillsUserEntity() {
		return skillsUserEntity;
	}

	public void setSkillsUserEntity(SkillsUserEntity skillsUserEntity) {
		this.skillsUserEntity = skillsUserEntity;
	}
}
