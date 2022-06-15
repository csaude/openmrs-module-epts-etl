package org.openmrs.module.eptssync.model.pojo.destination; 
 
import org.openmrs.module.eptssync.model.pojo.generic.*; 
 
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.eptssync.utilities.AttDefinedElements; 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class PersonAttributeVO extends AbstractOpenMRSObject implements OpenMRSObject { 
	private Integer personAttributeId;
	private Integer personId;
	private String value;
	private Integer personAttributeTypeId;
	private Integer creator;
	private Integer changedBy;
	private byte voided;
	private Integer voidedBy;
	private java.util.Date dateVoided;
	private String voidReason;
 
	public PersonAttributeVO() { 
		this.metadata = false;
	} 
 
	public void setPersonAttributeId(Integer personAttributeId){ 
	 	this.personAttributeId = personAttributeId;
	}
 
	public Integer getPersonAttributeId(){ 
		return this.personAttributeId;
	}
 
	public void setPersonId(Integer personId){ 
	 	this.personId = personId;
	}
 
	public Integer getPersonId(){ 
		return this.personId;
	}
 
	public void setValue(String value){ 
	 	this.value = value;
	}
 
	public String getValue(){ 
		return this.value;
	}
 
	public void setPersonAttributeTypeId(Integer personAttributeTypeId){ 
	 	this.personAttributeTypeId = personAttributeTypeId;
	}
 
	public Integer getPersonAttributeTypeId(){ 
		return this.personAttributeTypeId;
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
 

 
	public Integer getObjectId() { 
 		return this.personAttributeId; 
	} 
 
	public void setObjectId(Integer selfId){ 
		this.personAttributeId = selfId; 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		super.load(rs);
		if (rs.getObject("person_attribute_id") != null) this.personAttributeId = rs.getInt("person_attribute_id");
		if (rs.getObject("person_id") != null) this.personId = rs.getInt("person_id");
		this.value = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("value") != null ? rs.getString("value").trim() : null);
		if (rs.getObject("person_attribute_type_id") != null) this.personAttributeTypeId = rs.getInt("person_attribute_type_id");
		if (rs.getObject("creator") != null) this.creator = rs.getInt("creator");
		this.dateCreated =  rs.getTimestamp("date_created") != null ? new java.util.Date( rs.getTimestamp("date_created").getTime() ) : null;
		if (rs.getObject("changed_by") != null) this.changedBy = rs.getInt("changed_by");
		this.dateChanged =  rs.getTimestamp("date_changed") != null ? new java.util.Date( rs.getTimestamp("date_changed").getTime() ) : null;
		this.voided = rs.getByte("voided");
		if (rs.getObject("voided_by") != null) this.voidedBy = rs.getInt("voided_by");
		this.dateVoided =  rs.getTimestamp("date_voided") != null ? new java.util.Date( rs.getTimestamp("date_voided").getTime() ) : null;
		this.voidReason = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("void_reason") != null ? rs.getString("void_reason").trim() : null);
		this.uuid = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("uuid") != null ? rs.getString("uuid").trim() : null);
	} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "person_attribute_id"; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithoutObjectId(){ 
 		return "INSERT INTO person_attribute(person_id, value, person_attribute_type_id, creator, date_created, changed_by, date_changed, voided, voided_by, date_voided, void_reason, uuid) VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithoutObjectId(){ 
 		Object[] params = {this.personId, this.value, this.personAttributeTypeId, this.creator, this.dateCreated, this.changedBy, this.dateChanged, this.voided, this.voidedBy, this.dateVoided, this.voidReason, this.uuid};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithObjectId(){ 
 		return "INSERT INTO person_attribute(person_attribute_id, person_id, value, person_attribute_type_id, creator, date_created, changed_by, date_changed, voided, voided_by, date_voided, void_reason, uuid) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithObjectId(){ 
 		Object[] params = {this.personAttributeId, this.personId, this.value, this.personAttributeTypeId, this.creator, this.dateCreated, this.changedBy, this.dateChanged, this.voided, this.voidedBy, this.dateVoided, this.voidReason, this.uuid};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.personId, this.value, this.personAttributeTypeId, this.creator, this.dateCreated, this.changedBy, this.dateChanged, this.voided, this.voidedBy, this.dateVoided, this.voidReason, this.uuid, this.personAttributeId};		return params; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE person_attribute SET person_id = ?, value = ?, person_attribute_type_id = ?, creator = ?, date_created = ?, changed_by = ?, date_changed = ?, voided = ?, voided_by = ?, date_voided = ?, void_reason = ?, uuid = ? WHERE person_attribute_id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.personId) + "," + (this.value != null ? "\""+ utilities.scapeQuotationMarks(value)  +"\"" : null) + "," + (this.personAttributeTypeId) + "," + (this.creator) + "," + (this.dateCreated != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateCreated)  +"\"" : null) + "," + (this.changedBy) + "," + (this.dateChanged != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateChanged)  +"\"" : null) + "," + (this.voided) + "," + (this.voidedBy) + "," + (this.dateVoided != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateVoided)  +"\"" : null) + "," + (this.voidReason != null ? "\""+ utilities.scapeQuotationMarks(voidReason)  +"\"" : null) + "," + (this.uuid != null ? "\""+ utilities.scapeQuotationMarks(uuid)  +"\"" : null); 
	} 
 
	@Override
	public boolean hasParents() {
		if (this.changedBy != 0) return true;

		if (this.creator != 0) return true;

		if (this.voidedBy != 0) return true;

		if (this.personAttributeTypeId != 0) return true;

		if (this.personId != 0) return true;

		return false;
	}

	@Override
	public Integer getParentValue(String parentAttName) {		
		if (parentAttName.equals("changedBy")) return this.changedBy;		
		if (parentAttName.equals("creator")) return this.creator;		
		if (parentAttName.equals("voidedBy")) return this.voidedBy;		
		if (parentAttName.equals("personAttributeTypeId")) return this.personAttributeTypeId;		
		if (parentAttName.equals("personId")) return this.personId;

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
		if (parentAttName.equals("voidedBy")) {
			this.voidedBy = newParent.getObjectId();
			return;
		}		
		if (parentAttName.equals("personAttributeTypeId")) {
			this.personAttributeTypeId = newParent.getObjectId();
			return;
		}		
		if (parentAttName.equals("personId")) {
			this.personId = newParent.getObjectId();
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
		if (parentAttName.equals("voidedBy")) {
			this.voidedBy = null;
			return;
		}		
		if (parentAttName.equals("personAttributeTypeId")) {
			this.personAttributeTypeId = null;
			return;
		}		
		if (parentAttName.equals("personId")) {
			this.personId = null;
			return;
		}

		throw new RuntimeException("No found parent for: " + parentAttName);
	}


}