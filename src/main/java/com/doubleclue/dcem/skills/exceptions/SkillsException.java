package com.doubleclue.dcem.skills.exceptions;

import java.util.ResourceBundle;

import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.skills.logic.SkillsModule;

public class SkillsException extends DcemException {

	private static final long serialVersionUID = 1L;

	private SkillsErrorCodes skillsErrorCode;

	public String getLocalizedMessageWithMessage() {
		try {
			ResourceBundle bundle = ResourceBundle.getBundle(SkillsModule.RESOURCE_NAME, JsfUtils.getLocale());
			String error = JsfUtils.getMessageFromBundle(bundle, SkillsErrorCodes.class.getSimpleName() + "." + skillsErrorCode.name(), super.getMessage());
			if (error.startsWith("???")) {
				return this.toString();
			}
			return error;
		} catch (Exception e) {
			return this.toString();
		}
	}

	public String getLocalizedMessage() {
		try {
			ResourceBundle bundle = ResourceBundle.getBundle(SkillsModule.RESOURCE_NAME, JsfUtils.getLocale());
			String error = JsfUtils.getStringSafely(bundle, SkillsErrorCodes.class.getSimpleName() + "." + skillsErrorCode.name());
			if (error.startsWith("???")) {
				return this.toString();
			}
			return error;
		} catch (Exception e) {
			return this.toString();
		}
	}

	public SkillsException(SkillsErrorCodes skillsErrorCode, String message, Throwable cause) {
		super(null, message, cause);
		this.skillsErrorCode = skillsErrorCode;
	}

	public SkillsException(SkillsErrorCodes skillsErrorCode, String message) {
		super(null, message, null);
		this.skillsErrorCode = skillsErrorCode;
	}

	public SkillsErrorCodes getSkillsErrorCode() {
		return skillsErrorCode;
	}

	public void setSkillsErrorCode(SkillsErrorCodes skillsErrorCode) {
		this.skillsErrorCode = skillsErrorCode;
	}

	public Throwable getExceptionCause() {
		return super.getCause();
	}

	public String getMessage() {
		return super.getMessage();
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(skillsErrorCode.name() + " - ");
		if (getMessage() != null) {
			sb.append(getMessage());
		}
		if (getCause() != null) {
			sb.append(" - ");
			sb.append(getCause().toString());
		}
		return sb.toString();
	}

}
