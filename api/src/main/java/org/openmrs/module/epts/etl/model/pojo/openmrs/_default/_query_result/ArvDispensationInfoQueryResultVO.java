package org.openmrs.module.epts.etl.model.pojo.openmrs._default._query_result;

import org.openmrs.module.epts.etl.model.pojo.generic.*; 
 
import org.openmrs.module.epts.etl.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.epts.etl.utilities.AttDefinedElements; 
 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class ArvDispensationInfoQueryResultVO extends AbstractDatabaseObject implements DatabaseObject { 
	private String dispensationType;
 
	public ArvDispensationInfoQueryResultVO() { 
		this.metadata = false;
	} 
 
	public void setDispensationType(String dispensationType){ 
	 	this.dispensationType = dispensationType;
	}


 
	public String getDispensationType(){ 
		return this.dispensationType;
	}
 
	public Integer getObjectId() { 
 		return 0; 
	} 
 
	public void setObjectId(Integer selfId){ 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		super.load(rs);
		this.dispensationType = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("dispensation_type") != null ? rs.getString("dispensation_type").trim() : null);
	} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return null; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithoutObjectId(){ 
 		return "INSERT INTO arv_dispensation_info(dispensation_type) VALUES( ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithoutObjectId(){ 
 		Object[] params = {this.dispensationType};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithObjectId(){ 
 		return "INSERT INTO arv_dispensation_info(dispensation_type) VALUES( ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithObjectId(){ 
 		Object[] params = {this.dispensationType};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.dispensationType, null};		return params; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE arv_dispensation_info SET dispensation_type = ? WHERE null = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.dispensationType != null ? "\""+ utilities.scapeQuotationMarks(dispensationType)  +"\"" : null); 
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
		return "arv_dispensation_info";
	}


}