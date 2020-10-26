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
 
public class ActiveListAllergyVO extends AbstractOpenMRSObject implements OpenMRSObject { 
	private int activeListId;
	private String allergyType;
	private int reactionConceptId;
	private String severity;
 
	public ActiveListAllergyVO() { 
		this.metadata = false;
	} 
 
	public void setActiveListId(int activeListId){ 
	 	this.activeListId = activeListId;
	}
 
	public int getActiveListId(){ 
		return this.activeListId;
	}
 
	public void setAllergyType(String allergyType){ 
	 	this.allergyType = allergyType;
	}
 
	public String getAllergyType(){ 
		return this.allergyType;
	}
 
	public void setReactionConceptId(int reactionConceptId){ 
	 	this.reactionConceptId = reactionConceptId;
	}
 
	public int getReactionConceptId(){ 
		return this.reactionConceptId;
	}
 
	public void setSeverity(String severity){ 
	 	this.severity = severity;
	}


 
	public String getSeverity(){ 
		return this.severity;
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
 		return this.activeListId; 
	} 
 
	public void setObjectId(int selfId){ 
		this.activeListId = selfId; 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		this.activeListId = rs.getInt("active_list_id");
		this.allergyType = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("allergy_type") != null ? rs.getString("allergy_type").trim() : null);
		this.reactionConceptId = rs.getInt("reaction_concept_id");
		this.severity = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("severity") != null ? rs.getString("severity").trim() : null);
	} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "active_list_id"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParams(){ 
 		Object[] params = {this.allergyType, this.reactionConceptId == 0 ? null : this.reactionConceptId, this.severity};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.allergyType, this.reactionConceptId == 0 ? null : this.reactionConceptId, this.severity, this.activeListId};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQL(){ 
 		return "INSERT INTO active_list_allergy(allergy_type, reaction_concept_id, severity) VALUES(?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE active_list_allergy SET allergy_type = ?, reaction_concept_id = ?, severity = ? WHERE active_list_id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.allergyType != null ? "\""+ utilities.scapeQuotationMarks(allergyType)  +"\"" : null) + "," + (this.reactionConceptId == 0 ? null : this.reactionConceptId) + "," + (this.severity != null ? "\""+ utilities.scapeQuotationMarks(severity)  +"\"" : null); 
	} 
 
	@Override
	public boolean isGeneratedFromSkeletonClass() {
		return false;
	}

	@Override
	public boolean hasParents() {
		if (this.reactionConceptId != 0) return true;
		return false;
	}

	@Override
	public int retrieveSharedPKKey(Connection conn) throws ParentNotYetMigratedException, DBException {
		throw new RuntimeException("No PKSharedInfo defined!");	}

	@Override
	public void loadDestParentInfo(Connection conn) throws ParentNotYetMigratedException, DBException {
		OpenMRSObject parentOnDestination = null;
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.sourcepkg.ConceptVO.class, this.reactionConceptId, true, conn); 
		this.reactionConceptId = 0;
		if (parentOnDestination  != null) this.reactionConceptId = parentOnDestination.getObjectId();
 
	}

	@Override
	public int getParentValue(String parentAttName) {		
		if (parentAttName.equals("reactionConceptId")) return this.reactionConceptId;

		throw new RuntimeException("No found parent for: " + parentAttName);	}


}