package org.openmrs.module.eptssync.model.pojo.openmrs._default;

import org.openmrs.module.eptssync.model.pojo.generic.*; 
 
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.eptssync.utilities.AttDefinedElements; 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class FormVO extends AbstractDatabaseObject implements DatabaseObject { 
	private Integer formId;
	private String name;
	private String version;
	private Integer build;
	private byte published;
	private byte[] description;
	private Integer encounterType;
	private byte[] template;
	private byte[] xslt;
	private Integer creator;
	private Integer changedBy;
	private byte retired;
	private Integer retiredBy;
	private java.util.Date dateRetired;
	private String retiredReason;
 
	public FormVO() { 
		this.metadata = true;
	} 
 
	public void setFormId(Integer formId){ 
	 	this.formId = formId;
	}
 
	public Integer getFormId(){ 
		return this.formId;
	}
 
	public void setName(String name){ 
	 	this.name = name;
	}
 
	public String getName(){ 
		return this.name;
	}
 
	public void setVersion(String version){ 
	 	this.version = version;
	}
 
	public String getVersion(){ 
		return this.version;
	}
 
	public void setBuild(Integer build){ 
	 	this.build = build;
	}
 
	public Integer getBuild(){ 
		return this.build;
	}
 
	public void setPublished(byte published){ 
	 	this.published = published;
	}
 
	public byte getPublished(){ 
		return this.published;
	}
 
	public void setDescription(byte[] description){ 
	 	this.description = description;
	}
 
	public byte[] getDescription(){ 
		return this.description;
	}
 
	public void setEncounterType(Integer encounterType){ 
	 	this.encounterType = encounterType;
	}
 
	public Integer getEncounterType(){ 
		return this.encounterType;
	}
 
	public void setTemplate(byte[] template){ 
	 	this.template = template;
	}
 
	public byte[] getTemplate(){ 
		return this.template;
	}
 
	public void setXslt(byte[] xslt){ 
	 	this.xslt = xslt;
	}
 
	public byte[] getXslt(){ 
		return this.xslt;
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
 
	public void setRetired(byte retired){ 
	 	this.retired = retired;
	}
 
	public byte getRetired(){ 
		return this.retired;
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
 
	public void setRetiredReason(String retiredReason){ 
	 	this.retiredReason = retiredReason;
	}
 
	public String getRetiredReason(){ 
		return this.retiredReason;
	}
 

 
	public Integer getObjectId() { 
 		return this.formId; 
	} 
 
	public void setObjectId(Integer selfId){ 
		this.formId = selfId; 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		super.load(rs);
		if (rs.getObject("form_id") != null) this.formId = rs.getInt("form_id");
		this.name = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("name") != null ? rs.getString("name").trim() : null);
		this.version = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("version") != null ? rs.getString("version").trim() : null);
		if (rs.getObject("build") != null) this.build = rs.getInt("build");
		this.published = rs.getByte("published");
		this.description = rs.getBytes("description");
		if (rs.getObject("encounter_type") != null) this.encounterType = rs.getInt("encounter_type");
		this.template = rs.getBytes("template");
		this.xslt = rs.getBytes("xslt");
		if (rs.getObject("creator") != null) this.creator = rs.getInt("creator");
		this.dateCreated =  rs.getTimestamp("date_created") != null ? new java.util.Date( rs.getTimestamp("date_created").getTime() ) : null;
		if (rs.getObject("changed_by") != null) this.changedBy = rs.getInt("changed_by");
		this.dateChanged =  rs.getTimestamp("date_changed") != null ? new java.util.Date( rs.getTimestamp("date_changed").getTime() ) : null;
		this.retired = rs.getByte("retired");
		if (rs.getObject("retired_by") != null) this.retiredBy = rs.getInt("retired_by");
		this.dateRetired =  rs.getTimestamp("date_retired") != null ? new java.util.Date( rs.getTimestamp("date_retired").getTime() ) : null;
		this.retiredReason = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("retired_reason") != null ? rs.getString("retired_reason").trim() : null);
		this.uuid = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("uuid") != null ? rs.getString("uuid").trim() : null);
	} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "form_id"; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithoutObjectId(){ 
 		return "INSERT INTO form(name, version, build, published, description, encounter_type, template, xslt, creator, date_created, changed_by, date_changed, retired, retired_by, date_retired, retired_reason, uuid) VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithoutObjectId(){ 
 		Object[] params = {this.name, this.version, this.build, this.published, this.description, this.encounterType, this.template, this.xslt, this.creator, this.dateCreated, this.changedBy, this.dateChanged, this.retired, this.retiredBy, this.dateRetired, this.retiredReason, this.uuid};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithObjectId(){ 
 		return "INSERT INTO form(form_id, name, version, build, published, description, encounter_type, template, xslt, creator, date_created, changed_by, date_changed, retired, retired_by, date_retired, retired_reason, uuid) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithObjectId(){ 
 		Object[] params = {this.formId, this.name, this.version, this.build, this.published, this.description, this.encounterType, this.template, this.xslt, this.creator, this.dateCreated, this.changedBy, this.dateChanged, this.retired, this.retiredBy, this.dateRetired, this.retiredReason, this.uuid};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.name, this.version, this.build, this.published, this.description, this.encounterType, this.template, this.xslt, this.creator, this.dateCreated, this.changedBy, this.dateChanged, this.retired, this.retiredBy, this.dateRetired, this.retiredReason, this.uuid, this.formId};		return params; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE form SET name = ?, version = ?, build = ?, published = ?, description = ?, encounter_type = ?, template = ?, xslt = ?, creator = ?, date_created = ?, changed_by = ?, date_changed = ?, retired = ?, retired_by = ?, date_retired = ?, retired_reason = ?, uuid = ? WHERE form_id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.name != null ? "\""+ utilities.scapeQuotationMarks(name)  +"\"" : null) + "," + (this.version != null ? "\""+ utilities.scapeQuotationMarks(version)  +"\"" : null) + "," + (this.build) + "," + (this.published) + "," + (this.description != null ? "\""+description+"\"" : null) + "," + (this.encounterType) + "," + (this.template != null ? "\""+template+"\"" : null) + "," + (this.xslt != null ? "\""+xslt+"\"" : null) + "," + (this.creator) + "," + (this.dateCreated != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateCreated)  +"\"" : null) + "," + (this.changedBy) + "," + (this.dateChanged != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateChanged)  +"\"" : null) + "," + (this.retired) + "," + (this.retiredBy) + "," + (this.dateRetired != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateRetired)  +"\"" : null) + "," + (this.retiredReason != null ? "\""+ utilities.scapeQuotationMarks(retiredReason)  +"\"" : null) + "," + (this.uuid != null ? "\""+ utilities.scapeQuotationMarks(uuid)  +"\"" : null); 
	} 
 
	@Override
	public boolean hasParents() {
		if (this.encounterType != 0) return true;

		if (this.creator != 0) return true;

		if (this.changedBy != 0) return true;

		if (this.retiredBy != 0) return true;

		return false;
	}

	@Override
	public Integer getParentValue(String parentAttName) {		
		if (parentAttName.equals("encounterType")) return this.encounterType;		
		if (parentAttName.equals("creator")) return this.creator;		
		if (parentAttName.equals("changedBy")) return this.changedBy;		
		if (parentAttName.equals("retiredBy")) return this.retiredBy;

		throw new RuntimeException("No found parent for: " + parentAttName);	}

	@Override
	public void changeParentValue(String parentAttName, DatabaseObject newParent) {		
		if (parentAttName.equals("encounterType")) {
			this.encounterType = newParent.getObjectId();
			return;
		}		
		if (parentAttName.equals("creator")) {
			this.creator = newParent.getObjectId();
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
		if (parentAttName.equals("encounterType")) {
			this.encounterType = null;
			return;
		}		
		if (parentAttName.equals("creator")) {
			this.creator = null;
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