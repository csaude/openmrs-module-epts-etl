package org.openmrs.module.eptssync.model.pojo.source.eip_change_dectetor; 
 
import org.openmrs.module.eptssync.model.pojo.generic.*; 
 
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.eptssync.utilities.AttDefinedElements; 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class ProgramWorkflowVO extends AbstractOpenMRSObject implements OpenMRSObject { 
	private int programWorkflowId;
	private int programId;
	private int conceptId;
	private int creator;
	private byte retired;
	private int changedBy;
 
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
 

 
	public int getObjectId() { 
 		return this.programWorkflowId; 
	} 
 
	public void setObjectId(int selfId){ 
		this.programWorkflowId = selfId; 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		super.load(rs);
		this.programWorkflowId = rs.getInt("program_workflow_id");
		this.programId = rs.getInt("program_id");
		this.conceptId = rs.getInt("concept_id");
		this.creator = rs.getInt("creator");
		this.dateCreated =  rs.getTimestamp("date_created") != null ? new java.util.Date( rs.getTimestamp("date_created").getTime() ) : null;
		this.retired = rs.getByte("retired");
		this.changedBy = rs.getInt("changed_by");
		this.dateChanged =  rs.getTimestamp("date_changed") != null ? new java.util.Date( rs.getTimestamp("date_changed").getTime() ) : null;
		this.uuid = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("uuid") != null ? rs.getString("uuid").trim() : null);
	} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "program_workflow_id"; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithoutObjectId(){ 
 		return "INSERT INTO program_workflow(program_workflow_id, program_id, concept_id, creator, date_created, retired, changed_by, date_changed, uuid) VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithoutObjectId(){ 
 		Object[] params = {this.programWorkflowId, this.programId == 0 ? null : this.programId, this.conceptId == 0 ? null : this.conceptId, this.creator == 0 ? null : this.creator, this.dateCreated, this.retired, this.changedBy == 0 ? null : this.changedBy, this.dateChanged, this.uuid};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithObjectId(){ 
 		return "INSERT INTO program_workflow(program_workflow_id, program_workflow_id, program_id, concept_id, creator, date_created, retired, changed_by, date_changed, uuid) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithObjectId(){ 
 		Object[] params = {this.programWorkflowId, this.programWorkflowId, this.programId == 0 ? null : this.programId, this.conceptId == 0 ? null : this.conceptId, this.creator == 0 ? null : this.creator, this.dateCreated, this.retired, this.changedBy == 0 ? null : this.changedBy, this.dateChanged, this.uuid};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.programWorkflowId, this.programId == 0 ? null : this.programId, this.conceptId == 0 ? null : this.conceptId, this.creator == 0 ? null : this.creator, this.dateCreated, this.retired, this.changedBy == 0 ? null : this.changedBy, this.dateChanged, this.uuid, this.programWorkflowId};		return params; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE program_workflow SET program_workflow_id = ?, program_id = ?, concept_id = ?, creator = ?, date_created = ?, retired = ?, changed_by = ?, date_changed = ?, uuid = ? WHERE program_workflow_id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.programWorkflowId) + "," + (this.programId == 0 ? null : this.programId) + "," + (this.conceptId == 0 ? null : this.conceptId) + "," + (this.creator == 0 ? null : this.creator) + "," + (this.dateCreated != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateCreated)  +"\"" : null) + "," + (this.retired) + "," + (this.changedBy == 0 ? null : this.changedBy) + "," + (this.dateChanged != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateChanged)  +"\"" : null) + "," + (this.uuid != null ? "\""+ utilities.scapeQuotationMarks(uuid)  +"\"" : null); 
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
	public int getParentValue(String parentAttName) {		
		if (parentAttName.equals("programId")) return this.programId;		
		if (parentAttName.equals("changedBy")) return this.changedBy;		
		if (parentAttName.equals("conceptId")) return this.conceptId;		
		if (parentAttName.equals("creator")) return this.creator;

		throw new RuntimeException("No found parent for: " + parentAttName);	}

	@Override
	public void changeParentValue(String parentAttName, OpenMRSObject newParent) {		
		if (parentAttName.equals("programId")) {
			this.programId = newParent.getObjectId();
			return;
		}		
		if (parentAttName.equals("changedBy")) {
			this.changedBy = newParent.getObjectId();
			return;
		}		
		if (parentAttName.equals("conceptId")) {
			this.conceptId = newParent.getObjectId();
			return;
		}		
		if (parentAttName.equals("creator")) {
			this.creator = newParent.getObjectId();
			return;
		}

		throw new RuntimeException("No found parent for: " + parentAttName);
	}


}