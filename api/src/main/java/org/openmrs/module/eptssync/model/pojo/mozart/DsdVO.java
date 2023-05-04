package org.openmrs.module.eptssync.model.pojo.mozart;

import org.openmrs.module.eptssync.model.pojo.generic.*; 
 
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.eptssync.utilities.AttDefinedElements; 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
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
 
	public void setDsdId(Integer dsdId){ 
	 	this.dsdId = dsdId;
	}
 
	public Integer getDsdId(){ 
		return this.dsdId;
	}
 
	public void setDsdStateId(Integer dsdStateId){ 
	 	this.dsdStateId = dsdStateId;
	}
 
	public Integer getDsdStateId(){ 
		return this.dsdStateId;
	}
 
	public void setDsdUuid(String dsdUuid){ 
	 	this.dsdUuid = dsdUuid;
	}


 
	public String getDsdUuid(){ 
		return this.dsdUuid;
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
		if (rs.getObject("dsd_id") != null) this.dsdId = rs.getInt("dsd_id");
		if (rs.getObject("dsd_state_id") != null) this.dsdStateId = rs.getInt("dsd_state_id");
		this.dsdUuid = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("dsd_uuid") != null ? rs.getString("dsd_uuid").trim() : null);
	} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "id"; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithoutObjectId(){ 
 		return "INSERT INTO dsd(encounter_uuid, dsd_id, dsd_state_id, dsd_uuid) VALUES( ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithoutObjectId(){ 
 		Object[] params = {this.encounterUuid, this.dsdId, this.dsdStateId, this.dsdUuid};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithObjectId(){ 
 		return "INSERT INTO dsd(id, encounter_uuid, dsd_id, dsd_state_id, dsd_uuid) VALUES(?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithObjectId(){ 
 		Object[] params = {this.id, this.encounterUuid, this.dsdId, this.dsdStateId, this.dsdUuid};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.encounterUuid, this.dsdId, this.dsdStateId, this.dsdUuid, this.id};		return params; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE dsd SET encounter_uuid = ?, dsd_id = ?, dsd_state_id = ?, dsd_uuid = ? WHERE id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.encounterUuid != null ? "\""+ utilities.scapeQuotationMarks(encounterUuid)  +"\"" : null) + "," + (this.dsdId) + "," + (this.dsdStateId) + "," + (this.dsdUuid != null ? "\""+ utilities.scapeQuotationMarks(dsdUuid)  +"\"" : null); 
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