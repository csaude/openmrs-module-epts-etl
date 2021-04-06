package org.openmrs.module.eptssync.model.pojo.destination; 
 
import org.openmrs.module.eptssync.model.pojo.generic.*; 
 
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.eptssync.utilities.AttDefinedElements; 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class PatientProgramVO extends AbstractOpenMRSObject implements OpenMRSObject { 
	private int patientProgramId;
	private int patientId;
	private int programId;
	private java.util.Date dateEnrolled;
	private java.util.Date dateCompleted;
	private int creator;
	private int changedBy;
	private byte voided;
	private int voidedBy;
	private java.util.Date dateVoided;
	private String voidReason;
	private int locationId;
	private int outcomeConceptId;
 
	public PatientProgramVO() { 
		this.metadata = false;
	} 
 
	public void setPatientProgramId(int patientProgramId){ 
	 	this.patientProgramId = patientProgramId;
	}
 
	public int getPatientProgramId(){ 
		return this.patientProgramId;
	}
 
	public void setPatientId(int patientId){ 
	 	this.patientId = patientId;
	}
 
	public int getPatientId(){ 
		return this.patientId;
	}
 
	public void setProgramId(int programId){ 
	 	this.programId = programId;
	}
 
	public int getProgramId(){ 
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
 
	public void setCreator(int creator){ 
	 	this.creator = creator;
	}
 
	public int getCreator(){ 
		return this.creator;
	}
 
	public void setChangedBy(int changedBy){ 
	 	this.changedBy = changedBy;
	}
 
	public int getChangedBy(){ 
		return this.changedBy;
	}
 
	public void setVoided(byte voided){ 
	 	this.voided = voided;
	}
 
	public byte getVoided(){ 
		return this.voided;
	}
 
	public void setVoidedBy(int voidedBy){ 
	 	this.voidedBy = voidedBy;
	}
 
	public int getVoidedBy(){ 
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
 
	public void setLocationId(int locationId){ 
	 	this.locationId = locationId;
	}
 
	public int getLocationId(){ 
		return this.locationId;
	}
 
	public void setOutcomeConceptId(int outcomeConceptId){ 
	 	this.outcomeConceptId = outcomeConceptId;
	}


 
	public int getOutcomeConceptId(){ 
		return this.outcomeConceptId;
	}
 
	public int getObjectId() { 
 		return this.patientProgramId; 
	} 
 
	public void setObjectId(int selfId){ 
		this.patientProgramId = selfId; 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		this.patientProgramId = rs.getInt("patient_program_id");
		this.patientId = rs.getInt("patient_id");
		this.programId = rs.getInt("program_id");
		this.dateEnrolled =  rs.getTimestamp("date_enrolled") != null ? new java.util.Date( rs.getTimestamp("date_enrolled").getTime() ) : null;
		this.dateCompleted =  rs.getTimestamp("date_completed") != null ? new java.util.Date( rs.getTimestamp("date_completed").getTime() ) : null;
		this.creator = rs.getInt("creator");
		this.dateCreated =  rs.getTimestamp("date_created") != null ? new java.util.Date( rs.getTimestamp("date_created").getTime() ) : null;
		this.changedBy = rs.getInt("changed_by");
		this.dateChanged =  rs.getTimestamp("date_changed") != null ? new java.util.Date( rs.getTimestamp("date_changed").getTime() ) : null;
		this.voided = rs.getByte("voided");
		this.voidedBy = rs.getInt("voided_by");
		this.dateVoided =  rs.getTimestamp("date_voided") != null ? new java.util.Date( rs.getTimestamp("date_voided").getTime() ) : null;
		this.voidReason = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("void_reason") != null ? rs.getString("void_reason").trim() : null);
		this.uuid = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("uuid") != null ? rs.getString("uuid").trim() : null);
		this.locationId = rs.getInt("location_id");
		this.outcomeConceptId = rs.getInt("outcome_concept_id");
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
 		Object[] params = {this.patientId == 0 ? null : this.patientId, this.programId == 0 ? null : this.programId, this.dateEnrolled, this.dateCompleted, this.creator == 0 ? null : this.creator, this.dateCreated, this.changedBy == 0 ? null : this.changedBy, this.dateChanged, this.voided, this.voidedBy == 0 ? null : this.voidedBy, this.dateVoided, this.voidReason, this.uuid, this.locationId == 0 ? null : this.locationId, this.outcomeConceptId == 0 ? null : this.outcomeConceptId};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithObjectId(){ 
 		return "INSERT INTO patient_program(patient_program_id, patient_id, program_id, date_enrolled, date_completed, creator, date_created, changed_by, date_changed, voided, voided_by, date_voided, void_reason, uuid, location_id, outcome_concept_id) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithObjectId(){ 
 		Object[] params = {this.patientProgramId, this.patientId == 0 ? null : this.patientId, this.programId == 0 ? null : this.programId, this.dateEnrolled, this.dateCompleted, this.creator == 0 ? null : this.creator, this.dateCreated, this.changedBy == 0 ? null : this.changedBy, this.dateChanged, this.voided, this.voidedBy == 0 ? null : this.voidedBy, this.dateVoided, this.voidReason, this.uuid, this.locationId == 0 ? null : this.locationId, this.outcomeConceptId == 0 ? null : this.outcomeConceptId};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.patientId == 0 ? null : this.patientId, this.programId == 0 ? null : this.programId, this.dateEnrolled, this.dateCompleted, this.creator == 0 ? null : this.creator, this.dateCreated, this.changedBy == 0 ? null : this.changedBy, this.dateChanged, this.voided, this.voidedBy == 0 ? null : this.voidedBy, this.dateVoided, this.voidReason, this.uuid, this.locationId == 0 ? null : this.locationId, this.outcomeConceptId == 0 ? null : this.outcomeConceptId, this.patientProgramId};		return params; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE patient_program SET patient_id = ?, program_id = ?, date_enrolled = ?, date_completed = ?, creator = ?, date_created = ?, changed_by = ?, date_changed = ?, voided = ?, voided_by = ?, date_voided = ?, void_reason = ?, uuid = ?, location_id = ?, outcome_concept_id = ? WHERE patient_program_id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.patientId == 0 ? null : this.patientId) + "," + (this.programId == 0 ? null : this.programId) + "," + (this.dateEnrolled != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateEnrolled)  +"\"" : null) + "," + (this.dateCompleted != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateCompleted)  +"\"" : null) + "," + (this.creator == 0 ? null : this.creator) + "," + (this.dateCreated != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateCreated)  +"\"" : null) + "," + (this.changedBy == 0 ? null : this.changedBy) + "," + (this.dateChanged != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateChanged)  +"\"" : null) + "," + (this.voided) + "," + (this.voidedBy == 0 ? null : this.voidedBy) + "," + (this.dateVoided != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateVoided)  +"\"" : null) + "," + (this.voidReason != null ? "\""+ utilities.scapeQuotationMarks(voidReason)  +"\"" : null) + "," + (this.uuid != null ? "\""+ utilities.scapeQuotationMarks(uuid)  +"\"" : null) + "," + (this.locationId == 0 ? null : this.locationId) + "," + (this.outcomeConceptId == 0 ? null : this.outcomeConceptId); 
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
	public int getParentValue(String parentAttName) {		
		if (parentAttName.equals("patientId")) return this.patientId;		
		if (parentAttName.equals("creator")) return this.creator;		
		if (parentAttName.equals("locationId")) return this.locationId;		
		if (parentAttName.equals("outcomeConceptId")) return this.outcomeConceptId;		
		if (parentAttName.equals("programId")) return this.programId;		
		if (parentAttName.equals("changedBy")) return this.changedBy;		
		if (parentAttName.equals("voidedBy")) return this.voidedBy;

		throw new RuntimeException("No found parent for: " + parentAttName);	}

	@Override
	public void changeParentValue(String parentAttName, OpenMRSObject newParent) {		
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


}