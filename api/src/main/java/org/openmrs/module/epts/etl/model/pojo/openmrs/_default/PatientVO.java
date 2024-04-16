package org.openmrs.module.epts.etl.model.pojo.openmrs._default;

import org.openmrs.module.epts.etl.model.pojo.generic.*; 
 
import org.openmrs.module.epts.etl.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.epts.etl.utilities.AttDefinedElements; 
 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class PatientVO extends AbstractDatabaseObject implements DatabaseObject { 
	private Integer patientId;
	private Integer creator;
	private Integer changedBy;
	private Byte voided;
	private Integer voidedBy;
	private java.util.Date dateVoided;
	private String voidReason;
	private String allergyStatus;
 
	public PatientVO() { 
		this.metadata = false;
	} 
 
	public void setPatientId(Integer patientId){ 
	 	this.patientId = patientId;
	}
 
	public Integer getPatientId(){ 
		return this.patientId;
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
 
	public void setVoided(Byte voided){ 
	 	this.voided = voided;
	}
 
	public Byte getVoided(){ 
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
 
	public void setAllergyStatus(String allergyStatus){ 
	 	this.allergyStatus = allergyStatus;
	}


 
	public String getAllergyStatus(){ 
		return this.allergyStatus;
	}
 
	@Override
	public void load(ResultSet rs) throws SQLException{ 
		super.load(rs);
 
if (rs.getObject("patient_id") != null) this.patientId = rs.getInt("patient_id");
if (rs.getObject("creator") != null) this.creator = rs.getInt("creator");
this.dateCreated =  rs.getTimestamp("date_created") != null ? new java.util.Date( rs.getTimestamp("date_created").getTime() ) : null;
if (rs.getObject("changed_by") != null) this.changedBy = rs.getInt("changed_by");
this.dateChanged =  rs.getTimestamp("date_changed") != null ? new java.util.Date( rs.getTimestamp("date_changed").getTime() ) : null;
this.voided = rs.getByte("voided");
if (rs.getObject("voided_by") != null) this.voidedBy = rs.getInt("voided_by");
this.dateVoided =  rs.getTimestamp("date_voided") != null ? new java.util.Date( rs.getTimestamp("date_voided").getTime() ) : null;
this.voidReason = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("void_reason") != null ? rs.getString("void_reason").trim() : null);
this.allergyStatus = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("allergy_status") != null ? rs.getString("allergy_status").trim() : null);
	} 
 
	@JsonIgnore
	@Override
	public String getInsertSQLWithoutObjectId(){ 
 		return "INSERT INTO patient(creator, date_created, changed_by, date_changed, voided, voided_by, date_voided, void_reason, allergy_status) VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	@Override
	public String getInsertSQLWithObjectId(){ 
 		return "INSERT INTO patient(patient_id, creator, date_created, changed_by, date_changed, voided, voided_by, date_voided, void_reason, allergy_status) VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	@Override
	public Object[]  getInsertParamsWithoutObjectId(){ 
 		Object[] params = {this.creator, this.dateCreated, this.changedBy, this.dateChanged, this.voided, this.voidedBy, this.dateVoided, this.voidReason, this.allergyStatus};
		return params; 
	} 
 
	@JsonIgnore
	@Override
	public Object[]  getInsertParamsWithObjectId(){ 
 		Object[] params = {this.patientId, this.creator, this.dateCreated, this.changedBy, this.dateChanged, this.voided, this.voidedBy, this.dateVoided, this.voidReason, this.allergyStatus};
		return params; 
	} 
 
	@JsonIgnore
	@Override
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.patientId, this.creator, this.dateCreated, this.changedBy, this.dateChanged, this.voided, this.voidedBy, this.dateVoided, this.voidReason, this.allergyStatus, this.patientId};
		return params; 
	} 
 
	@JsonIgnore
	@Override
	public String getUpdateSQL(){ 
 		return "UPDATE patient SET patient_id = ?, creator = ?, date_created = ?, changed_by = ?, date_changed = ?, voided = ?, voided_by = ?, date_voided = ?, void_reason = ?, allergy_status = ? WHERE patient_id = ? "; 
	} 
 
	@JsonIgnore
	@Override
	public String generateInsertValuesWithoutObjectId(){ 
 		return ""+(this.creator) + "," + (this.dateCreated != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateCreated)  +"\"" : null) + "," + (this.changedBy) + "," + (this.dateChanged != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateChanged)  +"\"" : null) + "," + (this.voided) + "," + (this.voidedBy) + "," + (this.dateVoided != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateVoided)  +"\"" : null) + "," + (this.voidReason != null ? "\""+ utilities.scapeQuotationMarks(voidReason)  +"\"" : null) + "," + (this.allergyStatus != null ? "\""+ utilities.scapeQuotationMarks(allergyStatus)  +"\"" : null); 
	} 
 
	@JsonIgnore
	@Override
	public String generateInsertValuesWithObjectId(){ 
 		return ""+(this.patientId) + "," + (this.creator) + "," + (this.dateCreated != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateCreated)  +"\"" : null) + "," + (this.changedBy) + "," + (this.dateChanged != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateChanged)  +"\"" : null) + "," + (this.voided) + "," + (this.voidedBy) + "," + (this.dateVoided != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateVoided)  +"\"" : null) + "," + (this.voidReason != null ? "\""+ utilities.scapeQuotationMarks(voidReason)  +"\"" : null) + "," + (this.allergyStatus != null ? "\""+ utilities.scapeQuotationMarks(allergyStatus)  +"\"" : null); 
	} 
 
	@Override
	public boolean hasParents() {
		if (this.patientId != null) return true;

		if (this.changedBy != null) return true;

		if (this.creator != null) return true;

		if (this.voidedBy != null) return true;

		return false;
	}

	@Override
	public Integer getParentValue(String parentAttName) {		
		if (parentAttName.equals("patientId")) return this.patientId;		
		if (parentAttName.equals("changedBy")) return this.changedBy;		
		if (parentAttName.equals("creator")) return this.creator;		
		if (parentAttName.equals("voidedBy")) return this.voidedBy;

		throw new RuntimeException("No found parent for: " + parentAttName);	}

	@Override
	public String generateTableName() {
		return "patient";
	}


}