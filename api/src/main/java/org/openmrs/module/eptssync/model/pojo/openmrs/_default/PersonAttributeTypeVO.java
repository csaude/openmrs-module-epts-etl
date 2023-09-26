package org.openmrs.module.eptssync.model.pojo.openmrs._default;

import org.openmrs.module.eptssync.model.pojo.generic.*; 
 
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.eptssync.utilities.AttDefinedElements; 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class PersonAttributeTypeVO extends AbstractDatabaseObject implements DatabaseObject { 
	private Integer personAttributeTypeId;
	private String name;
	private byte[] description;
	private String format;
	private Integer foreignKey;
	private byte searchable;
	private Integer creator;
	private Integer changedBy;
	private byte retired;
	private Integer retiredBy;
	private java.util.Date dateRetired;
	private String retireReason;
	private String editPrivilege;
	private double sortWeight;
 
	public PersonAttributeTypeVO() { 
		this.metadata = true;
	} 
 
	public void setPersonAttributeTypeId(Integer personAttributeTypeId){ 
	 	this.personAttributeTypeId = personAttributeTypeId;
	}
 
	public Integer getPersonAttributeTypeId(){ 
		return this.personAttributeTypeId;
	}
 
	public void setName(String name){ 
	 	this.name = name;
	}
 
	public String getName(){ 
		return this.name;
	}
 
	public void setDescription(byte[] description){ 
	 	this.description = description;
	}
 
	public byte[] getDescription(){ 
		return this.description;
	}
 
	public void setFormat(String format){ 
	 	this.format = format;
	}
 
	public String getFormat(){ 
		return this.format;
	}
 
	public void setForeignKey(Integer foreignKey){ 
	 	this.foreignKey = foreignKey;
	}
 
	public Integer getForeignKey(){ 
		return this.foreignKey;
	}
 
	public void setSearchable(byte searchable){ 
	 	this.searchable = searchable;
	}
 
	public byte getSearchable(){ 
		return this.searchable;
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
 
	public void setRetireReason(String retireReason){ 
	 	this.retireReason = retireReason;
	}
 
	public String getRetireReason(){ 
		return this.retireReason;
	}
 
	public void setEditPrivilege(String editPrivilege){ 
	 	this.editPrivilege = editPrivilege;
	}
 
	public String getEditPrivilege(){ 
		return this.editPrivilege;
	}
 
	public void setSortWeight(double sortWeight){ 
	 	this.sortWeight = sortWeight;
	}


 
	public double getSortWeight(){ 
		return this.sortWeight;
	}
 
	public Integer getObjectId() { 
 		return this.personAttributeTypeId; 
	} 
 
	public void setObjectId(Integer selfId){ 
		this.personAttributeTypeId = selfId; 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		super.load(rs);
		if (rs.getObject("person_attribute_type_id") != null) this.personAttributeTypeId = rs.getInt("person_attribute_type_id");
		this.name = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("name") != null ? rs.getString("name").trim() : null);
		this.description = rs.getBytes("description");
		this.format = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("format") != null ? rs.getString("format").trim() : null);
		if (rs.getObject("foreign_key") != null) this.foreignKey = rs.getInt("foreign_key");
		this.searchable = rs.getByte("searchable");
		if (rs.getObject("creator") != null) this.creator = rs.getInt("creator");
		this.dateCreated =  rs.getTimestamp("date_created") != null ? new java.util.Date( rs.getTimestamp("date_created").getTime() ) : null;
		if (rs.getObject("changed_by") != null) this.changedBy = rs.getInt("changed_by");
		this.dateChanged =  rs.getTimestamp("date_changed") != null ? new java.util.Date( rs.getTimestamp("date_changed").getTime() ) : null;
		this.retired = rs.getByte("retired");
		if (rs.getObject("retired_by") != null) this.retiredBy = rs.getInt("retired_by");
		this.dateRetired =  rs.getTimestamp("date_retired") != null ? new java.util.Date( rs.getTimestamp("date_retired").getTime() ) : null;
		this.retireReason = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("retire_reason") != null ? rs.getString("retire_reason").trim() : null);
		this.editPrivilege = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("edit_privilege") != null ? rs.getString("edit_privilege").trim() : null);
		this.uuid = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("uuid") != null ? rs.getString("uuid").trim() : null);
		this.sortWeight = rs.getDouble("sort_weight");
	} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "person_attribute_type_id"; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithoutObjectId(){ 
 		return "INSERT INTO person_attribute_type(name, description, format, foreign_key, searchable, creator, date_created, changed_by, date_changed, retired, retired_by, date_retired, retire_reason, edit_privilege, uuid, sort_weight) VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithoutObjectId(){ 
 		Object[] params = {this.name, this.description, this.format, this.foreignKey, this.searchable, this.creator, this.dateCreated, this.changedBy, this.dateChanged, this.retired, this.retiredBy, this.dateRetired, this.retireReason, this.editPrivilege, this.uuid, this.sortWeight};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithObjectId(){ 
 		return "INSERT INTO person_attribute_type(person_attribute_type_id, name, description, format, foreign_key, searchable, creator, date_created, changed_by, date_changed, retired, retired_by, date_retired, retire_reason, edit_privilege, uuid, sort_weight) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithObjectId(){ 
 		Object[] params = {this.personAttributeTypeId, this.name, this.description, this.format, this.foreignKey, this.searchable, this.creator, this.dateCreated, this.changedBy, this.dateChanged, this.retired, this.retiredBy, this.dateRetired, this.retireReason, this.editPrivilege, this.uuid, this.sortWeight};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.name, this.description, this.format, this.foreignKey, this.searchable, this.creator, this.dateCreated, this.changedBy, this.dateChanged, this.retired, this.retiredBy, this.dateRetired, this.retireReason, this.editPrivilege, this.uuid, this.sortWeight, this.personAttributeTypeId};		return params; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE person_attribute_type SET name = ?, description = ?, format = ?, foreign_key = ?, searchable = ?, creator = ?, date_created = ?, changed_by = ?, date_changed = ?, retired = ?, retired_by = ?, date_retired = ?, retire_reason = ?, edit_privilege = ?, uuid = ?, sort_weight = ? WHERE person_attribute_type_id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.name != null ? "\""+ utilities.scapeQuotationMarks(name)  +"\"" : null) + "," + (this.description != null ? "\""+description+"\"" : null) + "," + (this.format != null ? "\""+ utilities.scapeQuotationMarks(format)  +"\"" : null) + "," + (this.foreignKey) + "," + (this.searchable) + "," + (this.creator) + "," + (this.dateCreated != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateCreated)  +"\"" : null) + "," + (this.changedBy) + "," + (this.dateChanged != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateChanged)  +"\"" : null) + "," + (this.retired) + "," + (this.retiredBy) + "," + (this.dateRetired != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateRetired)  +"\"" : null) + "," + (this.retireReason != null ? "\""+ utilities.scapeQuotationMarks(retireReason)  +"\"" : null) + "," + (this.editPrivilege != null ? "\""+ utilities.scapeQuotationMarks(editPrivilege)  +"\"" : null) + "," + (this.uuid != null ? "\""+ utilities.scapeQuotationMarks(uuid)  +"\"" : null) + "," + (this.sortWeight); 
	} 
 
	@Override
	public boolean hasParents() {
		if (this.changedBy != 0) return true;

		if (this.creator != 0) return true;

		if (this.editPrivilege != null) return true;

		if (this.retiredBy != 0) return true;

		return false;
	}

	@Override
	public Integer getParentValue(String parentAttName) {		
		if (parentAttName.equals("changedBy")) return this.changedBy;		
		if (parentAttName.equals("creator")) return this.creator;		
		if (parentAttName.equals("editPrivilege")) return 0;		
		if (parentAttName.equals("retiredBy")) return this.retiredBy;

		throw new RuntimeException("No found parent for: " + parentAttName);	}

	@Override
	public void changeParentValue(String parentAttName, DatabaseObject newParent) {		
		if (parentAttName.equals("changedBy")) {
			this.changedBy = newParent.getObjectId();
			return;
		}		
		if (parentAttName.equals("creator")) {
			this.creator = newParent.getObjectId();
			return;
		}		
		if (parentAttName.equals("editPrivilege")) {
			this.editPrivilege = "" + newParent.getObjectId();
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
		if (parentAttName.equals("changedBy")) {
			this.changedBy = null;
			return;
		}		
		if (parentAttName.equals("creator")) {
			this.creator = null;
			return;
		}		
		if (parentAttName.equals("editPrivilege")) {
			this.editPrivilege = null;
			return;
		}		
		if (parentAttName.equals("retiredBy")) {
			this.retiredBy = null;
			return;
		}

		throw new RuntimeException("No found parent for: " + parentAttName);
	}


}