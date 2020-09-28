package org.openmrs.module.eptssync.model.openmrs.destinationpkg; 
 
import org.openmrs.module.eptssync.model.openmrs.generic.*; 
 
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.eptssync.utilities.db.conn.DBException; 
import org.openmrs.module.eptssync.exceptions.ParentNotYetMigratedException; 
 
import java.sql.Connection; 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class PrivilegeVO extends AbstractOpenMRSObject implements OpenMRSObject { 
	private String privilege;
	private String description;
	private String uuid;
 
	public PrivilegeVO() { 
		this.metadata = false;
	} 
 
	public void setPrivilege(String privilege){ 
	 	this.privilege = privilege;
	}
 
	public String getPrivilege(){ 
		return this.privilege;
	}
 
	public void setDescription(String description){ 
	 	this.description = description;
	}
 
	public String getDescription(){ 
		return this.description;
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
 		return 0; 
	} 
 
	public void setObjectId(int selfId){ 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		this.privilege = rs.getString("privilege") != null ? rs.getString("privilege").trim() : null;
		this.description = rs.getString("description") != null ? rs.getString("description").trim() : null;
			} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "privilege"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParams(){ 
 		Object[] params = {this.description, this.uuid};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.description, this.uuid, this.privilege};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQL(){ 
 		return "INSERT INTO privilege(description, uuid) VALUES(?, ?);"; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE privilege SET description = ?, uuid = ? WHERE privilege = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.description != null ? "\""+description+"\"" : null) + "," + (this.uuid != null ? "\""+uuid+"\"" : null); 
	} 
 
	@Override
	public boolean isGeneratedFromSkeletonClass() {
		return false;
	}

	@Override
	public boolean hasParents() {
		return false;
	}

	@Override
	public int retrieveSharedPKKey(Connection conn) throws ParentNotYetMigratedException, DBException {
		throw new RuntimeException("No PKSharedInfo defined!");	}

	@Override
	public void loadDestParentInfo(Connection conn) throws ParentNotYetMigratedException, DBException {
		OpenMRSObject parentOnDestination = null;
 
	}

	@Override
	public int getParentValue(String parentAttName) {

		throw new RuntimeException("No found parent for: " + parentAttName);	}


}