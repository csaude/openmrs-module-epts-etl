package org.openmrs.module.eptssync.model.openmrs.destinationpkg; 
 
import org.openmrs.module.eptssync.model.openmrs.generic.*; 
 
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.eptssync.utilities.db.conn.DBException; 
import org.openmrs.module.eptssync.utilities.AttDefinedElements; 
import org.openmrs.module.eptssync.exceptions.ParentNotYetMigratedException; 
 
import java.sql.Connection; 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import java.io.File; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class PocinvBatchVO extends AbstractOpenMRSObject implements OpenMRSObject { 
	private int batchId;
	private int drugPackageId;
	private int locationId;
	private java.util.Date expireDate;
	private int packageQuantity;
	private int packageQuantityUnits;
	private int remainPackageQuantityUnits;
	private int unbalancedUnitsQuantity;
	private int version;
	private String uuid;
	private int creator;
	private java.util.Date dateCreated;
	private int changedBy;
	private java.util.Date dateChanged;
	private byte retired;
	private int retiredBy;
	private java.util.Date dateRetired;
	private String retireReason;
 
	public PocinvBatchVO() { 
		this.metadata = false;
	} 
 
	public void setBatchId(int batchId){ 
	 	this.batchId = batchId;
	}
 
	public int getBatchId(){ 
		return this.batchId;
	}
 
	public void setDrugPackageId(int drugPackageId){ 
	 	this.drugPackageId = drugPackageId;
	}
 
	public int getDrugPackageId(){ 
		return this.drugPackageId;
	}
 
	public void setLocationId(int locationId){ 
	 	this.locationId = locationId;
	}
 
	public int getLocationId(){ 
		return this.locationId;
	}
 
	public void setExpireDate(java.util.Date expireDate){ 
	 	this.expireDate = expireDate;
	}
 
	public java.util.Date getExpireDate(){ 
		return this.expireDate;
	}
 
	public void setPackageQuantity(int packageQuantity){ 
	 	this.packageQuantity = packageQuantity;
	}
 
	public int getPackageQuantity(){ 
		return this.packageQuantity;
	}
 
	public void setPackageQuantityUnits(int packageQuantityUnits){ 
	 	this.packageQuantityUnits = packageQuantityUnits;
	}
 
	public int getPackageQuantityUnits(){ 
		return this.packageQuantityUnits;
	}
 
	public void setRemainPackageQuantityUnits(int remainPackageQuantityUnits){ 
	 	this.remainPackageQuantityUnits = remainPackageQuantityUnits;
	}
 
	public int getRemainPackageQuantityUnits(){ 
		return this.remainPackageQuantityUnits;
	}
 
	public void setUnbalancedUnitsQuantity(int unbalancedUnitsQuantity){ 
	 	this.unbalancedUnitsQuantity = unbalancedUnitsQuantity;
	}
 
	public int getUnbalancedUnitsQuantity(){ 
		return this.unbalancedUnitsQuantity;
	}
 
	public void setVersion(int version){ 
	 	this.version = version;
	}
 
	public int getVersion(){ 
		return this.version;
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
 		return this.batchId; 
	} 
 
	public void setObjectId(int selfId){ 
		this.batchId = selfId; 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		this.batchId = rs.getInt("batch_id");
		this.drugPackageId = rs.getInt("drug_package_id");
		this.locationId = rs.getInt("location_id");
		this.expireDate =  rs.getTimestamp("expire_date") != null ? new java.util.Date( rs.getTimestamp("expire_date").getTime() ) : null;
		this.packageQuantity = rs.getInt("package_quantity");
		this.packageQuantityUnits = rs.getInt("package_quantity_units");
		this.remainPackageQuantityUnits = rs.getInt("remain_package_quantity_units");
		this.unbalancedUnitsQuantity = rs.getInt("unbalanced_units_quantity");
		this.version = rs.getInt("version");
		this.uuid = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("uuid") != null ? rs.getString("uuid").trim() : null);
		this.creator = rs.getInt("creator");
		this.dateCreated =  rs.getTimestamp("date_created") != null ? new java.util.Date( rs.getTimestamp("date_created").getTime() ) : null;
		this.changedBy = rs.getInt("changed_by");
		this.dateChanged =  rs.getTimestamp("date_changed") != null ? new java.util.Date( rs.getTimestamp("date_changed").getTime() ) : null;
		this.retired = rs.getByte("retired");
		this.retiredBy = rs.getInt("retired_by");
		this.dateRetired =  rs.getTimestamp("date_retired") != null ? new java.util.Date( rs.getTimestamp("date_retired").getTime() ) : null;
		this.retireReason = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("retire_reason") != null ? rs.getString("retire_reason").trim() : null);
	} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "batch_id"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParams(){ 
 		Object[] params = {this.drugPackageId == 0 ? null : this.drugPackageId, this.locationId == 0 ? null : this.locationId, this.expireDate, this.packageQuantity, this.packageQuantityUnits, this.remainPackageQuantityUnits, this.unbalancedUnitsQuantity, this.version, this.uuid, this.creator == 0 ? null : this.creator, this.dateCreated, this.changedBy == 0 ? null : this.changedBy, this.dateChanged, this.retired, this.retiredBy == 0 ? null : this.retiredBy, this.dateRetired, this.retireReason};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.drugPackageId == 0 ? null : this.drugPackageId, this.locationId == 0 ? null : this.locationId, this.expireDate, this.packageQuantity, this.packageQuantityUnits, this.remainPackageQuantityUnits, this.unbalancedUnitsQuantity, this.version, this.uuid, this.creator == 0 ? null : this.creator, this.dateCreated, this.changedBy == 0 ? null : this.changedBy, this.dateChanged, this.retired, this.retiredBy == 0 ? null : this.retiredBy, this.dateRetired, this.retireReason, this.batchId};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQL(){ 
 		return "INSERT INTO pocinv_batch(drug_package_id, location_id, expire_date, package_quantity, package_quantity_units, remain_package_quantity_units, unbalanced_units_quantity, version, uuid, creator, date_created, changed_by, date_changed, retired, retired_by, date_retired, retire_reason) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE pocinv_batch SET drug_package_id = ?, location_id = ?, expire_date = ?, package_quantity = ?, package_quantity_units = ?, remain_package_quantity_units = ?, unbalanced_units_quantity = ?, version = ?, uuid = ?, creator = ?, date_created = ?, changed_by = ?, date_changed = ?, retired = ?, retired_by = ?, date_retired = ?, retire_reason = ? WHERE batch_id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.drugPackageId == 0 ? null : this.drugPackageId) + "," + (this.locationId == 0 ? null : this.locationId) + "," + (this.expireDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(expireDate)  +"\"" : null) + "," + (this.packageQuantity) + "," + (this.packageQuantityUnits) + "," + (this.remainPackageQuantityUnits) + "," + (this.unbalancedUnitsQuantity) + "," + (this.version) + "," + (this.uuid != null ? "\""+ utilities.scapeQuotationMarks(uuid)  +"\"" : null) + "," + (this.creator == 0 ? null : this.creator) + "," + (this.dateCreated != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateCreated)  +"\"" : null) + "," + (this.changedBy == 0 ? null : this.changedBy) + "," + (this.dateChanged != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateChanged)  +"\"" : null) + "," + (this.retired) + "," + (this.retiredBy == 0 ? null : this.retiredBy) + "," + (this.dateRetired != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateRetired)  +"\"" : null) + "," + (this.retireReason != null ? "\""+ utilities.scapeQuotationMarks(retireReason)  +"\"" : null); 
	} 
 
	@Override
	public boolean isGeneratedFromSkeletonClass() {
		return false;
	}

	@Override
	public boolean hasParents() {
		if (this.changedBy != 0) return true;
		if (this.creator != 0) return true;
		if (this.drugPackageId != 0) return true;
		if (this.locationId != 0) return true;
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
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.destinationpkg.PocinvDrugPackageVO.class, this.drugPackageId, false, conn); 
		this.drugPackageId = 0;
		if (parentOnDestination  != null) this.drugPackageId = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.destinationpkg.LocationVO.class, this.locationId, false, conn); 
		this.locationId = 0;
		if (parentOnDestination  != null) this.locationId = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.destinationpkg.UsersVO.class, this.retiredBy, true, conn); 
		this.retiredBy = 0;
		if (parentOnDestination  != null) this.retiredBy = parentOnDestination.getObjectId();
 
	}

	@Override
	public int getParentValue(String parentAttName) {		
		if (parentAttName.equals("changedBy")) return this.changedBy;		
		if (parentAttName.equals("creator")) return this.creator;		
		if (parentAttName.equals("drugPackageId")) return this.drugPackageId;		
		if (parentAttName.equals("locationId")) return this.locationId;		
		if (parentAttName.equals("retiredBy")) return this.retiredBy;

		throw new RuntimeException("No found parent for: " + parentAttName);	}


}