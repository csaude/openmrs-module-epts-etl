package org.openmrs.module.epts.etl.controller.conf.tablemapping;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.epts.etl.controller.conf.AppInfo;
import org.openmrs.module.epts.etl.controller.conf.DstConf;
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
	
	private String dataSourceName;
	
	private String dstField;
	
	public FieldsMapping() {
	}
	
	public FieldsMapping(String srcField, String dataSourceName, String destField) {
		this.srcField = srcField;
		this.dataSourceName = dataSourceName;
		this.dstField = destField;
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
		return "[srcField: " + srcField + ", dstField: " + dstField + "]";
	}
	
	public Object retrieveValue(DstConf mappingInfo, List<DatabaseObject> srcObjects,
	        AppInfo appInfo, Connection conn) throws DBException, ForbiddenOperationException {
		
		for (DatabaseObject srcObject : srcObjects) {
			if (this.getDataSourceName().equals(srcObject.generateTableName())) {
				return srcObject.getFieldValue(this.getSrcFieldAsClassField());
			}
		}
		
		throw new ForbiddenOperationException(
		        "The field '" + this.srcField + " does not belong to any configured source table");
	}
	
}
