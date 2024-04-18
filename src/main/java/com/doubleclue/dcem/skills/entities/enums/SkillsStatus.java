package com.doubleclue.dcem.skills.entities.enums;

import java.util.Locale;
import java.util.ResourceBundle;

import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.skills.logic.SkillsModule;

public enum SkillsStatus {
	OWNS(""), SELF_INITIATED(" \u1330"), RECOMMENDED (" \u2690"), REQUIRED (" \u2691");

	String icon;
	
	
	private SkillsStatus(String icon) {
		this.icon = icon;
	}

	public String getLocaleText() {
		return JsfUtils.getStringSafely(SkillsModule.RESOURCE_NAME, this.getClass().getSimpleName() + "." + this.name()) + icon;
	}

	public String getLocaleText(String locale) {
		try {
			ResourceBundle resourceBundle = ResourceBundle.getBundle(SkillsModule.RESOURCE_NAME, new Locale(locale));
			return resourceBundle.getString(this.getClass().getSimpleName() + "." + this.name()) + icon;
		} catch (Exception e) {
			return "???" + this.name();
		}
	}

	public String getIcon() {
		return icon;
	}

}
