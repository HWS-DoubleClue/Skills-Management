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

import org.hibernate.Hibernate;

import com.doubleclue.dcem.core.entities.EntityInterface;
import com.doubleclue.dcem.core.gui.DcemGui;
import com.doubleclue.dcem.skills.entities.enums.SkillsStatus;

@NamedQueries({
		@NamedQuery(name = SkillsUserJobProfileEntity.GET_ALL_PROFILES_OF_USER, query = "SELECT ujp FROM SkillsUserJobProfileEntity ujp JOIN FETCH ujp.jobProfile WHERE ujp.skillsUserEntity.id = ?1 ORDER BY ujp.jobProfile.name ASC"),
		@NamedQuery(name = SkillsUserJobProfileEntity.GET_ALL_USER_JOBPROFILES_BY_JOBPROFILE, query = "SELECT ujp FROM SkillsUserJobProfileEntity ujp WHERE ujp.jobProfile = ?1 "), })

@Entity
@Table(name = "skills_userjobprofile", uniqueConstraints = @UniqueConstraint(name = "UK_SKILLS_USERJOBPROFILES", columnNames = { "user_id", "jobprofile_id" }))

public class SkillsUserJobProfileEntity extends EntityInterface implements SkillsComparableInterface {

	public static final String GET_ALL_PROFILES_OF_USER = "SkillsUserJobProfileEntity.getAllProfilesOfUser";
	public static final String GET_ALL_USER_JOBPROFILES_BY_JOBPROFILE = "SkillsUserJobProfileEntity.getAllUserJobProfilesByJobProfile";

	@Id
	@Column(name = "dc_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@DcemGui(subClass = "dcemUser")
	@ManyToOne
	@JoinColumn(name = "user_id", referencedColumnName = "skills_user_id", foreignKey = @ForeignKey(name = "FK_SKILLS_USERJOBPROFILE"), nullable = false, insertable = true, updatable = false)
	private SkillsUserEntity skillsUserEntity;

	@DcemGui(subClass = "name")
	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.REFRESH)
	@JoinColumn(referencedColumnName = "dc_id", foreignKey = @ForeignKey(name = "FK_SKILLS_JOBPROFILE_USER"), name = "jobprofile_id", nullable = false)
	private SkillsJobProfileEntity jobProfile;

	@Column(name = "dc_match")
	private int match = 0; // this is current user match in percent of the user Profile

	@DcemGui
	@Enumerated(EnumType.ORDINAL)
	@Column(name = "dc_status", nullable = false)
	private SkillsStatus status;

	@Column(name = "dc_date", nullable = true)
	private LocalDate date;

	public SkillsUserJobProfileEntity() {
	}

	public String getFormattedDate(String language) {
		return Objects.isNull(date) == true ? "" : DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(new Locale(language)).format(date);
	}

	public boolean isProfileNotOwned() {
		if (status == null || status.equals(SkillsStatus.OWNS) == false) {
			return true;
		}
		return false;
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

	public SkillsUserEntity getSkillsUserEntity() {
		return skillsUserEntity;
	}

	public void setSkillsUserEntity(SkillsUserEntity skillsUserEntity) {
		this.skillsUserEntity = skillsUserEntity;
	}

	public SkillsJobProfileEntity getJobProfile() {
		return jobProfile;
	}

	public void setJobProfile(SkillsJobProfileEntity jobProfile) {
		this.jobProfile = jobProfile;
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

	public int getMatch() {
		return match;
	}

	public void setMatch(int match) {
		this.match = match;
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
		SkillsUserJobProfileEntity other = (SkillsUserJobProfileEntity) obj;
		return Objects.equals(id, other.getId());
	}

	@Override
	public String toString() {
		return jobProfile + ": " + match + "%";
	}

	@Override
	public boolean compareObject(SkillsComparableInterface entity) {
		if (Hibernate.getClass(this) != Hibernate.getClass(entity)) {
			return false;
		}
		SkillsUserJobProfileEntity other = (SkillsUserJobProfileEntity) entity;
		if (this.getSkillsUserEntity().equals(other.getSkillsUserEntity()) == false) {
			return false;
		}
		if (this.getJobProfile().equals(other.getJobProfile()) == false) {
			return false;
		}
		if (this.getMatch() == other.getMatch() == false) {
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
