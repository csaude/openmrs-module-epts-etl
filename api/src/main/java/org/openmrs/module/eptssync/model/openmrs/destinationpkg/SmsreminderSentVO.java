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
 
public class SmsreminderSentVO extends AbstractOpenMRSObject implements OpenMRSObject { 
	private int sentId;
	private String cellNumber;
	private java.util.Date alertDate;
	private String message;
	private int remainDays;
	private int patientId;
	private String status;
	private java.util.Date created;
 
	public SmsreminderSentVO() { 
		this.metadata = false;
	} 
 
	public void setSentId(int sentId){ 
	 	this.sentId = sentId;
	}
 
	public int getSentId(){ 
		return this.sentId;
	}
 
	public void setCellNumber(String cellNumber){ 
	 	this.cellNumber = cellNumber;
	}
 
	public String getCellNumber(){ 
		return this.cellNumber;
	}
 
	public void setAlertDate(java.util.Date alertDate){ 
	 	this.alertDate = alertDate;
	}
 
	public java.util.Date getAlertDate(){ 
		return this.alertDate;
	}
 
	public void setMessage(String message){ 
	 	this.message = message;
	}
 
	public String getMessage(){ 
		return this.message;
	}
 
	public void setRemainDays(int remainDays){ 
	 	this.remainDays = remainDays;
	}
 
	public int getRemainDays(){ 
		return this.remainDays;
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
 
	public void setCreated(java.util.Date created){ 
	 	this.created = created;
	}


 
	public java.util.Date getCreated(){ 
		return this.created;
	}	public String getUuid(){ 
		return null;
	}
 
	public void setUuid(String uuid){ }
 
	public int getOriginRecordId(){ 
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
 		return this.sentId; 
	} 
 
	public void setObjectId(int selfId){ 
		this.sentId = selfId; 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		this.sentId = rs.getInt("sent_id");
		this.cellNumber = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("cell_number") != null ? rs.getString("cell_number").trim() : null);
		this.alertDate =  rs.getTimestamp("alert_date") != null ? new java.util.Date( rs.getTimestamp("alert_date").getTime() ) : null;
		this.message = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("message") != null ? rs.getString("message").trim() : null);
		this.remainDays = rs.getInt("remain_days");
		this.patientId = rs.getInt("patient_id");
		this.status = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("status") != null ? rs.getString("status").trim() : null);
		this.created =  rs.getTimestamp("created") != null ? new java.util.Date( rs.getTimestamp("created").getTime() ) : null;
	} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "sent_id"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParams(){ 
 		Object[] params = {this.cellNumber, this.alertDate, this.message, this.remainDays, this.patientId == 0 ? null : this.patientId, this.status, this.created};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.cellNumber, this.alertDate, this.message, this.remainDays, this.patientId == 0 ? null : this.patientId, this.status, this.created, this.sentId};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQL(){ 
 		return "INSERT INTO smsreminder_sent(cell_number, alert_date, message, remain_days, patient_id, status, created) VALUES(?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE smsreminder_sent SET cell_number = ?, alert_date = ?, message = ?, remain_days = ?, patient_id = ?, status = ?, created = ? WHERE sent_id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.cellNumber != null ? "\""+ utilities.scapeQuotationMarks(cellNumber)  +"\"" : null) + "," + (this.alertDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(alertDate)  +"\"" : null) + "," + (this.message != null ? "\""+ utilities.scapeQuotationMarks(message)  +"\"" : null) + "," + (this.remainDays) + "," + (this.patientId == 0 ? null : this.patientId) + "," + (this.status != null ? "\""+ utilities.scapeQuotationMarks(status)  +"\"" : null) + "," + (this.created != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(created)  +"\"" : null); 
	} 
 
	@Override
	public boolean isGeneratedFromSkeletonClass() {
		return false;
	}

	@Override
	public boolean hasParents() {
		if (this.patientId != 0) return true;
		return false;
	}

	@Override
	public int retrieveSharedPKKey(Connection conn) throws ParentNotYetMigratedException, DBException {
		throw new RuntimeException("No PKSharedInfo defined!");	}

	@Override
	public void loadDestParentInfo(Connection conn) throws ParentNotYetMigratedException, DBException {
		OpenMRSObject parentOnDestination = null;
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.destinationpkg.PatientVO.class, this.patientId, false, conn); 
		this.patientId = 0;
		if (parentOnDestination  != null) this.patientId = parentOnDestination.getObjectId();
 
	}

	@Override
	public int getParentValue(String parentAttName) {		
		if (parentAttName.equals("patientId")) return this.patientId;

		throw new RuntimeException("No found parent for: " + parentAttName);	}


}