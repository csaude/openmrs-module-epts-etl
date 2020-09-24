package org.openmrs.module.eptssync.model.openmrs; 
 
import org.openmrs.module.eptssync.model.openmrs.generic.*; 
 
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.eptssync.utilities.db.conn.DBException; 
import org.openmrs.module.eptssync.exceptions.ParentNotYetMigratedException; 
 
import java.sql.Connection; 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class ActiveListVO extends AbstractOpenMRSObject implements OpenMRSObject { 
	private int activeListId;
	private int activeListTypeId;
	private int personId;
	private int conceptId;
	private int startObsId;
	private int stopObsId;
	private java.util.Date startDate;
	private java.util.Date endDate;
	private String comments;
	private int creator;
	private java.util.Date dateCreated;
	private byte voided;
	private int voidedBy;
	private java.util.Date dateVoided;
	private String voidReason;
	private String uuid;
 
	public ActiveListVO() { 
		this.metadata = false;
	} 
 
	public void setActiveListId(int activeListId){ 
	 	this.activeListId = activeListId;
	}
 
	public int getActiveListId(){ 
		return this.activeListId;
	}
 
	public void setActiveListTypeId(int activeListTypeId){ 
	 	this.activeListTypeId = activeListTypeId;
	}
 
	public int getActiveListTypeId(){ 
		return this.activeListTypeId;
	}
 
	public void setPersonId(int personId){ 
	 	this.personId = personId;
	}
 
	public int getPersonId(){ 
		return this.personId;
	}
 
	public void setConceptId(int conceptId){ 
	 	this.conceptId = conceptId;
	}
 
	public int getConceptId(){ 
		return this.conceptId;
	}
 
	public void setStartObsId(int startObsId){ 
	 	this.startObsId = startObsId;
	}
 
	public int getStartObsId(){ 
		return this.startObsId;
	}
 
	public void setStopObsId(int stopObsId){ 
	 	this.stopObsId = stopObsId;
	}
 
	public int getStopObsId(){ 
		return this.stopObsId;
	}
 
	public void setStartDate(java.util.Date startDate){ 
	 	this.startDate = startDate;
	}
 
	public java.util.Date getStartDate(){ 
		return this.startDate;
	}
 
	public void setEndDate(java.util.Date endDate){ 
	 	this.endDate = endDate;
	}
 
	public java.util.Date getEndDate(){ 
		return this.endDate;
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
 
	public void setVoided(byte voided){ 
	 	this.voided = voided;
	}
 
	public byte getVoided(){ 
		return this.voided;
	}
 
	public void setVoidedBy(int voidedBy){ 
	 	this.voidedBy = voidedBy;
	}
 
	public int getVoidedBy(){ 
		return this.voidedBy;
	}
 
	public void setDateVoided(java.util.Date dateVoided){ 
	 	this.dateVoided = dateVoided;
	}
 
	public java.util.Date getDateVoided(){ 
		return this.dateVoided;
	}
 
	public void setVoidReason(String voidReason){ 
	 	this.voidReason = voidReason;
	}
 
	public String getVoidReason(){ 
		return this.voidReason;
	}
 
	public void setUuid(String uuid){ 
	 	this.uuid = uuid;
	}


 
	public String getUuid(){ 
		return this.uuid;
	}	public int getOriginRecordId(){ 
		return this.originRecordId;
	}
 
	public void setOriginRecordId(int originRecordId){ 
	 	this.originRecordId = originRecordId;
	}
 
	public String getOriginAppLocationCode(){ 
		return this.originAppLocationCode;
	}
 
	public void setOriginAppLocationCode(String originAppLocationCode){ 
	 	this.originAppLocationCode = originAppLocationCode;
	}
 
	public int getConsistent(){ 
		return this.consistent;
	}
 
	public void setConsistent(int consistent){ 
	 	this.consistent = consistent;
	}
 

 
	public int getObjectId() { 
 		return this.activeListId; 
	} 
 
	public void setObjectId(int selfId){ 
		this.activeListId = selfId; 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		this.activeListId = rs.getInt("active_list_id");
		this.activeListTypeId = rs.getInt("active_list_type_id");
		this.personId = rs.getInt("person_id");
		this.conceptId = rs.getInt("concept_id");
		this.startObsId = rs.getInt("start_obs_id");
		this.stopObsId = rs.getInt("stop_obs_id");
		this.startDate =  rs.getTimestamp("start_date") != null ? new java.util.Date( rs.getTimestamp("start_date").getTime() ) : null;
		this.endDate =  rs.getTimestamp("end_date") != null ? new java.util.Date( rs.getTimestamp("end_date").getTime() ) : null;
		this.comments = rs.getString("comments") != null ? rs.getString("comments").trim() : null;
		this.creator = rs.getInt("creator");
		this.dateCreated =  rs.getTimestamp("date_created") != null ? new java.util.Date( rs.getTimestamp("date_created").getTime() ) : null;
		this.voided = rs.getByte("voided");
		this.voidedBy = rs.getInt("voided_by");
		this.dateVoided =  rs.getTimestamp("date_voided") != null ? new java.util.Date( rs.getTimestamp("date_voided").getTime() ) : null;
		this.voidReason = rs.getString("void_reason") != null ? rs.getString("void_reason").trim() : null;
			} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "active_list_id"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParams(){ 
 		Object[] params = {this.activeListTypeId == 0 ? null : this.activeListTypeId, this.personId == 0 ? null : this.personId, this.conceptId == 0 ? null : this.conceptId, this.startObsId == 0 ? null : this.startObsId, this.stopObsId == 0 ? null : this.stopObsId, this.startDate, this.endDate, this.comments, this.creator == 0 ? null : this.creator, this.dateCreated, this.voided, this.voidedBy == 0 ? null : this.voidedBy, this.dateVoided, this.voidReason, this.uuid};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.activeListTypeId == 0 ? null : this.activeListTypeId, this.personId == 0 ? null : this.personId, this.conceptId == 0 ? null : this.conceptId, this.startObsId == 0 ? null : this.startObsId, this.stopObsId == 0 ? null : this.stopObsId, this.startDate, this.endDate, this.comments, this.creator == 0 ? null : this.creator, this.dateCreated, this.voided, this.voidedBy == 0 ? null : this.voidedBy, this.dateVoided, this.voidReason, this.uuid, this.activeListId};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQL(){ 
 		return "INSERT INTO active_list(active_list_type_id, person_id, concept_id, start_obs_id, stop_obs_id, start_date, end_date, comments, creator, date_created, voided, voided_by, date_voided, void_reason, uuid) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE active_list SET active_list_type_id = ?, person_id = ?, concept_id = ?, start_obs_id = ?, stop_obs_id = ?, start_date = ?, end_date = ?, comments = ?, creator = ?, date_created = ?, voided = ?, voided_by = ?, date_voided = ?, void_reason = ?, uuid = ? WHERE active_list_id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return (this.activeListTypeId == 0 ? null : this.activeListTypeId) + "," + (this.personId == 0 ? null : this.personId) + "," + (this.conceptId == 0 ? null : this.conceptId) + "," + (this.startObsId == 0 ? null : this.startObsId) + "," + (this.stopObsId == 0 ? null : this.stopObsId) + "," + (this.startDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(startDate)  +"\"" : null) + "," + (this.endDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(endDate)  +"\"" : null) + "," + (this.comments != null ? "\""+comments+"\"" : null) + "," + (this.creator == 0 ? null : this.creator) + "," + (this.dateCreated != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateCreated)  +"\"" : null) + "," + (this.voided) + "," + (this.voidedBy == 0 ? null : this.voidedBy) + "," + (this.dateVoided != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateVoided)  +"\"" : null) + "," + (this.voidReason != null ? "\""+voidReason+"\"" : null) + "," + (this.uuid != null ? "\""+uuid+"\"" : null); 
	} 
 
	@Override
	public boolean hasParents() {
		if (this.activeListTypeId != 0) return true;
		if (this.conceptId != 0) return true;
		if (this.personId != 0) return true;
		if (this.startObsId != 0) return true;
		if (this.stopObsId != 0) return true;
		if (this.creator != 0) return true;
		if (this.voidedBy != 0) return true;
		return false;
	}

	@Override
	public int retrieveSharedPKKey(Connection conn) throws ParentNotYetMigratedException, DBException {
		throw new RuntimeException("No PKSharedInfo defined!");	}

	@Override
	public void loadDestParentInfo(Connection conn) throws ParentNotYetMigratedException, DBException {
		OpenMRSObject parentOnDestination = null;
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.ActiveListTypeVO.class, this.activeListTypeId, false, conn); 
		this.activeListTypeId = 0;
		if (parentOnDestination  != null) this.activeListTypeId = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.ConceptVO.class, this.conceptId, false, conn); 
		this.conceptId = 0;
		if (parentOnDestination  != null) this.conceptId = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.PersonVO.class, this.personId, false, conn); 
		this.personId = 0;
		if (parentOnDestination  != null) this.personId = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.ObsVO.class, this.startObsId, true, conn); 
		this.startObsId = 0;
		if (parentOnDestination  != null) this.startObsId = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.ObsVO.class, this.stopObsId, true, conn); 
		this.stopObsId = 0;
		if (parentOnDestination  != null) this.stopObsId = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.UsersVO.class, this.creator, false, conn); 
		this.creator = 0;
		if (parentOnDestination  != null) this.creator = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.UsersVO.class, this.voidedBy, true, conn); 
		this.voidedBy = 0;
		if (parentOnDestination  != null) this.voidedBy = parentOnDestination.getObjectId();
 
	}

	@Override
	public int getParentValue(String parentAttName) {		
		if (parentAttName.equals("activeListTypeId")) return this.activeListTypeId;		
		if (parentAttName.equals("conceptId")) return this.conceptId;		
		if (parentAttName.equals("personId")) return this.personId;		
		if (parentAttName.equals("startObsId")) return this.startObsId;		
		if (parentAttName.equals("stopObsId")) return this.stopObsId;		
		if (parentAttName.equals("creator")) return this.creator;		
		if (parentAttName.equals("voidedBy")) return this.voidedBy;

		throw new RuntimeException("No found parent for: " + parentAttName);	}


}