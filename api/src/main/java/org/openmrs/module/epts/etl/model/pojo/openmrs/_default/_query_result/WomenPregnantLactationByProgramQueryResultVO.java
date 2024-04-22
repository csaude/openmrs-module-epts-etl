package org.openmrs.module.epts.etl.model.pojo.openmrs._default._query_result;

import org.openmrs.module.epts.etl.model.pojo.generic.*; 
 
import org.openmrs.module.epts.etl.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.epts.etl.utilities.AttDefinedElements; 
 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class WomenPregnantLactationByProgramQueryResultVO extends AbstractDatabaseObject implements DatabaseObject { 
	private Integer patientId;
	private java.util.Date dataParto;
	private String status;
	private String source;
 
	public WomenPregnantLactationByProgramQueryResultVO() { 
		this.metadata = false;
	} 
 
	public void setPatientId(Integer patientId){ 
	 	this.patientId = patientId;
	}
 
	public Integer getPatientId(){ 
		return this.patientId;
	}
 
	public void setDataParto(java.util.Date dataParto){ 
	 	this.dataParto = dataParto;
	}
 
	public java.util.Date getDataParto(){ 
		return this.dataParto;
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
 
	@Override
	public void load(ResultSet rs) throws SQLException{ 
		super.load(rs);
 
if (rs.getObject("patient_id") != null) this.patientId = rs.getInt("patient_id");
this.dataParto =  rs.getTimestamp("data_parto") != null ? new java.util.Date( rs.getTimestamp("data_parto").getTime() ) : null;
this.status = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("status") != null ? rs.getString("status").trim() : null);
this.source = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("source") != null ? rs.getString("source").trim() : null);
	} 
 
	@JsonIgnore
	@Override
	public String getInsertSQLWithoutObjectId(){ 
 		return "INSERT INTO women_pregnant_lactation_by_program(patient_id, data_parto, status, source) VALUES( ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	@Override
	public String getInsertSQLWithObjectId(){ 
 		return "INSERT INTO women_pregnant_lactation_by_program(patient_id, data_parto, status, source) VALUES( ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	@Override
	public Object[]  getInsertParamsWithoutObjectId(){ 
 		Object[] params = {this.patientId, this.dataParto, this.status, this.source};
		return params; 
	} 
 
	@JsonIgnore
	@Override
	public Object[]  getInsertParamsWithObjectId(){ 
 		Object[] params = {this.patientId, this.dataParto, this.status, this.source};
		return params; 
	} 
 
	@JsonIgnore
	@Override
	public Object[]  getUpdateParams(){ 
 		throw new RuntimeException("Impossible auto update command! No primary key is defined for table object!");	} 
 
	@JsonIgnore
	@Override
	public String getUpdateSQL(){ 
 		throw new RuntimeException("Impossible auto update command! No primary key is defined for table object!");	} 
 
	@JsonIgnore
	@Override
	public String generateInsertValuesWithoutObjectId(){ 
 		return ""+(this.patientId) + "," + (this.dataParto != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dataParto)  +"\"" : null) + "," + (this.status != null ? "\""+ utilities.scapeQuotationMarks(status)  +"\"" : null) + "," + (this.source != null ? "\""+ utilities.scapeQuotationMarks(source)  +"\"" : null); 
	} 
 
	@JsonIgnore
	@Override
	public String generateInsertValuesWithObjectId(){ 
 		return ""+(this.patientId) + "," + (this.dataParto != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dataParto)  +"\"" : null) + "," + (this.status != null ? "\""+ utilities.scapeQuotationMarks(status)  +"\"" : null) + "," + (this.source != null ? "\""+ utilities.scapeQuotationMarks(source)  +"\"" : null); 
	} 
 
	@Override
	public boolean hasParents() {
		return false;
	}

	@Override
	public Integer getParentValue(String parentAttName) {

		throw new RuntimeException("No found parent for: " + parentAttName);	}

	@Override
	public String generateTableName() {
		return "women_pregnant_lactation_by_program";
	}


}