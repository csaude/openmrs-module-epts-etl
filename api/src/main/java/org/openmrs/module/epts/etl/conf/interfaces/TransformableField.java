package org.openmrs.module.epts.etl.conf.interfaces;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.epts.etl.conf.DstConf;
import org.openmrs.module.epts.etl.etl.processor.transformer.ArithmeticFieldTransformer;
import org.openmrs.module.epts.etl.etl.processor.transformer.DefaultFieldTransformer;
import org.openmrs.module.epts.etl.etl.processor.transformer.EtlFieldTransformer;
import org.openmrs.module.epts.etl.etl.processor.transformer.EtlRecordTransformer;
import org.openmrs.module.epts.etl.etl.processor.transformer.MappingFieldTransformer;
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
	
	Object getDefaultValue();
	
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
	
	@SuppressWarnings({ "unchecked" })
	default void tryToLoadTransformer() {
		if (this.hasTransformer()) {
			
			String transformerStr = this.getTransformer();
			String className = transformerStr;
			
			if (transformerStr.contains("(") && transformerStr.endsWith(")")) {
				int start = transformerStr.indexOf("(");
				
				className = transformerStr.substring(0, start).trim();
			}
			
			if (this.getTransformer().startsWith(EtlFieldTransformer.STRING_TRANSFORMER)) {
				this.setTransformerInstance(StringTranformer.getInstance());
			} else if (this.getTransformer().startsWith(EtlFieldTransformer.ARITHMETIC_TRANSFORMER)) {
				this.setTransformerInstance(ArithmeticFieldTransformer.getInstance());
			} else if (this.getTransformer().startsWith(EtlFieldTransformer.MAPPING_TRANSFORMER)) {
				this.setTransformerInstance(MappingFieldTransformer.getInstance(this.tryToLoadTransformerParameters()));
			} else {
				try {
					ClassLoader loader = EtlRecordTransformer.class.getClassLoader();
					
					Class<? extends EtlFieldTransformer> transformerClazz = (Class<? extends EtlFieldTransformer>) loader
					        .loadClass(className);
					
					List<Object> transformerParameters = this.tryToLoadTransformerParameters();
					
					EtlFieldTransformer instance;
					
					if (transformerParameters == null || transformerParameters.isEmpty()) {
						instance = transformerClazz.getDeclaredConstructor().newInstance();
					} else {
						Class<?>[] paramTypes = transformerParameters.stream().map(Object::getClass)
						        .toArray(Class<?>[]::new);
						
						Constructor<? extends EtlFieldTransformer> constructor = transformerClazz
						        .getDeclaredConstructor(paramTypes);
						
						instance = constructor.newInstance(transformerParameters.toArray());
					}
					
					this.setTransformerInstance(instance);
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
	
	default List<Object> tryToLoadTransformerParameters() {
		String transformerStr = this.getTransformer();
		List<Object> params = new ArrayList<>();
		
		if (transformerStr.contains("(") && transformerStr.endsWith(")")) {
			
			int start = transformerStr.indexOf("(");
			int end = transformerStr.lastIndexOf(")");
			
			String paramsStr = transformerStr.substring(start + 1, end).trim();
			
			if (!paramsStr.isEmpty()) {
				String[] splitParams = paramsStr.split(",");
				
				for (String p : splitParams) {
					params.add(p.trim());
				}
			}
		}
		
		return params;
	}
	
}
