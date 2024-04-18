package com.doubleclue.dcem.skills.entities.enums;

import java.util.Locale;
import java.util.ResourceBundle;

import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.skills.logic.SkillsModule;

public enum ApprovalStatus {
	APPROVED, PENDING;
	
	public String getLocaleText() {
		return JsfUtils.getStringSafely(SkillsModule.RESOURCE_NAME, this.getClass().getSimpleName() + "." + this.name());
	}
	
	public String getLocaleText(String locale) {
		try {
			ResourceBundle resourceBundle = ResourceBundle.getBundle(SkillsModule.RESOURCE_NAME, new Locale(locale));
			return resourceBundle.getString(this.getClass().getSimpleName() + "." + this.name());
		} catch (Exception e) {
			return "???" + this.name();
		}
	}
}
