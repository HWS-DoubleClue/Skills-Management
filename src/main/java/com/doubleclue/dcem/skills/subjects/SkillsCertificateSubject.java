//
//
package com.doubleclue.dcem.skills.subjects;

//
import javax.enterprise.context.ApplicationScoped;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.SubjectAbs;
import com.doubleclue.dcem.core.logic.ActionSelection;
import com.doubleclue.dcem.core.logic.ActionType;
import com.doubleclue.dcem.core.logic.RawAction;
import com.doubleclue.dcem.skills.logic.SkillsConstants;
import com.doubleclue.dcem.skills.logic.SkillsModule;
import com.doubleclue.dcem.skills.entities.SkillsCertificateEntity;

@SuppressWarnings("serial")
@ApplicationScoped
public class SkillsCertificateSubject extends SubjectAbs {

	public SkillsCertificateSubject() {
		rawActions.add(new RawAction(DcemConstants.ACTION_ADD, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN },
				ActionSelection.CREATE_OBJECT));
		rawActions.add(new RawAction(DcemConstants.ACTION_EDIT,
				new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN },
				ActionSelection.ONE_ONLY));
		rawActions.add(new RawAction(DcemConstants.ACTION_DELETE,
				new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN },
				ActionSelection.ONE_OR_MORE));
		
		RawAction rawAction = new RawAction(SkillsConstants.REQUEST_CERTIFICATE, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN,
				DcemConstants.SYSTEM_ROLE_ADMIN, DcemConstants.SYSTEM_ROLE_USER });
		rawAction.setIcon("fa fa-plus");
		rawActions.add(rawAction);
		
		RawAction rawActionApprove = new RawAction(SkillsConstants.APPROVE_CERTIFICATE, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN },
				ActionSelection.ONE_OR_MORE);
		rawActionApprove.setIcon("fa fa-check");
		rawActionApprove.setActionType(ActionType.EL_METHOD);
		rawActionApprove.setElMethodExpression("#{skillsCertificateDialog.actionApproveCertificate()}");
		rawActions.add(rawActionApprove);

		rawActions.add(new RawAction(DcemConstants.ACTION_VIEW,  new String[] { DcemConstants.SYSTEM_ROLE_USER}));
		rawActions.add(new RawAction(DcemConstants.ACTION_MANAGE, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN }));
	}

	@Override
	public String getModuleId() {
		return SkillsModule.MODULE_ID;
	}

	@Override
	public int getRank() {
		return 30;
	}

	@Override
	public String getIconName() {
		return "fa fa-solid fa-certificate";
	}

	@Override
	public String getPath() {
		return DcemConstants.AUTO_VIEW_PATH;
	}

	@Override
	public Class<?> getKlass() {
		return SkillsCertificateEntity.class;
	}
}
