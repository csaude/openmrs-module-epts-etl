package org.openmrs.module.eptssync.model.openmrs.sourcepkg; 
 
import org.openmrs.module.eptssync.model.openmrs.generic.*; 
 
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.eptssync.utilities.db.conn.DBException; 
import org.openmrs.module.eptssync.exceptions.ParentNotYetMigratedException; 
 
import java.sql.Connection; 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
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
	private int consistent;
	private java.util.Date lastSyncDate;
	private int originRecordId;
	private String originAppLocationCode;
 
	public EncounterProviderVO() { 
		this.metadata = false;
	} 
 
	public void setEncounterProviderId(int encounterProviderId){ 
	 	this.encounterProviderId = encounterProviderId;
	}
 
	public int getEncounterProviderId(){ 
		return this.encounterProviderId;
	}
 
	public void setEncounterId(int encounterId){ 
	 	this.encounterId = encounterId;
	}
 
	public int getEncounterId(){ 
		return this.encounterId;
	}
 
	public void setProviderId(int providerId){ 
	 	this.providerId = providerId;
	}
 
	public int getProviderId(){ 
		return this.providerId;
	}
 
	public void setEncounterRoleId(int encounterRoleId){ 
	 	this.encounterRoleId = encounterRoleId;
	}
 
	public int getEncounterRoleId(){ 
		return this.encounterRoleId;
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
 
	public void setDateVoided(java.util.Date dateVoided){ 
	 	this.dateVoided = dateVoided;
	}
 
	public java.util.Date getDateVoided(){ 
		return this.dateVoided;
	}
 
	public void setVoidedBy(int voidedBy){ 
	 	this.voidedBy = voidedBy;
	}
 
	public int getVoidedBy(){ 
		return this.voidedBy;
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
 		return this.encounterProviderId; 
	} 
 
	public void setObjectId(int selfId){ 
		this.encounterProviderId = selfId; 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		this.encounterProviderId = rs.getInt("encounter_provider_id");
		this.encounterId = rs.getInt("encounter_id");
		this.providerId = rs.getInt("provider_id");
		this.encounterRoleId = rs.getInt("encounter_role_id");
		this.creator = rs.getInt("creator");
		this.dateCreated =  rs.getTimestamp("date_created") != null ? new java.util.Date( rs.getTimestamp("date_created").getTime() ) : null;
		this.changedBy = rs.getInt("changed_by");
		this.dateChanged =  rs.getTimestamp("date_changed") != null ? new java.util.Date( rs.getTimestamp("date_changed").getTime() ) : null;
		this.voided = rs.getByte("voided");
		this.dateVoided =  rs.getTimestamp("date_voided") != null ? new java.util.Date( rs.getTimestamp("date_voided").getTime() ) : null;
		this.voidedBy = rs.getInt("voided_by");
		this.voidReason = rs.getString("void_reason") != null ? rs.getString("void_reason").trim() : null;
		this.uuid = rs.getString("uuid") != null ? rs.getString("uuid").trim() : null;
		this.consistent = rs.getInt("consistent");
		this.lastSyncDate =  rs.getTimestamp("last_sync_date") != null ? new java.util.Date( rs.getTimestamp("last_sync_date").getTime() ) : null;
		this.originRecordId = rs.getInt("origin_record_id");
			} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "encounter_provider_id"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParams(){ 
 		Object[] params = {this.encounterId == 0 ? null : this.encounterId, this.providerId == 0 ? null : this.providerId, this.encounterRoleId == 0 ? null : this.encounterRoleId, this.creator == 0 ? null : this.creator, this.dateCreated, this.changedBy == 0 ? null : this.changedBy, this.dateChanged, this.voided, this.dateVoided, this.voidedBy == 0 ? null : this.voidedBy, this.voidReason, this.uuid, this.consistent, this.lastSyncDate, this.originRecordId, this.originAppLocationCode};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.encounterId == 0 ? null : this.encounterId, this.providerId == 0 ? null : this.providerId, this.encounterRoleId == 0 ? null : this.encounterRoleId, this.creator == 0 ? null : this.creator, this.dateCreated, this.changedBy == 0 ? null : this.changedBy, this.dateChanged, this.voided, this.dateVoided, this.voidedBy == 0 ? null : this.voidedBy, this.voidReason, this.uuid, this.consistent, this.lastSyncDate, this.originRecordId, this.originAppLocationCode, this.encounterProviderId};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQL(){ 
 		return "INSERT INTO encounter_provider(encounter_id, provider_id, encounter_role_id, creator, date_created, changed_by, date_changed, voided, date_voided, voided_by, void_reason, uuid, consistent, last_sync_date, origin_record_id, origin_app_location_code) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE encounter_provider SET encounter_id = ?, provider_id = ?, encounter_role_id = ?, creator = ?, date_created = ?, changed_by = ?, date_changed = ?, voided = ?, date_voided = ?, voided_by = ?, void_reason = ?, uuid = ?, consistent = ?, last_sync_date = ?, origin_record_id = ?, origin_app_location_code = ? WHERE encounter_provider_id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.encounterId == 0 ? null : this.encounterId) + "," + (this.providerId == 0 ? null : this.providerId) + "," + (this.encounterRoleId == 0 ? null : this.encounterRoleId) + "," + (this.creator == 0 ? null : this.creator) + "," + (this.dateCreated != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateCreated)  +"\"" : null) + "," + (this.changedBy == 0 ? null : this.changedBy) + "," + (this.dateChanged != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateChanged)  +"\"" : null) + "," + (this.voided) + "," + (this.dateVoided != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateVoided)  +"\"" : null) + "," + (this.voidedBy == 0 ? null : this.voidedBy) + "," + (this.voidReason != null ? "\""+voidReason+"\"" : null) + "," + (this.uuid != null ? "\""+uuid+"\"" : null) + "," + (this.consistent) + "," + (this.lastSyncDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(lastSyncDate)  +"\"" : null) + "," + (this.originRecordId) + "," + (this.originAppLocationCode != null ? "\""+originAppLocationCode+"\"" : null); 
	} 
 
	@Override
	public boolean isGeneratedFromSkeletonClass() {
		return false;
	}

	@Override
	public boolean hasParents() {
		if (this.encounterId != 0) return true;
		if (this.changedBy != 0) return true;
		if (this.creator != 0) return true;
		if (this.voidedBy != 0) return true;
		if (this.encounterRoleId != 0) return true;
		if (this.providerId != 0) return true;
		return false;
	}

	@Override
	public int retrieveSharedPKKey(Connection conn) throws ParentNotYetMigratedException, DBException {
		throw new RuntimeException("No PKSharedInfo defined!");	}

	@Override
	public void loadDestParentInfo(Connection conn) throws ParentNotYetMigratedException, DBException {
		OpenMRSObject parentOnDestination = null;
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.sourcepkg.EncounterVO.class, this.encounterId, false, conn); 
		this.encounterId = 0;
		if (parentOnDestination  != null) this.encounterId = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.sourcepkg.UsersVO.class, this.changedBy, true, conn); 
		this.changedBy = 0;
		if (parentOnDestination  != null) this.changedBy = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.sourcepkg.UsersVO.class, this.creator, false, conn); 
		this.creator = 0;
		if (parentOnDestination  != null) this.creator = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.sourcepkg.UsersVO.class, this.voidedBy, true, conn); 
		this.voidedBy = 0;
		if (parentOnDestination  != null) this.voidedBy = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.sourcepkg.ProviderVO.class, this.providerId, false, conn); 
		this.providerId = 0;
		if (parentOnDestination  != null) this.providerId = parentOnDestination.getObjectId();
 
	}

	@Override
	public int getParentValue(String parentAttName) {		
		if (parentAttName.equals("encounterId")) return this.encounterId;		
		if (parentAttName.equals("changedBy")) return this.changedBy;		
		if (parentAttName.equals("creator")) return this.creator;		
		if (parentAttName.equals("voidedBy")) return this.voidedBy;		
		if (parentAttName.equals("encounterRoleId")) return this.encounterRoleId;		
		if (parentAttName.equals("providerId")) return this.providerId;

		throw new RuntimeException("No found parent for: " + parentAttName);	}


}