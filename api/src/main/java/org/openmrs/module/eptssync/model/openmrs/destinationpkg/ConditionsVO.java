package org.openmrs.module.eptssync.model.openmrs.destinationpkg; 
 
import org.openmrs.module.eptssync.model.openmrs.generic.*; 
 
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.eptssync.utilities.db.conn.DBException; 
import org.openmrs.module.eptssync.utilities.AttDefinedElements; 
import org.openmrs.module.eptssync.exceptions.ParentNotYetMigratedException; 
 
import java.sql.Connection; 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import java.io.File; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class ConditionsVO extends AbstractOpenMRSObject implements OpenMRSObject { 
	private int conditionId;
	private int previousConditionId;
	private int patientId;
	private String status;
	private int conceptId;
	private String conditionNonCoded;
	private java.util.Date onsetDate;
	private String additionalDetail;
	private java.util.Date endDate;
	private int endReason;
	private int creator;
	private java.util.Date dateCreated;
	private byte voided;
	private int voidedBy;
	private java.util.Date dateVoided;
	private String voidReason;
	private String uuid;
 
	public ConditionsVO() { 
		this.metadata = false;
	} 
 
	public void setConditionId(int conditionId){ 
	 	this.conditionId = conditionId;
	}
 
	public int getConditionId(){ 
		return this.conditionId;
	}
 
	public void setPreviousConditionId(int previousConditionId){ 
	 	this.previousConditionId = previousConditionId;
	}
 
	public int getPreviousConditionId(){ 
		return this.previousConditionId;
	}
 
	public void setPatientId(int patientId){ 
	 	this.patientId = patientId;
	}
 
	public int getPatientId(){ 
		return this.patientId;
	}
 
	public void setStatus(String status){ 
	 	this.status = status;
	}
 
	public String getStatus(){ 
		return this.status;
	}
 
	public void setConceptId(int conceptId){ 
	 	this.conceptId = conceptId;
	}
 
	public int getConceptId(){ 
		return this.conceptId;
	}
 
	public void setConditionNonCoded(String conditionNonCoded){ 
	 	this.conditionNonCoded = conditionNonCoded;
	}
 
	public String getConditionNonCoded(){ 
		return this.conditionNonCoded;
	}
 
	public void setOnsetDate(java.util.Date onsetDate){ 
	 	this.onsetDate = onsetDate;
	}
 
	public java.util.Date getOnsetDate(){ 
		return this.onsetDate;
	}
 
	public void setAdditionalDetail(String additionalDetail){ 
	 	this.additionalDetail = additionalDetail;
	}
 
	public String getAdditionalDetail(){ 
		return this.additionalDetail;
	}
 
	public void setEndDate(java.util.Date endDate){ 
	 	this.endDate = endDate;
	}
 
	public java.util.Date getEndDate(){ 
		return this.endDate;
	}
 
	public void setEndReason(int endReason){ 
	 	this.endReason = endReason;
	}
 
	public int getEndReason(){ 
		return this.endReason;
	}
 
	public void setCreator(int creator){ 
	 	this.creator = creator;
	}
 
	public int getCreator(){ 
		return this.creator;
	}
 
	public void setDateCreated(java.util.Date dateCreated){ 
	 	this.dateCreated = dateCreated;
	}
 
	public java.util.Date getDateCreated(){ 
		return this.dateCreated;
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
 
	public void setUuid(String uuid){ 
	 	this.uuid = uuid;
	}


 
	public String getUuid(){ 
		return this.uuid;
	}	public int getOriginRecordId(){ 
		return 0;
	}
 
	public void setOriginRecordId(int originRecordId){ }
 
	public String getOriginAppLocationCode(){ 
		return null;
	}
 
	public void setOriginAppLocationCode(String originAppLocationCode){ }
 
	public int getConsistent(){ 
		return 0;
	}
 
	public void setConsistent(int consistent){ }
 

 
	public int getObjectId() { 
 		return this.conditionId; 
	} 
 
	public void setObjectId(int selfId){ 
		this.conditionId = selfId; 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		this.conditionId = rs.getInt("condition_id");
		this.previousConditionId = rs.getInt("previous_condition_id");
		this.patientId = rs.getInt("patient_id");
		this.status = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("status") != null ? rs.getString("status").trim() : null);
		this.conceptId = rs.getInt("concept_id");
		this.conditionNonCoded = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("condition_non_coded") != null ? rs.getString("condition_non_coded").trim() : null);
		this.onsetDate =  rs.getTimestamp("onset_date") != null ? new java.util.Date( rs.getTimestamp("onset_date").getTime() ) : null;
		this.additionalDetail = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("additional_detail") != null ? rs.getString("additional_detail").trim() : null);
		this.endDate =  rs.getTimestamp("end_date") != null ? new java.util.Date( rs.getTimestamp("end_date").getTime() ) : null;
		this.endReason = rs.getInt("end_reason");
		this.creator = rs.getInt("creator");
		this.dateCreated =  rs.getTimestamp("date_created") != null ? new java.util.Date( rs.getTimestamp("date_created").getTime() ) : null;
		this.voided = rs.getByte("voided");
		this.voidedBy = rs.getInt("voided_by");
		this.dateVoided =  rs.getTimestamp("date_voided") != null ? new java.util.Date( rs.getTimestamp("date_voided").getTime() ) : null;
		this.voidReason = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("void_reason") != null ? rs.getString("void_reason").trim() : null);
		this.uuid = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("uuid") != null ? rs.getString("uuid").trim() : null);
	} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "condition_id"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParams(){ 
 		Object[] params = {this.previousConditionId == 0 ? null : this.previousConditionId, this.patientId == 0 ? null : this.patientId, this.status, this.conceptId == 0 ? null : this.conceptId, this.conditionNonCoded, this.onsetDate, this.additionalDetail, this.endDate, this.endReason == 0 ? null : this.endReason, this.creator == 0 ? null : this.creator, this.dateCreated, this.voided, this.voidedBy == 0 ? null : this.voidedBy, this.dateVoided, this.voidReason, this.uuid};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.previousConditionId == 0 ? null : this.previousConditionId, this.patientId == 0 ? null : this.patientId, this.status, this.conceptId == 0 ? null : this.conceptId, this.conditionNonCoded, this.onsetDate, this.additionalDetail, this.endDate, this.endReason == 0 ? null : this.endReason, this.creator == 0 ? null : this.creator, this.dateCreated, this.voided, this.voidedBy == 0 ? null : this.voidedBy, this.dateVoided, this.voidReason, this.uuid, this.conditionId};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQL(){ 
 		return "INSERT INTO conditions(previous_condition_id, patient_id, status, concept_id, condition_non_coded, onset_date, additional_detail, end_date, end_reason, creator, date_created, voided, voided_by, date_voided, void_reason, uuid) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE conditions SET previous_condition_id = ?, patient_id = ?, status = ?, concept_id = ?, condition_non_coded = ?, onset_date = ?, additional_detail = ?, end_date = ?, end_reason = ?, creator = ?, date_created = ?, voided = ?, voided_by = ?, date_voided = ?, void_reason = ?, uuid = ? WHERE condition_id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.previousConditionId == 0 ? null : this.previousConditionId) + "," + (this.patientId == 0 ? null : this.patientId) + "," + (this.status != null ? "\""+ utilities.scapeQuotationMarks(status)  +"\"" : null) + "," + (this.conceptId == 0 ? null : this.conceptId) + "," + (this.conditionNonCoded != null ? "\""+ utilities.scapeQuotationMarks(conditionNonCoded)  +"\"" : null) + "," + (this.onsetDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(onsetDate)  +"\"" : null) + "," + (this.additionalDetail != null ? "\""+ utilities.scapeQuotationMarks(additionalDetail)  +"\"" : null) + "," + (this.endDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(endDate)  +"\"" : null) + "," + (this.endReason == 0 ? null : this.endReason) + "," + (this.creator == 0 ? null : this.creator) + "," + (this.dateCreated != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateCreated)  +"\"" : null) + "," + (this.voided) + "," + (this.voidedBy == 0 ? null : this.voidedBy) + "," + (this.dateVoided != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateVoided)  +"\"" : null) + "," + (this.voidReason != null ? "\""+ utilities.scapeQuotationMarks(voidReason)  +"\"" : null) + "," + (this.uuid != null ? "\""+ utilities.scapeQuotationMarks(uuid)  +"\"" : null); 
	} 
 
	@Override
	public boolean isGeneratedFromSkeletonClass() {
		return false;
	}

	@Override
	public boolean hasParents() {
		if (this.conceptId != 0) return true;
		if (this.creator != 0) return true;
		if (this.endReason != 0) return true;
		if (this.patientId != 0) return true;
		if (this.previousConditionId != 0) return true;
		if (this.voidedBy != 0) return true;
		return false;
	}

	@Override
	public int retrieveSharedPKKey(Connection conn) throws ParentNotYetMigratedException, DBException {
		throw new RuntimeException("No PKSharedInfo defined!");	}

	@Override
	public void loadDestParentInfo(Connection conn) throws ParentNotYetMigratedException, DBException {
		OpenMRSObject parentOnDestination = null;
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.destinationpkg.ConceptVO.class, this.conceptId, false, conn); 
		this.conceptId = 0;
		if (parentOnDestination  != null) this.conceptId = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.destinationpkg.UsersVO.class, this.creator, false, conn); 
		this.creator = 0;
		if (parentOnDestination  != null) this.creator = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.destinationpkg.ConceptVO.class, this.endReason, true, conn); 
		this.endReason = 0;
		if (parentOnDestination  != null) this.endReason = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.destinationpkg.PatientVO.class, this.patientId, false, conn); 
		this.patientId = 0;
		if (parentOnDestination  != null) this.patientId = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.destinationpkg.ConditionsVO.class, this.previousConditionId, true, conn); 
		this.previousConditionId = 0;
		if (parentOnDestination  != null) this.previousConditionId = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.destinationpkg.UsersVO.class, this.voidedBy, true, conn); 
		this.voidedBy = 0;
		if (parentOnDestination  != null) this.voidedBy = parentOnDestination.getObjectId();
 
	}

	@Override
	public int getParentValue(String parentAttName) {		
		if (parentAttName.equals("conceptId")) return this.conceptId;		
		if (parentAttName.equals("creator")) return this.creator;		
		if (parentAttName.equals("endReason")) return this.endReason;		
		if (parentAttName.equals("patientId")) return this.patientId;		
		if (parentAttName.equals("previousConditionId")) return this.previousConditionId;		
		if (parentAttName.equals("voidedBy")) return this.voidedBy;

		throw new RuntimeException("No found parent for: " + parentAttName);	}


}