package org.openmrs.module.epts.etl.model.pojo.openmrs._default._query_result;

import org.openmrs.module.epts.etl.model.pojo.generic.*; 
 
import org.openmrs.module.epts.etl.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.epts.etl.utilities.AttDefinedElements; 
 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class DifferentiatedModelInfoQueryResultVO extends AbstractDatabaseObject implements DatabaseObject { 
	private String differentiatedModel;
	private String differentiatedModelStatus;
 
	public DifferentiatedModelInfoQueryResultVO() { 
		this.metadata = false;
	} 
 
	public void setDifferentiatedModel(String differentiatedModel){ 
	 	this.differentiatedModel = differentiatedModel;
	}
 
	public String getDifferentiatedModel(){ 
		return this.differentiatedModel;
	}
 
	public void setDifferentiatedModelStatus(String differentiatedModelStatus){ 
	 	this.differentiatedModelStatus = differentiatedModelStatus;
	}


 
	public String getDifferentiatedModelStatus(){ 
		return this.differentiatedModelStatus;
	}
 
	public Integer getObjectId() { 
 		return 0; 
	} 
 
	public void setObjectId(Integer selfId){ 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		super.load(rs);
		this.differentiatedModel = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("differentiated_model") != null ? rs.getString("differentiated_model").trim() : null);
		this.differentiatedModelStatus = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("differentiated_model_status") != null ? rs.getString("differentiated_model_status").trim() : null);
	} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return null; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithoutObjectId(){ 
 		return "INSERT INTO differentiated_model_info(differentiated_model, differentiated_model_status) VALUES( ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithoutObjectId(){ 
 		Object[] params = {this.differentiatedModel, this.differentiatedModelStatus};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithObjectId(){ 
 		return "INSERT INTO differentiated_model_info(differentiated_model, differentiated_model_status) VALUES( ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithObjectId(){ 
 		Object[] params = {this.differentiatedModel, this.differentiatedModelStatus};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.differentiatedModel, this.differentiatedModelStatus, null};		return params; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE differentiated_model_info SET differentiated_model = ?, differentiated_model_status = ? WHERE null = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.differentiatedModel != null ? "\""+ utilities.scapeQuotationMarks(differentiatedModel)  +"\"" : null) + "," + (this.differentiatedModelStatus != null ? "\""+ utilities.scapeQuotationMarks(differentiatedModelStatus)  +"\"" : null); 
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
		return "differentiated_model_info";
	}


}