package org.openmrs.module.eptssync.model.pojo.destination; 
 
import org.openmrs.module.eptssync.model.pojo.generic.*; 
 
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.eptssync.utilities.AttDefinedElements; 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class EncounterVO extends AbstractOpenMRSObject implements OpenMRSObject { 
	private int encounterId;
	private int encounterType;
	private int patientId;
	private int locationId;
	private int formId;
	private java.util.Date encounterDatetime;
	private int creator;
	private byte voided;
	private int voidedBy;
	private java.util.Date dateVoided;
	private String voidReason;
	private int changedBy;
	private int visitId;
 
	public EncounterVO() { 
		this.metadata = false;
	} 
 
	public void setEncounterId(int encounterId){ 
	 	this.encounterId = encounterId;
	}
 
	public int getEncounterId(){ 
		return this.encounterId;
	}
 
	public void setEncounterType(int encounterType){ 
	 	this.encounterType = encounterType;
	}
 
	public int getEncounterType(){ 
		return this.encounterType;
	}
 
	public void setPatientId(int patientId){ 
	 	this.patientId = patientId;
	}
 
	public int getPatientId(){ 
		return this.patientId;
	}
 
	public void setLocationId(int locationId){ 
	 	this.locationId = locationId;
	}
 
	public int getLocationId(){ 
		return this.locationId;
	}
 
	public void setFormId(int formId){ 
	 	this.formId = formId;
	}
 
	public int getFormId(){ 
		return this.formId;
	}
 
	public void setEncounterDatetime(java.util.Date encounterDatetime){ 
	 	this.encounterDatetime = encounterDatetime;
	}
 
	public java.util.Date getEncounterDatetime(){ 
		return this.encounterDatetime;
	}
 
	public void setCreator(int creator){ 
	 	this.creator = creator;
	}
 
	public int getCreator(){ 
		return this.creator;
	}
 
	public void setVoided(byte voided){ 
	 	this.voided = voided;
	}
 
	public byte getVoided(){ 
		return this.voided;
	}
 
	public void setVoidedBy(int voidedBy){ 
	 	this.voidedBy = voidedBy;
	}
 
	public int getVoidedBy(){ 
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
 
	public void setChangedBy(int changedBy){ 
	 	this.changedBy = changedBy;
	}
 
	public int getChangedBy(){ 
		return this.changedBy;
	}
 
	public void setVisitId(int visitId){ 
	 	this.visitId = visitId;
	}


 
	public int getVisitId(){ 
		return this.visitId;
	}
 
	public int getObjectId() { 
 		return this.encounterId; 
	} 
 
	public void setObjectId(int selfId){ 
		this.encounterId = selfId; 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		this.encounterId = rs.getInt("encounter_id");
		this.encounterType = rs.getInt("encounter_type");
		this.patientId = rs.getInt("patient_id");
		this.locationId = rs.getInt("location_id");
		this.formId = rs.getInt("form_id");
		this.encounterDatetime =  rs.getTimestamp("encounter_datetime") != null ? new java.util.Date( rs.getTimestamp("encounter_datetime").getTime() ) : null;
		this.creator = rs.getInt("creator");
		this.dateCreated =  rs.getTimestamp("date_created") != null ? new java.util.Date( rs.getTimestamp("date_created").getTime() ) : null;
		this.voided = rs.getByte("voided");
		this.voidedBy = rs.getInt("voided_by");
		this.dateVoided =  rs.getTimestamp("date_voided") != null ? new java.util.Date( rs.getTimestamp("date_voided").getTime() ) : null;
		this.voidReason = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("void_reason") != null ? rs.getString("void_reason").trim() : null);
		this.uuid = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("uuid") != null ? rs.getString("uuid").trim() : null);
		this.changedBy = rs.getInt("changed_by");
		this.dateChanged =  rs.getTimestamp("date_changed") != null ? new java.util.Date( rs.getTimestamp("date_changed").getTime() ) : null;
		this.visitId = rs.getInt("visit_id");
	} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "encounter_id"; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithoutObjectId(){ 
 		return "INSERT INTO encounter(encounter_type, patient_id, location_id, form_id, encounter_datetime, creator, date_created, voided, voided_by, date_voided, void_reason, uuid, changed_by, date_changed, visit_id) VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithoutObjectId(){ 
 		Object[] params = {this.encounterType == 0 ? null : this.encounterType, this.patientId == 0 ? null : this.patientId, this.locationId == 0 ? null : this.locationId, this.formId == 0 ? null : this.formId, this.encounterDatetime, this.creator == 0 ? null : this.creator, this.dateCreated, this.voided, this.voidedBy == 0 ? null : this.voidedBy, this.dateVoided, this.voidReason, this.uuid, this.changedBy == 0 ? null : this.changedBy, this.dateChanged, this.visitId == 0 ? null : this.visitId};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithObjectId(){ 
 		return "INSERT INTO encounter(encounter_id, encounter_type, patient_id, location_id, form_id, encounter_datetime, creator, date_created, voided, voided_by, date_voided, void_reason, uuid, changed_by, date_changed, visit_id) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithObjectId(){ 
 		Object[] params = {this.encounterId, this.encounterType == 0 ? null : this.encounterType, this.patientId == 0 ? null : this.patientId, this.locationId == 0 ? null : this.locationId, this.formId == 0 ? null : this.formId, this.encounterDatetime, this.creator == 0 ? null : this.creator, this.dateCreated, this.voided, this.voidedBy == 0 ? null : this.voidedBy, this.dateVoided, this.voidReason, this.uuid, this.changedBy == 0 ? null : this.changedBy, this.dateChanged, this.visitId == 0 ? null : this.visitId};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.encounterType == 0 ? null : this.encounterType, this.patientId == 0 ? null : this.patientId, this.locationId == 0 ? null : this.locationId, this.formId == 0 ? null : this.formId, this.encounterDatetime, this.creator == 0 ? null : this.creator, this.dateCreated, this.voided, this.voidedBy == 0 ? null : this.voidedBy, this.dateVoided, this.voidReason, this.uuid, this.changedBy == 0 ? null : this.changedBy, this.dateChanged, this.visitId == 0 ? null : this.visitId, this.encounterId};		return params; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE encounter SET encounter_type = ?, patient_id = ?, location_id = ?, form_id = ?, encounter_datetime = ?, creator = ?, date_created = ?, voided = ?, voided_by = ?, date_voided = ?, void_reason = ?, uuid = ?, changed_by = ?, date_changed = ?, visit_id = ? WHERE encounter_id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.encounterType == 0 ? null : this.encounterType) + "," + (this.patientId == 0 ? null : this.patientId) + "," + (this.locationId == 0 ? null : this.locationId) + "," + (this.formId == 0 ? null : this.formId) + "," + (this.encounterDatetime != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(encounterDatetime)  +"\"" : null) + "," + (this.creator == 0 ? null : this.creator) + "," + (this.dateCreated != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateCreated)  +"\"" : null) + "," + (this.voided) + "," + (this.voidedBy == 0 ? null : this.voidedBy) + "," + (this.dateVoided != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateVoided)  +"\"" : null) + "," + (this.voidReason != null ? "\""+ utilities.scapeQuotationMarks(voidReason)  +"\"" : null) + "," + (this.uuid != null ? "\""+ utilities.scapeQuotationMarks(uuid)  +"\"" : null) + "," + (this.changedBy == 0 ? null : this.changedBy) + "," + (this.dateChanged != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateChanged)  +"\"" : null) + "," + (this.visitId == 0 ? null : this.visitId); 
	} 
 
	@Override
	public boolean hasParents() {
		if (this.changedBy != 0) return true;

		if (this.formId != 0) return true;

		if (this.creator != 0) return true;

		if (this.locationId != 0) return true;

		if (this.patientId != 0) return true;

		if (this.encounterType != 0) return true;

		if (this.visitId != 0) return true;

		if (this.voidedBy != 0) return true;

		return false;
	}

	@Override
	public int getParentValue(String parentAttName) {		
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
	public void changeParentValue(String parentAttName, OpenMRSObject newParent) {		
		if (parentAttName.equals("changedBy")) {
			this.changedBy = newParent.getObjectId();
			return;
		}		
		if (parentAttName.equals("formId")) {
			this.formId = newParent.getObjectId();
			return;
		}		
		if (parentAttName.equals("creator")) {
			this.creator = newParent.getObjectId();
			return;
		}		
		if (parentAttName.equals("locationId")) {
			this.locationId = newParent.getObjectId();
			return;
		}		
		if (parentAttName.equals("patientId")) {
			this.patientId = newParent.getObjectId();
			return;
		}		
		if (parentAttName.equals("encounterType")) {
			this.encounterType = newParent.getObjectId();
			return;
		}		
		if (parentAttName.equals("visitId")) {
			this.visitId = newParent.getObjectId();
			return;
		}		
		if (parentAttName.equals("voidedBy")) {
			this.voidedBy = newParent.getObjectId();
			return;
		}

		throw new RuntimeException("No found parent for: " + parentAttName);
	}


}