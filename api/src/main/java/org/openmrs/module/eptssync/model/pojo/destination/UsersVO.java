package org.openmrs.module.eptssync.model.pojo.destination;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.openmrs.module.eptssync.model.pojo.generic.AbstractOpenMRSObject;
import org.openmrs.module.eptssync.model.pojo.generic.OpenMRSObject;
import org.openmrs.module.eptssync.utilities.AttDefinedElements;
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities;

import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class UsersVO extends AbstractOpenMRSObject implements OpenMRSObject { 
	private Integer userId;
	private String systemId;
	private String username;
	private String password;
	private String salt;
	private String secretQuestion;
	private String secretAnswer;
	private Integer creator;
	private Integer changedBy;
	private Integer personId;
	private byte retired;
	private Integer retiredBy;
	private java.util.Date dateRetired;
	private String retireReason;
	private String activationKey;
	private String email;
 
	public UsersVO() { 
		this.metadata = false;
	} 
 
	public void setUserId(Integer userId){ 
	 	this.userId = userId;
	}
 
	public Integer getUserId(){ 
		return this.userId;
	}
 
	public void setSystemId(String systemId){ 
	 	this.systemId = systemId;
	}
 
	public String getSystemId(){ 
		return this.systemId;
	}
 
	public void setUsername(String username){ 
	 	this.username = username;
	}
 
	public String getUsername(){ 
		return this.username;
	}
 
	public void setPassword(String password){ 
	 	this.password = password;
	}
 
	public String getPassword(){ 
		return this.password;
	}
 
	public void setSalt(String salt){ 
	 	this.salt = salt;
	}
 
	public String getSalt(){ 
		return this.salt;
	}
 
	public void setSecretQuestion(String secretQuestion){ 
	 	this.secretQuestion = secretQuestion;
	}
 
	public String getSecretQuestion(){ 
		return this.secretQuestion;
	}
 
	public void setSecretAnswer(String secretAnswer){ 
	 	this.secretAnswer = secretAnswer;
	}
 
	public String getSecretAnswer(){ 
		return this.secretAnswer;
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
 
	public void setPersonId(Integer personId){ 
	 	this.personId = personId;
	}
 
	public Integer getPersonId(){ 
		return this.personId;
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
 
	public void setActivationKey(String activationKey){ 
	 	this.activationKey = activationKey;
	}
 
	public String getActivationKey(){ 
		return this.activationKey;
	}
 
	public void setEmail(String email){ 
	 	this.email = email;
	}


 
	public String getEmail(){ 
		return this.email;
	}
 
	public Integer getObjectId() { 
 		return this.userId; 
	} 
 
	public void setObjectId(Integer selfId){ 
		this.userId = selfId; 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		super.load(rs);
		try {
			if (rs.getObject("user_id") != null) this.userId = rs.getInt("user_id");
			this.systemId = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("system_id") != null ? rs.getString("system_id").trim() : null);
			this.username = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("username") != null ? rs.getString("username").trim() : null);
			this.password = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("password") != null ? rs.getString("password").trim() : null);
			this.salt = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("salt") != null ? rs.getString("salt").trim() : null);
			this.secretQuestion = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("secret_question") != null ? rs.getString("secret_question").trim() : null);
			this.secretAnswer = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("secret_answer") != null ? rs.getString("secret_answer").trim() : null);
			if (rs.getObject("creator") != null) this.creator = rs.getInt("creator");
			this.dateCreated =  rs.getTimestamp("date_created") != null ? new java.util.Date( rs.getTimestamp("date_created").getTime() ) : null;
			if (rs.getObject("changed_by") != null) this.changedBy = rs.getInt("changed_by");
			this.dateChanged =  rs.getTimestamp("date_changed") != null ? new java.util.Date( rs.getTimestamp("date_changed").getTime() ) : null;
			if (rs.getObject("person_id") != null) this.personId = rs.getInt("person_id");
			this.retired = rs.getByte("retired");
			if (rs.getObject("retired_by") != null) this.retiredBy = rs.getInt("retired_by");
			this.dateRetired =  rs.getTimestamp("date_retired") != null ? new java.util.Date( rs.getTimestamp("date_retired").getTime() ) : null;
			this.retireReason = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("retire_reason") != null ? rs.getString("retire_reason").trim() : null);
			this.uuid = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("uuid") != null ? rs.getString("uuid").trim() : null);
			this.activationKey = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("activation_key") != null ? rs.getString("activation_key").trim() : null);
			this.email = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("email") != null ? rs.getString("email").trim() : null);
		}
		catch (SQLException e) {
			
		}
	} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "user_id"; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithoutObjectId(){ 
 		return "INSERT INTO users(system_id, username, password, salt, secret_question, secret_answer, creator, date_created, changed_by, date_changed, person_id, retired, retired_by, date_retired, retire_reason, uuid, activation_key, email) VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithoutObjectId(){ 
 		Object[] params = {this.systemId, this.username, this.password, this.salt, this.secretQuestion, this.secretAnswer, this.creator, this.dateCreated, this.changedBy, this.dateChanged, this.personId, this.retired, this.retiredBy, this.dateRetired, this.retireReason, this.uuid, this.activationKey, this.email};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithObjectId(){ 
 		return "INSERT INTO users(user_id, system_id, username, password, salt, secret_question, secret_answer, creator, date_created, changed_by, date_changed, person_id, retired, retired_by, date_retired, retire_reason, uuid, activation_key, email) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithObjectId(){ 
 		Object[] params = {this.userId, this.systemId, this.username, this.password, this.salt, this.secretQuestion, this.secretAnswer, this.creator, this.dateCreated, this.changedBy, this.dateChanged, this.personId, this.retired, this.retiredBy, this.dateRetired, this.retireReason, this.uuid, this.activationKey, this.email};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.systemId, this.username, this.password, this.salt, this.secretQuestion, this.secretAnswer, this.creator, this.dateCreated, this.changedBy, this.dateChanged, this.personId, this.retired, this.retiredBy, this.dateRetired, this.retireReason, this.uuid, this.activationKey, this.email, this.userId};		return params; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE users SET system_id = ?, username = ?, password = ?, salt = ?, secret_question = ?, secret_answer = ?, creator = ?, date_created = ?, changed_by = ?, date_changed = ?, person_id = ?, retired = ?, retired_by = ?, date_retired = ?, retire_reason = ?, uuid = ?, activation_key = ?, email = ? WHERE user_id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.systemId != null ? "\""+ utilities.scapeQuotationMarks(systemId)  +"\"" : null) + "," + (this.username != null ? "\""+ utilities.scapeQuotationMarks(username)  +"\"" : null) + "," + (this.password != null ? "\""+ utilities.scapeQuotationMarks(password)  +"\"" : null) + "," + (this.salt != null ? "\""+ utilities.scapeQuotationMarks(salt)  +"\"" : null) + "," + (this.secretQuestion != null ? "\""+ utilities.scapeQuotationMarks(secretQuestion)  +"\"" : null) + "," + (this.secretAnswer != null ? "\""+ utilities.scapeQuotationMarks(secretAnswer)  +"\"" : null) + "," + (this.creator) + "," + (this.dateCreated != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateCreated)  +"\"" : null) + "," + (this.changedBy) + "," + (this.dateChanged != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateChanged)  +"\"" : null) + "," + (this.personId) + "," + (this.retired) + "," + (this.retiredBy) + "," + (this.dateRetired != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateRetired)  +"\"" : null) + "," + (this.retireReason != null ? "\""+ utilities.scapeQuotationMarks(retireReason)  +"\"" : null) + "," + (this.uuid != null ? "\""+ utilities.scapeQuotationMarks(uuid)  +"\"" : null) + "," + (this.activationKey != null ? "\""+ utilities.scapeQuotationMarks(activationKey)  +"\"" : null) + "," + (this.email != null ? "\""+ utilities.scapeQuotationMarks(email)  +"\"" : null); 
	} 
 
	@Override
	public boolean hasParents() {
		if (this.personId != 0) return true;

		if (this.creator != 0) return true;

		if (this.changedBy != 0) return true;

		if (this.retiredBy != 0) return true;

		return false;
	}

	@Override
	public Integer getParentValue(String parentAttName) {		
		if (parentAttName.equals("personId")) return this.personId;		
		if (parentAttName.equals("creator")) return this.creator;		
		if (parentAttName.equals("changedBy")) return this.changedBy;		
		if (parentAttName.equals("retiredBy")) return this.retiredBy;

		throw new RuntimeException("No found parent for: " + parentAttName);	}

	@Override
	public void changeParentValue(String parentAttName, OpenMRSObject newParent) {		
		if (parentAttName.equals("personId")) {
			this.personId = newParent.getObjectId();
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
		if (parentAttName.equals("personId")) {
			this.personId = null;
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