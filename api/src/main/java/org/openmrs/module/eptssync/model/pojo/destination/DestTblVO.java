package org.openmrs.module.eptssync.model.pojo.destination;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.openmrs.module.eptssync.model.pojo.generic.AbstractDatabaseObject;
import org.openmrs.module.eptssync.model.pojo.generic.DatabaseObject;
import org.openmrs.module.eptssync.utilities.AttDefinedElements;
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class DestTblVO extends AbstractDatabaseObject implements DatabaseObject {
	
	private Integer id;
	
	private String field01;
	
	private String field02;
	
	private java.util.Date originCreationDate;
	
	public DestTblVO() {
		this.metadata = false;
	}
	
	public void setId(Integer id) {
		this.id = id;
	}
	
	public Integer getId() {
		return this.id;
	}
	
	public void setField01(String field01) {
		this.field01 = field01;
	}
	
	public String getField01() {
		return this.field01;
	}
	
	public void setField02(String field02) {
		this.field02 = field02;
	}
	
	public String getField02() {
		return this.field02;
	}
	
	public java.util.Date getOriginCreationDate() {
		return originCreationDate;
	}
	
	public void setOriginCreationDate(java.util.Date originCreationDate) {
		this.originCreationDate = originCreationDate;
	}
	
	public Integer getObjectId() {
		return this.id;
	}
	
	public void setObjectId(Integer selfId) {
		this.id = selfId;
	}
	
	public void load(ResultSet rs) throws SQLException {
		super.load(rs);
		if (rs.getObject("id") != null)
			this.id = rs.getInt("id");
		this.field01 = AttDefinedElements
		        .removeStrangeCharactersOnString(rs.getString("field_01") != null ? rs.getString("field_01").trim() : null);
		this.field02 = AttDefinedElements
		        .removeStrangeCharactersOnString(rs.getString("field_02") != null ? rs.getString("field_02").trim() : null);
		this.originCreationDate = rs.getTimestamp("origin_creation_date") != null
		        ? new java.util.Date(rs.getTimestamp("origin_creation_date").getTime())
		        : null;
	}
	
	@JsonIgnore
	public String generateDBPrimaryKeyAtt() {
		return "id";
	}
	
	@JsonIgnore
	public String getInsertSQLWithoutObjectId() {
		return "INSERT INTO dest_tbl(field_01, field_02, origin_creation_date) VALUES( ?, ?, ?);";
	}
	
	@JsonIgnore
	public Object[] getInsertParamsWithoutObjectId() {
		Object[] params = { this.field01, this.field02, this.originCreationDate };
		return params;
	}
	
	@JsonIgnore
	public String getInsertSQLWithObjectId() {
		return "INSERT INTO dest_tbl(id, field_01, field_02, origin_creation_date) VALUES(?, ?, ?, ?);";
	}
	
	@JsonIgnore
	public Object[] getInsertParamsWithObjectId() {
		Object[] params = { this.id, this.field01, this.field02, this.originCreationDate };
		return params;
	}
	
	@JsonIgnore
	public Object[] getUpdateParams() {
		Object[] params = { this.field01, this.field02, this.originCreationDate, this.id };
		return params;
	}
	
	@JsonIgnore
	public String getUpdateSQL() {
		return "UPDATE dest_tbl SET field_01 = ?, field_02 = ?, origin_creation_date = ? WHERE id = ?;";
	}
	
	@JsonIgnore
	public String generateInsertValues() {
		return "" + (this.field01 != null ? "\"" + utilities.scapeQuotationMarks(field01) + "\"" : null) + ","
		        + (this.field02 != null ? "\"" + utilities.scapeQuotationMarks(field02) + "\"" : null) + ","
		        + (this.originCreationDate != null
		                ? "\"" + DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(originCreationDate) + "\""
		                : null);
	}
	
	@Override
	public boolean hasParents() {
		return false;
	}
	
	@Override
	public Integer getParentValue(String parentAttName) {
		
		throw new RuntimeException("No found parent for: " + parentAttName);
	}
	
	@Override
	public void changeParentValue(String parentAttName, DatabaseObject newParent) {
		
		throw new RuntimeException("No found parent for: " + parentAttName);
	}
	
	@Override
	public void setParentToNull(String parentAttName) {
		
		throw new RuntimeException("No found parent for: " + parentAttName);
	}
	
}
