package org.openmrs.module.epts.etl.model.pojo.mozart.tmp;

import org.openmrs.module.epts.etl.model.pojo.generic.*; 
 
import org.openmrs.module.epts.etl.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.epts.etl.utilities.AttDefinedElements; 
 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class PatientStateVO extends AbstractDatabaseObject implements DatabaseObject { 
	private Integer id;
	private String patientUuid;
	private Integer programId;
	private java.util.Date programEnrollmentDate;
	private java.util.Date programCompletedDate;
	private String locationUuid;
	private String enrollmentUuid;
	private String encounterUuid;
	private Integer sourceId;
	private Integer stateId;
	private java.util.Date stateDate;
	private java.util.Date createdDate;
	private String stateUuid;
	private String sourceDatabase;
 
	public PatientStateVO() { 
		this.metadata = false;
	} 
 
	public void setId(Integer id){ 
	 	this.id = id;
	}
 
	public Integer getId(){ 
		return this.id;
	}
 
	public void setPatientUuid(String patientUuid){ 
	 	this.patientUuid = patientUuid;
	}
 
	public String getPatientUuid(){ 
		return this.patientUuid;
	}
 
	public void setProgramId(Integer programId){ 
	 	this.programId = programId;
	}
 
	public Integer getProgramId(){ 
		return this.programId;
	}
 
	public void setProgramEnrollmentDate(java.util.Date programEnrollmentDate){ 
	 	this.programEnrollmentDate = programEnrollmentDate;
	}
 
	public java.util.Date getProgramEnrollmentDate(){ 
		return this.programEnrollmentDate;
	}
 
	public void setProgramCompletedDate(java.util.Date programCompletedDate){ 
	 	this.programCompletedDate = programCompletedDate;
	}
 
	public java.util.Date getProgramCompletedDate(){ 
		return this.programCompletedDate;
	}
 
	public void setLocationUuid(String locationUuid){ 
	 	this.locationUuid = locationUuid;
	}
 
	public String getLocationUuid(){ 
		return this.locationUuid;
	}
 
	public void setEnrollmentUuid(String enrollmentUuid){ 
	 	this.enrollmentUuid = enrollmentUuid;
	}
 
	public String getEnrollmentUuid(){ 
		return this.enrollmentUuid;
	}
 
	public void setEncounterUuid(String encounterUuid){ 
	 	this.encounterUuid = encounterUuid;
	}
 
	public String getEncounterUuid(){ 
		return this.encounterUuid;
	}
 
	public void setSourceId(Integer sourceId){ 
	 	this.sourceId = sourceId;
	}
 
	public Integer getSourceId(){ 
		return this.sourceId;
	}
 
	public void setStateId(Integer stateId){ 
	 	this.stateId = stateId;
	}
 
	public Integer getStateId(){ 
		return this.stateId;
	}
 
	public void setStateDate(java.util.Date stateDate){ 
	 	this.stateDate = stateDate;
	}
 
	public java.util.Date getStateDate(){ 
		return this.stateDate;
	}
 
	public void setCreatedDate(java.util.Date createdDate){ 
	 	this.createdDate = createdDate;
	}
 
	public java.util.Date getCreatedDate(){ 
		return this.createdDate;
	}
 
	public void setStateUuid(String stateUuid){ 
	 	this.stateUuid = stateUuid;
	}
 
	public String getStateUuid(){ 
		return this.stateUuid;
	}
 
	public void setSourceDatabase(String sourceDatabase){ 
	 	this.sourceDatabase = sourceDatabase;
	}


 
	public String getSourceDatabase(){ 
		return this.sourceDatabase;
	}
 
	@Override
	public void load(ResultSet rs) throws SQLException{ 
		super.load(rs);
 
if (rs.getObject("id") != null) this.id = rs.getInt("id");
this.patientUuid = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("patient_uuid") != null ? rs.getString("patient_uuid").trim() : null);
if (rs.getObject("program_id") != null) this.programId = rs.getInt("program_id");
this.programEnrollmentDate =  rs.getTimestamp("program_enrollment_date") != null ? new java.util.Date( rs.getTimestamp("program_enrollment_date").getTime() ) : null;
this.programCompletedDate =  rs.getTimestamp("program_completed_date") != null ? new java.util.Date( rs.getTimestamp("program_completed_date").getTime() ) : null;
this.locationUuid = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("location_uuid") != null ? rs.getString("location_uuid").trim() : null);
this.enrollmentUuid = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("enrollment_uuid") != null ? rs.getString("enrollment_uuid").trim() : null);
this.encounterUuid = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("encounter_uuid") != null ? rs.getString("encounter_uuid").trim() : null);
if (rs.getObject("source_id") != null) this.sourceId = rs.getInt("source_id");
if (rs.getObject("state_id") != null) this.stateId = rs.getInt("state_id");
this.stateDate =  rs.getTimestamp("state_date") != null ? new java.util.Date( rs.getTimestamp("state_date").getTime() ) : null;
this.createdDate =  rs.getTimestamp("created_date") != null ? new java.util.Date( rs.getTimestamp("created_date").getTime() ) : null;
this.stateUuid = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("state_uuid") != null ? rs.getString("state_uuid").trim() : null);
this.sourceDatabase = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("source_database") != null ? rs.getString("source_database").trim() : null);
	} 
 
	@JsonIgnore
	@Override
	public String getInsertSQLWithoutObjectId(){ 
 		return "INSERT INTO patient_state(patient_uuid, program_id, program_enrollment_date, program_completed_date, location_uuid, enrollment_uuid, encounter_uuid, source_id, state_id, state_date, created_date, state_uuid, source_database) VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	@Override
	public String getInsertSQLWithObjectId(){ 
 		return "INSERT INTO patient_state(id, patient_uuid, program_id, program_enrollment_date, program_completed_date, location_uuid, enrollment_uuid, encounter_uuid, source_id, state_id, state_date, created_date, state_uuid, source_database) VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	@Override
	public Object[]  getInsertParamsWithoutObjectId(){ 
 		Object[] params = {this.patientUuid, this.programId, this.programEnrollmentDate, this.programCompletedDate, this.locationUuid, this.enrollmentUuid, this.encounterUuid, this.sourceId, this.stateId, this.stateDate, this.createdDate, this.stateUuid, this.sourceDatabase};
		return params; 
	} 
 
	@JsonIgnore
	@Override
	public Object[]  getInsertParamsWithObjectId(){ 
 		Object[] params = {this.id, this.patientUuid, this.programId, this.programEnrollmentDate, this.programCompletedDate, this.locationUuid, this.enrollmentUuid, this.encounterUuid, this.sourceId, this.stateId, this.stateDate, this.createdDate, this.stateUuid, this.sourceDatabase};
		return params; 
	} 
 
	@JsonIgnore
	@Override
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.id, this.patientUuid, this.programId, this.programEnrollmentDate, this.programCompletedDate, this.locationUuid, this.enrollmentUuid, this.encounterUuid, this.sourceId, this.stateId, this.stateDate, this.createdDate, this.stateUuid, this.sourceDatabase, this.id};
		return params; 
	} 
 
	@JsonIgnore
	@Override
	public String getUpdateSQL(){ 
 		return "UPDATE patient_state SET id = ?, patient_uuid = ?, program_id = ?, program_enrollment_date = ?, program_completed_date = ?, location_uuid = ?, enrollment_uuid = ?, encounter_uuid = ?, source_id = ?, state_id = ?, state_date = ?, created_date = ?, state_uuid = ?, source_database = ? WHERE id = ? "; 
	} 
 
	@JsonIgnore
	@Override
	public String generateInsertValuesWithoutObjectId(){ 
 		return ""+(this.patientUuid != null ? "\""+ utilities.scapeQuotationMarks(patientUuid)  +"\"" : null) + "," + (this.programId) + "," + (this.programEnrollmentDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(programEnrollmentDate)  +"\"" : null) + "," + (this.programCompletedDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(programCompletedDate)  +"\"" : null) + "," + (this.locationUuid != null ? "\""+ utilities.scapeQuotationMarks(locationUuid)  +"\"" : null) + "," + (this.enrollmentUuid != null ? "\""+ utilities.scapeQuotationMarks(enrollmentUuid)  +"\"" : null) + "," + (this.encounterUuid != null ? "\""+ utilities.scapeQuotationMarks(encounterUuid)  +"\"" : null) + "," + (this.sourceId) + "," + (this.stateId) + "," + (this.stateDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(stateDate)  +"\"" : null) + "," + (this.createdDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(createdDate)  +"\"" : null) + "," + (this.stateUuid != null ? "\""+ utilities.scapeQuotationMarks(stateUuid)  +"\"" : null) + "," + (this.sourceDatabase != null ? "\""+ utilities.scapeQuotationMarks(sourceDatabase)  +"\"" : null); 
	} 
 
	@JsonIgnore
	@Override
	public String generateInsertValuesWithObjectId(){ 
 		return ""+(this.id) + "," + (this.patientUuid != null ? "\""+ utilities.scapeQuotationMarks(patientUuid)  +"\"" : null) + "," + (this.programId) + "," + (this.programEnrollmentDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(programEnrollmentDate)  +"\"" : null) + "," + (this.programCompletedDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(programCompletedDate)  +"\"" : null) + "," + (this.locationUuid != null ? "\""+ utilities.scapeQuotationMarks(locationUuid)  +"\"" : null) + "," + (this.enrollmentUuid != null ? "\""+ utilities.scapeQuotationMarks(enrollmentUuid)  +"\"" : null) + "," + (this.encounterUuid != null ? "\""+ utilities.scapeQuotationMarks(encounterUuid)  +"\"" : null) + "," + (this.sourceId) + "," + (this.stateId) + "," + (this.stateDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(stateDate)  +"\"" : null) + "," + (this.createdDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(createdDate)  +"\"" : null) + "," + (this.stateUuid != null ? "\""+ utilities.scapeQuotationMarks(stateUuid)  +"\"" : null) + "," + (this.sourceDatabase != null ? "\""+ utilities.scapeQuotationMarks(sourceDatabase)  +"\"" : null); 
	} 
 
	@Override
	public boolean hasParents() {
		return false;
	}

	@Override
	public Integer getParentValue(String parentAttName) {

		throw new RuntimeException("No found parent for: " + parentAttName);	}

	@Override
	public String generateTableName() {
		return "patient_state";
	}


}