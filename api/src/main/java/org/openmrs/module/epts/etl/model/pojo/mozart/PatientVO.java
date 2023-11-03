package org.openmrs.module.epts.etl.model.pojo.mozart;

import java.sql.SQLException;

import org.openmrs.module.epts.etl.model.pojo.generic.*;
import org.openmrs.module.epts.etl.utilities.AttDefinedElements;
import org.openmrs.module.epts.etl.utilities.DateAndTimeUtilities;

import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class PatientVO extends AbstractDatabaseObject implements DatabaseObject { 
	private Integer id;
	private Integer patientId;
	private String patientUuid;
	private String gender;
	private java.util.Date birthdate;
	private byte birthdateEstimated;
	private String sourceDatabase;
 
	public PatientVO() { 
		this.metadata = false;
	} 
 
	public void setId(Integer id){ 
	 	this.id = id;
	}
 
	public Integer getId(){ 
		return this.id;
	}
 
	public void setPatientId(Integer patientId){ 
	 	this.patientId = patientId;
	}
 
	public Integer getPatientId(){ 
		return this.patientId;
	}
 
	public void setPatientUuid(String patientUuid){ 
	 	this.patientUuid = patientUuid;
	}
 
	public String getPatientUuid(){ 
		return this.patientUuid;
	}
 
	public void setGender(String gender){ 
	 	this.gender = gender;
	}
 
	public String getGender(){ 
		return this.gender;
	}
 
	public void setBirthdate(java.util.Date birthdate){ 
	 	this.birthdate = birthdate;
	}
 
	public java.util.Date getBirthdate(){ 
		return this.birthdate;
	}
 
	public void setBirthdateEstimated(byte birthdateEstimated){ 
	 	this.birthdateEstimated = birthdateEstimated;
	}
 
	public byte getBirthdateEstimated(){ 
		return this.birthdateEstimated;
	}
 
	public void setSourceDatabase(String sourceDatabase){ 
	 	this.sourceDatabase = sourceDatabase;
	}


 
	public String getSourceDatabase(){ 
		return this.sourceDatabase;
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
		if (rs.getObject("patient_id") != null) this.patientId = rs.getInt("patient_id");
		this.patientUuid = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("patient_uuid") != null ? rs.getString("patient_uuid").trim() : null);
		this.gender = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("gender") != null ? rs.getString("gender").trim() : null);
		this.birthdate =  rs.getTimestamp("birthdate") != null ? new java.util.Date( rs.getTimestamp("birthdate").getTime() ) : null;
		this.birthdateEstimated = rs.getByte("birthdate_estimated");
		this.dateCreated =  rs.getTimestamp("date_created") != null ? new java.util.Date( rs.getTimestamp("date_created").getTime() ) : null;
		this.sourceDatabase = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("source_database") != null ? rs.getString("source_database").trim() : null);
	} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "id"; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithoutObjectId(){ 
 		return "INSERT INTO patient(patient_id, patient_uuid, gender, birthdate, birthdate_estimated, date_created, source_database) VALUES( ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithoutObjectId(){ 
 		Object[] params = {this.patientId, this.patientUuid, this.gender, this.birthdate, this.birthdateEstimated, this.dateCreated, this.sourceDatabase};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithObjectId(){ 
 		return "INSERT INTO patient(id, patient_id, patient_uuid, gender, birthdate, birthdate_estimated, date_created, source_database) VALUES(?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithObjectId(){ 
 		Object[] params = {this.id, this.patientId, this.patientUuid, this.gender, this.birthdate, this.birthdateEstimated, this.dateCreated, this.sourceDatabase};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.patientId, this.patientUuid, this.gender, this.birthdate, this.birthdateEstimated, this.dateCreated, this.sourceDatabase, this.id};		return params; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE patient SET patient_id = ?, patient_uuid = ?, gender = ?, birthdate = ?, birthdate_estimated = ?, date_created = ?, source_database = ? WHERE id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.patientId) + "," + (this.patientUuid != null ? "\""+ utilities.scapeQuotationMarks(patientUuid)  +"\"" : null) + "," + (this.gender != null ? "\""+ utilities.scapeQuotationMarks(gender)  +"\"" : null) + "," + (this.birthdate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(birthdate)  +"\"" : null) + "," + (this.birthdateEstimated) + "," + (this.dateCreated != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateCreated)  +"\"" : null) + "," + (this.sourceDatabase != null ? "\""+ utilities.scapeQuotationMarks(sourceDatabase)  +"\"" : null); 
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