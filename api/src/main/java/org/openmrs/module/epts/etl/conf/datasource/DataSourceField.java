package org.openmrs.module.epts.etl.conf.datasource;

import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.epts.etl.conf.interfaces.EtlDataSource;
import org.openmrs.module.epts.etl.conf.interfaces.TransformableField;
import org.openmrs.module.epts.etl.conf.types.EtlNullBehavior;
import org.openmrs.module.epts.etl.conf.types.RelationshipResolutionStrategy;
import org.openmrs.module.epts.etl.controller.conf.tablemapping.FieldsMapping;
import org.openmrs.module.epts.etl.etl.processor.transformer.EtlFieldTransformer;
import org.openmrs.module.epts.etl.model.Field;

public class DataSourceField extends Field implements TransformableField {
	
	private static final long serialVersionUID = -7824136202167355998L;
	
	private String transformer;
	
	private EtlFieldTransformer transformerInstance;
	
	private Boolean dataTypeLoaded;
	
	private Object defaultValue;
	
	private Object overrideTriggerValue;
	
	private String srcField;
	
	private FieldsMapping auxFieldMapping;
	
	private RelationshipResolutionStrategy relationshipResolutionStrategy;
	
	private EtlNullBehavior nullValueBehavior;
	
	private EtlDataSource parent;
	
	public DataSourceField() {
		this.nullValueBehavior = EtlNullBehavior.ALLOW;
		this.relationshipResolutionStrategy = RelationshipResolutionStrategy.RESOLVE;
	}
	
	public static DataSourceField fastCreate(String name, Object value) {
		DataSourceField ds = new DataSourceField();
		
		ds.setValue(value);
		ds.setName(name);
		
		return ds;
	}
	
	public RelationshipResolutionStrategy getRelationshipResolutionStrategy() {
		return relationshipResolutionStrategy;
	}
	
	public void setRelationshipResolutionStrategy(RelationshipResolutionStrategy relationshipResolutionStrategy) {
		this.relationshipResolutionStrategy = relationshipResolutionStrategy;
	}
	
	public EtlDataSource getParent() {
		return parent;
	}
	
	public void setParent(EtlDataSource parent) {
		this.parent = parent;
	}
	
	@Override
	public Object getOverrideTriggerValue() {
		return this.overrideTriggerValue;
	}
	
	@Override
	public void setOverrideTriggerValue(Object overrideTriggerValue) {
		this.overrideTriggerValue = overrideTriggerValue;
	}
	
	public void setNullValueBehavior(EtlNullBehavior nullValueBehavior) {
		this.nullValueBehavior = nullValueBehavior;
	}
	
	@Override
	public EtlNullBehavior nullValueBehavior() {
		return this.nullValueBehavior;
	}
	
	@Override
	public String getDataSourceName() {
		return this.getParent().getName();
	}
	
	@Override
	public Object getDefaultValue() {
		return this.defaultValue;
	}
	
	public void setDefaultValue(Object defaultValue) {
		this.defaultValue = defaultValue;
	}
	
	@Override
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
	
	public Boolean isDataTypeLoaded() {
		return dataTypeLoaded != null && dataTypeLoaded;
	}
	
	public void setDataTypeLoaded(Boolean dataTypeLoaded) {
		this.dataTypeLoaded = dataTypeLoaded;
	}
	
	public FieldsMapping getAuxFieldMapping() {
		return auxFieldMapping;
	}
	
	public void setAuxFieldMapping(FieldsMapping auxFieldMapping) {
		this.auxFieldMapping = auxFieldMapping;
	}
	
	public Boolean hasAuxFieldMapping() {
		return this.auxFieldMapping != null;
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
			this.setSrcField(fDs.getSrcField());
			this.setAuxFieldMapping(fDs.getAuxFieldMapping());
			this.setNullValueBehavior(fDs.nullValueBehavior());
			this.setRelationshipResolutionStrategy(fDs.getRelationshipResolutionStrategy());
		}
	}
	
	@Override
	public Object getValueToTransform() {
		return this.getValue();
	}
	
	@Override
	public Boolean hasSrcField() {
		return Boolean.FALSE;
	}
	
	@Override
	public String getDstField() {
		return this.getName();
	}
	
	@Override
	public String getSrcField() {
		return this.srcField;
	}
	
	public void setSrcField(String srcField) {
		this.srcField = srcField;
	}
	
	public static List<DataSourceField> cloneAll(List<DataSourceField> toCloneFrom, ObjectDataSource toCloneTo) {
		if (toCloneFrom == null)
			return null;
		
		List<DataSourceField> clonedItems = new ArrayList<>(toCloneFrom.size());
		
		for (DataSourceField dsF : toCloneFrom) {
			DataSourceField clonedItem = new DataSourceField();
			
			clonedItem.copyFrom(dsF);
			clonedItem.setParent(toCloneTo);
			
			clonedItems.add(clonedItem);
		}
		
		return clonedItems;
	}
	
	@Override
	public RelationshipResolutionStrategy relationshipResolutionStrategy() {
		return this.relationshipResolutionStrategy;
	}
	
	@Override
	public EtlDataSource getDataSource() {
		return this.parent;
	}
}
