package org.openmrs.module.epts.etl.model.pojo.mozart.partitioned;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.openmrs.module.epts.etl.model.pojo.generic.AbstractDatabaseObject;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObject;
import org.openmrs.module.epts.etl.utilities.AttDefinedElements;
import org.openmrs.module.epts.etl.utilities.DateAndTimeUtilities;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class FamilyPlanningVO extends AbstractDatabaseObject implements DatabaseObject {
	
	private String encounterUuid;
	
	private Integer fpConceptId;
	
	private java.util.Date fpDate;
	
	private Integer fpMethod;
	
	private String fpUuid;
	
	private Integer formId;
	
	private Integer encounterType;
	
	private String patientUuid;
	
	private java.util.Date createdDate;
	
	private java.util.Date encounterDate;
	
	private java.util.Date changeDate;
	
	private String locationUuid;
	
	private String sourceDatabase;
	
	public FamilyPlanningVO() {
		this.metadata = false;
	}
	
	public void setEncounterUuid(String encounterUuid) {
		this.encounterUuid = encounterUuid;
	}
	
	public String getEncounterUuid() {
		return this.encounterUuid;
	}
	
	public void setFpConceptId(Integer fpConceptId) {
		this.fpConceptId = fpConceptId;
	}
	
	public Integer getFpConceptId() {
		return this.fpConceptId;
	}
	
	public void setFpDate(java.util.Date fpDate) {
		this.fpDate = fpDate;
	}
	
	public java.util.Date getFpDate() {
		return this.fpDate;
	}
	
	public void setFpMethod(Integer fpMethod) {
		this.fpMethod = fpMethod;
	}
	
	public Integer getFpMethod() {
		return this.fpMethod;
	}
	
	public void setFpUuid(String fpUuid) {
		this.fpUuid = fpUuid;
	}
	
	public String getFpUuid() {
		return this.fpUuid;
	}
	
	public void setFormId(Integer formId) {
		this.formId = formId;
	}
	
	public Integer getFormId() {
		return this.formId;
	}
	
	public void setEncounterType(Integer encounterType) {
		this.encounterType = encounterType;
	}
	
	public Integer getEncounterType() {
		return this.encounterType;
	}
	
	public void setPatientUuid(String patientUuid) {
		this.patientUuid = patientUuid;
	}
	
	public String getPatientUuid() {
		return this.patientUuid;
	}
	
	public void setCreatedDate(java.util.Date createdDate) {
		this.createdDate = createdDate;
	}
	
	public java.util.Date getCreatedDate() {
		return this.createdDate;
	}
	
	public void setEncounterDate(java.util.Date encounterDate) {
		this.encounterDate = encounterDate;
	}
	
	public java.util.Date getEncounterDate() {
		return this.encounterDate;
	}
	
	public void setChangeDate(java.util.Date changeDate) {
		this.changeDate = changeDate;
	}
	
	public java.util.Date getChangeDate() {
		return this.changeDate;
	}
	
	public void setLocationUuid(String locationUuid) {
		this.locationUuid = locationUuid;
	}
	
	public String getLocationUuid() {
		return this.locationUuid;
	}
	
	public void setSourceDatabase(String sourceDatabase) {
		this.sourceDatabase = sourceDatabase;
	}
	
	public String getSourceDatabase() {
		return this.sourceDatabase;
	}
	
	@Override
	public void load(ResultSet rs) throws SQLException {
		super.load(rs);
		
		this.encounterUuid = AttDefinedElements.removeStrangeCharactersOnString(
		    rs.getString("encounter_uuid") != null ? rs.getString("encounter_uuid").trim() : null);
		if (rs.getObject("fp_concept_id") != null)
			this.fpConceptId = rs.getInt("fp_concept_id");
		this.fpDate = rs.getTimestamp("fp_date") != null ? new java.util.Date(rs.getTimestamp("fp_date").getTime()) : null;
		if (rs.getObject("fp_method") != null)
			this.fpMethod = rs.getInt("fp_method");
		this.fpUuid = AttDefinedElements
		        .removeStrangeCharactersOnString(rs.getString("fp_uuid") != null ? rs.getString("fp_uuid").trim() : null);
		if (rs.getObject("form_id") != null)
			this.formId = rs.getInt("form_id");
		if (rs.getObject("encounter_type") != null)
			this.encounterType = rs.getInt("encounter_type");
		this.patientUuid = AttDefinedElements.removeStrangeCharactersOnString(
		    rs.getString("patient_uuid") != null ? rs.getString("patient_uuid").trim() : null);
		this.createdDate = rs.getTimestamp("created_date") != null
		        ? new java.util.Date(rs.getTimestamp("created_date").getTime())
		        : null;
		this.encounterDate = rs.getTimestamp("encounter_date") != null
		        ? new java.util.Date(rs.getTimestamp("encounter_date").getTime())
		        : null;
		this.changeDate = rs.getTimestamp("change_date") != null
		        ? new java.util.Date(rs.getTimestamp("change_date").getTime())
		        : null;
		this.locationUuid = AttDefinedElements.removeStrangeCharactersOnString(
		    rs.getString("location_uuid") != null ? rs.getString("location_uuid").trim() : null);
		this.sourceDatabase = AttDefinedElements.removeStrangeCharactersOnString(
		    rs.getString("source_database") != null ? rs.getString("source_database").trim() : null);
	}
	
	@JsonIgnore
	@Override
	public String getInsertSQLWithoutObjectId() {
		return "INSERT INTO family_planning(encounter_uuid, fp_concept_id, fp_date, fp_method, form_id, encounter_type, patient_uuid, created_date, change_date, location_uuid, source_database) VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
	}
	
	@JsonIgnore
	@Override
	public String getInsertSQLWithObjectId() {
		return "INSERT INTO family_planning(encounter_uuid, fp_concept_id, fp_date, fp_method, fp_uuid, form_id, encounter_type, patient_uuid, created_date, encounter_date, change_date, location_uuid, source_database) VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
	}
	
	@JsonIgnore
	@Override
	public Object[] getInsertParamsWithoutObjectId() {
		Object[] params = { this.encounterUuid, this.fpConceptId, this.fpDate, this.fpMethod, this.formId,
		        this.encounterType, this.patientUuid, this.createdDate, this.changeDate, this.locationUuid,
		        this.sourceDatabase };
		return params;
	}
	
	@JsonIgnore
	@Override
	public Object[] getInsertParamsWithObjectId() {
		Object[] params = { this.encounterUuid, this.fpConceptId, this.fpDate, this.fpMethod, this.fpUuid, this.formId,
		        this.encounterType, this.patientUuid, this.createdDate, this.encounterDate, this.changeDate,
		        this.locationUuid, this.sourceDatabase };
		return params;
	}
	
	@JsonIgnore
	@Override
	public Object[] getUpdateParams() {
		Object[] params = { this.encounterUuid, this.fpConceptId, this.fpDate, this.fpMethod, this.fpUuid, this.formId,
		        this.encounterType, this.patientUuid, this.createdDate, this.encounterDate, this.changeDate,
		        this.locationUuid, this.sourceDatabase, this.encounterDate, this.fpUuid };
		return params;
	}
	
	@JsonIgnore
	@Override
	public String getUpdateSQL() {
		return "UPDATE family_planning SET encounter_uuid = ?, fp_concept_id = ?, fp_date = ?, fp_method = ?, fp_uuid = ?, form_id = ?, encounter_type = ?, patient_uuid = ?, created_date = ?, encounter_date = ?, change_date = ?, location_uuid = ?, source_database = ? WHERE encounter_date = ?  AND fp_uuid = ? ";
	}
	
	@JsonIgnore
	@Override
	public String generateInsertValuesWithoutObjectId() {
		return "" + (this.encounterUuid != null ? "\"" + utilities.scapeQuotationMarks(encounterUuid) + "\"" : null) + ","
		        + (this.fpConceptId) + ","
		        + (this.fpDate != null ? "\"" + DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(fpDate) + "\"" : null) + ","
		        + (this.fpMethod) + "," + (this.formId) + "," + (this.encounterType) + ","
		        + (this.patientUuid != null ? "\"" + utilities.scapeQuotationMarks(patientUuid) + "\"" : null) + ","
		        + (this.createdDate != null ? "\"" + DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(createdDate) + "\"" : null)
		        + ","
		        + (this.changeDate != null ? "\"" + DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(changeDate) + "\"" : null)
		        + "," + (this.locationUuid != null ? "\"" + utilities.scapeQuotationMarks(locationUuid) + "\"" : null) + ","
		        + (this.sourceDatabase != null ? "\"" + utilities.scapeQuotationMarks(sourceDatabase) + "\"" : null);
	}
	
	@JsonIgnore
	@Override
	public String generateInsertValuesWithObjectId() {
		return "" + (this.encounterUuid != null ? "\"" + utilities.scapeQuotationMarks(encounterUuid) + "\"" : null) + ","
		        + (this.fpConceptId) + ","
		        + (this.fpDate != null ? "\"" + DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(fpDate) + "\"" : null) + ","
		        + (this.fpMethod) + "," + (this.fpUuid != null ? "\"" + utilities.scapeQuotationMarks(fpUuid) + "\"" : null)
		        + "," + (this.formId) + "," + (this.encounterType) + ","
		        + (this.patientUuid != null ? "\"" + utilities.scapeQuotationMarks(patientUuid) + "\"" : null) + ","
		        + (this.createdDate != null ? "\"" + DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(createdDate) + "\"" : null)
		        + ","
		        + (this.encounterDate != null ? "\"" + DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(encounterDate) + "\""
		                : null)
		        + ","
		        + (this.changeDate != null ? "\"" + DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(changeDate) + "\"" : null)
		        + "," + (this.locationUuid != null ? "\"" + utilities.scapeQuotationMarks(locationUuid) + "\"" : null) + ","
		        + (this.sourceDatabase != null ? "\"" + utilities.scapeQuotationMarks(sourceDatabase) + "\"" : null);
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
		return "family_planning";
	}
	
}
