package org.openmrs.module.epts.etl.model.pojo.openmrs._default;

import org.openmrs.module.epts.etl.model.pojo.generic.*; 
 
import org.openmrs.module.epts.etl.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.epts.etl.utilities.AttDefinedElements; 
 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class EncounterVO extends AbstractDatabaseObject implements DatabaseObject { 
	private Integer encounterId;
	private Integer encounterType;
	private Integer patientId;
	private Integer locationId;
	private Integer formId;
	private java.util.Date encounterDatetime;
	private Integer creator;
	private Byte voided;
	private Integer voidedBy;
	private java.util.Date dateVoided;
	private String voidReason;
	private Integer changedBy;
	private Integer visitId;
 
	public EncounterVO() { 
		this.metadata = false;
	} 
 
	public void setEncounterId(Integer encounterId){ 
	 	this.encounterId = encounterId;
	}
 
	public Integer getEncounterId(){ 
		return this.encounterId;
	}
 
	public void setEncounterType(Integer encounterType){ 
	 	this.encounterType = encounterType;
	}
 
	public Integer getEncounterType(){ 
		return this.encounterType;
	}
 
	public void setPatientId(Integer patientId){ 
	 	this.patientId = patientId;
	}
 
	public Integer getPatientId(){ 
		return this.patientId;
	}
 
	public void setLocationId(Integer locationId){ 
	 	this.locationId = locationId;
	}
 
	public Integer getLocationId(){ 
		return this.locationId;
	}
 
	public void setFormId(Integer formId){ 
	 	this.formId = formId;
	}
 
	public Integer getFormId(){ 
		return this.formId;
	}
 
	public void setEncounterDatetime(java.util.Date encounterDatetime){ 
	 	this.encounterDatetime = encounterDatetime;
	}
 
	public java.util.Date getEncounterDatetime(){ 
		return this.encounterDatetime;
	}
 
	public void setCreator(Integer creator){ 
	 	this.creator = creator;
	}
 
	public Integer getCreator(){ 
		return this.creator;
	}
 
	public void setVoided(Byte voided){ 
	 	this.voided = voided;
	}
 
	public Byte getVoided(){ 
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
 
	public void setChangedBy(Integer changedBy){ 
	 	this.changedBy = changedBy;
	}
 
	public Integer getChangedBy(){ 
		return this.changedBy;
	}
 
	public void setVisitId(Integer visitId){ 
	 	this.visitId = visitId;
	}


 
	public Integer getVisitId(){ 
		return this.visitId;
	}
 
	@Override
	public void load(ResultSet rs) throws SQLException{ 
		super.load(rs);
 
if (rs.getObject("encounter_id") != null) this.encounterId = rs.getInt("encounter_id");
if (rs.getObject("encounter_type") != null) this.encounterType = rs.getInt("encounter_type");
if (rs.getObject("patient_id") != null) this.patientId = rs.getInt("patient_id");
if (rs.getObject("location_id") != null) this.locationId = rs.getInt("location_id");
if (rs.getObject("form_id") != null) this.formId = rs.getInt("form_id");
this.encounterDatetime =  rs.getTimestamp("encounter_datetime") != null ? new java.util.Date( rs.getTimestamp("encounter_datetime").getTime() ) : null;
if (rs.getObject("creator") != null) this.creator = rs.getInt("creator");
this.dateCreated =  rs.getTimestamp("date_created") != null ? new java.util.Date( rs.getTimestamp("date_created").getTime() ) : null;
this.voided = rs.getByte("voided");
if (rs.getObject("voided_by") != null) this.voidedBy = rs.getInt("voided_by");
this.dateVoided =  rs.getTimestamp("date_voided") != null ? new java.util.Date( rs.getTimestamp("date_voided").getTime() ) : null;
this.voidReason = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("void_reason") != null ? rs.getString("void_reason").trim() : null);
this.uuid = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("uuid") != null ? rs.getString("uuid").trim() : null);
if (rs.getObject("changed_by") != null) this.changedBy = rs.getInt("changed_by");
this.dateChanged =  rs.getTimestamp("date_changed") != null ? new java.util.Date( rs.getTimestamp("date_changed").getTime() ) : null;
if (rs.getObject("visit_id") != null) this.visitId = rs.getInt("visit_id");
	} 
 
	@JsonIgnore
	@Override
	public String getInsertSQLWithoutObjectId(){ 
 		return "INSERT INTO encounter(encounter_type, patient_id, location_id, form_id, encounter_datetime, creator, date_created, voided, voided_by, date_voided, void_reason, uuid, changed_by, date_changed, visit_id) VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	@Override
	public String getInsertSQLWithObjectId(){ 
 		return "INSERT INTO encounter(encounter_id, encounter_type, patient_id, location_id, form_id, encounter_datetime, creator, date_created, voided, voided_by, date_voided, void_reason, uuid, changed_by, date_changed, visit_id) VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	@Override
	public Object[]  getInsertParamsWithoutObjectId(){ 
 		Object[] params = {this.encounterType, this.patientId, this.locationId, this.formId, this.encounterDatetime, this.creator, this.dateCreated, this.voided, this.voidedBy, this.dateVoided, this.voidReason, this.uuid, this.changedBy, this.dateChanged, this.visitId};
		return params; 
	} 
 
	@JsonIgnore
	@Override
	public Object[]  getInsertParamsWithObjectId(){ 
 		Object[] params = {this.encounterId, this.encounterType, this.patientId, this.locationId, this.formId, this.encounterDatetime, this.creator, this.dateCreated, this.voided, this.voidedBy, this.dateVoided, this.voidReason, this.uuid, this.changedBy, this.dateChanged, this.visitId};
		return params; 
	} 
 
	@JsonIgnore
	@Override
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.encounterId, this.encounterType, this.patientId, this.locationId, this.formId, this.encounterDatetime, this.creator, this.dateCreated, this.voided, this.voidedBy, this.dateVoided, this.voidReason, this.uuid, this.changedBy, this.dateChanged, this.visitId, this.encounterId};
		return params; 
	} 
 
	@JsonIgnore
	@Override
	public String getUpdateSQL(){ 
 		return "UPDATE encounter SET encounter_id = ?, encounter_type = ?, patient_id = ?, location_id = ?, form_id = ?, encounter_datetime = ?, creator = ?, date_created = ?, voided = ?, voided_by = ?, date_voided = ?, void_reason = ?, uuid = ?, changed_by = ?, date_changed = ?, visit_id = ? WHERE encounter_id = ? "; 
	} 
 
	@JsonIgnore
	@Override
	public String generateInsertValuesWithoutObjectId(){ 
 		return ""+(this.encounterType) + "," + (this.patientId) + "," + (this.locationId) + "," + (this.formId) + "," + (this.encounterDatetime != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(encounterDatetime)  +"\"" : null) + "," + (this.creator) + "," + (this.dateCreated != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateCreated)  +"\"" : null) + "," + (this.voided) + "," + (this.voidedBy) + "," + (this.dateVoided != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateVoided)  +"\"" : null) + "," + (this.voidReason != null ? "\""+ utilities.scapeQuotationMarks(voidReason)  +"\"" : null) + "," + (this.uuid != null ? "\""+ utilities.scapeQuotationMarks(uuid)  +"\"" : null) + "," + (this.changedBy) + "," + (this.dateChanged != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateChanged)  +"\"" : null) + "," + (this.visitId); 
	} 
 
	@JsonIgnore
	@Override
	public String generateInsertValuesWithObjectId(){ 
 		return ""+(this.encounterId) + "," + (this.encounterType) + "," + (this.patientId) + "," + (this.locationId) + "," + (this.formId) + "," + (this.encounterDatetime != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(encounterDatetime)  +"\"" : null) + "," + (this.creator) + "," + (this.dateCreated != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateCreated)  +"\"" : null) + "," + (this.voided) + "," + (this.voidedBy) + "," + (this.dateVoided != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateVoided)  +"\"" : null) + "," + (this.voidReason != null ? "\""+ utilities.scapeQuotationMarks(voidReason)  +"\"" : null) + "," + (this.uuid != null ? "\""+ utilities.scapeQuotationMarks(uuid)  +"\"" : null) + "," + (this.changedBy) + "," + (this.dateChanged != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateChanged)  +"\"" : null) + "," + (this.visitId); 
	} 
 
	@Override
	public boolean hasParents() {
		if (this.changedBy != null) return true;

		if (this.formId != null) return true;

		if (this.creator != null) return true;

		if (this.locationId != null) return true;

		if (this.patientId != null) return true;

		if (this.encounterType != null) return true;

		if (this.visitId != null) return true;

		if (this.voidedBy != null) return true;

		return false;
	}

	@Override
	public Integer getParentValue(String parentAttName) {		
		if (parentAttName.equals("changedBy")) return this.changedBy;		
		if (parentAttName.equals("formId")) return this.formId;		
		if (parentAttName.equals("creator")) return this.creator;		
		if (parentAttName.equals("locationId")) return this.locationId;		
		if (parentAttName.equals("patientId")) return this.patientId;		
		if (parentAttName.equals("encounterType")) return this.encounterType;		
		if (parentAttName.equals("visitId")) return this.visitId;		
		if (parentAttName.equals("voidedBy")) return this.voidedBy;

		throw new RuntimeException("No found parent for: " + parentAttName);	}

	@Override
	public String generateTableName() {
		return "encounter";
	}


}