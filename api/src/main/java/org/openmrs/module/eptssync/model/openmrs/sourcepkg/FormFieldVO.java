package org.openmrs.module.eptssync.model.openmrs.sourcepkg; 
 
import org.openmrs.module.eptssync.model.openmrs.generic.*; 
 
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.eptssync.utilities.db.conn.DBException; 
import org.openmrs.module.eptssync.exceptions.ParentNotYetMigratedException; 
 
import java.sql.Connection; 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class FormFieldVO extends AbstractOpenMRSObject implements OpenMRSObject { 
	private int formFieldId;
	private int formId;
	private int fieldId;
	private int fieldNumber;
	private String fieldPart;
	private int pageNumber;
	private int parentFormField;
	private int minOccurs;
	private int maxOccurs;
	private byte required;
	private int changedBy;
	private java.util.Date dateChanged;
	private int creator;
	private java.util.Date dateCreated;
	private float sortWeight;
	private String uuid;
 
	public FormFieldVO() { 
		this.metadata = false;
	} 
 
	public void setFormFieldId(int formFieldId){ 
	 	this.formFieldId = formFieldId;
	}
 
	public int getFormFieldId(){ 
		return this.formFieldId;
	}
 
	public void setFormId(int formId){ 
	 	this.formId = formId;
	}
 
	public int getFormId(){ 
		return this.formId;
	}
 
	public void setFieldId(int fieldId){ 
	 	this.fieldId = fieldId;
	}
 
	public int getFieldId(){ 
		return this.fieldId;
	}
 
	public void setFieldNumber(int fieldNumber){ 
	 	this.fieldNumber = fieldNumber;
	}
 
	public int getFieldNumber(){ 
		return this.fieldNumber;
	}
 
	public void setFieldPart(String fieldPart){ 
	 	this.fieldPart = fieldPart;
	}
 
	public String getFieldPart(){ 
		return this.fieldPart;
	}
 
	public void setPageNumber(int pageNumber){ 
	 	this.pageNumber = pageNumber;
	}
 
	public int getPageNumber(){ 
		return this.pageNumber;
	}
 
	public void setParentFormField(int parentFormField){ 
	 	this.parentFormField = parentFormField;
	}
 
	public int getParentFormField(){ 
		return this.parentFormField;
	}
 
	public void setMinOccurs(int minOccurs){ 
	 	this.minOccurs = minOccurs;
	}
 
	public int getMinOccurs(){ 
		return this.minOccurs;
	}
 
	public void setMaxOccurs(int maxOccurs){ 
	 	this.maxOccurs = maxOccurs;
	}
 
	public int getMaxOccurs(){ 
		return this.maxOccurs;
	}
 
	public void setRequired(byte required){ 
	 	this.required = required;
	}
 
	public byte getRequired(){ 
		return this.required;
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
 
	public void setSortWeight(float sortWeight){ 
	 	this.sortWeight = sortWeight;
	}
 
	public float getSortWeight(){ 
		return this.sortWeight;
	}
 
	public void setUuid(String uuid){ 
	 	this.uuid = uuid;
	}


 
	public String getUuid(){ 
		return this.uuid;
	}	public int getOriginRecordId(){ 
		return 0;
	}
 
	public void setOriginRecordId(int originRecordId){ }
 
	public String getOriginAppLocationCode(){ 
		return null;
	}
 
	public void setOriginAppLocationCode(String originAppLocationCode){ }
 
	public int getConsistent(){ 
		return 0;
	}
 
	public void setConsistent(int consistent){ }
 

 
	public int getObjectId() { 
 		return this.formFieldId; 
	} 
 
	public void setObjectId(int selfId){ 
		this.formFieldId = selfId; 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		this.formFieldId = rs.getInt("form_field_id");
		this.formId = rs.getInt("form_id");
		this.fieldId = rs.getInt("field_id");
		this.fieldNumber = rs.getInt("field_number");
		this.fieldPart = rs.getString("field_part") != null ? rs.getString("field_part").trim() : null;
		this.pageNumber = rs.getInt("page_number");
		this.parentFormField = rs.getInt("parent_form_field");
		this.minOccurs = rs.getInt("min_occurs");
		this.maxOccurs = rs.getInt("max_occurs");
		this.required = rs.getByte("required");
		this.changedBy = rs.getInt("changed_by");
		this.dateChanged =  rs.getTimestamp("date_changed") != null ? new java.util.Date( rs.getTimestamp("date_changed").getTime() ) : null;
		this.creator = rs.getInt("creator");
		this.dateCreated =  rs.getTimestamp("date_created") != null ? new java.util.Date( rs.getTimestamp("date_created").getTime() ) : null;
		this.sortWeight = rs.getFloat("sort_weight");
			} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "form_field_id"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParams(){ 
 		Object[] params = {this.formId == 0 ? null : this.formId, this.fieldId == 0 ? null : this.fieldId, this.fieldNumber, this.fieldPart, this.pageNumber, this.parentFormField == 0 ? null : this.parentFormField, this.minOccurs, this.maxOccurs, this.required, this.changedBy == 0 ? null : this.changedBy, this.dateChanged, this.creator == 0 ? null : this.creator, this.dateCreated, this.sortWeight, this.uuid};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.formId == 0 ? null : this.formId, this.fieldId == 0 ? null : this.fieldId, this.fieldNumber, this.fieldPart, this.pageNumber, this.parentFormField == 0 ? null : this.parentFormField, this.minOccurs, this.maxOccurs, this.required, this.changedBy == 0 ? null : this.changedBy, this.dateChanged, this.creator == 0 ? null : this.creator, this.dateCreated, this.sortWeight, this.uuid, this.formFieldId};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQL(){ 
 		return "INSERT INTO form_field(form_id, field_id, field_number, field_part, page_number, parent_form_field, min_occurs, max_occurs, required, changed_by, date_changed, creator, date_created, sort_weight, uuid) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE form_field SET form_id = ?, field_id = ?, field_number = ?, field_part = ?, page_number = ?, parent_form_field = ?, min_occurs = ?, max_occurs = ?, required = ?, changed_by = ?, date_changed = ?, creator = ?, date_created = ?, sort_weight = ?, uuid = ? WHERE form_field_id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.formId == 0 ? null : this.formId) + "," + (this.fieldId == 0 ? null : this.fieldId) + "," + (this.fieldNumber) + "," + (this.fieldPart != null ? "\""+fieldPart+"\"" : null) + "," + (this.pageNumber) + "," + (this.parentFormField == 0 ? null : this.parentFormField) + "," + (this.minOccurs) + "," + (this.maxOccurs) + "," + (this.required) + "," + (this.changedBy == 0 ? null : this.changedBy) + "," + (this.dateChanged != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateChanged)  +"\"" : null) + "," + (this.creator == 0 ? null : this.creator) + "," + (this.dateCreated != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateCreated)  +"\"" : null) + "," + (this.sortWeight) + "," + (this.uuid != null ? "\""+uuid+"\"" : null); 
	} 
 
	@Override
	public boolean isGeneratedFromSkeletonClass() {
		return false;
	}

	@Override
	public boolean hasParents() {
		if (this.fieldId != 0) return true;
		if (this.formId != 0) return true;
		if (this.parentFormField != 0) return true;
		if (this.creator != 0) return true;
		if (this.changedBy != 0) return true;
		return false;
	}

	@Override
	public int retrieveSharedPKKey(Connection conn) throws ParentNotYetMigratedException, DBException {
		throw new RuntimeException("No PKSharedInfo defined!");	}

	@Override
	public void loadDestParentInfo(Connection conn) throws ParentNotYetMigratedException, DBException {
		OpenMRSObject parentOnDestination = null;
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.sourcepkg.FieldVO.class, this.fieldId, false, conn); 
		this.fieldId = 0;
		if (parentOnDestination  != null) this.fieldId = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.sourcepkg.FormVO.class, this.formId, false, conn); 
		this.formId = 0;
		if (parentOnDestination  != null) this.formId = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.sourcepkg.FormFieldVO.class, this.parentFormField, true, conn); 
		this.parentFormField = 0;
		if (parentOnDestination  != null) this.parentFormField = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.sourcepkg.UsersVO.class, this.creator, false, conn); 
		this.creator = 0;
		if (parentOnDestination  != null) this.creator = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.sourcepkg.UsersVO.class, this.changedBy, true, conn); 
		this.changedBy = 0;
		if (parentOnDestination  != null) this.changedBy = parentOnDestination.getObjectId();
 
	}

	@Override
	public int getParentValue(String parentAttName) {		
		if (parentAttName.equals("fieldId")) return this.fieldId;		
		if (parentAttName.equals("formId")) return this.formId;		
		if (parentAttName.equals("parentFormField")) return this.parentFormField;		
		if (parentAttName.equals("creator")) return this.creator;		
		if (parentAttName.equals("changedBy")) return this.changedBy;

		throw new RuntimeException("No found parent for: " + parentAttName);	}


}