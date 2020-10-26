package org.openmrs.module.eptssync.model.openmrs.sourcepkg; 
 
import org.openmrs.module.eptssync.model.openmrs.generic.*; 
 
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.eptssync.utilities.db.conn.DBException; 
import org.openmrs.module.eptssync.utilities.AttDefinedElements; 
import org.openmrs.module.eptssync.exceptions.ParentNotYetMigratedException; 
 
import java.sql.Connection; 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class NotificationAlertRecipientVO extends AbstractOpenMRSObject implements OpenMRSObject { 
	private int alertId;
	private int userId;
	private int alertRead;
	private java.util.Date dateChanged;
	private String uuid;
 
	public NotificationAlertRecipientVO() { 
		this.metadata = false;
	} 
 
	public void setAlertId(int alertId){ 
	 	this.alertId = alertId;
	}
 
	public int getAlertId(){ 
		return this.alertId;
	}
 
	public void setUserId(int userId){ 
	 	this.userId = userId;
	}
 
	public int getUserId(){ 
		return this.userId;
	}
 
	public void setAlertRead(int alertRead){ 
	 	this.alertRead = alertRead;
	}
 
	public int getAlertRead(){ 
		return this.alertRead;
	}
 
	public void setDateChanged(java.util.Date dateChanged){ 
	 	this.dateChanged = dateChanged;
	}
 
	public java.util.Date getDateChanged(){ 
		return this.dateChanged;
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
 		return this.alertId; 
	} 
 
	public void setObjectId(int selfId){ 
		this.alertId = selfId; 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		this.alertId = rs.getInt("alert_id");
		this.userId = rs.getInt("user_id");
		this.alertRead = rs.getInt("alert_read");
		this.dateChanged =  rs.getTimestamp("date_changed") != null ? new java.util.Date( rs.getTimestamp("date_changed").getTime() ) : null;
		this.uuid = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("uuid") != null ? rs.getString("uuid").trim() : null);
	} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "alert_id"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParams(){ 
 		Object[] params = {this.userId == 0 ? null : this.userId, this.alertRead, this.dateChanged, this.uuid};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.userId == 0 ? null : this.userId, this.alertRead, this.dateChanged, this.uuid, this.alertId};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQL(){ 
 		return "INSERT INTO notification_alert_recipient(user_id, alert_read, date_changed, uuid) VALUES(?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE notification_alert_recipient SET user_id = ?, alert_read = ?, date_changed = ?, uuid = ? WHERE alert_id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.userId == 0 ? null : this.userId) + "," + (this.alertRead) + "," + (this.dateChanged != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateChanged)  +"\"" : null) + "," + (this.uuid != null ? "\""+ utilities.scapeQuotationMarks(uuid)  +"\"" : null); 
	} 
 
	@Override
	public boolean isGeneratedFromSkeletonClass() {
		return false;
	}

	@Override
	public boolean hasParents() {
		if (this.userId != 0) return true;
		if (this.alertId != 0) return true;
		return false;
	}

	@Override
	public int retrieveSharedPKKey(Connection conn) throws ParentNotYetMigratedException, DBException {
		throw new RuntimeException("No PKSharedInfo defined!");	}

	@Override
	public void loadDestParentInfo(Connection conn) throws ParentNotYetMigratedException, DBException {
		OpenMRSObject parentOnDestination = null;
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.sourcepkg.UsersVO.class, this.userId, false, conn); 
		this.userId = 0;
		if (parentOnDestination  != null) this.userId = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.sourcepkg.NotificationAlertVO.class, this.alertId, false, conn); 
		this.alertId = 0;
		if (parentOnDestination  != null) this.alertId = parentOnDestination.getObjectId();
 
	}

	@Override
	public int getParentValue(String parentAttName) {		
		if (parentAttName.equals("userId")) return this.userId;		
		if (parentAttName.equals("alertId")) return this.alertId;

		throw new RuntimeException("No found parent for: " + parentAttName);	}


}