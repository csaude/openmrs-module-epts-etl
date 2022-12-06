package org.openmrs.module.eptssync.model.pojo.merge_test_src_db;

import org.openmrs.module.eptssync.model.pojo.generic.*; 
 
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.eptssync.utilities.AttDefinedElements; 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class PersonVO extends AbstractDatabaseObject implements DatabaseObject { 
	private Integer personId;
	private String gender;
	private java.util.Date birthdate;
	private String personUuid;
	private java.util.Date updatedOn;
 
	public PersonVO() { 
		this.metadata = false;
	} 
 
	public void setPersonId(Integer personId){ 
	 	this.personId = personId;
	}
 
	public Integer getPersonId(){ 
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
 
	public void setPersonUuid(String personUuid){ 
	 	this.personUuid = personUuid;
	}
 
	public String getPersonUuid(){ 
		return this.personUuid;
	}
 
	public void setUpdatedOn(java.util.Date updatedOn){ 
	 	this.updatedOn = updatedOn;
	}


 
	public java.util.Date getUpdatedOn(){ 
		return this.updatedOn;
	}
 
	public Integer getObjectId() { 
 		return this.personId; 
	} 
 
	public void setObjectId(Integer selfId){ 
		this.personId = selfId; 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		super.load(rs);
		if (rs.getObject("person_id") != null) this.personId = rs.getInt("person_id");
		this.gender = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("gender") != null ? rs.getString("gender").trim() : null);
		this.birthdate =  rs.getTimestamp("birthdate") != null ? new java.util.Date( rs.getTimestamp("birthdate").getTime() ) : null;
		this.personUuid = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("person_uuid") != null ? rs.getString("person_uuid").trim() : null);
		this.updatedOn =  rs.getTimestamp("updated_on") != null ? new java.util.Date( rs.getTimestamp("updated_on").getTime() ) : null;
	} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "person_id"; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithoutObjectId(){ 
 		return "INSERT INTO person(gender, birthdate, person_uuid, updated_on) VALUES( ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithoutObjectId(){ 
 		Object[] params = {this.gender, this.birthdate, this.personUuid, this.updatedOn};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithObjectId(){ 
 		return "INSERT INTO person(person_id, gender, birthdate, person_uuid, updated_on) VALUES(?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithObjectId(){ 
 		Object[] params = {this.personId, this.gender, this.birthdate, this.personUuid, this.updatedOn};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.gender, this.birthdate, this.personUuid, this.updatedOn, this.personId};		return params; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE person SET gender = ?, birthdate = ?, person_uuid = ?, updated_on = ? WHERE person_id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.gender != null ? "\""+ utilities.scapeQuotationMarks(gender)  +"\"" : null) + "," + (this.birthdate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(birthdate)  +"\"" : null) + "," + (this.personUuid != null ? "\""+ utilities.scapeQuotationMarks(personUuid)  +"\"" : null) + "," + (this.updatedOn != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(updatedOn)  +"\"" : null); 
	} 
 
	@Override
	public boolean hasParents() {
		return false;
	}

	@Override
	public Integer getParentValue(String parentAttName) {

		throw new RuntimeException("No found parent for: " + parentAttName);	}

	@Override
	public void changeParentValue(String parentAttName, DatabaseObject newParent) {

		throw new RuntimeException("No found parent for: " + parentAttName);
	}

	@Override
	public void setParentToNull(String parentAttName) {

		throw new RuntimeException("No found parent for: " + parentAttName);
	}


}