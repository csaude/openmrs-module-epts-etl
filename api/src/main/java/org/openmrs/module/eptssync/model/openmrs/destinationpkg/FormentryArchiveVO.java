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
 
public class FormentryArchiveVO extends AbstractOpenMRSObject implements OpenMRSObject { 
	private int formentryArchiveId;
	private String formData;
	private java.util.Date dateCreated;
	private int creator;
 
	public FormentryArchiveVO() { 
		this.metadata = false;
	} 
 
	public void setFormentryArchiveId(int formentryArchiveId){ 
	 	this.formentryArchiveId = formentryArchiveId;
	}
 
	public int getFormentryArchiveId(){ 
		return this.formentryArchiveId;
	}
 
	public void setFormData(String formData){ 
	 	this.formData = formData;
	}
 
	public String getFormData(){ 
		return this.formData;
	}
 
	public void setDateCreated(java.util.Date dateCreated){ 
	 	this.dateCreated = dateCreated;
	}
 
	public java.util.Date getDateCreated(){ 
		return this.dateCreated;
	}
 
	public void setCreator(int creator){ 
	 	this.creator = creator;
	}


 
	public int getCreator(){ 
		return this.creator;
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
 		return this.formentryArchiveId; 
	} 
 
	public void setObjectId(int selfId){ 
		this.formentryArchiveId = selfId; 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		this.formentryArchiveId = rs.getInt("formentry_archive_id");
		this.formData = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("form_data") != null ? rs.getString("form_data").trim() : null);
		this.dateCreated =  rs.getTimestamp("date_created") != null ? new java.util.Date( rs.getTimestamp("date_created").getTime() ) : null;
		this.creator = rs.getInt("creator");
	} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "formentry_archive_id"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParams(){ 
 		Object[] params = {this.formData, this.dateCreated, this.creator == 0 ? null : this.creator};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.formData, this.dateCreated, this.creator == 0 ? null : this.creator, this.formentryArchiveId};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQL(){ 
 		return "INSERT INTO formentry_archive(form_data, date_created, creator) VALUES(?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE formentry_archive SET form_data = ?, date_created = ?, creator = ? WHERE formentry_archive_id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.formData != null ? "\""+ utilities.scapeQuotationMarks(formData)  +"\"" : null) + "," + (this.dateCreated != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateCreated)  +"\"" : null) + "," + (this.creator == 0 ? null : this.creator); 
	} 
 
	@Override
	public boolean isGeneratedFromSkeletonClass() {
		return false;
	}

	@Override
	public boolean hasParents() {
		if (this.creator != 0) return true;
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
 
	}

	@Override
	public int getParentValue(String parentAttName) {		
		if (parentAttName.equals("creator")) return this.creator;

		throw new RuntimeException("No found parent for: " + parentAttName);	}


}