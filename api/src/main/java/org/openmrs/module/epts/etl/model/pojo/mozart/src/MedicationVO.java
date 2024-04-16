package org.openmrs.module.epts.etl.model.pojo.mozart.src;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.openmrs.module.epts.etl.model.pojo.generic.AbstractDatabaseObject;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObject;
import org.openmrs.module.epts.etl.utilities.AttDefinedElements;
import org.openmrs.module.epts.etl.utilities.DateAndTimeUtilities;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class MedicationVO extends AbstractDatabaseObject implements DatabaseObject {
	
	private Integer id;
	
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
	
	public MedicationVO() {
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
	
	@Override
	public void load(ResultSet rs) throws SQLException {
		super.load(rs);
		
		if (rs.getObject("id") != null)
			this.id = rs.getInt("id");
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
	}
	
	@JsonIgnore
	@Override
	public String getInsertSQLWithoutObjectId() {
		return "INSERT INTO medication(encounter_uuid, regimen_id, formulation_id, quantity_prescribed, dosage, medication_pickup_date, next_pickup_date, mode_dispensation_id, med_sequence_id, type_dispensation_id, alternative_line_id, reason_change_regimen_id, med_side_effects_id, adherence_id, medication_uuid) VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
	}
	
	@JsonIgnore
	@Override
	public String getInsertSQLWithObjectId() {
		return "INSERT INTO medication(id, encounter_uuid, regimen_id, formulation_id, quantity_prescribed, dosage, medication_pickup_date, next_pickup_date, mode_dispensation_id, med_sequence_id, type_dispensation_id, alternative_line_id, reason_change_regimen_id, med_side_effects_id, adherence_id, medication_uuid) VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
	}
	
	@JsonIgnore
	@Override
	public Object[] getInsertParamsWithoutObjectId() {
		Object[] params = { this.encounterUuid, this.regimenId, this.formulationId, this.quantityPrescribed, this.dosage,
		        this.medicationPickupDate, this.nextPickupDate, this.modeDispensationId, this.medSequenceId,
		        this.typeDispensationId, this.alternativeLineId, this.reasonChangeRegimenId, this.medSideEffectsId,
		        this.adherenceId, this.medicationUuid };
		return params;
	}
	
	@JsonIgnore
	@Override
	public Object[] getInsertParamsWithObjectId() {
		Object[] params = { this.id, this.encounterUuid, this.regimenId, this.formulationId, this.quantityPrescribed,
		        this.dosage, this.medicationPickupDate, this.nextPickupDate, this.modeDispensationId, this.medSequenceId,
		        this.typeDispensationId, this.alternativeLineId, this.reasonChangeRegimenId, this.medSideEffectsId,
		        this.adherenceId, this.medicationUuid };
		return params;
	}
	
	@JsonIgnore
	@Override
	public Object[] getUpdateParams() {
		Object[] params = { this.id, this.encounterUuid, this.regimenId, this.formulationId, this.quantityPrescribed,
		        this.dosage, this.medicationPickupDate, this.nextPickupDate, this.modeDispensationId, this.medSequenceId,
		        this.typeDispensationId, this.alternativeLineId, this.reasonChangeRegimenId, this.medSideEffectsId,
		        this.adherenceId, this.medicationUuid, this.id };
		return params;
	}
	
	@JsonIgnore
	@Override
	public String getUpdateSQL() {
		return "UPDATE medication SET id = ?, encounter_uuid = ?, regimen_id = ?, formulation_id = ?, quantity_prescribed = ?, dosage = ?, medication_pickup_date = ?, next_pickup_date = ?, mode_dispensation_id = ?, med_sequence_id = ?, type_dispensation_id = ?, alternative_line_id = ?, reason_change_regimen_id = ?, med_side_effects_id = ?, adherence_id = ?, medication_uuid = ? WHERE id = ? ";
	}
	
	@JsonIgnore
	@Override
	public String generateInsertValuesWithoutObjectId() {
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
		        + (this.medicationUuid != null ? "\"" + utilities.scapeQuotationMarks(medicationUuid) + "\"" : null);
	}
	
	@JsonIgnore
	@Override
	public String generateInsertValuesWithObjectId() {
		return "" + (this.id) + ","
		        + (this.encounterUuid != null ? "\"" + utilities.scapeQuotationMarks(encounterUuid) + "\"" : null) + ","
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
		        + (this.medicationUuid != null ? "\"" + utilities.scapeQuotationMarks(medicationUuid) + "\"" : null);
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
