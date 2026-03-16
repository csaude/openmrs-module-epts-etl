package org.openmrs.module.epts.etl.etl.processor.transformer;

import org.openmrs.module.epts.etl.conf.DstConf;
import org.openmrs.module.epts.etl.conf.interfaces.EtlDataSource;
import org.openmrs.module.epts.etl.conf.interfaces.TransformableField;

public class FieldTransformingInfo {
	
	private EtlDataSource transformationDatasource;
	
	private TransformableField srcField;
	
	private Object transformedValue;
	
	private boolean loadedWithDefaultValue;
	
	public FieldTransformingInfo(TransformableField srcField, Object transformedValue,
	    EtlDataSource transformationDatasource) {
		
		this.srcField = srcField;
		this.transformationDatasource = transformationDatasource;
		this.transformedValue = transformedValue;
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
	
	@Override
	public String toString() {
		return this.srcField.getSrcField() + " = " + this.getTransformedValue() + ". Ds: " + this.transformationDatasource;
	}
}
