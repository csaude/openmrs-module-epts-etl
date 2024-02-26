package org.openmrs.module.epts.etl.model.pojo.openmrs.community_data_extraction;

import org.openmrs.module.epts.etl.model.pojo.generic.*; 
 
import org.openmrs.module.epts.etl.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.epts.etl.utilities.AttDefinedElements; 
 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class CommunityArvPatientVO extends AbstractDatabaseObject implements DatabaseObject { 
	private Integer id;
	private Integer patientId;
	private String healthFacility;
	private String district;
	private String sex;
	private java.util.Date dateOfBirth;
	private java.util.Date enrollmentDate;
	private Integer ageEnrollment;
	private Integer openmrsCurrentAge;
	private String maritalStatusAtEnrollment;
	private String pregnancyStatusAtEnrollment;
	private String womenStatus;
	private String educationAtEnrollment;
	private String occupationAtEnrollment;
	private String partnerStatusAtEnrollment;
	private String WHOClinicalStageAtEnrollment;
	private java.util.Date WHOClinicalStageAtEnrollmentDate;
	private double weightEnrollment;
	private java.util.Date weightDate;
	private double heightEnrollment;
	private java.util.Date heightDate;
	private java.util.Date artInitiationDate;
	private String artRegimen;
	private String patientStatus;
	private java.util.Date patientStatusDate;
	private String tbAtScreening;
	private String tbCoInfection;
	private java.util.Date pmtctEntryDate;
	private java.util.Date pmtctExitDate;
	private Integer locationId;
	private String currentStatusInDmc;
	private String urban;
	private String main;
 
	public CommunityArvPatientVO() { 
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
 
	public void setOpenmrsCurrentAge(Integer openmrsCurrentAge){ 
	 	this.openmrsCurrentAge = openmrsCurrentAge;
	}
 
	public Integer getOpenmrsCurrentAge(){ 
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
 
	public void setWomenStatus(String womenStatus){ 
	 	this.womenStatus = womenStatus;
	}
 
	public String getWomenStatus(){ 
		return this.womenStatus;
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
 
	public void setWHOClinicalStageAtEnrollment(String WHOClinicalStageAtEnrollment){ 
	 	this.WHOClinicalStageAtEnrollment = WHOClinicalStageAtEnrollment;
	}
 
	public String getWHOClinicalStageAtEnrollment(){ 
		return this.WHOClinicalStageAtEnrollment;
	}
 
	public void setWHOClinicalStageAtEnrollmentDate(java.util.Date WHOClinicalStageAtEnrollmentDate){ 
	 	this.WHOClinicalStageAtEnrollmentDate = WHOClinicalStageAtEnrollmentDate;
	}
 
	public java.util.Date getWHOClinicalStageAtEnrollmentDate(){ 
		return this.WHOClinicalStageAtEnrollmentDate;
	}
 
	public void setWeightEnrollment(double weightEnrollment){ 
	 	this.weightEnrollment = weightEnrollment;
	}
 
	public double getWeightEnrollment(){ 
		return this.weightEnrollment;
	}
 
	public void setWeightDate(java.util.Date weightDate){ 
	 	this.weightDate = weightDate;
	}
 
	public java.util.Date getWeightDate(){ 
		return this.weightDate;
	}
 
	public void setHeightEnrollment(double heightEnrollment){ 
	 	this.heightEnrollment = heightEnrollment;
	}
 
	public double getHeightEnrollment(){ 
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
 
	public void setArtRegimen(String artRegimen){ 
	 	this.artRegimen = artRegimen;
	}
 
	public String getArtRegimen(){ 
		return this.artRegimen;
	}
 
	public void setPatientStatus(String patientStatus){ 
	 	this.patientStatus = patientStatus;
	}
 
	public String getPatientStatus(){ 
		return this.patientStatus;
	}
 
	public void setPatientStatusDate(java.util.Date patientStatusDate){ 
	 	this.patientStatusDate = patientStatusDate;
	}
 
	public java.util.Date getPatientStatusDate(){ 
		return this.patientStatusDate;
	}
 
	public void setTbAtScreening(String tbAtScreening){ 
	 	this.tbAtScreening = tbAtScreening;
	}
 
	public String getTbAtScreening(){ 
		return this.tbAtScreening;
	}
 
	public void setTbCoInfection(String tbCoInfection){ 
	 	this.tbCoInfection = tbCoInfection;
	}
 
	public String getTbCoInfection(){ 
		return this.tbCoInfection;
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
 
	public void setLocationId(Integer locationId){ 
	 	this.locationId = locationId;
	}
 
	public Integer getLocationId(){ 
		return this.locationId;
	}
 
	public void setCurrentStatusInDmc(String currentStatusInDmc){ 
	 	this.currentStatusInDmc = currentStatusInDmc;
	}
 
	public String getCurrentStatusInDmc(){ 
		return this.currentStatusInDmc;
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
		this.healthFacility = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("health_facility") != null ? rs.getString("health_facility").trim() : null);
		this.district = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("district") != null ? rs.getString("district").trim() : null);
		this.sex = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("sex") != null ? rs.getString("sex").trim() : null);
		this.dateOfBirth =  rs.getTimestamp("date_of_birth") != null ? new java.util.Date( rs.getTimestamp("date_of_birth").getTime() ) : null;
		this.enrollmentDate =  rs.getTimestamp("enrollment_date") != null ? new java.util.Date( rs.getTimestamp("enrollment_date").getTime() ) : null;
		if (rs.getObject("age_enrollment") != null) this.ageEnrollment = rs.getInt("age_enrollment");
		if (rs.getObject("openmrs_current_age") != null) this.openmrsCurrentAge = rs.getInt("openmrs_current_age");
		this.maritalStatusAtEnrollment = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("marital_status_at_enrollment") != null ? rs.getString("marital_status_at_enrollment").trim() : null);
		this.pregnancyStatusAtEnrollment = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("pregnancy_status_at_enrollment") != null ? rs.getString("pregnancy_status_at_enrollment").trim() : null);
		this.womenStatus = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("women_status") != null ? rs.getString("women_status").trim() : null);
		this.educationAtEnrollment = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("education_at_enrollment") != null ? rs.getString("education_at_enrollment").trim() : null);
		this.occupationAtEnrollment = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("occupation_at_enrollment") != null ? rs.getString("occupation_at_enrollment").trim() : null);
		this.partnerStatusAtEnrollment = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("partner_status_at_enrollment") != null ? rs.getString("partner_status_at_enrollment").trim() : null);
		this.WHOClinicalStageAtEnrollment = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("WHO_clinical_stage_at_enrollment") != null ? rs.getString("WHO_clinical_stage_at_enrollment").trim() : null);
		this.WHOClinicalStageAtEnrollmentDate =  rs.getTimestamp("WHO_clinical_stage_at_enrollment_date") != null ? new java.util.Date( rs.getTimestamp("WHO_clinical_stage_at_enrollment_date").getTime() ) : null;
		this.weightEnrollment = rs.getDouble("weight_enrollment");
		this.weightDate =  rs.getTimestamp("weight_date") != null ? new java.util.Date( rs.getTimestamp("weight_date").getTime() ) : null;
		this.heightEnrollment = rs.getDouble("height_enrollment");
		this.heightDate =  rs.getTimestamp("height_date") != null ? new java.util.Date( rs.getTimestamp("height_date").getTime() ) : null;
		this.artInitiationDate =  rs.getTimestamp("art_initiation_date") != null ? new java.util.Date( rs.getTimestamp("art_initiation_date").getTime() ) : null;
		this.artRegimen = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("art_regimen") != null ? rs.getString("art_regimen").trim() : null);
		this.patientStatus = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("patient_status") != null ? rs.getString("patient_status").trim() : null);
		this.patientStatusDate =  rs.getTimestamp("patient_status_date") != null ? new java.util.Date( rs.getTimestamp("patient_status_date").getTime() ) : null;
		this.tbAtScreening = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("tb_at_screening") != null ? rs.getString("tb_at_screening").trim() : null);
		this.tbCoInfection = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("tb_co_infection") != null ? rs.getString("tb_co_infection").trim() : null);
		this.pmtctEntryDate =  rs.getTimestamp("pmtct_entry_date") != null ? new java.util.Date( rs.getTimestamp("pmtct_entry_date").getTime() ) : null;
		this.pmtctExitDate =  rs.getTimestamp("pmtct_exit_date") != null ? new java.util.Date( rs.getTimestamp("pmtct_exit_date").getTime() ) : null;
		if (rs.getObject("location_id") != null) this.locationId = rs.getInt("location_id");
		this.currentStatusInDmc = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("current_status_in_DMC") != null ? rs.getString("current_status_in_DMC").trim() : null);
		this.urban = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("urban") != null ? rs.getString("urban").trim() : null);
		this.main = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("main") != null ? rs.getString("main").trim() : null);
	} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "id"; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithoutObjectId(){ 
 		return "INSERT INTO community_arv_patient(patient_id, health_facility, district, sex, date_of_birth, enrollment_date, age_enrollment, openmrs_current_age, marital_status_at_enrollment, pregnancy_status_at_enrollment, women_status, education_at_enrollment, occupation_at_enrollment, partner_status_at_enrollment, WHO_clinical_stage_at_enrollment, WHO_clinical_stage_at_enrollment_date, weight_enrollment, weight_date, height_enrollment, height_date, art_initiation_date, art_regimen, patient_status, patient_status_date, tb_at_screening, tb_co_infection, pmtct_entry_date, pmtct_exit_date, location_id, current_status_in_DMC, urban, main) VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithoutObjectId(){ 
 		Object[] params = {this.patientId, this.healthFacility, this.district, this.sex, this.dateOfBirth, this.enrollmentDate, this.ageEnrollment, this.openmrsCurrentAge, this.maritalStatusAtEnrollment, this.pregnancyStatusAtEnrollment, this.womenStatus, this.educationAtEnrollment, this.occupationAtEnrollment, this.partnerStatusAtEnrollment, this.WHOClinicalStageAtEnrollment, this.WHOClinicalStageAtEnrollmentDate, this.weightEnrollment, this.weightDate, this.heightEnrollment, this.heightDate, this.artInitiationDate, this.artRegimen, this.patientStatus, this.patientStatusDate, this.tbAtScreening, this.tbCoInfection, this.pmtctEntryDate, this.pmtctExitDate, this.locationId, this.currentStatusInDmc, this.urban, this.main};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithObjectId(){ 
 		return "INSERT INTO community_arv_patient(id, patient_id, health_facility, district, sex, date_of_birth, enrollment_date, age_enrollment, openmrs_current_age, marital_status_at_enrollment, pregnancy_status_at_enrollment, women_status, education_at_enrollment, occupation_at_enrollment, partner_status_at_enrollment, WHO_clinical_stage_at_enrollment, WHO_clinical_stage_at_enrollment_date, weight_enrollment, weight_date, height_enrollment, height_date, art_initiation_date, art_regimen, patient_status, patient_status_date, tb_at_screening, tb_co_infection, pmtct_entry_date, pmtct_exit_date, location_id, current_status_in_DMC, urban, main) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithObjectId(){ 
 		Object[] params = {this.id, this.patientId, this.healthFacility, this.district, this.sex, this.dateOfBirth, this.enrollmentDate, this.ageEnrollment, this.openmrsCurrentAge, this.maritalStatusAtEnrollment, this.pregnancyStatusAtEnrollment, this.womenStatus, this.educationAtEnrollment, this.occupationAtEnrollment, this.partnerStatusAtEnrollment, this.WHOClinicalStageAtEnrollment, this.WHOClinicalStageAtEnrollmentDate, this.weightEnrollment, this.weightDate, this.heightEnrollment, this.heightDate, this.artInitiationDate, this.artRegimen, this.patientStatus, this.patientStatusDate, this.tbAtScreening, this.tbCoInfection, this.pmtctEntryDate, this.pmtctExitDate, this.locationId, this.currentStatusInDmc, this.urban, this.main};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.patientId, this.healthFacility, this.district, this.sex, this.dateOfBirth, this.enrollmentDate, this.ageEnrollment, this.openmrsCurrentAge, this.maritalStatusAtEnrollment, this.pregnancyStatusAtEnrollment, this.womenStatus, this.educationAtEnrollment, this.occupationAtEnrollment, this.partnerStatusAtEnrollment, this.WHOClinicalStageAtEnrollment, this.WHOClinicalStageAtEnrollmentDate, this.weightEnrollment, this.weightDate, this.heightEnrollment, this.heightDate, this.artInitiationDate, this.artRegimen, this.patientStatus, this.patientStatusDate, this.tbAtScreening, this.tbCoInfection, this.pmtctEntryDate, this.pmtctExitDate, this.locationId, this.currentStatusInDmc, this.urban, this.main, this.id};		return params; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE community_arv_patient SET patient_id = ?, health_facility = ?, district = ?, sex = ?, date_of_birth = ?, enrollment_date = ?, age_enrollment = ?, openmrs_current_age = ?, marital_status_at_enrollment = ?, pregnancy_status_at_enrollment = ?, women_status = ?, education_at_enrollment = ?, occupation_at_enrollment = ?, partner_status_at_enrollment = ?, WHO_clinical_stage_at_enrollment = ?, WHO_clinical_stage_at_enrollment_date = ?, weight_enrollment = ?, weight_date = ?, height_enrollment = ?, height_date = ?, art_initiation_date = ?, art_regimen = ?, patient_status = ?, patient_status_date = ?, tb_at_screening = ?, tb_co_infection = ?, pmtct_entry_date = ?, pmtct_exit_date = ?, location_id = ?, current_status_in_DMC = ?, urban = ?, main = ? WHERE id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.patientId) + "," + (this.healthFacility != null ? "\""+ utilities.scapeQuotationMarks(healthFacility)  +"\"" : null) + "," + (this.district != null ? "\""+ utilities.scapeQuotationMarks(district)  +"\"" : null) + "," + (this.sex != null ? "\""+ utilities.scapeQuotationMarks(sex)  +"\"" : null) + "," + (this.dateOfBirth != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateOfBirth)  +"\"" : null) + "," + (this.enrollmentDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(enrollmentDate)  +"\"" : null) + "," + (this.ageEnrollment) + "," + (this.openmrsCurrentAge) + "," + (this.maritalStatusAtEnrollment != null ? "\""+ utilities.scapeQuotationMarks(maritalStatusAtEnrollment)  +"\"" : null) + "," + (this.pregnancyStatusAtEnrollment != null ? "\""+ utilities.scapeQuotationMarks(pregnancyStatusAtEnrollment)  +"\"" : null) + "," + (this.womenStatus != null ? "\""+ utilities.scapeQuotationMarks(womenStatus)  +"\"" : null) + "," + (this.educationAtEnrollment != null ? "\""+ utilities.scapeQuotationMarks(educationAtEnrollment)  +"\"" : null) + "," + (this.occupationAtEnrollment != null ? "\""+ utilities.scapeQuotationMarks(occupationAtEnrollment)  +"\"" : null) + "," + (this.partnerStatusAtEnrollment != null ? "\""+ utilities.scapeQuotationMarks(partnerStatusAtEnrollment)  +"\"" : null) + "," + (this.WHOClinicalStageAtEnrollment != null ? "\""+ utilities.scapeQuotationMarks(WHOClinicalStageAtEnrollment)  +"\"" : null) + "," + (this.WHOClinicalStageAtEnrollmentDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(WHOClinicalStageAtEnrollmentDate)  +"\"" : null) + "," + (this.weightEnrollment) + "," + (this.weightDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(weightDate)  +"\"" : null) + "," + (this.heightEnrollment) + "," + (this.heightDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(heightDate)  +"\"" : null) + "," + (this.artInitiationDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(artInitiationDate)  +"\"" : null) + "," + (this.artRegimen != null ? "\""+ utilities.scapeQuotationMarks(artRegimen)  +"\"" : null) + "," + (this.patientStatus != null ? "\""+ utilities.scapeQuotationMarks(patientStatus)  +"\"" : null) + "," + (this.patientStatusDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(patientStatusDate)  +"\"" : null) + "," + (this.tbAtScreening != null ? "\""+ utilities.scapeQuotationMarks(tbAtScreening)  +"\"" : null) + "," + (this.tbCoInfection != null ? "\""+ utilities.scapeQuotationMarks(tbCoInfection)  +"\"" : null) + "," + (this.pmtctEntryDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(pmtctEntryDate)  +"\"" : null) + "," + (this.pmtctExitDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(pmtctExitDate)  +"\"" : null) + "," + (this.locationId) + "," + (this.currentStatusInDmc != null ? "\""+ utilities.scapeQuotationMarks(currentStatusInDmc)  +"\"" : null) + "," + (this.urban != null ? "\""+ utilities.scapeQuotationMarks(urban)  +"\"" : null) + "," + (this.main != null ? "\""+ utilities.scapeQuotationMarks(main)  +"\"" : null); 
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
		return "community_arv_patient";
	}


}