package org.openmrs.module.eptssync.model.openmrs; 
 
import org.openmrs.module.eptssync.model.GenericSyncRecordDAO; 
 
import org.openmrs.module.eptssync.model.base.BaseVO; 
 
import org.openmrs.module.eptssync.utilities.db.conn.DBException; 
import org.openmrs.module.eptssync.utilities.db.conn.OpenConnection; 
import org.openmrs.module.eptssync.exceptions.ParentNotYetMigratedException; 
 
import java.sql.Connection; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class EncounterProviderVO extends AbstractOpenMRSObject implements OpenMRSObject { 
	private int encounterProviderId;
	private int encounterId;
	private int providerId;
	private int encounterRoleId;
	private int creator;
	private java.util.Date dateCreated;
	private int changedBy;
	private java.util.Date dateChanged;
	private byte voided;
	private java.util.Date dateVoided;
	private int voidedBy;
	private String voidReason;
	private String uuid;
	private java.util.Date lastSyncDate;
	private int originRecordId;
	private String originAppLocationCode;
 
	public EncounterProviderVO() { 
	} 
 
	public void setEncounterProviderId(int encounterProviderId){ 
	 	this.encounterProviderId = encounterProviderId;
	}
 
	public int getEncounterProviderId(){ 
		return this.encounterProviderId;
	}	public void setEncounterId(int encounterId){ 
	 	this.encounterId = encounterId;
	}
 
	public int getEncounterId(){ 
		return this.encounterId;
	}	public void setProviderId(int providerId){ 
	 	this.providerId = providerId;
	}
 
	public int getProviderId(){ 
		return this.providerId;
	}	public void setEncounterRoleId(int encounterRoleId){ 
	 	this.encounterRoleId = encounterRoleId;
	}
 
	public int getEncounterRoleId(){ 
		return this.encounterRoleId;
	}	public void setCreator(int creator){ 
	 	this.creator = creator;
	}
 
	public int getCreator(){ 
		return this.creator;
	}	public void setDateCreated(java.util.Date dateCreated){ 
	 	this.dateCreated = dateCreated;
	}
 
	public java.util.Date getDateCreated(){ 
		return this.dateCreated;
	}	public void setChangedBy(int changedBy){ 
	 	this.changedBy = changedBy;
	}
 
	public int getChangedBy(){ 
		return this.changedBy;
	}	public void setDateChanged(java.util.Date dateChanged){ 
	 	this.dateChanged = dateChanged;
	}
 
	public java.util.Date getDateChanged(){ 
		return this.dateChanged;
	}	public void setVoided(byte voided){ 
	 	this.voided = voided;
	}
 
	public byte getVoided(){ 
		return this.voided;
	}	public void setDateVoided(java.util.Date dateVoided){ 
	 	this.dateVoided = dateVoided;
	}
 
	public java.util.Date getDateVoided(){ 
		return this.dateVoided;
	}	public void setVoidedBy(int voidedBy){ 
	 	this.voidedBy = voidedBy;
	}
 
	public int getVoidedBy(){ 
		return this.voidedBy;
	}	public void setVoidReason(String voidReason){ 
	 	this.voidReason = voidReason;
	}
 
	public String getVoidReason(){ 
		return this.voidReason;
	}	public void setUuid(String uuid){ 
	 	this.uuid = uuid;
	}
 
	public String getUuid(){ 
		return this.uuid;
	}	public void setLastSyncDate(java.util.Date lastSyncDate){ 
	 	this.lastSyncDate = lastSyncDate;
	}
 
	public java.util.Date getLastSyncDate(){ 
		return this.lastSyncDate;
	}	public void setOriginRecordId(int originRecordId){ 
	 	this.originRecordId = originRecordId;
	}
 
	public int getOriginRecordId(){ 
		return this.originRecordId;
	}	public void setOriginAppLocationCode(String originAppLocationCode){ 
	 	this.originAppLocationCode = originAppLocationCode;
	}


 
	public String getOriginAppLocationCode(){ 
		return this.originAppLocationCode;
	}
 
	public int getObjectId() { 
 		return this.encounterProviderId; 
	} 
 
	public void setObjectId(int selfId){ 
		this.encounterProviderId = selfId; 
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
 		return "encounter_provider_id"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParams(){ 
 		Object[] params = {this.encounterId == 0 ? null : this.encounterId, this.providerId == 0 ? null : this.providerId, this.encounterRoleId == 0 ? null : this.encounterRoleId, this.creator == 0 ? null : this.creator, this.dateCreated, this.changedBy == 0 ? null : this.changedBy, this.dateChanged, this.voided, this.dateVoided, this.voidedBy == 0 ? null : this.voidedBy, this.voidReason, this.uuid, this.lastSyncDate, this.originRecordId, this.originAppLocationCode};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.encounterId == 0 ? null : this.encounterId, this.providerId == 0 ? null : this.providerId, this.encounterRoleId == 0 ? null : this.encounterRoleId, this.creator == 0 ? null : this.creator, this.dateCreated, this.changedBy == 0 ? null : this.changedBy, this.dateChanged, this.voided, this.dateVoided, this.voidedBy == 0 ? null : this.voidedBy, this.voidReason, this.uuid, this.lastSyncDate, this.originRecordId, this.originAppLocationCode, this.encounterProviderId};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQL(){ 
 		return "INSERT INTO encounter_provider(encounter_id, provider_id, encounter_role_id, creator, date_created, changed_by, date_changed, voided, date_voided, voided_by, void_reason, uuid, last_sync_date, origin_record_id, origin_app_location_code) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE encounter_provider SET encounter_id = ?, provider_id = ?, encounter_role_id = ?, creator = ?, date_created = ?, changed_by = ?, date_changed = ?, voided = ?, date_voided = ?, voided_by = ?, void_reason = ?, uuid = ?, last_sync_date = ?, origin_record_id = ?, origin_app_location_code = ? WHERE encounter_provider_id = ?;"; 
	} 
 
	@JsonIgnore
	public int getMainParentId(){ 
 		return encounterId; 
	} 
 
	public void setMainParentId(int mainParentId){ 
 		this.encounterId = mainParentId; 
	} 
 
	@JsonIgnore
	public String getMainParentTable(){ 
 		return "encounter";
	} 
 
	public void loadDestParentInfo(Connection conn) throws ParentNotYetMigratedException, DBException {
		OpenMRSObject parentOnDestination = null;
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.EncounterVO.class, this.encounterId,true, conn); 
		if (parentOnDestination  != null) this.encounterId = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.UsersVO.class, this.creator,false, conn); 
		if (parentOnDestination  != null) this.creator = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.UsersVO.class, this.changedBy,true, conn); 
		if (parentOnDestination  != null) this.changedBy = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.UsersVO.class, this.voidedBy,true, conn); 
		if (parentOnDestination  != null) this.voidedBy = parentOnDestination.getObjectId();
 
	}
}