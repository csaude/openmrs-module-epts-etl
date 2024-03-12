package org.openmrs.module.epts.etl.model.pojo.openmrs.community_data_extraction;

import org.openmrs.module.epts.etl.model.pojo.generic.*; 
 
import org.openmrs.module.epts.etl.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.epts.etl.utilities.AttDefinedElements; 
 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class CommunityArvPosologyVO extends AbstractDatabaseObject implements DatabaseObject { 
	private Integer id;
	private Integer patientId;
	private Integer encounterId;
	private java.util.Date visitDate;
	private String dmcType;
	private String therapeuticLine;
	private String posology;
 
	public CommunityArvPosologyVO() { 
		this.metadata = false;
	} 
 
	public void setId(Integer id){ 
	 	this.id = id;
	}
 
	public Integer getId(){ 
		return this.id;
	}
 
	public void setPatientId(Integer patientId){ 
	 	this.patientId = patientId;
	}
 
	public Integer getPatientId(){ 
		return this.patientId;
	}
 
	public void setEncounterId(Integer encounterId){ 
	 	this.encounterId = encounterId;
	}
 
	public Integer getEncounterId(){ 
		return this.encounterId;
	}
 
	public void setVisitDate(java.util.Date visitDate){ 
	 	this.visitDate = visitDate;
	}
 
	public java.util.Date getVisitDate(){ 
		return this.visitDate;
	}
 
	public void setDmcType(String dmcType){ 
	 	this.dmcType = dmcType;
	}
 
	public String getDmcType(){ 
		return this.dmcType;
	}
 
	public void setTherapeuticLine(String therapeuticLine){ 
	 	this.therapeuticLine = therapeuticLine;
	}
 
	public String getTherapeuticLine(){ 
		return this.therapeuticLine;
	}
 
	public void setPosology(String posology){ 
	 	this.posology = posology;
	}


 
	public String getPosology(){ 
		return this.posology;
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
		if (rs.getObject("patient_id") != null) this.patientId = rs.getInt("patient_id");
		if (rs.getObject("encounter_id") != null) this.encounterId = rs.getInt("encounter_id");
		this.visitDate =  rs.getTimestamp("visit_date") != null ? new java.util.Date( rs.getTimestamp("visit_date").getTime() ) : null;
		this.dmcType = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("dmc_type") != null ? rs.getString("dmc_type").trim() : null);
		this.therapeuticLine = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("therapeutic_line") != null ? rs.getString("therapeutic_line").trim() : null);
		this.posology = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("posology") != null ? rs.getString("posology").trim() : null);
	} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "id"; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithoutObjectId(){ 
 		return "INSERT INTO community_arv_posology(patient_id, encounter_id, visit_date, dmc_type, therapeutic_line, posology) VALUES( ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithoutObjectId(){ 
 		Object[] params = {this.patientId, this.encounterId, this.visitDate, this.dmcType, this.therapeuticLine, this.posology};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithObjectId(){ 
 		return "INSERT INTO community_arv_posology(id, patient_id, encounter_id, visit_date, dmc_type, therapeutic_line, posology) VALUES(?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithObjectId(){ 
 		Object[] params = {this.id, this.patientId, this.encounterId, this.visitDate, this.dmcType, this.therapeuticLine, this.posology};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.patientId, this.encounterId, this.visitDate, this.dmcType, this.therapeuticLine, this.posology, this.id};		return params; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE community_arv_posology SET patient_id = ?, encounter_id = ?, visit_date = ?, dmc_type = ?, therapeutic_line = ?, posology = ? WHERE id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.patientId) + "," + (this.encounterId) + "," + (this.visitDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(visitDate)  +"\"" : null) + "," + (this.dmcType != null ? "\""+ utilities.scapeQuotationMarks(dmcType)  +"\"" : null) + "," + (this.therapeuticLine != null ? "\""+ utilities.scapeQuotationMarks(therapeuticLine)  +"\"" : null) + "," + (this.posology != null ? "\""+ utilities.scapeQuotationMarks(posology)  +"\"" : null); 
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
		return "community_arv_posology";
	}


}