package com.doubleclue.dcem.skills.gui;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.PrimeFaces;
import org.primefaces.model.LazyDataModel;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.gui.AutoViewBean;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.gui.ViewLink;
import com.doubleclue.dcem.core.gui.ViewVariable;
import com.doubleclue.dcem.core.jpa.FilterOperator;
import com.doubleclue.dcem.core.jpa.FilterProperty;
import com.doubleclue.dcem.core.jpa.JpaLazyModel;
import com.doubleclue.dcem.core.jpa.VariableType;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.doubleclue.dcem.skills.entities.SkillsEntity;
import com.doubleclue.dcem.skills.logic.SkillsConstants;
import com.doubleclue.dcem.skills.logic.SkillsLogic;
import com.doubleclue.dcem.skills.logic.SkillsModule;
import com.doubleclue.dcem.skills.subjects.SkillsHierarchieSubject;
import com.doubleclue.dcem.skills.subjects.SkillsSubject;

@Named("skillsView")
@SessionScoped
public class SkillsView extends DcemView implements Serializable {

	@Inject
	AutoViewBean autoViewBean;

	@Inject
	SkillsModule skillsModule;

	@Inject
	OperatorSessionBean operatorSessionBean;

	@Inject
	SkillsDialog skillsDialog;

	@Inject
	SkillsLogic skillsLogic;

	@Inject
	SkillsHierarchieSubject hierarchieSubject;

	@Inject
	SkillsSubject skillSubject;
	
	@Inject
	SkillsMergeDialog skillsMergeDialog;

	ResourceBundle resourceBundle;
	private static final long serialVersionUID = 1L;

	@PostConstruct
	public void init() {
		skillsDialog.setParentView(this);
		subject = skillSubject;
		resourceBundle = JsfUtils.getBundle(SkillsModule.RESOURCE_NAME, operatorSessionBean.getLocale());

		addAutoViewAction(DcemConstants.ACTION_ADD, resourceBundle, skillsDialog, SkillsConstants.SKILLS_DIALOG_PATH);
		addAutoViewAction(DcemConstants.ACTION_COPY, resourceBundle, skillsDialog, SkillsConstants.SKILLS_DIALOG_PATH);
		addAutoViewAction(DcemConstants.ACTION_EDIT, resourceBundle, skillsDialog, SkillsConstants.SKILLS_DIALOG_PATH);

		addAutoViewAction(DcemConstants.ACTION_DELETE, resourceBundle, skillsDialog, DcemConstants.AUTO_CONFIRM_DIALOG_PATH);
		addAutoViewAction(SkillsConstants.REQUEST_SKILLS, resourceBundle, skillsDialog, SkillsConstants.SKILLS_DIALOG_PATH);
		addAutoViewAction(SkillsConstants.APPROVE_SKILL, resourceBundle, null, null);
		addAutoViewAction(SkillsConstants.MERGE_SKILLS, resourceBundle, skillsMergeDialog, SkillsConstants.SKILLS_MERGE_DIALOG_PATH);
		ViewLink viewLink = new ViewLink(hierarchieSubject, null, null);
		addAutoViewAction(DcemConstants.ACTION_ORGANIGRAM, resourceBundle, skillsDialog, SkillsConstants.SKILLS_DIALOG_PATH, viewLink);
	}

	@Override
	public void reload() {
		skillsDialog.setParentView(this);
	}

	public ResourceBundle getResourceBundle() {
		return resourceBundle;
	}

	public void actionApproveSkills() {
		try {
			List<Object> skillsEntitiesObj = autoViewBean.getSelectedItems();
			List<SkillsEntity> skills = new ArrayList<SkillsEntity>(skillsEntitiesObj.size());
			for (Object skillObj : skillsEntitiesObj) {
				skills.add((SkillsEntity) skillObj);
			}
			skillsLogic.approveSkills(skills);
			PrimeFaces.current().ajax().update("autoForm:pTable");
			JsfUtils.addInfoMessage(resourceBundle, "skills.approvalSuccess");
		} catch (Exception e) {
			JsfUtils.addErrorMessage(resourceBundle, "error.global");
			logger.error("", e);
		}
	}
	
	public LazyDataModel<?> getLazyModel() {
		if (lazyModel == null) {
			lazyModel = new JpaLazyModel<>(em, this);
			for (ViewVariable viewVariable : getViewVariables()) {
				
				if (viewVariable.getId().equals("name")) {
					lazyModel.addPreFilterProperties(
							new FilterProperty(viewVariable.getAttributes(), SkillsConstants.SKILLS_ROOT, null, VariableType.STRING, FilterOperator.NOT_EQUALS));
					break;
				}
			}
		}
		return lazyModel;
	}
	
}
