package org.openmrs.module.epts.etl.model.pojo.openmrs._default._query_result;

import org.openmrs.module.epts.etl.model.pojo.generic.*; 
 
import org.openmrs.module.epts.etl.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.epts.etl.utilities.AttDefinedElements; 
 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class CacumRelatedInfoQueryResultVO extends AbstractDatabaseObject implements DatabaseObject { 
	private String hiv;
	private java.util.Date visitDate;
	private Double ageFirstSexual;
	private String viaResult;
	private String cryotherapySameDayVia;
	private String referenceHospitalName;
	private String reasonReferral1;
	private String reasonReferral2;
	private String reasonReferral3;
	private String reasonReferral4;
	private String reasonReferral5;
	private String hdr;
	private String referenceResultHdr1;
	private String referenceResultHdr2;
	private String referenceResultHdr3;
	private String referenceResultHdr4;
	private String referenceResultHdr5;
	private String referenceResultHdr6;
 
	public CacumRelatedInfoQueryResultVO() { 
		this.metadata = false;
	} 
 
	public void setHiv(String hiv){ 
	 	this.hiv = hiv;
	}
 
	public String getHiv(){ 
		return this.hiv;
	}
 
	public void setVisitDate(java.util.Date visitDate){ 
	 	this.visitDate = visitDate;
	}
 
	public java.util.Date getVisitDate(){ 
		return this.visitDate;
	}
 
	public void setAgeFirstSexual(Double ageFirstSexual){ 
	 	this.ageFirstSexual = ageFirstSexual;
	}
 
	public Double getAgeFirstSexual(){ 
		return this.ageFirstSexual;
	}
 
	public void setViaResult(String viaResult){ 
	 	this.viaResult = viaResult;
	}
 
	public String getViaResult(){ 
		return this.viaResult;
	}
 
	public void setCryotherapySameDayVia(String cryotherapySameDayVia){ 
	 	this.cryotherapySameDayVia = cryotherapySameDayVia;
	}
 
	public String getCryotherapySameDayVia(){ 
		return this.cryotherapySameDayVia;
	}
 
	public void setReferenceHospitalName(String referenceHospitalName){ 
	 	this.referenceHospitalName = referenceHospitalName;
	}
 
	public String getReferenceHospitalName(){ 
		return this.referenceHospitalName;
	}
 
	public void setReasonReferral1(String reasonReferral1){ 
	 	this.reasonReferral1 = reasonReferral1;
	}
 
	public String getReasonReferral1(){ 
		return this.reasonReferral1;
	}
 
	public void setReasonReferral2(String reasonReferral2){ 
	 	this.reasonReferral2 = reasonReferral2;
	}
 
	public String getReasonReferral2(){ 
		return this.reasonReferral2;
	}
 
	public void setReasonReferral3(String reasonReferral3){ 
	 	this.reasonReferral3 = reasonReferral3;
	}
 
	public String getReasonReferral3(){ 
		return this.reasonReferral3;
	}
 
	public void setReasonReferral4(String reasonReferral4){ 
	 	this.reasonReferral4 = reasonReferral4;
	}
 
	public String getReasonReferral4(){ 
		return this.reasonReferral4;
	}
 
	public void setReasonReferral5(String reasonReferral5){ 
	 	this.reasonReferral5 = reasonReferral5;
	}
 
	public String getReasonReferral5(){ 
		return this.reasonReferral5;
	}
 
	public void setHdr(String hdr){ 
	 	this.hdr = hdr;
	}
 
	public String getHdr(){ 
		return this.hdr;
	}
 
	public void setReferenceResultHdr1(String referenceResultHdr1){ 
	 	this.referenceResultHdr1 = referenceResultHdr1;
	}
 
	public String getReferenceResultHdr1(){ 
		return this.referenceResultHdr1;
	}
 
	public void setReferenceResultHdr2(String referenceResultHdr2){ 
	 	this.referenceResultHdr2 = referenceResultHdr2;
	}
 
	public String getReferenceResultHdr2(){ 
		return this.referenceResultHdr2;
	}
 
	public void setReferenceResultHdr3(String referenceResultHdr3){ 
	 	this.referenceResultHdr3 = referenceResultHdr3;
	}
 
	public String getReferenceResultHdr3(){ 
		return this.referenceResultHdr3;
	}
 
	public void setReferenceResultHdr4(String referenceResultHdr4){ 
	 	this.referenceResultHdr4 = referenceResultHdr4;
	}
 
	public String getReferenceResultHdr4(){ 
		return this.referenceResultHdr4;
	}
 
	public void setReferenceResultHdr5(String referenceResultHdr5){ 
	 	this.referenceResultHdr5 = referenceResultHdr5;
	}
 
	public String getReferenceResultHdr5(){ 
		return this.referenceResultHdr5;
	}
 
	public void setReferenceResultHdr6(String referenceResultHdr6){ 
	 	this.referenceResultHdr6 = referenceResultHdr6;
	}


 
	public String getReferenceResultHdr6(){ 
		return this.referenceResultHdr6;
	}
 
	public Integer getObjectId() { 
 		return 0; 
	} 
 
	public void setObjectId(Integer selfId){ 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		super.load(rs);
		this.hiv = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("hiv") != null ? rs.getString("hiv").trim() : null);
		this.visitDate =  rs.getTimestamp("visit_date") != null ? new java.util.Date( rs.getTimestamp("visit_date").getTime() ) : null;
		if (rs.getObject("age_first_sexual") != null) this.ageFirstSexual = rs.getDouble("age_first_sexual");
		this.viaResult = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("via_result") != null ? rs.getString("via_result").trim() : null);
		this.cryotherapySameDayVia = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("cryotherapy_same_day_via") != null ? rs.getString("cryotherapy_same_day_via").trim() : null);
		this.referenceHospitalName = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("reference_hospital_name") != null ? rs.getString("reference_hospital_name").trim() : null);
		this.reasonReferral1 = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("reason_referral_1") != null ? rs.getString("reason_referral_1").trim() : null);
		this.reasonReferral2 = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("reason_referral_2") != null ? rs.getString("reason_referral_2").trim() : null);
		this.reasonReferral3 = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("reason_referral_3") != null ? rs.getString("reason_referral_3").trim() : null);
		this.reasonReferral4 = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("reason_referral_4") != null ? rs.getString("reason_referral_4").trim() : null);
		this.reasonReferral5 = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("reason_referral_5") != null ? rs.getString("reason_referral_5").trim() : null);
		this.hdr = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("hdr") != null ? rs.getString("hdr").trim() : null);
		this.referenceResultHdr1 = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("reference_result_hdr1") != null ? rs.getString("reference_result_hdr1").trim() : null);
		this.referenceResultHdr2 = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("reference_result_hdr2") != null ? rs.getString("reference_result_hdr2").trim() : null);
		this.referenceResultHdr3 = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("reference_result_hdr3") != null ? rs.getString("reference_result_hdr3").trim() : null);
		this.referenceResultHdr4 = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("reference_result_hdr4") != null ? rs.getString("reference_result_hdr4").trim() : null);
		this.referenceResultHdr5 = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("reference_result_hdr5") != null ? rs.getString("reference_result_hdr5").trim() : null);
		this.referenceResultHdr6 = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("reference_result_hdr6") != null ? rs.getString("reference_result_hdr6").trim() : null);
	} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return null; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithoutObjectId(){ 
 		return "INSERT INTO cacum_related_info(hiv, visit_date, age_first_sexual, via_result, cryotherapy_same_day_via, reference_hospital_name, reason_referral_1, reason_referral_2, reason_referral_3, reason_referral_4, reason_referral_5, hdr, reference_result_hdr1, reference_result_hdr2, reference_result_hdr3, reference_result_hdr4, reference_result_hdr5, reference_result_hdr6) VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithoutObjectId(){ 
 		Object[] params = {this.hiv, this.visitDate, this.ageFirstSexual, this.viaResult, this.cryotherapySameDayVia, this.referenceHospitalName, this.reasonReferral1, this.reasonReferral2, this.reasonReferral3, this.reasonReferral4, this.reasonReferral5, this.hdr, this.referenceResultHdr1, this.referenceResultHdr2, this.referenceResultHdr3, this.referenceResultHdr4, this.referenceResultHdr5, this.referenceResultHdr6};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithObjectId(){ 
 		return "INSERT INTO cacum_related_info(hiv, visit_date, age_first_sexual, via_result, cryotherapy_same_day_via, reference_hospital_name, reason_referral_1, reason_referral_2, reason_referral_3, reason_referral_4, reason_referral_5, hdr, reference_result_hdr1, reference_result_hdr2, reference_result_hdr3, reference_result_hdr4, reference_result_hdr5, reference_result_hdr6) VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithObjectId(){ 
 		Object[] params = {this.hiv, this.visitDate, this.ageFirstSexual, this.viaResult, this.cryotherapySameDayVia, this.referenceHospitalName, this.reasonReferral1, this.reasonReferral2, this.reasonReferral3, this.reasonReferral4, this.reasonReferral5, this.hdr, this.referenceResultHdr1, this.referenceResultHdr2, this.referenceResultHdr3, this.referenceResultHdr4, this.referenceResultHdr5, this.referenceResultHdr6};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.hiv, this.visitDate, this.ageFirstSexual, this.viaResult, this.cryotherapySameDayVia, this.referenceHospitalName, this.reasonReferral1, this.reasonReferral2, this.reasonReferral3, this.reasonReferral4, this.reasonReferral5, this.hdr, this.referenceResultHdr1, this.referenceResultHdr2, this.referenceResultHdr3, this.referenceResultHdr4, this.referenceResultHdr5, this.referenceResultHdr6, null};		return params; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE cacum_related_info SET hiv = ?, visit_date = ?, age_first_sexual = ?, via_result = ?, cryotherapy_same_day_via = ?, reference_hospital_name = ?, reason_referral_1 = ?, reason_referral_2 = ?, reason_referral_3 = ?, reason_referral_4 = ?, reason_referral_5 = ?, hdr = ?, reference_result_hdr1 = ?, reference_result_hdr2 = ?, reference_result_hdr3 = ?, reference_result_hdr4 = ?, reference_result_hdr5 = ?, reference_result_hdr6 = ? WHERE null = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.hiv != null ? "\""+ utilities.scapeQuotationMarks(hiv)  +"\"" : null) + "," + (this.visitDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(visitDate)  +"\"" : null) + "," + (this.ageFirstSexual) + "," + (this.viaResult != null ? "\""+ utilities.scapeQuotationMarks(viaResult)  +"\"" : null) + "," + (this.cryotherapySameDayVia != null ? "\""+ utilities.scapeQuotationMarks(cryotherapySameDayVia)  +"\"" : null) + "," + (this.referenceHospitalName != null ? "\""+ utilities.scapeQuotationMarks(referenceHospitalName)  +"\"" : null) + "," + (this.reasonReferral1 != null ? "\""+ utilities.scapeQuotationMarks(reasonReferral1)  +"\"" : null) + "," + (this.reasonReferral2 != null ? "\""+ utilities.scapeQuotationMarks(reasonReferral2)  +"\"" : null) + "," + (this.reasonReferral3 != null ? "\""+ utilities.scapeQuotationMarks(reasonReferral3)  +"\"" : null) + "," + (this.reasonReferral4 != null ? "\""+ utilities.scapeQuotationMarks(reasonReferral4)  +"\"" : null) + "," + (this.reasonReferral5 != null ? "\""+ utilities.scapeQuotationMarks(reasonReferral5)  +"\"" : null) + "," + (this.hdr != null ? "\""+ utilities.scapeQuotationMarks(hdr)  +"\"" : null) + "," + (this.referenceResultHdr1 != null ? "\""+ utilities.scapeQuotationMarks(referenceResultHdr1)  +"\"" : null) + "," + (this.referenceResultHdr2 != null ? "\""+ utilities.scapeQuotationMarks(referenceResultHdr2)  +"\"" : null) + "," + (this.referenceResultHdr3 != null ? "\""+ utilities.scapeQuotationMarks(referenceResultHdr3)  +"\"" : null) + "," + (this.referenceResultHdr4 != null ? "\""+ utilities.scapeQuotationMarks(referenceResultHdr4)  +"\"" : null) + "," + (this.referenceResultHdr5 != null ? "\""+ utilities.scapeQuotationMarks(referenceResultHdr5)  +"\"" : null) + "," + (this.referenceResultHdr6 != null ? "\""+ utilities.scapeQuotationMarks(referenceResultHdr6)  +"\"" : null); 
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
		return "cacum_related_info";
	}


}