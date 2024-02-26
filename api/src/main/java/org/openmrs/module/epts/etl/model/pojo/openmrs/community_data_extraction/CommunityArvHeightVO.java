package org.openmrs.module.epts.etl.model.pojo.openmrs.community_data_extraction;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.openmrs.module.epts.etl.model.pojo.generic.AbstractDatabaseObject;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObject;
import org.openmrs.module.epts.etl.utilities.DateAndTimeUtilities;

import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class CommunityArvHeightVO extends AbstractDatabaseObject implements DatabaseObject { 
	private Integer id;
	private Integer patientId;
	private Double height;
	private java.util.Date heightDate;
 
	public CommunityArvHeightVO() { 
		this.metadata = false;
	} 
 
	public void setId(Integer id){ 
	 	this.id = id;
	}
 
	public Integer getId(){ 
		return this.id;
	}
 
	public void setPatientId(Integer patientId){ 
	 	this.patientId = patientId;
	}
 
	public Integer getPatientId(){ 
		return this.patientId;
	}
 
	public void setHeight(Double height){ 
	 	this.height = height;
	}
 
	public Double getHeight(){ 
		return this.height;
	}
 
	public void setHeightDate(java.util.Date heightDate){ 
	 	this.heightDate = heightDate;
	}


 
	public java.util.Date getHeightDate(){ 
		return this.heightDate;
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
		if (rs.getObject("patient_id") != null) this.patientId = rs.getInt("patient_id");
		if (rs.getObject("height") != null) this.height = rs.getDouble("height");
		this.heightDate =  rs.getTimestamp("height_date") != null ? new java.util.Date( rs.getTimestamp("height_date").getTime() ) : null;
	} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "id"; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithoutObjectId(){ 
 		return "INSERT INTO community_arv_height(patient_id, height, height_date) VALUES( ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithoutObjectId(){ 
 		Object[] params = {this.patientId, this.height, this.heightDate};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithObjectId(){ 
 		return "INSERT INTO community_arv_height(id, patient_id, height, height_date) VALUES(?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithObjectId(){ 
 		Object[] params = {this.id, this.patientId, this.height, this.heightDate};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.patientId, this.height, this.heightDate, this.id};		return params; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE community_arv_height SET patient_id = ?, height = ?, height_date = ? WHERE id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.patientId) + "," + this.height + "," + (this.heightDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(heightDate)  +"\"" : null); 
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
		return "community_arv_height";
	}


}