package com.doubleclue.dcem.skills.entities;

import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Size;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.SortNatural;

import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.entities.EntityInterface;
import com.doubleclue.dcem.core.gui.DcemGui;

@NamedQueries({
	@NamedQuery(name = SkillsJobProfileEntity.REMOVE_USER_FROM_MANAGED_BY_FROM_SKILLSPROFILE, query = "UPDATE SkillsJobProfileEntity jp SET jp.managedBy= null WHERE jp.managedBy = ?1"),
	@NamedQuery(name = SkillsJobProfileEntity.GET_BY_SKILL, query = "SELECT DISTINCT jp FROM SkillsJobProfileEntity jp LEFT JOIN jp.skillLevels sl LEFT JOIN sl.skill s WHERE s = ?1 "),
	@NamedQuery(name = SkillsJobProfileEntity.GET_BY_SKILLS_LEVELS, query = "SELECT DISTINCT jp FROM SkillsJobProfileEntity jp LEFT JOIN jp.skillLevels sl WHERE sl IN ?1"),
	@NamedQuery(name = SkillsJobProfileEntity.GET_BY_NAME, query = "SELECT jp FROM SkillsJobProfileEntity jp WHERE jp.name = ?1 "),
	@NamedQuery(name = SkillsJobProfileEntity.GET_BY_NAME_LIKE, query = "SELECT jp FROM SkillsJobProfileEntity jp WHERE lower(jp.name) LIKE lower(?1) ORDER BY jp.name ASC"),
	@NamedQuery(name = SkillsJobProfileEntity.GET_SKILL_ID_AND_JOBPROFILE_COUNT_BY_SKILLS, query = "SELECT skill.id as id, COUNT(jp) as count FROM SkillsJobProfileEntity jp LEFT JOIN jp.skillLevels skillLevel LEFT JOIN skillLevel.skill skill WHERE skill in ?1 GROUP BY skill.id"), 		
})


@Entity
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Table(name = "skills_jobprofile", uniqueConstraints = @UniqueConstraint(name = "UK_JOBPROFILE_NAME", columnNames = { "dc_name" }))
public class SkillsJobProfileEntity extends EntityInterface {

	public static final String GET_BY_SKILL = "SkillsJobProfileEntity.getBySkill";
	public static final String GET_BY_SKILLS_LEVELS = "SkillsJobProfileEntity.getBySkills";
	public static final String REMOVE_USER_FROM_MANAGED_BY_FROM_SKILLSPROFILE = "SkillsJobProfileEntity.removeUserFromManagedByFromSkillsprofile";
	public static final String GET_BY_NAME = "SkillsJobProfileEntity.getByName";
	public static final String GET_BY_NAME_LIKE = "SkillsJobProfileEntity.getByNameLike";
	public static final String GET_SKILL_ID_AND_JOBPROFILE_COUNT_BY_SKILLS = "SkillsJobProfileEntity.getSkillAndJobprofileCountBySkills";

	@Id
	@Column(name = "dc_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@DcemGui
	@Size(max = 255)
	@Column(name = "dc_name", nullable = false)
	private String name;

	@DcemGui(visible = false)
	@Size(max = 255)
	@Column(nullable = true, name = "description")
	private String description;

	@ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
	@JoinTable(name = "skills_ref_skills_jobProfile", joinColumns = @JoinColumn(name = "dc_id"), foreignKey = @ForeignKey(name = "FK_SKILLS_JOBPROFILE"), inverseJoinColumns = @JoinColumn(name = "skills_level_id"), inverseForeignKey = @ForeignKey(name = "FK_JOBPROFILE_SKILLS"))
	@DcemGui(visible = true, subClass = "skill", name = "Skills")
	@SortNatural
	private SortedSet<SkillsLevelEntity> skillLevels;

	@DcemGui(visible = true, subClass = "certificateEntity", name = "Certificates")
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "skills_ref_certificate_priority_jobProfile", joinColumns = @JoinColumn(name = "dc_id"), foreignKey = @ForeignKey(name = "FK_CERTIFICATE__PRIORITY_JOBPROFILE"), inverseJoinColumns = @JoinColumn(name = "certificate_priority_id"), inverseForeignKey = @ForeignKey(name = "FK_JOBPROFILE_CERTIFICATE"))
	@SortNatural
	private SortedSet<SkillsCertificatePriorityEntity> certificatesPriorities;

	@DcemGui(name = "managedBy", subClass = "displayName")
	@ManyToOne
	@JoinColumn(nullable = true, foreignKey = @ForeignKey(name = "FK_JOBPROFILE_USER"), insertable = true, updatable = true)
	private DcemUser managedBy;
	
	public SkillsJobProfileEntity() {
		
	}
	
	public SkillsJobProfileEntity(String name) {
		this.name = name;
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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SkillsJobProfileEntity other = (SkillsJobProfileEntity) obj;
		return Objects.equals(id, other.getId());
	}

	@Override
	public String toString() {
		return name;
	}

	public SortedSet<SkillsLevelEntity> getSkillLevels() {
		return skillLevels;
	}

	public void setSkillLevels(SortedSet<SkillsLevelEntity> skillLevels) {
		this.skillLevels = skillLevels;
	}

	public DcemUser getManagedBy() {
		return managedBy;
	}

	public void setManagedBy(DcemUser managedBy) {
		this.managedBy = managedBy;
	}

	public SortedSet<SkillsCertificatePriorityEntity> getCertificatesPriorities() {
		if (certificatesPriorities == null) {
			certificatesPriorities = new TreeSet<>();
		}
		return certificatesPriorities;
		
	}

	public void setCertificatesPriorities(SortedSet<SkillsCertificatePriorityEntity> certificatesPriorities) {
		this.certificatesPriorities = certificatesPriorities;
	}

}
