package org.openmrs.module.epts.etl.model.pojo.openmrs.community_data_extraction;

import org.openmrs.module.epts.etl.model.pojo.generic.*; 
 
import org.openmrs.module.epts.etl.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.epts.etl.utilities.AttDefinedElements; 
 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class CommunityArvViralLoadVO extends AbstractDatabaseObject implements DatabaseObject { 
	private Integer id;
	private Integer encounterId;
	private Integer obsId;
	private String obsUuid;
	private Double cv;
	private String cvQualit;
	private String cvComments;
	private java.util.Date cvDate;
	private String source;
 
	public CommunityArvViralLoadVO() { 
		this.metadata = false;
	} 
 
	public void setId(Integer id){ 
	 	this.id = id;
	}
 
	public Integer getId(){ 
		return this.id;
	}
 
	public void setEncounterId(Integer encounterId){ 
	 	this.encounterId = encounterId;
	}
 
	public Integer getEncounterId(){ 
		return this.encounterId;
	}
 
	public void setObsId(Integer obsId){ 
	 	this.obsId = obsId;
	}
 
	public Integer getObsId(){ 
		return this.obsId;
	}
 
	public void setObsUuid(String obsUuid){ 
	 	this.obsUuid = obsUuid;
	}
 
	public String getObsUuid(){ 
		return this.obsUuid;
	}
 
	public void setCv(Double cv){ 
	 	this.cv = cv;
	}
 
	public Double getCv(){ 
		return this.cv;
	}
 
	public void setCvQualit(String cvQualit){ 
	 	this.cvQualit = cvQualit;
	}
 
	public String getCvQualit(){ 
		return this.cvQualit;
	}
 
	public void setCvComments(String cvComments){ 
	 	this.cvComments = cvComments;
	}
 
	public String getCvComments(){ 
		return this.cvComments;
	}
 
	public void setCvDate(java.util.Date cvDate){ 
	 	this.cvDate = cvDate;
	}
 
	public java.util.Date getCvDate(){ 
		return this.cvDate;
	}
 
	public void setSource(String source){ 
	 	this.source = source;
	}


 
	public String getSource(){ 
		return this.source;
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
		if (rs.getObject("encounter_id") != null) this.encounterId = rs.getInt("encounter_id");
		if (rs.getObject("obs_id") != null) this.obsId = rs.getInt("obs_id");
		this.obsUuid = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("obs_uuid") != null ? rs.getString("obs_uuid").trim() : null);
		if (rs.getObject("cv") != null) this.cv = rs.getDouble("cv");
		this.cvQualit = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("cv_qualit") != null ? rs.getString("cv_qualit").trim() : null);
		this.cvComments = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("cv_comments") != null ? rs.getString("cv_comments").trim() : null);
		this.cvDate =  rs.getTimestamp("cv_date") != null ? new java.util.Date( rs.getTimestamp("cv_date").getTime() ) : null;
		this.source = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("source") != null ? rs.getString("source").trim() : null);
	} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "id"; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithoutObjectId(){ 
 		return "INSERT INTO community_arv_viral_load(encounter_id, obs_id, obs_uuid, cv, cv_qualit, cv_comments, cv_date, source) VALUES( ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithoutObjectId(){ 
 		Object[] params = {this.encounterId, this.obsId, this.obsUuid, this.cv, this.cvQualit, this.cvComments, this.cvDate, this.source};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithObjectId(){ 
 		return "INSERT INTO community_arv_viral_load(id, encounter_id, obs_id, obs_uuid, cv, cv_qualit, cv_comments, cv_date, source) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithObjectId(){ 
 		Object[] params = {this.id, this.encounterId, this.obsId, this.obsUuid, this.cv, this.cvQualit, this.cvComments, this.cvDate, this.source};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.encounterId, this.obsId, this.obsUuid, this.cv, this.cvQualit, this.cvComments, this.cvDate, this.source, this.id};		return params; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE community_arv_viral_load SET encounter_id = ?, obs_id = ?, obs_uuid = ?, cv = ?, cv_qualit = ?, cv_comments = ?, cv_date = ?, source = ? WHERE id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.encounterId) + "," + (this.obsId) + "," + (this.obsUuid != null ? "\""+ utilities.scapeQuotationMarks(obsUuid)  +"\"" : null) + "," + (this.cv) + "," + (this.cvQualit != null ? "\""+ utilities.scapeQuotationMarks(cvQualit)  +"\"" : null) + "," + (this.cvComments != null ? "\""+ utilities.scapeQuotationMarks(cvComments)  +"\"" : null) + "," + (this.cvDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(cvDate)  +"\"" : null) + "," + (this.source != null ? "\""+ utilities.scapeQuotationMarks(source)  +"\"" : null); 
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
		return "community_arv_viral_load";
	}


}