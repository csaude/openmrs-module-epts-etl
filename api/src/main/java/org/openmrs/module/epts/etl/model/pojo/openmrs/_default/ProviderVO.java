package org.openmrs.module.epts.etl.model.pojo.openmrs._default;

import java.sql.SQLException;

import org.openmrs.module.epts.etl.model.pojo.generic.*;
import org.openmrs.module.epts.etl.utilities.AttDefinedElements;
import org.openmrs.module.epts.etl.utilities.DateAndTimeUtilities;

import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class ProviderVO extends AbstractDatabaseObject implements DatabaseObject { 
	private Integer providerId;
	private Integer personId;
	private String name;
	private String identifier;
	private Integer creator;
	private Integer changedBy;
	private byte retired;
	private Integer retiredBy;
	private java.util.Date dateRetired;
	private String retireReason;
	private Integer roleId;
	private Integer specialityId;
	private Integer providerRoleId;
 
	public ProviderVO() { 
		this.metadata = false;
	} 
 
	public void setProviderId(Integer providerId){ 
	 	this.providerId = providerId;
	}
 
	public Integer getProviderId(){ 
		return this.providerId;
	}
 
	public void setPersonId(Integer personId){ 
	 	this.personId = personId;
	}
 
	public Integer getPersonId(){ 
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
 
	public void setRoleId(Integer roleId){ 
	 	this.roleId = roleId;
	}
 
	public Integer getRoleId(){ 
		return this.roleId;
	}
 
	public void setSpecialityId(Integer specialityId){ 
	 	this.specialityId = specialityId;
	}
 
	public Integer getSpecialityId(){ 
		return this.specialityId;
	}
 
	public void setProviderRoleId(Integer providerRoleId){ 
	 	this.providerRoleId = providerRoleId;
	}


 
	public Integer getProviderRoleId(){ 
		return this.providerRoleId;
	}
 
	public Integer getObjectId() { 
 		return this.providerId; 
	} 
 
	public void setObjectId(Integer selfId){ 
		this.providerId = selfId; 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		super.load(rs);
		if (rs.getObject("provider_id") != null) this.providerId = rs.getInt("provider_id");
		if (rs.getObject("person_id") != null) this.personId = rs.getInt("person_id");
		this.name = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("name") != null ? rs.getString("name").trim() : null);
		this.identifier = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("identifier") != null ? rs.getString("identifier").trim() : null);
		if (rs.getObject("creator") != null) this.creator = rs.getInt("creator");
		this.dateCreated =  rs.getTimestamp("date_created") != null ? new java.util.Date( rs.getTimestamp("date_created").getTime() ) : null;
		if (rs.getObject("changed_by") != null) this.changedBy = rs.getInt("changed_by");
		this.dateChanged =  rs.getTimestamp("date_changed") != null ? new java.util.Date( rs.getTimestamp("date_changed").getTime() ) : null;
		this.retired = rs.getByte("retired");
		if (rs.getObject("retired_by") != null) this.retiredBy = rs.getInt("retired_by");
		this.dateRetired =  rs.getTimestamp("date_retired") != null ? new java.util.Date( rs.getTimestamp("date_retired").getTime() ) : null;
		this.retireReason = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("retire_reason") != null ? rs.getString("retire_reason").trim() : null);
		this.uuid = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("uuid") != null ? rs.getString("uuid").trim() : null);
		if (rs.getObject("role_id") != null) this.roleId = rs.getInt("role_id");
		if (rs.getObject("speciality_id") != null) this.specialityId = rs.getInt("speciality_id");
		if (rs.getObject("provider_role_id") != null) this.providerRoleId = rs.getInt("provider_role_id");
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
 		Object[] params = {this.personId, this.name, this.identifier, this.creator, this.dateCreated, this.changedBy, this.dateChanged, this.retired, this.retiredBy, this.dateRetired, this.retireReason, this.uuid, this.roleId, this.specialityId, this.providerRoleId};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithObjectId(){ 
 		return "INSERT INTO provider(provider_id, person_id, name, identifier, creator, date_created, changed_by, date_changed, retired, retired_by, date_retired, retire_reason, uuid, role_id, speciality_id, provider_role_id) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithObjectId(){ 
 		Object[] params = {this.providerId, this.personId, this.name, this.identifier, this.creator, this.dateCreated, this.changedBy, this.dateChanged, this.retired, this.retiredBy, this.dateRetired, this.retireReason, this.uuid, this.roleId, this.specialityId, this.providerRoleId};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.personId, this.name, this.identifier, this.creator, this.dateCreated, this.changedBy, this.dateChanged, this.retired, this.retiredBy, this.dateRetired, this.retireReason, this.uuid, this.roleId, this.specialityId, this.providerRoleId, this.providerId};		return params; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE provider SET person_id = ?, name = ?, identifier = ?, creator = ?, date_created = ?, changed_by = ?, date_changed = ?, retired = ?, retired_by = ?, date_retired = ?, retire_reason = ?, uuid = ?, role_id = ?, speciality_id = ?, provider_role_id = ? WHERE provider_id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.personId) + "," + (this.name != null ? "\""+ utilities.scapeQuotationMarks(name)  +"\"" : null) + "," + (this.identifier != null ? "\""+ utilities.scapeQuotationMarks(identifier)  +"\"" : null) + "," + (this.creator) + "," + (this.dateCreated != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateCreated)  +"\"" : null) + "," + (this.changedBy) + "," + (this.dateChanged != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateChanged)  +"\"" : null) + "," + (this.retired) + "," + (this.retiredBy) + "," + (this.dateRetired != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateRetired)  +"\"" : null) + "," + (this.retireReason != null ? "\""+ utilities.scapeQuotationMarks(retireReason)  +"\"" : null) + "," + (this.uuid != null ? "\""+ utilities.scapeQuotationMarks(uuid)  +"\"" : null) + "," + (this.roleId) + "," + (this.specialityId) + "," + (this.providerRoleId); 
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
	public Integer getParentValue(String parentAttName) {		
		if (parentAttName.equals("changedBy")) return this.changedBy;		
		if (parentAttName.equals("creator")) return this.creator;		
		if (parentAttName.equals("providerRoleId")) return this.providerRoleId;		
		if (parentAttName.equals("personId")) return this.personId;		
		if (parentAttName.equals("retiredBy")) return this.retiredBy;		
		if (parentAttName.equals("roleId")) return this.roleId;		
		if (parentAttName.equals("specialityId")) return this.specialityId;

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
		if (parentAttName.equals("providerRoleId")) {
			this.providerRoleId = null;
			return;
		}		
		if (parentAttName.equals("personId")) {
			this.personId = null;
			return;
		}		
		if (parentAttName.equals("retiredBy")) {
			this.retiredBy = null;
			return;
		}		
		if (parentAttName.equals("roleId")) {
			this.roleId = null;
			return;
		}		
		if (parentAttName.equals("specialityId")) {
			this.specialityId = null;
			return;
		}

		throw new RuntimeException("No found parent for: " + parentAttName);
	}


}