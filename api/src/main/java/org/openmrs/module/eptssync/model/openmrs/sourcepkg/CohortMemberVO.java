package org.openmrs.module.eptssync.model.openmrs.sourcepkg; 
 
import org.openmrs.module.eptssync.model.openmrs.generic.*; 
 
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.eptssync.utilities.db.conn.DBException; 
import org.openmrs.module.eptssync.exceptions.ParentNotYetMigratedException; 
 
import java.sql.Connection; 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class CohortMemberVO extends AbstractOpenMRSObject implements OpenMRSObject { 
	private int cohortId;
	private int patientId;
 
	public CohortMemberVO() { 
		this.metadata = false;
	} 
 
	public void setCohortId(int cohortId){ 
	 	this.cohortId = cohortId;
	}
 
	public int getCohortId(){ 
		return this.cohortId;
	}
 
	public void setPatientId(int patientId){ 
	 	this.patientId = patientId;
	}


 
	public int getPatientId(){ 
		return this.patientId;
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
 		return this.cohortId; 
	} 
 
	public void setObjectId(int selfId){ 
		this.cohortId = selfId; 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		this.cohortId = rs.getInt("cohort_id");
			} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "cohort_id"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParams(){ 
 		Object[] params = {this.patientId == 0 ? null : this.patientId};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.patientId == 0 ? null : this.patientId, this.cohortId};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQL(){ 
 		return "INSERT INTO cohort_member(patient_id) VALUES(?);"; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE cohort_member SET patient_id = ? WHERE cohort_id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.patientId == 0 ? null : this.patientId); 
	} 
 
	@Override
	public boolean isGeneratedFromSkeletonClass() {
		return false;
	}

	@Override
	public boolean hasParents() {
		if (this.patientId != 0) return true;
		if (this.cohortId != 0) return true;
		return false;
	}

	@Override
	public int retrieveSharedPKKey(Connection conn) throws ParentNotYetMigratedException, DBException {
		throw new RuntimeException("No PKSharedInfo defined!");	}

	@Override
	public void loadDestParentInfo(Connection conn) throws ParentNotYetMigratedException, DBException {
		OpenMRSObject parentOnDestination = null;
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.sourcepkg.PatientVO.class, this.patientId, false, conn); 
		this.patientId = 0;
		if (parentOnDestination  != null) this.patientId = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.sourcepkg.CohortVO.class, this.cohortId, false, conn); 
		this.cohortId = 0;
		if (parentOnDestination  != null) this.cohortId = parentOnDestination.getObjectId();
 
	}

	@Override
	public int getParentValue(String parentAttName) {		
		if (parentAttName.equals("patientId")) return this.patientId;		
		if (parentAttName.equals("cohortId")) return this.cohortId;

		throw new RuntimeException("No found parent for: " + parentAttName);	}


}