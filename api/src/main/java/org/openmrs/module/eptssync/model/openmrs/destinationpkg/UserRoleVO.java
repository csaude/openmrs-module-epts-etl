package org.openmrs.module.eptssync.model.openmrs.destinationpkg; 
 
import org.openmrs.module.eptssync.model.openmrs.generic.*; 
 
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.eptssync.utilities.db.conn.DBException; 
import org.openmrs.module.eptssync.exceptions.ParentNotYetMigratedException; 
 
import java.sql.Connection; 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class UserRoleVO extends AbstractOpenMRSObject implements OpenMRSObject { 
	private int userId;
	private String role;
 
	public UserRoleVO() { 
		this.metadata = false;
	} 
 
	public void setUserId(int userId){ 
	 	this.userId = userId;
	}
 
	public int getUserId(){ 
		return this.userId;
	}
 
	public void setRole(String role){ 
	 	this.role = role;
	}


 
	public String getRole(){ 
		return this.role;
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
		this.userId = rs.getInt("user_id");
			} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "role"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParams(){ 
 		Object[] params = {this.userId == 0 ? null : this.userId, null};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.userId == 0 ? null : this.userId, null, this.role};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQL(){ 
 		return "INSERT INTO user_role(user_id, null) VALUES(?, null);"; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE user_role SET user_id = ?, null WHERE role = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.userId == 0 ? null : this.userId) + "," + null; 
	} 
 
	@Override
	public boolean isGeneratedFromSkeletonClass() {
		return false;
	}

	@Override
	public boolean hasParents() {
		if (this.role != null) return true;
		if (this.userId != 0) return true;
		return false;
	}

	@Override
	public int retrieveSharedPKKey(Connection conn) throws ParentNotYetMigratedException, DBException {
		throw new RuntimeException("No PKSharedInfo defined!");	}

	@Override
	public void loadDestParentInfo(Connection conn) throws ParentNotYetMigratedException, DBException {
		OpenMRSObject parentOnDestination = null;
 
/*
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.destinationpkg.RoleVO.class, this.role, false, conn); 
		this.role = 0;
		if (parentOnDestination  != null) this.role = parentOnDestination.getObjectId();
 
*/
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.destinationpkg.UsersVO.class, this.userId, false, conn); 
		this.userId = 0;
		if (parentOnDestination  != null) this.userId = parentOnDestination.getObjectId();
 
	}

	@Override
	public int getParentValue(String parentAttName) {		
		if (parentAttName.equals("role")) return 0;		
		if (parentAttName.equals("userId")) return this.userId;

		throw new RuntimeException("No found parent for: " + parentAttName);	}


}