package org.openmrs.module.eptssync.model.pojo.generic_src;

import org.openmrs.module.eptssync.model.pojo.generic.*; 
 
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.eptssync.utilities.AttDefinedElements; 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class VisitVO extends AbstractDatabaseObject implements DatabaseObject { 
	private Integer visitId;
	private Integer patientId;
	private Integer visitTypeId;
	private java.util.Date dateStarted;
	private java.util.Date dateStopped;
	private Integer indicationConceptId;
	private Integer locationId;
	private Integer creator;
	private Integer changedBy;
	private byte voided;
	private Integer voidedBy;
	private java.util.Date dateVoided;
	private String voidReason;
 
	public VisitVO() { 
		this.metadata = false;
	} 
 
	public void setVisitId(Integer visitId){ 
	 	this.visitId = visitId;
	}
 
	public Integer getVisitId(){ 
		return this.visitId;
	}
 
	public void setPatientId(Integer patientId){ 
	 	this.patientId = patientId;
	}
 
	public Integer getPatientId(){ 
		return this.patientId;
	}
 
	public void setVisitTypeId(Integer visitTypeId){ 
	 	this.visitTypeId = visitTypeId;
	}
 
	public Integer getVisitTypeId(){ 
		return this.visitTypeId;
	}
 
	public void setDateStarted(java.util.Date dateStarted){ 
	 	this.dateStarted = dateStarted;
	}
 
	public java.util.Date getDateStarted(){ 
		return this.dateStarted;
	}
 
	public void setDateStopped(java.util.Date dateStopped){ 
	 	this.dateStopped = dateStopped;
	}
 
	public java.util.Date getDateStopped(){ 
		return this.dateStopped;
	}
 
	public void setIndicationConceptId(Integer indicationConceptId){ 
	 	this.indicationConceptId = indicationConceptId;
	}
 
	public Integer getIndicationConceptId(){ 
		return this.indicationConceptId;
	}
 
	public void setLocationId(Integer locationId){ 
	 	this.locationId = locationId;
	}
 
	public Integer getLocationId(){ 
		return this.locationId;
	}
 
	public void setCreator(Integer creator){ 
	 	this.creator = creator;
	}
 
	public Integer getCreator(){ 
		return this.creator;
	}
 
	public void setChangedBy(Integer changedBy){ 
	 	this.changedBy = changedBy;
	}
 
	public Integer getChangedBy(){ 
		return this.changedBy;
	}
 
	public void setVoided(byte voided){ 
	 	this.voided = voided;
	}
 
	public byte getVoided(){ 
		return this.voided;
	}
 
	public void setVoidedBy(Integer voidedBy){ 
	 	this.voidedBy = voidedBy;
	}
 
	public Integer getVoidedBy(){ 
		return this.voidedBy;
	}
 
	public void setDateVoided(java.util.Date dateVoided){ 
	 	this.dateVoided = dateVoided;
	}
 
	public java.util.Date getDateVoided(){ 
		return this.dateVoided;
	}
 
	public void setVoidReason(String voidReason){ 
	 	this.voidReason = voidReason;
	}
 
	public String getVoidReason(){ 
		return this.voidReason;
	}
 

 
	public Integer getObjectId() { 
 		return this.visitId; 
	} 
 
	public void setObjectId(Integer selfId){ 
		this.visitId = selfId; 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		super.load(rs);
		if (rs.getObject("visit_id") != null) this.visitId = rs.getInt("visit_id");
		if (rs.getObject("patient_id") != null) this.patientId = rs.getInt("patient_id");
		if (rs.getObject("visit_type_id") != null) this.visitTypeId = rs.getInt("visit_type_id");
		this.dateStarted =  rs.getTimestamp("date_started") != null ? new java.util.Date( rs.getTimestamp("date_started").getTime() ) : null;
		this.dateStopped =  rs.getTimestamp("date_stopped") != null ? new java.util.Date( rs.getTimestamp("date_stopped").getTime() ) : null;
		if (rs.getObject("indication_concept_id") != null) this.indicationConceptId = rs.getInt("indication_concept_id");
		if (rs.getObject("location_id") != null) this.locationId = rs.getInt("location_id");
		if (rs.getObject("creator") != null) this.creator = rs.getInt("creator");
		this.dateCreated =  rs.getTimestamp("date_created") != null ? new java.util.Date( rs.getTimestamp("date_created").getTime() ) : null;
		if (rs.getObject("changed_by") != null) this.changedBy = rs.getInt("changed_by");
		this.dateChanged =  rs.getTimestamp("date_changed") != null ? new java.util.Date( rs.getTimestamp("date_changed").getTime() ) : null;
		this.voided = rs.getByte("voided");
		if (rs.getObject("voided_by") != null) this.voidedBy = rs.getInt("voided_by");
		this.dateVoided =  rs.getTimestamp("date_voided") != null ? new java.util.Date( rs.getTimestamp("date_voided").getTime() ) : null;
		this.voidReason = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("void_reason") != null ? rs.getString("void_reason").trim() : null);
		this.uuid = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("uuid") != null ? rs.getString("uuid").trim() : null);
	} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "visit_id"; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithoutObjectId(){ 
 		return "INSERT INTO visit(patient_id, visit_type_id, date_started, date_stopped, indication_concept_id, location_id, creator, date_created, changed_by, date_changed, voided, voided_by, date_voided, void_reason, uuid) VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithoutObjectId(){ 
 		Object[] params = {this.patientId, this.visitTypeId, this.dateStarted, this.dateStopped, this.indicationConceptId, this.locationId, this.creator, this.dateCreated, this.changedBy, this.dateChanged, this.voided, this.voidedBy, this.dateVoided, this.voidReason, this.uuid};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithObjectId(){ 
 		return "INSERT INTO visit(visit_id, patient_id, visit_type_id, date_started, date_stopped, indication_concept_id, location_id, creator, date_created, changed_by, date_changed, voided, voided_by, date_voided, void_reason, uuid) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithObjectId(){ 
 		Object[] params = {this.visitId, this.patientId, this.visitTypeId, this.dateStarted, this.dateStopped, this.indicationConceptId, this.locationId, this.creator, this.dateCreated, this.changedBy, this.dateChanged, this.voided, this.voidedBy, this.dateVoided, this.voidReason, this.uuid};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.patientId, this.visitTypeId, this.dateStarted, this.dateStopped, this.indicationConceptId, this.locationId, this.creator, this.dateCreated, this.changedBy, this.dateChanged, this.voided, this.voidedBy, this.dateVoided, this.voidReason, this.uuid, this.visitId};		return params; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE visit SET patient_id = ?, visit_type_id = ?, date_started = ?, date_stopped = ?, indication_concept_id = ?, location_id = ?, creator = ?, date_created = ?, changed_by = ?, date_changed = ?, voided = ?, voided_by = ?, date_voided = ?, void_reason = ?, uuid = ? WHERE visit_id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.patientId) + "," + (this.visitTypeId) + "," + (this.dateStarted != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateStarted)  +"\"" : null) + "," + (this.dateStopped != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateStopped)  +"\"" : null) + "," + (this.indicationConceptId) + "," + (this.locationId) + "," + (this.creator) + "," + (this.dateCreated != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateCreated)  +"\"" : null) + "," + (this.changedBy) + "," + (this.dateChanged != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateChanged)  +"\"" : null) + "," + (this.voided) + "," + (this.voidedBy) + "," + (this.dateVoided != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateVoided)  +"\"" : null) + "," + (this.voidReason != null ? "\""+ utilities.scapeQuotationMarks(voidReason)  +"\"" : null) + "," + (this.uuid != null ? "\""+ utilities.scapeQuotationMarks(uuid)  +"\"" : null); 
	} 
 
	@Override
	public boolean hasParents() {
		if (this.changedBy != 0) return true;

		if (this.creator != 0) return true;

		if (this.indicationConceptId != 0) return true;

		if (this.locationId != 0) return true;

		if (this.patientId != 0) return true;

		if (this.visitTypeId != 0) return true;

		if (this.voidedBy != 0) return true;

		return false;
	}

	@Override
	public Integer getParentValue(String parentAttName) {		
		if (parentAttName.equals("changedBy")) return this.changedBy;		
		if (parentAttName.equals("creator")) return this.creator;		
		if (parentAttName.equals("indicationConceptId")) return this.indicationConceptId;		
		if (parentAttName.equals("locationId")) return this.locationId;		
		if (parentAttName.equals("patientId")) return this.patientId;		
		if (parentAttName.equals("visitTypeId")) return this.visitTypeId;		
		if (parentAttName.equals("voidedBy")) return this.voidedBy;

		throw new RuntimeException("No found parent for: " + parentAttName);	}

	@Override
	public void changeParentValue(String parentAttName, DatabaseObject newParent) {		
		if (parentAttName.equals("changedBy")) {
			this.changedBy = newParent.getObjectId();
			return;
		}		
		if (parentAttName.equals("creator")) {
			this.creator = newParent.getObjectId();
			return;
		}		
		if (parentAttName.equals("indicationConceptId")) {
			this.indicationConceptId = newParent.getObjectId();
			return;
		}		
		if (parentAttName.equals("locationId")) {
			this.locationId = newParent.getObjectId();
			return;
		}		
		if (parentAttName.equals("patientId")) {
			this.patientId = newParent.getObjectId();
			return;
		}		
		if (parentAttName.equals("visitTypeId")) {
			this.visitTypeId = newParent.getObjectId();
			return;
		}		
		if (parentAttName.equals("voidedBy")) {
			this.voidedBy = newParent.getObjectId();
			return;
		}

		throw new RuntimeException("No found parent for: " + parentAttName);
	}

	@Override
	public void setParentToNull(String parentAttName) {		
		if (parentAttName.equals("changedBy")) {
			this.changedBy = null;
			return;
		}		
		if (parentAttName.equals("creator")) {
			this.creator = null;
			return;
		}		
		if (parentAttName.equals("indicationConceptId")) {
			this.indicationConceptId = null;
			return;
		}		
		if (parentAttName.equals("locationId")) {
			this.locationId = null;
			return;
		}		
		if (parentAttName.equals("patientId")) {
			this.patientId = null;
			return;
		}		
		if (parentAttName.equals("visitTypeId")) {
			this.visitTypeId = null;
			return;
		}		
		if (parentAttName.equals("voidedBy")) {
			this.voidedBy = null;
			return;
		}

		throw new RuntimeException("No found parent for: " + parentAttName);
	}


}