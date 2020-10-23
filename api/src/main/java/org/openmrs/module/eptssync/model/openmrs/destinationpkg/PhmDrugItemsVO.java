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
 
public class PhmDrugItemsVO extends AbstractOpenMRSObject implements OpenMRSObject { 
	private int drugId;
	private String fnmCode;
	private int pharmaceuticalFormId;
	private int therapeuticGroupId;
	private int therapeuticClassId;
	private String uuid;
	private int creator;
	private java.util.Date dateCreated;
	private int changedBy;
	private java.util.Date dateChanged;
	private byte retired;
	private int retiredBy;
	private java.util.Date dateRetired;
	private String retireReason;
 
	public PhmDrugItemsVO() { 
		this.metadata = false;
	} 
 
	public void setDrugId(int drugId){ 
	 	this.drugId = drugId;
	}
 
	public int getDrugId(){ 
		return this.drugId;
	}
 
	public void setFnmCode(String fnmCode){ 
	 	this.fnmCode = fnmCode;
	}
 
	public String getFnmCode(){ 
		return this.fnmCode;
	}
 
	public void setPharmaceuticalFormId(int pharmaceuticalFormId){ 
	 	this.pharmaceuticalFormId = pharmaceuticalFormId;
	}
 
	public int getPharmaceuticalFormId(){ 
		return this.pharmaceuticalFormId;
	}
 
	public void setTherapeuticGroupId(int therapeuticGroupId){ 
	 	this.therapeuticGroupId = therapeuticGroupId;
	}
 
	public int getTherapeuticGroupId(){ 
		return this.therapeuticGroupId;
	}
 
	public void setTherapeuticClassId(int therapeuticClassId){ 
	 	this.therapeuticClassId = therapeuticClassId;
	}
 
	public int getTherapeuticClassId(){ 
		return this.therapeuticClassId;
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
 		return this.drugId; 
	} 
 
	public void setObjectId(int selfId){ 
		this.drugId = selfId; 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		this.drugId = rs.getInt("drug_id");
		this.fnmCode = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("fnm_code") != null ? rs.getString("fnm_code").trim() : null);
		this.pharmaceuticalFormId = rs.getInt("pharmaceutical_form_id");
		this.therapeuticGroupId = rs.getInt("therapeutic_group_id");
		this.therapeuticClassId = rs.getInt("therapeutic_class_id");
		this.uuid = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("uuid") != null ? rs.getString("uuid").trim() : null);
		this.creator = rs.getInt("creator");
		this.dateCreated =  rs.getTimestamp("date_created") != null ? new java.util.Date( rs.getTimestamp("date_created").getTime() ) : null;
		this.changedBy = rs.getInt("changed_by");
		this.dateChanged =  rs.getTimestamp("date_changed") != null ? new java.util.Date( rs.getTimestamp("date_changed").getTime() ) : null;
		this.retired = rs.getByte("retired");
		this.retiredBy = rs.getInt("retired_by");
		this.dateRetired =  rs.getTimestamp("date_retired") != null ? new java.util.Date( rs.getTimestamp("date_retired").getTime() ) : null;
		this.retireReason = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("retire_reason") != null ? rs.getString("retire_reason").trim() : null);
	} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "drug_id"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParams(){ 
 		Object[] params = {this.fnmCode, this.pharmaceuticalFormId == 0 ? null : this.pharmaceuticalFormId, this.therapeuticGroupId == 0 ? null : this.therapeuticGroupId, this.therapeuticClassId == 0 ? null : this.therapeuticClassId, this.uuid, this.creator == 0 ? null : this.creator, this.dateCreated, this.changedBy == 0 ? null : this.changedBy, this.dateChanged, this.retired, this.retiredBy == 0 ? null : this.retiredBy, this.dateRetired, this.retireReason};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.fnmCode, this.pharmaceuticalFormId == 0 ? null : this.pharmaceuticalFormId, this.therapeuticGroupId == 0 ? null : this.therapeuticGroupId, this.therapeuticClassId == 0 ? null : this.therapeuticClassId, this.uuid, this.creator == 0 ? null : this.creator, this.dateCreated, this.changedBy == 0 ? null : this.changedBy, this.dateChanged, this.retired, this.retiredBy == 0 ? null : this.retiredBy, this.dateRetired, this.retireReason, this.drugId};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQL(){ 
 		return "INSERT INTO phm_drug_items(fnm_code, pharmaceutical_form_id, therapeutic_group_id, therapeutic_class_id, uuid, creator, date_created, changed_by, date_changed, retired, retired_by, date_retired, retire_reason) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE phm_drug_items SET fnm_code = ?, pharmaceutical_form_id = ?, therapeutic_group_id = ?, therapeutic_class_id = ?, uuid = ?, creator = ?, date_created = ?, changed_by = ?, date_changed = ?, retired = ?, retired_by = ?, date_retired = ?, retire_reason = ? WHERE drug_id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.fnmCode != null ? "\""+ utilities.scapeQuotationMarks(fnmCode)  +"\"" : null) + "," + (this.pharmaceuticalFormId == 0 ? null : this.pharmaceuticalFormId) + "," + (this.therapeuticGroupId == 0 ? null : this.therapeuticGroupId) + "," + (this.therapeuticClassId == 0 ? null : this.therapeuticClassId) + "," + (this.uuid != null ? "\""+ utilities.scapeQuotationMarks(uuid)  +"\"" : null) + "," + (this.creator == 0 ? null : this.creator) + "," + (this.dateCreated != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateCreated)  +"\"" : null) + "," + (this.changedBy == 0 ? null : this.changedBy) + "," + (this.dateChanged != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateChanged)  +"\"" : null) + "," + (this.retired) + "," + (this.retiredBy == 0 ? null : this.retiredBy) + "," + (this.dateRetired != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateRetired)  +"\"" : null) + "," + (this.retireReason != null ? "\""+ utilities.scapeQuotationMarks(retireReason)  +"\"" : null); 
	} 
 
	@Override
	public boolean isGeneratedFromSkeletonClass() {
		return false;
	}

	@Override
	public boolean hasParents() {
		if (this.changedBy != 0) return true;
		if (this.creator != 0) return true;
		if (this.drugId != 0) return true;
		if (this.pharmaceuticalFormId != 0) return true;
		if (this.retiredBy != 0) return true;
		if (this.therapeuticClassId != 0) return true;
		if (this.therapeuticGroupId != 0) return true;
		return false;
	}

	@Override
	public int retrieveSharedPKKey(Connection conn) throws ParentNotYetMigratedException, DBException {
		throw new RuntimeException("No PKSharedInfo defined!");	}

	@Override
	public void loadDestParentInfo(Connection conn) throws ParentNotYetMigratedException, DBException {
		OpenMRSObject parentOnDestination = null;
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.destinationpkg.UsersVO.class, this.changedBy, true, conn); 
		this.changedBy = 0;
		if (parentOnDestination  != null) this.changedBy = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.destinationpkg.UsersVO.class, this.creator, false, conn); 
		this.creator = 0;
		if (parentOnDestination  != null) this.creator = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.destinationpkg.DrugVO.class, this.drugId, false, conn); 
		this.drugId = 0;
		if (parentOnDestination  != null) this.drugId = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.destinationpkg.ConceptVO.class, this.pharmaceuticalFormId, false, conn); 
		this.pharmaceuticalFormId = 0;
		if (parentOnDestination  != null) this.pharmaceuticalFormId = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.destinationpkg.UsersVO.class, this.retiredBy, true, conn); 
		this.retiredBy = 0;
		if (parentOnDestination  != null) this.retiredBy = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.destinationpkg.ConceptVO.class, this.therapeuticClassId, false, conn); 
		this.therapeuticClassId = 0;
		if (parentOnDestination  != null) this.therapeuticClassId = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.destinationpkg.ConceptVO.class, this.therapeuticGroupId, false, conn); 
		this.therapeuticGroupId = 0;
		if (parentOnDestination  != null) this.therapeuticGroupId = parentOnDestination.getObjectId();
 
	}

	@Override
	public int getParentValue(String parentAttName) {		
		if (parentAttName.equals("changedBy")) return this.changedBy;		
		if (parentAttName.equals("creator")) return this.creator;		
		if (parentAttName.equals("drugId")) return this.drugId;		
		if (parentAttName.equals("pharmaceuticalFormId")) return this.pharmaceuticalFormId;		
		if (parentAttName.equals("retiredBy")) return this.retiredBy;		
		if (parentAttName.equals("therapeuticClassId")) return this.therapeuticClassId;		
		if (parentAttName.equals("therapeuticGroupId")) return this.therapeuticGroupId;

		throw new RuntimeException("No found parent for: " + parentAttName);	}


}