package org.openmrs.module.epts.etl.model.pojo.openmrs._default._query_result;

import org.openmrs.module.epts.etl.model.pojo.generic.*; 
 
import org.openmrs.module.epts.etl.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.epts.etl.utilities.AttDefinedElements; 
 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class ArvVisitInfoQueryResultVO extends AbstractDatabaseObject implements DatabaseObject { 
	private String source;
	private java.util.Date nextVisitDate;
 
	public ArvVisitInfoQueryResultVO() { 
		this.metadata = false;
	} 
 
	public void setSource(String source){ 
	 	this.source = source;
	}
 
	public String getSource(){ 
		return this.source;
	}
 
	public void setNextVisitDate(java.util.Date nextVisitDate){ 
	 	this.nextVisitDate = nextVisitDate;
	}


 
	public java.util.Date getNextVisitDate(){ 
		return this.nextVisitDate;
	}
 
	public Integer getObjectId() { 
 		return 0; 
	} 
 
	public void setObjectId(Integer selfId){ 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		super.load(rs);
		this.source = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("source") != null ? rs.getString("source").trim() : null);
		this.nextVisitDate =  rs.getTimestamp("next_visit_date") != null ? new java.util.Date( rs.getTimestamp("next_visit_date").getTime() ) : null;
	} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return null; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithoutObjectId(){ 
 		return "INSERT INTO arv_visit_info(source, next_visit_date) VALUES( ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithoutObjectId(){ 
 		Object[] params = {this.source, this.nextVisitDate};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithObjectId(){ 
 		return "INSERT INTO arv_visit_info(source, next_visit_date) VALUES( ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithObjectId(){ 
 		Object[] params = {this.source, this.nextVisitDate};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.source, this.nextVisitDate, null};		return params; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE arv_visit_info SET source = ?, next_visit_date = ? WHERE null = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.source != null ? "\""+ utilities.scapeQuotationMarks(source)  +"\"" : null) + "," + (this.nextVisitDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(nextVisitDate)  +"\"" : null); 
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
		return "arv_visit_info";
	}


}