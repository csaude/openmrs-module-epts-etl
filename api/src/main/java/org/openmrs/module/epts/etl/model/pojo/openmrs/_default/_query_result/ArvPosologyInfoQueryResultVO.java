package org.openmrs.module.epts.etl.model.pojo.openmrs._default._query_result;

import org.openmrs.module.epts.etl.model.pojo.generic.*; 
 
import org.openmrs.module.epts.etl.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.epts.etl.utilities.AttDefinedElements; 
 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class ArvPosologyInfoQueryResultVO extends AbstractDatabaseObject implements DatabaseObject { 
	private String dmcType;
	private String posology;
	private String therapeuticLine;
 
	public ArvPosologyInfoQueryResultVO() { 
		this.metadata = false;
	} 
 
	public void setDmcType(String dmcType){ 
	 	this.dmcType = dmcType;
	}
 
	public String getDmcType(){ 
		return this.dmcType;
	}
 
	public void setPosology(String posology){ 
	 	this.posology = posology;
	}
 
	public String getPosology(){ 
		return this.posology;
	}
 
	public void setTherapeuticLine(String therapeuticLine){ 
	 	this.therapeuticLine = therapeuticLine;
	}


 
	public String getTherapeuticLine(){ 
		return this.therapeuticLine;
	}
 
	public Integer getObjectId() { 
 		return 0; 
	} 
 
	public void setObjectId(Integer selfId){ 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		super.load(rs);
		this.dmcType = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("dmc_type") != null ? rs.getString("dmc_type").trim() : null);
		this.posology = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("posology") != null ? rs.getString("posology").trim() : null);
		this.therapeuticLine = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("therapeutic_line") != null ? rs.getString("therapeutic_line").trim() : null);
	} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return null; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithoutObjectId(){ 
 		return "INSERT INTO arv_posology_info(dmc_type, posology, therapeutic_line) VALUES( ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithoutObjectId(){ 
 		Object[] params = {this.dmcType, this.posology, this.therapeuticLine};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithObjectId(){ 
 		return "INSERT INTO arv_posology_info(dmc_type, posology, therapeutic_line) VALUES( ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithObjectId(){ 
 		Object[] params = {this.dmcType, this.posology, this.therapeuticLine};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.dmcType, this.posology, this.therapeuticLine, null};		return params; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE arv_posology_info SET dmc_type = ?, posology = ?, therapeutic_line = ? WHERE null = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.dmcType != null ? "\""+ utilities.scapeQuotationMarks(dmcType)  +"\"" : null) + "," + (this.posology != null ? "\""+ utilities.scapeQuotationMarks(posology)  +"\"" : null) + "," + (this.therapeuticLine != null ? "\""+ utilities.scapeQuotationMarks(therapeuticLine)  +"\"" : null); 
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
		return "arv_posology_info";
	}


}