package org.openmrs.module.eptssync.model.openmrs; 
 
import org.openmrs.module.eptssync.model.GenericSyncRecordDAO; 
 
import org.openmrs.module.eptssync.utilities.db.conn.DBException; 
import org.openmrs.module.eptssync.utilities.db.conn.OpenConnection; 
import org.openmrs.module.eptssync.exceptions.ParentNotYetMigratedException; 
 
import java.sql.Connection; 
 
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
 
	public int getObjectId() { 
 		return this.providerAttributeTypeId; 
	} 
 
	public void setObjectId(int selfId){ 
		this.providerAttributeTypeId = selfId; 
	} 
 
	public void refreshLastSyncDate(OpenConnection conn){ 
		try{
			GenericSyncRecordDAO.refreshLastSyncDate(this, conn); 
		}catch(DBException e) {
			throw new RuntimeException(e);
		}
	}

	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "provider_attribute_type_id"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParams(){ 
 		Object[] params = {this.name, this.description, this.datatype, this.datatypeConfig, this.preferredHandler, this.handlerConfig, this.minOccurs, this.maxOccurs, this.creator == 0 ? null : this.creator, this.dateCreated, this.changedBy == 0 ? null : this.changedBy, this.dateChanged, this.retired, this.retiredBy == 0 ? null : this.retiredBy, this.dateRetired, this.retireReason, this.uuid, this.lastSyncDate, this.originRecordId, this.originAppLocationCode};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.name, this.description, this.datatype, this.datatypeConfig, this.preferredHandler, this.handlerConfig, this.minOccurs, this.maxOccurs, this.creator == 0 ? null : this.creator, this.dateCreated, this.changedBy == 0 ? null : this.changedBy, this.dateChanged, this.retired, this.retiredBy == 0 ? null : this.retiredBy, this.dateRetired, this.retireReason, this.uuid, this.lastSyncDate, this.originRecordId, this.originAppLocationCode, this.providerAttributeTypeId};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQL(){ 
 		return "INSERT INTO provider_attribute_type(name, description, datatype, datatype_config, preferred_handler, handler_config, min_occurs, max_occurs, creator, date_created, changed_by, date_changed, retired, retired_by, date_retired, retire_reason, uuid, last_sync_date, origin_record_id, origin_app_location_code) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE provider_attribute_type SET name = ?, description = ?, datatype = ?, datatype_config = ?, preferred_handler = ?, handler_config = ?, min_occurs = ?, max_occurs = ?, creator = ?, date_created = ?, changed_by = ?, date_changed = ?, retired = ?, retired_by = ?, date_retired = ?, retire_reason = ?, uuid = ?, last_sync_date = ?, origin_record_id = ?, origin_app_location_code = ? WHERE provider_attribute_type_id = ?;"; 
	} 
 
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
}