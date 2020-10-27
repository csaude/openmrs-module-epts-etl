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
 
public class AddressHierarchyEntryVO extends AbstractOpenMRSObject implements OpenMRSObject { 
	private int addressHierarchyEntryId;
	private String name;
	private int levelId;
	private int parentId;
	private String userGeneratedId;
	private double latitude;
	private double longitude;
	private double elevation;
	private String uuid;
 
	public AddressHierarchyEntryVO() { 
		this.metadata = false;
	} 
 
	public void setAddressHierarchyEntryId(int addressHierarchyEntryId){ 
	 	this.addressHierarchyEntryId = addressHierarchyEntryId;
	}
 
	public int getAddressHierarchyEntryId(){ 
		return this.addressHierarchyEntryId;
	}
 
	public void setName(String name){ 
	 	this.name = name;
	}
 
	public String getName(){ 
		return this.name;
	}
 
	public void setLevelId(int levelId){ 
	 	this.levelId = levelId;
	}
 
	public int getLevelId(){ 
		return this.levelId;
	}
 
	public void setParentId(int parentId){ 
	 	this.parentId = parentId;
	}
 
	public int getParentId(){ 
		return this.parentId;
	}
 
	public void setUserGeneratedId(String userGeneratedId){ 
	 	this.userGeneratedId = userGeneratedId;
	}
 
	public String getUserGeneratedId(){ 
		return this.userGeneratedId;
	}
 
	public void setLatitude(double latitude){ 
	 	this.latitude = latitude;
	}
 
	public double getLatitude(){ 
		return this.latitude;
	}
 
	public void setLongitude(double longitude){ 
	 	this.longitude = longitude;
	}
 
	public double getLongitude(){ 
		return this.longitude;
	}
 
	public void setElevation(double elevation){ 
	 	this.elevation = elevation;
	}
 
	public double getElevation(){ 
		return this.elevation;
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
 		return this.addressHierarchyEntryId; 
	} 
 
	public void setObjectId(int selfId){ 
		this.addressHierarchyEntryId = selfId; 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		this.addressHierarchyEntryId = rs.getInt("address_hierarchy_entry_id");
		this.name = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("name") != null ? rs.getString("name").trim() : null);
		this.levelId = rs.getInt("level_id");
		this.parentId = rs.getInt("parent_id");
		this.userGeneratedId = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("user_generated_id") != null ? rs.getString("user_generated_id").trim() : null);
		this.latitude = rs.getDouble("latitude");
		this.longitude = rs.getDouble("longitude");
		this.elevation = rs.getDouble("elevation");
		this.uuid = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("uuid") != null ? rs.getString("uuid").trim() : null);
	} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "address_hierarchy_entry_id"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParams(){ 
 		Object[] params = {this.name, this.levelId == 0 ? null : this.levelId, this.parentId == 0 ? null : this.parentId, this.userGeneratedId, this.latitude, this.longitude, this.elevation, this.uuid};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.name, this.levelId == 0 ? null : this.levelId, this.parentId == 0 ? null : this.parentId, this.userGeneratedId, this.latitude, this.longitude, this.elevation, this.uuid, this.addressHierarchyEntryId};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQL(){ 
 		return "INSERT INTO address_hierarchy_entry(name, level_id, parent_id, user_generated_id, latitude, longitude, elevation, uuid) VALUES(?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE address_hierarchy_entry SET name = ?, level_id = ?, parent_id = ?, user_generated_id = ?, latitude = ?, longitude = ?, elevation = ?, uuid = ? WHERE address_hierarchy_entry_id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.name != null ? "\""+ utilities.scapeQuotationMarks(name)  +"\"" : null) + "," + (this.levelId == 0 ? null : this.levelId) + "," + (this.parentId == 0 ? null : this.parentId) + "," + (this.userGeneratedId != null ? "\""+ utilities.scapeQuotationMarks(userGeneratedId)  +"\"" : null) + "," + (this.latitude) + "," + (this.longitude) + "," + (this.elevation) + "," + (this.uuid != null ? "\""+ utilities.scapeQuotationMarks(uuid)  +"\"" : null); 
	} 
 
	@Override
	public boolean isGeneratedFromSkeletonClass() {
		return false;
	}

	@Override
	public boolean hasParents() {
		if (this.levelId != 0) return true;
		if (this.parentId != 0) return true;
		return false;
	}

	@Override
	public int retrieveSharedPKKey(Connection conn) throws ParentNotYetMigratedException, DBException {
		throw new RuntimeException("No PKSharedInfo defined!");	}

	@Override
	public void loadDestParentInfo(Connection conn) throws ParentNotYetMigratedException, DBException {
		OpenMRSObject parentOnDestination = null;
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.sourcepkg.AddressHierarchyLevelVO.class, this.levelId, false, conn); 
		this.levelId = 0;
		if (parentOnDestination  != null) this.levelId = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.sourcepkg.AddressHierarchyEntryVO.class, this.parentId, true, conn); 
		this.parentId = 0;
		if (parentOnDestination  != null) this.parentId = parentOnDestination.getObjectId();
 
	}

	@Override
	public int getParentValue(String parentAttName) {		
		if (parentAttName.equals("levelId")) return this.levelId;		
		if (parentAttName.equals("parentId")) return this.parentId;

		throw new RuntimeException("No found parent for: " + parentAttName);	}


}