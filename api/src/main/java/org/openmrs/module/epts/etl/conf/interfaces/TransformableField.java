package org.openmrs.module.epts.etl.conf.interfaces;

import org.openmrs.module.epts.etl.conf.DstConf;
import org.openmrs.module.epts.etl.etl.processor.transformer.ArithmeticFieldTransformer;
import org.openmrs.module.epts.etl.etl.processor.transformer.EtlFieldTransformer;
import org.openmrs.module.epts.etl.etl.processor.transformer.FieldTransformerType;
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
	
	default boolean hasSrcField() {
		return utilities.stringHasValue(this.getSrcField());
	}
	
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
	
	default void tryToLoadTransformer(DstConf dstConf) {
		FieldTransformerType.tryToLoadTransformerToField(this, dstConf);
	}
	
}
