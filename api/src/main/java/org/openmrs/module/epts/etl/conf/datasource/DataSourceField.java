package org.openmrs.module.epts.etl.conf.datasource;

import java.util.ArrayList;
import java.util.List;

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
	
	public DataSourceField() {
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
	
	public boolean isDataTypeLoaded() {
		return dataTypeLoaded;
	}
	
	public void setDataTypeLoaded(boolean dataTypeLoaded) {
		this.dataTypeLoaded = dataTypeLoaded;
	}
	
	@Override
	public void copyFrom(Field f) {
		super.copyFrom(f);
		
		if (f instanceof DataSourceField) {
			DataSourceField fDs = (DataSourceField) f;
			this.setTransformer(fDs.getTransformer());
			this.setTransformerInstance(fDs.getTransformerInstance());
			this.setExtension(fDs.getExtension());
			this.setDataTypeLoaded(fDs.isDataTypeLoaded());
		}
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
	
	public static List<DataSourceField> cloneAll(List<DataSourceField> toCloneFrom, ObjectDataSource toCloneTo) {
		if (toCloneFrom == null)
			return null;
		
		List<DataSourceField> clonedItems = new ArrayList<>(toCloneFrom.size());
		
		for (DataSourceField dsF : toCloneFrom) {
			DataSourceField clonedItem = new DataSourceField();
			
			clonedItem.copyFrom(dsF);
			clonedItem.setDataSource(toCloneTo);
			
			clonedItems.add(clonedItem);
		}
		
		return clonedItems;
	}
	
}
