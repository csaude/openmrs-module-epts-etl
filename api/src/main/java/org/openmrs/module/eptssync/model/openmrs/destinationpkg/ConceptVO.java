package org.openmrs.module.eptssync.model.openmrs.destinationpkg; 
 
import org.openmrs.module.eptssync.model.openmrs.generic.*; 
 
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.eptssync.utilities.db.conn.DBException; 
import org.openmrs.module.eptssync.exceptions.ParentNotYetMigratedException; 
 
import java.sql.Connection; 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class ConceptVO extends AbstractOpenMRSObject implements OpenMRSObject { 
	private int conceptId;
	private byte retired;
	private String shortName;
	private String description;
	private String formText;
	private int datatypeId;
	private int classId;
	private byte isSet;
	private int creator;
	private java.util.Date dateCreated;
	private String version;
	private int changedBy;
	private java.util.Date dateChanged;
	private int retiredBy;
	private java.util.Date dateRetired;
	private String retireReason;
	private String uuid;
	private int consistent;
	private java.util.Date lastSyncDate;
	private int originRecordId;
	private String originAppLocationCode;
 
	public ConceptVO() { 
		this.metadata = true;
	} 
 
	public void setConceptId(int conceptId){ 
	 	this.conceptId = conceptId;
	}
 
	public int getConceptId(){ 
		return this.conceptId;
	}
 
	public void setRetired(byte retired){ 
	 	this.retired = retired;
	}
 
	public byte getRetired(){ 
		return this.retired;
	}
 
	public void setShortName(String shortName){ 
	 	this.shortName = shortName;
	}
 
	public String getShortName(){ 
		return this.shortName;
	}
 
	public void setDescription(String description){ 
	 	this.description = description;
	}
 
	public String getDescription(){ 
		return this.description;
	}
 
	public void setFormText(String formText){ 
	 	this.formText = formText;
	}
 
	public String getFormText(){ 
		return this.formText;
	}
 
	public void setDatatypeId(int datatypeId){ 
	 	this.datatypeId = datatypeId;
	}
 
	public int getDatatypeId(){ 
		return this.datatypeId;
	}
 
	public void setClassId(int classId){ 
	 	this.classId = classId;
	}
 
	public int getClassId(){ 
		return this.classId;
	}
 
	public void setIsSet(byte isSet){ 
	 	this.isSet = isSet;
	}
 
	public byte getIsSet(){ 
		return this.isSet;
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
 
	public void setVersion(String version){ 
	 	this.version = version;
	}
 
	public String getVersion(){ 
		return this.version;
	}
 
	public void setChangedBy(int changedBy){ 
	 	this.changedBy = changedBy;
	}
 
	public int getChangedBy(){ 
		return this.changedBy;
	}
 
	public void setDateChanged(java.util.Date dateChanged){ 
	 	this.dateChanged = dateChanged;
	}
 
	public java.util.Date getDateChanged(){ 
		return this.dateChanged;
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
 
	public void setOriginAppLocationCode(String originAppLocationCode){ 
	 	this.originAppLocationCode = originAppLocationCode;
	}


 
	public String getOriginAppLocationCode(){ 
		return this.originAppLocationCode;
	}
 
	public int getObjectId() { 
 		return this.conceptId; 
	} 
 
	public void setObjectId(int selfId){ 
		this.conceptId = selfId; 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		this.conceptId = rs.getInt("concept_id");
		this.retired = rs.getByte("retired");
		this.shortName = rs.getString("short_name") != null ? rs.getString("short_name").trim() : null;
		this.description = rs.getString("description") != null ? rs.getString("description").trim() : null;
		this.formText = rs.getString("form_text") != null ? rs.getString("form_text").trim() : null;
		this.datatypeId = rs.getInt("datatype_id");
		this.classId = rs.getInt("class_id");
		this.isSet = rs.getByte("is_set");
		this.creator = rs.getInt("creator");
		this.dateCreated =  rs.getTimestamp("date_created") != null ? new java.util.Date( rs.getTimestamp("date_created").getTime() ) : null;
		this.version = rs.getString("version") != null ? rs.getString("version").trim() : null;
		this.changedBy = rs.getInt("changed_by");
		this.dateChanged =  rs.getTimestamp("date_changed") != null ? new java.util.Date( rs.getTimestamp("date_changed").getTime() ) : null;
		this.retiredBy = rs.getInt("retired_by");
		this.dateRetired =  rs.getTimestamp("date_retired") != null ? new java.util.Date( rs.getTimestamp("date_retired").getTime() ) : null;
		this.retireReason = rs.getString("retire_reason") != null ? rs.getString("retire_reason").trim() : null;
		this.uuid = rs.getString("uuid") != null ? rs.getString("uuid").trim() : null;
		this.consistent = rs.getInt("consistent");
		this.lastSyncDate =  rs.getTimestamp("last_sync_date") != null ? new java.util.Date( rs.getTimestamp("last_sync_date").getTime() ) : null;
		this.originRecordId = rs.getInt("origin_record_id");
			} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "concept_id"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParams(){ 
 		Object[] params = {this.retired, this.shortName, this.description, this.formText, this.datatypeId == 0 ? null : this.datatypeId, this.classId == 0 ? null : this.classId, this.isSet, this.creator == 0 ? null : this.creator, this.dateCreated, this.version, this.changedBy == 0 ? null : this.changedBy, this.dateChanged, this.retiredBy == 0 ? null : this.retiredBy, this.dateRetired, this.retireReason, this.uuid, this.consistent, this.lastSyncDate, this.originRecordId, this.originAppLocationCode};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.retired, this.shortName, this.description, this.formText, this.datatypeId == 0 ? null : this.datatypeId, this.classId == 0 ? null : this.classId, this.isSet, this.creator == 0 ? null : this.creator, this.dateCreated, this.version, this.changedBy == 0 ? null : this.changedBy, this.dateChanged, this.retiredBy == 0 ? null : this.retiredBy, this.dateRetired, this.retireReason, this.uuid, this.consistent, this.lastSyncDate, this.originRecordId, this.originAppLocationCode, this.conceptId};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQL(){ 
 		return "INSERT INTO concept(retired, short_name, description, form_text, datatype_id, class_id, is_set, creator, date_created, version, changed_by, date_changed, retired_by, date_retired, retire_reason, uuid, consistent, last_sync_date, origin_record_id, origin_app_location_code) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE concept SET retired = ?, short_name = ?, description = ?, form_text = ?, datatype_id = ?, class_id = ?, is_set = ?, creator = ?, date_created = ?, version = ?, changed_by = ?, date_changed = ?, retired_by = ?, date_retired = ?, retire_reason = ?, uuid = ?, consistent = ?, last_sync_date = ?, origin_record_id = ?, origin_app_location_code = ? WHERE concept_id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.retired) + "," + (this.shortName != null ? "\""+shortName+"\"" : null) + "," + (this.description != null ? "\""+description+"\"" : null) + "," + (this.formText != null ? "\""+formText+"\"" : null) + "," + (this.datatypeId == 0 ? null : this.datatypeId) + "," + (this.classId == 0 ? null : this.classId) + "," + (this.isSet) + "," + (this.creator == 0 ? null : this.creator) + "," + (this.dateCreated != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateCreated)  +"\"" : null) + "," + (this.version != null ? "\""+version+"\"" : null) + "," + (this.changedBy == 0 ? null : this.changedBy) + "," + (this.dateChanged != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateChanged)  +"\"" : null) + "," + (this.retiredBy == 0 ? null : this.retiredBy) + "," + (this.dateRetired != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateRetired)  +"\"" : null) + "," + (this.retireReason != null ? "\""+retireReason+"\"" : null) + "," + (this.uuid != null ? "\""+uuid+"\"" : null) + "," + (this.consistent) + "," + (this.lastSyncDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(lastSyncDate)  +"\"" : null) + "," + (this.originRecordId) + "," + (this.originAppLocationCode != null ? "\""+originAppLocationCode+"\"" : null); 
	} 
 
	@Override
	public boolean isGeneratedFromSkeletonClass() {
		return false;
	}

	@Override
	public boolean hasParents() {
		if (this.classId != 0) return true;
		if (this.creator != 0) return true;
		if (this.datatypeId != 0) return true;
		if (this.changedBy != 0) return true;
		if (this.retiredBy != 0) return true;
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
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.destinationpkg.ConceptDatatypeVO.class, this.datatypeId, false, conn); 
		this.datatypeId = 0;
		if (parentOnDestination  != null) this.datatypeId = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.destinationpkg.UsersVO.class, this.changedBy, true, conn); 
		this.changedBy = 0;
		if (parentOnDestination  != null) this.changedBy = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.destinationpkg.UsersVO.class, this.retiredBy, true, conn); 
		this.retiredBy = 0;
		if (parentOnDestination  != null) this.retiredBy = parentOnDestination.getObjectId();
 
	}

	@Override
	public int getParentValue(String parentAttName) {		
		if (parentAttName.equals("classId")) return this.classId;		
		if (parentAttName.equals("creator")) return this.creator;		
		if (parentAttName.equals("datatypeId")) return this.datatypeId;		
		if (parentAttName.equals("changedBy")) return this.changedBy;		
		if (parentAttName.equals("retiredBy")) return this.retiredBy;

		throw new RuntimeException("No found parent for: " + parentAttName);	}


}