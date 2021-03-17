package org.openmrs.module.eptssync.model.pojo.molocue; 
 
import org.openmrs.module.eptssync.model.pojo.generic.*; 
 
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.eptssync.utilities.AttDefinedElements; 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
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
	private String version;
	private int changedBy;
	private int retiredBy;
	private java.util.Date dateRetired;
	private String retireReason;
 
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
 

 
	public int getObjectId() { 
 		return this.conceptId; 
	} 
 
	public void setObjectId(int selfId){ 
		this.conceptId = selfId; 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		this.conceptId = rs.getInt("concept_id");
		this.retired = rs.getByte("retired");
		this.shortName = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("short_name") != null ? rs.getString("short_name").trim() : null);
		this.description = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("description") != null ? rs.getString("description").trim() : null);
		this.formText = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("form_text") != null ? rs.getString("form_text").trim() : null);
		this.datatypeId = rs.getInt("datatype_id");
		this.classId = rs.getInt("class_id");
		this.isSet = rs.getByte("is_set");
		this.creator = rs.getInt("creator");
		this.dateCreated =  rs.getTimestamp("date_created") != null ? new java.util.Date( rs.getTimestamp("date_created").getTime() ) : null;
		this.version = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("version") != null ? rs.getString("version").trim() : null);
		this.changedBy = rs.getInt("changed_by");
		this.dateChanged =  rs.getTimestamp("date_changed") != null ? new java.util.Date( rs.getTimestamp("date_changed").getTime() ) : null;
		this.retiredBy = rs.getInt("retired_by");
		this.dateRetired =  rs.getTimestamp("date_retired") != null ? new java.util.Date( rs.getTimestamp("date_retired").getTime() ) : null;
		this.retireReason = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("retire_reason") != null ? rs.getString("retire_reason").trim() : null);
		this.uuid = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("uuid") != null ? rs.getString("uuid").trim() : null);
	} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "concept_id"; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithoutObjectId(){ 
 		return "INSERT INTO concept(concept_id, retired, short_name, description, form_text, datatype_id, class_id, is_set, creator, date_created, version, changed_by, date_changed, retired_by, date_retired, retire_reason, uuid) VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithoutObjectId(){ 
 		Object[] params = {this.conceptId, this.retired, this.shortName, this.description, this.formText, this.datatypeId == 0 ? null : this.datatypeId, this.classId == 0 ? null : this.classId, this.isSet, this.creator == 0 ? null : this.creator, this.dateCreated, this.version, this.changedBy == 0 ? null : this.changedBy, this.dateChanged, this.retiredBy == 0 ? null : this.retiredBy, this.dateRetired, this.retireReason, this.uuid};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithObjectId(){ 
 		return "INSERT INTO concept(concept_id, concept_id, retired, short_name, description, form_text, datatype_id, class_id, is_set, creator, date_created, version, changed_by, date_changed, retired_by, date_retired, retire_reason, uuid) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithObjectId(){ 
 		Object[] params = {this.conceptId, this.conceptId, this.retired, this.shortName, this.description, this.formText, this.datatypeId == 0 ? null : this.datatypeId, this.classId == 0 ? null : this.classId, this.isSet, this.creator == 0 ? null : this.creator, this.dateCreated, this.version, this.changedBy == 0 ? null : this.changedBy, this.dateChanged, this.retiredBy == 0 ? null : this.retiredBy, this.dateRetired, this.retireReason, this.uuid};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.conceptId, this.retired, this.shortName, this.description, this.formText, this.datatypeId == 0 ? null : this.datatypeId, this.classId == 0 ? null : this.classId, this.isSet, this.creator == 0 ? null : this.creator, this.dateCreated, this.version, this.changedBy == 0 ? null : this.changedBy, this.dateChanged, this.retiredBy == 0 ? null : this.retiredBy, this.dateRetired, this.retireReason, this.uuid, this.conceptId};		return params; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE concept SET concept_id = ?, retired = ?, short_name = ?, description = ?, form_text = ?, datatype_id = ?, class_id = ?, is_set = ?, creator = ?, date_created = ?, version = ?, changed_by = ?, date_changed = ?, retired_by = ?, date_retired = ?, retire_reason = ?, uuid = ? WHERE concept_id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.conceptId) + "," + (this.retired) + "," + (this.shortName != null ? "\""+ utilities.scapeQuotationMarks(shortName)  +"\"" : null) + "," + (this.description != null ? "\""+ utilities.scapeQuotationMarks(description)  +"\"" : null) + "," + (this.formText != null ? "\""+ utilities.scapeQuotationMarks(formText)  +"\"" : null) + "," + (this.datatypeId == 0 ? null : this.datatypeId) + "," + (this.classId == 0 ? null : this.classId) + "," + (this.isSet) + "," + (this.creator == 0 ? null : this.creator) + "," + (this.dateCreated != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateCreated)  +"\"" : null) + "," + (this.version != null ? "\""+ utilities.scapeQuotationMarks(version)  +"\"" : null) + "," + (this.changedBy == 0 ? null : this.changedBy) + "," + (this.dateChanged != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateChanged)  +"\"" : null) + "," + (this.retiredBy == 0 ? null : this.retiredBy) + "," + (this.dateRetired != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateRetired)  +"\"" : null) + "," + (this.retireReason != null ? "\""+ utilities.scapeQuotationMarks(retireReason)  +"\"" : null) + "," + (this.uuid != null ? "\""+ utilities.scapeQuotationMarks(uuid)  +"\"" : null); 
	} 
 
	@Override
	public boolean hasParents() {
		if (this.classId != 0) return true;

		if (this.creator != 0) return true;

		if (this.datatypeId != 0) return true;

		if (this.changedBy != 0) return true;

		if (this.retiredBy != 0) return true;

		return false;
	}

	@Override
	public int getParentValue(String parentAttName) {		
		if (parentAttName.equals("classId")) return this.classId;		
		if (parentAttName.equals("creator")) return this.creator;		
		if (parentAttName.equals("datatypeId")) return this.datatypeId;		
		if (parentAttName.equals("changedBy")) return this.changedBy;		
		if (parentAttName.equals("retiredBy")) return this.retiredBy;

		throw new RuntimeException("No found parent for: " + parentAttName);	}

	@Override
	public void changeParentValue(String parentAttName, OpenMRSObject newParent) {		
		if (parentAttName.equals("classId")) {
			this.classId = newParent.getObjectId();
			return;
		}		
		if (parentAttName.equals("creator")) {
			this.creator = newParent.getObjectId();
			return;
		}		
		if (parentAttName.equals("datatypeId")) {
			this.datatypeId = newParent.getObjectId();
			return;
		}		
		if (parentAttName.equals("changedBy")) {
			this.changedBy = newParent.getObjectId();
			return;
		}		
		if (parentAttName.equals("retiredBy")) {
			this.retiredBy = newParent.getObjectId();
			return;
		}

		throw new RuntimeException("No found parent for: " + parentAttName);
	}


}