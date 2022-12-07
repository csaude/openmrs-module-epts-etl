package org.openmrs.module.eptssync.model.pojo.destination;

import org.openmrs.module.eptssync.model.pojo.generic.*; 
 
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.eptssync.utilities.AttDefinedElements; 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class PatientIdentifierVO extends AbstractDatabaseObject implements DatabaseObject { 
	private Integer patientIdentifierId;
	private Integer patientId;
	private String identifier;
	private Integer identifierType;
	private byte preferred;
	private Integer locationId;
	private Integer creator;
	private byte voided;
	private Integer voidedBy;
	private java.util.Date dateVoided;
	private String voidReason;
	private Integer changedBy;
 
	public PatientIdentifierVO() { 
		this.metadata = false;
	} 
 
	public void setPatientIdentifierId(Integer patientIdentifierId){ 
	 	this.patientIdentifierId = patientIdentifierId;
	}
 
	public Integer getPatientIdentifierId(){ 
		return this.patientIdentifierId;
	}
 
	public void setPatientId(Integer patientId){ 
	 	this.patientId = patientId;
	}
 
	public Integer getPatientId(){ 
		return this.patientId;
	}
 
	public void setIdentifier(String identifier){ 
	 	this.identifier = identifier;
	}
 
	public String getIdentifier(){ 
		return this.identifier;
	}
 
	public void setIdentifierType(Integer identifierType){ 
	 	this.identifierType = identifierType;
	}
 
	public Integer getIdentifierType(){ 
		return this.identifierType;
	}
 
	public void setPreferred(byte preferred){ 
	 	this.preferred = preferred;
	}
 
	public byte getPreferred(){ 
		return this.preferred;
	}
 
	public void setLocationId(Integer locationId){ 
	 	this.locationId = locationId;
	}
 
	public Integer getLocationId(){ 
		return this.locationId;
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
 		return this.patientIdentifierId; 
	} 
 
	public void setObjectId(Integer selfId){ 
		this.patientIdentifierId = selfId; 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		super.load(rs);
		if (rs.getObject("patient_identifier_id") != null) this.patientIdentifierId = rs.getInt("patient_identifier_id");
		if (rs.getObject("patient_id") != null) this.patientId = rs.getInt("patient_id");
		this.identifier = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("identifier") != null ? rs.getString("identifier").trim() : null);
		if (rs.getObject("identifier_type") != null) this.identifierType = rs.getInt("identifier_type");
		this.preferred = rs.getByte("preferred");
		if (rs.getObject("location_id") != null) this.locationId = rs.getInt("location_id");
		if (rs.getObject("creator") != null) this.creator = rs.getInt("creator");
		this.dateCreated =  rs.getTimestamp("date_created") != null ? new java.util.Date( rs.getTimestamp("date_created").getTime() ) : null;
		this.voided = rs.getByte("voided");
		if (rs.getObject("voided_by") != null) this.voidedBy = rs.getInt("voided_by");
		this.dateVoided =  rs.getTimestamp("date_voided") != null ? new java.util.Date( rs.getTimestamp("date_voided").getTime() ) : null;
		this.voidReason = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("void_reason") != null ? rs.getString("void_reason").trim() : null);
		this.uuid = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("uuid") != null ? rs.getString("uuid").trim() : null);
		this.dateChanged =  rs.getTimestamp("date_changed") != null ? new java.util.Date( rs.getTimestamp("date_changed").getTime() ) : null;
		if (rs.getObject("changed_by") != null) this.changedBy = rs.getInt("changed_by");
	} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "patient_identifier_id"; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithoutObjectId(){ 
 		return "INSERT INTO patient_identifier(patient_id, identifier, identifier_type, preferred, location_id, creator, date_created, voided, voided_by, date_voided, void_reason, uuid, date_changed, changed_by) VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithoutObjectId(){ 
 		Object[] params = {this.patientId, this.identifier, this.identifierType, this.preferred, this.locationId, this.creator, this.dateCreated, this.voided, this.voidedBy, this.dateVoided, this.voidReason, this.uuid, this.dateChanged, this.changedBy};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithObjectId(){ 
 		return "INSERT INTO patient_identifier(patient_identifier_id, patient_id, identifier, identifier_type, preferred, location_id, creator, date_created, voided, voided_by, date_voided, void_reason, uuid, date_changed, changed_by) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithObjectId(){ 
 		Object[] params = {this.patientIdentifierId, this.patientId, this.identifier, this.identifierType, this.preferred, this.locationId, this.creator, this.dateCreated, this.voided, this.voidedBy, this.dateVoided, this.voidReason, this.uuid, this.dateChanged, this.changedBy};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.patientId, this.identifier, this.identifierType, this.preferred, this.locationId, this.creator, this.dateCreated, this.voided, this.voidedBy, this.dateVoided, this.voidReason, this.uuid, this.dateChanged, this.changedBy, this.patientIdentifierId};		return params; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE patient_identifier SET patient_id = ?, identifier = ?, identifier_type = ?, preferred = ?, location_id = ?, creator = ?, date_created = ?, voided = ?, voided_by = ?, date_voided = ?, void_reason = ?, uuid = ?, date_changed = ?, changed_by = ? WHERE patient_identifier_id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.patientId) + "," + (this.identifier != null ? "\""+ utilities.scapeQuotationMarks(identifier)  +"\"" : null) + "," + (this.identifierType) + "," + (this.preferred) + "," + (this.locationId) + "," + (this.creator) + "," + (this.dateCreated != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateCreated)  +"\"" : null) + "," + (this.voided) + "," + (this.voidedBy) + "," + (this.dateVoided != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateVoided)  +"\"" : null) + "," + (this.voidReason != null ? "\""+ utilities.scapeQuotationMarks(voidReason)  +"\"" : null) + "," + (this.uuid != null ? "\""+ utilities.scapeQuotationMarks(uuid)  +"\"" : null) + "," + (this.dateChanged != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateChanged)  +"\"" : null) + "," + (this.changedBy); 
	} 
 
	@Override
	public boolean hasParents() {
		if (this.identifierType != 0) return true;

		if (this.patientId != 0) return true;

		if (this.creator != 0) return true;

		if (this.voidedBy != 0) return true;

		if (this.changedBy != 0) return true;

		if (this.locationId != 0) return true;

		return false;
	}

	@Override
	public Integer getParentValue(String parentAttName) {		
		if (parentAttName.equals("identifierType")) return this.identifierType;		
		if (parentAttName.equals("patientId")) return this.patientId;		
		if (parentAttName.equals("creator")) return this.creator;		
		if (parentAttName.equals("voidedBy")) return this.voidedBy;		
		if (parentAttName.equals("changedBy")) return this.changedBy;		
		if (parentAttName.equals("locationId")) return this.locationId;

		throw new RuntimeException("No found parent for: " + parentAttName);	}

	@Override
	public void changeParentValue(String parentAttName, DatabaseObject newParent) {		
		if (parentAttName.equals("identifierType")) {
			this.identifierType = newParent.getObjectId();
			return;
		}		
		if (parentAttName.equals("patientId")) {
			this.patientId = newParent.getObjectId();
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
		if (parentAttName.equals("locationId")) {
			this.locationId = newParent.getObjectId();
			return;
		}

		throw new RuntimeException("No found parent for: " + parentAttName);
	}

	@Override
	public void setParentToNull(String parentAttName) {		
		if (parentAttName.equals("identifierType")) {
			this.identifierType = null;
			return;
		}		
		if (parentAttName.equals("patientId")) {
			this.patientId = null;
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
		if (parentAttName.equals("changedBy")) {
			this.changedBy = null;
			return;
		}		
		if (parentAttName.equals("locationId")) {
			this.locationId = null;
			return;
		}

		throw new RuntimeException("No found parent for: " + parentAttName);
	}


}