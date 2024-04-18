package com.doubleclue.dcem.skills.gui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.core.gui.AutoViewAction;
import com.doubleclue.dcem.core.gui.DcemDialog;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.doubleclue.dcem.skills.entities.SkillsEntity;
import com.doubleclue.dcem.skills.entities.SkillsUserEntity;
import com.doubleclue.dcem.skills.entities.SkillsUserSkillEntity;
import com.doubleclue.dcem.skills.entities.enums.ApprovalStatus;
import com.doubleclue.dcem.skills.entities.enums.SkillsLevel;
import com.doubleclue.dcem.skills.entities.enums.SkillsStatus;
import com.doubleclue.dcem.skills.exceptions.SkillsErrorCodes;
import com.doubleclue.dcem.skills.exceptions.SkillsException;
import com.doubleclue.dcem.skills.logic.SkillsEmailLogic;
import com.doubleclue.dcem.skills.logic.SkillsLogic;
import com.doubleclue.dcem.skills.logic.SkillsModule;
import com.doubleclue.dcem.skills.logic.SkillsUserLogic;

@SessionScoped
@Named("mySkillsDialog")
public class MySkillsDialog extends DcemDialog {

	@SuppressWarnings("unused")
	private Logger logger = LogManager.getLogger(MySkillsDialog.class);

	private static final long serialVersionUID = 1L;

	@Inject
	OperatorSessionBean operatorSessionBean;

	@Inject
	SkillsLogic skillsLogic;

	@Inject
	SkillsUserLogic skillsUserLogic;

	@Inject
	SkillsEmailLogic skillsEmailLogic;

	@Inject
	SkillsModule skillsModule;

	@Inject
	private SkillsHierarchieView hierarchieView;

	private ResourceBundle resourceBundle;
	private SkillsUserEntity skillsUserEntity;
	private SkillsEntity skillsEntity;

	private List<StatusSkills> userStatusSkills;

	@PostConstruct
	private void init() {
		resourceBundle = JsfUtils.getBundle(SkillsModule.RESOURCE_NAME, operatorSessionBean.getLocale());
	}

	@Override
	public void show(DcemView dcemView, AutoViewAction autoViewAction) throws Exception {
		skillsEntity = (SkillsEntity) dcemView.getActionObject();
		if (skillsEntity.isObtainable() == false) {
			throw new SkillsException(SkillsErrorCodes.CANNOT_EDIT_NON_OBTAINABLE_SKILL, "");
		}
		if (skillsEntity.getApprovalStatus().equals(ApprovalStatus.PENDING)
				&& skillsModule.getModulePreferences().isRequestedSkillAndCertificateImmediateAvailable() == false) {
			throw new SkillsException(SkillsErrorCodes.CANNOT_ADD_NON_APPROVED_SKILL, "");
		}
		if (skillsEntity.getId() == skillsLogic.getSkillRoot().getId()) {
			throw new SkillsException(SkillsErrorCodes.CANNOT_CHANGE_SKILL_ROOT, "");
		}
		skillsUserEntity = skillsUserLogic.getSkillsUserById(hierarchieView.getDcemUser().getId());
		userStatusSkills = null;
	}

	@Override
	public boolean actionOk() throws Exception {
		SkillsUserEntity oldSkillsUser = loadOldData(skillsUserEntity);
		List<SkillsUserSkillEntity> userSkills = oldSkillsUser.getSkills().stream().filter(userSkill -> userSkill.getSkill().getId() != skillsEntity.getId())
				.collect(Collectors.toList());
		int ownedLevel = userStatusSkills.get(0).getSkillList().get(0).getLevel().ordinal();
		List<SkillsUserSkillEntity> userSkillsInDialog = extractDialogUserSkills();
		Set<String> userSkillsLowerThanOwned = getStatusOfSkillsLowerThanOwned(userSkillsInDialog, ownedLevel);
		if (userSkillsLowerThanOwned.isEmpty() != true) {
			JsfUtils.addErrorMessage(resourceBundle, "mySkillDialog.invalid.targetLowerThanOwned", String.join(", ", userSkillsLowerThanOwned));
			return false;
		}
		if (hasDuplicateLevel(userSkillsInDialog)) {
			JsfUtils.addErrorMessage(resourceBundle, "mySkillDialog.invalid.duplicateLevel");
			return false;
		}
		userSkills.addAll(userSkillsInDialog);

		skillsUserEntity.setSkills(userSkills);
		skillsUserLogic.updateUserSkillEntity(skillsUserEntity);
		skillsEmailLogic.notifySkillsChange(oldSkillsUser, skillsUserEntity);
		return true;
	}

	private List<SkillsUserSkillEntity> extractDialogUserSkills() {
		List<SkillsUserSkillEntity> userSkillsInDialog = new ArrayList<>();
		for (int i = 0; i < SkillsStatus.values().length; i++) {
			StatusSkills statusSkills = userStatusSkills.get(i);
			for (SkillsUserSkillEntity skillsUserSkillEntity : statusSkills.getSkillList()) {
				if (skillsUserSkillEntity.getLevel() != SkillsLevel.No) {
					userSkillsInDialog.add(skillsUserSkillEntity);
				}
			}
		}
		return userSkillsInDialog;
	}

	private boolean hasDuplicateLevel(List<SkillsUserSkillEntity> userSkills) {
		int[] usedLevels = new int[SkillsLevel.values().length];
		for (SkillsUserSkillEntity userSkill : userSkills) {
			usedLevels[userSkill.getLevel().ordinal()] += 1;
			if (usedLevels[userSkill.getLevel().ordinal()] > 1) {
				return true;
			}
		}
		return false;
	}

	private Set<String> getStatusOfSkillsLowerThanOwned(List<SkillsUserSkillEntity> userSkills, int ownedLevel) {
		Set<String> userSkillsLowerThanOwned = new HashSet<>();
		for (SkillsUserSkillEntity userSkillInDialog : userSkills) {
			if (userSkillInDialog.getStatus() != SkillsStatus.OWNS && userSkillInDialog.getLevel().ordinal() <= ownedLevel) {
				userSkillsLowerThanOwned.add(userSkillInDialog.getStatus().getLocaleText());
			}
		}
		return userSkillsLowerThanOwned;
	}

	private SkillsUserEntity loadOldData(SkillsUserEntity skillsUserEntity) throws Exception {
		SkillsUserEntity oldSkillsUser = new SkillsUserEntity();
		oldSkillsUser.setSkills(skillsUserLogic.getSkillsOfUser(skillsUserEntity.getId()));
		skillsUserLogic.detachObjectsFromSkillsUser(oldSkillsUser);
		return oldSkillsUser;
	}

	@Override
	public void leavingDialog() {
		skillsUserEntity = null;
		skillsEntity = null;
		userStatusSkills = null;
	}	

	public List<SelectItem> getSkillsLevelSelection() {
		List<SelectItem> skillsLevelSelection = new ArrayList<SelectItem>();
		for (SkillsLevel level : SkillsLevel.values()) {
			skillsLevelSelection.add(new SelectItem(level, level.getLocaleText()));
		}
		return skillsLevelSelection;
	}

	public List<StatusSkills> getUserSkills() {
		if (userStatusSkills == null && skillsUserEntity != null) {
			userStatusSkills = new ArrayList<StatusSkills>(SkillsStatus.values().length);
			for (int i = 0; i < SkillsStatus.values().length; i++) {
				userStatusSkills.add(new StatusSkills(SkillsStatus.values()[i]));
			}
			if (skillsEntity != null && hierarchieView.getMySkills() != null) {
				for (SkillsUserSkillEntity skillsUserSkillEntity : hierarchieView.getMySkills()) {
					if (skillsUserSkillEntity.getSkill().getId().equals(skillsEntity.getId())) {
						StatusSkills statusSkills = userStatusSkills.get(skillsUserSkillEntity.getStatus().ordinal());
						statusSkills.add(skillsUserSkillEntity);
					}
				}
			}
			for (int i = 0; i < SkillsStatus.values().length; i++) {
				if (userStatusSkills != null) {
					StatusSkills statusSkills = userStatusSkills.get(i);
					if (statusSkills.skillList.isEmpty()) {
						statusSkills.skillList.add(new SkillsUserSkillEntity(skillsUserEntity, skillsEntity, SkillsLevel.No, SkillsStatus.values()[i]));
					}
				}
			}
		}
		return userStatusSkills;
	}

	public SkillsUserEntity getSkillsUserEntity() {
		return skillsUserEntity;
	}

	public void setSkillsUserEntity(SkillsUserEntity skillsUserEntity) {
		this.skillsUserEntity = skillsUserEntity;
	}

	public String getSkillNameWithParent() {
		return skillsEntity.getNameWithParent();
	}

	public class StatusSkills {
		SkillsStatus skillStatus;
		List<SkillsUserSkillEntity> skillList;

		public StatusSkills(SkillsStatus skillStatus) {
			super();
			this.skillStatus = skillStatus;
			this.skillList = new ArrayList<>();
		}

		public void add(SkillsUserSkillEntity skillsUserSkillEntity) {
			skillList.add(skillsUserSkillEntity);
		}

		public SkillsStatus getSkillStatus() {
			return skillStatus;
		}

		public void setSkillStatus(SkillsStatus skillStatus) {
			this.skillStatus = skillStatus;
		}

		public List<SkillsUserSkillEntity> getSkillList() {
			return skillList;
		}

		public void setSkillList(List<SkillsUserSkillEntity> skillList) {
			this.skillList = skillList;
		}

		@Override
		public String toString() {
			return "skillStatus=" + skillStatus + ", skillList=" + skillList;
		}

	}

	public SkillsEntity getSkillsEntity() {
		return skillsEntity;
	}

	public void setSkillsEntity(SkillsEntity skillsEntity) {
		this.skillsEntity = skillsEntity;
	}
}
