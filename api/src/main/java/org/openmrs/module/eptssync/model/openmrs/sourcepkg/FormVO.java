package org.openmrs.module.eptssync.model.openmrs.sourcepkg; 
 
import org.openmrs.module.eptssync.model.openmrs.generic.*; 
 
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.eptssync.utilities.db.conn.DBException; 
import org.openmrs.module.eptssync.exceptions.ParentNotYetMigratedException; 
 
import java.sql.Connection; 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class FormVO extends AbstractOpenMRSObject implements OpenMRSObject { 
	private int formId;
	private String name;
	private String version;
	private int build;
	private byte published;
	private String description;
	private int encounterType;
	private String template;
	private String xslt;
	private int creator;
	private java.util.Date dateCreated;
	private int changedBy;
	private java.util.Date dateChanged;
	private byte retired;
	private int retiredBy;
	private java.util.Date dateRetired;
	private String retiredReason;
	private String uuid;
 
	public FormVO() { 
		this.metadata = false;
	} 
 
	public void setFormId(int formId){ 
	 	this.formId = formId;
	}
 
	public int getFormId(){ 
		return this.formId;
	}
 
	public void setName(String name){ 
	 	this.name = name;
	}
 
	public String getName(){ 
		return this.name;
	}
 
	public void setVersion(String version){ 
	 	this.version = version;
	}
 
	public String getVersion(){ 
		return this.version;
	}
 
	public void setBuild(int build){ 
	 	this.build = build;
	}
 
	public int getBuild(){ 
		return this.build;
	}
 
	public void setPublished(byte published){ 
	 	this.published = published;
	}
 
	public byte getPublished(){ 
		return this.published;
	}
 
	public void setDescription(String description){ 
	 	this.description = description;
	}
 
	public String getDescription(){ 
		return this.description;
	}
 
	public void setEncounterType(int encounterType){ 
	 	this.encounterType = encounterType;
	}
 
	public int getEncounterType(){ 
		return this.encounterType;
	}
 
	public void setTemplate(String template){ 
	 	this.template = template;
	}
 
	public String getTemplate(){ 
		return this.template;
	}
 
	public void setXslt(String xslt){ 
	 	this.xslt = xslt;
	}
 
	public String getXslt(){ 
		return this.xslt;
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
 
	public void setRetired(byte retired){ 
	 	this.retired = retired;
	}
 
	public byte getRetired(){ 
		return this.retired;
	}
 
	public void setRetiredBy(int retiredBy){ 
	 	this.retiredBy = retiredBy;
	}
 
	public int getRetiredBy(){ 
		return this.retiredBy;
	}
 
	public void setDateRetired(java.util.Date dateRetired){ 
	 	this.dateRetired = dateRetired;
	}
 
	public java.util.Date getDateRetired(){ 
		return this.dateRetired;
	}
 
	public void setRetiredReason(String retiredReason){ 
	 	this.retiredReason = retiredReason;
	}
 
	public String getRetiredReason(){ 
		return this.retiredReason;
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
 		return this.formId; 
	} 
 
	public void setObjectId(int selfId){ 
		this.formId = selfId; 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		this.formId = rs.getInt("form_id");
		this.name = rs.getString("name") != null ? rs.getString("name").trim() : null;
		this.version = rs.getString("version") != null ? rs.getString("version").trim() : null;
		this.build = rs.getInt("build");
		this.published = rs.getByte("published");
		this.description = rs.getString("description") != null ? rs.getString("description").trim() : null;
		this.encounterType = rs.getInt("encounter_type");
		this.template = rs.getString("template") != null ? rs.getString("template").trim() : null;
		this.xslt = rs.getString("xslt") != null ? rs.getString("xslt").trim() : null;
		this.creator = rs.getInt("creator");
		this.dateCreated =  rs.getTimestamp("date_created") != null ? new java.util.Date( rs.getTimestamp("date_created").getTime() ) : null;
		this.changedBy = rs.getInt("changed_by");
		this.dateChanged =  rs.getTimestamp("date_changed") != null ? new java.util.Date( rs.getTimestamp("date_changed").getTime() ) : null;
		this.retired = rs.getByte("retired");
		this.retiredBy = rs.getInt("retired_by");
		this.dateRetired =  rs.getTimestamp("date_retired") != null ? new java.util.Date( rs.getTimestamp("date_retired").getTime() ) : null;
		this.retiredReason = rs.getString("retired_reason") != null ? rs.getString("retired_reason").trim() : null;
			} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "form_id"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParams(){ 
 		Object[] params = {this.name, this.version, this.build, this.published, this.description, this.encounterType == 0 ? null : this.encounterType, this.template, this.xslt, this.creator == 0 ? null : this.creator, this.dateCreated, this.changedBy == 0 ? null : this.changedBy, this.dateChanged, this.retired, this.retiredBy == 0 ? null : this.retiredBy, this.dateRetired, this.retiredReason, this.uuid};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.name, this.version, this.build, this.published, this.description, this.encounterType == 0 ? null : this.encounterType, this.template, this.xslt, this.creator == 0 ? null : this.creator, this.dateCreated, this.changedBy == 0 ? null : this.changedBy, this.dateChanged, this.retired, this.retiredBy == 0 ? null : this.retiredBy, this.dateRetired, this.retiredReason, this.uuid, this.formId};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQL(){ 
 		return "INSERT INTO form(name, version, build, published, description, encounter_type, template, xslt, creator, date_created, changed_by, date_changed, retired, retired_by, date_retired, retired_reason, uuid) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE form SET name = ?, version = ?, build = ?, published = ?, description = ?, encounter_type = ?, template = ?, xslt = ?, creator = ?, date_created = ?, changed_by = ?, date_changed = ?, retired = ?, retired_by = ?, date_retired = ?, retired_reason = ?, uuid = ? WHERE form_id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.name != null ? "\""+name+"\"" : null) + "," + (this.version != null ? "\""+version+"\"" : null) + "," + (this.build) + "," + (this.published) + "," + (this.description != null ? "\""+description+"\"" : null) + "," + (this.encounterType == 0 ? null : this.encounterType) + "," + (this.template != null ? "\""+template+"\"" : null) + "," + (this.xslt != null ? "\""+xslt+"\"" : null) + "," + (this.creator == 0 ? null : this.creator) + "," + (this.dateCreated != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateCreated)  +"\"" : null) + "," + (this.changedBy == 0 ? null : this.changedBy) + "," + (this.dateChanged != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateChanged)  +"\"" : null) + "," + (this.retired) + "," + (this.retiredBy == 0 ? null : this.retiredBy) + "," + (this.dateRetired != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateRetired)  +"\"" : null) + "," + (this.retiredReason != null ? "\""+retiredReason+"\"" : null) + "," + (this.uuid != null ? "\""+uuid+"\"" : null); 
	} 
 
	@Override
	public boolean isGeneratedFromSkeletonClass() {
		return false;
	}

	@Override
	public boolean hasParents() {
		if (this.encounterType != 0) return true;
		if (this.creator != 0) return true;
		if (this.changedBy != 0) return true;
		if (this.retiredBy != 0) return true;
		return false;
	}

	@Override
	public int retrieveSharedPKKey(Connection conn) throws ParentNotYetMigratedException, DBException {
		throw new RuntimeException("No PKSharedInfo defined!");	}

	@Override
	public void loadDestParentInfo(Connection conn) throws ParentNotYetMigratedException, DBException {
		OpenMRSObject parentOnDestination = null;
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.sourcepkg.EncounterTypeVO.class, this.encounterType, true, conn); 
		this.encounterType = 0;
		if (parentOnDestination  != null) this.encounterType = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.sourcepkg.UsersVO.class, this.creator, false, conn); 
		this.creator = 0;
		if (parentOnDestination  != null) this.creator = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.sourcepkg.UsersVO.class, this.changedBy, true, conn); 
		this.changedBy = 0;
		if (parentOnDestination  != null) this.changedBy = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.sourcepkg.UsersVO.class, this.retiredBy, true, conn); 
		this.retiredBy = 0;
		if (parentOnDestination  != null) this.retiredBy = parentOnDestination.getObjectId();
 
	}

	@Override
	public int getParentValue(String parentAttName) {		
		if (parentAttName.equals("encounterType")) return this.encounterType;		
		if (parentAttName.equals("creator")) return this.creator;		
		if (parentAttName.equals("changedBy")) return this.changedBy;		
		if (parentAttName.equals("retiredBy")) return this.retiredBy;

		throw new RuntimeException("No found parent for: " + parentAttName);	}


}