package org.openmrs.module.eptssync.model.openmrs; 
 
import org.openmrs.module.eptssync.model.GenericSyncRecordDAO; 
 
import org.openmrs.module.eptssync.utilities.db.conn.DBException; 
import org.openmrs.module.eptssync.utilities.db.conn.OpenConnection; 
import org.openmrs.module.eptssync.exceptions.ParentNotYetMigratedException; 
 
import java.sql.Connection; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class ConceptVO extends AbstractOpenMRSObject implements OpenMRSObject { 
	private int conceptId;
	private byte retired;
	private String shortName;
	private String description;
	private String formText;
	private int datatypeId;
	private int classId;
	private byte isSet;
	private int creator;
	private java.util.Date dateCreated;
	private String version;
	private int changedBy;
	private java.util.Date dateChanged;
	private int retiredBy;
	private java.util.Date dateRetired;
	private String retireReason;
	private String uuid;
	private java.util.Date lastSyncDate;
	private int originRecordId;
	private String originAppLocationCode;
 
	public ConceptVO() { 
		this.metadata = true;
	} 
 
	public void setConceptId(int conceptId){ 
	 	this.conceptId = conceptId;
	}
 
	public int getConceptId(){ 
		return this.conceptId;
	}
 
	public void setRetired(byte retired){ 
	 	this.retired = retired;
	}
 
	public byte getRetired(){ 
		return this.retired;
	}
 
	public void setShortName(String shortName){ 
	 	this.shortName = shortName;
	}
 
	public String getShortName(){ 
		return this.shortName;
	}
 
	public void setDescription(String description){ 
	 	this.description = description;
	}
 
	public String getDescription(){ 
		return this.description;
	}
 
	public void setFormText(String formText){ 
	 	this.formText = formText;
	}
 
	public String getFormText(){ 
		return this.formText;
	}
 
	public void setDatatypeId(int datatypeId){ 
	 	this.datatypeId = datatypeId;
	}
 
	public int getDatatypeId(){ 
		return this.datatypeId;
	}
 
	public void setClassId(int classId){ 
	 	this.classId = classId;
	}
 
	public int getClassId(){ 
		return this.classId;
	}
 
	public void setIsSet(byte isSet){ 
	 	this.isSet = isSet;
	}
 
	public byte getIsSet(){ 
		return this.isSet;
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
 
	public void setVersion(String version){ 
	 	this.version = version;
	}
 
	public String getVersion(){ 
		return this.version;
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
 		return this.conceptId; 
	} 
 
	public void setObjectId(int selfId){ 
		this.conceptId = selfId; 
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
 		return "concept_id"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParams(){ 
 		Object[] params = {this.retired, this.shortName, this.description, this.formText, this.datatypeId == 0 ? null : this.datatypeId, this.classId == 0 ? null : this.classId, this.isSet, this.creator == 0 ? null : this.creator, this.dateCreated, this.version, this.changedBy == 0 ? null : this.changedBy, this.dateChanged, this.retiredBy == 0 ? null : this.retiredBy, this.dateRetired, this.retireReason, this.uuid, this.lastSyncDate, this.originRecordId, this.originAppLocationCode};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.retired, this.shortName, this.description, this.formText, this.datatypeId == 0 ? null : this.datatypeId, this.classId == 0 ? null : this.classId, this.isSet, this.creator == 0 ? null : this.creator, this.dateCreated, this.version, this.changedBy == 0 ? null : this.changedBy, this.dateChanged, this.retiredBy == 0 ? null : this.retiredBy, this.dateRetired, this.retireReason, this.uuid, this.lastSyncDate, this.originRecordId, this.originAppLocationCode, this.conceptId};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQL(){ 
 		return "INSERT INTO concept(retired, short_name, description, form_text, datatype_id, class_id, is_set, creator, date_created, version, changed_by, date_changed, retired_by, date_retired, retire_reason, uuid, last_sync_date, origin_record_id, origin_app_location_code) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE concept SET retired = ?, short_name = ?, description = ?, form_text = ?, datatype_id = ?, class_id = ?, is_set = ?, creator = ?, date_created = ?, version = ?, changed_by = ?, date_changed = ?, retired_by = ?, date_retired = ?, retire_reason = ?, uuid = ?, last_sync_date = ?, origin_record_id = ?, origin_app_location_code = ? WHERE concept_id = ?;"; 
	} 
 
	public void loadDestParentInfo(Connection conn) throws ParentNotYetMigratedException, DBException {
		OpenMRSObject parentOnDestination = null;
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.UsersVO.class, this.creator, false, conn); 
		this.creator = 0;
		if (parentOnDestination  != null) this.creator = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.ConceptDatatypeVO.class, this.datatypeId, false, conn); 
		this.datatypeId = 0;
		if (parentOnDestination  != null) this.datatypeId = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.UsersVO.class, this.changedBy, true, conn); 
		this.changedBy = 0;
		if (parentOnDestination  != null) this.changedBy = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.UsersVO.class, this.retiredBy, true, conn); 
		this.retiredBy = 0;
		if (parentOnDestination  != null) this.retiredBy = parentOnDestination.getObjectId();
 
	}
}