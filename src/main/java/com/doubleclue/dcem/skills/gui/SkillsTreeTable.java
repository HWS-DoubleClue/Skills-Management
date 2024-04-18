package com.doubleclue.dcem.skills.gui;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.event.CellEditEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.doubleclue.dcem.core.logic.UserLogic;
import com.doubleclue.dcem.skills.entities.SkillsEntity;
import com.doubleclue.dcem.skills.entities.SkillsLevelEntity;
import com.doubleclue.dcem.skills.entities.enums.SkillsLevel;
import com.doubleclue.dcem.skills.logic.SkillsLogic;
import com.doubleclue.dcem.skills.logic.SkillsModule;
import com.doubleclue.dcem.skills.utils.SkillsUtils;

@SessionScoped
@Named
public class SkillsTreeTable implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final int DEFAULT_PRIORITY = 1;

	private static final Logger logger = LogManager.getLogger(SkillsTreeTable.class);

	@Inject
	SkillsLogic skillsLogic;

	@Inject
	UserLogic userLogic;

	@Inject
	OperatorSessionBean operatorSessionBean;

	TreeNode<SkillTreeNode> skillTree;
	List<SkillsEntity> allSkillsEntities;
	SortedSet<SkillsLevelEntity> availableSkills;
	OnCellEditInterfaces onCellEditInterfaces;
	boolean showAllAvailableSkills;
	DcemUser treeTableUser;
	boolean isEditOrUpdate;
	SkillsEntity skillRoot;

	ResourceBundle resourceBundle;

	@PostConstruct
	public void init() {
		try {
			resourceBundle = JsfUtils.getBundle(SkillsModule.RESOURCE_NAME, operatorSessionBean.getLocale());
			allSkillsEntities = skillsLogic.getAllSkillsWithFilteringNotApproved();
			skillRoot = skillsLogic.getSkillRoot();
		} catch (Exception e) {
			logger.error("", e);
		}
	}

	public void onCellEdit(CellEditEvent<?> event) {
		if (onCellEditInterfaces == null) {
			return;
		}
		try {
			onCellEditInterfaces.actionOnCellEdit(event);
		} catch (Exception dcemExp) {
			JsfUtils.addErrorMessage(dcemExp.getLocalizedMessage());
			logger.error("", dcemExp);
		}
	}

	public void actionListenShowAllSkills() {
		onCellEditInterfaces.listenShowAllSkills();
	}

	public TreeNode<SkillTreeNode> updateSkillTree(SortedSet<SkillsLevelEntity> availableSkills, OnCellEditInterfaces onRowInterfaces) {
		try {
			skillRoot = skillsLogic.getSkillRoot();
			allSkillsEntities = skillsLogic.getAllSkillsWithFilteringNotApproved();
			if (availableSkills == null) {
				availableSkills = new TreeSet<SkillsLevelEntity>();
			}
			this.availableSkills = availableSkills;
			this.onCellEditInterfaces = onRowInterfaces;

			skillTree = new DefaultTreeNode<SkillTreeNode>();
			SkillTreeNode skillRootNode = new SkillTreeNode(null, skillRoot, SkillsLevel.No, DEFAULT_PRIORITY);
			TreeNode<SkillTreeNode> rootSkillTreeNode = new DefaultTreeNode<SkillTreeNode>(skillRootNode);
			skillTreeChildren(rootSkillTreeNode, availableSkills);
			return skillTree;
		} catch (Exception e) {
			logger.error("", e);
			JsfUtils.addErrorMessage(resourceBundle, "error.global");
			return new DefaultTreeNode<SkillTreeNode>();
		}
	}

	private void skillTreeChildren(TreeNode<SkillTreeNode> treeNode, SortedSet<SkillsLevelEntity> availableSkills) {
		SkillsEntity nodeParentSkill = treeNode.getData().getSkillsEntity();
		for (SkillsEntity skillsEntity : allSkillsEntities) {
			if (skillsEntity.equals(skillRoot) || skillsEntity.getParent().equals(nodeParentSkill) == false) {
				continue;
			}
			SkillsLevelEntity skillsLevelEntity = getAvailableSkillLevelOrDefault(skillsEntity, availableSkills);
			DefaultTreeNode<SkillTreeNode> childNode = new DefaultTreeNode<SkillTreeNode>(new SkillTreeNode(skillsLevelEntity));

			if (showAllAvailableSkills || hasSkillOrChildrenWithLevel(skillsEntity, availableSkills)) {
				childNode.setExpanded(true);
				if (nodeParentSkill.equals(skillRoot)) {
					skillTree.getChildren().add(childNode);
				} else {
					treeNode.getChildren().add(childNode);
				}
				skillTreeChildren(childNode, availableSkills);
			}

		}
	}

	private SkillsLevelEntity getAvailableSkillLevelOrDefault(SkillsEntity skillsEntity, SortedSet<SkillsLevelEntity> availableSkills) {
		for (SkillsLevelEntity skillsLevelEntity : availableSkills) {
			if (skillsLevelEntity.getSkill().equals(skillsEntity)) {
				return skillsLevelEntity;
			}
		}
		return new SkillsLevelEntity(skillsEntity, SkillsLevel.No, DEFAULT_PRIORITY);
	}

	private boolean hasSkillOrChildrenWithLevel(SkillsEntity skillsEntityParent, SortedSet<SkillsLevelEntity> availableSkills) {
		SkillsEntity skillsEntity;
		for (SkillsLevelEntity skillsLevelEntity : availableSkills) {
			skillsEntity = skillsLevelEntity.getSkill();
			while (skillsEntity.equals(skillRoot) == false) {
				if (skillsEntity.equals(skillsEntityParent)) {
					return true;
				}
				skillsEntity = skillsEntity.getParent();
			}
		}
		return false;
	}

	public SortedSet<SkillsLevelEntity> getSkillAvailableList(TreeNode<SkillTreeNode> node, SortedSet<SkillsLevelEntity> sortedSet) {
		if (sortedSet == null) {
			sortedSet = new TreeSet<>();
		}
		if (node == null) {
			node = skillTree; // start from root
		}
		for (TreeNode<SkillTreeNode> treeNode : node.getChildren()) {
			if (treeNode.getData().getSkillsLevel() != SkillsLevel.No) {
				sortedSet.add(
						new SkillsLevelEntity(treeNode.getData().getSkillsEntity(), treeNode.getData().getSkillsLevel(), treeNode.getData().getPriority()));
			}
			getSkillAvailableList(treeNode, sortedSet);
		}
		return sortedSet;
	}

	public List<SelectItem> getSkillsLevel() {
		List<SelectItem> list = new ArrayList<SelectItem>();
		for (SkillsLevel skillsLevel : SkillsLevel.values()) {
			list.add(new SelectItem(skillsLevel.name(), skillsLevel.getLocaleText()));
		}
		return list;
	}

	public String getSkillsLevelOutput(SkillsLevel level) {
		return JsfUtils.getStringSafely(resourceBundle, level.name()) + " " + SkillsUtils.convertLevelToStars(level);
	}

	public List<SkillsEntity> getAllSkillsEntities() {
		return allSkillsEntities;
	}

	public void setAllSkillsEntities(List<SkillsEntity> allSkillsEntities) {
		this.allSkillsEntities = allSkillsEntities;
	}

	public boolean isShowAllAvailableSkills() {
		return showAllAvailableSkills;
	}

	public void setShowAllAvailableSkills(boolean showAllAvailableSkills) {
		this.showAllAvailableSkills = showAllAvailableSkills;
	}

	public DcemUser getTreeTableUser() {
		return treeTableUser;
	}

	public void setTreeTableUser(DcemUser treeTableUser) {
		this.treeTableUser = treeTableUser;
	}

	public TreeNode<SkillTreeNode> getSkillTree() {
		return skillTree;
	}

	public void setSkillTree(TreeNode<SkillTreeNode> skillTree) {
		this.skillTree = skillTree;
	}

}
