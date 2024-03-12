package org.openmrs.module.epts.etl.model.pojo.openmrs._default._query_result;

import org.openmrs.module.epts.etl.model.pojo.generic.*; 
 
import org.openmrs.module.epts.etl.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.epts.etl.utilities.AttDefinedElements; 
 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class PatientDataInfoQueryResultVO extends AbstractDatabaseObject implements DatabaseObject { 
	private java.util.Date birthdate;
	private String gender;
	private java.util.Date enrollmentDate;
	private Integer locationId;
	private String healthFacility;
	private String district;
	private String urban;
	private Double ageEnrollment;
	private Double openmrsCurrentAge;
	private String maritalStatusAtEnrollment;
	private String pregnancyStatusAtEnrollment;
	private String educationAtEnrollment;
	private String occupationAtEnrollment;
	private String partnerStatusAtEnrollment;
	private java.util.Date whoClinicalStageAtEnrollmentDate;
	private String whoClinicalStageAtEnrollment;
	private java.util.Date weightDate;
	private Double weightEnrollment;
	private Double heightEnrollment;
	private java.util.Date heightDate;
	private java.util.Date artInitiationDate;
	private String artRegimenCode;
	private java.util.Date patientStatusDate;
	private String patientStatusCode;
	private String tbAtScreening;
	private String tbCoInfectionStatus;
	private java.util.Date pmtctEntryDate;
	private java.util.Date pmtctExitDate;
	private String currentStatusInDmc;
 
	public PatientDataInfoQueryResultVO() { 
		this.metadata = false;
	} 
 
	public void setBirthdate(java.util.Date birthdate){ 
	 	this.birthdate = birthdate;
	}
 
	public java.util.Date getBirthdate(){ 
		return this.birthdate;
	}
 
	public void setGender(String gender){ 
	 	this.gender = gender;
	}
 
	public String getGender(){ 
		return this.gender;
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
 
	public void setUrban(String urban){ 
	 	this.urban = urban;
	}
 
	public String getUrban(){ 
		return this.urban;
	}
 
	public void setAgeEnrollment(Double ageEnrollment){ 
	 	this.ageEnrollment = ageEnrollment;
	}
 
	public Double getAgeEnrollment(){ 
		return this.ageEnrollment;
	}
 
	public void setOpenmrsCurrentAge(Double openmrsCurrentAge){ 
	 	this.openmrsCurrentAge = openmrsCurrentAge;
	}
 
	public Double getOpenmrsCurrentAge(){ 
		return this.openmrsCurrentAge;
	}
 
	public void setMaritalStatusAtEnrollment(String maritalStatusAtEnrollment){ 
	 	this.maritalStatusAtEnrollment = maritalStatusAtEnrollment;
	}
 
	public String getMaritalStatusAtEnrollment(){ 
		return this.maritalStatusAtEnrollment;
	}
 
	public void setPregnancyStatusAtEnrollment(String pregnancyStatusAtEnrollment){ 
	 	this.pregnancyStatusAtEnrollment = pregnancyStatusAtEnrollment;
	}
 
	public String getPregnancyStatusAtEnrollment(){ 
		return this.pregnancyStatusAtEnrollment;
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
 
	public void setPartnerStatusAtEnrollment(String partnerStatusAtEnrollment){ 
	 	this.partnerStatusAtEnrollment = partnerStatusAtEnrollment;
	}
 
	public String getPartnerStatusAtEnrollment(){ 
		return this.partnerStatusAtEnrollment;
	}
 
	public void setWhoClinicalStageAtEnrollmentDate(java.util.Date whoClinicalStageAtEnrollmentDate){ 
	 	this.whoClinicalStageAtEnrollmentDate = whoClinicalStageAtEnrollmentDate;
	}
 
	public java.util.Date getWhoClinicalStageAtEnrollmentDate(){ 
		return this.whoClinicalStageAtEnrollmentDate;
	}
 
	public void setWhoClinicalStageAtEnrollment(String whoClinicalStageAtEnrollment){ 
	 	this.whoClinicalStageAtEnrollment = whoClinicalStageAtEnrollment;
	}
 
	public String getWhoClinicalStageAtEnrollment(){ 
		return this.whoClinicalStageAtEnrollment;
	}
 
	public void setWeightDate(java.util.Date weightDate){ 
	 	this.weightDate = weightDate;
	}
 
	public java.util.Date getWeightDate(){ 
		return this.weightDate;
	}
 
	public void setWeightEnrollment(Double weightEnrollment){ 
	 	this.weightEnrollment = weightEnrollment;
	}
 
	public Double getWeightEnrollment(){ 
		return this.weightEnrollment;
	}
 
	public void setHeightEnrollment(Double heightEnrollment){ 
	 	this.heightEnrollment = heightEnrollment;
	}
 
	public Double getHeightEnrollment(){ 
		return this.heightEnrollment;
	}
 
	public void setHeightDate(java.util.Date heightDate){ 
	 	this.heightDate = heightDate;
	}
 
	public java.util.Date getHeightDate(){ 
		return this.heightDate;
	}
 
	public void setArtInitiationDate(java.util.Date artInitiationDate){ 
	 	this.artInitiationDate = artInitiationDate;
	}
 
	public java.util.Date getArtInitiationDate(){ 
		return this.artInitiationDate;
	}
 
	public void setArtRegimenCode(String artRegimenCode){ 
	 	this.artRegimenCode = artRegimenCode;
	}
 
	public String getArtRegimenCode(){ 
		return this.artRegimenCode;
	}
 
	public void setPatientStatusDate(java.util.Date patientStatusDate){ 
	 	this.patientStatusDate = patientStatusDate;
	}
 
	public java.util.Date getPatientStatusDate(){ 
		return this.patientStatusDate;
	}
 
	public void setPatientStatusCode(String patientStatusCode){ 
	 	this.patientStatusCode = patientStatusCode;
	}
 
	public String getPatientStatusCode(){ 
		return this.patientStatusCode;
	}
 
	public void setTbAtScreening(String tbAtScreening){ 
	 	this.tbAtScreening = tbAtScreening;
	}
 
	public String getTbAtScreening(){ 
		return this.tbAtScreening;
	}
 
	public void setTbCoInfectionStatus(String tbCoInfectionStatus){ 
	 	this.tbCoInfectionStatus = tbCoInfectionStatus;
	}
 
	public String getTbCoInfectionStatus(){ 
		return this.tbCoInfectionStatus;
	}
 
	public void setPmtctEntryDate(java.util.Date pmtctEntryDate){ 
	 	this.pmtctEntryDate = pmtctEntryDate;
	}
 
	public java.util.Date getPmtctEntryDate(){ 
		return this.pmtctEntryDate;
	}
 
	public void setPmtctExitDate(java.util.Date pmtctExitDate){ 
	 	this.pmtctExitDate = pmtctExitDate;
	}
 
	public java.util.Date getPmtctExitDate(){ 
		return this.pmtctExitDate;
	}
 
	public void setCurrentStatusInDmc(String currentStatusInDmc){ 
	 	this.currentStatusInDmc = currentStatusInDmc;
	}


 
	public String getCurrentStatusInDmc(){ 
		return this.currentStatusInDmc;
	}
 
	public Integer getObjectId() { 
 		return 0; 
	} 
 
	public void setObjectId(Integer selfId){ 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		super.load(rs);
		this.birthdate =  rs.getTimestamp("birthdate") != null ? new java.util.Date( rs.getTimestamp("birthdate").getTime() ) : null;
		this.gender = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("gender") != null ? rs.getString("gender").trim() : null);
		this.enrollmentDate =  rs.getTimestamp("enrollment_date") != null ? new java.util.Date( rs.getTimestamp("enrollment_date").getTime() ) : null;
		if (rs.getObject("location_id") != null) this.locationId = rs.getInt("location_id");
		this.healthFacility = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("health_facility") != null ? rs.getString("health_facility").trim() : null);
		this.district = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("district") != null ? rs.getString("district").trim() : null);
		this.urban = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("urban") != null ? rs.getString("urban").trim() : null);
		if (rs.getObject("age_enrollment") != null) this.ageEnrollment = rs.getDouble("age_enrollment");
		if (rs.getObject("openmrs_current_age") != null) this.openmrsCurrentAge = rs.getDouble("openmrs_current_age");
		this.maritalStatusAtEnrollment = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("marital_status_at_enrollment") != null ? rs.getString("marital_status_at_enrollment").trim() : null);
		this.pregnancyStatusAtEnrollment = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("pregnancy_status_at_enrollment") != null ? rs.getString("pregnancy_status_at_enrollment").trim() : null);
		this.educationAtEnrollment = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("education_at_enrollment") != null ? rs.getString("education_at_enrollment").trim() : null);
		this.occupationAtEnrollment = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("occupation_at_enrollment") != null ? rs.getString("occupation_at_enrollment").trim() : null);
		this.partnerStatusAtEnrollment = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("partner_status_at_enrollment") != null ? rs.getString("partner_status_at_enrollment").trim() : null);
		this.whoClinicalStageAtEnrollmentDate =  rs.getTimestamp("who_clinical_stage_at_enrollment_date") != null ? new java.util.Date( rs.getTimestamp("who_clinical_stage_at_enrollment_date").getTime() ) : null;
		this.whoClinicalStageAtEnrollment = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("who_clinical_stage_at_enrollment") != null ? rs.getString("who_clinical_stage_at_enrollment").trim() : null);
		this.weightDate =  rs.getTimestamp("weight_date") != null ? new java.util.Date( rs.getTimestamp("weight_date").getTime() ) : null;
		if (rs.getObject("weight_enrollment") != null) this.weightEnrollment = rs.getDouble("weight_enrollment");
		if (rs.getObject("height_enrollment") != null) this.heightEnrollment = rs.getDouble("height_enrollment");
		this.heightDate =  rs.getTimestamp("height_date") != null ? new java.util.Date( rs.getTimestamp("height_date").getTime() ) : null;
		this.artInitiationDate =  rs.getTimestamp("art_initiation_date") != null ? new java.util.Date( rs.getTimestamp("art_initiation_date").getTime() ) : null;
		this.artRegimenCode = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("art_regimen_code") != null ? rs.getString("art_regimen_code").trim() : null);
		this.patientStatusDate =  rs.getTimestamp("patient_status_date") != null ? new java.util.Date( rs.getTimestamp("patient_status_date").getTime() ) : null;
		this.patientStatusCode = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("patient_status_code") != null ? rs.getString("patient_status_code").trim() : null);
		this.tbAtScreening = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("tb_at_screening") != null ? rs.getString("tb_at_screening").trim() : null);
		this.tbCoInfectionStatus = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("tb_co_infection_status") != null ? rs.getString("tb_co_infection_status").trim() : null);
		this.pmtctEntryDate =  rs.getTimestamp("pmtct_entry_date") != null ? new java.util.Date( rs.getTimestamp("pmtct_entry_date").getTime() ) : null;
		this.pmtctExitDate =  rs.getTimestamp("pmtct_exit_date") != null ? new java.util.Date( rs.getTimestamp("pmtct_exit_date").getTime() ) : null;
		this.currentStatusInDmc = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("current_status_in_dmc") != null ? rs.getString("current_status_in_dmc").trim() : null);
	} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return null; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithoutObjectId(){ 
 		return "INSERT INTO patient_data_info(birthdate, gender, enrollment_date, location_id, health_facility, district, urban, age_enrollment, openmrs_current_age, marital_status_at_enrollment, pregnancy_status_at_enrollment, education_at_enrollment, occupation_at_enrollment, partner_status_at_enrollment, who_clinical_stage_at_enrollment_date, who_clinical_stage_at_enrollment, weight_date, weight_enrollment, height_enrollment, height_date, art_initiation_date, art_regimen_code, patient_status_date, patient_status_code, tb_at_screening, tb_co_infection_status, pmtct_entry_date, pmtct_exit_date, current_status_in_dmc) VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithoutObjectId(){ 
 		Object[] params = {this.birthdate, this.gender, this.enrollmentDate, this.locationId, this.healthFacility, this.district, this.urban, this.ageEnrollment, this.openmrsCurrentAge, this.maritalStatusAtEnrollment, this.pregnancyStatusAtEnrollment, this.educationAtEnrollment, this.occupationAtEnrollment, this.partnerStatusAtEnrollment, this.whoClinicalStageAtEnrollmentDate, this.whoClinicalStageAtEnrollment, this.weightDate, this.weightEnrollment, this.heightEnrollment, this.heightDate, this.artInitiationDate, this.artRegimenCode, this.patientStatusDate, this.patientStatusCode, this.tbAtScreening, this.tbCoInfectionStatus, this.pmtctEntryDate, this.pmtctExitDate, this.currentStatusInDmc};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithObjectId(){ 
 		return "INSERT INTO patient_data_info(birthdate, gender, enrollment_date, location_id, health_facility, district, urban, age_enrollment, openmrs_current_age, marital_status_at_enrollment, pregnancy_status_at_enrollment, education_at_enrollment, occupation_at_enrollment, partner_status_at_enrollment, who_clinical_stage_at_enrollment_date, who_clinical_stage_at_enrollment, weight_date, weight_enrollment, height_enrollment, height_date, art_initiation_date, art_regimen_code, patient_status_date, patient_status_code, tb_at_screening, tb_co_infection_status, pmtct_entry_date, pmtct_exit_date, current_status_in_dmc) VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithObjectId(){ 
 		Object[] params = {this.birthdate, this.gender, this.enrollmentDate, this.locationId, this.healthFacility, this.district, this.urban, this.ageEnrollment, this.openmrsCurrentAge, this.maritalStatusAtEnrollment, this.pregnancyStatusAtEnrollment, this.educationAtEnrollment, this.occupationAtEnrollment, this.partnerStatusAtEnrollment, this.whoClinicalStageAtEnrollmentDate, this.whoClinicalStageAtEnrollment, this.weightDate, this.weightEnrollment, this.heightEnrollment, this.heightDate, this.artInitiationDate, this.artRegimenCode, this.patientStatusDate, this.patientStatusCode, this.tbAtScreening, this.tbCoInfectionStatus, this.pmtctEntryDate, this.pmtctExitDate, this.currentStatusInDmc};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.birthdate, this.gender, this.enrollmentDate, this.locationId, this.healthFacility, this.district, this.urban, this.ageEnrollment, this.openmrsCurrentAge, this.maritalStatusAtEnrollment, this.pregnancyStatusAtEnrollment, this.educationAtEnrollment, this.occupationAtEnrollment, this.partnerStatusAtEnrollment, this.whoClinicalStageAtEnrollmentDate, this.whoClinicalStageAtEnrollment, this.weightDate, this.weightEnrollment, this.heightEnrollment, this.heightDate, this.artInitiationDate, this.artRegimenCode, this.patientStatusDate, this.patientStatusCode, this.tbAtScreening, this.tbCoInfectionStatus, this.pmtctEntryDate, this.pmtctExitDate, this.currentStatusInDmc, null};		return params; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE patient_data_info SET birthdate = ?, gender = ?, enrollment_date = ?, location_id = ?, health_facility = ?, district = ?, urban = ?, age_enrollment = ?, openmrs_current_age = ?, marital_status_at_enrollment = ?, pregnancy_status_at_enrollment = ?, education_at_enrollment = ?, occupation_at_enrollment = ?, partner_status_at_enrollment = ?, who_clinical_stage_at_enrollment_date = ?, who_clinical_stage_at_enrollment = ?, weight_date = ?, weight_enrollment = ?, height_enrollment = ?, height_date = ?, art_initiation_date = ?, art_regimen_code = ?, patient_status_date = ?, patient_status_code = ?, tb_at_screening = ?, tb_co_infection_status = ?, pmtct_entry_date = ?, pmtct_exit_date = ?, current_status_in_dmc = ? WHERE null = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.birthdate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(birthdate)  +"\"" : null) + "," + (this.gender != null ? "\""+ utilities.scapeQuotationMarks(gender)  +"\"" : null) + "," + (this.enrollmentDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(enrollmentDate)  +"\"" : null) + "," + (this.locationId) + "," + (this.healthFacility != null ? "\""+ utilities.scapeQuotationMarks(healthFacility)  +"\"" : null) + "," + (this.district != null ? "\""+ utilities.scapeQuotationMarks(district)  +"\"" : null) + "," + (this.urban != null ? "\""+ utilities.scapeQuotationMarks(urban)  +"\"" : null) + "," + (this.ageEnrollment) + "," + (this.openmrsCurrentAge) + "," + (this.maritalStatusAtEnrollment != null ? "\""+ utilities.scapeQuotationMarks(maritalStatusAtEnrollment)  +"\"" : null) + "," + (this.pregnancyStatusAtEnrollment != null ? "\""+ utilities.scapeQuotationMarks(pregnancyStatusAtEnrollment)  +"\"" : null) + "," + (this.educationAtEnrollment != null ? "\""+ utilities.scapeQuotationMarks(educationAtEnrollment)  +"\"" : null) + "," + (this.occupationAtEnrollment != null ? "\""+ utilities.scapeQuotationMarks(occupationAtEnrollment)  +"\"" : null) + "," + (this.partnerStatusAtEnrollment != null ? "\""+ utilities.scapeQuotationMarks(partnerStatusAtEnrollment)  +"\"" : null) + "," + (this.whoClinicalStageAtEnrollmentDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(whoClinicalStageAtEnrollmentDate)  +"\"" : null) + "," + (this.whoClinicalStageAtEnrollment != null ? "\""+ utilities.scapeQuotationMarks(whoClinicalStageAtEnrollment)  +"\"" : null) + "," + (this.weightDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(weightDate)  +"\"" : null) + "," + (this.weightEnrollment) + "," + (this.heightEnrollment) + "," + (this.heightDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(heightDate)  +"\"" : null) + "," + (this.artInitiationDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(artInitiationDate)  +"\"" : null) + "," + (this.artRegimenCode != null ? "\""+ utilities.scapeQuotationMarks(artRegimenCode)  +"\"" : null) + "," + (this.patientStatusDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(patientStatusDate)  +"\"" : null) + "," + (this.patientStatusCode != null ? "\""+ utilities.scapeQuotationMarks(patientStatusCode)  +"\"" : null) + "," + (this.tbAtScreening != null ? "\""+ utilities.scapeQuotationMarks(tbAtScreening)  +"\"" : null) + "," + (this.tbCoInfectionStatus != null ? "\""+ utilities.scapeQuotationMarks(tbCoInfectionStatus)  +"\"" : null) + "," + (this.pmtctEntryDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(pmtctEntryDate)  +"\"" : null) + "," + (this.pmtctExitDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(pmtctExitDate)  +"\"" : null) + "," + (this.currentStatusInDmc != null ? "\""+ utilities.scapeQuotationMarks(currentStatusInDmc)  +"\"" : null); 
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
		return "patient_data_info";
	}


}