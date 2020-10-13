package org.openmrs.module.eptssync.model.openmrs.destinationpkg; 
 
import org.openmrs.module.eptssync.model.openmrs.generic.*; 
 
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.eptssync.utilities.db.conn.DBException; 
import org.openmrs.module.eptssync.utilities.AttDefinedElements; 
import org.openmrs.module.eptssync.exceptions.ParentNotYetMigratedException; 
 
import java.sql.Connection; 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class PocinvBatchEntryVO extends AbstractOpenMRSObject implements OpenMRSObject { 
	private int batchEntryId;
	private int batchId;
	private int quantity;
	private String batchOperationType;
	private int reversalRequestorId;
	private int reversedId;
	private int orderId;
	private String uuid;
	private int creator;
	private java.util.Date dateCreated;
	private int changedBy;
	private java.util.Date dateChanged;
	private byte retired;
	private int retiredBy;
	private java.util.Date dateRetired;
	private String retireReason;
 
	public PocinvBatchEntryVO() { 
		this.metadata = false;
	} 
 
	public void setBatchEntryId(int batchEntryId){ 
	 	this.batchEntryId = batchEntryId;
	}
 
	public int getBatchEntryId(){ 
		return this.batchEntryId;
	}
 
	public void setBatchId(int batchId){ 
	 	this.batchId = batchId;
	}
 
	public int getBatchId(){ 
		return this.batchId;
	}
 
	public void setQuantity(int quantity){ 
	 	this.quantity = quantity;
	}
 
	public int getQuantity(){ 
		return this.quantity;
	}
 
	public void setBatchOperationType(String batchOperationType){ 
	 	this.batchOperationType = batchOperationType;
	}
 
	public String getBatchOperationType(){ 
		return this.batchOperationType;
	}
 
	public void setReversalRequestorId(int reversalRequestorId){ 
	 	this.reversalRequestorId = reversalRequestorId;
	}
 
	public int getReversalRequestorId(){ 
		return this.reversalRequestorId;
	}
 
	public void setReversedId(int reversedId){ 
	 	this.reversedId = reversedId;
	}
 
	public int getReversedId(){ 
		return this.reversedId;
	}
 
	public void setOrderId(int orderId){ 
	 	this.orderId = orderId;
	}
 
	public int getOrderId(){ 
		return this.orderId;
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
 		return this.batchEntryId; 
	} 
 
	public void setObjectId(int selfId){ 
		this.batchEntryId = selfId; 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		this.batchEntryId = rs.getInt("batch_entry_id");
		this.batchId = rs.getInt("batch_id");
		this.quantity = rs.getInt("quantity");
		this.batchOperationType = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("batch_operation_type") != null ? rs.getString("batch_operation_type").trim() : null);
		this.reversalRequestorId = rs.getInt("reversal_requestor_id");
		this.reversedId = rs.getInt("reversed_id");
		this.orderId = rs.getInt("order_id");
		this.uuid = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("uuid") != null ? rs.getString("uuid").trim() : null);
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
 		return "batch_entry_id"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParams(){ 
 		Object[] params = {this.batchId == 0 ? null : this.batchId, this.quantity, this.batchOperationType, this.reversalRequestorId == 0 ? null : this.reversalRequestorId, this.reversedId == 0 ? null : this.reversedId, this.orderId == 0 ? null : this.orderId, this.uuid, this.creator == 0 ? null : this.creator, this.dateCreated, this.changedBy == 0 ? null : this.changedBy, this.dateChanged, this.retired, this.retiredBy == 0 ? null : this.retiredBy, this.dateRetired, this.retireReason};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.batchId == 0 ? null : this.batchId, this.quantity, this.batchOperationType, this.reversalRequestorId == 0 ? null : this.reversalRequestorId, this.reversedId == 0 ? null : this.reversedId, this.orderId == 0 ? null : this.orderId, this.uuid, this.creator == 0 ? null : this.creator, this.dateCreated, this.changedBy == 0 ? null : this.changedBy, this.dateChanged, this.retired, this.retiredBy == 0 ? null : this.retiredBy, this.dateRetired, this.retireReason, this.batchEntryId};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQL(){ 
 		return "INSERT INTO pocinv_batch_entry(batch_id, quantity, batch_operation_type, reversal_requestor_id, reversed_id, order_id, uuid, creator, date_created, changed_by, date_changed, retired, retired_by, date_retired, retire_reason) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE pocinv_batch_entry SET batch_id = ?, quantity = ?, batch_operation_type = ?, reversal_requestor_id = ?, reversed_id = ?, order_id = ?, uuid = ?, creator = ?, date_created = ?, changed_by = ?, date_changed = ?, retired = ?, retired_by = ?, date_retired = ?, retire_reason = ? WHERE batch_entry_id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.batchId == 0 ? null : this.batchId) + "," + (this.quantity) + "," + (this.batchOperationType != null ? "\""+ utilities.scapeQuotationMarks(batchOperationType)  +"\"" : null) + "," + (this.reversalRequestorId == 0 ? null : this.reversalRequestorId) + "," + (this.reversedId == 0 ? null : this.reversedId) + "," + (this.orderId == 0 ? null : this.orderId) + "," + (this.uuid != null ? "\""+ utilities.scapeQuotationMarks(uuid)  +"\"" : null) + "," + (this.creator == 0 ? null : this.creator) + "," + (this.dateCreated != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateCreated)  +"\"" : null) + "," + (this.changedBy == 0 ? null : this.changedBy) + "," + (this.dateChanged != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateChanged)  +"\"" : null) + "," + (this.retired) + "," + (this.retiredBy == 0 ? null : this.retiredBy) + "," + (this.dateRetired != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateRetired)  +"\"" : null) + "," + (this.retireReason != null ? "\""+ utilities.scapeQuotationMarks(retireReason)  +"\"" : null); 
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
		if (this.orderId != 0) return true;
		if (this.retiredBy != 0) return true;
		if (this.reversalRequestorId != 0) return true;
		if (this.reversedId != 0) return true;
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
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.destinationpkg.OrdersVO.class, this.orderId, true, conn); 
		this.orderId = 0;
		if (parentOnDestination  != null) this.orderId = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.destinationpkg.UsersVO.class, this.retiredBy, true, conn); 
		this.retiredBy = 0;
		if (parentOnDestination  != null) this.retiredBy = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.destinationpkg.PocinvBatchEntryVO.class, this.reversalRequestorId, true, conn); 
		this.reversalRequestorId = 0;
		if (parentOnDestination  != null) this.reversalRequestorId = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.destinationpkg.PocinvBatchEntryVO.class, this.reversedId, true, conn); 
		this.reversedId = 0;
		if (parentOnDestination  != null) this.reversedId = parentOnDestination.getObjectId();
 
	}

	@Override
	public int getParentValue(String parentAttName) {		
		if (parentAttName.equals("batchId")) return this.batchId;		
		if (parentAttName.equals("changedBy")) return this.changedBy;		
		if (parentAttName.equals("creator")) return this.creator;		
		if (parentAttName.equals("orderId")) return this.orderId;		
		if (parentAttName.equals("retiredBy")) return this.retiredBy;		
		if (parentAttName.equals("reversalRequestorId")) return this.reversalRequestorId;		
		if (parentAttName.equals("reversedId")) return this.reversedId;

		throw new RuntimeException("No found parent for: " + parentAttName);	}


}