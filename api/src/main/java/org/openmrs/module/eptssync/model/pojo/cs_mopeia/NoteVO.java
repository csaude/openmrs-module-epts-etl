package org.openmrs.module.eptssync.model.pojo.cs_mopeia; 
 
import org.openmrs.module.eptssync.model.pojo.generic.*; 
 
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.eptssync.utilities.AttDefinedElements; 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
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
	private int changedBy;
 
	public NoteVO() { 
		this.metadata = false;
	} 
 
	public void setNoteId(int noteId){ 
	 	this.noteId = noteId;
	}
 
	public int getNoteId(){ 
		return this.noteId;
	}
 
	public void setNoteType(String noteType){ 
	 	this.noteType = noteType;
	}
 
	public String getNoteType(){ 
		return this.noteType;
	}
 
	public void setPatientId(int patientId){ 
	 	this.patientId = patientId;
	}
 
	public int getPatientId(){ 
		return this.patientId;
	}
 
	public void setObsId(int obsId){ 
	 	this.obsId = obsId;
	}
 
	public int getObsId(){ 
		return this.obsId;
	}
 
	public void setEncounterId(int encounterId){ 
	 	this.encounterId = encounterId;
	}
 
	public int getEncounterId(){ 
		return this.encounterId;
	}
 
	public void setText(String text){ 
	 	this.text = text;
	}
 
	public String getText(){ 
		return this.text;
	}
 
	public void setPriority(int priority){ 
	 	this.priority = priority;
	}
 
	public int getPriority(){ 
		return this.priority;
	}
 
	public void setParent(int parent){ 
	 	this.parent = parent;
	}
 
	public int getParent(){ 
		return this.parent;
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
 

 
	public int getObjectId() { 
 		return this.noteId; 
	} 
 
	public void setObjectId(int selfId){ 
		this.noteId = selfId; 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		this.noteId = rs.getInt("note_id");
		this.noteType = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("note_type") != null ? rs.getString("note_type").trim() : null);
		this.patientId = rs.getInt("patient_id");
		this.obsId = rs.getInt("obs_id");
		this.encounterId = rs.getInt("encounter_id");
		this.text = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("text") != null ? rs.getString("text").trim() : null);
		this.priority = rs.getInt("priority");
		this.parent = rs.getInt("parent");
		this.creator = rs.getInt("creator");
		this.dateCreated =  rs.getTimestamp("date_created") != null ? new java.util.Date( rs.getTimestamp("date_created").getTime() ) : null;
		this.changedBy = rs.getInt("changed_by");
		this.dateChanged =  rs.getTimestamp("date_changed") != null ? new java.util.Date( rs.getTimestamp("date_changed").getTime() ) : null;
		this.uuid = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("uuid") != null ? rs.getString("uuid").trim() : null);
	} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "note_id"; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithoutObjectId(){ 
 		return "INSERT INTO note(note_type, patient_id, obs_id, encounter_id, text, priority, parent, creator, date_created, changed_by, date_changed, uuid) VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithoutObjectId(){ 
 		Object[] params = {this.noteType, this.patientId == 0 ? null : this.patientId, this.obsId == 0 ? null : this.obsId, this.encounterId == 0 ? null : this.encounterId, this.text, this.priority, this.parent == 0 ? null : this.parent, this.creator == 0 ? null : this.creator, this.dateCreated, this.changedBy == 0 ? null : this.changedBy, this.dateChanged, this.uuid};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithObjectId(){ 
 		return "INSERT INTO note(note_id, note_type, patient_id, obs_id, encounter_id, text, priority, parent, creator, date_created, changed_by, date_changed, uuid) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithObjectId(){ 
 		Object[] params = {this.noteId, this.noteType, this.patientId == 0 ? null : this.patientId, this.obsId == 0 ? null : this.obsId, this.encounterId == 0 ? null : this.encounterId, this.text, this.priority, this.parent == 0 ? null : this.parent, this.creator == 0 ? null : this.creator, this.dateCreated, this.changedBy == 0 ? null : this.changedBy, this.dateChanged, this.uuid};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.noteType, this.patientId == 0 ? null : this.patientId, this.obsId == 0 ? null : this.obsId, this.encounterId == 0 ? null : this.encounterId, this.text, this.priority, this.parent == 0 ? null : this.parent, this.creator == 0 ? null : this.creator, this.dateCreated, this.changedBy == 0 ? null : this.changedBy, this.dateChanged, this.uuid, this.noteId};		return params; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE note SET note_type = ?, patient_id = ?, obs_id = ?, encounter_id = ?, text = ?, priority = ?, parent = ?, creator = ?, date_created = ?, changed_by = ?, date_changed = ?, uuid = ? WHERE note_id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.noteType != null ? "\""+ utilities.scapeQuotationMarks(noteType)  +"\"" : null) + "," + (this.patientId == 0 ? null : this.patientId) + "," + (this.obsId == 0 ? null : this.obsId) + "," + (this.encounterId == 0 ? null : this.encounterId) + "," + (this.text != null ? "\""+ utilities.scapeQuotationMarks(text)  +"\"" : null) + "," + (this.priority) + "," + (this.parent == 0 ? null : this.parent) + "," + (this.creator == 0 ? null : this.creator) + "," + (this.dateCreated != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateCreated)  +"\"" : null) + "," + (this.changedBy == 0 ? null : this.changedBy) + "," + (this.dateChanged != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateChanged)  +"\"" : null) + "," + (this.uuid != null ? "\""+ utilities.scapeQuotationMarks(uuid)  +"\"" : null); 
	} 
 
	@Override
	public boolean hasParents() {
		if (this.encounterId != 0) return true;

		if (this.parent != 0) return true;

		if (this.obsId != 0) return true;

		if (this.patientId != 0) return true;

		if (this.changedBy != 0) return true;

		if (this.creator != 0) return true;

		return false;
	}

	@Override
	public int getParentValue(String parentAttName) {		
		if (parentAttName.equals("encounterId")) return this.encounterId;		
		if (parentAttName.equals("parent")) return this.parent;		
		if (parentAttName.equals("obsId")) return this.obsId;		
		if (parentAttName.equals("patientId")) return this.patientId;		
		if (parentAttName.equals("changedBy")) return this.changedBy;		
		if (parentAttName.equals("creator")) return this.creator;

		throw new RuntimeException("No found parent for: " + parentAttName);	}

	@Override
	public void changeParentValue(String parentAttName, OpenMRSObject newParent) {		
		if (parentAttName.equals("encounterId")) {
			this.encounterId = newParent.getObjectId();
			return;
		}		
		if (parentAttName.equals("parent")) {
			this.parent = newParent.getObjectId();
			return;
		}		
		if (parentAttName.equals("obsId")) {
			this.obsId = newParent.getObjectId();
			return;
		}		
		if (parentAttName.equals("patientId")) {
			this.patientId = newParent.getObjectId();
			return;
		}		
		if (parentAttName.equals("changedBy")) {
			this.changedBy = newParent.getObjectId();
			return;
		}		
		if (parentAttName.equals("creator")) {
			this.creator = newParent.getObjectId();
			return;
		}

		throw new RuntimeException("No found parent for: " + parentAttName);
	}


}