package org.openmrs.module.eptssync.model.openmrs.sourcepkg; 
 
import org.openmrs.module.eptssync.model.openmrs.generic.*; 
 
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.eptssync.utilities.db.conn.DBException; 
import org.openmrs.module.eptssync.exceptions.ParentNotYetMigratedException; 
 
import java.sql.Connection; 
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
	private java.util.Date dateCreated;
	private byte voided;
	private int voidedBy;
	private java.util.Date dateVoided;
	private String voidReason;
	private String uuid;
	private int changedBy;
	private java.util.Date dateChanged;
	private int visitId;
	private int consistent;
	private java.util.Date lastSyncDate;
	private int originRecordId;
	private String originAppLocationCode;
 
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
 
	public void setDateCreated(java.util.Date dateCreated){ 
	 	this.dateCreated = dateCreated;
	}
 
	public java.util.Date getDateCreated(){ 
		return this.dateCreated;
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
 
	public void setUuid(String uuid){ 
	 	this.uuid = uuid;
	}
 
	public String getUuid(){ 
		return this.uuid;
	}
 
	public void setChangedBy(int changedBy){ 
	 	this.changedBy = changedBy;
	}
 
	public int getChangedBy(){ 
		return this.changedBy;
	}
 
	public void setDateChanged(java.util.Date dateChanged){ 
	 	this.dateChanged = dateChanged;
	}
 
	public java.util.Date getDateChanged(){ 
		return this.dateChanged;
	}
 
	public void setVisitId(int visitId){ 
	 	this.visitId = visitId;
	}
 
	public int getVisitId(){ 
		return this.visitId;
	}
 
	public void setConsistent(int consistent){ 
	 	this.consistent = consistent;
	}
 
	public int getConsistent(){ 
		return this.consistent;
	}
 
	public void setLastSyncDate(java.util.Date lastSyncDate){ 
	 	this.lastSyncDate = lastSyncDate;
	}
 
	public java.util.Date getLastSyncDate(){ 
		return this.lastSyncDate;
	}
 
	public void setOriginRecordId(int originRecordId){ 
	 	this.originRecordId = originRecordId;
	}
 
	public int getOriginRecordId(){ 
		return this.originRecordId;
	}
 
	public void setOriginAppLocationCode(String originAppLocationCode){ 
	 	this.originAppLocationCode = originAppLocationCode;
	}


 
	public String getOriginAppLocationCode(){ 
		return this.originAppLocationCode;
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
		this.voidReason = rs.getString("void_reason") != null ? rs.getString("void_reason").trim() : null;
		this.uuid = rs.getString("uuid") != null ? rs.getString("uuid").trim() : null;
		this.changedBy = rs.getInt("changed_by");
		this.dateChanged =  rs.getTimestamp("date_changed") != null ? new java.util.Date( rs.getTimestamp("date_changed").getTime() ) : null;
		this.visitId = rs.getInt("visit_id");
		this.consistent = rs.getInt("consistent");
		this.lastSyncDate =  rs.getTimestamp("last_sync_date") != null ? new java.util.Date( rs.getTimestamp("last_sync_date").getTime() ) : null;
		this.originRecordId = rs.getInt("origin_record_id");
			} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "encounter_id"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParams(){ 
 		Object[] params = {this.encounterType == 0 ? null : this.encounterType, this.patientId == 0 ? null : this.patientId, this.locationId == 0 ? null : this.locationId, this.formId == 0 ? null : this.formId, this.encounterDatetime, this.creator == 0 ? null : this.creator, this.dateCreated, this.voided, this.voidedBy == 0 ? null : this.voidedBy, this.dateVoided, this.voidReason, this.uuid, this.changedBy == 0 ? null : this.changedBy, this.dateChanged, this.visitId == 0 ? null : this.visitId, this.consistent, this.lastSyncDate, this.originRecordId, this.originAppLocationCode};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.encounterType == 0 ? null : this.encounterType, this.patientId == 0 ? null : this.patientId, this.locationId == 0 ? null : this.locationId, this.formId == 0 ? null : this.formId, this.encounterDatetime, this.creator == 0 ? null : this.creator, this.dateCreated, this.voided, this.voidedBy == 0 ? null : this.voidedBy, this.dateVoided, this.voidReason, this.uuid, this.changedBy == 0 ? null : this.changedBy, this.dateChanged, this.visitId == 0 ? null : this.visitId, this.consistent, this.lastSyncDate, this.originRecordId, this.originAppLocationCode, this.encounterId};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQL(){ 
 		return "INSERT INTO encounter(encounter_type, patient_id, location_id, form_id, encounter_datetime, creator, date_created, voided, voided_by, date_voided, void_reason, uuid, changed_by, date_changed, visit_id, consistent, last_sync_date, origin_record_id, origin_app_location_code) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE encounter SET encounter_type = ?, patient_id = ?, location_id = ?, form_id = ?, encounter_datetime = ?, creator = ?, date_created = ?, voided = ?, voided_by = ?, date_voided = ?, void_reason = ?, uuid = ?, changed_by = ?, date_changed = ?, visit_id = ?, consistent = ?, last_sync_date = ?, origin_record_id = ?, origin_app_location_code = ? WHERE encounter_id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.encounterType == 0 ? null : this.encounterType) + "," + (this.patientId == 0 ? null : this.patientId) + "," + (this.locationId == 0 ? null : this.locationId) + "," + (this.formId == 0 ? null : this.formId) + "," + (this.encounterDatetime != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(encounterDatetime)  +"\"" : null) + "," + (this.creator == 0 ? null : this.creator) + "," + (this.dateCreated != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateCreated)  +"\"" : null) + "," + (this.voided) + "," + (this.voidedBy == 0 ? null : this.voidedBy) + "," + (this.dateVoided != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateVoided)  +"\"" : null) + "," + (this.voidReason != null ? "\""+voidReason+"\"" : null) + "," + (this.uuid != null ? "\""+uuid+"\"" : null) + "," + (this.changedBy == 0 ? null : this.changedBy) + "," + (this.dateChanged != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateChanged)  +"\"" : null) + "," + (this.visitId == 0 ? null : this.visitId) + "," + (this.consistent) + "," + (this.lastSyncDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(lastSyncDate)  +"\"" : null) + "," + (this.originRecordId) + "," + (this.originAppLocationCode != null ? "\""+originAppLocationCode+"\"" : null); 
	} 
 
	@Override
	public boolean isGeneratedFromSkeletonClass() {
		return false;
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
	public int retrieveSharedPKKey(Connection conn) throws ParentNotYetMigratedException, DBException {
		throw new RuntimeException("No PKSharedInfo defined!");	}

	@Override
	public void loadDestParentInfo(Connection conn) throws ParentNotYetMigratedException, DBException {
		OpenMRSObject parentOnDestination = null;
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.sourcepkg.UsersVO.class, this.changedBy, true, conn); 
		this.changedBy = 0;
		if (parentOnDestination  != null) this.changedBy = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.sourcepkg.UsersVO.class, this.creator, false, conn); 
		this.creator = 0;
		if (parentOnDestination  != null) this.creator = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.sourcepkg.LocationVO.class, this.locationId, true, conn); 
		this.locationId = 0;
		if (parentOnDestination  != null) this.locationId = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.sourcepkg.PatientVO.class, this.patientId, false, conn); 
		this.patientId = 0;
		if (parentOnDestination  != null) this.patientId = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.sourcepkg.VisitVO.class, this.visitId, true, conn); 
		this.visitId = 0;
		if (parentOnDestination  != null) this.visitId = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.sourcepkg.UsersVO.class, this.voidedBy, true, conn); 
		this.voidedBy = 0;
		if (parentOnDestination  != null) this.voidedBy = parentOnDestination.getObjectId();
 
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


}