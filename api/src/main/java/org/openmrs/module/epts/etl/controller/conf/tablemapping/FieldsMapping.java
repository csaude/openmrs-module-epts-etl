package org.openmrs.module.epts.etl.controller.conf.tablemapping;

import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.epts.etl.conf.DstConf;
import org.openmrs.module.epts.etl.conf.EtlField;
import org.openmrs.module.epts.etl.conf.Extension;
import org.openmrs.module.epts.etl.conf.datasource.DataSourceField;
import org.openmrs.module.epts.etl.conf.datasource.SrcConf;
import org.openmrs.module.epts.etl.conf.interfaces.EtlAdditionalDataSource;
import org.openmrs.module.epts.etl.conf.interfaces.EtlDataSource;
import org.openmrs.module.epts.etl.conf.interfaces.TransformableField;
import org.openmrs.module.epts.etl.conf.types.EtlNullBehavior;
import org.openmrs.module.epts.etl.conf.types.RelationshipResolutionStrategy;
import org.openmrs.module.epts.etl.etl.processor.transformer.DefaultFieldTransformer;
import org.openmrs.module.epts.etl.etl.processor.transformer.EtlFieldTransformer;
import org.openmrs.module.epts.etl.exceptions.EtlExceptionImpl;
import org.openmrs.module.epts.etl.exceptions.FieldNotAvaliableInAnyDataSource;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.Field;
import org.openmrs.module.epts.etl.utilities.AttDefinedElements;
import org.openmrs.module.epts.etl.utilities.CommonUtilities;
import org.openmrs.module.epts.etl.utilities.db.conn.SQLUtilities;

/**
 * This class is used to map fields between any source table and destination table
 * 
 * @author jpboane
 */
public class FieldsMapping extends Field implements TransformableField {
	
	private static final long serialVersionUID = -2713197928272643006L;
	
	static CommonUtilities utilities = CommonUtilities.getInstance();
	
	private Object srcValue;
	
	private Object dstValue;
	
	private String srcField;
	
	private String dataSourceName;
	
	private String dstField;
	
	private Boolean mapToNullValue;
	
	private String transformer;
	
	private EtlFieldTransformer transformerInstance;
	
	private Extension extension;
	
	private Boolean dataTypeLoaded;
	
	private List<String> possibleSrc;
	
	private Object defaultValue;
	
	private Object overrideTriggerValue;
	
	private String originalSrcFieldDefinition;
	
	private RelationshipResolutionStrategy relationshipResolutionStrategy;
	
	private EtlNullBehavior nullValueBehavior;
	
	private EtlDataSource dataSource;
	
	public FieldsMapping() {
		this.nullValueBehavior = EtlNullBehavior.ALLOW;
		this.relationshipResolutionStrategy = RelationshipResolutionStrategy.RESOLVE;
		
		this.possibleSrc = new ArrayList<>(5);
	}
	
	public FieldsMapping(String srcFieldFullName, String dstField, Boolean tryToLoadTransformer) {
		this();
		
		this.setOriginalSrcFieldDefinition(srcFieldFullName);
		
		String[] fieldParts = utilities.stringHasValue(srcFieldFullName) ? srcFieldFullName.toString().split("\\.") : null;
		
		if (fieldParts != null) {
			if (fieldParts.length > 1) {
				this.dataSourceName = fieldParts[0];
				this.srcField = fieldParts[1];
			} else {
				if (utilities.isNumeric(fieldParts[0])) {
					this.srcValue = fieldParts[0];
				} else {
					this.srcField = fieldParts[0];
				}
			}
		} else {
			setMapToNullValue(true);
		}
		
		this.dstField = dstField != null ? dstField : this.srcField;
		
		if (dstField == null) {
			throw new EtlExceptionImpl("A FieldsMapping must have at least a srcFieldName or dstField");
		}
		
		if (tryToLoadTransformer)
			tryToLoadTransformer(null);
	}
	
	public EtlNullBehavior getNullValueBehavior() {
		return nullValueBehavior;
	}
	
	public void setNullValueBehavior(EtlNullBehavior nullValueBehavior) {
		this.nullValueBehavior = nullValueBehavior;
	}
	
	@Override
	public EtlNullBehavior nullValueBehavior() {
		return this.nullValueBehavior;
	}
	
	public RelationshipResolutionStrategy getRelationshipResolutionStrategy() {
		return relationshipResolutionStrategy;
	}
	
	public void setRelationshipResolutionStrategy(RelationshipResolutionStrategy relationshipResolutionStrategy) {
		this.relationshipResolutionStrategy = relationshipResolutionStrategy;
	}
	
	@Override
	public RelationshipResolutionStrategy relationshipResolutionStrategy() {
		return this.relationshipResolutionStrategy;
	}
	
	public String getOriginalSrcFieldDefinition() {
		return originalSrcFieldDefinition;
	}
	
	public void setOriginalSrcFieldDefinition(String originalSrcFieldDefinition) {
		this.originalSrcFieldDefinition = originalSrcFieldDefinition;
	}
	
	public FieldsMapping(String srcField, String dataSourceName, String dstField) {
		this(srcField, dstField, false);
		
		if (dataSourceName != null) {
			if (hasDataSourceName() && dataSourceName != null && this.dataSourceName.equals(dataSourceName)) {
				throw new EtlExceptionImpl(
				        "Mismatch datasource definition for " + srcField + ". On Field datasource definition '"
				                + this.dataSourceName + "' difer to parameter datasource '" + dataSourceName + "'");
			}
			
			this.possibleSrc.add(dataSourceName);
			this.dataSourceName = dataSourceName;
			
		} else {
			this.dataSourceName = dataSourceName;
		}
		
		if (srcField == null) {
			setMapToNullValue(true);
		}
		
		tryToLoadTransformer(null);
	}
	
	public static FieldsMapping fastCreate(DataSourceField dsF) {
		
		FieldsMapping f = FieldsMapping.fastCreate(dsF.getValue().toString(), dsF.getDstField(), true);
		
		if (dsF.getValue().toString().startsWith("@")) {
			
			String fn = dsF.getValue().toString().substring(1);
			
			Object paramValue = dsF.getParent().getRelatedEtlConf().getParamValue(fn);
			
			if (paramValue == null) {
				f.setSrcField(fn);
				
				EtlDataSource ds;
				
				if (dsF.getParent() instanceof SrcConf) {
					ds = dsF.getParent();
				} else if (dsF.getParent() instanceof EtlAdditionalDataSource) {
					ds = ((EtlAdditionalDataSource) dsF.getParent()).getRelatedSrcConf();
				} else {
					throw new EtlExceptionImpl("Unsupported datasource type " + dsF.getParent());
				}
				
				f.setDataSourceName(ds.getAlias());
			} else {
				f.setSrcValue(paramValue);
				f.setSrcField(null);
			}
		}
		
		return f;
		
	}
	
	@Override
	public Object getOverrideTriggerValue() {
		return this.overrideTriggerValue;
	}
	
	@Override
	public void setOverrideTriggerValue(Object overrideTriggerValue) {
		this.overrideTriggerValue = overrideTriggerValue;
	}
	
	@Override
	public Object getDefaultValue() {
		return this.defaultValue;
	}
	
	public void setDefaultValue(Object defaultValue) {
		this.defaultValue = defaultValue;
	}
	
	public List<String> getPossibleSrc() {
		return possibleSrc;
	}
	
	public void setPossibleSrc(List<String> possibleSrc) {
		this.possibleSrc = possibleSrc;
	}
	
	public Boolean isDataTypeLoaded() {
		return dataTypeLoaded != null && dataTypeLoaded;
	}
	
	public void setDataTypeLoaded(Boolean dataTypeLoaded) {
		this.dataTypeLoaded = dataTypeLoaded;
	}
	
	public static FieldsMapping fastCreate(String srcField, String destField, Boolean tryToLoadTYransformer) {
		return new FieldsMapping(srcField, destField, tryToLoadTYransformer);
	}
	
	public static FieldsMapping fastCreate(String fieldName) {
		Boolean loadTransformer = SQLUtilities.checkIfFieldDefinitionIncludeQualifier(fieldName);
		
		return fastCreate(fieldName, fieldName, loadTransformer);
	}
	
	public static FieldsMapping fastCreate(String fullFieldName, String dstField, DstConf dstConf) {
		FieldsMapping fieldMap = FieldsMapping.fastCreate(fullFieldName, dstField, false);
		
		fieldMap.tryToLoadDataSourceAndTransformer(fieldMap.getDataSourceName(), dstConf);
		
		return fieldMap;
		
	}
	
	private void tryToLoadDataSourceAndTransformer(String dataSourceName, DstConf dstConf) {
		if (dataSourceName != null) {
			EtlDataSource ds = dstConf.findDataSource(dataSourceName);
			
			if (ds != null) {
				this.setDataSourceName(ds.getAlias());
			} else {
				throw new EtlExceptionImpl("Invalid datasource '" + dataSourceName + "' on field definition '"
				        + this.getOriginalSrcFieldDefinition() + "'");
			}
			
		} else {
			try {
				dstConf.tryToLoadDataSourceToFieldMapping(this);
			}
			catch (FieldNotAvaliableInAnyDataSource e) {
				this.setSrcValue(this.getSrcField());
				this.setSrcField(null);
			}
			
		}
		
		this.tryToLoadTransformer(dstConf);
	}
	
	public void fullLoad(DstConf dstConf) {
		this.copyFrom(fastCreate(this.srcField, this.dstField, dstConf));
		
	}
	
	public EtlFieldTransformer getTransformerInstance() {
		return this.transformerInstance;
	}
	
	public Boolean useDefaultTransformer() {
		return getTransformerInstance() instanceof DefaultFieldTransformer;
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
	
	public Object getSrcValue() {
		return srcValue;
	}
	
	public void setSrcValue(Object srcValue) {
		this.srcValue = srcValue;
	}
	
	public Object getDstValue() {
		return dstValue;
	}
	
	public void setDstValue(Object dstValue) {
		this.dstValue = dstValue;
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
		
		if (this.getPossibleSrc().size() == 0) {
			this.getPossibleSrc().add(dataSourceName);
		} else if (this.getPossibleSrc().size() == 1) {
			this.getPossibleSrc().set(0, dataSourceName);
		}
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
		String str = srcField != null ? ("srcField: " + srcField) : "";
		
		String separator = !str.isEmpty() && srcValue != null ? ", " : "";
		
		str += separator + (srcValue != null ? ("srcValue: " + srcValue) : "");
		
		separator = !str.isEmpty() && dstField != null ? ", " : "";
		
		str += separator + (dstField != null ? ("dstField: " + dstField) : "");
		
		separator = !str.isEmpty() && dataSourceName != null ? ", " : "";
		
		str += separator + (dataSourceName != null ? ("dataSourceName: " + dataSourceName) : "");
		
		return "[" + str + "]";
	}
	
	public void setMapToNullValue(Boolean b) {
		mapToNullValue = b;
	}
	
	public Boolean isMapToNullValue() {
		return mapToNullValue != null && mapToNullValue;
	}
	
	/**
	 * Parse this mapping to {@link Field}. The parsed field represent the {@link #dstField}
	 * 
	 * @param srcField the related src field
	 * @return the parsed field
	 */
	public Field parseToField(Field srcField) {
		Field f = (Field) srcField.cloneMe();
		
		f.setName(this.getDstField());
		f.setDataType(srcField.getDataType());
		
		return f;
	}
	
	public static List<Field> parseAllToField(List<FieldsMapping> toParse, DstConf dstConf, List<EtlDataSource> dataSource) {
		List<Field> parsed = new ArrayList<>(toParse.size());
		
		for (FieldsMapping fm : toParse) {
			
			Field dstField = null;
			
			if (fm.hasSrcField()) {
				Field srcField = fm.findSrcFieldInDataSource(dataSource);
				
				dstField = fm.parseToField(srcField);
			} else {
				//If there is no srcField then there must be a transformer
				
				fm.loadType(dstConf, null);
				
				dstField = Field.fastCreateField(fm.getDstField());
				
				if (!fm.isDataTypeLoaded()) {
					throw new ForbiddenOperationException(
					        "The dataType for dstField " + fm.getDstField() + " was not loaded");
				}
				
				dstField.setDataType(fm.getDataType());
			}
			
			parsed.add(dstField);
			
		}
		
		return parsed;
	}
	
	public Boolean hasSrcField() {
		return utilities.stringHasValue(this.getSrcField());
	}
	
	public Boolean hasSrcValue() {
		return this.getSrcValue() != null;
	}
	
	public Boolean hasDstField() {
		return utilities.stringHasValue(this.getDstField());
	}
	
	public Boolean hasDstValue() {
		return this.getDstValue() != null;
	}
	
	private Field findSrcFieldInDataSource(List<EtlDataSource> dataSource) {
		return findContainingDataSource(dataSource).getField(this.getSrcField());
	}
	
	private EtlDataSource findContainingDataSource(List<EtlDataSource> dataSource) {
		
		for (EtlDataSource ds : dataSource) {
			
			if (ds.getName().equals(this.getDataSourceName()) || ds.getAlias().equals(this.getDataSourceName())) {
				
				if (ds.getField(this.getSrcField()) == null) {
					throw new ForbiddenOperationException(
					        "No field '" + this.getSrcField() + "' found on datasource " + this.getDataSourceName() + "!");
				}
				
				return ds;
			}
		}
		
		throw new ForbiddenOperationException("The field '" + this.getSrcField() + "' was not found in any datasource ");
		
	}
	
	public static FieldsMapping converteFromEtlField(EtlField field, DstConf dstConf) {
		
		if (field.getSrcDataSource() == null) {
			throw new ForbiddenOperationException("The EtlField " + field.getName() + " has no datasource!");
		}
		
		FieldsMapping fm = new FieldsMapping();
		
		fm.setSrcField(field.getSrcField().getName());
		fm.setDataSourceName(field.getSrcDataSource().getName());
		fm.setDstField(field.getName());
		fm.tryToLoadTransformer(dstConf);
		
		return fm;
	}
	
	public void tryToLoadDataSourceInfoFromSrcField() {
		
		if (this.hasSrcField()) {
			String[] srcFieldParts = this.getSrcField().split("\\.");
			
			if (srcFieldParts.length > 2) {
				throw new ForbiddenOperationException("Malformed srcField " + this.getSrcField());
			} else if (srcFieldParts.length == 2) {
				if (!this.hasDataSourceName()) {
					this.setDataSourceName(srcFieldParts[0]);
				}
				
				this.setSrcField(srcFieldParts[1]);
			}
		}
		
	}
	
	public Boolean hasDataSourceName() {
		return utilities.stringHasValue(this.getDataSourceName());
	}
	
	@Override
	public Object getValueToTransform() {
		return this.getSrcValue();
	}
	
	@Override
	public String getName() {
		return hasSrcField() ? this.getSrcField() : this.getDstField();
	}
	
	public static List<FieldsMapping> cloneAll(List<FieldsMapping> toClone) {
		if (toClone == null)
			return null;
		
		List<FieldsMapping> cloned = new ArrayList<>(toClone.size());
		
		for (FieldsMapping f : toClone) {
			FieldsMapping clonedF = new FieldsMapping(f.getSrcField(), null, f.getDstField());
			
			clonedF.setSrcValue(f.getSrcValue());
			clonedF.setDstValue(f.getDstValue());
			
			cloned.add(clonedF);
		}
		
		return cloned;
	}
	
	@Override
	public void copyFrom(Field toCotoCOpyFrompyFrom) {
		super.copyFrom(toCotoCOpyFrompyFrom);
		
		if (toCotoCOpyFrompyFrom instanceof FieldsMapping) {
			FieldsMapping toCopyFormAsFieldsMapping = (FieldsMapping) toCotoCOpyFrompyFrom;
			
			this.srcValue = toCopyFormAsFieldsMapping.srcValue;
			this.dstValue = toCopyFormAsFieldsMapping.dstValue;
			this.srcField = toCopyFormAsFieldsMapping.srcField;
			this.dataSourceName = toCopyFormAsFieldsMapping.dataSourceName;
			this.dstField = toCopyFormAsFieldsMapping.dstField;
			this.mapToNullValue = toCopyFormAsFieldsMapping.mapToNullValue;
			this.transformer = toCopyFormAsFieldsMapping.transformer;
			this.transformerInstance = toCopyFormAsFieldsMapping.transformerInstance;
			this.extension = toCopyFormAsFieldsMapping.extension;
			this.dataTypeLoaded = toCopyFormAsFieldsMapping.dataTypeLoaded;
			this.possibleSrc = toCopyFormAsFieldsMapping.possibleSrc;
			this.defaultValue = toCopyFormAsFieldsMapping.defaultValue;
			this.overrideTriggerValue = toCopyFormAsFieldsMapping.overrideTriggerValue;
			this.nullValueBehavior = toCopyFormAsFieldsMapping.nullValueBehavior;
			this.relationshipResolutionStrategy = toCopyFormAsFieldsMapping.relationshipResolutionStrategy;
		}
	}
	
	@Override
	public void tryToReplacePlaceholders(EtlDatabaseObject schemaInfoSrc) {
		super.tryToReplacePlaceholders(schemaInfoSrc);
		
		setSrcValue(utilities.tryToReplacePlaceholders(getSrcValue(), schemaInfoSrc));
		setDstValue(utilities.tryToReplacePlaceholders(getDstValue(), schemaInfoSrc));
		setSrcField(utilities.tryToReplacePlaceholders(getSrcField(), schemaInfoSrc));
		setDataSourceName(utilities.tryToReplacePlaceholders(getDataSourceName(), schemaInfoSrc));
		setDstField(utilities.tryToReplacePlaceholders(getDstField(), schemaInfoSrc));
		setDataType(utilities.tryToReplacePlaceholders(getDataType(), schemaInfoSrc));
	}
	
	@Override
	public EtlDataSource getDataSource() {
		return this.dataSource;
	}
	
	public void setDataSource(EtlDataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	public Boolean isSetToNullValue() {
		return hasSrcValue() && srcValue.toString().toLowerCase().equals("null");
	}
}
