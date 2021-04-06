package org.openmrs.module.eptssync.model.pojo.destination; 
 
import org.openmrs.module.eptssync.model.pojo.generic.*; 
 
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.eptssync.utilities.AttDefinedElements; 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class EncounterProviderVO extends AbstractOpenMRSObject implements OpenMRSObject { 
	private int encounterProviderId;
	private int encounterId;
	private int providerId;
	private int encounterRoleId;
	private int creator;
	private int changedBy;
	private byte voided;
	private java.util.Date dateVoided;
	private int voidedBy;
	private String voidReason;
 
	public EncounterProviderVO() { 
		this.metadata = false;
	} 
 
	public void setEncounterProviderId(int encounterProviderId){ 
	 	this.encounterProviderId = encounterProviderId;
	}
 
	public int getEncounterProviderId(){ 
		return this.encounterProviderId;
	}
 
	public void setEncounterId(int encounterId){ 
	 	this.encounterId = encounterId;
	}
 
	public int getEncounterId(){ 
		return this.encounterId;
	}
 
	public void setProviderId(int providerId){ 
	 	this.providerId = providerId;
	}
 
	public int getProviderId(){ 
		return this.providerId;
	}
 
	public void setEncounterRoleId(int encounterRoleId){ 
	 	this.encounterRoleId = encounterRoleId;
	}
 
	public int getEncounterRoleId(){ 
		return this.encounterRoleId;
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
 
	public void setVoidedBy(int voidedBy){ 
	 	this.voidedBy = voidedBy;
	}
 
	public int getVoidedBy(){ 
		return this.voidedBy;
	}
 
	public void setVoidReason(String voidReason){ 
	 	this.voidReason = voidReason;
	}
 
	public String getVoidReason(){ 
		return this.voidReason;
	}
 

 
	public int getObjectId() { 
 		return this.encounterProviderId; 
	} 
 
	public void setObjectId(int selfId){ 
		this.encounterProviderId = selfId; 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		this.encounterProviderId = rs.getInt("encounter_provider_id");
		this.encounterId = rs.getInt("encounter_id");
		this.providerId = rs.getInt("provider_id");
		this.encounterRoleId = rs.getInt("encounter_role_id");
		this.creator = rs.getInt("creator");
		this.dateCreated =  rs.getTimestamp("date_created") != null ? new java.util.Date( rs.getTimestamp("date_created").getTime() ) : null;
		this.changedBy = rs.getInt("changed_by");
		this.dateChanged =  rs.getTimestamp("date_changed") != null ? new java.util.Date( rs.getTimestamp("date_changed").getTime() ) : null;
		this.voided = rs.getByte("voided");
		this.dateVoided =  rs.getTimestamp("date_voided") != null ? new java.util.Date( rs.getTimestamp("date_voided").getTime() ) : null;
		this.voidedBy = rs.getInt("voided_by");
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
 		Object[] params = {this.encounterId == 0 ? null : this.encounterId, this.providerId == 0 ? null : this.providerId, this.encounterRoleId == 0 ? null : this.encounterRoleId, this.creator == 0 ? null : this.creator, this.dateCreated, this.changedBy == 0 ? null : this.changedBy, this.dateChanged, this.voided, this.dateVoided, this.voidedBy == 0 ? null : this.voidedBy, this.voidReason, this.uuid};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithObjectId(){ 
 		return "INSERT INTO encounter_provider(encounter_provider_id, encounter_id, provider_id, encounter_role_id, creator, date_created, changed_by, date_changed, voided, date_voided, voided_by, void_reason, uuid) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithObjectId(){ 
 		Object[] params = {this.encounterProviderId, this.encounterId == 0 ? null : this.encounterId, this.providerId == 0 ? null : this.providerId, this.encounterRoleId == 0 ? null : this.encounterRoleId, this.creator == 0 ? null : this.creator, this.dateCreated, this.changedBy == 0 ? null : this.changedBy, this.dateChanged, this.voided, this.dateVoided, this.voidedBy == 0 ? null : this.voidedBy, this.voidReason, this.uuid};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.encounterId == 0 ? null : this.encounterId, this.providerId == 0 ? null : this.providerId, this.encounterRoleId == 0 ? null : this.encounterRoleId, this.creator == 0 ? null : this.creator, this.dateCreated, this.changedBy == 0 ? null : this.changedBy, this.dateChanged, this.voided, this.dateVoided, this.voidedBy == 0 ? null : this.voidedBy, this.voidReason, this.uuid, this.encounterProviderId};		return params; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE encounter_provider SET encounter_id = ?, provider_id = ?, encounter_role_id = ?, creator = ?, date_created = ?, changed_by = ?, date_changed = ?, voided = ?, date_voided = ?, voided_by = ?, void_reason = ?, uuid = ? WHERE encounter_provider_id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.encounterId == 0 ? null : this.encounterId) + "," + (this.providerId == 0 ? null : this.providerId) + "," + (this.encounterRoleId == 0 ? null : this.encounterRoleId) + "," + (this.creator == 0 ? null : this.creator) + "," + (this.dateCreated != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateCreated)  +"\"" : null) + "," + (this.changedBy == 0 ? null : this.changedBy) + "," + (this.dateChanged != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateChanged)  +"\"" : null) + "," + (this.voided) + "," + (this.dateVoided != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateVoided)  +"\"" : null) + "," + (this.voidedBy == 0 ? null : this.voidedBy) + "," + (this.voidReason != null ? "\""+ utilities.scapeQuotationMarks(voidReason)  +"\"" : null) + "," + (this.uuid != null ? "\""+ utilities.scapeQuotationMarks(uuid)  +"\"" : null); 
	} 
 
	@Override
	public boolean hasParents() {
		if (this.changedBy != 0) return true;

		if (this.creator != 0) return true;

		if (this.encounterId != 0) return true;

		if (this.encounterRoleId != 0) return true;

		if (this.providerId != 0) return true;

		if (this.voidedBy != 0) return true;

		return false;
	}

	@Override
	public int getParentValue(String parentAttName) {		
		if (parentAttName.equals("changedBy")) return this.changedBy;		
		if (parentAttName.equals("creator")) return this.creator;		
		if (parentAttName.equals("encounterId")) return this.encounterId;		
		if (parentAttName.equals("encounterRoleId")) return this.encounterRoleId;		
		if (parentAttName.equals("providerId")) return this.providerId;		
		if (parentAttName.equals("voidedBy")) return this.voidedBy;

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
		if (parentAttName.equals("encounterId")) {
			this.encounterId = newParent.getObjectId();
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
		if (parentAttName.equals("voidedBy")) {
			this.voidedBy = newParent.getObjectId();
			return;
		}

		throw new RuntimeException("No found parent for: " + parentAttName);
	}


}