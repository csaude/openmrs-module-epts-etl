package org.openmrs.module.eptssync.model.openmrs; 
 
import org.openmrs.module.eptssync.model.GenericSyncRecordDAO; 
 
import org.openmrs.module.eptssync.model.base.BaseVO; 
 
import org.openmrs.module.eptssync.utilities.db.conn.DBException; 
import org.openmrs.module.eptssync.utilities.db.conn.OpenConnection; 
import org.openmrs.module.eptssync.exceptions.ParentNotYetMigratedException; 
 
import java.sql.Connection; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class PersonVO extends AbstractOpenMRSObject implements OpenMRSObject { 
	private int personId;
	private String gender;
	private java.util.Date birthdate;
	private byte birthdateEstimated;
	private byte dead;
	private java.util.Date deathDate;
	private int causeOfDeath;
	private int creator;
	private java.util.Date dateCreated;
	private int changedBy;
	private java.util.Date dateChanged;
	private byte voided;
	private int voidedBy;
	private java.util.Date dateVoided;
	private String voidReason;
	private String uuid;
	private byte deathdateEstimated;
	private java.util.Date birthtime;
	private java.util.Date lastSyncDate;
	private int originRecordId;
	private String originAppLocationCode;
 
	public PersonVO() { 
	} 
 
	public void setPersonId(int personId){ 
	 	this.personId = personId;
	}
 
	public int getPersonId(){ 
		return this.personId;
	}	public void setGender(String gender){ 
	 	this.gender = gender;
	}
 
	public String getGender(){ 
		return this.gender;
	}	public void setBirthdate(java.util.Date birthdate){ 
	 	this.birthdate = birthdate;
	}
 
	public java.util.Date getBirthdate(){ 
		return this.birthdate;
	}	public void setBirthdateEstimated(byte birthdateEstimated){ 
	 	this.birthdateEstimated = birthdateEstimated;
	}
 
	public byte getBirthdateEstimated(){ 
		return this.birthdateEstimated;
	}	public void setDead(byte dead){ 
	 	this.dead = dead;
	}
 
	public byte getDead(){ 
		return this.dead;
	}	public void setDeathDate(java.util.Date deathDate){ 
	 	this.deathDate = deathDate;
	}
 
	public java.util.Date getDeathDate(){ 
		return this.deathDate;
	}	public void setCauseOfDeath(int causeOfDeath){ 
	 	this.causeOfDeath = causeOfDeath;
	}
 
	public int getCauseOfDeath(){ 
		return this.causeOfDeath;
	}	public void setCreator(int creator){ 
	 	this.creator = creator;
	}
 
	public int getCreator(){ 
		return this.creator;
	}	public void setDateCreated(java.util.Date dateCreated){ 
	 	this.dateCreated = dateCreated;
	}
 
	public java.util.Date getDateCreated(){ 
		return this.dateCreated;
	}	public void setChangedBy(int changedBy){ 
	 	this.changedBy = changedBy;
	}
 
	public int getChangedBy(){ 
		return this.changedBy;
	}	public void setDateChanged(java.util.Date dateChanged){ 
	 	this.dateChanged = dateChanged;
	}
 
	public java.util.Date getDateChanged(){ 
		return this.dateChanged;
	}	public void setVoided(byte voided){ 
	 	this.voided = voided;
	}
 
	public byte getVoided(){ 
		return this.voided;
	}	public void setVoidedBy(int voidedBy){ 
	 	this.voidedBy = voidedBy;
	}
 
	public int getVoidedBy(){ 
		return this.voidedBy;
	}	public void setDateVoided(java.util.Date dateVoided){ 
	 	this.dateVoided = dateVoided;
	}
 
	public java.util.Date getDateVoided(){ 
		return this.dateVoided;
	}	public void setVoidReason(String voidReason){ 
	 	this.voidReason = voidReason;
	}
 
	public String getVoidReason(){ 
		return this.voidReason;
	}	public void setUuid(String uuid){ 
	 	this.uuid = uuid;
	}
 
	public String getUuid(){ 
		return this.uuid;
	}	public void setDeathdateEstimated(byte deathdateEstimated){ 
	 	this.deathdateEstimated = deathdateEstimated;
	}
 
	public byte getDeathdateEstimated(){ 
		return this.deathdateEstimated;
	}	public void setBirthtime(java.util.Date birthtime){ 
	 	this.birthtime = birthtime;
	}
 
	public java.util.Date getBirthtime(){ 
		return this.birthtime;
	}	public void setLastSyncDate(java.util.Date lastSyncDate){ 
	 	this.lastSyncDate = lastSyncDate;
	}
 
	public java.util.Date getLastSyncDate(){ 
		return this.lastSyncDate;
	}	public void setOriginRecordId(int originRecordId){ 
	 	this.originRecordId = originRecordId;
	}
 
	public int getOriginRecordId(){ 
		return this.originRecordId;
	}	public void setOriginAppLocationCode(String originAppLocationCode){ 
	 	this.originAppLocationCode = originAppLocationCode;
	}


 
	public String getOriginAppLocationCode(){ 
		return this.originAppLocationCode;
	}
 
	public int getObjectId() { 
 		return this.personId; 
	} 
 
	public void setObjectId(int selfId){ 
		this.personId = selfId; 
	} 
 
	public void refreshLastSyncDate(OpenConnection conn){ 
		try{
			GenericSyncRecordDAO.refreshLastSyncDate(this, conn); 
		}catch(DBException e) {
			throw new RuntimeException(e);
		}
	}

	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "person_id"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParams(){ 
 		Object[] params = {this.gender, this.birthdate, this.birthdateEstimated, this.dead, this.deathDate, this.causeOfDeath == 0 ? null : this.causeOfDeath, this.creator == 0 ? null : this.creator, this.dateCreated, this.changedBy == 0 ? null : this.changedBy, this.dateChanged, this.voided, this.voidedBy == 0 ? null : this.voidedBy, this.dateVoided, this.voidReason, this.uuid, this.deathdateEstimated, this.birthtime, this.lastSyncDate, this.originRecordId, this.originAppLocationCode};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.gender, this.birthdate, this.birthdateEstimated, this.dead, this.deathDate, this.causeOfDeath == 0 ? null : this.causeOfDeath, this.creator == 0 ? null : this.creator, this.dateCreated, this.changedBy == 0 ? null : this.changedBy, this.dateChanged, this.voided, this.voidedBy == 0 ? null : this.voidedBy, this.dateVoided, this.voidReason, this.uuid, this.deathdateEstimated, this.birthtime, this.lastSyncDate, this.originRecordId, this.originAppLocationCode, this.personId};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQL(){ 
 		return "INSERT INTO person(gender, birthdate, birthdate_estimated, dead, death_date, cause_of_death, creator, date_created, changed_by, date_changed, voided, voided_by, date_voided, void_reason, uuid, deathdate_estimated, birthtime, last_sync_date, origin_record_id, origin_app_location_code) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE person SET gender = ?, birthdate = ?, birthdate_estimated = ?, dead = ?, death_date = ?, cause_of_death = ?, creator = ?, date_created = ?, changed_by = ?, date_changed = ?, voided = ?, voided_by = ?, date_voided = ?, void_reason = ?, uuid = ?, deathdate_estimated = ?, birthtime = ?, last_sync_date = ?, origin_record_id = ?, origin_app_location_code = ? WHERE person_id = ?;"; 
	} 
 
	@JsonIgnore
	public int getMainParentId(){ 
 		return creator; 
	} 
 
	public void setMainParentId(int mainParentId){ 
 		this.creator = mainParentId; 
	} 
 
	@JsonIgnore
	public String getMainParentTable(){ 
 		return "users";
	} 
 
	public void loadDestParentInfo(Connection conn) throws ParentNotYetMigratedException, DBException {
		OpenMRSObject parentOnDestination = null;
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.UsersVO.class, this.creator, false, conn); 
	this.creator = 0;
		if (parentOnDestination  != null) this.creator = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.UsersVO.class, this.changedBy, true, conn); 
	this.changedBy = 0;
		if (parentOnDestination  != null) this.changedBy = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.UsersVO.class, this.voidedBy, true, conn); 
	this.voidedBy = 0;
		if (parentOnDestination  != null) this.voidedBy = parentOnDestination.getObjectId();
 
	}
}