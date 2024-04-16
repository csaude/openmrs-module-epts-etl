package org.openmrs.module.epts.etl.model.pojo.mozart.partitioned;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.openmrs.module.epts.etl.model.pojo.generic.AbstractDatabaseObject;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObject;
import org.openmrs.module.epts.etl.utilities.AttDefinedElements;
import org.openmrs.module.epts.etl.utilities.DateAndTimeUtilities;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class MedicationVO extends AbstractDatabaseObject implements DatabaseObject {
	
	private String encounterUuid;
	
	private Integer regimenId;
	
	private Integer formulationId;
	
	private Double quantityPrescribed;
	
	private String dosage;
	
	private java.util.Date medicationPickupDate;
	
	private java.util.Date nextPickupDate;
	
	private Integer modeDispensationId;
	
	private Integer medSequenceId;
	
	private Integer typeDispensationId;
	
	private Integer alternativeLineId;
	
	private Integer reasonChangeRegimenId;
	
	private Integer medSideEffectsId;
	
	private Integer adherenceId;
	
	private String medicationUuid;
	
	private Integer formId;
	
	private Integer encounterType;
	
	private String patientUuid;
	
	private java.util.Date createdDate;
	
	private java.util.Date encounterDate;
	
	private java.util.Date changeDate;
	
	private String locationUuid;
	
	private String sourceDatabase;
	
	public MedicationVO() {
		this.metadata = false;
	}
	
	public void setEncounterUuid(String encounterUuid) {
		this.encounterUuid = encounterUuid;
	}
	
	public String getEncounterUuid() {
		return this.encounterUuid;
	}
	
	public void setRegimenId(Integer regimenId) {
		this.regimenId = regimenId;
	}
	
	public Integer getRegimenId() {
		return this.regimenId;
	}
	
	public void setFormulationId(Integer formulationId) {
		this.formulationId = formulationId;
	}
	
	public Integer getFormulationId() {
		return this.formulationId;
	}
	
	public void setQuantityPrescribed(Double quantityPrescribed) {
		this.quantityPrescribed = quantityPrescribed;
	}
	
	public Double getQuantityPrescribed() {
		return this.quantityPrescribed;
	}
	
	public void setDosage(String dosage) {
		this.dosage = dosage;
	}
	
	public String getDosage() {
		return this.dosage;
	}
	
	public void setMedicationPickupDate(java.util.Date medicationPickupDate) {
		this.medicationPickupDate = medicationPickupDate;
	}
	
	public java.util.Date getMedicationPickupDate() {
		return this.medicationPickupDate;
	}
	
	public void setNextPickupDate(java.util.Date nextPickupDate) {
		this.nextPickupDate = nextPickupDate;
	}
	
	public java.util.Date getNextPickupDate() {
		return this.nextPickupDate;
	}
	
	public void setModeDispensationId(Integer modeDispensationId) {
		this.modeDispensationId = modeDispensationId;
	}
	
	public Integer getModeDispensationId() {
		return this.modeDispensationId;
	}
	
	public void setMedSequenceId(Integer medSequenceId) {
		this.medSequenceId = medSequenceId;
	}
	
	public Integer getMedSequenceId() {
		return this.medSequenceId;
	}
	
	public void setTypeDispensationId(Integer typeDispensationId) {
		this.typeDispensationId = typeDispensationId;
	}
	
	public Integer getTypeDispensationId() {
		return this.typeDispensationId;
	}
	
	public void setAlternativeLineId(Integer alternativeLineId) {
		this.alternativeLineId = alternativeLineId;
	}
	
	public Integer getAlternativeLineId() {
		return this.alternativeLineId;
	}
	
	public void setReasonChangeRegimenId(Integer reasonChangeRegimenId) {
		this.reasonChangeRegimenId = reasonChangeRegimenId;
	}
	
	public Integer getReasonChangeRegimenId() {
		return this.reasonChangeRegimenId;
	}
	
	public void setMedSideEffectsId(Integer medSideEffectsId) {
		this.medSideEffectsId = medSideEffectsId;
	}
	
	public Integer getMedSideEffectsId() {
		return this.medSideEffectsId;
	}
	
	public void setAdherenceId(Integer adherenceId) {
		this.adherenceId = adherenceId;
	}
	
	public Integer getAdherenceId() {
		return this.adherenceId;
	}
	
	public void setMedicationUuid(String medicationUuid) {
		this.medicationUuid = medicationUuid;
	}
	
	public String getMedicationUuid() {
		return this.medicationUuid;
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
		if (rs.getObject("regimen_id") != null)
			this.regimenId = rs.getInt("regimen_id");
		if (rs.getObject("formulation_id") != null)
			this.formulationId = rs.getInt("formulation_id");
		if (rs.getObject("quantity_prescribed") != null)
			this.quantityPrescribed = rs.getDouble("quantity_prescribed");
		this.dosage = AttDefinedElements
		        .removeStrangeCharactersOnString(rs.getString("dosage") != null ? rs.getString("dosage").trim() : null);
		this.medicationPickupDate = rs.getTimestamp("medication_pickup_date") != null
		        ? new java.util.Date(rs.getTimestamp("medication_pickup_date").getTime())
		        : null;
		this.nextPickupDate = rs.getTimestamp("next_pickup_date") != null
		        ? new java.util.Date(rs.getTimestamp("next_pickup_date").getTime())
		        : null;
		if (rs.getObject("mode_dispensation_id") != null)
			this.modeDispensationId = rs.getInt("mode_dispensation_id");
		if (rs.getObject("med_sequence_id") != null)
			this.medSequenceId = rs.getInt("med_sequence_id");
		if (rs.getObject("type_dispensation_id") != null)
			this.typeDispensationId = rs.getInt("type_dispensation_id");
		if (rs.getObject("alternative_line_id") != null)
			this.alternativeLineId = rs.getInt("alternative_line_id");
		if (rs.getObject("reason_change_regimen_id") != null)
			this.reasonChangeRegimenId = rs.getInt("reason_change_regimen_id");
		if (rs.getObject("med_side_effects_id") != null)
			this.medSideEffectsId = rs.getInt("med_side_effects_id");
		if (rs.getObject("adherence_id") != null)
			this.adherenceId = rs.getInt("adherence_id");
		this.medicationUuid = AttDefinedElements.removeStrangeCharactersOnString(
		    rs.getString("medication_uuid") != null ? rs.getString("medication_uuid").trim() : null);
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
		return "INSERT INTO medication(encounter_uuid, regimen_id, formulation_id, quantity_prescribed, dosage, next_pickup_date, mode_dispensation_id, med_sequence_id, type_dispensation_id, alternative_line_id, reason_change_regimen_id, med_side_effects_id, adherence_id, form_id, encounter_type, patient_uuid, created_date, encounter_date, change_date, location_uuid, source_database) VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
	}
	
	@JsonIgnore
	@Override
	public String getInsertSQLWithObjectId() {
		return "INSERT INTO medication(encounter_uuid, regimen_id, formulation_id, quantity_prescribed, dosage, medication_pickup_date, next_pickup_date, mode_dispensation_id, med_sequence_id, type_dispensation_id, alternative_line_id, reason_change_regimen_id, med_side_effects_id, adherence_id, medication_uuid, form_id, encounter_type, patient_uuid, created_date, encounter_date, change_date, location_uuid, source_database) VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
	}
	
	@JsonIgnore
	@Override
	public Object[] getInsertParamsWithoutObjectId() {
		Object[] params = { this.encounterUuid, this.regimenId, this.formulationId, this.quantityPrescribed, this.dosage,
		        this.nextPickupDate, this.modeDispensationId, this.medSequenceId, this.typeDispensationId,
		        this.alternativeLineId, this.reasonChangeRegimenId, this.medSideEffectsId, this.adherenceId, this.formId,
		        this.encounterType, this.patientUuid, this.createdDate, this.encounterDate, this.changeDate,
		        this.locationUuid, this.sourceDatabase };
		return params;
	}
	
	@JsonIgnore
	@Override
	public Object[] getInsertParamsWithObjectId() {
		Object[] params = { this.encounterUuid, this.regimenId, this.formulationId, this.quantityPrescribed, this.dosage,
		        this.medicationPickupDate, this.nextPickupDate, this.modeDispensationId, this.medSequenceId,
		        this.typeDispensationId, this.alternativeLineId, this.reasonChangeRegimenId, this.medSideEffectsId,
		        this.adherenceId, this.medicationUuid, this.formId, this.encounterType, this.patientUuid, this.createdDate,
		        this.encounterDate, this.changeDate, this.locationUuid, this.sourceDatabase };
		return params;
	}
	
	@JsonIgnore
	@Override
	public Object[] getUpdateParams() {
		Object[] params = { this.encounterUuid, this.regimenId, this.formulationId, this.quantityPrescribed, this.dosage,
		        this.medicationPickupDate, this.nextPickupDate, this.modeDispensationId, this.medSequenceId,
		        this.typeDispensationId, this.alternativeLineId, this.reasonChangeRegimenId, this.medSideEffectsId,
		        this.adherenceId, this.medicationUuid, this.formId, this.encounterType, this.patientUuid, this.createdDate,
		        this.encounterDate, this.changeDate, this.locationUuid, this.sourceDatabase, this.medicationPickupDate,
		        this.medicationUuid };
		return params;
	}
	
	@JsonIgnore
	@Override
	public String getUpdateSQL() {
		return "UPDATE medication SET encounter_uuid = ?, regimen_id = ?, formulation_id = ?, quantity_prescribed = ?, dosage = ?, medication_pickup_date = ?, next_pickup_date = ?, mode_dispensation_id = ?, med_sequence_id = ?, type_dispensation_id = ?, alternative_line_id = ?, reason_change_regimen_id = ?, med_side_effects_id = ?, adherence_id = ?, medication_uuid = ?, form_id = ?, encounter_type = ?, patient_uuid = ?, created_date = ?, encounter_date = ?, change_date = ?, location_uuid = ?, source_database = ? WHERE medication_pickup_date = ?  AND medication_uuid = ? ";
	}
	
	@JsonIgnore
	@Override
	public String generateInsertValuesWithoutObjectId() {
		return "" + (this.encounterUuid != null ? "\"" + utilities.scapeQuotationMarks(encounterUuid) + "\"" : null) + ","
		        + (this.regimenId) + "," + (this.formulationId) + "," + (this.quantityPrescribed) + ","
		        + (this.dosage != null ? "\"" + utilities.scapeQuotationMarks(dosage) + "\"" : null) + ","
		        + (this.nextPickupDate != null ? "\"" + DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(nextPickupDate) + "\""
		                : null)
		        + "," + (this.modeDispensationId) + "," + (this.medSequenceId) + "," + (this.typeDispensationId) + ","
		        + (this.alternativeLineId) + "," + (this.reasonChangeRegimenId) + "," + (this.medSideEffectsId) + ","
		        + (this.adherenceId) + "," + (this.formId) + "," + (this.encounterType) + ","
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
	
	@JsonIgnore
	@Override
	public String generateInsertValuesWithObjectId() {
		return "" + (this.encounterUuid != null ? "\"" + utilities.scapeQuotationMarks(encounterUuid) + "\"" : null) + ","
		        + (this.regimenId) + "," + (this.formulationId) + "," + (this.quantityPrescribed) + ","
		        + (this.dosage != null ? "\"" + utilities.scapeQuotationMarks(dosage) + "\"" : null) + ","
		        + (this.medicationPickupDate != null
		                ? "\"" + DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(medicationPickupDate) + "\""
		                : null)
		        + ","
		        + (this.nextPickupDate != null ? "\"" + DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(nextPickupDate) + "\""
		                : null)
		        + "," + (this.modeDispensationId) + "," + (this.medSequenceId) + "," + (this.typeDispensationId) + ","
		        + (this.alternativeLineId) + "," + (this.reasonChangeRegimenId) + "," + (this.medSideEffectsId) + ","
		        + (this.adherenceId) + ","
		        + (this.medicationUuid != null ? "\"" + utilities.scapeQuotationMarks(medicationUuid) + "\"" : null) + ","
		        + (this.formId) + "," + (this.encounterType) + ","
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
		return "medication";
	}
	
}
