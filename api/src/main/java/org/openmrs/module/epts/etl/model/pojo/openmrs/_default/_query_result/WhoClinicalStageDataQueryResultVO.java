package org.openmrs.module.epts.etl.model.pojo.openmrs._default._query_result;

import org.openmrs.module.epts.etl.model.pojo.generic.*; 
 
import org.openmrs.module.epts.etl.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.epts.etl.utilities.AttDefinedElements; 
 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class WhoClinicalStageDataQueryResultVO extends AbstractDatabaseObject implements DatabaseObject { 
	private String whoStage;
 
	public WhoClinicalStageDataQueryResultVO() { 
		this.metadata = false;
	} 
 
	public void setWhoStage(String whoStage){ 
	 	this.whoStage = whoStage;
	}


 
	public String getWhoStage(){ 
		return this.whoStage;
	}
 
	public Integer getObjectId() { 
 		return 0; 
	} 
 
	public void setObjectId(Integer selfId){ 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		super.load(rs);
		this.whoStage = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("who_stage") != null ? rs.getString("who_stage").trim() : null);
	} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return null; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithoutObjectId(){ 
 		return "INSERT INTO who_clinical_stage_data(who_stage) VALUES( ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithoutObjectId(){ 
 		Object[] params = {this.whoStage};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithObjectId(){ 
 		return "INSERT INTO who_clinical_stage_data(who_stage) VALUES( ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithObjectId(){ 
 		Object[] params = {this.whoStage};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.whoStage, null};		return params; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE who_clinical_stage_data SET who_stage = ? WHERE null = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.whoStage != null ? "\""+ utilities.scapeQuotationMarks(whoStage)  +"\"" : null); 
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
		return "who_clinical_stage_data";
	}


}