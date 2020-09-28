package org.openmrs.module.eptssync.model.openmrs.sourcepkg; 
 
import org.openmrs.module.eptssync.model.openmrs.generic.*; 
 
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.eptssync.utilities.db.conn.DBException; 
import org.openmrs.module.eptssync.exceptions.ParentNotYetMigratedException; 
 
import java.sql.Connection; 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class VisitVO extends AbstractOpenMRSObject implements OpenMRSObject { 
	private int visitId;
	private int patientId;
	private int visitTypeId;
	private java.util.Date dateStarted;
	private java.util.Date dateStopped;
	private int indicationConceptId;
	private int locationId;
	private int creator;
	private java.util.Date dateCreated;
	private int changedBy;
	private java.util.Date dateChanged;
	private byte voided;
	private int voidedBy;
	private java.util.Date dateVoided;
	private String voidReason;
	private String uuid;
	private java.util.Date lastSyncDate;
	private int originRecordId;
	private String originAppLocationCode;
	private int consistent;
 
	public VisitVO() { 
		this.metadata = false;
	} 
 
	public void setVisitId(int visitId){ 
	 	this.visitId = visitId;
	}
 
	public int getVisitId(){ 
		return this.visitId;
	}
 
	public void setPatientId(int patientId){ 
	 	this.patientId = patientId;
	}
 
	public int getPatientId(){ 
		return this.patientId;
	}
 
	public void setVisitTypeId(int visitTypeId){ 
	 	this.visitTypeId = visitTypeId;
	}
 
	public int getVisitTypeId(){ 
		return this.visitTypeId;
	}
 
	public void setDateStarted(java.util.Date dateStarted){ 
	 	this.dateStarted = dateStarted;
	}
 
	public java.util.Date getDateStarted(){ 
		return this.dateStarted;
	}
 
	public void setDateStopped(java.util.Date dateStopped){ 
	 	this.dateStopped = dateStopped;
	}
 
	public java.util.Date getDateStopped(){ 
		return this.dateStopped;
	}
 
	public void setIndicationConceptId(int indicationConceptId){ 
	 	this.indicationConceptId = indicationConceptId;
	}
 
	public int getIndicationConceptId(){ 
		return this.indicationConceptId;
	}
 
	public void setLocationId(int locationId){ 
	 	this.locationId = locationId;
	}
 
	public int getLocationId(){ 
		return this.locationId;
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
 
	public void setConsistent(int consistent){ 
	 	this.consistent = consistent;
	}


 
	public int getConsistent(){ 
		return this.consistent;
	}
 
	public int getObjectId() { 
 		return this.visitId; 
	} 
 
	public void setObjectId(int selfId){ 
		this.visitId = selfId; 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		this.visitId = rs.getInt("visit_id");
		this.patientId = rs.getInt("patient_id");
		this.visitTypeId = rs.getInt("visit_type_id");
		this.dateStarted =  rs.getTimestamp("date_started") != null ? new java.util.Date( rs.getTimestamp("date_started").getTime() ) : null;
		this.dateStopped =  rs.getTimestamp("date_stopped") != null ? new java.util.Date( rs.getTimestamp("date_stopped").getTime() ) : null;
		this.indicationConceptId = rs.getInt("indication_concept_id");
		this.locationId = rs.getInt("location_id");
		this.creator = rs.getInt("creator");
		this.dateCreated =  rs.getTimestamp("date_created") != null ? new java.util.Date( rs.getTimestamp("date_created").getTime() ) : null;
		this.changedBy = rs.getInt("changed_by");
		this.dateChanged =  rs.getTimestamp("date_changed") != null ? new java.util.Date( rs.getTimestamp("date_changed").getTime() ) : null;
		this.voided = rs.getByte("voided");
		this.voidedBy = rs.getInt("voided_by");
		this.dateVoided =  rs.getTimestamp("date_voided") != null ? new java.util.Date( rs.getTimestamp("date_voided").getTime() ) : null;
		this.voidReason = rs.getString("void_reason") != null ? rs.getString("void_reason").trim() : null;
		this.uuid = rs.getString("uuid") != null ? rs.getString("uuid").trim() : null;
		this.lastSyncDate =  rs.getTimestamp("last_sync_date") != null ? new java.util.Date( rs.getTimestamp("last_sync_date").getTime() ) : null;
		this.originRecordId = rs.getInt("origin_record_id");
		this.originAppLocationCode = rs.getString("origin_app_location_code") != null ? rs.getString("origin_app_location_code").trim() : null;
			} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "visit_id"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParams(){ 
 		Object[] params = {this.patientId == 0 ? null : this.patientId, this.visitTypeId == 0 ? null : this.visitTypeId, this.dateStarted, this.dateStopped, this.indicationConceptId == 0 ? null : this.indicationConceptId, this.locationId == 0 ? null : this.locationId, this.creator == 0 ? null : this.creator, this.dateCreated, this.changedBy == 0 ? null : this.changedBy, this.dateChanged, this.voided, this.voidedBy == 0 ? null : this.voidedBy, this.dateVoided, this.voidReason, this.uuid, this.lastSyncDate, this.originRecordId, this.originAppLocationCode, this.consistent};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.patientId == 0 ? null : this.patientId, this.visitTypeId == 0 ? null : this.visitTypeId, this.dateStarted, this.dateStopped, this.indicationConceptId == 0 ? null : this.indicationConceptId, this.locationId == 0 ? null : this.locationId, this.creator == 0 ? null : this.creator, this.dateCreated, this.changedBy == 0 ? null : this.changedBy, this.dateChanged, this.voided, this.voidedBy == 0 ? null : this.voidedBy, this.dateVoided, this.voidReason, this.uuid, this.lastSyncDate, this.originRecordId, this.originAppLocationCode, this.consistent, this.visitId};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQL(){ 
 		return "INSERT INTO visit(patient_id, visit_type_id, date_started, date_stopped, indication_concept_id, location_id, creator, date_created, changed_by, date_changed, voided, voided_by, date_voided, void_reason, uuid, last_sync_date, origin_record_id, origin_app_location_code, consistent) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE visit SET patient_id = ?, visit_type_id = ?, date_started = ?, date_stopped = ?, indication_concept_id = ?, location_id = ?, creator = ?, date_created = ?, changed_by = ?, date_changed = ?, voided = ?, voided_by = ?, date_voided = ?, void_reason = ?, uuid = ?, last_sync_date = ?, origin_record_id = ?, origin_app_location_code = ?, consistent = ? WHERE visit_id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.patientId == 0 ? null : this.patientId) + "," + (this.visitTypeId == 0 ? null : this.visitTypeId) + "," + (this.dateStarted != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateStarted)  +"\"" : null) + "," + (this.dateStopped != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateStopped)  +"\"" : null) + "," + (this.indicationConceptId == 0 ? null : this.indicationConceptId) + "," + (this.locationId == 0 ? null : this.locationId) + "," + (this.creator == 0 ? null : this.creator) + "," + (this.dateCreated != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateCreated)  +"\"" : null) + "," + (this.changedBy == 0 ? null : this.changedBy) + "," + (this.dateChanged != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateChanged)  +"\"" : null) + "," + (this.voided) + "," + (this.voidedBy == 0 ? null : this.voidedBy) + "," + (this.dateVoided != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateVoided)  +"\"" : null) + "," + (this.voidReason != null ? "\""+voidReason+"\"" : null) + "," + (this.uuid != null ? "\""+uuid+"\"" : null) + "," + (this.lastSyncDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(lastSyncDate)  +"\"" : null) + "," + (this.originRecordId) + "," + (this.originAppLocationCode != null ? "\""+originAppLocationCode+"\"" : null) + "," + (this.consistent); 
	} 
 
	@Override
	public boolean isGeneratedFromSkeletonClass() {
		return false;
	}

	@Override
	public boolean hasParents() {
		if (this.changedBy != 0) return true;
		if (this.creator != 0) return true;
		if (this.indicationConceptId != 0) return true;
		if (this.locationId != 0) return true;
		if (this.patientId != 0) return true;
		if (this.visitTypeId != 0) return true;
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
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.sourcepkg.UsersVO.class, this.voidedBy, true, conn); 
		this.voidedBy = 0;
		if (parentOnDestination  != null) this.voidedBy = parentOnDestination.getObjectId();
 
	}

	@Override
	public int getParentValue(String parentAttName) {		
		if (parentAttName.equals("changedBy")) return this.changedBy;		
		if (parentAttName.equals("creator")) return this.creator;		
		if (parentAttName.equals("indicationConceptId")) return this.indicationConceptId;		
		if (parentAttName.equals("locationId")) return this.locationId;		
		if (parentAttName.equals("patientId")) return this.patientId;		
		if (parentAttName.equals("visitTypeId")) return this.visitTypeId;		
		if (parentAttName.equals("voidedBy")) return this.voidedBy;

		throw new RuntimeException("No found parent for: " + parentAttName);	}


}