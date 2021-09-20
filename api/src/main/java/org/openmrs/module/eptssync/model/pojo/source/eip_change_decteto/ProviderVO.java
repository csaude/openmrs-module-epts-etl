package org.openmrs.module.eptssync.model.pojo.source.eip_change_decteto; 
 
import org.openmrs.module.eptssync.model.pojo.generic.*; 
 
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.eptssync.utilities.AttDefinedElements; 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class ProviderVO extends AbstractOpenMRSObject implements OpenMRSObject { 
	private int providerId;
	private int personId;
	private String name;
	private String identifier;
	private int creator;
	private int changedBy;
	private byte retired;
	private int retiredBy;
	private java.util.Date dateRetired;
	private String retireReason;
	private int roleId;
	private int specialityId;
	private int providerRoleId;
 
	public ProviderVO() { 
		this.metadata = false;
	} 
 
	public void setProviderId(int providerId){ 
	 	this.providerId = providerId;
	}
 
	public int getProviderId(){ 
		return this.providerId;
	}
 
	public void setPersonId(int personId){ 
	 	this.personId = personId;
	}
 
	public int getPersonId(){ 
		return this.personId;
	}
 
	public void setName(String name){ 
	 	this.name = name;
	}
 
	public String getName(){ 
		return this.name;
	}
 
	public void setIdentifier(String identifier){ 
	 	this.identifier = identifier;
	}
 
	public String getIdentifier(){ 
		return this.identifier;
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
 
	public void setRoleId(int roleId){ 
	 	this.roleId = roleId;
	}
 
	public int getRoleId(){ 
		return this.roleId;
	}
 
	public void setSpecialityId(int specialityId){ 
	 	this.specialityId = specialityId;
	}
 
	public int getSpecialityId(){ 
		return this.specialityId;
	}
 
	public void setProviderRoleId(int providerRoleId){ 
	 	this.providerRoleId = providerRoleId;
	}


 
	public int getProviderRoleId(){ 
		return this.providerRoleId;
	}
 
	public int getObjectId() { 
 		return this.providerId; 
	} 
 
	public void setObjectId(int selfId){ 
		this.providerId = selfId; 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		super.load(rs);
		this.providerId = rs.getInt("provider_id");
		this.personId = rs.getInt("person_id");
		this.name = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("name") != null ? rs.getString("name").trim() : null);
		this.identifier = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("identifier") != null ? rs.getString("identifier").trim() : null);
		this.creator = rs.getInt("creator");
		this.dateCreated =  rs.getTimestamp("date_created") != null ? new java.util.Date( rs.getTimestamp("date_created").getTime() ) : null;
		this.changedBy = rs.getInt("changed_by");
		this.dateChanged =  rs.getTimestamp("date_changed") != null ? new java.util.Date( rs.getTimestamp("date_changed").getTime() ) : null;
		this.retired = rs.getByte("retired");
		this.retiredBy = rs.getInt("retired_by");
		this.dateRetired =  rs.getTimestamp("date_retired") != null ? new java.util.Date( rs.getTimestamp("date_retired").getTime() ) : null;
		this.retireReason = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("retire_reason") != null ? rs.getString("retire_reason").trim() : null);
		this.uuid = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("uuid") != null ? rs.getString("uuid").trim() : null);
		this.roleId = rs.getInt("role_id");
		this.specialityId = rs.getInt("speciality_id");
		this.providerRoleId = rs.getInt("provider_role_id");
	} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "provider_id"; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithoutObjectId(){ 
 		return "INSERT INTO provider(person_id, name, identifier, creator, date_created, changed_by, date_changed, retired, retired_by, date_retired, retire_reason, uuid, role_id, speciality_id, provider_role_id) VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithoutObjectId(){ 
 		Object[] params = {this.personId == 0 ? null : this.personId, this.name, this.identifier, this.creator == 0 ? null : this.creator, this.dateCreated, this.changedBy == 0 ? null : this.changedBy, this.dateChanged, this.retired, this.retiredBy == 0 ? null : this.retiredBy, this.dateRetired, this.retireReason, this.uuid, this.roleId == 0 ? null : this.roleId, this.specialityId == 0 ? null : this.specialityId, this.providerRoleId == 0 ? null : this.providerRoleId};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithObjectId(){ 
 		return "INSERT INTO provider(provider_id, person_id, name, identifier, creator, date_created, changed_by, date_changed, retired, retired_by, date_retired, retire_reason, uuid, role_id, speciality_id, provider_role_id) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithObjectId(){ 
 		Object[] params = {this.providerId, this.personId == 0 ? null : this.personId, this.name, this.identifier, this.creator == 0 ? null : this.creator, this.dateCreated, this.changedBy == 0 ? null : this.changedBy, this.dateChanged, this.retired, this.retiredBy == 0 ? null : this.retiredBy, this.dateRetired, this.retireReason, this.uuid, this.roleId == 0 ? null : this.roleId, this.specialityId == 0 ? null : this.specialityId, this.providerRoleId == 0 ? null : this.providerRoleId};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.personId == 0 ? null : this.personId, this.name, this.identifier, this.creator == 0 ? null : this.creator, this.dateCreated, this.changedBy == 0 ? null : this.changedBy, this.dateChanged, this.retired, this.retiredBy == 0 ? null : this.retiredBy, this.dateRetired, this.retireReason, this.uuid, this.roleId == 0 ? null : this.roleId, this.specialityId == 0 ? null : this.specialityId, this.providerRoleId == 0 ? null : this.providerRoleId, this.providerId};		return params; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE provider SET person_id = ?, name = ?, identifier = ?, creator = ?, date_created = ?, changed_by = ?, date_changed = ?, retired = ?, retired_by = ?, date_retired = ?, retire_reason = ?, uuid = ?, role_id = ?, speciality_id = ?, provider_role_id = ? WHERE provider_id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.personId == 0 ? null : this.personId) + "," + (this.name != null ? "\""+ utilities.scapeQuotationMarks(name)  +"\"" : null) + "," + (this.identifier != null ? "\""+ utilities.scapeQuotationMarks(identifier)  +"\"" : null) + "," + (this.creator == 0 ? null : this.creator) + "," + (this.dateCreated != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateCreated)  +"\"" : null) + "," + (this.changedBy == 0 ? null : this.changedBy) + "," + (this.dateChanged != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateChanged)  +"\"" : null) + "," + (this.retired) + "," + (this.retiredBy == 0 ? null : this.retiredBy) + "," + (this.dateRetired != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateRetired)  +"\"" : null) + "," + (this.retireReason != null ? "\""+ utilities.scapeQuotationMarks(retireReason)  +"\"" : null) + "," + (this.uuid != null ? "\""+ utilities.scapeQuotationMarks(uuid)  +"\"" : null) + "," + (this.roleId == 0 ? null : this.roleId) + "," + (this.specialityId == 0 ? null : this.specialityId) + "," + (this.providerRoleId == 0 ? null : this.providerRoleId); 
	} 
 
	@Override
	public boolean hasParents() {
		if (this.changedBy != 0) return true;

		if (this.creator != 0) return true;

		if (this.providerRoleId != 0) return true;

		if (this.personId != 0) return true;

		if (this.retiredBy != 0) return true;

		if (this.roleId != 0) return true;

		if (this.specialityId != 0) return true;

		return false;
	}

	@Override
	public int getParentValue(String parentAttName) {		
		if (parentAttName.equals("changedBy")) return this.changedBy;		
		if (parentAttName.equals("creator")) return this.creator;		
		if (parentAttName.equals("providerRoleId")) return this.providerRoleId;		
		if (parentAttName.equals("personId")) return this.personId;		
		if (parentAttName.equals("retiredBy")) return this.retiredBy;		
		if (parentAttName.equals("roleId")) return this.roleId;		
		if (parentAttName.equals("specialityId")) return this.specialityId;

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
		if (parentAttName.equals("providerRoleId")) {
			this.providerRoleId = newParent.getObjectId();
			return;
		}		
		if (parentAttName.equals("personId")) {
			this.personId = newParent.getObjectId();
			return;
		}		
		if (parentAttName.equals("retiredBy")) {
			this.retiredBy = newParent.getObjectId();
			return;
		}		
		if (parentAttName.equals("roleId")) {
			this.roleId = newParent.getObjectId();
			return;
		}		
		if (parentAttName.equals("specialityId")) {
			this.specialityId = newParent.getObjectId();
			return;
		}

		throw new RuntimeException("No found parent for: " + parentAttName);
	}


}