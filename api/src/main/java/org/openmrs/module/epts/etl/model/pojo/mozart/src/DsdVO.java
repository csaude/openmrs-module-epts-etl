package org.openmrs.module.epts.etl.model.pojo.mozart.src;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.openmrs.module.epts.etl.model.pojo.generic.AbstractDatabaseObject;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObject;
import org.openmrs.module.epts.etl.utilities.AttDefinedElements;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class DsdVO extends AbstractDatabaseObject implements DatabaseObject {
	
	private Integer id;
	
	private String encounterUuid;
	
	private Integer dsdId;
	
	private Integer dsdStateId;
	
	private String dsdUuid;
	
	public DsdVO() {
		this.metadata = false;
	}
	
	public void setId(Integer id) {
		this.id = id;
	}
	
	public Integer getId() {
		return this.id;
	}
	
	public void setEncounterUuid(String encounterUuid) {
		this.encounterUuid = encounterUuid;
	}
	
	public String getEncounterUuid() {
		return this.encounterUuid;
	}
	
	public void setDsdId(Integer dsdId) {
		this.dsdId = dsdId;
	}
	
	public Integer getDsdId() {
		return this.dsdId;
	}
	
	public void setDsdStateId(Integer dsdStateId) {
		this.dsdStateId = dsdStateId;
	}
	
	public Integer getDsdStateId() {
		return this.dsdStateId;
	}
	
	public void setDsdUuid(String dsdUuid) {
		this.dsdUuid = dsdUuid;
	}
	
	public String getDsdUuid() {
		return this.dsdUuid;
	}
	
	@Override
	public void load(ResultSet rs) throws SQLException {
		super.load(rs);
		
		if (rs.getObject("id") != null)
			this.id = rs.getInt("id");
		this.encounterUuid = AttDefinedElements.removeStrangeCharactersOnString(
		    rs.getString("encounter_uuid") != null ? rs.getString("encounter_uuid").trim() : null);
		if (rs.getObject("dsd_id") != null)
			this.dsdId = rs.getInt("dsd_id");
		if (rs.getObject("dsd_state_id") != null)
			this.dsdStateId = rs.getInt("dsd_state_id");
		this.dsdUuid = AttDefinedElements
		        .removeStrangeCharactersOnString(rs.getString("dsd_uuid") != null ? rs.getString("dsd_uuid").trim() : null);
	}
	
	@JsonIgnore
	@Override
	public String getInsertSQLWithoutObjectId() {
		return "INSERT INTO dsd(encounter_uuid, dsd_id, dsd_state_id, dsd_uuid) VALUES( ?, ?, ?, ?);";
	}
	
	@JsonIgnore
	@Override
	public String getInsertSQLWithObjectId() {
		return "INSERT INTO dsd(id, encounter_uuid, dsd_id, dsd_state_id, dsd_uuid) VALUES( ?, ?, ?, ?, ?);";
	}
	
	@JsonIgnore
	@Override
	public Object[] getInsertParamsWithoutObjectId() {
		Object[] params = { this.encounterUuid, this.dsdId, this.dsdStateId, this.dsdUuid };
		return params;
	}
	
	@JsonIgnore
	@Override
	public Object[] getInsertParamsWithObjectId() {
		Object[] params = { this.id, this.encounterUuid, this.dsdId, this.dsdStateId, this.dsdUuid };
		return params;
	}
	
	@JsonIgnore
	@Override
	public Object[] getUpdateParams() {
		Object[] params = { this.id, this.encounterUuid, this.dsdId, this.dsdStateId, this.dsdUuid, this.id };
		return params;
	}
	
	@JsonIgnore
	@Override
	public String getUpdateSQL() {
		return "UPDATE dsd SET id = ?, encounter_uuid = ?, dsd_id = ?, dsd_state_id = ?, dsd_uuid = ? WHERE id = ? ";
	}
	
	@JsonIgnore
	@Override
	public String generateInsertValuesWithoutObjectId() {
		return "" + (this.encounterUuid != null ? "\"" + utilities.scapeQuotationMarks(encounterUuid) + "\"" : null) + ","
		        + (this.dsdId) + "," + (this.dsdStateId) + ","
		        + (this.dsdUuid != null ? "\"" + utilities.scapeQuotationMarks(dsdUuid) + "\"" : null);
	}
	
	@JsonIgnore
	@Override
	public String generateInsertValuesWithObjectId() {
		return "" + (this.id) + ","
		        + (this.encounterUuid != null ? "\"" + utilities.scapeQuotationMarks(encounterUuid) + "\"" : null) + ","
		        + (this.dsdId) + "," + (this.dsdStateId) + ","
		        + (this.dsdUuid != null ? "\"" + utilities.scapeQuotationMarks(dsdUuid) + "\"" : null);
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
		return "dsd";
	}
	
}
