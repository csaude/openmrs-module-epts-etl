package org.openmrs.module.eptssync.model.openmrs; 
 
import org.openmrs.module.eptssync.model.GenericSyncRecordDAO; 
 
import org.openmrs.module.eptssync.utilities.db.conn.DBException; 
import org.openmrs.module.eptssync.utilities.db.conn.OpenConnection; 
import org.openmrs.module.eptssync.exceptions.ParentNotYetMigratedException; 
 
import java.sql.Connection; 
 
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
	private java.util.Date dateCreated;
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
	private String uuid;
	private int changedBy;
	private java.util.Date dateChanged;
	private java.util.Date lastSyncDate;
	private int originRecordId;
	private String originAppLocationCode;
 
	public LocationVO() { 
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
 
	public void setDateCreated(java.util.Date dateCreated){ 
	 	this.dateCreated = dateCreated;
	}
 
	public java.util.Date getDateCreated(){ 
		return this.dateCreated;
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
 
	public void setUuid(String uuid){ 
	 	this.uuid = uuid;
	}
 
	public String getUuid(){ 
		return this.uuid;
	}
 
	public void setChangedBy(int changedBy){ 
	 	this.changedBy = changedBy;
	}
 
	public int getChangedBy(){ 
		return this.changedBy;
	}
 
	public void setDateChanged(java.util.Date dateChanged){ 
	 	this.dateChanged = dateChanged;
	}
 
	public java.util.Date getDateChanged(){ 
		return this.dateChanged;
	}
 
	public void setLastSyncDate(java.util.Date lastSyncDate){ 
	 	this.lastSyncDate = lastSyncDate;
	}
 
	public java.util.Date getLastSyncDate(){ 
		return this.lastSyncDate;
	}
 
	public void setOriginRecordId(int originRecordId){ 
	 	this.originRecordId = originRecordId;
	}
 
	public int getOriginRecordId(){ 
		return this.originRecordId;
	}
 
	public void setOriginAppLocationCode(String originAppLocationCode){ 
	 	this.originAppLocationCode = originAppLocationCode;
	}


 
	public String getOriginAppLocationCode(){ 
		return this.originAppLocationCode;
	}
 
	public int getObjectId() { 
 		return this.locationId; 
	} 
 
	public void setObjectId(int selfId){ 
		this.locationId = selfId; 
	} 
 
	public void refreshLastSyncDate(OpenConnection conn){ 
		try{
			GenericSyncRecordDAO.refreshLastSyncDate(this, conn); 
		}catch(DBException e) {
			throw new RuntimeException(e);
		}
	}

	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "location_id"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParams(){ 
 		Object[] params = {this.name, this.description, this.address1, this.address2, this.cityVillage, this.stateProvince, this.postalCode, this.country, this.latitude, this.longitude, this.creator == 0 ? null : this.creator, this.dateCreated, this.countyDistrict, this.address3, this.address6, this.address5, this.address4, this.retired, this.retiredBy == 0 ? null : this.retiredBy, this.dateRetired, this.retireReason, this.parentLocation == 0 ? null : this.parentLocation, this.uuid, this.changedBy == 0 ? null : this.changedBy, this.dateChanged, this.lastSyncDate, this.originRecordId, this.originAppLocationCode};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.name, this.description, this.address1, this.address2, this.cityVillage, this.stateProvince, this.postalCode, this.country, this.latitude, this.longitude, this.creator == 0 ? null : this.creator, this.dateCreated, this.countyDistrict, this.address3, this.address6, this.address5, this.address4, this.retired, this.retiredBy == 0 ? null : this.retiredBy, this.dateRetired, this.retireReason, this.parentLocation == 0 ? null : this.parentLocation, this.uuid, this.changedBy == 0 ? null : this.changedBy, this.dateChanged, this.lastSyncDate, this.originRecordId, this.originAppLocationCode, this.locationId};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQL(){ 
 		return "INSERT INTO location(name, description, address1, address2, city_village, state_province, postal_code, country, latitude, longitude, creator, date_created, county_district, address3, address6, address5, address4, retired, retired_by, date_retired, retire_reason, parent_location, uuid, changed_by, date_changed, last_sync_date, origin_record_id, origin_app_location_code) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE location SET name = ?, description = ?, address1 = ?, address2 = ?, city_village = ?, state_province = ?, postal_code = ?, country = ?, latitude = ?, longitude = ?, creator = ?, date_created = ?, county_district = ?, address3 = ?, address6 = ?, address5 = ?, address4 = ?, retired = ?, retired_by = ?, date_retired = ?, retire_reason = ?, parent_location = ?, uuid = ?, changed_by = ?, date_changed = ?, last_sync_date = ?, origin_record_id = ?, origin_app_location_code = ? WHERE location_id = ?;"; 
	} 
 
	public void loadDestParentInfo(Connection conn) throws ParentNotYetMigratedException, DBException {
		OpenMRSObject parentOnDestination = null;
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.UsersVO.class, this.changedBy, true, conn); 
		this.changedBy = 0;
		if (parentOnDestination  != null) this.changedBy = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.LocationVO.class, this.parentLocation, true, conn); 
		this.parentLocation = 0;
		if (parentOnDestination  != null) this.parentLocation = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.UsersVO.class, this.creator, false, conn); 
		this.creator = 0;
		if (parentOnDestination  != null) this.creator = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.UsersVO.class, this.retiredBy, true, conn); 
		this.retiredBy = 0;
		if (parentOnDestination  != null) this.retiredBy = parentOnDestination.getObjectId();
 
	}
}