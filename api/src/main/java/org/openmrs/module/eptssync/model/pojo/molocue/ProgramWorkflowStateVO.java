package org.openmrs.module.eptssync.model.pojo.molocue; 
 
import org.openmrs.module.eptssync.model.pojo.generic.*; 
 
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.eptssync.utilities.AttDefinedElements; 
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
	private byte retired;
	private int changedBy;
 
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
		this.uuid = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("uuid") != null ? rs.getString("uuid").trim() : null);
	} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "program_workflow_state_id"; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithoutObjectId(){ 
 		return "INSERT INTO program_workflow_state(program_workflow_state_id, program_workflow_id, concept_id, initial, terminal, creator, date_created, retired, changed_by, date_changed, uuid) VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithoutObjectId(){ 
 		Object[] params = {this.programWorkflowStateId, this.programWorkflowId == 0 ? null : this.programWorkflowId, this.conceptId == 0 ? null : this.conceptId, this.initial, this.terminal, this.creator == 0 ? null : this.creator, this.dateCreated, this.retired, this.changedBy == 0 ? null : this.changedBy, this.dateChanged, this.uuid};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithObjectId(){ 
 		return "INSERT INTO program_workflow_state(program_workflow_state_id, program_workflow_state_id, program_workflow_id, concept_id, initial, terminal, creator, date_created, retired, changed_by, date_changed, uuid) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithObjectId(){ 
 		Object[] params = {this.programWorkflowStateId, this.programWorkflowStateId, this.programWorkflowId == 0 ? null : this.programWorkflowId, this.conceptId == 0 ? null : this.conceptId, this.initial, this.terminal, this.creator == 0 ? null : this.creator, this.dateCreated, this.retired, this.changedBy == 0 ? null : this.changedBy, this.dateChanged, this.uuid};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.programWorkflowStateId, this.programWorkflowId == 0 ? null : this.programWorkflowId, this.conceptId == 0 ? null : this.conceptId, this.initial, this.terminal, this.creator == 0 ? null : this.creator, this.dateCreated, this.retired, this.changedBy == 0 ? null : this.changedBy, this.dateChanged, this.uuid, this.programWorkflowStateId};		return params; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE program_workflow_state SET program_workflow_state_id = ?, program_workflow_id = ?, concept_id = ?, initial = ?, terminal = ?, creator = ?, date_created = ?, retired = ?, changed_by = ?, date_changed = ?, uuid = ? WHERE program_workflow_state_id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.programWorkflowStateId) + "," + (this.programWorkflowId == 0 ? null : this.programWorkflowId) + "," + (this.conceptId == 0 ? null : this.conceptId) + "," + (this.initial) + "," + (this.terminal) + "," + (this.creator == 0 ? null : this.creator) + "," + (this.dateCreated != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateCreated)  +"\"" : null) + "," + (this.retired) + "," + (this.changedBy == 0 ? null : this.changedBy) + "," + (this.dateChanged != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateChanged)  +"\"" : null) + "," + (this.uuid != null ? "\""+ utilities.scapeQuotationMarks(uuid)  +"\"" : null); 
	} 
 
	@Override
	public boolean hasParents() {
		if (this.changedBy != 0) return true;

		if (this.conceptId != 0) return true;

		if (this.creator != 0) return true;

		if (this.programWorkflowId != 0) return true;

		return false;
	}

	@Override
	public int getParentValue(String parentAttName) {		
		if (parentAttName.equals("changedBy")) return this.changedBy;		
		if (parentAttName.equals("conceptId")) return this.conceptId;		
		if (parentAttName.equals("creator")) return this.creator;		
		if (parentAttName.equals("programWorkflowId")) return this.programWorkflowId;

		throw new RuntimeException("No found parent for: " + parentAttName);	}

	@Override
	public void changeParentValue(String parentAttName, OpenMRSObject newParent) {		
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
		if (parentAttName.equals("programWorkflowId")) {
			this.programWorkflowId = newParent.getObjectId();
			return;
		}

		throw new RuntimeException("No found parent for: " + parentAttName);
	}


}