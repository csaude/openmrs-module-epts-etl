package org.openmrs.module.epts.etl.conf.interfaces;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.epts.etl.conf.DstConf;
import org.openmrs.module.epts.etl.etl.processor.transformer.ArithmeticFieldTransformer;
import org.openmrs.module.epts.etl.etl.processor.transformer.CoalesceFieldTransformer;
import org.openmrs.module.epts.etl.etl.processor.transformer.DefaultFieldTransformer;
import org.openmrs.module.epts.etl.etl.processor.transformer.EtlFieldTransformer;
import org.openmrs.module.epts.etl.etl.processor.transformer.EtlRecordTransformer;
import org.openmrs.module.epts.etl.etl.processor.transformer.FastSqlFieldTransformer;
import org.openmrs.module.epts.etl.etl.processor.transformer.MappingFieldTransformer;
import org.openmrs.module.epts.etl.etl.processor.transformer.ParentOnDemandLoadTransformer;
import org.openmrs.module.epts.etl.etl.processor.transformer.SimpleValueTransformer;
import org.openmrs.module.epts.etl.etl.processor.transformer.StringTranformer;
import org.openmrs.module.epts.etl.etl.processor.transformer.openmrs.OpenMrsEncounterForObsOnDemandLoadTransformer;
import org.openmrs.module.epts.etl.etl.processor.transformer.openmrs.OpenMrsVisitOnDemandLoadTransformer;
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
	
	/**
	 * Returns the value that triggers the override mechanism. If the source field value equals this
	 * value, the field value will be replaced by the configured {@code defaultValue}. Example:
	 * <pre>
	 * overrideTriggerValue = ""
	 * defaultValue = "UNKNOWN"
	 * </pre> If the source value is an empty string, the resulting value will be "UNKNOWN".
	 *
	 * @return the value that triggers overriding the source value with the default value
	 */
	Object getOverrideTriggerValue();
	
	/**
	 * Sets the value that should trigger the override mechanism.
	 *
	 * @param obj the value that, when matched against the source value, causes the default value to
	 *            be used instead
	 */
	void setOverrideTriggerValue(Object obj);
	
	/**
	 * Determines whether the provided value should be overridden by the configured
	 * {@code defaultValue}. The override happens only if:
	 * <ul>
	 * <li>{@code overrideValueIfEqualsTo} is defined</li>
	 * <li>{@code defaultValue} is defined</li>
	 * <li>The provided value equals {@code overrideValueIfEqualsTo}</li>
	 * </ul>
	 *
	 * @param obj the source field value to evaluate
	 * @return {@code true} if the value should be replaced by {@code defaultValue}, {@code false}
	 *         otherwise
	 * @throws ForbiddenOperationException if an override value is defined but no default value is
	 *             configured
	 */
	default boolean shouldOverrideValue(Object obj) throws ForbiddenOperationException {
		
		if (this.getOverrideTriggerValue() != null) {
			
			if (this.getDefaultValue() == null) {
				throw new ForbiddenOperationException("Cannot override!! No defaultValue is set to this field.");
			}
			
			if (obj instanceof Boolean) {
				if (this.getOverrideTriggerValue() instanceof Boolean) {
					return this.getOverrideTriggerValue().equals(obj);
				}
				if (this.getOverrideTriggerValue() instanceof String) {
					Boolean true_ = utilities.isStringIn(this.getOverrideTriggerValue().toString(), "true", "1");
					
					return true_.equals(obj);
				}
			} else
				return this.getOverrideTriggerValue().equals(obj);
		}
		
		return false;
	}
	
	default boolean hasDataType() {
		return utilities.stringHasValue(this.getDataType());
	}
	
	default void loadType(DstConf dstConf, EtlDataSource dataSource) {
		if (this.hasDataType()) {
			if (!utilities.isStringIn(this.getDataType().toLowerCase(), "int", "double", "string", "date", "long",
			    "boolean")) {
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
		return utilities.stringHasValue(getTransformer());
	}
	
	@SuppressWarnings({ "unchecked" })
	default void tryToLoadTransformer(DstConf dstConf) {
		
		if (getTransformerInstance() != null) {
			return;
		}
		
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
			} else if (this.getTransformer().startsWith(EtlFieldTransformer.FAST_SQL_TRANSFORMER)) {
				this.setTransformerInstance(FastSqlFieldTransformer.getInstance(this.tryToLoadTransformerParameters()));
			} else if (this.getTransformer().startsWith(EtlFieldTransformer.COALESCE_TRANSFORMER)) {
				this.setTransformerInstance(
				    CoalesceFieldTransformer.getInstance(this.tryToLoadTransformerParameters(), dstConf, this));
			} else if (this.getTransformer().startsWith(EtlFieldTransformer.PARENT_ON_DEMAND_TRANSFORMER)) {
				this.setTransformerInstance(
				    ParentOnDemandLoadTransformer.getInstance(this.tryToLoadTransformerParameters(), dstConf, this));
			} else if (this.getTransformer().startsWith(EtlFieldTransformer.OPENMRS_VISIT_ON_DEMAND_TRANSFORMER)) {
				this.setTransformerInstance(
				    OpenMrsVisitOnDemandLoadTransformer.getInstance(this.tryToLoadTransformerParameters(), dstConf, this));
			} else if (this.getTransformer().startsWith(EtlFieldTransformer.OPENMRS_ENCOUNTER_ON_DEMAND_TRANSFORMER)) {
				this.setTransformerInstance(OpenMrsEncounterForObsOnDemandLoadTransformer
				        .getInstance(this.tryToLoadTransformerParameters(), dstConf, this));
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
