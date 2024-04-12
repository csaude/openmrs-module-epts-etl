package org.openmrs.module.epts.etl.model.pojo.mozart.dst;

import org.openmrs.module.epts.etl.model.pojo.generic.*; 
 
import org.openmrs.module.epts.etl.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.epts.etl.utilities.AttDefinedElements; 
 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class LaboratoryVO extends AbstractDatabaseObject implements DatabaseObject { 
	private Integer id;
	private String encounterUuid;
	private java.util.Date encounterDate;
	private Integer labTestId;
	private Integer request;
	private java.util.Date orderDate;
	private java.util.Date sampleCollectionDate;
	private java.util.Date resultReportDate;
	private Integer resultQualitativeId;
	private Double resultNumeric;
	private String resultUnits;
	private String resultComment;
	private Integer specimenTypeId;
	private String labtestUuid;
	private Integer formId;
	private Integer encounterType;
	private String patientUuid;
	private java.util.Date createdDate;
	private java.util.Date changeDate;
	private String locationUuid;
	private String sourceDatabase;
 
	public LaboratoryVO() { 
		this.metadata = false;
	} 
 
	public void setId(Integer id){ 
	 	this.id = id;
	}
 
	public Integer getId(){ 
		return this.id;
	}
 
	public void setEncounterUuid(String encounterUuid){ 
	 	this.encounterUuid = encounterUuid;
	}
 
	public String getEncounterUuid(){ 
		return this.encounterUuid;
	}
 
	public void setEncounterDate(java.util.Date encounterDate){ 
	 	this.encounterDate = encounterDate;
	}
 
	public java.util.Date getEncounterDate(){ 
		return this.encounterDate;
	}
 
	public void setLabTestId(Integer labTestId){ 
	 	this.labTestId = labTestId;
	}
 
	public Integer getLabTestId(){ 
		return this.labTestId;
	}
 
	public void setRequest(Integer request){ 
	 	this.request = request;
	}
 
	public Integer getRequest(){ 
		return this.request;
	}
 
	public void setOrderDate(java.util.Date orderDate){ 
	 	this.orderDate = orderDate;
	}
 
	public java.util.Date getOrderDate(){ 
		return this.orderDate;
	}
 
	public void setSampleCollectionDate(java.util.Date sampleCollectionDate){ 
	 	this.sampleCollectionDate = sampleCollectionDate;
	}
 
	public java.util.Date getSampleCollectionDate(){ 
		return this.sampleCollectionDate;
	}
 
	public void setResultReportDate(java.util.Date resultReportDate){ 
	 	this.resultReportDate = resultReportDate;
	}
 
	public java.util.Date getResultReportDate(){ 
		return this.resultReportDate;
	}
 
	public void setResultQualitativeId(Integer resultQualitativeId){ 
	 	this.resultQualitativeId = resultQualitativeId;
	}
 
	public Integer getResultQualitativeId(){ 
		return this.resultQualitativeId;
	}
 
	public void setResultNumeric(Double resultNumeric){ 
	 	this.resultNumeric = resultNumeric;
	}
 
	public Double getResultNumeric(){ 
		return this.resultNumeric;
	}
 
	public void setResultUnits(String resultUnits){ 
	 	this.resultUnits = resultUnits;
	}
 
	public String getResultUnits(){ 
		return this.resultUnits;
	}
 
	public void setResultComment(String resultComment){ 
	 	this.resultComment = resultComment;
	}
 
	public String getResultComment(){ 
		return this.resultComment;
	}
 
	public void setSpecimenTypeId(Integer specimenTypeId){ 
	 	this.specimenTypeId = specimenTypeId;
	}
 
	public Integer getSpecimenTypeId(){ 
		return this.specimenTypeId;
	}
 
	public void setLabtestUuid(String labtestUuid){ 
	 	this.labtestUuid = labtestUuid;
	}
 
	public String getLabtestUuid(){ 
		return this.labtestUuid;
	}
 
	public void setFormId(Integer formId){ 
	 	this.formId = formId;
	}
 
	public Integer getFormId(){ 
		return this.formId;
	}
 
	public void setEncounterType(Integer encounterType){ 
	 	this.encounterType = encounterType;
	}
 
	public Integer getEncounterType(){ 
		return this.encounterType;
	}
 
	public void setPatientUuid(String patientUuid){ 
	 	this.patientUuid = patientUuid;
	}
 
	public String getPatientUuid(){ 
		return this.patientUuid;
	}
 
	public void setCreatedDate(java.util.Date createdDate){ 
	 	this.createdDate = createdDate;
	}
 
	public java.util.Date getCreatedDate(){ 
		return this.createdDate;
	}
 
	public void setChangeDate(java.util.Date changeDate){ 
	 	this.changeDate = changeDate;
	}
 
	public java.util.Date getChangeDate(){ 
		return this.changeDate;
	}
 
	public void setLocationUuid(String locationUuid){ 
	 	this.locationUuid = locationUuid;
	}
 
	public String getLocationUuid(){ 
		return this.locationUuid;
	}
 
	public void setSourceDatabase(String sourceDatabase){ 
	 	this.sourceDatabase = sourceDatabase;
	}


 
	public String getSourceDatabase(){ 
		return this.sourceDatabase;
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
		this.encounterUuid = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("encounter_uuid") != null ? rs.getString("encounter_uuid").trim() : null);
		this.encounterDate =  rs.getTimestamp("encounter_date") != null ? new java.util.Date( rs.getTimestamp("encounter_date").getTime() ) : null;
		if (rs.getObject("lab_test_id") != null) this.labTestId = rs.getInt("lab_test_id");
		if (rs.getObject("request") != null) this.request = rs.getInt("request");
		this.orderDate =  rs.getTimestamp("order_date") != null ? new java.util.Date( rs.getTimestamp("order_date").getTime() ) : null;
		this.sampleCollectionDate =  rs.getTimestamp("sample_collection_date") != null ? new java.util.Date( rs.getTimestamp("sample_collection_date").getTime() ) : null;
		this.resultReportDate =  rs.getTimestamp("result_report_date") != null ? new java.util.Date( rs.getTimestamp("result_report_date").getTime() ) : null;
		if (rs.getObject("result_qualitative_id") != null) this.resultQualitativeId = rs.getInt("result_qualitative_id");
		if (rs.getObject("result_numeric") != null) this.resultNumeric = rs.getDouble("result_numeric");
		this.resultUnits = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("result_units") != null ? rs.getString("result_units").trim() : null);
		this.resultComment = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("result_comment") != null ? rs.getString("result_comment").trim() : null);
		if (rs.getObject("specimen_type_id") != null) this.specimenTypeId = rs.getInt("specimen_type_id");
		this.labtestUuid = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("labtest_uuid") != null ? rs.getString("labtest_uuid").trim() : null);
		if (rs.getObject("form_id") != null) this.formId = rs.getInt("form_id");
		if (rs.getObject("encounter_type") != null) this.encounterType = rs.getInt("encounter_type");
		this.patientUuid = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("patient_uuid") != null ? rs.getString("patient_uuid").trim() : null);
		this.createdDate =  rs.getTimestamp("created_date") != null ? new java.util.Date( rs.getTimestamp("created_date").getTime() ) : null;
		this.changeDate =  rs.getTimestamp("change_date") != null ? new java.util.Date( rs.getTimestamp("change_date").getTime() ) : null;
		this.locationUuid = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("location_uuid") != null ? rs.getString("location_uuid").trim() : null);
		this.sourceDatabase = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("source_database") != null ? rs.getString("source_database").trim() : null);
	} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "id"; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithoutObjectId(){ 
 		return "INSERT INTO laboratory(encounter_uuid, encounter_date, lab_test_id, request, order_date, sample_collection_date, result_report_date, result_qualitative_id, result_numeric, result_units, result_comment, specimen_type_id, labtest_uuid, form_id, encounter_type, patient_uuid, created_date, change_date, location_uuid, source_database) VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithoutObjectId(){ 
 		Object[] params = {this.encounterUuid, this.encounterDate, this.labTestId, this.request, this.orderDate, this.sampleCollectionDate, this.resultReportDate, this.resultQualitativeId, this.resultNumeric, this.resultUnits, this.resultComment, this.specimenTypeId, this.labtestUuid, this.formId, this.encounterType, this.patientUuid, this.createdDate, this.changeDate, this.locationUuid, this.sourceDatabase};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithObjectId(){ 
 		return "INSERT INTO laboratory(id, encounter_uuid, encounter_date, lab_test_id, request, order_date, sample_collection_date, result_report_date, result_qualitative_id, result_numeric, result_units, result_comment, specimen_type_id, labtest_uuid, form_id, encounter_type, patient_uuid, created_date, change_date, location_uuid, source_database) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithObjectId(){ 
 		Object[] params = {this.id, this.encounterUuid, this.encounterDate, this.labTestId, this.request, this.orderDate, this.sampleCollectionDate, this.resultReportDate, this.resultQualitativeId, this.resultNumeric, this.resultUnits, this.resultComment, this.specimenTypeId, this.labtestUuid, this.formId, this.encounterType, this.patientUuid, this.createdDate, this.changeDate, this.locationUuid, this.sourceDatabase};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.encounterUuid, this.encounterDate, this.labTestId, this.request, this.orderDate, this.sampleCollectionDate, this.resultReportDate, this.resultQualitativeId, this.resultNumeric, this.resultUnits, this.resultComment, this.specimenTypeId, this.labtestUuid, this.formId, this.encounterType, this.patientUuid, this.createdDate, this.changeDate, this.locationUuid, this.sourceDatabase, this.id};		return params; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE laboratory SET encounter_uuid = ?, encounter_date = ?, lab_test_id = ?, request = ?, order_date = ?, sample_collection_date = ?, result_report_date = ?, result_qualitative_id = ?, result_numeric = ?, result_units = ?, result_comment = ?, specimen_type_id = ?, labtest_uuid = ?, form_id = ?, encounter_type = ?, patient_uuid = ?, created_date = ?, change_date = ?, location_uuid = ?, source_database = ? WHERE id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.encounterUuid != null ? "\""+ utilities.scapeQuotationMarks(encounterUuid)  +"\"" : null) + "," + (this.encounterDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(encounterDate)  +"\"" : null) + "," + (this.labTestId) + "," + (this.request) + "," + (this.orderDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(orderDate)  +"\"" : null) + "," + (this.sampleCollectionDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(sampleCollectionDate)  +"\"" : null) + "," + (this.resultReportDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(resultReportDate)  +"\"" : null) + "," + (this.resultQualitativeId) + "," + (this.resultNumeric) + "," + (this.resultUnits != null ? "\""+ utilities.scapeQuotationMarks(resultUnits)  +"\"" : null) + "," + (this.resultComment != null ? "\""+ utilities.scapeQuotationMarks(resultComment)  +"\"" : null) + "," + (this.specimenTypeId) + "," + (this.labtestUuid != null ? "\""+ utilities.scapeQuotationMarks(labtestUuid)  +"\"" : null) + "," + (this.formId) + "," + (this.encounterType) + "," + (this.patientUuid != null ? "\""+ utilities.scapeQuotationMarks(patientUuid)  +"\"" : null) + "," + (this.createdDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(createdDate)  +"\"" : null) + "," + (this.changeDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(changeDate)  +"\"" : null) + "," + (this.locationUuid != null ? "\""+ utilities.scapeQuotationMarks(locationUuid)  +"\"" : null) + "," + (this.sourceDatabase != null ? "\""+ utilities.scapeQuotationMarks(sourceDatabase)  +"\"" : null); 
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
		return "laboratory";
	}


}