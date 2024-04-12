package org.openmrs.module.epts.etl.model.pojo.mozart.src;

import org.openmrs.module.epts.etl.model.pojo.generic.*; 
 
import org.openmrs.module.epts.etl.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.epts.etl.utilities.AttDefinedElements; 
 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class ProphylaxisVO extends AbstractDatabaseObject implements DatabaseObject { 
	private Integer id;
	private String encounterUuid;
	private java.util.Date encounterDate;
	private Integer regimenProphylaxisTpt;
	private Integer regimenProphylaxisCtx;
	private Integer regimenProphylaxisPrep;
	private Double noOfUnits;
	private Integer prophylaxisStatus;
	private Integer secondaryEffectsTpt;
	private Integer secondaryEffectsCtz;
	private Integer dispensationType;
	private java.util.Date nextPickupDate;
 
	public ProphylaxisVO() { 
		this.metadata = false;
	} 
 
	public void setId(Integer id){ 
	 	this.id = id;
	}
 
	public Integer getId(){ 
		return this.id;
	}
 
	public void setEncounterUuid(String encounterUuid){ 
	 	this.encounterUuid = encounterUuid;
	}
 
	public String getEncounterUuid(){ 
		return this.encounterUuid;
	}
 
	public void setEncounterDate(java.util.Date encounterDate){ 
	 	this.encounterDate = encounterDate;
	}
 
	public java.util.Date getEncounterDate(){ 
		return this.encounterDate;
	}
 
	public void setRegimenProphylaxisTpt(Integer regimenProphylaxisTpt){ 
	 	this.regimenProphylaxisTpt = regimenProphylaxisTpt;
	}
 
	public Integer getRegimenProphylaxisTpt(){ 
		return this.regimenProphylaxisTpt;
	}
 
	public void setRegimenProphylaxisCtx(Integer regimenProphylaxisCtx){ 
	 	this.regimenProphylaxisCtx = regimenProphylaxisCtx;
	}
 
	public Integer getRegimenProphylaxisCtx(){ 
		return this.regimenProphylaxisCtx;
	}
 
	public void setRegimenProphylaxisPrep(Integer regimenProphylaxisPrep){ 
	 	this.regimenProphylaxisPrep = regimenProphylaxisPrep;
	}
 
	public Integer getRegimenProphylaxisPrep(){ 
		return this.regimenProphylaxisPrep;
	}
 
	public void setNoOfUnits(Double noOfUnits){ 
	 	this.noOfUnits = noOfUnits;
	}
 
	public Double getNoOfUnits(){ 
		return this.noOfUnits;
	}
 
	public void setProphylaxisStatus(Integer prophylaxisStatus){ 
	 	this.prophylaxisStatus = prophylaxisStatus;
	}
 
	public Integer getProphylaxisStatus(){ 
		return this.prophylaxisStatus;
	}
 
	public void setSecondaryEffectsTpt(Integer secondaryEffectsTpt){ 
	 	this.secondaryEffectsTpt = secondaryEffectsTpt;
	}
 
	public Integer getSecondaryEffectsTpt(){ 
		return this.secondaryEffectsTpt;
	}
 
	public void setSecondaryEffectsCtz(Integer secondaryEffectsCtz){ 
	 	this.secondaryEffectsCtz = secondaryEffectsCtz;
	}
 
	public Integer getSecondaryEffectsCtz(){ 
		return this.secondaryEffectsCtz;
	}
 
	public void setDispensationType(Integer dispensationType){ 
	 	this.dispensationType = dispensationType;
	}
 
	public Integer getDispensationType(){ 
		return this.dispensationType;
	}
 
	public void setNextPickupDate(java.util.Date nextPickupDate){ 
	 	this.nextPickupDate = nextPickupDate;
	}


 
	public java.util.Date getNextPickupDate(){ 
		return this.nextPickupDate;
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
		this.encounterUuid = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("encounter_uuid") != null ? rs.getString("encounter_uuid").trim() : null);
		this.encounterDate =  rs.getTimestamp("encounter_date") != null ? new java.util.Date( rs.getTimestamp("encounter_date").getTime() ) : null;
		if (rs.getObject("regimen_prophylaxis_tpt") != null) this.regimenProphylaxisTpt = rs.getInt("regimen_prophylaxis_tpt");
		if (rs.getObject("regimen_prophylaxis_ctx") != null) this.regimenProphylaxisCtx = rs.getInt("regimen_prophylaxis_ctx");
		if (rs.getObject("regimen_prophylaxis_prep") != null) this.regimenProphylaxisPrep = rs.getInt("regimen_prophylaxis_prep");
		if (rs.getObject("no_of_units") != null) this.noOfUnits = rs.getDouble("no_of_units");
		if (rs.getObject("prophylaxis_status") != null) this.prophylaxisStatus = rs.getInt("prophylaxis_status");
		if (rs.getObject("secondary_effects_tpt") != null) this.secondaryEffectsTpt = rs.getInt("secondary_effects_tpt");
		if (rs.getObject("secondary_effects_ctz") != null) this.secondaryEffectsCtz = rs.getInt("secondary_effects_ctz");
		if (rs.getObject("dispensation_type") != null) this.dispensationType = rs.getInt("dispensation_type");
		this.nextPickupDate =  rs.getTimestamp("next_pickup_date") != null ? new java.util.Date( rs.getTimestamp("next_pickup_date").getTime() ) : null;
	} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "id"; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithoutObjectId(){ 
 		return "INSERT INTO prophylaxis(encounter_uuid, encounter_date, regimen_prophylaxis_tpt, regimen_prophylaxis_ctx, regimen_prophylaxis_prep, no_of_units, prophylaxis_status, secondary_effects_tpt, secondary_effects_ctz, dispensation_type, next_pickup_date) VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithoutObjectId(){ 
 		Object[] params = {this.encounterUuid, this.encounterDate, this.regimenProphylaxisTpt, this.regimenProphylaxisCtx, this.regimenProphylaxisPrep, this.noOfUnits, this.prophylaxisStatus, this.secondaryEffectsTpt, this.secondaryEffectsCtz, this.dispensationType, this.nextPickupDate};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithObjectId(){ 
 		return "INSERT INTO prophylaxis(id, encounter_uuid, encounter_date, regimen_prophylaxis_tpt, regimen_prophylaxis_ctx, regimen_prophylaxis_prep, no_of_units, prophylaxis_status, secondary_effects_tpt, secondary_effects_ctz, dispensation_type, next_pickup_date) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithObjectId(){ 
 		Object[] params = {this.id, this.encounterUuid, this.encounterDate, this.regimenProphylaxisTpt, this.regimenProphylaxisCtx, this.regimenProphylaxisPrep, this.noOfUnits, this.prophylaxisStatus, this.secondaryEffectsTpt, this.secondaryEffectsCtz, this.dispensationType, this.nextPickupDate};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.encounterUuid, this.encounterDate, this.regimenProphylaxisTpt, this.regimenProphylaxisCtx, this.regimenProphylaxisPrep, this.noOfUnits, this.prophylaxisStatus, this.secondaryEffectsTpt, this.secondaryEffectsCtz, this.dispensationType, this.nextPickupDate, this.id};		return params; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE prophylaxis SET encounter_uuid = ?, encounter_date = ?, regimen_prophylaxis_tpt = ?, regimen_prophylaxis_ctx = ?, regimen_prophylaxis_prep = ?, no_of_units = ?, prophylaxis_status = ?, secondary_effects_tpt = ?, secondary_effects_ctz = ?, dispensation_type = ?, next_pickup_date = ? WHERE id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.encounterUuid != null ? "\""+ utilities.scapeQuotationMarks(encounterUuid)  +"\"" : null) + "," + (this.encounterDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(encounterDate)  +"\"" : null) + "," + (this.regimenProphylaxisTpt) + "," + (this.regimenProphylaxisCtx) + "," + (this.regimenProphylaxisPrep) + "," + (this.noOfUnits) + "," + (this.prophylaxisStatus) + "," + (this.secondaryEffectsTpt) + "," + (this.secondaryEffectsCtz) + "," + (this.dispensationType) + "," + (this.nextPickupDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(nextPickupDate)  +"\"" : null); 
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
		return "prophylaxis";
	}


}