package org.openmrs.module.epts.etl.model.pojo.openmrs._default;

import org.openmrs.module.epts.etl.model.pojo.generic.*; 
 
import org.openmrs.module.epts.etl.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.epts.etl.utilities.AttDefinedElements; 
 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class ClinicalsummaryUsageReportVO extends AbstractDatabaseObject implements DatabaseObject { 
	private Integer id;
	private String report;
	private String healthFacility;
	private String userName;
	private String confidentialTerms;
	private String appVersion;
	private java.util.Date dateOpened;
	private Integer creator;
	private Byte voided;
	private Integer voidedBy;
	private java.util.Date dateVoided;
	private String voidReason;
	private Integer changedBy;
 
	public ClinicalsummaryUsageReportVO() { 
		this.metadata = false;
	} 
 
	public void setId(Integer id){ 
	 	this.id = id;
	}
 
	public Integer getId(){ 
		return this.id;
	}
 
	public void setReport(String report){ 
	 	this.report = report;
	}
 
	public String getReport(){ 
		return this.report;
	}
 
	public void setHealthFacility(String healthFacility){ 
	 	this.healthFacility = healthFacility;
	}
 
	public String getHealthFacility(){ 
		return this.healthFacility;
	}
 
	public void setUserName(String userName){ 
	 	this.userName = userName;
	}
 
	public String getUserName(){ 
		return this.userName;
	}
 
	public void setConfidentialTerms(String confidentialTerms){ 
	 	this.confidentialTerms = confidentialTerms;
	}
 
	public String getConfidentialTerms(){ 
		return this.confidentialTerms;
	}
 
	public void setAppVersion(String appVersion){ 
	 	this.appVersion = appVersion;
	}
 
	public String getAppVersion(){ 
		return this.appVersion;
	}
 
	public void setDateOpened(java.util.Date dateOpened){ 
	 	this.dateOpened = dateOpened;
	}
 
	public java.util.Date getDateOpened(){ 
		return this.dateOpened;
	}
 
	public void setCreator(Integer creator){ 
	 	this.creator = creator;
	}
 
	public Integer getCreator(){ 
		return this.creator;
	}
 
	public void setVoided(Byte voided){ 
	 	this.voided = voided;
	}
 
	public Byte getVoided(){ 
		return this.voided;
	}
 
	public void setVoidedBy(Integer voidedBy){ 
	 	this.voidedBy = voidedBy;
	}
 
	public Integer getVoidedBy(){ 
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
 
	public void setChangedBy(Integer changedBy){ 
	 	this.changedBy = changedBy;
	}
 
	public Integer getChangedBy(){ 
		return this.changedBy;
	}
 

 
	public Integer getObjectId() { 
 		return this.id; 
	} 
 
	public void setObjectId(Integer selfId){ 
		this.id = selfId; 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		super.load(rs);
		if (rs.getObject("id") != null) this.id = rs.getInt("id");
		this.report = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("report") != null ? rs.getString("report").trim() : null);
		this.healthFacility = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("health_facility") != null ? rs.getString("health_facility").trim() : null);
		this.userName = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("user_name") != null ? rs.getString("user_name").trim() : null);
		this.confidentialTerms = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("confidential_terms") != null ? rs.getString("confidential_terms").trim() : null);
		this.appVersion = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("app_version") != null ? rs.getString("app_version").trim() : null);
		this.dateOpened =  rs.getTimestamp("date_opened") != null ? new java.util.Date( rs.getTimestamp("date_opened").getTime() ) : null;
		if (rs.getObject("creator") != null) this.creator = rs.getInt("creator");
		this.dateCreated =  rs.getTimestamp("date_created") != null ? new java.util.Date( rs.getTimestamp("date_created").getTime() ) : null;
		this.voided = rs.getByte("voided");
		if (rs.getObject("voided_by") != null) this.voidedBy = rs.getInt("voided_by");
		this.dateVoided =  rs.getTimestamp("date_voided") != null ? new java.util.Date( rs.getTimestamp("date_voided").getTime() ) : null;
		this.voidReason = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("void_reason") != null ? rs.getString("void_reason").trim() : null);
		if (rs.getObject("changed_by") != null) this.changedBy = rs.getInt("changed_by");
		this.dateChanged =  rs.getTimestamp("date_changed") != null ? new java.util.Date( rs.getTimestamp("date_changed").getTime() ) : null;
		this.uuid = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("uuid") != null ? rs.getString("uuid").trim() : null);
	} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "id"; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithoutObjectId(){ 
 		return "INSERT INTO clinicalsummary_usage_report(report, health_facility, user_name, confidential_terms, app_version, date_opened, creator, date_created, voided, voided_by, date_voided, void_reason, changed_by, date_changed, uuid) VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithoutObjectId(){ 
 		Object[] params = {this.report, this.healthFacility, this.userName, this.confidentialTerms, this.appVersion, this.dateOpened, this.creator, this.dateCreated, this.voided, this.voidedBy, this.dateVoided, this.voidReason, this.changedBy, this.dateChanged, this.uuid};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithObjectId(){ 
 		return "INSERT INTO clinicalsummary_usage_report(id, report, health_facility, user_name, confidential_terms, app_version, date_opened, creator, date_created, voided, voided_by, date_voided, void_reason, changed_by, date_changed, uuid) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithObjectId(){ 
 		Object[] params = {this.id, this.report, this.healthFacility, this.userName, this.confidentialTerms, this.appVersion, this.dateOpened, this.creator, this.dateCreated, this.voided, this.voidedBy, this.dateVoided, this.voidReason, this.changedBy, this.dateChanged, this.uuid};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.report, this.healthFacility, this.userName, this.confidentialTerms, this.appVersion, this.dateOpened, this.creator, this.dateCreated, this.voided, this.voidedBy, this.dateVoided, this.voidReason, this.changedBy, this.dateChanged, this.uuid, this.id};		return params; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE clinicalsummary_usage_report SET report = ?, health_facility = ?, user_name = ?, confidential_terms = ?, app_version = ?, date_opened = ?, creator = ?, date_created = ?, voided = ?, voided_by = ?, date_voided = ?, void_reason = ?, changed_by = ?, date_changed = ?, uuid = ? WHERE id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.report != null ? "\""+ utilities.scapeQuotationMarks(report)  +"\"" : null) + "," + (this.healthFacility != null ? "\""+ utilities.scapeQuotationMarks(healthFacility)  +"\"" : null) + "," + (this.userName != null ? "\""+ utilities.scapeQuotationMarks(userName)  +"\"" : null) + "," + (this.confidentialTerms != null ? "\""+ utilities.scapeQuotationMarks(confidentialTerms)  +"\"" : null) + "," + (this.appVersion != null ? "\""+ utilities.scapeQuotationMarks(appVersion)  +"\"" : null) + "," + (this.dateOpened != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateOpened)  +"\"" : null) + "," + (this.creator) + "," + (this.dateCreated != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateCreated)  +"\"" : null) + "," + (this.voided) + "," + (this.voidedBy) + "," + (this.dateVoided != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateVoided)  +"\"" : null) + "," + (this.voidReason != null ? "\""+ utilities.scapeQuotationMarks(voidReason)  +"\"" : null) + "," + (this.changedBy) + "," + (this.dateChanged != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateChanged)  +"\"" : null) + "," + (this.uuid != null ? "\""+ utilities.scapeQuotationMarks(uuid)  +"\"" : null); 
	} 
 
	@Override
	public boolean hasParents() {
		return false;
	}

	@Override
	public Integer getParentValue(String parentAttName) {

		throw new RuntimeException("No found parent for: " + parentAttName);	}

	@Override
	public void changeParentValue(String parentAttName, DatabaseObject newParent) {

		throw new RuntimeException("No found parent for: " + parentAttName);
	}

	@Override
	public void setParentToNull(String parentAttName) {

		throw new RuntimeException("No found parent for: " + parentAttName);
	}

	@Override
	public String generateTableName() {
		return "clinicalsummary_usage_report";
	}


}