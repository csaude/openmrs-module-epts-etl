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
 
public class UserPropertyVO extends AbstractOpenMRSObject implements OpenMRSObject { 
	private int userId;
	private String property;
	private String propertyValue;
 
	public UserPropertyVO() { 
		this.metadata = false;
	} 
 
	public void setUserId(int userId){ 
	 	this.userId = userId;
	}
 
	public int getUserId(){ 
		return this.userId;
	}
 
	public void setProperty(String property){ 
	 	this.property = property;
	}
 
	public String getProperty(){ 
		return this.property;
	}
 
	public void setPropertyValue(String propertyValue){ 
	 	this.propertyValue = propertyValue;
	}


 
	public String getPropertyValue(){ 
		return this.propertyValue;
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
		this.property = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("property") != null ? rs.getString("property").trim() : null);
		this.propertyValue = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("property_value") != null ? rs.getString("property_value").trim() : null);
	} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "property"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParams(){ 
 		Object[] params = {this.userId == 0 ? null : this.userId, this.propertyValue};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.userId == 0 ? null : this.userId, this.propertyValue, this.property};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQL(){ 
 		return "INSERT INTO user_property(user_id, property_value) VALUES(?, ?);"; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE user_property SET user_id = ?, property_value = ? WHERE property = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.userId == 0 ? null : this.userId) + "," + (this.propertyValue != null ? "\""+ utilities.scapeQuotationMarks(propertyValue)  +"\"" : null); 
	} 
 
	@Override
	public boolean isGeneratedFromSkeletonClass() {
		return false;
	}

	@Override
	public boolean hasParents() {
		if (this.userId != 0) return true;
		return false;
	}

	@Override
	public int retrieveSharedPKKey(Connection conn) throws ParentNotYetMigratedException, DBException {
		throw new RuntimeException("No PKSharedInfo defined!");	}

	@Override
	public void loadDestParentInfo(Connection conn) throws ParentNotYetMigratedException, DBException {
		OpenMRSObject parentOnDestination = null;
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.destinationpkg.UsersVO.class, this.userId, false, conn); 
		this.userId = 0;
		if (parentOnDestination  != null) this.userId = parentOnDestination.getObjectId();
 
	}

	@Override
	public int getParentValue(String parentAttName) {		
		if (parentAttName.equals("userId")) return this.userId;

		throw new RuntimeException("No found parent for: " + parentAttName);	}


}