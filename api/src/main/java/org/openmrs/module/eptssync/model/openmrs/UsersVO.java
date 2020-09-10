package org.openmrs.module.eptssync.model.openmrs; 
 
import org.openmrs.module.eptssync.model.OpenMRSObject; 
import org.openmrs.module.eptssync.model.OpenMRSObjectDAO; 
import org.openmrs.module.eptssync.model.GenericSyncRecordDAO; 
 
import org.openmrs.module.eptssync.model.base.BaseVO; 
 
import org.openmrs.module.eptssync.utilities.db.conn.DBException; 
import org.openmrs.module.eptssync.utilities.db.conn.OpenConnection; 
 
import java.sql.Connection; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class UsersVO extends BaseVO implements OpenMRSObject { 
	private int userId;
	private String systemId;
	private String username;
	private String password;
	private String salt;
	private String secretQuestion;
	private String secretAnswer;
	private int creator;
	private java.util.Date dateCreated;
	private int changedBy;
	private java.util.Date dateChanged;
	private int personId;
	private byte retired;
	private int retiredBy;
	private java.util.Date dateRetired;
	private String retireReason;
	private String uuid;
	private java.util.Date lastSyncDate;
	private int originRecordId;
	private String originAppLocationCode;
 
	public UsersVO() { 
	} 
 
	public void setUserId(int userId){ 
	 	this.userId = userId;
	}
 
	public int getUserId(){ 
		return this.userId;
	}	public void setSystemId(String systemId){ 
	 	this.systemId = systemId;
	}
 
	public String getSystemId(){ 
		return this.systemId;
	}	public void setUsername(String username){ 
	 	this.username = username;
	}
 
	public String getUsername(){ 
		return this.username;
	}	public void setPassword(String password){ 
	 	this.password = password;
	}
 
	public String getPassword(){ 
		return this.password;
	}	public void setSalt(String salt){ 
	 	this.salt = salt;
	}
 
	public String getSalt(){ 
		return this.salt;
	}	public void setSecretQuestion(String secretQuestion){ 
	 	this.secretQuestion = secretQuestion;
	}
 
	public String getSecretQuestion(){ 
		return this.secretQuestion;
	}	public void setSecretAnswer(String secretAnswer){ 
	 	this.secretAnswer = secretAnswer;
	}
 
	public String getSecretAnswer(){ 
		return this.secretAnswer;
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
	}	public void setPersonId(int personId){ 
	 	this.personId = personId;
	}
 
	public int getPersonId(){ 
		return this.personId;
	}	public void setRetired(byte retired){ 
	 	this.retired = retired;
	}
 
	public byte getRetired(){ 
		return this.retired;
	}	public void setRetiredBy(int retiredBy){ 
	 	this.retiredBy = retiredBy;
	}
 
	public int getRetiredBy(){ 
		return this.retiredBy;
	}	public void setDateRetired(java.util.Date dateRetired){ 
	 	this.dateRetired = dateRetired;
	}
 
	public java.util.Date getDateRetired(){ 
		return this.dateRetired;
	}	public void setRetireReason(String retireReason){ 
	 	this.retireReason = retireReason;
	}
 
	public String getRetireReason(){ 
		return this.retireReason;
	}	public void setUuid(String uuid){ 
	 	this.uuid = uuid;
	}
 
	public String getUuid(){ 
		return this.uuid;
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
 		return this.userId; 
	} 
 
	public void setObjectId(int selfId){ 
		this.userId = selfId; 
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
 		return "user_id"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParams(){ 
 		Object[] params = {this.systemId, this.username, this.password, this.salt, this.secretQuestion, this.secretAnswer, this.creator, this.dateCreated, this.changedBy, this.dateChanged, this.personId, this.retired, this.retiredBy, this.dateRetired, this.retireReason, this.uuid, this.lastSyncDate, this.originRecordId, this.originAppLocationCode};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.systemId, this.username, this.password, this.salt, this.secretQuestion, this.secretAnswer, this.creator, this.dateCreated, this.changedBy, this.dateChanged, this.personId, this.retired, this.retiredBy, this.dateRetired, this.retireReason, this.uuid, this.lastSyncDate, this.originRecordId, this.originAppLocationCode, this.userId};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQL(){ 
 		return "INSERT INTO users(system_id, username, password, salt, secret_question, secret_answer, creator, date_created, changed_by, date_changed, person_id, retired, retired_by, date_retired, retire_reason, uuid, last_sync_date, origin_record_id, origin_app_location_code) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE users SET system_id = ?, username = ?, password = ?, salt = ?, secret_question = ?, secret_answer = ?, creator = ?, date_created = ?, changed_by = ?, date_changed = ?, person_id = ?, retired = ?, retired_by = ?, date_retired = ?, retire_reason = ?, uuid = ?, last_sync_date = ?, origin_record_id = ?, origin_app_location_code = ? WHERE user_id = ?;"; 
	} 
 
	@JsonIgnore
	public int getMainParentId(){ 
 		return 0; 
	} 
 
	public void setMainParentId(int mainParentId){ 
 	} 
 
	@JsonIgnore
	public String getMainParentTable(){ 
 		return null;
	} 
 
	public void loadDestParentInfo(Connection conn) throws DBException {
		OpenMRSObject parentOnDestination = null;
 
	}
}