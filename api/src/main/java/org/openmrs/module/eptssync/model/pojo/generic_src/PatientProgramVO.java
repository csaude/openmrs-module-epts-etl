package org.openmrs.module.eptssync.model.pojo.generic_src;

import org.openmrs.module.eptssync.model.pojo.generic.*; 
 
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.eptssync.utilities.AttDefinedElements; 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class PatientProgramVO extends AbstractDatabaseObject implements DatabaseObject { 
	private Integer patientProgramId;
	private Integer patientId;
	private Integer programId;
	private java.util.Date dateEnrolled;
	private java.util.Date dateCompleted;
	private Integer creator;
	private Integer changedBy;
	private byte voided;
	private Integer voidedBy;
	private java.util.Date dateVoided;
	private String voidReason;
	private Integer locationId;
	private Integer outcomeConceptId;
 
	public PatientProgramVO() { 
		this.metadata = false;
	} 
 
	public void setPatientProgramId(Integer patientProgramId){ 
	 	this.patientProgramId = patientProgramId;
	}
 
	public Integer getPatientProgramId(){ 
		return this.patientProgramId;
	}
 
	public void setPatientId(Integer patientId){ 
	 	this.patientId = patientId;
	}
 
	public Integer getPatientId(){ 
		return this.patientId;
	}
 
	public void setProgramId(Integer programId){ 
	 	this.programId = programId;
	}
 
	public Integer getProgramId(){ 
		return this.programId;
	}
 
	public void setDateEnrolled(java.util.Date dateEnrolled){ 
	 	this.dateEnrolled = dateEnrolled;
	}
 
	public java.util.Date getDateEnrolled(){ 
		return this.dateEnrolled;
	}
 
	public void setDateCompleted(java.util.Date dateCompleted){ 
	 	this.dateCompleted = dateCompleted;
	}
 
	public java.util.Date getDateCompleted(){ 
		return this.dateCompleted;
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
 
	public void setLocationId(Integer locationId){ 
	 	this.locationId = locationId;
	}
 
	public Integer getLocationId(){ 
		return this.locationId;
	}
 
	public void setOutcomeConceptId(Integer outcomeConceptId){ 
	 	this.outcomeConceptId = outcomeConceptId;
	}


 
	public Integer getOutcomeConceptId(){ 
		return this.outcomeConceptId;
	}
 
	public Integer getObjectId() { 
 		return this.patientProgramId; 
	} 
 
	public void setObjectId(Integer selfId){ 
		this.patientProgramId = selfId; 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		super.load(rs);
		if (rs.getObject("patient_program_id") != null) this.patientProgramId = rs.getInt("patient_program_id");
		if (rs.getObject("patient_id") != null) this.patientId = rs.getInt("patient_id");
		if (rs.getObject("program_id") != null) this.programId = rs.getInt("program_id");
		this.dateEnrolled =  rs.getTimestamp("date_enrolled") != null ? new java.util.Date( rs.getTimestamp("date_enrolled").getTime() ) : null;
		this.dateCompleted =  rs.getTimestamp("date_completed") != null ? new java.util.Date( rs.getTimestamp("date_completed").getTime() ) : null;
		if (rs.getObject("creator") != null) this.creator = rs.getInt("creator");
		this.dateCreated =  rs.getTimestamp("date_created") != null ? new java.util.Date( rs.getTimestamp("date_created").getTime() ) : null;
		if (rs.getObject("changed_by") != null) this.changedBy = rs.getInt("changed_by");
		this.dateChanged =  rs.getTimestamp("date_changed") != null ? new java.util.Date( rs.getTimestamp("date_changed").getTime() ) : null;
		this.voided = rs.getByte("voided");
		if (rs.getObject("voided_by") != null) this.voidedBy = rs.getInt("voided_by");
		this.dateVoided =  rs.getTimestamp("date_voided") != null ? new java.util.Date( rs.getTimestamp("date_voided").getTime() ) : null;
		this.voidReason = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("void_reason") != null ? rs.getString("void_reason").trim() : null);
		this.uuid = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("uuid") != null ? rs.getString("uuid").trim() : null);
		if (rs.getObject("location_id") != null) this.locationId = rs.getInt("location_id");
		if (rs.getObject("outcome_concept_id") != null) this.outcomeConceptId = rs.getInt("outcome_concept_id");
	} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "patient_program_id"; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithoutObjectId(){ 
 		return "INSERT INTO patient_program(patient_id, program_id, date_enrolled, date_completed, creator, date_created, changed_by, date_changed, voided, voided_by, date_voided, void_reason, uuid, location_id, outcome_concept_id) VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithoutObjectId(){ 
 		Object[] params = {this.patientId, this.programId, this.dateEnrolled, this.dateCompleted, this.creator, this.dateCreated, this.changedBy, this.dateChanged, this.voided, this.voidedBy, this.dateVoided, this.voidReason, this.uuid, this.locationId, this.outcomeConceptId};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithObjectId(){ 
 		return "INSERT INTO patient_program(patient_program_id, patient_id, program_id, date_enrolled, date_completed, creator, date_created, changed_by, date_changed, voided, voided_by, date_voided, void_reason, uuid, location_id, outcome_concept_id) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithObjectId(){ 
 		Object[] params = {this.patientProgramId, this.patientId, this.programId, this.dateEnrolled, this.dateCompleted, this.creator, this.dateCreated, this.changedBy, this.dateChanged, this.voided, this.voidedBy, this.dateVoided, this.voidReason, this.uuid, this.locationId, this.outcomeConceptId};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.patientId, this.programId, this.dateEnrolled, this.dateCompleted, this.creator, this.dateCreated, this.changedBy, this.dateChanged, this.voided, this.voidedBy, this.dateVoided, this.voidReason, this.uuid, this.locationId, this.outcomeConceptId, this.patientProgramId};		return params; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE patient_program SET patient_id = ?, program_id = ?, date_enrolled = ?, date_completed = ?, creator = ?, date_created = ?, changed_by = ?, date_changed = ?, voided = ?, voided_by = ?, date_voided = ?, void_reason = ?, uuid = ?, location_id = ?, outcome_concept_id = ? WHERE patient_program_id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.patientId) + "," + (this.programId) + "," + (this.dateEnrolled != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateEnrolled)  +"\"" : null) + "," + (this.dateCompleted != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateCompleted)  +"\"" : null) + "," + (this.creator) + "," + (this.dateCreated != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateCreated)  +"\"" : null) + "," + (this.changedBy) + "," + (this.dateChanged != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateChanged)  +"\"" : null) + "," + (this.voided) + "," + (this.voidedBy) + "," + (this.dateVoided != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateVoided)  +"\"" : null) + "," + (this.voidReason != null ? "\""+ utilities.scapeQuotationMarks(voidReason)  +"\"" : null) + "," + (this.uuid != null ? "\""+ utilities.scapeQuotationMarks(uuid)  +"\"" : null) + "," + (this.locationId) + "," + (this.outcomeConceptId); 
	} 
 
	@Override
	public boolean hasParents() {
		if (this.patientId != 0) return true;

		if (this.creator != 0) return true;

		if (this.locationId != 0) return true;

		if (this.outcomeConceptId != 0) return true;

		if (this.programId != 0) return true;

		if (this.changedBy != 0) return true;

		if (this.voidedBy != 0) return true;

		return false;
	}

	@Override
	public Integer getParentValue(String parentAttName) {		
		if (parentAttName.equals("patientId")) return this.patientId;		
		if (parentAttName.equals("creator")) return this.creator;		
		if (parentAttName.equals("locationId")) return this.locationId;		
		if (parentAttName.equals("outcomeConceptId")) return this.outcomeConceptId;		
		if (parentAttName.equals("programId")) return this.programId;		
		if (parentAttName.equals("changedBy")) return this.changedBy;		
		if (parentAttName.equals("voidedBy")) return this.voidedBy;

		throw new RuntimeException("No found parent for: " + parentAttName);	}

	@Override
	public void changeParentValue(String parentAttName, DatabaseObject newParent) {		
		if (parentAttName.equals("patientId")) {
			this.patientId = newParent.getObjectId();
			return;
		}		
		if (parentAttName.equals("creator")) {
			this.creator = newParent.getObjectId();
			return;
		}		
		if (parentAttName.equals("locationId")) {
			this.locationId = newParent.getObjectId();
			return;
		}		
		if (parentAttName.equals("outcomeConceptId")) {
			this.outcomeConceptId = newParent.getObjectId();
			return;
		}		
		if (parentAttName.equals("programId")) {
			this.programId = newParent.getObjectId();
			return;
		}		
		if (parentAttName.equals("changedBy")) {
			this.changedBy = newParent.getObjectId();
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
		if (parentAttName.equals("patientId")) {
			this.patientId = null;
			return;
		}		
		if (parentAttName.equals("creator")) {
			this.creator = null;
			return;
		}		
		if (parentAttName.equals("locationId")) {
			this.locationId = null;
			return;
		}		
		if (parentAttName.equals("outcomeConceptId")) {
			this.outcomeConceptId = null;
			return;
		}		
		if (parentAttName.equals("programId")) {
			this.programId = null;
			return;
		}		
		if (parentAttName.equals("changedBy")) {
			this.changedBy = null;
			return;
		}		
		if (parentAttName.equals("voidedBy")) {
			this.voidedBy = null;
			return;
		}

		throw new RuntimeException("No found parent for: " + parentAttName);
	}


}