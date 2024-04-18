package com.doubleclue.dcem.skills.gui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.gui.AutoDialogBean;
import com.doubleclue.dcem.core.gui.AutoViewAction;
import com.doubleclue.dcem.core.gui.AutoViewBean;
import com.doubleclue.dcem.core.gui.DcemDialog;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.doubleclue.dcem.skills.entities.SkillsEntity;
import com.doubleclue.dcem.skills.entities.enums.ApprovalStatus;
import com.doubleclue.dcem.skills.exceptions.SkillsErrorCodes;
import com.doubleclue.dcem.skills.exceptions.SkillsException;
import com.doubleclue.dcem.skills.logic.SkillsConstants;
import com.doubleclue.dcem.skills.logic.SkillsEmailLogic;
import com.doubleclue.dcem.skills.logic.SkillsLogic;
import com.doubleclue.dcem.skills.logic.SkillsModule;
import com.doubleclue.dcem.skills.utils.SkillsUtils;

@SessionScoped
@Named("skillsDialog")
public class SkillsDialog extends DcemDialog {

	private static final Logger logger = LogManager.getLogger(DcemView.class);

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

	private static final long serialVersionUID = 1L;
	private ResourceBundle resourceBundle;

	private SkillsEntity skillRoot;
	private String action;
	private SkillsEntity skillsEntity;
	private String comment;
	private Integer selectedParentId;

	private List<SelectItem> availableParents = new LinkedList<>();
	private List<SelectItem> skillsApprovalSelection;

	private boolean deleteParentSkill;
	private List<SkillsEntity> selectedSkills;
	private ApprovalStatus approvalStatus;

	@PostConstruct
	private void init() {
		resourceBundle = JsfUtils.getBundle(SkillsModule.RESOURCE_NAME, operatorSessionBean.getLocale());
	}

	@Override
	public boolean actionOk() throws Exception {
		if (skillsEntity.getName() == null || skillsEntity.getName().trim().isEmpty() || skillsEntity.getAbbreviation() == null
				|| skillsEntity.getAbbreviation().trim().isEmpty()) {
			JsfUtils.addErrorMessage(SkillsModule.RESOURCE_NAME, "skills.dialog.invalid.name");
			return false;
		}
		skillsEntity.setName(skillsEntity.getName().trim());
		skillsEntity.setAbbreviation(skillsEntity.getAbbreviation().trim());
		if (SkillsUtils.isValidName(skillsEntity.getName()) == false || SkillsUtils.isValidName(skillsEntity.getAbbreviation()) == false) {
			JsfUtils.addErrorMessage(JsfUtils.getStringSafely(resourceBundle, "skills.invalidCharacters") + ": " + SkillsConstants.SPECIAL_CHARACTERS);
			return false;
		}
		if (skillsEntity.getName().toLowerCase().equals(SkillsConstants.SKILLS_ROOT.toLowerCase())
				|| skillsEntity.getAbbreviation().toLowerCase().equals(SkillsConstants.SKILLS_ROOT.toLowerCase())) {
			JsfUtils.addErrorMessage(resourceBundle, "skills.dialog.error.skillsRoot");
			return false;
		}

		if (selectedParentId == null) {
			selectedParentId = 0;
		}
		if (selectedParentId > 0) {
			skillsEntity.setParent(skillsLogic.getSkillById(selectedParentId));
		} else {
			skillsEntity.setParent(null);
		}
		if (action.equals(DcemConstants.ACTION_ADD) || approvalStatus == ApprovalStatus.APPROVED) {
			skillsEntity.setApprovalStatus(ApprovalStatus.APPROVED);
		}
		skillsLogic.addOrUpdateSkill(skillsEntity, getAutoViewAction().getDcemAction());

		if (SkillsConstants.REQUEST_SKILLS.equals(action)) {
			skillsEmailLogic.notifySkillsRequest(skillsEntity, comment);
		}
		return true;
	}

	@Override
	public void show(DcemView dcemView, AutoViewAction autoViewAction) throws Exception {
		action = this.getAutoViewAction().getDcemAction().getAction();
		skillRoot = skillsLogic.getSkillRoot();
		if (action.equals(DcemConstants.ACTION_DELETE)) {
			List<Object> skillsEntitiesObj = autoViewBean.getSelectedItems();
			for (Object skillObj : skillsEntitiesObj) {
				SkillsEntity skillsEntityDelete = (SkillsEntity) skillObj;
				if (skillsEntityDelete.getId() == skillRoot.getId()) {
					throw new SkillsException(SkillsErrorCodes.CANNOT_DELETE_SKILL_ROOT, "not allowed to change skill root");
				}
			}
			return;
		}
		if (action.equals(DcemConstants.ACTION_ADD) || action.equals(SkillsConstants.REQUEST_SKILLS)) {
			skillsEntity = (SkillsEntity) dcemView.getActionObject();
			if (skillsEntity.getParent() == null) {
				skillsEntity.setParent(skillRoot);
			}
			if (skillsEntity.getParent().getApprovalStatus() == ApprovalStatus.PENDING) {
				throw new SkillsException(SkillsErrorCodes.CANNOT_ADD_NON_APPROVED_SKILL, null);
			}
		}
		
		if (action.equals(SkillsConstants.REQUEST_SKILLS)) {
			skillsEntity = new SkillsEntity();
			if (skillsHierarchieView.getSelectionNode() != null) {
				selectedParentId = ((SkillsEntity) skillsHierarchieView.getSelectionNode().getData()).getId();
			} else {
				selectedParentId = skillRoot.getId();
			}
			skillsEntity.setRequestedFrom(operatorSessionBean.getDcemUser());
			skillsEntity.setApprovalStatus(ApprovalStatus.PENDING);
		}
		if (action.equals(DcemConstants.ACTION_EDIT) || action.equals(DcemConstants.ACTION_COPY) || action.equals(DcemConstants.ACTION_ADD)) {
			skillsEntity = (SkillsEntity) dcemView.getActionObject();
			if (skillsEntity.getId() == skillRoot.getId()) {
				throw new SkillsException(SkillsErrorCodes.CANNOT_CHANGE_SKILL_ROOT, "not allowed to change skill root");
			}
			selectedParentId = skillsEntity.getParent() != null ? skillsEntity.getParent().getId() : skillRoot.getId();
		}
		availableParents = null;
		approvalStatus = skillsEntity.getApprovalStatus();
	}

	@Override
	public String getConfirmText() {
		try {
			StringBuilder deleteMsgBuilder = new StringBuilder();
			deleteParentSkill = false;
			List<Object> skillsEntitiesObj = autoViewBean.getSelectedItems();
			List<String> parentSkillsList = new ArrayList<String>();
			selectedSkills = new ArrayList<SkillsEntity>(skillsEntitiesObj.size());
			for (Object skillObj : skillsEntitiesObj) {
				SkillsEntity skillsEntityDelete = skillsLogic.getSkillById(((SkillsEntity) skillObj).getId());
				if (skillsEntityDelete == null) {
					continue;
				}
				if (skillsEntityDelete.getChildren().isEmpty() == false) {
					if (deleteParentSkill == false) {
						deleteMsgBuilder.append(JsfUtils.getStringSafely(resourceBundle, "skillsDialog.deleteInformationParentSkill"));
						deleteParentSkill = true;
					}
					parentSkillsList.add("<li>" + skillsEntityDelete.getNameWithParent() + "</li>");
				}
				selectedSkills.add(skillsEntityDelete);
			}
			if (deleteParentSkill == true) {
				parentSkillsList = parentSkillsList.stream().sorted().collect(Collectors.toList());
				for (String parentSkill : parentSkillsList) {
					deleteMsgBuilder.append(parentSkill);
				}
				deleteMsgBuilder.append("</ul>");
				return deleteMsgBuilder.toString();
			}
			selectedSkills = selectedSkills.stream()
					.sorted(Comparator.comparing((SkillsEntity skill) -> skill.getNameWithParent(), String.CASE_INSENSITIVE_ORDER))
					.collect(Collectors.toList());
			HashMap<Integer, Long> userCountMap = skillsLogic.getUserCountForSkills(selectedSkills);
			HashMap<Integer, Long> certificateCountMap = skillsLogic.getCertificateCountForSkills(selectedSkills);
			HashMap<Integer, Long> jobCountMap = skillsLogic.getJobprofileCountForSkills(selectedSkills);

			deleteMsgBuilder.append(JsfUtils.getStringSafely(resourceBundle, "skillsDialog.deleteHeader"));
			for (SkillsEntity skill : selectedSkills) {
				Long userCount = userCountMap.getOrDefault(skill.getId(), 0L);
				Long certificateCount = certificateCountMap.getOrDefault(skill.getId(), 0L);
				Long jobCount = jobCountMap.getOrDefault(skill.getId(), 0L);
				if (userCount + certificateCount + jobCount > 0) {
					deleteMsgBuilder
							.append(String.format(JsfUtils.getStringSafely(resourceBundle, "skillsDialog.deleteInformationSkill"), skill.getNameWithParent()));
					deleteMsgBuilder.append("<ul>");
					if (userCount > 0) {
						deleteMsgBuilder
								.append(String.format(JsfUtils.getStringSafely(resourceBundle, "skillsDialog.deleteInformationNumberUsers"), userCount));
					}
					if (certificateCount > 0) {
						deleteMsgBuilder
								.append(String.format(JsfUtils.getStringSafely(resourceBundle, "skillsDialog.deleteInformationNumberCerts"), certificateCount));
					}
					if (jobCount > 0) {
						deleteMsgBuilder.append(
								String.format(JsfUtils.getStringSafely(resourceBundle, "skillsDialog.deleteInformationNumberSkillsProfiles"), jobCount));
					}
					deleteMsgBuilder.append("</ul>");
				}
			}
			String deleteMsg = deleteMsgBuilder.toString();
			return deleteMsg;
		} catch (Exception e) {
			logger.error("", e);
			return JsfUtils.getStringSafely(resourceBundle, "skillsDialog.warningErrorDeleteInformation") + super.getConfirmText();
		}
	}

	@Override
	public void actionConfirm() throws Exception {
		if (deleteParentSkill == true) {
			JsfUtils.addErrorMessage(resourceBundle, "skillsDialog.errorDeletingParent");
			return;
		}
		skillsLogic.deleteSkills(selectedSkills);
	}

	public void leavingDialog() {
		action = null;
		skillsEntity = null;
		comment = null;
		selectedParentId = null;
		availableParents = null;
		selectedSkills = null;
	}

	public List<SelectItem> getAvailableParents() {
		try {
			if (availableParents == null || availableParents.isEmpty()) {
				availableParents = new ArrayList<SelectItem>();
				List<SkillsEntity> allSkills = skillsLogic.getAllApprovedSkills();
				String name;
				for (SkillsEntity skillsEntityParent : allSkills) {
					if (skillsEntityParent.getId().equals(skillsEntity.getId())) {
						continue;
					}
					if (isChildOfSelectedSkill(skillsEntityParent)) {
						continue;
					}
					name = skillsEntityParent.getNameWithParent();
					availableParents.add(new SelectItem(skillsEntityParent.getId(), name));
				}
			}
			return availableParents;
		} catch (Exception e) {
			logger.error("", e);
			JsfUtils.addErrorMessage(resourceBundle, "error.global");
			return new ArrayList<SelectItem>();
		}
	}

	private boolean isChildOfSelectedSkill(SkillsEntity skillEntityParent) {
		while (skillEntityParent.equals(skillRoot) == false) {
			if (skillEntityParent.getParent().equals(skillsEntity)) {
				return true;
			}
			skillEntityParent = skillEntityParent.getParent();
		}
		return false;
	}

	public boolean isSkillsPending() {
		if (skillsEntity == null) {
			return false;
		}
		return ApprovalStatus.PENDING.equals(skillsEntity.getApprovalStatus()) || approvalStatus == ApprovalStatus.PENDING;
	}

	public List<SelectItem> getSkillsApprovalSelection() {
		if (skillsApprovalSelection == null) {
			skillsApprovalSelection = new ArrayList<SelectItem>();
			for (ApprovalStatus status : ApprovalStatus.values()) {
				skillsApprovalSelection.add(new SelectItem(status, status.getLocaleText()));
			}
		}
		return skillsApprovalSelection;
	}

	public void actionApproveSkill() {
		approvalStatus = ApprovalStatus.APPROVED;
	}

	@Override
	public String getHeight() {
		return "40em";
	}

	public Integer getSelectedParentId() {
		return selectedParentId;
	}

	public void setSelectedParentId(Integer selectedParent) {
		this.selectedParentId = selectedParent;
	}

	public SkillsEntity getSkillsEntity() {
		return skillsEntity;
	}

	public void setSkillsEntity(SkillsEntity skillsEntity) {
		this.skillsEntity = skillsEntity;
	}

	public boolean isActionRequestSkill() {
		if (action == null || action.equals(SkillsConstants.REQUEST_SKILLS) == false) {
			return false;
		}
		return true;
	}

	public boolean isActionAdd() {
		if (action == null || action.equals(DcemConstants.ACTION_ADD) == false) {
			return false;
		}
		return true;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public ApprovalStatus getApprovalStatus() {
		return approvalStatus;
	}

	public void setApprovalStatus(ApprovalStatus approvalStatus) {
		this.approvalStatus = approvalStatus;
	}

}
