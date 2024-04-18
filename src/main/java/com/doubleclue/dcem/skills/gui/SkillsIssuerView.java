package com.doubleclue.dcem.skills.gui;

import java.io.Serializable;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.gui.AutoViewBean;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.doubleclue.dcem.skills.logic.SkillsConstants;
import com.doubleclue.dcem.skills.logic.SkillsModule;
import com.doubleclue.dcem.skills.subjects.SkillsIssuerSubject;

@Named("skillsIssuerView")
@SessionScoped
public class SkillsIssuerView extends DcemView implements Serializable {

	@Inject
	AutoViewBean autoViewBean;

	@Inject
	SkillsModule skillsModule;

	@Inject
	OperatorSessionBean operatorSessionBean;

	@Inject
	SkillsIssuerSubject skillsIssuerSubject;

	@Inject
	SkillsIssuerDialog skillsIssuerDialog;

	ResourceBundle resourceBundle;
	private static final long serialVersionUID = 1L;

	@PostConstruct
	public void init() {
		subject = skillsIssuerSubject;
		ResourceBundle resourceBundle = JsfUtils.getBundle(SkillsModule.RESOURCE_NAME, operatorSessionBean.getLocale());

		addAutoViewAction(DcemConstants.ACTION_ADD, resourceBundle, skillsIssuerDialog, SkillsConstants.SKILLS_ISSUER_DIALOG_PATH);
		addAutoViewAction(DcemConstants.ACTION_EDIT, resourceBundle, skillsIssuerDialog, SkillsConstants.SKILLS_ISSUER_DIALOG_PATH);
		addAutoViewAction(DcemConstants.ACTION_DELETE, resourceBundle, skillsIssuerDialog, DcemConstants.AUTO_CONFIRM_DIALOG_PATH);
	}

	@Override
	public void reload() {
	}

	public ResourceBundle getResourceBundle() {
		return resourceBundle;
	}

}
