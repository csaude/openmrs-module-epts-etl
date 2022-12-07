package org.openmrs.module.eptssync.model.pojo.merge_test_src_db;

import org.openmrs.module.eptssync.model.pojo.generic.*; 
 
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.eptssync.utilities.AttDefinedElements; 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class PersonNameVO extends AbstractDatabaseObject implements DatabaseObject { 
	private Integer personNameId;
	private byte preferred;
	private Integer personId;
	private String givenName;
	private String middleName;
	private String familyName;
	private String personNameUuid;
 
	public PersonNameVO() { 
		this.metadata = false;
	} 
 
	public void setPersonNameId(Integer personNameId){ 
	 	this.personNameId = personNameId;
	}
 
	public Integer getPersonNameId(){ 
		return this.personNameId;
	}
 
	public void setPreferred(byte preferred){ 
	 	this.preferred = preferred;
	}
 
	public byte getPreferred(){ 
		return this.preferred;
	}
 
	public void setPersonId(Integer personId){ 
	 	this.personId = personId;
	}
 
	public Integer getPersonId(){ 
		return this.personId;
	}
 
	public void setGivenName(String givenName){ 
	 	this.givenName = givenName;
	}
 
	public String getGivenName(){ 
		return this.givenName;
	}
 
	public void setMiddleName(String middleName){ 
	 	this.middleName = middleName;
	}
 
	public String getMiddleName(){ 
		return this.middleName;
	}
 
	public void setFamilyName(String familyName){ 
	 	this.familyName = familyName;
	}
 
	public String getFamilyName(){ 
		return this.familyName;
	}
 
	public void setPersonNameUuid(String personNameUuid){ 
	 	this.personNameUuid = personNameUuid;
	}


 
	public String getPersonNameUuid(){ 
		return this.personNameUuid;
	}
 
	public Integer getObjectId() { 
 		return this.personNameId; 
	} 
 
	public void setObjectId(Integer selfId){ 
		this.personNameId = selfId; 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		super.load(rs);
		if (rs.getObject("person_name_id") != null) this.personNameId = rs.getInt("person_name_id");
		this.preferred = rs.getByte("preferred");
		if (rs.getObject("person_id") != null) this.personId = rs.getInt("person_id");
		this.givenName = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("given_name") != null ? rs.getString("given_name").trim() : null);
		this.middleName = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("middle_name") != null ? rs.getString("middle_name").trim() : null);
		this.familyName = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("family_name") != null ? rs.getString("family_name").trim() : null);
		this.personNameUuid = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("person_name_uuid") != null ? rs.getString("person_name_uuid").trim() : null);
	} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "person_name_id"; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithoutObjectId(){ 
 		return "INSERT INTO person_name(preferred, person_id, given_name, middle_name, family_name, person_name_uuid) VALUES( ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithoutObjectId(){ 
 		Object[] params = {this.preferred, this.personId, this.givenName, this.middleName, this.familyName, this.personNameUuid};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithObjectId(){ 
 		return "INSERT INTO person_name(person_name_id, preferred, person_id, given_name, middle_name, family_name, person_name_uuid) VALUES(?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithObjectId(){ 
 		Object[] params = {this.personNameId, this.preferred, this.personId, this.givenName, this.middleName, this.familyName, this.personNameUuid};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.preferred, this.personId, this.givenName, this.middleName, this.familyName, this.personNameUuid, this.personNameId};		return params; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE person_name SET preferred = ?, person_id = ?, given_name = ?, middle_name = ?, family_name = ?, person_name_uuid = ? WHERE person_name_id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.preferred) + "," + (this.personId) + "," + (this.givenName != null ? "\""+ utilities.scapeQuotationMarks(givenName)  +"\"" : null) + "," + (this.middleName != null ? "\""+ utilities.scapeQuotationMarks(middleName)  +"\"" : null) + "," + (this.familyName != null ? "\""+ utilities.scapeQuotationMarks(familyName)  +"\"" : null) + "," + (this.personNameUuid != null ? "\""+ utilities.scapeQuotationMarks(personNameUuid)  +"\"" : null); 
	} 
 
	@Override
	public boolean hasParents() {
		if (this.personId != 0) return true;

		return false;
	}

	@Override
	public Integer getParentValue(String parentAttName) {		
		if (parentAttName.equals("personId")) return this.personId;

		throw new RuntimeException("No found parent for: " + parentAttName);	}

	@Override
	public void changeParentValue(String parentAttName, DatabaseObject newParent) {		
		if (parentAttName.equals("personId")) {
			this.personId = newParent.getObjectId();
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

		throw new RuntimeException("No found parent for: " + parentAttName);
	}


}