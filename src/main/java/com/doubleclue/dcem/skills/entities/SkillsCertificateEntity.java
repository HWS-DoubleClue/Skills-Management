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
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Size;

import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.entities.EntityInterface;
import com.doubleclue.dcem.core.gui.DcemGui;
import com.doubleclue.dcem.core.jpa.VariableType;
import com.doubleclue.dcem.skills.entities.enums.ApprovalStatus;

@NamedQueries({
		@NamedQuery(name = SkillsCertificateEntity.GET_ALL_CERTIFICATES_BY_SKILLS, query = "SELECT DISTINCT sc FROM SkillsCertificateEntity sc LEFT JOIN sc.appliesForSkills skill WHERE skill IN ?1 "),
		@NamedQuery(name = SkillsCertificateEntity.GET_BY_NAME, query = "SELECT sc FROM SkillsCertificateEntity sc WHERE sc.name = (?1)"),
		@NamedQuery(name = SkillsCertificateEntity.GET_SKILL_AND_CERTIFICATE_COUNT_BY_SKILLS, query = "SELECT skill.id as id, COUNT(distinct cert) as count FROM SkillsCertificateEntity cert LEFT JOIN cert.appliesForSkills skill WHERE skill in ?1 GROUP BY skill.id"), })

@Entity
@Table(name = "skills_certificate", uniqueConstraints = @UniqueConstraint(name = "UK_CERTIFICATE_NAME", columnNames = { "certificate_name" }))
public class SkillsCertificateEntity extends EntityInterface implements Comparable<SkillsCertificateEntity> {

	public static final String GET_ALL_CERTIFICATES_BY_SKILLS = "skills_certificate.getAllCertificateBySkill";
	public static final String GET_BY_NAME = "skills_certificate.getByName";
	public static final String GET_SKILL_AND_CERTIFICATE_COUNT_BY_SKILLS = "skills_certificate.getSkillAndCertificateCountBySkill";

	@Id
	@Column(name = "skills_certificate_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@DcemGui
	@Size(max = 255)
	@Column(name = "certificate_name", nullable = false)
	private String name;

	@DcemGui
	@Size(max = 255)
	@Column(nullable = true, name = "description")
	private String description;

	@DcemGui(subClass = "name")
	@ManyToOne
	@JoinColumn(referencedColumnName = "skills_issuer_id", foreignKey = @ForeignKey(name = "FK_CERTIFICATE_ISSUER"), name = "issuer_id", nullable = true, insertable = true, updatable = true)
	private SkillsIssuerEntity issuer;

	@DcemGui(subClass = "name", variableType = VariableType.LIST)
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "skills_certificate_skills", joinColumns = @JoinColumn(name = "certificate_id"), foreignKey = @ForeignKey(name = "FK_SKILLS_CERTIFICATE"), inverseJoinColumns = @JoinColumn(name = "skills_id"), inverseForeignKey = @ForeignKey(name = "FK_CERTIFICATE_SKILLS"))
	private List<SkillsEntity> appliesForSkills;

	@DcemGui
	@Enumerated(EnumType.ORDINAL)
	@Column(name = "dc_status", nullable = false)
	private ApprovalStatus approvalStatus;

	@DcemGui
	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn(name = "requested_from_id", referencedColumnName = "dc_id", foreignKey = @ForeignKey(name = "FK_CERTIFICATES_REQUESTEDFROM"), insertable = true)
	private DcemUser requestedFrom;

	public SkillsCertificateEntity() {
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

	public SkillsCertificateEntity(String name) {
		this.name = name;
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

	public SkillsIssuerEntity getIssuer() {
		return issuer;
	}

	public void setIssuer(SkillsIssuerEntity issuer) {
		this.issuer = issuer;
	}

	public List<SkillsEntity> getAppliesForSkills() {
		if (appliesForSkills == null) {
			return new ArrayList<SkillsEntity>();
		}
		return appliesForSkills;
	}

	public void setAppliesForSkills(List<SkillsEntity> appliesForSkills) {
		this.appliesForSkills = appliesForSkills;
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
		SkillsCertificateEntity other = (SkillsCertificateEntity) obj;
		return Objects.equals(id, other.getId());
	}

	@Override
	public String toString() {
		return name;
	}

	public ApprovalStatus getApprovalStatus() {
		return approvalStatus;
	}

	public void setApprovalStatus(ApprovalStatus approvalStatus) {
		this.approvalStatus = approvalStatus;
	}

	@Override
	public int compareTo(SkillsCertificateEntity certificateEntity) {
		return this.name.compareTo(certificateEntity.getName());
	}

	public DcemUser getRequestedFrom() {
		return requestedFrom;
	}

	public void setRequestedFrom(DcemUser requestedFrom) {
		this.requestedFrom = requestedFrom;
	}
}
