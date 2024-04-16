package org.openmrs.module.epts.etl.model.pojo.mozart.src;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.openmrs.module.epts.etl.model.pojo.generic.AbstractDatabaseObject;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObject;
import org.openmrs.module.epts.etl.utilities.AttDefinedElements;
import org.openmrs.module.epts.etl.utilities.DateAndTimeUtilities;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class TbDataVO extends AbstractDatabaseObject implements DatabaseObject {
	
	private Integer id;
	
	private String encounterUuid;
	
	private java.util.Date encounterDate;
	
	private Integer tbSymptom;
	
	private Integer symptomFever;
	
	private Integer symptomWeightLoss;
	
	private Integer symptomNightSweat;
	
	private Integer symptomCough;
	
	private Integer symptomAsthenia;
	
	private Integer symptomTbContact;
	
	private Integer symptomAdenopathy;
	
	private Integer tbDiagnose;
	
	private Integer tbTreatment;
	
	private java.util.Date tbTreatmentDate;
	
	public TbDataVO() {
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
	
	public void setEncounterDate(java.util.Date encounterDate) {
		this.encounterDate = encounterDate;
	}
	
	public java.util.Date getEncounterDate() {
		return this.encounterDate;
	}
	
	public void setTbSymptom(Integer tbSymptom) {
		this.tbSymptom = tbSymptom;
	}
	
	public Integer getTbSymptom() {
		return this.tbSymptom;
	}
	
	public void setSymptomFever(Integer symptomFever) {
		this.symptomFever = symptomFever;
	}
	
	public Integer getSymptomFever() {
		return this.symptomFever;
	}
	
	public void setSymptomWeightLoss(Integer symptomWeightLoss) {
		this.symptomWeightLoss = symptomWeightLoss;
	}
	
	public Integer getSymptomWeightLoss() {
		return this.symptomWeightLoss;
	}
	
	public void setSymptomNightSweat(Integer symptomNightSweat) {
		this.symptomNightSweat = symptomNightSweat;
	}
	
	public Integer getSymptomNightSweat() {
		return this.symptomNightSweat;
	}
	
	public void setSymptomCough(Integer symptomCough) {
		this.symptomCough = symptomCough;
	}
	
	public Integer getSymptomCough() {
		return this.symptomCough;
	}
	
	public void setSymptomAsthenia(Integer symptomAsthenia) {
		this.symptomAsthenia = symptomAsthenia;
	}
	
	public Integer getSymptomAsthenia() {
		return this.symptomAsthenia;
	}
	
	public void setSymptomTbContact(Integer symptomTbContact) {
		this.symptomTbContact = symptomTbContact;
	}
	
	public Integer getSymptomTbContact() {
		return this.symptomTbContact;
	}
	
	public void setSymptomAdenopathy(Integer symptomAdenopathy) {
		this.symptomAdenopathy = symptomAdenopathy;
	}
	
	public Integer getSymptomAdenopathy() {
		return this.symptomAdenopathy;
	}
	
	public void setTbDiagnose(Integer tbDiagnose) {
		this.tbDiagnose = tbDiagnose;
	}
	
	public Integer getTbDiagnose() {
		return this.tbDiagnose;
	}
	
	public void setTbTreatment(Integer tbTreatment) {
		this.tbTreatment = tbTreatment;
	}
	
	public Integer getTbTreatment() {
		return this.tbTreatment;
	}
	
	public void setTbTreatmentDate(java.util.Date tbTreatmentDate) {
		this.tbTreatmentDate = tbTreatmentDate;
	}
	
	public java.util.Date getTbTreatmentDate() {
		return this.tbTreatmentDate;
	}
	
	@Override
	public void load(ResultSet rs) throws SQLException {
		super.load(rs);
		
		if (rs.getObject("id") != null)
			this.id = rs.getInt("id");
		this.encounterUuid = AttDefinedElements.removeStrangeCharactersOnString(
		    rs.getString("encounter_uuid") != null ? rs.getString("encounter_uuid").trim() : null);
		this.encounterDate = rs.getTimestamp("encounter_date") != null
		        ? new java.util.Date(rs.getTimestamp("encounter_date").getTime())
		        : null;
		if (rs.getObject("tb_symptom") != null)
			this.tbSymptom = rs.getInt("tb_symptom");
		if (rs.getObject("symptom_fever") != null)
			this.symptomFever = rs.getInt("symptom_fever");
		if (rs.getObject("symptom_weight_loss") != null)
			this.symptomWeightLoss = rs.getInt("symptom_weight_loss");
		if (rs.getObject("symptom_night_sweat") != null)
			this.symptomNightSweat = rs.getInt("symptom_night_sweat");
		if (rs.getObject("symptom_cough") != null)
			this.symptomCough = rs.getInt("symptom_cough");
		if (rs.getObject("symptom_asthenia") != null)
			this.symptomAsthenia = rs.getInt("symptom_asthenia");
		if (rs.getObject("symptom_tb_contact") != null)
			this.symptomTbContact = rs.getInt("symptom_tb_contact");
		if (rs.getObject("symptom_adenopathy") != null)
			this.symptomAdenopathy = rs.getInt("symptom_adenopathy");
		if (rs.getObject("tb_diagnose") != null)
			this.tbDiagnose = rs.getInt("tb_diagnose");
		if (rs.getObject("tb_treatment") != null)
			this.tbTreatment = rs.getInt("tb_treatment");
		this.tbTreatmentDate = rs.getTimestamp("tb_treatment_date") != null
		        ? new java.util.Date(rs.getTimestamp("tb_treatment_date").getTime())
		        : null;
	}
	
	@JsonIgnore
	@Override
	public String getInsertSQLWithoutObjectId() {
		return "INSERT INTO tb_data(encounter_uuid, encounter_date, tb_symptom, symptom_fever, symptom_weight_loss, symptom_night_sweat, symptom_cough, symptom_asthenia, symptom_tb_contact, symptom_adenopathy, tb_diagnose, tb_treatment, tb_treatment_date) VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
	}
	
	@JsonIgnore
	@Override
	public String getInsertSQLWithObjectId() {
		return "INSERT INTO tb_data(id, encounter_uuid, encounter_date, tb_symptom, symptom_fever, symptom_weight_loss, symptom_night_sweat, symptom_cough, symptom_asthenia, symptom_tb_contact, symptom_adenopathy, tb_diagnose, tb_treatment, tb_treatment_date) VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
	}
	
	@JsonIgnore
	@Override
	public Object[] getInsertParamsWithoutObjectId() {
		Object[] params = { this.encounterUuid, this.encounterDate, this.tbSymptom, this.symptomFever,
		        this.symptomWeightLoss, this.symptomNightSweat, this.symptomCough, this.symptomAsthenia,
		        this.symptomTbContact, this.symptomAdenopathy, this.tbDiagnose, this.tbTreatment, this.tbTreatmentDate };
		return params;
	}
	
	@JsonIgnore
	@Override
	public Object[] getInsertParamsWithObjectId() {
		Object[] params = { this.id, this.encounterUuid, this.encounterDate, this.tbSymptom, this.symptomFever,
		        this.symptomWeightLoss, this.symptomNightSweat, this.symptomCough, this.symptomAsthenia,
		        this.symptomTbContact, this.symptomAdenopathy, this.tbDiagnose, this.tbTreatment, this.tbTreatmentDate };
		return params;
	}
	
	@JsonIgnore
	@Override
	public Object[] getUpdateParams() {
		Object[] params = { this.id, this.encounterUuid, this.encounterDate, this.tbSymptom, this.symptomFever,
		        this.symptomWeightLoss, this.symptomNightSweat, this.symptomCough, this.symptomAsthenia,
		        this.symptomTbContact, this.symptomAdenopathy, this.tbDiagnose, this.tbTreatment, this.tbTreatmentDate,
		        this.id };
		return params;
	}
	
	@JsonIgnore
	@Override
	public String getUpdateSQL() {
		return "UPDATE tb_data SET id = ?, encounter_uuid = ?, encounter_date = ?, tb_symptom = ?, symptom_fever = ?, symptom_weight_loss = ?, symptom_night_sweat = ?, symptom_cough = ?, symptom_asthenia = ?, symptom_tb_contact = ?, symptom_adenopathy = ?, tb_diagnose = ?, tb_treatment = ?, tb_treatment_date = ? WHERE id = ? ";
	}
	
	@JsonIgnore
	@Override
	public String generateInsertValuesWithoutObjectId() {
		return "" + (this.encounterUuid != null ? "\"" + utilities.scapeQuotationMarks(encounterUuid) + "\"" : null) + ","
		        + (this.encounterDate != null ? "\"" + DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(encounterDate) + "\""
		                : null)
		        + "," + (this.tbSymptom) + "," + (this.symptomFever) + "," + (this.symptomWeightLoss) + ","
		        + (this.symptomNightSweat) + "," + (this.symptomCough) + "," + (this.symptomAsthenia) + ","
		        + (this.symptomTbContact) + "," + (this.symptomAdenopathy) + "," + (this.tbDiagnose) + ","
		        + (this.tbTreatment) + ","
		        + (this.tbTreatmentDate != null ? "\"" + DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(tbTreatmentDate) + "\""
		                : null);
	}
	
	@JsonIgnore
	@Override
	public String generateInsertValuesWithObjectId() {
		return "" + (this.id) + ","
		        + (this.encounterUuid != null ? "\"" + utilities.scapeQuotationMarks(encounterUuid) + "\"" : null) + ","
		        + (this.encounterDate != null ? "\"" + DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(encounterDate) + "\""
		                : null)
		        + "," + (this.tbSymptom) + "," + (this.symptomFever) + "," + (this.symptomWeightLoss) + ","
		        + (this.symptomNightSweat) + "," + (this.symptomCough) + "," + (this.symptomAsthenia) + ","
		        + (this.symptomTbContact) + "," + (this.symptomAdenopathy) + "," + (this.tbDiagnose) + ","
		        + (this.tbTreatment) + ","
		        + (this.tbTreatmentDate != null ? "\"" + DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(tbTreatmentDate) + "\""
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
	public String generateTableName() {
		return "tb_data";
	}
	
}
