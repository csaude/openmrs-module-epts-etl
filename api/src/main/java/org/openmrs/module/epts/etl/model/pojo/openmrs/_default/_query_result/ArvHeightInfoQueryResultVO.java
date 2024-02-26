package org.openmrs.module.epts.etl.model.pojo.openmrs._default._query_result;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.openmrs.module.epts.etl.model.pojo.generic.AbstractDatabaseObject;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObject;
import org.openmrs.module.epts.etl.utilities.DateAndTimeUtilities;

import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class ArvHeightInfoQueryResultVO extends AbstractDatabaseObject implements DatabaseObject { 
	private double height;
	private java.util.Date heightDate;
 
	public ArvHeightInfoQueryResultVO() { 
		this.metadata = false;
	} 
 
	public void setHeight(double height){ 
	 	this.height = height;
	}
 
	public double getHeight(){ 
		return this.height;
	}
 
	public void setHeightDate(java.util.Date heightDate){ 
	 	this.heightDate = heightDate;
	}


 
	public java.util.Date getHeightDate(){ 
		return this.heightDate;
	}
 
	public Integer getObjectId() { 
 		return 0; 
	} 
 
	public void setObjectId(Integer selfId){ 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		super.load(rs);
		this.height = rs.getDouble("height");
		this.heightDate =  rs.getTimestamp("height_date") != null ? new java.util.Date( rs.getTimestamp("height_date").getTime() ) : null;
	} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return null; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithoutObjectId(){ 
 		return "INSERT INTO arv_height_info(patient_id, height, height_date) VALUES( ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithoutObjectId(){ 
 		Object[] params = {this.height, this.heightDate};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithObjectId(){ 
 		return "INSERT INTO arv_height_info(patient_id, height, height_date) VALUES( ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithObjectId(){ 
 		Object[] params = {this.height, this.heightDate};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.height, this.heightDate, null};		return params; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE arv_height_info SET patient_id = ?, height = ?, height_date = ? WHERE null = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+ (this.height) + "," + (this.heightDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(heightDate)  +"\"" : null); 
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
		return "arv_height_info";
	}


}