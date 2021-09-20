package org.openmrs.module.eptssync.model.pojo.source.eip_change_decteto; 
 
import org.openmrs.module.eptssync.model.pojo.generic.*; 
 
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.eptssync.utilities.AttDefinedElements; 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class PersonAttributeTypeVO extends AbstractOpenMRSObject implements OpenMRSObject { 
	private int personAttributeTypeId;
	private String name;
	private String description;
	private String format;
	private int foreignKey;
	private byte searchable;
	private int creator;
	private int changedBy;
	private byte retired;
	private int retiredBy;
	private java.util.Date dateRetired;
	private String retireReason;
	private String editPrivilege;
	private double sortWeight;
 
	public PersonAttributeTypeVO() { 
		this.metadata = true;
	} 
 
	public void setPersonAttributeTypeId(int personAttributeTypeId){ 
	 	this.personAttributeTypeId = personAttributeTypeId;
	}
 
	public int getPersonAttributeTypeId(){ 
		return this.personAttributeTypeId;
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
 
	public void setFormat(String format){ 
	 	this.format = format;
	}
 
	public String getFormat(){ 
		return this.format;
	}
 
	public void setForeignKey(int foreignKey){ 
	 	this.foreignKey = foreignKey;
	}
 
	public int getForeignKey(){ 
		return this.foreignKey;
	}
 
	public void setSearchable(byte searchable){ 
	 	this.searchable = searchable;
	}
 
	public byte getSearchable(){ 
		return this.searchable;
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
 
	public int getObjectId() { 
 		return this.personAttributeTypeId; 
	} 
 
	public void setObjectId(int selfId){ 
		this.personAttributeTypeId = selfId; 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		super.load(rs);
		this.personAttributeTypeId = rs.getInt("person_attribute_type_id");
		this.name = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("name") != null ? rs.getString("name").trim() : null);
		this.description = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("description") != null ? rs.getString("description").trim() : null);
		this.format = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("format") != null ? rs.getString("format").trim() : null);
		this.foreignKey = rs.getInt("foreign_key");
		this.searchable = rs.getByte("searchable");
		this.creator = rs.getInt("creator");
		this.dateCreated =  rs.getTimestamp("date_created") != null ? new java.util.Date( rs.getTimestamp("date_created").getTime() ) : null;
		this.changedBy = rs.getInt("changed_by");
		this.dateChanged =  rs.getTimestamp("date_changed") != null ? new java.util.Date( rs.getTimestamp("date_changed").getTime() ) : null;
		this.retired = rs.getByte("retired");
		this.retiredBy = rs.getInt("retired_by");
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
 		return "INSERT INTO person_attribute_type(person_attribute_type_id, name, description, format, foreign_key, searchable, creator, date_created, changed_by, date_changed, retired, retired_by, date_retired, retire_reason, edit_privilege, uuid, sort_weight) VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithoutObjectId(){ 
 		Object[] params = {this.personAttributeTypeId, this.name, this.description, this.format, this.foreignKey, this.searchable, this.creator == 0 ? null : this.creator, this.dateCreated, this.changedBy == 0 ? null : this.changedBy, this.dateChanged, this.retired, this.retiredBy == 0 ? null : this.retiredBy, this.dateRetired, this.retireReason, this.editPrivilege, this.uuid, this.sortWeight};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithObjectId(){ 
 		return "INSERT INTO person_attribute_type(person_attribute_type_id, person_attribute_type_id, name, description, format, foreign_key, searchable, creator, date_created, changed_by, date_changed, retired, retired_by, date_retired, retire_reason, edit_privilege, uuid, sort_weight) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithObjectId(){ 
 		Object[] params = {this.personAttributeTypeId, this.personAttributeTypeId, this.name, this.description, this.format, this.foreignKey, this.searchable, this.creator == 0 ? null : this.creator, this.dateCreated, this.changedBy == 0 ? null : this.changedBy, this.dateChanged, this.retired, this.retiredBy == 0 ? null : this.retiredBy, this.dateRetired, this.retireReason, this.editPrivilege, this.uuid, this.sortWeight};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.personAttributeTypeId, this.name, this.description, this.format, this.foreignKey, this.searchable, this.creator == 0 ? null : this.creator, this.dateCreated, this.changedBy == 0 ? null : this.changedBy, this.dateChanged, this.retired, this.retiredBy == 0 ? null : this.retiredBy, this.dateRetired, this.retireReason, this.editPrivilege, this.uuid, this.sortWeight, this.personAttributeTypeId};		return params; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE person_attribute_type SET person_attribute_type_id = ?, name = ?, description = ?, format = ?, foreign_key = ?, searchable = ?, creator = ?, date_created = ?, changed_by = ?, date_changed = ?, retired = ?, retired_by = ?, date_retired = ?, retire_reason = ?, edit_privilege = ?, uuid = ?, sort_weight = ? WHERE person_attribute_type_id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.personAttributeTypeId) + "," + (this.name != null ? "\""+ utilities.scapeQuotationMarks(name)  +"\"" : null) + "," + (this.description != null ? "\""+ utilities.scapeQuotationMarks(description)  +"\"" : null) + "," + (this.format != null ? "\""+ utilities.scapeQuotationMarks(format)  +"\"" : null) + "," + (this.foreignKey) + "," + (this.searchable) + "," + (this.creator == 0 ? null : this.creator) + "," + (this.dateCreated != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateCreated)  +"\"" : null) + "," + (this.changedBy == 0 ? null : this.changedBy) + "," + (this.dateChanged != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateChanged)  +"\"" : null) + "," + (this.retired) + "," + (this.retiredBy == 0 ? null : this.retiredBy) + "," + (this.dateRetired != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateRetired)  +"\"" : null) + "," + (this.retireReason != null ? "\""+ utilities.scapeQuotationMarks(retireReason)  +"\"" : null) + "," + (this.editPrivilege != null ? "\""+ utilities.scapeQuotationMarks(editPrivilege)  +"\"" : null) + "," + (this.uuid != null ? "\""+ utilities.scapeQuotationMarks(uuid)  +"\"" : null) + "," + (this.sortWeight); 
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
	public int getParentValue(String parentAttName) {		
		if (parentAttName.equals("changedBy")) return this.changedBy;		
		if (parentAttName.equals("creator")) return this.creator;		
		if (parentAttName.equals("editPrivilege")) return 0;		
		if (parentAttName.equals("retiredBy")) return this.retiredBy;

		throw new RuntimeException("No found parent for: " + parentAttName);	}

	@Override
	public void changeParentValue(String parentAttName, OpenMRSObject newParent) {		
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


}