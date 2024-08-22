package org.openmrs.module.epts.etl.conf.interfaces;

import org.openmrs.module.epts.etl.etl.processor.transformer.ArithmeticFieldTransformer;
import org.openmrs.module.epts.etl.etl.processor.transformer.DefaultFieldTransformer;
import org.openmrs.module.epts.etl.etl.processor.transformer.EtlFieldTransformer;
import org.openmrs.module.epts.etl.etl.processor.transformer.EtlRecordTransformer;
import org.openmrs.module.epts.etl.etl.processor.transformer.SimpleValueTransformer;
import org.openmrs.module.epts.etl.etl.processor.transformer.StringTranformer;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;

/**
 * In an ETL a {@link TransformableField} represents a field which can have its value (src-value)
 * transformed
 */
public interface TransformableField {
	
	String getName();
	
	EtlFieldTransformer getTransformerInstance();
	
	void setTransformerInstance(EtlFieldTransformer transformer);
	
	String getTransformer();
	
	String getValueToTransform();
	
	String getDataSourceName();
	
	void setTransformer(String transformer);
	
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
