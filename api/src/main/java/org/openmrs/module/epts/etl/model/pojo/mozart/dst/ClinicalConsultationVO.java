package org.openmrs.module.epts.etl.model.pojo.mozart.dst;

import org.openmrs.module.epts.etl.model.pojo.generic.*; 
 
import org.openmrs.module.epts.etl.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.epts.etl.utilities.AttDefinedElements; 
 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class ClinicalConsultationVO extends AbstractDatabaseObject implements DatabaseObject { 
	private Integer id;
	private String encounterUuid;
	private java.util.Date consultationDate;
	private java.util.Date scheduledDate;
	private Double bpDiastolic;
	private Double bpSystolic;
	private Integer whoStaging;
	private Double weight;
	private Double height;
	private Double armCircumference;
	private Integer nutritionalGrade;
	private Integer formId;
	private Integer encounterType;
	private String patientUuid;
	private java.util.Date createdDate;
	private java.util.Date encounterDate;
	private java.util.Date changeDate;
	private String locationUuid;
	private String sourceDatabase;
 
	public ClinicalConsultationVO() { 
		this.metadata = false;
	} 
 
	public void setId(Integer id){ 
	 	this.id = id;
	}
 
	public Integer getId(){ 
		return this.id;
	}
 
	public void setEncounterUuid(String encounterUuid){ 
	 	this.encounterUuid = encounterUuid;
	}
 
	public String getEncounterUuid(){ 
		return this.encounterUuid;
	}
 
	public void setConsultationDate(java.util.Date consultationDate){ 
	 	this.consultationDate = consultationDate;
	}
 
	public java.util.Date getConsultationDate(){ 
		return this.consultationDate;
	}
 
	public void setScheduledDate(java.util.Date scheduledDate){ 
	 	this.scheduledDate = scheduledDate;
	}
 
	public java.util.Date getScheduledDate(){ 
		return this.scheduledDate;
	}
 
	public void setBpDiastolic(Double bpDiastolic){ 
	 	this.bpDiastolic = bpDiastolic;
	}
 
	public Double getBpDiastolic(){ 
		return this.bpDiastolic;
	}
 
	public void setBpSystolic(Double bpSystolic){ 
	 	this.bpSystolic = bpSystolic;
	}
 
	public Double getBpSystolic(){ 
		return this.bpSystolic;
	}
 
	public void setWhoStaging(Integer whoStaging){ 
	 	this.whoStaging = whoStaging;
	}
 
	public Integer getWhoStaging(){ 
		return this.whoStaging;
	}
 
	public void setWeight(Double weight){ 
	 	this.weight = weight;
	}
 
	public Double getWeight(){ 
		return this.weight;
	}
 
	public void setHeight(Double height){ 
	 	this.height = height;
	}
 
	public Double getHeight(){ 
		return this.height;
	}
 
	public void setArmCircumference(Double armCircumference){ 
	 	this.armCircumference = armCircumference;
	}
 
	public Double getArmCircumference(){ 
		return this.armCircumference;
	}
 
	public void setNutritionalGrade(Integer nutritionalGrade){ 
	 	this.nutritionalGrade = nutritionalGrade;
	}
 
	public Integer getNutritionalGrade(){ 
		return this.nutritionalGrade;
	}
 
	public void setFormId(Integer formId){ 
	 	this.formId = formId;
	}
 
	public Integer getFormId(){ 
		return this.formId;
	}
 
	public void setEncounterType(Integer encounterType){ 
	 	this.encounterType = encounterType;
	}
 
	public Integer getEncounterType(){ 
		return this.encounterType;
	}
 
	public void setPatientUuid(String patientUuid){ 
	 	this.patientUuid = patientUuid;
	}
 
	public String getPatientUuid(){ 
		return this.patientUuid;
	}
 
	public void setCreatedDate(java.util.Date createdDate){ 
	 	this.createdDate = createdDate;
	}
 
	public java.util.Date getCreatedDate(){ 
		return this.createdDate;
	}
 
	public void setEncounterDate(java.util.Date encounterDate){ 
	 	this.encounterDate = encounterDate;
	}
 
	public java.util.Date getEncounterDate(){ 
		return this.encounterDate;
	}
 
	public void setChangeDate(java.util.Date changeDate){ 
	 	this.changeDate = changeDate;
	}
 
	public java.util.Date getChangeDate(){ 
		return this.changeDate;
	}
 
	public void setLocationUuid(String locationUuid){ 
	 	this.locationUuid = locationUuid;
	}
 
	public String getLocationUuid(){ 
		return this.locationUuid;
	}
 
	public void setSourceDatabase(String sourceDatabase){ 
	 	this.sourceDatabase = sourceDatabase;
	}


 
	public String getSourceDatabase(){ 
		return this.sourceDatabase;
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
		this.encounterUuid = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("encounter_uuid") != null ? rs.getString("encounter_uuid").trim() : null);
		this.consultationDate =  rs.getTimestamp("consultation_date") != null ? new java.util.Date( rs.getTimestamp("consultation_date").getTime() ) : null;
		this.scheduledDate =  rs.getTimestamp("scheduled_date") != null ? new java.util.Date( rs.getTimestamp("scheduled_date").getTime() ) : null;
		if (rs.getObject("bp_diastolic") != null) this.bpDiastolic = rs.getDouble("bp_diastolic");
		if (rs.getObject("bp_systolic") != null) this.bpSystolic = rs.getDouble("bp_systolic");
		if (rs.getObject("who_staging") != null) this.whoStaging = rs.getInt("who_staging");
		if (rs.getObject("weight") != null) this.weight = rs.getDouble("weight");
		if (rs.getObject("height") != null) this.height = rs.getDouble("height");
		if (rs.getObject("arm_circumference") != null) this.armCircumference = rs.getDouble("arm_circumference");
		if (rs.getObject("nutritional_grade") != null) this.nutritionalGrade = rs.getInt("nutritional_grade");
		if (rs.getObject("form_id") != null) this.formId = rs.getInt("form_id");
		if (rs.getObject("encounter_type") != null) this.encounterType = rs.getInt("encounter_type");
		this.patientUuid = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("patient_uuid") != null ? rs.getString("patient_uuid").trim() : null);
		this.createdDate =  rs.getTimestamp("created_date") != null ? new java.util.Date( rs.getTimestamp("created_date").getTime() ) : null;
		this.encounterDate =  rs.getTimestamp("encounter_date") != null ? new java.util.Date( rs.getTimestamp("encounter_date").getTime() ) : null;
		this.changeDate =  rs.getTimestamp("change_date") != null ? new java.util.Date( rs.getTimestamp("change_date").getTime() ) : null;
		this.locationUuid = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("location_uuid") != null ? rs.getString("location_uuid").trim() : null);
		this.sourceDatabase = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("source_database") != null ? rs.getString("source_database").trim() : null);
	} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "id"; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithoutObjectId(){ 
 		return "INSERT INTO clinical_consultation(encounter_uuid, consultation_date, scheduled_date, bp_diastolic, bp_systolic, who_staging, weight, height, arm_circumference, nutritional_grade, form_id, encounter_type, patient_uuid, created_date, encounter_date, change_date, location_uuid, source_database) VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithoutObjectId(){ 
 		Object[] params = {this.encounterUuid, this.consultationDate, this.scheduledDate, this.bpDiastolic, this.bpSystolic, this.whoStaging, this.weight, this.height, this.armCircumference, this.nutritionalGrade, this.formId, this.encounterType, this.patientUuid, this.createdDate, this.encounterDate, this.changeDate, this.locationUuid, this.sourceDatabase};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithObjectId(){ 
 		return "INSERT INTO clinical_consultation(id, encounter_uuid, consultation_date, scheduled_date, bp_diastolic, bp_systolic, who_staging, weight, height, arm_circumference, nutritional_grade, form_id, encounter_type, patient_uuid, created_date, encounter_date, change_date, location_uuid, source_database) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithObjectId(){ 
 		Object[] params = {this.id, this.encounterUuid, this.consultationDate, this.scheduledDate, this.bpDiastolic, this.bpSystolic, this.whoStaging, this.weight, this.height, this.armCircumference, this.nutritionalGrade, this.formId, this.encounterType, this.patientUuid, this.createdDate, this.encounterDate, this.changeDate, this.locationUuid, this.sourceDatabase};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.encounterUuid, this.consultationDate, this.scheduledDate, this.bpDiastolic, this.bpSystolic, this.whoStaging, this.weight, this.height, this.armCircumference, this.nutritionalGrade, this.formId, this.encounterType, this.patientUuid, this.createdDate, this.encounterDate, this.changeDate, this.locationUuid, this.sourceDatabase, this.id};		return params; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE clinical_consultation SET encounter_uuid = ?, consultation_date = ?, scheduled_date = ?, bp_diastolic = ?, bp_systolic = ?, who_staging = ?, weight = ?, height = ?, arm_circumference = ?, nutritional_grade = ?, form_id = ?, encounter_type = ?, patient_uuid = ?, created_date = ?, encounter_date = ?, change_date = ?, location_uuid = ?, source_database = ? WHERE id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.encounterUuid != null ? "\""+ utilities.scapeQuotationMarks(encounterUuid)  +"\"" : null) + "," + (this.consultationDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(consultationDate)  +"\"" : null) + "," + (this.scheduledDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(scheduledDate)  +"\"" : null) + "," + (this.bpDiastolic) + "," + (this.bpSystolic) + "," + (this.whoStaging) + "," + (this.weight) + "," + (this.height) + "," + (this.armCircumference) + "," + (this.nutritionalGrade) + "," + (this.formId) + "," + (this.encounterType) + "," + (this.patientUuid != null ? "\""+ utilities.scapeQuotationMarks(patientUuid)  +"\"" : null) + "," + (this.createdDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(createdDate)  +"\"" : null) + "," + (this.encounterDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(encounterDate)  +"\"" : null) + "," + (this.changeDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(changeDate)  +"\"" : null) + "," + (this.locationUuid != null ? "\""+ utilities.scapeQuotationMarks(locationUuid)  +"\"" : null) + "," + (this.sourceDatabase != null ? "\""+ utilities.scapeQuotationMarks(sourceDatabase)  +"\"" : null); 
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
		return "clinical_consultation";
	}


}