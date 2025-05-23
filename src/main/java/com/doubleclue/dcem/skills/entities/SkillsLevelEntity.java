package com.doubleclue.dcem.skills.entities;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.CascadeType;
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
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.doubleclue.dcem.core.entities.EntityInterface;
import com.doubleclue.dcem.core.gui.DcemGui;
import com.doubleclue.dcem.skills.entities.enums.SkillsLevel;
import com.fasterxml.jackson.annotation.JsonIgnore;


@Entity
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Table(name = "skills_skills_level", uniqueConstraints = @UniqueConstraint(name = "UK_SKILLS_SKILLSLEVEL", columnNames = { "skills_id", "level", "dc_priority" }))
@NamedQueries({ 
	@NamedQuery(name = SkillsLevelEntity.GET_ALL_SKILL_LEVELS_BY_SKILLS, query = "SELECT DISTINCT sl FROM SkillsLevelEntity sl where sl.skill in ?1"),
	@NamedQuery(name = SkillsLevelEntity.GET_SKILLLEVEL, query = "SELECT sl FROM SkillsLevelEntity sl WHERE sl.skill = ?1 AND sl.level = ?2 AND sl.priority = ?3"),
})
public class SkillsLevelEntity extends EntityInterface implements Comparable<SkillsLevelEntity>, Serializable  {


	private static final long serialVersionUID = 1L;
	public static final String GET_ALL_SKILL_LEVELS_BY_SKILLS = "skillsLevel.getAllBySkill";
	public static final String GET_SKILLLEVEL = "skillsLevel.getSkilllevel";

	@Id
	@Column(name = "skills_level_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@JsonIgnore
	private Integer id;

	@DcemGui(subClass = "name")
	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.REFRESH)
	@JoinColumn(referencedColumnName = "skills_id", foreignKey = @ForeignKey(name = "FK_SKILLS"), name = "skills_id", nullable = false)
	private SkillsEntity skill;

	@DcemGui
	@Enumerated(EnumType.ORDINAL)
	@Column(name = "level", nullable = false)
	private SkillsLevel level;
	
	@Column(name = "dc_priority")
	private int priority = 1;

	public SkillsLevelEntity() {

	}

	public SkillsLevelEntity(SkillsEntity skillInput, SkillsLevel levelInput , int priority) {
		this.skill = skillInput;
		this.level = levelInput;
		this.priority = priority;
	}

	public SkillsLevelEntity(int id, SkillsEntity skillsEntity, SkillsLevel skillsLevel, int priority) {
		this.id = id;
		this.skill = skillsEntity;
		this.level = skillsLevel;
		this.priority = priority;
	}
	
	@Override
	public Integer getId() {
		return id;
	}

	@Override
	@JsonIgnore
	public void setId(Number id) {
		if (id != null) {
			this.id = id.intValue();
		} else {
			id = null;
		}
	}


	public SkillsLevel getLevel() {
		return level;
	}

	public void setLevel(SkillsLevel level) {
		this.level = level;
	}

	@Override
	public String toString() {
		return skill.getName() + ":" + level.name() + "\u2191" + priority ;
	}

	public SkillsEntity getSkill() {
		return skill;
	}

	public void setSkill(SkillsEntity skill) {
		this.skill = skill;
	}

	@Override
	public int hashCode() {
		return Objects.hash(getSkill().getId(), level, priority);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		SkillsLevelEntity other = (SkillsLevelEntity) obj;
		return (this.getSkill().getId() == other.getSkill().getId()) && (this.getLevel() == other.getLevel()) && (this.getPriority() == other.getPriority());
	}

	@Override
	public int compareTo(SkillsLevelEntity skillsLevelEntity) {
		if (this.getSkill().equals(skillsLevelEntity.getSkill()) == false) {
			return this.getSkill().getNameWithParent().compareTo(skillsLevelEntity.getSkill().getNameWithParent());
		} else if (this.getLevel() != skillsLevelEntity.getLevel()) {
			return this.getLevel().compareTo(skillsLevelEntity.getLevel());
		} else {
			return this.getPriority() - skillsLevelEntity.getPriority();
		}
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

}
