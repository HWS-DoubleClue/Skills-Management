package com.doubleclue.dcem.skills.gui;

import java.util.List;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.gui.AutoViewAction;
import com.doubleclue.dcem.core.gui.DcemApplicationBean;
import com.doubleclue.dcem.core.gui.DcemDialog;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.doubleclue.dcem.skills.entities.SkillsIssuerEntity;
import com.doubleclue.dcem.skills.logic.SkillsIssuerLogic;
import com.doubleclue.dcem.skills.logic.SkillsModule;

@SessionScoped
@Named("skillsIssuerDialog")
public class SkillsIssuerDialog extends DcemDialog {

	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(DcemView.class);

	@Inject
	OperatorSessionBean operatorSessionBean;

	@Inject
	SkillsIssuerLogic skillsIssuerLogic;
	
	@Inject
	DcemApplicationBean applicationBean;
	
	ResourceBundle resourceBundle;

	private SkillsIssuerEntity skillsIssuerEntity;

	@PostConstruct
	private void init() {
		resourceBundle = JsfUtils.getBundle(SkillsModule.RESOURCE_NAME, operatorSessionBean.getLocale());
	}

	@Override
	public boolean actionOk() {
		skillsIssuerLogic.addOrUpdateSkillsIssuer(skillsIssuerEntity, this.getAutoViewAction().getDcemAction());
		return true;
	}

	public void show(DcemView dcemView, AutoViewAction autoViewAction) {
		skillsIssuerEntity = (SkillsIssuerEntity) this.getActionObject();
		if (autoViewAction.getDcemAction().getAction().equals(DcemConstants.ACTION_ADD)) {
			skillsIssuerEntity.setCountry("DE");
		}
	}

	@Override
	public String getHeight() {
		return "40em";
	}

	@Override
	public String getWidth() {
		return "35em";
	}

	public List<SelectItem> getAvailableCountries() {
		return applicationBean.getAvailableCountries(operatorSessionBean.getLocale());
	}

	public void leavingDialog() {
		skillsIssuerEntity = null;
	}

	public SkillsIssuerEntity getSkillsIssuerEntity() {
		return skillsIssuerEntity;
	}

	public void setSkillsIssuerEntity(SkillsIssuerEntity skillsIssuerEntity) {
		this.skillsIssuerEntity = skillsIssuerEntity;
	}

}
