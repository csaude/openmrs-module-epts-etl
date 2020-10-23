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
 
public class ConceptReferenceMapVO extends AbstractOpenMRSObject implements OpenMRSObject { 
	private int conceptMapId;
	private int creator;
	private java.util.Date dateCreated;
	private int conceptId;
	private String uuid;
	private int conceptReferenceTermId;
	private int conceptMapTypeId;
	private int changedBy;
	private java.util.Date dateChanged;
 
	public ConceptReferenceMapVO() { 
		this.metadata = false;
	} 
 
	public void setConceptMapId(int conceptMapId){ 
	 	this.conceptMapId = conceptMapId;
	}
 
	public int getConceptMapId(){ 
		return this.conceptMapId;
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
 
	public void setConceptId(int conceptId){ 
	 	this.conceptId = conceptId;
	}
 
	public int getConceptId(){ 
		return this.conceptId;
	}
 
	public void setUuid(String uuid){ 
	 	this.uuid = uuid;
	}
 
	public String getUuid(){ 
		return this.uuid;
	}
 
	public void setConceptReferenceTermId(int conceptReferenceTermId){ 
	 	this.conceptReferenceTermId = conceptReferenceTermId;
	}
 
	public int getConceptReferenceTermId(){ 
		return this.conceptReferenceTermId;
	}
 
	public void setConceptMapTypeId(int conceptMapTypeId){ 
	 	this.conceptMapTypeId = conceptMapTypeId;
	}
 
	public int getConceptMapTypeId(){ 
		return this.conceptMapTypeId;
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
 		return this.conceptMapId; 
	} 
 
	public void setObjectId(int selfId){ 
		this.conceptMapId = selfId; 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		this.conceptMapId = rs.getInt("concept_map_id");
		this.creator = rs.getInt("creator");
		this.dateCreated =  rs.getTimestamp("date_created") != null ? new java.util.Date( rs.getTimestamp("date_created").getTime() ) : null;
		this.conceptId = rs.getInt("concept_id");
		this.uuid = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("uuid") != null ? rs.getString("uuid").trim() : null);
		this.conceptReferenceTermId = rs.getInt("concept_reference_term_id");
		this.conceptMapTypeId = rs.getInt("concept_map_type_id");
		this.changedBy = rs.getInt("changed_by");
		this.dateChanged =  rs.getTimestamp("date_changed") != null ? new java.util.Date( rs.getTimestamp("date_changed").getTime() ) : null;
	} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "concept_map_id"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParams(){ 
 		Object[] params = {this.creator == 0 ? null : this.creator, this.dateCreated, this.conceptId == 0 ? null : this.conceptId, this.uuid, this.conceptReferenceTermId == 0 ? null : this.conceptReferenceTermId, this.conceptMapTypeId == 0 ? null : this.conceptMapTypeId, this.changedBy == 0 ? null : this.changedBy, this.dateChanged};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.creator == 0 ? null : this.creator, this.dateCreated, this.conceptId == 0 ? null : this.conceptId, this.uuid, this.conceptReferenceTermId == 0 ? null : this.conceptReferenceTermId, this.conceptMapTypeId == 0 ? null : this.conceptMapTypeId, this.changedBy == 0 ? null : this.changedBy, this.dateChanged, this.conceptMapId};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQL(){ 
 		return "INSERT INTO concept_reference_map(creator, date_created, concept_id, uuid, concept_reference_term_id, concept_map_type_id, changed_by, date_changed) VALUES(?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE concept_reference_map SET creator = ?, date_created = ?, concept_id = ?, uuid = ?, concept_reference_term_id = ?, concept_map_type_id = ?, changed_by = ?, date_changed = ? WHERE concept_map_id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.creator == 0 ? null : this.creator) + "," + (this.dateCreated != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateCreated)  +"\"" : null) + "," + (this.conceptId == 0 ? null : this.conceptId) + "," + (this.uuid != null ? "\""+ utilities.scapeQuotationMarks(uuid)  +"\"" : null) + "," + (this.conceptReferenceTermId == 0 ? null : this.conceptReferenceTermId) + "," + (this.conceptMapTypeId == 0 ? null : this.conceptMapTypeId) + "," + (this.changedBy == 0 ? null : this.changedBy) + "," + (this.dateChanged != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateChanged)  +"\"" : null); 
	} 
 
	@Override
	public boolean isGeneratedFromSkeletonClass() {
		return false;
	}

	@Override
	public boolean hasParents() {
		if (this.creator != 0) return true;
		if (this.conceptId != 0) return true;
		if (this.conceptMapTypeId != 0) return true;
		if (this.conceptReferenceTermId != 0) return true;
		if (this.changedBy != 0) return true;
		return false;
	}

	@Override
	public int retrieveSharedPKKey(Connection conn) throws ParentNotYetMigratedException, DBException {
		throw new RuntimeException("No PKSharedInfo defined!");	}

	@Override
	public void loadDestParentInfo(Connection conn) throws ParentNotYetMigratedException, DBException {
		OpenMRSObject parentOnDestination = null;
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.destinationpkg.UsersVO.class, this.creator, false, conn); 
		this.creator = 0;
		if (parentOnDestination  != null) this.creator = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.destinationpkg.ConceptVO.class, this.conceptId, false, conn); 
		this.conceptId = 0;
		if (parentOnDestination  != null) this.conceptId = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.destinationpkg.ConceptMapTypeVO.class, this.conceptMapTypeId, false, conn); 
		this.conceptMapTypeId = 0;
		if (parentOnDestination  != null) this.conceptMapTypeId = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.destinationpkg.ConceptReferenceTermVO.class, this.conceptReferenceTermId, false, conn); 
		this.conceptReferenceTermId = 0;
		if (parentOnDestination  != null) this.conceptReferenceTermId = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.destinationpkg.UsersVO.class, this.changedBy, true, conn); 
		this.changedBy = 0;
		if (parentOnDestination  != null) this.changedBy = parentOnDestination.getObjectId();
 
	}

	@Override
	public int getParentValue(String parentAttName) {		
		if (parentAttName.equals("creator")) return this.creator;		
		if (parentAttName.equals("conceptId")) return this.conceptId;		
		if (parentAttName.equals("conceptMapTypeId")) return this.conceptMapTypeId;		
		if (parentAttName.equals("conceptReferenceTermId")) return this.conceptReferenceTermId;		
		if (parentAttName.equals("changedBy")) return this.changedBy;

		throw new RuntimeException("No found parent for: " + parentAttName);	}


}