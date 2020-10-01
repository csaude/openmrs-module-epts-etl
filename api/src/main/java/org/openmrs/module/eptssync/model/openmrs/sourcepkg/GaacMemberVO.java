package org.openmrs.module.eptssync.model.openmrs.sourcepkg; 
 
import org.openmrs.module.eptssync.model.openmrs.generic.*; 
 
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.eptssync.utilities.db.conn.DBException; 
import org.openmrs.module.eptssync.exceptions.ParentNotYetMigratedException; 
 
import java.sql.Connection; 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class GaacMemberVO extends AbstractOpenMRSObject implements OpenMRSObject { 
	private int gaacMemberId;
	private int gaacId;
	private int memberId;
	private java.util.Date startDate;
	private java.util.Date endDate;
	private int reasonLeavingType;
	private String description;
	private short leaving;
	private short restart;
	private java.util.Date restartDate;
	private int creator;
	private java.util.Date dateCreated;
	private int changedBy;
	private java.util.Date dateChanged;
	private short voided;
	private int voidedBy;
	private java.util.Date dateVoided;
	private String voidReason;
	private String uuid;
 
	public GaacMemberVO() { 
		this.metadata = false;
	} 
 
	public void setGaacMemberId(int gaacMemberId){ 
	 	this.gaacMemberId = gaacMemberId;
	}
 
	public int getGaacMemberId(){ 
		return this.gaacMemberId;
	}
 
	public void setGaacId(int gaacId){ 
	 	this.gaacId = gaacId;
	}
 
	public int getGaacId(){ 
		return this.gaacId;
	}
 
	public void setMemberId(int memberId){ 
	 	this.memberId = memberId;
	}
 
	public int getMemberId(){ 
		return this.memberId;
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
 
	public void setReasonLeavingType(int reasonLeavingType){ 
	 	this.reasonLeavingType = reasonLeavingType;
	}
 
	public int getReasonLeavingType(){ 
		return this.reasonLeavingType;
	}
 
	public void setDescription(String description){ 
	 	this.description = description;
	}
 
	public String getDescription(){ 
		return this.description;
	}
 
	public void setLeaving(short leaving){ 
	 	this.leaving = leaving;
	}
 
	public short getLeaving(){ 
		return this.leaving;
	}
 
	public void setRestart(short restart){ 
	 	this.restart = restart;
	}
 
	public short getRestart(){ 
		return this.restart;
	}
 
	public void setRestartDate(java.util.Date restartDate){ 
	 	this.restartDate = restartDate;
	}
 
	public java.util.Date getRestartDate(){ 
		return this.restartDate;
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
 
	public void setVoided(short voided){ 
	 	this.voided = voided;
	}
 
	public short getVoided(){ 
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
 		return this.gaacMemberId; 
	} 
 
	public void setObjectId(int selfId){ 
		this.gaacMemberId = selfId; 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		this.gaacMemberId = rs.getInt("gaac_member_id");
		this.gaacId = rs.getInt("gaac_id");
		this.memberId = rs.getInt("member_id");
		this.startDate =  rs.getTimestamp("start_date") != null ? new java.util.Date( rs.getTimestamp("start_date").getTime() ) : null;
		this.endDate =  rs.getTimestamp("end_date") != null ? new java.util.Date( rs.getTimestamp("end_date").getTime() ) : null;
		this.reasonLeavingType = rs.getInt("reason_leaving_type");
		this.description = rs.getString("description") != null ? rs.getString("description").trim() : null;
		this.leaving = rs.getShort("leaving");
		this.restart = rs.getShort("restart");
		this.restartDate =  rs.getTimestamp("restart_date") != null ? new java.util.Date( rs.getTimestamp("restart_date").getTime() ) : null;
		this.creator = rs.getInt("creator");
		this.dateCreated =  rs.getTimestamp("date_created") != null ? new java.util.Date( rs.getTimestamp("date_created").getTime() ) : null;
		this.changedBy = rs.getInt("changed_by");
		this.dateChanged =  rs.getTimestamp("date_changed") != null ? new java.util.Date( rs.getTimestamp("date_changed").getTime() ) : null;
		this.voided = rs.getShort("voided");
		this.voidedBy = rs.getInt("voided_by");
		this.dateVoided =  rs.getTimestamp("date_voided") != null ? new java.util.Date( rs.getTimestamp("date_voided").getTime() ) : null;
		this.voidReason = rs.getString("void_reason") != null ? rs.getString("void_reason").trim() : null;
			} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "gaac_member_id"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParams(){ 
 		Object[] params = {this.gaacId == 0 ? null : this.gaacId, this.memberId == 0 ? null : this.memberId, this.startDate, this.endDate, this.reasonLeavingType == 0 ? null : this.reasonLeavingType, this.description, this.leaving, this.restart, this.restartDate, this.creator == 0 ? null : this.creator, this.dateCreated, this.changedBy == 0 ? null : this.changedBy, this.dateChanged, this.voided, this.voidedBy == 0 ? null : this.voidedBy, this.dateVoided, this.voidReason, this.uuid};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.gaacId == 0 ? null : this.gaacId, this.memberId == 0 ? null : this.memberId, this.startDate, this.endDate, this.reasonLeavingType == 0 ? null : this.reasonLeavingType, this.description, this.leaving, this.restart, this.restartDate, this.creator == 0 ? null : this.creator, this.dateCreated, this.changedBy == 0 ? null : this.changedBy, this.dateChanged, this.voided, this.voidedBy == 0 ? null : this.voidedBy, this.dateVoided, this.voidReason, this.uuid, this.gaacMemberId};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQL(){ 
 		return "INSERT INTO gaac_member(gaac_id, member_id, start_date, end_date, reason_leaving_type, description, leaving, restart, restart_date, creator, date_created, changed_by, date_changed, voided, voided_by, date_voided, void_reason, uuid) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE gaac_member SET gaac_id = ?, member_id = ?, start_date = ?, end_date = ?, reason_leaving_type = ?, description = ?, leaving = ?, restart = ?, restart_date = ?, creator = ?, date_created = ?, changed_by = ?, date_changed = ?, voided = ?, voided_by = ?, date_voided = ?, void_reason = ?, uuid = ? WHERE gaac_member_id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.gaacId == 0 ? null : this.gaacId) + "," + (this.memberId == 0 ? null : this.memberId) + "," + (this.startDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(startDate)  +"\"" : null) + "," + (this.endDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(endDate)  +"\"" : null) + "," + (this.reasonLeavingType == 0 ? null : this.reasonLeavingType) + "," + (this.description != null ? "\""+description+"\"" : null) + "," + (this.leaving) + "," + (this.restart) + "," + (this.restartDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(restartDate)  +"\"" : null) + "," + (this.creator == 0 ? null : this.creator) + "," + (this.dateCreated != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateCreated)  +"\"" : null) + "," + (this.changedBy == 0 ? null : this.changedBy) + "," + (this.dateChanged != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateChanged)  +"\"" : null) + "," + (this.voided) + "," + (this.voidedBy == 0 ? null : this.voidedBy) + "," + (this.dateVoided != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateVoided)  +"\"" : null) + "," + (this.voidReason != null ? "\""+voidReason+"\"" : null) + "," + (this.uuid != null ? "\""+uuid+"\"" : null); 
	} 
 
	@Override
	public boolean isGeneratedFromSkeletonClass() {
		return false;
	}

	@Override
	public boolean hasParents() {
		if (this.gaacId != 0) return true;
		if (this.memberId != 0) return true;
		if (this.reasonLeavingType != 0) return true;
		if (this.changedBy != 0) return true;
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
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.sourcepkg.GaacVO.class, this.gaacId, false, conn); 
		this.gaacId = 0;
		if (parentOnDestination  != null) this.gaacId = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.sourcepkg.PatientVO.class, this.memberId, false, conn); 
		this.memberId = 0;
		if (parentOnDestination  != null) this.memberId = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.sourcepkg.GaacReasonLeavingTypeVO.class, this.reasonLeavingType, true, conn); 
		this.reasonLeavingType = 0;
		if (parentOnDestination  != null) this.reasonLeavingType = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.sourcepkg.UsersVO.class, this.changedBy, true, conn); 
		this.changedBy = 0;
		if (parentOnDestination  != null) this.changedBy = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.sourcepkg.UsersVO.class, this.creator, false, conn); 
		this.creator = 0;
		if (parentOnDestination  != null) this.creator = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.sourcepkg.UsersVO.class, this.voidedBy, true, conn); 
		this.voidedBy = 0;
		if (parentOnDestination  != null) this.voidedBy = parentOnDestination.getObjectId();
 
	}

	@Override
	public int getParentValue(String parentAttName) {		
		if (parentAttName.equals("gaacId")) return this.gaacId;		
		if (parentAttName.equals("memberId")) return this.memberId;		
		if (parentAttName.equals("reasonLeavingType")) return this.reasonLeavingType;		
		if (parentAttName.equals("changedBy")) return this.changedBy;		
		if (parentAttName.equals("creator")) return this.creator;		
		if (parentAttName.equals("voidedBy")) return this.voidedBy;

		throw new RuntimeException("No found parent for: " + parentAttName);	}


}