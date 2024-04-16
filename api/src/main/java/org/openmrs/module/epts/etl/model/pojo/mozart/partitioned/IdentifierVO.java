package org.openmrs.module.epts.etl.model.pojo.mozart.partitioned;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.openmrs.module.epts.etl.model.pojo.generic.AbstractDatabaseObject;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObject;
import org.openmrs.module.epts.etl.utilities.AttDefinedElements;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class IdentifierVO extends AbstractDatabaseObject implements DatabaseObject {
	
	private Integer identifierSeq;
	
	private String patientUuid;
	
	private Integer identifierType;
	
	private String identifierValue;
	
	private Byte preferred;
	
	private String identifierUuid;
	
	public IdentifierVO() {
		this.metadata = false;
	}
	
	public void setIdentifierSeq(Integer identifierSeq) {
		this.identifierSeq = identifierSeq;
	}
	
	public Integer getIdentifierSeq() {
		return this.identifierSeq;
	}
	
	public void setPatientUuid(String patientUuid) {
		this.patientUuid = patientUuid;
	}
	
	public String getPatientUuid() {
		return this.patientUuid;
	}
	
	public void setIdentifierType(Integer identifierType) {
		this.identifierType = identifierType;
	}
	
	public Integer getIdentifierType() {
		return this.identifierType;
	}
	
	public void setIdentifierValue(String identifierValue) {
		this.identifierValue = identifierValue;
	}
	
	public String getIdentifierValue() {
		return this.identifierValue;
	}
	
	public void setPreferred(Byte preferred) {
		this.preferred = preferred;
	}
	
	public Byte getPreferred() {
		return this.preferred;
	}
	
	public void setIdentifierUuid(String identifierUuid) {
		this.identifierUuid = identifierUuid;
	}
	
	public String getIdentifierUuid() {
		return this.identifierUuid;
	}
	
	@Override
	public void load(ResultSet rs) throws SQLException {
		super.load(rs);
		
		if (rs.getObject("identifier_seq") != null)
			this.identifierSeq = rs.getInt("identifier_seq");
		this.patientUuid = AttDefinedElements.removeStrangeCharactersOnString(
		    rs.getString("patient_uuid") != null ? rs.getString("patient_uuid").trim() : null);
		if (rs.getObject("identifier_type") != null)
			this.identifierType = rs.getInt("identifier_type");
		this.identifierValue = AttDefinedElements.removeStrangeCharactersOnString(
		    rs.getString("identifier_value") != null ? rs.getString("identifier_value").trim() : null);
		this.preferred = rs.getByte("preferred");
		this.identifierUuid = AttDefinedElements.removeStrangeCharactersOnString(
		    rs.getString("identifier_uuid") != null ? rs.getString("identifier_uuid").trim() : null);
	}
	
	@JsonIgnore
	@Override
	public String getInsertSQLWithoutObjectId() {
		return "INSERT INTO identifier(patient_uuid, identifier_type, identifier_value, preferred, identifier_uuid) VALUES( ?, ?, ?, ?, ?);";
	}
	
	@JsonIgnore
	@Override
	public String getInsertSQLWithObjectId() {
		return "INSERT INTO identifier(identifier_seq, patient_uuid, identifier_type, identifier_value, preferred, identifier_uuid) VALUES( ?, ?, ?, ?, ?, ?);";
	}
	
	@JsonIgnore
	@Override
	public Object[] getInsertParamsWithoutObjectId() {
		Object[] params = { this.patientUuid, this.identifierType, this.identifierValue, this.preferred,
		        this.identifierUuid };
		return params;
	}
	
	@JsonIgnore
	@Override
	public Object[] getInsertParamsWithObjectId() {
		Object[] params = { this.identifierSeq, this.patientUuid, this.identifierType, this.identifierValue, this.preferred,
		        this.identifierUuid };
		return params;
	}
	
	@JsonIgnore
	@Override
	public Object[] getUpdateParams() {
		Object[] params = { this.identifierSeq, this.patientUuid, this.identifierType, this.identifierValue, this.preferred,
		        this.identifierUuid, this.identifierSeq };
		return params;
	}
	
	@JsonIgnore
	@Override
	public String getUpdateSQL() {
		return "UPDATE identifier SET identifier_seq = ?, patient_uuid = ?, identifier_type = ?, identifier_value = ?, preferred = ?, identifier_uuid = ? WHERE identifier_seq = ? ";
	}
	
	@JsonIgnore
	@Override
	public String generateInsertValuesWithoutObjectId() {
		return "" + (this.patientUuid != null ? "\"" + utilities.scapeQuotationMarks(patientUuid) + "\"" : null) + ","
		        + (this.identifierType) + ","
		        + (this.identifierValue != null ? "\"" + utilities.scapeQuotationMarks(identifierValue) + "\"" : null) + ","
		        + (this.preferred) + ","
		        + (this.identifierUuid != null ? "\"" + utilities.scapeQuotationMarks(identifierUuid) + "\"" : null);
	}
	
	@JsonIgnore
	@Override
	public String generateInsertValuesWithObjectId() {
		return "" + (this.identifierSeq) + ","
		        + (this.patientUuid != null ? "\"" + utilities.scapeQuotationMarks(patientUuid) + "\"" : null) + ","
		        + (this.identifierType) + ","
		        + (this.identifierValue != null ? "\"" + utilities.scapeQuotationMarks(identifierValue) + "\"" : null) + ","
		        + (this.preferred) + ","
		        + (this.identifierUuid != null ? "\"" + utilities.scapeQuotationMarks(identifierUuid) + "\"" : null);
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
	public String generateTableName() {
		return "identifier";
	}
	
}
