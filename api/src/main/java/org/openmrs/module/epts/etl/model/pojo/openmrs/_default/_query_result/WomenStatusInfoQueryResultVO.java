package org.openmrs.module.epts.etl.model.pojo.openmrs._default._query_result;

import org.openmrs.module.epts.etl.model.pojo.generic.*; 
 
import org.openmrs.module.epts.etl.utilities.AttDefinedElements; 
 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class WomenStatusInfoQueryResultVO extends AbstractDatabaseObject implements DatabaseObject { 
	private String womenStatus;
 
	public WomenStatusInfoQueryResultVO() { 
		this.metadata = false;
	} 
 
	public void setWomenStatus(String womenStatus){ 
	 	this.womenStatus = womenStatus;
	}


 
	public String getWomenStatus(){ 
		return this.womenStatus;
	}
 
	@Override
	public void load(ResultSet rs) throws SQLException{ 
		super.load(rs);
 
this.womenStatus = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("women_status") != null ? rs.getString("women_status").trim() : null);
	} 
 
	@JsonIgnore
	@Override
	public String getInsertSQLWithoutObjectId(){ 
 		return "INSERT INTO women_status_info(women_status) VALUES( ?);"; 
	} 
 
	@JsonIgnore
	@Override
	public String getInsertSQLWithObjectId(){ 
 		return "INSERT INTO women_status_info(women_status) VALUES( ?);"; 
	} 
 
	@JsonIgnore
	@Override
	public Object[]  getInsertParamsWithoutObjectId(){ 
 		Object[] params = {this.womenStatus};
		return params; 
	} 
 
	@JsonIgnore
	@Override
	public Object[]  getInsertParamsWithObjectId(){ 
 		Object[] params = {this.womenStatus};
		return params; 
	} 
 
	@JsonIgnore
	@Override
	public Object[]  getUpdateParams(){ 
 		throw new RuntimeException("Impossible auto update command! No primary key is defined for table object!");	} 
 
	@JsonIgnore
	@Override
	public String getUpdateSQL(){ 
 		throw new RuntimeException("Impossible auto update command! No primary key is defined for table object!");	} 
 
	@JsonIgnore
	@Override
	public String generateInsertValuesWithoutObjectId(){ 
 		return ""+(this.womenStatus != null ? "\""+ utilities.scapeQuotationMarks(womenStatus)  +"\"" : null); 
	} 
 
	@JsonIgnore
	@Override
	public String generateInsertValuesWithObjectId(){ 
 		return ""+(this.womenStatus != null ? "\""+ utilities.scapeQuotationMarks(womenStatus)  +"\"" : null); 
	} 
 
	@Override
	public boolean hasParents() {
		return false;
	}

	@Override
	public Integer getParentValue(String parentAttName) {

		throw new RuntimeException("No found parent for: " + parentAttName);	}

	@Override
	public String generateTableName() {
		return "women_status_info";
	}


}