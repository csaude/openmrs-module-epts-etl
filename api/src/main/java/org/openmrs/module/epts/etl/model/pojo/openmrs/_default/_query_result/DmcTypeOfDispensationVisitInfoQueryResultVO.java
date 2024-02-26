package org.openmrs.module.epts.etl.model.pojo.openmrs._default._query_result;

import org.openmrs.module.epts.etl.model.pojo.generic.*; 
 
import org.openmrs.module.epts.etl.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.epts.etl.utilities.AttDefinedElements; 
 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class DmcTypeOfDispensationVisitInfoQueryResultVO extends AbstractDatabaseObject implements DatabaseObject { 
	private String elegibblyDmc;
	private String valueDmc;
	private String typeDmc;
 
	public DmcTypeOfDispensationVisitInfoQueryResultVO() { 
		this.metadata = false;
	} 
 
	public void setElegibblyDmc(String elegibblyDmc){ 
	 	this.elegibblyDmc = elegibblyDmc;
	}
 
	public String getElegibblyDmc(){ 
		return this.elegibblyDmc;
	}
 
	public void setValueDmc(String valueDmc){ 
	 	this.valueDmc = valueDmc;
	}
 
	public String getValueDmc(){ 
		return this.valueDmc;
	}
 
	public void setTypeDmc(String typeDmc){ 
	 	this.typeDmc = typeDmc;
	}


 
	public String getTypeDmc(){ 
		return this.typeDmc;
	}
 
	public Integer getObjectId() { 
 		return 0; 
	} 
 
	public void setObjectId(Integer selfId){ 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		super.load(rs);
		this.elegibblyDmc = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("elegibbly_dmc") != null ? rs.getString("elegibbly_dmc").trim() : null);
		this.valueDmc = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("value_dmc") != null ? rs.getString("value_dmc").trim() : null);
		this.typeDmc = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("type_dmc") != null ? rs.getString("type_dmc").trim() : null);
	} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return null; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithoutObjectId(){ 
 		return "INSERT INTO dmc_type_of_dispensation_visit_info(elegibbly_dmc, value_dmc, type_dmc) VALUES( ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithoutObjectId(){ 
 		Object[] params = {this.elegibblyDmc, this.valueDmc, this.typeDmc};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithObjectId(){ 
 		return "INSERT INTO dmc_type_of_dispensation_visit_info(elegibbly_dmc, value_dmc, type_dmc) VALUES( ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithObjectId(){ 
 		Object[] params = {this.elegibblyDmc, this.valueDmc, this.typeDmc};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.elegibblyDmc, this.valueDmc, this.typeDmc, null};		return params; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE dmc_type_of_dispensation_visit_info SET elegibbly_dmc = ?, value_dmc = ?, type_dmc = ? WHERE null = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.elegibblyDmc != null ? "\""+ utilities.scapeQuotationMarks(elegibblyDmc)  +"\"" : null) + "," + (this.valueDmc != null ? "\""+ utilities.scapeQuotationMarks(valueDmc)  +"\"" : null) + "," + (this.typeDmc != null ? "\""+ utilities.scapeQuotationMarks(typeDmc)  +"\"" : null); 
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
		return "dmc_type_of_dispensation_visit_info";
	}


}