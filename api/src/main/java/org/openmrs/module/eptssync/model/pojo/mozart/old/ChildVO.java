package org.openmrs.module.eptssync.model.pojo.mozart.old;

import org.openmrs.module.eptssync.model.pojo.generic.*; 
 
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.eptssync.utilities.AttDefinedElements; 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class ChildVO extends AbstractDatabaseObject implements DatabaseObject { 
	private Integer id;
	private Integer parentId;
	private String varcharField;
	private String uniqueField;
	private java.util.Date dateField;
	private java.util.Date dateFieldAuto;
 
	public ChildVO() { 
		this.metadata = false;
	} 
 
	public void setId(Integer id){ 
	 	this.id = id;
	}
 
	public Integer getId(){ 
		return this.id;
	}
 
	public void setParentId(Integer parentId){ 
	 	this.parentId = parentId;
	}
 
	public Integer getParentId(){ 
		return this.parentId;
	}
 
	public void setVarcharField(String varcharField){ 
	 	this.varcharField = varcharField;
	}
 
	public String getVarcharField(){ 
		return this.varcharField;
	}
 
	public void setUniqueField(String uniqueField){ 
	 	this.uniqueField = uniqueField;
	}
 
	public String getUniqueField(){ 
		return this.uniqueField;
	}
 
	public void setDateField(java.util.Date dateField){ 
	 	this.dateField = dateField;
	}
 
	public java.util.Date getDateField(){ 
		return this.dateField;
	}
 
	public void setDateFieldAuto(java.util.Date dateFieldAuto){ 
	 	this.dateFieldAuto = dateFieldAuto;
	}


 
	public java.util.Date getDateFieldAuto(){ 
		return this.dateFieldAuto;
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
		if (rs.getObject("parent_id") != null) this.parentId = rs.getInt("parent_id");
		this.varcharField = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("varchar_field") != null ? rs.getString("varchar_field").trim() : null);
		this.uniqueField = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("unique_field") != null ? rs.getString("unique_field").trim() : null);
		this.dateField =  rs.getTimestamp("date_field") != null ? new java.util.Date( rs.getTimestamp("date_field").getTime() ) : null;
		this.dateFieldAuto =  rs.getTimestamp("date_field_auto") != null ? new java.util.Date( rs.getTimestamp("date_field_auto").getTime() ) : null;
	} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "id"; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithoutObjectId(){ 
 		return "INSERT INTO child(parent_id, varchar_field, unique_field, date_field, date_field_auto) VALUES( ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithoutObjectId(){ 
 		Object[] params = {this.parentId, this.varcharField, this.uniqueField, this.dateField, this.dateFieldAuto};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithObjectId(){ 
 		return "INSERT INTO child(id, parent_id, varchar_field, unique_field, date_field, date_field_auto) VALUES(?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithObjectId(){ 
 		Object[] params = {this.id, this.parentId, this.varcharField, this.uniqueField, this.dateField, this.dateFieldAuto};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.parentId, this.varcharField, this.uniqueField, this.dateField, this.dateFieldAuto, this.id};		return params; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE child SET parent_id = ?, varchar_field = ?, unique_field = ?, date_field = ?, date_field_auto = ? WHERE id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.parentId) + "," + (this.varcharField != null ? "\""+ utilities.scapeQuotationMarks(varcharField)  +"\"" : null) + "," + (this.uniqueField != null ? "\""+ utilities.scapeQuotationMarks(uniqueField)  +"\"" : null) + "," + (this.dateField != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateField)  +"\"" : null) + "," + (this.dateFieldAuto != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateFieldAuto)  +"\"" : null); 
	} 
 
	@Override
	public boolean hasParents() {
		if (this.parentId != 0) return true;

		return false;
	}

	@Override
	public Integer getParentValue(String parentAttName) {		
		if (parentAttName.equals("parentId")) return this.parentId;

		throw new RuntimeException("No found parent for: " + parentAttName);	}

	@Override
	public void changeParentValue(String parentAttName, DatabaseObject newParent) {		
		if (parentAttName.equals("parentId")) {
			this.parentId = newParent.getObjectId();
			return;
		}

		throw new RuntimeException("No found parent for: " + parentAttName);
	}

	@Override
	public void setParentToNull(String parentAttName) {		
		if (parentAttName.equals("parentId")) {
			this.parentId = null;
			return;
		}

		throw new RuntimeException("No found parent for: " + parentAttName);
	}


}