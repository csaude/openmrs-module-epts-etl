package org.openmrs.module.epts.etl.controller.conf.tablemapping;

import java.sql.Connection;

import org.openmrs.module.epts.etl.conf.Extension;
import org.openmrs.module.epts.etl.etl.processor.transformer.ArithmeticFieldTransformer;
import org.openmrs.module.epts.etl.etl.processor.transformer.DefaultFieldTransformer;
import org.openmrs.module.epts.etl.etl.processor.transformer.EtlFieldTransformer;
import org.openmrs.module.epts.etl.etl.processor.transformer.EtlRecordTransformer;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.utilities.AttDefinedElements;
import org.openmrs.module.epts.etl.utilities.CommonUtilities;

/**
 * This class is used to map fields between any source table and destination table
 * 
 * @author jpboane
 */
public class FieldsMapping {
	
	static CommonUtilities utilities = CommonUtilities.getInstance();
	
	private String srcValue;
	
	private String srcField;
	
	private String dataSourceName;
	
	private String dstField;
	
	private boolean mapToNullValue;
	
	private String transformer;
	
	private EtlFieldTransformer transformerInstance;
	
	private Extension extension;
	
	public FieldsMapping() {
	}
	
	public FieldsMapping(String srcField, String dataSourceName, String destField) {
		this.srcField = srcField;
		this.dataSourceName = dataSourceName;
		this.dstField = destField;
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
	
	public boolean hasTransformer() {
		return getTransformer() != null;
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
	
	public boolean hasTransformerInstance() {
		return this.getTransformerInstance() != null;
	}
	
	public void setMapToNullValue(boolean b) {
		mapToNullValue = b;
	}
	
	public boolean isMapToNullValue() {
		return mapToNullValue;
	}
	
	@SuppressWarnings("unchecked")
	public void tryToLoadTransformer(Connection conn) {
		if (this.hasTransformer()) {
			
			if (this.getTransformer().equals(EtlFieldTransformer.DEFAULT_TRANSFORMER)) {
				this.setTransformer(DefaultFieldTransformer.class.getCanonicalName());
				this.setTransformerInstance(DefaultFieldTransformer.getInstance());
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
		} else {
			this.setTransformer(DefaultFieldTransformer.class.getCanonicalName());
			
			this.setTransformerInstance(DefaultFieldTransformer.getInstance());
		}
	}
	
}
