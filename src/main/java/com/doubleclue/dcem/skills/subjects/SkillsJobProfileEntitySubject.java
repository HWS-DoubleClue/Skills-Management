//
//
package com.doubleclue.dcem.skills.subjects;
//
import javax.enterprise.context.ApplicationScoped;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.SubjectAbs;
import com.doubleclue.dcem.core.logic.ActionSelection;
import com.doubleclue.dcem.core.logic.RawAction;
import com.doubleclue.dcem.skills.logic.SkillsConstants;
import com.doubleclue.dcem.skills.logic.SkillsModule;
import com.doubleclue.dcem.skills.entities.SkillsJobProfileEntity;

@SuppressWarnings("serial")
@ApplicationScoped
public class SkillsJobProfileEntitySubject extends SubjectAbs {
	
	public SkillsJobProfileEntitySubject() {

		rawActions.add(new RawAction(DcemConstants.ACTION_ADD, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN },
				ActionSelection.IGNORE));	
		rawActions.add(new RawAction(DcemConstants.ACTION_EDIT, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN},
				ActionSelection.ONE_ONLY));
		rawActions.add(new RawAction(DcemConstants.ACTION_DELETE, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN },
				ActionSelection.ONE_OR_MORE));
		RawAction rawAction = new RawAction(SkillsConstants.SKILLS_IMPORT, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN },
				ActionSelection.ONE_ONLY);
		rawAction.setIcon("fa fa-solid fa-file-import");
		rawActions.add(rawAction);
				
		rawActions.add(new RawAction (DcemConstants.ACTION_VIEW, new String[] { DcemConstants.SYSTEM_ROLE_USER }));
		rawActions.add(new RawAction (DcemConstants.ACTION_MANAGE, new String [] {DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN}));	
		
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
		return "fa fa-toolbox";
	}

	@Override
	public String getPath() {
		return DcemConstants.AUTO_VIEW_PATH;
	}


	@Override
	public Class<?> getKlass() {
		return SkillsJobProfileEntity.class;
	}

}
