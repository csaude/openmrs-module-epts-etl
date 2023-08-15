package org.openmrs.module.eptssync.model.pojo.mozart.old;

import org.openmrs.module.eptssync.model.pojo.generic.*; 
 
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.eptssync.utilities.AttDefinedElements; 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class LocationVO extends AbstractDatabaseObject implements DatabaseObject { 
	private Integer id;
	private Integer locationId;
	private String locationUuid;
	private String sismaId;
	private String datimId;
	private String sismaHddId;
	private String name;
	private String provinceName;
	private String provinceDistrict;
	private byte selected;
 
	public LocationVO() { 
		this.metadata = false;
	} 
 
	public void setId(Integer id){ 
	 	this.id = id;
	}
 
	public Integer getId(){ 
		return this.id;
	}
 
	public void setLocationId(Integer locationId){ 
	 	this.locationId = locationId;
	}
 
	public Integer getLocationId(){ 
		return this.locationId;
	}
 
	public void setLocationUuid(String locationUuid){ 
	 	this.locationUuid = locationUuid;
	}
 
	public String getLocationUuid(){ 
		return this.locationUuid;
	}
 
	public void setSismaId(String sismaId){ 
	 	this.sismaId = sismaId;
	}
 
	public String getSismaId(){ 
		return this.sismaId;
	}
 
	public void setDatimId(String datimId){ 
	 	this.datimId = datimId;
	}
 
	public String getDatimId(){ 
		return this.datimId;
	}
 
	public void setSismaHddId(String sismaHddId){ 
	 	this.sismaHddId = sismaHddId;
	}
 
	public String getSismaHddId(){ 
		return this.sismaHddId;
	}
 
	public void setName(String name){ 
	 	this.name = name;
	}
 
	public String getName(){ 
		return this.name;
	}
 
	public void setProvinceName(String provinceName){ 
	 	this.provinceName = provinceName;
	}
 
	public String getProvinceName(){ 
		return this.provinceName;
	}
 
	public void setProvinceDistrict(String provinceDistrict){ 
	 	this.provinceDistrict = provinceDistrict;
	}
 
	public String getProvinceDistrict(){ 
		return this.provinceDistrict;
	}
 
	public void setSelected(byte selected){ 
	 	this.selected = selected;
	}


 
	public byte getSelected(){ 
		return this.selected;
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
		if (rs.getObject("location_id") != null) this.locationId = rs.getInt("location_id");
		this.locationUuid = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("location_uuid") != null ? rs.getString("location_uuid").trim() : null);
		this.sismaId = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("sisma_id") != null ? rs.getString("sisma_id").trim() : null);
		this.datimId = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("datim_id") != null ? rs.getString("datim_id").trim() : null);
		this.sismaHddId = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("sisma_hdd_id") != null ? rs.getString("sisma_hdd_id").trim() : null);
		this.name = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("name") != null ? rs.getString("name").trim() : null);
		this.provinceName = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("province_name") != null ? rs.getString("province_name").trim() : null);
		this.provinceDistrict = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("province_district") != null ? rs.getString("province_district").trim() : null);
		this.selected = rs.getByte("selected");
	} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "id"; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithoutObjectId(){ 
 		return "INSERT INTO location(location_id, location_uuid, sisma_id, datim_id, sisma_hdd_id, name, province_name, province_district, selected) VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithoutObjectId(){ 
 		Object[] params = {this.locationId, this.locationUuid, this.sismaId, this.datimId, this.sismaHddId, this.name, this.provinceName, this.provinceDistrict, this.selected};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithObjectId(){ 
 		return "INSERT INTO location(id, location_id, location_uuid, sisma_id, datim_id, sisma_hdd_id, name, province_name, province_district, selected) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithObjectId(){ 
 		Object[] params = {this.id, this.locationId, this.locationUuid, this.sismaId, this.datimId, this.sismaHddId, this.name, this.provinceName, this.provinceDistrict, this.selected};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.locationId, this.locationUuid, this.sismaId, this.datimId, this.sismaHddId, this.name, this.provinceName, this.provinceDistrict, this.selected, this.id};		return params; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE location SET location_id = ?, location_uuid = ?, sisma_id = ?, datim_id = ?, sisma_hdd_id = ?, name = ?, province_name = ?, province_district = ?, selected = ? WHERE id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.locationId) + "," + (this.locationUuid != null ? "\""+ utilities.scapeQuotationMarks(locationUuid)  +"\"" : null) + "," + (this.sismaId != null ? "\""+ utilities.scapeQuotationMarks(sismaId)  +"\"" : null) + "," + (this.datimId != null ? "\""+ utilities.scapeQuotationMarks(datimId)  +"\"" : null) + "," + (this.sismaHddId != null ? "\""+ utilities.scapeQuotationMarks(sismaHddId)  +"\"" : null) + "," + (this.name != null ? "\""+ utilities.scapeQuotationMarks(name)  +"\"" : null) + "," + (this.provinceName != null ? "\""+ utilities.scapeQuotationMarks(provinceName)  +"\"" : null) + "," + (this.provinceDistrict != null ? "\""+ utilities.scapeQuotationMarks(provinceDistrict)  +"\"" : null) + "," + (this.selected); 
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


}