package org.openmrs.module.eptssync.model.pojo.source.eip_change_dectetor; 
 
import org.openmrs.module.eptssync.model.pojo.generic.*; 
 
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.eptssync.utilities.AttDefinedElements; 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class RelationshipVO extends AbstractOpenMRSObject implements OpenMRSObject { 
	private int relationshipId;
	private int personA;
	private int relationship;
	private int personB;
	private int creator;
	private byte voided;
	private int voidedBy;
	private java.util.Date dateVoided;
	private String voidReason;
	private int changedBy;
	private java.util.Date startDate;
	private java.util.Date endDate;
 
	public RelationshipVO() { 
		this.metadata = false;
	} 
 
	public void setRelationshipId(int relationshipId){ 
	 	this.relationshipId = relationshipId;
	}
 
	public int getRelationshipId(){ 
		return this.relationshipId;
	}
 
	public void setPersonA(int personA){ 
	 	this.personA = personA;
	}
 
	public int getPersonA(){ 
		return this.personA;
	}
 
	public void setRelationship(int relationship){ 
	 	this.relationship = relationship;
	}
 
	public int getRelationship(){ 
		return this.relationship;
	}
 
	public void setPersonB(int personB){ 
	 	this.personB = personB;
	}
 
	public int getPersonB(){ 
		return this.personB;
	}
 
	public void setCreator(int creator){ 
	 	this.creator = creator;
	}
 
	public int getCreator(){ 
		return this.creator;
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
 
	public void setChangedBy(int changedBy){ 
	 	this.changedBy = changedBy;
	}
 
	public int getChangedBy(){ 
		return this.changedBy;
	}
 
	public void setStartDate(java.util.Date startDate){ 
	 	this.startDate = startDate;
	}
 
	public java.util.Date getStartDate(){ 
		return this.startDate;
	}
 
	public void setEndDate(java.util.Date endDate){ 
	 	this.endDate = endDate;
	}


 
	public java.util.Date getEndDate(){ 
		return this.endDate;
	}
 
	public int getObjectId() { 
 		return this.relationshipId; 
	} 
 
	public void setObjectId(int selfId){ 
		this.relationshipId = selfId; 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		super.load(rs);
		this.relationshipId = rs.getInt("relationship_id");
		this.personA = rs.getInt("person_a");
		this.relationship = rs.getInt("relationship");
		this.personB = rs.getInt("person_b");
		this.creator = rs.getInt("creator");
		this.dateCreated =  rs.getTimestamp("date_created") != null ? new java.util.Date( rs.getTimestamp("date_created").getTime() ) : null;
		this.voided = rs.getByte("voided");
		this.voidedBy = rs.getInt("voided_by");
		this.dateVoided =  rs.getTimestamp("date_voided") != null ? new java.util.Date( rs.getTimestamp("date_voided").getTime() ) : null;
		this.voidReason = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("void_reason") != null ? rs.getString("void_reason").trim() : null);
		this.uuid = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("uuid") != null ? rs.getString("uuid").trim() : null);
		this.dateChanged =  rs.getTimestamp("date_changed") != null ? new java.util.Date( rs.getTimestamp("date_changed").getTime() ) : null;
		this.changedBy = rs.getInt("changed_by");
		this.startDate =  rs.getTimestamp("start_date") != null ? new java.util.Date( rs.getTimestamp("start_date").getTime() ) : null;
		this.endDate =  rs.getTimestamp("end_date") != null ? new java.util.Date( rs.getTimestamp("end_date").getTime() ) : null;
	} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "relationship_id"; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithoutObjectId(){ 
 		return "INSERT INTO relationship(person_a, relationship, person_b, creator, date_created, voided, voided_by, date_voided, void_reason, uuid, date_changed, changed_by, start_date, end_date) VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithoutObjectId(){ 
 		Object[] params = {this.personA == 0 ? null : this.personA, this.relationship == 0 ? null : this.relationship, this.personB == 0 ? null : this.personB, this.creator == 0 ? null : this.creator, this.dateCreated, this.voided, this.voidedBy == 0 ? null : this.voidedBy, this.dateVoided, this.voidReason, this.uuid, this.dateChanged, this.changedBy == 0 ? null : this.changedBy, this.startDate, this.endDate};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithObjectId(){ 
 		return "INSERT INTO relationship(relationship_id, person_a, relationship, person_b, creator, date_created, voided, voided_by, date_voided, void_reason, uuid, date_changed, changed_by, start_date, end_date) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithObjectId(){ 
 		Object[] params = {this.relationshipId, this.personA == 0 ? null : this.personA, this.relationship == 0 ? null : this.relationship, this.personB == 0 ? null : this.personB, this.creator == 0 ? null : this.creator, this.dateCreated, this.voided, this.voidedBy == 0 ? null : this.voidedBy, this.dateVoided, this.voidReason, this.uuid, this.dateChanged, this.changedBy == 0 ? null : this.changedBy, this.startDate, this.endDate};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.personA == 0 ? null : this.personA, this.relationship == 0 ? null : this.relationship, this.personB == 0 ? null : this.personB, this.creator == 0 ? null : this.creator, this.dateCreated, this.voided, this.voidedBy == 0 ? null : this.voidedBy, this.dateVoided, this.voidReason, this.uuid, this.dateChanged, this.changedBy == 0 ? null : this.changedBy, this.startDate, this.endDate, this.relationshipId};		return params; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE relationship SET person_a = ?, relationship = ?, person_b = ?, creator = ?, date_created = ?, voided = ?, voided_by = ?, date_voided = ?, void_reason = ?, uuid = ?, date_changed = ?, changed_by = ?, start_date = ?, end_date = ? WHERE relationship_id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.personA == 0 ? null : this.personA) + "," + (this.relationship == 0 ? null : this.relationship) + "," + (this.personB == 0 ? null : this.personB) + "," + (this.creator == 0 ? null : this.creator) + "," + (this.dateCreated != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateCreated)  +"\"" : null) + "," + (this.voided) + "," + (this.voidedBy == 0 ? null : this.voidedBy) + "," + (this.dateVoided != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateVoided)  +"\"" : null) + "," + (this.voidReason != null ? "\""+ utilities.scapeQuotationMarks(voidReason)  +"\"" : null) + "," + (this.uuid != null ? "\""+ utilities.scapeQuotationMarks(uuid)  +"\"" : null) + "," + (this.dateChanged != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateChanged)  +"\"" : null) + "," + (this.changedBy == 0 ? null : this.changedBy) + "," + (this.startDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(startDate)  +"\"" : null) + "," + (this.endDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(endDate)  +"\"" : null); 
	} 
 
	@Override
	public boolean hasParents() {
		if (this.personA != 0) return true;

		if (this.personB != 0) return true;

		if (this.creator != 0) return true;

		if (this.voidedBy != 0) return true;

		if (this.changedBy != 0) return true;

		if (this.relationship != 0) return true;

		return false;
	}

	@Override
	public int getParentValue(String parentAttName) {		
		if (parentAttName.equals("personA")) return this.personA;		
		if (parentAttName.equals("personB")) return this.personB;		
		if (parentAttName.equals("creator")) return this.creator;		
		if (parentAttName.equals("voidedBy")) return this.voidedBy;		
		if (parentAttName.equals("changedBy")) return this.changedBy;		
		if (parentAttName.equals("relationship")) return this.relationship;

		throw new RuntimeException("No found parent for: " + parentAttName);	}

	@Override
	public void changeParentValue(String parentAttName, OpenMRSObject newParent) {		
		if (parentAttName.equals("personA")) {
			this.personA = newParent.getObjectId();
			return;
		}		
		if (parentAttName.equals("personB")) {
			this.personB = newParent.getObjectId();
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
		if (parentAttName.equals("changedBy")) {
			this.changedBy = newParent.getObjectId();
			return;
		}		
		if (parentAttName.equals("relationship")) {
			this.relationship = newParent.getObjectId();
			return;
		}

		throw new RuntimeException("No found parent for: " + parentAttName);
	}


}