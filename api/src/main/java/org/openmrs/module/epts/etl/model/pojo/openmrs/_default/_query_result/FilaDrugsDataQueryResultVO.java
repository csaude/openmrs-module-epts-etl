package org.openmrs.module.epts.etl.model.pojo.openmrs._default._query_result;

import org.openmrs.module.epts.etl.model.pojo.generic.*; 
 
import org.openmrs.module.epts.etl.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.epts.etl.utilities.AttDefinedElements; 
 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class FilaDrugsDataQueryResultVO extends AbstractDatabaseObject implements DatabaseObject { 
	private String regime;
	private String formulation;
	private Integer groupId;
	private double quantity;
	private String dosage;
	private java.util.Date nextArtDate;
	private String accommodationCamp;
	private String dispensationModel;
 
	public FilaDrugsDataQueryResultVO() { 
		this.metadata = false;
	} 
 
	public void setRegime(String regime){ 
	 	this.regime = regime;
	}
 
	public String getRegime(){ 
		return this.regime;
	}
 
	public void setFormulation(String formulation){ 
	 	this.formulation = formulation;
	}
 
	public String getFormulation(){ 
		return this.formulation;
	}
 
	public void setGroupId(Integer groupId){ 
	 	this.groupId = groupId;
	}
 
	public Integer getGroupId(){ 
		return this.groupId;
	}
 
	public void setQuantity(double quantity){ 
	 	this.quantity = quantity;
	}
 
	public double getQuantity(){ 
		return this.quantity;
	}
 
	public void setDosage(String dosage){ 
	 	this.dosage = dosage;
	}
 
	public String getDosage(){ 
		return this.dosage;
	}
 
	public void setNextArtDate(java.util.Date nextArtDate){ 
	 	this.nextArtDate = nextArtDate;
	}
 
	public java.util.Date getNextArtDate(){ 
		return this.nextArtDate;
	}
 
	public void setAccommodationCamp(String accommodationCamp){ 
	 	this.accommodationCamp = accommodationCamp;
	}
 
	public String getAccommodationCamp(){ 
		return this.accommodationCamp;
	}
 
	public void setDispensationModel(String dispensationModel){ 
	 	this.dispensationModel = dispensationModel;
	}


 
	public String getDispensationModel(){ 
		return this.dispensationModel;
	}
 
	public Integer getObjectId() { 
 		return 0; 
	} 
 
	public void setObjectId(Integer selfId){ 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		super.load(rs);
		this.regime = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("regime") != null ? rs.getString("regime").trim() : null);
		this.formulation = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("formulation") != null ? rs.getString("formulation").trim() : null);
		if (rs.getObject("group_id") != null) this.groupId = rs.getInt("group_id");
		this.quantity = rs.getDouble("quantity");
		this.dosage = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("dosage") != null ? rs.getString("dosage").trim() : null);
		this.nextArtDate =  rs.getTimestamp("next_art_date") != null ? new java.util.Date( rs.getTimestamp("next_art_date").getTime() ) : null;
		this.accommodationCamp = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("accommodation_camp") != null ? rs.getString("accommodation_camp").trim() : null);
		this.dispensationModel = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("dispensation_model") != null ? rs.getString("dispensation_model").trim() : null);
	} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return null; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithoutObjectId(){ 
 		return "INSERT INTO fila_drugs_data(regime, formulation, group_id, quantity, dosage, next_art_date, accommodation_camp, dispensation_model) VALUES( ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithoutObjectId(){ 
 		Object[] params = {this.regime, this.formulation, this.groupId, this.quantity, this.dosage, this.nextArtDate, this.accommodationCamp, this.dispensationModel};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithObjectId(){ 
 		return "INSERT INTO fila_drugs_data(regime, formulation, group_id, quantity, dosage, next_art_date, accommodation_camp, dispensation_model) VALUES( ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithObjectId(){ 
 		Object[] params = {this.regime, this.formulation, this.groupId, this.quantity, this.dosage, this.nextArtDate, this.accommodationCamp, this.dispensationModel};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.regime, this.formulation, this.groupId, this.quantity, this.dosage, this.nextArtDate, this.accommodationCamp, this.dispensationModel, null};		return params; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE fila_drugs_data SET regime = ?, formulation = ?, group_id = ?, quantity = ?, dosage = ?, next_art_date = ?, accommodation_camp = ?, dispensation_model = ? WHERE null = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.regime != null ? "\""+ utilities.scapeQuotationMarks(regime)  +"\"" : null) + "," + (this.formulation != null ? "\""+ utilities.scapeQuotationMarks(formulation)  +"\"" : null) + "," + (this.groupId) + "," + (this.quantity) + "," + (this.dosage != null ? "\""+ utilities.scapeQuotationMarks(dosage)  +"\"" : null) + "," + (this.nextArtDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(nextArtDate)  +"\"" : null) + "," + (this.accommodationCamp != null ? "\""+ utilities.scapeQuotationMarks(accommodationCamp)  +"\"" : null) + "," + (this.dispensationModel != null ? "\""+ utilities.scapeQuotationMarks(dispensationModel)  +"\"" : null); 
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
		return "fila_drugs_data";
	}


}