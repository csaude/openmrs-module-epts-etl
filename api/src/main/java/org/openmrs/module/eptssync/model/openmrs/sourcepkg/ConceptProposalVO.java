package org.openmrs.module.eptssync.model.openmrs.sourcepkg; 
 
import org.openmrs.module.eptssync.model.openmrs.generic.*; 
 
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.eptssync.utilities.db.conn.DBException; 
import org.openmrs.module.eptssync.exceptions.ParentNotYetMigratedException; 
 
import java.sql.Connection; 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class ConceptProposalVO extends AbstractOpenMRSObject implements OpenMRSObject { 
	private int conceptProposalId;
	private int conceptId;
	private int encounterId;
	private String originalText;
	private String finalText;
	private int obsId;
	private int obsConceptId;
	private String state;
	private String comments;
	private int creator;
	private java.util.Date dateCreated;
	private int changedBy;
	private java.util.Date dateChanged;
	private String locale;
	private String uuid;
 
	public ConceptProposalVO() { 
		this.metadata = false;
	} 
 
	public void setConceptProposalId(int conceptProposalId){ 
	 	this.conceptProposalId = conceptProposalId;
	}
 
	public int getConceptProposalId(){ 
		return this.conceptProposalId;
	}
 
	public void setConceptId(int conceptId){ 
	 	this.conceptId = conceptId;
	}
 
	public int getConceptId(){ 
		return this.conceptId;
	}
 
	public void setEncounterId(int encounterId){ 
	 	this.encounterId = encounterId;
	}
 
	public int getEncounterId(){ 
		return this.encounterId;
	}
 
	public void setOriginalText(String originalText){ 
	 	this.originalText = originalText;
	}
 
	public String getOriginalText(){ 
		return this.originalText;
	}
 
	public void setFinalText(String finalText){ 
	 	this.finalText = finalText;
	}
 
	public String getFinalText(){ 
		return this.finalText;
	}
 
	public void setObsId(int obsId){ 
	 	this.obsId = obsId;
	}
 
	public int getObsId(){ 
		return this.obsId;
	}
 
	public void setObsConceptId(int obsConceptId){ 
	 	this.obsConceptId = obsConceptId;
	}
 
	public int getObsConceptId(){ 
		return this.obsConceptId;
	}
 
	public void setState(String state){ 
	 	this.state = state;
	}
 
	public String getState(){ 
		return this.state;
	}
 
	public void setComments(String comments){ 
	 	this.comments = comments;
	}
 
	public String getComments(){ 
		return this.comments;
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
 
	public void setChangedBy(int changedBy){ 
	 	this.changedBy = changedBy;
	}
 
	public int getChangedBy(){ 
		return this.changedBy;
	}
 
	public void setDateChanged(java.util.Date dateChanged){ 
	 	this.dateChanged = dateChanged;
	}
 
	public java.util.Date getDateChanged(){ 
		return this.dateChanged;
	}
 
	public void setLocale(String locale){ 
	 	this.locale = locale;
	}
 
	public String getLocale(){ 
		return this.locale;
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
 		return this.conceptProposalId; 
	} 
 
	public void setObjectId(int selfId){ 
		this.conceptProposalId = selfId; 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		this.conceptProposalId = rs.getInt("concept_proposal_id");
		this.conceptId = rs.getInt("concept_id");
		this.encounterId = rs.getInt("encounter_id");
		this.originalText = rs.getString("original_text") != null ? rs.getString("original_text").trim() : null;
		this.finalText = rs.getString("final_text") != null ? rs.getString("final_text").trim() : null;
		this.obsId = rs.getInt("obs_id");
		this.obsConceptId = rs.getInt("obs_concept_id");
		this.state = rs.getString("state") != null ? rs.getString("state").trim() : null;
		this.comments = rs.getString("comments") != null ? rs.getString("comments").trim() : null;
		this.creator = rs.getInt("creator");
		this.dateCreated =  rs.getTimestamp("date_created") != null ? new java.util.Date( rs.getTimestamp("date_created").getTime() ) : null;
		this.changedBy = rs.getInt("changed_by");
		this.dateChanged =  rs.getTimestamp("date_changed") != null ? new java.util.Date( rs.getTimestamp("date_changed").getTime() ) : null;
		this.locale = rs.getString("locale") != null ? rs.getString("locale").trim() : null;
			} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "concept_proposal_id"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParams(){ 
 		Object[] params = {this.conceptId == 0 ? null : this.conceptId, this.encounterId == 0 ? null : this.encounterId, this.originalText, this.finalText, this.obsId == 0 ? null : this.obsId, this.obsConceptId == 0 ? null : this.obsConceptId, this.state, this.comments, this.creator == 0 ? null : this.creator, this.dateCreated, this.changedBy == 0 ? null : this.changedBy, this.dateChanged, this.locale, this.uuid};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.conceptId == 0 ? null : this.conceptId, this.encounterId == 0 ? null : this.encounterId, this.originalText, this.finalText, this.obsId == 0 ? null : this.obsId, this.obsConceptId == 0 ? null : this.obsConceptId, this.state, this.comments, this.creator == 0 ? null : this.creator, this.dateCreated, this.changedBy == 0 ? null : this.changedBy, this.dateChanged, this.locale, this.uuid, this.conceptProposalId};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQL(){ 
 		return "INSERT INTO concept_proposal(concept_id, encounter_id, original_text, final_text, obs_id, obs_concept_id, state, comments, creator, date_created, changed_by, date_changed, locale, uuid) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE concept_proposal SET concept_id = ?, encounter_id = ?, original_text = ?, final_text = ?, obs_id = ?, obs_concept_id = ?, state = ?, comments = ?, creator = ?, date_created = ?, changed_by = ?, date_changed = ?, locale = ?, uuid = ? WHERE concept_proposal_id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.conceptId == 0 ? null : this.conceptId) + "," + (this.encounterId == 0 ? null : this.encounterId) + "," + (this.originalText != null ? "\""+originalText+"\"" : null) + "," + (this.finalText != null ? "\""+finalText+"\"" : null) + "," + (this.obsId == 0 ? null : this.obsId) + "," + (this.obsConceptId == 0 ? null : this.obsConceptId) + "," + (this.state != null ? "\""+state+"\"" : null) + "," + (this.comments != null ? "\""+comments+"\"" : null) + "," + (this.creator == 0 ? null : this.creator) + "," + (this.dateCreated != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateCreated)  +"\"" : null) + "," + (this.changedBy == 0 ? null : this.changedBy) + "," + (this.dateChanged != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateChanged)  +"\"" : null) + "," + (this.locale != null ? "\""+locale+"\"" : null) + "," + (this.uuid != null ? "\""+uuid+"\"" : null); 
	} 
 
	@Override
	public boolean isGeneratedFromSkeletonClass() {
		return false;
	}

	@Override
	public boolean hasParents() {
		if (this.conceptId != 0) return true;
		if (this.encounterId != 0) return true;
		if (this.obsConceptId != 0) return true;
		if (this.obsId != 0) return true;
		if (this.changedBy != 0) return true;
		if (this.creator != 0) return true;
		return false;
	}

	@Override
	public int retrieveSharedPKKey(Connection conn) throws ParentNotYetMigratedException, DBException {
		throw new RuntimeException("No PKSharedInfo defined!");	}

	@Override
	public void loadDestParentInfo(Connection conn) throws ParentNotYetMigratedException, DBException {
		OpenMRSObject parentOnDestination = null;
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.sourcepkg.ConceptVO.class, this.conceptId, true, conn); 
		this.conceptId = 0;
		if (parentOnDestination  != null) this.conceptId = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.sourcepkg.EncounterVO.class, this.encounterId, true, conn); 
		this.encounterId = 0;
		if (parentOnDestination  != null) this.encounterId = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.sourcepkg.ConceptVO.class, this.obsConceptId, true, conn); 
		this.obsConceptId = 0;
		if (parentOnDestination  != null) this.obsConceptId = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.sourcepkg.ObsVO.class, this.obsId, true, conn); 
		this.obsId = 0;
		if (parentOnDestination  != null) this.obsId = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.sourcepkg.UsersVO.class, this.changedBy, true, conn); 
		this.changedBy = 0;
		if (parentOnDestination  != null) this.changedBy = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.sourcepkg.UsersVO.class, this.creator, false, conn); 
		this.creator = 0;
		if (parentOnDestination  != null) this.creator = parentOnDestination.getObjectId();
 
	}

	@Override
	public int getParentValue(String parentAttName) {		
		if (parentAttName.equals("conceptId")) return this.conceptId;		
		if (parentAttName.equals("encounterId")) return this.encounterId;		
		if (parentAttName.equals("obsConceptId")) return this.obsConceptId;		
		if (parentAttName.equals("obsId")) return this.obsId;		
		if (parentAttName.equals("changedBy")) return this.changedBy;		
		if (parentAttName.equals("creator")) return this.creator;

		throw new RuntimeException("No found parent for: " + parentAttName);	}


}