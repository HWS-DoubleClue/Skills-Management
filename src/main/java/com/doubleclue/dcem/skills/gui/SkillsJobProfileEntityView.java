package com.doubleclue.dcem.skills.gui;

import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.gui.DcemApplicationBean;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.skills.logic.SkillsConstants;
import com.doubleclue.dcem.skills.subjects.SkillsJobProfileEntitySubject;

@SuppressWarnings("serial")
@Named("skillsJobProfileEntityView")
@SessionScoped
public class SkillsJobProfileEntityView extends DcemView {

	@Inject
	private SkillsJobProfileEntitySubject skillsJobProfileEntitySubject;

	@Inject
	private SkillsJobProfileEntityDialog skillsJobProfileEntityDialog; // small letters

	@Inject
	private SkillsImportDialog skillsImportDialog;

	@Inject
	DcemApplicationBean applicationBean;

	@PostConstruct
	private void init() {
		subject = skillsJobProfileEntitySubject;
		ResourceBundle resourceBundle = JsfUtils.getBundle(DcemConstants.CORE_RESOURCE, operatorSessionBean.getLocale());
		ResourceBundle skillsResourceBundle = JsfUtils.getBundle(SkillsConstants.RESOURCES, operatorSessionBean.getLocale());
		addAutoViewAction(DcemConstants.ACTION_ADD, resourceBundle, skillsJobProfileEntityDialog, SkillsConstants.SKILLS_USER_JOBPROFILE_DIALOG_PATH);
		addAutoViewAction(DcemConstants.ACTION_EDIT, resourceBundle, skillsJobProfileEntityDialog, SkillsConstants.SKILLS_USER_JOBPROFILE_DIALOG_PATH);
		addAutoViewAction(DcemConstants.ACTION_DELETE, resourceBundle, skillsJobProfileEntityDialog, DcemConstants.AUTO_CONFIRM_DIALOG_PATH);
		addAutoViewAction(SkillsConstants.SKILLS_IMPORT, skillsResourceBundle, skillsImportDialog, SkillsConstants.SKILLS_USER_JOBPROFILE_IMPORT_SKILLS_PATH);
	}

	/*
	* This method is called when the view is displayed or reloaded
	*
	*/
	@Override
	public void reload() {

	}

}
