package org.openmrs.module.epts.etl.conf.interfaces;

import org.openmrs.module.epts.etl.conf.DstConf;
import org.openmrs.module.epts.etl.etl.processor.transformer.ArithmeticFieldTransformer;
import org.openmrs.module.epts.etl.etl.processor.transformer.DefaultFieldTransformer;
import org.openmrs.module.epts.etl.etl.processor.transformer.EtlFieldTransformer;
import org.openmrs.module.epts.etl.etl.processor.transformer.EtlRecordTransformer;
import org.openmrs.module.epts.etl.etl.processor.transformer.SimpleValueTransformer;
import org.openmrs.module.epts.etl.etl.processor.transformer.StringTranformer;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.utilities.CommonUtilities;

/**
 * In an ETL a {@link TransformableField} represents a field which can have its value (src-value)
 * transformed
 */
public interface TransformableField {
	
	static CommonUtilities utilities = CommonUtilities.getInstance();
	
	String getName();
	
	EtlFieldTransformer getTransformerInstance();
	
	void setTransformerInstance(EtlFieldTransformer transformer);
	
	String getTransformer();
	
	String getValueToTransform();
	
	String getDataSourceName();
	
	String getDataType();
	
	void setDataType(String dataType);
	
	void setTransformer(String transformer);
	
	boolean hasSrcField();
	
	void setDataTypeLoaded(boolean dataTypeLoaded);
	
	String getDstField();
	
	String getSrcField();
	
	default boolean hasDataType() {
		return utilities.stringHasValue(this.getDataType());
	}
	
	default void loadType(DstConf dstConf, EtlDataSource dataSource) {
		if (this.hasDataType()) {
			if (!utilities.isStringIn(this.getDataType().toLowerCase(), "int", "double", "string", "date", "long")) {
				throw new ForbiddenOperationException("Unsupported dataType for field " + this.getDstField());
			}
		} else if (dstConf != null && dstConf.containsField(this.getDstField())) {
			this.setDataType(dstConf.getField(this.getDstField()).getDataType());
		} else if (this.hasSrcField()) {
			if (dataSource != null) {
				if (dataSource.containsField(this.getSrcField())) {
					this.setDataType(dataSource.getField(this.getSrcField()).getDataType());
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
	
	default boolean hasTransformerInstance() {
		return this.getTransformerInstance() != null;
	}
	
	default boolean hasTransformer() {
		return getTransformer() != null;
	}
	
	@SuppressWarnings("unchecked")
	default void tryToLoadTransformer() {
		if (this.hasTransformer()) {
			
			if (this.getTransformer().equals(EtlFieldTransformer.STRING_TRANSFORMER)) {
				this.setTransformer(StringTranformer.class.getCanonicalName());
				this.setTransformerInstance(StringTranformer.getInstance());
			} else if (this.getTransformer().equals(EtlFieldTransformer.ARITHMETIC_TRANSFORMER)) {
				this.setTransformer(ArithmeticFieldTransformer.class.getCanonicalName());
				this.setTransformerInstance(ArithmeticFieldTransformer.getInstance());
			} else {
				try {
					ClassLoader loader = EtlRecordTransformer.class.getClassLoader();
					
					Class<? extends EtlFieldTransformer> transformerClazz = (Class<? extends EtlFieldTransformer>) loader
					        .loadClass(this.getTransformer());
					
					this.setTransformerInstance(transformerClazz.newInstance());
				}
				catch (Exception e) {
					throw new ForbiddenOperationException(
					        "Error loading transformer class [" + this.getTransformer() + "]!!! " + e.getLocalizedMessage());
				}
			}
		} else if (this.getValueToTransform() != null) {
			this.setTransformer(SimpleValueTransformer.class.getCanonicalName());
			this.setTransformerInstance(SimpleValueTransformer.getInstance());
		} else {
			this.setTransformer(DefaultFieldTransformer.class.getCanonicalName());
			
			this.setTransformerInstance(DefaultFieldTransformer.getInstance());
		}
	}
}
