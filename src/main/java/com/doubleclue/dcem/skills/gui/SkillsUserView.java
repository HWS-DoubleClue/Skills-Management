package com.doubleclue.dcem.skills.gui;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.entities.DcemAction;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.entities.DcemUserExtension;
import com.doubleclue.dcem.core.entities.DcemUserExtension_;
import com.doubleclue.dcem.core.entities.DcemUser_;
import com.doubleclue.dcem.core.entities.DepartmentEntity;
import com.doubleclue.dcem.core.entities.DepartmentEntity_;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.skills.entities.SkillsUserEntity;
import com.doubleclue.dcem.skills.entities.SkillsUserEntity_;
import com.doubleclue.dcem.skills.logic.SkillsConstants;
import com.doubleclue.dcem.skills.logic.SkillsModule;
import com.doubleclue.dcem.skills.subjects.SkillsUserSubject;

@Named("skillsUserView")
@SessionScoped
public class SkillsUserView extends DcemView implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private SkillsUserDialog skillsUserDialog;

	@Inject
	private SkillsUserSubject skillUserSubject;

	ResourceBundle resourceBundle;
	private boolean viewManager;

	@PostConstruct
	public void init() {
		skillsUserDialog.setParentView(this);
		subject = skillUserSubject;
		ResourceBundle resourceBundle = JsfUtils.getBundle(SkillsModule.RESOURCE_NAME, operatorSessionBean.getLocale());
		
		addAutoViewAction(DcemConstants.ACTION_ADD, resourceBundle, skillsUserDialog, SkillsConstants.SKILLS_USER_DIALOG_PATH);
		addAutoViewAction(DcemConstants.ACTION_EDIT, resourceBundle, skillsUserDialog, SkillsConstants.SKILLS_USER_DIALOG_PATH);
		addAutoViewAction(DcemConstants.ACTION_DELETE, resourceBundle, skillsUserDialog, DcemConstants.AUTO_CONFIRM_DIALOG_PATH);
	}

	@Override
	public void reload() {
		DcemAction dcemAction = new DcemAction(subject, DcemConstants.ACTION_MANAGE);
		viewManager = operatorSessionBean.isPermission(dcemAction);
	}

	@Override
	public List<Predicate> getPredicates(CriteriaBuilder criteriaBuilder, Root<?> root) {
		ArrayList<Predicate> predicates = new ArrayList<Predicate>();
		if (viewManager == true) {
			return predicates;
		}
		Predicate predicate;
		@SuppressWarnings("unchecked")
		Root<SkillsUserEntity> skillsUserEntityroot = (Root<SkillsUserEntity>) root;

		// Head of Department
		Join<SkillsUserEntity, DcemUser> joinDcemUser = skillsUserEntityroot.join(SkillsUserEntity_.dcemUser, JoinType.LEFT);
		Join<DcemUser, DcemUserExtension> joinDcemUserExtension = joinDcemUser.join(DcemUser_.dcemUserExt, JoinType.LEFT);
		Join<DcemUserExtension, DepartmentEntity> joinDepartment = joinDcemUserExtension.join(DcemUserExtension_.department, JoinType.LEFT);
		Predicate hodPredicate = criteriaBuilder.equal(joinDepartment.get(DepartmentEntity_.headOf), operatorSessionBean.getDcemUser());

		Join<DepartmentEntity, DepartmentEntity> joinParentDepartment = joinDepartment.join(DepartmentEntity_.parentDepartment, JoinType.LEFT);
		Predicate hodParentPredicate = criteriaBuilder.equal(joinParentDepartment.get(DepartmentEntity_.headOf), operatorSessionBean.getDcemUser());

		predicate = criteriaBuilder.or(hodParentPredicate, hodPredicate);

		predicates.add(predicate);
		return predicates;
	}

	public ResourceBundle getResourceBundle() {
		return resourceBundle;
	}
}
