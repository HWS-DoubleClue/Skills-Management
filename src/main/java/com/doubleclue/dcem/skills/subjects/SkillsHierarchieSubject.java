package com.doubleclue.dcem.skills.subjects;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.SubjectAbs;
import com.doubleclue.dcem.core.logic.ActionSelection;
import com.doubleclue.dcem.core.logic.ActionType;
import com.doubleclue.dcem.core.logic.RawAction;
import com.doubleclue.dcem.skills.logic.SkillsConstants;
import com.doubleclue.dcem.skills.logic.SkillsModule;

public class SkillsHierarchieSubject extends SubjectAbs {

	private static final long serialVersionUID = 1L;

	public SkillsHierarchieSubject() {
		rawActions.add(new RawAction(DcemConstants.ACTION_ADD, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN },
				ActionSelection.CREATE_OBJECT));
		rawActions.add(new RawAction(DcemConstants.ACTION_EDIT, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN },
				ActionSelection.ONE_ONLY));

		rawActions.add(new RawAction(DcemConstants.ACTION_DELETE, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN },
				ActionSelection.ONE_OR_MORE));
		RawAction rawAction = new RawAction(SkillsConstants.REQUEST_SKILLS,
				new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN, DcemConstants.SYSTEM_ROLE_USER });
		rawAction.setIcon("fa fa-plus");
		rawActions.add(rawAction);
		
		RawAction rawActionTableView = new RawAction(SkillsConstants.SHOW_SKILL_TABLE,
				new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN, DcemConstants.SYSTEM_ROLE_USER }, ActionSelection.IGNORE);
		rawActionTableView.setIcon("fa fa-solid fa-table-list");
		rawActionTableView.setActionType(ActionType.VIEW_LINK);
		rawActions.add(rawActionTableView);
		
		RawAction rawActionApprove = new RawAction(SkillsConstants.APPROVE_SKILL, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN },
				ActionSelection.ONE_OR_MORE);
		rawActionApprove.setIcon("fa fa-check");
		rawActionApprove.setActionType(ActionType.EL_METHOD);
		rawActionApprove.setElMethodExpression("#{skillsHierarchieView.actionApproveSkills()}");
		rawActions.add(rawActionApprove);
		RawAction rawActionSearchSkill = new RawAction(SkillsConstants.HIERARCHY_SEARCH_USER_WITH_SKILL,
				new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN, DcemConstants.SYSTEM_ROLE_USER }, ActionSelection.ONE_ONLY);
		rawActionSearchSkill.setActionType(ActionType.EL_METHOD);
		rawActionSearchSkill.setElMethodExpression("#{skillsHierarchieView.actionSearchUserWithSkillsInDashboard()}");
		rawActions.add(rawActionSearchSkill);
		
		rawActions.add(new RawAction(DcemConstants.ACTION_VIEW, new String[] { DcemConstants.SYSTEM_ROLE_USER }));
		rawActions.add(new RawAction(DcemConstants.ACTION_MANAGE, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN }));
		
		RawAction rawActionMySkill = new RawAction(SkillsConstants.ACTION_EDIT_MYSKILLS, new String[] { DcemConstants.SYSTEM_ROLE_USER, DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN },
				ActionSelection.ONE_ONLY);
		rawActionMySkill.setIcon("fa fa-screwdriver-wrench");
		rawActions.add(rawActionMySkill);
		
	};

	@Override
	public String getModuleId() {
		return SkillsModule.MODULE_ID;
	}

	@Override
	public int getRank() {
		return 10;
	}

	@Override
	public String getIconName() {
		return "fa fa-sitemap";
	}

	@Override
	public String getPath() {
		return SkillsConstants.SKILLS_HIERARCHIE_PATH;
	}

	@Override
	public Class<?> getKlass() {
		return null;
	}
}
