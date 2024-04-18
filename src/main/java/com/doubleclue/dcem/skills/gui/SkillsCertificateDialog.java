package com.doubleclue.dcem.skills.gui;

import java.util.ArrayList;
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
import com.doubleclue.dcem.core.gui.AutoViewAction;
import com.doubleclue.dcem.core.gui.DcemDialog;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.doubleclue.dcem.skills.entities.SkillsCertificateEntity;
import com.doubleclue.dcem.skills.entities.SkillsEntity;
import com.doubleclue.dcem.skills.entities.SkillsIssuerEntity;
import com.doubleclue.dcem.skills.entities.enums.ApprovalStatus;
import com.doubleclue.dcem.skills.logic.SkillsCertificateLogic;
import com.doubleclue.dcem.skills.logic.SkillsConstants;
import com.doubleclue.dcem.skills.logic.SkillsEmailLogic;
import com.doubleclue.dcem.skills.logic.SkillsIssuerLogic;
import com.doubleclue.dcem.skills.logic.SkillsLogic;
import com.doubleclue.dcem.skills.logic.SkillsModule;
import com.doubleclue.dcem.skills.utils.SkillsUtils;

@SessionScoped
@Named("skillsCertificateDialog")
public class SkillsCertificateDialog extends DcemDialog {

	private Logger logger = LogManager.getLogger(SkillsCertificateDialog.class);
	private static final long serialVersionUID = 1L;

	@Inject
	OperatorSessionBean operatorSessionBean;

	@Inject
	SkillsCertificateLogic skillsCertificateLogic;

	@Inject
	SkillsIssuerLogic skillsIssuerLogic;

	@Inject
	SkillsLogic skillsLogic;

	@Inject
	SkillsEmailLogic skillsEmailLogic;

	private String action;
	private String issuerName;
	private ResourceBundle resourceBundle;
	private SkillsCertificateEntity skillsCertificateEntity;
	private List<SelectItem> skillsApprovalSelection;
	private boolean issuerNotListed;

	private List<SkillsEntity> selectedSkills;
	private List<SkillsEntity> allSkills;

	@PostConstruct
	private void init() {
		resourceBundle = JsfUtils.getBundle(SkillsModule.RESOURCE_NAME, operatorSessionBean.getLocale());
	}

	@Override
	public void show(DcemView dcemView, AutoViewAction autoViewAction) {
		action = this.getAutoViewAction().getDcemAction().getAction();
		issuerNotListed = false;
		if (action.equals(SkillsConstants.REQUEST_CERTIFICATE)) {
			skillsCertificateEntity = new SkillsCertificateEntity();
			skillsCertificateEntity.setApprovalStatus(ApprovalStatus.PENDING);
		} else if (action.equals(DcemConstants.ACTION_ADD)) {
			skillsCertificateEntity = new SkillsCertificateEntity();
			skillsCertificateEntity.setApprovalStatus(ApprovalStatus.APPROVED);
		} else {
			skillsCertificateEntity = (SkillsCertificateEntity) dcemView.getActionObject();
		}

		if (action.equals(DcemConstants.ACTION_EDIT)) {
			if (skillsCertificateEntity.getIssuer() != null) {
				issuerName = skillsCertificateEntity.getIssuer().getName();
			} else {
				issuerNotListed = true;
			}
		}
		selectedSkills = skillsCertificateEntity.getAppliesForSkills() != null
				? skillsCertificateEntity.getAppliesForSkills().stream().collect(Collectors.toList())
				: new ArrayList<SkillsEntity>();
	}

	@Override
	public boolean actionOk() throws Exception {
		if (SkillsUtils.isValidName(skillsCertificateEntity.getName()) == false) {
			JsfUtils.addErrorMessage(JsfUtils.getStringSafely(resourceBundle, "skills.invalidCharacters") + ": " + SkillsConstants.SPECIAL_CHARACTERS);
			return false;
		}
		SkillsIssuerEntity skillsIssuerEntity = new SkillsIssuerEntity();
		if (issuerName != null) {
			if (issuerNotListed == true) {
				skillsCertificateEntity.setIssuer(null);
			} else {
				skillsIssuerEntity = skillsIssuerLogic.getIssuerByName(issuerName);
				if (skillsIssuerEntity == null) {
					JsfUtils.addErrorMessage(resourceBundle, "skillsCertificateDialog.error.issuerList");
					return false;
				}
				skillsCertificateEntity.setIssuer(skillsIssuerEntity);
			}
		}
		skillsCertificateEntity.setAppliesForSkills(selectedSkills);
		skillsCertificateLogic.addOrUpdateSkillsCertificate(skillsCertificateEntity, this.getAutoViewAction().getDcemAction());
		if (action.equals(SkillsConstants.REQUEST_CERTIFICATE)) {
			skillsCertificateEntity.setRequestedFrom(operatorSessionBean.getDcemUser());
			skillsEmailLogic.notifyCertificateRequest(skillsCertificateEntity);
		}
		return true;
	}

	@Override
	public void leavingDialog() {
		action = null;
		issuerName = null;
		skillsCertificateEntity = null;
		selectedSkills = null;
		allSkills = null;
	}

	public List<String> completeIssuer(String name) {
		try {
			return skillsIssuerLogic.getCompleteIssuer(name, 50);
		} catch (Exception e) {
			JsfUtils.addErrorMessage(resourceBundle, "skillsCertificate.issuer.error.completeIssuer");
			logger.error("Cannot get Issuer, ", e);
			return null;
		}
	}

	public boolean isActionRequestCertificate() {
		if (action == null || action.equals(SkillsConstants.REQUEST_CERTIFICATE) == false) {
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

	public List<SelectItem> getSkillsApprovalSelection() {
		if (skillsApprovalSelection == null) {
			skillsApprovalSelection = new ArrayList<SelectItem>();
			for (ApprovalStatus status : ApprovalStatus.values()) {
				skillsApprovalSelection.add(new SelectItem(status, status.getLocaleText()));
			}
		}
		return skillsApprovalSelection;
	}

	public List<SkillsEntity> getAllSkills() {
		try {
			if (allSkills == null) {
				allSkills = skillsLogic.getAllObtainableSkillsWithFilteringNotApproved();
				for (SkillsEntity skill : skillsCertificateEntity.getAppliesForSkills()) {
					if (skill.isObtainable() == false) {
						allSkills.add(skill); // add all existing but non-obtainable 
					}
				}
			}
			return allSkills;
		} catch (Exception e) {
			logger.error("", e);
			JsfUtils.addErrorMessage(resourceBundle, "error.global");
			return new ArrayList<SkillsEntity>();
		}
	}
	
	public String getCertificateStatus(SkillsCertificateEntity skillsCertificateEntity) {
		if (skillsCertificateEntity == null || skillsCertificateEntity.getApprovalStatus() == null) {
			return "-";
		} else {
			return skillsCertificateEntity.getApprovalStatus().getLocaleText();
		}
	}
	
	public void actionApproveCertificate() {
		skillsCertificateEntity.setApprovalStatus(ApprovalStatus.APPROVED);
		skillsCertificateEntity.setRequestedFrom(null);
	}
	
	public boolean isCertificatePending() {
		if (skillsCertificateEntity == null) {
			return false;
		}
		return ApprovalStatus.PENDING.equals(skillsCertificateEntity.getApprovalStatus());
	}

	public String getIssuerName() {
		return issuerName;
	}

	public void setIssuerName(String issuerName) {
		this.issuerName = issuerName;
	}

	public SkillsCertificateEntity getSkillsCertificateEntity() {
		return skillsCertificateEntity;
	}

	public void setSkillsCertificateEntity(SkillsCertificateEntity skillsCertificateEntity) {
		this.skillsCertificateEntity = skillsCertificateEntity;
	}

	@Override
	public String getHeight() {
		return "35em";
	}

	@Override
	public String getWidth() {
		return "42em";
	}

	public List<SkillsEntity> getSelectedSkills() {
		return selectedSkills;
	}

	public void setSelectedSkills(List<SkillsEntity> selectedSkills) {
		this.selectedSkills = selectedSkills;
	}

	public void setSkillsApprovalSelection(List<SelectItem> skillsApprovalSelection) {
		this.skillsApprovalSelection = skillsApprovalSelection;
	}

	public boolean isIssuerNotListed() {
		return issuerNotListed;
	}

	public void setIssuerNotListed(boolean issuerNotListed) {
		this.issuerNotListed = issuerNotListed;
	}
}
