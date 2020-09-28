package org.openmrs.module.eptssync.model.openmrs.sourcepkg; 
 
import org.openmrs.module.eptssync.model.openmrs.generic.*; 
 
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.eptssync.utilities.db.conn.DBException; 
import org.openmrs.module.eptssync.exceptions.ParentNotYetMigratedException; 
 
import java.sql.Connection; 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class ProviderAttributeVO extends AbstractOpenMRSObject implements OpenMRSObject { 
	private int providerAttributeId;
	private int providerId;
	private int attributeTypeId;
	private String valueReference;
	private String uuid;
	private int creator;
	private java.util.Date dateCreated;
	private int changedBy;
	private java.util.Date dateChanged;
	private byte voided;
	private int voidedBy;
	private java.util.Date dateVoided;
	private String voidReason;
 
	public ProviderAttributeVO() { 
		this.metadata = false;
	} 
 
	public void setProviderAttributeId(int providerAttributeId){ 
	 	this.providerAttributeId = providerAttributeId;
	}
 
	public int getProviderAttributeId(){ 
		return this.providerAttributeId;
	}
 
	public void setProviderId(int providerId){ 
	 	this.providerId = providerId;
	}
 
	public int getProviderId(){ 
		return this.providerId;
	}
 
	public void setAttributeTypeId(int attributeTypeId){ 
	 	this.attributeTypeId = attributeTypeId;
	}
 
	public int getAttributeTypeId(){ 
		return this.attributeTypeId;
	}
 
	public void setValueReference(String valueReference){ 
	 	this.valueReference = valueReference;
	}
 
	public String getValueReference(){ 
		return this.valueReference;
	}
 
	public void setUuid(String uuid){ 
	 	this.uuid = uuid;
	}
 
	public String getUuid(){ 
		return this.uuid;
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
 		return this.providerAttributeId; 
	} 
 
	public void setObjectId(int selfId){ 
		this.providerAttributeId = selfId; 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		this.providerAttributeId = rs.getInt("provider_attribute_id");
		this.providerId = rs.getInt("provider_id");
		this.attributeTypeId = rs.getInt("attribute_type_id");
		this.valueReference = rs.getString("value_reference") != null ? rs.getString("value_reference").trim() : null;
		this.uuid = rs.getString("uuid") != null ? rs.getString("uuid").trim() : null;
		this.creator = rs.getInt("creator");
		this.dateCreated =  rs.getTimestamp("date_created") != null ? new java.util.Date( rs.getTimestamp("date_created").getTime() ) : null;
		this.changedBy = rs.getInt("changed_by");
		this.dateChanged =  rs.getTimestamp("date_changed") != null ? new java.util.Date( rs.getTimestamp("date_changed").getTime() ) : null;
		this.voided = rs.getByte("voided");
		this.voidedBy = rs.getInt("voided_by");
		this.dateVoided =  rs.getTimestamp("date_voided") != null ? new java.util.Date( rs.getTimestamp("date_voided").getTime() ) : null;
			} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "provider_attribute_id"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParams(){ 
 		Object[] params = {this.providerId == 0 ? null : this.providerId, this.attributeTypeId == 0 ? null : this.attributeTypeId, this.valueReference, this.uuid, this.creator == 0 ? null : this.creator, this.dateCreated, this.changedBy == 0 ? null : this.changedBy, this.dateChanged, this.voided, this.voidedBy == 0 ? null : this.voidedBy, this.dateVoided, this.voidReason};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.providerId == 0 ? null : this.providerId, this.attributeTypeId == 0 ? null : this.attributeTypeId, this.valueReference, this.uuid, this.creator == 0 ? null : this.creator, this.dateCreated, this.changedBy == 0 ? null : this.changedBy, this.dateChanged, this.voided, this.voidedBy == 0 ? null : this.voidedBy, this.dateVoided, this.voidReason, this.providerAttributeId};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQL(){ 
 		return "INSERT INTO provider_attribute(provider_id, attribute_type_id, value_reference, uuid, creator, date_created, changed_by, date_changed, voided, voided_by, date_voided, void_reason) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE provider_attribute SET provider_id = ?, attribute_type_id = ?, value_reference = ?, uuid = ?, creator = ?, date_created = ?, changed_by = ?, date_changed = ?, voided = ?, voided_by = ?, date_voided = ?, void_reason = ? WHERE provider_attribute_id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.providerId == 0 ? null : this.providerId) + "," + (this.attributeTypeId == 0 ? null : this.attributeTypeId) + "," + (this.valueReference != null ? "\""+valueReference+"\"" : null) + "," + (this.uuid != null ? "\""+uuid+"\"" : null) + "," + (this.creator == 0 ? null : this.creator) + "," + (this.dateCreated != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateCreated)  +"\"" : null) + "," + (this.changedBy == 0 ? null : this.changedBy) + "," + (this.dateChanged != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateChanged)  +"\"" : null) + "," + (this.voided) + "," + (this.voidedBy == 0 ? null : this.voidedBy) + "," + (this.dateVoided != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateVoided)  +"\"" : null) + "," + (this.voidReason != null ? "\""+voidReason+"\"" : null); 
	} 
 
	@Override
	public boolean isGeneratedFromSkeletonClass() {
		return false;
	}

	@Override
	public boolean hasParents() {
		if (this.attributeTypeId != 0) return true;
		if (this.changedBy != 0) return true;
		if (this.creator != 0) return true;
		if (this.providerId != 0) return true;
		if (this.voidedBy != 0) return true;
		return false;
	}

	@Override
	public int retrieveSharedPKKey(Connection conn) throws ParentNotYetMigratedException, DBException {
		throw new RuntimeException("No PKSharedInfo defined!");	}

	@Override
	public void loadDestParentInfo(Connection conn) throws ParentNotYetMigratedException, DBException {
		OpenMRSObject parentOnDestination = null;
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.sourcepkg.ProviderAttributeTypeVO.class, this.attributeTypeId, false, conn); 
		this.attributeTypeId = 0;
		if (parentOnDestination  != null) this.attributeTypeId = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.sourcepkg.UsersVO.class, this.changedBy, true, conn); 
		this.changedBy = 0;
		if (parentOnDestination  != null) this.changedBy = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.sourcepkg.UsersVO.class, this.creator, false, conn); 
		this.creator = 0;
		if (parentOnDestination  != null) this.creator = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.sourcepkg.ProviderVO.class, this.providerId, false, conn); 
		this.providerId = 0;
		if (parentOnDestination  != null) this.providerId = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.sourcepkg.UsersVO.class, this.voidedBy, true, conn); 
		this.voidedBy = 0;
		if (parentOnDestination  != null) this.voidedBy = parentOnDestination.getObjectId();
 
	}

	@Override
	public int getParentValue(String parentAttName) {		
		if (parentAttName.equals("attributeTypeId")) return this.attributeTypeId;		
		if (parentAttName.equals("changedBy")) return this.changedBy;		
		if (parentAttName.equals("creator")) return this.creator;		
		if (parentAttName.equals("providerId")) return this.providerId;		
		if (parentAttName.equals("voidedBy")) return this.voidedBy;

		throw new RuntimeException("No found parent for: " + parentAttName);	}


}