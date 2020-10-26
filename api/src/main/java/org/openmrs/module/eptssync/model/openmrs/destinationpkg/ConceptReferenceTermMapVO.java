package org.openmrs.module.eptssync.model.openmrs.destinationpkg; 
 
import org.openmrs.module.eptssync.model.openmrs.generic.*; 
 
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.eptssync.utilities.db.conn.DBException; 
import org.openmrs.module.eptssync.utilities.AttDefinedElements; 
import org.openmrs.module.eptssync.exceptions.ParentNotYetMigratedException; 
 
import java.sql.Connection; 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class ConceptReferenceTermMapVO extends AbstractOpenMRSObject implements OpenMRSObject { 
	private int conceptReferenceTermMapId;
	private int termAId;
	private int termBId;
	private int aIsToBId;
	private int creator;
	private java.util.Date dateCreated;
	private int changedBy;
	private java.util.Date dateChanged;
	private String uuid;
 
	public ConceptReferenceTermMapVO() { 
		this.metadata = false;
	} 
 
	public void setConceptReferenceTermMapId(int conceptReferenceTermMapId){ 
	 	this.conceptReferenceTermMapId = conceptReferenceTermMapId;
	}
 
	public int getConceptReferenceTermMapId(){ 
		return this.conceptReferenceTermMapId;
	}
 
	public void setTermAId(int termAId){ 
	 	this.termAId = termAId;
	}
 
	public int getTermAId(){ 
		return this.termAId;
	}
 
	public void setTermBId(int termBId){ 
	 	this.termBId = termBId;
	}
 
	public int getTermBId(){ 
		return this.termBId;
	}
 
	public void setAIsToBId(int aIsToBId){ 
	 	this.aIsToBId = aIsToBId;
	}
 
	public int getAIsToBId(){ 
		return this.aIsToBId;
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
 		return this.conceptReferenceTermMapId; 
	} 
 
	public void setObjectId(int selfId){ 
		this.conceptReferenceTermMapId = selfId; 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		this.conceptReferenceTermMapId = rs.getInt("concept_reference_term_map_id");
		this.termAId = rs.getInt("term_a_id");
		this.termBId = rs.getInt("term_b_id");
		this.aIsToBId = rs.getInt("a_is_to_b_id");
		this.creator = rs.getInt("creator");
		this.dateCreated =  rs.getTimestamp("date_created") != null ? new java.util.Date( rs.getTimestamp("date_created").getTime() ) : null;
		this.changedBy = rs.getInt("changed_by");
		this.dateChanged =  rs.getTimestamp("date_changed") != null ? new java.util.Date( rs.getTimestamp("date_changed").getTime() ) : null;
		this.uuid = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("uuid") != null ? rs.getString("uuid").trim() : null);
	} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "concept_reference_term_map_id"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParams(){ 
 		Object[] params = {this.termAId == 0 ? null : this.termAId, this.termBId == 0 ? null : this.termBId, this.aIsToBId == 0 ? null : this.aIsToBId, this.creator == 0 ? null : this.creator, this.dateCreated, this.changedBy == 0 ? null : this.changedBy, this.dateChanged, this.uuid};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.termAId == 0 ? null : this.termAId, this.termBId == 0 ? null : this.termBId, this.aIsToBId == 0 ? null : this.aIsToBId, this.creator == 0 ? null : this.creator, this.dateCreated, this.changedBy == 0 ? null : this.changedBy, this.dateChanged, this.uuid, this.conceptReferenceTermMapId};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQL(){ 
 		return "INSERT INTO concept_reference_term_map(term_a_id, term_b_id, a_is_to_b_id, creator, date_created, changed_by, date_changed, uuid) VALUES(?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE concept_reference_term_map SET term_a_id = ?, term_b_id = ?, a_is_to_b_id = ?, creator = ?, date_created = ?, changed_by = ?, date_changed = ?, uuid = ? WHERE concept_reference_term_map_id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.termAId == 0 ? null : this.termAId) + "," + (this.termBId == 0 ? null : this.termBId) + "," + (this.aIsToBId == 0 ? null : this.aIsToBId) + "," + (this.creator == 0 ? null : this.creator) + "," + (this.dateCreated != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateCreated)  +"\"" : null) + "," + (this.changedBy == 0 ? null : this.changedBy) + "," + (this.dateChanged != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateChanged)  +"\"" : null) + "," + (this.uuid != null ? "\""+ utilities.scapeQuotationMarks(uuid)  +"\"" : null); 
	} 
 
	@Override
	public boolean isGeneratedFromSkeletonClass() {
		return false;
	}

	@Override
	public boolean hasParents() {
		if (this.aIsToBId != 0) return true;
		if (this.termAId != 0) return true;
		if (this.termBId != 0) return true;
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
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.destinationpkg.ConceptMapTypeVO.class, this.aIsToBId, false, conn); 
		this.aIsToBId = 0;
		if (parentOnDestination  != null) this.aIsToBId = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.destinationpkg.ConceptReferenceTermVO.class, this.termAId, false, conn); 
		this.termAId = 0;
		if (parentOnDestination  != null) this.termAId = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.destinationpkg.ConceptReferenceTermVO.class, this.termBId, false, conn); 
		this.termBId = 0;
		if (parentOnDestination  != null) this.termBId = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.destinationpkg.UsersVO.class, this.changedBy, true, conn); 
		this.changedBy = 0;
		if (parentOnDestination  != null) this.changedBy = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.destinationpkg.UsersVO.class, this.creator, false, conn); 
		this.creator = 0;
		if (parentOnDestination  != null) this.creator = parentOnDestination.getObjectId();
 
	}

	@Override
	public int getParentValue(String parentAttName) {		
		if (parentAttName.equals("aIsToBId")) return this.aIsToBId;		
		if (parentAttName.equals("termAId")) return this.termAId;		
		if (parentAttName.equals("termBId")) return this.termBId;		
		if (parentAttName.equals("changedBy")) return this.changedBy;		
		if (parentAttName.equals("creator")) return this.creator;

		throw new RuntimeException("No found parent for: " + parentAttName);	}


}