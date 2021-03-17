package org.openmrs.module.eptssync.model.pojo.molocue; 
 
import org.openmrs.module.eptssync.model.pojo.generic.*; 
 
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.eptssync.utilities.AttDefinedElements; 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class VisitAttributeVO extends AbstractOpenMRSObject implements OpenMRSObject { 
	private int visitAttributeId;
	private int visitId;
	private int attributeTypeId;
	private String valueReference;
	private int creator;
	private int changedBy;
	private byte voided;
	private int voidedBy;
	private java.util.Date dateVoided;
	private String voidReason;
 
	public VisitAttributeVO() { 
		this.metadata = false;
	} 
 
	public void setVisitAttributeId(int visitAttributeId){ 
	 	this.visitAttributeId = visitAttributeId;
	}
 
	public int getVisitAttributeId(){ 
		return this.visitAttributeId;
	}
 
	public void setVisitId(int visitId){ 
	 	this.visitId = visitId;
	}
 
	public int getVisitId(){ 
		return this.visitId;
	}
 
	public void setAttributeTypeId(int attributeTypeId){ 
	 	this.attributeTypeId = attributeTypeId;
	}
 
	public int getAttributeTypeId(){ 
		return this.attributeTypeId;
	}
 
	public void setValueReference(String valueReference){ 
	 	this.valueReference = valueReference;
	}
 
	public String getValueReference(){ 
		return this.valueReference;
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
 
	public int getObjectId() { 
 		return this.visitAttributeId; 
	} 
 
	public void setObjectId(int selfId){ 
		this.visitAttributeId = selfId; 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		this.visitAttributeId = rs.getInt("visit_attribute_id");
		this.visitId = rs.getInt("visit_id");
		this.attributeTypeId = rs.getInt("attribute_type_id");
		this.valueReference = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("value_reference") != null ? rs.getString("value_reference").trim() : null);
		this.uuid = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("uuid") != null ? rs.getString("uuid").trim() : null);
		this.creator = rs.getInt("creator");
		this.dateCreated =  rs.getTimestamp("date_created") != null ? new java.util.Date( rs.getTimestamp("date_created").getTime() ) : null;
		this.changedBy = rs.getInt("changed_by");
		this.dateChanged =  rs.getTimestamp("date_changed") != null ? new java.util.Date( rs.getTimestamp("date_changed").getTime() ) : null;
		this.voided = rs.getByte("voided");
		this.voidedBy = rs.getInt("voided_by");
		this.dateVoided =  rs.getTimestamp("date_voided") != null ? new java.util.Date( rs.getTimestamp("date_voided").getTime() ) : null;
		this.voidReason = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("void_reason") != null ? rs.getString("void_reason").trim() : null);
	} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "visit_attribute_id"; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithoutObjectId(){ 
 		return "INSERT INTO visit_attribute(visit_id, attribute_type_id, value_reference, uuid, creator, date_created, changed_by, date_changed, voided, voided_by, date_voided, void_reason) VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithoutObjectId(){ 
 		Object[] params = {this.visitId == 0 ? null : this.visitId, this.attributeTypeId == 0 ? null : this.attributeTypeId, this.valueReference, this.uuid, this.creator == 0 ? null : this.creator, this.dateCreated, this.changedBy == 0 ? null : this.changedBy, this.dateChanged, this.voided, this.voidedBy == 0 ? null : this.voidedBy, this.dateVoided, this.voidReason};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithObjectId(){ 
 		return "INSERT INTO visit_attribute(visit_attribute_id, visit_id, attribute_type_id, value_reference, uuid, creator, date_created, changed_by, date_changed, voided, voided_by, date_voided, void_reason) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithObjectId(){ 
 		Object[] params = {this.visitAttributeId, this.visitId == 0 ? null : this.visitId, this.attributeTypeId == 0 ? null : this.attributeTypeId, this.valueReference, this.uuid, this.creator == 0 ? null : this.creator, this.dateCreated, this.changedBy == 0 ? null : this.changedBy, this.dateChanged, this.voided, this.voidedBy == 0 ? null : this.voidedBy, this.dateVoided, this.voidReason};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.visitId == 0 ? null : this.visitId, this.attributeTypeId == 0 ? null : this.attributeTypeId, this.valueReference, this.uuid, this.creator == 0 ? null : this.creator, this.dateCreated, this.changedBy == 0 ? null : this.changedBy, this.dateChanged, this.voided, this.voidedBy == 0 ? null : this.voidedBy, this.dateVoided, this.voidReason, this.visitAttributeId};		return params; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE visit_attribute SET visit_id = ?, attribute_type_id = ?, value_reference = ?, uuid = ?, creator = ?, date_created = ?, changed_by = ?, date_changed = ?, voided = ?, voided_by = ?, date_voided = ?, void_reason = ? WHERE visit_attribute_id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.visitId == 0 ? null : this.visitId) + "," + (this.attributeTypeId == 0 ? null : this.attributeTypeId) + "," + (this.valueReference != null ? "\""+ utilities.scapeQuotationMarks(valueReference)  +"\"" : null) + "," + (this.uuid != null ? "\""+ utilities.scapeQuotationMarks(uuid)  +"\"" : null) + "," + (this.creator == 0 ? null : this.creator) + "," + (this.dateCreated != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateCreated)  +"\"" : null) + "," + (this.changedBy == 0 ? null : this.changedBy) + "," + (this.dateChanged != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateChanged)  +"\"" : null) + "," + (this.voided) + "," + (this.voidedBy == 0 ? null : this.voidedBy) + "," + (this.dateVoided != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateVoided)  +"\"" : null) + "," + (this.voidReason != null ? "\""+ utilities.scapeQuotationMarks(voidReason)  +"\"" : null); 
	} 
 
	@Override
	public boolean hasParents() {
		if (this.attributeTypeId != 0) return true;

		if (this.changedBy != 0) return true;

		if (this.creator != 0) return true;

		if (this.visitId != 0) return true;

		if (this.voidedBy != 0) return true;

		return false;
	}

	@Override
	public int getParentValue(String parentAttName) {		
		if (parentAttName.equals("attributeTypeId")) return this.attributeTypeId;		
		if (parentAttName.equals("changedBy")) return this.changedBy;		
		if (parentAttName.equals("creator")) return this.creator;		
		if (parentAttName.equals("visitId")) return this.visitId;		
		if (parentAttName.equals("voidedBy")) return this.voidedBy;

		throw new RuntimeException("No found parent for: " + parentAttName);	}

	@Override
	public void changeParentValue(String parentAttName, OpenMRSObject newParent) {		
		if (parentAttName.equals("attributeTypeId")) {
			this.attributeTypeId = newParent.getObjectId();
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
		if (parentAttName.equals("visitId")) {
			this.visitId = newParent.getObjectId();
			return;
		}		
		if (parentAttName.equals("voidedBy")) {
			this.voidedBy = newParent.getObjectId();
			return;
		}

		throw new RuntimeException("No found parent for: " + parentAttName);
	}


}