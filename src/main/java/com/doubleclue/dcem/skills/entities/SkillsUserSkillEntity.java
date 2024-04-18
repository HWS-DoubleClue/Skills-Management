package com.doubleclue.dcem.skills.entities;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.NamedEntityGraphs;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.Hibernate;

import com.doubleclue.dcem.core.entities.EntityInterface;
import com.doubleclue.dcem.core.gui.DcemGui;
import com.doubleclue.dcem.skills.entities.enums.SkillsLevel;
import com.doubleclue.dcem.skills.entities.enums.SkillsStatus;

@NamedEntityGraphs({ @NamedEntityGraph(name = SkillsUserSkillEntity.GRAPH_SKILL, attributeNodes = { @NamedAttributeNode(value = "skill"), }), })

@NamedQueries({
		@NamedQuery(name = SkillsUserSkillEntity.GET_ALL_SKILLS_OF_USER, query = "SELECT us FROM SkillsUserSkillEntity us JOIN FETCH us.skill "
				+ "WHERE us.skillsUserEntity.id = ?1 " + "ORDER BY us.skill.name ASC"),
		@NamedQuery(name = SkillsUserSkillEntity.GET_BY_SKILLS_ID_AND_SKILLS_USER, query = "SELECT us FROM SkillsUserSkillEntity us "
				+ "WHERE us.skillsUserEntity = ?1 AND us.skill.id = ?2 AND us.status = com.doubleclue.dcem.skills.entities.enums.SkillsStatus.OWNS"),
		@NamedQuery(name = SkillsUserSkillEntity.GET_TARGET_SKILLS_OF_USER, query = "SELECT us FROM SkillsUserSkillEntity us "
				+ "WHERE us.skillsUserEntity = ?1 AND us.status != com.doubleclue.dcem.skills.entities.enums.SkillsStatus.OWNS"),
		@NamedQuery(name = SkillsUserSkillEntity.DELETE_USER_SKILLS_BY_SKILLS, query = "DELETE FROM SkillsUserSkillEntity us where us.skill in ?1"),
		@NamedQuery(name = SkillsUserSkillEntity.GET_SKILL_AND_USER_COUNT_BY_SKILLS, query = "SELECT skill.id as id, "
				+ "COUNT(distinct sus.skillsUserEntity) as count FROM SkillsUserSkillEntity sus LEFT JOIN sus.skill skill "
				+ "WHERE skill in ?1 GROUP BY skill.id"), })

@Entity
@Table(name = "skills_user_skill", uniqueConstraints = {
		@UniqueConstraint(name = "UK_SKILLS_USER_SKILL", columnNames = { "skills_user_id", "skills_id", "level" }) })
public class SkillsUserSkillEntity extends EntityInterface implements Comparable<SkillsUserSkillEntity>, SkillsComparableInterface {

	public static final String GRAPH_SKILL = "SkillUserEntity.graphSkill";
	public static final String GET_ALL_SKILLS_OF_USER = "skillsUserSkillEntity.getAllSkillsOfUser";
	public static final String GET_BY_SKILLS_ID_AND_SKILLS_USER = "SkillUserEntity.getBySkillsIdAndSkillsUser";
	public static final String GET_TARGET_SKILLS_OF_USER = "SkillUserEntity.getTargetSkillsOfUser";
	public static final String DELETE_USER_SKILLS_BY_SKILLS = "skillsUserSkillEntity.deleteUserSkillsBySkills";
	public static final String GET_SKILL_AND_USER_COUNT_BY_SKILLS = "SkillsUserEntity.getSkillAndUserCountFromSkills";

	@Id
	@Column(name = "user_skill_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@DcemGui(subClass = "dcemUser")
	@ManyToOne
	@JoinColumn(name = "skills_user_id", referencedColumnName = "skills_user_id", foreignKey = @ForeignKey(name = "FK_SKILLS_USER_OF_USERSKILL"), nullable = false, insertable = true, updatable = true)
	private SkillsUserEntity skillsUserEntity;

	@DcemGui(subClass = "name")
	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(referencedColumnName = "skills_id", foreignKey = @ForeignKey(name = "FK_SKILLS_SKILL_OF_USERSKILL"), name = "skills_id", nullable = false)
	private SkillsEntity skill;

	@DcemGui
	@Enumerated(EnumType.ORDINAL)
	@Column(name = "level", nullable = false)
	private SkillsLevel level;

	@DcemGui
	@Enumerated(EnumType.ORDINAL)
	@Column(name = "skill_status", nullable = false)
	private SkillsStatus status;

	@Column(name = "skills_date", nullable = true)
	private LocalDate date;

	public SkillsUserSkillEntity() {
	}

	public SkillsUserSkillEntity(SkillsUserEntity skillsUserEntity, SkillsEntity skill, SkillsLevel level, SkillsStatus status) {
		this.skillsUserEntity = skillsUserEntity;
		this.level = level;
		this.skill = skill;
		this.status = status;
	}

	public boolean isSkillNotOwned() {
		if (status == null || status.equals(SkillsStatus.OWNS) == false) {
			return true;
		}
		return false;
	}

	public String getFormattedDate(String language) {
		return Objects.isNull(date) == true ? "" : DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(new Locale(language)).format(date);
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
		return Objects.equals(id, ((SkillsUserSkillEntity) obj).getId());
	}

	public SkillsUserEntity getSkillsUserEntity() {
		return skillsUserEntity;
	}

	public void setSkillsUserEntity(SkillsUserEntity skillsUserEntity) {
		this.skillsUserEntity = skillsUserEntity;
	}

	public SkillsStatus getStatus() {
		return status;
	}

	public void setStatus(SkillsStatus status) {
		this.status = status;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public SkillsEntity getSkill() {
		return skill;
	}

	public void setSkill(SkillsEntity skill) {
		this.skill = skill;
	}

	public SkillsLevel getLevel() {
		return level;
	}

	public void setLevel(SkillsLevel level) {
		this.level = level;
	}

	@Override
	public String toString() {
		if (status != SkillsStatus.OWNS) {
			return skill + ":" + level + " (" + status.getLocaleText() + ")";
		}
		return skill + ":" + level;
	}

	@Override
	public int compareTo(SkillsUserSkillEntity userSkillEntity) {
		status.compareTo(userSkillEntity.getStatus());
		return 0;
	}

	@Override
	public boolean compareObject(SkillsComparableInterface entity) {
		if (Hibernate.getClass(this) != Hibernate.getClass(entity)) {
			return false;
		}
		SkillsUserSkillEntity other = (SkillsUserSkillEntity) entity;
		if (this.getSkillsUserEntity().equals(other.getSkillsUserEntity()) == false) {
			return false;
		}
		if (this.getSkill().equals(other.getSkill()) == false) {
			return false;
		}
		if (this.getLevel().equals(other.getLevel()) == false) {
			return false;
		}
		if (this.getStatus().equals(other.getStatus()) == false) {
			return false;
		}
		if (this.getDate() == null) {
			if (other.getDate() != null) {
				return false;
			}
		} else {
			if (this.getDate().equals(other.getDate()) == false) {
				return false;
			}
		}
		return true;
	}
}
