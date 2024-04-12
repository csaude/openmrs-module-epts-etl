package org.openmrs.module.epts.etl.model.pojo.openmrs.cacum_data_extration;

import org.openmrs.module.epts.etl.model.pojo.generic.*; 
 
import org.openmrs.module.epts.etl.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.epts.etl.utilities.AttDefinedElements; 
 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class CacumWomenPregnantLactationVO extends AbstractDatabaseObject implements DatabaseObject { 
	private Integer id;
	private Integer patientId;
	private Integer encounterId;
	private Integer programId;
	private String encounterUuid;
	private String programUuid;
	private java.util.Date dateStatus;
	private String cacumWomenStatus;
	private String source;
 
	public CacumWomenPregnantLactationVO() { 
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
 
	public void setEncounterId(Integer encounterId){ 
	 	this.encounterId = encounterId;
	}
 
	public Integer getEncounterId(){ 
		return this.encounterId;
	}
 
	public void setProgramId(Integer programId){ 
	 	this.programId = programId;
	}
 
	public Integer getProgramId(){ 
		return this.programId;
	}
 
	public void setEncounterUuid(String encounterUuid){ 
	 	this.encounterUuid = encounterUuid;
	}
 
	public String getEncounterUuid(){ 
		return this.encounterUuid;
	}
 
	public void setProgramUuid(String programUuid){ 
	 	this.programUuid = programUuid;
	}
 
	public String getProgramUuid(){ 
		return this.programUuid;
	}
 
	public void setDateStatus(java.util.Date dateStatus){ 
	 	this.dateStatus = dateStatus;
	}
 
	public java.util.Date getDateStatus(){ 
		return this.dateStatus;
	}
 
	public void setCacumWomenStatus(String cacumWomenStatus){ 
	 	this.cacumWomenStatus = cacumWomenStatus;
	}
 
	public String getCacumWomenStatus(){ 
		return this.cacumWomenStatus;
	}
 
	public void setSource(String source){ 
	 	this.source = source;
	}


 
	public String getSource(){ 
		return this.source;
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
		if (rs.getObject("encounter_id") != null) this.encounterId = rs.getInt("encounter_id");
		if (rs.getObject("program_id") != null) this.programId = rs.getInt("program_id");
		this.encounterUuid = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("encounter_uuid") != null ? rs.getString("encounter_uuid").trim() : null);
		this.programUuid = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("program_uuid") != null ? rs.getString("program_uuid").trim() : null);
		this.dateStatus =  rs.getTimestamp("date_status") != null ? new java.util.Date( rs.getTimestamp("date_status").getTime() ) : null;
		this.cacumWomenStatus = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("cacum_women_status") != null ? rs.getString("cacum_women_status").trim() : null);
		this.source = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("source") != null ? rs.getString("source").trim() : null);
	} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "id"; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithoutObjectId(){ 
 		return "INSERT INTO cacum_women_pregnant_lactation(patient_id, encounter_id, program_id, encounter_uuid, program_uuid, date_status, cacum_women_status, source) VALUES( ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithoutObjectId(){ 
 		Object[] params = {this.patientId, this.encounterId, this.programId, this.encounterUuid, this.programUuid, this.dateStatus, this.cacumWomenStatus, this.source};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithObjectId(){ 
 		return "INSERT INTO cacum_women_pregnant_lactation(id, patient_id, encounter_id, program_id, encounter_uuid, program_uuid, date_status, cacum_women_status, source) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithObjectId(){ 
 		Object[] params = {this.id, this.patientId, this.encounterId, this.programId, this.encounterUuid, this.programUuid, this.dateStatus, this.cacumWomenStatus, this.source};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.patientId, this.encounterId, this.programId, this.encounterUuid, this.programUuid, this.dateStatus, this.cacumWomenStatus, this.source, this.id};		return params; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE cacum_women_pregnant_lactation SET patient_id = ?, encounter_id = ?, program_id = ?, encounter_uuid = ?, program_uuid = ?, date_status = ?, cacum_women_status = ?, source = ? WHERE id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.patientId) + "," + (this.encounterId) + "," + (this.programId) + "," + (this.encounterUuid != null ? "\""+ utilities.scapeQuotationMarks(encounterUuid)  +"\"" : null) + "," + (this.programUuid != null ? "\""+ utilities.scapeQuotationMarks(programUuid)  +"\"" : null) + "," + (this.dateStatus != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateStatus)  +"\"" : null) + "," + (this.cacumWomenStatus != null ? "\""+ utilities.scapeQuotationMarks(cacumWomenStatus)  +"\"" : null) + "," + (this.source != null ? "\""+ utilities.scapeQuotationMarks(source)  +"\"" : null); 
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

	@Override
	public String generateTableName() {
		return "cacum_women_pregnant_lactation";
	}


}