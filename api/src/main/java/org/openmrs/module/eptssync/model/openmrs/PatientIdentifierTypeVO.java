package org.openmrs.module.eptssync.model.openmrs; 
 
import org.openmrs.module.eptssync.model.openmrs.generic.*; 
 
import org.openmrs.module.eptssync.utilities.db.conn.DBException; 
import org.openmrs.module.eptssync.utilities.db.conn.OpenConnection; 
import org.openmrs.module.eptssync.exceptions.ParentNotYetMigratedException; 
 
import java.sql.Connection; 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class PatientIdentifierTypeVO extends AbstractOpenMRSObject implements OpenMRSObject { 
	private int patientIdentifierTypeId;
	private String name;
	private String description;
	private String format;
	private byte checkDigit;
	private int creator;
	private java.util.Date dateCreated;
	private byte required;
	private String formatDescription;
	private String validator;
	private byte retired;
	private int retiredBy;
	private java.util.Date dateRetired;
	private String retireReason;
	private String uuid;
	private String locationBehavior;
	private String uniquenessBehavior;
	private byte swappable;
	private java.util.Date lastSyncDate;
	private int originRecordId;
	private java.util.Date dateChanged;
	private String originAppLocationCode;
 
	public PatientIdentifierTypeVO() { 
		this.metadata = true;
	} 
 
	public void setPatientIdentifierTypeId(int patientIdentifierTypeId){ 
	 	this.patientIdentifierTypeId = patientIdentifierTypeId;
	}
 
	public int getPatientIdentifierTypeId(){ 
		return this.patientIdentifierTypeId;
	}
 
	public void setName(String name){ 
	 	this.name = name;
	}
 
	public String getName(){ 
		return this.name;
	}
 
	public void setDescription(String description){ 
	 	this.description = description;
	}
 
	public String getDescription(){ 
		return this.description;
	}
 
	public void setFormat(String format){ 
	 	this.format = format;
	}
 
	public String getFormat(){ 
		return this.format;
	}
 
	public void setCheckDigit(byte checkDigit){ 
	 	this.checkDigit = checkDigit;
	}
 
	public byte getCheckDigit(){ 
		return this.checkDigit;
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
 
	public void setRequired(byte required){ 
	 	this.required = required;
	}
 
	public byte getRequired(){ 
		return this.required;
	}
 
	public void setFormatDescription(String formatDescription){ 
	 	this.formatDescription = formatDescription;
	}
 
	public String getFormatDescription(){ 
		return this.formatDescription;
	}
 
	public void setValidator(String validator){ 
	 	this.validator = validator;
	}
 
	public String getValidator(){ 
		return this.validator;
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
 
	public void setUuid(String uuid){ 
	 	this.uuid = uuid;
	}
 
	public String getUuid(){ 
		return this.uuid;
	}
 
	public void setLocationBehavior(String locationBehavior){ 
	 	this.locationBehavior = locationBehavior;
	}
 
	public String getLocationBehavior(){ 
		return this.locationBehavior;
	}
 
	public void setUniquenessBehavior(String uniquenessBehavior){ 
	 	this.uniquenessBehavior = uniquenessBehavior;
	}
 
	public String getUniquenessBehavior(){ 
		return this.uniquenessBehavior;
	}
 
	public void setSwappable(byte swappable){ 
	 	this.swappable = swappable;
	}
 
	public byte getSwappable(){ 
		return this.swappable;
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
 		return this.patientIdentifierTypeId; 
	} 
 
	public void setObjectId(int selfId){ 
		this.patientIdentifierTypeId = selfId; 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		this.patientIdentifierTypeId = rs.getInt("patient_identifier_type_id");
		this.name = rs.getString("name") != null ? rs.getString("name").trim() : null;
		this.description = rs.getString("description") != null ? rs.getString("description").trim() : null;
		this.format = rs.getString("format") != null ? rs.getString("format").trim() : null;
		this.checkDigit = rs.getByte("check_digit");
		this.creator = rs.getInt("creator");
		this.dateCreated =  rs.getTimestamp("date_created") != null ? new java.util.Date( rs.getTimestamp("date_created").getTime() ) : null;
		this.required = rs.getByte("required");
		this.formatDescription = rs.getString("format_description") != null ? rs.getString("format_description").trim() : null;
		this.validator = rs.getString("validator") != null ? rs.getString("validator").trim() : null;
		this.retired = rs.getByte("retired");
		this.retiredBy = rs.getInt("retired_by");
		this.dateRetired =  rs.getTimestamp("date_retired") != null ? new java.util.Date( rs.getTimestamp("date_retired").getTime() ) : null;
		this.retireReason = rs.getString("retire_reason") != null ? rs.getString("retire_reason").trim() : null;
		this.uuid = rs.getString("uuid") != null ? rs.getString("uuid").trim() : null;
		this.locationBehavior = rs.getString("location_behavior") != null ? rs.getString("location_behavior").trim() : null;
		this.uniquenessBehavior = rs.getString("uniqueness_behavior") != null ? rs.getString("uniqueness_behavior").trim() : null;
		this.swappable = rs.getByte("swappable");
		this.lastSyncDate =  rs.getTimestamp("last_sync_date") != null ? new java.util.Date( rs.getTimestamp("last_sync_date").getTime() ) : null;
		this.originRecordId = rs.getInt("origin_record_id");
		this.dateChanged =  rs.getTimestamp("date_changed") != null ? new java.util.Date( rs.getTimestamp("date_changed").getTime() ) : null;
			} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "patient_identifier_type_id"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParams(){ 
 		Object[] params = {this.name, this.description, this.format, this.checkDigit, this.creator == 0 ? null : this.creator, this.dateCreated, this.required, this.formatDescription, this.validator, this.retired, this.retiredBy == 0 ? null : this.retiredBy, this.dateRetired, this.retireReason, this.uuid, this.locationBehavior, this.uniquenessBehavior, this.swappable, this.lastSyncDate, this.originRecordId, this.dateChanged, this.originAppLocationCode};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.name, this.description, this.format, this.checkDigit, this.creator == 0 ? null : this.creator, this.dateCreated, this.required, this.formatDescription, this.validator, this.retired, this.retiredBy == 0 ? null : this.retiredBy, this.dateRetired, this.retireReason, this.uuid, this.locationBehavior, this.uniquenessBehavior, this.swappable, this.lastSyncDate, this.originRecordId, this.dateChanged, this.originAppLocationCode, this.patientIdentifierTypeId};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQL(){ 
 		return "INSERT INTO patient_identifier_type(name, description, format, check_digit, creator, date_created, required, format_description, validator, retired, retired_by, date_retired, retire_reason, uuid, location_behavior, uniqueness_behavior, swappable, last_sync_date, origin_record_id, date_changed, origin_app_location_code) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE patient_identifier_type SET name = ?, description = ?, format = ?, check_digit = ?, creator = ?, date_created = ?, required = ?, format_description = ?, validator = ?, retired = ?, retired_by = ?, date_retired = ?, retire_reason = ?, uuid = ?, location_behavior = ?, uniqueness_behavior = ?, swappable = ?, last_sync_date = ?, origin_record_id = ?, date_changed = ?, origin_app_location_code = ? WHERE patient_identifier_type_id = ?;"; 
	} 
 
	public void loadDestParentInfo(Connection conn) throws ParentNotYetMigratedException, DBException {
		OpenMRSObject parentOnDestination = null;
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.UsersVO.class, this.creator, false, conn); 
		this.creator = 0;
		if (parentOnDestination  != null) this.creator = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.UsersVO.class, this.retiredBy, true, conn); 
		this.retiredBy = 0;
		if (parentOnDestination  != null) this.retiredBy = parentOnDestination.getObjectId();
 
	}
}