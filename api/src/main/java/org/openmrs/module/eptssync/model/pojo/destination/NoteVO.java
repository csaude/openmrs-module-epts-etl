package org.openmrs.module.eptssync.model.pojo.destination;

import org.openmrs.module.eptssync.model.pojo.generic.*; 
 
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.eptssync.utilities.AttDefinedElements; 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class NoteVO extends AbstractDatabaseObject implements DatabaseObject { 
	private Integer noteId;
	private String noteType;
	private Integer patientId;
	private Integer obsId;
	private Integer encounterId;
	private String text;
	private Integer priority;
	private Integer parent;
	private Integer creator;
	private Integer changedBy;
 
	public NoteVO() { 
		this.metadata = false;
	} 
 
	public void setNoteId(Integer noteId){ 
	 	this.noteId = noteId;
	}
 
	public Integer getNoteId(){ 
		return this.noteId;
	}
 
	public void setNoteType(String noteType){ 
	 	this.noteType = noteType;
	}
 
	public String getNoteType(){ 
		return this.noteType;
	}
 
	public void setPatientId(Integer patientId){ 
	 	this.patientId = patientId;
	}
 
	public Integer getPatientId(){ 
		return this.patientId;
	}
 
	public void setObsId(Integer obsId){ 
	 	this.obsId = obsId;
	}
 
	public Integer getObsId(){ 
		return this.obsId;
	}
 
	public void setEncounterId(Integer encounterId){ 
	 	this.encounterId = encounterId;
	}
 
	public Integer getEncounterId(){ 
		return this.encounterId;
	}
 
	public void setText(String text){ 
	 	this.text = text;
	}
 
	public String getText(){ 
		return this.text;
	}
 
	public void setPriority(Integer priority){ 
	 	this.priority = priority;
	}
 
	public Integer getPriority(){ 
		return this.priority;
	}
 
	public void setParent(Integer parent){ 
	 	this.parent = parent;
	}
 
	public Integer getParent(){ 
		return this.parent;
	}
 
	public void setCreator(Integer creator){ 
	 	this.creator = creator;
	}
 
	public Integer getCreator(){ 
		return this.creator;
	}
 
	public void setChangedBy(Integer changedBy){ 
	 	this.changedBy = changedBy;
	}
 
	public Integer getChangedBy(){ 
		return this.changedBy;
	}
 

 
	public Integer getObjectId() { 
 		return this.noteId; 
	} 
 
	public void setObjectId(Integer selfId){ 
		this.noteId = selfId; 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		super.load(rs);
		if (rs.getObject("note_id") != null) this.noteId = rs.getInt("note_id");
		this.noteType = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("note_type") != null ? rs.getString("note_type").trim() : null);
		if (rs.getObject("patient_id") != null) this.patientId = rs.getInt("patient_id");
		if (rs.getObject("obs_id") != null) this.obsId = rs.getInt("obs_id");
		if (rs.getObject("encounter_id") != null) this.encounterId = rs.getInt("encounter_id");
		this.text = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("text") != null ? rs.getString("text").trim() : null);
		if (rs.getObject("priority") != null) this.priority = rs.getInt("priority");
		if (rs.getObject("parent") != null) this.parent = rs.getInt("parent");
		if (rs.getObject("creator") != null) this.creator = rs.getInt("creator");
		this.dateCreated =  rs.getTimestamp("date_created") != null ? new java.util.Date( rs.getTimestamp("date_created").getTime() ) : null;
		if (rs.getObject("changed_by") != null) this.changedBy = rs.getInt("changed_by");
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
 		Object[] params = {this.noteType, this.patientId, this.obsId, this.encounterId, this.text, this.priority, this.parent, this.creator, this.dateCreated, this.changedBy, this.dateChanged, this.uuid};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithObjectId(){ 
 		return "INSERT INTO note(note_id, note_type, patient_id, obs_id, encounter_id, text, priority, parent, creator, date_created, changed_by, date_changed, uuid) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithObjectId(){ 
 		Object[] params = {this.noteId, this.noteType, this.patientId, this.obsId, this.encounterId, this.text, this.priority, this.parent, this.creator, this.dateCreated, this.changedBy, this.dateChanged, this.uuid};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.noteType, this.patientId, this.obsId, this.encounterId, this.text, this.priority, this.parent, this.creator, this.dateCreated, this.changedBy, this.dateChanged, this.uuid, this.noteId};		return params; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE note SET note_type = ?, patient_id = ?, obs_id = ?, encounter_id = ?, text = ?, priority = ?, parent = ?, creator = ?, date_created = ?, changed_by = ?, date_changed = ?, uuid = ? WHERE note_id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.noteType != null ? "\""+ utilities.scapeQuotationMarks(noteType)  +"\"" : null) + "," + (this.patientId) + "," + (this.obsId) + "," + (this.encounterId) + "," + (this.text != null ? "\""+ utilities.scapeQuotationMarks(text)  +"\"" : null) + "," + (this.priority) + "," + (this.parent) + "," + (this.creator) + "," + (this.dateCreated != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateCreated)  +"\"" : null) + "," + (this.changedBy) + "," + (this.dateChanged != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateChanged)  +"\"" : null) + "," + (this.uuid != null ? "\""+ utilities.scapeQuotationMarks(uuid)  +"\"" : null); 
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
	public Integer getParentValue(String parentAttName) {		
		if (parentAttName.equals("encounterId")) return this.encounterId;		
		if (parentAttName.equals("parent")) return this.parent;		
		if (parentAttName.equals("obsId")) return this.obsId;		
		if (parentAttName.equals("patientId")) return this.patientId;		
		if (parentAttName.equals("changedBy")) return this.changedBy;		
		if (parentAttName.equals("creator")) return this.creator;

		throw new RuntimeException("No found parent for: " + parentAttName);	}

	@Override
	public void changeParentValue(String parentAttName, DatabaseObject newParent) {		
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

	@Override
	public void setParentToNull(String parentAttName) {		
		if (parentAttName.equals("encounterId")) {
			this.encounterId = null;
			return;
		}		
		if (parentAttName.equals("parent")) {
			this.parent = null;
			return;
		}		
		if (parentAttName.equals("obsId")) {
			this.obsId = null;
			return;
		}		
		if (parentAttName.equals("patientId")) {
			this.patientId = null;
			return;
		}		
		if (parentAttName.equals("changedBy")) {
			this.changedBy = null;
			return;
		}		
		if (parentAttName.equals("creator")) {
			this.creator = null;
			return;
		}

		throw new RuntimeException("No found parent for: " + parentAttName);
	}


}