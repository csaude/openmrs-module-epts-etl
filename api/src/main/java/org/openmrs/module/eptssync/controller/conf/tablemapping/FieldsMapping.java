package org.openmrs.module.eptssync.controller.conf.tablemapping;

import java.sql.Connection;

import org.openmrs.module.eptssync.controller.conf.AppInfo;
import org.openmrs.module.eptssync.exceptions.ForbiddenOperationException;
import org.openmrs.module.eptssync.model.pojo.generic.DatabaseObject;
import org.openmrs.module.eptssync.utilities.AttDefinedElements;
import org.openmrs.module.eptssync.utilities.CommonUtilities;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;

/**
 * This class is used to map fields between an source table and destination table
 * 
 * @author jpboane
 */
public class FieldsMapping {
	
	static CommonUtilities utilities = CommonUtilities.getInstance();
	
	private String srcField;
	
	private String srcTable;
	
	private String destField;
	
	public FieldsMapping() {
	}
	
	public FieldsMapping(String srcField, String srcTable, String destField) {
		this.srcField = srcField;
		this.srcTable = srcTable;
		this.destField = destField;
	}
	
	public String getDestField() {
		return destField;
	}
	
	public void setDestField(String destField) {
		this.destField = destField;
	}
	
	public String getSrcFieldAsClassField() {
		return AttDefinedElements.convertTableAttNameToClassAttName(this.srcField);
	}
	
	public String getDestFieldAsClassField() {
		return AttDefinedElements.convertTableAttNameToClassAttName(this.destField);
	}
	
	public String getSrcField() {
		return srcField;
	}
	
	public void setSrcField(String srcField) {
		this.srcField = srcField;
	}
	
	public String getSrcTable() {
		return srcTable;
	}
	
	public void setSrcTable(String srcTable) {
		this.srcTable = srcTable;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof FieldsMapping))
			return false;
		
		FieldsMapping fm = (FieldsMapping) obj;
		
		return this.destField.equals(fm.destField);
	}
	
	@Override
	public String toString() {
		return "[srcField: " + srcField + ", destField: " + destField + "]";
	}
	
	public Object retrieveValue(MappedTableInfo mappingInfo, DatabaseObject srcObject, AppInfo appInfo, Connection conn)
	        throws DBException, ForbiddenOperationException {
		if (this.getSrcTable().equals(mappingInfo.getRelatedTableConfiguration().getTableName())) {
			return srcObject.getFieldValue(this.getSrcFieldAsClassField());
		} else {
			MappingSrcData src = mappingInfo.findAdditionalDataSrc(this.getSrcTable());
			
			DatabaseObject relatedSrcObject = src.loadRelatedSrcObject(srcObject, appInfo, conn);
			
			return relatedSrcObject != null ? relatedSrcObject.getFieldValue(this.getSrcFieldAsClassField()) : null;
		}
	}
	
}
