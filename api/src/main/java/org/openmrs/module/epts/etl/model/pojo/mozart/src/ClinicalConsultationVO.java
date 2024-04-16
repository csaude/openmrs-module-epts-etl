package org.openmrs.module.epts.etl.model.pojo.mozart.src;

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
	
	public ClinicalConsultationVO() {
		this.metadata = false;
	}
	
	public void setId(Integer id) {
		this.id = id;
	}
	
	public Integer getId() {
		return this.id;
	}
	
	public void setEncounterUuid(String encounterUuid) {
		this.encounterUuid = encounterUuid;
	}
	
	public String getEncounterUuid() {
		return this.encounterUuid;
	}
	
	public void setConsultationDate(java.util.Date consultationDate) {
		this.consultationDate = consultationDate;
	}
	
	public java.util.Date getConsultationDate() {
		return this.consultationDate;
	}
	
	public void setScheduledDate(java.util.Date scheduledDate) {
		this.scheduledDate = scheduledDate;
	}
	
	public java.util.Date getScheduledDate() {
		return this.scheduledDate;
	}
	
	public void setBpDiastolic(Double bpDiastolic) {
		this.bpDiastolic = bpDiastolic;
	}
	
	public Double getBpDiastolic() {
		return this.bpDiastolic;
	}
	
	public void setBpSystolic(Double bpSystolic) {
		this.bpSystolic = bpSystolic;
	}
	
	public Double getBpSystolic() {
		return this.bpSystolic;
	}
	
	public void setWhoStaging(Integer whoStaging) {
		this.whoStaging = whoStaging;
	}
	
	public Integer getWhoStaging() {
		return this.whoStaging;
	}
	
	public void setWeight(Double weight) {
		this.weight = weight;
	}
	
	public Double getWeight() {
		return this.weight;
	}
	
	public void setHeight(Double height) {
		this.height = height;
	}
	
	public Double getHeight() {
		return this.height;
	}
	
	public void setArmCircumference(Double armCircumference) {
		this.armCircumference = armCircumference;
	}
	
	public Double getArmCircumference() {
		return this.armCircumference;
	}
	
	public void setNutritionalGrade(Integer nutritionalGrade) {
		this.nutritionalGrade = nutritionalGrade;
	}
	
	public Integer getNutritionalGrade() {
		return this.nutritionalGrade;
	}
	
	@Override
	public void load(ResultSet rs) throws SQLException {
		super.load(rs);
		
		if (rs.getObject("id") != null)
			this.id = rs.getInt("id");
		this.encounterUuid = AttDefinedElements.removeStrangeCharactersOnString(
		    rs.getString("encounter_uuid") != null ? rs.getString("encounter_uuid").trim() : null);
		this.consultationDate = rs.getTimestamp("consultation_date") != null
		        ? new java.util.Date(rs.getTimestamp("consultation_date").getTime())
		        : null;
		this.scheduledDate = rs.getTimestamp("scheduled_date") != null
		        ? new java.util.Date(rs.getTimestamp("scheduled_date").getTime())
		        : null;
		if (rs.getObject("bp_diastolic") != null)
			this.bpDiastolic = rs.getDouble("bp_diastolic");
		if (rs.getObject("bp_systolic") != null)
			this.bpSystolic = rs.getDouble("bp_systolic");
		if (rs.getObject("who_staging") != null)
			this.whoStaging = rs.getInt("who_staging");
		if (rs.getObject("weight") != null)
			this.weight = rs.getDouble("weight");
		if (rs.getObject("height") != null)
			this.height = rs.getDouble("height");
		if (rs.getObject("arm_circumference") != null)
			this.armCircumference = rs.getDouble("arm_circumference");
		if (rs.getObject("nutritional_grade") != null)
			this.nutritionalGrade = rs.getInt("nutritional_grade");
	}
	
	@JsonIgnore
	@Override
	public String getInsertSQLWithoutObjectId() {
		return "INSERT INTO clinical_consultation(encounter_uuid, consultation_date, scheduled_date, bp_diastolic, bp_systolic, who_staging, weight, height, arm_circumference, nutritional_grade) VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
	}
	
	@JsonIgnore
	@Override
	public String getInsertSQLWithObjectId() {
		return "INSERT INTO clinical_consultation(id, encounter_uuid, consultation_date, scheduled_date, bp_diastolic, bp_systolic, who_staging, weight, height, arm_circumference, nutritional_grade) VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
	}
	
	@JsonIgnore
	@Override
	public Object[] getInsertParamsWithoutObjectId() {
		Object[] params = { this.encounterUuid, this.consultationDate, this.scheduledDate, this.bpDiastolic, this.bpSystolic,
		        this.whoStaging, this.weight, this.height, this.armCircumference, this.nutritionalGrade };
		return params;
	}
	
	@JsonIgnore
	@Override
	public Object[] getInsertParamsWithObjectId() {
		Object[] params = { this.id, this.encounterUuid, this.consultationDate, this.scheduledDate, this.bpDiastolic,
		        this.bpSystolic, this.whoStaging, this.weight, this.height, this.armCircumference, this.nutritionalGrade };
		return params;
	}
	
	@JsonIgnore
	@Override
	public Object[] getUpdateParams() {
		Object[] params = { this.id, this.encounterUuid, this.consultationDate, this.scheduledDate, this.bpDiastolic,
		        this.bpSystolic, this.whoStaging, this.weight, this.height, this.armCircumference, this.nutritionalGrade,
		        this.id };
		return params;
	}
	
	@JsonIgnore
	@Override
	public String getUpdateSQL() {
		return "UPDATE clinical_consultation SET id = ?, encounter_uuid = ?, consultation_date = ?, scheduled_date = ?, bp_diastolic = ?, bp_systolic = ?, who_staging = ?, weight = ?, height = ?, arm_circumference = ?, nutritional_grade = ? WHERE id = ? ";
	}
	
	@JsonIgnore
	@Override
	public String generateInsertValuesWithoutObjectId() {
		return "" + (this.encounterUuid != null ? "\"" + utilities.scapeQuotationMarks(encounterUuid) + "\"" : null) + ","
		        + (this.consultationDate != null
		                ? "\"" + DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(consultationDate) + "\""
		                : null)
		        + ","
		        + (this.scheduledDate != null ? "\"" + DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(scheduledDate) + "\""
		                : null)
		        + "," + (this.bpDiastolic) + "," + (this.bpSystolic) + "," + (this.whoStaging) + "," + (this.weight) + ","
		        + (this.height) + "," + (this.armCircumference) + "," + (this.nutritionalGrade);
	}
	
	@JsonIgnore
	@Override
	public String generateInsertValuesWithObjectId() {
		return "" + (this.id) + ","
		        + (this.encounterUuid != null ? "\"" + utilities.scapeQuotationMarks(encounterUuid) + "\"" : null) + ","
		        + (this.consultationDate != null
		                ? "\"" + DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(consultationDate) + "\""
		                : null)
		        + ","
		        + (this.scheduledDate != null ? "\"" + DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(scheduledDate) + "\""
		                : null)
		        + "," + (this.bpDiastolic) + "," + (this.bpSystolic) + "," + (this.whoStaging) + "," + (this.weight) + ","
		        + (this.height) + "," + (this.armCircumference) + "," + (this.nutritionalGrade);
	}
	
	@Override
	public boolean hasParents() {
		return false;
	}
	
	@Override
	public Integer getParentValue(String parentAttName) {
		
		throw new RuntimeException("No found parent for: " + parentAttName);
	}
	
	@Override
	public String generateTableName() {
		return "clinical_consultation";
	}
	
}
