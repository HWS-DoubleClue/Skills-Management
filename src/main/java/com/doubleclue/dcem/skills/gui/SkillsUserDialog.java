package com.doubleclue.dcem.skills.gui;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.MediaType;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.PrimeFaces;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import com.doubleclue.comm.thrift.CloudSafeOptions;
import com.doubleclue.comm.thrift.CloudSafeOwner;
import com.doubleclue.dcem.as.entities.CloudSafeEntity;
import com.doubleclue.dcem.as.entities.DeviceEntity;
import com.doubleclue.dcem.as.logic.CloudSafeLogic;
import com.doubleclue.dcem.as.logic.CloudSafeUploadFile;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.gui.AutoDialogBean;
import com.doubleclue.dcem.core.gui.AutoViewAction;
import com.doubleclue.dcem.core.gui.AutoViewBean;
import com.doubleclue.dcem.core.gui.DcemDialog;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.doubleclue.dcem.core.logic.UserLogic;
import com.doubleclue.dcem.core.utils.DcemUtils;
import com.doubleclue.dcem.skills.entities.SkillsCertificateEntity;
import com.doubleclue.dcem.skills.entities.SkillsEntity;
import com.doubleclue.dcem.skills.entities.SkillsJobProfileEntity;
import com.doubleclue.dcem.skills.entities.SkillsUserCertificateEntity;
import com.doubleclue.dcem.skills.entities.SkillsUserEntity;
import com.doubleclue.dcem.skills.entities.SkillsUserJobProfileEntity;
import com.doubleclue.dcem.skills.entities.SkillsUserSkillEntity;
import com.doubleclue.dcem.skills.entities.enums.AvailabilityStatus;
import com.doubleclue.dcem.skills.entities.enums.SkillsLevel;
import com.doubleclue.dcem.skills.entities.enums.SkillsStatus;
import com.doubleclue.dcem.skills.logic.SkillsCertificateLogic;
import com.doubleclue.dcem.skills.logic.SkillsConstants;
import com.doubleclue.dcem.skills.logic.SkillsEmailLogic;
import com.doubleclue.dcem.skills.logic.SkillsJobProfileEntityLogic;
import com.doubleclue.dcem.skills.logic.SkillsLevelLogic;
import com.doubleclue.dcem.skills.logic.SkillsLogic;
import com.doubleclue.dcem.skills.logic.SkillsModule;
import com.doubleclue.dcem.skills.logic.SkillsUserLogic;

@SessionScoped
@Named("skillsUserDialog")
public class SkillsUserDialog extends DcemDialog {

	private Logger logger = LogManager.getLogger(SkillsUserDialog.class);
	private final int MAX_DESCRIPTION_LENGTH = 1024 * 4;
	private static final long serialVersionUID = 1L;

	@Inject
	AutoDialogBean autoDialogBean;

	@Inject
	UserLogic userLogic;

	@Inject
	AutoViewBean autoViewBean;

	@Inject
	OperatorSessionBean operatorSessionBean;

	@Inject
	SkillsLogic skillsLogic;

	@Inject
	SkillsUserLogic skillsUserLogic;

	@Inject
	SkillsCertificateLogic skillsCertificateLogic;

	@Inject
	SkillsLevelLogic skillsLevelLogic;

	@Inject
	SkillsTreeTable skillsTreeTable;

	@Inject
	SkillsUserView skillsUserView;

	@Inject
	SkillsJobProfileEntityLogic skillsJobProfileEntityLogic;

	@Inject
	SkillsEmailLogic skillsEmailLogic;

	@Inject
	SkillsModule skillsModule;

	@Inject
	CloudSafeLogic cloudSafeLogic;

	private ResourceBundle resourceBundle;

	private SkillsUserEntity skillsUserEntity;

	private DcemUser dcemUser;
	private byte[] image;
	private DcemUser reportsToDcemUser;
	private byte[] reportsToImage;
	private String action;
	private int editIndex;
	private boolean addUser;
	private boolean editObject;
	private boolean changedUserProfile;
	private List<SelectItem> skillsStatusSelection;
	private List<SelectItem> skillsAvailabilitySelection;

	// #### Skills ####
	private List<SkillsUserSkillEntity> userSkills;
	private List<SkillsUserSkillEntity> selectedUserSkills;
	private SkillsUserSkillEntity skillsUserSkillEntity;
	private String skillNameWithParent;

	// #### Certificates ####
	private List<SkillsUserCertificateEntity> userCertificates;
	private List<SkillsUserCertificateEntity> selectedUserCertificates;
	private SkillsUserCertificateEntity skillsUserCertificateEntity;
	private SkillsCertificateEntity skillsCertificateEntity;
	private String certificateName;
	private int certificateId;
	private HashMap<SkillsCertificateEntity, List<CloudSafeUploadFile>> mapCertificateToUploadedFiles;
	private List<CloudSafeUploadFile> displayedFiles;
	private List<CloudSafeUploadFile> newUploadedFiles;
	private List<CloudSafeEntity> deletedFiles;
	private CloudSafeEntity cloudSafeRoot;
	private List<CloudSafeUploadFile> selectedFiles = new LinkedList<CloudSafeUploadFile>();
	private StreamedContent downloadFile;
	private boolean multipleFiles;

	// #### Profiles ####
	private List<SkillsUserJobProfileEntity> userJobProfiles;
	private List<SkillsUserJobProfileEntity> selectedUserJobProfiles;
	private SkillsUserJobProfileEntity skillsUserJobProfileEntity;
	private String jobProfileName;

	// #### Properties ####
	private String reportsToUserLoginId;

	@PostConstruct
	private void init() {
		resourceBundle = JsfUtils.getBundle(SkillsModule.RESOURCE_NAME, operatorSessionBean.getLocale());
		cloudSafeRoot = cloudSafeLogic.getCloudSafeRoot();
	}

	@Override
	public void show(DcemView dcemView, AutoViewAction autoViewAction) throws Exception {
		action = getAutoViewAction().getDcemAction().getAction();
		if (action.equals(DcemConstants.ACTION_DELETE)) {
			return;
		}
		skillsUserEntity = (SkillsUserEntity) dcemView.getActionObject();
		if (action.equals(DcemConstants.ACTION_EDIT) || action.equals(SkillsConstants.ACTION_EDIT_MYSKILLS)) {
			addUser = false;
			dcemUser = userLogic.getUser(skillsUserEntity.getDcemUser().getId());
			image = dcemUser.getPhoto();
			reportsToDcemUser = skillsUserEntity.getReportsTo() == null ? null : userLogic.getUser(skillsUserEntity.getReportsTo().getId());
			reportsToImage = reportsToDcemUser == null ? null : reportsToDcemUser.getPhoto();
			userSkills = skillsUserLogic.getSkillsOfUser(skillsUserEntity.getId());
			userCertificates = skillsCertificateLogic.getAllCertificatesFromUserWithFiles(skillsUserEntity);
			userJobProfiles = skillsJobProfileEntityLogic.getJobProfilesOfUser(skillsUserEntity.getId());
			for (SkillsUserCertificateEntity userCertificate : userCertificates) {
				userCertificate.getSkillsCertificateEntity().getAppliesForSkills().size();
			}
			updateMatches();
		} else if (action.equals(DcemConstants.ACTION_ADD)) {
			addUser = true;
			dcemUser = null;
			image = null;
			reportsToDcemUser = null;
			reportsToImage = null;
			userSkills = new ArrayList<SkillsUserSkillEntity>();
			userCertificates = new ArrayList<SkillsUserCertificateEntity>();
			userJobProfiles = new ArrayList<SkillsUserJobProfileEntity>();
		}

		changedUserProfile = false;
		skillsUserSkillEntity = new SkillsUserSkillEntity();
		skillsUserCertificateEntity = new SkillsUserCertificateEntity();
		skillsCertificateEntity = new SkillsCertificateEntity();
		skillsUserJobProfileEntity = new SkillsUserJobProfileEntity();
		deletedFiles = new LinkedList<CloudSafeEntity>();
		mapCertificateToUploadedFiles = new HashMap<SkillsCertificateEntity, List<CloudSafeUploadFile>>();
	}

	@Override
	public boolean actionOk() throws Exception {
		if (skillsUserEntity.getDescription() != null && skillsUserEntity.getDescription().length() > MAX_DESCRIPTION_LENGTH) {
			JsfUtils.addErrorMessage(SkillsModule.RESOURCE_NAME, "skillsUserDialog.error.userDescriptionTooLong");
			return false;
		}
		SkillsUserEntity oldSkillsUser = new SkillsUserEntity();
		if (this.getAutoViewAction().getDcemAction().getAction().equals(DcemConstants.ACTION_ADD)
				|| this.getAutoViewAction().getDcemAction().getAction().equals(DcemConstants.ACTION_COPY)) {
			if (dcemUser == null) {
				JsfUtils.addErrorMessage(SkillsModule.RESOURCE_NAME, "skillsUserDialog.invalidUser");
				return false;
			}
			SkillsUserEntity skillsUser = skillsUserLogic.getSkillsUserById(dcemUser.getId());
			if (skillsUser != null) {
				JsfUtils.addErrorMessage(SkillsModule.RESOURCE_NAME, "skillsUserDialog.userAlreadyExists");
				return false;
			}
			skillsUserEntity.setDcemUser(dcemUser);
		} else {
			oldSkillsUser = loadOldData(skillsUserEntity);
		}
		skillsUserEntity.setReportsTo(reportsToDcemUser);
		if (AvailabilityStatus.AvailableFrom.equals(skillsUserEntity.getAvailability()) == false) {
			skillsUserEntity.setAvailableFrom(null);
		}
		skillsUserEntity.setSkills(userSkills);
		skillsUserEntity.setCertificates(userCertificates);
		skillsUserEntity.setUserJobProfiles(userJobProfiles);
		prepareFilesForUpload(skillsUserEntity, mapCertificateToUploadedFiles);
		skillsUserLogic.addOrUpdateUserEntityWithCertificates(skillsUserEntity, this.getAutoViewAction().getDcemAction(), mapCertificateToUploadedFiles,
				deletedFiles);

		if (changedUserProfile) {
			skillsEmailLogic.notifySkillsChange(oldSkillsUser, skillsUserEntity);
		}
		return true;
	}

	private SkillsUserEntity loadOldData(SkillsUserEntity skillsUserEntity) throws Exception {
		SkillsUserEntity oldSkillsUser = new SkillsUserEntity();
		oldSkillsUser.setSkills(skillsUserLogic.getSkillsOfUser(skillsUserEntity.getId()));
		oldSkillsUser.setCertificates(skillsCertificateLogic.getAllCertificatesFromUserWithFiles(skillsUserEntity));
		oldSkillsUser.setUserJobProfiles(skillsJobProfileEntityLogic.getJobProfilesOfUser(skillsUserEntity.getId()));
		skillsUserLogic.detachObjectsFromSkillsUser(oldSkillsUser);
		return oldSkillsUser;
	}

	private void prepareFilesForUpload(SkillsUserEntity skillsUserEntity,
			HashMap<SkillsCertificateEntity, List<CloudSafeUploadFile>> mapCertificateToUploadedFiles) {
		for (List<CloudSafeUploadFile> uploadedFilesForCert : mapCertificateToUploadedFiles.values()) {
			for (CloudSafeUploadFile cloudSafeUploadFile : uploadedFilesForCert) {
				cloudSafeUploadFile.getCloudSafeEntity()
						.setName(getUniqueFileNamePrefix(skillsUserCertificateEntity, skillsUserEntity) + cloudSafeUploadFile.getFileName());
				cloudSafeUploadFile.getCloudSafeEntity().setUser(skillsUserEntity.getDcemUser());
			}
		}
	}

	public void userListener() {
		SkillsUserEntity selectedSkillsUser = skillsUserLogic.getSkillsUserById(dcemUser.getId());
		if (selectedSkillsUser != null) {
			dcemUser = null;
			image = null;
			JsfUtils.addErrorMessage(SkillsModule.RESOURCE_NAME, "skillsUserDialog.skillUserExistsAlready");
		}

	}

	@Override
	public void leavingDialog() {
		dcemUser = null;
		image = null;
		reportsToDcemUser = null;
		reportsToImage = null;
		// #### Skills ####
		skillsUserEntity = null;
		userSkills = null;
		selectedUserSkills = null;
		skillsUserSkillEntity = null;
		skillNameWithParent = null;
		editIndex = 0;
		// #### Certificates ####
		userCertificates = null;
		selectedUserCertificates = null;
		skillsUserCertificateEntity = null;
		skillsCertificateEntity = null;
		selectedFiles = null;
		displayedFiles = null;
		certificateName = null;
		certificateId = 0;
		// #### JobProfiles ####
		userJobProfiles = null;
		selectedUserJobProfiles = null;
		skillsUserJobProfileEntity = null;
		jobProfileName = null;
		// #### Properties ####
		reportsToUserLoginId = null;

	}

	@Override
	public String getHeight() {
		return "45em";
	}

	@Override
	public String getWidth() {
		return "75em";
	}

	// ####################### SkillsTab #######################

	public void actionPrepareAddSkill() {
		skillsUserSkillEntity = new SkillsUserSkillEntity();
		skillsUserSkillEntity.setStatus(SkillsStatus.OWNS);
		skillNameWithParent = "";
		editObject = false;
		editIndex = -1;
	}

	public void actionPrepareEditSkill() {
		if (selectedUserSkills == null || selectedUserSkills.isEmpty()) {
			JsfUtils.addErrorMessage(SkillsModule.RESOURCE_NAME, "skillsUserDialog.invalid.emptySelection");
			return;
		}
		if (selectedUserSkills.size() > 1) {
			JsfUtils.addErrorMessage(SkillsModule.RESOURCE_NAME, "skillsUserDialog.invalid.tooManySelection");
			return;
		}
		try {
			skillsUserSkillEntity = new SkillsUserSkillEntity();
			DcemUtils.copyObject(selectedUserSkills.get(0), skillsUserSkillEntity);

			editObject = true;
			editIndex = getIndexOfUserSkill(skillsUserSkillEntity);
			skillNameWithParent = skillsUserSkillEntity.getSkill().getNameWithParent();
			PrimeFaces.current().executeScript("PF('skillsDialogVar').show();");
		} catch (Exception e) {
			JsfUtils.addErrorMessage(SkillsModule.RESOURCE_NAME, "error.global");
			logger.error("Could not load skill for edit", e);
		}
	}

	public void actionRemoveSkill() {
		if (selectedUserSkills == null || selectedUserSkills.isEmpty()) {
			JsfUtils.addErrorMessage(SkillsModule.RESOURCE_NAME, "skillsUserDialog.invalid.emptySelection");
			return;
		}
		try {
			for (SkillsUserSkillEntity removeSkill : selectedUserSkills) {
				int idx = getIndexOfUserSkill(removeSkill);
				userSkills.remove(idx);
			}
			changedUserProfile = true;
			List<String> skillsInOwnedCertificatesNotOwned = compareSkillsWithOwnedCertificate(selectedUserSkills);
			if (skillsInOwnedCertificatesNotOwned.isEmpty() == false) {
				JsfUtils.addWarnMessage(resourceBundle, "skillsUserDialog.warning.SkillRemovedButOwnedInCertificate",
						String.join(", ", skillsInOwnedCertificatesNotOwned));
			}
			updateMatches();
		} catch (Exception e) {
			logger.error("Could not remove skill targets", e);
			JsfUtils.addErrorMessage(SkillsModule.RESOURCE_NAME, "error.global");
		}
	}

	private List<String> compareSkillsWithOwnedCertificate(List<SkillsUserSkillEntity> userSkills) {
		List<String> skillsInCertificate = new ArrayList<String>();
		Set<SkillsEntity> allSkillsInCertificate = new HashSet<SkillsEntity>();
		for (SkillsUserCertificateEntity userCertificate : userCertificates) {
			allSkillsInCertificate.addAll(userCertificate.getSkillsCertificateEntity().getAppliesForSkills());
		}
		for (SkillsUserSkillEntity userSkill : userSkills) {
			if (userSkill.isSkillNotOwned() == false && allSkillsInCertificate.contains(userSkill.getSkill())) {
				skillsInCertificate.add(userSkill.getSkill().getNameWithParent());
			}
		}
		return skillsInCertificate;
	}

	public void actionAddOrUpdateSkill() {
		try {
			if (editObject == false) {
				SkillsEntity skill = skillsLogic.getSkillByNameAndParent(skillNameWithParent);
				if (skill.isObtainable() == false) {
					JsfUtils.addErrorMessageToComponentId(JsfUtils.getStringSafely(resourceBundle, "skillsUserDialog.invalid.nonObtainableSkill"),
							"skillsDialogForm:skillsDialogMsg");
					return;
				}
				skillsUserSkillEntity.setSkill(skill);
				skillsUserSkillEntity.setSkillsUserEntity(skillsUserEntity);
			}
			if (isValidNewSkill(skillsUserSkillEntity, editIndex) == false) {
				JsfUtils.addErrorMessageToComponentId(JsfUtils.getStringSafely(resourceBundle, "skillsUserDialog.invalid.duplicateSkillLevel"),
						"skillsDialogForm:skillsDialogMsg");
				return;
			}
			if (isSkillLowerThanOwnedSkill(skillsUserSkillEntity, editIndex)) {
				JsfUtils.addErrorMessageToComponentId(JsfUtils.getStringSafely(resourceBundle, "skillsUserDialog.invalid.lowerThanOwnedSkillLevel"),
						"skillsDialogForm:skillsDialogMsg");
				return;
			}
			if (editObject == false) {
				userSkills.add(skillsUserSkillEntity);
			} else {
				userSkills.set(editIndex, skillsUserSkillEntity);
			}
			if (skillsUserSkillEntity.getStatus().equals(SkillsStatus.OWNS)) {
				removeLowerThanOwnedSkill(skillsUserSkillEntity);
			}
			changedUserProfile = true;
			PrimeFaces.current().executeScript("PF('skillsDialogVar').hide();");
			updateMatches();
			return;
		} catch (Exception e) {
			logger.error("Could not add/edit skill target", e);
			JsfUtils.addErrorMessageToComponentId(JsfUtils.getStringSafely(resourceBundle, "error.global"), "skillsDialogForm:skillsDialogMsg");
		}
	}

	public List<String> actionCompleteSkillWithParent(String name) {
		List<String> filteredSkillsAsString = new ArrayList<String>();
		try {
			List<SkillsEntity> filteredSkils = skillsLogic.getAutoCompleteSkillsListWithFilteringNotApproved(name, 10);
			for (SkillsEntity skill : filteredSkils) {
				filteredSkillsAsString.add(skill.getNameWithParent());
			}
		} catch (Exception e) {
			logger.error("Could not create filtered skill list by search string: " + name, e);
			JsfUtils.addErrorMessageToComponentId(JsfUtils.getStringSafely(resourceBundle, "error.global"), "skillsDialogForm:skillsDialogMsg");
		}
		return filteredSkillsAsString;
	}

	private void removeLowerThanOwnedSkill(SkillsUserSkillEntity newUserSkill) {
		List<SkillsUserSkillEntity> updatedList = new ArrayList<SkillsUserSkillEntity>();
		for (SkillsUserSkillEntity userSkill : userSkills) {
			if (newUserSkill.getSkill().getId() == userSkill.getSkill().getId() && userSkill.getLevel().ordinal() < newUserSkill.getLevel().ordinal()) {
				continue;
			}
			updatedList.add(userSkill);
		}
		userSkills = updatedList;
	}

	private boolean isValidNewSkill(SkillsUserSkillEntity userSkill, int editIndex) throws Exception {
		int index = getIndexOfUserSkill(userSkill);
		if (index != -1 && index != editIndex) {
			return false;
		}
		return true;
	}

	private boolean isSkillLowerThanOwnedSkill(SkillsUserSkillEntity userSkill, int editIndex) throws Exception {
		int indexOfOwnedSkill = getIndexOfOwnedSkill(userSkill);
		if (indexOfOwnedSkill != -1 && indexOfOwnedSkill != editIndex) {
			SkillsUserSkillEntity ownedSkill = userSkills.get(indexOfOwnedSkill);
			if (userSkill.getLevel().ordinal() < ownedSkill.getLevel().ordinal()) {
				return true;
			}
		}
		return false;
	}

	private int getIndexOfUserSkill(SkillsUserSkillEntity userSkill) {
		for (int idx = 0; idx < userSkills.size(); idx++) {
			if (userSkills.get(idx).getSkill().getId() == userSkill.getSkill().getId() && userSkills.get(idx).getLevel().equals(userSkill.getLevel())) {
				return idx;
			}
		}
		return -1;
	}

	private int getIndexOfOwnedSkill(SkillsUserSkillEntity newUserSkill) throws Exception {
		for (int idx = 0; idx < userSkills.size(); idx++) {
			if (newUserSkill.getSkill().getId() == userSkills.get(idx).getSkill().getId() && userSkills.get(idx).getStatus().equals(SkillsStatus.OWNS)) {
				return idx;
			}
		}
		return -1;
	}

	// ####################### CertificateTab #######################

	public void actionPrepareAddCertificate() {
		skillsUserCertificateEntity = new SkillsUserCertificateEntity();
		skillsUserCertificateEntity.setStatus(SkillsStatus.OWNS);
		certificateName = "";
		selectedFiles = new ArrayList<CloudSafeUploadFile>();
		displayedFiles = new LinkedList<CloudSafeUploadFile>();
		newUploadedFiles = new LinkedList<CloudSafeUploadFile>();
		editObject = false;
		editIndex = -1;
	}

	public void actionPrepareEditCertificate() {
		if (selectedUserCertificates == null || selectedUserCertificates.isEmpty()) {
			JsfUtils.addErrorMessage(SkillsModule.RESOURCE_NAME, "skillsUserDialog.invalid.emptySelection");
			return;
		}
		if (selectedUserCertificates.size() > 1) {
			JsfUtils.addErrorMessage(SkillsModule.RESOURCE_NAME, "skillsUserDialog.invalid.tooManySelection");
			return;
		}
		try {
			skillsUserCertificateEntity = new SkillsUserCertificateEntity();
			DcemUtils.copyObject(selectedUserCertificates.get(0), skillsUserCertificateEntity);
			skillsCertificateEntity = selectedUserCertificates.get(0).getSkillsCertificateEntity();
			certificateName = skillsCertificateEntity.getName();

			selectedFiles = new ArrayList<CloudSafeUploadFile>();
			newUploadedFiles = new LinkedList<CloudSafeUploadFile>();
			displayedFiles = new LinkedList<CloudSafeUploadFile>();
			displayedFiles.addAll(mapCertificateToUploadedFiles.getOrDefault(skillsCertificateEntity, new LinkedList<CloudSafeUploadFile>()));
			for (CloudSafeEntity alreadySavedFiles : skillsUserCertificateEntity.getFiles()) {
				displayedFiles.add(new CloudSafeUploadFile(
						alreadySavedFiles.getName().substring(getUniqueFileNamePrefix(skillsUserCertificateEntity, skillsUserEntity).length()), null,
						alreadySavedFiles));
			}
			editObject = true;
			editIndex = getIndexOfUserCertificate(skillsUserCertificateEntity);
			PrimeFaces.current().executeScript("PF('certificateDialogVar').show();");
		} catch (Exception e) {
			JsfUtils.addErrorMessage(SkillsModule.RESOURCE_NAME, "error.global");
			logger.error("Could not load certificate for edit", e);
		}
	}

	public void actionRemoveCertificate() {
		if (selectedUserCertificates == null || selectedUserCertificates.isEmpty()) {
			JsfUtils.addErrorMessage(SkillsModule.RESOURCE_NAME, "skillsUserDialog.invalid.emptySelection");
			return;
		}
		try {
			if (deletedFiles == null) {
				deletedFiles = new LinkedList<CloudSafeEntity>();
			}
			for (SkillsUserCertificateEntity removeCertificate : selectedUserCertificates) {
				int idx = getIndexOfUserCertificate(removeCertificate);
				userCertificates.remove(idx);
				deletedFiles.addAll(removeCertificate.getFiles());
			}
			changedUserProfile = true;
			updateMatches();
		} catch (Exception e) {
			logger.error("Could not remove certificates", e);
			JsfUtils.addErrorMessage(SkillsModule.RESOURCE_NAME, "error.global");
		}
	}

	public void actionAddOrUpdateCertificate() {
		try {
			if (editObject == false) {
				skillsUserCertificateEntity.setSkillsUserEntity(skillsUserEntity);
			}
			SkillsCertificateEntity skillsCertificate = skillsCertificateLogic.getCertificateByName(certificateName);
			skillsUserCertificateEntity.setSkillsCertificateEntity(skillsCertificate);
			if (isValidNewCertificate(skillsUserCertificateEntity, editIndex) == false) {
				JsfUtils.addErrorMessageToComponentId(JsfUtils.getStringSafely(resourceBundle, "skillsUserDialog.invalid.duplicateCertificate"),
						"certificateDialogForm:certificateDialogMsg");
				return;
			}
			if (editObject == false) {
				userCertificates.add(skillsUserCertificateEntity);
			} else {
				userCertificates.set(editIndex, skillsUserCertificateEntity);
			}
			if (skillsUserCertificateEntity.getExpirationDate() != null && skillsUserCertificateEntity.getExpirationDate().isAfter(LocalDate.now())) {
				skillsUserCertificateEntity.setSendNotification(0);
			}

			List<CloudSafeUploadFile> allFilesToUpload = mapCertificateToUploadedFiles.getOrDefault(skillsCertificate, new ArrayList<CloudSafeUploadFile>());
			allFilesToUpload.addAll(newUploadedFiles);
			mapCertificateToUploadedFiles.put(skillsCertificate, allFilesToUpload);
			changedUserProfile = true;
			PrimeFaces.current().executeScript("PF('certificateDialogVar').hide();");
			List<String> skillsInOwnedCertificatesNotOwned = compareOwnedCertificateWithOwnedSkills(skillsUserCertificateEntity);
			if (skillsInOwnedCertificatesNotOwned.isEmpty() == false) {
				JsfUtils.addWarnMessage(resourceBundle, "skillsUserDialog.warning.CertificateSkillNotOwned",
						skillsUserCertificateEntity.getSkillsCertificateEntity().getName(), String.join(", ", skillsInOwnedCertificatesNotOwned));
			}
			updateMatches();
			return;
		} catch (Exception e) {
			logger.error("Could not add/edit certificate", e);
			JsfUtils.addErrorMessageToComponentId(JsfUtils.getStringSafely(resourceBundle, "error.global"), "certificateDialogForm:certificateDialogMsg");
		}
	}

	private List<String> compareOwnedCertificateWithOwnedSkills(SkillsUserCertificateEntity userCertificate) {
		List<String> skillsNotOwned = new ArrayList<String>();
		if (userCertificate.isCertificateNotOwned()) {
			return skillsNotOwned;
		}
		Set<SkillsEntity> ownedSkills = userSkills.stream().filter(userSkill -> userSkill.isSkillNotOwned() == false).map(userSkill -> userSkill.getSkill())
				.collect(Collectors.toSet());

		for (SkillsEntity certificateSkill : userCertificate.getSkillsCertificateEntity().getAppliesForSkills()) {
			if (ownedSkills.contains(certificateSkill) == false) {
				skillsNotOwned.add(certificateSkill.getNameWithParent());
			}
		}
		return skillsNotOwned;
	}

	public List<String> actionCompleteCertificate(String name) {
		List<String> filteredCertificatesAsString = new ArrayList<String>();
		try {
			return skillsCertificateLogic.getAutoCompleteCertificateNameListWithFilteringNotApproved(name, 30);
		} catch (Exception e) {
			logger.error("Could not create filtered certificate list by search string: " + name, e);
			JsfUtils.addErrorMessageToComponentId(JsfUtils.getStringSafely(resourceBundle, "error.global"), "certificateDialogForm:certificateDialogMsg");
		}
		return filteredCertificatesAsString;
	}

	private boolean isValidNewCertificate(SkillsUserCertificateEntity userCertificate, int editIndex) {
		int index = getIndexOfUserCertificate(userCertificate);
		if (index != -1 && index != editIndex) {
			return false;
		}
		return true;
	}

	private int getIndexOfUserCertificate(SkillsUserCertificateEntity skillsUserCertificateEntity) {
		for (int idx = 0; idx < userCertificates.size(); idx++) {
			if (userCertificates.get(idx).getSkillsCertificateEntity().getId() == skillsUserCertificateEntity.getSkillsCertificateEntity().getId()) {
				return idx;
			}
		}
		return -1;
	}

	private String getUniqueFileNamePrefix(SkillsUserCertificateEntity skillsUserCertificateEntity, SkillsUserEntity skillsUserEntity) {
		return SkillsConstants.UPLOAD_CERTIFICATE_FILENAME_PREFIX + skillsUserCertificateEntity.getSkillsCertificateEntity().getId() + "."
				+ skillsUserEntity.getDcemUser().getId() + ".";
	}

	// ####################### FileUpload #######################

	public void handleFileUpload(FileUploadEvent event) {
		for (CloudSafeUploadFile uploadedFile : displayedFiles) {
			if (uploadedFile.fileName.equals(event.getFile().getFileName())) {
				JsfUtils.addErrorMessageToComponentId(
						JsfUtils.getStringSafely(resourceBundle, "skillsUserDialog.invalid.duplicateCertificateName") + ": " + uploadedFile.fileName,
						"certificateDialogForm:skillsDialogMsg");
				return;
			}
		}

		File tempFile = null;
		FileOutputStream os = null;
		InputStream is = null;
		try {
			tempFile = File.createTempFile("dcem-", "-certificateFile");
			os = new FileOutputStream(tempFile);
			is = event.getFile().getInputStream();

			byte[] buffer = new byte[1024 * 8];
			int length;
			while ((length = is.read(buffer)) > 0) {
				os.write(buffer, 0, length);
			}
		} catch (IOException e) {
			JsfUtils.addErrorMessageToComponentId(JsfUtils.getStringSafely(resourceBundle, "skillsUserDialog.error.unableToUploadFile"),
					"certificateDialogForm:certificateDialogMsg");
			logger.error("Unable to upload file " + tempFile.getName(), e);
			return;
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					JsfUtils.addErrorMessageToComponentId(JsfUtils.getStringSafely(resourceBundle, "skillsUserDialog.error.unableToLoadFile"),
							"certificateDialogForm:certificateDialogMsg");
					logger.error("Unable to load file " + tempFile.getName(), e);
				}
			}

			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {
					JsfUtils.addErrorMessageToComponentId(JsfUtils.getStringSafely(resourceBundle, "skillsUserDialog.error.unableToLoadFile"),
							"certificateDialogForm:certificateDialogMsg");
					logger.error("Unable to save file " + tempFile.getName(), e);
				}
			}
		}

		CloudSafeEntity cloudSafeEntity = new CloudSafeEntity(CloudSafeOwner.GLOBAL, (DcemUser) null, (DeviceEntity) null, (String) null, (LocalDateTime) null,
				CloudSafeOptions.ENC.name(), false, cloudSafeRoot, operatorSessionBean.getDcemUser());
		cloudSafeEntity.setLength(event.getFile().getSize());
		CloudSafeUploadFile newFile = new CloudSafeUploadFile(event.getFile().getFileName(), tempFile, cloudSafeEntity);
		newUploadedFiles.add(newFile);
		displayedFiles.add(newFile);
	}

	public void actionDownloadSingleFile() {
		downloadFile = null;
		if (selectedFiles.isEmpty()) {
			JsfUtils.addErrorMessageToComponentId(JsfUtils.getStringSafely(resourceBundle, "skillsUserDialog.invalid.emptySelection"),
					"certificateDialogForm:certificateDialogMsg");
			return;
		}

		CloudSafeUploadFile selectedFile = selectedFiles.get(0);
		try {
			InputStream inputStream = selectedFile.getCloudSafeEntity().getId() != null
					? cloudSafeLogic.getCloudSafeContentAsStream(selectedFile.getCloudSafeEntity(), null, null)
					: new FileInputStream(selectedFile.getFile());

			downloadFile = DefaultStreamedContent.builder().name(selectedFiles.get(0).fileName).contentType(MediaType.APPLICATION_FORM_URLENCODED)
					.stream(() -> inputStream).build();

		} catch (Exception e) {
			JsfUtils.addErrorMessageToComponentId(JsfUtils.getStringSafely(resourceBundle, "skillsUserDialog.error.unableToDownloadFile"),
					"certificateDialogForm:certificateDialogMsg");
			logger.error("Unable to download file " + downloadFile.getName(), e);
		}
	}

	public void actionDownloadMultipleFilesOrFolders() {
		if (selectedFiles == null || selectedFiles.isEmpty()) {
			JsfUtils.addErrorMessageToComponentId(JsfUtils.getStringSafely(resourceBundle, "skillsUserDialog.invalid.emptySelection"),
					"certificateDialogForm:certificateDialogMsg");
			return;
		}

		try {
			OutputStream output = JsfUtils.getDownloadFileOutputStream("application/zip",
					skillsUserEntity.getDcemUser().getDisplayNameOrLoginId() + "_" + SkillsConstants.CERTIFICATES_DATA_ZIP);
			ZipOutputStream zipOutputStream = new ZipOutputStream(output);
			for (CloudSafeUploadFile uploadedFile : selectedFiles) {
				String path = uploadedFile.fileName;
				zipFoldersOrFiles(path, zipOutputStream, output, uploadedFile);
			}
			zipOutputStream.close();
			output.close();
			selectedFiles = new ArrayList<CloudSafeUploadFile>();
			FacesContext.getCurrentInstance().responseComplete();
		} catch (Exception e) {
			JsfUtils.addErrorMessageToComponentId(JsfUtils.getStringSafely(resourceBundle, "skillsUserDialog.error.unableToDownloadFile"),
					"certificateDialogForm:certificateDialogMsg");
			logger.error("Unable to download archive of selected files ", e);
			FacesContext.getCurrentInstance().responseComplete();
		}
	}

	private void zipFoldersOrFiles(String path, ZipOutputStream zipOutputStream, OutputStream output, CloudSafeUploadFile uploadedFile) throws Exception {
		zipOutputStream.setLevel(Deflater.BEST_SPEED);
		int length = -1;
		byte[] buffer = new byte[1024 * 16];

		try (InputStream inputStream = uploadedFile.getCloudSafeEntity().getId() != null
				? cloudSafeLogic.getCloudSafeContentAsStream(uploadedFile.getCloudSafeEntity(), null, null)
				: new FileInputStream(uploadedFile.getFile()); BufferedInputStream bis = new BufferedInputStream(inputStream)) {

			ZipEntry zipEntry = new ZipEntry(uploadedFile.getFileName());
			zipOutputStream.putNextEntry(zipEntry);
			while ((length = bis.read(buffer)) != -1) {
				zipOutputStream.write(buffer, 0, length);
			}
		} catch (Exception e) {
			logger.info("Unable to create archive ", e);
			throw e;
		}
	}

	public void removeFile() {
		if (selectedFiles.isEmpty()) {
			JsfUtils.addErrorMessageToComponentId(JsfUtils.getStringSafely(resourceBundle, "skillsUserDialog.invalid.emptySelection"),
					"certificateDialogForm:certificateDialogMsg");
			return;
		}

		for (CloudSafeUploadFile selectedUploadedFile : selectedFiles) {
			Iterator<CloudSafeUploadFile> iterator = displayedFiles.iterator();
			while (iterator.hasNext()) {
				CloudSafeUploadFile toRemoveObject = iterator.next();
				if (selectedUploadedFile.fileName.equals(toRemoveObject.fileName)) {
					iterator.remove();
				}
			}
			for (CloudSafeUploadFile dcemUploadFile : displayedFiles) {
				if (dcemUploadFile.fileName.equals(selectedUploadedFile.fileName)) {
					displayedFiles.remove(dcemUploadFile);
					break;
				}
			}
			if (Objects.isNull(selectedUploadedFile.file) == true) {
				skillsUserCertificateEntity.getFiles().remove(selectedUploadedFile.cloudSafeEntity);
				deletedFiles.add(selectedUploadedFile.cloudSafeEntity);
			}
		}
		selectedFiles = null;
	}

	public void setMultipleFiles() {
		if (selectedFiles == null || selectedFiles.size() <= 1) {
			multipleFiles = false;
		}
		if (selectedFiles.size() > 1) {
			multipleFiles = true;
		}
	}

	public String toUploadFile(CloudSafeUploadFile cloudSafeUploadFile) {
		if (cloudSafeUploadFile.getFile() != null) {
			return JsfUtils.getStringSafely(resourceBundle, "skillsUserDialog.toUpload");
		}
		return JsfUtils.getStringSafely(resourceBundle, "skillsUserDialog.uploaded");
	}

	public boolean isMultipleFiles() {
		return multipleFiles;
	}

	public void setMultipleFiles(boolean multipleFiles) {
		this.multipleFiles = multipleFiles;
	}

	// ####################### JobProfiles #######################

	public void actionPrepareAddJobProfile() {
		skillsUserJobProfileEntity = new SkillsUserJobProfileEntity();
		skillsUserJobProfileEntity.setStatus(SkillsStatus.RECOMMENDED);
		jobProfileName = "";
		editObject = false;
		editIndex = -1;
	}

	public void actionPrepareEditJobProfile() {
		if (selectedUserJobProfiles == null || selectedUserJobProfiles.isEmpty()) {
			JsfUtils.addErrorMessage(SkillsModule.RESOURCE_NAME, "skillsUserDialog.invalid.emptySelection");
			return;
		}
		if (selectedUserJobProfiles.size() > 1) {
			JsfUtils.addErrorMessage(SkillsModule.RESOURCE_NAME, "skillsUserDialog.invalid.tooManySelection");
			return;
		}
		try {
			skillsUserJobProfileEntity = new SkillsUserJobProfileEntity();
			DcemUtils.copyObject(selectedUserJobProfiles.get(0), skillsUserJobProfileEntity);

			editObject = true;
			jobProfileName = skillsUserJobProfileEntity.getJobProfile().getName();
			editIndex = getIndexOfUserProfile(skillsUserJobProfileEntity);
			PrimeFaces.current().executeScript("PF('jobProfileDialogVar').show();");
		} catch (Exception e) {
			JsfUtils.addErrorMessage(SkillsModule.RESOURCE_NAME, "error.global");
			logger.error("Could not load job profile for edit", e);
		}
	}

	public void actionRemoveJobProfile() {
		if (selectedUserJobProfiles == null || selectedUserJobProfiles.isEmpty()) {
			JsfUtils.addErrorMessage(SkillsModule.RESOURCE_NAME, "skillsUserDialog.invalid.emptySelection");
			return;
		}
		try {
			for (SkillsUserJobProfileEntity removeJobProfile : selectedUserJobProfiles) {
				int idx = getIndexOfUserProfile(removeJobProfile);
				userJobProfiles.remove(idx);
			}
			changedUserProfile = true;
		} catch (Exception e) {
			logger.error("Could not remove job profile", e);
			JsfUtils.addErrorMessage(SkillsModule.RESOURCE_NAME, "error.global");
		}
	}

	public void actionAddOrUpdateJobProfile() {
		try {
			if (editObject == false) {
				SkillsJobProfileEntity jobProfile = skillsJobProfileEntityLogic.getJobProfileByName(jobProfileName);
				jobProfile.getSkillLevels().size(); // avoid lazy exception if computing match
				skillsUserJobProfileEntity.setJobProfile(jobProfile);
				skillsUserJobProfileEntity.setSkillsUserEntity(skillsUserEntity);
				if (isValidNewJobProfile(skillsUserJobProfileEntity, editIndex) == false) {
					JsfUtils.addErrorMessageToComponentId(JsfUtils.getStringSafely(resourceBundle, "skillsUserDialog.invalid.duplicateJobProfile"),
							"jobProfileDialogForm:jobProfileDialogMsg");
					return;
				}
				userJobProfiles.add(skillsUserJobProfileEntity);
			} else {
				userJobProfiles.set(editIndex, skillsUserJobProfileEntity);
			}
			changedUserProfile = true;
			updateMatches();
			PrimeFaces.current().executeScript("PF('jobProfileDialogVar').hide();");
			return;
		} catch (Exception e) {
			logger.error("Could not add/edit job profile", e);
			JsfUtils.addErrorMessageToComponentId(JsfUtils.getStringSafely(resourceBundle, "error.global"), "skillsDialogForm:skillsDialogMsg");
		}
	}

	public List<String> actionCompleteJobProfile(String name) {
		List<String> filteredJobProfilesAsString = new ArrayList<String>();
		try {
			List<SkillsJobProfileEntity> filteredJobProfiles = skillsJobProfileEntityLogic.getAutoCompleteJobProfileList(name, 10);
			for (SkillsJobProfileEntity jobProfile : filteredJobProfiles) {
				filteredJobProfilesAsString.add(jobProfile.getName());
			}
		} catch (Exception e) {
			logger.error("Could not create filtered jobprofile list by search string: " + name, e);
			JsfUtils.addErrorMessageToComponentId(JsfUtils.getStringSafely(resourceBundle, "error.global"), "jobProfileDialogForm:jobProfileDialogMsg");
		}
		return filteredJobProfilesAsString;
	}

	private boolean isValidNewJobProfile(SkillsUserJobProfileEntity skillsUserJobProfileEntity, int editIndex) throws Exception {
		int index = getIndexOfUserProfile(skillsUserJobProfileEntity);
		if (index != -1 && index != editIndex) {
			return false;
		}
		return true;
	}

	private int getIndexOfUserProfile(SkillsUserJobProfileEntity userJobProfile) {
		for (int idx = 0; idx < userJobProfiles.size(); idx++) {
			if (userJobProfiles.get(idx).getJobProfile().getId() == userJobProfile.getJobProfile().getId()) {
				return idx;
			}
		}
		return -1;
	}

	public boolean isAdminEditing() {
		return action.equals(SkillsConstants.ACTION_EDIT_MYSKILLS) == false;
	}

	private void updateMatches() {
		try {
			SkillsUserEntity tempUserEntity = new SkillsUserEntity(userSkills, userJobProfiles, userCertificates);
			SkillsUserLogic.updateAllMatches(tempUserEntity);
		} catch (Exception e) {
			logger.error("Could not recalculate match", e);
			JsfUtils.addErrorMessage(SkillsModule.RESOURCE_NAME, "skillsUserDialog.error.computingMatch");
		}
	}

	// ####################### Shared Utils #######################

	public List<SelectItem> getSkillsStatusSelection() {
		if (skillsStatusSelection == null) {
			skillsStatusSelection = new ArrayList<SelectItem>();
			for (SkillsStatus status : SkillsStatus.values()) {
				skillsStatusSelection.add(new SelectItem(status, status.getLocaleText()));
			}
		}
		return skillsStatusSelection;
	}

	public List<SelectItem> getSkillsLevelSelection() {
		List<SelectItem> skillsLevelSelection = new ArrayList<SelectItem>();
		for (SkillsLevel level : SkillsLevel.values()) {
			if (level.equals(SkillsLevel.No)) {
				continue;
			}
			skillsLevelSelection.add(new SelectItem(level, level.getLocaleText()));
		}
		return skillsLevelSelection;
	}

	public List<SelectItem> getSkillsAvailabilitySelection() {
		if (skillsAvailabilitySelection == null) {
			skillsAvailabilitySelection = new ArrayList<SelectItem>();
			for (AvailabilityStatus availabilityStatus : AvailabilityStatus.values()) {
				skillsAvailabilitySelection.add(new SelectItem(availabilityStatus, availabilityStatus.getLocaleText()));
			}
		}
		return skillsAvailabilitySelection;
	}

	public boolean isAvailableFrom() {
		if (skillsUserEntity == null) {
			return false;
		}
		return AvailabilityStatus.AvailableFrom.equals(skillsUserEntity.getAvailability());
	}

	public StreamedContent getUserPhoto() {
		try {
			if (image == null) {
				return JsfUtils.getDefaultUserImage();
			} else {
				InputStream in = new ByteArrayInputStream(image);
				return DefaultStreamedContent.builder().contentType("image/png").stream(() -> in).build();
			}
		} catch (Exception e) {
			return null;
		}
	}

	public StreamedContent getReportsToUserPhoto() {
		try {
			if (reportsToImage == null) {
				return JsfUtils.getDefaultUserImage();
			} else {
				InputStream in = new ByteArrayInputStream(reportsToImage);
				return DefaultStreamedContent.builder().contentType("image/png").stream(() -> in).build();
			}
		} catch (Exception e) {
			return null;
		}
	}

	// ####################### Getters/Setters #######################

	public boolean isAddUser() {
		return addUser;
	}

	public SkillsUserEntity getSkillsUserEntity() {
		return skillsUserEntity;
	}

	public void setSkillsUserEntity(SkillsUserEntity skillsUserEntity) {
		this.skillsUserEntity = skillsUserEntity;
	}

	public String getSkillNameWithParent() {
		return skillNameWithParent;
	}

	public void setSkillNameWithParent(String skillNameWithParent) {
		this.skillNameWithParent = skillNameWithParent;
	}

	public List<SkillsUserSkillEntity> getUserSkills() {
		return userSkills;
	}

	public void setUserSkills(List<SkillsUserSkillEntity> userSkills) {
		this.userSkills = userSkills;
	}

	public List<SkillsUserSkillEntity> getSelectedUserSkills() {
		return selectedUserSkills;
	}

	public void setSelectedUserSkills(List<SkillsUserSkillEntity> selectedSkills) {
		this.selectedUserSkills = selectedSkills;
	}

	public SkillsUserSkillEntity getSkillsUserSkillEntity() {
		return skillsUserSkillEntity;
	}

	public void setSkillsUserSkillEntity(SkillsUserSkillEntity skillsUserSkillEntity) {
		this.skillsUserSkillEntity = skillsUserSkillEntity;
	}

	public boolean isEditObject() {
		return editObject;
	}

	public List<SkillsUserCertificateEntity> getUserCertificates() {
		return userCertificates;
	}

	public void setUserCertificates(List<SkillsUserCertificateEntity> userCertificates) {
		this.userCertificates = userCertificates;
	}

	public List<SkillsUserCertificateEntity> getSelectedUserCertificates() {
		return selectedUserCertificates;
	}

	public void setSelectedUserCertificates(List<SkillsUserCertificateEntity> selectedCertificates) {
		this.selectedUserCertificates = selectedCertificates;
	}

	public SkillsUserCertificateEntity getSkillsUserCertificateEntity() {
		return skillsUserCertificateEntity;
	}

	public void setSkillsUserCertificateEntity(SkillsUserCertificateEntity skillsUserCertificateEntity) {
		this.skillsUserCertificateEntity = skillsUserCertificateEntity;
	}

	public int getCertificateId() {
		return certificateId;
	}

	public void setCertificateId(int certificateId) {
		this.certificateId = certificateId;
	}

	public SkillsCertificateEntity getSkillsCertificateEntity() {
		return skillsCertificateEntity;
	}

	public void setSkillsCertificateEntity(SkillsCertificateEntity skillsCertificateEntity) {
		this.skillsCertificateEntity = skillsCertificateEntity;
	}

	public String getCertificateName() {
		return certificateName;
	}

	public void setCertificateName(String certificateName) {
		this.certificateName = certificateName;
	}

	public List<CloudSafeUploadFile> getSelectedFiles() {
		if (selectedFiles == null) {
			selectedFiles = new ArrayList<CloudSafeUploadFile>();
		}
		return selectedFiles;
	}

	public void setSelectedFiles(List<CloudSafeUploadFile> selectedFiles) {
		this.selectedFiles = selectedFiles;
	}

	public StreamedContent getDownloadFile() {
		return downloadFile;
	}

	public void setDownloadFile(StreamedContent downloadFile) {
		this.downloadFile = downloadFile;
	}

	public List<SkillsUserJobProfileEntity> getUserJobProfiles() {
		return userJobProfiles;
	}

	public void setUserJobProfiles(List<SkillsUserJobProfileEntity> userJobProfiles) {
		this.userJobProfiles = userJobProfiles;
	}

	public SkillsUserJobProfileEntity getSkillsUserJobProfileEntity() {
		return skillsUserJobProfileEntity;
	}

	public void setSkillsUserJobProfileEntity(SkillsUserJobProfileEntity skillsUserJobProfileEntity) {
		this.skillsUserJobProfileEntity = skillsUserJobProfileEntity;
	}

	public List<SkillsUserJobProfileEntity> getSelectedUserJobProfiles() {
		return selectedUserJobProfiles;
	}

	public void setSelectedUserJobProfiles(List<SkillsUserJobProfileEntity> selectedUserJobProfiles) {
		this.selectedUserJobProfiles = selectedUserJobProfiles;
	}

	public String getJobProfileName() {
		return jobProfileName;
	}

	public void setJobProfileName(String jobProfileName) {
		this.jobProfileName = jobProfileName;
	}

	public String getReportsToUserLoginId() {
		return reportsToUserLoginId;
	}

	public void setReportsToUserLoginId(String reportsToUserLoginId) {
		this.reportsToUserLoginId = reportsToUserLoginId;
	}

	public List<CloudSafeUploadFile> getDisplayedFiles() {
		return displayedFiles;
	}

	public void setDisplayedFiles(List<CloudSafeUploadFile> displayedFiles) {
		this.displayedFiles = displayedFiles;
	}

	public DcemUser getDcemUser() {
		return dcemUser;
	}

	public void setDcemUser(DcemUser dcemUser) {
		this.dcemUser = dcemUser;
		this.image = dcemUser.getPhoto();
	}

	public DcemUser getReportsToDcemUser() {
		return reportsToDcemUser;
	}

	public void setReportsToDcemUser(DcemUser reportsToDcemUser) {
		this.reportsToDcemUser = reportsToDcemUser;
		this.reportsToImage = dcemUser.getPhoto();
	}

}
