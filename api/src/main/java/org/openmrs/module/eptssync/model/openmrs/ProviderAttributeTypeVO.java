package org.openmrs.module.eptssync.model.openmrs; 
 
import org.openmrs.module.eptssync.model.openmrs.generic.*; 
 
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.eptssync.utilities.db.conn.DBException; 
import org.openmrs.module.eptssync.exceptions.ParentNotYetMigratedException; 
 
import java.sql.Connection; 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class ProviderAttributeTypeVO extends AbstractOpenMRSObject implements OpenMRSObject { 
	private int providerAttributeTypeId;
	private String name;
	private String description;
	private String datatype;
	private String datatypeConfig;
	private String preferredHandler;
	private String handlerConfig;
	private int minOccurs;
	private int maxOccurs;
	private int creator;
	private java.util.Date dateCreated;
	private int changedBy;
	private java.util.Date dateChanged;
	private byte retired;
	private int retiredBy;
	private java.util.Date dateRetired;
	private String retireReason;
	private String uuid;
	private java.util.Date lastSyncDate;
	private int originRecordId;
	private String originAppLocationCode;
	private int consistent;
 
	public ProviderAttributeTypeVO() { 
		this.metadata = true;
	} 
 
	public void setProviderAttributeTypeId(int providerAttributeTypeId){ 
	 	this.providerAttributeTypeId = providerAttributeTypeId;
	}
 
	public int getProviderAttributeTypeId(){ 
		return this.providerAttributeTypeId;
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
 
	public void setDatatype(String datatype){ 
	 	this.datatype = datatype;
	}
 
	public String getDatatype(){ 
		return this.datatype;
	}
 
	public void setDatatypeConfig(String datatypeConfig){ 
	 	this.datatypeConfig = datatypeConfig;
	}
 
	public String getDatatypeConfig(){ 
		return this.datatypeConfig;
	}
 
	public void setPreferredHandler(String preferredHandler){ 
	 	this.preferredHandler = preferredHandler;
	}
 
	public String getPreferredHandler(){ 
		return this.preferredHandler;
	}
 
	public void setHandlerConfig(String handlerConfig){ 
	 	this.handlerConfig = handlerConfig;
	}
 
	public String getHandlerConfig(){ 
		return this.handlerConfig;
	}
 
	public void setMinOccurs(int minOccurs){ 
	 	this.minOccurs = minOccurs;
	}
 
	public int getMinOccurs(){ 
		return this.minOccurs;
	}
 
	public void setMaxOccurs(int maxOccurs){ 
	 	this.maxOccurs = maxOccurs;
	}
 
	public int getMaxOccurs(){ 
		return this.maxOccurs;
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
	}
 
	public void setUuid(String uuid){ 
	 	this.uuid = uuid;
	}
 
	public String getUuid(){ 
		return this.uuid;
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
 		return this.providerAttributeTypeId; 
	} 
 
	public void setObjectId(int selfId){ 
		this.providerAttributeTypeId = selfId; 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		this.providerAttributeTypeId = rs.getInt("provider_attribute_type_id");
		this.name = rs.getString("name") != null ? rs.getString("name").trim() : null;
		this.description = rs.getString("description") != null ? rs.getString("description").trim() : null;
		this.datatype = rs.getString("datatype") != null ? rs.getString("datatype").trim() : null;
		this.datatypeConfig = rs.getString("datatype_config") != null ? rs.getString("datatype_config").trim() : null;
		this.preferredHandler = rs.getString("preferred_handler") != null ? rs.getString("preferred_handler").trim() : null;
		this.handlerConfig = rs.getString("handler_config") != null ? rs.getString("handler_config").trim() : null;
		this.minOccurs = rs.getInt("min_occurs");
		this.maxOccurs = rs.getInt("max_occurs");
		this.creator = rs.getInt("creator");
		this.dateCreated =  rs.getTimestamp("date_created") != null ? new java.util.Date( rs.getTimestamp("date_created").getTime() ) : null;
		this.changedBy = rs.getInt("changed_by");
		this.dateChanged =  rs.getTimestamp("date_changed") != null ? new java.util.Date( rs.getTimestamp("date_changed").getTime() ) : null;
		this.retired = rs.getByte("retired");
		this.retiredBy = rs.getInt("retired_by");
		this.dateRetired =  rs.getTimestamp("date_retired") != null ? new java.util.Date( rs.getTimestamp("date_retired").getTime() ) : null;
		this.retireReason = rs.getString("retire_reason") != null ? rs.getString("retire_reason").trim() : null;
		this.uuid = rs.getString("uuid") != null ? rs.getString("uuid").trim() : null;
		this.lastSyncDate =  rs.getTimestamp("last_sync_date") != null ? new java.util.Date( rs.getTimestamp("last_sync_date").getTime() ) : null;
		this.originRecordId = rs.getInt("origin_record_id");
		this.originAppLocationCode = rs.getString("origin_app_location_code") != null ? rs.getString("origin_app_location_code").trim() : null;
			} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "provider_attribute_type_id"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParams(){ 
 		Object[] params = {this.name, this.description, this.datatype, this.datatypeConfig, this.preferredHandler, this.handlerConfig, this.minOccurs, this.maxOccurs, this.creator == 0 ? null : this.creator, this.dateCreated, this.changedBy == 0 ? null : this.changedBy, this.dateChanged, this.retired, this.retiredBy == 0 ? null : this.retiredBy, this.dateRetired, this.retireReason, this.uuid, this.lastSyncDate, this.originRecordId, this.originAppLocationCode, this.consistent};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.name, this.description, this.datatype, this.datatypeConfig, this.preferredHandler, this.handlerConfig, this.minOccurs, this.maxOccurs, this.creator == 0 ? null : this.creator, this.dateCreated, this.changedBy == 0 ? null : this.changedBy, this.dateChanged, this.retired, this.retiredBy == 0 ? null : this.retiredBy, this.dateRetired, this.retireReason, this.uuid, this.lastSyncDate, this.originRecordId, this.originAppLocationCode, this.consistent, this.providerAttributeTypeId};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQL(){ 
 		return "INSERT INTO provider_attribute_type(name, description, datatype, datatype_config, preferred_handler, handler_config, min_occurs, max_occurs, creator, date_created, changed_by, date_changed, retired, retired_by, date_retired, retire_reason, uuid, last_sync_date, origin_record_id, origin_app_location_code, consistent) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE provider_attribute_type SET name = ?, description = ?, datatype = ?, datatype_config = ?, preferred_handler = ?, handler_config = ?, min_occurs = ?, max_occurs = ?, creator = ?, date_created = ?, changed_by = ?, date_changed = ?, retired = ?, retired_by = ?, date_retired = ?, retire_reason = ?, uuid = ?, last_sync_date = ?, origin_record_id = ?, origin_app_location_code = ?, consistent = ? WHERE provider_attribute_type_id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return (this.name != null ? "\""+name+"\"" : null) + "," + (this.description != null ? "\""+description+"\"" : null) + "," + (this.datatype != null ? "\""+datatype+"\"" : null) + "," + (this.datatypeConfig != null ? "\""+datatypeConfig+"\"" : null) + "," + (this.preferredHandler != null ? "\""+preferredHandler+"\"" : null) + "," + (this.handlerConfig != null ? "\""+handlerConfig+"\"" : null) + "," + (this.minOccurs) + "," + (this.maxOccurs) + "," + (this.dateCreated != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateCreated)  +"\"" : null) + "," + (this.dateChanged != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateChanged)  +"\"" : null) + "," + (this.retired) + "," + (this.dateRetired != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateRetired)  +"\"" : null) + "," + (this.retireReason != null ? "\""+retireReason+"\"" : null) + "," + (this.uuid != null ? "\""+uuid+"\"" : null) + "," + (this.lastSyncDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(lastSyncDate)  +"\"" : null) + "," + (this.originRecordId) + "," + (this.originAppLocationCode != null ? "\""+originAppLocationCode+"\"" : null) + "," + (this.consistent); 
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
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.UsersVO.class, this.changedBy, true, conn); 
		this.changedBy = 0;
		if (parentOnDestination  != null) this.changedBy = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.UsersVO.class, this.creator, false, conn); 
		this.creator = 0;
		if (parentOnDestination  != null) this.creator = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.UsersVO.class, this.retiredBy, true, conn); 
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