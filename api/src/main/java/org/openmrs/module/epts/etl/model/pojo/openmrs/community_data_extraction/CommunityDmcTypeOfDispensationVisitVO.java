package org.openmrs.module.epts.etl.model.pojo.openmrs.community_data_extraction;

import org.openmrs.module.epts.etl.model.pojo.generic.*; 
 
import org.openmrs.module.epts.etl.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.epts.etl.utilities.AttDefinedElements; 
 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class CommunityDmcTypeOfDispensationVisitVO extends AbstractDatabaseObject implements DatabaseObject { 
	private Integer id;
	private Integer patientId;
	private String elegibblyDmc;
	private java.util.Date dateElegibblyDmc;
	private String typeDmc;
	private String valueDmc;
 
	public CommunityDmcTypeOfDispensationVisitVO() { 
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
 
	public void setElegibblyDmc(String elegibblyDmc){ 
	 	this.elegibblyDmc = elegibblyDmc;
	}
 
	public String getElegibblyDmc(){ 
		return this.elegibblyDmc;
	}
 
	public void setDateElegibblyDmc(java.util.Date dateElegibblyDmc){ 
	 	this.dateElegibblyDmc = dateElegibblyDmc;
	}
 
	public java.util.Date getDateElegibblyDmc(){ 
		return this.dateElegibblyDmc;
	}
 
	public void setTypeDmc(String typeDmc){ 
	 	this.typeDmc = typeDmc;
	}
 
	public String getTypeDmc(){ 
		return this.typeDmc;
	}
 
	public void setValueDmc(String valueDmc){ 
	 	this.valueDmc = valueDmc;
	}


 
	public String getValueDmc(){ 
		return this.valueDmc;
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
		this.elegibblyDmc = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("elegibbly_dmc") != null ? rs.getString("elegibbly_dmc").trim() : null);
		this.dateElegibblyDmc =  rs.getTimestamp("date_elegibbly_dmc") != null ? new java.util.Date( rs.getTimestamp("date_elegibbly_dmc").getTime() ) : null;
		this.typeDmc = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("type_dmc") != null ? rs.getString("type_dmc").trim() : null);
		this.valueDmc = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("value_dmc") != null ? rs.getString("value_dmc").trim() : null);
	} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "id"; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithoutObjectId(){ 
 		return "INSERT INTO community_dmc_type_of_dispensation_visit(patient_id, elegibbly_dmc, date_elegibbly_dmc, type_dmc, value_dmc) VALUES( ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithoutObjectId(){ 
 		Object[] params = {this.patientId, this.elegibblyDmc, this.dateElegibblyDmc, this.typeDmc, this.valueDmc};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithObjectId(){ 
 		return "INSERT INTO community_dmc_type_of_dispensation_visit(id, patient_id, elegibbly_dmc, date_elegibbly_dmc, type_dmc, value_dmc) VALUES(?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithObjectId(){ 
 		Object[] params = {this.id, this.patientId, this.elegibblyDmc, this.dateElegibblyDmc, this.typeDmc, this.valueDmc};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.patientId, this.elegibblyDmc, this.dateElegibblyDmc, this.typeDmc, this.valueDmc, this.id};		return params; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE community_dmc_type_of_dispensation_visit SET patient_id = ?, elegibbly_dmc = ?, date_elegibbly_dmc = ?, type_dmc = ?, value_dmc = ? WHERE id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.patientId) + "," + (this.elegibblyDmc != null ? "\""+ utilities.scapeQuotationMarks(elegibblyDmc)  +"\"" : null) + "," + (this.dateElegibblyDmc != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateElegibblyDmc)  +"\"" : null) + "," + (this.typeDmc != null ? "\""+ utilities.scapeQuotationMarks(typeDmc)  +"\"" : null) + "," + (this.valueDmc != null ? "\""+ utilities.scapeQuotationMarks(valueDmc)  +"\"" : null); 
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
		return "community_dmc_type_of_dispensation_visit";
	}


}