package org.openmrs.module.epts.etl.conf.datasource;

import org.openmrs.module.epts.etl.conf.Extension;
import org.openmrs.module.epts.etl.conf.interfaces.ObjectDataSource;
import org.openmrs.module.epts.etl.conf.interfaces.TransformableField;
import org.openmrs.module.epts.etl.etl.processor.transformer.EtlFieldTransformer;
import org.openmrs.module.epts.etl.model.Field;

public class DataSourceField extends Field implements TransformableField {
	
	private static final long serialVersionUID = -7824136202167355998L;
	
	private String transformer;
	
	private EtlFieldTransformer transformerInstance;
	
	private Extension extension;
	
	private boolean dataTypeLoaded;
	
	private ObjectDataSource dataSource;
	
	private boolean applyNullValue;
	
	public DataSourceField() {
	}
	
	public void setDataSource(ObjectDataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	public ObjectDataSource getDataSource() {
		return dataSource;
	}
	
	public boolean applyNullValue() {
		return applyNullValue;
	}
	
	public boolean isApplyNullValue() {
		return applyNullValue;
	}
	
	public void setApplyNullValue(boolean applyNullValue) {
		this.applyNullValue = applyNullValue;
	}
	
	@Override
	public String getDataSourceName() {
		return this.getDataSource().getName();
	}
	
	public String getTransformer() {
		return transformer;
	}
	
	public void setTransformer(String transformer) {
		this.transformer = transformer;
	}
	
	public EtlFieldTransformer getTransformerInstance() {
		return transformerInstance;
	}
	
	public void setTransformerInstance(EtlFieldTransformer transformerInstance) {
		this.transformerInstance = transformerInstance;
	}
	
	public Extension getExtension() {
		return extension;
	}
	
	public void setExtension(Extension extension) {
		this.extension = extension;
	}
	
	public boolean isDataTypeLoaded() {
		return dataTypeLoaded;
	}
	
	public void setDataTypeLoaded(boolean dataTypeLoaded) {
		this.dataTypeLoaded = dataTypeLoaded;
	}
	
	@Override
	public String getValueToTransform() {
		return this.getValue() != null ? this.getValue().toString() : null;
	}
	
	@Override
	public boolean hasSrcField() {
		return false;
	}
	
	@Override
	public String getDstField() {
		return this.getName();
	}
	
	@Override
	public String getSrcField() {
		return this.getName();
	}
	
}
