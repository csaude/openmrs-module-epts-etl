package org.openmrs.module.eptssync.model.openmrs.destinationpkg; 
 
import org.openmrs.module.eptssync.model.openmrs.generic.*; 
 
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.eptssync.utilities.db.conn.DBException; 
import org.openmrs.module.eptssync.exceptions.ParentNotYetMigratedException; 
 
import java.sql.Connection; 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class FormentryErrorVO extends AbstractOpenMRSObject implements OpenMRSObject { 
	private int formentryErrorId;
	private String formData;
	private String error;
	private String errorDetails;
	private int creator;
	private java.util.Date dateCreated;
 
	public FormentryErrorVO() { 
		this.metadata = false;
	} 
 
	public void setFormentryErrorId(int formentryErrorId){ 
	 	this.formentryErrorId = formentryErrorId;
	}
 
	public int getFormentryErrorId(){ 
		return this.formentryErrorId;
	}
 
	public void setFormData(String formData){ 
	 	this.formData = formData;
	}
 
	public String getFormData(){ 
		return this.formData;
	}
 
	public void setError(String error){ 
	 	this.error = error;
	}
 
	public String getError(){ 
		return this.error;
	}
 
	public void setErrorDetails(String errorDetails){ 
	 	this.errorDetails = errorDetails;
	}
 
	public String getErrorDetails(){ 
		return this.errorDetails;
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
 		return this.formentryErrorId; 
	} 
 
	public void setObjectId(int selfId){ 
		this.formentryErrorId = selfId; 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		this.formentryErrorId = rs.getInt("formentry_error_id");
		this.formData = rs.getString("form_data") != null ? rs.getString("form_data").trim() : null;
		this.error = rs.getString("error") != null ? rs.getString("error").trim() : null;
		this.errorDetails = rs.getString("error_details") != null ? rs.getString("error_details").trim() : null;
		this.creator = rs.getInt("creator");
			} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "formentry_error_id"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParams(){ 
 		Object[] params = {this.formData, this.error, this.errorDetails, this.creator == 0 ? null : this.creator, this.dateCreated};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.formData, this.error, this.errorDetails, this.creator == 0 ? null : this.creator, this.dateCreated, this.formentryErrorId};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQL(){ 
 		return "INSERT INTO formentry_error(form_data, error, error_details, creator, date_created) VALUES(?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE formentry_error SET form_data = ?, error = ?, error_details = ?, creator = ?, date_created = ? WHERE formentry_error_id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.formData != null ? "\""+formData+"\"" : null) + "," + (this.error != null ? "\""+error+"\"" : null) + "," + (this.errorDetails != null ? "\""+errorDetails+"\"" : null) + "," + (this.creator == 0 ? null : this.creator) + "," + (this.dateCreated != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateCreated)  +"\"" : null); 
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