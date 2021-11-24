package org.openmrs.module.eptssync.model.pojo.source.qlm_24_julho; 
 
import org.openmrs.module.eptssync.model.pojo.generic.*; 
 
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.eptssync.utilities.AttDefinedElements; 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class PatientIdentifierTypeVO extends AbstractOpenMRSObject implements OpenMRSObject { 
	private int patientIdentifierTypeId;
	private String name;
	private String description;
	private String format;
	private byte checkDigit;
	private int creator;
	private byte required;
	private String formatDescription;
	private String validator;
	private byte retired;
	private int retiredBy;
	private java.util.Date dateRetired;
	private String retireReason;
	private String locationBehavior;
	private String uniquenessBehavior;
	private int changedBy;
 
	public PatientIdentifierTypeVO() { 
		this.metadata = true;
	} 
 
	public void setPatientIdentifierTypeId(int patientIdentifierTypeId){ 
	 	this.patientIdentifierTypeId = patientIdentifierTypeId;
	}
 
	public int getPatientIdentifierTypeId(){ 
		return this.patientIdentifierTypeId;
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
 
	public void setFormat(String format){ 
	 	this.format = format;
	}
 
	public String getFormat(){ 
		return this.format;
	}
 
	public void setCheckDigit(byte checkDigit){ 
	 	this.checkDigit = checkDigit;
	}
 
	public byte getCheckDigit(){ 
		return this.checkDigit;
	}
 
	public void setCreator(int creator){ 
	 	this.creator = creator;
	}
 
	public int getCreator(){ 
		return this.creator;
	}
 
	public void setRequired(byte required){ 
	 	this.required = required;
	}
 
	public byte getRequired(){ 
		return this.required;
	}
 
	public void setFormatDescription(String formatDescription){ 
	 	this.formatDescription = formatDescription;
	}
 
	public String getFormatDescription(){ 
		return this.formatDescription;
	}
 
	public void setValidator(String validator){ 
	 	this.validator = validator;
	}
 
	public String getValidator(){ 
		return this.validator;
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
 
	public void setLocationBehavior(String locationBehavior){ 
	 	this.locationBehavior = locationBehavior;
	}
 
	public String getLocationBehavior(){ 
		return this.locationBehavior;
	}
 
	public void setUniquenessBehavior(String uniquenessBehavior){ 
	 	this.uniquenessBehavior = uniquenessBehavior;
	}
 
	public String getUniquenessBehavior(){ 
		return this.uniquenessBehavior;
	}
 
	public void setChangedBy(int changedBy){ 
	 	this.changedBy = changedBy;
	}


 
	public int getChangedBy(){ 
		return this.changedBy;
	}
 
	public int getObjectId() { 
 		return this.patientIdentifierTypeId; 
	} 
 
	public void setObjectId(int selfId){ 
		this.patientIdentifierTypeId = selfId; 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		super.load(rs);
		this.patientIdentifierTypeId = rs.getInt("patient_identifier_type_id");
		this.name = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("name") != null ? rs.getString("name").trim() : null);
		this.description = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("description") != null ? rs.getString("description").trim() : null);
		this.format = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("format") != null ? rs.getString("format").trim() : null);
		this.checkDigit = rs.getByte("check_digit");
		this.creator = rs.getInt("creator");
		this.dateCreated =  rs.getTimestamp("date_created") != null ? new java.util.Date( rs.getTimestamp("date_created").getTime() ) : null;
		this.required = rs.getByte("required");
		this.formatDescription = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("format_description") != null ? rs.getString("format_description").trim() : null);
		this.validator = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("validator") != null ? rs.getString("validator").trim() : null);
		this.retired = rs.getByte("retired");
		this.retiredBy = rs.getInt("retired_by");
		this.dateRetired =  rs.getTimestamp("date_retired") != null ? new java.util.Date( rs.getTimestamp("date_retired").getTime() ) : null;
		this.retireReason = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("retire_reason") != null ? rs.getString("retire_reason").trim() : null);
		this.uuid = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("uuid") != null ? rs.getString("uuid").trim() : null);
		this.locationBehavior = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("location_behavior") != null ? rs.getString("location_behavior").trim() : null);
		this.uniquenessBehavior = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("uniqueness_behavior") != null ? rs.getString("uniqueness_behavior").trim() : null);
		this.dateChanged =  rs.getTimestamp("date_changed") != null ? new java.util.Date( rs.getTimestamp("date_changed").getTime() ) : null;
		this.changedBy = rs.getInt("changed_by");
	} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "patient_identifier_type_id"; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithoutObjectId(){ 
 		return "INSERT INTO patient_identifier_type(patient_identifier_type_id, name, description, format, check_digit, creator, date_created, required, format_description, validator, retired, retired_by, date_retired, retire_reason, uuid, location_behavior, uniqueness_behavior, date_changed, changed_by) VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithoutObjectId(){ 
 		Object[] params = {this.patientIdentifierTypeId, this.name, this.description, this.format, this.checkDigit, this.creator == 0 ? null : this.creator, this.dateCreated, this.required, this.formatDescription, this.validator, this.retired, this.retiredBy == 0 ? null : this.retiredBy, this.dateRetired, this.retireReason, this.uuid, this.locationBehavior, this.uniquenessBehavior, this.dateChanged, this.changedBy == 0 ? null : this.changedBy};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQLWithObjectId(){ 
 		return "INSERT INTO patient_identifier_type(patient_identifier_type_id, patient_identifier_type_id, name, description, format, check_digit, creator, date_created, required, format_description, validator, retired, retired_by, date_retired, retire_reason, uuid, location_behavior, uniqueness_behavior, date_changed, changed_by) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParamsWithObjectId(){ 
 		Object[] params = {this.patientIdentifierTypeId, this.patientIdentifierTypeId, this.name, this.description, this.format, this.checkDigit, this.creator == 0 ? null : this.creator, this.dateCreated, this.required, this.formatDescription, this.validator, this.retired, this.retiredBy == 0 ? null : this.retiredBy, this.dateRetired, this.retireReason, this.uuid, this.locationBehavior, this.uniquenessBehavior, this.dateChanged, this.changedBy == 0 ? null : this.changedBy};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.patientIdentifierTypeId, this.name, this.description, this.format, this.checkDigit, this.creator == 0 ? null : this.creator, this.dateCreated, this.required, this.formatDescription, this.validator, this.retired, this.retiredBy == 0 ? null : this.retiredBy, this.dateRetired, this.retireReason, this.uuid, this.locationBehavior, this.uniquenessBehavior, this.dateChanged, this.changedBy == 0 ? null : this.changedBy, this.patientIdentifierTypeId};		return params; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE patient_identifier_type SET patient_identifier_type_id = ?, name = ?, description = ?, format = ?, check_digit = ?, creator = ?, date_created = ?, required = ?, format_description = ?, validator = ?, retired = ?, retired_by = ?, date_retired = ?, retire_reason = ?, uuid = ?, location_behavior = ?, uniqueness_behavior = ?, date_changed = ?, changed_by = ? WHERE patient_identifier_type_id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.patientIdentifierTypeId) + "," + (this.name != null ? "\""+ utilities.scapeQuotationMarks(name)  +"\"" : null) + "," + (this.description != null ? "\""+ utilities.scapeQuotationMarks(description)  +"\"" : null) + "," + (this.format != null ? "\""+ utilities.scapeQuotationMarks(format)  +"\"" : null) + "," + (this.checkDigit) + "," + (this.creator == 0 ? null : this.creator) + "," + (this.dateCreated != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateCreated)  +"\"" : null) + "," + (this.required) + "," + (this.formatDescription != null ? "\""+ utilities.scapeQuotationMarks(formatDescription)  +"\"" : null) + "," + (this.validator != null ? "\""+ utilities.scapeQuotationMarks(validator)  +"\"" : null) + "," + (this.retired) + "," + (this.retiredBy == 0 ? null : this.retiredBy) + "," + (this.dateRetired != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateRetired)  +"\"" : null) + "," + (this.retireReason != null ? "\""+ utilities.scapeQuotationMarks(retireReason)  +"\"" : null) + "," + (this.uuid != null ? "\""+ utilities.scapeQuotationMarks(uuid)  +"\"" : null) + "," + (this.locationBehavior != null ? "\""+ utilities.scapeQuotationMarks(locationBehavior)  +"\"" : null) + "," + (this.uniquenessBehavior != null ? "\""+ utilities.scapeQuotationMarks(uniquenessBehavior)  +"\"" : null) + "," + (this.dateChanged != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateChanged)  +"\"" : null) + "," + (this.changedBy == 0 ? null : this.changedBy); 
	} 
 
	@Override
	public boolean hasParents() {
		if (this.changedBy != 0) return true;

		if (this.creator != 0) return true;

		if (this.retiredBy != 0) return true;

		return false;
	}

	@Override
	public int getParentValue(String parentAttName) {		
		if (parentAttName.equals("changedBy")) return this.changedBy;		
		if (parentAttName.equals("creator")) return this.creator;		
		if (parentAttName.equals("retiredBy")) return this.retiredBy;

		throw new RuntimeException("No found parent for: " + parentAttName);	}

	@Override
	public void changeParentValue(String parentAttName, OpenMRSObject newParent) {		
		if (parentAttName.equals("changedBy")) {
			this.changedBy = newParent.getObjectId();
			return;
		}		
		if (parentAttName.equals("creator")) {
			this.creator = newParent.getObjectId();
			return;
		}		
		if (parentAttName.equals("retiredBy")) {
			this.retiredBy = newParent.getObjectId();
			return;
		}

		throw new RuntimeException("No found parent for: " + parentAttName);
	}


}