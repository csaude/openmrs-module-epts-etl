package org.openmrs.module.eptssync.model.pojo.cs_mopeia; 
 
import org.openmrs.module.eptssync.model.pojo.generic.*; 
 
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.eptssync.utilities.AttDefinedElements; 
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
 

 
	public int getObjectId() { 
 		return this.cohortId; 
	} 
 
	public void setObjectId(int selfId){ 
		this.cohortId = selfId; 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		this.cohortId = rs.getInt("cohort_id");
		this.patientId = rs.getInt("patient_id");
	} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "cohort_id"; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithoutObjectId(){ 
 		return "INSERT INTO cohort_member(patient_id) VALUES( ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithoutObjectId(){ 
 		Object[] params = {this.patientId == 0 ? null : this.patientId};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithObjectId(){ 
 		return "INSERT INTO cohort_member(cohort_id, patient_id) VALUES(?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithObjectId(){ 
 		Object[] params = {this.cohortId, this.patientId == 0 ? null : this.patientId};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.patientId == 0 ? null : this.patientId, this.cohortId};		return params; 
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
	public boolean hasParents() {
		if (this.patientId != 0) return true;

		if (this.cohortId != 0) return true;

		return false;
	}

	@Override
	public int getParentValue(String parentAttName) {		
		if (parentAttName.equals("patientId")) return this.patientId;		
		if (parentAttName.equals("cohortId")) return this.cohortId;

		throw new RuntimeException("No found parent for: " + parentAttName);	}

	@Override
	public void changeParentValue(String parentAttName, OpenMRSObject newParent) {		
		if (parentAttName.equals("patientId")) {
			this.patientId = newParent.getObjectId();
			return;
		}		
		if (parentAttName.equals("cohortId")) {
			this.cohortId = newParent.getObjectId();
			return;
		}

		throw new RuntimeException("No found parent for: " + parentAttName);
	}


}