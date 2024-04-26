package com.doubleclue.dcem.skills.gui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
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
import com.doubleclue.dcem.skills.entities.SkillsEntity;
import com.doubleclue.dcem.skills.entities.enums.ApprovalStatus;
import com.doubleclue.dcem.skills.logic.SkillsEmailLogic;
import com.doubleclue.dcem.skills.logic.SkillsLogic;
import com.doubleclue.dcem.skills.logic.SkillsModule;
import com.doubleclue.dcem.skills.logic.SkillsUserLogic;

@SessionScoped
@Named("skillsMergeDialog")
public class SkillsMergeDialog extends DcemDialog {

	private static final Logger logger = LogManager.getLogger(SkillsMergeDialog.class);

	@Inject
	AutoDialogBean autoDialogBean;

	@Inject
	AutoViewBean autoViewBean;

	@Inject
	SkillsModule skillsModule;

	@Inject
	OperatorSessionBean operatorSessionBean;

	@Inject
	SkillsLogic skillsLogic;

	@Inject
	SkillsHierarchieView skillsHierarchieView;

	@Inject
	SkillsEmailLogic skillsEmailLogic;

	@Inject
	SkillsUserLogic skillsUserLogic;

	private static final long serialVersionUID = 1L;
	private ResourceBundle resourceBundle;
	
	private SkillsEntity skillRoot;

	private String nameOfMainSkillWithParent;
	private String nameOfMergingSkillWithParent;

	@PostConstruct
	private void init() {
		resourceBundle = JsfUtils.getBundle(SkillsModule.RESOURCE_NAME, operatorSessionBean.getLocale());
	}

	@Override
	public boolean actionOk() throws Exception {
		try {
			SkillsEntity mainSkill = skillsLogic.getSkillByNameAndParent(nameOfMainSkillWithParent);
			SkillsEntity mergingSkill = skillsLogic.getSkillByNameAndParent(nameOfMergingSkillWithParent);

			if (mainSkill == null || mergingSkill == null) {
				JsfUtils.addErrorMessage(resourceBundle, "skillsMergeDialog.invalid.skillsNotSelected");
				return false;
			}
			if (mainSkill.getApprovalStatus().equals(ApprovalStatus.PENDING)) {
				JsfUtils.addErrorMessage(resourceBundle, "skillsMergeDialog.invalid.mergeIntoUnapprovedSkill");
				return false;
			}
			if (mergingSkill.equals(mainSkill)) {
				JsfUtils.addErrorMessage(resourceBundle, "skillsMergeDialog.invalid.mergeSkillEqualsMainSkill");
				return false;
			}
			if (isParentOf(mergingSkill, mainSkill)) {
				JsfUtils.addErrorMessage(resourceBundle, "skillsMergeDialog.invalid.mergeSkillIsParentOfMainSkill"); 
				return false;
			}
			String childName = getCommonChildName(mainSkill, mergingSkill);
			if (childName != null) {
				String errorMsg = String.format(JsfUtils.getStringSafely(resourceBundle, "skillsMergeDialog.invalid.sameChildName"), childName);
				JsfUtils.addErrorMessage(errorMsg); 
				return false;
			}
			skillsLogic.mergeSkills(getAutoViewAction().getDcemAction(),  mainSkill, mergingSkill);
			return true;
		} catch (Exception e) {
			logger.error("", e);
			JsfUtils.addErrorMessage(resourceBundle, "error.global");
			return false;
		}
	}

	@Override
	public void show(DcemView dcemView, AutoViewAction autoViewAction) throws Exception {
		skillRoot = skillsLogic.getSkillRoot();
		nameOfMainSkillWithParent = "";
		nameOfMergingSkillWithParent = "";
	}

	public void leavingDialog() {
		nameOfMainSkillWithParent = null;
		nameOfMergingSkillWithParent = null;
	}

	private boolean isParentOf(SkillsEntity parent, SkillsEntity child) {
		SkillsEntity childsParent = child.getParent();
		while (childsParent.equals(skillRoot) == false) {
			if (childsParent.equals(parent)) {
				return true;
			}
			childsParent = childsParent.getParent();
		}
		return false;
	}

	public String getCommonChildName(SkillsEntity skill, SkillsEntity otherSkill) {
		HashSet<String> skillNames = new HashSet<String>();
		for (SkillsEntity childOfOther : otherSkill.getChildren()) {
			skillNames.add(childOfOther.getName());
		}
		for (SkillsEntity child : skill.getChildren()) {
			if (skillNames.contains(child.getName())) {
				return child.getName();
			}
		}
		return null;
	}

	public List<String> actionCompleteSkillWithParent(String name) {
		List<String> filteredSkillsAsString = new ArrayList<String>();
		try {
			List<SkillsEntity> filteredSkils = skillsLogic.getAutoCompleteSkillsList(name, 10);
			for (SkillsEntity skill : filteredSkils) {
				if (skill.equals(skillRoot)) {
					continue;
				}
				filteredSkillsAsString.add(skill.getNameWithParent());
			}
		} catch (Exception e) {
			logger.error("Could not create filtered skill list by search string: " + name, e);
			JsfUtils.addErrorMessage(JsfUtils.getStringSafely(resourceBundle, "error.global"));
		}
		return filteredSkillsAsString;
	}

	@Override
	public String getHeight() {
		return "25em";
	}

	@Override
	public String getWidth() {
		return "65em";
	}

	public String getNameOfMainSkillWithParent() {
		return nameOfMainSkillWithParent;
	}

	public void setNameOfMainSkillWithParent(String nameOfMainSkillWithParent) {
		this.nameOfMainSkillWithParent = nameOfMainSkillWithParent;
	}

	public String getNameOfMergingSkillWithParent() {
		return nameOfMergingSkillWithParent;
	}

	public void setNameOfMergingSkillWithParent(String nameOfMergingSkillWithParent) {
		this.nameOfMergingSkillWithParent = nameOfMergingSkillWithParent;
	}
}
