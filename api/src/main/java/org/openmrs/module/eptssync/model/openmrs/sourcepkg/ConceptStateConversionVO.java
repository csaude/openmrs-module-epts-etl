package org.openmrs.module.eptssync.model.openmrs.sourcepkg; 
 
import org.openmrs.module.eptssync.model.openmrs.generic.*; 
 
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.eptssync.utilities.db.conn.DBException; 
import org.openmrs.module.eptssync.exceptions.ParentNotYetMigratedException; 
 
import java.sql.Connection; 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class ConceptStateConversionVO extends AbstractOpenMRSObject implements OpenMRSObject { 
	private int conceptStateConversionId;
	private int conceptId;
	private int programWorkflowId;
	private int programWorkflowStateId;
	private String uuid;
 
	public ConceptStateConversionVO() { 
		this.metadata = false;
	} 
 
	public void setConceptStateConversionId(int conceptStateConversionId){ 
	 	this.conceptStateConversionId = conceptStateConversionId;
	}
 
	public int getConceptStateConversionId(){ 
		return this.conceptStateConversionId;
	}
 
	public void setConceptId(int conceptId){ 
	 	this.conceptId = conceptId;
	}
 
	public int getConceptId(){ 
		return this.conceptId;
	}
 
	public void setProgramWorkflowId(int programWorkflowId){ 
	 	this.programWorkflowId = programWorkflowId;
	}
 
	public int getProgramWorkflowId(){ 
		return this.programWorkflowId;
	}
 
	public void setProgramWorkflowStateId(int programWorkflowStateId){ 
	 	this.programWorkflowStateId = programWorkflowStateId;
	}
 
	public int getProgramWorkflowStateId(){ 
		return this.programWorkflowStateId;
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
 		return this.conceptStateConversionId; 
	} 
 
	public void setObjectId(int selfId){ 
		this.conceptStateConversionId = selfId; 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		this.conceptStateConversionId = rs.getInt("concept_state_conversion_id");
		this.conceptId = rs.getInt("concept_id");
		this.programWorkflowId = rs.getInt("program_workflow_id");
		this.programWorkflowStateId = rs.getInt("program_workflow_state_id");
			} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "concept_state_conversion_id"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParams(){ 
 		Object[] params = {this.conceptId == 0 ? null : this.conceptId, this.programWorkflowId == 0 ? null : this.programWorkflowId, this.programWorkflowStateId == 0 ? null : this.programWorkflowStateId, this.uuid};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.conceptId == 0 ? null : this.conceptId, this.programWorkflowId == 0 ? null : this.programWorkflowId, this.programWorkflowStateId == 0 ? null : this.programWorkflowStateId, this.uuid, this.conceptStateConversionId};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQL(){ 
 		return "INSERT INTO concept_state_conversion(concept_id, program_workflow_id, program_workflow_state_id, uuid) VALUES(?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE concept_state_conversion SET concept_id = ?, program_workflow_id = ?, program_workflow_state_id = ?, uuid = ? WHERE concept_state_conversion_id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.conceptId == 0 ? null : this.conceptId) + "," + (this.programWorkflowId == 0 ? null : this.programWorkflowId) + "," + (this.programWorkflowStateId == 0 ? null : this.programWorkflowStateId) + "," + (this.uuid != null ? "\""+uuid+"\"" : null); 
	} 
 
	@Override
	public boolean isGeneratedFromSkeletonClass() {
		return false;
	}

	@Override
	public boolean hasParents() {
		if (this.conceptId != 0) return true;
		if (this.programWorkflowId != 0) return true;
		if (this.programWorkflowStateId != 0) return true;
		return false;
	}

	@Override
	public int retrieveSharedPKKey(Connection conn) throws ParentNotYetMigratedException, DBException {
		throw new RuntimeException("No PKSharedInfo defined!");	}

	@Override
	public void loadDestParentInfo(Connection conn) throws ParentNotYetMigratedException, DBException {
		OpenMRSObject parentOnDestination = null;
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.sourcepkg.ConceptVO.class, this.conceptId, true, conn); 
		this.conceptId = 0;
		if (parentOnDestination  != null) this.conceptId = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.sourcepkg.ProgramWorkflowVO.class, this.programWorkflowId, true, conn); 
		this.programWorkflowId = 0;
		if (parentOnDestination  != null) this.programWorkflowId = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.sourcepkg.ProgramWorkflowStateVO.class, this.programWorkflowStateId, true, conn); 
		this.programWorkflowStateId = 0;
		if (parentOnDestination  != null) this.programWorkflowStateId = parentOnDestination.getObjectId();
 
	}

	@Override
	public int getParentValue(String parentAttName) {		
		if (parentAttName.equals("conceptId")) return this.conceptId;		
		if (parentAttName.equals("programWorkflowId")) return this.programWorkflowId;		
		if (parentAttName.equals("programWorkflowStateId")) return this.programWorkflowStateId;

		throw new RuntimeException("No found parent for: " + parentAttName);	}


}