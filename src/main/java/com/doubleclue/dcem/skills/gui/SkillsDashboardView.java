package com.doubleclue.dcem.skills.gui;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.Tuple;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.PrimeFaces;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.gui.AutoViewAction;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.doubleclue.dcem.skills.entities.SkillsJobProfileEntity;
import com.doubleclue.dcem.skills.entities.SkillsUserCertificateEntity;
import com.doubleclue.dcem.skills.entities.SkillsUserEntity;
import com.doubleclue.dcem.skills.entities.SkillsUserJobProfileEntity;
import com.doubleclue.dcem.skills.entities.SkillsUserSkillEntity;
import com.doubleclue.dcem.skills.entities.enums.AvailabilityStatus;
import com.doubleclue.dcem.skills.entities.enums.LogicalConjunction;
import com.doubleclue.dcem.skills.entities.enums.SkillsLevel;
import com.doubleclue.dcem.skills.entities.enums.SkillsStatus;
import com.doubleclue.dcem.skills.logic.SkillsCertificateLogic;
import com.doubleclue.dcem.skills.logic.SkillsConstants;
import com.doubleclue.dcem.skills.logic.SkillsJobProfileEntityLogic;
import com.doubleclue.dcem.skills.logic.SkillsModule;
import com.doubleclue.dcem.skills.logic.SkillsUserLogic;
import com.doubleclue.dcem.skills.subjects.SkillsDashboardSubject;
import com.doubleclue.dcem.skills.utils.SkillsUtils;

@Named("skillsDashboardView")
@SessionScoped
public class SkillsDashboardView extends DcemView implements Serializable {

	private static final long serialVersionUID = 1L;

	private Logger logger = LogManager.getLogger(SkillsDashboardView.class);

	@Inject
	OperatorSessionBean operatorSessionBean;

	@Inject
	SkillsDashboardSubject skillsDashboardSubject;

	@Inject
	SkillsUserLogic skillsUserLogic;

	@Inject
	SkillsJobProfileEntityLogic skillsJobProfileEntityLogic;

	@Inject
	SkillsCertificateLogic skillsCertificateLogic;

	@Inject
	SkillsUserDialog skillsUserDialog;

	@Inject
	SkillsImportDialog skillsImportDialog;

	private ResourceBundle resourceBundle;

	private String skillSearchName;
	private int selectedLevelFilter;
	private int searchJobProfileMatch;
	private boolean skillsFilter;
	private boolean certificateFilter;
	private boolean jobProfileFilter;
	private boolean skillsOnlyOwnedFilter;

	private List<SkillSearchUser> skillSearchUsers;

	private SkillsUserEntity skillsUserEntity;
	private SkillsUserEntity selectedUser = null;
	private boolean searchRights;

	private List<SkillsUserSkillEntity> userSkills;
	private List<SkillsUserJobProfileEntity> userJobProfiles;
	private List<SkillsUserCertificateEntity> userCertificates;

	@PostConstruct
	public void init() {
		subject = skillsDashboardSubject;
		resourceBundle = JsfUtils.getBundle(SkillsModule.RESOURCE_NAME, operatorSessionBean.getLocale());

		certificateFilter = parseBooleanUserSetting(SkillsConstants.CERTIFICATE_FILTER, true);
		skillsFilter = parseBooleanUserSetting(SkillsConstants.SKILLS_FILTER, true);
		jobProfileFilter = parseBooleanUserSetting(SkillsConstants.JOB_PROFILE_FILTER, true);
		selectedLevelFilter = parseIntUserSetting(SkillsConstants.SKILLS_LEVEL_FILTER, 0);
		searchJobProfileMatch = parseIntUserSetting(SkillsConstants.JOB_PROFILE_MATCH, 0);
		skillsOnlyOwnedFilter = parseBooleanUserSetting(SkillsConstants.SKILLS_FILTER_ONLY_OWNED, true);

		AutoViewAction skillsSearchAction = createAutoViewAction(SkillsConstants.SKILLS_USER_SEARCH, resourceBundle, null, null, null);
		searchRights = skillsSearchAction == null ? false : true;
		addAutoViewAction(SkillsConstants.ACTION_EDIT_MYSKILLS, resourceBundle, skillsUserDialog, SkillsConstants.SKILLS_USER_DIALOG_PATH);
		addAutoViewAction(SkillsConstants.SKILLS_IMPORT, resourceBundle, skillsImportDialog, SkillsConstants.SKILLS_USER_JOBPROFILE_IMPORT_SKILLS_PATH);
	}

	@Override
	public void reload() {
		skillsUserDialog.setParentView(this);
		skillsImportDialog.setParentView(this);
		try {
			skillsUserEntity = skillsUserLogic.retrieveSkillsUserByDcemUser(operatorSessionBean.getDcemUser());
			skillsUserEntity.getPhoto(); // Avoiding Lazy initialization
			if (searchRights == false) {
				selectedUser = skillsUserEntity;
			}
			if (selectedUser != null) {
				selectedUser = skillsUserLogic.getSkillsUserById(selectedUser.getId()); // Does selected User still exist?
			}
			if (selectedUser == null) {
				userSkills = new ArrayList<SkillsUserSkillEntity>();
				userJobProfiles = new ArrayList<SkillsUserJobProfileEntity>();
				userCertificates = new ArrayList<SkillsUserCertificateEntity>();
			} else {
				userSkills = skillsUserLogic.getSkillsOfUser(selectedUser.getId());
				userJobProfiles = skillsJobProfileEntityLogic.getJobProfilesOfUser(selectedUser.getId());
				userCertificates = skillsCertificateLogic.getAllCertificatesFromUser(selectedUser);
			}
		} catch (Exception e) {
			JsfUtils.addErrorMessage(resourceBundle, "skillsDashboardView.error.reloadUser");
			logger.error("", e);
		}
	}

	@Override
	public void leavingView() {
		skillSearchName = null;
		skillSearchUsers = null;
		selectedUser = null;
		// skillsUserEntity = null; // TODO: should be null?
		// userSkills = null;
		// userJobProfiles = null;
		// userCertificates = null;
	}

	public void actionEditSkills() {
		setActionObject(skillsUserEntity);
		AutoViewAction autoViewAction = this.getAutoViewAction(SkillsConstants.ACTION_EDIT_MYSKILLS);
		if (autoViewAction != null) {
			viewNavigator.setActiveDialog(autoViewAction);
		}
	}

	public void actionImportJobProfile(SkillsUserJobProfileEntity skillsUserJobProfileEntity) {
		try {
			SkillsJobProfileEntity skillsJobProfileEntity = skillsJobProfileEntityLogic
					.getJobProfileByName(skillsUserJobProfileEntity.getJobProfile().getName());
			setActionObject(skillsJobProfileEntity);
			skillsImportDialog.setSkillsUserEntity(skillsUserJobProfileEntity.getSkillsUserEntity());
			AutoViewAction autoViewAction = this.getAutoViewAction(SkillsConstants.SKILLS_IMPORT);
			if (autoViewAction != null) {
				viewNavigator.setActiveDialog(autoViewAction);
			}
		} catch (Exception e) {
			logger.error("Could not open ImportJobProfileDialog", e);
			JsfUtils.addErrorMessage(resourceBundle, "error.global");
		}
	}

	public boolean isEditSkillsPermitted() {
		AutoViewAction autoViewAction = this.getAutoViewAction(SkillsConstants.ACTION_EDIT_MYSKILLS);
		if (autoViewAction != null) {
			return true;
		}
		return false;
	}

	public void actionSearch() {
		if (skillSearchName == null || skillSearchName.trim().isEmpty()) {
			skillSearchUsers = new ArrayList<SkillSearchUser>();
			if (selectedUser != null && selectedUser.equals(skillsUserEntity) == false) {
				selectedUser = null; // only clear user on empty search if it is not the operator
			}
			return;
		}
		skillSearchName = skillSearchName.trim();
		boolean simpleSearch = true;
		if (skillSearchName.contains(SkillsConstants.SEARCH_AND_SEPERATOR) && skillSearchName.contains(SkillsConstants.SEARCH_OR_SEPERATOR)) {
			JsfUtils.addErrorMessage(resourceBundle, "skillsDashboard.invalid.searchterm");
			return;
		} else if (skillSearchName.contains(SkillsConstants.SEARCH_AND_SEPERATOR) || skillSearchName.contains(SkillsConstants.SEARCH_OR_SEPERATOR)) {
			simpleSearch = false;
		}

		// Improvement: change SkillSearchUser to have List<String> for skills/cert/profiles to remove hashmaps
		skillSearchUsers = new ArrayList<SkillSearchUser>();
		selectedUser = null;
		HashSet<SkillsUserEntity> allFoundSkillsUser = new HashSet<SkillsUserEntity>();
		HashMap<SkillsUserEntity, List<String>> certificates = new HashMap<SkillsUserEntity, List<String>>();
		HashMap<SkillsUserEntity, List<SkillsUserSkillEntity>> skills = new HashMap<SkillsUserEntity, List<SkillsUserSkillEntity>>();
		HashMap<SkillsUserEntity, List<String>> jobProfiles = new HashMap<SkillsUserEntity, List<String>>();

		try {
			if (certificateFilter && simpleSearch) {
				List<Tuple> certificateTuples = skillsUserLogic.getUserAndCertificatesForSkillSearch(skillSearchName);
				for (Tuple tuple : certificateTuples) {
					List<String> certificateList = certificates.getOrDefault(tuple.get("user"), new ArrayList<String>());
					certificateList.add((String) tuple.get("certificate"));
					certificates.put((SkillsUserEntity) tuple.get("user"), certificateList);
					allFoundSkillsUser.add((SkillsUserEntity) tuple.get("user"));
				}
			}
			if (skillsFilter) {
				LogicalConjunction logicalConjunction = skillSearchName.contains(SkillsConstants.SEARCH_OR_SEPERATOR) ? LogicalConjunction.OR
						: LogicalConjunction.AND;
				List<String> skillsNameList = new ArrayList<String>();
				if (simpleSearch == false) {
					String[] skillsNames = skillSearchName.split("[|&]");
					for (String skillsName : skillsNames) {
						if (skillsName != null && skillsName.trim().isEmpty() == false) {
							skillsNameList.add(skillsName.trim().toLowerCase());
						}
					}
				} else {
					skillsNameList.add(skillSearchName);
				}
				List<Tuple> skillsTuples = skillsUserLogic.getUserAndSkillsForSkillSearch(skillsNameList, selectedLevelFilter, logicalConjunction,
						skillsOnlyOwnedFilter);
				List<SkillsUserSkillEntity> skillList = new ArrayList<SkillsUserSkillEntity>();
				for (Tuple tuple : skillsTuples) {
					skillList = skills.getOrDefault(tuple.get("user"), new ArrayList<SkillsUserSkillEntity>());
					skillList.add((SkillsUserSkillEntity) tuple.get("userSkill"));
					skills.put((SkillsUserEntity) tuple.get("user"), skillList);
					allFoundSkillsUser.add((SkillsUserEntity) tuple.get("user"));
				}
				Collections.sort(skillList, Comparator.<SkillsUserSkillEntity, Integer> comparing(s -> s.getLevel().ordinal()).reversed());
			}
			if (jobProfileFilter && simpleSearch) {
				List<Tuple> jobProfileTuples = skillsUserLogic.getJobProfilesForSkillSearch(skillSearchName, searchJobProfileMatch);
				for (Tuple tuple : jobProfileTuples) {
					List<String> jobProfileList = jobProfiles.getOrDefault(tuple.get("user"), new ArrayList<String>());
					jobProfileList.add((String) tuple.get("jobProfileName") + ": " + ((Integer) tuple.get("match")).toString() + "%");
					jobProfiles.put((SkillsUserEntity) tuple.get("user"), jobProfileList);
					allFoundSkillsUser.add((SkillsUserEntity) tuple.get("user"));
				}
			}

			for (SkillsUserEntity skillsUser : allFoundSkillsUser) {
				List<String> certificateList = certificates.getOrDefault(skillsUser, new ArrayList<>());
				List<SkillsUserSkillEntity> skillList = skills.getOrDefault(skillsUser, new ArrayList<>());
				List<String> jobProfileList = jobProfiles.getOrDefault(skillsUser, new ArrayList<>());
				SkillSearchUser skillSearchUser = new SkillSearchUser(skillsUser, skillList,
						certificateList.toString().substring(1, certificateList.toString().length() - 1),
						jobProfileList.toString().substring(1, jobProfileList.toString().length() - 1));
				skillSearchUsers.add(skillSearchUser);
			}
		} catch (Exception e) {
			logger.error("", e);
			JsfUtils.addErrorMessage(resourceBundle, "error.global");
		}
	}

	public String getStyledSkillsResult(List<SkillsUserSkillEntity> skillsList) {
		List<String> skillList = new ArrayList<String>();
		for (SkillsUserSkillEntity skillsUserSkillEntity : skillsList) {
			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append("<b>" + skillsUserSkillEntity.getSkill().getName() + "</b> ");
			stringBuilder.append(SkillsUtils.convertLevelToStars(skillsUserSkillEntity.getLevel()));
			if (skillsUserSkillEntity.getStatus().equals(SkillsStatus.OWNS) == false) {
				stringBuilder.append(" <i class=\"fa-regular fa-flag\"/></i> ");
			}
			stringBuilder.append("<br />");
			skillList.add(stringBuilder.toString());
		}
		return String.join(" ", skillList);

	}

	public boolean actionFilterByAvailableDate(Object value, Object filter, Locale locale) {
		LocalDate filterDate;
		if (filter == null) {
			filterDate = LocalDate.now();
		}
		filterDate = (LocalDate) filter;
		SkillsUserEntity skillsUser = (SkillsUserEntity) value;
		if (skillsUser.getAvailability() == AvailabilityStatus.Available) {
			return true;
		}
		if (skillsUser.getAvailability() == AvailabilityStatus.AvailableFrom && skillsUser.getAvailableFrom() != null
				&& skillsUser.getAvailableFrom().isBefore(filterDate.plusDays(1))) {
			return true;
		}
		return false;
	}

	public void actionSaveFilterToBrowser() {
		try {
			operatorSessionBean.setLocalStorageUserSetting(SkillsConstants.SKILLS_FILTER, String.valueOf(skillsFilter));
			operatorSessionBean.setLocalStorageUserSetting(SkillsConstants.SKILLS_LEVEL_FILTER, String.valueOf(selectedLevelFilter));
			operatorSessionBean.setLocalStorageUserSetting(SkillsConstants.CERTIFICATE_FILTER, String.valueOf(certificateFilter));
			operatorSessionBean.setLocalStorageUserSetting(SkillsConstants.JOB_PROFILE_FILTER, String.valueOf(jobProfileFilter));
			operatorSessionBean.setLocalStorageUserSetting(SkillsConstants.JOB_PROFILE_MATCH, String.valueOf(searchJobProfileMatch));
			operatorSessionBean.setLocalStorageUserSetting(SkillsConstants.SKILLS_FILTER, String.valueOf(skillsOnlyOwnedFilter));
		} catch (Exception e) {
			logger.error("Unexpected Error", e);
			JsfUtils.addErrorMessage(resourceBundle, "error.global");
		}

	}

	public StreamedContent getSelectedUserPhoto() {
		if (selectedUser == null) {
			return null;
		}
		return getUserPhoto(selectedUser.getDcemUser());
	}

	public StreamedContent getUserPhoto(DcemUser dcemUser) {
		if (dcemUser == null) {
			return null;
		}
		byte[] image = dcemUser.getPhoto();
		if (image != null) {
			InputStream in = new ByteArrayInputStream(image);
			return DefaultStreamedContent.builder().contentType("image/png").stream(() -> in).build();
		} else {
			return JsfUtils.getDefaultUserImage();
		}
	}

	public List<SelectItem> getSkillsLevel() {
		List<SelectItem> list = new ArrayList<SelectItem>();
		for (SkillsLevel skillsLevel : SkillsLevel.values()) {
			if (skillsLevel.ordinal() == 0) {
				continue;
			}
			list.add(new SelectItem(skillsLevel.ordinal(), JsfUtils.getStringSafely(resourceBundle, skillsLevel.name())));
		}
		return list;
	}

	private boolean parseBooleanUserSetting(String settingKey, boolean defaultValue) {
		String settingValue = operatorSessionBean.getLocalStorageUserSetting(settingKey);
		return settingValue != null ? Boolean.parseBoolean(settingValue) : defaultValue;
	}

	private int parseIntUserSetting(String settingKey, int defaultValue) {
		String settingValue = operatorSessionBean.getLocalStorageUserSetting(settingKey);
		return settingValue != null ? Integer.parseInt(settingValue) : defaultValue;
	}

	public void actionShowUserSkills(SkillSearchUser skillSearchUser) {
		selectedUser = skillSearchUser.getSkillsUserEntity();
		reload();
		PrimeFaces.current().executeScript("PF('skillsPropertiesPanel').show()");
	}

	public String userStyle(SkillSearchUser skillSearchUser) {
		if (selectedUser != null && selectedUser.getId() == skillSearchUser.getSkillsUserEntity().getId()) {
			return "font-weight: bold";
		}
		return "";
	}

	public void onCloseSkillPanel() {
		selectedUser = null;
		reload();
	}

	public void actionShowMySkills() {
		selectedUser = skillsUserEntity;
		reload();
		PrimeFaces.current().executeScript("PF('skillsPropertiesPanel').show()");
	}

	public String getUserSkillName() {
		if (selectedUser != null) {
			return selectedUser.getDcemUser().getDisplayName();
		}
		return null;
	}

	// public int getTruncateLength(String skillSearchResult) {
	// String substringToKeep1 = "\u2B50";
	// String substringToKeep2 = "> <";
	//
	// Pattern pattern = Pattern.compile(substringToKeep1 + "|" + substringToKeep2);
	// Matcher matcher = pattern.matcher(skillSearchResult);
	// StringBuilder resultStringBuilder = new StringBuilder();
	//
	// while (matcher.find()) {
	// resultStringBuilder.append(matcher.group());
	// }
	// String resultString = resultStringBuilder.toString();
	// if (resultString.contains(substringToKeep2)) {
	// String[] splitString = resultString.split(substringToKeep2);
	// String maxString = splitString[0];
	// for (String stringPart : splitString) {
	// if (stringPart.compareTo(maxString) > 0) {
	// maxString = stringPart;
	// }
	// }
	// resultString = maxString;
	// }
	// return resultString.length();
	// }

	public int getTruncateLength(List<SkillsUserSkillEntity> skillList) {
		if (skillList.isEmpty()) {
			return 0;
		}
		Integer biggestStarCount = skillList.get(0).getLevel().ordinal();
		for (SkillsUserSkillEntity skillsUserSkillEntity : skillList) {
			if (biggestStarCount.compareTo(skillsUserSkillEntity.getLevel().ordinal()) > 0) {
				biggestStarCount = skillsUserSkillEntity.getLevel().ordinal();
			}
		}
		return biggestStarCount;
	}

	// public int getTruncateLength(String skillSearchResult) {
	// String[] substrings = skillSearchResult.split("> <|\u2B50");
	// String maxSubstring = Arrays.stream(substrings).max(Comparator.comparingInt(String::length)).orElse("");
	// return maxSubstring.length();
	// }

	public String getAvailability(SkillsUserEntity skillsUserEntity) {
		return SkillsUtils.getUserAvailability(skillsUserEntity, true);
	}

	public String getAvailabilityAsText(SkillsUserEntity skillsUserEntity) {
		return SkillsUtils.getUserAvailability(skillsUserEntity, false);
	}

	public SkillsUserEntity getSkillsUserEntity() {
		return skillsUserEntity;
	}

	public void setSkillsUserEntity(SkillsUserEntity skillsUserEntity) {
		this.skillsUserEntity = skillsUserEntity;
	}

	public List<SkillsUserSkillEntity> getSkillsTargets() {
		return userSkills;
	}

	public void setSkillsTargets(List<SkillsUserSkillEntity> skillsTargets) {
		this.userSkills = skillsTargets;
	}

	public List<SkillsUserSkillEntity> getUserSkills() {
		return userSkills;
	}

	public void setUserSkills(List<SkillsUserSkillEntity> userSkills) {
		this.userSkills = userSkills;
	}

	public List<SkillsUserJobProfileEntity> getUserJobProfiles() {
		return userJobProfiles;
	}

	public void setUserJobProfiles(List<SkillsUserJobProfileEntity> userJobProfiles) {
		this.userJobProfiles = userJobProfiles;
	}

	public List<SkillsUserCertificateEntity> getUserCertificates() {
		return userCertificates;
	}

	public void setUserCertificates(List<SkillsUserCertificateEntity> userCertificates) {
		this.userCertificates = userCertificates;
	}

	public String getSkillSearchName() {
		return skillSearchName;
	}

	public void setSkillSearchName(String skillSearchName) {
		this.skillSearchName = skillSearchName;
	}

	public List<SkillSearchUser> getSearchSkillUser() {
		return skillSearchUsers;
	}

	public void setSearchSkillUser(List<SkillSearchUser> searchSkillUser) {
		this.skillSearchUsers = searchSkillUser;
	}

	public boolean isCertificateFilter() {
		return certificateFilter;
	}

	public void setCertificateFilter(boolean certificateFilter) {
		this.certificateFilter = certificateFilter;
	}

	public boolean isSkillsFilter() {
		return skillsFilter;
	}

	public void setSkillsFilter(boolean skillsFilter) {
		this.skillsFilter = skillsFilter;
	}

	public int getSelectedLevelFilter() {
		return selectedLevelFilter;
	}

	public void setSelectedLevelFilter(int selectedLevelFilter) {
		this.selectedLevelFilter = selectedLevelFilter;
	}

	public int getSearchJobProfileMatch() {
		return searchJobProfileMatch;
	}

	public void setSearchJobProfileMatch(int searchJobProfileMatch) {
		this.searchJobProfileMatch = searchJobProfileMatch;
	}

	public boolean isJobProfileFilter() {
		return jobProfileFilter;
	}

	public void setJobProfileFilter(boolean jobProfileFilter) {
		this.jobProfileFilter = jobProfileFilter;
	}

	public boolean isSearchRights() {
		return searchRights;
	}

	public void setSearchRights(boolean searchRights) {
		this.searchRights = searchRights;
	}

	public SkillsUserEntity getSelectedUser() {
		return selectedUser;
	}

	public void setSelectedUser(SkillsUserEntity selectedUser) {
		this.selectedUser = selectedUser;
	}

	public boolean isSkillsOnlyOwnedFilter() {
		return skillsOnlyOwnedFilter;
	}

	public void setSkillsOnlyOwnedFilter(boolean skillsOnlyOwnedFilter) {
		this.skillsOnlyOwnedFilter = skillsOnlyOwnedFilter;
	}
}
