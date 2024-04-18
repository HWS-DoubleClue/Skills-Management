package com.doubleclue.dcem.skills.gui;

import java.util.List;
import java.util.ResourceBundle;
import java.util.SortedSet;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.PrimeFaces;
import org.primefaces.event.CellEditEvent;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.AutoViewAction;
import com.doubleclue.dcem.core.gui.DcemDialog;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.doubleclue.dcem.skills.entities.SkillsCertificateEntity;
import com.doubleclue.dcem.skills.entities.SkillsCertificatePriorityEntity;
import com.doubleclue.dcem.skills.entities.SkillsEntity;
import com.doubleclue.dcem.skills.entities.SkillsJobProfileEntity;
import com.doubleclue.dcem.skills.entities.SkillsLevelEntity;
import com.doubleclue.dcem.skills.logic.SkillsCertificateLogic;
import com.doubleclue.dcem.skills.logic.SkillsConstants;
import com.doubleclue.dcem.skills.logic.SkillsJobProfileEntityLogic;
import com.doubleclue.dcem.skills.logic.SkillsModule;
import com.doubleclue.dcem.skills.utils.SkillsUtils;

@Named("skillsJobProfileEntityDialog")
@SessionScoped
public class SkillsJobProfileEntityDialog extends DcemDialog implements OnCellEditInterfaces {

	private Logger logger = LogManager.getLogger(SkillsJobProfileEntityDialog.class);

	@Inject
	private SkillsJobProfileEntityLogic skillsJobProfileEntityLogic;

	@Inject
	private SkillsCertificateLogic skillsCertificateLogic;

	@Inject
	private OperatorSessionBean operatorSessionBean;

	@Inject
	private SkillsTreeTable skillsTreeTable;

	private String newCertificateName;
	private int certificatePriority;

	private SkillsJobProfileEntity jobProfileEntity;
	private SortedSet<SkillsCertificatePriorityEntity> certificatesPriority;
	
	private ResourceBundle resourceBundle;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@PostConstruct
	private void init() {
		resourceBundle = JsfUtils.getBundle(SkillsModule.RESOURCE_NAME, operatorSessionBean.getLocale());
	}

	/*
	* This method is called on OK button
	* @return true to close the dialog 
	* 
	*/
	@Override
	public boolean actionOk() throws Exception {
		if (SkillsUtils.isValidName(jobProfileEntity.getName()) == false) {
			JsfUtils.addErrorMessage(JsfUtils.getStringSafely(resourceBundle, "skills.invalidCharacters") + ": " + SkillsConstants.SPECIAL_CHARACTERS); 
			return false;
		}
		jobProfileEntity.setCertificatesPriorities(certificatesPriority);
		jobProfileEntity.setManagedBy(operatorSessionBean.getDcemUser());
		jobProfileEntity.setSkillLevels(skillsTreeTable.getSkillAvailableList(null, null));
		skillsJobProfileEntityLogic.addOrUpdate(jobProfileEntity, this.getAutoViewAction().getDcemAction());
		newCertificateName = null;
		return true;
	}

	@Override
	public String getHeight() {
		return "74vh";
	}

	@Override
	public String getWidth() {
		return "800";
	}

	public void removeCertificate(SkillsCertificatePriorityEntity certificatePriorityEntity) {
		certificatesPriority.remove(certificatePriorityEntity);
	}
	
	public void actionAddCertificate() {
		try {
		SkillsCertificateEntity certificateEntity = skillsCertificateLogic.getCertificateByName(newCertificateName);
		if (certificateEntity == null) {
			JsfUtils.addErrorMessage(resourceBundle, "skillsJobprofileDialog.error.certificateNotFound");
			return;
		}
		SkillsCertificatePriorityEntity certificatePriorityEntity = skillsCertificateLogic.getOrCreateCertificatePriority(certificateEntity,
				certificatePriority);
		for (SkillsCertificatePriorityEntity certificatePriority : certificatesPriority) {
			if (certificatePriority.getCertificateEntity().getId() == certificateEntity.getId()) {
				JsfUtils.addErrorMessage(resourceBundle, "skillsJobprofileDialog.warn.certificateExistsAlready"); 
				return;
			}
		}
		certificatesPriority.add(certificatePriorityEntity);
		PrimeFaces.current().executeScript("PF('certificateDialog').hide();");
		} catch (Exception e) {
			logger.error("", e);
			JsfUtils.addErrorMessage(resourceBundle, "error.global");
		}
	}

	public List<String> autoCompleteSkillsCertificatesName(String name) {
		try {
			return skillsCertificateLogic.getAutoCompleteCertificateNameListWithFilteringNotApproved(name, 30);
		} catch (Throwable e) {
			JsfUtils.addErrorMessage(e.getMessage());
			logger.error("autocomplete " + name, e);
			return null;
		}
	}

	/*
	* This method is called before Dialog is opened
	* 
	*/
	@Override
	public void show(DcemView dcemView, AutoViewAction autoViewAction) throws Exception {
		String action = this.getAutoViewAction().getDcemAction().getAction();
		jobProfileEntity = (SkillsJobProfileEntity) this.getActionObject();
		if (action.equals(DcemConstants.ACTION_EDIT)) {
			jobProfileEntity = skillsJobProfileEntityLogic.findJobProfileById(jobProfileEntity.getId());
		}
		certificatesPriority = jobProfileEntity.getCertificatesPriorities();
		certificatesPriority.isEmpty(); // load certificates from db to avoid lazy problems
		skillsTreeTable.updateSkillTree(jobProfileEntity.getSkillLevels(), this);
	}

	public void leaving() {
		certificatesPriority = null;
	}

	@Override
	public void actionOnCellEdit(CellEditEvent<?> event) throws DcemException, Exception {
	}

	@Override
	public void listenShowAllSkills() {
		try {
			SortedSet<SkillsLevelEntity> currentSelectedSkillLevels = skillsTreeTable.getSkillAvailableList(null, null);
			skillsTreeTable.updateSkillTree(currentSelectedSkillLevels, this);
		} catch (Exception e) {
			JsfUtils.addErrorMessage("Opps something went wrong. " + e.getMessage());
			logger.log(Level.WARN, "Unable to update skill tree for jobprofile", e);
		}
	}
	
	public boolean isSkillObtainableOrUsed(SkillsEntity skill) {
		if (skill.isObtainable()) {
			return true;
		}
		if (jobProfileEntity != null && jobProfileEntity.getSkillLevels() != null) {
			return jobProfileEntity.getSkillLevels().stream().anyMatch(skillLevel -> skillLevel.getSkill().equals(skill));
		}
		return false;
		
	}

	public SkillsJobProfileEntity getJobProfileEntity() {
		return jobProfileEntity;
	}

	public void setJobProfileEntity(SkillsJobProfileEntity jobProfileEntity) {
		this.jobProfileEntity = jobProfileEntity;
	}

	public String getNewCertificateName() {
		return newCertificateName;
	}

	public void setNewCertificateName(String newCertificateName) {
		this.newCertificateName = newCertificateName;
	}

	public int getCertificatePriority() {
		return certificatePriority;
	}

	public void setCertificatePriority(int certificatePriority) {
		this.certificatePriority = certificatePriority;
	}

	public SortedSet<SkillsCertificatePriorityEntity> getCertificatesPriority() {
		return certificatesPriority;
	}

	public void setCertificatesPriority(SortedSet<SkillsCertificatePriorityEntity> certificatesPriority) {
		this.certificatesPriority = certificatesPriority;
	}
}
