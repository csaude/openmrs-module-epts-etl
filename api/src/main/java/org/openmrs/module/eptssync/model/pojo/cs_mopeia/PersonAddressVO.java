package org.openmrs.module.eptssync.model.pojo.cs_mopeia; 
 
import org.openmrs.module.eptssync.model.pojo.generic.*; 
 
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.eptssync.utilities.AttDefinedElements; 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class PersonAddressVO extends AbstractOpenMRSObject implements OpenMRSObject { 
	private int personAddressId;
	private int personId;
	private byte preferred;
	private String address1;
	private String address2;
	private String cityVillage;
	private String stateProvince;
	private String postalCode;
	private String country;
	private String latitude;
	private String longitude;
	private int creator;
	private byte voided;
	private int voidedBy;
	private java.util.Date dateVoided;
	private String voidReason;
	private String countyDistrict;
	private String address3;
	private String address6;
	private String address5;
	private String address4;
	private int changedBy;
	private java.util.Date startDate;
	private java.util.Date endDate;
 
	public PersonAddressVO() { 
		this.metadata = false;
	} 
 
	public void setPersonAddressId(int personAddressId){ 
	 	this.personAddressId = personAddressId;
	}
 
	public int getPersonAddressId(){ 
		return this.personAddressId;
	}
 
	public void setPersonId(int personId){ 
	 	this.personId = personId;
	}
 
	public int getPersonId(){ 
		return this.personId;
	}
 
	public void setPreferred(byte preferred){ 
	 	this.preferred = preferred;
	}
 
	public byte getPreferred(){ 
		return this.preferred;
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
 
	public void setVoided(byte voided){ 
	 	this.voided = voided;
	}
 
	public byte getVoided(){ 
		return this.voided;
	}
 
	public void setVoidedBy(int voidedBy){ 
	 	this.voidedBy = voidedBy;
	}
 
	public int getVoidedBy(){ 
		return this.voidedBy;
	}
 
	public void setDateVoided(java.util.Date dateVoided){ 
	 	this.dateVoided = dateVoided;
	}
 
	public java.util.Date getDateVoided(){ 
		return this.dateVoided;
	}
 
	public void setVoidReason(String voidReason){ 
	 	this.voidReason = voidReason;
	}
 
	public String getVoidReason(){ 
		return this.voidReason;
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
 
	public void setChangedBy(int changedBy){ 
	 	this.changedBy = changedBy;
	}
 
	public int getChangedBy(){ 
		return this.changedBy;
	}
 
	public void setStartDate(java.util.Date startDate){ 
	 	this.startDate = startDate;
	}
 
	public java.util.Date getStartDate(){ 
		return this.startDate;
	}
 
	public void setEndDate(java.util.Date endDate){ 
	 	this.endDate = endDate;
	}


 
	public java.util.Date getEndDate(){ 
		return this.endDate;
	}
 
	public int getObjectId() { 
 		return this.personAddressId; 
	} 
 
	public void setObjectId(int selfId){ 
		this.personAddressId = selfId; 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		this.personAddressId = rs.getInt("person_address_id");
		this.personId = rs.getInt("person_id");
		this.preferred = rs.getByte("preferred");
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
		this.voided = rs.getByte("voided");
		this.voidedBy = rs.getInt("voided_by");
		this.dateVoided =  rs.getTimestamp("date_voided") != null ? new java.util.Date( rs.getTimestamp("date_voided").getTime() ) : null;
		this.voidReason = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("void_reason") != null ? rs.getString("void_reason").trim() : null);
		this.countyDistrict = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("county_district") != null ? rs.getString("county_district").trim() : null);
		this.address3 = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("address3") != null ? rs.getString("address3").trim() : null);
		this.address6 = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("address6") != null ? rs.getString("address6").trim() : null);
		this.address5 = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("address5") != null ? rs.getString("address5").trim() : null);
		this.address4 = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("address4") != null ? rs.getString("address4").trim() : null);
		this.uuid = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("uuid") != null ? rs.getString("uuid").trim() : null);
		this.dateChanged =  rs.getTimestamp("date_changed") != null ? new java.util.Date( rs.getTimestamp("date_changed").getTime() ) : null;
		this.changedBy = rs.getInt("changed_by");
		this.startDate =  rs.getTimestamp("start_date") != null ? new java.util.Date( rs.getTimestamp("start_date").getTime() ) : null;
		this.endDate =  rs.getTimestamp("end_date") != null ? new java.util.Date( rs.getTimestamp("end_date").getTime() ) : null;
	} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "person_address_id"; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithoutObjectId(){ 
 		return "INSERT INTO person_address(person_id, preferred, address1, address2, city_village, state_province, postal_code, country, latitude, longitude, creator, date_created, voided, voided_by, date_voided, void_reason, county_district, address3, address6, address5, address4, uuid, date_changed, changed_by, start_date, end_date) VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithoutObjectId(){ 
 		Object[] params = {this.personId == 0 ? null : this.personId, this.preferred, this.address1, this.address2, this.cityVillage, this.stateProvince, this.postalCode, this.country, this.latitude, this.longitude, this.creator == 0 ? null : this.creator, this.dateCreated, this.voided, this.voidedBy == 0 ? null : this.voidedBy, this.dateVoided, this.voidReason, this.countyDistrict, this.address3, this.address6, this.address5, this.address4, this.uuid, this.dateChanged, this.changedBy == 0 ? null : this.changedBy, this.startDate, this.endDate};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithObjectId(){ 
 		return "INSERT INTO person_address(person_address_id, person_id, preferred, address1, address2, city_village, state_province, postal_code, country, latitude, longitude, creator, date_created, voided, voided_by, date_voided, void_reason, county_district, address3, address6, address5, address4, uuid, date_changed, changed_by, start_date, end_date) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithObjectId(){ 
 		Object[] params = {this.personAddressId, this.personId == 0 ? null : this.personId, this.preferred, this.address1, this.address2, this.cityVillage, this.stateProvince, this.postalCode, this.country, this.latitude, this.longitude, this.creator == 0 ? null : this.creator, this.dateCreated, this.voided, this.voidedBy == 0 ? null : this.voidedBy, this.dateVoided, this.voidReason, this.countyDistrict, this.address3, this.address6, this.address5, this.address4, this.uuid, this.dateChanged, this.changedBy == 0 ? null : this.changedBy, this.startDate, this.endDate};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.personId == 0 ? null : this.personId, this.preferred, this.address1, this.address2, this.cityVillage, this.stateProvince, this.postalCode, this.country, this.latitude, this.longitude, this.creator == 0 ? null : this.creator, this.dateCreated, this.voided, this.voidedBy == 0 ? null : this.voidedBy, this.dateVoided, this.voidReason, this.countyDistrict, this.address3, this.address6, this.address5, this.address4, this.uuid, this.dateChanged, this.changedBy == 0 ? null : this.changedBy, this.startDate, this.endDate, this.personAddressId};		return params; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE person_address SET person_id = ?, preferred = ?, address1 = ?, address2 = ?, city_village = ?, state_province = ?, postal_code = ?, country = ?, latitude = ?, longitude = ?, creator = ?, date_created = ?, voided = ?, voided_by = ?, date_voided = ?, void_reason = ?, county_district = ?, address3 = ?, address6 = ?, address5 = ?, address4 = ?, uuid = ?, date_changed = ?, changed_by = ?, start_date = ?, end_date = ? WHERE person_address_id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.personId == 0 ? null : this.personId) + "," + (this.preferred) + "," + (this.address1 != null ? "\""+ utilities.scapeQuotationMarks(address1)  +"\"" : null) + "," + (this.address2 != null ? "\""+ utilities.scapeQuotationMarks(address2)  +"\"" : null) + "," + (this.cityVillage != null ? "\""+ utilities.scapeQuotationMarks(cityVillage)  +"\"" : null) + "," + (this.stateProvince != null ? "\""+ utilities.scapeQuotationMarks(stateProvince)  +"\"" : null) + "," + (this.postalCode != null ? "\""+ utilities.scapeQuotationMarks(postalCode)  +"\"" : null) + "," + (this.country != null ? "\""+ utilities.scapeQuotationMarks(country)  +"\"" : null) + "," + (this.latitude != null ? "\""+ utilities.scapeQuotationMarks(latitude)  +"\"" : null) + "," + (this.longitude != null ? "\""+ utilities.scapeQuotationMarks(longitude)  +"\"" : null) + "," + (this.creator == 0 ? null : this.creator) + "," + (this.dateCreated != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateCreated)  +"\"" : null) + "," + (this.voided) + "," + (this.voidedBy == 0 ? null : this.voidedBy) + "," + (this.dateVoided != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateVoided)  +"\"" : null) + "," + (this.voidReason != null ? "\""+ utilities.scapeQuotationMarks(voidReason)  +"\"" : null) + "," + (this.countyDistrict != null ? "\""+ utilities.scapeQuotationMarks(countyDistrict)  +"\"" : null) + "," + (this.address3 != null ? "\""+ utilities.scapeQuotationMarks(address3)  +"\"" : null) + "," + (this.address6 != null ? "\""+ utilities.scapeQuotationMarks(address6)  +"\"" : null) + "," + (this.address5 != null ? "\""+ utilities.scapeQuotationMarks(address5)  +"\"" : null) + "," + (this.address4 != null ? "\""+ utilities.scapeQuotationMarks(address4)  +"\"" : null) + "," + (this.uuid != null ? "\""+ utilities.scapeQuotationMarks(uuid)  +"\"" : null) + "," + (this.dateChanged != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateChanged)  +"\"" : null) + "," + (this.changedBy == 0 ? null : this.changedBy) + "," + (this.startDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(startDate)  +"\"" : null) + "," + (this.endDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(endDate)  +"\"" : null); 
	} 
 
	@Override
	public boolean hasParents() {
		if (this.personId != 0) return true;

		if (this.creator != 0) return true;

		if (this.voidedBy != 0) return true;

		if (this.changedBy != 0) return true;

		return false;
	}

	@Override
	public int getParentValue(String parentAttName) {		
		if (parentAttName.equals("personId")) return this.personId;		
		if (parentAttName.equals("creator")) return this.creator;		
		if (parentAttName.equals("voidedBy")) return this.voidedBy;		
		if (parentAttName.equals("changedBy")) return this.changedBy;

		throw new RuntimeException("No found parent for: " + parentAttName);	}

	@Override
	public void changeParentValue(String parentAttName, OpenMRSObject newParent) {		
		if (parentAttName.equals("personId")) {
			this.personId = newParent.getObjectId();
			return;
		}		
		if (parentAttName.equals("creator")) {
			this.creator = newParent.getObjectId();
			return;
		}		
		if (parentAttName.equals("voidedBy")) {
			this.voidedBy = newParent.getObjectId();
			return;
		}		
		if (parentAttName.equals("changedBy")) {
			this.changedBy = newParent.getObjectId();
			return;
		}

		throw new RuntimeException("No found parent for: " + parentAttName);
	}


}