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
 
public class SerializedObjectVO extends AbstractOpenMRSObject implements OpenMRSObject { 
	private int serializedObjectId;
	private String name;
	private String description;
	private String type;
	private String subtype;
	private String serializationClass;
	private String serializedData;
	private java.util.Date dateCreated;
	private int creator;
	private java.util.Date dateChanged;
	private int changedBy;
	private byte retired;
	private java.util.Date dateRetired;
	private int retiredBy;
	private String retireReason;
	private String uuid;
 
	public SerializedObjectVO() { 
		this.metadata = false;
	} 
 
	public void setSerializedObjectId(int serializedObjectId){ 
	 	this.serializedObjectId = serializedObjectId;
	}
 
	public int getSerializedObjectId(){ 
		return this.serializedObjectId;
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
 
	public void setType(String type){ 
	 	this.type = type;
	}
 
	public String getType(){ 
		return this.type;
	}
 
	public void setSubtype(String subtype){ 
	 	this.subtype = subtype;
	}
 
	public String getSubtype(){ 
		return this.subtype;
	}
 
	public void setSerializationClass(String serializationClass){ 
	 	this.serializationClass = serializationClass;
	}
 
	public String getSerializationClass(){ 
		return this.serializationClass;
	}
 
	public void setSerializedData(String serializedData){ 
	 	this.serializedData = serializedData;
	}
 
	public String getSerializedData(){ 
		return this.serializedData;
	}
 
	public void setDateCreated(java.util.Date dateCreated){ 
	 	this.dateCreated = dateCreated;
	}
 
	public java.util.Date getDateCreated(){ 
		return this.dateCreated;
	}
 
	public void setCreator(int creator){ 
	 	this.creator = creator;
	}
 
	public int getCreator(){ 
		return this.creator;
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
 
	public void setRetired(byte retired){ 
	 	this.retired = retired;
	}
 
	public byte getRetired(){ 
		return this.retired;
	}
 
	public void setDateRetired(java.util.Date dateRetired){ 
	 	this.dateRetired = dateRetired;
	}
 
	public java.util.Date getDateRetired(){ 
		return this.dateRetired;
	}
 
	public void setRetiredBy(int retiredBy){ 
	 	this.retiredBy = retiredBy;
	}
 
	public int getRetiredBy(){ 
		return this.retiredBy;
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
 		return this.serializedObjectId; 
	} 
 
	public void setObjectId(int selfId){ 
		this.serializedObjectId = selfId; 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		this.serializedObjectId = rs.getInt("serialized_object_id");
		this.name = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("name") != null ? rs.getString("name").trim() : null);
		this.description = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("description") != null ? rs.getString("description").trim() : null);
		this.type = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("type") != null ? rs.getString("type").trim() : null);
		this.subtype = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("subtype") != null ? rs.getString("subtype").trim() : null);
		this.serializationClass = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("serialization_class") != null ? rs.getString("serialization_class").trim() : null);
		this.serializedData = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("serialized_data") != null ? rs.getString("serialized_data").trim() : null);
		this.dateCreated =  rs.getTimestamp("date_created") != null ? new java.util.Date( rs.getTimestamp("date_created").getTime() ) : null;
		this.creator = rs.getInt("creator");
		this.dateChanged =  rs.getTimestamp("date_changed") != null ? new java.util.Date( rs.getTimestamp("date_changed").getTime() ) : null;
		this.changedBy = rs.getInt("changed_by");
		this.retired = rs.getByte("retired");
		this.dateRetired =  rs.getTimestamp("date_retired") != null ? new java.util.Date( rs.getTimestamp("date_retired").getTime() ) : null;
		this.retiredBy = rs.getInt("retired_by");
		this.retireReason = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("retire_reason") != null ? rs.getString("retire_reason").trim() : null);
		this.uuid = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("uuid") != null ? rs.getString("uuid").trim() : null);
	} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "serialized_object_id"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParams(){ 
 		Object[] params = {this.name, this.description, this.type, this.subtype, this.serializationClass, this.serializedData, this.dateCreated, this.creator == 0 ? null : this.creator, this.dateChanged, this.changedBy == 0 ? null : this.changedBy, this.retired, this.dateRetired, this.retiredBy == 0 ? null : this.retiredBy, this.retireReason, this.uuid};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.name, this.description, this.type, this.subtype, this.serializationClass, this.serializedData, this.dateCreated, this.creator == 0 ? null : this.creator, this.dateChanged, this.changedBy == 0 ? null : this.changedBy, this.retired, this.dateRetired, this.retiredBy == 0 ? null : this.retiredBy, this.retireReason, this.uuid, this.serializedObjectId};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQL(){ 
 		return "INSERT INTO serialized_object(name, description, type, subtype, serialization_class, serialized_data, date_created, creator, date_changed, changed_by, retired, date_retired, retired_by, retire_reason, uuid) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE serialized_object SET name = ?, description = ?, type = ?, subtype = ?, serialization_class = ?, serialized_data = ?, date_created = ?, creator = ?, date_changed = ?, changed_by = ?, retired = ?, date_retired = ?, retired_by = ?, retire_reason = ?, uuid = ? WHERE serialized_object_id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.name != null ? "\""+ utilities.scapeQuotationMarks(name)  +"\"" : null) + "," + (this.description != null ? "\""+ utilities.scapeQuotationMarks(description)  +"\"" : null) + "," + (this.type != null ? "\""+ utilities.scapeQuotationMarks(type)  +"\"" : null) + "," + (this.subtype != null ? "\""+ utilities.scapeQuotationMarks(subtype)  +"\"" : null) + "," + (this.serializationClass != null ? "\""+ utilities.scapeQuotationMarks(serializationClass)  +"\"" : null) + "," + (this.serializedData != null ? "\""+ utilities.scapeQuotationMarks(serializedData)  +"\"" : null) + "," + (this.dateCreated != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateCreated)  +"\"" : null) + "," + (this.creator == 0 ? null : this.creator) + "," + (this.dateChanged != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateChanged)  +"\"" : null) + "," + (this.changedBy == 0 ? null : this.changedBy) + "," + (this.retired) + "," + (this.dateRetired != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateRetired)  +"\"" : null) + "," + (this.retiredBy == 0 ? null : this.retiredBy) + "," + (this.retireReason != null ? "\""+ utilities.scapeQuotationMarks(retireReason)  +"\"" : null) + "," + (this.uuid != null ? "\""+ utilities.scapeQuotationMarks(uuid)  +"\"" : null); 
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
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.destinationpkg.UsersVO.class, this.retiredBy, true, conn); 
		this.retiredBy = 0;
		if (parentOnDestination  != null) this.retiredBy = parentOnDestination.getObjectId();
 
	}

	@Override
	public int getParentValue(String parentAttName) {		
		if (parentAttName.equals("changedBy")) return this.changedBy;		
		if (parentAttName.equals("creator")) return this.creator;		
		if (parentAttName.equals("retiredBy")) return this.retiredBy;

		throw new RuntimeException("No found parent for: " + parentAttName);	}


}