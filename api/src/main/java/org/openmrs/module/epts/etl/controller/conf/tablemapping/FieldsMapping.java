package org.openmrs.module.epts.etl.controller.conf.tablemapping;

import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.epts.etl.conf.DstConf;
import org.openmrs.module.epts.etl.conf.EtlField;
import org.openmrs.module.epts.etl.conf.Extension;
import org.openmrs.module.epts.etl.conf.interfaces.EtlDataSource;
import org.openmrs.module.epts.etl.conf.interfaces.TransformableField;
import org.openmrs.module.epts.etl.etl.processor.transformer.ArithmeticFieldTransformer;
import org.openmrs.module.epts.etl.etl.processor.transformer.DefaultFieldTransformer;
import org.openmrs.module.epts.etl.etl.processor.transformer.EtlFieldTransformer;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.Field;
import org.openmrs.module.epts.etl.utilities.AttDefinedElements;
import org.openmrs.module.epts.etl.utilities.CommonUtilities;

/**
 * This class is used to map fields between any source table and destination table
 * 
 * @author jpboane
 */
public class FieldsMapping implements TransformableField {
	
	static CommonUtilities utilities = CommonUtilities.getInstance();
	
	private String srcValue;
	
	private String srcField;
	
	private String dataSourceName;
	
	private String dstField;
	
	private boolean mapToNullValue;
	
	private String transformer;
	
	private EtlFieldTransformer transformerInstance;
	
	private Extension extension;
	
	private String dataType;
	
	private boolean dataTypeLoaded;
	
	public FieldsMapping() {
	}
	
	public FieldsMapping(String srcField, String dataSourceName, String destField) {
		this.srcField = srcField;
		this.dataSourceName = dataSourceName;
		this.dstField = destField;
	}
	
	public boolean isDataTypeLoaded() {
		return dataTypeLoaded;
	}
	
	public void setDataTypeLoaded(boolean dataTypeLoaded) {
		this.dataTypeLoaded = dataTypeLoaded;
	}
	
	public String getDataType() {
		return dataType;
	}
	
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}
	
	public static FieldsMapping fastCreate(String srcField, String destField) {
		return new FieldsMapping(srcField, null, destField);
	}
	
	public static FieldsMapping fastCreate(String fieldName) {
		return fastCreate(fieldName, fieldName);
	}
	
	public EtlFieldTransformer getTransformerInstance() {
		return this.transformerInstance;
	}
	
	public Extension getExtension() {
		return extension;
	}
	
	public void setExtension(Extension extension) {
		this.extension = extension;
	}
	
	public boolean useDefaultTransformer() {
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
	
	public void setMapToNullValue(boolean b) {
		mapToNullValue = b;
	}
	
	public boolean isMapToNullValue() {
		return mapToNullValue;
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
		f.setType(srcField.getType());
		
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
				
				dstField.setType(fm.getDataType());
			}
			
			parsed.add(dstField);
			
		}
		
		return parsed;
	}
	
	public void loadType(DstConf dstConf, EtlDataSource dataSource) {
		if (this.hasDataType()) {
			if (!utilities.isStringIn(this.getDataType().toLowerCase(), "int", "double", "string", "date", "long")) {
				throw new ForbiddenOperationException("Unsupported dataType for dstField " + this.getDstField());
			}
		} else if (dstConf.containsField(this.getDstField())) {
			this.setDataType(dstConf.getField(this.getDstField()).getType());
		} else if (this.hasSrcField()) {
			if (dataSource != null) {
				if (dataSource.containsField(this.getSrcField())) {
					this.setDataType(dataSource.getField(this.getSrcField()).getType());
				} else {
					throw new ForbiddenOperationException(
					        "The Datasource (" + dataSource.getName() + ") does not contain the src field "
					                + this.getSrcField() + " for dstField " + this.getDstField());
				}
			} else {
				throw new ForbiddenOperationException("There is no datasource for " + this.getSrcField());
			}
		} else if (this.hasTransformer()) {
			
			if (!this.hasTransformerInstance()) {
				throw new ForbiddenOperationException("The transformer instance for dstField " + this.getDstField()
				        + " was not loaded! Please load the transformer instance before!!!");
			}
			
			if (this.getTransformerInstance() instanceof ArithmeticFieldTransformer) {
				this.setDataType("double");
			} else {
				this.setDataType("String");
			}
		} else {
			if (!this.hasTransformer()) {
				throw new ForbiddenOperationException("There is no transformer for dstField " + this.getDstField());
			}
		}
		
		this.setDataTypeLoaded(true);
	}
	
	public boolean hasDataType() {
		return utilities.stringHasValue(this.getDataType());
	}
	
	public boolean hasSrcField() {
		return utilities.stringHasValue(this.getSrcField());
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
	
	public static FieldsMapping converteFromEtlField(EtlField field) {
		
		if (field.getSrcDataSource() == null) {
			throw new ForbiddenOperationException("The EtlField " + field.getName() + " has no datasource!");
		}
		
		FieldsMapping fm = new FieldsMapping();
		
		fm.setSrcField(field.getSrcField().getName());
		fm.setDataSourceName(field.getSrcDataSource().getName());
		fm.setDstField(field.getName());
		
		fm.tryToLoadTransformer();
		
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
	
	public boolean hasDataSourceName() {
		return utilities.stringHasValue(this.getDataSourceName());
	}
	
	@Override
	public String getValueToTransform() {
		return this.getSrcValue();
	}
	
	@Override
	public String getName() {
		return this.dstField;
	}
	
}
