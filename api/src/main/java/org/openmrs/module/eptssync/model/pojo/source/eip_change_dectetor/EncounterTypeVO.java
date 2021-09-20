package org.openmrs.module.eptssync.model.pojo.source.eip_change_dectetor; 
 
import org.openmrs.module.eptssync.model.pojo.generic.*; 
 
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.eptssync.utilities.AttDefinedElements; 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class EncounterTypeVO extends AbstractOpenMRSObject implements OpenMRSObject { 
	private int encounterTypeId;
	private String name;
	private String description;
	private int creator;
	private byte retired;
	private int retiredBy;
	private java.util.Date dateRetired;
	private String retireReason;
	private String viewPrivilege;
	private String editPrivilege;
	private int changedBy;
 
	public EncounterTypeVO() { 
		this.metadata = true;
	} 
 
	public void setEncounterTypeId(int encounterTypeId){ 
	 	this.encounterTypeId = encounterTypeId;
	}
 
	public int getEncounterTypeId(){ 
		return this.encounterTypeId;
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
 
	public void setViewPrivilege(String viewPrivilege){ 
	 	this.viewPrivilege = viewPrivilege;
	}
 
	public String getViewPrivilege(){ 
		return this.viewPrivilege;
	}
 
	public void setEditPrivilege(String editPrivilege){ 
	 	this.editPrivilege = editPrivilege;
	}
 
	public String getEditPrivilege(){ 
		return this.editPrivilege;
	}
 
	public void setChangedBy(int changedBy){ 
	 	this.changedBy = changedBy;
	}
 
	public int getChangedBy(){ 
		return this.changedBy;
	}
 

 
	public int getObjectId() { 
 		return this.encounterTypeId; 
	} 
 
	public void setObjectId(int selfId){ 
		this.encounterTypeId = selfId; 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		super.load(rs);
		this.encounterTypeId = rs.getInt("encounter_type_id");
		this.name = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("name") != null ? rs.getString("name").trim() : null);
		this.description = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("description") != null ? rs.getString("description").trim() : null);
		this.creator = rs.getInt("creator");
		this.dateCreated =  rs.getTimestamp("date_created") != null ? new java.util.Date( rs.getTimestamp("date_created").getTime() ) : null;
		this.retired = rs.getByte("retired");
		this.retiredBy = rs.getInt("retired_by");
		this.dateRetired =  rs.getTimestamp("date_retired") != null ? new java.util.Date( rs.getTimestamp("date_retired").getTime() ) : null;
		this.retireReason = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("retire_reason") != null ? rs.getString("retire_reason").trim() : null);
		this.uuid = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("uuid") != null ? rs.getString("uuid").trim() : null);
		this.viewPrivilege = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("view_privilege") != null ? rs.getString("view_privilege").trim() : null);
		this.editPrivilege = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("edit_privilege") != null ? rs.getString("edit_privilege").trim() : null);
		this.changedBy = rs.getInt("changed_by");
		this.dateChanged =  rs.getTimestamp("date_changed") != null ? new java.util.Date( rs.getTimestamp("date_changed").getTime() ) : null;
	} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "encounter_type_id"; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithoutObjectId(){ 
 		return "INSERT INTO encounter_type(encounter_type_id, name, description, creator, date_created, retired, retired_by, date_retired, retire_reason, uuid, view_privilege, edit_privilege, changed_by, date_changed) VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithoutObjectId(){ 
 		Object[] params = {this.encounterTypeId, this.name, this.description, this.creator == 0 ? null : this.creator, this.dateCreated, this.retired, this.retiredBy == 0 ? null : this.retiredBy, this.dateRetired, this.retireReason, this.uuid, this.viewPrivilege, this.editPrivilege, this.changedBy == 0 ? null : this.changedBy, this.dateChanged};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithObjectId(){ 
 		return "INSERT INTO encounter_type(encounter_type_id, encounter_type_id, name, description, creator, date_created, retired, retired_by, date_retired, retire_reason, uuid, view_privilege, edit_privilege, changed_by, date_changed) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithObjectId(){ 
 		Object[] params = {this.encounterTypeId, this.encounterTypeId, this.name, this.description, this.creator == 0 ? null : this.creator, this.dateCreated, this.retired, this.retiredBy == 0 ? null : this.retiredBy, this.dateRetired, this.retireReason, this.uuid, this.viewPrivilege, this.editPrivilege, this.changedBy == 0 ? null : this.changedBy, this.dateChanged};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.encounterTypeId, this.name, this.description, this.creator == 0 ? null : this.creator, this.dateCreated, this.retired, this.retiredBy == 0 ? null : this.retiredBy, this.dateRetired, this.retireReason, this.uuid, this.viewPrivilege, this.editPrivilege, this.changedBy == 0 ? null : this.changedBy, this.dateChanged, this.encounterTypeId};		return params; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE encounter_type SET encounter_type_id = ?, name = ?, description = ?, creator = ?, date_created = ?, retired = ?, retired_by = ?, date_retired = ?, retire_reason = ?, uuid = ?, view_privilege = ?, edit_privilege = ?, changed_by = ?, date_changed = ? WHERE encounter_type_id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.encounterTypeId) + "," + (this.name != null ? "\""+ utilities.scapeQuotationMarks(name)  +"\"" : null) + "," + (this.description != null ? "\""+ utilities.scapeQuotationMarks(description)  +"\"" : null) + "," + (this.creator == 0 ? null : this.creator) + "," + (this.dateCreated != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateCreated)  +"\"" : null) + "," + (this.retired) + "," + (this.retiredBy == 0 ? null : this.retiredBy) + "," + (this.dateRetired != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateRetired)  +"\"" : null) + "," + (this.retireReason != null ? "\""+ utilities.scapeQuotationMarks(retireReason)  +"\"" : null) + "," + (this.uuid != null ? "\""+ utilities.scapeQuotationMarks(uuid)  +"\"" : null) + "," + (this.viewPrivilege != null ? "\""+ utilities.scapeQuotationMarks(viewPrivilege)  +"\"" : null) + "," + (this.editPrivilege != null ? "\""+ utilities.scapeQuotationMarks(editPrivilege)  +"\"" : null) + "," + (this.changedBy == 0 ? null : this.changedBy) + "," + (this.dateChanged != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateChanged)  +"\"" : null); 
	} 
 
	@Override
	public boolean hasParents() {
		if (this.changedBy != 0) return true;

		if (this.editPrivilege != null) return true;

		if (this.viewPrivilege != null) return true;

		if (this.creator != 0) return true;

		if (this.retiredBy != 0) return true;

		return false;
	}

	@Override
	public int getParentValue(String parentAttName) {		
		if (parentAttName.equals("changedBy")) return this.changedBy;		
		if (parentAttName.equals("editPrivilege")) return 0;		
		if (parentAttName.equals("viewPrivilege")) return 0;		
		if (parentAttName.equals("creator")) return this.creator;		
		if (parentAttName.equals("retiredBy")) return this.retiredBy;

		throw new RuntimeException("No found parent for: " + parentAttName);	}

	@Override
	public void changeParentValue(String parentAttName, OpenMRSObject newParent) {		
		if (parentAttName.equals("changedBy")) {
			this.changedBy = newParent.getObjectId();
			return;
		}		
		if (parentAttName.equals("editPrivilege")) {
			this.editPrivilege = "" + newParent.getObjectId();
			return;
		}		
		if (parentAttName.equals("viewPrivilege")) {
			this.viewPrivilege = "" + newParent.getObjectId();
			return;
		}		
		if (parentAttName.equals("creator")) {
			this.creator = newParent.getObjectId();
			return;
		}		
		if (parentAttName.equals("retiredBy")) {
			this.retiredBy = newParent.getObjectId();
			return;
		}

		throw new RuntimeException("No found parent for: " + parentAttName);
	}


}