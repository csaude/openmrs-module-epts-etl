package org.openmrs.module.epts.etl.model.pojo.openmrs._default._query_result;

import org.openmrs.module.epts.etl.model.pojo.generic.*; 
 
import org.openmrs.module.epts.etl.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.epts.etl.utilities.AttDefinedElements; 
 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class ArvViralLoadInfoQueryResultVO extends AbstractDatabaseObject implements DatabaseObject { 
	private double cv;
	private String cvQualit;
	private String cvComments;
	private java.util.Date cvDate;
	private String source;
 
	public ArvViralLoadInfoQueryResultVO() { 
		this.metadata = false;
	} 
 
	public void setCv(double cv){ 
	 	this.cv = cv;
	}
 
	public double getCv(){ 
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
 		return 0; 
	} 
 
	public void setObjectId(Integer selfId){ 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		super.load(rs);
		this.cv = rs.getDouble("cv");
		this.cvQualit = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("cv_qualit") != null ? rs.getString("cv_qualit").trim() : null);
		this.cvComments = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("cv_comments") != null ? rs.getString("cv_comments").trim() : null);
		this.cvDate =  rs.getTimestamp("cv_date") != null ? new java.util.Date( rs.getTimestamp("cv_date").getTime() ) : null;
		this.source = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("source") != null ? rs.getString("source").trim() : null);
	} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return null; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithoutObjectId(){ 
 		return "INSERT INTO arv_viral_load_info(cv, cv_qualit, cv_comments, cv_date, source) VALUES( ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithoutObjectId(){ 
 		Object[] params = {this.cv, this.cvQualit, this.cvComments, this.cvDate, this.source};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithObjectId(){ 
 		return "INSERT INTO arv_viral_load_info(cv, cv_qualit, cv_comments, cv_date, source) VALUES( ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithObjectId(){ 
 		Object[] params = {this.cv, this.cvQualit, this.cvComments, this.cvDate, this.source};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.cv, this.cvQualit, this.cvComments, this.cvDate, this.source, null};		return params; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE arv_viral_load_info SET cv = ?, cv_qualit = ?, cv_comments = ?, cv_date = ?, source = ? WHERE null = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.cv) + "," + (this.cvQualit != null ? "\""+ utilities.scapeQuotationMarks(cvQualit)  +"\"" : null) + "," + (this.cvComments != null ? "\""+ utilities.scapeQuotationMarks(cvComments)  +"\"" : null) + "," + (this.cvDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(cvDate)  +"\"" : null) + "," + (this.source != null ? "\""+ utilities.scapeQuotationMarks(source)  +"\"" : null); 
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
		return "arv_viral_load_info";
	}


}