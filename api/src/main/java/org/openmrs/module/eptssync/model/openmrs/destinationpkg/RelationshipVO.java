package org.openmrs.module.eptssync.model.openmrs.destinationpkg; 
 
import org.openmrs.module.eptssync.model.openmrs.generic.*; 
 
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.eptssync.utilities.db.conn.DBException; 
import org.openmrs.module.eptssync.exceptions.ParentNotYetMigratedException; 
 
import java.sql.Connection; 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class RelationshipVO extends AbstractOpenMRSObject implements OpenMRSObject { 
	private int relationshipId;
	private int personA;
	private int relationship;
	private int personB;
	private int creator;
	private java.util.Date dateCreated;
	private byte voided;
	private int voidedBy;
	private java.util.Date dateVoided;
	private String voidReason;
	private String uuid;
	private java.util.Date dateChanged;
	private int changedBy;
	private java.util.Date startDate;
	private java.util.Date endDate;
	private java.util.Date lastSyncDate;
	private int originRecordId;
	private String originAppLocationCode;
	private int consistent;
 
	public RelationshipVO() { 
		this.metadata = false;
	} 
 
	public void setRelationshipId(int relationshipId){ 
	 	this.relationshipId = relationshipId;
	}
 
	public int getRelationshipId(){ 
		return this.relationshipId;
	}
 
	public void setPersonA(int personA){ 
	 	this.personA = personA;
	}
 
	public int getPersonA(){ 
		return this.personA;
	}
 
	public void setRelationship(int relationship){ 
	 	this.relationship = relationship;
	}
 
	public int getRelationship(){ 
		return this.relationship;
	}
 
	public void setPersonB(int personB){ 
	 	this.personB = personB;
	}
 
	public int getPersonB(){ 
		return this.personB;
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
 
	public void setDateChanged(java.util.Date dateChanged){ 
	 	this.dateChanged = dateChanged;
	}
 
	public java.util.Date getDateChanged(){ 
		return this.dateChanged;
	}
 
	public void setChangedBy(int changedBy){ 
	 	this.changedBy = changedBy;
	}
 
	public int getChangedBy(){ 
		return this.changedBy;
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
 		return this.relationshipId; 
	} 
 
	public void setObjectId(int selfId){ 
		this.relationshipId = selfId; 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		this.relationshipId = rs.getInt("relationship_id");
		this.personA = rs.getInt("person_a");
		this.relationship = rs.getInt("relationship");
		this.personB = rs.getInt("person_b");
		this.creator = rs.getInt("creator");
		this.dateCreated =  rs.getTimestamp("date_created") != null ? new java.util.Date( rs.getTimestamp("date_created").getTime() ) : null;
		this.voided = rs.getByte("voided");
		this.voidedBy = rs.getInt("voided_by");
		this.dateVoided =  rs.getTimestamp("date_voided") != null ? new java.util.Date( rs.getTimestamp("date_voided").getTime() ) : null;
		this.voidReason = rs.getString("void_reason") != null ? rs.getString("void_reason").trim() : null;
		this.uuid = rs.getString("uuid") != null ? rs.getString("uuid").trim() : null;
		this.dateChanged =  rs.getTimestamp("date_changed") != null ? new java.util.Date( rs.getTimestamp("date_changed").getTime() ) : null;
		this.changedBy = rs.getInt("changed_by");
		this.startDate =  rs.getTimestamp("start_date") != null ? new java.util.Date( rs.getTimestamp("start_date").getTime() ) : null;
		this.endDate =  rs.getTimestamp("end_date") != null ? new java.util.Date( rs.getTimestamp("end_date").getTime() ) : null;
		this.lastSyncDate =  rs.getTimestamp("last_sync_date") != null ? new java.util.Date( rs.getTimestamp("last_sync_date").getTime() ) : null;
		this.originRecordId = rs.getInt("origin_record_id");
		this.originAppLocationCode = rs.getString("origin_app_location_code") != null ? rs.getString("origin_app_location_code").trim() : null;
			} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "relationship_id"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParams(){ 
 		Object[] params = {this.personA == 0 ? null : this.personA, this.relationship == 0 ? null : this.relationship, this.personB == 0 ? null : this.personB, this.creator == 0 ? null : this.creator, this.dateCreated, this.voided, this.voidedBy == 0 ? null : this.voidedBy, this.dateVoided, this.voidReason, this.uuid, this.dateChanged, this.changedBy == 0 ? null : this.changedBy, this.startDate, this.endDate, this.lastSyncDate, this.originRecordId, this.originAppLocationCode, this.consistent};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.personA == 0 ? null : this.personA, this.relationship == 0 ? null : this.relationship, this.personB == 0 ? null : this.personB, this.creator == 0 ? null : this.creator, this.dateCreated, this.voided, this.voidedBy == 0 ? null : this.voidedBy, this.dateVoided, this.voidReason, this.uuid, this.dateChanged, this.changedBy == 0 ? null : this.changedBy, this.startDate, this.endDate, this.lastSyncDate, this.originRecordId, this.originAppLocationCode, this.consistent, this.relationshipId};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQL(){ 
 		return "INSERT INTO relationship(person_a, relationship, person_b, creator, date_created, voided, voided_by, date_voided, void_reason, uuid, date_changed, changed_by, start_date, end_date, last_sync_date, origin_record_id, origin_app_location_code, consistent) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE relationship SET person_a = ?, relationship = ?, person_b = ?, creator = ?, date_created = ?, voided = ?, voided_by = ?, date_voided = ?, void_reason = ?, uuid = ?, date_changed = ?, changed_by = ?, start_date = ?, end_date = ?, last_sync_date = ?, origin_record_id = ?, origin_app_location_code = ?, consistent = ? WHERE relationship_id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.personA == 0 ? null : this.personA) + "," + (this.relationship == 0 ? null : this.relationship) + "," + (this.personB == 0 ? null : this.personB) + "," + (this.creator == 0 ? null : this.creator) + "," + (this.dateCreated != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateCreated)  +"\"" : null) + "," + (this.voided) + "," + (this.voidedBy == 0 ? null : this.voidedBy) + "," + (this.dateVoided != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateVoided)  +"\"" : null) + "," + (this.voidReason != null ? "\""+voidReason+"\"" : null) + "," + (this.uuid != null ? "\""+uuid+"\"" : null) + "," + (this.dateChanged != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateChanged)  +"\"" : null) + "," + (this.changedBy == 0 ? null : this.changedBy) + "," + (this.startDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(startDate)  +"\"" : null) + "," + (this.endDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(endDate)  +"\"" : null) + "," + (this.lastSyncDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(lastSyncDate)  +"\"" : null) + "," + (this.originRecordId) + "," + (this.originAppLocationCode != null ? "\""+originAppLocationCode+"\"" : null) + "," + (this.consistent); 
	} 
 
	@Override
	public boolean isGeneratedFromSkeletonClass() {
		return false;
	}

	@Override
	public boolean hasParents() {
		if (this.personA != 0) return true;
		if (this.personB != 0) return true;
		if (this.creator != 0) return true;
		if (this.voidedBy != 0) return true;
		if (this.changedBy != 0) return true;
		if (this.relationship != 0) return true;
		return false;
	}

	@Override
	public int retrieveSharedPKKey(Connection conn) throws ParentNotYetMigratedException, DBException {
		throw new RuntimeException("No PKSharedInfo defined!");	}

	@Override
	public void loadDestParentInfo(Connection conn) throws ParentNotYetMigratedException, DBException {
		OpenMRSObject parentOnDestination = null;
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.destinationpkg.PersonVO.class, this.personA, false, conn); 
		this.personA = 0;
		if (parentOnDestination  != null) this.personA = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.destinationpkg.PersonVO.class, this.personB, false, conn); 
		this.personB = 0;
		if (parentOnDestination  != null) this.personB = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.destinationpkg.UsersVO.class, this.creator, false, conn); 
		this.creator = 0;
		if (parentOnDestination  != null) this.creator = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.destinationpkg.UsersVO.class, this.voidedBy, true, conn); 
		this.voidedBy = 0;
		if (parentOnDestination  != null) this.voidedBy = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.destinationpkg.UsersVO.class, this.changedBy, true, conn); 
		this.changedBy = 0;
		if (parentOnDestination  != null) this.changedBy = parentOnDestination.getObjectId();
 
	}

	@Override
	public int getParentValue(String parentAttName) {		
		if (parentAttName.equals("personA")) return this.personA;		
		if (parentAttName.equals("personB")) return this.personB;		
		if (parentAttName.equals("creator")) return this.creator;		
		if (parentAttName.equals("voidedBy")) return this.voidedBy;		
		if (parentAttName.equals("changedBy")) return this.changedBy;		
		if (parentAttName.equals("relationship")) return this.relationship;

		throw new RuntimeException("No found parent for: " + parentAttName);	}


}