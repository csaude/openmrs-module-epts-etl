package org.openmrs.module.eptssync.model.openmrs.sourcepkg; 
 
import org.openmrs.module.eptssync.model.openmrs.generic.*; 
 
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.eptssync.utilities.db.conn.DBException; 
import org.openmrs.module.eptssync.exceptions.ParentNotYetMigratedException; 
 
import java.sql.Connection; 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class ConceptAnswerVO extends AbstractOpenMRSObject implements OpenMRSObject { 
	private int conceptAnswerId;
	private int conceptId;
	private int answerConcept;
	private int answerDrug;
	private int creator;
	private java.util.Date dateCreated;
	private String uuid;
	private double sortWeight;
 
	public ConceptAnswerVO() { 
		this.metadata = false;
	} 
 
	public void setConceptAnswerId(int conceptAnswerId){ 
	 	this.conceptAnswerId = conceptAnswerId;
	}
 
	public int getConceptAnswerId(){ 
		return this.conceptAnswerId;
	}
 
	public void setConceptId(int conceptId){ 
	 	this.conceptId = conceptId;
	}
 
	public int getConceptId(){ 
		return this.conceptId;
	}
 
	public void setAnswerConcept(int answerConcept){ 
	 	this.answerConcept = answerConcept;
	}
 
	public int getAnswerConcept(){ 
		return this.answerConcept;
	}
 
	public void setAnswerDrug(int answerDrug){ 
	 	this.answerDrug = answerDrug;
	}
 
	public int getAnswerDrug(){ 
		return this.answerDrug;
	}
 
	public void setCreator(int creator){ 
	 	this.creator = creator;
	}
 
	public int getCreator(){ 
		return this.creator;
	}
 
	public void setDateCreated(java.util.Date dateCreated){ 
	 	this.dateCreated = dateCreated;
	}
 
	public java.util.Date getDateCreated(){ 
		return this.dateCreated;
	}
 
	public void setUuid(String uuid){ 
	 	this.uuid = uuid;
	}
 
	public String getUuid(){ 
		return this.uuid;
	}
 
	public void setSortWeight(double sortWeight){ 
	 	this.sortWeight = sortWeight;
	}


 
	public double getSortWeight(){ 
		return this.sortWeight;
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
 		return this.conceptAnswerId; 
	} 
 
	public void setObjectId(int selfId){ 
		this.conceptAnswerId = selfId; 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		this.conceptAnswerId = rs.getInt("concept_answer_id");
		this.conceptId = rs.getInt("concept_id");
		this.answerConcept = rs.getInt("answer_concept");
		this.answerDrug = rs.getInt("answer_drug");
		this.creator = rs.getInt("creator");
		this.dateCreated =  rs.getTimestamp("date_created") != null ? new java.util.Date( rs.getTimestamp("date_created").getTime() ) : null;
		this.uuid = rs.getString("uuid") != null ? rs.getString("uuid").trim() : null;
			} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "concept_answer_id"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParams(){ 
 		Object[] params = {this.conceptId == 0 ? null : this.conceptId, this.answerConcept == 0 ? null : this.answerConcept, this.answerDrug == 0 ? null : this.answerDrug, this.creator == 0 ? null : this.creator, this.dateCreated, this.uuid, this.sortWeight};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.conceptId == 0 ? null : this.conceptId, this.answerConcept == 0 ? null : this.answerConcept, this.answerDrug == 0 ? null : this.answerDrug, this.creator == 0 ? null : this.creator, this.dateCreated, this.uuid, this.sortWeight, this.conceptAnswerId};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQL(){ 
 		return "INSERT INTO concept_answer(concept_id, answer_concept, answer_drug, creator, date_created, uuid, sort_weight) VALUES(?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE concept_answer SET concept_id = ?, answer_concept = ?, answer_drug = ?, creator = ?, date_created = ?, uuid = ?, sort_weight = ? WHERE concept_answer_id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.conceptId == 0 ? null : this.conceptId) + "," + (this.answerConcept == 0 ? null : this.answerConcept) + "," + (this.answerDrug == 0 ? null : this.answerDrug) + "," + (this.creator == 0 ? null : this.creator) + "," + (this.dateCreated != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateCreated)  +"\"" : null) + "," + (this.uuid != null ? "\""+uuid+"\"" : null) + "," + (this.sortWeight); 
	} 
 
	@Override
	public boolean isGeneratedFromSkeletonClass() {
		return false;
	}

	@Override
	public boolean hasParents() {
		if (this.answerConcept != 0) return true;
		if (this.answerDrug != 0) return true;
		if (this.creator != 0) return true;
		if (this.conceptId != 0) return true;
		return false;
	}

	@Override
	public int retrieveSharedPKKey(Connection conn) throws ParentNotYetMigratedException, DBException {
		throw new RuntimeException("No PKSharedInfo defined!");	}

	@Override
	public void loadDestParentInfo(Connection conn) throws ParentNotYetMigratedException, DBException {
		OpenMRSObject parentOnDestination = null;
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.sourcepkg.ConceptVO.class, this.answerConcept, true, conn); 
		this.answerConcept = 0;
		if (parentOnDestination  != null) this.answerConcept = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.sourcepkg.DrugVO.class, this.answerDrug, true, conn); 
		this.answerDrug = 0;
		if (parentOnDestination  != null) this.answerDrug = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.sourcepkg.UsersVO.class, this.creator, false, conn); 
		this.creator = 0;
		if (parentOnDestination  != null) this.creator = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.sourcepkg.ConceptVO.class, this.conceptId, false, conn); 
		this.conceptId = 0;
		if (parentOnDestination  != null) this.conceptId = parentOnDestination.getObjectId();
 
	}

	@Override
	public int getParentValue(String parentAttName) {		
		if (parentAttName.equals("answerConcept")) return this.answerConcept;		
		if (parentAttName.equals("answerDrug")) return this.answerDrug;		
		if (parentAttName.equals("creator")) return this.creator;		
		if (parentAttName.equals("conceptId")) return this.conceptId;

		throw new RuntimeException("No found parent for: " + parentAttName);	}


}