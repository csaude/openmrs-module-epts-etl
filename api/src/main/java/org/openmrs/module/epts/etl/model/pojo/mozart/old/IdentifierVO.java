package org.openmrs.module.epts.etl.model.pojo.mozart.old;

import java.sql.SQLException;

import org.openmrs.module.epts.etl.model.pojo.generic.*;
import org.openmrs.module.epts.etl.utilities.AttDefinedElements;
import org.openmrs.module.epts.etl.utilities.DateAndTimeUtilities;

import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class IdentifierVO extends AbstractDatabaseObject implements DatabaseObject { 
	private Integer identifierSeq;
	private String patientUuid;
	private Integer identifierType;
	private String identifierValue;
	private byte preferred;
	private String identifierUuid;
 
	public IdentifierVO() { 
		this.metadata = false;
	} 
 
	public void setIdentifierSeq(Integer identifierSeq){ 
	 	this.identifierSeq = identifierSeq;
	}
 
	public Integer getIdentifierSeq(){ 
		return this.identifierSeq;
	}
 
	public void setPatientUuid(String patientUuid){ 
	 	this.patientUuid = patientUuid;
	}
 
	public String getPatientUuid(){ 
		return this.patientUuid;
	}
 
	public void setIdentifierType(Integer identifierType){ 
	 	this.identifierType = identifierType;
	}
 
	public Integer getIdentifierType(){ 
		return this.identifierType;
	}
 
	public void setIdentifierValue(String identifierValue){ 
	 	this.identifierValue = identifierValue;
	}
 
	public String getIdentifierValue(){ 
		return this.identifierValue;
	}
 
	public void setPreferred(byte preferred){ 
	 	this.preferred = preferred;
	}
 
	public byte getPreferred(){ 
		return this.preferred;
	}
 
	public void setIdentifierUuid(String identifierUuid){ 
	 	this.identifierUuid = identifierUuid;
	}


 
	public String getIdentifierUuid(){ 
		return this.identifierUuid;
	}
 
	public Integer getObjectId() { 
 		return this.identifierSeq; 
	} 
 
	public void setObjectId(Integer selfId){ 
		this.identifierSeq = selfId; 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		super.load(rs);
		if (rs.getObject("identifier_seq") != null) this.identifierSeq = rs.getInt("identifier_seq");
		this.patientUuid = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("patient_uuid") != null ? rs.getString("patient_uuid").trim() : null);
		if (rs.getObject("identifier_type") != null) this.identifierType = rs.getInt("identifier_type");
		this.identifierValue = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("identifier_value") != null ? rs.getString("identifier_value").trim() : null);
		this.preferred = rs.getByte("preferred");
		this.identifierUuid = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("identifier_uuid") != null ? rs.getString("identifier_uuid").trim() : null);
	} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "identifier_seq"; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithoutObjectId(){ 
 		return "INSERT INTO identifier(patient_uuid, identifier_type, identifier_value, preferred, identifier_uuid) VALUES( ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithoutObjectId(){ 
 		Object[] params = {this.patientUuid, this.identifierType, this.identifierValue, this.preferred, this.identifierUuid};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithObjectId(){ 
 		return "INSERT INTO identifier(identifier_seq, patient_uuid, identifier_type, identifier_value, preferred, identifier_uuid) VALUES(?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithObjectId(){ 
 		Object[] params = {this.identifierSeq, this.patientUuid, this.identifierType, this.identifierValue, this.preferred, this.identifierUuid};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.patientUuid, this.identifierType, this.identifierValue, this.preferred, this.identifierUuid, this.identifierSeq};		return params; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE identifier SET patient_uuid = ?, identifier_type = ?, identifier_value = ?, preferred = ?, identifier_uuid = ? WHERE identifier_seq = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.patientUuid != null ? "\""+ utilities.scapeQuotationMarks(patientUuid)  +"\"" : null) + "," + (this.identifierType) + "," + (this.identifierValue != null ? "\""+ utilities.scapeQuotationMarks(identifierValue)  +"\"" : null) + "," + (this.preferred) + "," + (this.identifierUuid != null ? "\""+ utilities.scapeQuotationMarks(identifierUuid)  +"\"" : null); 
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