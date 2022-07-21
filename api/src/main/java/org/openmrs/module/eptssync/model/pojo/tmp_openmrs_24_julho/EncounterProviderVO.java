package org.openmrs.module.eptssync.model.pojo.tmp_openmrs_24_julho;

import org.openmrs.module.eptssync.model.pojo.generic.*; 
 
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.eptssync.utilities.AttDefinedElements; 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class EncounterProviderVO extends AbstractOpenMRSObject implements OpenMRSObject { 
	private Integer encounterProviderId;
	private Integer encounterId;
	private Integer providerId;
	private Integer encounterRoleId;
	private Integer creator;
	private Integer changedBy;
	private byte voided;
	private java.util.Date dateVoided;
	private Integer voidedBy;
	private String voidReason;
 
	public EncounterProviderVO() { 
		this.metadata = false;
	} 
 
	public void setEncounterProviderId(Integer encounterProviderId){ 
	 	this.encounterProviderId = encounterProviderId;
	}
 
	public Integer getEncounterProviderId(){ 
		return this.encounterProviderId;
	}
 
	public void setEncounterId(Integer encounterId){ 
	 	this.encounterId = encounterId;
	}
 
	public Integer getEncounterId(){ 
		return this.encounterId;
	}
 
	public void setProviderId(Integer providerId){ 
	 	this.providerId = providerId;
	}
 
	public Integer getProviderId(){ 
		return this.providerId;
	}
 
	public void setEncounterRoleId(Integer encounterRoleId){ 
	 	this.encounterRoleId = encounterRoleId;
	}
 
	public Integer getEncounterRoleId(){ 
		return this.encounterRoleId;
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
 
	public void setVoided(byte voided){ 
	 	this.voided = voided;
	}
 
	public byte getVoided(){ 
		return this.voided;
	}
 
	public void setDateVoided(java.util.Date dateVoided){ 
	 	this.dateVoided = dateVoided;
	}
 
	public java.util.Date getDateVoided(){ 
		return this.dateVoided;
	}
 
	public void setVoidedBy(Integer voidedBy){ 
	 	this.voidedBy = voidedBy;
	}
 
	public Integer getVoidedBy(){ 
		return this.voidedBy;
	}
 
	public void setVoidReason(String voidReason){ 
	 	this.voidReason = voidReason;
	}
 
	public String getVoidReason(){ 
		return this.voidReason;
	}
 

 
	public Integer getObjectId() { 
 		return this.encounterProviderId; 
	} 
 
	public void setObjectId(Integer selfId){ 
		this.encounterProviderId = selfId; 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		super.load(rs);
		if (rs.getObject("encounter_provider_id") != null) this.encounterProviderId = rs.getInt("encounter_provider_id");
		if (rs.getObject("encounter_id") != null) this.encounterId = rs.getInt("encounter_id");
		if (rs.getObject("provider_id") != null) this.providerId = rs.getInt("provider_id");
		if (rs.getObject("encounter_role_id") != null) this.encounterRoleId = rs.getInt("encounter_role_id");
		if (rs.getObject("creator") != null) this.creator = rs.getInt("creator");
		this.dateCreated =  rs.getTimestamp("date_created") != null ? new java.util.Date( rs.getTimestamp("date_created").getTime() ) : null;
		if (rs.getObject("changed_by") != null) this.changedBy = rs.getInt("changed_by");
		this.dateChanged =  rs.getTimestamp("date_changed") != null ? new java.util.Date( rs.getTimestamp("date_changed").getTime() ) : null;
		this.voided = rs.getByte("voided");
		this.dateVoided =  rs.getTimestamp("date_voided") != null ? new java.util.Date( rs.getTimestamp("date_voided").getTime() ) : null;
		if (rs.getObject("voided_by") != null) this.voidedBy = rs.getInt("voided_by");
		this.voidReason = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("void_reason") != null ? rs.getString("void_reason").trim() : null);
		this.uuid = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("uuid") != null ? rs.getString("uuid").trim() : null);
	} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "encounter_provider_id"; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithoutObjectId(){ 
 		return "INSERT INTO encounter_provider(encounter_id, provider_id, encounter_role_id, creator, date_created, changed_by, date_changed, voided, date_voided, voided_by, void_reason, uuid) VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithoutObjectId(){ 
 		Object[] params = {this.encounterId, this.providerId, this.encounterRoleId, this.creator, this.dateCreated, this.changedBy, this.dateChanged, this.voided, this.dateVoided, this.voidedBy, this.voidReason, this.uuid};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithObjectId(){ 
 		return "INSERT INTO encounter_provider(encounter_provider_id, encounter_id, provider_id, encounter_role_id, creator, date_created, changed_by, date_changed, voided, date_voided, voided_by, void_reason, uuid) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithObjectId(){ 
 		Object[] params = {this.encounterProviderId, this.encounterId, this.providerId, this.encounterRoleId, this.creator, this.dateCreated, this.changedBy, this.dateChanged, this.voided, this.dateVoided, this.voidedBy, this.voidReason, this.uuid};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.encounterId, this.providerId, this.encounterRoleId, this.creator, this.dateCreated, this.changedBy, this.dateChanged, this.voided, this.dateVoided, this.voidedBy, this.voidReason, this.uuid, this.encounterProviderId};		return params; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE encounter_provider SET encounter_id = ?, provider_id = ?, encounter_role_id = ?, creator = ?, date_created = ?, changed_by = ?, date_changed = ?, voided = ?, date_voided = ?, voided_by = ?, void_reason = ?, uuid = ? WHERE encounter_provider_id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.encounterId) + "," + (this.providerId) + "," + (this.encounterRoleId) + "," + (this.creator) + "," + (this.dateCreated != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateCreated)  +"\"" : null) + "," + (this.changedBy) + "," + (this.dateChanged != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateChanged)  +"\"" : null) + "," + (this.voided) + "," + (this.dateVoided != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateVoided)  +"\"" : null) + "," + (this.voidedBy) + "," + (this.voidReason != null ? "\""+ utilities.scapeQuotationMarks(voidReason)  +"\"" : null) + "," + (this.uuid != null ? "\""+ utilities.scapeQuotationMarks(uuid)  +"\"" : null); 
	} 
 
	@Override
	public boolean hasParents() {
		if (this.encounterId != 0) return true;

		if (this.changedBy != 0) return true;

		if (this.creator != 0) return true;

		if (this.voidedBy != 0) return true;

		if (this.encounterRoleId != 0) return true;

		if (this.providerId != 0) return true;

		return false;
	}

	@Override
	public Integer getParentValue(String parentAttName) {		
		if (parentAttName.equals("encounterId")) return this.encounterId;		
		if (parentAttName.equals("changedBy")) return this.changedBy;		
		if (parentAttName.equals("creator")) return this.creator;		
		if (parentAttName.equals("voidedBy")) return this.voidedBy;		
		if (parentAttName.equals("encounterRoleId")) return this.encounterRoleId;		
		if (parentAttName.equals("providerId")) return this.providerId;

		throw new RuntimeException("No found parent for: " + parentAttName);	}

	@Override
	public void changeParentValue(String parentAttName, OpenMRSObject newParent) {		
		if (parentAttName.equals("encounterId")) {
			this.encounterId = newParent.getObjectId();
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
		if (parentAttName.equals("voidedBy")) {
			this.voidedBy = newParent.getObjectId();
			return;
		}		
		if (parentAttName.equals("encounterRoleId")) {
			this.encounterRoleId = newParent.getObjectId();
			return;
		}		
		if (parentAttName.equals("providerId")) {
			this.providerId = newParent.getObjectId();
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
		if (parentAttName.equals("changedBy")) {
			this.changedBy = null;
			return;
		}		
		if (parentAttName.equals("creator")) {
			this.creator = null;
			return;
		}		
		if (parentAttName.equals("voidedBy")) {
			this.voidedBy = null;
			return;
		}		
		if (parentAttName.equals("encounterRoleId")) {
			this.encounterRoleId = null;
			return;
		}		
		if (parentAttName.equals("providerId")) {
			this.providerId = null;
			return;
		}

		throw new RuntimeException("No found parent for: " + parentAttName);
	}


}