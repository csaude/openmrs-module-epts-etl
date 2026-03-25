package org.openmrs.module.epts.etl.etl.processor.transformer;

import java.util.List;

import org.openmrs.module.epts.etl.conf.DstConf;
import org.openmrs.module.epts.etl.conf.interfaces.TransformableField;

public abstract class AbstractEtlFieldTransformer implements EtlFieldTransformer {
	
	protected List<Object> parameters;
	
	protected DstConf relatedDstConf;
	
	protected TransformableField field;
	
	public AbstractEtlFieldTransformer(List<Object> parameters, DstConf relatedDstConf, TransformableField field) {
		this.parameters = parameters;
		this.relatedDstConf = relatedDstConf;
		this.field = field;
	}
}
