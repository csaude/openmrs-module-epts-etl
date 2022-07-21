package org.openmrs.module.eptssync.model.pojo.destination;

import org.openmrs.module.eptssync.model.pojo.generic.*; 
 
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.eptssync.utilities.AttDefinedElements; 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class OrdersVO extends AbstractOpenMRSObject implements OpenMRSObject { 
	private Integer orderId;
	private Integer orderTypeId;
	private Integer conceptId;
	private Integer orderer;
	private Integer encounterId;
	private String instructions;
	private java.util.Date dateActivated;
	private java.util.Date autoExpireDate;
	private java.util.Date dateStopped;
	private Integer orderReason;
	private Integer creator;
	private byte voided;
	private Integer voidedBy;
	private java.util.Date dateVoided;
	private String voidReason;
	private Integer patientId;
	private String accessionNumber;
	private String orderReasonNonCoded;
	private String urgency;
	private String orderNumber;
	private Integer previousOrderId;
	private String orderAction;
	private String commentToFulfiller;
	private Integer careSetting;
	private java.util.Date scheduledDate;
	private Integer orderGroupId;
	private double sortWeight;
	private String fulfillerComment;
	private String fulfillerStatus;
 
	public OrdersVO() { 
		this.metadata = false;
	} 
 
	public void setOrderId(Integer orderId){ 
	 	this.orderId = orderId;
	}
 
	public Integer getOrderId(){ 
		return this.orderId;
	}
 
	public void setOrderTypeId(Integer orderTypeId){ 
	 	this.orderTypeId = orderTypeId;
	}
 
	public Integer getOrderTypeId(){ 
		return this.orderTypeId;
	}
 
	public void setConceptId(Integer conceptId){ 
	 	this.conceptId = conceptId;
	}
 
	public Integer getConceptId(){ 
		return this.conceptId;
	}
 
	public void setOrderer(Integer orderer){ 
	 	this.orderer = orderer;
	}
 
	public Integer getOrderer(){ 
		return this.orderer;
	}
 
	public void setEncounterId(Integer encounterId){ 
	 	this.encounterId = encounterId;
	}
 
	public Integer getEncounterId(){ 
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
 
	public void setOrderReason(Integer orderReason){ 
	 	this.orderReason = orderReason;
	}
 
	public Integer getOrderReason(){ 
		return this.orderReason;
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
 
	public void setPatientId(Integer patientId){ 
	 	this.patientId = patientId;
	}
 
	public Integer getPatientId(){ 
		return this.patientId;
	}
 
	public void setAccessionNumber(String accessionNumber){ 
	 	this.accessionNumber = accessionNumber;
	}
 
	public String getAccessionNumber(){ 
		return this.accessionNumber;
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
 
	public void setPreviousOrderId(Integer previousOrderId){ 
	 	this.previousOrderId = previousOrderId;
	}
 
	public Integer getPreviousOrderId(){ 
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
 
	public void setCareSetting(Integer careSetting){ 
	 	this.careSetting = careSetting;
	}
 
	public Integer getCareSetting(){ 
		return this.careSetting;
	}
 
	public void setScheduledDate(java.util.Date scheduledDate){ 
	 	this.scheduledDate = scheduledDate;
	}
 
	public java.util.Date getScheduledDate(){ 
		return this.scheduledDate;
	}
 
	public void setOrderGroupId(Integer orderGroupId){ 
	 	this.orderGroupId = orderGroupId;
	}
 
	public Integer getOrderGroupId(){ 
		return this.orderGroupId;
	}
 
	public void setSortWeight(double sortWeight){ 
	 	this.sortWeight = sortWeight;
	}
 
	public double getSortWeight(){ 
		return this.sortWeight;
	}
 
	public void setFulfillerComment(String fulfillerComment){ 
	 	this.fulfillerComment = fulfillerComment;
	}
 
	public String getFulfillerComment(){ 
		return this.fulfillerComment;
	}
 
	public void setFulfillerStatus(String fulfillerStatus){ 
	 	this.fulfillerStatus = fulfillerStatus;
	}


 
	public String getFulfillerStatus(){ 
		return this.fulfillerStatus;
	}
 
	public Integer getObjectId() { 
 		return this.orderId; 
	} 
 
	public void setObjectId(Integer selfId){ 
		this.orderId = selfId; 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		super.load(rs);
		if (rs.getObject("order_id") != null) this.orderId = rs.getInt("order_id");
		if (rs.getObject("order_type_id") != null) this.orderTypeId = rs.getInt("order_type_id");
		if (rs.getObject("concept_id") != null) this.conceptId = rs.getInt("concept_id");
		if (rs.getObject("orderer") != null) this.orderer = rs.getInt("orderer");
		if (rs.getObject("encounter_id") != null) this.encounterId = rs.getInt("encounter_id");
		this.instructions = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("instructions") != null ? rs.getString("instructions").trim() : null);
		this.dateActivated =  rs.getTimestamp("date_activated") != null ? new java.util.Date( rs.getTimestamp("date_activated").getTime() ) : null;
		this.autoExpireDate =  rs.getTimestamp("auto_expire_date") != null ? new java.util.Date( rs.getTimestamp("auto_expire_date").getTime() ) : null;
		this.dateStopped =  rs.getTimestamp("date_stopped") != null ? new java.util.Date( rs.getTimestamp("date_stopped").getTime() ) : null;
		if (rs.getObject("order_reason") != null) this.orderReason = rs.getInt("order_reason");
		if (rs.getObject("creator") != null) this.creator = rs.getInt("creator");
		this.dateCreated =  rs.getTimestamp("date_created") != null ? new java.util.Date( rs.getTimestamp("date_created").getTime() ) : null;
		this.voided = rs.getByte("voided");
		if (rs.getObject("voided_by") != null) this.voidedBy = rs.getInt("voided_by");
		this.dateVoided =  rs.getTimestamp("date_voided") != null ? new java.util.Date( rs.getTimestamp("date_voided").getTime() ) : null;
		this.voidReason = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("void_reason") != null ? rs.getString("void_reason").trim() : null);
		if (rs.getObject("patient_id") != null) this.patientId = rs.getInt("patient_id");
		this.accessionNumber = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("accession_number") != null ? rs.getString("accession_number").trim() : null);
		this.uuid = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("uuid") != null ? rs.getString("uuid").trim() : null);
		this.orderReasonNonCoded = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("order_reason_non_coded") != null ? rs.getString("order_reason_non_coded").trim() : null);
		this.urgency = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("urgency") != null ? rs.getString("urgency").trim() : null);
		this.orderNumber = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("order_number") != null ? rs.getString("order_number").trim() : null);
		if (rs.getObject("previous_order_id") != null) this.previousOrderId = rs.getInt("previous_order_id");
		this.orderAction = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("order_action") != null ? rs.getString("order_action").trim() : null);
		this.commentToFulfiller = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("comment_to_fulfiller") != null ? rs.getString("comment_to_fulfiller").trim() : null);
		if (rs.getObject("care_setting") != null) this.careSetting = rs.getInt("care_setting");
		this.scheduledDate =  rs.getTimestamp("scheduled_date") != null ? new java.util.Date( rs.getTimestamp("scheduled_date").getTime() ) : null;
		if (rs.getObject("order_group_id") != null) this.orderGroupId = rs.getInt("order_group_id");
		this.sortWeight = rs.getDouble("sort_weight");
		this.fulfillerComment = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("fulfiller_comment") != null ? rs.getString("fulfiller_comment").trim() : null);
		this.fulfillerStatus = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("fulfiller_status") != null ? rs.getString("fulfiller_status").trim() : null);
	} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "order_id"; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithoutObjectId(){ 
 		return "INSERT INTO orders(order_type_id, concept_id, orderer, encounter_id, instructions, date_activated, auto_expire_date, date_stopped, order_reason, creator, date_created, voided, voided_by, date_voided, void_reason, patient_id, accession_number, uuid, order_reason_non_coded, urgency, order_number, previous_order_id, order_action, comment_to_fulfiller, care_setting, scheduled_date, order_group_id, sort_weight, fulfiller_comment, fulfiller_status) VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithoutObjectId(){ 
 		Object[] params = {this.orderTypeId, this.conceptId, this.orderer, this.encounterId, this.instructions, this.dateActivated, this.autoExpireDate, this.dateStopped, this.orderReason, this.creator, this.dateCreated, this.voided, this.voidedBy, this.dateVoided, this.voidReason, this.patientId, this.accessionNumber, this.uuid, this.orderReasonNonCoded, this.urgency, this.orderNumber, this.previousOrderId, this.orderAction, this.commentToFulfiller, this.careSetting, this.scheduledDate, this.orderGroupId, this.sortWeight, this.fulfillerComment, this.fulfillerStatus};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithObjectId(){ 
 		return "INSERT INTO orders(order_id, order_type_id, concept_id, orderer, encounter_id, instructions, date_activated, auto_expire_date, date_stopped, order_reason, creator, date_created, voided, voided_by, date_voided, void_reason, patient_id, accession_number, uuid, order_reason_non_coded, urgency, order_number, previous_order_id, order_action, comment_to_fulfiller, care_setting, scheduled_date, order_group_id, sort_weight, fulfiller_comment, fulfiller_status) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithObjectId(){ 
 		Object[] params = {this.orderId, this.orderTypeId, this.conceptId, this.orderer, this.encounterId, this.instructions, this.dateActivated, this.autoExpireDate, this.dateStopped, this.orderReason, this.creator, this.dateCreated, this.voided, this.voidedBy, this.dateVoided, this.voidReason, this.patientId, this.accessionNumber, this.uuid, this.orderReasonNonCoded, this.urgency, this.orderNumber, this.previousOrderId, this.orderAction, this.commentToFulfiller, this.careSetting, this.scheduledDate, this.orderGroupId, this.sortWeight, this.fulfillerComment, this.fulfillerStatus};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.orderTypeId, this.conceptId, this.orderer, this.encounterId, this.instructions, this.dateActivated, this.autoExpireDate, this.dateStopped, this.orderReason, this.creator, this.dateCreated, this.voided, this.voidedBy, this.dateVoided, this.voidReason, this.patientId, this.accessionNumber, this.uuid, this.orderReasonNonCoded, this.urgency, this.orderNumber, this.previousOrderId, this.orderAction, this.commentToFulfiller, this.careSetting, this.scheduledDate, this.orderGroupId, this.sortWeight, this.fulfillerComment, this.fulfillerStatus, this.orderId};		return params; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE orders SET order_type_id = ?, concept_id = ?, orderer = ?, encounter_id = ?, instructions = ?, date_activated = ?, auto_expire_date = ?, date_stopped = ?, order_reason = ?, creator = ?, date_created = ?, voided = ?, voided_by = ?, date_voided = ?, void_reason = ?, patient_id = ?, accession_number = ?, uuid = ?, order_reason_non_coded = ?, urgency = ?, order_number = ?, previous_order_id = ?, order_action = ?, comment_to_fulfiller = ?, care_setting = ?, scheduled_date = ?, order_group_id = ?, sort_weight = ?, fulfiller_comment = ?, fulfiller_status = ? WHERE order_id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.orderTypeId) + "," + (this.conceptId) + "," + (this.orderer) + "," + (this.encounterId) + "," + (this.instructions != null ? "\""+ utilities.scapeQuotationMarks(instructions)  +"\"" : null) + "," + (this.dateActivated != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateActivated)  +"\"" : null) + "," + (this.autoExpireDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(autoExpireDate)  +"\"" : null) + "," + (this.dateStopped != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateStopped)  +"\"" : null) + "," + (this.orderReason) + "," + (this.creator) + "," + (this.dateCreated != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateCreated)  +"\"" : null) + "," + (this.voided) + "," + (this.voidedBy) + "," + (this.dateVoided != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateVoided)  +"\"" : null) + "," + (this.voidReason != null ? "\""+ utilities.scapeQuotationMarks(voidReason)  +"\"" : null) + "," + (this.patientId) + "," + (this.accessionNumber != null ? "\""+ utilities.scapeQuotationMarks(accessionNumber)  +"\"" : null) + "," + (this.uuid != null ? "\""+ utilities.scapeQuotationMarks(uuid)  +"\"" : null) + "," + (this.orderReasonNonCoded != null ? "\""+ utilities.scapeQuotationMarks(orderReasonNonCoded)  +"\"" : null) + "," + (this.urgency != null ? "\""+ utilities.scapeQuotationMarks(urgency)  +"\"" : null) + "," + (this.orderNumber != null ? "\""+ utilities.scapeQuotationMarks(orderNumber)  +"\"" : null) + "," + (this.previousOrderId) + "," + (this.orderAction != null ? "\""+ utilities.scapeQuotationMarks(orderAction)  +"\"" : null) + "," + (this.commentToFulfiller != null ? "\""+ utilities.scapeQuotationMarks(commentToFulfiller)  +"\"" : null) + "," + (this.careSetting) + "," + (this.scheduledDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(scheduledDate)  +"\"" : null) + "," + (this.orderGroupId) + "," + (this.sortWeight) + "," + (this.fulfillerComment != null ? "\""+ utilities.scapeQuotationMarks(fulfillerComment)  +"\"" : null) + "," + (this.fulfillerStatus != null ? "\""+ utilities.scapeQuotationMarks(fulfillerStatus)  +"\"" : null); 
	} 
 
	@Override
	public boolean hasParents() {
		if (this.orderReason != 0) return true;

		if (this.orderer != 0) return true;

		if (this.creator != 0) return true;

		if (this.patientId != 0) return true;

		if (this.careSetting != 0) return true;

		if (this.encounterId != 0) return true;

		if (this.orderGroupId != 0) return true;

		if (this.previousOrderId != 0) return true;

		if (this.orderTypeId != 0) return true;

		if (this.voidedBy != 0) return true;

		return false;
	}

	@Override
	public Integer getParentValue(String parentAttName) {		
		if (parentAttName.equals("orderReason")) return this.orderReason;		
		if (parentAttName.equals("orderer")) return this.orderer;		
		if (parentAttName.equals("creator")) return this.creator;		
		if (parentAttName.equals("patientId")) return this.patientId;		
		if (parentAttName.equals("careSetting")) return this.careSetting;		
		if (parentAttName.equals("encounterId")) return this.encounterId;		
		if (parentAttName.equals("orderGroupId")) return this.orderGroupId;		
		if (parentAttName.equals("previousOrderId")) return this.previousOrderId;		
		if (parentAttName.equals("orderTypeId")) return this.orderTypeId;		
		if (parentAttName.equals("voidedBy")) return this.voidedBy;

		throw new RuntimeException("No found parent for: " + parentAttName);	}

	@Override
	public void changeParentValue(String parentAttName, OpenMRSObject newParent) {		
		if (parentAttName.equals("orderReason")) {
			this.orderReason = newParent.getObjectId();
			return;
		}		
		if (parentAttName.equals("orderer")) {
			this.orderer = newParent.getObjectId();
			return;
		}		
		if (parentAttName.equals("creator")) {
			this.creator = newParent.getObjectId();
			return;
		}		
		if (parentAttName.equals("patientId")) {
			this.patientId = newParent.getObjectId();
			return;
		}		
		if (parentAttName.equals("careSetting")) {
			this.careSetting = newParent.getObjectId();
			return;
		}		
		if (parentAttName.equals("encounterId")) {
			this.encounterId = newParent.getObjectId();
			return;
		}		
		if (parentAttName.equals("orderGroupId")) {
			this.orderGroupId = newParent.getObjectId();
			return;
		}		
		if (parentAttName.equals("previousOrderId")) {
			this.previousOrderId = newParent.getObjectId();
			return;
		}		
		if (parentAttName.equals("orderTypeId")) {
			this.orderTypeId = newParent.getObjectId();
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
		if (parentAttName.equals("orderReason")) {
			this.orderReason = null;
			return;
		}		
		if (parentAttName.equals("orderer")) {
			this.orderer = null;
			return;
		}		
		if (parentAttName.equals("creator")) {
			this.creator = null;
			return;
		}		
		if (parentAttName.equals("patientId")) {
			this.patientId = null;
			return;
		}		
		if (parentAttName.equals("careSetting")) {
			this.careSetting = null;
			return;
		}		
		if (parentAttName.equals("encounterId")) {
			this.encounterId = null;
			return;
		}		
		if (parentAttName.equals("orderGroupId")) {
			this.orderGroupId = null;
			return;
		}		
		if (parentAttName.equals("previousOrderId")) {
			this.previousOrderId = null;
			return;
		}		
		if (parentAttName.equals("orderTypeId")) {
			this.orderTypeId = null;
			return;
		}		
		if (parentAttName.equals("voidedBy")) {
			this.voidedBy = null;
			return;
		}

		throw new RuntimeException("No found parent for: " + parentAttName);
	}


}