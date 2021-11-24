package org.openmrs.module.eptssync.model.pojo.source.qlm_24_julho; 
 
import org.openmrs.module.eptssync.model.pojo.generic.*; 
 
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.eptssync.utilities.AttDefinedElements; 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class LocationVO extends AbstractOpenMRSObject implements OpenMRSObject { 
	private int locationId;
	private String name;
	private String description;
	private String address1;
	private String address2;
	private String cityVillage;
	private String stateProvince;
	private String postalCode;
	private String country;
	private String latitude;
	private String longitude;
	private int creator;
	private String countyDistrict;
	private String address3;
	private String address6;
	private String address5;
	private String address4;
	private byte retired;
	private int retiredBy;
	private java.util.Date dateRetired;
	private String retireReason;
	private int parentLocation;
	private int changedBy;
	private String address7;
	private String address8;
	private String address9;
	private String address10;
	private String address11;
	private String address12;
	private String address13;
	private String address14;
	private String address15;
 
	public LocationVO() { 
		this.metadata = true;
	} 
 
	public void setLocationId(int locationId){ 
	 	this.locationId = locationId;
	}
 
	public int getLocationId(){ 
		return this.locationId;
	}
 
	public void setName(String name){ 
	 	this.name = name;
	}
 
	public String getName(){ 
		return this.name;
	}
 
	public void setDescription(String description){ 
	 	this.description = description;
	}
 
	public String getDescription(){ 
		return this.description;
	}
 
	public void setAddress1(String address1){ 
	 	this.address1 = address1;
	}
 
	public String getAddress1(){ 
		return this.address1;
	}
 
	public void setAddress2(String address2){ 
	 	this.address2 = address2;
	}
 
	public String getAddress2(){ 
		return this.address2;
	}
 
	public void setCityVillage(String cityVillage){ 
	 	this.cityVillage = cityVillage;
	}
 
	public String getCityVillage(){ 
		return this.cityVillage;
	}
 
	public void setStateProvince(String stateProvince){ 
	 	this.stateProvince = stateProvince;
	}
 
	public String getStateProvince(){ 
		return this.stateProvince;
	}
 
	public void setPostalCode(String postalCode){ 
	 	this.postalCode = postalCode;
	}
 
	public String getPostalCode(){ 
		return this.postalCode;
	}
 
	public void setCountry(String country){ 
	 	this.country = country;
	}
 
	public String getCountry(){ 
		return this.country;
	}
 
	public void setLatitude(String latitude){ 
	 	this.latitude = latitude;
	}
 
	public String getLatitude(){ 
		return this.latitude;
	}
 
	public void setLongitude(String longitude){ 
	 	this.longitude = longitude;
	}
 
	public String getLongitude(){ 
		return this.longitude;
	}
 
	public void setCreator(int creator){ 
	 	this.creator = creator;
	}
 
	public int getCreator(){ 
		return this.creator;
	}
 
	public void setCountyDistrict(String countyDistrict){ 
	 	this.countyDistrict = countyDistrict;
	}
 
	public String getCountyDistrict(){ 
		return this.countyDistrict;
	}
 
	public void setAddress3(String address3){ 
	 	this.address3 = address3;
	}
 
	public String getAddress3(){ 
		return this.address3;
	}
 
	public void setAddress6(String address6){ 
	 	this.address6 = address6;
	}
 
	public String getAddress6(){ 
		return this.address6;
	}
 
	public void setAddress5(String address5){ 
	 	this.address5 = address5;
	}
 
	public String getAddress5(){ 
		return this.address5;
	}
 
	public void setAddress4(String address4){ 
	 	this.address4 = address4;
	}
 
	public String getAddress4(){ 
		return this.address4;
	}
 
	public void setRetired(byte retired){ 
	 	this.retired = retired;
	}
 
	public byte getRetired(){ 
		return this.retired;
	}
 
	public void setRetiredBy(int retiredBy){ 
	 	this.retiredBy = retiredBy;
	}
 
	public int getRetiredBy(){ 
		return this.retiredBy;
	}
 
	public void setDateRetired(java.util.Date dateRetired){ 
	 	this.dateRetired = dateRetired;
	}
 
	public java.util.Date getDateRetired(){ 
		return this.dateRetired;
	}
 
	public void setRetireReason(String retireReason){ 
	 	this.retireReason = retireReason;
	}
 
	public String getRetireReason(){ 
		return this.retireReason;
	}
 
	public void setParentLocation(int parentLocation){ 
	 	this.parentLocation = parentLocation;
	}
 
	public int getParentLocation(){ 
		return this.parentLocation;
	}
 
	public void setChangedBy(int changedBy){ 
	 	this.changedBy = changedBy;
	}
 
	public int getChangedBy(){ 
		return this.changedBy;
	}
 
	public void setAddress7(String address7){ 
	 	this.address7 = address7;
	}
 
	public String getAddress7(){ 
		return this.address7;
	}
 
	public void setAddress8(String address8){ 
	 	this.address8 = address8;
	}
 
	public String getAddress8(){ 
		return this.address8;
	}
 
	public void setAddress9(String address9){ 
	 	this.address9 = address9;
	}
 
	public String getAddress9(){ 
		return this.address9;
	}
 
	public void setAddress10(String address10){ 
	 	this.address10 = address10;
	}
 
	public String getAddress10(){ 
		return this.address10;
	}
 
	public void setAddress11(String address11){ 
	 	this.address11 = address11;
	}
 
	public String getAddress11(){ 
		return this.address11;
	}
 
	public void setAddress12(String address12){ 
	 	this.address12 = address12;
	}
 
	public String getAddress12(){ 
		return this.address12;
	}
 
	public void setAddress13(String address13){ 
	 	this.address13 = address13;
	}
 
	public String getAddress13(){ 
		return this.address13;
	}
 
	public void setAddress14(String address14){ 
	 	this.address14 = address14;
	}
 
	public String getAddress14(){ 
		return this.address14;
	}
 
	public void setAddress15(String address15){ 
	 	this.address15 = address15;
	}


 
	public String getAddress15(){ 
		return this.address15;
	}
 
	public int getObjectId() { 
 		return this.locationId; 
	} 
 
	public void setObjectId(int selfId){ 
		this.locationId = selfId; 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		super.load(rs);
		this.locationId = rs.getInt("location_id");
		this.name = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("name") != null ? rs.getString("name").trim() : null);
		this.description = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("description") != null ? rs.getString("description").trim() : null);
		this.address1 = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("address1") != null ? rs.getString("address1").trim() : null);
		this.address2 = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("address2") != null ? rs.getString("address2").trim() : null);
		this.cityVillage = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("city_village") != null ? rs.getString("city_village").trim() : null);
		this.stateProvince = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("state_province") != null ? rs.getString("state_province").trim() : null);
		this.postalCode = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("postal_code") != null ? rs.getString("postal_code").trim() : null);
		this.country = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("country") != null ? rs.getString("country").trim() : null);
		this.latitude = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("latitude") != null ? rs.getString("latitude").trim() : null);
		this.longitude = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("longitude") != null ? rs.getString("longitude").trim() : null);
		this.creator = rs.getInt("creator");
		this.dateCreated =  rs.getTimestamp("date_created") != null ? new java.util.Date( rs.getTimestamp("date_created").getTime() ) : null;
		this.countyDistrict = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("county_district") != null ? rs.getString("county_district").trim() : null);
		this.address3 = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("address3") != null ? rs.getString("address3").trim() : null);
		this.address6 = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("address6") != null ? rs.getString("address6").trim() : null);
		this.address5 = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("address5") != null ? rs.getString("address5").trim() : null);
		this.address4 = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("address4") != null ? rs.getString("address4").trim() : null);
		this.retired = rs.getByte("retired");
		this.retiredBy = rs.getInt("retired_by");
		this.dateRetired =  rs.getTimestamp("date_retired") != null ? new java.util.Date( rs.getTimestamp("date_retired").getTime() ) : null;
		this.retireReason = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("retire_reason") != null ? rs.getString("retire_reason").trim() : null);
		this.parentLocation = rs.getInt("parent_location");
		this.uuid = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("uuid") != null ? rs.getString("uuid").trim() : null);
		this.changedBy = rs.getInt("changed_by");
		this.dateChanged =  rs.getTimestamp("date_changed") != null ? new java.util.Date( rs.getTimestamp("date_changed").getTime() ) : null;
		this.address7 = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("address7") != null ? rs.getString("address7").trim() : null);
		this.address8 = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("address8") != null ? rs.getString("address8").trim() : null);
		this.address9 = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("address9") != null ? rs.getString("address9").trim() : null);
		this.address10 = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("address10") != null ? rs.getString("address10").trim() : null);
		this.address11 = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("address11") != null ? rs.getString("address11").trim() : null);
		this.address12 = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("address12") != null ? rs.getString("address12").trim() : null);
		this.address13 = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("address13") != null ? rs.getString("address13").trim() : null);
		this.address14 = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("address14") != null ? rs.getString("address14").trim() : null);
		this.address15 = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("address15") != null ? rs.getString("address15").trim() : null);
	} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "location_id"; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithoutObjectId(){ 
 		return "INSERT INTO location(location_id, name, description, address1, address2, city_village, state_province, postal_code, country, latitude, longitude, creator, date_created, county_district, address3, address6, address5, address4, retired, retired_by, date_retired, retire_reason, parent_location, uuid, changed_by, date_changed, address7, address8, address9, address10, address11, address12, address13, address14, address15) VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithoutObjectId(){ 
 		Object[] params = {this.locationId, this.name, this.description, this.address1, this.address2, this.cityVillage, this.stateProvince, this.postalCode, this.country, this.latitude, this.longitude, this.creator == 0 ? null : this.creator, this.dateCreated, this.countyDistrict, this.address3, this.address6, this.address5, this.address4, this.retired, this.retiredBy == 0 ? null : this.retiredBy, this.dateRetired, this.retireReason, this.parentLocation == 0 ? null : this.parentLocation, this.uuid, this.changedBy == 0 ? null : this.changedBy, this.dateChanged, this.address7, this.address8, this.address9, this.address10, this.address11, this.address12, this.address13, this.address14, this.address15};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithObjectId(){ 
 		return "INSERT INTO location(location_id, location_id, name, description, address1, address2, city_village, state_province, postal_code, country, latitude, longitude, creator, date_created, county_district, address3, address6, address5, address4, retired, retired_by, date_retired, retire_reason, parent_location, uuid, changed_by, date_changed, address7, address8, address9, address10, address11, address12, address13, address14, address15) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithObjectId(){ 
 		Object[] params = {this.locationId, this.locationId, this.name, this.description, this.address1, this.address2, this.cityVillage, this.stateProvince, this.postalCode, this.country, this.latitude, this.longitude, this.creator == 0 ? null : this.creator, this.dateCreated, this.countyDistrict, this.address3, this.address6, this.address5, this.address4, this.retired, this.retiredBy == 0 ? null : this.retiredBy, this.dateRetired, this.retireReason, this.parentLocation == 0 ? null : this.parentLocation, this.uuid, this.changedBy == 0 ? null : this.changedBy, this.dateChanged, this.address7, this.address8, this.address9, this.address10, this.address11, this.address12, this.address13, this.address14, this.address15};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.locationId, this.name, this.description, this.address1, this.address2, this.cityVillage, this.stateProvince, this.postalCode, this.country, this.latitude, this.longitude, this.creator == 0 ? null : this.creator, this.dateCreated, this.countyDistrict, this.address3, this.address6, this.address5, this.address4, this.retired, this.retiredBy == 0 ? null : this.retiredBy, this.dateRetired, this.retireReason, this.parentLocation == 0 ? null : this.parentLocation, this.uuid, this.changedBy == 0 ? null : this.changedBy, this.dateChanged, this.address7, this.address8, this.address9, this.address10, this.address11, this.address12, this.address13, this.address14, this.address15, this.locationId};		return params; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE location SET location_id = ?, name = ?, description = ?, address1 = ?, address2 = ?, city_village = ?, state_province = ?, postal_code = ?, country = ?, latitude = ?, longitude = ?, creator = ?, date_created = ?, county_district = ?, address3 = ?, address6 = ?, address5 = ?, address4 = ?, retired = ?, retired_by = ?, date_retired = ?, retire_reason = ?, parent_location = ?, uuid = ?, changed_by = ?, date_changed = ?, address7 = ?, address8 = ?, address9 = ?, address10 = ?, address11 = ?, address12 = ?, address13 = ?, address14 = ?, address15 = ? WHERE location_id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.locationId) + "," + (this.name != null ? "\""+ utilities.scapeQuotationMarks(name)  +"\"" : null) + "," + (this.description != null ? "\""+ utilities.scapeQuotationMarks(description)  +"\"" : null) + "," + (this.address1 != null ? "\""+ utilities.scapeQuotationMarks(address1)  +"\"" : null) + "," + (this.address2 != null ? "\""+ utilities.scapeQuotationMarks(address2)  +"\"" : null) + "," + (this.cityVillage != null ? "\""+ utilities.scapeQuotationMarks(cityVillage)  +"\"" : null) + "," + (this.stateProvince != null ? "\""+ utilities.scapeQuotationMarks(stateProvince)  +"\"" : null) + "," + (this.postalCode != null ? "\""+ utilities.scapeQuotationMarks(postalCode)  +"\"" : null) + "," + (this.country != null ? "\""+ utilities.scapeQuotationMarks(country)  +"\"" : null) + "," + (this.latitude != null ? "\""+ utilities.scapeQuotationMarks(latitude)  +"\"" : null) + "," + (this.longitude != null ? "\""+ utilities.scapeQuotationMarks(longitude)  +"\"" : null) + "," + (this.creator == 0 ? null : this.creator) + "," + (this.dateCreated != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateCreated)  +"\"" : null) + "," + (this.countyDistrict != null ? "\""+ utilities.scapeQuotationMarks(countyDistrict)  +"\"" : null) + "," + (this.address3 != null ? "\""+ utilities.scapeQuotationMarks(address3)  +"\"" : null) + "," + (this.address6 != null ? "\""+ utilities.scapeQuotationMarks(address6)  +"\"" : null) + "," + (this.address5 != null ? "\""+ utilities.scapeQuotationMarks(address5)  +"\"" : null) + "," + (this.address4 != null ? "\""+ utilities.scapeQuotationMarks(address4)  +"\"" : null) + "," + (this.retired) + "," + (this.retiredBy == 0 ? null : this.retiredBy) + "," + (this.dateRetired != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateRetired)  +"\"" : null) + "," + (this.retireReason != null ? "\""+ utilities.scapeQuotationMarks(retireReason)  +"\"" : null) + "," + (this.parentLocation == 0 ? null : this.parentLocation) + "," + (this.uuid != null ? "\""+ utilities.scapeQuotationMarks(uuid)  +"\"" : null) + "," + (this.changedBy == 0 ? null : this.changedBy) + "," + (this.dateChanged != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateChanged)  +"\"" : null) + "," + (this.address7 != null ? "\""+ utilities.scapeQuotationMarks(address7)  +"\"" : null) + "," + (this.address8 != null ? "\""+ utilities.scapeQuotationMarks(address8)  +"\"" : null) + "," + (this.address9 != null ? "\""+ utilities.scapeQuotationMarks(address9)  +"\"" : null) + "," + (this.address10 != null ? "\""+ utilities.scapeQuotationMarks(address10)  +"\"" : null) + "," + (this.address11 != null ? "\""+ utilities.scapeQuotationMarks(address11)  +"\"" : null) + "," + (this.address12 != null ? "\""+ utilities.scapeQuotationMarks(address12)  +"\"" : null) + "," + (this.address13 != null ? "\""+ utilities.scapeQuotationMarks(address13)  +"\"" : null) + "," + (this.address14 != null ? "\""+ utilities.scapeQuotationMarks(address14)  +"\"" : null) + "," + (this.address15 != null ? "\""+ utilities.scapeQuotationMarks(address15)  +"\"" : null); 
	} 
 
	@Override
	public boolean hasParents() {
		if (this.changedBy != 0) return true;

		if (this.parentLocation != 0) return true;

		if (this.creator != 0) return true;

		if (this.retiredBy != 0) return true;

		return false;
	}

	@Override
	public int getParentValue(String parentAttName) {		
		if (parentAttName.equals("changedBy")) return this.changedBy;		
		if (parentAttName.equals("parentLocation")) return this.parentLocation;		
		if (parentAttName.equals("creator")) return this.creator;		
		if (parentAttName.equals("retiredBy")) return this.retiredBy;

		throw new RuntimeException("No found parent for: " + parentAttName);	}

	@Override
	public void changeParentValue(String parentAttName, OpenMRSObject newParent) {		
		if (parentAttName.equals("changedBy")) {
			this.changedBy = newParent.getObjectId();
			return;
		}		
		if (parentAttName.equals("parentLocation")) {
			this.parentLocation = newParent.getObjectId();
			return;
		}		
		if (parentAttName.equals("creator")) {
			this.creator = newParent.getObjectId();
			return;
		}		
		if (parentAttName.equals("retiredBy")) {
			this.retiredBy = newParent.getObjectId();
			return;
		}

		throw new RuntimeException("No found parent for: " + parentAttName);
	}


}