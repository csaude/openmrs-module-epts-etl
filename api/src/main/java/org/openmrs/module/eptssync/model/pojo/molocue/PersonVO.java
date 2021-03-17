package org.openmrs.module.eptssync.model.pojo.molocue; 
 
import org.openmrs.module.eptssync.model.pojo.generic.*; 
 
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.eptssync.utilities.AttDefinedElements; 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
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
	private int changedBy;
	private byte voided;
	private int voidedBy;
	private java.util.Date dateVoided;
	private String voidReason;
	private byte deathdateEstimated;
	private java.util.Date birthtime;
 
	public PersonVO() { 
		this.metadata = false;
	} 
 
	public void setPersonId(int personId){ 
	 	this.personId = personId;
	}
 
	public int getPersonId(){ 
		return this.personId;
	}
 
	public void setGender(String gender){ 
	 	this.gender = gender;
	}
 
	public String getGender(){ 
		return this.gender;
	}
 
	public void setBirthdate(java.util.Date birthdate){ 
	 	this.birthdate = birthdate;
	}
 
	public java.util.Date getBirthdate(){ 
		return this.birthdate;
	}
 
	public void setBirthdateEstimated(byte birthdateEstimated){ 
	 	this.birthdateEstimated = birthdateEstimated;
	}
 
	public byte getBirthdateEstimated(){ 
		return this.birthdateEstimated;
	}
 
	public void setDead(byte dead){ 
	 	this.dead = dead;
	}
 
	public byte getDead(){ 
		return this.dead;
	}
 
	public void setDeathDate(java.util.Date deathDate){ 
	 	this.deathDate = deathDate;
	}
 
	public java.util.Date getDeathDate(){ 
		return this.deathDate;
	}
 
	public void setCauseOfDeath(int causeOfDeath){ 
	 	this.causeOfDeath = causeOfDeath;
	}
 
	public int getCauseOfDeath(){ 
		return this.causeOfDeath;
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
 
	public void setVoidedBy(int voidedBy){ 
	 	this.voidedBy = voidedBy;
	}
 
	public int getVoidedBy(){ 
		return this.voidedBy;
	}
 
	public void setDateVoided(java.util.Date dateVoided){ 
	 	this.dateVoided = dateVoided;
	}
 
	public java.util.Date getDateVoided(){ 
		return this.dateVoided;
	}
 
	public void setVoidReason(String voidReason){ 
	 	this.voidReason = voidReason;
	}
 
	public String getVoidReason(){ 
		return this.voidReason;
	}
 
	public void setDeathdateEstimated(byte deathdateEstimated){ 
	 	this.deathdateEstimated = deathdateEstimated;
	}
 
	public byte getDeathdateEstimated(){ 
		return this.deathdateEstimated;
	}
 
	public void setBirthtime(java.util.Date birthtime){ 
	 	this.birthtime = birthtime;
	}


 
	public java.util.Date getBirthtime(){ 
		return this.birthtime;
	}
 
	public int getObjectId() { 
 		return this.personId; 
	} 
 
	public void setObjectId(int selfId){ 
		this.personId = selfId; 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		this.personId = rs.getInt("person_id");
		this.gender = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("gender") != null ? rs.getString("gender").trim() : null);
		this.birthdate =  rs.getTimestamp("birthdate") != null ? new java.util.Date( rs.getTimestamp("birthdate").getTime() ) : null;
		this.birthdateEstimated = rs.getByte("birthdate_estimated");
		this.dead = rs.getByte("dead");
		this.deathDate =  rs.getTimestamp("death_date") != null ? new java.util.Date( rs.getTimestamp("death_date").getTime() ) : null;
		this.causeOfDeath = rs.getInt("cause_of_death");
		this.creator = rs.getInt("creator");
		this.dateCreated =  rs.getTimestamp("date_created") != null ? new java.util.Date( rs.getTimestamp("date_created").getTime() ) : null;
		this.changedBy = rs.getInt("changed_by");
		this.dateChanged =  rs.getTimestamp("date_changed") != null ? new java.util.Date( rs.getTimestamp("date_changed").getTime() ) : null;
		this.voided = rs.getByte("voided");
		this.voidedBy = rs.getInt("voided_by");
		this.dateVoided =  rs.getTimestamp("date_voided") != null ? new java.util.Date( rs.getTimestamp("date_voided").getTime() ) : null;
		this.voidReason = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("void_reason") != null ? rs.getString("void_reason").trim() : null);
		this.uuid = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("uuid") != null ? rs.getString("uuid").trim() : null);
		this.deathdateEstimated = rs.getByte("deathdate_estimated");
		this.birthtime =  rs.getTimestamp("birthtime") != null ? new java.util.Date( rs.getTimestamp("birthtime").getTime() ) : null;
	} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "person_id"; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithoutObjectId(){ 
 		return "INSERT INTO person(gender, birthdate, birthdate_estimated, dead, death_date, cause_of_death, creator, date_created, changed_by, date_changed, voided, voided_by, date_voided, void_reason, uuid, deathdate_estimated, birthtime) VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithoutObjectId(){ 
 		Object[] params = {this.gender, this.birthdate, this.birthdateEstimated, this.dead, this.deathDate, this.causeOfDeath == 0 ? null : this.causeOfDeath, this.creator == 0 ? null : this.creator, this.dateCreated, this.changedBy == 0 ? null : this.changedBy, this.dateChanged, this.voided, this.voidedBy == 0 ? null : this.voidedBy, this.dateVoided, this.voidReason, this.uuid, this.deathdateEstimated, this.birthtime};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithObjectId(){ 
 		return "INSERT INTO person(person_id, gender, birthdate, birthdate_estimated, dead, death_date, cause_of_death, creator, date_created, changed_by, date_changed, voided, voided_by, date_voided, void_reason, uuid, deathdate_estimated, birthtime) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithObjectId(){ 
 		Object[] params = {this.personId, this.gender, this.birthdate, this.birthdateEstimated, this.dead, this.deathDate, this.causeOfDeath == 0 ? null : this.causeOfDeath, this.creator == 0 ? null : this.creator, this.dateCreated, this.changedBy == 0 ? null : this.changedBy, this.dateChanged, this.voided, this.voidedBy == 0 ? null : this.voidedBy, this.dateVoided, this.voidReason, this.uuid, this.deathdateEstimated, this.birthtime};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.gender, this.birthdate, this.birthdateEstimated, this.dead, this.deathDate, this.causeOfDeath == 0 ? null : this.causeOfDeath, this.creator == 0 ? null : this.creator, this.dateCreated, this.changedBy == 0 ? null : this.changedBy, this.dateChanged, this.voided, this.voidedBy == 0 ? null : this.voidedBy, this.dateVoided, this.voidReason, this.uuid, this.deathdateEstimated, this.birthtime, this.personId};		return params; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE person SET gender = ?, birthdate = ?, birthdate_estimated = ?, dead = ?, death_date = ?, cause_of_death = ?, creator = ?, date_created = ?, changed_by = ?, date_changed = ?, voided = ?, voided_by = ?, date_voided = ?, void_reason = ?, uuid = ?, deathdate_estimated = ?, birthtime = ? WHERE person_id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.gender != null ? "\""+ utilities.scapeQuotationMarks(gender)  +"\"" : null) + "," + (this.birthdate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(birthdate)  +"\"" : null) + "," + (this.birthdateEstimated) + "," + (this.dead) + "," + (this.deathDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(deathDate)  +"\"" : null) + "," + (this.causeOfDeath == 0 ? null : this.causeOfDeath) + "," + (this.creator == 0 ? null : this.creator) + "," + (this.dateCreated != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateCreated)  +"\"" : null) + "," + (this.changedBy == 0 ? null : this.changedBy) + "," + (this.dateChanged != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateChanged)  +"\"" : null) + "," + (this.voided) + "," + (this.voidedBy == 0 ? null : this.voidedBy) + "," + (this.dateVoided != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateVoided)  +"\"" : null) + "," + (this.voidReason != null ? "\""+ utilities.scapeQuotationMarks(voidReason)  +"\"" : null) + "," + (this.uuid != null ? "\""+ utilities.scapeQuotationMarks(uuid)  +"\"" : null) + "," + (this.deathdateEstimated) + "," + (this.birthtime != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(birthtime)  +"\"" : null); 
	} 
 
	@Override
	public boolean hasParents() {
		if (this.causeOfDeath != 0) return true;

		if (this.changedBy != 0) return true;

		if (this.creator != 0) return true;

		if (this.voidedBy != 0) return true;

		return false;
	}

	@Override
	public int getParentValue(String parentAttName) {		
		if (parentAttName.equals("causeOfDeath")) return this.causeOfDeath;		
		if (parentAttName.equals("changedBy")) return this.changedBy;		
		if (parentAttName.equals("creator")) return this.creator;		
		if (parentAttName.equals("voidedBy")) return this.voidedBy;

		throw new RuntimeException("No found parent for: " + parentAttName);	}

	@Override
	public void changeParentValue(String parentAttName, OpenMRSObject newParent) {		
		if (parentAttName.equals("causeOfDeath")) {
			this.causeOfDeath = newParent.getObjectId();
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

		throw new RuntimeException("No found parent for: " + parentAttName);
	}


}