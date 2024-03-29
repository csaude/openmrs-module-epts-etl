package org.openmrs.module.epts.etl.model.pojo.openmrs._default;

import java.sql.SQLException;

import org.openmrs.module.epts.etl.model.pojo.generic.*;
import org.openmrs.module.epts.etl.utilities.AttDefinedElements;
import org.openmrs.module.epts.etl.utilities.DateAndTimeUtilities;

import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class PersonNameVO extends AbstractDatabaseObject implements DatabaseObject { 
	private Integer personNameId;
	private byte preferred;
	private Integer personId;
	private String prefix;
	private String givenName;
	private String middleName;
	private String familyNamePrefix;
	private String familyName;
	private String familyName2;
	private String familyNameSuffix;
	private String degree;
	private Integer creator;
	private byte voided;
	private Integer voidedBy;
	private java.util.Date dateVoided;
	private String voidReason;
	private Integer changedBy;
 
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
 
	public void setPrefix(String prefix){ 
	 	this.prefix = prefix;
	}
 
	public String getPrefix(){ 
		return this.prefix;
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
 
	public void setFamilyNamePrefix(String familyNamePrefix){ 
	 	this.familyNamePrefix = familyNamePrefix;
	}
 
	public String getFamilyNamePrefix(){ 
		return this.familyNamePrefix;
	}
 
	public void setFamilyName(String familyName){ 
	 	this.familyName = familyName;
	}
 
	public String getFamilyName(){ 
		return this.familyName;
	}
 
	public void setFamilyName2(String familyName2){ 
	 	this.familyName2 = familyName2;
	}
 
	public String getFamilyName2(){ 
		return this.familyName2;
	}
 
	public void setFamilyNameSuffix(String familyNameSuffix){ 
	 	this.familyNameSuffix = familyNameSuffix;
	}
 
	public String getFamilyNameSuffix(){ 
		return this.familyNameSuffix;
	}
 
	public void setDegree(String degree){ 
	 	this.degree = degree;
	}
 
	public String getDegree(){ 
		return this.degree;
	}
 
	public void setCreator(Integer creator){ 
	 	this.creator = creator;
	}
 
	public Integer getCreator(){ 
		return this.creator;
	}
 
	public void setVoided(byte voided){ 
	 	this.voided = voided;
	}
 
	public byte getVoided(){ 
		return this.voided;
	}
 
	public void setVoidedBy(Integer voidedBy){ 
	 	this.voidedBy = voidedBy;
	}
 
	public Integer getVoidedBy(){ 
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
 
	public void setChangedBy(Integer changedBy){ 
	 	this.changedBy = changedBy;
	}
 
	public Integer getChangedBy(){ 
		return this.changedBy;
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
		this.prefix = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("prefix") != null ? rs.getString("prefix").trim() : null);
		this.givenName = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("given_name") != null ? rs.getString("given_name").trim() : null);
		this.middleName = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("middle_name") != null ? rs.getString("middle_name").trim() : null);
		this.familyNamePrefix = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("family_name_prefix") != null ? rs.getString("family_name_prefix").trim() : null);
		this.familyName = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("family_name") != null ? rs.getString("family_name").trim() : null);
		this.familyName2 = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("family_name2") != null ? rs.getString("family_name2").trim() : null);
		this.familyNameSuffix = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("family_name_suffix") != null ? rs.getString("family_name_suffix").trim() : null);
		this.degree = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("degree") != null ? rs.getString("degree").trim() : null);
		if (rs.getObject("creator") != null) this.creator = rs.getInt("creator");
		this.dateCreated =  rs.getTimestamp("date_created") != null ? new java.util.Date( rs.getTimestamp("date_created").getTime() ) : null;
		this.voided = rs.getByte("voided");
		if (rs.getObject("voided_by") != null) this.voidedBy = rs.getInt("voided_by");
		this.dateVoided =  rs.getTimestamp("date_voided") != null ? new java.util.Date( rs.getTimestamp("date_voided").getTime() ) : null;
		this.voidReason = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("void_reason") != null ? rs.getString("void_reason").trim() : null);
		if (rs.getObject("changed_by") != null) this.changedBy = rs.getInt("changed_by");
		this.dateChanged =  rs.getTimestamp("date_changed") != null ? new java.util.Date( rs.getTimestamp("date_changed").getTime() ) : null;
		this.uuid = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("uuid") != null ? rs.getString("uuid").trim() : null);
	} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "person_name_id"; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithoutObjectId(){ 
 		return "INSERT INTO person_name(preferred, person_id, prefix, given_name, middle_name, family_name_prefix, family_name, family_name2, family_name_suffix, degree, creator, date_created, voided, voided_by, date_voided, void_reason, changed_by, date_changed, uuid) VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithoutObjectId(){ 
 		Object[] params = {this.preferred, this.personId, this.prefix, this.givenName, this.middleName, this.familyNamePrefix, this.familyName, this.familyName2, this.familyNameSuffix, this.degree, this.creator, this.dateCreated, this.voided, this.voidedBy, this.dateVoided, this.voidReason, this.changedBy, this.dateChanged, this.uuid};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithObjectId(){ 
 		return "INSERT INTO person_name(person_name_id, preferred, person_id, prefix, given_name, middle_name, family_name_prefix, family_name, family_name2, family_name_suffix, degree, creator, date_created, voided, voided_by, date_voided, void_reason, changed_by, date_changed, uuid) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithObjectId(){ 
 		Object[] params = {this.personNameId, this.preferred, this.personId, this.prefix, this.givenName, this.middleName, this.familyNamePrefix, this.familyName, this.familyName2, this.familyNameSuffix, this.degree, this.creator, this.dateCreated, this.voided, this.voidedBy, this.dateVoided, this.voidReason, this.changedBy, this.dateChanged, this.uuid};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.preferred, this.personId, this.prefix, this.givenName, this.middleName, this.familyNamePrefix, this.familyName, this.familyName2, this.familyNameSuffix, this.degree, this.creator, this.dateCreated, this.voided, this.voidedBy, this.dateVoided, this.voidReason, this.changedBy, this.dateChanged, this.uuid, this.personNameId};		return params; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE person_name SET preferred = ?, person_id = ?, prefix = ?, given_name = ?, middle_name = ?, family_name_prefix = ?, family_name = ?, family_name2 = ?, family_name_suffix = ?, degree = ?, creator = ?, date_created = ?, voided = ?, voided_by = ?, date_voided = ?, void_reason = ?, changed_by = ?, date_changed = ?, uuid = ? WHERE person_name_id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.preferred) + "," + (this.personId) + "," + (this.prefix != null ? "\""+ utilities.scapeQuotationMarks(prefix)  +"\"" : null) + "," + (this.givenName != null ? "\""+ utilities.scapeQuotationMarks(givenName)  +"\"" : null) + "," + (this.middleName != null ? "\""+ utilities.scapeQuotationMarks(middleName)  +"\"" : null) + "," + (this.familyNamePrefix != null ? "\""+ utilities.scapeQuotationMarks(familyNamePrefix)  +"\"" : null) + "," + (this.familyName != null ? "\""+ utilities.scapeQuotationMarks(familyName)  +"\"" : null) + "," + (this.familyName2 != null ? "\""+ utilities.scapeQuotationMarks(familyName2)  +"\"" : null) + "," + (this.familyNameSuffix != null ? "\""+ utilities.scapeQuotationMarks(familyNameSuffix)  +"\"" : null) + "," + (this.degree != null ? "\""+ utilities.scapeQuotationMarks(degree)  +"\"" : null) + "," + (this.creator) + "," + (this.dateCreated != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateCreated)  +"\"" : null) + "," + (this.voided) + "," + (this.voidedBy) + "," + (this.dateVoided != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateVoided)  +"\"" : null) + "," + (this.voidReason != null ? "\""+ utilities.scapeQuotationMarks(voidReason)  +"\"" : null) + "," + (this.changedBy) + "," + (this.dateChanged != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateChanged)  +"\"" : null) + "," + (this.uuid != null ? "\""+ utilities.scapeQuotationMarks(uuid)  +"\"" : null); 
	} 
 
	@Override
	public boolean hasParents() {
		if (this.personId != 0) return true;

		if (this.creator != 0) return true;

		if (this.voidedBy != 0) return true;

		return false;
	}

	@Override
	public Integer getParentValue(String parentAttName) {		
		if (parentAttName.equals("personId")) return this.personId;		
		if (parentAttName.equals("creator")) return this.creator;		
		if (parentAttName.equals("voidedBy")) return this.voidedBy;

		throw new RuntimeException("No found parent for: " + parentAttName);	}

	@Override
	public void changeParentValue(String parentAttName, DatabaseObject newParent) {		
		if (parentAttName.equals("personId")) {
			this.personId = newParent.getObjectId();
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
		if (parentAttName.equals("voidedBy")) {
			this.voidedBy = null;
			return;
		}

		throw new RuntimeException("No found parent for: " + parentAttName);
	}


}