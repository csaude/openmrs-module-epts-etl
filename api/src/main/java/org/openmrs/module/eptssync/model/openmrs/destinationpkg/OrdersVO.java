package org.openmrs.module.eptssync.model.openmrs.destinationpkg; 
 
import org.openmrs.module.eptssync.model.openmrs.generic.*; 
 
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.eptssync.utilities.db.conn.DBException; 
import org.openmrs.module.eptssync.utilities.AttDefinedElements; 
import org.openmrs.module.eptssync.exceptions.ParentNotYetMigratedException; 
 
import java.sql.Connection; 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class OrdersVO extends AbstractOpenMRSObject implements OpenMRSObject { 
	private int orderId;
	private int orderTypeId;
	private int conceptId;
	private int orderer;
	private int encounterId;
	private String instructions;
	private java.util.Date dateActivated;
	private java.util.Date autoExpireDate;
	private java.util.Date dateStopped;
	private int orderReason;
	private int creator;
	private java.util.Date dateCreated;
	private byte voided;
	private int voidedBy;
	private java.util.Date dateVoided;
	private String voidReason;
	private int patientId;
	private String accessionNumber;
	private String uuid;
	private String orderReasonNonCoded;
	private String urgency;
	private String orderNumber;
	private int previousOrderId;
	private String orderAction;
	private String commentToFulfiller;
	private int careSetting;
	private java.util.Date scheduledDate;
	private int consistent;
	private java.util.Date lastSyncDate;
	private int originRecordId;
	private java.util.Date dateChanged;
	private String originAppLocationCode;
 
	public OrdersVO() { 
		this.metadata = false;
	} 
 
	public void setOrderId(int orderId){ 
	 	this.orderId = orderId;
	}
 
	public int getOrderId(){ 
		return this.orderId;
	}
 
	public void setOrderTypeId(int orderTypeId){ 
	 	this.orderTypeId = orderTypeId;
	}
 
	public int getOrderTypeId(){ 
		return this.orderTypeId;
	}
 
	public void setConceptId(int conceptId){ 
	 	this.conceptId = conceptId;
	}
 
	public int getConceptId(){ 
		return this.conceptId;
	}
 
	public void setOrderer(int orderer){ 
	 	this.orderer = orderer;
	}
 
	public int getOrderer(){ 
		return this.orderer;
	}
 
	public void setEncounterId(int encounterId){ 
	 	this.encounterId = encounterId;
	}
 
	public int getEncounterId(){ 
		return this.encounterId;
	}
 
	public void setInstructions(String instructions){ 
	 	this.instructions = instructions;
	}
 
	public String getInstructions(){ 
		return this.instructions;
	}
 
	public void setDateActivated(java.util.Date dateActivated){ 
	 	this.dateActivated = dateActivated;
	}
 
	public java.util.Date getDateActivated(){ 
		return this.dateActivated;
	}
 
	public void setAutoExpireDate(java.util.Date autoExpireDate){ 
	 	this.autoExpireDate = autoExpireDate;
	}
 
	public java.util.Date getAutoExpireDate(){ 
		return this.autoExpireDate;
	}
 
	public void setDateStopped(java.util.Date dateStopped){ 
	 	this.dateStopped = dateStopped;
	}
 
	public java.util.Date getDateStopped(){ 
		return this.dateStopped;
	}
 
	public void setOrderReason(int orderReason){ 
	 	this.orderReason = orderReason;
	}
 
	public int getOrderReason(){ 
		return this.orderReason;
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
 
	public void setPatientId(int patientId){ 
	 	this.patientId = patientId;
	}
 
	public int getPatientId(){ 
		return this.patientId;
	}
 
	public void setAccessionNumber(String accessionNumber){ 
	 	this.accessionNumber = accessionNumber;
	}
 
	public String getAccessionNumber(){ 
		return this.accessionNumber;
	}
 
	public void setUuid(String uuid){ 
	 	this.uuid = uuid;
	}
 
	public String getUuid(){ 
		return this.uuid;
	}
 
	public void setOrderReasonNonCoded(String orderReasonNonCoded){ 
	 	this.orderReasonNonCoded = orderReasonNonCoded;
	}
 
	public String getOrderReasonNonCoded(){ 
		return this.orderReasonNonCoded;
	}
 
	public void setUrgency(String urgency){ 
	 	this.urgency = urgency;
	}
 
	public String getUrgency(){ 
		return this.urgency;
	}
 
	public void setOrderNumber(String orderNumber){ 
	 	this.orderNumber = orderNumber;
	}
 
	public String getOrderNumber(){ 
		return this.orderNumber;
	}
 
	public void setPreviousOrderId(int previousOrderId){ 
	 	this.previousOrderId = previousOrderId;
	}
 
	public int getPreviousOrderId(){ 
		return this.previousOrderId;
	}
 
	public void setOrderAction(String orderAction){ 
	 	this.orderAction = orderAction;
	}
 
	public String getOrderAction(){ 
		return this.orderAction;
	}
 
	public void setCommentToFulfiller(String commentToFulfiller){ 
	 	this.commentToFulfiller = commentToFulfiller;
	}
 
	public String getCommentToFulfiller(){ 
		return this.commentToFulfiller;
	}
 
	public void setCareSetting(int careSetting){ 
	 	this.careSetting = careSetting;
	}
 
	public int getCareSetting(){ 
		return this.careSetting;
	}
 
	public void setScheduledDate(java.util.Date scheduledDate){ 
	 	this.scheduledDate = scheduledDate;
	}
 
	public java.util.Date getScheduledDate(){ 
		return this.scheduledDate;
	}
 
	public void setConsistent(int consistent){ 
	 	this.consistent = consistent;
	}
 
	public int getConsistent(){ 
		return this.consistent;
	}
 
	public void setLastSyncDate(java.util.Date lastSyncDate){ 
	 	this.lastSyncDate = lastSyncDate;
	}
 
	public java.util.Date getLastSyncDate(){ 
		return this.lastSyncDate;
	}
 
	public void setOriginRecordId(int originRecordId){ 
	 	this.originRecordId = originRecordId;
	}
 
	public int getOriginRecordId(){ 
		return this.originRecordId;
	}
 
	public void setDateChanged(java.util.Date dateChanged){ 
	 	this.dateChanged = dateChanged;
	}
 
	public java.util.Date getDateChanged(){ 
		return this.dateChanged;
	}
 
	public void setOriginAppLocationCode(String originAppLocationCode){ 
	 	this.originAppLocationCode = originAppLocationCode;
	}


 
	public String getOriginAppLocationCode(){ 
		return this.originAppLocationCode;
	}
 
	public int getObjectId() { 
 		return this.orderId; 
	} 
 
	public void setObjectId(int selfId){ 
		this.orderId = selfId; 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		this.orderId = rs.getInt("order_id");
		this.orderTypeId = rs.getInt("order_type_id");
		this.conceptId = rs.getInt("concept_id");
		this.orderer = rs.getInt("orderer");
		this.encounterId = rs.getInt("encounter_id");
		this.instructions = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("instructions") != null ? rs.getString("instructions").trim() : null);
		this.dateActivated =  rs.getTimestamp("date_activated") != null ? new java.util.Date( rs.getTimestamp("date_activated").getTime() ) : null;
		this.autoExpireDate =  rs.getTimestamp("auto_expire_date") != null ? new java.util.Date( rs.getTimestamp("auto_expire_date").getTime() ) : null;
		this.dateStopped =  rs.getTimestamp("date_stopped") != null ? new java.util.Date( rs.getTimestamp("date_stopped").getTime() ) : null;
		this.orderReason = rs.getInt("order_reason");
		this.creator = rs.getInt("creator");
		this.dateCreated =  rs.getTimestamp("date_created") != null ? new java.util.Date( rs.getTimestamp("date_created").getTime() ) : null;
		this.voided = rs.getByte("voided");
		this.voidedBy = rs.getInt("voided_by");
		this.dateVoided =  rs.getTimestamp("date_voided") != null ? new java.util.Date( rs.getTimestamp("date_voided").getTime() ) : null;
		this.voidReason = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("void_reason") != null ? rs.getString("void_reason").trim() : null);
		this.patientId = rs.getInt("patient_id");
		this.accessionNumber = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("accession_number") != null ? rs.getString("accession_number").trim() : null);
		this.uuid = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("uuid") != null ? rs.getString("uuid").trim() : null);
		this.orderReasonNonCoded = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("order_reason_non_coded") != null ? rs.getString("order_reason_non_coded").trim() : null);
		this.urgency = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("urgency") != null ? rs.getString("urgency").trim() : null);
		this.orderNumber = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("order_number") != null ? rs.getString("order_number").trim() : null);
		this.previousOrderId = rs.getInt("previous_order_id");
		this.orderAction = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("order_action") != null ? rs.getString("order_action").trim() : null);
		this.commentToFulfiller = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("comment_to_fulfiller") != null ? rs.getString("comment_to_fulfiller").trim() : null);
		this.careSetting = rs.getInt("care_setting");
		this.scheduledDate =  rs.getTimestamp("scheduled_date") != null ? new java.util.Date( rs.getTimestamp("scheduled_date").getTime() ) : null;
		this.consistent = rs.getInt("consistent");
		this.lastSyncDate =  rs.getTimestamp("last_sync_date") != null ? new java.util.Date( rs.getTimestamp("last_sync_date").getTime() ) : null;
		this.originRecordId = rs.getInt("origin_record_id");
		this.dateChanged =  rs.getTimestamp("date_changed") != null ? new java.util.Date( rs.getTimestamp("date_changed").getTime() ) : null;
		this.originAppLocationCode = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("origin_app_location_code") != null ? rs.getString("origin_app_location_code").trim() : null);
	} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "order_id"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParams(){ 
 		Object[] params = {this.orderTypeId == 0 ? null : this.orderTypeId, this.conceptId, this.orderer == 0 ? null : this.orderer, this.encounterId == 0 ? null : this.encounterId, this.instructions, this.dateActivated, this.autoExpireDate, this.dateStopped, this.orderReason == 0 ? null : this.orderReason, this.creator == 0 ? null : this.creator, this.dateCreated, this.voided, this.voidedBy == 0 ? null : this.voidedBy, this.dateVoided, this.voidReason, this.patientId == 0 ? null : this.patientId, this.accessionNumber, this.uuid, this.orderReasonNonCoded, this.urgency, this.orderNumber, this.previousOrderId == 0 ? null : this.previousOrderId, this.orderAction, this.commentToFulfiller, this.careSetting == 0 ? null : this.careSetting, this.scheduledDate, this.consistent, this.lastSyncDate, this.originRecordId, this.dateChanged, this.originAppLocationCode};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.orderTypeId == 0 ? null : this.orderTypeId, this.conceptId, this.orderer == 0 ? null : this.orderer, this.encounterId == 0 ? null : this.encounterId, this.instructions, this.dateActivated, this.autoExpireDate, this.dateStopped, this.orderReason == 0 ? null : this.orderReason, this.creator == 0 ? null : this.creator, this.dateCreated, this.voided, this.voidedBy == 0 ? null : this.voidedBy, this.dateVoided, this.voidReason, this.patientId == 0 ? null : this.patientId, this.accessionNumber, this.uuid, this.orderReasonNonCoded, this.urgency, this.orderNumber, this.previousOrderId == 0 ? null : this.previousOrderId, this.orderAction, this.commentToFulfiller, this.careSetting == 0 ? null : this.careSetting, this.scheduledDate, this.consistent, this.lastSyncDate, this.originRecordId, this.dateChanged, this.originAppLocationCode, this.orderId};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQL(){ 
 		return "INSERT INTO orders(order_type_id, concept_id, orderer, encounter_id, instructions, date_activated, auto_expire_date, date_stopped, order_reason, creator, date_created, voided, voided_by, date_voided, void_reason, patient_id, accession_number, uuid, order_reason_non_coded, urgency, order_number, previous_order_id, order_action, comment_to_fulfiller, care_setting, scheduled_date, consistent, last_sync_date, origin_record_id, date_changed, origin_app_location_code) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE orders SET order_type_id = ?, concept_id = ?, orderer = ?, encounter_id = ?, instructions = ?, date_activated = ?, auto_expire_date = ?, date_stopped = ?, order_reason = ?, creator = ?, date_created = ?, voided = ?, voided_by = ?, date_voided = ?, void_reason = ?, patient_id = ?, accession_number = ?, uuid = ?, order_reason_non_coded = ?, urgency = ?, order_number = ?, previous_order_id = ?, order_action = ?, comment_to_fulfiller = ?, care_setting = ?, scheduled_date = ?, consistent = ?, last_sync_date = ?, origin_record_id = ?, date_changed = ?, origin_app_location_code = ? WHERE order_id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.orderTypeId == 0 ? null : this.orderTypeId) + "," + (this.conceptId) + "," + (this.orderer == 0 ? null : this.orderer) + "," + (this.encounterId == 0 ? null : this.encounterId) + "," + (this.instructions != null ? "\""+ utilities.scapeQuotationMarks(instructions)  +"\"" : null) + "," + (this.dateActivated != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateActivated)  +"\"" : null) + "," + (this.autoExpireDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(autoExpireDate)  +"\"" : null) + "," + (this.dateStopped != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateStopped)  +"\"" : null) + "," + (this.orderReason == 0 ? null : this.orderReason) + "," + (this.creator == 0 ? null : this.creator) + "," + (this.dateCreated != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateCreated)  +"\"" : null) + "," + (this.voided) + "," + (this.voidedBy == 0 ? null : this.voidedBy) + "," + (this.dateVoided != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateVoided)  +"\"" : null) + "," + (this.voidReason != null ? "\""+ utilities.scapeQuotationMarks(voidReason)  +"\"" : null) + "," + (this.patientId == 0 ? null : this.patientId) + "," + (this.accessionNumber != null ? "\""+ utilities.scapeQuotationMarks(accessionNumber)  +"\"" : null) + "," + (this.uuid != null ? "\""+ utilities.scapeQuotationMarks(uuid)  +"\"" : null) + "," + (this.orderReasonNonCoded != null ? "\""+ utilities.scapeQuotationMarks(orderReasonNonCoded)  +"\"" : null) + "," + (this.urgency != null ? "\""+ utilities.scapeQuotationMarks(urgency)  +"\"" : null) + "," + (this.orderNumber != null ? "\""+ utilities.scapeQuotationMarks(orderNumber)  +"\"" : null) + "," + (this.previousOrderId == 0 ? null : this.previousOrderId) + "," + (this.orderAction != null ? "\""+ utilities.scapeQuotationMarks(orderAction)  +"\"" : null) + "," + (this.commentToFulfiller != null ? "\""+ utilities.scapeQuotationMarks(commentToFulfiller)  +"\"" : null) + "," + (this.careSetting == 0 ? null : this.careSetting) + "," + (this.scheduledDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(scheduledDate)  +"\"" : null) + "," + (this.consistent) + "," + (this.lastSyncDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(lastSyncDate)  +"\"" : null) + "," + (this.originRecordId) + "," + (this.dateChanged != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateChanged)  +"\"" : null) + "," + (this.originAppLocationCode != null ? "\""+ utilities.scapeQuotationMarks(originAppLocationCode)  +"\"" : null); 
	} 
 
	@Override
	public boolean isGeneratedFromSkeletonClass() {
		return false;
	}

	@Override
	public boolean hasParents() {
		if (this.orderReason != 0) return true;
		if (this.orderer != 0) return true;
		if (this.creator != 0) return true;
		if (this.patientId != 0) return true;
		if (this.careSetting != 0) return true;
		if (this.encounterId != 0) return true;
		if (this.previousOrderId != 0) return true;
		if (this.orderTypeId != 0) return true;
		if (this.voidedBy != 0) return true;
		return false;
	}

	@Override
	public int retrieveSharedPKKey(Connection conn) throws ParentNotYetMigratedException, DBException {
		throw new RuntimeException("No PKSharedInfo defined!");	}

	@Override
	public void loadDestParentInfo(Connection conn) throws ParentNotYetMigratedException, DBException {
		OpenMRSObject parentOnDestination = null;
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.destinationpkg.UsersVO.class, this.creator, false, conn); 
		this.creator = 0;
		if (parentOnDestination  != null) this.creator = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.destinationpkg.PatientVO.class, this.patientId, false, conn); 
		this.patientId = 0;
		if (parentOnDestination  != null) this.patientId = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.destinationpkg.EncounterVO.class, this.encounterId, false, conn); 
		this.encounterId = 0;
		if (parentOnDestination  != null) this.encounterId = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.destinationpkg.OrdersVO.class, this.previousOrderId, true, conn); 
		this.previousOrderId = 0;
		if (parentOnDestination  != null) this.previousOrderId = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.destinationpkg.UsersVO.class, this.voidedBy, true, conn); 
		this.voidedBy = 0;
		if (parentOnDestination  != null) this.voidedBy = parentOnDestination.getObjectId();
 
	}

	@Override
	public int getParentValue(String parentAttName) {		
		if (parentAttName.equals("orderReason")) return this.orderReason;		
		if (parentAttName.equals("orderer")) return this.orderer;		
		if (parentAttName.equals("creator")) return this.creator;		
		if (parentAttName.equals("patientId")) return this.patientId;		
		if (parentAttName.equals("careSetting")) return this.careSetting;		
		if (parentAttName.equals("encounterId")) return this.encounterId;		
		if (parentAttName.equals("previousOrderId")) return this.previousOrderId;		
		if (parentAttName.equals("orderTypeId")) return this.orderTypeId;		
		if (parentAttName.equals("voidedBy")) return this.voidedBy;

		throw new RuntimeException("No found parent for: " + parentAttName);	}


}