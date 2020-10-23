package org.openmrs.module.eptssync.model.openmrs.destinationpkg; 
 
import org.openmrs.module.eptssync.model.openmrs.generic.*; 
 
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.eptssync.utilities.db.conn.DBException; 
import org.openmrs.module.eptssync.utilities.AttDefinedElements; 
import org.openmrs.module.eptssync.exceptions.ParentNotYetMigratedException; 
 
import java.sql.Connection; 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import java.io.File; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class ObsRelationshipVO extends AbstractOpenMRSObject implements OpenMRSObject { 
	private int obsRelationshipId;
	private int obsRelationshipTypeId;
	private int sourceObsId;
	private int targetObsId;
	private String uuid;
	private java.util.Date dateCreated;
	private int creator;
 
	public ObsRelationshipVO() { 
		this.metadata = false;
	} 
 
	public void setObsRelationshipId(int obsRelationshipId){ 
	 	this.obsRelationshipId = obsRelationshipId;
	}
 
	public int getObsRelationshipId(){ 
		return this.obsRelationshipId;
	}
 
	public void setObsRelationshipTypeId(int obsRelationshipTypeId){ 
	 	this.obsRelationshipTypeId = obsRelationshipTypeId;
	}
 
	public int getObsRelationshipTypeId(){ 
		return this.obsRelationshipTypeId;
	}
 
	public void setSourceObsId(int sourceObsId){ 
	 	this.sourceObsId = sourceObsId;
	}
 
	public int getSourceObsId(){ 
		return this.sourceObsId;
	}
 
	public void setTargetObsId(int targetObsId){ 
	 	this.targetObsId = targetObsId;
	}
 
	public int getTargetObsId(){ 
		return this.targetObsId;
	}
 
	public void setUuid(String uuid){ 
	 	this.uuid = uuid;
	}
 
	public String getUuid(){ 
		return this.uuid;
	}
 
	public void setDateCreated(java.util.Date dateCreated){ 
	 	this.dateCreated = dateCreated;
	}
 
	public java.util.Date getDateCreated(){ 
		return this.dateCreated;
	}
 
	public void setCreator(int creator){ 
	 	this.creator = creator;
	}


 
	public int getCreator(){ 
		return this.creator;
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
 		return this.obsRelationshipId; 
	} 
 
	public void setObjectId(int selfId){ 
		this.obsRelationshipId = selfId; 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		this.obsRelationshipId = rs.getInt("obs_relationship_id");
		this.obsRelationshipTypeId = rs.getInt("obs_relationship_type_id");
		this.sourceObsId = rs.getInt("source_obs_id");
		this.targetObsId = rs.getInt("target_obs_id");
		this.uuid = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("uuid") != null ? rs.getString("uuid").trim() : null);
		this.dateCreated =  rs.getTimestamp("date_created") != null ? new java.util.Date( rs.getTimestamp("date_created").getTime() ) : null;
		this.creator = rs.getInt("creator");
	} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "obs_relationship_id"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParams(){ 
 		Object[] params = {this.obsRelationshipTypeId == 0 ? null : this.obsRelationshipTypeId, this.sourceObsId == 0 ? null : this.sourceObsId, this.targetObsId == 0 ? null : this.targetObsId, this.uuid, this.dateCreated, this.creator == 0 ? null : this.creator};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.obsRelationshipTypeId == 0 ? null : this.obsRelationshipTypeId, this.sourceObsId == 0 ? null : this.sourceObsId, this.targetObsId == 0 ? null : this.targetObsId, this.uuid, this.dateCreated, this.creator == 0 ? null : this.creator, this.obsRelationshipId};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQL(){ 
 		return "INSERT INTO obs_relationship(obs_relationship_type_id, source_obs_id, target_obs_id, uuid, date_created, creator) VALUES(?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE obs_relationship SET obs_relationship_type_id = ?, source_obs_id = ?, target_obs_id = ?, uuid = ?, date_created = ?, creator = ? WHERE obs_relationship_id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.obsRelationshipTypeId == 0 ? null : this.obsRelationshipTypeId) + "," + (this.sourceObsId == 0 ? null : this.sourceObsId) + "," + (this.targetObsId == 0 ? null : this.targetObsId) + "," + (this.uuid != null ? "\""+ utilities.scapeQuotationMarks(uuid)  +"\"" : null) + "," + (this.dateCreated != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateCreated)  +"\"" : null) + "," + (this.creator == 0 ? null : this.creator); 
	} 
 
	@Override
	public boolean isGeneratedFromSkeletonClass() {
		return false;
	}

	@Override
	public boolean hasParents() {
		if (this.obsRelationshipTypeId != 0) return true;
		if (this.sourceObsId != 0) return true;
		if (this.targetObsId != 0) return true;
		if (this.creator != 0) return true;
		return false;
	}

	@Override
	public int retrieveSharedPKKey(Connection conn) throws ParentNotYetMigratedException, DBException {
		throw new RuntimeException("No PKSharedInfo defined!");	}

	@Override
	public void loadDestParentInfo(Connection conn) throws ParentNotYetMigratedException, DBException {
		OpenMRSObject parentOnDestination = null;
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.destinationpkg.ObsRelationshipTypeVO.class, this.obsRelationshipTypeId, false, conn); 
		this.obsRelationshipTypeId = 0;
		if (parentOnDestination  != null) this.obsRelationshipTypeId = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.destinationpkg.ObsVO.class, this.sourceObsId, false, conn); 
		this.sourceObsId = 0;
		if (parentOnDestination  != null) this.sourceObsId = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.destinationpkg.ObsVO.class, this.targetObsId, false, conn); 
		this.targetObsId = 0;
		if (parentOnDestination  != null) this.targetObsId = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.destinationpkg.PersonVO.class, this.creator, false, conn); 
		this.creator = 0;
		if (parentOnDestination  != null) this.creator = parentOnDestination.getObjectId();
 
	}

	@Override
	public int getParentValue(String parentAttName) {		
		if (parentAttName.equals("obsRelationshipTypeId")) return this.obsRelationshipTypeId;		
		if (parentAttName.equals("sourceObsId")) return this.sourceObsId;		
		if (parentAttName.equals("targetObsId")) return this.targetObsId;		
		if (parentAttName.equals("creator")) return this.creator;

		throw new RuntimeException("No found parent for: " + parentAttName);	}


}