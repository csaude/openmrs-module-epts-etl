package org.openmrs.module.eptssync.model.openmrs.destinationpkg; 
 
import org.openmrs.module.eptssync.model.openmrs.generic.*; 
 
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.eptssync.utilities.db.conn.DBException; 
import org.openmrs.module.eptssync.exceptions.ParentNotYetMigratedException; 
 
import java.sql.Connection; 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class ProvidermanagementProviderRoleProviderAttributeTypeVO extends AbstractOpenMRSObject implements OpenMRSObject { 
	private int providerRoleId;
	private int providerAttributeTypeId;
 
	public ProvidermanagementProviderRoleProviderAttributeTypeVO() { 
		this.metadata = false;
	} 
 
	public void setProviderRoleId(int providerRoleId){ 
	 	this.providerRoleId = providerRoleId;
	}
 
	public int getProviderRoleId(){ 
		return this.providerRoleId;
	}
 
	public void setProviderAttributeTypeId(int providerAttributeTypeId){ 
	 	this.providerAttributeTypeId = providerAttributeTypeId;
	}


 
	public int getProviderAttributeTypeId(){ 
		return this.providerAttributeTypeId;
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
			} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "null"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParams(){ 
 		Object[] params = {this.providerRoleId == 0 ? null : this.providerRoleId, this.providerAttributeTypeId == 0 ? null : this.providerAttributeTypeId};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.providerRoleId == 0 ? null : this.providerRoleId, this.providerAttributeTypeId == 0 ? null : this.providerAttributeTypeId, null};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQL(){ 
 		return "INSERT INTO providermanagement_provider_role_provider_attribute_type(provider_role_id, provider_attribute_type_id) VALUES(?, ?);"; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE providermanagement_provider_role_provider_attribute_type SET provider_role_id = ?, provider_attribute_type_id = ? WHERE null = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.providerRoleId == 0 ? null : this.providerRoleId) + "," + (this.providerAttributeTypeId == 0 ? null : this.providerAttributeTypeId); 
	} 
 
	@Override
	public boolean isGeneratedFromSkeletonClass() {
		return false;
	}

	@Override
	public boolean hasParents() {
		if (this.providerAttributeTypeId != 0) return true;
		if (this.providerRoleId != 0) return true;
		return false;
	}

	@Override
	public int retrieveSharedPKKey(Connection conn) throws ParentNotYetMigratedException, DBException {
		throw new RuntimeException("No PKSharedInfo defined!");	}

	@Override
	public void loadDestParentInfo(Connection conn) throws ParentNotYetMigratedException, DBException {
		OpenMRSObject parentOnDestination = null;
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.destinationpkg.ProviderAttributeTypeVO.class, this.providerAttributeTypeId, false, conn); 
		this.providerAttributeTypeId = 0;
		if (parentOnDestination  != null) this.providerAttributeTypeId = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.destinationpkg.ProvidermanagementProviderRoleVO.class, this.providerRoleId, false, conn); 
		this.providerRoleId = 0;
		if (parentOnDestination  != null) this.providerRoleId = parentOnDestination.getObjectId();
 
	}

	@Override
	public int getParentValue(String parentAttName) {		
		if (parentAttName.equals("providerAttributeTypeId")) return this.providerAttributeTypeId;		
		if (parentAttName.equals("providerRoleId")) return this.providerRoleId;

		throw new RuntimeException("No found parent for: " + parentAttName);	}


}