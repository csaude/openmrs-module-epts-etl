package org.openmrs.module.eptssync.model.pojo.openmrs._default;

import org.openmrs.module.eptssync.model.pojo.generic.*; 
 
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.eptssync.utilities.AttDefinedElements; 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class ObsVO extends AbstractDatabaseObject implements DatabaseObject { 
	private Integer obsId;
	private Integer personId;
	private Integer conceptId;
	private Integer encounterId;
	private Integer orderId;
	private java.util.Date obsDatetime;
	private Integer locationId;
	private Integer obsGroupId;
	private String accessionNumber;
	private Integer valueGroupId;
	private Integer valueCoded;
	private Integer valueCodedNameId;
	private Integer valueDrug;
	private java.util.Date valueDatetime;
	private double valueNumeric;
	private String valueModifier;
	private String valueText;
	private String comments;
	private Integer creator;
	private byte voided;
	private Integer voidedBy;
	private java.util.Date dateVoided;
	private String voidReason;
	private String valueComplex;
	private Integer previousVersion;
	private String formNamespaceAndPath;
	private String status;
	private String interpretation;
 
	public ObsVO() { 
		this.metadata = false;
	} 
 
	public void setObsId(Integer obsId){ 
	 	this.obsId = obsId;
	}
 
	public Integer getObsId(){ 
		return this.obsId;
	}
 
	public void setPersonId(Integer personId){ 
	 	this.personId = personId;
	}
 
	public Integer getPersonId(){ 
		return this.personId;
	}
 
	public void setConceptId(Integer conceptId){ 
	 	this.conceptId = conceptId;
	}
 
	public Integer getConceptId(){ 
		return this.conceptId;
	}
 
	public void setEncounterId(Integer encounterId){ 
	 	this.encounterId = encounterId;
	}
 
	public Integer getEncounterId(){ 
		return this.encounterId;
	}
 
	public void setOrderId(Integer orderId){ 
	 	this.orderId = orderId;
	}
 
	public Integer getOrderId(){ 
		return this.orderId;
	}
 
	public void setObsDatetime(java.util.Date obsDatetime){ 
	 	this.obsDatetime = obsDatetime;
	}
 
	public java.util.Date getObsDatetime(){ 
		return this.obsDatetime;
	}
 
	public void setLocationId(Integer locationId){ 
	 	this.locationId = locationId;
	}
 
	public Integer getLocationId(){ 
		return this.locationId;
	}
 
	public void setObsGroupId(Integer obsGroupId){ 
	 	this.obsGroupId = obsGroupId;
	}
 
	public Integer getObsGroupId(){ 
		return this.obsGroupId;
	}
 
	public void setAccessionNumber(String accessionNumber){ 
	 	this.accessionNumber = accessionNumber;
	}
 
	public String getAccessionNumber(){ 
		return this.accessionNumber;
	}
 
	public void setValueGroupId(Integer valueGroupId){ 
	 	this.valueGroupId = valueGroupId;
	}
 
	public Integer getValueGroupId(){ 
		return this.valueGroupId;
	}
 
	public void setValueCoded(Integer valueCoded){ 
	 	this.valueCoded = valueCoded;
	}
 
	public Integer getValueCoded(){ 
		return this.valueCoded;
	}
 
	public void setValueCodedNameId(Integer valueCodedNameId){ 
	 	this.valueCodedNameId = valueCodedNameId;
	}
 
	public Integer getValueCodedNameId(){ 
		return this.valueCodedNameId;
	}
 
	public void setValueDrug(Integer valueDrug){ 
	 	this.valueDrug = valueDrug;
	}
 
	public Integer getValueDrug(){ 
		return this.valueDrug;
	}
 
	public void setValueDatetime(java.util.Date valueDatetime){ 
	 	this.valueDatetime = valueDatetime;
	}
 
	public java.util.Date getValueDatetime(){ 
		return this.valueDatetime;
	}
 
	public void setValueNumeric(double valueNumeric){ 
	 	this.valueNumeric = valueNumeric;
	}
 
	public double getValueNumeric(){ 
		return this.valueNumeric;
	}
 
	public void setValueModifier(String valueModifier){ 
	 	this.valueModifier = valueModifier;
	}
 
	public String getValueModifier(){ 
		return this.valueModifier;
	}
 
	public void setValueText(String valueText){ 
	 	this.valueText = valueText;
	}
 
	public String getValueText(){ 
		return this.valueText;
	}
 
	public void setComments(String comments){ 
	 	this.comments = comments;
	}
 
	public String getComments(){ 
		return this.comments;
	}
 
	public void setCreator(Integer creator){ 
	 	this.creator = creator;
	}
 
	public Integer getCreator(){ 
		return this.creator;
	}
 
	public void setVoided(byte voided){ 
	 	this.voided = voided;
	}
 
	public byte getVoided(){ 
		return this.voided;
	}
 
	public void setVoidedBy(Integer voidedBy){ 
	 	this.voidedBy = voidedBy;
	}
 
	public Integer getVoidedBy(){ 
		return this.voidedBy;
	}
 
	public void setDateVoided(java.util.Date dateVoided){ 
	 	this.dateVoided = dateVoided;
	}
 
	public java.util.Date getDateVoided(){ 
		return this.dateVoided;
	}
 
	public void setVoidReason(String voidReason){ 
	 	this.voidReason = voidReason;
	}
 
	public String getVoidReason(){ 
		return this.voidReason;
	}
 
	public void setValueComplex(String valueComplex){ 
	 	this.valueComplex = valueComplex;
	}
 
	public String getValueComplex(){ 
		return this.valueComplex;
	}
 
	public void setPreviousVersion(Integer previousVersion){ 
	 	this.previousVersion = previousVersion;
	}
 
	public Integer getPreviousVersion(){ 
		return this.previousVersion;
	}
 
	public void setFormNamespaceAndPath(String formNamespaceAndPath){ 
	 	this.formNamespaceAndPath = formNamespaceAndPath;
	}
 
	public String getFormNamespaceAndPath(){ 
		return this.formNamespaceAndPath;
	}
 
	public void setStatus(String status){ 
	 	this.status = status;
	}
 
	public String getStatus(){ 
		return this.status;
	}
 
	public void setInterpretation(String interpretation){ 
	 	this.interpretation = interpretation;
	}


 
	public String getIntegererpretation(){ 
		return this.interpretation;
	}
 
	public Integer getObjectId() { 
 		return this.obsId; 
	} 
 
	public void setObjectId(Integer selfId){ 
		this.obsId = selfId; 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		super.load(rs);
		if (rs.getObject("obs_id") != null) this.obsId = rs.getInt("obs_id");
		if (rs.getObject("person_id") != null) this.personId = rs.getInt("person_id");
		if (rs.getObject("concept_id") != null) this.conceptId = rs.getInt("concept_id");
		if (rs.getObject("encounter_id") != null) this.encounterId = rs.getInt("encounter_id");
		if (rs.getObject("order_id") != null) this.orderId = rs.getInt("order_id");
		this.obsDatetime =  rs.getTimestamp("obs_datetime") != null ? new java.util.Date( rs.getTimestamp("obs_datetime").getTime() ) : null;
		if (rs.getObject("location_id") != null) this.locationId = rs.getInt("location_id");
		if (rs.getObject("obs_group_id") != null) this.obsGroupId = rs.getInt("obs_group_id");
		this.accessionNumber = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("accession_number") != null ? rs.getString("accession_number").trim() : null);
		if (rs.getObject("value_group_id") != null) this.valueGroupId = rs.getInt("value_group_id");
		if (rs.getObject("value_coded") != null) this.valueCoded = rs.getInt("value_coded");
		if (rs.getObject("value_coded_name_id") != null) this.valueCodedNameId = rs.getInt("value_coded_name_id");
		if (rs.getObject("value_drug") != null) this.valueDrug = rs.getInt("value_drug");
		this.valueDatetime =  rs.getTimestamp("value_datetime") != null ? new java.util.Date( rs.getTimestamp("value_datetime").getTime() ) : null;
		this.valueNumeric = rs.getDouble("value_numeric");
		this.valueModifier = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("value_modifier") != null ? rs.getString("value_modifier").trim() : null);
		this.valueText = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("value_text") != null ? rs.getString("value_text").trim() : null);
		this.comments = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("comments") != null ? rs.getString("comments").trim() : null);
		if (rs.getObject("creator") != null) this.creator = rs.getInt("creator");
		this.dateCreated =  rs.getTimestamp("date_created") != null ? new java.util.Date( rs.getTimestamp("date_created").getTime() ) : null;
		this.voided = rs.getByte("voided");
		if (rs.getObject("voided_by") != null) this.voidedBy = rs.getInt("voided_by");
		this.dateVoided =  rs.getTimestamp("date_voided") != null ? new java.util.Date( rs.getTimestamp("date_voided").getTime() ) : null;
		this.voidReason = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("void_reason") != null ? rs.getString("void_reason").trim() : null);
		this.valueComplex = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("value_complex") != null ? rs.getString("value_complex").trim() : null);
		this.uuid = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("uuid") != null ? rs.getString("uuid").trim() : null);
		if (rs.getObject("previous_version") != null) this.previousVersion = rs.getInt("previous_version");
		this.formNamespaceAndPath = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("form_namespace_and_path") != null ? rs.getString("form_namespace_and_path").trim() : null);
		this.status = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("status") != null ? rs.getString("status").trim() : null);
		this.interpretation = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("interpretation") != null ? rs.getString("interpretation").trim() : null);
	} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "obs_id"; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithoutObjectId(){ 
 		return "INSERT INTO obs(person_id, concept_id, encounter_id, order_id, obs_datetime, location_id, obs_group_id, accession_number, value_group_id, value_coded, value_coded_name_id, value_drug, value_datetime, value_numeric, value_modifier, value_text, comments, creator, date_created, voided, voided_by, date_voided, void_reason, value_complex, uuid, previous_version, form_namespace_and_path, status, interpretation) VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithoutObjectId(){ 
 		Object[] params = {this.personId, this.conceptId, this.encounterId, this.orderId, this.obsDatetime, this.locationId, this.obsGroupId, this.accessionNumber, this.valueGroupId, this.valueCoded, this.valueCodedNameId, this.valueDrug, this.valueDatetime, this.valueNumeric, this.valueModifier, this.valueText, this.comments, this.creator, this.dateCreated, this.voided, this.voidedBy, this.dateVoided, this.voidReason, this.valueComplex, this.uuid, this.previousVersion, this.formNamespaceAndPath, this.status, this.interpretation};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithObjectId(){ 
 		return "INSERT INTO obs(obs_id, person_id, concept_id, encounter_id, order_id, obs_datetime, location_id, obs_group_id, accession_number, value_group_id, value_coded, value_coded_name_id, value_drug, value_datetime, value_numeric, value_modifier, value_text, comments, creator, date_created, voided, voided_by, date_voided, void_reason, value_complex, uuid, previous_version, form_namespace_and_path, status, interpretation) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithObjectId(){ 
 		Object[] params = {this.obsId, this.personId, this.conceptId, this.encounterId, this.orderId, this.obsDatetime, this.locationId, this.obsGroupId, this.accessionNumber, this.valueGroupId, this.valueCoded, this.valueCodedNameId, this.valueDrug, this.valueDatetime, this.valueNumeric, this.valueModifier, this.valueText, this.comments, this.creator, this.dateCreated, this.voided, this.voidedBy, this.dateVoided, this.voidReason, this.valueComplex, this.uuid, this.previousVersion, this.formNamespaceAndPath, this.status, this.interpretation};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.personId, this.conceptId, this.encounterId, this.orderId, this.obsDatetime, this.locationId, this.obsGroupId, this.accessionNumber, this.valueGroupId, this.valueCoded, this.valueCodedNameId, this.valueDrug, this.valueDatetime, this.valueNumeric, this.valueModifier, this.valueText, this.comments, this.creator, this.dateCreated, this.voided, this.voidedBy, this.dateVoided, this.voidReason, this.valueComplex, this.uuid, this.previousVersion, this.formNamespaceAndPath, this.status, this.interpretation, this.obsId};		return params; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE obs SET person_id = ?, concept_id = ?, encounter_id = ?, order_id = ?, obs_datetime = ?, location_id = ?, obs_group_id = ?, accession_number = ?, value_group_id = ?, value_coded = ?, value_coded_name_id = ?, value_drug = ?, value_datetime = ?, value_numeric = ?, value_modifier = ?, value_text = ?, comments = ?, creator = ?, date_created = ?, voided = ?, voided_by = ?, date_voided = ?, void_reason = ?, value_complex = ?, uuid = ?, previous_version = ?, form_namespace_and_path = ?, status = ?, interpretation = ? WHERE obs_id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.personId) + "," + (this.conceptId) + "," + (this.encounterId) + "," + (this.orderId) + "," + (this.obsDatetime != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(obsDatetime)  +"\"" : null) + "," + (this.locationId) + "," + (this.obsGroupId) + "," + (this.accessionNumber != null ? "\""+ utilities.scapeQuotationMarks(accessionNumber)  +"\"" : null) + "," + (this.valueGroupId) + "," + (this.valueCoded) + "," + (this.valueCodedNameId) + "," + (this.valueDrug) + "," + (this.valueDatetime != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(valueDatetime)  +"\"" : null) + "," + (this.valueNumeric) + "," + (this.valueModifier != null ? "\""+ utilities.scapeQuotationMarks(valueModifier)  +"\"" : null) + "," + (this.valueText != null ? "\""+ utilities.scapeQuotationMarks(valueText)  +"\"" : null) + "," + (this.comments != null ? "\""+ utilities.scapeQuotationMarks(comments)  +"\"" : null) + "," + (this.creator) + "," + (this.dateCreated != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateCreated)  +"\"" : null) + "," + (this.voided) + "," + (this.voidedBy) + "," + (this.dateVoided != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateVoided)  +"\"" : null) + "," + (this.voidReason != null ? "\""+ utilities.scapeQuotationMarks(voidReason)  +"\"" : null) + "," + (this.valueComplex != null ? "\""+ utilities.scapeQuotationMarks(valueComplex)  +"\"" : null) + "," + (this.uuid != null ? "\""+ utilities.scapeQuotationMarks(uuid)  +"\"" : null) + "," + (this.previousVersion) + "," + (this.formNamespaceAndPath != null ? "\""+ utilities.scapeQuotationMarks(formNamespaceAndPath)  +"\"" : null) + "," + (this.status != null ? "\""+ utilities.scapeQuotationMarks(status)  +"\"" : null) + "," + (this.interpretation != null ? "\""+ utilities.scapeQuotationMarks(interpretation)  +"\"" : null); 
	} 
 
	@Override
	public boolean hasParents() {
		if (this.valueCoded != 0) return true;

		if (this.valueDrug != 0) return true;

		if (this.encounterId != 0) return true;

		if (this.conceptId != 0) return true;

		if (this.creator != 0) return true;

		if (this.obsGroupId != 0) return true;

		if (this.locationId != 0) return true;

		if (this.valueCodedNameId != 0) return true;

		if (this.orderId != 0) return true;

		if (this.personId != 0) return true;

		if (this.previousVersion != 0) return true;

		if (this.voidedBy != 0) return true;

		return false;
	}

	@Override
	public Integer getParentValue(String parentAttName) {		
		if (parentAttName.equals("valueCoded")) return this.valueCoded;		
		if (parentAttName.equals("valueDrug")) return this.valueDrug;		
		if (parentAttName.equals("encounterId")) return this.encounterId;		
		if (parentAttName.equals("conceptId")) return this.conceptId;		
		if (parentAttName.equals("creator")) return this.creator;		
		if (parentAttName.equals("obsGroupId")) return this.obsGroupId;		
		if (parentAttName.equals("locationId")) return this.locationId;		
		if (parentAttName.equals("valueCodedNameId")) return this.valueCodedNameId;		
		if (parentAttName.equals("orderId")) return this.orderId;		
		if (parentAttName.equals("personId")) return this.personId;		
		if (parentAttName.equals("previousVersion")) return this.previousVersion;		
		if (parentAttName.equals("voidedBy")) return this.voidedBy;

		throw new RuntimeException("No found parent for: " + parentAttName);	}

	@Override
	public void changeParentValue(String parentAttName, DatabaseObject newParent) {		
		if (parentAttName.equals("valueCoded")) {
			this.valueCoded = newParent.getObjectId();
			return;
		}		
		if (parentAttName.equals("valueDrug")) {
			this.valueDrug = newParent.getObjectId();
			return;
		}		
		if (parentAttName.equals("encounterId")) {
			this.encounterId = newParent.getObjectId();
			return;
		}		
		if (parentAttName.equals("conceptId")) {
			this.conceptId = newParent.getObjectId();
			return;
		}		
		if (parentAttName.equals("creator")) {
			this.creator = newParent.getObjectId();
			return;
		}		
		if (parentAttName.equals("obsGroupId")) {
			this.obsGroupId = newParent.getObjectId();
			return;
		}		
		if (parentAttName.equals("locationId")) {
			this.locationId = newParent.getObjectId();
			return;
		}		
		if (parentAttName.equals("valueCodedNameId")) {
			this.valueCodedNameId = newParent.getObjectId();
			return;
		}		
		if (parentAttName.equals("orderId")) {
			this.orderId = newParent.getObjectId();
			return;
		}		
		if (parentAttName.equals("personId")) {
			this.personId = newParent.getObjectId();
			return;
		}		
		if (parentAttName.equals("previousVersion")) {
			this.previousVersion = newParent.getObjectId();
			return;
		}		
		if (parentAttName.equals("voidedBy")) {
			this.voidedBy = newParent.getObjectId();
			return;
		}

		throw new RuntimeException("No found parent for: " + parentAttName);
	}

	@Override
	public void setParentToNull(String parentAttName) {		
		if (parentAttName.equals("valueCoded")) {
			this.valueCoded = null;
			return;
		}		
		if (parentAttName.equals("valueDrug")) {
			this.valueDrug = null;
			return;
		}		
		if (parentAttName.equals("encounterId")) {
			this.encounterId = null;
			return;
		}		
		if (parentAttName.equals("conceptId")) {
			this.conceptId = null;
			return;
		}		
		if (parentAttName.equals("creator")) {
			this.creator = null;
			return;
		}		
		if (parentAttName.equals("obsGroupId")) {
			this.obsGroupId = null;
			return;
		}		
		if (parentAttName.equals("locationId")) {
			this.locationId = null;
			return;
		}		
		if (parentAttName.equals("valueCodedNameId")) {
			this.valueCodedNameId = null;
			return;
		}		
		if (parentAttName.equals("orderId")) {
			this.orderId = null;
			return;
		}		
		if (parentAttName.equals("personId")) {
			this.personId = null;
			return;
		}		
		if (parentAttName.equals("previousVersion")) {
			this.previousVersion = null;
			return;
		}		
		if (parentAttName.equals("voidedBy")) {
			this.voidedBy = null;
			return;
		}

		throw new RuntimeException("No found parent for: " + parentAttName);
	}


}