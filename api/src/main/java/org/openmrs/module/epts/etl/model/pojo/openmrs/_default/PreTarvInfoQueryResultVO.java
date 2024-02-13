package org.openmrs.module.epts.etl.model.pojo.openmrs._default;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.openmrs.module.epts.etl.model.pojo.generic.AbstractDatabaseObject;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObject;
import org.openmrs.module.epts.etl.utilities.AttDefinedElements;
import org.openmrs.module.epts.etl.utilities.DateAndTimeUtilities;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class PreTarvInfoQueryResultVO extends AbstractDatabaseObject implements DatabaseObject {
	
	private Integer patientId;
	
	private java.util.Date enrollmentDate;
	
	private Integer locationId;
	
	private String locationName;
	
	private java.util.Date birthdate;
	
	private int ageEnrollment;
	
	private int openmrsCurrentAge;
	
	private String gender;
	
	private String maritalStatusAtEnrollment;
	
	private String pregnancyStatusAtEnrollment;
	
	public PreTarvInfoQueryResultVO() {
		this.metadata = false;
	}
	
	public void setPatientId(Integer patientId) {
		this.patientId = patientId;
	}
	
	public Integer getPatientId() {
		return this.patientId;
	}
	
	public void setEnrollmentDate(java.util.Date enrollmentDate) {
		this.enrollmentDate = enrollmentDate;
	}
	
	public java.util.Date getEnrollmentDate() {
		return this.enrollmentDate;
	}
	
	public void setLocationId(Integer locationId) {
		this.locationId = locationId;
	}
	
	public Integer getLocationId() {
		return this.locationId;
	}
	
	public void setLocationName(String locationName) {
		this.locationName = locationName;
	}
	
	public String getLocationName() {
		return this.locationName;
	}
	
	public void setBirthdate(java.util.Date birthdate) {
		this.birthdate = birthdate;
	}
	
	public java.util.Date getBirthdate() {
		return this.birthdate;
	}
	
	public void setAgeEnrollment(int ageEnrollment) {
		this.ageEnrollment = ageEnrollment;
	}
	
	public double getAgeEnrollment() {
		return this.ageEnrollment;
	}
	
	public void setOpenmrsCurrentAge(int openmrsCurrentAge) {
		this.openmrsCurrentAge = openmrsCurrentAge;
	}
	
	public double getOpenmrsCurrentAge() {
		return this.openmrsCurrentAge;
	}
	
	public void setGender(String gender) {
		this.gender = gender;
	}
	
	public String getGender() {
		return this.gender;
	}
	
	public void setMaritalStatusAtEnrollment(String maritalStatusAtEnrollment) {
		this.maritalStatusAtEnrollment = maritalStatusAtEnrollment;
	}
	
	public String getMaritalStatusAtEnrollment() {
		return this.maritalStatusAtEnrollment;
	}
	
	public void setPregnancyStatusAtEnrollment(String pregnancyStatusAtEnrollment) {
		this.pregnancyStatusAtEnrollment = pregnancyStatusAtEnrollment;
	}
	
	public String getPregnancyStatusAtEnrollment() {
		return this.pregnancyStatusAtEnrollment;
	}
	
	public Integer getObjectId() {
		return 0;
	}
	
	public void setObjectId(Integer selfId) {
	}
	
	public void load(ResultSet rs) throws SQLException {
		super.load(rs);
		if (rs.getObject("patient_id") != null)
			this.patientId = rs.getInt("patient_id");
		this.enrollmentDate = rs.getTimestamp("enrollment_date") != null
		        ? new java.util.Date(rs.getTimestamp("enrollment_date").getTime())
		        : null;
		if (rs.getObject("location_id") != null)
			this.locationId = rs.getInt("location_id");
		this.locationName = AttDefinedElements.removeStrangeCharactersOnString(
		    rs.getString("location_name") != null ? rs.getString("location_name").trim() : null);
		this.birthdate = rs.getTimestamp("birthdate") != null ? new java.util.Date(rs.getTimestamp("birthdate").getTime())
		        : null;
		this.ageEnrollment = rs.getInt("age_enrollment");
		this.openmrsCurrentAge = rs.getInt("openmrs_current_age");
		this.gender = AttDefinedElements
		        .removeStrangeCharactersOnString(rs.getString("gender") != null ? rs.getString("gender").trim() : null);
		this.maritalStatusAtEnrollment = AttDefinedElements.removeStrangeCharactersOnString(
		    rs.getString("marital_status_at_enrollment") != null ? rs.getString("marital_status_at_enrollment").trim()
		            : null);
		this.pregnancyStatusAtEnrollment = AttDefinedElements.removeStrangeCharactersOnString(
		    rs.getString("pregnancy_status_at_enrollment") != null ? rs.getString("pregnancy_status_at_enrollment").trim()
		            : null);
	}
	
	@JsonIgnore
	public String generateDBPrimaryKeyAtt() {
		return null;
	}
	
	@JsonIgnore
	public String getInsertSQLWithoutObjectId() {
		return "INSERT INTO pre_tarv_info(patient_id, enrollment_date, location_id, location_name, birthdate, age_enrollment, openmrs_current_age, gender, marital_status_at_enrollment, pregnancy_status_at_enrollment) VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
	}
	
	@JsonIgnore
	public Object[] getInsertParamsWithoutObjectId() {
		Object[] params = { this.patientId, this.enrollmentDate, this.locationId, this.locationName, this.birthdate,
		        this.ageEnrollment, this.openmrsCurrentAge, this.gender, this.maritalStatusAtEnrollment,
		        this.pregnancyStatusAtEnrollment };
		return params;
	}
	
	@JsonIgnore
	public String getInsertSQLWithObjectId() {
		return "INSERT INTO pre_tarv_info(patient_id, enrollment_date, location_id, location_name, birthdate, age_enrollment, openmrs_current_age, gender, marital_status_at_enrollment, pregnancy_status_at_enrollment) VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
	}
	
	@JsonIgnore
	public Object[] getInsertParamsWithObjectId() {
		Object[] params = { this.patientId, this.enrollmentDate, this.locationId, this.locationName, this.birthdate,
		        this.ageEnrollment, this.openmrsCurrentAge, this.gender, this.maritalStatusAtEnrollment,
		        this.pregnancyStatusAtEnrollment };
		return params;
	}
	
	@JsonIgnore
	public Object[] getUpdateParams() {
		Object[] params = { this.patientId, this.enrollmentDate, this.locationId, this.locationName, this.birthdate,
		        this.ageEnrollment, this.openmrsCurrentAge, this.gender, this.maritalStatusAtEnrollment,
		        this.pregnancyStatusAtEnrollment, null };
		return params;
	}
	
	@JsonIgnore
	public String getUpdateSQL() {
		return "UPDATE pre_tarv_info SET patient_id = ?, enrollment_date = ?, location_id = ?, location_name = ?, birthdate = ?, age_enrollment = ?, openmrs_current_age = ?, gender = ?, marital_status_at_enrollment = ?, pregnancy_status_at_enrollment = ? WHERE null = ?;";
	}
	
	@JsonIgnore
	public String generateInsertValues() {
		return "" + (this.patientId) + ","
		        + (this.enrollmentDate != null ? "\"" + DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(enrollmentDate) + "\""
		                : null)
		        + "," + (this.locationId) + ","
		        + (this.locationName != null ? "\"" + utilities.scapeQuotationMarks(locationName) + "\"" : null) + ","
		        + (this.birthdate != null ? "\"" + DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(birthdate) + "\"" : null)
		        + "," + (this.ageEnrollment) + "," + (this.openmrsCurrentAge) + ","
		        + (this.gender != null ? "\"" + utilities.scapeQuotationMarks(gender) + "\"" : null) + ","
		        + (this.maritalStatusAtEnrollment != null
		                ? "\"" + utilities.scapeQuotationMarks(maritalStatusAtEnrollment) + "\""
		                : null)
		        + ","
		        + (this.pregnancyStatusAtEnrollment != null
		                ? "\"" + utilities.scapeQuotationMarks(pregnancyStatusAtEnrollment) + "\""
		                : null);
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
	public void changeParentValue(String parentAttName, DatabaseObject newParent) {
		
		throw new RuntimeException("No found parent for: " + parentAttName);
	}
	
	@Override
	public void setParentToNull(String parentAttName) {
		
		throw new RuntimeException("No found parent for: " + parentAttName);
	}
	
	@Override
	public String generateTableName() {
		return "pre_tarv_info";
	}
	
}
