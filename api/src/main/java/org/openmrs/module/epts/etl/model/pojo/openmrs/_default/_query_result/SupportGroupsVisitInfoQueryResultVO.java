package org.openmrs.module.epts.etl.model.pojo.openmrs._default._query_result;

import org.openmrs.module.epts.etl.model.pojo.generic.*; 
 
import org.openmrs.module.epts.etl.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.epts.etl.utilities.AttDefinedElements; 
 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class SupportGroupsVisitInfoQueryResultVO extends AbstractDatabaseObject implements DatabaseObject { 
	private String elegibblySupportGroups;
	private String typeSupportGroups;
	private String valueSupportGroups;
 
	public SupportGroupsVisitInfoQueryResultVO() { 
		this.metadata = false;
	} 
 
	public void setElegibblySupportGroups(String elegibblySupportGroups){ 
	 	this.elegibblySupportGroups = elegibblySupportGroups;
	}
 
	public String getElegibblySupportGroups(){ 
		return this.elegibblySupportGroups;
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
 		return 0; 
	} 
 
	public void setObjectId(Integer selfId){ 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		super.load(rs);
		this.elegibblySupportGroups = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("elegibbly_support_groups") != null ? rs.getString("elegibbly_support_groups").trim() : null);
		this.typeSupportGroups = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("type_support_groups") != null ? rs.getString("type_support_groups").trim() : null);
		this.valueSupportGroups = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("value_support_groups") != null ? rs.getString("value_support_groups").trim() : null);
	} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return null; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithoutObjectId(){ 
 		return "INSERT INTO support_groups_visit_info(elegibbly_support_groups, type_support_groups, value_support_groups) VALUES( ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithoutObjectId(){ 
 		Object[] params = {this.elegibblySupportGroups, this.typeSupportGroups, this.valueSupportGroups};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithObjectId(){ 
 		return "INSERT INTO support_groups_visit_info(elegibbly_support_groups, type_support_groups, value_support_groups) VALUES( ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithObjectId(){ 
 		Object[] params = {this.elegibblySupportGroups, this.typeSupportGroups, this.valueSupportGroups};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.elegibblySupportGroups, this.typeSupportGroups, this.valueSupportGroups, null};		return params; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE support_groups_visit_info SET elegibbly_support_groups = ?, type_support_groups = ?, value_support_groups = ? WHERE null = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.elegibblySupportGroups != null ? "\""+ utilities.scapeQuotationMarks(elegibblySupportGroups)  +"\"" : null) + "," + (this.typeSupportGroups != null ? "\""+ utilities.scapeQuotationMarks(typeSupportGroups)  +"\"" : null) + "," + (this.valueSupportGroups != null ? "\""+ utilities.scapeQuotationMarks(valueSupportGroups)  +"\"" : null); 
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
		return "support_groups_visit_info";
	}


}