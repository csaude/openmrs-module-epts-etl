package org.openmrs.module.epts.etl.model.pojo.openmrs._default._query_result;

import org.openmrs.module.epts.etl.model.pojo.generic.*; 
 
import org.openmrs.module.epts.etl.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.epts.etl.utilities.AttDefinedElements; 
 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class CacumPatientDataQueryResultVO extends AbstractDatabaseObject implements DatabaseObject { 
	private Integer patientId;
	private String patientUuid;
	private Double ageEnrollment;
	private Double currentAge;
	private String sex;
	private java.util.Date birthdate;
	private String urban;
	private String main;
	private java.util.Date enrollmentDate;
	private Integer locationId;
	private String healthFacility;
	private String district;
	private String educationAtEnrollment;
	private String occupationAtEnrollment;
	private String maritalStatusAtEnrollment;
	private String adress1;
	private String adress2;
	private String village;
	private java.util.Date artInitiationDate;
	private String pregnancyStatusAtEnrollment;
 
	public CacumPatientDataQueryResultVO() { 
		this.metadata = false;
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
 
	public void setAgeEnrollment(Double ageEnrollment){ 
	 	this.ageEnrollment = ageEnrollment;
	}
 
	public Double getAgeEnrollment(){ 
		return this.ageEnrollment;
	}
 
	public void setCurrentAge(Double currentAge){ 
	 	this.currentAge = currentAge;
	}
 
	public Double getCurrentAge(){ 
		return this.currentAge;
	}
 
	public void setSex(String sex){ 
	 	this.sex = sex;
	}
 
	public String getSex(){ 
		return this.sex;
	}
 
	public void setBirthdate(java.util.Date birthdate){ 
	 	this.birthdate = birthdate;
	}
 
	public java.util.Date getBirthdate(){ 
		return this.birthdate;
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
 
	public void setEnrollmentDate(java.util.Date enrollmentDate){ 
	 	this.enrollmentDate = enrollmentDate;
	}
 
	public java.util.Date getEnrollmentDate(){ 
		return this.enrollmentDate;
	}
 
	public void setLocationId(Integer locationId){ 
	 	this.locationId = locationId;
	}
 
	public Integer getLocationId(){ 
		return this.locationId;
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
 
	public void setEducationAtEnrollment(String educationAtEnrollment){ 
	 	this.educationAtEnrollment = educationAtEnrollment;
	}
 
	public String getEducationAtEnrollment(){ 
		return this.educationAtEnrollment;
	}
 
	public void setOccupationAtEnrollment(String occupationAtEnrollment){ 
	 	this.occupationAtEnrollment = occupationAtEnrollment;
	}
 
	public String getOccupationAtEnrollment(){ 
		return this.occupationAtEnrollment;
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
 
	@Override
	public void load(ResultSet rs) throws SQLException{ 
		super.load(rs);
 
if (rs.getObject("patient_id") != null) this.patientId = rs.getInt("patient_id");
this.patientUuid = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("patient_uuid") != null ? rs.getString("patient_uuid").trim() : null);
if (rs.getObject("age_enrollment") != null) this.ageEnrollment = rs.getDouble("age_enrollment");
if (rs.getObject("current_age") != null) this.currentAge = rs.getDouble("current_age");
this.sex = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("sex") != null ? rs.getString("sex").trim() : null);
this.birthdate =  rs.getTimestamp("birthdate") != null ? new java.util.Date( rs.getTimestamp("birthdate").getTime() ) : null;
this.urban = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("urban") != null ? rs.getString("urban").trim() : null);
this.main = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("main") != null ? rs.getString("main").trim() : null);
this.enrollmentDate =  rs.getTimestamp("enrollment_date") != null ? new java.util.Date( rs.getTimestamp("enrollment_date").getTime() ) : null;
if (rs.getObject("location_id") != null) this.locationId = rs.getInt("location_id");
this.healthFacility = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("health_facility") != null ? rs.getString("health_facility").trim() : null);
this.district = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("district") != null ? rs.getString("district").trim() : null);
this.educationAtEnrollment = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("education_at_enrollment") != null ? rs.getString("education_at_enrollment").trim() : null);
this.occupationAtEnrollment = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("occupation_at_enrollment") != null ? rs.getString("occupation_at_enrollment").trim() : null);
this.maritalStatusAtEnrollment = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("marital_status_at_enrollment") != null ? rs.getString("marital_status_at_enrollment").trim() : null);
this.adress1 = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("adress_1") != null ? rs.getString("adress_1").trim() : null);
this.adress2 = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("adress_2") != null ? rs.getString("adress_2").trim() : null);
this.village = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("village") != null ? rs.getString("village").trim() : null);
this.artInitiationDate =  rs.getTimestamp("art_initiation_date") != null ? new java.util.Date( rs.getTimestamp("art_initiation_date").getTime() ) : null;
this.pregnancyStatusAtEnrollment = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("pregnancy_status_at_enrollment") != null ? rs.getString("pregnancy_status_at_enrollment").trim() : null);
	} 
 
	@JsonIgnore
	@Override
	public String getInsertSQLWithoutObjectId(){ 
 		return "INSERT INTO cacum_patient_data(patient_id, patient_uuid, age_enrollment, current_age, sex, birthdate, urban, main, enrollment_date, location_id, health_facility, district, education_at_enrollment, occupation_at_enrollment, marital_status_at_enrollment, adress_1, adress_2, village, art_initiation_date, pregnancy_status_at_enrollment) VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	@Override
	public String getInsertSQLWithObjectId(){ 
 		return "INSERT INTO cacum_patient_data(patient_id, patient_uuid, age_enrollment, current_age, sex, birthdate, urban, main, enrollment_date, location_id, health_facility, district, education_at_enrollment, occupation_at_enrollment, marital_status_at_enrollment, adress_1, adress_2, village, art_initiation_date, pregnancy_status_at_enrollment) VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	@Override
	public Object[]  getInsertParamsWithoutObjectId(){ 
 		Object[] params = {this.patientId, this.patientUuid, this.ageEnrollment, this.currentAge, this.sex, this.birthdate, this.urban, this.main, this.enrollmentDate, this.locationId, this.healthFacility, this.district, this.educationAtEnrollment, this.occupationAtEnrollment, this.maritalStatusAtEnrollment, this.adress1, this.adress2, this.village, this.artInitiationDate, this.pregnancyStatusAtEnrollment};
		return params; 
	} 
 
	@JsonIgnore
	@Override
	public Object[]  getInsertParamsWithObjectId(){ 
 		Object[] params = {this.patientId, this.patientUuid, this.ageEnrollment, this.currentAge, this.sex, this.birthdate, this.urban, this.main, this.enrollmentDate, this.locationId, this.healthFacility, this.district, this.educationAtEnrollment, this.occupationAtEnrollment, this.maritalStatusAtEnrollment, this.adress1, this.adress2, this.village, this.artInitiationDate, this.pregnancyStatusAtEnrollment};
		return params; 
	} 
 
	@JsonIgnore
	@Override
	public Object[]  getUpdateParams(){ 
 		throw new RuntimeException("Impossible auto update command! No primary key is defined for table object!");	} 
 
	@JsonIgnore
	@Override
	public String getUpdateSQL(){ 
 		throw new RuntimeException("Impossible auto update command! No primary key is defined for table object!");	} 
 
	@JsonIgnore
	@Override
	public String generateInsertValuesWithoutObjectId(){ 
 		return ""+(this.patientId) + "," + (this.patientUuid != null ? "\""+ utilities.scapeQuotationMarks(patientUuid)  +"\"" : null) + "," + (this.ageEnrollment) + "," + (this.currentAge) + "," + (this.sex != null ? "\""+ utilities.scapeQuotationMarks(sex)  +"\"" : null) + "," + (this.birthdate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(birthdate)  +"\"" : null) + "," + (this.urban != null ? "\""+ utilities.scapeQuotationMarks(urban)  +"\"" : null) + "," + (this.main != null ? "\""+ utilities.scapeQuotationMarks(main)  +"\"" : null) + "," + (this.enrollmentDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(enrollmentDate)  +"\"" : null) + "," + (this.locationId) + "," + (this.healthFacility != null ? "\""+ utilities.scapeQuotationMarks(healthFacility)  +"\"" : null) + "," + (this.district != null ? "\""+ utilities.scapeQuotationMarks(district)  +"\"" : null) + "," + (this.educationAtEnrollment != null ? "\""+ utilities.scapeQuotationMarks(educationAtEnrollment)  +"\"" : null) + "," + (this.occupationAtEnrollment != null ? "\""+ utilities.scapeQuotationMarks(occupationAtEnrollment)  +"\"" : null) + "," + (this.maritalStatusAtEnrollment != null ? "\""+ utilities.scapeQuotationMarks(maritalStatusAtEnrollment)  +"\"" : null) + "," + (this.adress1 != null ? "\""+ utilities.scapeQuotationMarks(adress1)  +"\"" : null) + "," + (this.adress2 != null ? "\""+ utilities.scapeQuotationMarks(adress2)  +"\"" : null) + "," + (this.village != null ? "\""+ utilities.scapeQuotationMarks(village)  +"\"" : null) + "," + (this.artInitiationDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(artInitiationDate)  +"\"" : null) + "," + (this.pregnancyStatusAtEnrollment != null ? "\""+ utilities.scapeQuotationMarks(pregnancyStatusAtEnrollment)  +"\"" : null); 
	} 
 
	@JsonIgnore
	@Override
	public String generateInsertValuesWithObjectId(){ 
 		return ""+(this.patientId) + "," + (this.patientUuid != null ? "\""+ utilities.scapeQuotationMarks(patientUuid)  +"\"" : null) + "," + (this.ageEnrollment) + "," + (this.currentAge) + "," + (this.sex != null ? "\""+ utilities.scapeQuotationMarks(sex)  +"\"" : null) + "," + (this.birthdate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(birthdate)  +"\"" : null) + "," + (this.urban != null ? "\""+ utilities.scapeQuotationMarks(urban)  +"\"" : null) + "," + (this.main != null ? "\""+ utilities.scapeQuotationMarks(main)  +"\"" : null) + "," + (this.enrollmentDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(enrollmentDate)  +"\"" : null) + "," + (this.locationId) + "," + (this.healthFacility != null ? "\""+ utilities.scapeQuotationMarks(healthFacility)  +"\"" : null) + "," + (this.district != null ? "\""+ utilities.scapeQuotationMarks(district)  +"\"" : null) + "," + (this.educationAtEnrollment != null ? "\""+ utilities.scapeQuotationMarks(educationAtEnrollment)  +"\"" : null) + "," + (this.occupationAtEnrollment != null ? "\""+ utilities.scapeQuotationMarks(occupationAtEnrollment)  +"\"" : null) + "," + (this.maritalStatusAtEnrollment != null ? "\""+ utilities.scapeQuotationMarks(maritalStatusAtEnrollment)  +"\"" : null) + "," + (this.adress1 != null ? "\""+ utilities.scapeQuotationMarks(adress1)  +"\"" : null) + "," + (this.adress2 != null ? "\""+ utilities.scapeQuotationMarks(adress2)  +"\"" : null) + "," + (this.village != null ? "\""+ utilities.scapeQuotationMarks(village)  +"\"" : null) + "," + (this.artInitiationDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(artInitiationDate)  +"\"" : null) + "," + (this.pregnancyStatusAtEnrollment != null ? "\""+ utilities.scapeQuotationMarks(pregnancyStatusAtEnrollment)  +"\"" : null); 
	} 
 
	@Override
	public boolean hasParents() {
		return false;
	}

	@Override
	public Integer getParentValue(String parentAttName) {

		throw new RuntimeException("No found parent for: " + parentAttName);	}

	@Override
	public String generateTableName() {
		return "cacum_patient_data";
	}


}