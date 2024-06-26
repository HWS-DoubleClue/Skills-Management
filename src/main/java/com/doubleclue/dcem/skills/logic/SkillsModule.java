package com.doubleclue.dcem.skills.logic;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.entities.TenantEntity;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.SupportedLanguage;
import com.doubleclue.dcem.core.jpa.DcemTransactional;
import com.doubleclue.dcem.core.logic.CreateTenant;
import com.doubleclue.dcem.core.logic.TemplateLogic;
import com.doubleclue.dcem.core.logic.UserLogic;
import com.doubleclue.dcem.core.logic.module.DcemModule;
import com.doubleclue.dcem.core.logic.module.ModulePreferences;

@ApplicationScoped
@Named("skillsModule")
public class SkillsModule extends DcemModule {

	@Inject
	CreateTenant createTenant;

	@Inject
	TemplateLogic templateLogic;

	@Inject
	SkillsEmailLogic skillsEmailLogic;
	
	@Inject
	SkillsUserLogic skillsUserLogic;
	
	@Inject
	UserLogic userLogic;

	private static final long serialVersionUID = 1L;

	public static final String MODULE_ID = "skills";
	public static final String MODULE_NAME = "Skills";
	public static final String RESOURCE_NAME = "com.doubleclue.dcem.skills.resources.Messages";
	public static final int MODULE_RANK = 100;

	public static final String TEMPLATES_PACKAGE = "com/doubleclue/dcem/skills/templates";

	@Override
	public String getResourceName() {
		return RESOURCE_NAME;
	}

	@Override
	public String getName() {
		return MODULE_NAME;
	}

	@Override
	public String getId() {
		return MODULE_ID;
	}

	@Override
	public int getRank() {
		return MODULE_RANK;
	}

	@Override
	public ModulePreferences getDefaultPreferences() {
		return new SkillsPreferences();
	}

	@Override
	public SkillsPreferences getModulePreferences() {
		return ((SkillsPreferences) super.getModulePreferences());
	}

	@Override
	public void initializeDb(DcemUser superAdmin) throws DcemException {
		createTenant.updateTemplates(TEMPLATES_PACKAGE);
	}

	@Override
	@DcemTransactional
	public void initializeTenant(TenantEntity tenantEntity) throws DcemException {
		String tenantName = tenantEntity.getName();
		logger.debug("Start initializeTenant " + tenantName);
		SkillsTenantData tenantData = new SkillsTenantData();
		super.initializeTenant(tenantEntity, tenantData);
		String[] templateNames = new String[] { SkillsConstants.SKILLS_NOTIFICATION_TEMPLATE };
		SupportedLanguage[] languages = new SupportedLanguage[] { SupportedLanguage.English, SupportedLanguage.German };
		templateLogic.getUpdateTemplateByName(getClass(), templateNames, languages, SkillsModule.TEMPLATES_PACKAGE);
	}

	@Override
	public void runNightlyTask() throws DcemException {
		skillsEmailLogic.sendExpirationEmail();
	}

	@Override
	@DcemTransactional
	public void deleteUserFromDbPre(DcemUser dcemUser) throws DcemException {
		skillsUserLogic.deleteSkillsUser(dcemUser);
	}
	
	public SkillsTenantData getSkillsTenantData() {
		return (SkillsTenantData) getModuleTenantData();
	}
	
	public void preferencesValidation(ModulePreferences modulePreferences) throws DcemException {
	}

}
