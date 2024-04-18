package com.doubleclue.dcem.skills.utils;

import java.time.LocalDate;

import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.skills.entities.SkillsUserEntity;
import com.doubleclue.dcem.skills.entities.enums.AvailabilityStatus;
import com.doubleclue.dcem.skills.entities.enums.SkillsLevel;
import com.doubleclue.dcem.skills.logic.SkillsConstants;
import com.doubleclue.dcem.skills.logic.SkillsModule;

public class SkillsUtils {

	public static String convertLevelToStars(SkillsLevel skillsLevel) {
		String stars = "";
		for (int i = 0; i < skillsLevel.ordinal(); i++) {
			stars += "\u2B50";
			// stars += "\u2606";
			// stars += "\u2605";
		}
		return stars;
	}

	// public static String convertLevelToStarsWithEmptyStars(SkillsLevel skillsLevel) {
	// String stars = "";
	// for (int i = 0; i < 5; i++) {
	// if (i < skillsLevel.ordinal()) {
	// stars += "\u2605";
	// } else {
	// stars += "\u2606";
	// }
	// // stars += "\u2605";
	// // stars += "\u2B50";
	// }
	//
	// return stars;
	// }

	public static String getUserAvailability(SkillsUserEntity skillsUserEntity, boolean asIcons) {
		if (skillsUserEntity == null) {
			return "";
		}
		if (skillsUserEntity.getAvailability() == AvailabilityStatus.AvailableFrom && skillsUserEntity.getAvailableFrom() == null) {
			return "?";
		}
		if (skillsUserEntity.getAvailability() == AvailabilityStatus.NotAvailable) {
			if (asIcons) {
				return "\u274c";
			} else {
				return AvailabilityStatus.NotAvailable.getLocaleText();
			}
		}
		if (skillsUserEntity.getAvailability() == AvailabilityStatus.Available || (skillsUserEntity.getAvailability() == AvailabilityStatus.AvailableFrom
				&& skillsUserEntity.getAvailableFrom().isBefore(LocalDate.now().plusDays(1)))) {
			if (asIcons) {
			return "\u2705";
			} else {
				return AvailabilityStatus.Available.getLocaleText();
			}
		}
		return JsfUtils.getStringSafely(SkillsModule.RESOURCE_NAME, "Skills.availability") + " "
				+ JsfUtils.getLocalDateFormat(skillsUserEntity.getAvailableFrom());
	}

	public static boolean isValidName(String name) {
		if (name == null) {
			return false;
		}
		for (int i = 0; i < name.length(); i++) {
			if (SkillsConstants.SPECIAL_CHARACTERS.contains(name.subSequence(i, i + 1))) {
				return false;
			}
		}
		return true;
	}

}
