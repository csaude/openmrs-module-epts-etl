package org.openmrs.module.eptssync.model.openmrs.destinationpkg; 
 
import org.openmrs.module.eptssync.model.openmrs.generic.*; 
 
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.eptssync.utilities.db.conn.DBException; 
import org.openmrs.module.eptssync.exceptions.ParentNotYetMigratedException; 
 
import java.sql.Connection; 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class CohortVO extends AbstractOpenMRSObject implements OpenMRSObject { 
	public CohortVO() { 
	} 
 
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
 
	public int getObjectId(){ 
		return 0;
	}
 
	public void setObjectId(int objectId){ }
 
	public String getUuid(){ 
		return null;
	}
 
	public void setUuid(String uuid){ }
 
	public String generateDBPrimaryKeyAtt(){ 
 		return null; 
	} 
 
	public Object[]  getInsertParams(){ 
 		return null; 
	} 
 
	public Object[]  getUpdateParams(){ 
 		return null; 
	} 
 
	public String getInsertSQL(){ 
 		return null; 
	} 
 
	public String getUpdateSQL(){ 
 		return null; 
	} 
 
	public String generateInsertValues(){ 
 		return null; 
	} 
 
	public boolean hasParents() {
		return false;
	}

	public int retrieveSharedPKKey(Connection conn) throws ParentNotYetMigratedException, DBException {
		throw new RuntimeException("No PKSharedInfo defined!");	}

	@Override
	public void loadDestParentInfo(Connection conn) throws ParentNotYetMigratedException, DBException {
	}

	@Override
	public int getParentValue(String parentAttName) {		return 0;
	}

	@Override
	public boolean isGeneratedFromSkeletonClass() {
		return true;
	}


}