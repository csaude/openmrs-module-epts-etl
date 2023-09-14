package org.openmrs.module.eptssync.model.pojo.openmrs._default;

import org.openmrs.module.eptssync.model.pojo.generic.*; 
 
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.eptssync.utilities.AttDefinedElements; 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class SrcTblVO extends AbstractDatabaseObject implements DatabaseObject { 
	private Integer id;
	private String field01;
	private String field02;
	private java.util.Date creationDate;
 
	public SrcTblVO() { 
		this.metadata = false;
	} 
 
	public void setId(Integer id){ 
	 	this.id = id;
	}
 
	public Integer getId(){ 
		return this.id;
	}
 
	public void setField01(String field01){ 
	 	this.field01 = field01;
	}
 
	public String getField01(){ 
		return this.field01;
	}
 
	public void setField02(String field02){ 
	 	this.field02 = field02;
	}
 
	public String getField02(){ 
		return this.field02;
	}
 
	public void setCreationDate(java.util.Date creationDate){ 
	 	this.creationDate = creationDate;
	}


 
	public java.util.Date getCreationDate(){ 
		return this.creationDate;
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
		this.field01 = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("field_01") != null ? rs.getString("field_01").trim() : null);
		this.field02 = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("field_02") != null ? rs.getString("field_02").trim() : null);
		this.creationDate =  rs.getTimestamp("creation_date") != null ? new java.util.Date( rs.getTimestamp("creation_date").getTime() ) : null;
	} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "id"; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithoutObjectId(){ 
 		return "INSERT INTO src_tbl(field_01, field_02, creation_date) VALUES( ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithoutObjectId(){ 
 		Object[] params = {this.field01, this.field02, this.creationDate};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithObjectId(){ 
 		return "INSERT INTO src_tbl(id, field_01, field_02, creation_date) VALUES(?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithObjectId(){ 
 		Object[] params = {this.id, this.field01, this.field02, this.creationDate};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.field01, this.field02, this.creationDate, this.id};		return params; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE src_tbl SET field_01 = ?, field_02 = ?, creation_date = ? WHERE id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.field01 != null ? "\""+ utilities.scapeQuotationMarks(field01)  +"\"" : null) + "," + (this.field02 != null ? "\""+ utilities.scapeQuotationMarks(field02)  +"\"" : null) + "," + (this.creationDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(creationDate)  +"\"" : null); 
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