package org.openmrs.module.epts.etl.model.pojo.openmrs._default;

import java.sql.SQLException;

import org.openmrs.module.epts.etl.model.pojo.generic.*;
import org.openmrs.module.epts.etl.utilities.AttDefinedElements;
import org.openmrs.module.epts.etl.utilities.DateAndTimeUtilities;

import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class ConceptVO extends AbstractDatabaseObject implements DatabaseObject { 
	private Integer conceptId;
	private byte retired;
	private String shortName;
	private byte[] description;
	private byte[] formText;
	private Integer datatypeId;
	private Integer classId;
	private byte isSet;
	private Integer creator;
	private String version;
	private Integer changedBy;
	private Integer retiredBy;
	private java.util.Date dateRetired;
	private String retireReason;
 
	public ConceptVO() { 
		this.metadata = true;
	} 
 
	public void setConceptId(Integer conceptId){ 
	 	this.conceptId = conceptId;
	}
 
	public Integer getConceptId(){ 
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
 
	public void setDescription(byte[] description){ 
	 	this.description = description;
	}
 
	public byte[] getDescription(){ 
		return this.description;
	}
 
	public void setFormText(byte[] formText){ 
	 	this.formText = formText;
	}
 
	public byte[] getFormText(){ 
		return this.formText;
	}
 
	public void setDatatypeId(Integer datatypeId){ 
	 	this.datatypeId = datatypeId;
	}
 
	public Integer getDatatypeId(){ 
		return this.datatypeId;
	}
 
	public void setClassId(Integer classId){ 
	 	this.classId = classId;
	}
 
	public Integer getClassId(){ 
		return this.classId;
	}
 
	public void setIsSet(byte isSet){ 
	 	this.isSet = isSet;
	}
 
	public byte getIsSet(){ 
		return this.isSet;
	}
 
	public void setCreator(Integer creator){ 
	 	this.creator = creator;
	}
 
	public Integer getCreator(){ 
		return this.creator;
	}
 
	public void setVersion(String version){ 
	 	this.version = version;
	}
 
	public String getVersion(){ 
		return this.version;
	}
 
	public void setChangedBy(Integer changedBy){ 
	 	this.changedBy = changedBy;
	}
 
	public Integer getChangedBy(){ 
		return this.changedBy;
	}
 
	public void setRetiredBy(Integer retiredBy){ 
	 	this.retiredBy = retiredBy;
	}
 
	public Integer getRetiredBy(){ 
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
 

 
	public Integer getObjectId() { 
 		return this.conceptId; 
	} 
 
	public void setObjectId(Integer selfId){ 
		this.conceptId = selfId; 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		super.load(rs);
		if (rs.getObject("concept_id") != null) this.conceptId = rs.getInt("concept_id");
		this.retired = rs.getByte("retired");
		this.shortName = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("short_name") != null ? rs.getString("short_name").trim() : null);
		this.description = rs.getBytes("description");
		this.formText = rs.getBytes("form_text");
		if (rs.getObject("datatype_id") != null) this.datatypeId = rs.getInt("datatype_id");
		if (rs.getObject("class_id") != null) this.classId = rs.getInt("class_id");
		this.isSet = rs.getByte("is_set");
		if (rs.getObject("creator") != null) this.creator = rs.getInt("creator");
		this.dateCreated =  rs.getTimestamp("date_created") != null ? new java.util.Date( rs.getTimestamp("date_created").getTime() ) : null;
		this.version = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("version") != null ? rs.getString("version").trim() : null);
		if (rs.getObject("changed_by") != null) this.changedBy = rs.getInt("changed_by");
		this.dateChanged =  rs.getTimestamp("date_changed") != null ? new java.util.Date( rs.getTimestamp("date_changed").getTime() ) : null;
		if (rs.getObject("retired_by") != null) this.retiredBy = rs.getInt("retired_by");
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
 		return "INSERT INTO concept(retired, short_name, description, form_text, datatype_id, class_id, is_set, creator, date_created, version, changed_by, date_changed, retired_by, date_retired, retire_reason, uuid) VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithoutObjectId(){ 
 		Object[] params = {this.retired, this.shortName, this.description, this.formText, this.datatypeId, this.classId, this.isSet, this.creator, this.dateCreated, this.version, this.changedBy, this.dateChanged, this.retiredBy, this.dateRetired, this.retireReason, this.uuid};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithObjectId(){ 
 		return "INSERT INTO concept(concept_id, retired, short_name, description, form_text, datatype_id, class_id, is_set, creator, date_created, version, changed_by, date_changed, retired_by, date_retired, retire_reason, uuid) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithObjectId(){ 
 		Object[] params = {this.conceptId, this.retired, this.shortName, this.description, this.formText, this.datatypeId, this.classId, this.isSet, this.creator, this.dateCreated, this.version, this.changedBy, this.dateChanged, this.retiredBy, this.dateRetired, this.retireReason, this.uuid};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.retired, this.shortName, this.description, this.formText, this.datatypeId, this.classId, this.isSet, this.creator, this.dateCreated, this.version, this.changedBy, this.dateChanged, this.retiredBy, this.dateRetired, this.retireReason, this.uuid, this.conceptId};		return params; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE concept SET retired = ?, short_name = ?, description = ?, form_text = ?, datatype_id = ?, class_id = ?, is_set = ?, creator = ?, date_created = ?, version = ?, changed_by = ?, date_changed = ?, retired_by = ?, date_retired = ?, retire_reason = ?, uuid = ? WHERE concept_id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.retired) + "," + (this.shortName != null ? "\""+ utilities.scapeQuotationMarks(shortName)  +"\"" : null) + "," + (this.description != null ? "\""+description+"\"" : null) + "," + (this.formText != null ? "\""+formText+"\"" : null) + "," + (this.datatypeId) + "," + (this.classId) + "," + (this.isSet) + "," + (this.creator) + "," + (this.dateCreated != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateCreated)  +"\"" : null) + "," + (this.version != null ? "\""+ utilities.scapeQuotationMarks(version)  +"\"" : null) + "," + (this.changedBy) + "," + (this.dateChanged != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateChanged)  +"\"" : null) + "," + (this.retiredBy) + "," + (this.dateRetired != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateRetired)  +"\"" : null) + "," + (this.retireReason != null ? "\""+ utilities.scapeQuotationMarks(retireReason)  +"\"" : null) + "," + (this.uuid != null ? "\""+ utilities.scapeQuotationMarks(uuid)  +"\"" : null); 
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
	public Integer getParentValue(String parentAttName) {		
		if (parentAttName.equals("classId")) return this.classId;		
		if (parentAttName.equals("creator")) return this.creator;		
		if (parentAttName.equals("datatypeId")) return this.datatypeId;		
		if (parentAttName.equals("changedBy")) return this.changedBy;		
		if (parentAttName.equals("retiredBy")) return this.retiredBy;

		throw new RuntimeException("No found parent for: " + parentAttName);	}

	@Override
	public void changeParentValue(String parentAttName, DatabaseObject newParent) {		
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

	@Override
	public void setParentToNull(String parentAttName) {		
		if (parentAttName.equals("classId")) {
			this.classId = null;
			return;
		}		
		if (parentAttName.equals("creator")) {
			this.creator = null;
			return;
		}		
		if (parentAttName.equals("datatypeId")) {
			this.datatypeId = null;
			return;
		}		
		if (parentAttName.equals("changedBy")) {
			this.changedBy = null;
			return;
		}		
		if (parentAttName.equals("retiredBy")) {
			this.retiredBy = null;
			return;
		}

		throw new RuntimeException("No found parent for: " + parentAttName);
	}


}