package org.openmrs.module.eptssync.model.openmrs.sourcepkg; 
 
import org.openmrs.module.eptssync.model.openmrs.generic.*; 
 
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.eptssync.utilities.db.conn.DBException; 
import org.openmrs.module.eptssync.exceptions.ParentNotYetMigratedException; 
 
import java.sql.Connection; 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class OrderTypeVO extends AbstractOpenMRSObject implements OpenMRSObject { 
	private int orderTypeId;
	private String name;
	private String description;
	private int creator;
	private java.util.Date dateCreated;
	private byte retired;
	private int retiredBy;
	private java.util.Date dateRetired;
	private String retireReason;
	private String uuid;
	private String javaClassName;
	private int parent;
	private int changedBy;
	private java.util.Date dateChanged;
 
	public OrderTypeVO() { 
		this.metadata = false;
	} 
 
	public void setOrderTypeId(int orderTypeId){ 
	 	this.orderTypeId = orderTypeId;
	}
 
	public int getOrderTypeId(){ 
		return this.orderTypeId;
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
 
	public void setUuid(String uuid){ 
	 	this.uuid = uuid;
	}
 
	public String getUuid(){ 
		return this.uuid;
	}
 
	public void setJavaClassName(String javaClassName){ 
	 	this.javaClassName = javaClassName;
	}
 
	public String getJavaClassName(){ 
		return this.javaClassName;
	}
 
	public void setParent(int parent){ 
	 	this.parent = parent;
	}
 
	public int getParent(){ 
		return this.parent;
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
 		return this.orderTypeId; 
	} 
 
	public void setObjectId(int selfId){ 
		this.orderTypeId = selfId; 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		this.orderTypeId = rs.getInt("order_type_id");
		this.name = rs.getString("name") != null ? rs.getString("name").trim() : null;
		this.description = rs.getString("description") != null ? rs.getString("description").trim() : null;
		this.creator = rs.getInt("creator");
		this.dateCreated =  rs.getTimestamp("date_created") != null ? new java.util.Date( rs.getTimestamp("date_created").getTime() ) : null;
		this.retired = rs.getByte("retired");
		this.retiredBy = rs.getInt("retired_by");
		this.dateRetired =  rs.getTimestamp("date_retired") != null ? new java.util.Date( rs.getTimestamp("date_retired").getTime() ) : null;
		this.retireReason = rs.getString("retire_reason") != null ? rs.getString("retire_reason").trim() : null;
		this.uuid = rs.getString("uuid") != null ? rs.getString("uuid").trim() : null;
		this.javaClassName = rs.getString("java_class_name") != null ? rs.getString("java_class_name").trim() : null;
		this.parent = rs.getInt("parent");
		this.changedBy = rs.getInt("changed_by");
			} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "order_type_id"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParams(){ 
 		Object[] params = {this.name, this.description, this.creator == 0 ? null : this.creator, this.dateCreated, this.retired, this.retiredBy == 0 ? null : this.retiredBy, this.dateRetired, this.retireReason, this.uuid, this.javaClassName, this.parent == 0 ? null : this.parent, this.changedBy == 0 ? null : this.changedBy, this.dateChanged};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.name, this.description, this.creator == 0 ? null : this.creator, this.dateCreated, this.retired, this.retiredBy == 0 ? null : this.retiredBy, this.dateRetired, this.retireReason, this.uuid, this.javaClassName, this.parent == 0 ? null : this.parent, this.changedBy == 0 ? null : this.changedBy, this.dateChanged, this.orderTypeId};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQL(){ 
 		return "INSERT INTO order_type(name, description, creator, date_created, retired, retired_by, date_retired, retire_reason, uuid, java_class_name, parent, changed_by, date_changed) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE order_type SET name = ?, description = ?, creator = ?, date_created = ?, retired = ?, retired_by = ?, date_retired = ?, retire_reason = ?, uuid = ?, java_class_name = ?, parent = ?, changed_by = ?, date_changed = ? WHERE order_type_id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.name != null ? "\""+name+"\"" : null) + "," + (this.description != null ? "\""+description+"\"" : null) + "," + (this.creator == 0 ? null : this.creator) + "," + (this.dateCreated != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateCreated)  +"\"" : null) + "," + (this.retired) + "," + (this.retiredBy == 0 ? null : this.retiredBy) + "," + (this.dateRetired != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateRetired)  +"\"" : null) + "," + (this.retireReason != null ? "\""+retireReason+"\"" : null) + "," + (this.uuid != null ? "\""+uuid+"\"" : null) + "," + (this.javaClassName != null ? "\""+javaClassName+"\"" : null) + "," + (this.parent == 0 ? null : this.parent) + "," + (this.changedBy == 0 ? null : this.changedBy) + "," + (this.dateChanged != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateChanged)  +"\"" : null); 
	} 
 
	@Override
	public boolean isGeneratedFromSkeletonClass() {
		return false;
	}

	@Override
	public boolean hasParents() {
		if (this.changedBy != 0) return true;
		if (this.parent != 0) return true;
		if (this.creator != 0) return true;
		if (this.retiredBy != 0) return true;
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
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.sourcepkg.OrderTypeVO.class, this.parent, true, conn); 
		this.parent = 0;
		if (parentOnDestination  != null) this.parent = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.sourcepkg.UsersVO.class, this.creator, false, conn); 
		this.creator = 0;
		if (parentOnDestination  != null) this.creator = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.sourcepkg.UsersVO.class, this.retiredBy, true, conn); 
		this.retiredBy = 0;
		if (parentOnDestination  != null) this.retiredBy = parentOnDestination.getObjectId();
 
	}

	@Override
	public int getParentValue(String parentAttName) {		
		if (parentAttName.equals("changedBy")) return this.changedBy;		
		if (parentAttName.equals("parent")) return this.parent;		
		if (parentAttName.equals("creator")) return this.creator;		
		if (parentAttName.equals("retiredBy")) return this.retiredBy;

		throw new RuntimeException("No found parent for: " + parentAttName);	}


}