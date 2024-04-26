package com.doubleclue.dcem.skills.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.PrimeFaces;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.gui.DcemApplicationBean;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.skills.entities.SkillsCertificateEntity;
import com.doubleclue.dcem.skills.logic.SkillsCertificateLogic;
import com.doubleclue.dcem.skills.logic.SkillsConstants;
import com.doubleclue.dcem.skills.logic.SkillsModule;
import com.doubleclue.dcem.skills.subjects.SkillsCertificateSubject;

@SuppressWarnings("serial")
@Named("skillsCertificateView")
@SessionScoped
public class SkillsCertificateView extends DcemView {

	@Inject
	private SkillsCertificateSubject skillsCertificateEntitySubject;

	@Inject
	private SkillsCertificateDialog skillsCertificateDialog;
	
	@Inject
	private SkillsCertificateLogic skillsCertificateLogic;

	@Inject
	DcemApplicationBean applicationBean;
	
	ResourceBundle resourceBundle;

	@PostConstruct
	private void init() {
		subject = skillsCertificateEntitySubject;
		resourceBundle = JsfUtils.getBundle(SkillsModule.RESOURCE_NAME, operatorSessionBean.getLocale());
		addAutoViewAction(DcemConstants.ACTION_ADD, resourceBundle, skillsCertificateDialog, SkillsConstants.SKILLS_CERTIFICATE_DIALOG_PATH);
		addAutoViewAction(DcemConstants.ACTION_EDIT, resourceBundle, skillsCertificateDialog, SkillsConstants.SKILLS_CERTIFICATE_DIALOG_PATH);
		addAutoViewAction(DcemConstants.ACTION_DELETE, resourceBundle, skillsCertificateDialog, DcemConstants.AUTO_CONFIRM_DIALOG_PATH);
		addAutoViewAction(SkillsConstants.REQUEST_CERTIFICATE, resourceBundle, skillsCertificateDialog, SkillsConstants.SKILLS_CERTIFICATE_DIALOG_PATH);
		addAutoViewAction(SkillsConstants.APPROVE_CERTIFICATE, resourceBundle, skillsCertificateDialog, null);
	}

	@Override
	public void reload() {

	}

	@Override
	public Object createActionObject() {
		return super.createActionObject();
	}
	
	}
