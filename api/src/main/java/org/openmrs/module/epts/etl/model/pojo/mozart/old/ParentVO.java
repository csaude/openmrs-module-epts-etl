package org.openmrs.module.epts.etl.model.pojo.mozart.old;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.openmrs.module.epts.etl.model.pojo.generic.AbstractDatabaseObject;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObject;
import org.openmrs.module.epts.etl.utilities.AttDefinedElements;
import org.openmrs.module.epts.etl.utilities.DateAndTimeUtilities;

import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class ParentVO extends AbstractDatabaseObject implements DatabaseObject { 
	private Integer id;
	private String varcharField;
	private String uniqueField;
	private java.util.Date dateField;
 
	public ParentVO() { 
		this.metadata = false;
	} 
 
	public void setId(Integer id){ 
	 	this.id = id;
	}
 
	public Integer getId(){ 
		return this.id;
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
 
	public Integer getObjectId() { 
 		return this.id; 
	} 
 
	public void setObjectId(Integer selfId){ 
		this.id = selfId; 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		super.load(rs);
		if (rs.getObject("id") != null) this.id = rs.getInt("id");
		this.varcharField = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("varchar_field") != null ? rs.getString("varchar_field").trim() : null);
		this.uniqueField = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("unique_field") != null ? rs.getString("unique_field").trim() : null);
		this.dateField =  rs.getTimestamp("date_field") != null ? new java.util.Date( rs.getTimestamp("date_field").getTime() ) : null;
	} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "id"; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithoutObjectId(){ 
 		return "INSERT INTO parent(varchar_field, unique_field, date_field) VALUES( ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithoutObjectId(){ 
 		Object[] params = {this.varcharField, this.uniqueField, this.dateField};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithObjectId(){ 
 		return "INSERT INTO parent(id, varchar_field, unique_field, date_field) VALUES(?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithObjectId(){ 
 		Object[] params = {this.id, this.varcharField, this.uniqueField, this.dateField};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.varcharField, this.uniqueField, this.dateField, this.id};		return params; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE parent SET varchar_field = ?, unique_field = ?, date_field = ? WHERE id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.varcharField != null ? "\""+ utilities.scapeQuotationMarks(varcharField)  +"\"" : null) + "," + (this.uniqueField != null ? "\""+ utilities.scapeQuotationMarks(uniqueField)  +"\"" : null) + "," + (this.dateField != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateField)  +"\"" : null); 
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