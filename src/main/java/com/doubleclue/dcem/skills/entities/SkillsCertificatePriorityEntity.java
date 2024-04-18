package com.doubleclue.dcem.skills.entities;

import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
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

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.entities.EntityInterface;
import com.doubleclue.dcem.core.gui.DcemGui;

@Entity
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Table(name = "skills_cert_Priority", uniqueConstraints = @UniqueConstraint(name = "UK_SKILLS_CERTIFICATE_PRIORITY", columnNames = { "dc_cert", "dc_priority" }))
@NamedQueries({ 
	@NamedQuery(name = SkillsCertificatePriorityEntity.GET_CERTIFICATE_PRIORITY, query = "SELECT sl FROM SkillsCertificatePriorityEntity sl WHERE sl.certificateEntity = ?1  AND sl.priority = ?2"),
})
public class SkillsCertificatePriorityEntity extends EntityInterface implements Comparable<SkillsCertificatePriorityEntity>  {

	public static final String GET_CERTIFICATE_PRIORITY = "certificatePriority.CertificatePriority";

	@Id
	@Column(name = "dc_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@DcemGui(subClass = "name")
	@ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.REFRESH)
	@JoinColumn(referencedColumnName = "skills_certificate_id", foreignKey = @ForeignKey(name = "FK_SKILLS_CERTIFICATE_PRIO"), name = "dc_cert", nullable = false)
	private SkillsCertificateEntity certificateEntity;

	@Column(name = "dc_priority")
	int priority = 1;

	public SkillsCertificatePriorityEntity() {
	}

	public SkillsCertificatePriorityEntity(SkillsCertificateEntity certificateEntity, int priority) {
		this.certificateEntity = certificateEntity;
		this.priority = priority;
	}

	public SkillsCertificatePriorityEntity(int id, SkillsCertificateEntity certificateEntity, int priority) {
		this.id = id;
		this.certificateEntity = certificateEntity;
		this.priority = priority;
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
	public String toString() {
		return certificateEntity.getName() + DcemConstants.ARROW_UP + priority;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(getCertificateEntity().getId(), priority);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		SkillsCertificatePriorityEntity other = (SkillsCertificatePriorityEntity) obj;
		return (this.getCertificateEntity().getId() == other.getCertificateEntity().getId()) && (priority == other.getPriority());
	}

	@Override
	public int compareTo(SkillsCertificatePriorityEntity skillsLevelEntity) {
		if (skillsLevelEntity.priority == this.getPriority()) {
			return skillsLevelEntity.getCertificateEntity().getName().compareTo(this.getCertificateEntity().getName());
		}
		return skillsLevelEntity.getPriority() - this.getPriority();
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public SkillsCertificateEntity getCertificateEntity() {
		return certificateEntity;
	}

	public void setCertificateEntity(SkillsCertificateEntity certificateEntity) {
		this.certificateEntity = certificateEntity;
	}
}
