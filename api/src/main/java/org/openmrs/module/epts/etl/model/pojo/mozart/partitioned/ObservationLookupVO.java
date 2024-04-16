package org.openmrs.module.epts.etl.model.pojo.mozart.partitioned;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.openmrs.module.epts.etl.model.pojo.generic.AbstractDatabaseObject;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObject;
import org.openmrs.module.epts.etl.utilities.AttDefinedElements;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class ObservationLookupVO extends AbstractDatabaseObject implements DatabaseObject {
	
	private Integer conceptId;
	
	private String conceptName;
	
	public ObservationLookupVO() {
		this.metadata = true;
	}
	
	public void setConceptId(Integer conceptId) {
		this.conceptId = conceptId;
	}
	
	public Integer getConceptId() {
		return this.conceptId;
	}
	
	public void setConceptName(String conceptName) {
		this.conceptName = conceptName;
	}
	
	public String getConceptName() {
		return this.conceptName;
	}
	
	@Override
	public void load(ResultSet rs) throws SQLException {
		super.load(rs);
		
		if (rs.getObject("concept_id") != null)
			this.conceptId = rs.getInt("concept_id");
		this.conceptName = AttDefinedElements.removeStrangeCharactersOnString(
		    rs.getString("concept_name") != null ? rs.getString("concept_name").trim() : null);
	}
	
	@JsonIgnore
	@Override
	public String getInsertSQLWithoutObjectId() {
		return "INSERT INTO observation_lookup(concept_name) VALUES( ?);";
	}
	
	@JsonIgnore
	@Override
	public String getInsertSQLWithObjectId() {
		return "INSERT INTO observation_lookup(concept_id, concept_name) VALUES( ?, ?);";
	}
	
	@JsonIgnore
	@Override
	public Object[] getInsertParamsWithoutObjectId() {
		Object[] params = { this.conceptName };
		return params;
	}
	
	@JsonIgnore
	@Override
	public Object[] getInsertParamsWithObjectId() {
		Object[] params = { this.conceptId, this.conceptName };
		return params;
	}
	
	@JsonIgnore
	@Override
	public Object[] getUpdateParams() {
		Object[] params = { this.conceptId, this.conceptName, this.conceptId };
		return params;
	}
	
	@JsonIgnore
	@Override
	public String getUpdateSQL() {
		return "UPDATE observation_lookup SET concept_id = ?, concept_name = ? WHERE concept_id = ? ";
	}
	
	@JsonIgnore
	@Override
	public String generateInsertValuesWithoutObjectId() {
		return "" + (this.conceptName != null ? "\"" + utilities.scapeQuotationMarks(conceptName) + "\"" : null);
	}
	
	@JsonIgnore
	@Override
	public String generateInsertValuesWithObjectId() {
		return "" + (this.conceptId) + ","
		        + (this.conceptName != null ? "\"" + utilities.scapeQuotationMarks(conceptName) + "\"" : null);
	}
	
	@Override
	public boolean hasParents() {
		return false;
	}
	
	@Override
	public Integer getParentValue(String parentAttName) {
		
		throw new RuntimeException("No found parent for: " + parentAttName);
	}
	
	@Override
	public String generateTableName() {
		return "observation_lookup";
	}
	
}
