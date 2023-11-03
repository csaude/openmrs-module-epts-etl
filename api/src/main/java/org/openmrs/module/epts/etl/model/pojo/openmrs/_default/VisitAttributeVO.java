package org.openmrs.module.epts.etl.model.pojo.openmrs._default;

import java.sql.SQLException;

import org.openmrs.module.epts.etl.model.pojo.generic.*;
import org.openmrs.module.epts.etl.utilities.AttDefinedElements;
import org.openmrs.module.epts.etl.utilities.DateAndTimeUtilities;

import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class VisitAttributeVO extends AbstractDatabaseObject implements DatabaseObject { 
	private Integer visitAttributeId;
	private Integer visitId;
	private Integer attributeTypeId;
	private String valueReference;
	private Integer creator;
	private Integer changedBy;
	private byte voided;
	private Integer voidedBy;
	private java.util.Date dateVoided;
	private String voidReason;
 
	public VisitAttributeVO() { 
		this.metadata = false;
	} 
 
	public void setVisitAttributeId(Integer visitAttributeId){ 
	 	this.visitAttributeId = visitAttributeId;
	}
 
	public Integer getVisitAttributeId(){ 
		return this.visitAttributeId;
	}
 
	public void setVisitId(Integer visitId){ 
	 	this.visitId = visitId;
	}
 
	public Integer getVisitId(){ 
		return this.visitId;
	}
 
	public void setAttributeTypeId(Integer attributeTypeId){ 
	 	this.attributeTypeId = attributeTypeId;
	}
 
	public Integer getAttributeTypeId(){ 
		return this.attributeTypeId;
	}
 
	public void setValueReference(String valueReference){ 
	 	this.valueReference = valueReference;
	}
 
	public String getValueReference(){ 
		return this.valueReference;
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
 		return this.visitAttributeId; 
	} 
 
	public void setObjectId(Integer selfId){ 
		this.visitAttributeId = selfId; 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		super.load(rs);
		if (rs.getObject("visit_attribute_id") != null) this.visitAttributeId = rs.getInt("visit_attribute_id");
		if (rs.getObject("visit_id") != null) this.visitId = rs.getInt("visit_id");
		if (rs.getObject("attribute_type_id") != null) this.attributeTypeId = rs.getInt("attribute_type_id");
		this.valueReference = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("value_reference") != null ? rs.getString("value_reference").trim() : null);
		this.uuid = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("uuid") != null ? rs.getString("uuid").trim() : null);
		if (rs.getObject("creator") != null) this.creator = rs.getInt("creator");
		this.dateCreated =  rs.getTimestamp("date_created") != null ? new java.util.Date( rs.getTimestamp("date_created").getTime() ) : null;
		if (rs.getObject("changed_by") != null) this.changedBy = rs.getInt("changed_by");
		this.dateChanged =  rs.getTimestamp("date_changed") != null ? new java.util.Date( rs.getTimestamp("date_changed").getTime() ) : null;
		this.voided = rs.getByte("voided");
		if (rs.getObject("voided_by") != null) this.voidedBy = rs.getInt("voided_by");
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
 		Object[] params = {this.visitId, this.attributeTypeId, this.valueReference, this.uuid, this.creator, this.dateCreated, this.changedBy, this.dateChanged, this.voided, this.voidedBy, this.dateVoided, this.voidReason};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithObjectId(){ 
 		return "INSERT INTO visit_attribute(visit_attribute_id, visit_id, attribute_type_id, value_reference, uuid, creator, date_created, changed_by, date_changed, voided, voided_by, date_voided, void_reason) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithObjectId(){ 
 		Object[] params = {this.visitAttributeId, this.visitId, this.attributeTypeId, this.valueReference, this.uuid, this.creator, this.dateCreated, this.changedBy, this.dateChanged, this.voided, this.voidedBy, this.dateVoided, this.voidReason};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.visitId, this.attributeTypeId, this.valueReference, this.uuid, this.creator, this.dateCreated, this.changedBy, this.dateChanged, this.voided, this.voidedBy, this.dateVoided, this.voidReason, this.visitAttributeId};		return params; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE visit_attribute SET visit_id = ?, attribute_type_id = ?, value_reference = ?, uuid = ?, creator = ?, date_created = ?, changed_by = ?, date_changed = ?, voided = ?, voided_by = ?, date_voided = ?, void_reason = ? WHERE visit_attribute_id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.visitId) + "," + (this.attributeTypeId) + "," + (this.valueReference != null ? "\""+ utilities.scapeQuotationMarks(valueReference)  +"\"" : null) + "," + (this.uuid != null ? "\""+ utilities.scapeQuotationMarks(uuid)  +"\"" : null) + "," + (this.creator) + "," + (this.dateCreated != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateCreated)  +"\"" : null) + "," + (this.changedBy) + "," + (this.dateChanged != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateChanged)  +"\"" : null) + "," + (this.voided) + "," + (this.voidedBy) + "," + (this.dateVoided != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateVoided)  +"\"" : null) + "," + (this.voidReason != null ? "\""+ utilities.scapeQuotationMarks(voidReason)  +"\"" : null); 
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
	public Integer getParentValue(String parentAttName) {		
		if (parentAttName.equals("attributeTypeId")) return this.attributeTypeId;		
		if (parentAttName.equals("changedBy")) return this.changedBy;		
		if (parentAttName.equals("creator")) return this.creator;		
		if (parentAttName.equals("visitId")) return this.visitId;		
		if (parentAttName.equals("voidedBy")) return this.voidedBy;

		throw new RuntimeException("No found parent for: " + parentAttName);	}

	@Override
	public void changeParentValue(String parentAttName, DatabaseObject newParent) {		
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

	@Override
	public void setParentToNull(String parentAttName) {		
		if (parentAttName.equals("attributeTypeId")) {
			this.attributeTypeId = null;
			return;
		}		
		if (parentAttName.equals("changedBy")) {
			this.changedBy = null;
			return;
		}		
		if (parentAttName.equals("creator")) {
			this.creator = null;
			return;
		}		
		if (parentAttName.equals("visitId")) {
			this.visitId = null;
			return;
		}		
		if (parentAttName.equals("voidedBy")) {
			this.voidedBy = null;
			return;
		}

		throw new RuntimeException("No found parent for: " + parentAttName);
	}


}