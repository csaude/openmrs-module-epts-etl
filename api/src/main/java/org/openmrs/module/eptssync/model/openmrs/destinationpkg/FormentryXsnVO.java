package org.openmrs.module.eptssync.model.openmrs.destinationpkg; 
 
import org.openmrs.module.eptssync.model.openmrs.generic.*; 
 
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.eptssync.utilities.db.conn.DBException; 
import org.openmrs.module.eptssync.exceptions.ParentNotYetMigratedException; 
 
import java.sql.Connection; 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class FormentryXsnVO extends AbstractOpenMRSObject implements OpenMRSObject { 
	private int formentryXsnId;
	private int formId;
	private byte[] xsnData;
	private int creator;
	private java.util.Date dateCreated;
	private int archived;
	private int archivedBy;
	private java.util.Date dateArchived;
	private String uuid;
 
	public FormentryXsnVO() { 
		this.metadata = false;
	} 
 
	public void setFormentryXsnId(int formentryXsnId){ 
	 	this.formentryXsnId = formentryXsnId;
	}
 
	public int getFormentryXsnId(){ 
		return this.formentryXsnId;
	}
 
	public void setFormId(int formId){ 
	 	this.formId = formId;
	}
 
	public int getFormId(){ 
		return this.formId;
	}
 
	public void setXsnData(byte[] xsnData){ 
	 	this.xsnData = xsnData;
	}
 
	public byte[] getXsnData(){ 
		return this.xsnData;
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
 
	public void setArchived(int archived){ 
	 	this.archived = archived;
	}
 
	public int getArchived(){ 
		return this.archived;
	}
 
	public void setArchivedBy(int archivedBy){ 
	 	this.archivedBy = archivedBy;
	}
 
	public int getArchivedBy(){ 
		return this.archivedBy;
	}
 
	public void setDateArchived(java.util.Date dateArchived){ 
	 	this.dateArchived = dateArchived;
	}
 
	public java.util.Date getDateArchived(){ 
		return this.dateArchived;
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
 		return this.formentryXsnId; 
	} 
 
	public void setObjectId(int selfId){ 
		this.formentryXsnId = selfId; 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		this.formentryXsnId = rs.getInt("formentry_xsn_id");
		this.formId = rs.getInt("form_id");
		this.xsnData = rs.getBytes("xsn_data");
		this.creator = rs.getInt("creator");
		this.dateCreated =  rs.getTimestamp("date_created") != null ? new java.util.Date( rs.getTimestamp("date_created").getTime() ) : null;
		this.archived = rs.getInt("archived");
		this.archivedBy = rs.getInt("archived_by");
		this.dateArchived =  rs.getTimestamp("date_archived") != null ? new java.util.Date( rs.getTimestamp("date_archived").getTime() ) : null;
			} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "formentry_xsn_id"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParams(){ 
 		Object[] params = {this.formId == 0 ? null : this.formId, this.xsnData, this.creator == 0 ? null : this.creator, this.dateCreated, this.archived, this.archivedBy == 0 ? null : this.archivedBy, this.dateArchived, this.uuid};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.formId == 0 ? null : this.formId, this.xsnData, this.creator == 0 ? null : this.creator, this.dateCreated, this.archived, this.archivedBy == 0 ? null : this.archivedBy, this.dateArchived, this.uuid, this.formentryXsnId};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQL(){ 
 		return "INSERT INTO formentry_xsn(form_id, xsn_data, creator, date_created, archived, archived_by, date_archived, uuid) VALUES(?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE formentry_xsn SET form_id = ?, xsn_data = ?, creator = ?, date_created = ?, archived = ?, archived_by = ?, date_archived = ?, uuid = ? WHERE formentry_xsn_id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.formId == 0 ? null : this.formId) + "," + (this.xsnData != null ? "\""+xsnData+"\"" : null) + "," + (this.creator == 0 ? null : this.creator) + "," + (this.dateCreated != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateCreated)  +"\"" : null) + "," + (this.archived) + "," + (this.archivedBy == 0 ? null : this.archivedBy) + "," + (this.dateArchived != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateArchived)  +"\"" : null) + "," + (this.uuid != null ? "\""+uuid+"\"" : null); 
	} 
 
	@Override
	public boolean isGeneratedFromSkeletonClass() {
		return false;
	}

	@Override
	public boolean hasParents() {
		if (this.formId != 0) return true;
		if (this.archivedBy != 0) return true;
		if (this.creator != 0) return true;
		return false;
	}

	@Override
	public int retrieveSharedPKKey(Connection conn) throws ParentNotYetMigratedException, DBException {
		throw new RuntimeException("No PKSharedInfo defined!");	}

	@Override
	public void loadDestParentInfo(Connection conn) throws ParentNotYetMigratedException, DBException {
		OpenMRSObject parentOnDestination = null;
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.destinationpkg.FormVO.class, this.formId, false, conn); 
		this.formId = 0;
		if (parentOnDestination  != null) this.formId = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.destinationpkg.UsersVO.class, this.archivedBy, true, conn); 
		this.archivedBy = 0;
		if (parentOnDestination  != null) this.archivedBy = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.destinationpkg.UsersVO.class, this.creator, false, conn); 
		this.creator = 0;
		if (parentOnDestination  != null) this.creator = parentOnDestination.getObjectId();
 
	}

	@Override
	public int getParentValue(String parentAttName) {		
		if (parentAttName.equals("formId")) return this.formId;		
		if (parentAttName.equals("archivedBy")) return this.archivedBy;		
		if (parentAttName.equals("creator")) return this.creator;

		throw new RuntimeException("No found parent for: " + parentAttName);	}


}