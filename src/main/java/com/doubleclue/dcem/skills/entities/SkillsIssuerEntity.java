package com.doubleclue.dcem.skills.entities;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Size;

import com.doubleclue.dcem.core.entities.EntityInterface;
import com.doubleclue.dcem.core.gui.DcemGui;

@Entity
@Table(name = "skills_issuer", uniqueConstraints = @UniqueConstraint(name = "UK_ISSUER_NAME", columnNames = { "issuer_name" }))

@NamedQueries({
	@NamedQuery(name = SkillsIssuerEntity.GET_ISSUER_NAME_BY_NAME, query = "SELECT ie.name FROM SkillsIssuerEntity ie WHERE ie.name LIKE LOWER(?1) ORDER BY ie.name ASC"),
	@NamedQuery(name = SkillsIssuerEntity.GET_BY_NAME, query = "SELECT ie FROM SkillsIssuerEntity ie WHERE ie.name = ?1"),
})

public class SkillsIssuerEntity extends EntityInterface {

	public static final String GET_ISSUER_NAME_BY_NAME = "skillsissuer.getIssuerByName";
	public static final String GET_BY_NAME = "skillsissuer.getByIssuerName";

	public SkillsIssuerEntity() {
	}

	@Id
	@Column(name = "skills_issuer_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@DcemGui
	@Size(max = 255)
	@Column(name = "issuer_name", nullable = false)
	private String name;
	
	@DcemGui (visible = false)
	@Size(max = 255)
	@Column(nullable = true)
	private String address;

	@DcemGui 
	@Size(max = 63)
	@Column(nullable = false)
	private String country;

	@DcemGui
	@Size(max = 63)
	@Column
	private String homepage;

	@DcemGui (visible = false)
	@Size(max = 255)
	@Column(nullable = true)
	private String accreditation;

	@DcemGui 
	@Size(max = 255)
	@Column(name = "contact_person", nullable = true)
	private String contactPerson;

	@DcemGui (visible = false)
	@Size(max = 255)
	@Column(name = "contact_email", nullable = true)
	private String contactEmail;

	@DcemGui (visible = false)
	@Size(max = 255)
	@Column (nullable = true)
	private String comment;

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

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getHomepage() {
		return homepage;
	}

	public void setHomepage(String homepage) {
		this.homepage = homepage;
	}

	public String getContactPerson() {
		return contactPerson;
	}

	public void setContactPerson(String contactPerson) {
		this.contactPerson = contactPerson;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	@Override
	public String toString() {
		return name;
	}

	public String getAccreditation() {
		return accreditation;
	}

	public void setAccreditation(String accreditation) {
		this.accreditation = accreditation;
	}

	public String getContactEmail() {
		return contactEmail;
	}

	public void setContactEmail(String contactEmail) {
		this.contactEmail = contactEmail;
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
		SkillsIssuerEntity other = (SkillsIssuerEntity) obj;
		return Objects.equals(id, other.getId());
	}
}
