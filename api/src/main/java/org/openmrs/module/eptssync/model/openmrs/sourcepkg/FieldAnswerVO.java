package org.openmrs.module.eptssync.model.openmrs.sourcepkg; 
 
import org.openmrs.module.eptssync.model.openmrs.generic.*; 
 
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.eptssync.utilities.db.conn.DBException; 
import org.openmrs.module.eptssync.exceptions.ParentNotYetMigratedException; 
 
import java.sql.Connection; 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class FieldAnswerVO extends AbstractOpenMRSObject implements OpenMRSObject { 
	private int fieldId;
	private int answerId;
	private int creator;
	private java.util.Date dateCreated;
	private String uuid;
 
	public FieldAnswerVO() { 
		this.metadata = false;
	} 
 
	public void setFieldId(int fieldId){ 
	 	this.fieldId = fieldId;
	}
 
	public int getFieldId(){ 
		return this.fieldId;
	}
 
	public void setAnswerId(int answerId){ 
	 	this.answerId = answerId;
	}
 
	public int getAnswerId(){ 
		return this.answerId;
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
 		return this.answerId; 
	} 
 
	public void setObjectId(int selfId){ 
		this.answerId = selfId; 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		this.fieldId = rs.getInt("field_id");
		this.answerId = rs.getInt("answer_id");
		this.creator = rs.getInt("creator");
		this.dateCreated =  rs.getTimestamp("date_created") != null ? new java.util.Date( rs.getTimestamp("date_created").getTime() ) : null;
			} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "answer_id"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParams(){ 
 		Object[] params = {this.fieldId == 0 ? null : this.fieldId, this.creator == 0 ? null : this.creator, this.dateCreated, this.uuid};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.fieldId == 0 ? null : this.fieldId, this.creator == 0 ? null : this.creator, this.dateCreated, this.uuid, this.answerId};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQL(){ 
 		return "INSERT INTO field_answer(field_id, creator, date_created, uuid) VALUES(?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE field_answer SET field_id = ?, creator = ?, date_created = ?, uuid = ? WHERE answer_id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.fieldId == 0 ? null : this.fieldId) + "," + (this.creator == 0 ? null : this.creator) + "," + (this.dateCreated != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateCreated)  +"\"" : null) + "," + (this.uuid != null ? "\""+uuid+"\"" : null); 
	} 
 
	@Override
	public boolean isGeneratedFromSkeletonClass() {
		return false;
	}

	@Override
	public boolean hasParents() {
		if (this.fieldId != 0) return true;
		if (this.answerId != 0) return true;
		if (this.creator != 0) return true;
		return false;
	}

	@Override
	public int retrieveSharedPKKey(Connection conn) throws ParentNotYetMigratedException, DBException {
		throw new RuntimeException("No PKSharedInfo defined!");	}

	@Override
	public void loadDestParentInfo(Connection conn) throws ParentNotYetMigratedException, DBException {
		OpenMRSObject parentOnDestination = null;
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.sourcepkg.FieldVO.class, this.fieldId, false, conn); 
		this.fieldId = 0;
		if (parentOnDestination  != null) this.fieldId = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.sourcepkg.ConceptVO.class, this.answerId, false, conn); 
		this.answerId = 0;
		if (parentOnDestination  != null) this.answerId = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.sourcepkg.UsersVO.class, this.creator, false, conn); 
		this.creator = 0;
		if (parentOnDestination  != null) this.creator = parentOnDestination.getObjectId();
 
	}

	@Override
	public int getParentValue(String parentAttName) {		
		if (parentAttName.equals("fieldId")) return this.fieldId;		
		if (parentAttName.equals("answerId")) return this.answerId;		
		if (parentAttName.equals("creator")) return this.creator;

		throw new RuntimeException("No found parent for: " + parentAttName);	}


}