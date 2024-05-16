package com.doubleclue.dcem.skills.subjects;

import javax.enterprise.context.ApplicationScoped;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.SubjectAbs;
import com.doubleclue.dcem.core.logic.ActionSelection;
import com.doubleclue.dcem.core.logic.ActionType;
import com.doubleclue.dcem.core.logic.RawAction;
import com.doubleclue.dcem.skills.entities.SkillsEntity;
import com.doubleclue.dcem.skills.logic.SkillsConstants;
import com.doubleclue.dcem.skills.logic.SkillsModule;

@SuppressWarnings("serial")
@ApplicationScoped
public class SkillsSubject extends SubjectAbs {

	public SkillsSubject() {
		rawActions.add(new RawAction(DcemConstants.ACTION_ADD, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN },
				ActionSelection.CREATE_OBJECT));
		rawActions.add(new RawAction(DcemConstants.ACTION_EDIT, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN },
				ActionSelection.ONE_ONLY));
		rawActions.add(new RawAction(DcemConstants.ACTION_COPY, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN },
				ActionSelection.ONE_ONLY));

		rawActions.add(new RawAction(DcemConstants.ACTION_DELETE, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN },
				ActionSelection.ONE_OR_MORE));

		RawAction rawAction = new RawAction(DcemConstants.ACTION_ORGANIGRAM,
				new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN, DcemConstants.SYSTEM_ROLE_USER }, ActionSelection.IGNORE);
		rawAction.setIcon("fa fa-sitemap");
		rawAction.setActionType(ActionType.VIEW_LINK);
		rawActions.add(rawAction);
		
		RawAction rawActionRequest = new RawAction(SkillsConstants.REQUEST_SKILLS,
				new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN, DcemConstants.SYSTEM_ROLE_USER }, ActionSelection.CREATE_OBJECT);
		rawActionRequest.setIcon("fa fa-plus");
		rawActionRequest.setActionType(ActionType.CREATE_OBJECT);
		rawActions.add(rawActionRequest);
		
		RawAction rawActionApprove = new RawAction(SkillsConstants.APPROVE_SKILL, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN },
				ActionSelection.ONE_OR_MORE);
		rawActionApprove.setIcon("fa fa-check");
		rawActionApprove.setActionType(ActionType.EL_METHOD);
		rawActionApprove.setElMethodExpression("#{skillsView.actionApproveSkills()}");
		rawActions.add(rawActionApprove);
		
		RawAction rawActionMerge = new RawAction(SkillsConstants.MERGE_SKILLS,
				new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN}, ActionSelection.IGNORE);
		rawActionMerge.setIcon("fa fa-solid fa-arrow-right-to-bracket");
		rawActions.add(rawActionMerge);

		rawActions.add(new RawAction(DcemConstants.ACTION_VIEW, new String[] { DcemConstants.SYSTEM_ROLE_USER }));
		rawActions.add(new RawAction(DcemConstants.ACTION_MANAGE, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN }));
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
		return "fa fa-screwdriver-wrench";
	}

	@Override
	public String getPath() {
		return DcemConstants.AUTO_VIEW_PATH;
	}

	@Override
	public Class<?> getKlass() {
		return SkillsEntity.class;
	}

}
