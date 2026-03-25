package org.openmrs.module.epts.etl.etl.processor.transformer;

import java.util.List;

import org.openmrs.module.epts.etl.conf.DstConf;
import org.openmrs.module.epts.etl.conf.interfaces.TransformableField;

public interface TransformerFactory {
	
	EtlFieldTransformer create(List<Object> parameters, DstConf relatedDstConf, TransformableField field);
}
