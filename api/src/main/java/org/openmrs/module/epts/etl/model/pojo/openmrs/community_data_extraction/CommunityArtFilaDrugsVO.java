package org.openmrs.module.epts.etl.model.pojo.openmrs.community_data_extraction;

import org.openmrs.module.epts.etl.model.pojo.generic.*; 
 
import org.openmrs.module.epts.etl.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.epts.etl.utilities.AttDefinedElements; 
 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class CommunityArtFilaDrugsVO extends AbstractDatabaseObject implements DatabaseObject { 
	private Integer id;
	private Integer patientId;
	private Integer encounterId;
	private Integer obsId;
	private String obsUuid;
	private String regime;
	private String formulation;
	private Integer groupId;
	private Integer quantity;
	private String dosage;
	private java.util.Date pickupDate;
	private java.util.Date nextArtDate;
	private String accommodationCamp;
	private String dispensationModel;
	private String source;
 
	public CommunityArtFilaDrugsVO() { 
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
 
	public void setEncounterId(Integer encounterId){ 
	 	this.encounterId = encounterId;
	}
 
	public Integer getEncounterId(){ 
		return this.encounterId;
	}
 
	public void setObsId(Integer obsId){ 
	 	this.obsId = obsId;
	}
 
	public Integer getObsId(){ 
		return this.obsId;
	}
 
	public void setObsUuid(String obsUuid){ 
	 	this.obsUuid = obsUuid;
	}
 
	public String getObsUuid(){ 
		return this.obsUuid;
	}
 
	public void setRegime(String regime){ 
	 	this.regime = regime;
	}
 
	public String getRegime(){ 
		return this.regime;
	}
 
	public void setFormulation(String formulation){ 
	 	this.formulation = formulation;
	}
 
	public String getFormulation(){ 
		return this.formulation;
	}
 
	public void setGroupId(Integer groupId){ 
	 	this.groupId = groupId;
	}
 
	public Integer getGroupId(){ 
		return this.groupId;
	}
 
	public void setQuantity(Integer quantity){ 
	 	this.quantity = quantity;
	}
 
	public Integer getQuantity(){ 
		return this.quantity;
	}
 
	public void setDosage(String dosage){ 
	 	this.dosage = dosage;
	}
 
	public String getDosage(){ 
		return this.dosage;
	}
 
	public void setPickupDate(java.util.Date pickupDate){ 
	 	this.pickupDate = pickupDate;
	}
 
	public java.util.Date getPickupDate(){ 
		return this.pickupDate;
	}
 
	public void setNextArtDate(java.util.Date nextArtDate){ 
	 	this.nextArtDate = nextArtDate;
	}
 
	public java.util.Date getNextArtDate(){ 
		return this.nextArtDate;
	}
 
	public void setAccommodationCamp(String accommodationCamp){ 
	 	this.accommodationCamp = accommodationCamp;
	}
 
	public String getAccommodationCamp(){ 
		return this.accommodationCamp;
	}
 
	public void setDispensationModel(String dispensationModel){ 
	 	this.dispensationModel = dispensationModel;
	}
 
	public String getDispensationModel(){ 
		return this.dispensationModel;
	}
 
	public void setSource(String source){ 
	 	this.source = source;
	}


 
	public String getSource(){ 
		return this.source;
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
		if (rs.getObject("encounter_id") != null) this.encounterId = rs.getInt("encounter_id");
		if (rs.getObject("obs_id") != null) this.obsId = rs.getInt("obs_id");
		this.obsUuid = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("obs_uuid") != null ? rs.getString("obs_uuid").trim() : null);
		this.regime = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("regime") != null ? rs.getString("regime").trim() : null);
		this.formulation = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("formulation") != null ? rs.getString("formulation").trim() : null);
		if (rs.getObject("group_id") != null) this.groupId = rs.getInt("group_id");
		if (rs.getObject("quantity") != null) this.quantity = rs.getInt("quantity");
		this.dosage = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("dosage") != null ? rs.getString("dosage").trim() : null);
		this.pickupDate =  rs.getTimestamp("pickup_date") != null ? new java.util.Date( rs.getTimestamp("pickup_date").getTime() ) : null;
		this.nextArtDate =  rs.getTimestamp("next_art_date") != null ? new java.util.Date( rs.getTimestamp("next_art_date").getTime() ) : null;
		this.accommodationCamp = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("accommodation_camp") != null ? rs.getString("accommodation_camp").trim() : null);
		this.dispensationModel = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("dispensation_model") != null ? rs.getString("dispensation_model").trim() : null);
		this.source = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("source") != null ? rs.getString("source").trim() : null);
	} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "id"; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithoutObjectId(){ 
 		return "INSERT INTO community_art_fila_drugs(patient_id, encounter_id, obs_id, obs_uuid, regime, formulation, group_id, quantity, dosage, pickup_date, next_art_date, accommodation_camp, dispensation_model, source) VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithoutObjectId(){ 
 		Object[] params = {this.patientId, this.encounterId, this.obsId, this.obsUuid, this.regime, this.formulation, this.groupId, this.quantity, this.dosage, this.pickupDate, this.nextArtDate, this.accommodationCamp, this.dispensationModel, this.source};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithObjectId(){ 
 		return "INSERT INTO community_art_fila_drugs(id, patient_id, encounter_id, obs_id, obs_uuid, regime, formulation, group_id, quantity, dosage, pickup_date, next_art_date, accommodation_camp, dispensation_model, source) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithObjectId(){ 
 		Object[] params = {this.id, this.patientId, this.encounterId, this.obsId, this.obsUuid, this.regime, this.formulation, this.groupId, this.quantity, this.dosage, this.pickupDate, this.nextArtDate, this.accommodationCamp, this.dispensationModel, this.source};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.patientId, this.encounterId, this.obsId, this.obsUuid, this.regime, this.formulation, this.groupId, this.quantity, this.dosage, this.pickupDate, this.nextArtDate, this.accommodationCamp, this.dispensationModel, this.source, this.id};		return params; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE community_art_fila_drugs SET patient_id = ?, encounter_id = ?, obs_id = ?, obs_uuid = ?, regime = ?, formulation = ?, group_id = ?, quantity = ?, dosage = ?, pickup_date = ?, next_art_date = ?, accommodation_camp = ?, dispensation_model = ?, source = ? WHERE id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.patientId) + "," + (this.encounterId) + "," + (this.obsId) + "," + (this.obsUuid != null ? "\""+ utilities.scapeQuotationMarks(obsUuid)  +"\"" : null) + "," + (this.regime != null ? "\""+ utilities.scapeQuotationMarks(regime)  +"\"" : null) + "," + (this.formulation != null ? "\""+ utilities.scapeQuotationMarks(formulation)  +"\"" : null) + "," + (this.groupId) + "," + (this.quantity) + "," + (this.dosage != null ? "\""+ utilities.scapeQuotationMarks(dosage)  +"\"" : null) + "," + (this.pickupDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(pickupDate)  +"\"" : null) + "," + (this.nextArtDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(nextArtDate)  +"\"" : null) + "," + (this.accommodationCamp != null ? "\""+ utilities.scapeQuotationMarks(accommodationCamp)  +"\"" : null) + "," + (this.dispensationModel != null ? "\""+ utilities.scapeQuotationMarks(dispensationModel)  +"\"" : null) + "," + (this.source != null ? "\""+ utilities.scapeQuotationMarks(source)  +"\"" : null); 
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
		return "community_art_fila_drugs";
	}


}