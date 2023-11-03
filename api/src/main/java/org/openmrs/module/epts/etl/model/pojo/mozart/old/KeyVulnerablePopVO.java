package org.openmrs.module.epts.etl.model.pojo.mozart.old;

import java.sql.SQLException;

import org.openmrs.module.epts.etl.model.pojo.generic.*;
import org.openmrs.module.epts.etl.utilities.AttDefinedElements;
import org.openmrs.module.epts.etl.utilities.DateAndTimeUtilities;

import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class KeyVulnerablePopVO extends AbstractDatabaseObject implements DatabaseObject { 
	private Integer id;
	private String encounterUuid;
	private Integer popType;
	private Integer popId;
	private String popOther;
 
	public KeyVulnerablePopVO() { 
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
	} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "id"; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithoutObjectId(){ 
 		return "INSERT INTO key_vulnerable_pop(encounter_uuid, pop_type, pop_id, pop_other) VALUES( ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithoutObjectId(){ 
 		Object[] params = {this.encounterUuid, this.popType, this.popId, this.popOther};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithObjectId(){ 
 		return "INSERT INTO key_vulnerable_pop(id, encounter_uuid, pop_type, pop_id, pop_other) VALUES(?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithObjectId(){ 
 		Object[] params = {this.id, this.encounterUuid, this.popType, this.popId, this.popOther};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.encounterUuid, this.popType, this.popId, this.popOther, this.id};		return params; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE key_vulnerable_pop SET encounter_uuid = ?, pop_type = ?, pop_id = ?, pop_other = ? WHERE id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.encounterUuid != null ? "\""+ utilities.scapeQuotationMarks(encounterUuid)  +"\"" : null) + "," + (this.popType) + "," + (this.popId) + "," + (this.popOther != null ? "\""+ utilities.scapeQuotationMarks(popOther)  +"\"" : null); 
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