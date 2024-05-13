package org.openmrs.module.epts.etl.controller.conf.tablemapping;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.epts.etl.conf.AppInfo;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
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
	
	private String srcValue;
	
	private String srcField;
	
	private String dataSourceName;
	
	private String dstField;
	
	public FieldsMapping() {
	}
	
	public FieldsMapping(String srcField, String dataSourceName, String destField) {
		this.srcField = srcField;
		this.dataSourceName = dataSourceName;
		this.dstField = destField;
	}
	
	public static FieldsMapping fastCreate(String srcField, String destField) {
		return new FieldsMapping(srcField, null, destField);
	}
	
	public static FieldsMapping fastCreate(String fieldName) {
		return fastCreate(fieldName, fieldName);
	}
	
	public String getSrcValue() {
		return srcValue;
	}
	
	public void setSrcValue(String srcValue) {
		this.srcValue = srcValue;
	}
	
	public String getDstField() {
		return dstField;
	}
	
	public void setDstField(String destField) {
		this.dstField = destField;
	}
	
	public String getSrcFieldAsClassField() {
		return AttDefinedElements.convertTableAttNameToClassAttName(this.srcField);
	}
	
	public String getDestFieldAsClassField() {
		return AttDefinedElements.convertTableAttNameToClassAttName(this.dstField);
	}
	
	public String getSrcField() {
		return srcField;
	}
	
	public void setSrcField(String srcField) {
		this.srcField = srcField;
	}
	
	public String getDataSourceName() {
		return dataSourceName;
	}
	
	public void setDataSourceName(String dataSourceName) {
		this.dataSourceName = dataSourceName;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof FieldsMapping))
			return false;
		
		FieldsMapping fm = (FieldsMapping) obj;
		
		return this.dstField.equals(fm.dstField);
	}
	
	@Override
	public String toString() {
		return "[srcField: " + srcField + ", dstField: " + dstField + ", dataSourceName: " + this.dataSourceName + "]";
	}
	
	public Object retrieveValue(EtlDatabaseObject dstObject, List<EtlDatabaseObject> srcObjects, AppInfo appInfo, Connection conn)
	        throws DBException, ForbiddenOperationException {
		
		if (this.srcValue != null) {
			return utilities.parseValue(this.srcValue, utilities.getFieldType(dstObject, this.dstField));
		}
		
		for (EtlDatabaseObject srcObject : srcObjects) {
			if (this.getDataSourceName().equals(srcObject.getRelatedConfiguration().getAlias())) {
				try {
					return srcObject.getFieldValue(this.getSrcField());
				}
				catch (ForbiddenOperationException e) {
					return srcObject.getFieldValue(this.getSrcFieldAsClassField());
				}
			}
		}
		
		throw new ForbiddenOperationException(
		        "The field '" + this.srcField + " does not belong to any configured source table");
	}
}
