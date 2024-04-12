package org.openmrs.module.epts.etl.model.pojo.mozart.src;

import org.openmrs.module.epts.etl.model.pojo.generic.*; 
 
import org.openmrs.module.epts.etl.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.epts.etl.utilities.AttDefinedElements; 
 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class ObservationLookupVO extends AbstractDatabaseObject implements DatabaseObject { 
	private Integer conceptId;
	private String conceptName;
 
	public ObservationLookupVO() { 
		this.metadata = true;
	} 
 
	public void setConceptId(Integer conceptId){ 
	 	this.conceptId = conceptId;
	}
 
	public Integer getConceptId(){ 
		return this.conceptId;
	}
 
	public void setConceptName(String conceptName){ 
	 	this.conceptName = conceptName;
	}


 
	public String getConceptName(){ 
		return this.conceptName;
	}
 
	public Integer getObjectId() { 
 		return this.conceptId; 
	} 
 
	public void setObjectId(Integer selfId){ 
		this.conceptId = selfId; 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		super.load(rs);
		if (rs.getObject("concept_id") != null) this.conceptId = rs.getInt("concept_id");
		this.conceptName = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("concept_name") != null ? rs.getString("concept_name").trim() : null);
	} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "concept_id"; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithoutObjectId(){ 
 		return "INSERT INTO observation_lookup(concept_name) VALUES( ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithoutObjectId(){ 
 		Object[] params = {this.conceptName};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithObjectId(){ 
 		return "INSERT INTO observation_lookup(concept_id, concept_name) VALUES(?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithObjectId(){ 
 		Object[] params = {this.conceptId, this.conceptName};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.conceptName, this.conceptId};		return params; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE observation_lookup SET concept_name = ? WHERE concept_id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.conceptName != null ? "\""+ utilities.scapeQuotationMarks(conceptName)  +"\"" : null); 
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
		return "observation_lookup";
	}


}