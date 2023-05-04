package org.openmrs.module.eptssync.model.pojo.mozart;

import org.openmrs.module.eptssync.model.pojo.generic.*; 
 
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.eptssync.utilities.AttDefinedElements; 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class PatientStateVO extends AbstractDatabaseObject implements DatabaseObject { 
	private Integer id;
	private String patientUuid;
	private Integer programId;
	private java.util.Date programEnrolmentDate;
	private java.util.Date programCompletedDate;
	private String locationUuid;
	private String enrolmentUuid;
	private Integer sourceId;
	private Integer stateId;
	private java.util.Date stateDate;
	private String stateUuid;
 
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
 
	public void setProgramEnrolmentDate(java.util.Date programEnrolmentDate){ 
	 	this.programEnrolmentDate = programEnrolmentDate;
	}
 
	public java.util.Date getProgramEnrolmentDate(){ 
		return this.programEnrolmentDate;
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
 
	public void setEnrolmentUuid(String enrolmentUuid){ 
	 	this.enrolmentUuid = enrolmentUuid;
	}
 
	public String getEnrolmentUuid(){ 
		return this.enrolmentUuid;
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
 
	public void setStateUuid(String stateUuid){ 
	 	this.stateUuid = stateUuid;
	}


 
	public String getStateUuid(){ 
		return this.stateUuid;
	}
 
	public Integer getObjectId() { 
 		return this.id; 
	} 
 
	public void setObjectId(Integer selfId){ 
		this.id = selfId; 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		super.load(rs);
		if (rs.getObject("id") != null) this.id = rs.getInt("id");
		this.patientUuid = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("patient_uuid") != null ? rs.getString("patient_uuid").trim() : null);
		if (rs.getObject("program_id") != null) this.programId = rs.getInt("program_id");
		this.programEnrolmentDate =  rs.getTimestamp("program_enrolment_date") != null ? new java.util.Date( rs.getTimestamp("program_enrolment_date").getTime() ) : null;
		this.programCompletedDate =  rs.getTimestamp("program_completed_date") != null ? new java.util.Date( rs.getTimestamp("program_completed_date").getTime() ) : null;
		this.locationUuid = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("location_uuid") != null ? rs.getString("location_uuid").trim() : null);
		this.enrolmentUuid = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("enrolment_uuid") != null ? rs.getString("enrolment_uuid").trim() : null);
		if (rs.getObject("source_id") != null) this.sourceId = rs.getInt("source_id");
		if (rs.getObject("state_id") != null) this.stateId = rs.getInt("state_id");
		this.stateDate =  rs.getTimestamp("state_date") != null ? new java.util.Date( rs.getTimestamp("state_date").getTime() ) : null;
		this.stateUuid = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("state_uuid") != null ? rs.getString("state_uuid").trim() : null);
	} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "id"; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithoutObjectId(){ 
 		return "INSERT INTO patient_state(patient_uuid, program_id, program_enrolment_date, program_completed_date, location_uuid, enrolment_uuid, source_id, state_id, state_date, state_uuid) VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithoutObjectId(){ 
 		Object[] params = {this.patientUuid, this.programId, this.programEnrolmentDate, this.programCompletedDate, this.locationUuid, this.enrolmentUuid, this.sourceId, this.stateId, this.stateDate, this.stateUuid};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithObjectId(){ 
 		return "INSERT INTO patient_state(id, patient_uuid, program_id, program_enrolment_date, program_completed_date, location_uuid, enrolment_uuid, source_id, state_id, state_date, state_uuid) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithObjectId(){ 
 		Object[] params = {this.id, this.patientUuid, this.programId, this.programEnrolmentDate, this.programCompletedDate, this.locationUuid, this.enrolmentUuid, this.sourceId, this.stateId, this.stateDate, this.stateUuid};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.patientUuid, this.programId, this.programEnrolmentDate, this.programCompletedDate, this.locationUuid, this.enrolmentUuid, this.sourceId, this.stateId, this.stateDate, this.stateUuid, this.id};		return params; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE patient_state SET patient_uuid = ?, program_id = ?, program_enrolment_date = ?, program_completed_date = ?, location_uuid = ?, enrolment_uuid = ?, source_id = ?, state_id = ?, state_date = ?, state_uuid = ? WHERE id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.patientUuid != null ? "\""+ utilities.scapeQuotationMarks(patientUuid)  +"\"" : null) + "," + (this.programId) + "," + (this.programEnrolmentDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(programEnrolmentDate)  +"\"" : null) + "," + (this.programCompletedDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(programCompletedDate)  +"\"" : null) + "," + (this.locationUuid != null ? "\""+ utilities.scapeQuotationMarks(locationUuid)  +"\"" : null) + "," + (this.enrolmentUuid != null ? "\""+ utilities.scapeQuotationMarks(enrolmentUuid)  +"\"" : null) + "," + (this.sourceId) + "," + (this.stateId) + "," + (this.stateDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(stateDate)  +"\"" : null) + "," + (this.stateUuid != null ? "\""+ utilities.scapeQuotationMarks(stateUuid)  +"\"" : null); 
	} 
 
	@Override
	public boolean hasParents() {
		return false;
	}

	@Override
	public Integer getParentValue(String parentAttName) {

		throw new RuntimeException("No found parent for: " + parentAttName);	}

	@Override
	public void changeParentValue(String parentAttName, DatabaseObject newParent) {

		throw new RuntimeException("No found parent for: " + parentAttName);
	}

	@Override
	public void setParentToNull(String parentAttName) {

		throw new RuntimeException("No found parent for: " + parentAttName);
	}


}