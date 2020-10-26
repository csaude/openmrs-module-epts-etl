package org.openmrs.module.eptssync.model.openmrs.sourcepkg; 
 
import org.openmrs.module.eptssync.model.openmrs.generic.*; 
 
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.eptssync.utilities.db.conn.DBException; 
import org.openmrs.module.eptssync.utilities.AttDefinedElements; 
import org.openmrs.module.eptssync.exceptions.ParentNotYetMigratedException; 
 
import java.sql.Connection; 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class LogicTokenRegistrationVO extends AbstractOpenMRSObject implements OpenMRSObject { 
	private int tokenRegistrationId;
	private int creator;
	private java.util.Date dateCreated;
	private int changedBy;
	private java.util.Date dateChanged;
	private String token;
	private String providerClassName;
	private String providerToken;
	private String configuration;
	private String uuid;
 
	public LogicTokenRegistrationVO() { 
		this.metadata = false;
	} 
 
	public void setTokenRegistrationId(int tokenRegistrationId){ 
	 	this.tokenRegistrationId = tokenRegistrationId;
	}
 
	public int getTokenRegistrationId(){ 
		return this.tokenRegistrationId;
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
 
	public void setToken(String token){ 
	 	this.token = token;
	}
 
	public String getToken(){ 
		return this.token;
	}
 
	public void setProviderClassName(String providerClassName){ 
	 	this.providerClassName = providerClassName;
	}
 
	public String getProviderClassName(){ 
		return this.providerClassName;
	}
 
	public void setProviderToken(String providerToken){ 
	 	this.providerToken = providerToken;
	}
 
	public String getProviderToken(){ 
		return this.providerToken;
	}
 
	public void setConfiguration(String configuration){ 
	 	this.configuration = configuration;
	}
 
	public String getConfiguration(){ 
		return this.configuration;
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
 		return this.tokenRegistrationId; 
	} 
 
	public void setObjectId(int selfId){ 
		this.tokenRegistrationId = selfId; 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		this.tokenRegistrationId = rs.getInt("token_registration_id");
		this.creator = rs.getInt("creator");
		this.dateCreated =  rs.getTimestamp("date_created") != null ? new java.util.Date( rs.getTimestamp("date_created").getTime() ) : null;
		this.changedBy = rs.getInt("changed_by");
		this.dateChanged =  rs.getTimestamp("date_changed") != null ? new java.util.Date( rs.getTimestamp("date_changed").getTime() ) : null;
		this.token = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("token") != null ? rs.getString("token").trim() : null);
		this.providerClassName = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("provider_class_name") != null ? rs.getString("provider_class_name").trim() : null);
		this.providerToken = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("provider_token") != null ? rs.getString("provider_token").trim() : null);
		this.configuration = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("configuration") != null ? rs.getString("configuration").trim() : null);
		this.uuid = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("uuid") != null ? rs.getString("uuid").trim() : null);
	} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "token_registration_id"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParams(){ 
 		Object[] params = {this.creator == 0 ? null : this.creator, this.dateCreated, this.changedBy == 0 ? null : this.changedBy, this.dateChanged, this.token, this.providerClassName, this.providerToken, this.configuration, this.uuid};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.creator == 0 ? null : this.creator, this.dateCreated, this.changedBy == 0 ? null : this.changedBy, this.dateChanged, this.token, this.providerClassName, this.providerToken, this.configuration, this.uuid, this.tokenRegistrationId};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQL(){ 
 		return "INSERT INTO logic_token_registration(creator, date_created, changed_by, date_changed, token, provider_class_name, provider_token, configuration, uuid) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE logic_token_registration SET creator = ?, date_created = ?, changed_by = ?, date_changed = ?, token = ?, provider_class_name = ?, provider_token = ?, configuration = ?, uuid = ? WHERE token_registration_id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.creator == 0 ? null : this.creator) + "," + (this.dateCreated != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateCreated)  +"\"" : null) + "," + (this.changedBy == 0 ? null : this.changedBy) + "," + (this.dateChanged != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateChanged)  +"\"" : null) + "," + (this.token != null ? "\""+ utilities.scapeQuotationMarks(token)  +"\"" : null) + "," + (this.providerClassName != null ? "\""+ utilities.scapeQuotationMarks(providerClassName)  +"\"" : null) + "," + (this.providerToken != null ? "\""+ utilities.scapeQuotationMarks(providerToken)  +"\"" : null) + "," + (this.configuration != null ? "\""+ utilities.scapeQuotationMarks(configuration)  +"\"" : null) + "," + (this.uuid != null ? "\""+ utilities.scapeQuotationMarks(uuid)  +"\"" : null); 
	} 
 
	@Override
	public boolean isGeneratedFromSkeletonClass() {
		return false;
	}

	@Override
	public boolean hasParents() {
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
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.sourcepkg.UsersVO.class, this.changedBy, true, conn); 
		this.changedBy = 0;
		if (parentOnDestination  != null) this.changedBy = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.sourcepkg.UsersVO.class, this.creator, false, conn); 
		this.creator = 0;
		if (parentOnDestination  != null) this.creator = parentOnDestination.getObjectId();
 
	}

	@Override
	public int getParentValue(String parentAttName) {		
		if (parentAttName.equals("changedBy")) return this.changedBy;		
		if (parentAttName.equals("creator")) return this.creator;

		throw new RuntimeException("No found parent for: " + parentAttName);	}


}