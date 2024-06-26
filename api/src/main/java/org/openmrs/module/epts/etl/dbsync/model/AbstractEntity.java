package org.openmrs.module.epts.etl.dbsync.model;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

public abstract class AbstractEntity implements Serializable {
	
	private static final long serialVersionUID = 2264380900101137549L;
	
	private Long id;
	
	private Date dateCreated;
	
	/**
	 * Gets the id
	 *
	 * @return the id
	 */
	public Long getId() {
		return id;
	}
	
	/**
	 * Sets the id
	 *
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}
	
	/**
	 * Gets the dateCreated
	 *
	 * @return the dateCreated
	 */
	public Date getDateCreated() {
		return dateCreated;
	}
	
	/**
	 * Sets the dateCreated
	 *
	 * @param dateCreated the dateCreated to set
	 */
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}
	
	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		
		if (other == null || !getClass().isAssignableFrom(other.getClass())) {
			return false;
		}
		
		AbstractEntity otherObj = (AbstractEntity) other;
		if (getId() == null && otherObj.getId() == null) {
			return super.equals(other);
		}
		
		return Objects.equals(getId(), otherObj.getId());
	}
	
	@Override
	public int hashCode() {
		return getId() != null ? getId().hashCode() : super.hashCode();
	}
	
}
