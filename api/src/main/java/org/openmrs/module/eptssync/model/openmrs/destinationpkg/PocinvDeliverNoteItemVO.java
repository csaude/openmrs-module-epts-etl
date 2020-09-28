package org.openmrs.module.eptssync.model.openmrs.destinationpkg; 
 
import org.openmrs.module.eptssync.model.openmrs.generic.*; 
 
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.eptssync.utilities.db.conn.DBException; 
import org.openmrs.module.eptssync.exceptions.ParentNotYetMigratedException; 
 
import java.sql.Connection; 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class PocinvDeliverNoteItemVO extends AbstractOpenMRSObject implements OpenMRSObject { 
	private int deliverNoteItemId;
	private int deliverNoteId;
	private int drugPackageId;
	private int quantity;
	private int authorizedQuantity;
	private int requestedQuantity;
	private int unitPrice;
	private int batchId;
	private java.util.Date expireDate;
	private String lotNumber;
	private String tokenNumber;
	private String uuid;
	private int creator;
	private java.util.Date dateCreated;
	private int changedBy;
	private java.util.Date dateChanged;
	private byte retired;
	private int retiredBy;
	private java.util.Date dateRetired;
	private String retireReason;
 
	public PocinvDeliverNoteItemVO() { 
		this.metadata = false;
	} 
 
	public void setDeliverNoteItemId(int deliverNoteItemId){ 
	 	this.deliverNoteItemId = deliverNoteItemId;
	}
 
	public int getDeliverNoteItemId(){ 
		return this.deliverNoteItemId;
	}
 
	public void setDeliverNoteId(int deliverNoteId){ 
	 	this.deliverNoteId = deliverNoteId;
	}
 
	public int getDeliverNoteId(){ 
		return this.deliverNoteId;
	}
 
	public void setDrugPackageId(int drugPackageId){ 
	 	this.drugPackageId = drugPackageId;
	}
 
	public int getDrugPackageId(){ 
		return this.drugPackageId;
	}
 
	public void setQuantity(int quantity){ 
	 	this.quantity = quantity;
	}
 
	public int getQuantity(){ 
		return this.quantity;
	}
 
	public void setAuthorizedQuantity(int authorizedQuantity){ 
	 	this.authorizedQuantity = authorizedQuantity;
	}
 
	public int getAuthorizedQuantity(){ 
		return this.authorizedQuantity;
	}
 
	public void setRequestedQuantity(int requestedQuantity){ 
	 	this.requestedQuantity = requestedQuantity;
	}
 
	public int getRequestedQuantity(){ 
		return this.requestedQuantity;
	}
 
	public void setUnitPrice(int unitPrice){ 
	 	this.unitPrice = unitPrice;
	}
 
	public int getUnitPrice(){ 
		return this.unitPrice;
	}
 
	public void setBatchId(int batchId){ 
	 	this.batchId = batchId;
	}
 
	public int getBatchId(){ 
		return this.batchId;
	}
 
	public void setExpireDate(java.util.Date expireDate){ 
	 	this.expireDate = expireDate;
	}
 
	public java.util.Date getExpireDate(){ 
		return this.expireDate;
	}
 
	public void setLotNumber(String lotNumber){ 
	 	this.lotNumber = lotNumber;
	}
 
	public String getLotNumber(){ 
		return this.lotNumber;
	}
 
	public void setTokenNumber(String tokenNumber){ 
	 	this.tokenNumber = tokenNumber;
	}
 
	public String getTokenNumber(){ 
		return this.tokenNumber;
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
 		return this.deliverNoteItemId; 
	} 
 
	public void setObjectId(int selfId){ 
		this.deliverNoteItemId = selfId; 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		this.deliverNoteItemId = rs.getInt("deliver_note_item_id");
		this.deliverNoteId = rs.getInt("deliver_note_id");
		this.drugPackageId = rs.getInt("drug_package_id");
		this.quantity = rs.getInt("quantity");
		this.authorizedQuantity = rs.getInt("authorized_quantity");
		this.requestedQuantity = rs.getInt("requested_quantity");
		this.unitPrice = rs.getInt("unit_price");
		this.batchId = rs.getInt("batch_id");
		this.expireDate =  rs.getTimestamp("expire_date") != null ? new java.util.Date( rs.getTimestamp("expire_date").getTime() ) : null;
		this.lotNumber = rs.getString("lot_number") != null ? rs.getString("lot_number").trim() : null;
		this.tokenNumber = rs.getString("token_number") != null ? rs.getString("token_number").trim() : null;
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
 		return "deliver_note_item_id"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParams(){ 
 		Object[] params = {this.deliverNoteId == 0 ? null : this.deliverNoteId, this.drugPackageId == 0 ? null : this.drugPackageId, this.quantity, this.authorizedQuantity, this.requestedQuantity, this.unitPrice, this.batchId == 0 ? null : this.batchId, this.expireDate, this.lotNumber, this.tokenNumber, this.uuid, this.creator == 0 ? null : this.creator, this.dateCreated, this.changedBy == 0 ? null : this.changedBy, this.dateChanged, this.retired, this.retiredBy == 0 ? null : this.retiredBy, this.dateRetired, this.retireReason};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.deliverNoteId == 0 ? null : this.deliverNoteId, this.drugPackageId == 0 ? null : this.drugPackageId, this.quantity, this.authorizedQuantity, this.requestedQuantity, this.unitPrice, this.batchId == 0 ? null : this.batchId, this.expireDate, this.lotNumber, this.tokenNumber, this.uuid, this.creator == 0 ? null : this.creator, this.dateCreated, this.changedBy == 0 ? null : this.changedBy, this.dateChanged, this.retired, this.retiredBy == 0 ? null : this.retiredBy, this.dateRetired, this.retireReason, this.deliverNoteItemId};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQL(){ 
 		return "INSERT INTO pocinv_deliver_note_item(deliver_note_id, drug_package_id, quantity, authorized_quantity, requested_quantity, unit_price, batch_id, expire_date, lot_number, token_number, uuid, creator, date_created, changed_by, date_changed, retired, retired_by, date_retired, retire_reason) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE pocinv_deliver_note_item SET deliver_note_id = ?, drug_package_id = ?, quantity = ?, authorized_quantity = ?, requested_quantity = ?, unit_price = ?, batch_id = ?, expire_date = ?, lot_number = ?, token_number = ?, uuid = ?, creator = ?, date_created = ?, changed_by = ?, date_changed = ?, retired = ?, retired_by = ?, date_retired = ?, retire_reason = ? WHERE deliver_note_item_id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.deliverNoteId == 0 ? null : this.deliverNoteId) + "," + (this.drugPackageId == 0 ? null : this.drugPackageId) + "," + (this.quantity) + "," + (this.authorizedQuantity) + "," + (this.requestedQuantity) + "," + (this.unitPrice) + "," + (this.batchId == 0 ? null : this.batchId) + "," + (this.expireDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(expireDate)  +"\"" : null) + "," + (this.lotNumber != null ? "\""+lotNumber+"\"" : null) + "," + (this.tokenNumber != null ? "\""+tokenNumber+"\"" : null) + "," + (this.uuid != null ? "\""+uuid+"\"" : null) + "," + (this.creator == 0 ? null : this.creator) + "," + (this.dateCreated != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateCreated)  +"\"" : null) + "," + (this.changedBy == 0 ? null : this.changedBy) + "," + (this.dateChanged != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateChanged)  +"\"" : null) + "," + (this.retired) + "," + (this.retiredBy == 0 ? null : this.retiredBy) + "," + (this.dateRetired != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateRetired)  +"\"" : null) + "," + (this.retireReason != null ? "\""+retireReason+"\"" : null); 
	} 
 
	@Override
	public boolean isGeneratedFromSkeletonClass() {
		return false;
	}

	@Override
	public boolean hasParents() {
		if (this.batchId != 0) return true;
		if (this.changedBy != 0) return true;
		if (this.creator != 0) return true;
		if (this.deliverNoteId != 0) return true;
		if (this.drugPackageId != 0) return true;
		if (this.retiredBy != 0) return true;
		return false;
	}

	@Override
	public int retrieveSharedPKKey(Connection conn) throws ParentNotYetMigratedException, DBException {
		throw new RuntimeException("No PKSharedInfo defined!");	}

	@Override
	public void loadDestParentInfo(Connection conn) throws ParentNotYetMigratedException, DBException {
		OpenMRSObject parentOnDestination = null;
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.destinationpkg.PocinvBatchVO.class, this.batchId, false, conn); 
		this.batchId = 0;
		if (parentOnDestination  != null) this.batchId = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.destinationpkg.UsersVO.class, this.changedBy, true, conn); 
		this.changedBy = 0;
		if (parentOnDestination  != null) this.changedBy = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.destinationpkg.UsersVO.class, this.creator, false, conn); 
		this.creator = 0;
		if (parentOnDestination  != null) this.creator = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.destinationpkg.PocinvDeliverNoteVO.class, this.deliverNoteId, false, conn); 
		this.deliverNoteId = 0;
		if (parentOnDestination  != null) this.deliverNoteId = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.destinationpkg.PocinvDrugPackageVO.class, this.drugPackageId, false, conn); 
		this.drugPackageId = 0;
		if (parentOnDestination  != null) this.drugPackageId = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.destinationpkg.UsersVO.class, this.retiredBy, true, conn); 
		this.retiredBy = 0;
		if (parentOnDestination  != null) this.retiredBy = parentOnDestination.getObjectId();
 
	}

	@Override
	public int getParentValue(String parentAttName) {		
		if (parentAttName.equals("batchId")) return this.batchId;		
		if (parentAttName.equals("changedBy")) return this.changedBy;		
		if (parentAttName.equals("creator")) return this.creator;		
		if (parentAttName.equals("deliverNoteId")) return this.deliverNoteId;		
		if (parentAttName.equals("drugPackageId")) return this.drugPackageId;		
		if (parentAttName.equals("retiredBy")) return this.retiredBy;

		throw new RuntimeException("No found parent for: " + parentAttName);	}


}