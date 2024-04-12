package org.openmrs.module.epts.etl.model.pojo.openmrs._default;

import org.openmrs.module.epts.etl.model.pojo.generic.*; 
 
import org.openmrs.module.epts.etl.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.epts.etl.utilities.AttDefinedElements; 
 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class CacumPatientVO extends AbstractDatabaseObject implements DatabaseObject { 
	private Integer id;
	private Integer patientId;
	private String patientUuid;
	private String healthFacility;
	private String district;
	private String sex;
	private java.util.Date dateOfBirth;
	private Integer currentAge;
	private java.util.Date enrollmentDate;
	private Integer ageEnrollment;
	private String occupationAtEnrollment;
	private String educationAtEnrollment;
	private String maritalStatusAtEnrollment;
	private String adress1;
	private String adress2;
	private String village;
	private java.util.Date artInitiationDate;
	private String pregnancyStatusAtEnrollment;
	private String womenStatus;
	private Integer numberChildrenEnrollment;
	private Integer locationId;
	private String urban;
	private String main;
 
	public CacumPatientVO() { 
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
 
	public void setPatientUuid(String patientUuid){ 
	 	this.patientUuid = patientUuid;
	}
 
	public String getPatientUuid(){ 
		return this.patientUuid;
	}
 
	public void setHealthFacility(String healthFacility){ 
	 	this.healthFacility = healthFacility;
	}
 
	public String getHealthFacility(){ 
		return this.healthFacility;
	}
 
	public void setDistrict(String district){ 
	 	this.district = district;
	}
 
	public String getDistrict(){ 
		return this.district;
	}
 
	public void setSex(String sex){ 
	 	this.sex = sex;
	}
 
	public String getSex(){ 
		return this.sex;
	}
 
	public void setDateOfBirth(java.util.Date dateOfBirth){ 
	 	this.dateOfBirth = dateOfBirth;
	}
 
	public java.util.Date getDateOfBirth(){ 
		return this.dateOfBirth;
	}
 
	public void setCurrentAge(Integer currentAge){ 
	 	this.currentAge = currentAge;
	}
 
	public Integer getCurrentAge(){ 
		return this.currentAge;
	}
 
	public void setEnrollmentDate(java.util.Date enrollmentDate){ 
	 	this.enrollmentDate = enrollmentDate;
	}
 
	public java.util.Date getEnrollmentDate(){ 
		return this.enrollmentDate;
	}
 
	public void setAgeEnrollment(Integer ageEnrollment){ 
	 	this.ageEnrollment = ageEnrollment;
	}
 
	public Integer getAgeEnrollment(){ 
		return this.ageEnrollment;
	}
 
	public void setOccupationAtEnrollment(String occupationAtEnrollment){ 
	 	this.occupationAtEnrollment = occupationAtEnrollment;
	}
 
	public String getOccupationAtEnrollment(){ 
		return this.occupationAtEnrollment;
	}
 
	public void setEducationAtEnrollment(String educationAtEnrollment){ 
	 	this.educationAtEnrollment = educationAtEnrollment;
	}
 
	public String getEducationAtEnrollment(){ 
		return this.educationAtEnrollment;
	}
 
	public void setMaritalStatusAtEnrollment(String maritalStatusAtEnrollment){ 
	 	this.maritalStatusAtEnrollment = maritalStatusAtEnrollment;
	}
 
	public String getMaritalStatusAtEnrollment(){ 
		return this.maritalStatusAtEnrollment;
	}
 
	public void setAdress1(String adress1){ 
	 	this.adress1 = adress1;
	}
 
	public String getAdress1(){ 
		return this.adress1;
	}
 
	public void setAdress2(String adress2){ 
	 	this.adress2 = adress2;
	}
 
	public String getAdress2(){ 
		return this.adress2;
	}
 
	public void setVillage(String village){ 
	 	this.village = village;
	}
 
	public String getVillage(){ 
		return this.village;
	}
 
	public void setArtInitiationDate(java.util.Date artInitiationDate){ 
	 	this.artInitiationDate = artInitiationDate;
	}
 
	public java.util.Date getArtInitiationDate(){ 
		return this.artInitiationDate;
	}
 
	public void setPregnancyStatusAtEnrollment(String pregnancyStatusAtEnrollment){ 
	 	this.pregnancyStatusAtEnrollment = pregnancyStatusAtEnrollment;
	}
 
	public String getPregnancyStatusAtEnrollment(){ 
		return this.pregnancyStatusAtEnrollment;
	}
 
	public void setWomenStatus(String womenStatus){ 
	 	this.womenStatus = womenStatus;
	}
 
	public String getWomenStatus(){ 
		return this.womenStatus;
	}
 
	public void setNumberChildrenEnrollment(Integer numberChildrenEnrollment){ 
	 	this.numberChildrenEnrollment = numberChildrenEnrollment;
	}
 
	public Integer getNumberChildrenEnrollment(){ 
		return this.numberChildrenEnrollment;
	}
 
	public void setLocationId(Integer locationId){ 
	 	this.locationId = locationId;
	}
 
	public Integer getLocationId(){ 
		return this.locationId;
	}
 
	public void setUrban(String urban){ 
	 	this.urban = urban;
	}
 
	public String getUrban(){ 
		return this.urban;
	}
 
	public void setMain(String main){ 
	 	this.main = main;
	}


 
	public String getMain(){ 
		return this.main;
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
		this.patientUuid = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("patient_uuid") != null ? rs.getString("patient_uuid").trim() : null);
		this.healthFacility = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("health_facility") != null ? rs.getString("health_facility").trim() : null);
		this.district = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("district") != null ? rs.getString("district").trim() : null);
		this.sex = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("sex") != null ? rs.getString("sex").trim() : null);
		this.dateOfBirth =  rs.getTimestamp("date_of_birth") != null ? new java.util.Date( rs.getTimestamp("date_of_birth").getTime() ) : null;
		if (rs.getObject("current_age") != null) this.currentAge = rs.getInt("current_age");
		this.enrollmentDate =  rs.getTimestamp("enrollment_date") != null ? new java.util.Date( rs.getTimestamp("enrollment_date").getTime() ) : null;
		if (rs.getObject("age_enrollment") != null) this.ageEnrollment = rs.getInt("age_enrollment");
		this.occupationAtEnrollment = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("occupation_at_enrollment") != null ? rs.getString("occupation_at_enrollment").trim() : null);
		this.educationAtEnrollment = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("education_at_enrollment") != null ? rs.getString("education_at_enrollment").trim() : null);
		this.maritalStatusAtEnrollment = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("marital_status_at_enrollment") != null ? rs.getString("marital_status_at_enrollment").trim() : null);
		this.adress1 = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("adress_1") != null ? rs.getString("adress_1").trim() : null);
		this.adress2 = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("adress_2") != null ? rs.getString("adress_2").trim() : null);
		this.village = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("village") != null ? rs.getString("village").trim() : null);
		this.artInitiationDate =  rs.getTimestamp("art_initiation_date") != null ? new java.util.Date( rs.getTimestamp("art_initiation_date").getTime() ) : null;
		this.pregnancyStatusAtEnrollment = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("pregnancy_status_at_enrollment") != null ? rs.getString("pregnancy_status_at_enrollment").trim() : null);
		this.womenStatus = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("women_status") != null ? rs.getString("women_status").trim() : null);
		if (rs.getObject("number_children_enrollment") != null) this.numberChildrenEnrollment = rs.getInt("number_children_enrollment");
		if (rs.getObject("location_id") != null) this.locationId = rs.getInt("location_id");
		this.urban = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("urban") != null ? rs.getString("urban").trim() : null);
		this.main = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("main") != null ? rs.getString("main").trim() : null);
	} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "id"; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithoutObjectId(){ 
 		return "INSERT INTO cacum_data.cacum_patient(patient_id, patient_uuid, health_facility, district, sex, date_of_birth, current_age, enrollment_date, age_enrollment, occupation_at_enrollment, education_at_enrollment, marital_status_at_enrollment, adress_1, adress_2, village, art_initiation_date, pregnancy_status_at_enrollment, women_status, number_children_enrollment, location_id, urban, main) VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithoutObjectId(){ 
 		Object[] params = {this.patientId, this.patientUuid, this.healthFacility, this.district, this.sex, this.dateOfBirth, this.currentAge, this.enrollmentDate, this.ageEnrollment, this.occupationAtEnrollment, this.educationAtEnrollment, this.maritalStatusAtEnrollment, this.adress1, this.adress2, this.village, this.artInitiationDate, this.pregnancyStatusAtEnrollment, this.womenStatus, this.numberChildrenEnrollment, this.locationId, this.urban, this.main};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithObjectId(){ 
 		return "INSERT INTO cacum_data.cacum_patient(id, patient_id, patient_uuid, health_facility, district, sex, date_of_birth, current_age, enrollment_date, age_enrollment, occupation_at_enrollment, education_at_enrollment, marital_status_at_enrollment, adress_1, adress_2, village, art_initiation_date, pregnancy_status_at_enrollment, women_status, number_children_enrollment, location_id, urban, main) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithObjectId(){ 
 		Object[] params = {this.id, this.patientId, this.patientUuid, this.healthFacility, this.district, this.sex, this.dateOfBirth, this.currentAge, this.enrollmentDate, this.ageEnrollment, this.occupationAtEnrollment, this.educationAtEnrollment, this.maritalStatusAtEnrollment, this.adress1, this.adress2, this.village, this.artInitiationDate, this.pregnancyStatusAtEnrollment, this.womenStatus, this.numberChildrenEnrollment, this.locationId, this.urban, this.main};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.patientId, this.patientUuid, this.healthFacility, this.district, this.sex, this.dateOfBirth, this.currentAge, this.enrollmentDate, this.ageEnrollment, this.occupationAtEnrollment, this.educationAtEnrollment, this.maritalStatusAtEnrollment, this.adress1, this.adress2, this.village, this.artInitiationDate, this.pregnancyStatusAtEnrollment, this.womenStatus, this.numberChildrenEnrollment, this.locationId, this.urban, this.main, this.id};		return params; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE cacum_data.cacum_patient SET patient_id = ?, patient_uuid = ?, health_facility = ?, district = ?, sex = ?, date_of_birth = ?, current_age = ?, enrollment_date = ?, age_enrollment = ?, occupation_at_enrollment = ?, education_at_enrollment = ?, marital_status_at_enrollment = ?, adress_1 = ?, adress_2 = ?, village = ?, art_initiation_date = ?, pregnancy_status_at_enrollment = ?, women_status = ?, number_children_enrollment = ?, location_id = ?, urban = ?, main = ? WHERE id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.patientId) + "," + (this.patientUuid != null ? "\""+ utilities.scapeQuotationMarks(patientUuid)  +"\"" : null) + "," + (this.healthFacility != null ? "\""+ utilities.scapeQuotationMarks(healthFacility)  +"\"" : null) + "," + (this.district != null ? "\""+ utilities.scapeQuotationMarks(district)  +"\"" : null) + "," + (this.sex != null ? "\""+ utilities.scapeQuotationMarks(sex)  +"\"" : null) + "," + (this.dateOfBirth != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateOfBirth)  +"\"" : null) + "," + (this.currentAge) + "," + (this.enrollmentDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(enrollmentDate)  +"\"" : null) + "," + (this.ageEnrollment) + "," + (this.occupationAtEnrollment != null ? "\""+ utilities.scapeQuotationMarks(occupationAtEnrollment)  +"\"" : null) + "," + (this.educationAtEnrollment != null ? "\""+ utilities.scapeQuotationMarks(educationAtEnrollment)  +"\"" : null) + "," + (this.maritalStatusAtEnrollment != null ? "\""+ utilities.scapeQuotationMarks(maritalStatusAtEnrollment)  +"\"" : null) + "," + (this.adress1 != null ? "\""+ utilities.scapeQuotationMarks(adress1)  +"\"" : null) + "," + (this.adress2 != null ? "\""+ utilities.scapeQuotationMarks(adress2)  +"\"" : null) + "," + (this.village != null ? "\""+ utilities.scapeQuotationMarks(village)  +"\"" : null) + "," + (this.artInitiationDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(artInitiationDate)  +"\"" : null) + "," + (this.pregnancyStatusAtEnrollment != null ? "\""+ utilities.scapeQuotationMarks(pregnancyStatusAtEnrollment)  +"\"" : null) + "," + (this.womenStatus != null ? "\""+ utilities.scapeQuotationMarks(womenStatus)  +"\"" : null) + "," + (this.numberChildrenEnrollment) + "," + (this.locationId) + "," + (this.urban != null ? "\""+ utilities.scapeQuotationMarks(urban)  +"\"" : null) + "," + (this.main != null ? "\""+ utilities.scapeQuotationMarks(main)  +"\"" : null); 
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
		return "cacum_data.cacum_patient";
	}


}