package org.openmrs.module.eptssync.model.openmrs.destinationpkg; 
 
import org.openmrs.module.eptssync.model.openmrs.generic.*; 
 
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.eptssync.utilities.db.conn.DBException; 
import org.openmrs.module.eptssync.exceptions.ParentNotYetMigratedException; 
 
import java.sql.Connection; 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class PocinvDeliverNoteVO extends AbstractOpenMRSObject implements OpenMRSObject { 
	private int deliverNoteId;
	private int locationId;
	private java.util.Date deliveryDate;
	private String simamNumber;
	private String uuid;
	private int creator;
	private java.util.Date dateCreated;
	private int changedBy;
	private java.util.Date dateChanged;
	private byte retired;
	private int retiredBy;
	private java.util.Date dateRetired;
	private String retireReason;
 
	public PocinvDeliverNoteVO() { 
		this.metadata = false;
	} 
 
	public void setDeliverNoteId(int deliverNoteId){ 
	 	this.deliverNoteId = deliverNoteId;
	}
 
	public int getDeliverNoteId(){ 
		return this.deliverNoteId;
	}
 
	public void setLocationId(int locationId){ 
	 	this.locationId = locationId;
	}
 
	public int getLocationId(){ 
		return this.locationId;
	}
 
	public void setDeliveryDate(java.util.Date deliveryDate){ 
	 	this.deliveryDate = deliveryDate;
	}
 
	public java.util.Date getDeliveryDate(){ 
		return this.deliveryDate;
	}
 
	public void setSimamNumber(String simamNumber){ 
	 	this.simamNumber = simamNumber;
	}
 
	public String getSimamNumber(){ 
		return this.simamNumber;
	}
 
	public void setUuid(String uuid){ 
	 	this.uuid = uuid;
	}
 
	public String getUuid(){ 
		return this.uuid;
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
	}	public int getOriginRecordId(){ 
		return 0;
	}
 
	public void setOriginRecordId(int originRecordId){ }
 
	public String getOriginAppLocationCode(){ 
		return null;
	}
 
	public void setOriginAppLocationCode(String originAppLocationCode){ }
 
	public int getConsistent(){ 
		return 0;
	}
 
	public void setConsistent(int consistent){ }
 

 
	public int getObjectId() { 
 		return this.deliverNoteId; 
	} 
 
	public void setObjectId(int selfId){ 
		this.deliverNoteId = selfId; 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		this.deliverNoteId = rs.getInt("deliver_note_id");
		this.locationId = rs.getInt("location_id");
		this.deliveryDate =  rs.getTimestamp("delivery_date") != null ? new java.util.Date( rs.getTimestamp("delivery_date").getTime() ) : null;
		this.simamNumber = rs.getString("simam_number") != null ? rs.getString("simam_number").trim() : null;
		this.uuid = rs.getString("uuid") != null ? rs.getString("uuid").trim() : null;
		this.creator = rs.getInt("creator");
		this.dateCreated =  rs.getTimestamp("date_created") != null ? new java.util.Date( rs.getTimestamp("date_created").getTime() ) : null;
		this.changedBy = rs.getInt("changed_by");
		this.dateChanged =  rs.getTimestamp("date_changed") != null ? new java.util.Date( rs.getTimestamp("date_changed").getTime() ) : null;
		this.retired = rs.getByte("retired");
		this.retiredBy = rs.getInt("retired_by");
		this.dateRetired =  rs.getTimestamp("date_retired") != null ? new java.util.Date( rs.getTimestamp("date_retired").getTime() ) : null;
			} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "deliver_note_id"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParams(){ 
 		Object[] params = {this.locationId, this.deliveryDate, this.simamNumber, this.uuid, this.creator == 0 ? null : this.creator, this.dateCreated, this.changedBy == 0 ? null : this.changedBy, this.dateChanged, this.retired, this.retiredBy == 0 ? null : this.retiredBy, this.dateRetired, this.retireReason};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.locationId, this.deliveryDate, this.simamNumber, this.uuid, this.creator == 0 ? null : this.creator, this.dateCreated, this.changedBy == 0 ? null : this.changedBy, this.dateChanged, this.retired, this.retiredBy == 0 ? null : this.retiredBy, this.dateRetired, this.retireReason, this.deliverNoteId};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQL(){ 
 		return "INSERT INTO pocinv_deliver_note(location_id, delivery_date, simam_number, uuid, creator, date_created, changed_by, date_changed, retired, retired_by, date_retired, retire_reason) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE pocinv_deliver_note SET location_id = ?, delivery_date = ?, simam_number = ?, uuid = ?, creator = ?, date_created = ?, changed_by = ?, date_changed = ?, retired = ?, retired_by = ?, date_retired = ?, retire_reason = ? WHERE deliver_note_id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.locationId) + "," + (this.deliveryDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(deliveryDate)  +"\"" : null) + "," + (this.simamNumber != null ? "\""+simamNumber+"\"" : null) + "," + (this.uuid != null ? "\""+uuid+"\"" : null) + "," + (this.creator == 0 ? null : this.creator) + "," + (this.dateCreated != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateCreated)  +"\"" : null) + "," + (this.changedBy == 0 ? null : this.changedBy) + "," + (this.dateChanged != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateChanged)  +"\"" : null) + "," + (this.retired) + "," + (this.retiredBy == 0 ? null : this.retiredBy) + "," + (this.dateRetired != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateRetired)  +"\"" : null) + "," + (this.retireReason != null ? "\""+retireReason+"\"" : null); 
	} 
 
	@Override
	public boolean isGeneratedFromSkeletonClass() {
		return false;
	}

	@Override
	public boolean hasParents() {
		if (this.changedBy != 0) return true;
		if (this.creator != 0) return true;
		if (this.retiredBy != 0) return true;
		if (this.retiredBy != 0) return true;
		return false;
	}

	@Override
	public int retrieveSharedPKKey(Connection conn) throws ParentNotYetMigratedException, DBException {
		throw new RuntimeException("No PKSharedInfo defined!");	}

	@Override
	public void loadDestParentInfo(Connection conn) throws ParentNotYetMigratedException, DBException {
		OpenMRSObject parentOnDestination = null;
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.destinationpkg.UsersVO.class, this.changedBy, true, conn); 
		this.changedBy = 0;
		if (parentOnDestination  != null) this.changedBy = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.destinationpkg.UsersVO.class, this.creator, false, conn); 
		this.creator = 0;
		if (parentOnDestination  != null) this.creator = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.destinationpkg.LocationVO.class, this.retiredBy, true, conn); 
		this.retiredBy = 0;
		if (parentOnDestination  != null) this.retiredBy = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.destinationpkg.UsersVO.class, this.retiredBy, true, conn); 
		this.retiredBy = 0;
		if (parentOnDestination  != null) this.retiredBy = parentOnDestination.getObjectId();
 
	}

	@Override
	public int getParentValue(String parentAttName) {		
		if (parentAttName.equals("changedBy")) return this.changedBy;		
		if (parentAttName.equals("creator")) return this.creator;		
		if (parentAttName.equals("retiredBy")) return this.retiredBy;		
		if (parentAttName.equals("retiredBy")) return this.retiredBy;

		throw new RuntimeException("No found parent for: " + parentAttName);	}


}