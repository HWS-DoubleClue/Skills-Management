package com.doubleclue.dcem.skills.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.core.gui.AutoDialogBean;
import com.doubleclue.dcem.core.gui.AutoViewAction;
import com.doubleclue.dcem.core.gui.AutoViewBean;
import com.doubleclue.dcem.core.gui.DcemDialog;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.doubleclue.dcem.core.logic.UserLogic;
import com.doubleclue.dcem.skills.entities.SkillsEntity;
import com.doubleclue.dcem.skills.entities.SkillsUserEntity;
import com.doubleclue.dcem.skills.entities.SkillsUserSkillEntity;
import com.doubleclue.dcem.skills.entities.enums.ApprovalStatus;
import com.doubleclue.dcem.skills.entities.enums.AvailabilityStatus;
import com.doubleclue.dcem.skills.entities.enums.SkillsLevel;
import com.doubleclue.dcem.skills.entities.enums.SkillsStatus;
import com.doubleclue.dcem.skills.exceptions.SkillsErrorCodes;
import com.doubleclue.dcem.skills.exceptions.SkillsException;
import com.doubleclue.dcem.skills.logic.SkillsEmailLogic;
import com.doubleclue.dcem.skills.logic.SkillsLevelLogic;
import com.doubleclue.dcem.skills.logic.SkillsLogic;
import com.doubleclue.dcem.skills.logic.SkillsModule;
import com.doubleclue.dcem.skills.logic.SkillsUserLogic;
import com.doubleclue.dcem.skills.utils.SkillsUtils;

@SessionScoped
@Named("mySkillsDialog")
public class MySkillsDialog extends DcemDialog {

	private Logger logger = LogManager.getLogger(MySkillsDialog.class);

	private static final long serialVersionUID = 1L;

	@Inject
	AutoDialogBean autoDialogBean;

	@Inject
	UserLogic userLogic;

	@Inject
	AutoViewBean autoViewBean;

	@Inject
	OperatorSessionBean operatorSessionBean;

	@Inject
	SkillsLogic skillsLogic;

	@Inject
	SkillsUserLogic skillsUserLogic;

	@Inject
	SkillsLevelLogic skillsLevelLogic;

	@Inject
	SkillsTreeTable skillsTreeTable;

	@Inject
	SkillsEmailLogic skillsEmailLogic;

	@Inject
	SkillsModule skillsModule;

	private ResourceBundle resourceBundle;

	private SkillsUserEntity skillsUserEntity;
	SkillsEntity skillsEntity;
	private SkillsEntity skillRoot;

	private List<SkillsUserSkillEntity> userSkills;
	private SkillsUserSkillEntity skillsUserSkillEntity;

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
		if (skillsEntity.getStatus().equals(ApprovalStatus.PENDING) && skillsModule.getModulePreferences().isRequestedSkillAndCertificateImmediateAvailable() == false) {
			throw new SkillsException(SkillsErrorCodes.CANNOT_ADD_NON_APPROVED_SKILL, "");
		}
		skillRoot = skillsLogic.getSkillRoot();
		if (skillsEntity.getId() == skillRoot.getId()) {
			throw new SkillsException(SkillsErrorCodes.CANNOT_CHANGE_SKILL_ROOT, "");
		}
		skillsUserEntity = skillsUserLogic.getSkillsUserById(operatorSessionBean.getDcemUser().getId());
		skillsUserSkillEntity = null;
		for (SkillsUserSkillEntity skillsUserSkillEntity_ : skillsUserEntity.getSkills()) {
			if (skillsUserSkillEntity_.getSkill().getId() == skillsEntity.getId()) {
				skillsUserSkillEntity = skillsUserSkillEntity_;
				break;
			}
		}
		if (skillsUserSkillEntity == null) {
			skillsUserSkillEntity = new SkillsUserSkillEntity(skillsUserEntity, skillsEntity, SkillsLevel.No, SkillsStatus.OWNS);
			skillsUserSkillEntity.setSkillsUserEntity(skillsUserEntity);
		}
		userSkills = skillsUserEntity.getSkills();
	}

	@Override
	public boolean actionOk() throws Exception {
		SkillsUserEntity oldSkillsUser = loadOldData(skillsUserEntity);
		if (skillsUserSkillEntity.getId() == null) {
			if (skillsUserSkillEntity.getLevel() != SkillsLevel.No) {
				userSkills.add(skillsUserSkillEntity);
			} else {
				return true;
			}
		} else {
			if (skillsUserSkillEntity.getLevel() == SkillsLevel.No) {
				userSkills.remove(skillsUserSkillEntity);
			}
		}
		skillsUserEntity.setSkills(userSkills);
		skillsUserLogic.updateUserSkillEntity(skillsUserEntity);
		skillsEmailLogic.notifySkillsChange(oldSkillsUser, skillsUserEntity);
		return true;
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
		userSkills = null;
		skillsUserSkillEntity = null;
	}

	public List<SelectItem> getSkillsStatusSelection() {
		List<SelectItem> skillsStatusSelection = new ArrayList<SelectItem>();
		for (SkillsStatus status : SkillsStatus.values()) {
			skillsStatusSelection.add(new SelectItem(status, status.getLocaleText()));
		}
		return skillsStatusSelection;
	}

	public List<SelectItem> getSkillsLevelSelection() {
		List<SelectItem> skillsLevelSelection = new ArrayList<SelectItem>();
		for (SkillsLevel level : SkillsLevel.values()) {
			skillsLevelSelection.add(new SelectItem(level, level.getLocaleText()));
		}
		return skillsLevelSelection;
	}

	public boolean isAvailableFrom() {
		return AvailabilityStatus.AvailableFrom.equals(skillsUserEntity.getAvailability());
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

	public SkillsUserSkillEntity getSkillsUserSkillEntity() {
		return skillsUserSkillEntity;
	}

	public void setSkillsUserSkillEntity(SkillsUserSkillEntity skillsUserSkillEntity) {
		this.skillsUserSkillEntity = skillsUserSkillEntity;
	}
}
