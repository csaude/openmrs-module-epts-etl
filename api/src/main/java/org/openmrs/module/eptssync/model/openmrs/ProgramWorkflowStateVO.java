package org.openmrs.module.eptssync.model.openmrs; 
 
import org.openmrs.module.eptssync.model.GenericSyncRecordDAO;
import org.openmrs.module.eptssync.model.openmrs.generic.AbstractOpenMRSObject;
import org.openmrs.module.eptssync.model.openmrs.generic.OpenMRSObject;
import org.openmrs.module.eptssync.utilities.db.conn.DBException; 
import org.openmrs.module.eptssync.utilities.db.conn.OpenConnection; 
import org.openmrs.module.eptssync.exceptions.ParentNotYetMigratedException; 
 
import java.sql.Connection; 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class ProgramWorkflowStateVO extends AbstractOpenMRSObject implements OpenMRSObject { 
	private int programWorkflowStateId;
	private int programWorkflowId;
	private int conceptId;
	private byte initial;
	private byte terminal;
	private int creator;
	private java.util.Date dateCreated;
	private byte retired;
	private int changedBy;
	private java.util.Date dateChanged;
	private String uuid;
	private java.util.Date lastSyncDate;
	private int originRecordId;
	private String originAppLocationCode;
 
	public ProgramWorkflowStateVO() { 
		this.metadata = true;
	} 
 
	public void setProgramWorkflowStateId(int programWorkflowStateId){ 
	 	this.programWorkflowStateId = programWorkflowStateId;
	}
 
	public int getProgramWorkflowStateId(){ 
		return this.programWorkflowStateId;
	}
 
	public void setProgramWorkflowId(int programWorkflowId){ 
	 	this.programWorkflowId = programWorkflowId;
	}
 
	public int getProgramWorkflowId(){ 
		return this.programWorkflowId;
	}
 
	public void setConceptId(int conceptId){ 
	 	this.conceptId = conceptId;
	}
 
	public int getConceptId(){ 
		return this.conceptId;
	}
 
	public void setInitial(byte initial){ 
	 	this.initial = initial;
	}
 
	public byte getInitial(){ 
		return this.initial;
	}
 
	public void setTerminal(byte terminal){ 
	 	this.terminal = terminal;
	}
 
	public byte getTerminal(){ 
		return this.terminal;
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
 		return this.programWorkflowStateId; 
	} 
 
	public void setObjectId(int selfId){ 
		this.programWorkflowStateId = selfId; 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		this.programWorkflowStateId = rs.getInt("program_workflow_state_id");
		this.programWorkflowId = rs.getInt("program_workflow_id");
		this.conceptId = rs.getInt("concept_id");
		this.initial = rs.getByte("initial");
		this.terminal = rs.getByte("terminal");
		this.creator = rs.getInt("creator");
		this.dateCreated =  rs.getTimestamp("date_created") != null ? new java.util.Date( rs.getTimestamp("date_created").getTime() ) : null;
		this.retired = rs.getByte("retired");
		this.changedBy = rs.getInt("changed_by");
		this.dateChanged =  rs.getTimestamp("date_changed") != null ? new java.util.Date( rs.getTimestamp("date_changed").getTime() ) : null;
		this.uuid = rs.getString("uuid") != null ? rs.getString("uuid").trim() : null;
		this.lastSyncDate =  rs.getTimestamp("last_sync_date") != null ? new java.util.Date( rs.getTimestamp("last_sync_date").getTime() ) : null;
		this.originRecordId = rs.getInt("origin_record_id");
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
 		return "program_workflow_state_id"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParams(){ 
 		Object[] params = {this.programWorkflowId == 0 ? null : this.programWorkflowId, this.conceptId == 0 ? null : this.conceptId, this.initial, this.terminal, this.creator == 0 ? null : this.creator, this.dateCreated, this.retired, this.changedBy == 0 ? null : this.changedBy, this.dateChanged, this.uuid, this.lastSyncDate, this.originRecordId, this.originAppLocationCode};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.programWorkflowId == 0 ? null : this.programWorkflowId, this.conceptId == 0 ? null : this.conceptId, this.initial, this.terminal, this.creator == 0 ? null : this.creator, this.dateCreated, this.retired, this.changedBy == 0 ? null : this.changedBy, this.dateChanged, this.uuid, this.lastSyncDate, this.originRecordId, this.originAppLocationCode, this.programWorkflowStateId};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQL(){ 
 		return "INSERT INTO program_workflow_state(program_workflow_id, concept_id, initial, terminal, creator, date_created, retired, changed_by, date_changed, uuid, last_sync_date, origin_record_id, origin_app_location_code) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE program_workflow_state SET program_workflow_id = ?, concept_id = ?, initial = ?, terminal = ?, creator = ?, date_created = ?, retired = ?, changed_by = ?, date_changed = ?, uuid = ?, last_sync_date = ?, origin_record_id = ?, origin_app_location_code = ? WHERE program_workflow_state_id = ?;"; 
	} 
 
	public void loadDestParentInfo(Connection conn) throws ParentNotYetMigratedException, DBException {
		OpenMRSObject parentOnDestination = null;
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.UsersVO.class, this.changedBy, true, conn); 
		this.changedBy = 0;
		if (parentOnDestination  != null) this.changedBy = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.ConceptVO.class, this.conceptId, false, conn); 
		this.conceptId = 0;
		if (parentOnDestination  != null) this.conceptId = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.UsersVO.class, this.creator, false, conn); 
		this.creator = 0;
		if (parentOnDestination  != null) this.creator = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.ProgramWorkflowVO.class, this.programWorkflowId, false, conn); 
		this.programWorkflowId = 0;
		if (parentOnDestination  != null) this.programWorkflowId = parentOnDestination.getObjectId();
 
	}
}