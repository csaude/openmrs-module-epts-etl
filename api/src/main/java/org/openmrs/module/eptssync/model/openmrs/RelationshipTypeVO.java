package org.openmrs.module.eptssync.model.openmrs; 
 
import org.openmrs.module.eptssync.model.openmrs.generic.*; 
 
import org.openmrs.module.eptssync.utilities.db.conn.DBException; 
import org.openmrs.module.eptssync.utilities.db.conn.OpenConnection; 
import org.openmrs.module.eptssync.exceptions.ParentNotYetMigratedException; 
 
import java.sql.Connection; 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class RelationshipTypeVO extends AbstractOpenMRSObject implements OpenMRSObject { 
	private int relationshipTypeId;
	private String aIsToB;
	private String bIsToA;
	private int preferred;
	private int weight;
	private String description;
	private int creator;
	private java.util.Date dateCreated;
	private String uuid;
	private byte retired;
	private int retiredBy;
	private java.util.Date dateRetired;
	private String retireReason;
	private java.util.Date lastSyncDate;
	private int originRecordId;
	private java.util.Date dateChanged;
	private String originAppLocationCode;
 
	public RelationshipTypeVO() { 
		this.metadata = true;
	} 
 
	public void setRelationshipTypeId(int relationshipTypeId){ 
	 	this.relationshipTypeId = relationshipTypeId;
	}
 
	public int getRelationshipTypeId(){ 
		return this.relationshipTypeId;
	}
 
	public void setAIsToB(String aIsToB){ 
	 	this.aIsToB = aIsToB;
	}
 
	public String getAIsToB(){ 
		return this.aIsToB;
	}
 
	public void setBIsToA(String bIsToA){ 
	 	this.bIsToA = bIsToA;
	}
 
	public String getBIsToA(){ 
		return this.bIsToA;
	}
 
	public void setPreferred(int preferred){ 
	 	this.preferred = preferred;
	}
 
	public int getPreferred(){ 
		return this.preferred;
	}
 
	public void setWeight(int weight){ 
	 	this.weight = weight;
	}
 
	public int getWeight(){ 
		return this.weight;
	}
 
	public void setDescription(String description){ 
	 	this.description = description;
	}
 
	public String getDescription(){ 
		return this.description;
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
 
	public void setUuid(String uuid){ 
	 	this.uuid = uuid;
	}
 
	public String getUuid(){ 
		return this.uuid;
	}
 
	public void setRetired(byte retired){ 
	 	this.retired = retired;
	}
 
	public byte getRetired(){ 
		return this.retired;
	}
 
	public void setRetiredBy(int retiredBy){ 
	 	this.retiredBy = retiredBy;
	}
 
	public int getRetiredBy(){ 
		return this.retiredBy;
	}
 
	public void setDateRetired(java.util.Date dateRetired){ 
	 	this.dateRetired = dateRetired;
	}
 
	public java.util.Date getDateRetired(){ 
		return this.dateRetired;
	}
 
	public void setRetireReason(String retireReason){ 
	 	this.retireReason = retireReason;
	}
 
	public String getRetireReason(){ 
		return this.retireReason;
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
 
	public void setDateChanged(java.util.Date dateChanged){ 
	 	this.dateChanged = dateChanged;
	}
 
	public java.util.Date getDateChanged(){ 
		return this.dateChanged;
	}
 
	public void setOriginAppLocationCode(String originAppLocationCode){ 
	 	this.originAppLocationCode = originAppLocationCode;
	}


 
	public String getOriginAppLocationCode(){ 
		return this.originAppLocationCode;
	}
 
	public int getObjectId() { 
 		return this.relationshipTypeId; 
	} 
 
	public void setObjectId(int selfId){ 
		this.relationshipTypeId = selfId; 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		this.relationshipTypeId = rs.getInt("relationship_type_id");
		this.aIsToB = rs.getString("a_is_to_b") != null ? rs.getString("a_is_to_b").trim() : null;
		this.bIsToA = rs.getString("b_is_to_a") != null ? rs.getString("b_is_to_a").trim() : null;
		this.preferred = rs.getInt("preferred");
		this.weight = rs.getInt("weight");
		this.description = rs.getString("description") != null ? rs.getString("description").trim() : null;
		this.creator = rs.getInt("creator");
		this.dateCreated =  rs.getTimestamp("date_created") != null ? new java.util.Date( rs.getTimestamp("date_created").getTime() ) : null;
		this.uuid = rs.getString("uuid") != null ? rs.getString("uuid").trim() : null;
		this.retired = rs.getByte("retired");
		this.retiredBy = rs.getInt("retired_by");
		this.dateRetired =  rs.getTimestamp("date_retired") != null ? new java.util.Date( rs.getTimestamp("date_retired").getTime() ) : null;
		this.retireReason = rs.getString("retire_reason") != null ? rs.getString("retire_reason").trim() : null;
		this.lastSyncDate =  rs.getTimestamp("last_sync_date") != null ? new java.util.Date( rs.getTimestamp("last_sync_date").getTime() ) : null;
		this.originRecordId = rs.getInt("origin_record_id");
		this.dateChanged =  rs.getTimestamp("date_changed") != null ? new java.util.Date( rs.getTimestamp("date_changed").getTime() ) : null;
			} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "relationship_type_id"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParams(){ 
 		Object[] params = {this.aIsToB, this.bIsToA, this.preferred, this.weight, this.description, this.creator == 0 ? null : this.creator, this.dateCreated, this.uuid, this.retired, this.retiredBy == 0 ? null : this.retiredBy, this.dateRetired, this.retireReason, this.lastSyncDate, this.originRecordId, this.dateChanged, this.originAppLocationCode};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.aIsToB, this.bIsToA, this.preferred, this.weight, this.description, this.creator == 0 ? null : this.creator, this.dateCreated, this.uuid, this.retired, this.retiredBy == 0 ? null : this.retiredBy, this.dateRetired, this.retireReason, this.lastSyncDate, this.originRecordId, this.dateChanged, this.originAppLocationCode, this.relationshipTypeId};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQL(){ 
 		return "INSERT INTO relationship_type(a_is_to_b, b_is_to_a, preferred, weight, description, creator, date_created, uuid, retired, retired_by, date_retired, retire_reason, last_sync_date, origin_record_id, date_changed, origin_app_location_code) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE relationship_type SET a_is_to_b = ?, b_is_to_a = ?, preferred = ?, weight = ?, description = ?, creator = ?, date_created = ?, uuid = ?, retired = ?, retired_by = ?, date_retired = ?, retire_reason = ?, last_sync_date = ?, origin_record_id = ?, date_changed = ?, origin_app_location_code = ? WHERE relationship_type_id = ?;"; 
	} 
 
	public void loadDestParentInfo(Connection conn) throws ParentNotYetMigratedException, DBException {
		OpenMRSObject parentOnDestination = null;
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.UsersVO.class, this.creator, false, conn); 
		this.creator = 0;
		if (parentOnDestination  != null) this.creator = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.UsersVO.class, this.retiredBy, true, conn); 
		this.retiredBy = 0;
		if (parentOnDestination  != null) this.retiredBy = parentOnDestination.getObjectId();
 
	}
}