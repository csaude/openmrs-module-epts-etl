package org.openmrs.module.eptssync.model.pojo.destination; 
 
import org.openmrs.module.eptssync.model.pojo.generic.*; 
 
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.eptssync.utilities.AttDefinedElements; 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class ProgramVO extends AbstractOpenMRSObject implements OpenMRSObject { 
	private int programId;
	private int conceptId;
	private int creator;
	private int changedBy;
	private byte retired;
	private String name;
	private String description;
	private int outcomesConceptId;
 
	public ProgramVO() { 
		this.metadata = true;
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
 
	public void setOutcomesConceptId(int outcomesConceptId){ 
	 	this.outcomesConceptId = outcomesConceptId;
	}


 
	public int getOutcomesConceptId(){ 
		return this.outcomesConceptId;
	}
 
	public int getObjectId() { 
 		return this.programId; 
	} 
 
	public void setObjectId(int selfId){ 
		this.programId = selfId; 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		this.programId = rs.getInt("program_id");
		this.conceptId = rs.getInt("concept_id");
		this.creator = rs.getInt("creator");
		this.dateCreated =  rs.getTimestamp("date_created") != null ? new java.util.Date( rs.getTimestamp("date_created").getTime() ) : null;
		this.changedBy = rs.getInt("changed_by");
		this.dateChanged =  rs.getTimestamp("date_changed") != null ? new java.util.Date( rs.getTimestamp("date_changed").getTime() ) : null;
		this.retired = rs.getByte("retired");
		this.name = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("name") != null ? rs.getString("name").trim() : null);
		this.description = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("description") != null ? rs.getString("description").trim() : null);
		this.uuid = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("uuid") != null ? rs.getString("uuid").trim() : null);
		this.outcomesConceptId = rs.getInt("outcomes_concept_id");
	} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "program_id"; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithoutObjectId(){ 
 		return "INSERT INTO program(program_id, concept_id, creator, date_created, changed_by, date_changed, retired, name, description, uuid, outcomes_concept_id) VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithoutObjectId(){ 
 		Object[] params = {this.programId, this.conceptId == 0 ? null : this.conceptId, this.creator == 0 ? null : this.creator, this.dateCreated, this.changedBy == 0 ? null : this.changedBy, this.dateChanged, this.retired, this.name, this.description, this.uuid, this.outcomesConceptId == 0 ? null : this.outcomesConceptId};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithObjectId(){ 
 		return "INSERT INTO program(program_id, program_id, concept_id, creator, date_created, changed_by, date_changed, retired, name, description, uuid, outcomes_concept_id) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithObjectId(){ 
 		Object[] params = {this.programId, this.programId, this.conceptId == 0 ? null : this.conceptId, this.creator == 0 ? null : this.creator, this.dateCreated, this.changedBy == 0 ? null : this.changedBy, this.dateChanged, this.retired, this.name, this.description, this.uuid, this.outcomesConceptId == 0 ? null : this.outcomesConceptId};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.programId, this.conceptId == 0 ? null : this.conceptId, this.creator == 0 ? null : this.creator, this.dateCreated, this.changedBy == 0 ? null : this.changedBy, this.dateChanged, this.retired, this.name, this.description, this.uuid, this.outcomesConceptId == 0 ? null : this.outcomesConceptId, this.programId};		return params; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE program SET program_id = ?, concept_id = ?, creator = ?, date_created = ?, changed_by = ?, date_changed = ?, retired = ?, name = ?, description = ?, uuid = ?, outcomes_concept_id = ? WHERE program_id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.programId) + "," + (this.conceptId == 0 ? null : this.conceptId) + "," + (this.creator == 0 ? null : this.creator) + "," + (this.dateCreated != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateCreated)  +"\"" : null) + "," + (this.changedBy == 0 ? null : this.changedBy) + "," + (this.dateChanged != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateChanged)  +"\"" : null) + "," + (this.retired) + "," + (this.name != null ? "\""+ utilities.scapeQuotationMarks(name)  +"\"" : null) + "," + (this.description != null ? "\""+ utilities.scapeQuotationMarks(description)  +"\"" : null) + "," + (this.uuid != null ? "\""+ utilities.scapeQuotationMarks(uuid)  +"\"" : null) + "," + (this.outcomesConceptId == 0 ? null : this.outcomesConceptId); 
	} 
 
	@Override
	public boolean hasParents() {
		if (this.conceptId != 0) return true;

		if (this.creator != 0) return true;

		if (this.outcomesConceptId != 0) return true;

		if (this.changedBy != 0) return true;

		return false;
	}

	@Override
	public int getParentValue(String parentAttName) {		
		if (parentAttName.equals("conceptId")) return this.conceptId;		
		if (parentAttName.equals("creator")) return this.creator;		
		if (parentAttName.equals("outcomesConceptId")) return this.outcomesConceptId;		
		if (parentAttName.equals("changedBy")) return this.changedBy;

		throw new RuntimeException("No found parent for: " + parentAttName);	}

	@Override
	public void changeParentValue(String parentAttName, OpenMRSObject newParent) {		
		if (parentAttName.equals("conceptId")) {
			this.conceptId = newParent.getObjectId();
			return;
		}		
		if (parentAttName.equals("creator")) {
			this.creator = newParent.getObjectId();
			return;
		}		
		if (parentAttName.equals("outcomesConceptId")) {
			this.outcomesConceptId = newParent.getObjectId();
			return;
		}		
		if (parentAttName.equals("changedBy")) {
			this.changedBy = newParent.getObjectId();
			return;
		}

		throw new RuntimeException("No found parent for: " + parentAttName);
	}


}