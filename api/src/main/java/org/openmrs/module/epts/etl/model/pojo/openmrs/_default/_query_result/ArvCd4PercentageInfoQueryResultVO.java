package org.openmrs.module.epts.etl.model.pojo.openmrs._default._query_result;

import org.openmrs.module.epts.etl.model.pojo.generic.*; 
 
import org.openmrs.module.epts.etl.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.epts.etl.utilities.AttDefinedElements; 
 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class ArvCd4PercentageInfoQueryResultVO extends AbstractDatabaseObject implements DatabaseObject { 
	private Double cd4;
	private java.util.Date cd4Date;
 
	public ArvCd4PercentageInfoQueryResultVO() { 
		this.metadata = false;
	} 
 
	public void setCd4(Double cd4){ 
	 	this.cd4 = cd4;
	}
 
	public Double getCd4(){ 
		return this.cd4;
	}
 
	public void setCd4Date(java.util.Date cd4Date){ 
	 	this.cd4Date = cd4Date;
	}


 
	public java.util.Date getCd4Date(){ 
		return this.cd4Date;
	}
 
	public Integer getObjectId() { 
 		return 0; 
	} 
 
	public void setObjectId(Integer selfId){ 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		super.load(rs);
		if (rs.getObject("cd4") != null) this.cd4 = rs.getDouble("cd4");
		this.cd4Date =  rs.getTimestamp("cd4_date") != null ? new java.util.Date( rs.getTimestamp("cd4_date").getTime() ) : null;
	} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return null; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithoutObjectId(){ 
 		return "INSERT INTO arv_cd4_percentage_info(cd4, cd4_date) VALUES( ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithoutObjectId(){ 
 		Object[] params = {this.cd4, this.cd4Date};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithObjectId(){ 
 		return "INSERT INTO arv_cd4_percentage_info(cd4, cd4_date) VALUES( ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithObjectId(){ 
 		Object[] params = {this.cd4, this.cd4Date};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.cd4, this.cd4Date, null};		return params; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE arv_cd4_percentage_info SET cd4 = ?, cd4_date = ? WHERE null = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.cd4) + "," + (this.cd4Date != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(cd4Date)  +"\"" : null); 
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
		return "arv_cd4_percentage_info";
	}


}