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
 
public class PersonNameVO extends AbstractOpenMRSObject implements OpenMRSObject { 
	private int personNameId;
	private byte preferred;
	private int personId;
	private String prefix;
	private String givenName;
	private String middleName;
	private String familyNamePrefix;
	private String familyName;
	private String familyName2;
	private String familyNameSuffix;
	private String degree;
	private int creator;
	private java.util.Date dateCreated;
	private byte voided;
	private int voidedBy;
	private java.util.Date dateVoided;
	private String voidReason;
	private int changedBy;
	private java.util.Date dateChanged;
	private String uuid;
	private int consistent;
	private java.util.Date lastSyncDate;
	private int originRecordId;
	private String originAppLocationCode;
 
	public PersonNameVO() { 
		this.metadata = false;
	} 
 
	public void setPersonNameId(int personNameId){ 
	 	this.personNameId = personNameId;
	}
 
	public int getPersonNameId(){ 
		return this.personNameId;
	}
 
	public void setPreferred(byte preferred){ 
	 	this.preferred = preferred;
	}
 
	public byte getPreferred(){ 
		return this.preferred;
	}
 
	public void setPersonId(int personId){ 
	 	this.personId = personId;
	}
 
	public int getPersonId(){ 
		return this.personId;
	}
 
	public void setPrefix(String prefix){ 
	 	this.prefix = prefix;
	}
 
	public String getPrefix(){ 
		return this.prefix;
	}
 
	public void setGivenName(String givenName){ 
	 	this.givenName = givenName;
	}
 
	public String getGivenName(){ 
		return this.givenName;
	}
 
	public void setMiddleName(String middleName){ 
	 	this.middleName = middleName;
	}
 
	public String getMiddleName(){ 
		return this.middleName;
	}
 
	public void setFamilyNamePrefix(String familyNamePrefix){ 
	 	this.familyNamePrefix = familyNamePrefix;
	}
 
	public String getFamilyNamePrefix(){ 
		return this.familyNamePrefix;
	}
 
	public void setFamilyName(String familyName){ 
	 	this.familyName = familyName;
	}
 
	public String getFamilyName(){ 
		return this.familyName;
	}
 
	public void setFamilyName2(String familyName2){ 
	 	this.familyName2 = familyName2;
	}
 
	public String getFamilyName2(){ 
		return this.familyName2;
	}
 
	public void setFamilyNameSuffix(String familyNameSuffix){ 
	 	this.familyNameSuffix = familyNameSuffix;
	}
 
	public String getFamilyNameSuffix(){ 
		return this.familyNameSuffix;
	}
 
	public void setDegree(String degree){ 
	 	this.degree = degree;
	}
 
	public String getDegree(){ 
		return this.degree;
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
 		return this.personNameId; 
	} 
 
	public void setObjectId(int selfId){ 
		this.personNameId = selfId; 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		this.personNameId = rs.getInt("person_name_id");
		this.preferred = rs.getByte("preferred");
		this.personId = rs.getInt("person_id");
		this.prefix = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("prefix") != null ? rs.getString("prefix").trim() : null);
		this.givenName = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("given_name") != null ? rs.getString("given_name").trim() : null);
		this.middleName = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("middle_name") != null ? rs.getString("middle_name").trim() : null);
		this.familyNamePrefix = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("family_name_prefix") != null ? rs.getString("family_name_prefix").trim() : null);
		this.familyName = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("family_name") != null ? rs.getString("family_name").trim() : null);
		this.familyName2 = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("family_name2") != null ? rs.getString("family_name2").trim() : null);
		this.familyNameSuffix = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("family_name_suffix") != null ? rs.getString("family_name_suffix").trim() : null);
		this.degree = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("degree") != null ? rs.getString("degree").trim() : null);
		this.creator = rs.getInt("creator");
		this.dateCreated =  rs.getTimestamp("date_created") != null ? new java.util.Date( rs.getTimestamp("date_created").getTime() ) : null;
		this.voided = rs.getByte("voided");
		this.voidedBy = rs.getInt("voided_by");
		this.dateVoided =  rs.getTimestamp("date_voided") != null ? new java.util.Date( rs.getTimestamp("date_voided").getTime() ) : null;
		this.voidReason = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("void_reason") != null ? rs.getString("void_reason").trim() : null);
		this.changedBy = rs.getInt("changed_by");
		this.dateChanged =  rs.getTimestamp("date_changed") != null ? new java.util.Date( rs.getTimestamp("date_changed").getTime() ) : null;
		this.uuid = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("uuid") != null ? rs.getString("uuid").trim() : null);
		this.consistent = rs.getInt("consistent");
		this.lastSyncDate =  rs.getTimestamp("last_sync_date") != null ? new java.util.Date( rs.getTimestamp("last_sync_date").getTime() ) : null;
		this.originRecordId = rs.getInt("origin_record_id");
		this.originAppLocationCode = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("origin_app_location_code") != null ? rs.getString("origin_app_location_code").trim() : null);
	} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "person_name_id"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParams(){ 
 		Object[] params = {this.preferred, this.personId == 0 ? null : this.personId, this.prefix, this.givenName, this.middleName, this.familyNamePrefix, this.familyName, this.familyName2, this.familyNameSuffix, this.degree, this.creator == 0 ? null : this.creator, this.dateCreated, this.voided, this.voidedBy == 0 ? null : this.voidedBy, this.dateVoided, this.voidReason, this.changedBy, this.dateChanged, this.uuid, this.consistent, this.lastSyncDate, this.originRecordId, this.originAppLocationCode};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.preferred, this.personId == 0 ? null : this.personId, this.prefix, this.givenName, this.middleName, this.familyNamePrefix, this.familyName, this.familyName2, this.familyNameSuffix, this.degree, this.creator == 0 ? null : this.creator, this.dateCreated, this.voided, this.voidedBy == 0 ? null : this.voidedBy, this.dateVoided, this.voidReason, this.changedBy, this.dateChanged, this.uuid, this.consistent, this.lastSyncDate, this.originRecordId, this.originAppLocationCode, this.personNameId};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQL(){ 
 		return "INSERT INTO person_name(preferred, person_id, prefix, given_name, middle_name, family_name_prefix, family_name, family_name2, family_name_suffix, degree, creator, date_created, voided, voided_by, date_voided, void_reason, changed_by, date_changed, uuid, consistent, last_sync_date, origin_record_id, origin_app_location_code) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE person_name SET preferred = ?, person_id = ?, prefix = ?, given_name = ?, middle_name = ?, family_name_prefix = ?, family_name = ?, family_name2 = ?, family_name_suffix = ?, degree = ?, creator = ?, date_created = ?, voided = ?, voided_by = ?, date_voided = ?, void_reason = ?, changed_by = ?, date_changed = ?, uuid = ?, consistent = ?, last_sync_date = ?, origin_record_id = ?, origin_app_location_code = ? WHERE person_name_id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.preferred) + "," + (this.personId == 0 ? null : this.personId) + "," + (this.prefix != null ? "\""+ utilities.scapeQuotationMarks(prefix)  +"\"" : null) + "," + (this.givenName != null ? "\""+ utilities.scapeQuotationMarks(givenName)  +"\"" : null) + "," + (this.middleName != null ? "\""+ utilities.scapeQuotationMarks(middleName)  +"\"" : null) + "," + (this.familyNamePrefix != null ? "\""+ utilities.scapeQuotationMarks(familyNamePrefix)  +"\"" : null) + "," + (this.familyName != null ? "\""+ utilities.scapeQuotationMarks(familyName)  +"\"" : null) + "," + (this.familyName2 != null ? "\""+ utilities.scapeQuotationMarks(familyName2)  +"\"" : null) + "," + (this.familyNameSuffix != null ? "\""+ utilities.scapeQuotationMarks(familyNameSuffix)  +"\"" : null) + "," + (this.degree != null ? "\""+ utilities.scapeQuotationMarks(degree)  +"\"" : null) + "," + (this.creator == 0 ? null : this.creator) + "," + (this.dateCreated != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateCreated)  +"\"" : null) + "," + (this.voided) + "," + (this.voidedBy == 0 ? null : this.voidedBy) + "," + (this.dateVoided != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateVoided)  +"\"" : null) + "," + (this.voidReason != null ? "\""+ utilities.scapeQuotationMarks(voidReason)  +"\"" : null) + "," + (this.changedBy) + "," + (this.dateChanged != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateChanged)  +"\"" : null) + "," + (this.uuid != null ? "\""+ utilities.scapeQuotationMarks(uuid)  +"\"" : null) + "," + (this.consistent) + "," + (this.lastSyncDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(lastSyncDate)  +"\"" : null) + "," + (this.originRecordId) + "," + (this.originAppLocationCode != null ? "\""+ utilities.scapeQuotationMarks(originAppLocationCode)  +"\"" : null); 
	} 
 
	@Override
	public boolean isGeneratedFromSkeletonClass() {
		return false;
	}

	@Override
	public boolean hasParents() {
		if (this.personId != 0) return true;
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
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.destinationpkg.PersonVO.class, this.personId, false, conn); 
		this.personId = 0;
		if (parentOnDestination  != null) this.personId = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.destinationpkg.UsersVO.class, this.creator, false, conn); 
		this.creator = 0;
		if (parentOnDestination  != null) this.creator = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.destinationpkg.UsersVO.class, this.voidedBy, true, conn); 
		this.voidedBy = 0;
		if (parentOnDestination  != null) this.voidedBy = parentOnDestination.getObjectId();
 
	}

	@Override
	public int getParentValue(String parentAttName) {		
		if (parentAttName.equals("personId")) return this.personId;		
		if (parentAttName.equals("creator")) return this.creator;		
		if (parentAttName.equals("voidedBy")) return this.voidedBy;

		throw new RuntimeException("No found parent for: " + parentAttName);	}


}