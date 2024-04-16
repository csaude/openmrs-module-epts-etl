package org.openmrs.module.epts.etl.model.pojo.openmrs._default;

import org.openmrs.module.epts.etl.model.pojo.generic.*; 
 
import org.openmrs.module.epts.etl.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.epts.etl.utilities.AttDefinedElements; 
 
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
	private Byte voided;
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
 
	public void setVoided(Byte voided){ 
	 	this.voided = voided;
	}
 
	public Byte getVoided(){ 
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
 
	@Override
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
	@Override
	public String getInsertSQLWithoutObjectId(){ 
 		return "INSERT INTO patient_program(patient_id, program_id, date_enrolled, date_completed, creator, date_created, changed_by, date_changed, voided, voided_by, date_voided, void_reason, uuid, location_id, outcome_concept_id) VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	@Override
	public String getInsertSQLWithObjectId(){ 
 		return "INSERT INTO patient_program(patient_program_id, patient_id, program_id, date_enrolled, date_completed, creator, date_created, changed_by, date_changed, voided, voided_by, date_voided, void_reason, uuid, location_id, outcome_concept_id) VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	@Override
	public Object[]  getInsertParamsWithoutObjectId(){ 
 		Object[] params = {this.patientId, this.programId, this.dateEnrolled, this.dateCompleted, this.creator, this.dateCreated, this.changedBy, this.dateChanged, this.voided, this.voidedBy, this.dateVoided, this.voidReason, this.uuid, this.locationId, this.outcomeConceptId};
		return params; 
	} 
 
	@JsonIgnore
	@Override
	public Object[]  getInsertParamsWithObjectId(){ 
 		Object[] params = {this.patientProgramId, this.patientId, this.programId, this.dateEnrolled, this.dateCompleted, this.creator, this.dateCreated, this.changedBy, this.dateChanged, this.voided, this.voidedBy, this.dateVoided, this.voidReason, this.uuid, this.locationId, this.outcomeConceptId};
		return params; 
	} 
 
	@JsonIgnore
	@Override
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.patientProgramId, this.patientId, this.programId, this.dateEnrolled, this.dateCompleted, this.creator, this.dateCreated, this.changedBy, this.dateChanged, this.voided, this.voidedBy, this.dateVoided, this.voidReason, this.uuid, this.locationId, this.outcomeConceptId, this.patientProgramId};
		return params; 
	} 
 
	@JsonIgnore
	@Override
	public String getUpdateSQL(){ 
 		return "UPDATE patient_program SET patient_program_id = ?, patient_id = ?, program_id = ?, date_enrolled = ?, date_completed = ?, creator = ?, date_created = ?, changed_by = ?, date_changed = ?, voided = ?, voided_by = ?, date_voided = ?, void_reason = ?, uuid = ?, location_id = ?, outcome_concept_id = ? WHERE patient_program_id = ? "; 
	} 
 
	@JsonIgnore
	@Override
	public String generateInsertValuesWithoutObjectId(){ 
 		return ""+(this.patientId) + "," + (this.programId) + "," + (this.dateEnrolled != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateEnrolled)  +"\"" : null) + "," + (this.dateCompleted != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateCompleted)  +"\"" : null) + "," + (this.creator) + "," + (this.dateCreated != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateCreated)  +"\"" : null) + "," + (this.changedBy) + "," + (this.dateChanged != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateChanged)  +"\"" : null) + "," + (this.voided) + "," + (this.voidedBy) + "," + (this.dateVoided != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateVoided)  +"\"" : null) + "," + (this.voidReason != null ? "\""+ utilities.scapeQuotationMarks(voidReason)  +"\"" : null) + "," + (this.uuid != null ? "\""+ utilities.scapeQuotationMarks(uuid)  +"\"" : null) + "," + (this.locationId) + "," + (this.outcomeConceptId); 
	} 
 
	@JsonIgnore
	@Override
	public String generateInsertValuesWithObjectId(){ 
 		return ""+(this.patientProgramId) + "," + (this.patientId) + "," + (this.programId) + "," + (this.dateEnrolled != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateEnrolled)  +"\"" : null) + "," + (this.dateCompleted != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateCompleted)  +"\"" : null) + "," + (this.creator) + "," + (this.dateCreated != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateCreated)  +"\"" : null) + "," + (this.changedBy) + "," + (this.dateChanged != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateChanged)  +"\"" : null) + "," + (this.voided) + "," + (this.voidedBy) + "," + (this.dateVoided != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateVoided)  +"\"" : null) + "," + (this.voidReason != null ? "\""+ utilities.scapeQuotationMarks(voidReason)  +"\"" : null) + "," + (this.uuid != null ? "\""+ utilities.scapeQuotationMarks(uuid)  +"\"" : null) + "," + (this.locationId) + "," + (this.outcomeConceptId); 
	} 
 
	@Override
	public boolean hasParents() {
		if (this.patientId != null) return true;

		if (this.creator != null) return true;

		if (this.locationId != null) return true;

		if (this.outcomeConceptId != null) return true;

		if (this.programId != null) return true;

		if (this.changedBy != null) return true;

		if (this.voidedBy != null) return true;

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
	public String generateTableName() {
		return "patient_program";
	}


}