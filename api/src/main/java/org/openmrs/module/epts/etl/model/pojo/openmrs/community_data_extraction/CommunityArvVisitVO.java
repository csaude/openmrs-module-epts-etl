package org.openmrs.module.epts.etl.model.pojo.openmrs.community_data_extraction;

import org.openmrs.module.epts.etl.model.pojo.generic.*; 
 
import org.openmrs.module.epts.etl.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.epts.etl.utilities.AttDefinedElements; 
 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class CommunityArvVisitVO extends AbstractDatabaseObject implements DatabaseObject { 
	private Integer id;
	private Integer patientId;
	private java.util.Date visitDate;
	private java.util.Date nextVisitDate;
	private String source;
	private Integer encounter;
 
	public CommunityArvVisitVO() { 
		this.metadata = false;
	} 
 
	public void setId(Integer id){ 
	 	this.id = id;
	}
 
	public Integer getId(){ 
		return this.id;
	}
 
	public void setPatientId(Integer patientId){ 
	 	this.patientId = patientId;
	}
 
	public Integer getPatientId(){ 
		return this.patientId;
	}
 
	public void setVisitDate(java.util.Date visitDate){ 
	 	this.visitDate = visitDate;
	}
 
	public java.util.Date getVisitDate(){ 
		return this.visitDate;
	}
 
	public void setNextVisitDate(java.util.Date nextVisitDate){ 
	 	this.nextVisitDate = nextVisitDate;
	}
 
	public java.util.Date getNextVisitDate(){ 
		return this.nextVisitDate;
	}
 
	public void setSource(String source){ 
	 	this.source = source;
	}
 
	public String getSource(){ 
		return this.source;
	}
 
	public void setEncounter(Integer encounter){ 
	 	this.encounter = encounter;
	}


 
	public Integer getEncounter(){ 
		return this.encounter;
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
		if (rs.getObject("patient_id") != null) this.patientId = rs.getInt("patient_id");
		this.visitDate =  rs.getTimestamp("visit_date") != null ? new java.util.Date( rs.getTimestamp("visit_date").getTime() ) : null;
		this.nextVisitDate =  rs.getTimestamp("next_visit_date") != null ? new java.util.Date( rs.getTimestamp("next_visit_date").getTime() ) : null;
		this.source = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("source") != null ? rs.getString("source").trim() : null);
		if (rs.getObject("encounter") != null) this.encounter = rs.getInt("encounter");
	} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "id"; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithoutObjectId(){ 
 		return "INSERT INTO community_arv_visit(patient_id, visit_date, next_visit_date, source, encounter) VALUES( ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithoutObjectId(){ 
 		Object[] params = {this.patientId, this.visitDate, this.nextVisitDate, this.source, this.encounter};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithObjectId(){ 
 		return "INSERT INTO community_arv_visit(id, patient_id, visit_date, next_visit_date, source, encounter) VALUES(?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithObjectId(){ 
 		Object[] params = {this.id, this.patientId, this.visitDate, this.nextVisitDate, this.source, this.encounter};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.patientId, this.visitDate, this.nextVisitDate, this.source, this.encounter, this.id};		return params; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE community_arv_visit SET patient_id = ?, visit_date = ?, next_visit_date = ?, source = ?, encounter = ? WHERE id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.patientId) + "," + (this.visitDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(visitDate)  +"\"" : null) + "," + (this.nextVisitDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(nextVisitDate)  +"\"" : null) + "," + (this.source != null ? "\""+ utilities.scapeQuotationMarks(source)  +"\"" : null) + "," + (this.encounter); 
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
		return "community_arv_visit";
	}


}