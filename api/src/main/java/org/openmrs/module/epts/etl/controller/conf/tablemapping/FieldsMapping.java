package org.openmrs.module.epts.etl.controller.conf.tablemapping;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.epts.etl.controller.conf.AppInfo;
import org.openmrs.module.epts.etl.controller.conf.SyncDestinationTableConfiguration;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObject;
import org.openmrs.module.epts.etl.utilities.AttDefinedElements;
import org.openmrs.module.epts.etl.utilities.CommonUtilities;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

/**
 * This class is used to map fields between an source table and destination table
 * 
 * @author jpboane
 */
public class FieldsMapping {
	
	static CommonUtilities utilities = CommonUtilities.getInstance();
	
	private String srcField;
	
	private String srcName;
	
	private String destField;
	
	public FieldsMapping() {
	}
	
	public FieldsMapping(String srcField, String srcName, String destField) {
		this.srcField = srcField;
		this.srcName = srcName;
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
	
	public String getSrcName() {
		return srcName;
	}
	
	public void setSrcName(String srcName) {
		this.srcName = srcName;
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
	
	public Object retrieveValue(SyncDestinationTableConfiguration mappingInfo, List<DatabaseObject> srcObjects, AppInfo appInfo,
	        Connection conn) throws DBException, ForbiddenOperationException {
		
		for (DatabaseObject srcObject : srcObjects) {
			if (this.getSrcName().equals(srcObject.generateTableName())) {
				return srcObject.getFieldValue(this.getSrcFieldAsClassField());
			}
		}
		
		throw new ForbiddenOperationException(
		        "The field '" + this.srcField + " does not belong to any configured source table");
	}
	
}
