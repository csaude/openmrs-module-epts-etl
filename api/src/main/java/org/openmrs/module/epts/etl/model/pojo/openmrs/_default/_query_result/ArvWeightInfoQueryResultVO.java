package org.openmrs.module.epts.etl.model.pojo.openmrs._default._query_result;

import org.openmrs.module.epts.etl.model.pojo.generic.*; 
 
import org.openmrs.module.epts.etl.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.epts.etl.utilities.AttDefinedElements; 
 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class ArvWeightInfoQueryResultVO extends AbstractDatabaseObject implements DatabaseObject { 
	private double Weight;
 
	public ArvWeightInfoQueryResultVO() { 
		this.metadata = false;
	} 
 
	public void setWeight(double Weight){ 
	 	this.Weight = Weight;
	}


 
	public double getWeight(){ 
		return this.Weight;
	}
 
	public Integer getObjectId() { 
 		return 0; 
	} 
 
	public void setObjectId(Integer selfId){ 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		super.load(rs);
		this.Weight = rs.getDouble("Weight");
	} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return null; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithoutObjectId(){ 
 		return "INSERT INTO arv_weight_info(Weight) VALUES( ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithoutObjectId(){ 
 		Object[] params = {this.Weight};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithObjectId(){ 
 		return "INSERT INTO arv_weight_info(Weight) VALUES( ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithObjectId(){ 
 		Object[] params = {this.Weight};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.Weight, null};		return params; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE arv_weight_info SET Weight = ? WHERE null = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.Weight); 
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
		return "arv_weight_info";
	}


}