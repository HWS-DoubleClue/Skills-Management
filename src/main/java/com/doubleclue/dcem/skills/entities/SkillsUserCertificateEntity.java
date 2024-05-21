package com.doubleclue.dcem.skills.entities;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
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
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.NamedEntityGraphs;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.Hibernate;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.doubleclue.dcem.as.entities.CloudSafeEntity;
import com.doubleclue.dcem.core.entities.EntityInterface;
import com.doubleclue.dcem.core.gui.DcemGui;
import com.doubleclue.dcem.core.utils.compare.DcemCompare;
import com.doubleclue.dcem.skills.entities.enums.SkillsStatus;

@NamedEntityGraphs({ @NamedEntityGraph(name = SkillsUserCertificateEntity.GRAPH_FILES, attributeNodes = { @NamedAttributeNode(value = "files") }), })

@NamedQueries({
		@NamedQuery(name = SkillsUserCertificateEntity.GET_BY_USER, query = "SELECT uc FROM SkillsUserCertificateEntity uc JOIN FETCH uc.skillsCertificateEntity WHERE uc.skillsUserEntity = ?1"),
		@NamedQuery(name = SkillsUserCertificateEntity.GET_ALL_SOON_EXPIRING_CERTIFICATES, query = "SELECT suc FROM SkillsUserCertificateEntity suc "
				+ "WHERE suc.expirationDate < (?1) AND suc.sendNotification <= (?2)"),
		@NamedQuery(name = SkillsUserCertificateEntity.GET_BY_CERTIFICATE_ID_AND_USER, query = "SELECT suc FROM SkillsUserCertificateEntity suc "
				+ "WHERE suc.skillsUserEntity.id = ?1 AND suc.skillsCertificateEntity.id = ?2 AND suc.status = com.doubleclue.dcem.skills.entities.enums.SkillsStatus.OWNS"),
		@NamedQuery(name = SkillsUserCertificateEntity.GET_TARGET_CERTIFICATES_OF_USER, query = "SELECT suc FROM SkillsUserCertificateEntity suc "
				+ "WHERE suc.skillsUserEntity.id = ?1 AND suc.status != com.doubleclue.dcem.skills.entities.enums.SkillsStatus.OWNS"),
		@NamedQuery(name = SkillsUserCertificateEntity.REMOVE_ALL_CERTIFICATES_FROM_USER, query = "DELETE FROM SkillsUserCertificateEntity suc WHERE suc.skillsUserEntity.id = ?1 "), })

@Entity
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Table(name = "skills_user_certificates", uniqueConstraints = @UniqueConstraint(name = "UK_SKILLS_USERCERTIFICATES", columnNames = { "skills_user_id",
		"skills_certificate_id" }))
public class SkillsUserCertificateEntity extends EntityInterface implements SkillsComparableInterface {

	public static final String GRAPH_FILES = "skillsUserCertificateEntity.graphFiles";
	public static final String GRAPH_CERTIFICATES = "skillsUserCertificateEntity.graphCertificates";
	public static final String GET_BY_USER = "skillsUserCertificateEntity.getByUser";
	public final static String GET_ALL_SOON_EXPIRING_CERTIFICATES = "skillsUserCertificateEntity.getAllSoonExpiringCertificateUser";
	public final static String GET_BY_CERTIFICATE_ID_AND_USER = "skillsUserCertificateEntity.getByCertificateIdAndUser";
	public final static String GET_TARGET_CERTIFICATES_OF_USER = "skillsUserCertificateEntity.getTargetCertificatesOfUser";
	public final static String REMOVE_ALL_CERTIFICATES_FROM_USER = "skillsUserCertificateEntity.removeAllCertificatesFromUser";

	@Id
	@Column(name = "skills_user_certificate_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@ManyToOne
	@JoinColumn(referencedColumnName = "skills_user_id", foreignKey = @ForeignKey(name = "FK_CERTIFICATE_SKILLSUSER"), name = "skills_user_id", nullable = false, insertable = true, updatable = true)
	private SkillsUserEntity skillsUserEntity;

	@DcemGui(subClass = "name")
	@ManyToOne
	@JoinColumn(referencedColumnName = "skills_certificate_id", foreignKey = @ForeignKey(name = "FK_CERTIFICATE_USERCERTIFICATE"), name = "skills_certificate_id", nullable = false, insertable = true, updatable = true)
	private SkillsCertificateEntity skillsCertificateEntity;

	@DcemCompare(ignore = true)
	@ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "skills_certificate_files", joinColumns = @JoinColumn(name = "certificate_id"), foreignKey = @ForeignKey(name = "FK_FILES_CERTIFICATE"), inverseJoinColumns = @JoinColumn(name = "file_id"), inverseForeignKey = @ForeignKey(name = "FK_CERTIFICATE_FILE"))
	private List<CloudSafeEntity> files;

	@DcemGui
	@Enumerated(EnumType.ORDINAL)
	@Column(name = "certificate_status", nullable = false)
	private SkillsStatus status;

	@Column(name = "certificate_date")
	private LocalDate date;

	@Column(name = "expiration_date")
	private LocalDate expirationDate;

	@Column(name = "comment")
	private String comment;

	@Column
	private Integer sendNotification = 0;

	public SkillsUserCertificateEntity() {
	}

	public SkillsUserCertificateEntity(SkillsUserEntity skillsUserEntity, SkillsCertificateEntity skillsCertificateEntity, SkillsStatus status) {
		this.skillsUserEntity = skillsUserEntity;
		this.skillsCertificateEntity = skillsCertificateEntity;
		this.status = status;
	}

	public String getFormattedDate(String language) {
		return Objects.isNull(date) == true ? "" : DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(new Locale(language)).format(date);
	}

	public String getFormattedExpirationDate(String language) {
		return Objects.isNull(expirationDate) == true ? ""
				: DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(new Locale(language)).format(expirationDate);
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

	public boolean isCertificateNotOwned() {
		if (status == null || status.equals(SkillsStatus.OWNS) == false) {
			return true;
		}
		return false;
	}

	public SkillsUserEntity getSkillsUserEntity() {
		return skillsUserEntity;
	}

	public void setSkillsUserEntity(SkillsUserEntity skillsUserEntity) {
		this.skillsUserEntity = skillsUserEntity;
	}

	public SkillsCertificateEntity getSkillsCertificateEntity() {
		return skillsCertificateEntity;
	}

	public void setSkillsCertificateEntity(SkillsCertificateEntity skillsCertificateEntity) {
		this.skillsCertificateEntity = skillsCertificateEntity;
	}

	public List<CloudSafeEntity> getFiles() {
		if (files == null) {
			files = new ArrayList<CloudSafeEntity>();
		}
		return files;
	}

	public void setFiles(List<CloudSafeEntity> files) {
		this.files = files;
	}

	public LocalDate getExpirationDate() {
		return expirationDate;
	}

	public void setExpirationDate(LocalDate expirationDate) {
		this.expirationDate = expirationDate;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SkillsUserCertificateEntity other = (SkillsUserCertificateEntity) obj;
		return Objects.equals(id, other.getId());
	}

	@Override
	public String toString() {
		if (status != SkillsStatus.OWNS) {
			return "(" + skillsCertificateEntity.getName() + ")";
		}
		return skillsCertificateEntity.getName();
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
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

	public int getSendNotification() {
		if (sendNotification == null) {
			sendNotification = 0;
		}
		return sendNotification.intValue();
	}

	public void setSendNotification(Integer sendNotification) {
		this.sendNotification = sendNotification;
	}

	@Override
	public boolean compareObject(SkillsComparableInterface entity) {
		if (Hibernate.getClass(this) != Hibernate.getClass(entity)) {
			return false;
		}
		SkillsUserCertificateEntity other = (SkillsUserCertificateEntity) entity;
		if (this.getSkillsUserEntity().equals(other.getSkillsUserEntity()) == false) {
			return false;
		}
		if (this.getSkillsCertificateEntity().equals(other.getSkillsCertificateEntity()) == false) {
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
		if (this.getExpirationDate() == null) {
			if (other.getExpirationDate() != null) {
				return false;
			}
		} else {
			if (this.getExpirationDate().equals(other.getExpirationDate()) == false) {
				return false;
			}
		}
		return true;
	}
}
