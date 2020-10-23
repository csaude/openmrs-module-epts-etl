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
 
public class ProvidermanagementProviderRoleRelationshipTypeVO extends AbstractOpenMRSObject implements OpenMRSObject { 
	private int providerRoleId;
	private int relationshipTypeId;
 
	public ProvidermanagementProviderRoleRelationshipTypeVO() { 
		this.metadata = false;
	} 
 
	public void setProviderRoleId(int providerRoleId){ 
	 	this.providerRoleId = providerRoleId;
	}
 
	public int getProviderRoleId(){ 
		return this.providerRoleId;
	}
 
	public void setRelationshipTypeId(int relationshipTypeId){ 
	 	this.relationshipTypeId = relationshipTypeId;
	}


 
	public int getRelationshipTypeId(){ 
		return this.relationshipTypeId;
	}	public String getUuid(){ 
		return null;
	}
 
	public void setUuid(String uuid){ }
 
	public int getOriginRecordId(){ 
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
 		return 0; 
	} 
 
	public void setObjectId(int selfId){ 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		this.providerRoleId = rs.getInt("provider_role_id");
		this.relationshipTypeId = rs.getInt("relationship_type_id");
	} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "null"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParams(){ 
 		Object[] params = {this.providerRoleId == 0 ? null : this.providerRoleId, this.relationshipTypeId == 0 ? null : this.relationshipTypeId};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.providerRoleId == 0 ? null : this.providerRoleId, this.relationshipTypeId == 0 ? null : this.relationshipTypeId, null};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQL(){ 
 		return "INSERT INTO providermanagement_provider_role_relationship_type(provider_role_id, relationship_type_id) VALUES(?, ?);"; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE providermanagement_provider_role_relationship_type SET provider_role_id = ?, relationship_type_id = ? WHERE null = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.providerRoleId == 0 ? null : this.providerRoleId) + "," + (this.relationshipTypeId == 0 ? null : this.relationshipTypeId); 
	} 
 
	@Override
	public boolean isGeneratedFromSkeletonClass() {
		return false;
	}

	@Override
	public boolean hasParents() {
		if (this.providerRoleId != 0) return true;
		if (this.relationshipTypeId != 0) return true;
		return false;
	}

	@Override
	public int retrieveSharedPKKey(Connection conn) throws ParentNotYetMigratedException, DBException {
		throw new RuntimeException("No PKSharedInfo defined!");	}

	@Override
	public void loadDestParentInfo(Connection conn) throws ParentNotYetMigratedException, DBException {
		OpenMRSObject parentOnDestination = null;
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.destinationpkg.ProvidermanagementProviderRoleVO.class, this.providerRoleId, false, conn); 
		this.providerRoleId = 0;
		if (parentOnDestination  != null) this.providerRoleId = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.destinationpkg.RelationshipTypeVO.class, this.relationshipTypeId, false, conn); 
		this.relationshipTypeId = 0;
		if (parentOnDestination  != null) this.relationshipTypeId = parentOnDestination.getObjectId();
 
	}

	@Override
	public int getParentValue(String parentAttName) {		
		if (parentAttName.equals("providerRoleId")) return this.providerRoleId;		
		if (parentAttName.equals("relationshipTypeId")) return this.relationshipTypeId;

		throw new RuntimeException("No found parent for: " + parentAttName);	}


}