package org.openmrs.module.epts.etl.conf.datasource;

import org.openmrs.module.epts.etl.conf.Extension;
import org.openmrs.module.epts.etl.conf.interfaces.ObjectDataSource;
import org.openmrs.module.epts.etl.conf.interfaces.TransformableField;
import org.openmrs.module.epts.etl.etl.processor.transformer.EtlFieldTransformer;
import org.openmrs.module.epts.etl.model.Field;

public class DataSourceField extends Field implements TransformableField {
	
	private String transformer;
	
	private EtlFieldTransformer transformerInstance;
	
	private Extension extension;
	
	private boolean dataTypeLoaded;
	
	private ObjectDataSource dataSource;
	
	public DataSourceField() {
		System.err.println();
	}
	
	public void setDataSource(ObjectDataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	public ObjectDataSource getDataSource() {
		return dataSource;
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
	
	public String getDataType() {
		return super.getType();
	}
	
	@Override
	public void setDataType(String dataType) {
		super.setType(dataType);
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
		return true;
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
