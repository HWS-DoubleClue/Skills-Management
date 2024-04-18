package com.doubleclue.dcem.skills.logic;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.doubleclue.dcem.core.gui.DcemGui;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.logic.module.ModulePreferences;

@SuppressWarnings("serial")
@XmlType
@XmlRootElement(name = "skillsPreferences")
public class SkillsPreferences extends ModulePreferences {

	@DcemGui(style = "width: 30em")
	private String manualsLink = "https://doubleclue.com/files/DC_Skills_Manual_";

	@DcemGui(separator = "General")
	private boolean disableEmailNotifications;
	
	@DcemGui
	private boolean notificationForHod = true;

	@DcemGui(style = "width: 50em")
	private String recipientsForRequestSkills;

	@DcemGui
	private boolean requestedSkillAndCertificateImmediateAvailable;

	@DcemGui
	private int firstNotificationForExpiringCertificate;

	@DcemGui
	private int secondNotificationForExpiringCertificate;

	public String getManualsLink() {
		return manualsLink;
	}

	public void setManualsLink(String manualsLink) {
		this.manualsLink = manualsLink;
	}

	public boolean isNotificationForHod() {
		return notificationForHod;
	}

	public void setNotificationForHod(boolean notificationForHoD) {
		this.notificationForHod = notificationForHoD;
	}

	public String getRecipientsForRequestSkills() {
		return recipientsForRequestSkills;
	}

	public void setRecipientsForRequestSkills(String recipientsForRequestSkills) {
		this.recipientsForRequestSkills = recipientsForRequestSkills;
	}

	public int getFirstNotificationForExpiringCertificate() {
		return firstNotificationForExpiringCertificate;
	}

	public void setFirstNotificationForExpiringCertificate(int firstNotificationForExpiringCertificate) throws Exception {
		if (firstNotificationForExpiringCertificate < 0) {
			JsfUtils.addErrorMessage(SkillsModule.RESOURCE_NAME, "preferences.invalidExpiringReminder");
			throw new Exception();
		}
		this.firstNotificationForExpiringCertificate = firstNotificationForExpiringCertificate;
	}

	public int getSecondNotificationForExpiringCertificate() {
		return secondNotificationForExpiringCertificate;
	}

	public void setSecondNotificationForExpiringCertificate(int secondNotificationForExpiringCertificate) throws Exception {
		if (secondNotificationForExpiringCertificate < 0) {
			JsfUtils.addErrorMessage(SkillsModule.RESOURCE_NAME, "preferences.invalidExpiringReminder");
			throw new Exception();
		}
		if (secondNotificationForExpiringCertificate > firstNotificationForExpiringCertificate) {
			this.secondNotificationForExpiringCertificate = firstNotificationForExpiringCertificate;
			this.firstNotificationForExpiringCertificate = secondNotificationForExpiringCertificate;
		} else {
			this.secondNotificationForExpiringCertificate = secondNotificationForExpiringCertificate;
		}
	}

	public boolean isRequestedSkillAndCertificateImmediateAvailable() {
		return requestedSkillAndCertificateImmediateAvailable;
	}

	public void setRequestedSkillAndCertificateImmediateAvailable(boolean requestedSkillAndCertificateImmediateAvailable) {
		this.requestedSkillAndCertificateImmediateAvailable = requestedSkillAndCertificateImmediateAvailable;
	}

	public boolean isDisableEmailNotifications() {
		return disableEmailNotifications;
	}

	public void setDisableEmailNotifications(boolean disableEmailNotifications) {
		this.disableEmailNotifications = disableEmailNotifications;
	}

}
