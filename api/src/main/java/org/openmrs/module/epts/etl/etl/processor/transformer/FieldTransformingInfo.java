package org.openmrs.module.epts.etl.etl.processor.transformer;

import org.openmrs.module.epts.etl.conf.DstConf;
import org.openmrs.module.epts.etl.conf.interfaces.EtlDataSource;
import org.openmrs.module.epts.etl.conf.interfaces.TransformableField;
import org.openmrs.module.epts.etl.utilities.CommonUtilities;

public class FieldTransformingInfo {
	
	private static CommonUtilities utilities = CommonUtilities.getInstance();
	
	private EtlDataSource transformationDatasource;
	
	private TransformableField srcField;
	
	private Object transformedValue;
	
	private boolean loadedWithDefaultValue;
	
	public FieldTransformingInfo(TransformableField srcField, Object transformedValue,
	    EtlDataSource transformationDatasource) {
		
		this.srcField = srcField;
		this.transformationDatasource = transformationDatasource;
		this.transformedValue = transformedValue;
		
		tryToParseValue();
	}
	
	void tryToParseValue() {
		if (!this.srcField.hasDataType()) {
			return;
		}
		
		if (!this.srcField.hasTypeClass()) {
			this.srcField.determineTypeClass();
		}
		
		if (this.transformedValue != null && utilities.isNumericType(srcField.getTypeClass())) {
			this.transformedValue = utilities.parseValue(transformedValue.toString(), srcField.getTypeClass());
		} else if (transformedValue != null && utilities.isBooleanType(srcField.getTypeClass())) {
			this.transformedValue = utilities.parseValue(transformedValue.toString(), srcField.getTypeClass());
		}
	}
	
	public EtlDataSource getTransformationDatasource() {
		return transformationDatasource;
	}
	
	public void setTransformationDatasource(EtlDataSource transformationDatasource) {
		this.transformationDatasource = transformationDatasource;
	}
	
	public TransformableField getSrcField() {
		return srcField;
	}
	
	public void setSrcField(TransformableField srcField) {
		this.srcField = srcField;
	}
	
	public Object getTransformedValue() {
		return transformedValue;
	}
	
	public void setTransformedValue(Object transformedValue) {
		this.transformedValue = transformedValue;
		
		tryToParseValue();
	}
	
	public boolean loadedWithDefaultValue() {
		return loadedWithDefaultValue;
	}
	
	public boolean isLoadedWithDefaultValue() {
		return loadedWithDefaultValue;
	}
	
	public void setLoadedWithDefaultValue(boolean loadedWithDefaultValue) {
		this.loadedWithDefaultValue = loadedWithDefaultValue;
	}
	
	public boolean isLoadedWithDstValue() {
		return this.loadedWithDefaultValue || this.getTransformationDatasource() == null
		        || this.getTransformationDatasource() instanceof DstConf;
	}
	
	public boolean skipRelationshipResolution() {
		return isLoadedWithDefaultValue() || isLoadedWithDstValue() || srcField.relationshipResolutionStrategy().skip();
	}
	
	@Override
	public String toString() {
		return this.srcField.getSrcField() + " = " + this.getTransformedValue() + ". Ds: " + this.transformationDatasource;
	}
}
