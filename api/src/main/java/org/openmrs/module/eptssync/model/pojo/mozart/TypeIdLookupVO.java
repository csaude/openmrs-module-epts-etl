package org.openmrs.module.eptssync.model.pojo.mozart;

import org.openmrs.module.eptssync.model.pojo.generic.*; 
 
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.eptssync.utilities.AttDefinedElements; 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class TypeIdLookupVO extends AbstractDatabaseObject implements DatabaseObject { 
	private Integer id;
	private String tableName;
	private String columnName;
	private String idTypeLookup;
	private String idTypeDesc;
	private String notes;
 
	public TypeIdLookupVO() { 
		this.metadata = true;
	} 
 
	public void setId(Integer id){ 
	 	this.id = id;
	}
 
	public Integer getId(){ 
		return this.id;
	}
 
	public void setTableName(String tableName){ 
	 	this.tableName = tableName;
	}
 
	public String getTableName(){ 
		return this.tableName;
	}
 
	public void setColumnName(String columnName){ 
	 	this.columnName = columnName;
	}
 
	public String getColumnName(){ 
		return this.columnName;
	}
 
	public void setIdTypeLookup(String idTypeLookup){ 
	 	this.idTypeLookup = idTypeLookup;
	}
 
	public String getIdTypeLookup(){ 
		return this.idTypeLookup;
	}
 
	public void setIdTypeDesc(String idTypeDesc){ 
	 	this.idTypeDesc = idTypeDesc;
	}
 
	public String getIdTypeDesc(){ 
		return this.idTypeDesc;
	}
 
	public void setNotes(String notes){ 
	 	this.notes = notes;
	}


 
	public String getNotes(){ 
		return this.notes;
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
		this.tableName = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("table_name") != null ? rs.getString("table_name").trim() : null);
		this.columnName = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("column_name") != null ? rs.getString("column_name").trim() : null);
		this.idTypeLookup = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("id_type_lookup") != null ? rs.getString("id_type_lookup").trim() : null);
		this.idTypeDesc = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("id_type_desc") != null ? rs.getString("id_type_desc").trim() : null);
		this.notes = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("notes") != null ? rs.getString("notes").trim() : null);
	} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "id"; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithoutObjectId(){ 
 		return "INSERT INTO type_id_lookup(table_name, column_name, id_type_lookup, id_type_desc, notes) VALUES( ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithoutObjectId(){ 
 		Object[] params = {this.tableName, this.columnName, this.idTypeLookup, this.idTypeDesc, this.notes};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithObjectId(){ 
 		return "INSERT INTO type_id_lookup(id, table_name, column_name, id_type_lookup, id_type_desc, notes) VALUES(?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithObjectId(){ 
 		Object[] params = {this.id, this.tableName, this.columnName, this.idTypeLookup, this.idTypeDesc, this.notes};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.tableName, this.columnName, this.idTypeLookup, this.idTypeDesc, this.notes, this.id};		return params; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE type_id_lookup SET table_name = ?, column_name = ?, id_type_lookup = ?, id_type_desc = ?, notes = ? WHERE id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.tableName != null ? "\""+ utilities.scapeQuotationMarks(tableName)  +"\"" : null) + "," + (this.columnName != null ? "\""+ utilities.scapeQuotationMarks(columnName)  +"\"" : null) + "," + (this.idTypeLookup != null ? "\""+ utilities.scapeQuotationMarks(idTypeLookup)  +"\"" : null) + "," + (this.idTypeDesc != null ? "\""+ utilities.scapeQuotationMarks(idTypeDesc)  +"\"" : null) + "," + (this.notes != null ? "\""+ utilities.scapeQuotationMarks(notes)  +"\"" : null); 
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