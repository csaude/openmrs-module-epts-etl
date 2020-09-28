package org.openmrs.module.eptssync.model.openmrs.sourcepkg; 
 
import org.openmrs.module.eptssync.model.openmrs.generic.*; 
 
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.eptssync.utilities.db.conn.DBException; 
import org.openmrs.module.eptssync.exceptions.ParentNotYetMigratedException; 
 
import java.sql.Connection; 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class OrderFrequencyVO extends AbstractOpenMRSObject implements OpenMRSObject { 
	private int orderFrequencyId;
	private int conceptId;
	private double frequencyPerDay;
	private int creator;
	private java.util.Date dateCreated;
	private byte retired;
	private int retiredBy;
	private java.util.Date dateRetired;
	private String retireReason;
	private int changedBy;
	private java.util.Date dateChanged;
	private String uuid;
 
	public OrderFrequencyVO() { 
		this.metadata = false;
	} 
 
	public void setOrderFrequencyId(int orderFrequencyId){ 
	 	this.orderFrequencyId = orderFrequencyId;
	}
 
	public int getOrderFrequencyId(){ 
		return this.orderFrequencyId;
	}
 
	public void setConceptId(int conceptId){ 
	 	this.conceptId = conceptId;
	}
 
	public int getConceptId(){ 
		return this.conceptId;
	}
 
	public void setFrequencyPerDay(double frequencyPerDay){ 
	 	this.frequencyPerDay = frequencyPerDay;
	}
 
	public double getFrequencyPerDay(){ 
		return this.frequencyPerDay;
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
 		return this.orderFrequencyId; 
	} 
 
	public void setObjectId(int selfId){ 
		this.orderFrequencyId = selfId; 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		this.orderFrequencyId = rs.getInt("order_frequency_id");
		this.conceptId = rs.getInt("concept_id");
		this.frequencyPerDay = rs.getDouble("frequency_per_day");
		this.creator = rs.getInt("creator");
		this.dateCreated =  rs.getTimestamp("date_created") != null ? new java.util.Date( rs.getTimestamp("date_created").getTime() ) : null;
		this.retired = rs.getByte("retired");
		this.retiredBy = rs.getInt("retired_by");
		this.dateRetired =  rs.getTimestamp("date_retired") != null ? new java.util.Date( rs.getTimestamp("date_retired").getTime() ) : null;
		this.retireReason = rs.getString("retire_reason") != null ? rs.getString("retire_reason").trim() : null;
		this.changedBy = rs.getInt("changed_by");
		this.dateChanged =  rs.getTimestamp("date_changed") != null ? new java.util.Date( rs.getTimestamp("date_changed").getTime() ) : null;
			} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "order_frequency_id"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParams(){ 
 		Object[] params = {this.conceptId == 0 ? null : this.conceptId, this.frequencyPerDay, this.creator == 0 ? null : this.creator, this.dateCreated, this.retired, this.retiredBy == 0 ? null : this.retiredBy, this.dateRetired, this.retireReason, this.changedBy == 0 ? null : this.changedBy, this.dateChanged, this.uuid};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.conceptId == 0 ? null : this.conceptId, this.frequencyPerDay, this.creator == 0 ? null : this.creator, this.dateCreated, this.retired, this.retiredBy == 0 ? null : this.retiredBy, this.dateRetired, this.retireReason, this.changedBy == 0 ? null : this.changedBy, this.dateChanged, this.uuid, this.orderFrequencyId};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQL(){ 
 		return "INSERT INTO order_frequency(concept_id, frequency_per_day, creator, date_created, retired, retired_by, date_retired, retire_reason, changed_by, date_changed, uuid) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE order_frequency SET concept_id = ?, frequency_per_day = ?, creator = ?, date_created = ?, retired = ?, retired_by = ?, date_retired = ?, retire_reason = ?, changed_by = ?, date_changed = ?, uuid = ? WHERE order_frequency_id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.conceptId == 0 ? null : this.conceptId) + "," + (this.frequencyPerDay) + "," + (this.creator == 0 ? null : this.creator) + "," + (this.dateCreated != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateCreated)  +"\"" : null) + "," + (this.retired) + "," + (this.retiredBy == 0 ? null : this.retiredBy) + "," + (this.dateRetired != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateRetired)  +"\"" : null) + "," + (this.retireReason != null ? "\""+retireReason+"\"" : null) + "," + (this.changedBy == 0 ? null : this.changedBy) + "," + (this.dateChanged != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateChanged)  +"\"" : null) + "," + (this.uuid != null ? "\""+uuid+"\"" : null); 
	} 
 
	@Override
	public boolean isGeneratedFromSkeletonClass() {
		return false;
	}

	@Override
	public boolean hasParents() {
		if (this.changedBy != 0) return true;
		if (this.conceptId != 0) return true;
		if (this.creator != 0) return true;
		if (this.retiredBy != 0) return true;
		return false;
	}

	@Override
	public int retrieveSharedPKKey(Connection conn) throws ParentNotYetMigratedException, DBException {
		throw new RuntimeException("No PKSharedInfo defined!");	}

	@Override
	public void loadDestParentInfo(Connection conn) throws ParentNotYetMigratedException, DBException {
		OpenMRSObject parentOnDestination = null;
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.sourcepkg.UsersVO.class, this.changedBy, true, conn); 
		this.changedBy = 0;
		if (parentOnDestination  != null) this.changedBy = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.sourcepkg.ConceptVO.class, this.conceptId, false, conn); 
		this.conceptId = 0;
		if (parentOnDestination  != null) this.conceptId = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.sourcepkg.UsersVO.class, this.creator, false, conn); 
		this.creator = 0;
		if (parentOnDestination  != null) this.creator = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.sourcepkg.UsersVO.class, this.retiredBy, true, conn); 
		this.retiredBy = 0;
		if (parentOnDestination  != null) this.retiredBy = parentOnDestination.getObjectId();
 
	}

	@Override
	public int getParentValue(String parentAttName) {		
		if (parentAttName.equals("changedBy")) return this.changedBy;		
		if (parentAttName.equals("conceptId")) return this.conceptId;		
		if (parentAttName.equals("creator")) return this.creator;		
		if (parentAttName.equals("retiredBy")) return this.retiredBy;

		throw new RuntimeException("No found parent for: " + parentAttName);	}


}