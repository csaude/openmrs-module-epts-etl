package org.openmrs.module.epts.etl.model.pojo.openmrs.community_data_extraction;

import org.openmrs.module.epts.etl.model.pojo.generic.*; 
 
import org.openmrs.module.epts.etl.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.epts.etl.utilities.AttDefinedElements; 
 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class CommunityDifferentiatedModelVO extends AbstractDatabaseObject implements DatabaseObject { 
	private Integer id;
	private Integer patientId;
	private java.util.Date visitDate;
	private String differentiatedModel;
	private String differentiatedModelStatus;
 
	public CommunityDifferentiatedModelVO() { 
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
 
	public void setVisitDate(java.util.Date visitDate){ 
	 	this.visitDate = visitDate;
	}
 
	public java.util.Date getVisitDate(){ 
		return this.visitDate;
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
 		return this.id; 
	} 
 
	public void setObjectId(Integer selfId){ 
		this.id = selfId; 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		super.load(rs);
		if (rs.getObject("id") != null) this.id = rs.getInt("id");
		if (rs.getObject("patient_id") != null) this.patientId = rs.getInt("patient_id");
		this.visitDate =  rs.getTimestamp("visit_date") != null ? new java.util.Date( rs.getTimestamp("visit_date").getTime() ) : null;
		this.differentiatedModel = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("differentiated_model") != null ? rs.getString("differentiated_model").trim() : null);
		this.differentiatedModelStatus = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("differentiated_model_status") != null ? rs.getString("differentiated_model_status").trim() : null);
	} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "id"; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithoutObjectId(){ 
 		return "INSERT INTO community_differentiated_model(patient_id, visit_date, differentiated_model, differentiated_model_status) VALUES( ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithoutObjectId(){ 
 		Object[] params = {this.patientId, this.visitDate, this.differentiatedModel, this.differentiatedModelStatus};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithObjectId(){ 
 		return "INSERT INTO community_differentiated_model(id, patient_id, visit_date, differentiated_model, differentiated_model_status) VALUES(?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithObjectId(){ 
 		Object[] params = {this.id, this.patientId, this.visitDate, this.differentiatedModel, this.differentiatedModelStatus};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.patientId, this.visitDate, this.differentiatedModel, this.differentiatedModelStatus, this.id};		return params; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE community_differentiated_model SET patient_id = ?, visit_date = ?, differentiated_model = ?, differentiated_model_status = ? WHERE id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.patientId) + "," + (this.visitDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(visitDate)  +"\"" : null) + "," + (this.differentiatedModel != null ? "\""+ utilities.scapeQuotationMarks(differentiatedModel)  +"\"" : null) + "," + (this.differentiatedModelStatus != null ? "\""+ utilities.scapeQuotationMarks(differentiatedModelStatus)  +"\"" : null); 
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
		return "community_differentiated_model";
	}


}