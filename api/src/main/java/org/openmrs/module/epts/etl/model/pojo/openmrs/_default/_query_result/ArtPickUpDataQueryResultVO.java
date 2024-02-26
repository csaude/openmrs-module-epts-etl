package org.openmrs.module.epts.etl.model.pojo.openmrs._default._query_result;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.openmrs.module.epts.etl.model.pojo.generic.AbstractDatabaseObject;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObject;
import org.openmrs.module.epts.etl.utilities.AttDefinedElements;
import org.openmrs.module.epts.etl.utilities.DateAndTimeUtilities;

import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class ArtPickUpDataQueryResultVO extends AbstractDatabaseObject implements DatabaseObject { 
	private String pickupArt;
	private java.util.Date artDate;
 
	public ArtPickUpDataQueryResultVO() { 
		this.metadata = false;
	} 
 
	public void setPickupArt(String pickupArt){ 
	 	this.pickupArt = pickupArt;
	}
 
	public String getPickupArt(){ 
		return this.pickupArt;
	}
 
	public void setArtDate(java.util.Date artDate){ 
	 	this.artDate = artDate;
	}


 
	public java.util.Date getArtDate(){ 
		return this.artDate;
	}
 
	public Integer getObjectId() { 
 		return 0; 
	} 
 
	public void setObjectId(Integer selfId){ 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		super.load(rs);
		this.pickupArt = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("pickup_art") != null ? rs.getString("pickup_art").trim() : null);
		this.artDate =  rs.getTimestamp("art_date") != null ? new java.util.Date( rs.getTimestamp("art_date").getTime() ) : null;
	} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return null; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithoutObjectId(){ 
 		return "INSERT INTO art_pick_up_data(pickup_art, art_date) VALUES( ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithoutObjectId(){ 
 		Object[] params = {this.pickupArt, this.artDate};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithObjectId(){ 
 		return "INSERT INTO art_pick_up_data(pickup_art, art_date) VALUES( ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithObjectId(){ 
 		Object[] params = {this.pickupArt, this.artDate};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.pickupArt, this.artDate, null};		return params; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE art_pick_up_data SET pickup_art = ?, art_date = ? WHERE null = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.pickupArt != null ? "\""+ utilities.scapeQuotationMarks(pickupArt)  +"\"" : null) + "," + (this.artDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(artDate)  +"\"" : null); 
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
		return "art_pick_up_data";
	}


}