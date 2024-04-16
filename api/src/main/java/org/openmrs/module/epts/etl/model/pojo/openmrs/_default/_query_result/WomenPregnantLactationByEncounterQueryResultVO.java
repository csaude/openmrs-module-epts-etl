package org.openmrs.module.epts.etl.model.pojo.openmrs._default._query_result;

import org.openmrs.module.epts.etl.model.pojo.generic.*; 
 
import org.openmrs.module.epts.etl.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.epts.etl.utilities.AttDefinedElements; 
 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class WomenPregnantLactationByEncounterQueryResultVO extends AbstractDatabaseObject implements DatabaseObject { 
	private java.util.Date dataGravida;
	private String status;
	private String source;
 
	public WomenPregnantLactationByEncounterQueryResultVO() { 
		this.metadata = false;
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
 
	@Override
	public void load(ResultSet rs) throws SQLException{ 
		super.load(rs);
 
this.dataGravida =  rs.getTimestamp("data_gravida") != null ? new java.util.Date( rs.getTimestamp("data_gravida").getTime() ) : null;
this.status = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("status") != null ? rs.getString("status").trim() : null);
this.source = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("source") != null ? rs.getString("source").trim() : null);
	} 
 
	@JsonIgnore
	@Override
	public String getInsertSQLWithoutObjectId(){ 
 		return "INSERT INTO women_pregnant_lactation_by_encounter(data_gravida, status, source) VALUES( ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	@Override
	public String getInsertSQLWithObjectId(){ 
 		return "INSERT INTO women_pregnant_lactation_by_encounter(data_gravida, status, source) VALUES( ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	@Override
	public Object[]  getInsertParamsWithoutObjectId(){ 
 		Object[] params = {this.dataGravida, this.status, this.source};
		return params; 
	} 
 
	@JsonIgnore
	@Override
	public Object[]  getInsertParamsWithObjectId(){ 
 		Object[] params = {this.dataGravida, this.status, this.source};
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
 		return ""+(this.dataGravida != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dataGravida)  +"\"" : null) + "," + (this.status != null ? "\""+ utilities.scapeQuotationMarks(status)  +"\"" : null) + "," + (this.source != null ? "\""+ utilities.scapeQuotationMarks(source)  +"\"" : null); 
	} 
 
	@JsonIgnore
	@Override
	public String generateInsertValuesWithObjectId(){ 
 		return ""+(this.dataGravida != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dataGravida)  +"\"" : null) + "," + (this.status != null ? "\""+ utilities.scapeQuotationMarks(status)  +"\"" : null) + "," + (this.source != null ? "\""+ utilities.scapeQuotationMarks(source)  +"\"" : null); 
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
		return "women_pregnant_lactation_by_encounter";
	}


}