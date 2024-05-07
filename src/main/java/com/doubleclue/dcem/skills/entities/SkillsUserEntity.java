package com.doubleclue.dcem.skills.entities;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.NamedEntityGraphs;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.NamedSubgraph;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.primefaces.model.SortOrder;

import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.entities.EntityInterface;
import com.doubleclue.dcem.core.gui.DcemGui;
import com.doubleclue.dcem.core.gui.IPhoto;
import com.doubleclue.dcem.core.jpa.VariableType;
import com.doubleclue.dcem.core.utils.compare.DcemCompare;
import com.doubleclue.dcem.skills.entities.enums.AvailabilityStatus;

@NamedEntityGraphs({ 
	@NamedEntityGraph(name = SkillsUserEntity.GRAPH_DCEMUSEREXT, attributeNodes = {
		@NamedAttributeNode(value = "dcemUser", subgraph = "subgraph.dcemUser"), }, subgraphs = {
				@NamedSubgraph(name = "subgraph.dcemUser", attributeNodes = { @NamedAttributeNode(value = "dcemUserExt") }) }), 
})

@NamedQueries({ 
	@NamedQuery(name = SkillsUserEntity.DELETE_SKILLSUSER, query = "DELETE  FROM SkillsUserEntity su WHERE su.dcemUser = ?1"),
	@NamedQuery(name = SkillsUserEntity.REMOVE_USER_FROM_REPORTS_TO_FROM_SKILLSUSERS, query = "UPDATE SkillsUserEntity su SET su.reportsTo= null WHERE su.reportsTo = ?1"),
	@NamedQuery(name = SkillsUserEntity.GET_ALL_USERS_BY_JOBPROFILES, query = "SELECT su FROM SkillsUserEntity su JOIN su.userJobProfiles ujp WHERE ujp.jobProfile IN ?1"),
	@NamedQuery(name = SkillsUserEntity.GET_COMPLETE_SKILLSUSER, query = "SELECT su.dcemUser.loginId FROM SkillsUserEntity su WHERE su.dcemUser.loginId LIKE LOWER(?1) ORDER BY su.dcemUser.loginId ASC"),
	@NamedQuery(name = SkillsUserEntity.GET_USER_CERTIFICATES_BY_NAME_FILTERED, query = "SELECT DISTINCT su as user, sc.name as certificate "
			+ "FROM SkillsUserEntity su " 
			+ "LEFT JOIN su.certificates suc " 
			+ "LEFT JOIN suc.skillsCertificateEntity sc "
			+ "WHERE LOWER(sc.name) LIKE lower(?1)"),
	@NamedQuery(name = SkillsUserEntity.GET_USER_JOBPROFILES_BY_NAME_FILTERED, query = "SELECT DISTINCT su as user, jp.name as jobProfileName, uj.match as match "
			+ "FROM SkillsUserEntity su " 
			+ "LEFT JOIN su.userJobProfiles uj " 
			+ "LEFT JOIN uj.jobProfile jp "
			+ "WHERE LOWER(jp.name) LIKE lower(?1) AND uj.match >= ?2"),
	@NamedQuery(name = SkillsUserEntity.REMOVE_ALL_SKILLS_FROM_USER, query = "DELETE FROM SkillsUserSkillEntity us WHERE us.skillsUserEntity.id = ?1 "),
	@NamedQuery(name = SkillsUserEntity.REMOVE_ALL_CERTIFICATES_FROM_USER, query = "DELETE FROM SkillsUserCertificateEntity uc WHERE uc.skillsUserEntity.id = ?1 "),
	@NamedQuery(name = SkillsUserEntity.REMOVE_ALL_JOBPROFILES_FROM_USER, query = "DELETE FROM SkillsUserJobProfileEntity ujp WHERE ujp.skillsUserEntity.id = ?1"),
	@NamedQuery(name = SkillsUserEntity.GET_RECIPIENTS_FOR_REQUESTS, query = "Select su.dcemUser FROM SkillsUserEntity su WHERE su.receiveRequests = true and su.disableNotifications = false "),
})

@SuppressWarnings("serial")
@Entity
@Table(name = "skills_skills_user")

public class SkillsUserEntity extends EntityInterface implements Serializable, IPhoto {

	public static final String GRAPH_DCEMUSEREXT = "SkillsUserEntity.graphDcemUserExt";
	public static final String DELETE_SKILLSUSER = "SkillsUserEntity.deleteSkillsUser";
	public static final String GET_ALL_USERS_BY_JOBPROFILES = "SkillsUserEntity.getAllUsersByJobProfiles";
	public static final String GET_COMPLETE_SKILLSUSER = "SkillsUserEntity.getCompleteSkillsUser";
	public static final String GET_USER_CERTIFICATES_BY_NAME_FILTERED = "SkillsUserEntity.getUserCertificatesByNameSkill";
	public static final String GET_USER_JOBPROFILES_BY_NAME_FILTERED = "SkillsUserEntity.getUserJobProfilesByNameSkill";
	public static final String REMOVE_ALL_SKILLS_FROM_USER = "SkillsUserEntity.removeAllSkillsFromUser";
	public static final String REMOVE_ALL_CERTIFICATES_FROM_USER = "SkillsUserEntity.removeAllCertificatesFromUser";
	public static final String REMOVE_ALL_JOBPROFILES_FROM_USER = "SkillsUserEntity.removeAllJobprofilesFromUser";
	public static final String REMOVE_USER_FROM_REPORTS_TO_FROM_SKILLSUSERS = "SkillsUserEntity.removeUserFromReportsToFromSkillsuser";
	public static final String GET_RECIPIENTS_FOR_REQUESTS = "SkillsUserEntity.getRecipientsForRequests";

	@DcemGui(visible = false)
	@Id
	@DcemCompare (ignore = true)
	private Integer id;

	@DcemGui(name = "Photo", subClass = "photo", variableType = VariableType.IMAGE)
	@Transient
	@DcemCompare (ignore = true)
	byte[] photo;

	@MapsId
	@OneToOne
	@JoinColumn(name = "skills_user_id", referencedColumnName = "dc_id", foreignKey = @ForeignKey(name = "FK_SKILLS_USER"), nullable = false, insertable = true, updatable = false)
	@DcemGui(subClass = "displayName", sortOrder = SortOrder.ASCENDING)
	@DcemCompare (ignore = true)
	private DcemUser dcemUser;
	
	@DcemGui(subClass = "skill", variableType = VariableType.LIST)
	@OneToMany(mappedBy = "skillsUserEntity", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<SkillsUserSkillEntity> skills;

	@DcemGui(subClass = "jobProfile", name = "JobProfiles", variableType = VariableType.LIST)
	@OneToMany(mappedBy = "skillsUserEntity", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<SkillsUserJobProfileEntity> userJobProfiles;

	@DcemGui(subClass = "skillsCertificateEntity", variableType = VariableType.LIST)
	@OneToMany(mappedBy = "skillsUserEntity", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<SkillsUserCertificateEntity> certificates;
	
	@DcemGui
	@Enumerated(EnumType.ORDINAL)
	@Column(name = "dc_availability", nullable = false)
	private AvailabilityStatus availability = AvailabilityStatus.Available;
	
	@DcemGui ()
	@Column (nullable = true)
	private LocalDate availableFrom;
	
	@ManyToOne
	@JoinColumn(name = "skills_user_report_id", referencedColumnName = "dc_id", foreignKey = @ForeignKey(name = "FK_SKILLS_REPORT_USER"), nullable = true, insertable = true, updatable = true)
	@DcemGui(subClass = "displayName", visible = false)
	@DcemCompare (ignore = true)
	private DcemUser reportsTo;
	
	@DcemGui (visible = false)
	@Column (nullable = true)
	@DcemCompare (withoutResult = true)
	private String description;
	
	@DcemGui
	@Column(nullable = false)
	private boolean disableNotifications = false;
	
	@DcemGui
	@Column(nullable = false)
	private boolean receiveRequests = false;
	

	public SkillsUserEntity() {
	}
	
	public SkillsUserEntity(DcemUser dcemUser) {
		setDcemUser(dcemUser);
		this.skills = new ArrayList<SkillsUserSkillEntity>();
	}
	

	public SkillsUserEntity(DcemUser dcemUser, List<SkillsUserSkillEntity> skills) {
		setDcemUser(dcemUser);
		this.skills = skills;
	}
	
	public SkillsUserEntity(List<SkillsUserSkillEntity> skills, List<SkillsUserJobProfileEntity> userJobProfiles, List<SkillsUserCertificateEntity> certificates) {
		this.skills = skills;
		this.userJobProfiles = userJobProfiles;
		this.certificates = certificates;
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

	public DcemUser getDcemUser() {
		return dcemUser;
	}

	public void setDcemUser(DcemUser dcemUser) {
		if (dcemUser != null) {
			id = dcemUser.getId();
		}
		this.dcemUser = dcemUser;
	}
	
	@Override
	public String toString() {
		if (dcemUser != null) {
			return dcemUser.getDisplayName();
		}
		return "SkillsUserEntity without dcemuser";
	}

	public List<SkillsUserSkillEntity> getSkills() {
		if (skills == null) {
			skills = new ArrayList<SkillsUserSkillEntity>();
		}
		return skills;
	}

	public void setSkills(List<SkillsUserSkillEntity> skills) {
		this.skills = skills;
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
		SkillsUserEntity other = (SkillsUserEntity) obj;
		return Objects.equals(id, other.getId());
	}

	public byte[] getPhoto() {
		return dcemUser.getPhoto();
	}

	public void setPhoto(byte[] photo) {
		this.photo = photo;
	}

	public List<SkillsUserCertificateEntity> getCertificates() {
		if (certificates == null) {
			certificates = new ArrayList<SkillsUserCertificateEntity>();
		}
		return certificates;
	}

	public void setCertificates(List<SkillsUserCertificateEntity> certificates) {
		this.certificates = certificates;
	}

	public List<SkillsUserJobProfileEntity> getUserJobProfiles() {
		if (userJobProfiles == null) {
			userJobProfiles = new ArrayList<SkillsUserJobProfileEntity>();
		}
		return userJobProfiles;
	}

	public void setUserJobProfiles(List<SkillsUserJobProfileEntity> userJobProfiles) {
		this.userJobProfiles = userJobProfiles;
	}

	public LocalDate getAvailableFrom() {
		return availableFrom;
	}

	public void setAvailableFrom(LocalDate availableFrom) {
		this.availableFrom = availableFrom;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public DcemUser getReportsTo() {
		return reportsTo;
	}

	public void setReportsTo(DcemUser reportsTo) {
		this.reportsTo = reportsTo;
	}

	public AvailabilityStatus getAvailability() {
		return availability;
	}

	public void setAvailability(AvailabilityStatus availability) {
		this.availability = availability;
	}

	public boolean isDisableNotifications() {
		return disableNotifications;
	}

	public void setDisableNotifications(boolean disableNotifications) {
		this.disableNotifications = disableNotifications;
	}

	public boolean getReceiveRequests() {
		return receiveRequests;
	}

	public void setReceiveRequests(boolean receiveRequests) {
		this.receiveRequests = receiveRequests;
	}
}
