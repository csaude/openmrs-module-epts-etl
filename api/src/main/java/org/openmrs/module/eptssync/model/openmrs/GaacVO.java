package org.openmrs.module.eptssync.model.openmrs; 
 
import org.openmrs.module.eptssync.model.GenericSyncRecordDAO; 
 
import org.openmrs.module.eptssync.utilities.db.conn.DBException; 
import org.openmrs.module.eptssync.utilities.db.conn.OpenConnection; 
import org.openmrs.module.eptssync.exceptions.ParentNotYetMigratedException; 
 
import java.sql.Connection; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class GaacVO extends AbstractOpenMRSObject implements OpenMRSObject { 
	private int gaacId;
	private String name;
	private String description;
	private String gaacIdentifier;
	private java.util.Date startDate;
	private java.util.Date endDate;
	private int focalPatientId;
	private int affinityType;
	private int locationId;
	private short crumbled;
	private String reasonCrumbled;
	private java.util.Date dateCrumbled;
	private int creator;
	private java.util.Date dateCreated;
	private int changedBy;
	private java.util.Date dateChanged;
	private short voided;
	private int voidedBy;
	private java.util.Date dateVoided;
	private String voidReason;
	private String uuid;
	private java.util.Date lastSyncDate;
	private int originRecordId;
	private String originAppLocationCode;
 
	public GaacVO() { 
		this.metadata = false;
	} 
 
	public void setGaacId(int gaacId){ 
	 	this.gaacId = gaacId;
	}
 
	public int getGaacId(){ 
		return this.gaacId;
	}
 
	public void setName(String name){ 
	 	this.name = name;
	}
 
	public String getName(){ 
		return this.name;
	}
 
	public void setDescription(String description){ 
	 	this.description = description;
	}
 
	public String getDescription(){ 
		return this.description;
	}
 
	public void setGaacIdentifier(String gaacIdentifier){ 
	 	this.gaacIdentifier = gaacIdentifier;
	}
 
	public String getGaacIdentifier(){ 
		return this.gaacIdentifier;
	}
 
	public void setStartDate(java.util.Date startDate){ 
	 	this.startDate = startDate;
	}
 
	public java.util.Date getStartDate(){ 
		return this.startDate;
	}
 
	public void setEndDate(java.util.Date endDate){ 
	 	this.endDate = endDate;
	}
 
	public java.util.Date getEndDate(){ 
		return this.endDate;
	}
 
	public void setFocalPatientId(int focalPatientId){ 
	 	this.focalPatientId = focalPatientId;
	}
 
	public int getFocalPatientId(){ 
		return this.focalPatientId;
	}
 
	public void setAffinityType(int affinityType){ 
	 	this.affinityType = affinityType;
	}
 
	public int getAffinityType(){ 
		return this.affinityType;
	}
 
	public void setLocationId(int locationId){ 
	 	this.locationId = locationId;
	}
 
	public int getLocationId(){ 
		return this.locationId;
	}
 
	public void setCrumbled(short crumbled){ 
	 	this.crumbled = crumbled;
	}
 
	public short getCrumbled(){ 
		return this.crumbled;
	}
 
	public void setReasonCrumbled(String reasonCrumbled){ 
	 	this.reasonCrumbled = reasonCrumbled;
	}
 
	public String getReasonCrumbled(){ 
		return this.reasonCrumbled;
	}
 
	public void setDateCrumbled(java.util.Date dateCrumbled){ 
	 	this.dateCrumbled = dateCrumbled;
	}
 
	public java.util.Date getDateCrumbled(){ 
		return this.dateCrumbled;
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
 
	public void setVoided(short voided){ 
	 	this.voided = voided;
	}
 
	public short getVoided(){ 
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
 
	public int getObjectId() { 
 		return this.gaacId; 
	} 
 
	public void setObjectId(int selfId){ 
		this.gaacId = selfId; 
	} 
 
	public void refreshLastSyncDate(OpenConnection conn){ 
		try{
			GenericSyncRecordDAO.refreshLastSyncDate(this, conn); 
		}catch(DBException e) {
			throw new RuntimeException(e);
		}
	}

	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "gaac_id"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParams(){ 
 		Object[] params = {this.name, this.description, this.gaacIdentifier, this.startDate, this.endDate, this.focalPatientId == 0 ? null : this.focalPatientId, this.affinityType == 0 ? null : this.affinityType, this.locationId == 0 ? null : this.locationId, this.crumbled, this.reasonCrumbled, this.dateCrumbled, this.creator == 0 ? null : this.creator, this.dateCreated, this.changedBy == 0 ? null : this.changedBy, this.dateChanged, this.voided, this.voidedBy == 0 ? null : this.voidedBy, this.dateVoided, this.voidReason, this.uuid, this.lastSyncDate, this.originRecordId, this.originAppLocationCode};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.name, this.description, this.gaacIdentifier, this.startDate, this.endDate, this.focalPatientId == 0 ? null : this.focalPatientId, this.affinityType == 0 ? null : this.affinityType, this.locationId == 0 ? null : this.locationId, this.crumbled, this.reasonCrumbled, this.dateCrumbled, this.creator == 0 ? null : this.creator, this.dateCreated, this.changedBy == 0 ? null : this.changedBy, this.dateChanged, this.voided, this.voidedBy == 0 ? null : this.voidedBy, this.dateVoided, this.voidReason, this.uuid, this.lastSyncDate, this.originRecordId, this.originAppLocationCode, this.gaacId};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQL(){ 
 		return "INSERT INTO gaac(name, description, gaac_identifier, start_date, end_date, focal_patient_id, affinity_type, location_id, crumbled, reason_crumbled, date_crumbled, creator, date_created, changed_by, date_changed, voided, voided_by, date_voided, void_reason, uuid, last_sync_date, origin_record_id, origin_app_location_code) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE gaac SET name = ?, description = ?, gaac_identifier = ?, start_date = ?, end_date = ?, focal_patient_id = ?, affinity_type = ?, location_id = ?, crumbled = ?, reason_crumbled = ?, date_crumbled = ?, creator = ?, date_created = ?, changed_by = ?, date_changed = ?, voided = ?, voided_by = ?, date_voided = ?, void_reason = ?, uuid = ?, last_sync_date = ?, origin_record_id = ?, origin_app_location_code = ? WHERE gaac_id = ?;"; 
	} 
 
	public void loadDestParentInfo(Connection conn) throws ParentNotYetMigratedException, DBException {
		OpenMRSObject parentOnDestination = null;
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.PatientVO.class, this.focalPatientId, true, conn); 
		this.focalPatientId = 0;
		if (parentOnDestination  != null) this.focalPatientId = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.LocationVO.class, this.locationId, true, conn); 
		this.locationId = 0;
		if (parentOnDestination  != null) this.locationId = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.UsersVO.class, this.changedBy, true, conn); 
		this.changedBy = 0;
		if (parentOnDestination  != null) this.changedBy = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.UsersVO.class, this.creator, false, conn); 
		this.creator = 0;
		if (parentOnDestination  != null) this.creator = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.UsersVO.class, this.voidedBy, true, conn); 
		this.voidedBy = 0;
		if (parentOnDestination  != null) this.voidedBy = parentOnDestination.getObjectId();
 
	}
}