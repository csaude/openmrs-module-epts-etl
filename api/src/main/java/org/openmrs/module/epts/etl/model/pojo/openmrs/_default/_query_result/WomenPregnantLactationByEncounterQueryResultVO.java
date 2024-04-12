package org.openmrs.module.epts.etl.model.pojo.openmrs._default._query_result;

import org.openmrs.module.epts.etl.model.pojo.generic.*; 
 
import org.openmrs.module.epts.etl.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.epts.etl.utilities.AttDefinedElements; 
 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class WomenPregnantLactationByEncounterQueryResultVO extends AbstractDatabaseObject implements DatabaseObject { 
	private Integer patientId;
	private java.util.Date dataGravida;
	private String status;
	private String source;
 
	public WomenPregnantLactationByEncounterQueryResultVO() { 
		this.metadata = false;
	} 
 
	public void setPatientId(Integer patientId){ 
	 	this.patientId = patientId;
	}
 
	public Integer getPatientId(){ 
		return this.patientId;
	}
 
	public void setDataGravida(java.util.Date dataGravida){ 
	 	this.dataGravida = dataGravida;
	}
 
	public java.util.Date getDataGravida(){ 
		return this.dataGravida;
	}
 
	public void setStatus(String status){ 
	 	this.status = status;
	}
 
	public String getStatus(){ 
		return this.status;
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
		this.dataGravida =  rs.getTimestamp("data_gravida") != null ? new java.util.Date( rs.getTimestamp("data_gravida").getTime() ) : null;
		this.status = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("status") != null ? rs.getString("status").trim() : null);
		this.source = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("source") != null ? rs.getString("source").trim() : null);
	} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return null; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithoutObjectId(){ 
 		return "INSERT INTO women_pregnant_lactation_by_encounter(patient_id, data_gravida, status, source) VALUES( ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithoutObjectId(){ 
 		Object[] params = {this.patientId, this.dataGravida, this.status, this.source};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithObjectId(){ 
 		return "INSERT INTO women_pregnant_lactation_by_encounter(patient_id, data_gravida, status, source) VALUES( ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithObjectId(){ 
 		Object[] params = {this.patientId, this.dataGravida, this.status, this.source};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.patientId, this.dataGravida, this.status, this.source, null};		return params; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE women_pregnant_lactation_by_encounter SET patient_id = ?, data_gravida = ?, status = ?, source = ? WHERE null = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.patientId) + "," + (this.dataGravida != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dataGravida)  +"\"" : null) + "," + (this.status != null ? "\""+ utilities.scapeQuotationMarks(status)  +"\"" : null) + "," + (this.source != null ? "\""+ utilities.scapeQuotationMarks(source)  +"\"" : null); 
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
		return "women_pregnant_lactation_by_encounter";
	}


}