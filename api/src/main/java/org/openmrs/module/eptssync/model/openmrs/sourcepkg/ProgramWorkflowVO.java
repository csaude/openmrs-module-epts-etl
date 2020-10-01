package org.openmrs.module.eptssync.model.openmrs.sourcepkg; 
 
import org.openmrs.module.eptssync.model.openmrs.generic.*; 
 
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.eptssync.utilities.db.conn.DBException; 
import org.openmrs.module.eptssync.exceptions.ParentNotYetMigratedException; 
 
import java.sql.Connection; 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class ProgramWorkflowVO extends AbstractOpenMRSObject implements OpenMRSObject { 
	private int programWorkflowId;
	private int programId;
	private int conceptId;
	private int creator;
	private java.util.Date dateCreated;
	private byte retired;
	private int changedBy;
	private java.util.Date dateChanged;
	private String uuid;
	private int consistent;
	private java.util.Date lastSyncDate;
	private int originRecordId;
	private String originAppLocationCode;
 
	public ProgramWorkflowVO() { 
		this.metadata = true;
	} 
 
	public void setProgramWorkflowId(int programWorkflowId){ 
	 	this.programWorkflowId = programWorkflowId;
	}
 
	public int getProgramWorkflowId(){ 
		return this.programWorkflowId;
	}
 
	public void setProgramId(int programId){ 
	 	this.programId = programId;
	}
 
	public int getProgramId(){ 
		return this.programId;
	}
 
	public void setConceptId(int conceptId){ 
	 	this.conceptId = conceptId;
	}
 
	public int getConceptId(){ 
		return this.conceptId;
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
 
	public void setConsistent(int consistent){ 
	 	this.consistent = consistent;
	}
 
	public int getConsistent(){ 
		return this.consistent;
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
 		return this.programWorkflowId; 
	} 
 
	public void setObjectId(int selfId){ 
		this.programWorkflowId = selfId; 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		this.programWorkflowId = rs.getInt("program_workflow_id");
		this.programId = rs.getInt("program_id");
		this.conceptId = rs.getInt("concept_id");
		this.creator = rs.getInt("creator");
		this.dateCreated =  rs.getTimestamp("date_created") != null ? new java.util.Date( rs.getTimestamp("date_created").getTime() ) : null;
		this.retired = rs.getByte("retired");
		this.changedBy = rs.getInt("changed_by");
		this.dateChanged =  rs.getTimestamp("date_changed") != null ? new java.util.Date( rs.getTimestamp("date_changed").getTime() ) : null;
		this.uuid = rs.getString("uuid") != null ? rs.getString("uuid").trim() : null;
		this.consistent = rs.getInt("consistent");
		this.lastSyncDate =  rs.getTimestamp("last_sync_date") != null ? new java.util.Date( rs.getTimestamp("last_sync_date").getTime() ) : null;
		this.originRecordId = rs.getInt("origin_record_id");
			} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "program_workflow_id"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParams(){ 
 		Object[] params = {this.programId == 0 ? null : this.programId, this.conceptId == 0 ? null : this.conceptId, this.creator == 0 ? null : this.creator, this.dateCreated, this.retired, this.changedBy == 0 ? null : this.changedBy, this.dateChanged, this.uuid, this.consistent, this.lastSyncDate, this.originRecordId, this.originAppLocationCode};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.programId == 0 ? null : this.programId, this.conceptId == 0 ? null : this.conceptId, this.creator == 0 ? null : this.creator, this.dateCreated, this.retired, this.changedBy == 0 ? null : this.changedBy, this.dateChanged, this.uuid, this.consistent, this.lastSyncDate, this.originRecordId, this.originAppLocationCode, this.programWorkflowId};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQL(){ 
 		return "INSERT INTO program_workflow(program_id, concept_id, creator, date_created, retired, changed_by, date_changed, uuid, consistent, last_sync_date, origin_record_id, origin_app_location_code) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE program_workflow SET program_id = ?, concept_id = ?, creator = ?, date_created = ?, retired = ?, changed_by = ?, date_changed = ?, uuid = ?, consistent = ?, last_sync_date = ?, origin_record_id = ?, origin_app_location_code = ? WHERE program_workflow_id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.programId == 0 ? null : this.programId) + "," + (this.conceptId == 0 ? null : this.conceptId) + "," + (this.creator == 0 ? null : this.creator) + "," + (this.dateCreated != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateCreated)  +"\"" : null) + "," + (this.retired) + "," + (this.changedBy == 0 ? null : this.changedBy) + "," + (this.dateChanged != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateChanged)  +"\"" : null) + "," + (this.uuid != null ? "\""+uuid+"\"" : null) + "," + (this.consistent) + "," + (this.lastSyncDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(lastSyncDate)  +"\"" : null) + "," + (this.originRecordId) + "," + (this.originAppLocationCode != null ? "\""+originAppLocationCode+"\"" : null); 
	} 
 
	@Override
	public boolean isGeneratedFromSkeletonClass() {
		return false;
	}

	@Override
	public boolean hasParents() {
		if (this.programId != 0) return true;
		if (this.changedBy != 0) return true;
		if (this.conceptId != 0) return true;
		if (this.creator != 0) return true;
		return false;
	}

	@Override
	public int retrieveSharedPKKey(Connection conn) throws ParentNotYetMigratedException, DBException {
		throw new RuntimeException("No PKSharedInfo defined!");	}

	@Override
	public void loadDestParentInfo(Connection conn) throws ParentNotYetMigratedException, DBException {
		OpenMRSObject parentOnDestination = null;
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.sourcepkg.ProgramVO.class, this.programId, false, conn); 
		this.programId = 0;
		if (parentOnDestination  != null) this.programId = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.sourcepkg.UsersVO.class, this.changedBy, true, conn); 
		this.changedBy = 0;
		if (parentOnDestination  != null) this.changedBy = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.sourcepkg.ConceptVO.class, this.conceptId, false, conn); 
		this.conceptId = 0;
		if (parentOnDestination  != null) this.conceptId = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.sourcepkg.UsersVO.class, this.creator, false, conn); 
		this.creator = 0;
		if (parentOnDestination  != null) this.creator = parentOnDestination.getObjectId();
 
	}

	@Override
	public int getParentValue(String parentAttName) {		
		if (parentAttName.equals("programId")) return this.programId;		
		if (parentAttName.equals("changedBy")) return this.changedBy;		
		if (parentAttName.equals("conceptId")) return this.conceptId;		
		if (parentAttName.equals("creator")) return this.creator;

		throw new RuntimeException("No found parent for: " + parentAttName);	}


}