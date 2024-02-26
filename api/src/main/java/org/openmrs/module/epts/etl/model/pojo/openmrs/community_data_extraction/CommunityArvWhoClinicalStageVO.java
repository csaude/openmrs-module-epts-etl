package org.openmrs.module.epts.etl.model.pojo.openmrs.community_data_extraction;

import org.openmrs.module.epts.etl.model.pojo.generic.*; 
 
import org.openmrs.module.epts.etl.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.epts.etl.utilities.AttDefinedElements; 
 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class CommunityArvWhoClinicalStageVO extends AbstractDatabaseObject implements DatabaseObject { 
	private Integer id;
	private Integer patientId;
	private String whoStage;
	private java.util.Date whoStageDate;
 
	public CommunityArvWhoClinicalStageVO() { 
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
 
	public void setWhoStage(String whoStage){ 
	 	this.whoStage = whoStage;
	}
 
	public String getWhoStage(){ 
		return this.whoStage;
	}
 
	public void setWhoStageDate(java.util.Date whoStageDate){ 
	 	this.whoStageDate = whoStageDate;
	}


 
	public java.util.Date getWhoStageDate(){ 
		return this.whoStageDate;
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
		this.whoStage = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("who_stage") != null ? rs.getString("who_stage").trim() : null);
		this.whoStageDate =  rs.getTimestamp("who_stage_date") != null ? new java.util.Date( rs.getTimestamp("who_stage_date").getTime() ) : null;
	} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "id"; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithoutObjectId(){ 
 		return "INSERT INTO community_arv_who_clinical_stage(patient_id, who_stage, who_stage_date) VALUES( ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithoutObjectId(){ 
 		Object[] params = {this.patientId, this.whoStage, this.whoStageDate};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithObjectId(){ 
 		return "INSERT INTO community_arv_who_clinical_stage(id, patient_id, who_stage, who_stage_date) VALUES(?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithObjectId(){ 
 		Object[] params = {this.id, this.patientId, this.whoStage, this.whoStageDate};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.patientId, this.whoStage, this.whoStageDate, this.id};		return params; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE community_arv_who_clinical_stage SET patient_id = ?, who_stage = ?, who_stage_date = ? WHERE id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.patientId) + "," + (this.whoStage != null ? "\""+ utilities.scapeQuotationMarks(whoStage)  +"\"" : null) + "," + (this.whoStageDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(whoStageDate)  +"\"" : null); 
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
		return "community_arv_who_clinical_stage";
	}


}