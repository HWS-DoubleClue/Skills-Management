package com.doubleclue.dcem.skills.subjects;

import javax.enterprise.context.ApplicationScoped;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.SubjectAbs;
import com.doubleclue.dcem.core.entities.DcemAction;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.logic.ActionSelection;
import com.doubleclue.dcem.core.logic.RawAction;
import com.doubleclue.dcem.skills.entities.SkillsUserEntity;
import com.doubleclue.dcem.skills.logic.SkillsModule;

@SuppressWarnings("serial")
@ApplicationScoped
public class SkillsUserSubject extends SubjectAbs {

	public SkillsUserSubject() {
		setShowIfHeadOf(true);
		rawActions.add(new RawAction(DcemConstants.ACTION_ADD, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN },
				ActionSelection.CREATE_OBJECT));
		rawActions.add(new RawAction(DcemConstants.ACTION_EDIT, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN },
				ActionSelection.ONE_ONLY));
		rawActions.add(new RawAction(DcemConstants.ACTION_DELETE, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN },
				ActionSelection.ONE_OR_MORE));

		rawActions.add(new RawAction(DcemConstants.ACTION_VIEW, null));
		rawActions.add(new RawAction(DcemConstants.ACTION_MANAGE, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN }));
	};

	@Override
	public String getModuleId() {
		return SkillsModule.MODULE_ID;
	}

	@Override
	public int getRank() {
		return 20;
	}

	@Override
	public String getIconName() {
		return "fa fa-user-pen";
	}

	@Override
	public String getPath() {
		return DcemConstants.AUTO_VIEW_PATH;
	}

	@Override
	public Class<?> getKlass() {
		return SkillsUserEntity.class;
	}

	@Override
	public boolean forceAction(DcemUser dcemUser, DcemAction dcemAction) {
		return DcemConstants.ACTION_EDIT.equals(dcemAction.getAction());
	}
}
