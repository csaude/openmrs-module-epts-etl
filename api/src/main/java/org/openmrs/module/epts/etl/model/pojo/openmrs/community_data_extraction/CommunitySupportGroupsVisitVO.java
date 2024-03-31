package org.openmrs.module.epts.etl.model.pojo.openmrs.community_data_extraction;

import org.openmrs.module.epts.etl.model.pojo.generic.*; 
 
import org.openmrs.module.epts.etl.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.epts.etl.utilities.AttDefinedElements; 
 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class CommunitySupportGroupsVisitVO extends AbstractDatabaseObject implements DatabaseObject { 
	private Integer id;
	private Integer patientId;
	private Integer encounterId;
	private String encounterUuid;
	private String elegibblySupportGroups;
	private java.util.Date dateElegibblySupportGroups;
	private String typeSupportGroups;
	private String valueSupportGroups;
 
	public CommunitySupportGroupsVisitVO() { 
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
 
	public void setEncounterUuid(String encounterUuid){ 
	 	this.encounterUuid = encounterUuid;
	}
 
	public String getEncounterUuid(){ 
		return this.encounterUuid;
	}
 
	public void setElegibblySupportGroups(String elegibblySupportGroups){ 
	 	this.elegibblySupportGroups = elegibblySupportGroups;
	}
 
	public String getElegibblySupportGroups(){ 
		return this.elegibblySupportGroups;
	}
 
	public void setDateElegibblySupportGroups(java.util.Date dateElegibblySupportGroups){ 
	 	this.dateElegibblySupportGroups = dateElegibblySupportGroups;
	}
 
	public java.util.Date getDateElegibblySupportGroups(){ 
		return this.dateElegibblySupportGroups;
	}
 
	public void setTypeSupportGroups(String typeSupportGroups){ 
	 	this.typeSupportGroups = typeSupportGroups;
	}
 
	public String getTypeSupportGroups(){ 
		return this.typeSupportGroups;
	}
 
	public void setValueSupportGroups(String valueSupportGroups){ 
	 	this.valueSupportGroups = valueSupportGroups;
	}


 
	public String getValueSupportGroups(){ 
		return this.valueSupportGroups;
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
		this.encounterUuid = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("encounter_uuid") != null ? rs.getString("encounter_uuid").trim() : null);
		this.elegibblySupportGroups = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("elegibbly_support_groups") != null ? rs.getString("elegibbly_support_groups").trim() : null);
		this.dateElegibblySupportGroups =  rs.getTimestamp("date_elegibbly_support_groups") != null ? new java.util.Date( rs.getTimestamp("date_elegibbly_support_groups").getTime() ) : null;
		this.typeSupportGroups = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("type_support_groups") != null ? rs.getString("type_support_groups").trim() : null);
		this.valueSupportGroups = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("value_support_groups") != null ? rs.getString("value_support_groups").trim() : null);
	} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "id"; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithoutObjectId(){ 
 		return "INSERT INTO community_support_groups_visit(patient_id, encounter_id, encounter_uuid, elegibbly_support_groups, date_elegibbly_support_groups, type_support_groups, value_support_groups) VALUES( ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithoutObjectId(){ 
 		Object[] params = {this.patientId, this.encounterId, this.encounterUuid, this.elegibblySupportGroups, this.dateElegibblySupportGroups, this.typeSupportGroups, this.valueSupportGroups};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithObjectId(){ 
 		return "INSERT INTO community_support_groups_visit(id, patient_id, encounter_id, encounter_uuid, elegibbly_support_groups, date_elegibbly_support_groups, type_support_groups, value_support_groups) VALUES(?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithObjectId(){ 
 		Object[] params = {this.id, this.patientId, this.encounterId, this.encounterUuid, this.elegibblySupportGroups, this.dateElegibblySupportGroups, this.typeSupportGroups, this.valueSupportGroups};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.patientId, this.encounterId, this.encounterUuid, this.elegibblySupportGroups, this.dateElegibblySupportGroups, this.typeSupportGroups, this.valueSupportGroups, this.id};		return params; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE community_support_groups_visit SET patient_id = ?, encounter_id = ?, encounter_uuid = ?, elegibbly_support_groups = ?, date_elegibbly_support_groups = ?, type_support_groups = ?, value_support_groups = ? WHERE id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.patientId) + "," + (this.encounterId) + "," + (this.encounterUuid != null ? "\""+ utilities.scapeQuotationMarks(encounterUuid)  +"\"" : null) + "," + (this.elegibblySupportGroups != null ? "\""+ utilities.scapeQuotationMarks(elegibblySupportGroups)  +"\"" : null) + "," + (this.dateElegibblySupportGroups != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateElegibblySupportGroups)  +"\"" : null) + "," + (this.typeSupportGroups != null ? "\""+ utilities.scapeQuotationMarks(typeSupportGroups)  +"\"" : null) + "," + (this.valueSupportGroups != null ? "\""+ utilities.scapeQuotationMarks(valueSupportGroups)  +"\"" : null); 
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
		return "community_support_groups_visit";
	}


}