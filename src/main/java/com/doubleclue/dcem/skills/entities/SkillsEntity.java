package com.doubleclue.dcem.skills.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Size;

import org.hibernate.Hibernate;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.primefaces.model.SortOrder;

import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.entities.EntityInterface;
import com.doubleclue.dcem.core.gui.DcemGui;
import com.doubleclue.dcem.core.utils.compare.DcemCompare;
import com.doubleclue.dcem.skills.entities.enums.ApprovalStatus;
import com.doubleclue.dcem.skills.logic.SkillsConstants;

@Entity
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Table(name = "skills_skills", uniqueConstraints = { @UniqueConstraint(name = "UK_SKILLS_NAME", columnNames = { "skills_name", "parent_id" }),
		@UniqueConstraint(name = "UK_SKILLS_ABBR", columnNames = { "abbreviation" }), })

@NamedQueries({ @NamedQuery(name = SkillsEntity.GET_ALL, query = "SELECT tt FROM SkillsEntity tt ORDER BY tt.name ASC"),
		@NamedQuery(name = SkillsEntity.GET_BY_NAME_AND_PARENT, query = "SELECT tt FROM SkillsEntity tt LEFT JOIN tt.parent pp WHERE tt.name = ?1 AND (pp.name = ?2 OR (pp is NULL AND ?2 is NULL))"),
		@NamedQuery(name = SkillsEntity.GET_PARENTLESS_SKILLS, query = "SELECT tt FROM SkillsEntity tt LEFT JOIN tt.parent pp WHERE pp is NULL "),
		@NamedQuery(name = SkillsEntity.GET_BY_NAME, query = "SELECT tt FROM SkillsEntity tt WHERE LOWER(tt.name) LIKE LOWER(?1) ORDER BY tt.name ASC"),
		@NamedQuery(name = SkillsEntity.GET_APPROVED_SKILLS, query = "SELECT tt FROM SkillsEntity tt WHERE tt.approvalStatus = com.doubleclue.dcem.skills.entities.enums.ApprovalStatus.APPROVED ORDER BY tt.name ASC"), })
public class SkillsEntity extends EntityInterface {

	public static final String GET_ALL = "skills.all";
	public static final String GET_PARENTLESS_SKILLS = "skills.withoutParent";
	public static final String GET_BY_NAME_AND_PARENT = "skills.byNameAndParent";
	public static final String GET_BY_NAME = "skills.byName";
	public static final String GET_APPROVED_SKILLS = "skills.getApprovedSkills";

	@Id
	@Column(name = "skills_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@DcemGui(sortOrder = SortOrder.ASCENDING)
	@Size(max = 255)
	@Column(name = "skills_name")
	private String name;

	@DcemGui(name = "Parent", subClass = "name")
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(referencedColumnName = "skills_id", foreignKey = @ForeignKey(name = "FK_SKILLS_PARENT"), name = "parent_id", nullable = true, insertable = true, updatable = true)
	private SkillsEntity parent;

	@DcemGui
	@Size(max = 63)
	private String abbreviation;

	@DcemGui
	@Size(max = 255)
	@DcemCompare (withoutResult = true)
	private String description;

	@DcemGui
	@Enumerated(EnumType.ORDINAL)
	@Column(name = "dc_status", nullable = false)
	private ApprovalStatus approvalStatus;

	@DcemGui
	@ManyToOne
	@JoinColumn(name = "requested_from_id", referencedColumnName = "dc_id", foreignKey = @ForeignKey(name = "FK_SKILLS_REQUESTEDFROM"), insertable = true)
	private DcemUser requestedFrom;

	@OneToMany(mappedBy = "parent")
	private List<SkillsEntity> children;

	@DcemGui
	@Column(name = "obtainable", nullable = false)
	private boolean obtainable = true;

	public SkillsEntity() {
	}

	@Override
	public Integer getId() {
		return id;
	}

	@Override
	public void setId(Number id) {
		if (id != null) {
			this.id = id.intValue();
		} else {
			id = null;
		}
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNameWithAbbr() {
		return name + " (" + getAbbreviation() + ")";
	}

	public String getAbbreviation() {
		return abbreviation;
	}

	public void setAbbreviation(String abbreviation) {
		this.abbreviation = abbreviation;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public SkillsEntity getParent() {
		return parent;
	}

	public void setParent(SkillsEntity parent) {
		this.parent = parent;
	}

	

	@Override
	public String toString() {
		return name;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (Hibernate.getClass(this ) != Hibernate.getClass(obj)) {
			return false;
		}
		return Objects.equals(id, ((SkillsEntity)obj).getId());
	}

	public DcemUser getRequestedFrom() {
		return requestedFrom;
	}

	public void setRequestedFrom(DcemUser requestedFrom) {
		this.requestedFrom = requestedFrom;
	}

	public List<SkillsEntity> getChildren() {
		if (children == null) {
			return new ArrayList<SkillsEntity>();
		}
		return children;
	}

	public void setChildren(List<SkillsEntity> children) {
		this.children = children;
	}

	public String getNameWithParent() {
		if (parent == null) {
			return name;
		}
		return parent.getName().equals(SkillsConstants.SKILLS_ROOT) ? name : name + SkillsConstants.PARENT_SEPERATOR + parent.getName();
	}

	public boolean isObtainable() {
		return obtainable;
	}

	public void setObtainable(boolean obtainable) {
		this.obtainable = obtainable;
	}

	public ApprovalStatus getApprovalStatus() {
		return approvalStatus;
	}

	public void setApprovalStatus(ApprovalStatus approvalStatus) {
		this.approvalStatus = approvalStatus;
	}
}
