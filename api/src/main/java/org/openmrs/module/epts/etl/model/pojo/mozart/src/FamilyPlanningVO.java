package org.openmrs.module.epts.etl.model.pojo.mozart.src;

import org.openmrs.module.epts.etl.model.pojo.generic.*; 
 
import org.openmrs.module.epts.etl.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.epts.etl.utilities.AttDefinedElements; 
 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class FamilyPlanningVO extends AbstractDatabaseObject implements DatabaseObject { 
	private Integer id;
	private String encounterUuid;
	private Integer fpConceptId;
	private java.util.Date fpDate;
	private Integer fpMethod;
	private String fpUuid;
 
	public FamilyPlanningVO() { 
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
 
	public void setFpConceptId(Integer fpConceptId){ 
	 	this.fpConceptId = fpConceptId;
	}
 
	public Integer getFpConceptId(){ 
		return this.fpConceptId;
	}
 
	public void setFpDate(java.util.Date fpDate){ 
	 	this.fpDate = fpDate;
	}
 
	public java.util.Date getFpDate(){ 
		return this.fpDate;
	}
 
	public void setFpMethod(Integer fpMethod){ 
	 	this.fpMethod = fpMethod;
	}
 
	public Integer getFpMethod(){ 
		return this.fpMethod;
	}
 
	public void setFpUuid(String fpUuid){ 
	 	this.fpUuid = fpUuid;
	}


 
	public String getFpUuid(){ 
		return this.fpUuid;
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
		if (rs.getObject("fp_concept_id") != null) this.fpConceptId = rs.getInt("fp_concept_id");
		this.fpDate =  rs.getTimestamp("fp_date") != null ? new java.util.Date( rs.getTimestamp("fp_date").getTime() ) : null;
		if (rs.getObject("fp_method") != null) this.fpMethod = rs.getInt("fp_method");
		this.fpUuid = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("fp_uuid") != null ? rs.getString("fp_uuid").trim() : null);
	} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "id"; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithoutObjectId(){ 
 		return "INSERT INTO family_planning(encounter_uuid, fp_concept_id, fp_date, fp_method, fp_uuid) VALUES( ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithoutObjectId(){ 
 		Object[] params = {this.encounterUuid, this.fpConceptId, this.fpDate, this.fpMethod, this.fpUuid};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithObjectId(){ 
 		return "INSERT INTO family_planning(id, encounter_uuid, fp_concept_id, fp_date, fp_method, fp_uuid) VALUES(?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithObjectId(){ 
 		Object[] params = {this.id, this.encounterUuid, this.fpConceptId, this.fpDate, this.fpMethod, this.fpUuid};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.encounterUuid, this.fpConceptId, this.fpDate, this.fpMethod, this.fpUuid, this.id};		return params; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE family_planning SET encounter_uuid = ?, fp_concept_id = ?, fp_date = ?, fp_method = ?, fp_uuid = ? WHERE id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.encounterUuid != null ? "\""+ utilities.scapeQuotationMarks(encounterUuid)  +"\"" : null) + "," + (this.fpConceptId) + "," + (this.fpDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(fpDate)  +"\"" : null) + "," + (this.fpMethod) + "," + (this.fpUuid != null ? "\""+ utilities.scapeQuotationMarks(fpUuid)  +"\"" : null); 
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
		return "family_planning";
	}


}