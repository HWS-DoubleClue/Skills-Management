package com.doubleclue.dcem.skills.subjects;

import javax.enterprise.context.ApplicationScoped;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.SubjectAbs;
import com.doubleclue.dcem.core.logic.ActionSelection;
import com.doubleclue.dcem.core.logic.RawAction;
import com.doubleclue.dcem.skills.logic.SkillsConstants;
import com.doubleclue.dcem.skills.logic.SkillsModule;

@SuppressWarnings("serial")
@ApplicationScoped
public class SkillsDashboardSubject extends SubjectAbs {

	public SkillsDashboardSubject() {
		RawAction rawActionEditSkillTargets = new RawAction(SkillsConstants.ACTION_EDIT_MYSKILLS,
				new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN, DcemConstants.SYSTEM_ROLE_USER }, ActionSelection.IGNORE);
		RawAction rawActionImportSkills = new RawAction(SkillsConstants.SKILLS_IMPORT,
				new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN, DcemConstants.SYSTEM_ROLE_USER });
		rawActions.add(rawActionImportSkills);
		rawActions.add(rawActionEditSkillTargets);
		rawActions.add(new RawAction(SkillsConstants.SKILLS_USER_SEARCH, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN },
				ActionSelection.IGNORE));

		rawActions.add(new RawAction(DcemConstants.ACTION_VIEW, new String[] { DcemConstants.SYSTEM_ROLE_USER }));
		rawActions.add(new RawAction(DcemConstants.ACTION_MANAGE, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN }));
	};

	@Override
	public String getModuleId() {
		return SkillsModule.MODULE_ID;
	}

	@Override
	public int getRank() {
		return 1;
	}

	@Override
	public String getIconName() {
		return "fa fa-desktop";
	}

	@Override
	public String getPath() {
		return SkillsConstants.SKILLS_DASHBOARD_PATH;
	}

	@Override
	public Class<?> getKlass() {
		return null;
	}

}
