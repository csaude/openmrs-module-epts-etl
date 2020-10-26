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
 
public class AddressHierarchyAddressToEntryMapVO extends AbstractOpenMRSObject implements OpenMRSObject { 
	private int addressToEntryMapId;
	private int addressId;
	private int entryId;
	private String uuid;
 
	public AddressHierarchyAddressToEntryMapVO() { 
		this.metadata = false;
	} 
 
	public void setAddressToEntryMapId(int addressToEntryMapId){ 
	 	this.addressToEntryMapId = addressToEntryMapId;
	}
 
	public int getAddressToEntryMapId(){ 
		return this.addressToEntryMapId;
	}
 
	public void setAddressId(int addressId){ 
	 	this.addressId = addressId;
	}
 
	public int getAddressId(){ 
		return this.addressId;
	}
 
	public void setEntryId(int entryId){ 
	 	this.entryId = entryId;
	}
 
	public int getEntryId(){ 
		return this.entryId;
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
 		return this.addressToEntryMapId; 
	} 
 
	public void setObjectId(int selfId){ 
		this.addressToEntryMapId = selfId; 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		this.addressToEntryMapId = rs.getInt("address_to_entry_map_id");
		this.addressId = rs.getInt("address_id");
		this.entryId = rs.getInt("entry_id");
		this.uuid = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("uuid") != null ? rs.getString("uuid").trim() : null);
	} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "address_to_entry_map_id"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParams(){ 
 		Object[] params = {this.addressId == 0 ? null : this.addressId, this.entryId == 0 ? null : this.entryId, this.uuid};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.addressId == 0 ? null : this.addressId, this.entryId == 0 ? null : this.entryId, this.uuid, this.addressToEntryMapId};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQL(){ 
 		return "INSERT INTO address_hierarchy_address_to_entry_map(address_id, entry_id, uuid) VALUES(?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE address_hierarchy_address_to_entry_map SET address_id = ?, entry_id = ?, uuid = ? WHERE address_to_entry_map_id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.addressId == 0 ? null : this.addressId) + "," + (this.entryId == 0 ? null : this.entryId) + "," + (this.uuid != null ? "\""+ utilities.scapeQuotationMarks(uuid)  +"\"" : null); 
	} 
 
	@Override
	public boolean isGeneratedFromSkeletonClass() {
		return false;
	}

	@Override
	public boolean hasParents() {
		if (this.addressId != 0) return true;
		if (this.entryId != 0) return true;
		return false;
	}

	@Override
	public int retrieveSharedPKKey(Connection conn) throws ParentNotYetMigratedException, DBException {
		throw new RuntimeException("No PKSharedInfo defined!");	}

	@Override
	public void loadDestParentInfo(Connection conn) throws ParentNotYetMigratedException, DBException {
		OpenMRSObject parentOnDestination = null;
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.destinationpkg.PersonAddressVO.class, this.addressId, false, conn); 
		this.addressId = 0;
		if (parentOnDestination  != null) this.addressId = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.destinationpkg.AddressHierarchyEntryVO.class, this.entryId, false, conn); 
		this.entryId = 0;
		if (parentOnDestination  != null) this.entryId = parentOnDestination.getObjectId();
 
	}

	@Override
	public int getParentValue(String parentAttName) {		
		if (parentAttName.equals("addressId")) return this.addressId;		
		if (parentAttName.equals("entryId")) return this.entryId;

		throw new RuntimeException("No found parent for: " + parentAttName);	}


}