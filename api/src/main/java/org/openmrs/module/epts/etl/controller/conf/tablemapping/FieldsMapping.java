package org.openmrs.module.epts.etl.controller.conf.tablemapping;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.epts.etl.etl.processor.transformer.EtlFieldTransformer;
import org.openmrs.module.epts.etl.etl.processor.transformer.EtlRecordTransformer;
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
	
	private boolean mapToNullValue;
	
	private String transformer;
	
	private EtlFieldTransformer transformerInstance;
	
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
	
	public EtlFieldTransformer getTransformerInstance() {
		if (this.transformerInstance == null) {
			tryToLoadTransformer();
		}
		
		return this.transformerInstance;
	}
	
	/**
	 * @throws ForbiddenOperationException
	 */
	@SuppressWarnings("unchecked")
	public void tryToLoadTransformer() throws ForbiddenOperationException {
		if (this.hasTransformer()) {
			try {
				ClassLoader loader = EtlRecordTransformer.class.getClassLoader();
				
				Class<? extends EtlFieldTransformer> t = (Class<? extends EtlFieldTransformer>) loader
				        .loadClass(this.getTransformer());
				
				this.transformerInstance = t.newInstance();
			}
			catch (Exception e) {
				throw new ForbiddenOperationException(
				        ". Error loading the transformer [" + this.getTransformer() + "] !!! " + e.getLocalizedMessage());
			}
			
		}
	}
	
	public void setTransformerInstance(EtlFieldTransformer transformerInstance) {
		this.transformerInstance = transformerInstance;
	}
	
	public String getTransformer() {
		return transformer;
	}
	
	public void setTransformer(String transformer) {
		this.transformer = transformer;
	}
	
	public boolean hasTransformer() {
		return getTransformer() != null;
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
	
	public String getDstFieldAsClassField() {
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
	
	public Object retrieveValue(EtlDatabaseObject dstObject, List<EtlDatabaseObject> srcObjects, Connection conn)
	        throws DBException, ForbiddenOperationException {
		
		if (hasTransformer()) {
			return getTransformerInstance().transform(srcObjects.get(0), this.getSrcField(), this.getDstField(), conn, conn);
		}
		
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
	
	public boolean hasTransformerInstance() {
		return this.getTransformerInstance() != null;
	}
	
	public void setMapToNullValue(boolean b) {
		mapToNullValue = b;
	}
	
	public boolean isMapToNullValue() {
		return mapToNullValue;
	}
	
}
