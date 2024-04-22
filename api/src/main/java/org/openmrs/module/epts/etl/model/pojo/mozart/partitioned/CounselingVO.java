package org.openmrs.module.epts.etl.model.pojo.mozart.partitioned;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.openmrs.module.epts.etl.model.pojo.generic.AbstractDatabaseObject;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObject;
import org.openmrs.module.epts.etl.utilities.AttDefinedElements;
import org.openmrs.module.epts.etl.utilities.DateAndTimeUtilities;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class CounselingVO extends AbstractDatabaseObject implements DatabaseObject {
	
	private String encounterUuid;
	
	private java.util.Date encounterDate;
	
	private Integer diagnosisReveal;
	
	private Integer hivDisclosure;
	
	private Integer adherencePlan;
	
	private Integer secondaryEffects;
	
	private Integer adherenceArt;
	
	private Double adherencePercent;
	
	private Integer consultationReason;
	
	private Integer acceptContact;
	
	private java.util.Date acceptDate;
	
	private Integer psychosocialRefusal;
	
	private Integer psychosocialSick;
	
	private Integer psychosocialNotbelieve;
	
	private Integer psychosocialLotofpills;
	
	private Integer psychosocialFeelbetter;
	
	private Integer psychosocialLackfood;
	
	private Integer psychosocialLacksupport;
	
	private Integer psychosocialDepression;
	
	private Integer psychosocialNotreveal;
	
	private Integer psychosocialToxicity;
	
	private Integer psychosocialLostpills;
	
	private Integer psychosocialStigma;
	
	private Integer psychosocialTransport;
	
	private Integer psychosocialGbv;
	
	private Integer psychosocialCultural;
	
	private Integer psychosocialDruguse;
	
	private Integer pp1;
	
	private Integer pp2;
	
	private Integer pp3;
	
	private Integer pp4;
	
	private Integer pp5;
	
	private Integer pp6;
	
	private Integer pp7;
	
	private Integer keypopLubricants;
	
	private Integer formId;
	
	private Integer encounterType;
	
	private String patientUuid;
	
	private java.util.Date createdDate;
	
	private java.util.Date changeDate;
	
	private String locationUuid;
	
	private String sourceDatabase;
	
	public CounselingVO() {
		this.metadata = false;
	}
	
	public void setEncounterUuid(String encounterUuid) {
		this.encounterUuid = encounterUuid;
	}
	
	public String getEncounterUuid() {
		return this.encounterUuid;
	}
	
	public void setEncounterDate(java.util.Date encounterDate) {
		this.encounterDate = encounterDate;
	}
	
	public java.util.Date getEncounterDate() {
		return this.encounterDate;
	}
	
	public void setDiagnosisReveal(Integer diagnosisReveal) {
		this.diagnosisReveal = diagnosisReveal;
	}
	
	public Integer getDiagnosisReveal() {
		return this.diagnosisReveal;
	}
	
	public void setHivDisclosure(Integer hivDisclosure) {
		this.hivDisclosure = hivDisclosure;
	}
	
	public Integer getHivDisclosure() {
		return this.hivDisclosure;
	}
	
	public void setAdherencePlan(Integer adherencePlan) {
		this.adherencePlan = adherencePlan;
	}
	
	public Integer getAdherencePlan() {
		return this.adherencePlan;
	}
	
	public void setSecondaryEffects(Integer secondaryEffects) {
		this.secondaryEffects = secondaryEffects;
	}
	
	public Integer getSecondaryEffects() {
		return this.secondaryEffects;
	}
	
	public void setAdherenceArt(Integer adherenceArt) {
		this.adherenceArt = adherenceArt;
	}
	
	public Integer getAdherenceArt() {
		return this.adherenceArt;
	}
	
	public void setAdherencePercent(Double adherencePercent) {
		this.adherencePercent = adherencePercent;
	}
	
	public Double getAdherencePercent() {
		return this.adherencePercent;
	}
	
	public void setConsultationReason(Integer consultationReason) {
		this.consultationReason = consultationReason;
	}
	
	public Integer getConsultationReason() {
		return this.consultationReason;
	}
	
	public void setAcceptContact(Integer acceptContact) {
		this.acceptContact = acceptContact;
	}
	
	public Integer getAcceptContact() {
		return this.acceptContact;
	}
	
	public void setAcceptDate(java.util.Date acceptDate) {
		this.acceptDate = acceptDate;
	}
	
	public java.util.Date getAcceptDate() {
		return this.acceptDate;
	}
	
	public void setPsychosocialRefusal(Integer psychosocialRefusal) {
		this.psychosocialRefusal = psychosocialRefusal;
	}
	
	public Integer getPsychosocialRefusal() {
		return this.psychosocialRefusal;
	}
	
	public void setPsychosocialSick(Integer psychosocialSick) {
		this.psychosocialSick = psychosocialSick;
	}
	
	public Integer getPsychosocialSick() {
		return this.psychosocialSick;
	}
	
	public void setPsychosocialNotbelieve(Integer psychosocialNotbelieve) {
		this.psychosocialNotbelieve = psychosocialNotbelieve;
	}
	
	public Integer getPsychosocialNotbelieve() {
		return this.psychosocialNotbelieve;
	}
	
	public void setPsychosocialLotofpills(Integer psychosocialLotofpills) {
		this.psychosocialLotofpills = psychosocialLotofpills;
	}
	
	public Integer getPsychosocialLotofpills() {
		return this.psychosocialLotofpills;
	}
	
	public void setPsychosocialFeelbetter(Integer psychosocialFeelbetter) {
		this.psychosocialFeelbetter = psychosocialFeelbetter;
	}
	
	public Integer getPsychosocialFeelbetter() {
		return this.psychosocialFeelbetter;
	}
	
	public void setPsychosocialLackfood(Integer psychosocialLackfood) {
		this.psychosocialLackfood = psychosocialLackfood;
	}
	
	public Integer getPsychosocialLackfood() {
		return this.psychosocialLackfood;
	}
	
	public void setPsychosocialLacksupport(Integer psychosocialLacksupport) {
		this.psychosocialLacksupport = psychosocialLacksupport;
	}
	
	public Integer getPsychosocialLacksupport() {
		return this.psychosocialLacksupport;
	}
	
	public void setPsychosocialDepression(Integer psychosocialDepression) {
		this.psychosocialDepression = psychosocialDepression;
	}
	
	public Integer getPsychosocialDepression() {
		return this.psychosocialDepression;
	}
	
	public void setPsychosocialNotreveal(Integer psychosocialNotreveal) {
		this.psychosocialNotreveal = psychosocialNotreveal;
	}
	
	public Integer getPsychosocialNotreveal() {
		return this.psychosocialNotreveal;
	}
	
	public void setPsychosocialToxicity(Integer psychosocialToxicity) {
		this.psychosocialToxicity = psychosocialToxicity;
	}
	
	public Integer getPsychosocialToxicity() {
		return this.psychosocialToxicity;
	}
	
	public void setPsychosocialLostpills(Integer psychosocialLostpills) {
		this.psychosocialLostpills = psychosocialLostpills;
	}
	
	public Integer getPsychosocialLostpills() {
		return this.psychosocialLostpills;
	}
	
	public void setPsychosocialStigma(Integer psychosocialStigma) {
		this.psychosocialStigma = psychosocialStigma;
	}
	
	public Integer getPsychosocialStigma() {
		return this.psychosocialStigma;
	}
	
	public void setPsychosocialTransport(Integer psychosocialTransport) {
		this.psychosocialTransport = psychosocialTransport;
	}
	
	public Integer getPsychosocialTransport() {
		return this.psychosocialTransport;
	}
	
	public void setPsychosocialGbv(Integer psychosocialGbv) {
		this.psychosocialGbv = psychosocialGbv;
	}
	
	public Integer getPsychosocialGbv() {
		return this.psychosocialGbv;
	}
	
	public void setPsychosocialCultural(Integer psychosocialCultural) {
		this.psychosocialCultural = psychosocialCultural;
	}
	
	public Integer getPsychosocialCultural() {
		return this.psychosocialCultural;
	}
	
	public void setPsychosocialDruguse(Integer psychosocialDruguse) {
		this.psychosocialDruguse = psychosocialDruguse;
	}
	
	public Integer getPsychosocialDruguse() {
		return this.psychosocialDruguse;
	}
	
	public void setPp1(Integer pp1) {
		this.pp1 = pp1;
	}
	
	public Integer getPp1() {
		return this.pp1;
	}
	
	public void setPp2(Integer pp2) {
		this.pp2 = pp2;
	}
	
	public Integer getPp2() {
		return this.pp2;
	}
	
	public void setPp3(Integer pp3) {
		this.pp3 = pp3;
	}
	
	public Integer getPp3() {
		return this.pp3;
	}
	
	public void setPp4(Integer pp4) {
		this.pp4 = pp4;
	}
	
	public Integer getPp4() {
		return this.pp4;
	}
	
	public void setPp5(Integer pp5) {
		this.pp5 = pp5;
	}
	
	public Integer getPp5() {
		return this.pp5;
	}
	
	public void setPp6(Integer pp6) {
		this.pp6 = pp6;
	}
	
	public Integer getPp6() {
		return this.pp6;
	}
	
	public void setPp7(Integer pp7) {
		this.pp7 = pp7;
	}
	
	public Integer getPp7() {
		return this.pp7;
	}
	
	public void setKeypopLubricants(Integer keypopLubricants) {
		this.keypopLubricants = keypopLubricants;
	}
	
	public Integer getKeypopLubricants() {
		return this.keypopLubricants;
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
		this.encounterDate = rs.getTimestamp("encounter_date") != null
		        ? new java.util.Date(rs.getTimestamp("encounter_date").getTime())
		        : null;
		if (rs.getObject("diagnosis_reveal") != null)
			this.diagnosisReveal = rs.getInt("diagnosis_reveal");
		if (rs.getObject("hiv_disclosure") != null)
			this.hivDisclosure = rs.getInt("hiv_disclosure");
		if (rs.getObject("adherence_plan") != null)
			this.adherencePlan = rs.getInt("adherence_plan");
		if (rs.getObject("secondary_effects") != null)
			this.secondaryEffects = rs.getInt("secondary_effects");
		if (rs.getObject("adherence_art") != null)
			this.adherenceArt = rs.getInt("adherence_art");
		if (rs.getObject("adherence_percent") != null)
			this.adherencePercent = rs.getDouble("adherence_percent");
		if (rs.getObject("consultation_reason") != null)
			this.consultationReason = rs.getInt("consultation_reason");
		if (rs.getObject("accept_contact") != null)
			this.acceptContact = rs.getInt("accept_contact");
		this.acceptDate = rs.getTimestamp("accept_date") != null
		        ? new java.util.Date(rs.getTimestamp("accept_date").getTime())
		        : null;
		if (rs.getObject("psychosocial_refusal") != null)
			this.psychosocialRefusal = rs.getInt("psychosocial_refusal");
		if (rs.getObject("psychosocial_sick") != null)
			this.psychosocialSick = rs.getInt("psychosocial_sick");
		if (rs.getObject("psychosocial_notbelieve") != null)
			this.psychosocialNotbelieve = rs.getInt("psychosocial_notbelieve");
		if (rs.getObject("psychosocial_lotofpills") != null)
			this.psychosocialLotofpills = rs.getInt("psychosocial_lotofpills");
		if (rs.getObject("psychosocial_feelbetter") != null)
			this.psychosocialFeelbetter = rs.getInt("psychosocial_feelbetter");
		if (rs.getObject("psychosocial_lackfood") != null)
			this.psychosocialLackfood = rs.getInt("psychosocial_lackfood");
		if (rs.getObject("psychosocial_lacksupport") != null)
			this.psychosocialLacksupport = rs.getInt("psychosocial_lacksupport");
		if (rs.getObject("psychosocial_depression") != null)
			this.psychosocialDepression = rs.getInt("psychosocial_depression");
		if (rs.getObject("psychosocial_notreveal") != null)
			this.psychosocialNotreveal = rs.getInt("psychosocial_notreveal");
		if (rs.getObject("psychosocial_toxicity") != null)
			this.psychosocialToxicity = rs.getInt("psychosocial_toxicity");
		if (rs.getObject("psychosocial_lostpills") != null)
			this.psychosocialLostpills = rs.getInt("psychosocial_lostpills");
		if (rs.getObject("psychosocial_stigma") != null)
			this.psychosocialStigma = rs.getInt("psychosocial_stigma");
		if (rs.getObject("psychosocial_transport") != null)
			this.psychosocialTransport = rs.getInt("psychosocial_transport");
		if (rs.getObject("psychosocial_gbv") != null)
			this.psychosocialGbv = rs.getInt("psychosocial_gbv");
		if (rs.getObject("psychosocial_cultural") != null)
			this.psychosocialCultural = rs.getInt("psychosocial_cultural");
		if (rs.getObject("psychosocial_druguse") != null)
			this.psychosocialDruguse = rs.getInt("psychosocial_druguse");
		if (rs.getObject("pp1") != null)
			this.pp1 = rs.getInt("pp1");
		if (rs.getObject("pp2") != null)
			this.pp2 = rs.getInt("pp2");
		if (rs.getObject("pp3") != null)
			this.pp3 = rs.getInt("pp3");
		if (rs.getObject("pp4") != null)
			this.pp4 = rs.getInt("pp4");
		if (rs.getObject("pp5") != null)
			this.pp5 = rs.getInt("pp5");
		if (rs.getObject("pp6") != null)
			this.pp6 = rs.getInt("pp6");
		if (rs.getObject("pp7") != null)
			this.pp7 = rs.getInt("pp7");
		if (rs.getObject("keypop_lubricants") != null)
			this.keypopLubricants = rs.getInt("keypop_lubricants");
		if (rs.getObject("form_id") != null)
			this.formId = rs.getInt("form_id");
		if (rs.getObject("encounter_type") != null)
			this.encounterType = rs.getInt("encounter_type");
		this.patientUuid = AttDefinedElements.removeStrangeCharactersOnString(
		    rs.getString("patient_uuid") != null ? rs.getString("patient_uuid").trim() : null);
		this.createdDate = rs.getTimestamp("created_date") != null
		        ? new java.util.Date(rs.getTimestamp("created_date").getTime())
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
		return "INSERT INTO counseling(diagnosis_reveal, hiv_disclosure, adherence_plan, secondary_effects, adherence_art, adherence_percent, consultation_reason, accept_contact, accept_date, psychosocial_refusal, psychosocial_sick, psychosocial_notbelieve, psychosocial_lotofpills, psychosocial_feelbetter, psychosocial_lackfood, psychosocial_lacksupport, psychosocial_depression, psychosocial_notreveal, psychosocial_toxicity, psychosocial_lostpills, psychosocial_stigma, psychosocial_transport, psychosocial_gbv, psychosocial_cultural, psychosocial_druguse, pp1, pp2, pp3, pp4, pp5, pp6, pp7, keypop_lubricants, form_id, encounter_type, patient_uuid, created_date, change_date, location_uuid, source_database) VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
	}
	
	@JsonIgnore
	@Override
	public String getInsertSQLWithObjectId() {
		return "INSERT INTO counseling(encounter_uuid, encounter_date, diagnosis_reveal, hiv_disclosure, adherence_plan, secondary_effects, adherence_art, adherence_percent, consultation_reason, accept_contact, accept_date, psychosocial_refusal, psychosocial_sick, psychosocial_notbelieve, psychosocial_lotofpills, psychosocial_feelbetter, psychosocial_lackfood, psychosocial_lacksupport, psychosocial_depression, psychosocial_notreveal, psychosocial_toxicity, psychosocial_lostpills, psychosocial_stigma, psychosocial_transport, psychosocial_gbv, psychosocial_cultural, psychosocial_druguse, pp1, pp2, pp3, pp4, pp5, pp6, pp7, keypop_lubricants, form_id, encounter_type, patient_uuid, created_date, change_date, location_uuid, source_database) VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
	}
	
	@JsonIgnore
	@Override
	public Object[] getInsertParamsWithoutObjectId() {
		Object[] params = { this.diagnosisReveal, this.hivDisclosure, this.adherencePlan, this.secondaryEffects,
		        this.adherenceArt, this.adherencePercent, this.consultationReason, this.acceptContact, this.acceptDate,
		        this.psychosocialRefusal, this.psychosocialSick, this.psychosocialNotbelieve, this.psychosocialLotofpills,
		        this.psychosocialFeelbetter, this.psychosocialLackfood, this.psychosocialLacksupport,
		        this.psychosocialDepression, this.psychosocialNotreveal, this.psychosocialToxicity,
		        this.psychosocialLostpills, this.psychosocialStigma, this.psychosocialTransport, this.psychosocialGbv,
		        this.psychosocialCultural, this.psychosocialDruguse, this.pp1, this.pp2, this.pp3, this.pp4, this.pp5,
		        this.pp6, this.pp7, this.keypopLubricants, this.formId, this.encounterType, this.patientUuid,
		        this.createdDate, this.changeDate, this.locationUuid, this.sourceDatabase };
		return params;
	}
	
	@JsonIgnore
	@Override
	public Object[] getInsertParamsWithObjectId() {
		Object[] params = { this.encounterUuid, this.encounterDate, this.diagnosisReveal, this.hivDisclosure,
		        this.adherencePlan, this.secondaryEffects, this.adherenceArt, this.adherencePercent, this.consultationReason,
		        this.acceptContact, this.acceptDate, this.psychosocialRefusal, this.psychosocialSick,
		        this.psychosocialNotbelieve, this.psychosocialLotofpills, this.psychosocialFeelbetter,
		        this.psychosocialLackfood, this.psychosocialLacksupport, this.psychosocialDepression,
		        this.psychosocialNotreveal, this.psychosocialToxicity, this.psychosocialLostpills, this.psychosocialStigma,
		        this.psychosocialTransport, this.psychosocialGbv, this.psychosocialCultural, this.psychosocialDruguse,
		        this.pp1, this.pp2, this.pp3, this.pp4, this.pp5, this.pp6, this.pp7, this.keypopLubricants, this.formId,
		        this.encounterType, this.patientUuid, this.createdDate, this.changeDate, this.locationUuid,
		        this.sourceDatabase };
		return params;
	}
	
	@JsonIgnore
	@Override
	public Object[] getUpdateParams() {
		Object[] params = { this.encounterUuid, this.encounterDate, this.diagnosisReveal, this.hivDisclosure,
		        this.adherencePlan, this.secondaryEffects, this.adherenceArt, this.adherencePercent, this.consultationReason,
		        this.acceptContact, this.acceptDate, this.psychosocialRefusal, this.psychosocialSick,
		        this.psychosocialNotbelieve, this.psychosocialLotofpills, this.psychosocialFeelbetter,
		        this.psychosocialLackfood, this.psychosocialLacksupport, this.psychosocialDepression,
		        this.psychosocialNotreveal, this.psychosocialToxicity, this.psychosocialLostpills, this.psychosocialStigma,
		        this.psychosocialTransport, this.psychosocialGbv, this.psychosocialCultural, this.psychosocialDruguse,
		        this.pp1, this.pp2, this.pp3, this.pp4, this.pp5, this.pp6, this.pp7, this.keypopLubricants, this.formId,
		        this.encounterType, this.patientUuid, this.createdDate, this.changeDate, this.locationUuid,
		        this.sourceDatabase, this.encounterDate, this.encounterUuid };
		return params;
	}
	
	@JsonIgnore
	@Override
	public String getUpdateSQL() {
		return "UPDATE counseling SET encounter_uuid = ?, encounter_date = ?, diagnosis_reveal = ?, hiv_disclosure = ?, adherence_plan = ?, secondary_effects = ?, adherence_art = ?, adherence_percent = ?, consultation_reason = ?, accept_contact = ?, accept_date = ?, psychosocial_refusal = ?, psychosocial_sick = ?, psychosocial_notbelieve = ?, psychosocial_lotofpills = ?, psychosocial_feelbetter = ?, psychosocial_lackfood = ?, psychosocial_lacksupport = ?, psychosocial_depression = ?, psychosocial_notreveal = ?, psychosocial_toxicity = ?, psychosocial_lostpills = ?, psychosocial_stigma = ?, psychosocial_transport = ?, psychosocial_gbv = ?, psychosocial_cultural = ?, psychosocial_druguse = ?, pp1 = ?, pp2 = ?, pp3 = ?, pp4 = ?, pp5 = ?, pp6 = ?, pp7 = ?, keypop_lubricants = ?, form_id = ?, encounter_type = ?, patient_uuid = ?, created_date = ?, change_date = ?, location_uuid = ?, source_database = ? WHERE encounter_date = ?  AND encounter_uuid = ? ";
	}
	
	@JsonIgnore
	@Override
	public String generateInsertValuesWithoutObjectId() {
		return "" + (this.diagnosisReveal) + "," + (this.hivDisclosure) + "," + (this.adherencePlan) + ","
		        + (this.secondaryEffects) + "," + (this.adherenceArt) + "," + (this.adherencePercent) + ","
		        + (this.consultationReason) + "," + (this.acceptContact) + ","
		        + (this.acceptDate != null ? "\"" + DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(acceptDate) + "\"" : null)
		        + "," + (this.psychosocialRefusal) + "," + (this.psychosocialSick) + "," + (this.psychosocialNotbelieve)
		        + "," + (this.psychosocialLotofpills) + "," + (this.psychosocialFeelbetter) + ","
		        + (this.psychosocialLackfood) + "," + (this.psychosocialLacksupport) + "," + (this.psychosocialDepression)
		        + "," + (this.psychosocialNotreveal) + "," + (this.psychosocialToxicity) + "," + (this.psychosocialLostpills)
		        + "," + (this.psychosocialStigma) + "," + (this.psychosocialTransport) + "," + (this.psychosocialGbv) + ","
		        + (this.psychosocialCultural) + "," + (this.psychosocialDruguse) + "," + (this.pp1) + "," + (this.pp2) + ","
		        + (this.pp3) + "," + (this.pp4) + "," + (this.pp5) + "," + (this.pp6) + "," + (this.pp7) + ","
		        + (this.keypopLubricants) + "," + (this.formId) + "," + (this.encounterType) + ","
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
		        + (this.encounterDate != null ? "\"" + DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(encounterDate) + "\""
		                : null)
		        + "," + (this.diagnosisReveal) + "," + (this.hivDisclosure) + "," + (this.adherencePlan) + ","
		        + (this.secondaryEffects) + "," + (this.adherenceArt) + "," + (this.adherencePercent) + ","
		        + (this.consultationReason) + "," + (this.acceptContact) + ","
		        + (this.acceptDate != null ? "\"" + DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(acceptDate) + "\"" : null)
		        + "," + (this.psychosocialRefusal) + "," + (this.psychosocialSick) + "," + (this.psychosocialNotbelieve)
		        + "," + (this.psychosocialLotofpills) + "," + (this.psychosocialFeelbetter) + ","
		        + (this.psychosocialLackfood) + "," + (this.psychosocialLacksupport) + "," + (this.psychosocialDepression)
		        + "," + (this.psychosocialNotreveal) + "," + (this.psychosocialToxicity) + "," + (this.psychosocialLostpills)
		        + "," + (this.psychosocialStigma) + "," + (this.psychosocialTransport) + "," + (this.psychosocialGbv) + ","
		        + (this.psychosocialCultural) + "," + (this.psychosocialDruguse) + "," + (this.pp1) + "," + (this.pp2) + ","
		        + (this.pp3) + "," + (this.pp4) + "," + (this.pp5) + "," + (this.pp6) + "," + (this.pp7) + ","
		        + (this.keypopLubricants) + "," + (this.formId) + "," + (this.encounterType) + ","
		        + (this.patientUuid != null ? "\"" + utilities.scapeQuotationMarks(patientUuid) + "\"" : null) + ","
		        + (this.createdDate != null ? "\"" + DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(createdDate) + "\"" : null)
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
		return "counseling";
	}
	
}
