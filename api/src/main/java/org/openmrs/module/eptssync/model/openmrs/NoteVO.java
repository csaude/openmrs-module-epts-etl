package org.openmrs.module.eptssync.model.openmrs; 
 
import org.openmrs.module.eptssync.model.GenericSyncRecordDAO; 
 
import org.openmrs.module.eptssync.model.base.BaseVO; 
 
import org.openmrs.module.eptssync.utilities.db.conn.DBException; 
import org.openmrs.module.eptssync.utilities.db.conn.OpenConnection; 
import org.openmrs.module.eptssync.exceptions.ParentNotYetMigratedException; 
 
import java.sql.Connection; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class NoteVO extends AbstractOpenMRSObject implements OpenMRSObject { 
	private int noteId;
	private String noteType;
	private int patientId;
	private int obsId;
	private int encounterId;
	private String text;
	private int priority;
	private int parent;
	private int creator;
	private java.util.Date dateCreated;
	private int changedBy;
	private java.util.Date dateChanged;
	private String uuid;
	private java.util.Date lastSyncDate;
	private int originRecordId;
	private String originAppLocationCode;
 
	public NoteVO() { 
	} 
 
	public void setNoteId(int noteId){ 
	 	this.noteId = noteId;
	}
 
	public int getNoteId(){ 
		return this.noteId;
	}	public void setNoteType(String noteType){ 
	 	this.noteType = noteType;
	}
 
	public String getNoteType(){ 
		return this.noteType;
	}	public void setPatientId(int patientId){ 
	 	this.patientId = patientId;
	}
 
	public int getPatientId(){ 
		return this.patientId;
	}	public void setObsId(int obsId){ 
	 	this.obsId = obsId;
	}
 
	public int getObsId(){ 
		return this.obsId;
	}	public void setEncounterId(int encounterId){ 
	 	this.encounterId = encounterId;
	}
 
	public int getEncounterId(){ 
		return this.encounterId;
	}	public void setText(String text){ 
	 	this.text = text;
	}
 
	public String getText(){ 
		return this.text;
	}	public void setPriority(int priority){ 
	 	this.priority = priority;
	}
 
	public int getPriority(){ 
		return this.priority;
	}	public void setParent(int parent){ 
	 	this.parent = parent;
	}
 
	public int getParent(){ 
		return this.parent;
	}	public void setCreator(int creator){ 
	 	this.creator = creator;
	}
 
	public int getCreator(){ 
		return this.creator;
	}	public void setDateCreated(java.util.Date dateCreated){ 
	 	this.dateCreated = dateCreated;
	}
 
	public java.util.Date getDateCreated(){ 
		return this.dateCreated;
	}	public void setChangedBy(int changedBy){ 
	 	this.changedBy = changedBy;
	}
 
	public int getChangedBy(){ 
		return this.changedBy;
	}	public void setDateChanged(java.util.Date dateChanged){ 
	 	this.dateChanged = dateChanged;
	}
 
	public java.util.Date getDateChanged(){ 
		return this.dateChanged;
	}	public void setUuid(String uuid){ 
	 	this.uuid = uuid;
	}
 
	public String getUuid(){ 
		return this.uuid;
	}	public void setLastSyncDate(java.util.Date lastSyncDate){ 
	 	this.lastSyncDate = lastSyncDate;
	}
 
	public java.util.Date getLastSyncDate(){ 
		return this.lastSyncDate;
	}	public void setOriginRecordId(int originRecordId){ 
	 	this.originRecordId = originRecordId;
	}
 
	public int getOriginRecordId(){ 
		return this.originRecordId;
	}	public void setOriginAppLocationCode(String originAppLocationCode){ 
	 	this.originAppLocationCode = originAppLocationCode;
	}


 
	public String getOriginAppLocationCode(){ 
		return this.originAppLocationCode;
	}
 
	public int getObjectId() { 
 		return this.noteId; 
	} 
 
	public void setObjectId(int selfId){ 
		this.noteId = selfId; 
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
 		return "note_id"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParams(){ 
 		Object[] params = {this.noteType, this.patientId == 0 ? null : this.patientId, this.obsId == 0 ? null : this.obsId, this.encounterId == 0 ? null : this.encounterId, this.text, this.priority, this.parent == 0 ? null : this.parent, this.creator == 0 ? null : this.creator, this.dateCreated, this.changedBy == 0 ? null : this.changedBy, this.dateChanged, this.uuid, this.lastSyncDate, this.originRecordId, this.originAppLocationCode};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.noteType, this.patientId == 0 ? null : this.patientId, this.obsId == 0 ? null : this.obsId, this.encounterId == 0 ? null : this.encounterId, this.text, this.priority, this.parent == 0 ? null : this.parent, this.creator == 0 ? null : this.creator, this.dateCreated, this.changedBy == 0 ? null : this.changedBy, this.dateChanged, this.uuid, this.lastSyncDate, this.originRecordId, this.originAppLocationCode, this.noteId};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQL(){ 
 		return "INSERT INTO note(note_type, patient_id, obs_id, encounter_id, text, priority, parent, creator, date_created, changed_by, date_changed, uuid, last_sync_date, origin_record_id, origin_app_location_code) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE note SET note_type = ?, patient_id = ?, obs_id = ?, encounter_id = ?, text = ?, priority = ?, parent = ?, creator = ?, date_created = ?, changed_by = ?, date_changed = ?, uuid = ?, last_sync_date = ?, origin_record_id = ?, origin_app_location_code = ? WHERE note_id = ?;"; 
	} 
 
	@JsonIgnore
	public int getMainParentId(){ 
 		return obsId; 
	} 
 
	public void setMainParentId(int mainParentId){ 
 		this.obsId = mainParentId; 
	} 
 
	@JsonIgnore
	public String getMainParentTable(){ 
 		return "obs";
	} 
 
	public void loadDestParentInfo(Connection conn) throws ParentNotYetMigratedException, DBException {
		OpenMRSObject parentOnDestination = null;
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.ObsVO.class, this.obsId,true, conn); 
		if (parentOnDestination  != null) this.obsId = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.PatientVO.class, this.patientId,true, conn); 
		if (parentOnDestination  != null) this.patientId = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.EncounterVO.class, this.encounterId,true, conn); 
		if (parentOnDestination  != null) this.encounterId = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.NoteVO.class, this.parent,true, conn); 
		if (parentOnDestination  != null) this.parent = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.UsersVO.class, this.creator,false, conn); 
		if (parentOnDestination  != null) this.creator = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.UsersVO.class, this.changedBy,true, conn); 
		if (parentOnDestination  != null) this.changedBy = parentOnDestination.getObjectId();
 
	}
}