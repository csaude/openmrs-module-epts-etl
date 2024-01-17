package org.openmrs.module.epts.etl.model.pojo.mozart;

import org.openmrs.module.epts.etl.model.pojo.generic.*; 
 
import org.openmrs.module.epts.etl.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.epts.etl.utilities.AttDefinedElements; 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class ObservationWithFormDataVO extends AbstractDatabaseObject implements DatabaseObject { 
	private Integer id;
	private String encounterUuid;
	private Integer conceptId;
	private java.util.Date observationDate;
	private double valueNumeric;
	private Integer valueConceptId;
	private String valueText;
	private java.util.Date valueDatetime;
	private String obsUuid;
	private Integer formId;
	private Integer encounterType;
	private String patientUuid;
	private java.util.Date createdDate;
	private java.util.Date encounterDate;
	private java.util.Date changeDate;
	private String locationUuid;
	private String sourceDatabase;
 
	public ObservationWithFormDataVO() { 
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
 
	public void setConceptId(Integer conceptId){ 
	 	this.conceptId = conceptId;
	}
 
	public Integer getConceptId(){ 
		return this.conceptId;
	}
 
	public void setObservationDate(java.util.Date observationDate){ 
	 	this.observationDate = observationDate;
	}
 
	public java.util.Date getObservationDate(){ 
		return this.observationDate;
	}
 
	public void setValueNumeric(double valueNumeric){ 
	 	this.valueNumeric = valueNumeric;
	}
 
	public double getValueNumeric(){ 
		return this.valueNumeric;
	}
 
	public void setValueConceptId(Integer valueConceptId){ 
	 	this.valueConceptId = valueConceptId;
	}
 
	public Integer getValueConceptId(){ 
		return this.valueConceptId;
	}
 
	public void setValueText(String valueText){ 
	 	this.valueText = valueText;
	}
 
	public String getValueText(){ 
		return this.valueText;
	}
 
	public void setValueDatetime(java.util.Date valueDatetime){ 
	 	this.valueDatetime = valueDatetime;
	}
 
	public java.util.Date getValueDatetime(){ 
		return this.valueDatetime;
	}
 
	public void setObsUuid(String obsUuid){ 
	 	this.obsUuid = obsUuid;
	}
 
	public String getObsUuid(){ 
		return this.obsUuid;
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
 
	public void setCreatedDate(java.util.Date createdDate){ 
	 	this.createdDate = createdDate;
	}
 
	public java.util.Date getCreatedDate(){ 
		return this.createdDate;
	}
 
	public void setEncounterDate(java.util.Date encounterDate){ 
	 	this.encounterDate = encounterDate;
	}
 
	public java.util.Date getEncounterDate(){ 
		return this.encounterDate;
	}
 
	public void setChangeDate(java.util.Date changeDate){ 
	 	this.changeDate = changeDate;
	}
 
	public java.util.Date getChangeDate(){ 
		return this.changeDate;
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
		if (rs.getObject("concept_id") != null) this.conceptId = rs.getInt("concept_id");
		this.observationDate =  rs.getTimestamp("observation_date") != null ? new java.util.Date( rs.getTimestamp("observation_date").getTime() ) : null;
		this.valueNumeric = rs.getDouble("value_numeric");
		if (rs.getObject("value_concept_id") != null) this.valueConceptId = rs.getInt("value_concept_id");
		this.valueText = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("value_text") != null ? rs.getString("value_text").trim() : null);
		this.valueDatetime =  rs.getTimestamp("value_datetime") != null ? new java.util.Date( rs.getTimestamp("value_datetime").getTime() ) : null;
		this.obsUuid = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("obs_uuid") != null ? rs.getString("obs_uuid").trim() : null);
		if (rs.getObject("form_id") != null) this.formId = rs.getInt("form_id");
		if (rs.getObject("encounter_type") != null) this.encounterType = rs.getInt("encounter_type");
		this.patientUuid = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("patient_uuid") != null ? rs.getString("patient_uuid").trim() : null);
		this.createdDate =  rs.getTimestamp("created_date") != null ? new java.util.Date( rs.getTimestamp("created_date").getTime() ) : null;
		this.encounterDate =  rs.getTimestamp("encounter_date") != null ? new java.util.Date( rs.getTimestamp("encounter_date").getTime() ) : null;
		this.changeDate =  rs.getTimestamp("change_date") != null ? new java.util.Date( rs.getTimestamp("change_date").getTime() ) : null;
		this.locationUuid = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("location_uuid") != null ? rs.getString("location_uuid").trim() : null);
		this.sourceDatabase = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("source_database") != null ? rs.getString("source_database").trim() : null);
	} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "id"; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithoutObjectId(){ 
 		return "INSERT INTO observation_with_form_data(encounter_uuid, concept_id, observation_date, value_numeric, value_concept_id, value_text, value_datetime, obs_uuid, form_id, encounter_type, patient_uuid, created_date, encounter_date, change_date, location_uuid, source_database) VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithoutObjectId(){ 
 		Object[] params = {this.encounterUuid, this.conceptId, this.observationDate, this.valueNumeric, this.valueConceptId, this.valueText, this.valueDatetime, this.obsUuid, this.formId, this.encounterType, this.patientUuid, this.createdDate, this.encounterDate, this.changeDate, this.locationUuid, this.sourceDatabase};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithObjectId(){ 
 		return "INSERT INTO observation_with_form_data(id, encounter_uuid, concept_id, observation_date, value_numeric, value_concept_id, value_text, value_datetime, obs_uuid, form_id, encounter_type, patient_uuid, created_date, encounter_date, change_date, location_uuid, source_database) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithObjectId(){ 
 		Object[] params = {this.id, this.encounterUuid, this.conceptId, this.observationDate, this.valueNumeric, this.valueConceptId, this.valueText, this.valueDatetime, this.obsUuid, this.formId, this.encounterType, this.patientUuid, this.createdDate, this.encounterDate, this.changeDate, this.locationUuid, this.sourceDatabase};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.encounterUuid, this.conceptId, this.observationDate, this.valueNumeric, this.valueConceptId, this.valueText, this.valueDatetime, this.obsUuid, this.formId, this.encounterType, this.patientUuid, this.createdDate, this.encounterDate, this.changeDate, this.locationUuid, this.sourceDatabase, this.id};		return params; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE observation_with_form_data SET encounter_uuid = ?, concept_id = ?, observation_date = ?, value_numeric = ?, value_concept_id = ?, value_text = ?, value_datetime = ?, obs_uuid = ?, form_id = ?, encounter_type = ?, patient_uuid = ?, created_date = ?, encounter_date = ?, change_date = ?, location_uuid = ?, source_database = ? WHERE id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.encounterUuid != null ? "\""+ utilities.scapeQuotationMarks(encounterUuid)  +"\"" : null) + "," + (this.conceptId) + "," + (this.observationDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(observationDate)  +"\"" : null) + "," + (this.valueNumeric) + "," + (this.valueConceptId) + "," + (this.valueText != null ? "\""+ utilities.scapeQuotationMarks(valueText)  +"\"" : null) + "," + (this.valueDatetime != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(valueDatetime)  +"\"" : null) + "," + (this.obsUuid != null ? "\""+ utilities.scapeQuotationMarks(obsUuid)  +"\"" : null) + "," + (this.formId) + "," + (this.encounterType) + "," + (this.patientUuid != null ? "\""+ utilities.scapeQuotationMarks(patientUuid)  +"\"" : null) + "," + (this.createdDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(createdDate)  +"\"" : null) + "," + (this.encounterDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(encounterDate)  +"\"" : null) + "," + (this.changeDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(changeDate)  +"\"" : null) + "," + (this.locationUuid != null ? "\""+ utilities.scapeQuotationMarks(locationUuid)  +"\"" : null) + "," + (this.sourceDatabase != null ? "\""+ utilities.scapeQuotationMarks(sourceDatabase)  +"\"" : null); 
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