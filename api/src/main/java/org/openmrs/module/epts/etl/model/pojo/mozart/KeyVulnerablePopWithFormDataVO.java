package org.openmrs.module.epts.etl.model.pojo.mozart;

import java.sql.SQLException;

import org.openmrs.module.epts.etl.model.pojo.generic.*;
import org.openmrs.module.epts.etl.utilities.AttDefinedElements;
import org.openmrs.module.epts.etl.utilities.DateAndTimeUtilities;

import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class KeyVulnerablePopWithFormDataVO extends AbstractDatabaseObject implements DatabaseObject { 
	private Integer id;
	private String encounterUuid;
	private Integer popType;
	private Integer popId;
	private String popOther;
	private String keyVulnerablePopUuid;
	private Integer formId;
	private Integer encounterType;
	private String patientUuid;
	private java.util.Date formCreatedDate;
	private java.util.Date encounterDate;
	private java.util.Date formChangeDate;
	private String locationUuid;
	private String sourceDatabase;
 
	public KeyVulnerablePopWithFormDataVO() { 
		this.metadata = false;
	} 
 
	public void setId(Integer id){ 
	 	this.id = id;
	}
 
	public Integer getId(){ 
		return this.id;
	}
 
	public void setEncounterUuid(String encounterUuid){ 
	 	this.encounterUuid = encounterUuid;
	}
 
	public String getEncounterUuid(){ 
		return this.encounterUuid;
	}
 
	public void setPopType(Integer popType){ 
	 	this.popType = popType;
	}
 
	public Integer getPopType(){ 
		return this.popType;
	}
 
	public void setPopId(Integer popId){ 
	 	this.popId = popId;
	}
 
	public Integer getPopId(){ 
		return this.popId;
	}
 
	public void setPopOther(String popOther){ 
	 	this.popOther = popOther;
	}
 
	public String getPopOther(){ 
		return this.popOther;
	}
 
	public void setKeyVulnerablePopUuid(String keyVulnerablePopUuid){ 
	 	this.keyVulnerablePopUuid = keyVulnerablePopUuid;
	}
 
	public String getKeyVulnerablePopUuid(){ 
		return this.keyVulnerablePopUuid;
	}
 
	public void setFormId(Integer formId){ 
	 	this.formId = formId;
	}
 
	public Integer getFormId(){ 
		return this.formId;
	}
 
	public void setEncounterType(Integer encounterType){ 
	 	this.encounterType = encounterType;
	}
 
	public Integer getEncounterType(){ 
		return this.encounterType;
	}
 
	public void setPatientUuid(String patientUuid){ 
	 	this.patientUuid = patientUuid;
	}
 
	public String getPatientUuid(){ 
		return this.patientUuid;
	}
 
	public void setFormCreatedDate(java.util.Date formCreatedDate){ 
	 	this.formCreatedDate = formCreatedDate;
	}
 
	public java.util.Date getFormCreatedDate(){ 
		return this.formCreatedDate;
	}
 
	public void setEncounterDate(java.util.Date encounterDate){ 
	 	this.encounterDate = encounterDate;
	}
 
	public java.util.Date getEncounterDate(){ 
		return this.encounterDate;
	}
 
	public void setFormChangeDate(java.util.Date formChangeDate){ 
	 	this.formChangeDate = formChangeDate;
	}
 
	public java.util.Date getFormChangeDate(){ 
		return this.formChangeDate;
	}
 
	public void setLocationUuid(String locationUuid){ 
	 	this.locationUuid = locationUuid;
	}
 
	public String getLocationUuid(){ 
		return this.locationUuid;
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
		this.encounterUuid = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("encounter_uuid") != null ? rs.getString("encounter_uuid").trim() : null);
		if (rs.getObject("pop_type") != null) this.popType = rs.getInt("pop_type");
		if (rs.getObject("pop_id") != null) this.popId = rs.getInt("pop_id");
		this.popOther = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("pop_other") != null ? rs.getString("pop_other").trim() : null);
		this.keyVulnerablePopUuid = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("key_vulnerable_pop_uuid") != null ? rs.getString("key_vulnerable_pop_uuid").trim() : null);
		if (rs.getObject("form_id") != null) this.formId = rs.getInt("form_id");
		if (rs.getObject("encounter_type") != null) this.encounterType = rs.getInt("encounter_type");
		this.patientUuid = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("patient_uuid") != null ? rs.getString("patient_uuid").trim() : null);
		this.formCreatedDate =  rs.getTimestamp("form_created_date") != null ? new java.util.Date( rs.getTimestamp("form_created_date").getTime() ) : null;
		this.encounterDate =  rs.getTimestamp("encounter_date") != null ? new java.util.Date( rs.getTimestamp("encounter_date").getTime() ) : null;
		this.formChangeDate =  rs.getTimestamp("form_change_date") != null ? new java.util.Date( rs.getTimestamp("form_change_date").getTime() ) : null;
		this.locationUuid = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("location_uuid") != null ? rs.getString("location_uuid").trim() : null);
		this.sourceDatabase = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("source_database") != null ? rs.getString("source_database").trim() : null);
	} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "id"; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithoutObjectId(){ 
 		return "INSERT INTO key_vulnerable_pop_with_form_data(encounter_uuid, pop_type, pop_id, pop_other, key_vulnerable_pop_uuid, form_id, encounter_type, patient_uuid, form_created_date, encounter_date, form_change_date, location_uuid, source_database) VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithoutObjectId(){ 
 		Object[] params = {this.encounterUuid, this.popType, this.popId, this.popOther, this.keyVulnerablePopUuid, this.formId, this.encounterType, this.patientUuid, this.formCreatedDate, this.encounterDate, this.formChangeDate, this.locationUuid, this.sourceDatabase};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithObjectId(){ 
 		return "INSERT INTO key_vulnerable_pop_with_form_data(id, encounter_uuid, pop_type, pop_id, pop_other, key_vulnerable_pop_uuid, form_id, encounter_type, patient_uuid, form_created_date, encounter_date, form_change_date, location_uuid, source_database) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithObjectId(){ 
 		Object[] params = {this.id, this.encounterUuid, this.popType, this.popId, this.popOther, this.keyVulnerablePopUuid, this.formId, this.encounterType, this.patientUuid, this.formCreatedDate, this.encounterDate, this.formChangeDate, this.locationUuid, this.sourceDatabase};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.encounterUuid, this.popType, this.popId, this.popOther, this.keyVulnerablePopUuid, this.formId, this.encounterType, this.patientUuid, this.formCreatedDate, this.encounterDate, this.formChangeDate, this.locationUuid, this.sourceDatabase, this.id};		return params; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE key_vulnerable_pop_with_form_data SET encounter_uuid = ?, pop_type = ?, pop_id = ?, pop_other = ?, key_vulnerable_pop_uuid = ?, form_id = ?, encounter_type = ?, patient_uuid = ?, form_created_date = ?, encounter_date = ?, form_change_date = ?, location_uuid = ?, source_database = ? WHERE id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.encounterUuid != null ? "\""+ utilities.scapeQuotationMarks(encounterUuid)  +"\"" : null) + "," + (this.popType) + "," + (this.popId) + "," + (this.popOther != null ? "\""+ utilities.scapeQuotationMarks(popOther)  +"\"" : null) + "," + (this.keyVulnerablePopUuid != null ? "\""+ utilities.scapeQuotationMarks(keyVulnerablePopUuid)  +"\"" : null) + "," + (this.formId) + "," + (this.encounterType) + "," + (this.patientUuid != null ? "\""+ utilities.scapeQuotationMarks(patientUuid)  +"\"" : null) + "," + (this.formCreatedDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(formCreatedDate)  +"\"" : null) + "," + (this.encounterDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(encounterDate)  +"\"" : null) + "," + (this.formChangeDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(formChangeDate)  +"\"" : null) + "," + (this.locationUuid != null ? "\""+ utilities.scapeQuotationMarks(locationUuid)  +"\"" : null) + "," + (this.sourceDatabase != null ? "\""+ utilities.scapeQuotationMarks(sourceDatabase)  +"\"" : null); 
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