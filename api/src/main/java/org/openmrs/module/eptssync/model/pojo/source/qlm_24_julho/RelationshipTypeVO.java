package org.openmrs.module.eptssync.model.pojo.source.qlm_24_julho; 
 
import org.openmrs.module.eptssync.model.pojo.generic.*; 
 
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.eptssync.utilities.AttDefinedElements; 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class RelationshipTypeVO extends AbstractOpenMRSObject implements OpenMRSObject { 
	private int relationshipTypeId;
	private String aIsToB;
	private String bIsToA;
	private int preferred;
	private int weight;
	private String description;
	private int creator;
	private byte retired;
	private int retiredBy;
	private java.util.Date dateRetired;
	private String retireReason;
	private int changedBy;
 
	public RelationshipTypeVO() { 
		this.metadata = true;
	} 
 
	public void setRelationshipTypeId(int relationshipTypeId){ 
	 	this.relationshipTypeId = relationshipTypeId;
	}
 
	public int getRelationshipTypeId(){ 
		return this.relationshipTypeId;
	}
 
	public void setAIsToB(String aIsToB){ 
	 	this.aIsToB = aIsToB;
	}
 
	public String getAIsToB(){ 
		return this.aIsToB;
	}
 
	public void setBIsToA(String bIsToA){ 
	 	this.bIsToA = bIsToA;
	}
 
	public String getBIsToA(){ 
		return this.bIsToA;
	}
 
	public void setPreferred(int preferred){ 
	 	this.preferred = preferred;
	}
 
	public int getPreferred(){ 
		return this.preferred;
	}
 
	public void setWeight(int weight){ 
	 	this.weight = weight;
	}
 
	public int getWeight(){ 
		return this.weight;
	}
 
	public void setDescription(String description){ 
	 	this.description = description;
	}
 
	public String getDescription(){ 
		return this.description;
	}
 
	public void setCreator(int creator){ 
	 	this.creator = creator;
	}
 
	public int getCreator(){ 
		return this.creator;
	}
 
	public void setRetired(byte retired){ 
	 	this.retired = retired;
	}
 
	public byte getRetired(){ 
		return this.retired;
	}
 
	public void setRetiredBy(int retiredBy){ 
	 	this.retiredBy = retiredBy;
	}
 
	public int getRetiredBy(){ 
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
 
	public void setChangedBy(int changedBy){ 
	 	this.changedBy = changedBy;
	}


 
	public int getChangedBy(){ 
		return this.changedBy;
	}
 
	public int getObjectId() { 
 		return this.relationshipTypeId; 
	} 
 
	public void setObjectId(int selfId){ 
		this.relationshipTypeId = selfId; 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		super.load(rs);
		this.relationshipTypeId = rs.getInt("relationship_type_id");
		this.aIsToB = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("a_is_to_b") != null ? rs.getString("a_is_to_b").trim() : null);
		this.bIsToA = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("b_is_to_a") != null ? rs.getString("b_is_to_a").trim() : null);
		this.preferred = rs.getInt("preferred");
		this.weight = rs.getInt("weight");
		this.description = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("description") != null ? rs.getString("description").trim() : null);
		this.creator = rs.getInt("creator");
		this.dateCreated =  rs.getTimestamp("date_created") != null ? new java.util.Date( rs.getTimestamp("date_created").getTime() ) : null;
		this.uuid = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("uuid") != null ? rs.getString("uuid").trim() : null);
		this.retired = rs.getByte("retired");
		this.retiredBy = rs.getInt("retired_by");
		this.dateRetired =  rs.getTimestamp("date_retired") != null ? new java.util.Date( rs.getTimestamp("date_retired").getTime() ) : null;
		this.retireReason = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("retire_reason") != null ? rs.getString("retire_reason").trim() : null);
		this.dateChanged =  rs.getTimestamp("date_changed") != null ? new java.util.Date( rs.getTimestamp("date_changed").getTime() ) : null;
		this.changedBy = rs.getInt("changed_by");
	} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "relationship_type_id"; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithoutObjectId(){ 
 		return "INSERT INTO relationship_type(relationship_type_id, a_is_to_b, b_is_to_a, preferred, weight, description, creator, date_created, uuid, retired, retired_by, date_retired, retire_reason, date_changed, changed_by) VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithoutObjectId(){ 
 		Object[] params = {this.relationshipTypeId, this.aIsToB, this.bIsToA, this.preferred, this.weight, this.description, this.creator == 0 ? null : this.creator, this.dateCreated, this.uuid, this.retired, this.retiredBy == 0 ? null : this.retiredBy, this.dateRetired, this.retireReason, this.dateChanged, this.changedBy == 0 ? null : this.changedBy};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithObjectId(){ 
 		return "INSERT INTO relationship_type(relationship_type_id, relationship_type_id, a_is_to_b, b_is_to_a, preferred, weight, description, creator, date_created, uuid, retired, retired_by, date_retired, retire_reason, date_changed, changed_by) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithObjectId(){ 
 		Object[] params = {this.relationshipTypeId, this.relationshipTypeId, this.aIsToB, this.bIsToA, this.preferred, this.weight, this.description, this.creator == 0 ? null : this.creator, this.dateCreated, this.uuid, this.retired, this.retiredBy == 0 ? null : this.retiredBy, this.dateRetired, this.retireReason, this.dateChanged, this.changedBy == 0 ? null : this.changedBy};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.relationshipTypeId, this.aIsToB, this.bIsToA, this.preferred, this.weight, this.description, this.creator == 0 ? null : this.creator, this.dateCreated, this.uuid, this.retired, this.retiredBy == 0 ? null : this.retiredBy, this.dateRetired, this.retireReason, this.dateChanged, this.changedBy == 0 ? null : this.changedBy, this.relationshipTypeId};		return params; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE relationship_type SET relationship_type_id = ?, a_is_to_b = ?, b_is_to_a = ?, preferred = ?, weight = ?, description = ?, creator = ?, date_created = ?, uuid = ?, retired = ?, retired_by = ?, date_retired = ?, retire_reason = ?, date_changed = ?, changed_by = ? WHERE relationship_type_id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.relationshipTypeId) + "," + (this.aIsToB != null ? "\""+ utilities.scapeQuotationMarks(aIsToB)  +"\"" : null) + "," + (this.bIsToA != null ? "\""+ utilities.scapeQuotationMarks(bIsToA)  +"\"" : null) + "," + (this.preferred) + "," + (this.weight) + "," + (this.description != null ? "\""+ utilities.scapeQuotationMarks(description)  +"\"" : null) + "," + (this.creator == 0 ? null : this.creator) + "," + (this.dateCreated != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateCreated)  +"\"" : null) + "," + (this.uuid != null ? "\""+ utilities.scapeQuotationMarks(uuid)  +"\"" : null) + "," + (this.retired) + "," + (this.retiredBy == 0 ? null : this.retiredBy) + "," + (this.dateRetired != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateRetired)  +"\"" : null) + "," + (this.retireReason != null ? "\""+ utilities.scapeQuotationMarks(retireReason)  +"\"" : null) + "," + (this.dateChanged != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateChanged)  +"\"" : null) + "," + (this.changedBy == 0 ? null : this.changedBy); 
	} 
 
	@Override
	public boolean hasParents() {
		if (this.changedBy != 0) return true;

		if (this.creator != 0) return true;

		if (this.retiredBy != 0) return true;

		return false;
	}

	@Override
	public int getParentValue(String parentAttName) {		
		if (parentAttName.equals("changedBy")) return this.changedBy;		
		if (parentAttName.equals("creator")) return this.creator;		
		if (parentAttName.equals("retiredBy")) return this.retiredBy;

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
		if (parentAttName.equals("retiredBy")) {
			this.retiredBy = newParent.getObjectId();
			return;
		}

		throw new RuntimeException("No found parent for: " + parentAttName);
	}


}