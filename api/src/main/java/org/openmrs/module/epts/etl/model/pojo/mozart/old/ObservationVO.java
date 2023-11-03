package org.openmrs.module.epts.etl.model.pojo.mozart.old;

import java.sql.SQLException;

import org.openmrs.module.epts.etl.model.pojo.generic.*;
import org.openmrs.module.epts.etl.utilities.AttDefinedElements;
import org.openmrs.module.epts.etl.utilities.DateAndTimeUtilities;

import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class ObservationVO extends AbstractDatabaseObject implements DatabaseObject { 
	private Integer id;
	private String encounterUuid;
	private Integer conceptId;
	private java.util.Date observationDate;
	private double valueNumeric;
	private Integer valueConceptId;
	private String valueText;
	private java.util.Date valueDatetime;
	private String obsUuid;
 
	public ObservationVO() { 
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
	} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "id"; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithoutObjectId(){ 
 		return "INSERT INTO observation(encounter_uuid, concept_id, observation_date, value_numeric, value_concept_id, value_text, value_datetime, obs_uuid) VALUES( ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithoutObjectId(){ 
 		Object[] params = {this.encounterUuid, this.conceptId, this.observationDate, this.valueNumeric, this.valueConceptId, this.valueText, this.valueDatetime, this.obsUuid};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithObjectId(){ 
 		return "INSERT INTO observation(id, encounter_uuid, concept_id, observation_date, value_numeric, value_concept_id, value_text, value_datetime, obs_uuid) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithObjectId(){ 
 		Object[] params = {this.id, this.encounterUuid, this.conceptId, this.observationDate, this.valueNumeric, this.valueConceptId, this.valueText, this.valueDatetime, this.obsUuid};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.encounterUuid, this.conceptId, this.observationDate, this.valueNumeric, this.valueConceptId, this.valueText, this.valueDatetime, this.obsUuid, this.id};		return params; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE observation SET encounter_uuid = ?, concept_id = ?, observation_date = ?, value_numeric = ?, value_concept_id = ?, value_text = ?, value_datetime = ?, obs_uuid = ? WHERE id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.encounterUuid != null ? "\""+ utilities.scapeQuotationMarks(encounterUuid)  +"\"" : null) + "," + (this.conceptId) + "," + (this.observationDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(observationDate)  +"\"" : null) + "," + (this.valueNumeric) + "," + (this.valueConceptId) + "," + (this.valueText != null ? "\""+ utilities.scapeQuotationMarks(valueText)  +"\"" : null) + "," + (this.valueDatetime != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(valueDatetime)  +"\"" : null) + "," + (this.obsUuid != null ? "\""+ utilities.scapeQuotationMarks(obsUuid)  +"\"" : null); 
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