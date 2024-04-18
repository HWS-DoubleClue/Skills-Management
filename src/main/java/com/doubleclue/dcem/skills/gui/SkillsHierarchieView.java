package com.doubleclue.dcem.skills.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.PrimeFaces;
import org.primefaces.event.organigram.OrganigramNodeDragDropEvent;
import org.primefaces.model.DefaultOrganigramNode;
import org.primefaces.model.OrganigramNode;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.entities.DcemAction;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.AutoViewAction;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.gui.ViewLink;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.doubleclue.dcem.skills.entities.SkillsEntity;
import com.doubleclue.dcem.skills.entities.SkillsUserEntity;
import com.doubleclue.dcem.skills.entities.SkillsUserSkillEntity;
import com.doubleclue.dcem.skills.entities.enums.ApprovalStatus;
import com.doubleclue.dcem.skills.entities.enums.SkillsStatus;
import com.doubleclue.dcem.skills.logic.SkillsConstants;
import com.doubleclue.dcem.skills.logic.SkillsLogic;
import com.doubleclue.dcem.skills.logic.SkillsModule;
import com.doubleclue.dcem.skills.logic.SkillsUserLogic;
import com.doubleclue.dcem.skills.subjects.SkillsHierarchieSubject;
import com.doubleclue.dcem.skills.subjects.SkillsSubject;
import com.doubleclue.dcem.skills.utils.SkillsUtils;

@SessionScoped
@Named
public class SkillsHierarchieView extends DcemView {

	private static final long serialVersionUID = 1L;

	@Inject
	OperatorSessionBean operatorSessionBean;

	@Inject
	SkillsLogic skillsLogic;

	@Inject
	SkillsSubject skillsSubject;

	@Inject
	SkillsHierarchieSubject skillsOrganigramSubject;

	@Inject
	SkillsDashboardView skillsDashboardView;

	@Inject
	SkillsDialog skillsDialog;

	@Inject
	MySkillsDialog mySkillsDialog;

	@Inject
	SkillsUserLogic skillsUserLogic;

	ResourceBundle resourceBundle;

	private SkillsEntity skillRoot;
	private OrganigramNode rootNode;
	private OrganigramNode selectionNode;
	List<SkillsUserSkillEntity> mySkills;
	boolean onlyMySkills;
	private boolean editRights;
	private boolean editMySkill;
	AutoViewAction searchSkillInDashboard;

	@PostConstruct
	public void init() {
		subject = skillsOrganigramSubject;
		skillsDialog.setParentView(this);
		resourceBundle = JsfUtils.getBundle(SkillsModule.RESOURCE_NAME, operatorSessionBean.getLocale());
		addAutoViewAction(DcemConstants.ACTION_ADD, resourceBundle, skillsDialog, SkillsConstants.SKILLS_DIALOG_PATH);
		editRights = addAutoViewAction(DcemConstants.ACTION_EDIT, resourceBundle, skillsDialog, SkillsConstants.SKILLS_DIALOG_PATH);
		addAutoViewAction(DcemConstants.ACTION_DELETE, resourceBundle, skillsDialog, DcemConstants.AUTO_CONFIRM_DIALOG_PATH);
		addAutoViewAction(SkillsConstants.REQUEST_SKILLS, resourceBundle, skillsDialog, SkillsConstants.SKILLS_DIALOG_PATH);
		addAutoViewAction(SkillsConstants.APPROVE_SKILL, resourceBundle, null, null);
		editMySkill = addAutoViewAction(SkillsConstants.ACTION_EDIT_MYSKILLS, resourceBundle, mySkillsDialog, SkillsConstants.MY_SKILLS_DIALOG_PATH);
		ViewLink viewLink = new ViewLink(skillsSubject, null, null);
		addAutoViewAction(SkillsConstants.SHOW_SKILL_TABLE, resourceBundle, skillsDialog, SkillsConstants.SKILLS_DIALOG_PATH, viewLink);
		searchSkillInDashboard = createAutoViewAction(SkillsConstants.SKILLS_USER_SEARCH, resourceBundle, null, null, null);
		reload();
	}

	@Override
	public void reload() {
		skillsDialog.setParentView(this);
		mySkills = new ArrayList<SkillsUserSkillEntity>();
		try {
			skillRoot = skillsLogic.getSkillRoot();
			SkillsUserEntity skillsUserEntity = skillsUserLogic.retrieveSkillsUserByDcemUser(operatorSessionBean.getDcemUser());
			mySkills = skillsUserEntity.getSkills();
			for (SkillsUserSkillEntity skillsUserSkillEntity : mySkills) {
				skillsUserSkillEntity.getSkill();
				skillsUserSkillEntity.getLevel();
			}
		} catch (Exception e) {
			logger.error("", e);
			JsfUtils.addErrorMessage(resourceBundle, "error.global");
		}
		updateHierarchy();
	}

	private void updateHierarchy() {
		try {
			rootNode = new DefaultOrganigramNode("root", skillRoot, null);
			rootNode.setSelectable(false);
			rootNode.setDroppable(true);
			rootNode.setDraggable(false);
			List<SkillsEntity> skillsEntities = skillsLogic.getAllSkills();
			organigramChildren(rootNode, skillsEntities);
		} catch (Exception e) {
			logger.error("", e);
			JsfUtils.addErrorMessage(resourceBundle, "error.global");
		}
	}

	private void organigramChildren(OrganigramNode organigramNode, List<SkillsEntity> skillsEntities) {
		for (SkillsEntity skillsEntity : skillsEntities) {
			if (skillsEntity.equals(skillRoot) == false && hasSkillOrChildrenWithLevel(skillsEntity)) {
				if (((SkillsEntity) organigramNode.getData()).getId() == skillsEntity.getParent().getId()) {
					OrganigramNode organigramChildNode = new DefaultOrganigramNode("skills", skillsEntity, organigramNode);
					organigramChildNode.setCollapsible(true);
					organigramChildNode.setSelectable(true);
					setRulesForDraggAndDropp(organigramChildNode);
					organigramChildren(organigramChildNode, skillsEntities);
				}
			}
		}
	}

	private void setRulesForDraggAndDropp(OrganigramNode organigramNode) {
		if (editRights == true) {
			organigramNode.setDraggable(true);
			organigramNode.setDroppable(true);
		}
		if (ApprovalStatus.PENDING.equals(((SkillsEntity) organigramNode.getData()).getStatus())) {
			organigramNode.setDroppable(false);
			organigramNode.setType("notApprovedSkills");
		}
	}

	private boolean hasSkillOrChildrenWithLevel(SkillsEntity skillsEntityParent) {
		SkillsEntity skillsEntity;
		if (onlyMySkills == true) {
			for (SkillsUserSkillEntity skillsUserSkillEntity : mySkills) {
				skillsEntity = skillsUserSkillEntity.getSkill();
				while (skillsEntity.equals(skillRoot) == false) {
					if (skillsEntity.equals(skillsEntityParent)) {
						return true;
					}
					skillsEntity = skillsEntity.getParent();
				}
			}
			return false;
		}
		return true;
	}

	public String getRanking(SkillsEntity skillsEntity) {
		if (mySkills == null || skillsEntity == null) {
			return null;
		}
		for (SkillsUserSkillEntity skillsUserSkillEntity : mySkills) {
			if (skillsUserSkillEntity.getSkill().getId().equals(skillsEntity.getId())) {
				return SkillsUtils.convertLevelToStars(skillsUserSkillEntity.getLevel());
			}
		}
		return null;
	}

	public String getRankingClass(SkillsEntity skillsEntity) {
		if (mySkills == null || skillsEntity == null) {
			return "";
		}
		for (SkillsUserSkillEntity skillsUserSkillEntity : mySkills) {
			if (skillsUserSkillEntity.getSkill().getId().equals(skillsEntity.getId())) {
				if (skillsUserSkillEntity.getStatus() == SkillsStatus.OWNS) {
					return "";
				} else {
					return DcemConstants.UI_FADE_RATING_CLASS;
				}
			}
		}
		return "";
	}

	public int getStatus(SkillsEntity skillsEntity) {
		if (mySkills == null || skillsEntity == null) {
			return -1;
		}
		for (SkillsUserSkillEntity skillsUserSkillEntity : mySkills) {
			if (skillsUserSkillEntity.getSkill().getId().equals(skillsEntity.getId())) {
				return skillsUserSkillEntity.getStatus().ordinal();
			}
		}
		return -1;
	}

	public String getTargetDate(SkillsEntity skillsEntity) {
		if (mySkills == null || skillsEntity == null) {
			return null;
		}
		for (SkillsUserSkillEntity skillsUserSkillEntity : mySkills) {
			if (skillsUserSkillEntity.getSkill().getId().equals(skillsEntity.getId())) {
				return skillsUserSkillEntity.getFormattedDate(operatorSessionBean.getLocale().getLanguage());
			}
		}
		return null;
	}

	public void leavingDialog() {

	}

	public OrganigramNode getRootNode() {
		if (rootNode == null) {
			updateHierarchy();
		}
		return rootNode;
	}

	public void setRootNode(OrganigramNode rootNode) {
		this.rootNode = rootNode;
	}

	public Object createActionObject() {
		if (selectionNode != null) {
			SkillsEntity skillsEntity = (SkillsEntity) selectionNode.getData();
			if (skillsEntity.getId() != null) { // check if it is the root
				SkillsEntity skillsEntityNew = new SkillsEntity();
				skillsEntityNew.setParent(skillsEntity);
				return skillsEntityNew;
			}
		}
		return new SkillsEntity();
	}

	public void nodeDragDropListener(OrganigramNodeDragDropEvent event) {
		SkillsEntity skillsEntity = (SkillsEntity) event.getOrganigramNode().getData();
		SkillsEntity skillsEntityTarget = (SkillsEntity) event.getTargetOrganigramNode().getData();
		if (skillsEntityTarget.getId() == null) {
			skillsEntity.setParent(null); // target is skill-root
		} else {
			skillsEntity.setParent(skillsEntityTarget);
		}
		try {
			skillsLogic.addOrUpdateSkill(skillsEntity, new DcemAction(skillsSubject, DcemConstants.ACTION_EDIT));
		} catch (DcemException e) {
			JsfUtils.addErrorMessage(e.getLocalizedMessage());
		} catch (Exception e) {
			JsfUtils.addErrorMessage(e.toString());
			logger.error("", e);
		}
		rootNode = null;
	}

	public void editSkill() {
		DcemAction dcemAction = new DcemAction(skillsSubject, DcemConstants.ACTION_EDIT);
		if (operatorSessionBean.isPermission(dcemAction)) {
			AutoViewAction autoViewAction = this.getAutoViewAction(DcemConstants.ACTION_EDIT);
			viewNavigator.setActiveDialog(autoViewAction);
		}
	}

	public void mySkill() {
		if (editMySkill == true) {
			AutoViewAction autoViewAction = this.getAutoViewAction(SkillsConstants.ACTION_EDIT_MYSKILLS);
			viewNavigator.setActiveDialog(autoViewAction);
		}
	}

	public boolean isAddSkill() {
		return this.getAutoViewAction(DcemConstants.ACTION_ADD) != null;
	}

	public boolean isEditSkill() {
		return this.getAutoViewAction(DcemConstants.ACTION_EDIT) != null;
	}

	public boolean isDeleteSkill() {
		return this.getAutoViewAction(DcemConstants.ACTION_DELETE) != null;
	}

	public boolean isApproveSkill() {
		return this.getAutoViewAction(SkillsConstants.APPROVE_SKILL) != null;
	}

	public boolean isSearchSkill() {
		if (searchSkillInDashboard != null && operatorSessionBean.isPermission(searchSkillInDashboard.getDcemAction())) {
			return true;
		}
		return false;
	}

	public void addSkill() {
		AutoViewAction autoViewAction = this.getAutoViewAction(DcemConstants.ACTION_ADD);
		if (autoViewAction != null) {
			viewNavigator.setActiveDialog(autoViewAction);
		}
	}

	public void deleteSkill() {
		AutoViewAction autoViewAction = this.getAutoViewAction(DcemConstants.ACTION_DELETE);
		if (autoViewAction != null) {
			viewNavigator.setActiveDialog(autoViewAction);
		}
	}

	public OrganigramNode getSelectionNode() {
		return selectionNode;
	}

	public void setSelectionNode(OrganigramNode selection) {
		this.selectionNode = selection;
		if (selection == null) {
			List<Object> selectionList = new ArrayList<Object>(0);
			autoViewBean.setSelectedItems((List<Object>) selectionList);
		} else {
			List<Object> selectionList = new ArrayList<Object>(1);
			selectionList.add((Object) selectionNode.getData());
			autoViewBean.setSelectedItems((List<Object>) selectionList);
		}
	}

	public boolean isOnlyMySkills() {
		return onlyMySkills;
	}

	public void setOnlyMySkills(boolean onlyMySkills) {
		this.onlyMySkills = onlyMySkills;
	}

	public boolean isNotApproved(OrganigramNode node) {
		return ApprovalStatus.PENDING.equals(((SkillsEntity) node.getData()).getStatus());
	}

	public void actionApproveSkills() {
		try {
			List<Object> skillsEntitiesObj = autoViewBean.getSelectedItems();
			List<SkillsEntity> skills = new ArrayList<SkillsEntity>(skillsEntitiesObj.size());
			for (Object skillObj : skillsEntitiesObj) {
				skills.add((SkillsEntity) skillObj);
				skillsLogic.approveSkills(skills);
			}
			reload();
			PrimeFaces.current().ajax().update("organigramForm");
			JsfUtils.addInfoMessage(resourceBundle, "skills.approvalSuccess");
		} catch (Exception e) {
			JsfUtils.addErrorMessage(resourceBundle, "error.global");
			logger.error("", e);
		}
	}

	public void actionSearchUserWithSkillsInDashboard() {
		skillsDashboardView.setSkillSearchName(((SkillsEntity) (selectionNode.getData())).getName());
		viewNavigator.setActiveView(SkillsModule.MODULE_ID + DcemConstants.MODULE_VIEW_SPLITTER + skillsDashboardView.getSubject().getViewName());
		skillsDashboardView.actionSearch();
	}

	public boolean isEditMySkill() {
		return editMySkill;
	}
	
	@Override
	public void leavingView() {
		selectionNode = null;
	}

}
