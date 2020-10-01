package org.openmrs.module.eptssync.model.openmrs.sourcepkg; 
 
import org.openmrs.module.eptssync.model.openmrs.generic.*; 
 
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.eptssync.utilities.db.conn.DBException; 
import org.openmrs.module.eptssync.exceptions.ParentNotYetMigratedException; 
 
import java.sql.Connection; 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class GaacVO extends AbstractOpenMRSObject implements OpenMRSObject { 
	private int gaacId;
	private String name;
	private String description;
	private String gaacIdentifier;
	private java.util.Date startDate;
	private java.util.Date endDate;
	private int focalPatientId;
	private int affinityType;
	private int locationId;
	private short crumbled;
	private String reasonCrumbled;
	private java.util.Date dateCrumbled;
	private int creator;
	private java.util.Date dateCreated;
	private int changedBy;
	private java.util.Date dateChanged;
	private short voided;
	private int voidedBy;
	private java.util.Date dateVoided;
	private String voidReason;
	private String uuid;
 
	public GaacVO() { 
		this.metadata = false;
	} 
 
	public void setGaacId(int gaacId){ 
	 	this.gaacId = gaacId;
	}
 
	public int getGaacId(){ 
		return this.gaacId;
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
 
	public void setGaacIdentifier(String gaacIdentifier){ 
	 	this.gaacIdentifier = gaacIdentifier;
	}
 
	public String getGaacIdentifier(){ 
		return this.gaacIdentifier;
	}
 
	public void setStartDate(java.util.Date startDate){ 
	 	this.startDate = startDate;
	}
 
	public java.util.Date getStartDate(){ 
		return this.startDate;
	}
 
	public void setEndDate(java.util.Date endDate){ 
	 	this.endDate = endDate;
	}
 
	public java.util.Date getEndDate(){ 
		return this.endDate;
	}
 
	public void setFocalPatientId(int focalPatientId){ 
	 	this.focalPatientId = focalPatientId;
	}
 
	public int getFocalPatientId(){ 
		return this.focalPatientId;
	}
 
	public void setAffinityType(int affinityType){ 
	 	this.affinityType = affinityType;
	}
 
	public int getAffinityType(){ 
		return this.affinityType;
	}
 
	public void setLocationId(int locationId){ 
	 	this.locationId = locationId;
	}
 
	public int getLocationId(){ 
		return this.locationId;
	}
 
	public void setCrumbled(short crumbled){ 
	 	this.crumbled = crumbled;
	}
 
	public short getCrumbled(){ 
		return this.crumbled;
	}
 
	public void setReasonCrumbled(String reasonCrumbled){ 
	 	this.reasonCrumbled = reasonCrumbled;
	}
 
	public String getReasonCrumbled(){ 
		return this.reasonCrumbled;
	}
 
	public void setDateCrumbled(java.util.Date dateCrumbled){ 
	 	this.dateCrumbled = dateCrumbled;
	}
 
	public java.util.Date getDateCrumbled(){ 
		return this.dateCrumbled;
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
 
	public void setVoided(short voided){ 
	 	this.voided = voided;
	}
 
	public short getVoided(){ 
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
 		return this.gaacId; 
	} 
 
	public void setObjectId(int selfId){ 
		this.gaacId = selfId; 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		this.gaacId = rs.getInt("gaac_id");
		this.name = rs.getString("name") != null ? rs.getString("name").trim() : null;
		this.description = rs.getString("description") != null ? rs.getString("description").trim() : null;
		this.gaacIdentifier = rs.getString("gaac_identifier") != null ? rs.getString("gaac_identifier").trim() : null;
		this.startDate =  rs.getTimestamp("start_date") != null ? new java.util.Date( rs.getTimestamp("start_date").getTime() ) : null;
		this.endDate =  rs.getTimestamp("end_date") != null ? new java.util.Date( rs.getTimestamp("end_date").getTime() ) : null;
		this.focalPatientId = rs.getInt("focal_patient_id");
		this.affinityType = rs.getInt("affinity_type");
		this.locationId = rs.getInt("location_id");
		this.crumbled = rs.getShort("crumbled");
		this.reasonCrumbled = rs.getString("reason_crumbled") != null ? rs.getString("reason_crumbled").trim() : null;
		this.dateCrumbled =  rs.getTimestamp("date_crumbled") != null ? new java.util.Date( rs.getTimestamp("date_crumbled").getTime() ) : null;
		this.creator = rs.getInt("creator");
		this.dateCreated =  rs.getTimestamp("date_created") != null ? new java.util.Date( rs.getTimestamp("date_created").getTime() ) : null;
		this.changedBy = rs.getInt("changed_by");
		this.dateChanged =  rs.getTimestamp("date_changed") != null ? new java.util.Date( rs.getTimestamp("date_changed").getTime() ) : null;
		this.voided = rs.getShort("voided");
		this.voidedBy = rs.getInt("voided_by");
		this.dateVoided =  rs.getTimestamp("date_voided") != null ? new java.util.Date( rs.getTimestamp("date_voided").getTime() ) : null;
		this.voidReason = rs.getString("void_reason") != null ? rs.getString("void_reason").trim() : null;
			} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "gaac_id"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParams(){ 
 		Object[] params = {this.name, this.description, this.gaacIdentifier, this.startDate, this.endDate, this.focalPatientId == 0 ? null : this.focalPatientId, this.affinityType == 0 ? null : this.affinityType, this.locationId == 0 ? null : this.locationId, this.crumbled, this.reasonCrumbled, this.dateCrumbled, this.creator == 0 ? null : this.creator, this.dateCreated, this.changedBy == 0 ? null : this.changedBy, this.dateChanged, this.voided, this.voidedBy == 0 ? null : this.voidedBy, this.dateVoided, this.voidReason, this.uuid};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.name, this.description, this.gaacIdentifier, this.startDate, this.endDate, this.focalPatientId == 0 ? null : this.focalPatientId, this.affinityType == 0 ? null : this.affinityType, this.locationId == 0 ? null : this.locationId, this.crumbled, this.reasonCrumbled, this.dateCrumbled, this.creator == 0 ? null : this.creator, this.dateCreated, this.changedBy == 0 ? null : this.changedBy, this.dateChanged, this.voided, this.voidedBy == 0 ? null : this.voidedBy, this.dateVoided, this.voidReason, this.uuid, this.gaacId};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQL(){ 
 		return "INSERT INTO gaac(name, description, gaac_identifier, start_date, end_date, focal_patient_id, affinity_type, location_id, crumbled, reason_crumbled, date_crumbled, creator, date_created, changed_by, date_changed, voided, voided_by, date_voided, void_reason, uuid) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE gaac SET name = ?, description = ?, gaac_identifier = ?, start_date = ?, end_date = ?, focal_patient_id = ?, affinity_type = ?, location_id = ?, crumbled = ?, reason_crumbled = ?, date_crumbled = ?, creator = ?, date_created = ?, changed_by = ?, date_changed = ?, voided = ?, voided_by = ?, date_voided = ?, void_reason = ?, uuid = ? WHERE gaac_id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.name != null ? "\""+name+"\"" : null) + "," + (this.description != null ? "\""+description+"\"" : null) + "," + (this.gaacIdentifier != null ? "\""+gaacIdentifier+"\"" : null) + "," + (this.startDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(startDate)  +"\"" : null) + "," + (this.endDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(endDate)  +"\"" : null) + "," + (this.focalPatientId == 0 ? null : this.focalPatientId) + "," + (this.affinityType == 0 ? null : this.affinityType) + "," + (this.locationId == 0 ? null : this.locationId) + "," + (this.crumbled) + "," + (this.reasonCrumbled != null ? "\""+reasonCrumbled+"\"" : null) + "," + (this.dateCrumbled != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateCrumbled)  +"\"" : null) + "," + (this.creator == 0 ? null : this.creator) + "," + (this.dateCreated != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateCreated)  +"\"" : null) + "," + (this.changedBy == 0 ? null : this.changedBy) + "," + (this.dateChanged != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateChanged)  +"\"" : null) + "," + (this.voided) + "," + (this.voidedBy == 0 ? null : this.voidedBy) + "," + (this.dateVoided != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateVoided)  +"\"" : null) + "," + (this.voidReason != null ? "\""+voidReason+"\"" : null) + "," + (this.uuid != null ? "\""+uuid+"\"" : null); 
	} 
 
	@Override
	public boolean isGeneratedFromSkeletonClass() {
		return false;
	}

	@Override
	public boolean hasParents() {
		if (this.affinityType != 0) return true;
		if (this.focalPatientId != 0) return true;
		if (this.locationId != 0) return true;
		if (this.changedBy != 0) return true;
		if (this.creator != 0) return true;
		if (this.voidedBy != 0) return true;
		return false;
	}

	@Override
	public int retrieveSharedPKKey(Connection conn) throws ParentNotYetMigratedException, DBException {
		throw new RuntimeException("No PKSharedInfo defined!");	}

	@Override
	public void loadDestParentInfo(Connection conn) throws ParentNotYetMigratedException, DBException {
		OpenMRSObject parentOnDestination = null;
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.sourcepkg.GaacAffinityTypeVO.class, this.affinityType, true, conn); 
		this.affinityType = 0;
		if (parentOnDestination  != null) this.affinityType = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.sourcepkg.PatientVO.class, this.focalPatientId, true, conn); 
		this.focalPatientId = 0;
		if (parentOnDestination  != null) this.focalPatientId = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.sourcepkg.LocationVO.class, this.locationId, true, conn); 
		this.locationId = 0;
		if (parentOnDestination  != null) this.locationId = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.sourcepkg.UsersVO.class, this.changedBy, true, conn); 
		this.changedBy = 0;
		if (parentOnDestination  != null) this.changedBy = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.sourcepkg.UsersVO.class, this.creator, false, conn); 
		this.creator = 0;
		if (parentOnDestination  != null) this.creator = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.sourcepkg.UsersVO.class, this.voidedBy, true, conn); 
		this.voidedBy = 0;
		if (parentOnDestination  != null) this.voidedBy = parentOnDestination.getObjectId();
 
	}

	@Override
	public int getParentValue(String parentAttName) {		
		if (parentAttName.equals("affinityType")) return this.affinityType;		
		if (parentAttName.equals("focalPatientId")) return this.focalPatientId;		
		if (parentAttName.equals("locationId")) return this.locationId;		
		if (parentAttName.equals("changedBy")) return this.changedBy;		
		if (parentAttName.equals("creator")) return this.creator;		
		if (parentAttName.equals("voidedBy")) return this.voidedBy;

		throw new RuntimeException("No found parent for: " + parentAttName);	}


}